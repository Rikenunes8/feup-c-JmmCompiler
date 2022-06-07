package pt.up.fe.comp.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import static pt.up.fe.comp.ast.AstNode.*;

public class ConstantFoldingVisitor extends PostorderJmmVisitor<String, Boolean> {
    public ConstantFoldingVisitor() {
        addVisit(AND_EXP, this::visitAndExp);
        addVisit(LESS_EXP, this::visitLessExp);
        addVisit(ADD_EXP, this::visitAddExp);
        addVisit(MULT_EXP, this::visitMultExp);
        addVisit(SUB_EXP, this::visitSubExp);
        addVisit(DIV_EXP, this::visitDivExp);
        addVisit(NOT_EXP, this::visitNotExp);
    }

    Boolean getBoolean(JmmNode jmmNode) {
        if (jmmNode.getKind().equals(TRUE_LITERAL.toString())) return true;
        if (jmmNode.getKind().equals(FALSE_LITERAL.toString())) return false;
        return null;
    }
    Integer getInt(JmmNode jmmNode) {
        if (!jmmNode.getKind().equals(INTEGER_LITERAL.toString())) return null;
        return Integer.parseInt(jmmNode.get("val"));
    }

    private Boolean visitAndExp(JmmNode jmmNode, String s) {
        var x = getBoolean(jmmNode.getJmmChild(0));
        var y = getBoolean(jmmNode.getJmmChild(1));
        if (x == null || y == null) return false;
        jmmNode.removeJmmChild(1);
        jmmNode.removeJmmChild(0);
        JmmNode newNode = new JmmNodeImpl(x && y ? TRUE_LITERAL.toString() : FALSE_LITERAL.toString());
        jmmNode.replace(newNode);
        return true;
    }

    private Boolean visitLessExp(JmmNode jmmNode, String s) {
        var x = getInt(jmmNode.getJmmChild(0));
        var y = getInt(jmmNode.getJmmChild(1));
        if (x == null || y == null) return false;
        jmmNode.removeJmmChild(1);
        jmmNode.removeJmmChild(0);
        JmmNode newNode = new JmmNodeImpl(x < y ? TRUE_LITERAL.toString() : FALSE_LITERAL.toString());
        jmmNode.replace(newNode);
        return true;
    }

    private Boolean visitAddExp(JmmNode jmmNode, String s) {
        var x = getInt(jmmNode.getJmmChild(0));
        var y = getInt(jmmNode.getJmmChild(1));
        if (x == null || y == null) return false;
        jmmNode.removeJmmChild(1);
        jmmNode.removeJmmChild(0);
        JmmNode newNode = new JmmNodeImpl(INTEGER_LITERAL.toString());
        newNode.put("val", String.valueOf(x + y));
        jmmNode.replace(newNode);
        return true;
    }

    private Boolean visitMultExp(JmmNode jmmNode, String s) {
        var x = getInt(jmmNode.getJmmChild(0));
        var y = getInt(jmmNode.getJmmChild(1));
        if (x == null || y == null) return false;
        jmmNode.removeJmmChild(1);
        jmmNode.removeJmmChild(0);
        JmmNode newNode = new JmmNodeImpl(INTEGER_LITERAL.toString());
        newNode.put("val", String.valueOf(x * y));
        jmmNode.replace(newNode);
        return true;
    }

    private Boolean visitSubExp(JmmNode jmmNode, String s) {
        var x = getInt(jmmNode.getJmmChild(0));
        var y = getInt(jmmNode.getJmmChild(1));
        if (x == null || y == null) return false;
        jmmNode.removeJmmChild(1);
        jmmNode.removeJmmChild(0);
        JmmNode newNode = new JmmNodeImpl(INTEGER_LITERAL.toString());
        newNode.put("val", String.valueOf(x - y));
        jmmNode.replace(newNode);
        return true;
    }

    private Boolean visitDivExp(JmmNode jmmNode, String s) {
        var x = getInt(jmmNode.getJmmChild(0));
        var y = getInt(jmmNode.getJmmChild(1));
        if (x == null || y == null) return false;
        jmmNode.removeJmmChild(1);
        jmmNode.removeJmmChild(0);
        JmmNode newNode = new JmmNodeImpl(INTEGER_LITERAL.toString());
        newNode.put("val", String.valueOf(x / y));
        jmmNode.replace(newNode);
        return true;
    }

    private Boolean visitNotExp(JmmNode jmmNode, String s) {
        var x = getBoolean(jmmNode.getJmmChild(0));
        if (x == null) return false;
        jmmNode.removeJmmChild(0);
        JmmNode newNode = new JmmNodeImpl(!x ? TRUE_LITERAL.toString() : FALSE_LITERAL.toString());
        jmmNode.replace(newNode);
        return true;
    }
}
