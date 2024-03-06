package ir;

import java.util.List;
import java.util.Iterator;
import ir.cfg.CFG;
import ir.cfg.BasicBlock;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ast.*;
import ir.tac.*;
import coco.Symbol;
import coco.SymbolTable;
import types.ArrayType;
import types.FuncType;
import types.VoidType;
import ir.tac.Jump.JumpType;

// Traverse the AST - generate a CFG for each function
public class IRGenerator implements ast.NodeVisitor, Iterable<CFG>{
 
    private CFG currCFG;
    private List<CFG> funcs = new ArrayList<CFG>();
    private BasicBlock currBlock = null;
    private Value prevVal = null;
    private int tempCount = 0;
    private int currInstr = 0;
    private int currBlockNum = 1;
    private String relOp = null;
    private Value dataSegBase = null;
    private boolean isArray = false;
    private LinkedList<Integer> arrDims = new LinkedList<Integer>();
    private Symbol arrSym = null;
    private ValueList args = null;
    private boolean noIRGen = false;
    private boolean needBool = false;
    private List<Call> deferredCalls = new ArrayList<Call>();
    private HashMap<Integer, Symbol> tempSyms = new HashMap<Integer, Symbol>();

    public static final Gdb GDB = new Gdb();
    public static final Sp SP = new Sp();

    private Symbol getTempSym() {
        if (!tempSyms.containsKey(tempCount)) {
            tempSyms.put(tempCount, new Symbol("_t" + tempCount, null));
        }

        return tempSyms.get(tempCount);
    }

    public List<CFG> functions() {
        return funcs;
    }

    public SSA generateIR(AST ast) {
        ast.getRoot().accept(this);
        SSA savedSSA = new SSA(funcs, currBlockNum);
        return savedSSA;
    }

    @Override
    public void visit (BoolLiteral node) {
        prevVal = new Literal(node);
        relOp = null;
    }

    @Override
    public void visit (IntegerLiteral node) {
        prevVal = new Literal(node);
    }

    @Override
    public void visit (FloatLiteral node) {
        prevVal = new Literal(node);
    }

    @Override
    public void visit (AddressOf node) {
        // If array, need to do indexing
        if (node.hasIndex()) {
            isArray = true;
            // Get instructions for indexing
            node.arrayIndex().accept(this);
            Value offset = prevVal;

            if (offset instanceof Temporary) {
                tempCount++;
            }

            Variable result = new Temporary(getTempSym(), currInstr);
            Variable start = new Variable(arrSym);
            currBlock.add(new Add(currInstr++, result, dataSegBase, start));

            if (offset instanceof Temporary) {
                tempCount--;
            }

            Variable result2 = new Temporary(getTempSym(), currInstr);
            currBlock.add(new Adda(currInstr++, result2, result, offset));
            prevVal = result2;
        } else {
            prevVal = new Variable(node.identifier());
        }
    }

