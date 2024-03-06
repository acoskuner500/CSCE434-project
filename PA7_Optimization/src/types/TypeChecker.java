package types;

import java.util.List;
import java.util.ArrayList;

import ast.*;
import coco.Symbol;

public class TypeChecker implements NodeVisitor {

    private StringBuilder errorBuffer = new StringBuilder();
    private Symbol currentFunction = null;
    private Type prevResult = null;
    private TypeList currentArgs = null;
    private Symbol currentArr = null;
    private Type arrType = null;

    /*
     * Useful error strings:
     *
     * "Call with args " + argTypes + " matches no function signature."
     * "Call with args " + argTypes + " matches multiple function signatures."
     * 
     * "IfStat requires relation condition not " + cond.getClass() + "."
     * "WhileStat requires relation condition not " + cond.getClass() + "."
     * "RepeatStat requires relation condition not " + cond.getClass() + "."
     * 
     * "Function " + currentFunction.name() + " returns " + statRetType +
     * " instead of " + funcRetType + "."
     * 
     * "Variable " + var.name() + " has invalid type " + var.type() + "."
     * "Array " + var.name() + " has invalid base type " + baseType + "."
     * 
     * 
     * "Function " + currentFunction.name() + " has a void arg at pos " + i + "."
     * "Function " + currentFunction.name() + " has an error in arg at pos " + i +
     * ": " + ((ErrorType) t).message())
     * "Not all paths in function " + currentFunction.name() + " return."
     */

