package pt.up.fe.comp.optimization;

import java.util.HashMap;
import java.util.Map;

public class ConstPropagationTable {
    private final Map<String, String> constants;
    private boolean propagating;

    public ConstPropagationTable() {
        this.constants = new HashMap<>();
        this.propagating = true;
    }
    public ConstPropagationTable(ConstPropagationTable table) {
        this.propagating = table.propagating;
        this.constants = new HashMap<>(table.constants);
    }

    public Map<String, String> getConstants() {
        return constants;
    }
    public void put(String key, String value) {
        this.constants.put(key, value);
    }
    public String get(String key) {
        if (!this.constants.containsKey(key)) return null;
        return this.constants.get(key);
    }
    public void remove(String key) {
        this.constants.remove(key);
    }

    public boolean isPropagating() {
        return propagating;
    }

    public void setPropagating(boolean propagating) {
        this.propagating = propagating;
    }

    public void clear() {
        this.constants.clear();
        this.propagating = true;
    }
}
