package pt.up.fe.comp.analysis;

import pt.up.fe.comp.ReportGenerator;
import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp.Utils.buildType;
import static pt.up.fe.comp.Utils.getType;

public class ReturnCheckingVisitor extends PreorderJmmVisitor<SymbolTableBuilder, Boolean> implements ReportGenerator {
    private List<Report> reports;

    public ReturnCheckingVisitor() {
        this.reports = new ArrayList<>();
        addVisit("ReturnStatement", this::visitReturnStatement);
    }

    private Boolean visitReturnStatement(JmmNode jmmNode, SymbolTableBuilder symbolTable) {
        if (!jmmNode.getAncestor("PublicMethod").isPresent())
            return false;

        JmmNode returnExp = jmmNode.getJmmChild(0);
        if (returnExp.getKind().equals("DotExp"))
            return true;

        String typeStr = jmmNode.getAncestor("PublicMethod").get().get("type");
        Type methodType = buildType(typeStr);
        Type returnType = getType(returnExp, symbolTable);

        if (methodType.equals(returnType))
            return true;
        this.addReport(jmmNode, "Return expression does not match method return type.");
        return false;
    }

    @Override
    public List<Report> getReports() {
        return this.reports;
    }

    @Override
    public void addReport(JmmNode node, String message) {
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")) , message));
    }
}
