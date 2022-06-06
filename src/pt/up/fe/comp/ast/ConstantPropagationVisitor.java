package pt.up.fe.comp.ast;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.ast.AstNode.METHOD_DECLARATION;

public class ConstantPropagationVisitor extends AJmmVisitor<Boolean, Boolean> {
    public ConstantPropagationVisitor() {
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
    }

    private Boolean visitMethodDeclaration(JmmNode jmmNode, Boolean aBoolean) {
        return false;
    }
}
