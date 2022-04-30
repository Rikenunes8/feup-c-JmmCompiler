package pt.up.fe.comp;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public interface ReportGenerator {
    List<Report> getReports();
    void addReport(JmmNode node, String message);

}
