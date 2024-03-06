package coco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashSet;

import ir.SSA;
import ir.tac.*;
import ir.cfg.*;
import ir.optimizations.ReassignedSetGenerator;
import types.*;

public class CodeGenerator implements TACVisitor, CFGVisitor {

    // Reserved registers
    private final int zeroReg = 0;
    private final int returnReg = 25;
    private final int spillReg1 = 26;
    private final int spillReg2 = 27;
    private final int FP = 28;
    private final int SP = 29;
    private final int GDB = 30;
    private final int BA = 31;

    private Variable[] spillRegs;
    private int usedSpill = 0;

    private HashMap<Symbol, Integer> globalOffsets;
    private HashMap<Symbol, Integer> localOffsets;
    private HashMap<Symbol, Integer> paramOffsets;

    private List<Pair<CFG, TreeMap<Integer, List<Integer>>>> funcBlocks;
    private TreeMap<Integer, List<Integer>> codeBlocks;
    private List<Integer> currInstr;
    private CFG currCFG;

    private HashMap<Variable, Integer> regAllocs;
    private HashSet<Integer> usedRegs;
    private HashSet<Variable> changedGlobals;

    // Used for determining store and load offsets (arrays)
    private Pair<Variable, Value> addressOffset;

    // Maps jump to block to the instruction ID it jumps to
    private HashMap<Integer, Integer> deferredBranch;
    private HashMap<Integer, CFG> deferredJump;
    private HashMap<Integer, CFG> deferredReturn;

    // Reading a variable, spilled variables need to be retrieved
    private int getRegR(Variable v) {
        return getReg(v, true);
    }

    // Writing a variable, spilled variables do not need to be retrieved
    // Spilled variables will be marked as dirty for later writeback
    private int getRegW(Variable v) {
        return getReg(v, false);
    }

    private int getReg(Variable v, boolean read) {
        int reg;

        // Check if the register is not spilled
        if (regAllocs.get(v) != RegisterAllocator.spilled) {
            reg = regAllocs.get(v);

            // // If global, may need to commit later
            // if (v.isGlobal() && !read) {
            //     changedGlobals.add(v);
            // }
        }
        // Otherwise, need to use a spill register
        else { 
            // Alternate between the two
            reg = ((usedSpill++) % 2 == 0) ? spillReg1 : spillReg2;

            // Pull in the new value if needed
            if (read) {
                // Pull from global space
                if (v.isGlobal()) {
                    currInstr.add(DLX.assemble(DLX.LDW, reg, GDB, globalOffsets.get(v.symbol())));
                }
                // Pull from local space
                else if (v.isLocal()) {
                    currInstr.add(DLX.assemble(DLX.LDW, reg, SP, localOffsets.get(v.symbol())));
                }
                // Store in parameter space
                else {
                    // Need to go past saved BA and FP
                    currInstr.add(DLX.assemble(DLX.LDW, reg, FP, paramOffsets.get(v.symbol()) + 8));
                }
            } else {
                // Track the current value of the spill register to write to memory
                spillRegs[reg - spillReg1] = v;
            }
        }

        usedRegs.add(reg);
        return reg;
    }

    private int allocateSize(List<Symbol> vars, HashMap<Symbol, Integer> offsets) {
        int totalSize = 0;
        
        // Allocate
        for (Symbol s : vars) {
            // Track offset from end of memory
            offsets.put(s, totalSize);

            // If array, need to account for all values
            if (s.type() instanceof ArrayType) {
                ArrayType currType = (ArrayType) s.type();
                int arrSize = currType.numElements() * 4;

                while (currType.elementType() instanceof ArrayType) {
                    currType = (ArrayType) currType.elementType();
                    arrSize *= currType.numElements();
                }

                totalSize += arrSize;
            }
            // Otherwise, the variable is 4 bytes
            else {
                totalSize += 4;
            }
        }

        return totalSize;
    }

