package ir.cfg;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ir.tac.TAC;
import ir.tac.Value;
import coco.Symbol;

public class BasicBlock extends Block implements Iterable<TAC> {

    private int num; // block number;
    private List<TAC> instructions;
    private String funcName;

    private List<BasicBlock> predecessors;
    private List<Successor> successors;

    // For optimizations
    private HashMap<Symbol, Value> entryValueSet;
    private HashMap<Symbol, Value> exitValueSet;
    private HashSet<Symbol> exitLiveVars;  // at exit of bblock
    private HashSet<Symbol> entryLiveVars; // at entry of bblock

    public static final Symbol unitializedSymbol = new Symbol("__uninitialized__", null);

    public BasicBlock(int num) {
        this.num = num;
        this.funcName = null;
        instructions = new ArrayList<TAC>();
        predecessors = new ArrayList<BasicBlock>();
        successors = new ArrayList<Successor>();
        entryValueSet = new HashMap<Symbol, Value>();
        exitValueSet = new HashMap<Symbol, Value>();
        entryValueSet.put(unitializedSymbol, null);
        exitValueSet.put(unitializedSymbol, null);
        exitLiveVars = new HashSet<Symbol>();
        entryLiveVars = new HashSet<Symbol>();
        exitLiveVars.add(unitializedSymbol);
        entryLiveVars.add(unitializedSymbol);
    }
    
    public BasicBlock(int num, String funcName) {
        this.num = num;
        this.funcName = funcName;
        instructions = new ArrayList<TAC>();
        predecessors = new ArrayList<BasicBlock>();
        successors = new ArrayList<Successor>();
        entryValueSet = new HashMap<Symbol, Value>();
        exitValueSet = new HashMap<Symbol, Value>();
        entryValueSet.put(unitializedSymbol, null);
        exitValueSet.put(unitializedSymbol, null);
        exitLiveVars = new HashSet<Symbol>();
        entryLiveVars = new HashSet<Symbol>();
        exitLiveVars.add(unitializedSymbol);
        entryLiveVars.add(unitializedSymbol);
    }

    public int blockNumber() {
        return num;
    }

    public List<TAC> getInstructions() {
        return instructions;
    }

    public String functionName() {
        return funcName;
    }

    public List<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public List<Successor> getSuccessors() {
        return successors;
    }

    public HashMap<Symbol, Value> getEntryValueSet() {
        return entryValueSet;
    }

    public HashMap<Symbol, Value> getExitValueSet() {
        return exitValueSet;
    }
    
    public HashSet<Symbol> getExitLiveSet() {
        return exitLiveVars;
    }

    public HashSet<Symbol> getEntryLiveSet() {
        return entryLiveVars;
    }

    public void add(TAC instr) {
        instructions.add(instr);
    }

    public void addPredecessor(BasicBlock prev) {
        predecessors.add(prev);
    }
    
    public void addSuccessor(BasicBlock next) {
        successors.add(new Successor(next));
    }

    public void addSuccessor(BasicBlock next, String label) {
        successors.add(new Successor(next, label));
    }

    public void addSuccessor(BasicBlock next, String label, String arrowType) {
        successors.add(new Successor(next, label, arrowType));
    }

    public void setFunction(String name) {
        funcName = name;
    }

    @Override
    public Iterator<TAC> iterator() {
        return instructions.iterator();
    }

    @Override
    public void accept(CFGVisitor visitor) {
        if(!visited) {
            visited = true;
            visitor.visit(this);
        }
    }

    public void accept(CFGVisitor visitor, boolean reset) {
        if (reset && visited) {
            visited = false;
            visitor.visit(this);
        } else if (!reset && visited) {
            visited = true;
            visitor.visit(this);
        }
    }

    @Override
    public void resetVisited() {
        visited = false;
    }
}