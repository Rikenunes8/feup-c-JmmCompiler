package pt.up.fe.comp.ast;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Map;

import static pt.up.fe.comp.ast.AstNode.*;

public class ConstantPropagationVisitor extends AJmmVisitor<Map<String, String>, Boolean> {
    private int counter;

    public ConstantPropagationVisitor() {
        this.counter = 0;
        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECLARATION, this::visitClassDeclaration);
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
        addVisit(ASSIGNMENT_STATEMENT, this::visitAssignmentStatement);
        addVisit(IF_STATEMENT, this::visitIfStatement);
        addVisit(WHILE_STATEMENT, this::visitWhileStatement);
        addVisit(EXPRESSION_STATEMENT, this::visitExpressionStatement);
        addVisit(SCOPE, this::visitScope);
        addVisit(AND_EXP, this::visitExpression);
        addVisit(LESS_EXP, this::visitExpression);
        addVisit(ADD_EXP, this::visitExpression);
        addVisit(MULT_EXP, this::visitExpression);
        addVisit(SUB_EXP, this::visitExpression);
        addVisit(DIV_EXP, this::visitExpression);
        addVisit(NOT_EXP, this::visitExpression);
        addVisit(ARRAY_ACCESS_EXP, this::visitArrayAccessExp);
        addVisit(DOT_EXP, this::visitDotExp);
        addVisit(NEW_INT_ARRAY, this::visitIntArray);
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
        addVisit(IDENTIFIER_LITERAL, this::visitIdentifier);
        setDefaultVisit(this::visitDefault);
    }


    private Boolean visitDefault(JmmNode jmmNode, Map<String, String> constants) {
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
            if (node.getKind().equals(WHILE_STATEMENT.toString())) return false;
            this.visit(node, constants);
        }
        return true;
    }

    private Boolean visitAssignmentStatement(JmmNode jmmNode, Map<String, String> constants) {
        var left = jmmNode.getJmmChild(0);
        var right = jmmNode.getJmmChild(1);

        if (left.getKind().equals(ARRAY_ACCESS_EXP.toString())) {
            this.visit(left, constants);
            this.visit(right, constants);
            return true;
        }

        if (right.getKind().equals(TRUE_LITERAL.toString())) {
            constants.put(left.get("val"), "true");
        }
        else if (right.getKind().equals(FALSE_LITERAL.toString())) {
            constants.put(left.get("val"), "false");
        }
        else if (right.getKind().equals(INTEGER_LITERAL.toString())) {
            constants.put(left.get("val"), right.get("val"));
        }
        else {
            this.visit(right, constants);
            if (left.getKind().equals(IDENTIFIER_LITERAL.toString())) {
                constants.remove(left.get("val"));
            }
        }
        return true;
    }

    private Boolean visitScope(JmmNode jmmNode, Map<String, String> constants) {
        for (var statement : jmmNode.getChildren()) {
            this.visit(statement, constants);
        }
        return true;
    }

    private Boolean visitExpressionStatement(JmmNode jmmNode, Map<String, String> constants) {
        this.visit(jmmNode.getJmmChild(0), constants);
        return true;
    }

    private Boolean visitWhileStatement(JmmNode jmmNode, Map<String, String> constants) {
        return true;
    }

    private Boolean visitIfStatement(JmmNode jmmNode, Map<String, String> constants) {
        for (var block : jmmNode.getChildren()) {
            this.visit(block.getJmmChild(0), constants); // Condition, IfBlock & ElseBlock
        }
        return true;
    }

    private Boolean visitExpression(JmmNode jmmNode, Map<String, String> constants) {
        for (var child : jmmNode.getChildren()) {
            this.visit(child, constants);
        }
        return true;
    }

    private Boolean visitArrayAccessExp(JmmNode jmmNode, Map<String, String> constants) {
        this.visit(jmmNode.getJmmChild(1), constants);
        return true;
    }

    private Boolean visitIntArray(JmmNode jmmNode, Map<String, String> constants) {
        this.visit(jmmNode.getJmmChild(0), constants);
        return true;
    }

    private Boolean visitDotExp(JmmNode jmmNode, Map<String, String> constants) {
        this.visit(jmmNode.getJmmChild(1), constants);
        return true;
    }

    private Boolean visitFunctionCall(JmmNode jmmNode, Map<String, String> constants) {
        for (var arg : jmmNode.getChildren()) {
            this.visit(arg, constants);
        }
        return true;
    }

    private Boolean visitIdentifier(JmmNode jmmNode, Map<String, String> constants) {
        String name = jmmNode.get("val");
        if (constants.containsKey(name)) {
            String value = constants.get(name);
            JmmNode newNode;
            if (value.equals("true")) {
                newNode = new JmmNodeImpl(TRUE_LITERAL.toString());
            } else if (value.equals("false")) {
                newNode = new JmmNodeImpl(FALSE_LITERAL.toString());
            } else {
                newNode = new JmmNodeImpl(INTEGER_LITERAL.toString());
                newNode.put("val", value);
            }
            newNode.put("col", jmmNode.get("col"));
            newNode.put("line", jmmNode.get("line"));
            this.counter++;
            jmmNode.replace(newNode);
        }
        return true;
    }

    public int getCounter() {
        return counter;
    }
}
