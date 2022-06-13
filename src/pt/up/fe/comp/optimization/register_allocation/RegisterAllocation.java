package pt.up.fe.comp.optimization.register_allocation;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.Utils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class RegisterAllocation {
    private final OllirResult ollirResult;
    private final ClassUnit ollir;
    private final Map<String, Set<String>> interferenceGraph;
    private Method currentMethod;
    private int minNRegistersForMethod;

    public RegisterAllocation(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.ollir = ollirResult.getOllirClass();
        this.interferenceGraph = new HashMap<>();
    }
    public OllirResult optimize(int nRegisters) {
        if (nRegisters == -1) return ollirResult;
        this.ollir.buildCFGs();
        for (var method : this.ollir.getMethods()) {
            this.currentMethod = method;
            this.interferenceGraph.clear();
            boolean possible = this.allocateRegisters(method, nRegisters);
            if (!possible) {
                ollirResult.getReports().add(new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, -1,
                        "Method " + method.getMethodName() + " needs at least " + minNRegistersForMethod + "registers"));
            }
        }
        return ollirResult;
    }

    private boolean allocateRegisters(Method method, int nRegisters) {
        var liveliness = new Liveliness(method.getInstructions());
        if (Utils.debug) {
            System.out.println("----- REGISTER ALLOCATION ------");
            liveliness.show();
        }
        var webs = liveliness.getWebs();
        this.buildInterferenceGraph(webs);

        final Map<String, Integer> coloredGraph = new HashMap<>();
        if (nRegisters == 0) {
            minNRegistersForMethod = this.getMinNRegistersPossible(coloredGraph);
            if (Utils.debug)
                System.out.println("Minimum number of registers for method \""+ this.currentMethod.getMethodName() +"\": " + minNRegistersForMethod);
            coloredGraph.clear();
            this.colorGraph(coloredGraph, minNRegistersForMethod);
        }
        else if (!this.colorGraph(coloredGraph, nRegisters)) {
            minNRegistersForMethod = this.getMinNRegistersPossible(coloredGraph);
            if (Utils.debug) System.out.println("-------------------------------");
            return false;
        }

        // Set virtual registers with colors
        var table = method.getVarTable();
        for (var variable : coloredGraph.keySet()) {
            Descriptor d = table.get(variable);
            var color = coloredGraph.get(variable);
            d.setVirtualReg(color);
        }
        if (Utils.debug) {
            System.out.println("Colored variables");
            System.out.println(coloredGraph);
            System.out.println("-------------------------------");
        }
        return true;
    }

    private void buildInterferenceGraph(Map<String, LivelinessRange> webs) {
        var variablesNames = webs.keySet();
        for (String w1 : variablesNames) {
            LivelinessRange wr1 = webs.get(w1);
            Set<String> interferences = new HashSet<>();

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

    private boolean colorGraph(Map<String, Integer> coloredGraph, int n) {
        Map<String, Set<String>> copyGraph = new HashMap<>(interferenceGraph);
        Stack<String> stack = new Stack<>();
        if (!this.graphToStack(copyGraph, stack, n)) return false;

        while (!stack.isEmpty()) {
            String variable = stack.pop();
            int register = this.getRegister(coloredGraph, variable, n);
            assert register != -1;
            coloredGraph.put(variable, register);
        }
        return true;
    }

    private int getRegister(Map<String, Integer> coloredGraph, String variable, int n) {
        int startRegister = this.getMethodNFixedRegisters(currentMethod);

        Set<Integer> registersAvailable = new HashSet<>();
        for (int i = startRegister; i < n + startRegister; i++) registersAvailable.add(i);

        var interferences = interferenceGraph.get(variable);
        for (var interference : interferences) {
            if (!coloredGraph.containsKey(interference)) continue;
            int registerUsed = coloredGraph.get(interference);
            registersAvailable.remove(registerUsed);
        }

        return registersAvailable.stream().findFirst().orElse(-1);
    }

    private boolean graphToStack(Map<String, Set<String>> graph, Stack<String> websStack, int n) {
        websStack.clear();
        while (!graph.isEmpty()) {
            if (!nextNodeToStack(graph, websStack, n)) return false;
        }
        return true;
    }
    private boolean nextNodeToStack(Map<String, Set<String>> graph, Stack<String> websStack, int n) {
        for (String web : graph.keySet()) {
            Set<String> interferences = interferenceGraph.get(web);
            if (interferences.size() < n) {
                for (var interference : interferences) {
                    if (graph.containsKey(interference))
                        graph.get(interference).remove(web);
                }
                websStack.add(web);
                graph.remove(web);
                return true;
            }
        }
        return false;
    }

    private int getMinNRegistersPossible(Map<String, Integer> coloredGraph) {
        int size = 1;
        while (!this.colorGraph(coloredGraph, size)) {
            size++;
        }
        return size;
    }
}