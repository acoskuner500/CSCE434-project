package coco;

import java.util.ArrayList;
import types.Type;

public class Symbol { 

    private String name;
    private Type type;

    public Symbol (String name, Type t) {
        this.name = name;
        this.type = t;
    }

    public String name () {
        return name;
    }

    public Type type() {
        return type;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }
}
