package ir.cfg;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import ir.tac.TAC;

public class BasicBlock extends Block implements Iterable<TAC> {

    private int num; // block number;
    private List<TAC> instructions;
    private String funcName;

    private List<BasicBlock> predecessors;
    private List<Successor> successors;

    public BasicBlock(int num) {
        this.num = num;
        this.funcName = null;
        instructions = new ArrayList<TAC>();
        predecessors = new ArrayList<BasicBlock>();
        successors = new ArrayList<Successor>();
    }
    
    public BasicBlock(int num, String funcName) {
        this.num = num;
        this.funcName = funcName;
        instructions = new ArrayList<TAC>();
        predecessors = new ArrayList<BasicBlock>();
        successors = new ArrayList<Successor>();
    }

    public int blockNumber() {
        return num;
    }

    public String functionName() {
        return funcName;
    }

    public List<BasicBlock> getPredessors() {
        return predecessors;
    }

    public List<Successor> getSuccessors() {
        return successors;
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