package types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeList extends Type implements Iterable<Type> {

    private List<Type> list;

    public TypeList () {
        list = new ArrayList<>();
    }

    public TypeList(List<Type> list) {
        this.list = list;
    }

    public void append (Type type) {
        list.add(type);
    }

    public List<Type> getList () {
        return list;
    }

    @Override
    public Iterator<Type> iterator () {
        return list.iterator();
    }

    //TODO more helper here
    @Override
    public String toString() {
        String ret = "";
        int remain = list.size();

        for (Type t: list) {
            ret += t;

            if (--remain > 0) {
                ret += ",";
            }
        }

        return ret;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof TypeList)) {
            return false;
        }

        TypeList tList = (TypeList) o;

        // Unequal if size is different
        if (this.list.size() != tList.list.size()) {
            return false;
        }

        // If all parameters are the same type they are equal
        for (int i = 0; i < this.list.size(); i++) {
            if (!this.list.get(i).getClass().equals(tList.list.get(i).getClass())) {
                return false;
            }
        }

        return true;
    }
}
