package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.Utils.*;
import static pt.up.fe.comp.ast.AstNode.*;
import static pt.up.fe.comp.ollir.OllirUtils.getCode;
import static pt.up.fe.comp.ollir.OllirUtils.getOllirType;


public class OllirExprVisitor extends AJmmVisitor<Integer, OllirExprGenerator> {
    private final SymbolTableBuilder symbolTable;
    private static int varAuxNumber = 0;

    public OllirExprVisitor(SymbolTableBuilder symbolTable) {
        this.symbolTable = symbolTable;

        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
        addVisit(INTEGER_LITERAL, this::visitIntegerLiteral);
        addVisit(TRUE_LITERAL, this::visitTrueLiteral);
        addVisit(FALSE_LITERAL , this::visitFalseLiteral);
        addVisit(AND_EXP, this::visitAndExp);
        addVisit(LESS_EXP, this::visitLessExp);
        addVisit(ADD_EXP, this::visitAddExp);
        addVisit(SUB_EXP, this::visitSubExp);
        addVisit(MULT_EXP, this::visitMultExp);
        addVisit(DIV_EXP, this::visitDivExp);
        addVisit(NOT_EXP, this::visitNotExp);

        addVisit(DOT_EXP, this::visitDotExp);
        addVisit(ARRAY_ACCESS_EXP, this::visitArrayAccessExp);
        addVisit(NEW_INT_ARRAY, this::visitNewIntArray);
        addVisit(NEW_OBJECT, this::visitNewObject);
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
    }

    private OllirExprGenerator visitIdentifierLiteral(JmmNode identifier, Integer dummy) {
        Type type = getType(identifier, this.symbolTable);
        return new OllirExprGenerator(identifier.get("val") + "." + getCode(type)); // TODO
    }
    private OllirExprGenerator visitIntegerLiteral(JmmNode integer, Integer dummy) {
        String exp = newVar("i32");
        String temp = "\t\t" + exp + " :=.i32 " + integer.get("val") + ".i32;\n";
        return new OllirExprGenerator(exp, temp);
    }
    private OllirExprGenerator visitTrueLiteral(JmmNode trueLiteral, Integer dummy) {
        String exp = newVar("bool");
        String temp = "\t\t" + exp + " :=.bool 1.bool;\n";
        return new OllirExprGenerator(exp, temp);
    }
    private OllirExprGenerator visitFalseLiteral(JmmNode falseLiteral, Integer dummy) {
        String exp = newVar("bool");
        String temp = "\t\t" + exp + " :=.bool 0.bool;\n";
        return new OllirExprGenerator(exp, temp);
    }

    private OllirExprGenerator visitAndExp(JmmNode andExp, Integer integer) {
        return this.visitBiOpExp(andExp, "bool", "&.bool");
    }
    private OllirExprGenerator visitLessExp(JmmNode lessExp, Integer integer) {
        return this.visitBiOpExp(lessExp, "bool", "<.i32");
    }
    private OllirExprGenerator visitAddExp(JmmNode addExp, Integer integer) {
        return this.visitBiOpExp(addExp, "i32", "+.i32");
    }
    private OllirExprGenerator visitSubExp(JmmNode subExp, Integer integer) {
        return this.visitBiOpExp(subExp, "i32", "-.i32");
    }
    private OllirExprGenerator visitMultExp(JmmNode multExp, Integer integer) {
        return this.visitBiOpExp(multExp, "i32", "*.i32");
    }
    private OllirExprGenerator visitDivExp(JmmNode divExp, Integer integer) {
        return this.visitBiOpExp(divExp, "i32", "/.i32");
    }
    private OllirExprGenerator visitBiOpExp(JmmNode jmmNode, String varType, String operator) {
        StringBuilder temps = new StringBuilder();
        OllirExprGenerator left = visit(jmmNode.getJmmChild(0));
        OllirExprGenerator right = visit(jmmNode.getJmmChild(1));

        temps.append(left.getTemps());
        temps.append(right.getTemps());

        String expr = newVar(varType);
        temps.append("\t\t")
                .append(expr)
                .append(" :=.").append(varType).append(" ")
                .append(left.getFullExp())
                .append(" ").append(operator).append(" ")
                .append(right.getFullExp())
                .append(";\n");
        return new OllirExprGenerator(expr, temps.toString());
    }
    private OllirExprGenerator visitNotExp(JmmNode jmmNode, Integer integer) {
        StringBuilder temps = new StringBuilder();
        JmmNode exp = jmmNode.getJmmChild(0);

        OllirExprGenerator exprPair = visit(exp);
        temps.append(exprPair.getTemps());

        String expr = newVar("bool");
        temps.append("\t\t")
            .append(expr)
            .append(" :=.bool ")
            .append("!.bool ")
            .append(exprPair.getFullExp())
            .append(";\n");
        return new OllirExprGenerator(expr, temps.toString());
    }

