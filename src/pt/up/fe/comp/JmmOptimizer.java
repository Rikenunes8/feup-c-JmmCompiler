package pt.up.fe.comp;

import java.util.List;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return JmmOptimization.super.optimize(semanticsResult);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        JmmNode node = semanticsResult.getRootNode();
        SymbolTable symbolTable = semanticsResult.getSymbolTable();
        List<Report> reports = semanticsResult.getReports();
        String ollirCode = ast_to_ollir(node);

        OllirResult ollirResult = new OllirResult(semanticsResult, ollirCode, reports);

        return ollirResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return JmmOptimization.super.optimize(ollirResult);
    }
}
