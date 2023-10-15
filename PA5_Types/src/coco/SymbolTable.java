package coco;

import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import coco.Symbol;
import types.*;

public class SymbolTable {

    // Stack of scoped symbol tables
    // Each symbol table is represented as a hash table
    Stack<HashMap<String, List<Symbol>>> scopedTables;

    public SymbolTable () {
        // Initialize the scope stack
        scopedTables = new Stack<HashMap<String, List<Symbol>>>();

        // Initialize the global scope with predefined functions
        HashMap<String, List<Symbol>> globalScope = new HashMap<String, List<Symbol>>();
        ArrayList<Symbol> overloads = new ArrayList<Symbol>();
        overloads.add(new Symbol("readInt", new FuncType(new TypeList(), new IntType())));
        globalScope.put("readInt", overloads);

        overloads = new ArrayList<Symbol>();
        overloads.add(new Symbol("readBool", new FuncType(new TypeList(), new BoolType())));
        globalScope.put("readBool", overloads);

        ArrayList<Type> params = new ArrayList<Type>();
        params.add(new IntType());
        overloads = new ArrayList<Symbol>();
        overloads.add(new Symbol("printInt", new FuncType(new TypeList(params), new VoidType())));
        globalScope.put("printInt", overloads);

        params = new ArrayList<Type>();
        params.add(new BoolType());
        overloads = new ArrayList<Symbol>();
        overloads.add(new Symbol("printBool", new FuncType(new TypeList(params), new VoidType())));
        globalScope.put("printBool", overloads);

        overloads = new ArrayList<Symbol>();
        overloads.add(new Symbol("println", new FuncType(new TypeList(), new VoidType())));
        globalScope.put("println", overloads);

        params = new ArrayList<Type>();
        ArrayList<Integer> arr = new ArrayList<Integer>();
        arr.add(null);

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
                // TODO: Type checking should figure out correct symbol
                return scope.get(name);
            }
        }

        throw new SymbolNotFoundError(name);
    }

    public Symbol insert (String name, Type type) throws RedeclarationError {
        // Check if symbol already exists in current scope
        HashMap<String, List<Symbol>> currScope = scopedTables.lastElement();

        if (currScope.containsKey(name)) {
            List<Symbol> overloads = currScope.get(name);

            // Only functions can be overloaded
            if (type instanceof FuncType && overloads.get(0).type() instanceof FuncType) {
                // Cannot redeclare with same parameters
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
            throw new RedeclarationError(name);
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