    @Override
    public void visit (ArrayIndex node) {
        Value idx;

        // Get array dimensions from symbol
        if (node.hasSymbol()) {
            arrSym = node.symbol();
            ArrayType arrType = (ArrayType) arrSym.type();

            // Determine what segment the array is in
            dataSegBase = arrSym.isGlobalVariable() ? GDB : SP;

            while (arrType.elementType() instanceof ArrayType) {
                arrType = (ArrayType) arrType.elementType();
                arrDims.add(arrType.numElements());
            }

            // Primitive types have width of 4
            arrDims.add(4);

            node.indexValue().accept(this);
            idx = prevVal;
        } else {
            node.arrayIndex().accept(this);
            Value prevResult = prevVal;

            node.indexValue().accept(this);
            idx = prevVal;

            // Add offset
            Variable result = new Temporary(getTempSym(), currInstr);
            currBlock.add(new Add(currInstr++, result, prevResult, idx));
            idx = result;
        }

        // Multiply index by width
        Literal width = new Literal(new IntegerLiteral(0, 0, arrDims.get(0)));
        arrDims.remove(0);
        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Mul(currInstr++, result, idx, width));
        prevVal = result;
    }

    @Override
    public void visit (Dereference node) {
        // If array, need to do indexing
        if (node.hasIndex()) {
            // Get instructions for indexing
            node.arrayIndex().accept(this);
            Value offset = prevVal;

            if (offset instanceof Temporary) {
                tempCount++;
            }

            Variable result = new Temporary(getTempSym(), currInstr);
            Variable start = new Variable(arrSym);
            currBlock.add(new Add(currInstr++, result, dataSegBase, start));

            if (offset instanceof Temporary) {
                tempCount--;
            }

            Variable result2 = new Temporary(getTempSym(), currInstr);
            currBlock.add(new Adda(currInstr++, result2, result, offset));

            Variable result3 = new Temporary(getTempSym(), currInstr);
            currBlock.add(new Load(currInstr++, result3, result2));
            isArray = false;
            prevVal = result3;
        } else {
            prevVal = new Variable(node.identifier());
        }
    }

    @Override
    public void visit (LogicalNot node) {
        needBool = true;
        node.operand().accept(this);
        Value val = prevVal;

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Xor(currInstr++, result, val, new Literal(new BoolLiteral(0, 0, true))));
        prevVal = result;
        relOp = null;
        needBool = false;
    }

    @Override
    public void visit (Power node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Pow(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Multiplication node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Mul(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Division node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        
        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Div(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Modulo node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Mod(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (LogicalAnd node) {
        needBool = true;
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        relOp = null;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        needBool = true;
        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new And(currInstr++, result, leftVal, rightVal));
        prevVal = result;
        relOp = null;
        needBool = false;
    }

    @Override
    public void visit (Addition node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Add(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Subtraction node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Sub(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (LogicalOr node) {
        needBool = true;
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        needBool = true;
        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Or(currInstr++, result, leftVal, rightVal));
        prevVal = result;
        relOp = null;
        needBool = false;
    }

    @Override
    public void visit (Relation node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount++;
        }

        node.rightOperand().accept(this);
        Value rightVal = prevVal;

        if (leftVal instanceof Temporary) {
            tempCount--;
        }

        Variable result = new Temporary(getTempSym(), currInstr);
        currBlock.add(new Cmp(currInstr++, result, leftVal, rightVal));
        if (needBool) {
            Variable dest = new Temporary(getTempSym(), currInstr);
            String op = node.operator();
            Literal one = new Literal(new IntegerLiteral(0, 0, 1));
            Literal two = new Literal(new IntegerLiteral(0, 0, 2));
            Literal negThreeOne = new Literal(new IntegerLiteral(0, 0, -31));
            Literal negOne = new Literal(new IntegerLiteral(0, 0, -1));

            if (Arrays.asList("==", "!=").contains(op)) {
                // c = AND c 1
                currBlock.add(new And(currInstr++, dest, result, one));
                result = dest;
                dest = new Temporary(getTempSym(), currInstr);

                // c = XOR c 1
                if (op.equals("==")) {
                    currBlock.add(new Xor(currInstr++, dest, result, one));
                }
            } else if (Arrays.asList("<=", "<").contains(op)) {
                // c = SUB c 1
                if (op.equals("<=")) {
                    currBlock.add(new Sub(currInstr++, dest, result, one));
                    result = dest;
                    dest = new Temporary(getTempSym(), currInstr);
                }

                // c = LSH c -31
                currBlock.add(new Lsh(currInstr++, dest, result, negThreeOne));
            } else if (Arrays.asList(">=", ">").contains(op)) {
                if (op.equals(">=")) {
                    // c = ADD c 2
                    currBlock.add(new Add(currInstr++, dest, result, two));
                } else {
                    // c = ADD c 1
                    currBlock.add(new Add(currInstr++, dest, result, one));
                }

                result = dest;
                dest = new Temporary(getTempSym(), currInstr);

                // c = LSH c -1
                currBlock.add(new Lsh(currInstr++, dest, result, negOne));
            }
        }
        
        prevVal = result;
        relOp = node.operator();
    }

    @Override
    public void visit (Assignment node) {
        needBool = true;
        node.source().accept(this);
        needBool = false;
        Value sourceVal = prevVal;

        if (sourceVal instanceof Temporary) {
            tempCount++;
        }

        node.destination().accept(this);
        Variable destVal = (Variable) prevVal;

        if (sourceVal instanceof Temporary) {
            tempCount--;
        }

        // If array, need to store at address
        if (isArray) {
            currBlock.add(new Store(currInstr++, sourceVal, destVal));
            isArray = false;
        } 
        // Otherwise just move into variable
        else {
            List<TAC> instr = currBlock.getInstructions();

            // Don't need to move the result of an assign instruction
            if (instr.size() > 0 && (instr.get(instr.size() - 1) instanceof Assign) && sourceVal instanceof Temporary) {
                Assign aTac = (Assign) instr.get(instr.size() - 1);
                aTac.setDestination(destVal);
            } 
            // Or a call 
            else if (instr.size() > 0 && (instr.get(instr.size() - 1) instanceof Call) && sourceVal instanceof Temporary) {
                Call cTac = (Call) instr.get(instr.size() - 1);
                cTac.setDestination(destVal);
            } 
            // Or a read
            else if (instr.size() > 0 && (instr.get(instr.size() - 1) instanceof Input) && sourceVal instanceof Temporary) {
                Input iTac = (Input) instr.get(instr.size() - 1);
                iTac.setDestination(destVal);
            } 
            // Or a load
            else if (instr.size() > 0 && instr.get(instr.size() - 1) instanceof Load) {
                Load lTac = (Load) instr.get(instr.size() - 1);
                lTac.setDestination(destVal);
            } else {
                currBlock.add(new Move(currInstr++, destVal, sourceVal));
            }
        }
    }

    @Override
    public void visit (ArgumentList node) {
        // Generate TAC for each argument computation
        int origTemp = tempCount;
        List<Value> argList = new ArrayList<Value>();

        for (Expression e : node) {
            e.accept(this); 
            argList.add(prevVal);

            if (prevVal instanceof Temporary) {
                tempCount++;
            }
        }

        tempCount = origTemp;
        args = new ValueList(argList);
    }

    @Override
    public void visit (FunctionCall node) {
        // After type-checking, symbol should be resolved
        Symbol s = node.symbols().get(0);
        
        // Check if pre-defined
        if (s == SymbolTable.readIntSymbol) {
            Variable result = new Temporary(getTempSym(), currInstr);
            currBlock.add(new Read(currInstr++, result));
            prevVal = result;
        } else if (s == SymbolTable.readBoolSymbol) {
            Variable result = new Temporary(getTempSym(), currInstr);
            currBlock.add(new ReadB(currInstr++, result));
            prevVal = result;
        } else if (s == SymbolTable.printIntSymbol) {
            Value arg = null;

            for (Expression e : node.arguments()) {
                e.accept(this);
                arg = prevVal;
            }

            currBlock.add(new Write(currInstr++, arg));
        } else if (s == SymbolTable.printBoolSymbol) {
            Value arg = null;

            for (Expression e : node.arguments()) {
                e.accept(this);
                arg = prevVal;
            }

            currBlock.add(new WriteB(currInstr++, arg));
        } else if (s == SymbolTable.printlnSymbol) {
            currBlock.add(new WriteNL(currInstr++));
        } else {
            // Get arguments as ValueList
            node.arguments().accept(this);
            FuncType fType = (FuncType) s.type();
            Temporary temp = null;
            
            if (!(fType.returnType() instanceof VoidType)) {
                temp = new Temporary(getTempSym(), currInstr);
                prevVal = temp;
            }

            // Defer resolving the call to the end for func CFG
            Call cTac = new Call(currInstr++, s, args, null, temp);
            currBlock.add(cTac);
            deferredCalls.add(cTac);
        }
    }

    @Override
    public void visit (IfStatement node) {
        // Add comparison
        node.condition().accept(this);
        Value comp = prevVal;
        BasicBlock compBlock = currBlock;

        // Make basic block for ELSE
        BasicBlock elseBlock = new BasicBlock(currBlockNum++);

        // Make basic block for THEN
        BasicBlock thenBlock = new BasicBlock(currBlockNum++);
        compBlock.addSuccessor(thenBlock, "then");  // label = "then"
        thenBlock.addPredecessor(compBlock);
        compBlock.addSuccessor(elseBlock, "else");  // label = "else"
        elseBlock.addPredecessor(compBlock);

        JumpType jType = node.hasElse() ? JumpType.IF_ELSE: JumpType.IF_THEN;

        // Figure out how to compare
        if (relOp == null || relOp.equals("!=")) {
            compBlock.add(new Beq(currInstr++, comp, elseBlock, jType));
        } else if (relOp.equals("==")) {
            compBlock.add(new Bne(currInstr++, comp, elseBlock, jType));
        } else if (relOp.equals("<")) {
            compBlock.add(new Bge(currInstr++, comp, elseBlock, jType));
        } else if (relOp.equals("<=")) {
            compBlock.add(new Bgt(currInstr++, comp, elseBlock, jType));
        } else if (relOp.equals(">")) {
            compBlock.add(new Ble(currInstr++, comp, elseBlock, jType));
        } else if (relOp.equals(">=")) {
            compBlock.add(new Blt(currInstr++, comp, elseBlock, jType));
        }

        relOp = null;

        // Build THEN block
        currBlock = thenBlock;
        node.thenStatements().accept(this);

        // Build ELSE block (if there)
        if (node.hasElse()) {
            boolean ifReturn = noIRGen;
            BasicBlock afterBlock = new BasicBlock(currBlockNum++);

            // If then has return, no need to connect to after statement
            if (!noIRGen) {
                // Add jump from then to after
                currBlock.add(new Bra(currInstr++, afterBlock));

                // Connect end of then to after
                currBlock.addSuccessor(afterBlock);
                afterBlock.addPredecessor(currBlock);
            }

            currBlock = elseBlock;
            noIRGen = false;
            node.elseStatements().accept(this);
            boolean elseReturn = noIRGen;

            if (!noIRGen) {
                // Connect end of else to after
                currBlock.addSuccessor(afterBlock);
                afterBlock.addPredecessor(currBlock);
                currBlock = afterBlock;
            }

            // If both cases return, no more IR for branch
            noIRGen = ifReturn && elseReturn;
        } else {
            // Connect end of then block to else
            if (!noIRGen) {
                currBlock.addSuccessor(elseBlock);
                elseBlock.addPredecessor(currBlock);
            }

            currBlock = elseBlock;
            noIRGen = false;
        }
    }

    @Override
    public void visit (WhileStatement node) {
        // Create block for comparison check
        BasicBlock compBlock;

        if (currBlock.getInstructions().size() > 0) {
            compBlock = new BasicBlock(currBlockNum++);
            currBlock.addSuccessor(compBlock);
            compBlock.addPredecessor(currBlock);
            currBlock = compBlock;
        } else {
            compBlock = currBlock;
        }

        node.condition().accept(this);
        Variable comp = (Variable) prevVal;

        // Make basic block for THEN
        BasicBlock thenBlock = new BasicBlock(currBlockNum++);
        compBlock.addSuccessor(thenBlock, "then"); // label="then"
        thenBlock.addPredecessor(compBlock);

        // Make basic block for ELSE
        BasicBlock elseBlock = new BasicBlock(currBlockNum++);

        // Figure out how to compare
        if (relOp == null || relOp.equals("!=")) {
            compBlock.add(new Beq(currInstr++, comp, elseBlock, JumpType.WHILE));
        } else if (relOp.equals("==")) {
            compBlock.add(new Bne(currInstr++, comp, elseBlock, JumpType.WHILE));
        } else if (relOp.equals("<")) {
            compBlock.add(new Bge(currInstr++, comp, elseBlock, JumpType.WHILE));
        } else if (relOp.equals("<=")) {
            compBlock.add(new Bgt(currInstr++, comp, elseBlock, JumpType.WHILE));
        } else if (relOp.equals(">")) {
            compBlock.add(new Ble(currInstr++, comp, elseBlock, JumpType.WHILE));
        } else if (relOp.equals(">=")) {
            compBlock.add(new Blt(currInstr++, comp, elseBlock, JumpType.WHILE));
        }

        relOp = null;

        // Build THEN block
        currBlock = thenBlock;
        node.doStatements().accept(this);
        
        // Add jump from then to comparison (if necessary)
        compBlock.addSuccessor(elseBlock, "else"); // label="else"
        elseBlock.addPredecessor(compBlock);

        if (!noIRGen) {
            currBlock.add(new Bra(currInstr++, compBlock));
            currBlock.addSuccessor(compBlock, "", "ne");
            compBlock.addPredecessor(currBlock);
        }

        // Switch to else for following statements
        currBlock = elseBlock;
    }

    @Override
    public void visit (RepeatStatement node) {
        // Make block for repeat statements (if necessary)
        BasicBlock repeatBlock;
        if (currBlock.getInstructions().isEmpty()) {
            repeatBlock = currBlock;
        } else {
            repeatBlock = new BasicBlock(currBlockNum++);
            currBlock.addSuccessor(repeatBlock);
            repeatBlock.addPredecessor(currBlock);
            currBlock = repeatBlock;
        }

        BasicBlock elseBlock = new BasicBlock(currBlockNum++); 
        node.repeatStatements().accept(this);
        if (!noIRGen) {
            node.condition().accept(this);
            currBlock.addSuccessor(repeatBlock, "then", "ne");
            repeatBlock.addPredecessor(currBlock);
            currBlock.addSuccessor(elseBlock, "else");
            elseBlock.addPredecessor(currBlock);
            Variable comp = (Variable) prevVal;

            // Figure out how to compare
            if (relOp == null || relOp.equals("!=")) {
                currBlock.add(new Beq(currInstr++, comp, repeatBlock, JumpType.REPEAT));
            } else if (relOp.equals("==")) {
                currBlock.add(new Bne(currInstr++, comp, repeatBlock, JumpType.REPEAT));
            } else if (relOp.equals("<")) {
                currBlock.add(new Bge(currInstr++, comp, repeatBlock, JumpType.REPEAT));
            } else if (relOp.equals("<=")) {
                currBlock.add(new Bgt(currInstr++, comp, repeatBlock, JumpType.REPEAT));
            } else if (relOp.equals(">")) {
                currBlock.add(new Ble(currInstr++, comp, repeatBlock, JumpType.REPEAT));
            } else if (relOp.equals(">=")) {
                currBlock.add(new Blt(currInstr++, comp, repeatBlock, JumpType.REPEAT));
            }

            relOp = null;
            currBlock = elseBlock;
        }
    }

    @Override
    public void visit (ReturnStatement node) {
        if (node.hasReturn()) {
            node.returnValue().accept(this);
            Value returnedVal = prevVal;
            currBlock.add(new Return(currInstr++, returnedVal));
        } else {
            currBlock.add(new Return(currInstr++));
        }

        noIRGen = true;
    }

    @Override
    public void visit (StatementSequence node) {
        // Generate TAC for each statement
        for (Statement s : node) {
            if (!noIRGen) {
                s.accept(this);
            }
        }
    }

    @Override
    public void visit (VariableDeclaration node) {
        // Do nothing I think? No TAC for declarations
        currCFG.addLocal(node.symbol());
    }

    @Override
    public void visit (FunctionBody node) {
        // Get local variables
        if (node.hasVariables()) {
            node.variables().accept(this);
        }

        node.functionStatementSequence().accept(this);

        // If void function, add an obligatory return statement
        if (((FuncType) currCFG.function().type()).returnType() instanceof VoidType) {
            // Only if there isn't one already
            List<TAC> inst = currBlock.getInstructions();
            if (inst.size() == 0 || !(inst.get(inst.size() - 1) instanceof Return)) {
                currBlock.add(new Return(currInstr++));
            }
        }
        noIRGen = false;
    }

    @Override
    public void visit (FunctionDeclaration node) {
        // Get function symbol
        Symbol funcSymbol = node.symbol();

        // Make CFG for this function
        currCFG = new CFG(currBlockNum++, funcSymbol, node.parameters());
        currBlock = currCFG.start();
        funcs.add(currCFG);

        // Make TAC for function
        node.body().accept(this);
    }

    @Override
    public void visit (DeclarationList node) {
        for (Declaration d : node) {
            d.accept(this);
        }
    }

    @Override
    public void visit (Computation node) {
        node.functions().accept(this);

        // Make basic block for main function
        currCFG = new CFG(currBlockNum++, node.main());
        funcs.add(currCFG);
        currBlock = currCFG.start();
        node.mainStatementSequence().accept(this);

        // Add an end of progam return to the end of main
        // Only if there isn't one already
        List<TAC> inst = currBlock.getInstructions();
        if (inst.size() == 0 || !(inst.get(inst.size() - 1) instanceof Return)) {
            currBlock.add(new Return(currInstr++));
        }

        // Resolve deferred calls to the appropriate function CFG
        for (Call deferCall : deferredCalls) {
            // Find associated function block
            for (CFG f_cfg : funcs) {
                if (deferCall.function() == f_cfg.function()) {
                    deferCall.setFunctionCFG(f_cfg);
                }
            }
        }
    }

    public Iterator<CFG> iterator() {
        return funcs.iterator();
    }
}
