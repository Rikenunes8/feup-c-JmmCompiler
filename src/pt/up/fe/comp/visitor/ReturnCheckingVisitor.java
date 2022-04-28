package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import static pt.up.fe.comp.visitor.Utils.buildType;
import static pt.up.fe.comp.visitor.Utils.getType;

public class ReturnCheckingVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {
    public ReturnCheckingVisitor() {
        addVisit("ReturnStatement", this::visitReturnStatement);
    }

    private Boolean visitReturnStatement(JmmNode jmmNode, JmmAnalyser jmmAnalyser) {
        if (!jmmNode.getAncestor("PublicMethod").isPresent())
            return false;
        String typeStr = jmmNode.getAncestor("PublicMethod").get().get("type");
        Type methodType = buildType(typeStr);
        Type returnType = getType(jmmNode, jmmAnalyser.getSymbolTable());

        if (methodType.equals(returnType))
            return true;
        jmmAnalyser.addReport(jmmNode, "Return expression does not match method return type.");
        return false;
    }
}
