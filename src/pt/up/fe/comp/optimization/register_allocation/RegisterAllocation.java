package pt.up.fe.comp.optimization.register_allocation;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

public class RegisterAllocation {
    private final ClassUnit ollir;

    public RegisterAllocation(ClassUnit ollir) {
        this.ollir = ollir;
    }
    public void optimize() {
        this.ollir.buildCFGs();
        for (var method : this.ollir.getMethods()) {
            this.allocateRegisters(method);
        }
    }

    private void allocateRegisters(Method method) {
        var cfg = new ControlFlowGraph(method);
        var graph = cfg.getCfg();
        for (var block : graph) {
            block.show();
        }
    }
}
