package ir;

import java.util.List;
import java.util.Iterator;
import ir.cfg.CFG;
import ir.cfg.BasicBlock;
import java.util.LinkedList;
import java.util.ArrayList;
import ast.*;
import ir.tac.*;
import coco.Symbol;
import coco.SymbolTable;
import types.ArrayType;

// Traverse the AST - generate a CFG for each function
public class IRGenerator implements ast.NodeVisitor, Iterable<CFG>{
 
    private CFG currCFG;
    private List<CFG> funcs = new ArrayList<CFG>();
    private BasicBlock currBlock = null;
    private Value prevVal = null;
    private int tempCount = 0;
    private int currInstr = 0;
    private int currBlockNum = 1;
    private String relOp = null;
    private String dataSegBase = null;
    private boolean isArray = false;
    private LinkedList<Integer> arrDims = new LinkedList<Integer>();
    private Symbol arrSym = null;
    private ValueList args = null;

    public List<CFG> functions() {
        return funcs;
    }

    @Override
    public void visit (BoolLiteral node) {
        prevVal = new Literal(node);
        relOp = null;
    }

    @Override
    public void visit (IntegerLiteral node) {
        prevVal = new Literal(node);
    }

    @Override
    public void visit (FloatLiteral node) {
        prevVal = new Literal(node);
    }

    @Override
    public void visit (AddressOf node) {
        // If array, need to do indexing
        if (node.hasIndex()) {
            isArray = true;
            // Get instructions for indexing
            node.arrayIndex().accept(this);
            Value offset = prevVal;

            Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            Variable base = new Variable(new Symbol(dataSegBase, null));
            Variable start = new Variable(arrSym);
            currBlock.add(new Add(currInstr++, result, base, start));

            Variable result2 = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            currBlock.add(new Adda(currInstr++, result2, result, offset));
            prevVal = result2;
        } else {
            prevVal = new Variable(node.identifier());
        }
    }

