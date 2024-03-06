package coco;

import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import ir.SSA;
import ir.cfg.*;
import ir.tac.*;
import types.ArrayType;
import ir.optimizations.LiveVarsGenerator;

public class RegisterAllocator implements CFGVisitor {

    public static final int spilled = -1;

    private HashMap<Variable, HashSet<Variable>> interferenceGraph;
    private HashMap<Variable, Integer> regAllocs;
    private boolean removeSilly;
   
    private void genInterference(SSA ssa) {
        interferenceGraph = new HashMap<Variable, HashSet<Variable>>();
        LiveVarsGenerator lvg = new LiveVarsGenerator();

        // Generate exit live sets
        for (CFG cfg : ssa) {
            cfg.resetLiveSet();
            List<Symbol> vars = new ArrayList<Symbol>();
            vars.addAll(cfg.reassignedVariables());
            lvg.generate(cfg, vars, true);
        }

        // Create the interference graph
        for (CFG cfg: ssa) {
            // Add all parameters to the interference graph
            for (Symbol s : cfg.parameters()) {
                interferenceGraph.put(new Variable(s), new HashSet<Variable>());
            }
            cfg.start().accept(this);
            cfg.resetVisited();
        }

        // System.out.println("INTERFERENCE GRAPH:");
        // for (Map.Entry<Variable, HashSet<Variable>> e : interferenceGraph.entrySet()) {
        //     String out = "\t" + e.getKey() + " -> ";

        //     for (Variable v : e.getValue()) {
        //         out += v + " ";
        //     }
        //     System.out.println(out);
        // }
    }

    private void colorGraph(int numRegs) {
        // If no graph, nothing to color
        if (interferenceGraph.isEmpty()) {
            return;
        }

        // Color nodes with Chaitin Algorithm
        // 1. Find a node with less than k edges
        Variable toRemove = null;
        for (Map.Entry<Variable, HashSet<Variable>> e : interferenceGraph.entrySet()) {
            if (e.getValue().size() < numRegs) {
                toRemove = e.getKey();
                break;
            }
        }
        
        // If no such node is found, find one and mark as problematic
        // Heuristic: pick the variable with the most edges
        if (toRemove == null) {
            int maxEdges = -1;
            for (Map.Entry<Variable, HashSet<Variable>> e : interferenceGraph.entrySet()) {
                if (e.getValue().size() > maxEdges) {
                    toRemove = e.getKey();
                    maxEdges = e.getValue().size();
                }
            }
        }

        // System.out.println("PICKED: " + toRemove);

        // 2. Remove it from the graph (add to stack)
        HashSet<Variable> removedNeighbors = interferenceGraph.get(toRemove);
        interferenceGraph.remove(toRemove);
        for (Map.Entry<Variable, HashSet<Variable>> e : interferenceGraph.entrySet()) {
            // Disconnect from rest of nodes
            e.getValue().remove(toRemove);
        }

        // 3. Recursively color the rest of the graph
        if (!interferenceGraph.isEmpty()) {
            colorGraph(numRegs);
        }

        // 4. Add the node back in (might not need to explicitly do)

        // 5. Assign a valid color
        // Get the colors of the neighbors
        List<Boolean> usedReg = new ArrayList<Boolean>(Collections.nCopies(numRegs, false));
        for (Variable v : removedNeighbors) {
            // Spilled variables don't count
            if (regAllocs.get(v) > 0) {
                usedReg.set(regAllocs.get(v) - 1, true);
            }
        }

        // Find a color not used by neighbors
        int assignedReg = spilled;
        for (int i = 1; i <= numRegs; i++) {
            if (!usedReg.get(i - 1)) {
                assignedReg = i;
                break;
            }
        }

        // Assign the color (-1 is a spilled variable)
        regAllocs.put(toRemove, assignedReg);
    }

    public HashMap<Variable, Integer> allocateRegisters(SSA ssa, int numRegs) {
        regAllocs = new HashMap<Variable, Integer>();
        removeSilly = false;

        // Create interference graph
        genInterference(ssa);

        // Color nodes with Chaitin Algorithm
        regAllocs = new HashMap<Variable, Integer>();
        colorGraph(numRegs);

        // System.out.println("REGISTER ALLOCATION:");
        // for (Map.Entry<Variable, Integer> e : regAllocs.entrySet()) {
        //     if (e.getValue() == -1) {
        //         System.out.println("\t" + e.getKey() + " -> spilled");
        //     } else {
        //         System.out.println("\t" + e.getKey() + " -> R" + e.getValue());
        //     }
        // }

        // Remove silly moves
        removeSilly = true;
        for (CFG cfg : ssa) {
            cfg.start().accept(this);
            cfg.resetVisited();
        }

        return regAllocs;
    }

    @Override
    public void visit(BasicBlock block) {
        // Remove silly moves
        if (removeSilly) {
            for (TAC tac : block) {
                if (!tac.isEliminated() && tac instanceof Move) {
                    Move mTac = (Move) tac;

                    // If destination and source have the same register, then the instruction is not needed
                    // TODO: May need to not remove if both are spilled
                    if (regAllocs.get(mTac.destination()) == spilled) {
                        continue;
                    }

                    if (regAllocs.get(mTac.destination()) == regAllocs.get(mTac.leftOperand())) {
                        tac.eliminate();
                    }
                }
            }

            for (Successor s : block.getSuccessors()) {
                s.destination().accept(this);
            }
            
            return;
        }

        // Evaluate leaf nodes first
        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }

        // Get live vars starting at exit
        HashSet<Symbol> currLive = new HashSet<Symbol>();
        currLive.addAll(block.getExitLiveSet());
        List<TAC> blockInstr = block.getInstructions();

