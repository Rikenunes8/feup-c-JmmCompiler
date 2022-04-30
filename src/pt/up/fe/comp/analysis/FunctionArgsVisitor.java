package pt.up.fe.comp.analysis;

import pt.up.fe.comp.ReportGenerator;
import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fe.comp.Utils.getType;
import static pt.up.fe.comp.Utils.isIdentifierDeclared;

public class FunctionArgsVisitor extends PreorderJmmVisitor<SymbolTableBuilder, Boolean> implements ReportGenerator {
    private List<Report> reports;

    public FunctionArgsVisitor() {
        this.reports = new ArrayList<>();
        addVisit("FunctionCall", this::visitFunctionCall);
    }

    private Boolean visitFunctionCall(JmmNode jmmNode, SymbolTableBuilder symbolTable) {
        String name = jmmNode.get("name");
        JmmNode left = jmmNode.getJmmParent().getJmmChild(0);

        if (left.getKind().equals("ThisLiteral")) {
            if (!symbolTable.getMethods().contains(name)) {
                if (symbolTable.getSuper() == null)
                    this.addReport(jmmNode, "method "+name+" does not exist in class "+symbolTable.getClassName());
                return true;
            }

            List<Type> params = symbolTable.getParameters(name).stream().map(Symbol::getType).collect(Collectors.toList());
            List<Type> args = jmmNode.getChildren().stream().map(arg -> getType(arg, symbolTable)).collect(Collectors.toList());
            if (params.size() != args.size()) {
                this.addReport(jmmNode,"method "+name+" in class "+symbolTable.getClassName()+" cannot be applied to given types");
                return true;
            }
            for (int i = 0; i < args.size(); i++) {
                Type argType = args.get(i);
                Type paramType = params.get(i);
                if (!argType.equals(paramType)) {
                    this.addReport(jmmNode, "error: incompatible types: "+argType.print()+" cannot be converted to "+paramType.print());
                }
            }
            return true;
        }
        else {
            if (isIdentifierDeclared(left, symbolTable)) {
                String typeName = getType(left, symbolTable).getName();
                if (!typeName.matches("boolean|int"))
                    return true;
                this.addReport(jmmNode, "Primitive type ("+typeName+") "+left.get("val")+" has no methods");
            }
            else {
                List<String> lastImports = symbolTable.getImports().stream()
                        .map(s -> s.split("\\."))
                        .map(strs -> strs[strs.length-1])
                        .collect(Collectors.toList());

                if (lastImports.contains(left.get("val")))
                    return true;
                this.addReport(jmmNode, "Unable to find "+left.get("val"));
            }
            return false;
        }
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
