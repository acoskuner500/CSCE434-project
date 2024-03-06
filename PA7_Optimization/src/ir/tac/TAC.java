package ir.tac;

public abstract class TAC implements Visitable{
    
    private int id; // instruction id

    private boolean eliminated; 

    protected TAC(int id) {
        this.id = id;
        this.eliminated = false;

        // saving code position will be helpful in debugging
    } 
    
    protected TAC(TAC other) {
       this.id = other.id;
       this.eliminated = other.eliminated; 
    }

    public int getID() {
        return id;
    }

    public void eliminate() {
        eliminated = true;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public abstract TAC clone();
}