    public int[] generate(SSA ssa, HashMap<Variable, Integer> regAllocs, List<Symbol> globalSymbols) {
        // Generate reassignment sets for globals
        ReassignedSetGenerator reassignSetGen = new ReassignedSetGenerator();
        for (CFG cfg : ssa) {
            reassignSetGen.generate(cfg);
        }

        // Track what is currently in the spill registers
        spillRegs = new Variable[2];
        spillRegs[0] = null;
        spillRegs[1] = null;
        usedSpill = 0; 

        ArrayList<Integer> instructions = new ArrayList<Integer>();
        changedGlobals = new HashSet<Variable>();
        this.regAllocs = regAllocs;

        // Track variable offsets from GDB
        globalOffsets = new HashMap<Symbol, Integer>();

        // Track variable offsets from SP
        localOffsets = new HashMap<Symbol, Integer>();

        // Track variable offsets from FP
        paramOffsets = new HashMap<Symbol, Integer>();

        // Allocate space for the global variables
        int globalSize = allocateSize(globalSymbols, globalOffsets);

        // Move GDB past global section
        if (globalSize > 0) {
            instructions.add(DLX.assemble(DLX.SUBI, GDB, GDB, globalSize));
        }

        // System.out.println("GLOBAL OFFSETS:");
        // for (Symbol s : globalOffsets.keySet()) {
        //     System.out.println("\t" + s.name() + ": " + globalOffsets.get(s));
        // }

        // Setup SP and FP past the global section
        instructions.add(DLX.assemble(DLX.SUB, FP, GDB, zeroReg));

        // Create space for temporary variables
        List<Symbol> localSymbols = new ArrayList<Symbol>();
        for (Variable v : regAllocs.keySet()) {
            if (v.symbol().name().charAt(0) == '_') {
                localSymbols.add(v.symbol());
            }
        }

        int localSize = allocateSize(localSymbols, localOffsets);
        if (localSize > 0) {
            instructions.add(DLX.assemble(DLX.SUBI, SP, FP, localSize));
        } else {
            instructions.add(DLX.assemble(DLX.ADD, SP, FP, zeroReg));
        }

        // Order blocks of code by their starting line numbers
        funcBlocks = new ArrayList<Pair<CFG, TreeMap<Integer, List<Integer>>>>();

        // Resolve branches, returns, and jumps after final instruction addresses are determined
        deferredBranch = new HashMap<Integer, Integer>();
        deferredReturn = new HashMap<Integer, CFG>();
        deferredJump = new HashMap<Integer, CFG>();

        // Generate code for each CFG
        for (CFG cfg : ssa.CFGs()) {
            currCFG = cfg;
            changedGlobals.clear();

            // Convert each block into assembly, then sort them based on relative order
            codeBlocks = new TreeMap<Integer, List<Integer>>();
            funcBlocks.add(new Pair<>(cfg, codeBlocks));

            usedRegs = new HashSet<Integer>();

            // Create function prologues
            if (cfg.function() != SymbolTable.mainSymbol) {
                localOffsets.clear();
                List<Integer> prologue = new ArrayList<Integer>();
                codeBlocks.put(Integer.MIN_VALUE, prologue);

                // Save BA and old FP; setup FP and SP
                prologue.add(DLX.assemble(DLX.PSH, BA, SP, -4));
                prologue.add(DLX.assemble(DLX.PSH, FP, SP, -4));
                prologue.add(DLX.assemble(DLX.ADD, FP, zeroReg, SP));

                // Allocate space for local variables and temporaries
                localSymbols.clear();
                localOffsets.clear();
                for (Symbol s : cfg.localVariables()) {
                    localSymbols.add(s);
                }
                for (Variable v : regAllocs.keySet()) {
                    if (v.symbol().name().charAt(0) == '_') {
                        localSymbols.add(v.symbol());
                    }
                }
                localSize = allocateSize(localSymbols, localOffsets);
                if (localSize > 0) {
                    prologue.add(DLX.assemble(DLX.SUBI, SP, SP, localSize));
                }
            
                // System.out.println("LOCAL OFFSETS FOR " + cfg.function() + ":");
                // for (Symbol s : localOffsets.keySet()) {
                //     System.out.println("\t" + s.name() + ": " + localOffsets.get(s));
                // }

                // Setup offsets for parameters
                paramOffsets.clear();
                int offset = 0;
                for (int i = cfg.parameters().size() - 1; i >= 0; i--) {
                    paramOffsets.put(cfg.parameters().get(i), offset);
                    offset += 4;
                }

                // System.out.println("PARAM OFFSETS FOR " + cfg.function() + ":");
                // for (Symbol s : paramOffsets.keySet()) {
                //     System.out.println("\t" + s.name() + ": " + paramOffsets.get(s));
                // }

                // Pull in params
                offset = 8;
                for (int i = cfg.parameters().size() - 1; i >= 0; i--) {
                    Variable pVar = new Variable(cfg.parameters().get(i));
                    prologue.add(DLX.assemble(DLX.LDW, getRegR(pVar), FP, offset));
                    offset += 4;
                }

                // // Pull in live globals (that are not spilled)
                // for (Symbol s : cfg.start().getEntryLiveSet()) {
                //     Variable tempVar = new Variable(s);
                //     if (regAllocs.get(tempVar) != RegisterAllocator.spilled) {
                //         prologue.add(DLX.assemble(DLX.LDW, regAllocs.get(tempVar), GDB, globalOffsets.get(s)));
                //     }
                // }
            }

            // Convert the CFG to assembly
            cfg.start().accept(this);

            // Create function epilogues
            if (cfg.function() != SymbolTable.mainSymbol) {
                List<Integer> epilogue = new ArrayList<Integer>();
                codeBlocks.put(Integer.MAX_VALUE, epilogue);

                // Reverse of prologue
                epilogue.add(DLX.assemble(DLX.ADD, SP, FP, zeroReg));
                epilogue.add(DLX.assemble(DLX.POP, FP, SP, 4));
                epilogue.add(DLX.assemble(DLX.POP, BA, SP, 4 * cfg.parameters().size() + 4));

                // Any globals changed in the function will need to be written to memory
                for (Symbol s : cfg.reassignedVariables()) {
                    // System.out.println("REASSIGNS " + s + " IN " + cfg.function());
                    Variable tempVar = new Variable(s);
                    if (regAllocs.get(tempVar) != RegisterAllocator.spilled) {
                        epilogue.add(DLX.assemble(DLX.STW, regAllocs.get(tempVar), GDB, globalOffsets.get(s)));
                    }
                }

                // Return to the caller
                epilogue.add(DLX.assemble(DLX.RET, BA));
            } else {
                // Main just needs an epilogue
                List<Integer> epilogue = new ArrayList<Integer>();
                codeBlocks.put(Integer.MAX_VALUE, epilogue);

                // End program
                epilogue.add(DLX.assemble(DLX.RET, 0));
            } 
        }

        // Track where each function's epilogue starts
        HashMap<CFG, Integer> epilogueStart = new HashMap<CFG, Integer>();

        // Track where each instruction ends up
        HashMap<Integer, Integer> instrMap = new HashMap<Integer, Integer>();
        HashMap<CFG, Integer> firstInstr = new HashMap<CFG, Integer>();

        // Assemble the blocks for each function in the correct order
        // Main should be first, which comes last in the list
        boolean first;
        List<CFG> cfgList = ssa.CFGs();
        for (int i = funcBlocks.size() - 1; i >= 0; i--) {
            first = true;
            
            // Go through each functions blocks in relative order
            for (Integer key: funcBlocks.get(i).second.keySet()) {
                List<Integer> blockInstr = funcBlocks.get(i).second.get(key);

                if (first) {
                    firstInstr.put(funcBlocks.get(i).first, instructions.size());
                    first = false;
                }

                if (key.equals(Integer.MAX_VALUE)) {
                    // Get rid of any useless branches to epilogue
                    if ((instructions.get(instructions.size() - 1) >>> 26) == DLX.BEQ) {
                        instructions.remove(instructions.size() - 1);
                    }
                    epilogueStart.put(funcBlocks.get(i).first, instructions.size());
                }

                instrMap.put(key, instructions.size());
                instructions.addAll(blockInstr);
            }
        }

        for (int i = 0; i < instructions.size(); i++) {
            // Resolve branch addresses
            if (deferredBranch.containsKey(instructions.get(i))) { 
                // Get the instruction number
                int j = instrMap.get(deferredBranch.get(instructions.get(i)));
                int offset = (j - i) & 0xFFFF;
                instructions.set(i, (instructions.get(i) & ~0xFFFF) | offset);
            }
            // Resolve jump addresses
            else if (deferredJump.containsKey(instructions.get(i))) {
                // Get the first instrution for the CFG
                CFG jumpDest = deferredJump.get(instructions.get(i));
                int destAddr = (firstInstr.get(jumpDest) * 4) & 0x3FFFFFF;
                instructions.set(i, (instructions.get(i) & 0xFC000000) | destAddr);
            }
            // Resolve return addresses
            else if (deferredReturn.containsKey(instructions.get(i))) {
                // Get the first instruction of the epilogue
                CFG currFunc = deferredReturn.get(instructions.get(i));

                int offset = (epilogueStart.get(currFunc) - i) & 0xFFFF;
                instructions.set(i, (instructions.get(i) & 0xFC000000) | offset);
            }
        }

        return instructions.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public void visit(BasicBlock block) {
        // Create new code block
        int firstInstr = block.getInstructions().get(0).getID();
        codeBlocks.put(firstInstr, new ArrayList<Integer>());
        currInstr = codeBlocks.get(firstInstr);

        // Generate code for each TAC
        for (TAC tac : block) {
            if (!tac.isEliminated()) {
                tac.accept(this);

                // If spill registers were written, store the values in memory
                if (spillRegs[0] != null) {
                    Variable dest = spillRegs[0];

                    // Store in global space
                    if (dest.isGlobal()) {
                        currInstr.add(DLX.assemble(DLX.STW, spillReg1, GDB, globalOffsets.get(dest.symbol())));
                    }
                    // Store in local space
                    else if (dest.isLocal()) {
                        currInstr.add(DLX.assemble(DLX.STW, spillReg1, SP, localOffsets.get(dest.symbol())));
                    }
                    // Store in parameter space
                    else if (dest.isGlobal()) {
                        currInstr.add(DLX.assemble(DLX.STW, spillReg1, FP, paramOffsets.get(dest.symbol())));
                    }

                    spillRegs[0] = null;
                }
                if (spillRegs[1] != null) {
                    Variable dest = spillRegs[1];

                    // Store in global space
                    if (dest.isGlobal()) {
                        currInstr.add(DLX.assemble(DLX.STW, spillReg2, GDB, globalOffsets.get(dest.symbol())));
                    }
                    // Store in local space
                    else if (dest.isLocal()) { 
                        currInstr.add(DLX.assemble(DLX.STW, spillReg2, SP, localOffsets.get(dest.symbol())));
                    }
                    // Store in parameter space
                    else if (dest.isGlobal()) {
                        currInstr.add(DLX.assemble(DLX.STW, spillReg2, FP, paramOffsets.get(dest.symbol())));
                    }

                    spillRegs[1] = null;
                }
            }
        }

        // Visit all successors
        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }
    }

