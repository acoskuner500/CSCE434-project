package ir.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import coco.Symbol;
import ir.tac.TAC;

public class CFG {

    private BasicBlock startBlock;
    private HashMap<Integer, BasicBlock> blocks;
    private Symbol func;
    private CFGReset reset = new CFGReset();
    private HashSet<Symbol> reassignedVars;
    private List<Symbol> locals;
    private List<Symbol> params;

    public CFG(int startNum, Symbol func) {
        startBlock = new BasicBlock(startNum, func.name());
        blocks = new HashMap<Integer, BasicBlock>();
        blocks.put(startNum, startBlock);
        this.func = func;
        reassignedVars = new HashSet<Symbol>();
        this.locals = new ArrayList<Symbol>();
        this.params = new ArrayList<Symbol>();
    }

    public CFG(int startNum, Symbol func, List<Symbol> params) {
        startBlock = new BasicBlock(startNum, func.name());
        blocks = new HashMap<Integer, BasicBlock>();
        blocks.put(startNum, startBlock);
        this.func = func;
        reassignedVars = new HashSet<Symbol>();
        this.locals = new ArrayList<Symbol>();
        this.params = params;
    }

    public Symbol function() {
        return func;
    }
   
    public void add(int blockNum, TAC instr) {
        blocks.get(blockNum).add(instr);
    }

    public void addLocal(Symbol s) {
        locals.add(s);
    }

    public BasicBlock start() {
        return startBlock;
    }

    public HashSet<Symbol> reassignedVariables() {
        return reassignedVars;
    }

    public List<Symbol> localVariables() {
        return locals;
    }

    public List<Symbol> parameters() {
        return params;
    }

    public void setStart(BasicBlock bb) {
        startBlock = bb;
    }

    public void resetVisited() {
        reset.visit(startBlock);
    }

    public void resetValueSet() {
        reset.resetValueSet(this);
    }

    public void resetLiveSet() {
        reset.resetLiveSet(this);
    }
}
