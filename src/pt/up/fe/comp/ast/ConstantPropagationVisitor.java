package pt.up.fe.comp.ast;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Map;

import static pt.up.fe.comp.ast.AstNode.*;

public class ConstantPropagationVisitor extends AJmmVisitor<Map<String, String>, Boolean> {
    public ConstantPropagationVisitor() {
        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECLARATION, this::visitClassDeclaration);
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
        addVisit(ASSIGNMENT_STATEMENT, this::visitAssignmentStatement);
        addVisit(IF_STATEMENT, this::visitIfStatement);
        addVisit(WHILE_STATEMENT, this::visitWhileStatement);
        addVisit(EXPRESSION_STATEMENT, this::visitExpressionStatement);
        addVisit(SCOPE, this::visitScope);
        setDefaultVisit(this::visitDefault);
    }

    private Boolean visitScope(JmmNode jmmNode, Map<String, String> stringStringMap) {
        return true;
    }

    private Boolean visitExpressionStatement(JmmNode jmmNode, Map<String, String> stringStringMap) {
        return true;
    }

    private Boolean visitWhileStatement(JmmNode jmmNode, Map<String, String> stringStringMap) {
        return true;
    }

    private Boolean visitIfStatement(JmmNode jmmNode, Map<String, String> stringStringMap) {
        return true;
    }

    private Boolean visitDefault(JmmNode jmmNode, Map<String, String> stringStringMap) {
        return true;
    }

    private Boolean visitProgram(JmmNode jmmNode, Map<String, String> constants) {
        this.visit(jmmNode.getJmmChild(1), constants);
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode jmmNode, Map<String, String> constants) {
        for (var node : jmmNode.getChildren()) {
            if (node.getKind().equals(METHOD_DECLARATION.toString()))
                this.visit(node, constants);
        }
        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode jmmNode, Map<String, String> constants) {
        for (var node : jmmNode.getChildren()) {
            this.visit(node, constants);
        }
        return false;
    }

    private Boolean visitAssignmentStatement(JmmNode jmmNode, Map<String, String> constants) {
        var left = jmmNode.getJmmChild(0);
        var right = jmmNode.getJmmChild(1);

        if (right.getKind().equals(TRUE_LITERAL.toString())) {
            constants.put(left.get("val"), "true");
        }
        else if (right.getKind().equals(FALSE_LITERAL.toString())) {
            constants.put(left.get("val"), "false");
        }
        else if (right.getKind().equals(INTEGER_LITERAL.toString())) {
            constants.put(left.get("val"), left.get("val"));
        }
        else {
            this.visit(right, constants);
            if (left.getKind().equals(IDENTIFIER_LITERAL.toString())) {
                constants.remove(left.get("val"));
            }
        }
        return true;
    }



}