    @Override
    public void visit(Add instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ADD, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else if (instr.leftOperand() instanceof Literal) {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                // Can just swap because it's commutative
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), getRegR(rVar), lVal.value()));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        } else if (instr.leftOperand() instanceof Gdb) {
            // Always add variable to GDB
            Variable rOp = (Variable) instr.rightOperand();
            currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), GDB, globalOffsets.get(rOp.symbol())));
        } else if (instr.leftOperand() instanceof Sp) {
            // Always add variable to GDB
            Variable rOp = (Variable) instr.rightOperand();
            currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), SP, localOffsets.get(rOp.symbol())));
        }
    }

    @Override
    public void visit(Adda instr) {
        // Left is always a variable, since it is the offset from base
        Variable lVar = (Variable) instr.leftOperand();

        // Right may be a literal if the offset from start was folded
        if (instr.rightOperand() instanceof Variable) {
            Variable rVar = (Variable) instr.rightOperand();
            addressOffset = new Pair<Variable, Value>(lVar, rVar);
        } else {
            Literal rVal = (Literal) instr.rightOperand();
            addressOffset = new Pair<Variable, Value>(lVar, rVal);
        }
    }

    @Override
    public void visit(And instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.AND, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ANDI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                // Can just swap because it's commutative
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ANDI, getRegW(dest), getRegR(rVar), lVal.value()));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.ANDI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Cmp instr) {
        Variable dest = instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lOp = (Variable) instr.leftOperand();
            // CMP
            if (instr.rightOperand() instanceof Variable) {
                Variable rOp = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.CMP, getRegW(dest), getRegR(lOp), getRegR(rOp)));
            } 
            // CMPI 
            else {
                Literal rOp = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.CMPI, getRegW(dest), getRegR(lOp), rOp.value()));
            }
        } else {
            Literal lOp = (Literal) instr.leftOperand();
            // CMPI
            if (instr.rightOperand() instanceof Variable) {
                Variable rOp = (Variable) instr.rightOperand();                
                currInstr.add(DLX.assemble(DLX.CMPI, getRegW(dest), getRegR(rOp), lOp.value()));

                // Need to invert sign because operands were flipped
                currInstr.add(DLX.assemble(DLX.SUB, getRegW(dest), zeroReg, getRegR(dest)));
            } else {
                Literal rOp = (Literal) instr.rightOperand();

                // Load the left literal into the results register, since it is expected to be used
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lOp.value()));
                currInstr.add(DLX.assemble(DLX.CMPI, getRegW(dest), getRegR(dest), rOp.value()));
            }
        }
    }

    @Override
    public void visit(Div instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.DIV, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.DIVI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.DIV, getRegW(dest), returnReg, getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.DIVI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Lsh instr) {
        Variable dest = instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lOp = (Variable) instr.leftOperand();
            // Right operand is always const
            Literal rOp = (Literal) instr.rightOperand();
            currInstr.add(DLX.assemble(DLX.LSHI, getRegW(dest), getRegR(lOp), rOp.value()));
        } else {
            Literal lOp = (Literal) instr.leftOperand();
            Literal rOp = (Literal) instr.rightOperand();

            // Load the left literal into the results register, since it is expected to be used
            currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lOp.value()));
            currInstr.add(DLX.assemble(DLX.LSHI, getRegW(dest), getRegR(dest), rOp.value()));
        }
    }

    @Override
    public void visit(Mod instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.MOD, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.MODI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.MOD, getRegW(dest), returnReg, getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.MODI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Move instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable source = (Variable) instr.leftOperand();
            currInstr.add(DLX.assemble(DLX.ADD, getRegW(dest), zeroReg, getRegR(source)));
        } else {
            Literal source = (Literal) instr.leftOperand();
            currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, source.value()));
        }
    }

    @Override
    public void visit(Mul instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.MUL, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.MULI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                // Can just swap because it's commutative
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.MULI, getRegW(dest), getRegR(rVar), lVal.value()));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.MULI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Or instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.OR, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ORI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                // Can just swap because it's commutative
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ORI, getRegW(dest), getRegR(rVar), lVal.value()));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.ORI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Pow instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.POW, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.POWI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.POW, getRegW(dest), returnReg, getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.POWI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Sub instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.SUB, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.SUBI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.SUBI, getRegW(dest), getRegR(rVar), lVal.value()));

                // Negate the result
                currInstr.add(DLX.assemble(DLX.SUB, getRegW(dest), zeroReg, getRegR(dest)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.SUBI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Xor instr) {
        Variable dest = (Variable) instr.destination();

        if (instr.leftOperand() instanceof Variable) {
            Variable lVar = (Variable) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.XOR, getRegW(dest), getRegR(lVar), getRegR(rVar)));
            } else {
                Literal rVal = (Literal) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.XORI, getRegW(dest), getRegR(lVar), rVal.value()));
            }
        } else {
            Literal lVal = (Literal) instr.leftOperand();

            if (instr.rightOperand() instanceof Variable) {
                // Can just swap because it's commutative
                Variable rVar = (Variable) instr.rightOperand();
                currInstr.add(DLX.assemble(DLX.XORI, getRegW(dest), getRegR(rVar), lVal.value()));
            } else {
                Literal rVal = (Literal) instr.rightOperand();

                // Load left literal into destination register
                currInstr.add(DLX.assemble(DLX.ADDI, getRegW(dest), zeroReg, lVal.value()));
                currInstr.add(DLX.assemble(DLX.XORI, getRegW(dest), getRegR(dest), rVal.value()));
            }
        }
    }

    @Override
    public void visit(Beq instr) {
        int code;
        List<TAC> destBlockInstr = instr.jumpDestination().getInstructions();

        if (!destBlockInstr.isEmpty()) {
            int destInstr = destBlockInstr.get(0).getID();

            if (instr.comparison() instanceof Variable) {
                Variable comp = (Variable) instr.comparison();
                code = DLX.assemble(DLX.BEQ, getRegR(comp), destInstr);
            } else {
                Literal comp = (Literal) instr.comparison();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, comp.value()));
                code = DLX.assemble(DLX.BEQ, returnReg, destInstr);
            }

            currInstr.add(code);
            deferredBranch.put(code, destInstr);
        } else {
            // TODO: Is this possible?
            System.err.println("Empty basic block #" + instr.jumpDestination().blockNumber());
        }
    }

    @Override
    public void visit(Bge instr) {
        int code;
        List<TAC> destBlockInstr = instr.jumpDestination().getInstructions();

        if (!destBlockInstr.isEmpty()) {
            int destInstr = destBlockInstr.get(0).getID();

            if (instr.comparison() instanceof Variable) {
                Variable comp = (Variable) instr.comparison();
                code = DLX.assemble(DLX.BGE, getRegR(comp), destInstr);
            } else {
                Literal comp = (Literal) instr.comparison();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, comp.value()));
                code = DLX.assemble(DLX.BGE, returnReg, destInstr);
            }

            currInstr.add(code);
            deferredBranch.put(code, destInstr);
        } else {
            // TODO: Is this possible?
            System.err.println("Empty basic block #" + instr.jumpDestination().blockNumber());
        }
    }

    @Override
    public void visit(Bgt instr) {
        int code;
        List<TAC> destBlockInstr = instr.jumpDestination().getInstructions();

        if (!destBlockInstr.isEmpty()) {
            int destInstr = destBlockInstr.get(0).getID();

            if (instr.comparison() instanceof Variable) {
                Variable comp = (Variable) instr.comparison();
                code = DLX.assemble(DLX.BGT, getRegR(comp), destInstr);
            } else {
                Literal comp = (Literal) instr.comparison();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, comp.value()));
                code = DLX.assemble(DLX.BGT, returnReg, destInstr);
            }

            currInstr.add(code);
            deferredBranch.put(code, destInstr);
        } else {
            // TODO: Is this possible?
            System.err.println("Empty basic block #" + instr.jumpDestination().blockNumber());
        }
    }

    @Override
    public void visit(Ble instr) {
        int code;
        List<TAC> destBlockInstr = instr.jumpDestination().getInstructions();

        if (!destBlockInstr.isEmpty()) {
            int destInstr = destBlockInstr.get(0).getID();

            if (instr.comparison() instanceof Variable) {
                Variable comp = (Variable) instr.comparison();
                code = DLX.assemble(DLX.BLE, getRegR(comp), destInstr);
            } else {
                Literal comp = (Literal) instr.comparison();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, comp.value()));
                code = DLX.assemble(DLX.BLE, returnReg, destInstr);
            }

            currInstr.add(code);
            deferredBranch.put(code, destInstr);
        } else {
            // TODO: Is this possible?
            System.err.println("Empty basic block #" + instr.jumpDestination().blockNumber());
        }
    }

    @Override
    public void visit(Blt instr) {
        int code;
        List<TAC> destBlockInstr = instr.jumpDestination().getInstructions();

        if (!destBlockInstr.isEmpty()) {
            int destInstr = destBlockInstr.get(0).getID();

            if (instr.comparison() instanceof Variable) {
                Variable comp = (Variable) instr.comparison();
                code = DLX.assemble(DLX.BLT, getRegR(comp), destInstr);
            } else {
                Literal comp = (Literal) instr.comparison();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, comp.value()));
                code = DLX.assemble(DLX.BLT, returnReg, destInstr);
            }

            currInstr.add(code);
            deferredBranch.put(code, destInstr);
        } else {
            // TODO: Is this possible?
            System.err.println("Empty basic block #" + instr.jumpDestination().blockNumber());
        }
    }

    @Override
    public void visit(Bne instr) {
        int code;
        List<TAC> destBlockInstr = instr.jumpDestination().getInstructions();

        if (!destBlockInstr.isEmpty()) {
            int destInstr = destBlockInstr.get(0).getID();

            if (instr.comparison() instanceof Variable) {
                Variable comp = (Variable) instr.comparison();
                code = DLX.assemble(DLX.BNE, getRegR(comp), destInstr);
            } else {
                Literal comp = (Literal) instr.comparison();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, comp.value()));
                code = DLX.assemble(DLX.BNE, returnReg, destInstr);
            }

            currInstr.add(code);
            deferredBranch.put(code, destInstr);
        } else {
            // TODO: Is this possible?
            System.err.println("Empty basic block #" + instr.jumpDestination().blockNumber());
        }
    }

    @Override
    public void visit(Bra instr) {
        int code;
        List<TAC> destBlockInstr = instr.jumpDestination().getInstructions();

        if (!destBlockInstr.isEmpty()) {
            int destInstr = destBlockInstr.get(0).getID();
            code = DLX.assemble(DLX.BEQ, zeroReg, destInstr);
            currInstr.add(code);
            deferredBranch.put(code, destBlockInstr.get(0).getID());
        } else {
            // TODO: Is this possible?
            System.err.println("Empty basic block #" + instr.jumpDestination().blockNumber());
        }
    }

    @Override
    public void visit(Call instr) {
        List<Integer> savedRegs = new ArrayList<Integer>();

        // Push registers
        for (int i = 1; i <= 27; i++) {
            if (usedRegs.contains(i)) {
                savedRegs.add(i);
                currInstr.add(DLX.assemble(DLX.PSH, i, SP, -4));
            }
        }

        // // Store globals
        // for (Variable v : changedGlobals) {
        //     currInstr.add(DLX.assemble(DLX.STW))
        // }

        // Push parameters
        for (Value p : instr.arguments()) {
            // If variable, get from register
            if (p instanceof Variable) {
                Variable pVar = (Variable) p;
                currInstr.add(DLX.assemble(DLX.PSH, getRegR(pVar), SP, -4));
            }
            // If literal, need to load into register
            else {
                Literal pVal = (Literal) p;
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, pVal.value()));
                currInstr.add(DLX.assemble(DLX.PSH, returnReg, SP, -4));
            }
        }

        int block = instr.functionCFG().start().blockNumber();

        // Jump to target function
        int code = DLX.assemble(DLX.JSR, block);
        currInstr.add(code);
        deferredJump.put(code, instr.functionCFG());

        // Unwind the saved registers (only if they weren't changed by function)
        for (int j = savedRegs.size() - 1; j >= 0; j--) {
            currInstr.add(DLX.assemble(DLX.POP, savedRegs.get(j), SP, 4));
        }

        // Any globals changed in the function will need to be loaded back
        for (Symbol s : instr.functionCFG().reassignedVariables()) {
            Variable tempVar = new Variable(s);
            if (regAllocs.get(tempVar) != RegisterAllocator.spilled) {
                currInstr.add(DLX.assemble(DLX.LDW, regAllocs.get(tempVar), GDB, globalOffsets.get(s)));
            }
        }

        // Get return value if needed
        Variable dest = instr.destination();
        if (dest != null) {
            currInstr.add(DLX.assemble(DLX.ADD, getRegW(dest), returnReg, zeroReg));
        }
    }

    @Override
    public void visit(Return instr) {
        if (instr.hasReturnValue()) {
            // Save return value in register
            if (instr.returnValue() instanceof Variable) {
                Variable retVar = (Variable) instr.returnValue();
                currInstr.add(DLX.assemble(DLX.ADD, returnReg, zeroReg, getRegR(retVar)));
            } else {
                Literal retVal = (Literal) instr.returnValue();
                currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, retVal.value()));
            }
        }

        // Jump to epilogue (only if necessary)
        int code = DLX.assemble(DLX.BEQ, zeroReg, instr.getID());
        currInstr.add(code);
        deferredReturn.put(code, currCFG);
    }

    @Override
    public void visit(Load instr) {
        Variable dest = instr.destination();
        Variable base = addressOffset.first;

        if (addressOffset.second instanceof Variable) {
            Variable offset = (Variable) addressOffset.second;
            currInstr.add(DLX.assemble(DLX.LDX, getRegW(dest), getRegR(base), getRegR(offset)));
        } else {
            Literal offset = (Literal) addressOffset.second;
            currInstr.add(DLX.assemble(DLX.LDW, getRegW(dest), getRegR(base), offset.value()));
        }
    }

    @Override
    public void visit(Store instr) {
        Variable base = (Variable) addressOffset.first;

        if (instr.value() instanceof Variable) {
            Variable val = (Variable) instr.value();

            if (addressOffset.second instanceof Variable) {
                Variable offset = (Variable) addressOffset.second;
                currInstr.add(DLX.assemble(DLX.STX, getRegR(val), getRegR(base), getRegR(offset)));
            } else {
                Literal offset = (Literal) addressOffset.second; 
                currInstr.add(DLX.assemble(DLX.STW, getRegR(val), getRegR(base), offset.value()));
            }
        } else {
            Literal val = (Literal) instr.value();

            // Load value into register
            currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, val.value()));

            if (addressOffset.second instanceof Variable) {
                Variable offset = (Variable) addressOffset.second;
                currInstr.add(DLX.assemble(DLX.STX, returnReg, getRegR(base), getRegR(offset)));
            } else {
                Literal offset = (Literal) addressOffset.second; 
                currInstr.add(DLX.assemble(DLX.STW, returnReg, getRegR(base), offset.value()));
            }
        }
    }

    @Override
    public void visit(Read instr) {
        currInstr.add(DLX.assemble(DLX.RDI, getRegW(instr.destination())));
    }

    @Override
    public void visit(ReadB instr) {
        currInstr.add(DLX.assemble(DLX.RDB, getRegW(instr.destination())));
    }

    @Override
    public void visit(Write instr) {
        if (instr.argument() instanceof Variable) {
            Variable arg = (Variable) instr.argument();
            currInstr.add(DLX.assemble(DLX.WRI, getRegR(arg)));
        } else {
            Literal arg = (Literal) instr.argument();
            currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, arg.value()));
            currInstr.add(DLX.assemble(DLX.WRI, returnReg));
        }
    }

    @Override
    public void visit(WriteB instr) {
        if (instr.argument() instanceof Variable) {
            Variable arg = (Variable) instr.argument();
            currInstr.add(DLX.assemble(DLX.WRB, getRegR(arg)));
        } else {
            Literal arg = (Literal) instr.argument();
            currInstr.add(DLX.assemble(DLX.ADDI, returnReg, zeroReg, arg.value()));
            currInstr.add(DLX.assemble(DLX.WRB, returnReg));
        }
    }

    @Override
    public void visit(WriteNL instr) {
        currInstr.add(DLX.assemble(DLX.WRL));
    }
}
