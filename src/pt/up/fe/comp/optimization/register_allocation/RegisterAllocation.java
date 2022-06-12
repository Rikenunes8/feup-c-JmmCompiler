package pt.up.fe.comp.optimization.register_allocation;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterAllocation {
    private final ClassUnit ollir;
    private final Map<String, List<String>> interferenceGraph;


    public RegisterAllocation(ClassUnit ollir) {
        this.ollir = ollir;
        this.interferenceGraph = new HashMap<>();
    }
    public void optimize() {
        this.ollir.buildCFGs();
        for (var method : this.ollir.getMethods()) {
            this.allocateRegisters(method);
        }
    }

    private void allocateRegisters(Method method) {
        var liveliness = new Liveliness(method.getInstructions());
        liveliness.show();
        var webs = liveliness.getWebs();
        buildInterferenceGraph(webs);
    }

    private void buildInterferenceGraph(Map<String, LivelinessRange> webs) {
        var variablesNames = webs.keySet();
        for (String w1 : variablesNames) {
            LivelinessRange wr1 = webs.get(w1);
            List<String> interferences = new ArrayList<>();

            for (String w2 : variablesNames) {
                if (w1.equals(w2)) continue;
                LivelinessRange wr2 = webs.get(w2);

                if (wr1.getStart() <= wr2.getStart() && wr1.getEnd() > wr2.getStart()
                        || wr1.getStart() <= wr2.getEnd() && wr1.getEnd() >= wr2.getEnd()
                        || wr1.getStart() >= wr2.getStart() && wr1.getEnd() <= wr2.getEnd()) {
                    interferences.add(w2);
                }
            }

            interferenceGraph.put(w1, interferences);
        }
    }
}
