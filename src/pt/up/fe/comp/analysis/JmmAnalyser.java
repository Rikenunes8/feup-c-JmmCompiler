package pt.up.fe.comp.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
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

        var symbolTableVisitor = new SymbolTableVisitor();
        symbolTableVisitor.visit(root, this.symbolTable);
        this.reports.addAll(symbolTableVisitor.getReports());

        new TypeCheckingVisitor().visit(root, this.symbolTable);
        //new FunctionArgsVisitor().visit(root, this.symbolTable);
        new ReturnCheckingVisitor().visit(root, this.symbolTable);

        return new JmmSemanticsResult(parserResult, this.symbolTable, this.reports);
    }
}
