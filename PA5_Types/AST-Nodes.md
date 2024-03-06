# AST Nodes

### Computation

    - Symbol : main
    - DeclarationList : vars
    - DeclarationList : funcs
    - StatementSequence : mainSeq

### StatementSequence

    - ArrayList<Statement> : stmts

### Assignment (Statement)

    - Expression : lhs
    - Expression : rhs

### IfStatement (Statement)

    - Expression : cond
    - StatementSequence : thenStmts
    - StatementSequence : elseStmts (optional)

### WhileStatement (Statement)

    - Expression : cond
    - StatementSequence : doStmts

### RepeatStatement (Statement)

    - StatementSequence : repStmts
    - Expression : cond

### ReturnStatement (Statement)

    - Expression : retVal

### DeclarationList

    - ArrayList<Declaration> : decls

### VariableDeclaration (Declaration)

    - Symbol : symbol (from Declaration)

### FunctionDeclaration (Declaration)

    - Symbol : symbol (from Declaration)
    - FunctionBody : funcBody

### FunctionBody

    - DeclarationList : vars
    - StatementSequence : funcSeq

### FunctionCall (Statement)

    - Symbol : func
    - ArgumentList : args

### ArgumentList

    - ArrayList<Expression> : params

### ArrayIndex

    - ArrayIndex : arrIdx (optional)
    - Symbol : ident (optional)
    - Expression idx

### Addition (Expression)

    - Expression : lhs
    - Expression : rhs

### Subtraction (Expression)

    - Expression : lhs
    - Expression : rhs

### Multiplication (Expression)

    - Expression : lhs
    - Expression : rhs

### Division (Expression)

    - Expression : lhs
    - Expression : rhs

### Power (Expression)

    - Expression : lhs
    - Expression : rhs

### Modulo (Expression)

    - Expression : lhs
    - Expression : rhs

### LogicalAnd (Expression)

    - Expression : lhs
    - Expression : rhs

### LogicalNot (Expression)

    - Expression : expr

### LogicalOr (Expression)

    - Expression : lhs
    - Expression : rhs

### Relation (Expression)

    - Expression : lhs
    - String : op
    - Expression : rhs

### IntegerLiteral (Expression)

    - int : val

### BoolLiteral (Expression)

    - boolean : val
