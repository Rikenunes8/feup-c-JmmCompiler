package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
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

import static pt.up.fe.comp.visitor.Utils.getType;

public class FunctionArgsVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {
    public FunctionArgsVisitor() {
        addVisit("FunctionCall", this::visitFunctionCall);
    }

    private Boolean visitFunctionCall(JmmNode jmmNode, JmmAnalyser jmmAnalyser) {
        System.out.println("Visiting: "+ jmmNode.get("name"));
        SymbolTable symbolTable = jmmAnalyser.getSymbolTable();
        String name = jmmNode.get("name");
        JmmNode left = jmmNode.getJmmParent().getJmmChild(0);

        if (left.getKind().equals("ThisLiteral")) {
            if (!symbolTable.getMethods().contains(name)) {
                if (symbolTable.getSuper() == null) {
                    jmmAnalyser.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                            "method "+name+" does not exists in class "+symbolTable.getClassName()));
                }
                return true;
            }

            List<Type> params = symbolTable.getParameters(name).stream()
                    .map(Symbol::getType).collect(Collectors.toList());
            List<Type> args = argsTypes(jmmNode);
            if (params.size() != args.size()) {
                jmmAnalyser.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                        "method "+name+" in class "+symbolTable.getClassName()+" cannot be applied to given types"));
                return true;
            }
            for (int i = 0; i < args.size(); i++) {
                Type argType = args.get(i);
                Type paramType = params.get(i);
                if (!argType.equals(paramType)) {
                    jmmAnalyser.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                            "error: incompatible types: "+argType.print()+" cannot be converted to "+paramType.print()));
                }
            }
        }
        else {
            // TODO check if class was imported
        }
        return true;
    }

    private List<Type> argsTypes(JmmNode methodCall) {
        List<Type> args = new ArrayList<>();
        for (JmmNode arg : methodCall.getChildren()) {
            args.add(getType(arg));
        }
        return args;
    }
}
