package ir.cfg;

import ir.tac.TAC;
import java.util.HashMap;
import coco.Symbol;

public class CFG {

    private BasicBlock startBlock;
    private HashMap<Integer, BasicBlock> blocks;
    private Symbol func;

    public CFG(int startNum, Symbol func) {
        startBlock = new BasicBlock(startNum, func.name());
        blocks = new HashMap<Integer, BasicBlock>();
        blocks.put(startNum, startBlock);
        this.func = func;
    }

    public Symbol function() {
        return func;
    }
   
    public void add(int blockNum, TAC instr) {
        blocks.get(blockNum).add(instr);
    }

    public BasicBlock start() {
        return startBlock;
    }

    public void resetVisited() {
        CFGReset r = new CFGReset();
        r.visit(startBlock);
    }
}
