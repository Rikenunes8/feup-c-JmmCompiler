package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.analysis.AnalysisUtils.buildType;
import static pt.up.fe.comp.analysis.AnalysisUtils.getType;
import static pt.up.fe.comp.ast.AstNode.*;

public class ReturnCheckingVisitor extends SemanticAnalyserVisitor {
    public ReturnCheckingVisitor() {
        super();
        addVisit(RETURN_STATEMENT, this::visitReturnStatement);
    }

    private Boolean visitReturnStatement(JmmNode jmmNode, SymbolTableBuilder symbolTable) {
        var methodDeclarationOpt = jmmNode.getAncestor(METHOD_DECLARATION.toString());
        if (!methodDeclarationOpt.isPresent())
            return false;

        JmmNode returnExp = jmmNode.getJmmChild(0);

        String typeStr = methodDeclarationOpt.get().get("type");
        Type methodType = buildType(typeStr);
        Type returnType = getType(returnExp, symbolTable);

        if (returnType == null || methodType.equals(returnType))
            return true;
        this.addReport(jmmNode, "Return expression does not match method return type.");
        return false;
    }
}
