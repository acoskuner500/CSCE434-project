package coco;

import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import types.*;

public class SymbolTable {

    // Stack of scoped symbol tables
    // Each symbol table is represented as a hash table
    Stack<HashMap<String, List<Symbol>>> scopedTables;

    // Reserved symbols
    public final static Symbol mainSymbol = new Symbol("main", new FuncType(new TypeList(), new VoidType()), false);
    public final static Symbol readIntSymbol = new Symbol("readInt", new FuncType(new TypeList(), new IntType()));
    public final static Symbol readBoolSymbol = new Symbol("readBool", new FuncType(new TypeList(), new BoolType()));
    // public final static Symbol readFloatSymbol = new Symbol("readFloat", new FuncType(new TypeList(), new FloatType()));
    public final static Symbol printIntSymbol = new Symbol("printInt", new FuncType(new TypeList(Arrays.asList(new IntType())), new VoidType()));
    public final static Symbol printBoolSymbol = new Symbol("printBool", new FuncType(new TypeList(Arrays.asList(new BoolType())), new VoidType()));
    // public final static Symbol printFloatSymbol = new Symbol("printFloat", new FuncType(new TypeList(Arrays.asList(new FloatType())), new VoidType()));
    public final static Symbol printlnSymbol = new Symbol("println", new FuncType(new TypeList(), new VoidType()));

    public SymbolTable () {
        // Initialize the scope stack
        scopedTables = new Stack<HashMap<String, List<Symbol>>>();

        // Initialize the global scope with predefined functions
        HashMap<String, List<Symbol>> globalScope = new HashMap<String, List<Symbol>>();
        ArrayList<Symbol> overloads = new ArrayList<Symbol>();
        overloads.add(readIntSymbol);
        globalScope.put("readInt", overloads);

        overloads = new ArrayList<Symbol>();
        overloads.add(readBoolSymbol);
        globalScope.put("readBool", overloads);

        // overloads = new ArrayList<Symbol>();
        // overloads.add(readFloatSymbol);
        // globalScope.put("readFloat", overloads);

        overloads = new ArrayList<Symbol>();
        overloads.add(printIntSymbol);
        globalScope.put("printInt", overloads);

        overloads = new ArrayList<Symbol>();
        overloads.add(printBoolSymbol);
        globalScope.put("printBool", overloads);

        // overloads = new ArrayList<Symbol>();
        // overloads.add(printFloatSymbol);
        // globalScope.put("printFloat", overloads);

        overloads = new ArrayList<Symbol>();
        overloads.add(printlnSymbol);
        globalScope.put("println", overloads);

        overloads = new ArrayList<Symbol>();
        overloads.add(mainSymbol);
        globalScope.put("main", overloads);

        // Add the global scope to the scope stack
        scopedTables.push(globalScope);
    }

    // lookup name in SymbolTable
    public List<Symbol> lookup (String name) throws SymbolNotFoundError {
        // Look through all scopes starting from the top
        for (int i = scopedTables.size() - 1; i >= 0; i--) {
            HashMap<String, List<Symbol>> scope = scopedTables.elementAt(i);

            // Check if the symbol is defined in this scope
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }

        throw new SymbolNotFoundError(name);
    }

    public Symbol insert (String name, Type type, boolean isGlobal) throws RedeclarationError {
        // Check if symbol already exists in current scope
        HashMap<String, List<Symbol>> currScope = scopedTables.lastElement();

        if (currScope.containsKey(name)) {
            List<Symbol> overloads = currScope.get(name);

            // Functions can be overloaded
            if (type instanceof FuncType) {
                // Cannot redeclare with same type (or parameters for function)
                for (Symbol s : overloads) {
                    if (type.equals(s.type())) {
                        throw new RedeclarationError(name);
                    }
                }

                // If different from every overload, can add
                Symbol newOverload = new Symbol(name, type);
                currScope.get(name).add(newOverload);
                return newOverload;
            } 
            // Can overload if it is the only variable with that name 
            else {
                for (Symbol s : overloads) {
                    if (!(s.type() instanceof FuncType)) {
                        throw new RedeclarationError(name);            
                    }
                }

                // If the only variable with this name, can add
                Symbol newOverload = new Symbol(name, type);
                currScope.get(name).add(newOverload);
                return newOverload;
            }
        }

        Symbol s = new Symbol(name, type, isGlobal);

        // Should be added to the current scope
        currScope.put(name, new ArrayList<Symbol>());
        currScope.get(name).add(s);
        return s;
    }

    public Symbol insert (String name, Type type) throws RedeclarationError {
        // Check if symbol already exists in current scope
        HashMap<String, List<Symbol>> currScope = scopedTables.lastElement();

        if (currScope.containsKey(name)) {
            List<Symbol> overloads = currScope.get(name);

            // Functions can be overloaded
            if (type instanceof FuncType) {
                // Cannot redeclare with same type (or parameters for function)
                for (Symbol s : overloads) {
                    if (type.equals(s.type())) {
                        throw new RedeclarationError(name);
                    }
                }

                // If different from every overload, can add
                Symbol newOverload = new Symbol(name, type);
                currScope.get(name).add(newOverload);
                return newOverload;
            } 
            // Can overload if it is the only variable with that name 
            else {
                for (Symbol s : overloads) {
                    if (!(s.type() instanceof FuncType)) {
                        throw new RedeclarationError(name);            
                    }
                }

                // If the only variable with this name, can add
                Symbol newOverload = new Symbol(name, type);
                currScope.get(name).add(newOverload);
                return newOverload;
            }
        }

        Symbol s = new Symbol(name, type);

        // Should be added to the current scope
        currScope.put(name, new ArrayList<Symbol>());
        currScope.get(name).add(s);
        return s;
    }

    // Enter a new scope
    public void enter() {
        // Add new symbol table to the stack
        scopedTables.push(new HashMap<String, List<Symbol>>());
    }

    // Exit the current scope
    public void exit() {
        // Remove the most recent scope
        scopedTables.pop();
    }
}

class SymbolNotFoundError extends Error {

    private static final long serialVersionUID = 1L;
    private final String name;

    public SymbolNotFoundError (String name) {
        super("Symbol " + name + " not found.");
        this.name = name;
    }

    public String name () {
        return name;
    }
}

class RedeclarationError extends Error {

    private static final long serialVersionUID = 1L;
    private final String name;

    public RedeclarationError (String name) {
        super("Symbol " + name + " being redeclared.");
        this.name = name;
    }

    public String name () {
        return name;
    }
}