    private OllirExprGenerator visitNewObject(JmmNode newObject, Integer integer) {
        StringBuilder temps = new StringBuilder();
        String type = newObject.get("name");

        String expr = newVar(type);
        temps.append("\t\t")
            .append(expr)
            .append(" :=.").append(type).append(" ")
            .append("new(").append(type).append(")")
            .append(".").append(type)
            .append(";\n");
        temps.append("\t\t").append("invokespecial(").append(expr).append(",\"<init>\").V;\n");
        return new OllirExprGenerator(expr, temps.toString());
    }
    private OllirExprGenerator visitNewIntArray(JmmNode newIntArray, Integer integer) {
        StringBuilder temps = new StringBuilder();
        String type = "array";
        JmmNode exp = newIntArray.getJmmChild(0);
        OllirExprGenerator exprPair = visit(exp);
        temps.append(exprPair.getTemps());

        String expr = newVar(type);
        temps.append("\t\t")
                .append(expr)
                .append(" :=.").append(type).append(" ")
                .append("new(").append(type).append(", ").append(exprPair.getFullExp()).append(")")
                .append(".").append(type)
                .append(";\n");
        return new OllirExprGenerator(expr, temps.toString());
    }
    private OllirExprGenerator visitArrayAccessExp(JmmNode arrayAccessExp, Integer integer) {
        StringBuilder temps = new StringBuilder();
        StringBuilder expr = new StringBuilder();
        JmmNode value = arrayAccessExp.getJmmChild(0);
        JmmNode index = arrayAccessExp.getJmmChild(1);
        OllirExprGenerator valuePair = visit(value);
        OllirExprGenerator indexPair = visit(index);
        temps.append(valuePair.getTemps());
        temps.append(indexPair.getTemps());

        Type type = getType(value, this.symbolTable);
        String val = valuePair.getFullExp().substring(0, valuePair.getFullExp().indexOf("."));

        expr.append(val).append("[").append(indexPair.getFullExp()).append("].").append(getOllirType(type.getName()));
        return new OllirExprGenerator(expr.toString(), temps.toString());
    }

    private OllirExprGenerator visitDotExp(JmmNode dotExp, Integer integer) {
        StringBuilder temps = new StringBuilder();
        JmmNode left = dotExp.getJmmChild(0);
        JmmNode right = dotExp.getJmmChild(1);
        OllirExprGenerator leftPair = visit(left);
        temps.append(leftPair.getTemps());

        if (right.getKind().equals(PROPERTY_LENGTH.toString())) {
            String exp = newVar("i32");
            temps.append("\t\t").append(exp).append(" :=.i32").append(" arraylength(").append(leftPair.getFullExp()).append(").i32;\n");
            return new OllirExprGenerator(exp, temps.toString());
        }
        else {
            OllirExprGenerator rightPair = visit(right);
            temps.append(rightPair.getTemps());

            // TODO
            return new OllirExprGenerator();
        }
    }
    private OllirExprGenerator visitFunctionCall(JmmNode jmmNode, Integer integer) {
        // TODO
        return new OllirExprGenerator();
    }

    private String newVar(String varType) {
        return "t" + (varAuxNumber++) + "." + varType;
    }
}
