
// variables in the Jack code
// | name | type | kind | index (running index for aech kind)

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public enum Kind {
        STATIC, FIELD, ARG, VAR, NONE
    }

    // symbol class
    private static class Symbol {
        private final String type;
        private final Kind kind;
        private final int index;

        public Symbol(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getType() {
            return this.type;
        }

        public Kind getKind() {
            return this.kind;
        }

        public int getIndex() {
            return this.index;
        }
    }

    private final Map<String, Symbol> classTable; // name->(type,kind,index)
    private final Map<String, Symbol> subroutineTable; // name->(type,kind,index)
    private final Map<Kind, Integer> indexTable; // kind->index

    public SymbolTable() {
        classTable = new HashMap<>();
        subroutineTable = new HashMap<>();
        indexTable = new HashMap<>();

        indexTable.put(Kind.STATIC, 0);
        indexTable.put(Kind.FIELD, 0);
        indexTable.put(Kind.ARG, 0);
        indexTable.put(Kind.VAR, 0);

    }

    public void reset() {
        // clears the **subroutine-level symbol table** and resets the index counters
        // for local variables (`VAR`) and arguments (`ARG`) back to `0`

        subroutineTable.clear();

        indexTable.put(Kind.VAR, 0);
        indexTable.put(Kind.ARG, 0);

    }

    public void define(String name, String type, Kind kind) {
        int i = indexTable.get(kind);
        Symbol symbol = new Symbol(type, kind, i);
        indexTable.put(kind, i + 1);
        if (kind == Kind.STATIC || kind == Kind.FIELD) {
            // classTable
            classTable.put(name, symbol);
        } else if (kind == Kind.ARG || kind == Kind.VAR) {
            subroutineTable.put(name, symbol);
        }
    }

    public int varCount(Kind kind) { // how many variables of specific kind
        return indexTable.getOrDefault(kind, 0);
    }

    public String kindOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).getKind().toString();
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).getKind().toString();
        }
        return "NONE";
    }

    public String typeOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).getType();
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).getType();
        }
        return null;
    }

    public int indexOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).getIndex();
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).getIndex();
        }

        return -1;
    }

}