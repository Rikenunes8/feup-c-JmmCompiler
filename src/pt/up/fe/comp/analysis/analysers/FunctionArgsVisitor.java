package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fe.comp.analysis.AnalysisUtils.*;
import static pt.up.fe.comp.ast.AstNode.*;

public class FunctionArgsVisitor extends SemanticAnalyserVisitor {

    public FunctionArgsVisitor() {
        super();
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
    }

    private Boolean visitFunctionCall(JmmNode methodCall, SymbolTableBuilder symbolTable) {
        String methodName = methodCall.get("name");
        JmmNode left = methodCall.getJmmParent().getJmmChild(0);

        Type leftType = getType(left, symbolTable);

        if (symbolTable.getSuper() == null && left.getKind().equals(IDENTIFIER_LITERAL.toString())
                && left.get("val").equals(symbolTable.getClassName()) && !symbolTable.getStatic(methodName)) {
            this.addReport(methodCall, "Cannot call a non static method like a static method");
        } else if (left.getKind().equals(THIS_LITERAL.toString())
                || (leftType != null && !leftType.isArray() && leftType.getName().equals(symbolTable.getClassName()))) {
            if (left.getKind().equals(THIS_LITERAL.toString()) && methodCall.getAncestor(METHOD_DECLARATION.toString()).get().get("static").equals("true")) {
                this.addReport(methodCall, "Cannot call this object in a static method");
                return true;
            }
            if (symbolTable.getSuper() != null) {
                return true;
            }
            if (!symbolTable.hasMethod(methodName)) {
                this.addReport(methodCall, "Method "+methodName+" does not exist in class "+symbolTable.getClassName());
                return true;
            }

            List<Type> params = symbolTable.getParameters(methodName).stream().map(Symbol::getType).collect(Collectors.toList());
            List<Type> args = methodCall.getChildren().stream().map(arg -> getType(arg, symbolTable)).collect(Collectors.toList());
            if (params.size() != args.size()) {
                this.addReport(methodCall,"Number of arguments does not match number of parameters");
                return true;
            }
            for (int i = 0; i < args.size(); i++) {
                Type argType = args.get(i);
                Type paramType = params.get(i);
                if (argType == null || paramType == null)
                    continue;
                if (!argType.equals(paramType)) {
                    this.addReport(methodCall, "Incompatible types: "+argType.print()+" cannot be converted to "+paramType.print());
                }
            }
        }
        return true;
    }
}
