package pt.up.fe.comp.optimization.register_allocation;

import java.util.HashSet;
import java.util.Set;

public class UseDef {
    private Set<String> def;
    private Set<String> use;

    public UseDef() {
        this.def = new HashSet<>();
        this.use = new HashSet<>();
    }

    public void addDef(String def) {
        this.def.add(def);
    }
    public void addUse(String use) {
        this.use.add(use);
    }

    public Set<String> getUse() {
        return use;
    }
    public Set<String> getDef() {
        return def;
    }
}
