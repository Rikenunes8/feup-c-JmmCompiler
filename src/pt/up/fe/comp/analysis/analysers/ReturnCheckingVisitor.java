package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.Utils.buildType;
import static pt.up.fe.comp.Utils.getType;

public class ReturnCheckingVisitor extends SemanticAnalyserVisitor {
    public ReturnCheckingVisitor() {
        super();
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
}
