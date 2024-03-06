package coco;

import types.Type;

public class Symbol { 

    private String name;
    private Type type;
    private Space allocSpace;

    private enum Space {
        GLOBAL,
        LOCAL,
        PARAMETER,
    };

    public Symbol (String name, Type t) {
        this.name = name;
        this.type = t;
        this.allocSpace = Space.LOCAL;
    }

    public Symbol (String name, Type t, boolean isGlobalVar) {
        this.name = name;
        this.type = t;
        this.allocSpace = isGlobalVar ? Space.GLOBAL : Space.PARAMETER;
    }

    public String name () {
        return name;
    }

    public Type type() {
        return type;
    }

    public boolean isGlobalVariable() {
        return allocSpace == Space.GLOBAL;
    }

    public boolean isParameter() {
        return allocSpace == Space.PARAMETER;
    }

    public boolean isLocal() {
        return allocSpace == Space.LOCAL;
    }

    @Override
    public String toString() {
        return name + (type != null ? ":" + type : "");
    }
}