    private void reportError(int lineNum, int charPos, String message) {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    public boolean hasError() {
        return errorBuffer.length() != 0;
    }

    public String errorReport() {
        return errorBuffer.toString();
    }

    public boolean check(AST ast) {
        ast.getRoot().accept(this);
        return !hasError();
    }

    @Override
    public void visit(BoolLiteral node) {
        prevResult = new BoolType();
    }

    @Override
    public void visit(IntegerLiteral node) {
        prevResult = new IntType();
    }

    @Override
    public void visit(FloatLiteral node) {
        prevResult = new FloatType();
    }

    @Override
    public void visit(AddressOf node) {
        if (node.hasIndex()) {
            node.arrayIndex().accept(this);
            if (!(prevResult instanceof ErrorType)) {
                prevResult = new AddressType(arrType);
                arrType = null;
            }
            currentArr = null;
        } else {
            prevResult = node.type();
        }
    }

    @Override
    public void visit(ArrayIndex node) {
        if (!node.hasSymbol()) {
            node.arrayIndex().accept(this);

            // If there was an earlier error, don't bother
            if (prevResult instanceof ErrorType) {
                return;
            }
        } else {
            currentArr = node.symbol();
            arrType = node.symbol().type();
        }

        Symbol old = currentArr;
        node.indexValue().accept(this);
        currentArr = old;

        // If it is not an array type, it cannot be indexed
        if (!(arrType instanceof ArrayType)) {
            prevResult = new ErrorType("Cannot index " + currentArr.type() + " with " + prevResult + ".");
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
            return;
        }
        
        // Check that index is within bounds (if can be statically determined)
        if (node.indexValue() instanceof IntegerLiteral) {
            IntegerLiteral idx = (IntegerLiteral) node.indexValue();
            int dimensionSize = ((ArrayType) arrType).numElements();

            if (idx.value() < 0 || idx.value() >= dimensionSize) {
                prevResult = new ErrorType("Array Index Out of Bounds : " + idx.value() + " for array " + currentArr.name());
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
            }
        } 
        // Make sure it is at least an integer type 
        else if (!(prevResult instanceof IntType)) {
            prevResult = new ErrorType("Cannot index " + currentArr.type() + " with " + prevResult + ".");
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
        }

        arrType = ((ArrayType) arrType).elementType();
    }

    @Override
    public void visit(Dereference node) {
        if (node.hasIndex()) {
            Type oldArrayElem = arrType;
            node.arrayIndex().accept(this);
            // If there was an error, cannot dereference
            if (!(prevResult instanceof ErrorType)) {
                prevResult = arrType;
                arrType = oldArrayElem;
            } else {
                prevResult = new ErrorType("Cannot dereference " + prevResult + "");
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
            }
            currentArr = null;
        } else {
            prevResult = node.identifier().type();
        }
    }

    @Override
    public void visit(LogicalNot node) {
        node.operand().accept(this);
        Type opType = prevResult;
        prevResult = opType.not();

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(Power node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.pow(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        } else {
            // Check for negative numbers
            if (node.leftOperand() instanceof IntegerLiteral) {
                IntegerLiteral leftVal = (IntegerLiteral) node.leftOperand();
                if (leftVal.value() < 0) {
                    prevResult = new ErrorType("Power cannot have a negative base of " + leftVal.value() + ".");
                    reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
                }
            }
            if (node.rightOperand() instanceof IntegerLiteral) {
                IntegerLiteral rightVal = (IntegerLiteral) node.rightOperand();
                if (rightVal.value() < 0) {
                    prevResult = new ErrorType("Power cannot have a negative exponent of " + rightVal.value() + ".");
                    reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
                }
            }
        }
    }

    @Override
    public void visit(Multiplication node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.mul(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(Division node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.div(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        } else {
            if (node.rightOperand() instanceof IntegerLiteral) {
            IntegerLiteral divisor = (IntegerLiteral) node.rightOperand();

            if (divisor.value() == 0) {
                prevResult = new ErrorType("Cannot divide by 0.");
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
            }
        }
        }
    }

    @Override
    public void visit(Modulo node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.mod(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(LogicalAnd node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.and(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(Addition node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.add(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(Subtraction node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.sub(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(LogicalOr node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.or(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(Relation node) {
        node.leftOperand().accept(this);
        Type leftType = prevResult;
        node.rightOperand().accept(this);
        Type rightType = prevResult;
        prevResult = leftType.compare(rightType);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.lineNumber(), node.charPosition(), err.getMessage());
        }

    }

    @Override
    public void visit(Assignment node) {
        node.destination().accept(this);
        Type dest = prevResult;
        node.source().accept(this);
        Type source = prevResult;
        prevResult = dest.assign(source);

        // If error, log it
        if (prevResult instanceof ErrorType) {
            ErrorType err = (ErrorType) prevResult;
            reportError(node.destination().lineNumber(), node.destination().charPosition(), err.getMessage());
        }
    }

    @Override
    public void visit(ArgumentList node) {
        currentArgs = new TypeList();
        if (node.empty())
            return;
        for (Expression e : node) {
            e.accept(this);
            currentArgs.append(prevResult);
        }
    }

    @Override
    public void visit(FunctionCall node) {
        TypeList oldList = currentArgs;
        node.arguments().accept(this);
        // Check if argument types match any of the overloads
        List<Symbol> overloads = node.symbols();
        for (Symbol s : overloads) {
            TypeList availableParams = ((FuncType) s.type()).parameters();
            if (availableParams.equals(currentArgs)) {
                prevResult = ((FuncType) s.type()).returnType();

                // Resolve to the correct symbol
                List<Symbol> resolved = new ArrayList<Symbol>();
                resolved.add(s);
                node.resolve(resolved);

                currentArgs = oldList;
                return;
            }
        }
        prevResult = new ErrorType("Call with args (" + currentArgs + ") matches no function signature.");
        reportError(node.lineNumber(), node.charPosition(), ((ErrorType) prevResult).getMessage());
        currentArgs = oldList;
    }

    @Override
    public void visit(IfStatement node) {
        node.condition().accept(this);
        // Condition needs to have a boolean type
        if (!(prevResult instanceof BoolType)) {
            reportError(node.lineNumber(), node.charPosition(),
                    "IfStat requires bool condition not " + prevResult + ".");
        }
        node.thenStatements().accept(this);
        if (node.hasElse()) {
            node.elseStatements().accept(this);
        }
    }

    @Override
    public void visit(WhileStatement node) {
        node.condition().accept(this);
        // Condition needs to have a boolean type
        if (!(prevResult instanceof BoolType)) {
            reportError(node.lineNumber(), node.charPosition(),
                    "WhileStat requires bool condition not " + prevResult + ".");
        }
        node.doStatements().accept(this);
    }

    @Override
    public void visit(RepeatStatement node) {
        node.repeatStatements().accept(this);
        node.condition().accept(this);
        // Condition needs to have a boolean type
        if (!(prevResult instanceof BoolType)) {
            reportError(node.lineNumber(), node.charPosition(),
                    "RepeatStat requires bool condition not " + prevResult + ".");
        }
    }

    @Override
    public void visit(ReturnStatement node) {
        if (node.hasReturn()) {
            node.returnValue().accept(this);
        } else {
            prevResult = new VoidType();
        }

        // Make sure return matches return type
        Type expectedReturn = ((FuncType) currentFunction.type()).returnType();
        if (prevResult.getClass() != expectedReturn.getClass()) {
            reportError(node.lineNumber(), node.charPosition(), "Function " + currentFunction.name() + " returns "
                    + prevResult + " instead of " + expectedReturn + ".");
        }
    }

    @Override
    public void visit(StatementSequence node) {
        for (Statement s : node) {
            s.accept(this);
        } 
    }

    private boolean hasReturns(StatementSequence node) {
        // Check if every path results in a return
        for (Statement stat : node) {
            if (stat instanceof ReturnStatement) {
                return true;
            } else if (stat instanceof IfStatement) {
                // Has to have return in THEN and ELSE
                IfStatement ifStat = (IfStatement) stat;
                if (ifStat.hasElse()) {
                    if (hasReturns(ifStat.thenStatements()) && hasReturns(ifStat.elseStatements())) {
                        return true;
                    }
                }
            } else if (stat instanceof RepeatStatement) {
                // Must have return on all paths in the repeat
                RepeatStatement repeatStat = (RepeatStatement) stat;
                if (hasReturns(repeatStat.repeatStatements())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void visit(VariableDeclaration node) {
        // Make sure that arrays have valid size
        if (node.symbol().type() instanceof ArrayType) {
            Type arr = node.symbol().type();

            while (arr instanceof ArrayType) {
                ArrayType arrDim = (ArrayType) arr;
                if (arrDim.numElements() <= 0) {
                    reportError(node.lineNumber(), node.charPosition(),
                            "Array " + node.symbol().name() + " has invalid size " + arrDim.numElements() + ".");
                }
                arr = arrDim.elementType();
            }
        }
    }

    @Override
    public void visit(FunctionBody node) {
        if (node.hasVariables()) {
            node.variables().accept(this);
        }
        node.functionStatementSequence().accept(this);

        // Check if all function paths have returns (if applicable)
        FuncType funcType = (FuncType) currentFunction.type();
        System.out.println(currentFunction);
        if (!(funcType.returnType() instanceof VoidType) && !hasReturns(node.functionStatementSequence())) {
            reportError(node.lineNumber(), node.charPosition(), "Not all paths in function " + currentFunction.name() + " return.");
        }
    }

    @Override
    public void visit(FunctionDeclaration node) {
        currentFunction = node.function();
        node.body().accept(this);
        currentFunction = null;
    }

    @Override
    public void visit(DeclarationList node) {
        if (node.empty())
            return;
        for (Declaration d : node) {
            d.accept(this);
        }
    }

    @Override
    public void visit(Computation node) {
        node.variables().accept(this);
        node.functions().accept(this);
        currentFunction = node.main();
        node.mainStatementSequence().accept(this);
        currentFunction = null;
    }
}
