package ir.optimizations;

import java.util.HashSet;

import ir.cfg.BasicBlock;
import ir.cfg.CFG;
import ir.cfg.CFGVisitor;
import ir.tac.TAC;
import ir.tac.Assign;
import ir.tac.Call;
import coco.Symbol;

public class ReassignedSetGenerator implements CFGVisitor {

    private HashSet<CFG> funcCalls = null;
    private HashSet<Symbol> reassignedVars = null;

    public void generate(CFG cfg) {
        reassignedVars = cfg.reassignedVariables();
        funcCalls = new HashSet<CFG>();
        cfg.start().accept(this);
        cfg.resetVisited();

        // For any function calls, add their hashsets
        HashSet<Symbol> oldSet = reassignedVars;
        HashSet<CFG> oldCalls = funcCalls;

        for (CFG funcCFG : oldCalls) {
            generate(funcCFG);
            
            for (Symbol s : funcCFG.reassignedVariables()) {
                oldSet.add(s);
            }
        }

        reassignedVars = null;
    }

    @Override
    public void visit(BasicBlock block) {
        for (TAC tac : block) {
            // Look for any reassignments
            if (tac instanceof Assign) {
                Assign aTac = (Assign) tac;
                Symbol destSym = aTac.destination().symbol();

                // Add only global variables
                if (destSym.isGlobalVariable()) {
                    reassignedVars.add(destSym);
                }
            }
            // If there is a function call, defer for later
            else if (tac instanceof Call) {
                Call cTac = (Call) tac;
                funcCalls.add(cTac.functionCFG());
            }
        }
    }
}