        // Iterate backwards through block instructions
        for (int i = blockInstr.size() - 1; i >= 0; i--) {
            // Check which variables are live at the same time
            Symbol[] liveSet = new Symbol[currLive.size()];
            currLive.toArray(liveSet);

            // Need to look at last element in case it never gets added
            for (int first = 0; first < currLive.size(); first++) {
                Variable firstVar = new Variable(liveSet[first]);

                // If new need to create new set
                if (!interferenceGraph.containsKey(firstVar)) {
                    interferenceGraph.put(firstVar, new HashSet<Variable>());
                }

                for (int second = first + 1; second < currLive.size(); second++) {
                    Variable secondVar = new Variable(liveSet[second]);

                    // If new, need to create new set
                    if (!interferenceGraph.containsKey(secondVar)) {
                        interferenceGraph.put(secondVar, new HashSet<Variable>());
                    }

                    interferenceGraph.get(firstVar).add(secondVar);
                    interferenceGraph.get(secondVar).add(firstVar);
                }
            }

            TAC tac = blockInstr.get(i);

            // Don't look at already eliminated instructions
            if (tac.isEliminated()) {
                continue;
            }

            Variable dest;

            if (tac instanceof Assign) {
                Assign aTac = (Assign) tac;

                // If reassigned it is no longer live
                currLive.remove(aTac.destination().symbol());

                // Add destination to graph, even if never live
                dest = new Variable(aTac.destination().symbol());
                if (!interferenceGraph.containsKey(dest)) {
                    interferenceGraph.put(dest, new HashSet<Variable>());
                }

                // If assigning to a variable, it can't share a register with live variables
                for (Symbol s : currLive) {
                    Variable otherVar = new Variable(s);

                    if (!interferenceGraph.containsKey(otherVar)) {
                        interferenceGraph.put(otherVar, new HashSet<Variable>());
                    }

                    interferenceGraph.get(dest).add(otherVar);
                    interferenceGraph.get(otherVar).add(dest);
                }

                // Any used variables are live
                if (aTac.leftOperand() instanceof Variable) {
                    Variable lVar = (Variable) aTac.leftOperand();
                    currLive.add(lVar.symbol());
                }
                if (!(aTac instanceof Move) && aTac.rightOperand() instanceof Variable) {
                    Variable rVar = (Variable) aTac.rightOperand();

                    // Don't count arrays since they live in memory
                    if (!(rVar.symbol().type() instanceof ArrayType)) {
                        currLive.add(rVar.symbol());
                    }
                }
            } else if (tac instanceof Jump) {
                Jump jTac = (Jump) tac;

                // If used in comparison, is live
                if (jTac.comparison() instanceof Variable) {
                    Variable compVar = (Variable) jTac.comparison();
                    currLive.add(compVar.symbol());
                }
            } else if (tac instanceof Call) {
                Call cTac = (Call) tac;
                List<Value> args = cTac.arguments().values();

                if (cTac.destination() != null) {
                    currLive.remove(cTac.destination().symbol());
                }

                for (Value arg : args) {
                    // If used as arg, is live
                    if (arg instanceof Variable) {
                        Variable argVar = (Variable) arg;
                        currLive.add(argVar.symbol());
                    }
                }

                // Add destination to graph, even if never live
                if (cTac.destination() != null) {
                    dest = new Variable(cTac.destination().symbol());
                    if (!interferenceGraph.containsKey(dest)) {
                        interferenceGraph.put(dest, new HashSet<Variable>());
                    }
                }
            } else if (tac instanceof Return) {
                Return rTac = (Return) tac;

                // If returned, is live
                if (rTac.returnValue() instanceof Variable) {
                    Variable retVar = (Variable) rTac.returnValue();
                    currLive.add(retVar.symbol());
                }
            } else if (tac instanceof Store) {
                Store sTac = (Store) tac;

                // If stored, is live
                if (sTac.value() instanceof Variable) {
                    Variable storeVar = (Variable) sTac.value();
                    currLive.add(storeVar.symbol());
                }

                // Location is also live
                currLive.add(sTac.location().symbol());
            } else if (tac instanceof Print) {
                Print pTac = (Print) tac;

                // If printed, is live
                if (pTac.argument() instanceof Variable) {
                    Variable argVar = (Variable) pTac.argument();
                    currLive.add(argVar.symbol());
                }
            } else if (tac instanceof Load) {
                Load lTac = (Load) tac;

                // Add destination to graph, even if never live
                dest = new Variable(lTac.destination().symbol());
                if (!interferenceGraph.containsKey(dest)) {
                    interferenceGraph.put(dest, new HashSet<Variable>());
                }
            } else if (tac instanceof Input) {
                Input iTac = (Input) tac;

                currLive.remove(iTac.destination().symbol());
            }
        } 

        // Check which variables are live at the same time
        Symbol[] liveSet = new Symbol[currLive.size()];
        currLive.toArray(liveSet);

        // Need to look at last element in case it never gets added
        for (int first = 0; first < currLive.size(); first++) {
            Variable firstVar = new Variable(liveSet[first]);

            // If new need to create new set
            if (!interferenceGraph.containsKey(firstVar)) {
                interferenceGraph.put(firstVar, new HashSet<Variable>());
            }

            for (int second = first + 1; second < currLive.size(); second++) {
                Variable secondVar = new Variable(liveSet[second]);

                // If new, need to create new set
                if (!interferenceGraph.containsKey(secondVar)) {
                    interferenceGraph.put(secondVar, new HashSet<Variable>());
                }

                interferenceGraph.get(firstVar).add(secondVar);
                interferenceGraph.get(secondVar).add(firstVar);
            }
        }
    }
}
