package ir;

import java.util.List;
import java.util.Iterator;
import ir.cfg.CFG;
import ir.cfg.CFGPrinter;

public class SSA implements Iterable<CFG> {

    private List<CFG> funcs;
    private Integer lastBlockNum;

    public SSA(List<CFG> funcs) {
        this.funcs = funcs;
    }

    public SSA(List<CFG> funcs, int lastBlockNum) {
        this.funcs = funcs;
        this.lastBlockNum = lastBlockNum;
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

    public List<CFG> CFGs() {
        return funcs;
    }

    public CFG mainCFG() {
        if (funcs.size() != 0) {
            return funcs.get(funcs.size() - 1);
        }

        return null;
    }

    public int lastBlockNumber() {
        return lastBlockNum;
    }

    public void setLastBlockNumber(int num) {
        lastBlockNum = num;
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
