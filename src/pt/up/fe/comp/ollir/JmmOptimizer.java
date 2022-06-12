package pt.up.fe.comp.ollir;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.analysis.SymbolTableBuilder;

import pt.up.fe.comp.optimization.ConstPropagationTable;
import pt.up.fe.comp.optimization.ConstantFoldingVisitor;
import pt.up.fe.comp.optimization.ConstantPropagationVisitor;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.optimization.register_allocation.RegisterAllocation;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        if (!semanticsResult.getConfig().containsKey("optimize") || !semanticsResult.getConfig().get("optimize").equals("true"))
            return semanticsResult;

        JmmNode root = semanticsResult.getRootNode();
        int counter = 1;
        while (counter > 0) {
            // Constant Propagation
            var constantPropagation = new ConstantPropagationVisitor();
            ConstPropagationTable table = new ConstPropagationTable(); // (name, const_value)
            constantPropagation.visit(root, table);
            counter = constantPropagation.getCounter();

            // Constant Folding
            var constantFolding = new ConstantFoldingVisitor();
            constantFolding.visit(root);
            counter += constantFolding.getCounter();
        }

        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        OllirGenerator ollirGenerator = new OllirGenerator((SymbolTableBuilder)semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());

        String ollirCode = ollirGenerator.getCode();

        System.out.println("\nOLLIR CODE:\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // TODO DEBUG
        System.out.println("Printing var tables");
        for (var method : ollirResult.getOllirClass().getMethods()) {
            var table = method.getVarTable();
            for (var ent : table.entrySet()) {
                System.out.println(ent.getKey() + "   -   " + ent.getValue().getVirtualReg());
            }
        }
        // TODO --------------------

        if (!ollirResult.getConfig().containsKey("registerAllocation")) return ollirResult;
        int nRegisters = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
        if (nRegisters == -1) return ollirResult;

        var ollirClass = ollirResult.getOllirClass();
        new RegisterAllocation(ollirClass).optimize(nRegisters);

        // TODO DEBUG
        System.out.println("Printing var tables optimized");
        for (var method : ollirClass.getMethods()) {
            var table = method.getVarTable();
            for (var ent : table.entrySet()) {
                System.out.println(ent.getKey() + "   -   " + ent.getValue().getVirtualReg());
            }
        }
        // TODO --------------------
        return ollirResult;
    }
}
