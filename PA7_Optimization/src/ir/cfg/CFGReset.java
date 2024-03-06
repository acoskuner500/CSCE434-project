package ir.cfg;

import java.util.HashMap;
import java.util.HashSet;

import coco.Symbol;
import ir.tac.Value;

public class CFGReset implements CFGVisitor {

    private boolean resetValue = false;
    private boolean resetLive = false;

    public void reset(CFG cfg) {
        cfg.start().accept(this, true);
    }

    public void resetValueSet(CFG cfg) {
        resetValue = true;
        cfg.start().accept(this, true);
        resetValue = false;
    }

    public void resetLiveSet(CFG cfg) {
        resetLive = true;
        cfg.start().accept(this, true);
        resetLive = false;
    }

    @Override
    public void visit(BasicBlock block) {
        if (resetValue) {
            HashMap<Symbol, Value> entrySet = block.getEntryValueSet();
            HashMap<Symbol, Value> exitSet = block.getExitValueSet();
            entrySet.clear();
            entrySet.put(BasicBlock.unitializedSymbol, null);
            exitSet.clear();
            exitSet.put(BasicBlock.unitializedSymbol, null);
        }

        if (resetLive) {
            HashSet<Symbol> entrySet = block.getEntryLiveSet();
            HashSet<Symbol> exitSet = block.getExitLiveSet();
            entrySet.clear();
            entrySet.add(BasicBlock.unitializedSymbol);
            exitSet.clear();
            exitSet.add(BasicBlock.unitializedSymbol);
        }

        block.resetVisited();

        for (Successor s: block.getSuccessors()) {
            s.destination().accept(this, true);
        }
    }

}
