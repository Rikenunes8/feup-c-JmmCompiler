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

    public void addReport(JmmNode node, String errorMessage) {
        // [TODO] acrescentar linha e coluna no nó da AST
        // reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), errorMessage));
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 2, 1, errorMessage)); 
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmNode root = parserResult.getRootNode();

        new SymbolTableVisitor().visit(root, this);
        //new TypeCheckingVisitor().visit(root, this);
        //new FunctionArgsVisitor().visit(root, this);
        //new ReturnCheckingVisitor().visit(root, this);

        return new JmmSemanticsResult(parserResult, this.symbolTable, this.reports);
    }
}
