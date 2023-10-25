package ir;

import java.util.List;
import java.util.Iterator;
import ir.cfg.CFG;
import ir.cfg.CFGPrinter;

public class SSA implements Iterable<CFG> {

    // TODO: Make this fit all possible IRs
    private List<CFG> funcs;

    public SSA(List<CFG> funcs) {
        this.funcs = funcs;
    }

    public String asDotGraph() {
        String dotgraph_text = "digraph G {\n";
        CFGPrinter cfgPrint = new CFGPrinter();

        for (CFG cfg : funcs) {
            dotgraph_text += cfgPrint.printDotGraph(cfg.start());
        }

        dotgraph_text += "}";
        return dotgraph_text;
    }

    public Iterator<CFG> iterator() {
        return funcs.iterator();
    }

    public void resetVisited() {
        for (CFG cfg : funcs) {
            cfg.resetVisited();
        }
    }
}