    @Override
    public void visit (ArrayIndex node) {
        Value idx;
        
        // Get array dimensions from symbol
        if (node.hasSymbol()) {
            arrSym = node.symbol();
            ArrayType arrType = (ArrayType) arrSym.type();

            while (arrType.elementType() instanceof ArrayType) {
                arrType = (ArrayType) arrType.elementType();
                arrDims.add(arrType.numElements());
            }

            // Primitive types have width of 4
            arrDims.add(4);

            node.indexValue().accept(this);
            idx = prevVal;
        } else {
            node.arrayIndex().accept(this);
            Value prevResult = prevVal;

            node.indexValue().accept(this);
            idx = prevVal;

            // Add offset
            Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            currBlock.add(new Add(currInstr++, result, prevResult, idx));
            idx = result;
        }

        // Multiply index by width
        Literal width = new Literal(new IntegerLiteral(0, 0, arrDims.get(0)));
        arrDims.remove(0);
        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Mul(currInstr++, result, idx, width));
        prevVal = result;
    }

    @Override
    public void visit (Dereference node) {
        // If array, need to do indexing
        if (node.hasIndex()) {
            // Get instructions for indexing
            node.arrayIndex().accept(this);
            Value offset = prevVal;

            Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            Variable base = new Variable(new Symbol(dataSegBase, null));
            Variable start = new Variable(arrSym);
            currBlock.add(new Add(currInstr++, result, base, start));

            Variable result2 = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            currBlock.add(new Adda(currInstr++, result2, result, offset));

            Variable result3 = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            currBlock.add(new Load(currInstr++, result3, result2));
            isArray = false;
            prevVal = result3;
        } else {
            prevVal = new Variable(node.identifier());
        }
    }

    @Override
    public void visit (LogicalNot node) {
        node.operand().accept(this);
        Value val = prevVal;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Not(currInstr++, result, val));
        prevVal = result;
        relOp = null;
    }

    @Override
    public void visit (Power node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Pow(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Multiplication node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Mul(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Division node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Div(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Modulo node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Mod(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (LogicalAnd node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new And(currInstr++, result, leftVal, rightVal));
        prevVal = result;
        relOp = null;
    }

    @Override
    public void visit (Addition node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Add(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (Subtraction node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Sub(currInstr++, result, leftVal, rightVal));
        prevVal = result;
    }

    @Override
    public void visit (LogicalOr node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Or(currInstr++, result, leftVal, rightVal));
        prevVal = result;
        relOp = null;
    }

    @Override
    public void visit (Relation node) {
        node.leftOperand().accept(this);
        Value leftVal = prevVal;
        tempCount++;

        node.rightOperand().accept(this);
        Value rightVal = prevVal;
        tempCount--;

        Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
        currBlock.add(new Cmp(currInstr++, result, leftVal, rightVal));
        prevVal = result;
        relOp = node.operator();
    }

    @Override
    public void visit (Assignment node) {
        node.source().accept(this);
        Value sourceVal = prevVal;
        tempCount++;

        node.destination().accept(this);
        Variable destVal = (Variable) prevVal;
        tempCount--;

        // If array, need to store at address
        if (isArray) {
            currBlock.add(new Store(currInstr++, destVal, sourceVal));
            isArray = false;
        } 
        // Otherwise just move into variable
        else {
            currBlock.add(new Move(currInstr++, destVal, sourceVal, destVal));
        }
    }

    @Override
    public void visit (ArgumentList node) {
        // Generate TAC for each argument computation
        int origTemp = tempCount;
        List<Value> argList = new ArrayList<Value>();
        for (Expression e : node) {
           e.accept(this); 
           argList.add(prevVal);
           tempCount++;
        }
        tempCount = origTemp;
        args = new ValueList(argList);
    }

    @Override
    public void visit (FunctionCall node) {
        // After type-checking, symbol should be resolved
        Symbol s = node.symbols().get(0);
        
        // Check if pre-defined
        if (s == SymbolTable.readIntSymbol) {
            Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            currBlock.add(new Read(currInstr++, result));
        } else if (s == SymbolTable.readBoolSymbol) {
            Variable result = new Temporary(new Symbol("_t" + tempCount, null), currInstr);
            currBlock.add(new ReadB(currInstr++, result));
        } else if (s == SymbolTable.printIntSymbol) {
            Value arg = null;

            for (Expression e : node.arguments()) {
                e.accept(this);
                arg = prevVal;
            }

            currBlock.add(new Write(currInstr++, arg));
        } else if (s == SymbolTable.printBoolSymbol) {
            Value arg = null;

            for (Expression e : node.arguments()) {
                e.accept(this);
                arg = prevVal;
            }

            currBlock.add(new WriteB(currInstr++, arg));
        } else if (s == SymbolTable.printlnSymbol) {
            currBlock.add(new WriteNL(currInstr++));
        } else {
            // Get arguments as ValueList
            node.arguments().accept(this);
            BasicBlock funcBlock = null;

            // Find associated function block
            for (CFG f_cfg : funcs) {
                if (s == f_cfg.function()) {
                    funcBlock = f_cfg.start();
                }
            }

            currBlock.add(new Call(currInstr++, s, args, funcBlock));
        }
    }

    @Override
    public void visit (IfStatement node) {
        // Add comparison
        node.condition().accept(this);
        Variable comp = (Variable) prevVal;

        // Make basic block for ELSE
        BasicBlock elseBlock = new BasicBlock(currBlockNum++);

        // Make basic block for THEN
        BasicBlock thenBlock = new BasicBlock(currBlockNum++);
        currBlock.addSuccessor(thenBlock, "then"); // label="then"
        thenBlock.addPredecessor(currBlock);
        currBlock.addSuccessor(elseBlock, "else"); // label="else"
        elseBlock.addPredecessor(currBlock);

        // Figure out how to compare
        if (relOp == null || relOp.equals("<=")) {
            currBlock.add(new Bgt(currInstr++, comp, elseBlock));
        } else if (relOp.equals("==")) {
            currBlock.add(new Bne(currInstr++, comp, elseBlock));
        } else if (relOp.equals("!=")) {
            currBlock.add(new Beq(currInstr++, comp, elseBlock));
        } else if (relOp.equals("<")) {
            currBlock.add(new Bge(currInstr++, comp, elseBlock));
        } else if (relOp.equals(">")) {
            currBlock.add(new Ble(currInstr++, comp, elseBlock));
        } else if (relOp.equals(">=")) {
            currBlock.add(new Blt(currInstr++, comp, elseBlock));
        }

        relOp = null;

        // Build THEN block
        currBlock = thenBlock;
        node.thenStatements().accept(this);

        // Build ELSE block (if there)
        if (node.hasElse()) {
            currBlock = elseBlock;
            // Add jump from then to after
            BasicBlock afterBlock = new BasicBlock(currBlockNum++);
            thenBlock.add(new Bra(currInstr++, afterBlock));

            node.elseStatements().accept(this);

            thenBlock.addSuccessor(afterBlock); // label=""
            afterBlock.addPredecessor(thenBlock);
            elseBlock.addSuccessor(afterBlock); // label=""
            afterBlock.addPredecessor(elseBlock);

            currBlock = afterBlock;
        } else {
            currBlock.addSuccessor(elseBlock);
            elseBlock.addPredecessor(currBlock);
            currBlock = elseBlock;
        }
    }

    @Override
    public void visit (WhileStatement node) {
        // Create block for comparison check
        BasicBlock compBlock = new BasicBlock(currBlockNum++);
        currBlock.addSuccessor(compBlock);
        compBlock.addPredecessor(compBlock);
        currBlock = compBlock;
        node.condition().accept(this);
        Variable comp = (Variable) prevVal;

        // Make basic block for THEN
        BasicBlock thenBlock = new BasicBlock(currBlockNum++);

        // Make basic block for ELSE
        BasicBlock elseBlock = new BasicBlock(currBlockNum++);
        currBlock.addSuccessor(thenBlock, "then"); // label="then"
        thenBlock.addPredecessor(currBlock);
        currBlock.addSuccessor(elseBlock, "else"); // label="else"
        elseBlock.addPredecessor(currBlock);

        // Figure out how to compare
        if (relOp == null) {
            // TODO: Handle non comparisons (booleans)
        } else if (relOp.equals("==")) {
            currBlock.add(new Bne(currInstr++, comp, elseBlock));
        } else if (relOp.equals("!=")) {
            currBlock.add(new Beq(currInstr++, comp, elseBlock));
        } else if (relOp.equals("<")) {
            currBlock.add(new Bge(currInstr++, comp, elseBlock));
        } else if (relOp.equals("<=")) {
            currBlock.add(new Bgt(currInstr++, comp, elseBlock));
        } else if (relOp.equals(">")) {
            currBlock.add(new Ble(currInstr++, comp, elseBlock));
        } else if (relOp.equals(">=")) {
            currBlock.add(new Blt(currInstr++, comp, elseBlock));
        }

        relOp = null;

        // Build THEN block
        currBlock = thenBlock;
        node.doStatements().accept(this);
        
        // Add jump from then to comparison
        currBlock.add(new Bra(currInstr++, compBlock));
        currBlock.addSuccessor(compBlock, "", "ne");
        compBlock.addPredecessor(currBlock);

        // Switch to else for following statements
        currBlock = elseBlock;
    }

    @Override
    public void visit (RepeatStatement node) {
        // Make block for repeat statements
        BasicBlock repeatBlock = new BasicBlock(currBlockNum++);
        currBlock.addSuccessor(repeatBlock);
        repeatBlock.addPredecessor(currBlock);

        BasicBlock elseBlock = new BasicBlock(currBlockNum++);
        
        currBlock = repeatBlock;
        node.repeatStatements().accept(this);
        node.condition().accept(this);
        currBlock.addSuccessor(repeatBlock, "then", "ne");
        repeatBlock.addPredecessor(currBlock);
        currBlock.addSuccessor(elseBlock, "else");
        elseBlock.addPredecessor(currBlock);
        Variable comp = (Variable) prevVal;

        // Figure out how to compare
        if (relOp == null) {
            // TODO: Handle non comparisons (booleans)
        } else if (relOp.equals("==")) {
            currBlock.add(new Bne(currInstr++, comp, currBlock));
        } else if (relOp.equals("!=")) {
            currBlock.add(new Beq(currInstr++, comp, currBlock));
        } else if (relOp.equals("<")) {
            currBlock.add(new Bge(currInstr++, comp, currBlock));
        } else if (relOp.equals("<=")) {
            currBlock.add(new Bgt(currInstr++, comp, currBlock));
        } else if (relOp.equals(">")) {
            currBlock.add(new Ble(currInstr++, comp, currBlock));
        } else if (relOp.equals(">=")) {
            currBlock.add(new Blt(currInstr++, comp, currBlock));
        }

        relOp = null;
        currBlock = elseBlock;
    }

    @Override
    public void visit (ReturnStatement node) {
        // TODO: TAC for returning
        if (node.hasReturn()) {
            node.returnValue().accept(this);
            Value returnedVal = prevVal;
            currBlock.add(new Ret(currInstr++, returnedVal));
        } else {
            currBlock.add(new Ret(currInstr++));
        }
    }

    @Override
    public void visit (StatementSequence node) {
        // Generate TAC for each statement
        for (Statement s : node) {
            s.accept(this);
        }
    }

    @Override
    public void visit (VariableDeclaration node) {
        // Do nothing I think? No TAC for declarations
    }

    @Override
    public void visit (FunctionBody node) {
        node.functionStatementSequence().accept(this);
    }

    @Override
    public void visit (FunctionDeclaration node) {
        // Get function symbol
        Symbol funcSymbol = node.symbol();

        // Make CFG for this function
        currCFG = new CFG(currBlockNum++, funcSymbol);
        currBlock = currCFG.start();
        funcs.add(currCFG);

        // Make TAC for function
        node.body().accept(this);
    }

    @Override
    public void visit (DeclarationList node) {
        for (Declaration d : node) {
            d.accept(this);
        }
    }

    @Override
    public void visit (Computation node) {
        dataSegBase = "SP";
        // Go through function definitions
        node.functions().accept(this);

        // Make basic block for main function
        currCFG = new CFG(currBlockNum++, node.main());
        funcs.add(currCFG);
        currBlock = currCFG.start();
        // Set data segment base to GDB
        dataSegBase = "GDB";
        node.mainStatementSequence().accept(this);
    }

    public Iterator<CFG> iterator() {
        return funcs.iterator();
    }
}
