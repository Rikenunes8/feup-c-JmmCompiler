package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.analysers.*;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    SymbolTableBuilder symbolTable;
    private List<Report> reports;

    public JmmAnalyser() {
        this.symbolTable = new SymbolTableBuilder();
        this.reports = new ArrayList<>();
    }

    public SymbolTableBuilder getSymbolTable() {
        return this.symbolTable;
    }

    public List<Report> getReports() {
        return reports;
    }


    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmNode root = parserResult.getRootNode();

        List<SemanticAnalyserVisitor> anlysers = Arrays.asList(
                new SymbolTableVisitor(),
                new FieldsInStaticMethods(),
                new UndefinedVarsVisitor(),
                new CheckImportsVisitor(),
                new TypeCheckingVisitor(),
                new FunctionArgsVisitor(),
                new ReturnCheckingVisitor()
        );

        for (SemanticAnalyserVisitor analyser : anlysers) {
            analyser.visit(root, this.symbolTable);
            this.reports.addAll(analyser.getReports());
        }

        return new JmmSemanticsResult(parserResult, this.symbolTable, this.reports);
    }
}
