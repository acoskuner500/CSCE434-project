package ir.cfg;

import java.util.HashMap;
import java.util.Map;
import ir.tac.TAC;
import ir.tac.Call;

// To print basic block in Dot language
public class CFGPrinter implements CFGVisitor {

    String outStr = "";
    int count = 0;

    public String printDotGraph(BasicBlock start) {
        start.accept(this);
        String ret = outStr;
        outStr = "";
        return ret;
    }
    
    @Override
    public void visit(BasicBlock block) {
        HashMap<String, BasicBlock> callLabels = new HashMap<String, BasicBlock>();

        // Need to add function name and labels
        outStr += "BB" + block.blockNumber() + "[shape=record, label=\"<b>" + (block.functionName() == null ? "" : block.functionName() + "\\n") + "BB" + block.blockNumber() + "|{";
        for (TAC tac : block) {
            if (tac instanceof Call) {
                Call c = (Call) tac;
                outStr += "<" + c.function().name() + "_" + tac.getID() + ">";
                if (c.functionCFG() != null) {
                    callLabels.put(c.function().name() + "_" + tac.getID(), c.functionCFG().start());
                }
            }

            outStr += tac + " |";
        }

        if (outStr.charAt(outStr.length() - 1) == '|') {
            outStr = outStr.substring(0, outStr.length() - 1);
        }

        outStr += "}\"];\n";

        // Get labels (need to actually add the text)
        for (Successor s : block.getSuccessors()) {
            BasicBlock dest = s.destination();
            outStr += "BB" + (block.blockNumber() >= 0 ? block.blockNumber() : "_" + block.blockNumber() * -1) + ":s -> BB" + dest.blockNumber() + ":" + s.arrowType() + " [label=\"" + s.label() + "\"];\n";
        }
        // Visit successors
        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }

        // Create the labels for function calls
        for (Map.Entry<String, BasicBlock> entry : callLabels.entrySet()) {
            outStr += "BB" + block.blockNumber() + ":" + entry.getKey() + " -> " + "BB" + entry.getValue().blockNumber() + ":b [color=red];\n";
        }
    }
}
