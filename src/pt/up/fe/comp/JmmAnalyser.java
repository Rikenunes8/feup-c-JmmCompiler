package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.visitor.SymbolTableVisitor;
import pt.up.fe.comp.visitor.TypeCheckingVisitor;

import java.util.ArrayList;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    SimpleSymbolTable symbolTable;
    private List<Report> reports;

    public JmmAnalyser() {
        this.symbolTable = new SimpleSymbolTable();
        this.reports = new ArrayList<>();
    }

    public SimpleSymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void addReport(JmmNode node, String errorMessage) {
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 2, errorMessage)); // [TODO] acrescentar linha no nó da AST e mudar aqui o 2 para Integer.parseInt(node.get("line"))
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmNode root = (JmmNode) parserResult.getRootNode();

        new SymbolTableVisitor().visit(root, this);
        new TypeCheckingVisitor().visit(root, this);

        return new JmmSemanticsResult(parserResult, this.symbolTable, this.reports);
    }
}
