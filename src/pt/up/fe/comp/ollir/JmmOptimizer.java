package pt.up.fe.comp.ollir;
import java.util.Collections;
import pt.up.fe.comp.analysis.SymbolTableBuilder;

import pt.up.fe.comp.optimization.ConstPropagationTable;
import pt.up.fe.comp.optimization.ConstantFoldingVisitor;
import pt.up.fe.comp.optimization.ConstantPropagationVisitor;
import pt.up.fe.comp.optimization.DeadCodeEliminationVisitor;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.optimization.WhileToDoWhile;
import pt.up.fe.comp.optimization.register_allocation.RegisterAllocation;


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

        var deadCodeElimination = new DeadCodeEliminationVisitor();
        deadCodeElimination.visit(root);

        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        OllirGenerator ollirGenerator = new OllirGenerator((SymbolTableBuilder)semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());

        String ollirCode = ollirGenerator.getCode();

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        if (ollirResult.getConfig().containsKey("optimize")
                && ollirResult.getConfig().get("optimize").equals("true")) {
            ollirResult = new WhileToDoWhile(ollirResult).optimize();
        }

        if (ollirResult.getConfig().containsKey("registerAllocation")) {
            int nRegisters = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
            ollirResult = new RegisterAllocation(ollirResult).optimize(nRegisters);
        }

        return ollirResult;
    }
}
