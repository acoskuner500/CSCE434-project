package ir.tac;

public interface TACVisitor {
    
    public void visit(Add instr);
    public void visit(Adda instr);
    public void visit(And instr);
    public void visit(Cmp instr);
    public void visit(Div instr);
    public void visit(Lsh instr);
    public void visit(Mod instr);
    public void visit(Move instr);
    public void visit(Mul instr);
    public void visit(Or instr);
    public void visit(Pow instr);
    public void visit(Sub instr);
    public void visit(Xor instr);

    public void visit(Beq instr);
    public void visit(Bge instr);
    public void visit(Bgt instr);
    public void visit(Ble instr);
    public void visit(Blt instr);
    public void visit(Bne instr);

    public void visit(Bra instr);
    public void visit(Call instr);
    public void visit(Return instr);

    public void visit(Load instr);
    public void visit(Store instr);

    public void visit(Read instr);
    public void visit(ReadB instr);

    public void visit(Write instr);
    public void visit(WriteB instr);
    public void visit(WriteNL instr);

}
