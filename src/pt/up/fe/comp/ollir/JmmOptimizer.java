package pt.up.fe.comp.ollir;

import java.util.Collections;

import pt.up.fe.comp.Utils;
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
        Utils.setUtils(semanticsResult.getConfig());

        // Print the AST TODO remove
        if (Utils.debug) {
            System.out.println("\n-------- AST --------");
            System.out.println((semanticsResult.getRootNode()).sanitize().toTree());
            System.out.println("---------------------\n");
        }

        if (!Utils.optimize) return semanticsResult;

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

        // Print the AST
        if (Utils.debug) { // TODO remove
            System.out.println("\n-------- AST OPTIMIZED --------");
            System.out.println((semanticsResult.getRootNode()).sanitize().toTree());
            System.out.println("---------------------\n");
        }

        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        Utils.setUtils(semanticsResult.getConfig());

        OllirGenerator ollirGenerator = new OllirGenerator((SymbolTableBuilder)semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());

        String ollirCode = ollirGenerator.getCode();

        // Print the OLLIR code
        if (Utils.debug) {
            System.out.println("\n--------- OLLIR ---------");
            System.out.println(ollirCode);
            System.out.println("-------------------------\n");
        }

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        Utils.setUtils(ollirResult.getConfig());

        if (Utils.optimize) ollirResult = new WhileToDoWhile(ollirResult).optimize();

        // Print the OLLIR code
        if (Utils.debug) { // TODO
            System.out.println("\n--------- OLLIR OPTIMIZE ---------");
            System.out.println(ollirResult.getOllirCode());
            System.out.println("-------------------------\n");
        }
        // TODO REMOVE
        System.out.println("Var table:");
        for (var method : ollirResult.getOllirClass().getMethods()) {
            for (var t : method.getVarTable().entrySet()) {
                System.out.println(t.getKey());
                System.out.println(t.getValue().getVirtualReg() + " - " + t.getValue().getVarType());
            }
        }

        if (ollirResult.getConfig().containsKey("registerAllocation")) {
            int nRegisters = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
            ollirResult = new RegisterAllocation(ollirResult).optimize(nRegisters);
        }

        return ollirResult;
    }
}
