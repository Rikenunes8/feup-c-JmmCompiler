package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static pt.up.fe.comp.Utils.*;
import static pt.up.fe.comp.ast.AstNode.*;
import static pt.up.fe.comp.ollir.OllirUtils.getCode;
import static pt.up.fe.comp.ollir.OllirUtils.getOllirType;


public class OllirExprVisitor extends AJmmVisitor<Integer, OllirExprPair> {
    private final StringBuilder code;
    private final SymbolTableBuilder symbolTable;
    private static int varAuxNumber = 0;

    public OllirExprVisitor(SymbolTableBuilder symbolTable) {
        this.code = new StringBuilder();
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
    }

    private OllirExprPair visitNewObject(JmmNode newObject, Integer integer) {
        StringBuilder temps = new StringBuilder();
        String type = newObject.get("name");

        String expression = "t"+ (varAuxNumber++) + "."+type;
        temps.append("\t\t")
                .append(expression)
                .append(" :=.").append(type).append(" ")
                .append("new(").append(type).append(")")
                .append(".").append(type)
                .append(";\n");
        temps.append("\t\t").append("invokespecial(").append(expression).append(",\"<init>\").V;\n");
        return new OllirExprPair(temps.toString(), expression);
    }

    private OllirExprPair visitNewIntArray(JmmNode newIntArray, Integer integer) {
        StringBuilder temps = new StringBuilder();
        String type = "array";
        JmmNode exp = newIntArray.getJmmChild(0);
        OllirExprPair exprPair = visit(exp);
        temps.append(exprPair.getTemps());


        String expression = "t"+ (varAuxNumber++) + "."+type;
        temps.append("\t\t")
                .append(expression)
                .append(" :=.").append(type).append(" ")
                .append("new(").append(type).append(", ").append(exprPair.getExpression()).append(")")
                .append(".").append(type)
                .append(";\n");
        return new OllirExprPair(temps.toString(), expression);
    }

    private OllirExprPair visitArrayAccessExp(JmmNode arrayAccessExp, Integer integer) {
        StringBuilder temps = new StringBuilder();
        StringBuilder expr = new StringBuilder();
        JmmNode value = arrayAccessExp.getJmmChild(0);
        JmmNode index = arrayAccessExp.getJmmChild(1);
        OllirExprPair valuePair = visit(value);
        OllirExprPair indexPair = visit(index);
        temps.append(valuePair.getTemps());
        temps.append(indexPair.getTemps());

        Type type = getType(value, this.symbolTable);
        String val = valuePair.getExpression().substring(0, valuePair.getExpression().indexOf("."));

        expr.append(val).append("[").append(indexPair.getExpression()).append("].").append(getOllirType(type.getName()));
        return new OllirExprPair(temps.toString(), expr.toString());
    }

    // t1.i32 :=.i32 arraylength($1.A.array.i32).i32;
    private OllirExprPair visitDotExp(JmmNode dotExp, Integer integer) {
        StringBuilder temps = new StringBuilder();
        JmmNode left = dotExp.getJmmChild(0);
        JmmNode right = dotExp.getJmmChild(1);
        OllirExprPair leftPair = visit(left);
        OllirExprPair rightPair = visit(right);
        temps.append(leftPair.getTemps());
        temps.append(rightPair.getTemps());
        if (right.getKind().equals(PROPERTY_LENGTH.toString())) {
            String exp = "t" + varAuxNumber++ + ".i32";
            temps.append("\t\t").append(exp).append(" :=.i32").append("arraylength(").append(leftPair.getExpression()).append(").i32");
            return new OllirExprPair(temps.toString(), exp);
        }
        else {
            // TODO
            return new OllirExprPair();
        }
    }

    private OllirExprPair visitNotExp(JmmNode jmmNode, Integer integer) {
        StringBuilder temps = new StringBuilder();
        JmmNode exp = jmmNode.getJmmChild(0);

        OllirExprPair exprPair = visit(exp);
        temps.append(exprPair.getTemps());

        String expression = "t"+ (varAuxNumber++) + ".bool";
        temps.append("\t\t")
                .append(expression)
                .append(" :=.bool ")
                .append("!.bool ")
                .append(exprPair.getExpression())
                .append(";\n");
        return new OllirExprPair(temps.toString(), expression);
    }

    private OllirExprPair visitDivExp(JmmNode divExp, Integer integer) {
        return this.visitBiOpExp(divExp, "i32", "/.i32");
    }

    private OllirExprPair visitMultExp(JmmNode multExp, Integer integer) {
        return this.visitBiOpExp(multExp, "i32", "*.i32");
    }

    private OllirExprPair visitSubExp(JmmNode subExp, Integer integer) {
        return this.visitBiOpExp(subExp, "i32", "-.i32");
    }

    private OllirExprPair visitAddExp(JmmNode addExp, Integer integer) {
        return this.visitBiOpExp(addExp, "i32", "+.i32");
    }

    private OllirExprPair visitLessExp(JmmNode lessExp, Integer integer) {
        return this.visitBiOpExp(lessExp, "bool", "<.i32");
    }

    private OllirExprPair visitAndExp(JmmNode andExp, Integer integer) {
        return this.visitBiOpExp(andExp, "bool", "&.bool");
    }

    private OllirExprPair visitBiOpExp(JmmNode jmmNode, String varType, String operator) {
        StringBuilder temps = new StringBuilder();
        JmmNode left = jmmNode.getJmmChild(0);
        JmmNode right = jmmNode.getJmmChild(1);

        OllirExprPair leftPair = visit(left);
        OllirExprPair rightPair = visit(right);
        temps.append(leftPair.getTemps());
        temps.append(rightPair.getTemps());

        String expression = "t"+ (varAuxNumber++) + "."+varType;
        temps.append("\t\t")
                .append(expression)
                .append(" :=.").append(varType).append(" ")
                .append(leftPair.getExpression())
                .append(" ").append(operator).append(" ")
                .append(rightPair.getExpression())
                .append(";\n");
        return new OllirExprPair(temps.toString(), expression);
    }

    public OllirExprPair visitIdentifierLiteral(JmmNode identifier, Integer dummy) {
        Type type = getType(identifier, this.symbolTable);
        return new OllirExprPair(identifier.get("val") +"."+ getCode(type)); // TODO
    }

    public OllirExprPair visitIntegerLiteral(JmmNode integer, Integer dummy) {
        String exp = "t" + varAuxNumber++ + ".i32";
        String temp = "\t\t" + exp + " :=.i32 " + integer.get("val") + ".i32;\n";
        return new OllirExprPair(temp, exp);
    }

    public OllirExprPair visitTrueLiteral(JmmNode trueLiteral, Integer dummy) {
        String exp = "t" + varAuxNumber++ + ".i32";
        String temp = "\t\t" + exp + " :=.i32 1.bool;\n";
        return new OllirExprPair(temp, exp);
    }

    public OllirExprPair visitFalseLiteral(JmmNode falseLiteral, Integer dummy) {
        String exp = "t" + varAuxNumber++ + ".i32";
        String temp = "\t\t" + exp + " :=.i32 0.bool;\n";
        return new OllirExprPair(temp, exp);
    }
}