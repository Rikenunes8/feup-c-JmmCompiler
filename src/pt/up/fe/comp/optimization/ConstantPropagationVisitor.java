package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import static pt.up.fe.comp.ast.AstNode.*;

public class ConstantPropagationVisitor extends AJmmVisitor<ConstPropagationTable, Boolean> {
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
        addVisit(RETURN_STATEMENT, this::visitReturnStatement);
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


    private Boolean visitDefault(JmmNode jmmNode, ConstPropagationTable table) {
        return true;
    }

    private Boolean visitProgram(JmmNode jmmNode, ConstPropagationTable table) {
        this.visit(jmmNode.getJmmChild(1), table);
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode jmmNode, ConstPropagationTable table) {
        for (var node : jmmNode.getChildren()) {
            if (node.getKind().equals(METHOD_DECLARATION.toString()))
                this.visit(node, table);
        }
        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode jmmNode, ConstPropagationTable table) {
        for (var node : jmmNode.getChildren()) {
            //if (node.getKind().equals(WHILE_STATEMENT.toString())) return false;
            this.visit(node, table);
        }
        return true;
    }

    private Boolean visitAssignmentStatement(JmmNode jmmNode, ConstPropagationTable table) {
        var left = jmmNode.getJmmChild(0);
        var right = jmmNode.getJmmChild(1);

        if (left.getKind().equals(ARRAY_ACCESS_EXP.toString())) {
            this.visit(left, table);
            this.visit(right, table);
            return true;
        }

        if (right.getKind().equals(TRUE_LITERAL.toString()) && table.isPropagating()) {
            table.put(left.get("val"), "true");
        }
        else if (right.getKind().equals(FALSE_LITERAL.toString()) && table.isPropagating()) {
            table.put(left.get("val"), "false");
        }
        else if (right.getKind().equals(INTEGER_LITERAL.toString()) && table.isPropagating()) {
            table.put(left.get("val"), right.get("val"));
        }
        else {
            this.visit(right, table);
            if (left.getKind().equals(IDENTIFIER_LITERAL.toString())) {
                table.remove(left.get("val"));
            }
        }
        return true;
    }

    private Boolean visitScope(JmmNode jmmNode, ConstPropagationTable table) {
        for (var statement : jmmNode.getChildren()) {
            this.visit(statement, table);
        }
        return true;
    }

    private Boolean visitExpressionStatement(JmmNode jmmNode, ConstPropagationTable table) {
        this.visit(jmmNode.getJmmChild(0), table);
        return true;
    }

    private Boolean visitWhileStatement(JmmNode jmmNode, ConstPropagationTable table) {
        table.setPropagating(false);
        for (var block : jmmNode.getChildren()) {
            this.visit(block.getJmmChild(0), table); // Condition, WhileBlock
        }
        table.setPropagating(true);
        for (var block : jmmNode.getChildren()) {
            this.visit(block.getJmmChild(0), table); // Condition, WhileBlock
        }
        return true;
    }

    private Boolean visitIfStatement(JmmNode jmmNode, ConstPropagationTable table) {
        for (var block : jmmNode.getChildren()) {
            this.visit(block.getJmmChild(0), table); // Condition, IfBlock & ElseBlock
        }
        return true;
    }

    private Boolean visitReturnStatement(JmmNode jmmNode, ConstPropagationTable table) {
        this.visit(jmmNode.getJmmChild(0), table);
        return true;
    }

    private Boolean visitExpression(JmmNode jmmNode, ConstPropagationTable table) {
        for (var child : jmmNode.getChildren()) {
            this.visit(child, table);
        }
        return true;
    }

    private Boolean visitArrayAccessExp(JmmNode jmmNode, ConstPropagationTable table) {
        this.visit(jmmNode.getJmmChild(1), table);
        return true;
    }

    private Boolean visitIntArray(JmmNode jmmNode, ConstPropagationTable table) {
        this.visit(jmmNode.getJmmChild(0), table);
        return true;
    }

    private Boolean visitDotExp(JmmNode jmmNode, ConstPropagationTable table) {
        this.visit(jmmNode.getJmmChild(1), table);
        return true;
    }

    private Boolean visitFunctionCall(JmmNode jmmNode, ConstPropagationTable table) {
        for (var arg : jmmNode.getChildren()) {
            this.visit(arg, table);
        }
        return true;
    }

    private Boolean visitIdentifier(JmmNode jmmNode, ConstPropagationTable table) {
        String name = jmmNode.get("val");
        String value = table.get(name);
        if (value != null && table.isPropagating()) {
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
