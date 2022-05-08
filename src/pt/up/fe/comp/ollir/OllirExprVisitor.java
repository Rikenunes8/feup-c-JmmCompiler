package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static pt.up.fe.comp.Utils.*;
import static pt.up.fe.comp.ast.AstNode.*;
import static pt.up.fe.comp.ollir.OllirUtils.getCode;


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

    private OllirExprPair visitNewObject(JmmNode jmmNode, Integer integer) {
        throw new NotImplementedException(); // TODO
    }

    private OllirExprPair visitNewIntArray(JmmNode jmmNode, Integer integer) {
        throw new NotImplementedException(); // TODO
    }

    private OllirExprPair visitArrayAccessExp(JmmNode jmmNode, Integer integer) {
        throw new NotImplementedException(); // TODO
    }

    private OllirExprPair visitDotExp(JmmNode jmmNode, Integer integer) {
            throw new NotImplementedException(); // TODO
    }

    private OllirExprPair visitNotExp(JmmNode jmmNode, Integer integer) {
        StringBuilder temps = new StringBuilder();
        JmmNode exp = jmmNode.getJmmChild(0);

        OllirExprPair exprPair = visit(exp);
        temps.append(exprPair.getTemps());

        String expression = "t"+ (varAuxNumber++) + ".bool";
        temps.append(expression)
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
        temps.append(expression)
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
        return new OllirExprPair(integer.get("val") + ".i32");
    }

    public OllirExprPair visitTrueLiteral(JmmNode trueLiteral, Integer dummy) {
        return new OllirExprPair("1.bool");
    }

    public OllirExprPair visitFalseLiteral(JmmNode falseLiteral, Integer dummy) {
        return new OllirExprPair("0.bool");
    }

     private String newVarAux(JmmNode node, String type){
         String value;
         if(node.getKind().equals("DotExp"))
         {
             // value = visit(node);
             value = "template.i32";
         }
         else
         {
             value = OllirUtils.getOllirExpression(node);
         }
         varAuxNumber++;
         return "\t\tt" + varAuxNumber + type + " :=" + type +" " + value + ";\n";
     }
}