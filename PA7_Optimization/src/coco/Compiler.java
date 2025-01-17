package coco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.cli.*;

import ast.*;
import ir.*;
import ir.cfg.*;
import types.*;
import ir.optimizations.*;
import ir.tac.TAC;
import ir.tac.Move;
import ir.tac.Variable;
import ir.tac.Literal;

public class Compiler { 

    // Error Reporting ============================================================
    private StringBuilder errorBuffer = new StringBuilder();

    private String reportSyntaxError(NonTerminal nt) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name()
                + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private String reportSyntaxError(Token.Kind kind) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got "
                + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    public String errorReport() {
        return errorBuffer.toString();
    }

    public boolean hasError() {
        return errorBuffer.length() != 0;
    }

    private class QuitParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public QuitParseException(String errorMessage) {
            super(errorMessage);
        }
    }

    private int lineNumber() {
        return currentToken.lineNumber();
    }

    private int charPosition() {
        return currentToken.charPosition();
    }

    // Compiler ===================================================================
    private Scanner scanner;
    private Token currentToken;

    private int numDataRegisters; // available registers are [1..numDataRegisters]
    private List<Integer> instructions;
    private ArrayList<Pair<FunctionCall, Token>> promisedCalls;
    private boolean isGlobal;
    private List<Symbol> globals;

    // Saved for any later use
    private AST savedAST;
    private SSA savedSSA;

    // Register allocations
    private HashMap<Variable, Integer> regAllocs;
    // Need to map from IDENT to memory offset

    public Compiler(Scanner scanner, int numRegs) {
        this.scanner = scanner;
        currentToken = this.scanner.next();
        numDataRegisters = numRegs;
        instructions = new ArrayList<>();
        promisedCalls = new ArrayList<Pair<FunctionCall, Token>>();
        globals = new ArrayList<Symbol>();
    }

    public AST genAST() {
        initSymbolTable();
        try {
            savedAST = new AST(computation());
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
            savedAST = new AST(null);
        }
        return savedAST;
    }

    public SSA genSSA(AST ast) {
        IRGenerator irGen = new IRGenerator();
        savedSSA = irGen.generateIR(ast);
        return savedSSA;
    }

    public String optimization(List<String> optArgs, CommandLine cmd) {
        savedSSA.resetVisited();

        boolean noChanges;
        Propagation propOp;
        ConstantFolding cfOp;
        DeadCodeElimination dceOp;

        // Look for the use of uninitialized variables
        LiveVarsGenerator lvg = new LiveVarsGenerator();
        for (CFG cfg : savedSSA) {
            lvg.generate(cfg);
        }

        // If there are global variables live at the start of main, they are uninitialized
        CFG mainCFG = savedSSA.CFGs().get(savedSSA.CFGs().size() - 1);
        int currID = -1;
        BasicBlock start = mainCFG.start();
        int currBlockID = savedSSA.lastBlockNumber();
        for (Symbol s : mainCFG.start().getEntryLiveSet()) {
            // Array types can be uninitialized
            if (s.isGlobalVariable() && !(s.type() instanceof ArrayType)) {
                // Issue warning
                System.out.println("[WARNING]: " + s + " is uninitialized");

                // If start is loop, need to create predecessor block
                if (!start.getPredecessors().isEmpty()) {
                    BasicBlock oldStart = start;
                    start = new BasicBlock(currBlockID++);
                    start.addSuccessor(oldStart);
                    oldStart.addPredecessor(start);
                    mainCFG.setStart(start);
                    start.setFunction(oldStart.functionName());
                    oldStart.setFunction(null);
                }
                
                // Add initialization at start of main
                List<TAC> instr = mainCFG.start().getInstructions();
                Variable dest = new Variable(s);
                Literal defaultVal = null;
                
                if (s.type() instanceof IntType) {
                    defaultVal = new Literal(new IntegerLiteral(0));
                } else {
                    defaultVal = new Literal(new BoolLiteral(false));
                }
                
                instr.add(0, new Move(currID--, dest, defaultVal));
            }
        }

        // If there are any local variables live at the start of functions, they are uninitialized
        for (int i = 0; i < savedSSA.CFGs().size() - 1; i++) {
            CFG cfg = savedSSA.CFGs().get(i);
            start = cfg.start();
            for (Symbol s : cfg.start().getEntryLiveSet()) {
                if (s.isLocal() && !(s.type() instanceof ArrayType)) {
                    // Issue warning
                    System.out.println("[WARNING]: " + s + " is uninitialized");

                    // If start is loop, need to create predecessor block
                    if (!start.getPredecessors().isEmpty()) {
                        BasicBlock oldStart = start;
                        start = new BasicBlock(currBlockID--);
                        start.addSuccessor(oldStart);
                        oldStart.addPredecessor(start);
                        cfg.setStart(start);
                        start.setFunction(oldStart.functionName());
                        oldStart.setFunction(null);
                    }

                    // Add initialization at start of function
                    List<TAC> instr = start.getInstructions();
                    Variable dest = new Variable(s);
                    Literal defaultVal = null;
                
                    if (s.type() instanceof IntType) {
                        defaultVal = new Literal(new IntegerLiteral(0));
                    } else {
                        defaultVal = new Literal(new BoolLiteral(false));
                    }
                    
                    instr.add(0, new Move(currID--, dest, defaultVal));
                }
            }
        }

        boolean loop = false;

        // Run all available optimizations
        if (cmd.hasOption("max")) {
            // If no specific order, use all
            if (optArgs.isEmpty()) {
                optArgs = Arrays.asList("cp", "cf", "cpp", "cse", "dce");
            }
            loop = true;
        }
        
        do {
            noChanges = true;
            for (String optim : optArgs) {
                switch (optim) {
                    // Constant Propagation
                    case "cp":
                        propOp = new Propagation();
                        for (CFG cfg : savedSSA) {
                            if (propOp.optimizeCP(cfg)) {
                                noChanges = false;
                            }
                        }
                        savedSSA.resetVisited();
                        break;
                    // Constant Folding
                    case "cf":
                        cfOp = new ConstantFolding();
                        for (CFG cfg : savedSSA) {
                            if (cfOp.optimize(cfg)) {
                                noChanges = false;
                            }
                        }
                        savedSSA.resetVisited();
                        break;
                    // Copy Propagation
                    case "cpp":
                        propOp = new Propagation();
                        for (CFG cfg : savedSSA) {
                            if (propOp.optimizeCPP(cfg)) {
                                noChanges = false;
                            }
                        }
                        savedSSA.resetVisited();
                        break;
                    // Common Subexpression Elimination
                    case "cse":
                        break;
                    // Dead Code Elimination
                    case "dce":
                        dceOp = new DeadCodeElimination();
                        for (CFG cfg : savedSSA) {
                            if (dceOp.optimize(cfg, globals)) {
                                noChanges = false;
                            }
                        }
                        savedSSA.resetVisited();
                        break;
                }
            }
        } while (loop && !noChanges);

        // Reset live sets
        for (CFG cfg : savedSSA) {
            cfg.resetLiveSet();
        }

        // Remove empty blocks
        CFGCleanup cfgCleaner = new CFGCleanup();
        cfgCleaner.clean(savedSSA);
      
        return savedSSA.asDotGraph();
    }

    public void regAlloc(int numRegs) {
        // Generate interference graph
        RegisterAllocator regAlloc = new RegisterAllocator();
        regAllocs = regAlloc.allocateRegisters(savedSSA, numRegs);
    }

    public int[] genCode() {
        // Array of op codes
        int[] instr;
        CodeGenerator cGen = new CodeGenerator();
        instr = cGen.generate(savedSSA, regAllocs, globals);
        return instr;
    }

    public int[] compile() {
        initSymbolTable();
        try {
            computation();
            return instructions.stream().mapToInt(Integer::intValue).toArray();
        } catch (QuitParseException q) {
            // errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() +
            // ")");
            // errorBuffer.append("[Could not complete parsing.]");
            return new ArrayList<Integer>().stream().mapToInt(Integer::intValue).toArray();
        }
    }

    // SymbolTable Management =====================================================
    private SymbolTable symbolTable;

    private void initSymbolTable() {
        symbolTable = new SymbolTable();
    }

    private void enterScope() {
        symbolTable.enter();
    }

    private void exitScope() {
        symbolTable.exit();
    }

    private List<Symbol> tryResolveVariable(Token ident, boolean funcFirstPass) {
        // Search for an identifier with the given name
        try {
            return symbolTable.lookup(ident.lexeme());
        } catch (SymbolNotFoundError e) {
            if (!funcFirstPass) {
                reportResolveSymbolError(ident.lexeme(), ident.lineNumber(), ident.charPosition());
            }
            return null;
        }
    }

    private Symbol tryDeclareVariable(Token ident, Type type) {
        try {
            return symbolTable.insert(ident.lexeme(), type);
        } catch (RedeclarationError e) {
            reportDeclareSymbolError(ident.lexeme(), ident.lineNumber(), ident.charPosition());
            return null;
        }
    }

    private Symbol tryDeclareVariable(Token ident, Type type, boolean isGlobal) {
        try {
            return symbolTable.insert(ident.lexeme(), type, isGlobal);
        } catch (RedeclarationError e) {
            reportDeclareSymbolError(ident.lexeme(), ident.lineNumber(), ident.charPosition());
            return null;
        }
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos) {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private String reportDeclareSymbolError(String name, int lineNum, int charPos) {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        return message;
    }

    // Helper Methods =============================================================
    private boolean have(Token.Kind kind) {
        return currentToken.is(kind);
    }

    private boolean have(NonTerminal nt) {
        return nt.firstSet().contains(currentToken.kind);
    }

    private boolean accept(Token.Kind kind) {
        if (have(kind)) {
            try {
                currentToken = scanner.next();
            } catch (NoSuchElementException e) {
                if (!kind.equals(Token.Kind.EOF)) {
                    String errorMessage = reportSyntaxError(kind);
                    throw new QuitParseException(errorMessage);
                }
            }
            return true;
        }
        return false;
    }

    private boolean accept(NonTerminal nt) {
        if (have(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }

    private boolean expect(Token.Kind kind) {
        if (accept(kind)) {
            return true;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private boolean expect(NonTerminal nt) {
        if (accept(nt)) {
            return true;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve(Token.Kind kind) {
        Token tok = currentToken;
        if (accept(kind)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve(NonTerminal nt) {
        Token tok = currentToken;
        if (accept(nt)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

    // Grammar Rules ==============================================================

    // function for matching rule that only expects nonterminal's FIRST set
    private Token matchNonTerminal(NonTerminal nt) {
        return expectRetrieve(nt);
    }

    // type = "bool" | "int" | "float"
    private Type type() {
        Token t = matchNonTerminal(NonTerminal.TYPE);

        switch (t.kind()) {
            case BOOL:
                return new BoolType();
            case INT:
                return new IntType();
            case FLOAT:
                return new FloatType();
            default:
                return new VoidType();
        }
    }

    // literal = integerLit | floatLit
    private Expression literal() {
        Token t = expectRetrieve(NonTerminal.LITERAL);

        switch (t.kind()) {
            case INT_VAL:
                return new IntegerLiteral(t.lineNumber(), t.charPosition(), Integer.parseInt(t.lexeme()));
            case TRUE:
            case FALSE:
                return new BoolLiteral(t.lineNumber(), t.charPosition(), Boolean.parseBoolean(t.lexeme()));
            case FLOAT_VAL:
                return new FloatLiteral(t.lineNumber(), t.charPosition(), Float.parseFloat(t.lexeme()));
            default:
                return null;
        }
    }

    // designator = ident { "[" relExpr "]" }
    private Expression designator(boolean isAddress) {
        Token ident = expectRetrieve(Token.Kind.IDENT);
        List<Symbol> overloads = tryResolveVariable(ident, false);

        // Try to find the correct symbol
        Symbol sym = overloads.get(0);
        for (Symbol s : overloads) {
            // It can't be a function
            if (!(s.type() instanceof FuncType)) {
                sym = s;
            }
        }

        // Build indices backwards
        ArrayIndex arrIdx = null;

        while (have(Token.Kind.OPEN_BRACKET)) {
            Token start = expectRetrieve(Token.Kind.OPEN_BRACKET);

            if (arrIdx == null) {
                arrIdx = new ArrayIndex(start.lineNumber(), start.charPosition(), sym, relExpr());
            } else {
                arrIdx = new ArrayIndex(start.lineNumber(), start.charPosition(), arrIdx, relExpr());
            }

            expect(Token.Kind.CLOSE_BRACKET);
        }

        Expression desig;

        if (isAddress) {
            if (arrIdx == null) {
                desig = new AddressOf(ident.lineNumber(), ident.charPosition(), (sym == null) ? null : sym);
            } else {
                desig = new AddressOf(ident.lineNumber(), ident.charPosition(), arrIdx);
            }
        } else {
            if (arrIdx == null) {
                desig = new Dereference(ident.lineNumber(), ident.charPosition(), (sym == null) ? null : sym);
            } else {
                desig = new Dereference(ident.lineNumber(), ident.charPosition(), arrIdx);
            }
        }

        return desig;
    }

    // computation = "main" {varDecl} {funcDecl} "{" statSeq "}" "."
    private Computation computation() {
        Token first = expectRetrieve(Token.Kind.MAIN);
        // Create symbol for main
        Symbol main = SymbolTable.mainSymbol;
        ArrayList<Declaration> varDecls = new ArrayList<Declaration>();
        ArrayList<Declaration> funcDecls = new ArrayList<Declaration>();

        // Deal with varDecl
        isGlobal = true;
        while (have(NonTerminal.VAR_DECL)) {
            ArrayList<VariableDeclaration> vDecs = varDecl();

            for (VariableDeclaration vd : vDecs) {
                varDecls.add(vd);
            }
        }
        isGlobal = false;

        // Deal with funcDecl
        while (have(NonTerminal.FUNC_DECL)) {
            funcDecls.add(funcDecl());
        }

        DeclarationList vars;
        if (!varDecls.isEmpty()) {
            vars = new DeclarationList(varDecls.get(0).lineNumber(), varDecls.get(0).charPosition(), varDecls);
        } else {
            vars = new DeclarationList(first.lineNumber(), first.charPosition(), varDecls);
        }

        DeclarationList funcs;
        if (!funcDecls.isEmpty()) {
            funcs = new DeclarationList(funcDecls.get(0).lineNumber(), funcDecls.get(0).charPosition(), funcDecls);
        } else {
            funcs = new DeclarationList(first.lineNumber(), first.charPosition(), funcDecls);
        }

        expect(Token.Kind.OPEN_BRACE);
        StatementSequence stmts = statSeq();
        expect(Token.Kind.CLOSE_BRACE);
        expect(Token.Kind.PERIOD);

        // After end of program, check to see if any unresolved function calls
        for (Pair<FunctionCall, Token> deferredCall : promisedCalls) {
            FunctionCall old = deferredCall.first;
            old.resolve(tryResolveVariable(deferredCall.second, false));
        }

        if (hasError()) {
            return null;
        }

        return new Computation(first.lineNumber(), first.charPosition(), main, vars, funcs, stmts);
    }

    // varDecl = typeDecl ident { "," ident } ";"
    private ArrayList<VariableDeclaration> varDecl() {
        Type t = typeDecl();
        ArrayList<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();

        do {
            Token identTok = expectRetrieve(Token.Kind.IDENT);
            // Add to the symbol table
            Symbol s = isGlobal ? tryDeclareVariable(identTok, t, isGlobal) : tryDeclareVariable(identTok, t);

            // Keep track of global declarations for code gen
            if (isGlobal) {
                globals.add(s);
            }

            if (s != null) {
                vars.add(new VariableDeclaration(identTok.lineNumber(),
                        identTok.charPosition() + identTok.lexeme().length(), s));
            }
        } while (accept(Token.Kind.COMMA));

        expect(Token.Kind.SEMICOLON);
        return vars;
    }

    // funcDecl = "function" ident formalParam ":" ( "void" | type ) funcBody
    private FunctionDeclaration funcDecl() {
        Token first = expectRetrieve(Token.Kind.FUNC);
        Token ident = expectRetrieve(Token.Kind.IDENT);
        // Get list of parameter types
        enterScope();
        List<Symbol> params = formalParam();
        expect(Token.Kind.COLON);

        Type retType;

        if (!accept(Token.Kind.VOID)) {
            retType = type();
        } else {
            retType = new VoidType();
        }

        FunctionBody funcBody = funcBody();
        exitScope();

        TypeList paramTypes = new TypeList();
        for (Symbol s : params) {
            paramTypes.append(s.type());
        }

        Symbol s = tryDeclareVariable(ident, new FuncType(paramTypes, retType));

        return new FunctionDeclaration(first.lineNumber(), first.charPosition(), s, funcBody, params);
    }

    // statSeq = statement ";" { statement ";" }
    private StatementSequence statSeq() {
        ArrayList<Statement> stmts = new ArrayList<Statement>();
        stmts.add(statement());
        expect(Token.Kind.SEMICOLON);

        while (have(NonTerminal.STATEMENT)) {
            stmts.add(statement());
            expect(Token.Kind.SEMICOLON);
        }

        return new StatementSequence(stmts.get(0).lineNumber(), stmts.get(0).charPosition(), stmts);
    }

    // typeDecl = type { "[" integerLit "]" }
    private Type typeDecl() {
        Type baseType = type();
        ArrayList<Integer> dims = new ArrayList<Integer>();

        while (accept(Token.Kind.OPEN_BRACKET)) {
            Token val = expectRetrieve(Token.Kind.INT_VAL);
            dims.add(Integer.parseInt(val.lexeme()));
            expect(Token.Kind.CLOSE_BRACKET);
        }

        for (int i = dims.size() - 1; i >= 0; i--) {
            baseType = new ArrayType(baseType, dims.get(i));
        }

        return baseType;
    }

    // formalParam = "(" [ paramDecl { "," paramDecl } ] ")"
    private List<Symbol> formalParam() {
        expect(Token.Kind.OPEN_PAREN);
        ArrayList<Symbol> params = new ArrayList<Symbol>();

        if (have(NonTerminal.PARAM_DECL)) {
            params.add(paramDecl());

            while (accept(Token.Kind.COMMA)) {
                params.add(paramDecl());
            }
        }
        expect(Token.Kind.CLOSE_PAREN);

        return params;
    }

    // funcBody = "{" { varDecl } statSeq "}" ";"
    private FunctionBody funcBody() {
        Token start = expectRetrieve(Token.Kind.OPEN_BRACE);
        ArrayList<Declaration> varDecls = new ArrayList<Declaration>();
        DeclarationList decs = null;

        while (have(NonTerminal.VAR_DECL)) {
            for (Declaration d : varDecl()) {
                varDecls.add(d);
            }
        }

        if (!varDecls.isEmpty()) {
            decs = new DeclarationList(varDecls.get(0).lineNumber(), varDecls.get(0).charPosition(), varDecls);
        }

        StatementSequence stmts = statSeq();
        expect(Token.Kind.CLOSE_BRACE);
        expect(Token.Kind.SEMICOLON);
        return new FunctionBody(start.lineNumber(), start.charPosition(), decs, stmts);
    }

    // statement = assign | funcCall | ifStat | whileStat | repeatStat | returnStat
    private Statement statement() {
        if (have(NonTerminal.ASSIGN)) {
            return assign();
        } else if (have(NonTerminal.FUNC_CALL)) {
            return funcCall();
        } else if (have(NonTerminal.IF_STAT)) {
            return ifStat();
        } else if (have(NonTerminal.WHILE_STAT)) {
            return whileStat();
        } else if (have(NonTerminal.REPEAT_STAT)) {
            return repeatStat();
        } else if (have(NonTerminal.RETURN_STAT)) {
            return returnStat();
        } else if (have(NonTerminal.STATEMENT)) {
            return statement();
        } else {
            expect(NonTerminal.STATEMENT);
            throw new QuitParseException(null);
        }
    }

    // paramDecl = paramType ident
    private Symbol paramDecl() {
        Type t = paramType();
        Token ident = expectRetrieve(Token.Kind.IDENT);
        return tryDeclareVariable(ident, t, false);
    }

    // assign = designator ( ( assignOp relExpr ) | unaryOp )
    private Assignment assign() {
        AddressOf ident = (AddressOf) designator(true);
        Dereference copy = null;
        if (ident.hasIndex()) {
            copy = new Dereference(ident.lineNumber(), ident.charPosition(), ident.arrayIndex());
        } else {
            copy = new Dereference(ident.lineNumber(), ident.charPosition(), ident.identifier());
        }

        if (have(NonTerminal.ASSIGN_OP)) {
            Token assignOp = expectRetrieve(NonTerminal.ASSIGN_OP);
            Expression source = relExpr();

            switch (assignOp.kind()) {
                case ADD_ASSIGN:
                    source = new Addition(assignOp.lineNumber(), assignOp.charPosition(), copy, source);
                    break;
                case SUB_ASSIGN:
                    source = new Subtraction(assignOp.lineNumber(), assignOp.charPosition(), copy, source);
                    break;
                case MUL_ASSIGN:
                    source = new Multiplication(assignOp.lineNumber(), assignOp.charPosition(), copy, source);
                    break;
                case DIV_ASSIGN:
                    source = new Division(assignOp.lineNumber(), assignOp.charPosition(), copy, source);
                    break;
                case MOD_ASSIGN:
                    source = new Modulo(assignOp.lineNumber(), assignOp.charPosition(), copy, source);
                    break;
                case POW_ASSIGN:
                    source = new Power(assignOp.lineNumber(), assignOp.charPosition(), copy, source);
                    break;
                default:
            }

            return new Assignment(assignOp.lineNumber(), assignOp.charPosition(), ident, source);
        } else if (have(NonTerminal.UNARY_OP)) {
            Token unaryOp = expectRetrieve(NonTerminal.UNARY_OP);
            Type identType;

            // Extract type
            if (ident.hasIndex()) {
                ArrayIndex arrIdx = ident.arrayIndex();
                while (!arrIdx.hasSymbol()) {
                    arrIdx = arrIdx.arrayIndex();
                }
                identType = arrIdx.symbol().type();
            } else {
                identType = ident.identifier().type();
            }

            switch (unaryOp.kind()) {
                case UNI_INC:
                    if (identType instanceof ArrayType) {
                        ArrayType arrType = (ArrayType) identType;
                        if (arrType.elementType() instanceof IntType) {
                            return new Assignment(
                                    unaryOp.lineNumber(),
                                    unaryOp.charPosition(),
                                    ident,
                                    new Addition(
                                            unaryOp.lineNumber(),
                                            unaryOp.charPosition(),
                                            copy,
                                            new IntegerLiteral(unaryOp.lineNumber(), unaryOp.charPosition(), 1)));
                        }
                    } else {
                        return new Assignment(
                                unaryOp.lineNumber(),
                                unaryOp.charPosition(),
                                ident,
                                new Addition(
                                        unaryOp.lineNumber(),
                                        unaryOp.charPosition(),
                                        copy,
                                        new IntegerLiteral(unaryOp.lineNumber(), unaryOp.charPosition(), 1)));
                    }
                case UNI_DEC:
                    if (identType instanceof ArrayType) {
                        ArrayType arrType = (ArrayType) identType;
                        if (arrType.elementType() instanceof IntType) {
                            return new Assignment(
                                    unaryOp.lineNumber(),
                                    unaryOp.charPosition(),
                                    ident,
                                    new Subtraction(
                                            unaryOp.lineNumber(),
                                            unaryOp.charPosition(),
                                            copy,
                                            new IntegerLiteral(unaryOp.lineNumber(), unaryOp.charPosition(), 1)));
                        }
                    } else {
                        return new Assignment(
                                unaryOp.lineNumber(),
                                unaryOp.charPosition(),
                                ident,
                                new Subtraction(
                                        unaryOp.lineNumber(),
                                        unaryOp.charPosition(),
                                        copy,
                                        new IntegerLiteral(unaryOp.lineNumber(), unaryOp.charPosition(), 1)));
                    }
                default:
            }

            return null;
        } else {
            expect(NonTerminal.ASSIGN);
            return null;
        }
    }

    // funcCall = "call" ident "(" [ relExpr { "," relExpr } ] ")"
    private FunctionCall funcCall() {
        Token start = expectRetrieve(Token.Kind.CALL);
        Token ident = expectRetrieve(Token.Kind.IDENT);
        List<Symbol> overloads = tryResolveVariable(ident, true);

        Token paramStart = expectRetrieve(Token.Kind.OPEN_PAREN);

        ArrayList<Expression> params = new ArrayList<Expression>();

        if (have(NonTerminal.REL_EXPR)) {
            params.add(relExpr());

            while (accept(Token.Kind.COMMA)) {
                params.add(relExpr());
            }
        }

        expect(Token.Kind.CLOSE_PAREN);

        // If no symbol exists, defer the resolution for later
        FunctionCall ret = new FunctionCall(start.lineNumber(), start.charPosition(), overloads,
                new ArgumentList(paramStart.lineNumber(), paramStart.charPosition(), params));

        if (overloads == null) {
            promisedCalls.add(new Pair<FunctionCall, Token>(ret, ident));
        }

        return ret;
    }

    // ifStat = "if" relation "then" statSeq [ "else" statSeq ] "fi"
    private IfStatement ifStat() {
        Token start = expectRetrieve(Token.Kind.IF);
        Expression cond = relation();
        expect(Token.Kind.THEN);
        StatementSequence thenStmts = statSeq();
        IfStatement ret;

        if (accept(Token.Kind.ELSE)) {
            enterScope();
            StatementSequence elseStmts = statSeq();
            exitScope();
            ret = new IfStatement(start.lineNumber(), start.charPosition(), cond, thenStmts, elseStmts);
        } else {
            enterScope();
            ret = new IfStatement(start.lineNumber(), start.charPosition(), cond, thenStmts);
            exitScope();
        }

        expect(Token.Kind.FI);
        return ret;
    }

    // whileStat = "while" relation "do" statSeq "od"
    private WhileStatement whileStat() {
        Token start = expectRetrieve(Token.Kind.WHILE);
        Expression cond = relation();
        expect(Token.Kind.DO);
        enterScope();
        StatementSequence stmts = statSeq();
        exitScope();
        expect(Token.Kind.OD);
        return new WhileStatement(start.lineNumber(), start.charPosition(), cond, stmts);
    }

    // repeatStat = "repeat" statSeq "until" relation
    private RepeatStatement repeatStat() {
        Token start = expectRetrieve(Token.Kind.REPEAT);
        enterScope();
        StatementSequence stmts = statSeq();
        exitScope();
        expect(Token.Kind.UNTIL);
        Expression cond = relation();
        return new RepeatStatement(start.lineNumber(), start.charPosition(), stmts, cond);
    }

    // returnStat = "return" [ relExpr ]
    private ReturnStatement returnStat() {
        Token start = expectRetrieve(Token.Kind.RETURN);

        if (have(NonTerminal.REL_EXPR)) {
            return new ReturnStatement(start.lineNumber(), start.charPosition(), relExpr());
        }

        return new ReturnStatement(start.lineNumber(), start.charPosition());
    }

    // paramType = type { "[" "]" }
    private Type paramType() {
        Type type = type();
        ArrayList<Integer> arr = new ArrayList<Integer>();

        while (accept(Token.Kind.OPEN_BRACKET)) {
            expect(Token.Kind.CLOSE_BRACKET);
            // Could be any size
            arr.add(null);
        }

        for (Integer i : arr) {
            type = new ArrayType(type, i);
        }

        return type;
    }

    // relExpr = addExpr { relOp addExpr }
    private Expression relExpr() {
        Expression first = addExpr();
        Expression ret = first;

        while (have(NonTerminal.REL_OP)) {
            Token op = expectRetrieve(NonTerminal.REL_OP);
            Expression second = addExpr();
            ret = new Relation(op.lineNumber(), op.charPosition(), ret, op.lexeme(), second);
        }

        return ret;
    }

    // relation = "(" relExpr ")"
    private Expression relation() {
        expect(Token.Kind.OPEN_PAREN);
        Expression rel = relExpr();
        expect(Token.Kind.CLOSE_PAREN);
        return rel;
    }

    // addExpr = multExpr { addOp multExpr }
    private Expression addExpr() {
        Expression first = multExpr();
        Expression ret = first;

        while (have(NonTerminal.ADD_OP)) {
            Token op = expectRetrieve(NonTerminal.ADD_OP);
            Expression second = multExpr();

            switch (op.kind()) {
                case ADD:
                    ret = new Addition(op.lineNumber(), op.charPosition(), ret, second);
                    break;
                case SUB:
                    ret = new Subtraction(op.lineNumber(), op.charPosition(), ret, second);
                    break;
                case OR:
                    ret = new LogicalOr(op.lineNumber(), op.charPosition(), ret, second);
                    break;
                default:
            }
        }

        return ret;
    }

    // multExpr = powExpr { multOp powExpr }
    private Expression multExpr() {
        Expression first = powExpr();
        Expression ret = first;

        while (have(NonTerminal.MUL_OP)) {
            Token op = expectRetrieve(NonTerminal.MUL_OP);
            Expression second = powExpr();

            switch (op.kind()) {
                case MUL:
                    ret = new Multiplication(op.lineNumber(), op.charPosition(), ret, second);
                    break;
                case DIV:
                    ret = new Division(op.lineNumber(), op.charPosition(), ret, second);
                    break;
                case MOD:
                    ret = new Modulo(op.lineNumber(), op.charPosition(), ret, second);
                    break;
                case AND:
                    ret = new LogicalAnd(op.lineNumber(), op.charPosition(), ret, second);
                    break;
                default:
            }
        }

        return ret;
    }

    // powExpr = groupExpr { powOp groupExpr }
    private Expression powExpr() {
        Expression first = groupExpr();
        Expression ret = first;

        while (have(Token.Kind.POW)) {
            Token op = expectRetrieve(Token.Kind.POW);
            Expression second = groupExpr();
            ret = new Power(op.lineNumber(), op.charPosition(), ret, second);
        }

        return ret;
    }

    // groupExpr = literal | designator | "not" relExpr | relation | funcCall
    private Expression groupExpr() {
        if (have(NonTerminal.LITERAL)) {
            return literal();
        } else if (have(NonTerminal.DESIGNATOR)) {
            return designator(false);
        } else if (have(Token.Kind.NOT)) {
            Token op = expectRetrieve(Token.Kind.NOT);
            Expression expr = relExpr();
            return new LogicalNot(op.lineNumber(), op.charPosition(), expr);
        } else if (have(NonTerminal.RELATION)) {
            return relation();
        } else if (have(NonTerminal.FUNC_CALL)) {
            return funcCall();
        } else {
            expect(NonTerminal.GROUP_EXPR);
            return null;
        }
    }
}
