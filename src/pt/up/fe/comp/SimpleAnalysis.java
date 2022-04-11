package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleAnalysis implements JmmAnalysis {
    SymbolTable symbolTable;
    private List<Report> reports;

    public SimpleAnalysis() {
        this.symbolTable = new SimpleSymbolTable();
        this.reports = new ArrayList<>();
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public List<Report> getReports() {
        return reports;
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmNode root = (JmmNode) parserResult.getRootNode();

        // new SymbolTableVisitor(root, symbolTable);

        return new JmmSemanticsResult(parserResult, this.symbolTable, this.reports);
    }
}
