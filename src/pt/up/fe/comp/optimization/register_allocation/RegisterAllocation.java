package pt.up.fe.comp.optimization.register_allocation;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

import java.util.*;

public class RegisterAllocation {
    private final ClassUnit ollir;
    private final Map<String, List<String>> interferenceGraph;
    private final Stack<String> coloringStack;
    private final Map<String, Integer> coloredGraph;
    private int nRegisters;
    private Method currentMethod;

    public RegisterAllocation(ClassUnit ollir) {
        this.ollir = ollir;
        this.interferenceGraph = new HashMap<>();
        this.coloredGraph = new HashMap<>();
        this.coloringStack = new Stack<>();
    }
    public void optimize(int nRegisters) {
        this.nRegisters = nRegisters;
        this.ollir.buildCFGs();
        for (var method : this.ollir.getMethods()) {
            this.currentMethod = method;
            this.allocateRegisters(method);
        }
    }

    private void allocateRegisters(Method method) {
        var liveliness = new Liveliness(method.getInstructions());
        liveliness.show();
        var webs = liveliness.getWebs();
        this.buildInterferenceGraph(webs);
        this.colorGraph();
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

    private int getMethodNFixedRegisters(Method method) {
        return method.getParams().size() + (method.isStaticMethod() ? 0 : 1);
    }

    private void colorGraph() {
        int minNRegisters = this.getMinNRegistersPossible();
        if (nRegisters < minNRegisters && nRegisters > 0) {
            // report it to the user with the minimum possible
        }
        else {
            if (nRegisters == 0) nRegisters = minNRegisters;
            Map<String, List<String>> copyGraph = new HashMap<>(interferenceGraph);

            while (!copyGraph.isEmpty()) {
                boolean success = nextNodeToStack(copyGraph);
                assert success;
            }

            while (!coloringStack.isEmpty()) {
                String variable = coloringStack.pop();
                int register = this.getRegister(variable);
                assert register != -1;
                coloredGraph.put(variable, register);
            }
        }
    }

    private boolean nextNodeToStack(Map<String, List<String>> copyGraph) {
        for (String web : copyGraph.keySet()) {
            if (interferenceGraph.get(web).size() < nRegisters) {
                coloringStack.add(web);
                copyGraph.remove(web);
                return true;
            }
        }
        return false;
    }

    private int getRegister(String variable) {
        int startRegister = this.getMethodNFixedRegisters(currentMethod);

        Set<Integer> registersAvailable = new HashSet<>();
        for (int i = startRegister; i < nRegisters; i++) registersAvailable.add(i);

        var interferences = interferenceGraph.get(variable);
        for (var interference : interferences) {
            if (!coloredGraph.containsKey(interference)) continue;
            int registerUsed = coloredGraph.get(interference);
            registersAvailable.remove(registerUsed);
        }

        return registersAvailable.stream().findFirst().orElse(-1);
    }

    private int getMinNRegistersPossible() {
        // clique
        return this.getMethodNFixedRegisters(currentMethod);
    }
}