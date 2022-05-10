package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.Utils.*;
import static pt.up.fe.comp.ast.AstNode.*;
import static pt.up.fe.comp.ollir.OllirGenerator.ident;
import static pt.up.fe.comp.ollir.OllirUtils.getCode;
import static pt.up.fe.comp.ollir.OllirUtils.getOllirType;

import pt.up.fe.comp.Utils;

import java.util.stream.Collectors;


public class OllirExprVisitor extends AJmmVisitor<Integer, OllirExprGenerator> {
    private final SymbolTableBuilder symbolTable;
    private static int varAuxNumber = 0;

    public OllirExprVisitor(SymbolTableBuilder symbolTable) {
        this.symbolTable = symbolTable;

        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
        addVisit(INTEGER_LITERAL, this::visitIntegerLiteral);
        addVisit(TRUE_LITERAL, this::visitTrueLiteral);
        addVisit(FALSE_LITERAL , this::visitFalseLiteral);
        addVisit(THIS_LITERAL, this::visitThisLiteral);
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

    private String getParameterPrefix(JmmNode identifier) {
        String identifierName = identifier.get("val");
        String methodSignature = identifier.getAncestor(METHOD_DECLARATION.toString()).get().get("name");

        String paramPrefix = "";
        var localVars = symbolTable.getLocalVariables(methodSignature).stream()
                .filter(symbol -> symbol.getName().equals(identifierName)).toList();
        if (localVars.isEmpty()) {
            var parameters = symbolTable.getParameters(methodSignature);
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).getName().equals(identifierName)) {
                    paramPrefix = "$"+(i+1)+".";
                    break;
                }
            }
        }
        return paramPrefix;
    }

    private OllirExprGenerator visitThisLiteral(JmmNode jmmNode, Integer integer) {
        return new OllirExprGenerator("this", "");
    }

    private OllirExprGenerator visitIdentifierLiteral(JmmNode identifier, Integer dummy) {
        String identifierName = identifier.get("val");
        String paramPrefix = getParameterPrefix(identifier);

        Type type = !Utils.isImported(identifierName, symbolTable) && !identifierName.equals(symbolTable.getClassName())
                ? getType(identifier, this.symbolTable)
                : new Type("void", false);
        String typeCode = getCode(type);
        return new OllirExprGenerator(paramPrefix + identifierName + "." + typeCode, typeCode );
    }
    private OllirExprGenerator visitIntegerLiteral(JmmNode integer, Integer dummy) {
        return new OllirExprGenerator(integer.get("val") + ".i32", "i32");
    }
    private OllirExprGenerator visitTrueLiteral(JmmNode trueLiteral, Integer dummy) {
        return new OllirExprGenerator("1.bool", "bool");
    }
    private OllirExprGenerator visitFalseLiteral(JmmNode falseLiteral, Integer dummy) {
        return new OllirExprGenerator("0.bool", "bool");
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
        StringBuilder expr = new StringBuilder();
        StringBuilder temps = new StringBuilder();

        OllirExprGenerator left = visit(jmmNode.getJmmChild(0));
        OllirExprGenerator right = visit(jmmNode.getJmmChild(1));

        temps.append(left.getTemps());
        temps.append(right.getTemps());

        String t1 = newVar(left.getType());
        String t2 = newVar(right.getType());

        temps.append(ident()).append(newVarInstr(t1, left.getType(), left.getFullExp()));
        temps.append(ident()).append(newVarInstr(t2, right.getType(), right.getFullExp()));

        expr.append(t1).append(" ").append(operator).append(" ").append(t2);
        return new OllirExprGenerator(expr.toString(), varType, temps.toString());
    }
    private OllirExprGenerator visitNotExp(JmmNode jmmNode, Integer integer) {
        StringBuilder expr = new StringBuilder();
        StringBuilder temps = new StringBuilder();

        OllirExprGenerator right = visit(jmmNode.getJmmChild(0));
        temps.append(right.getTemps());
        String t1 = newVar(right.getType());
        temps.append(ident()).append(newVarInstr(t1, right.getType(), right.getFullExp()));

        expr.append("!.bool").append(" ").append(t1);
        return new OllirExprGenerator(expr.toString(), "bool", temps.toString());
    }

    private OllirExprGenerator visitNewObject(JmmNode newObject, Integer integer) {
        StringBuilder temps = new StringBuilder();
        String type = newObject.get("name");

        String t1 = newVar(type);
        temps.append(ident()).append(newVarInstr(t1, type, "new(" + type + ")." + type));
        temps.append(ident()).append("invokespecial(").append(t1).append(",\"<init>\").V;\n");

        return new OllirExprGenerator(t1, type, temps.toString());
    }
    private OllirExprGenerator visitNewIntArray(JmmNode newIntArray, Integer integer) {
        StringBuilder temps = new StringBuilder();
        String type = "array.i32";
        OllirExprGenerator size = visit(newIntArray.getJmmChild(0));
        temps.append(size.getTemps());

        String t1 = newVar(size.getType());
        temps.append(ident()).append(newVarInstr(t1, size.getType(), size.getFullExp()));

        String expr = "new(array, " + t1 + ")." + type;
        return new OllirExprGenerator(expr, type, temps.toString());
    }
    private OllirExprGenerator visitArrayAccessExp(JmmNode arrayAccessExp, Integer integer) {
        StringBuilder expr = new StringBuilder();
        StringBuilder temps = new StringBuilder();
        JmmNode jmmValue = arrayAccessExp.getJmmChild(0);
        JmmNode jmmIndex = arrayAccessExp.getJmmChild(1);
        OllirExprGenerator value = visit(jmmValue);
        OllirExprGenerator index = visit(jmmIndex);
        temps.append(value.getTemps());
        temps.append(index.getTemps());

        String t1 = newVar(index.getType());
        temps.append(ident()).append(newVarInstr(t1, index.getType(), index.getFullExp()));
        String val = value.getFullExp().replace("." + value.getType(), "");
        String type = getOllirType(getType(jmmValue, this.symbolTable).getName());
        expr.append(val).append("[").append(t1).append("].").append(type);

        return new OllirExprGenerator(expr.toString(), type, temps.toString());
    }

    private OllirExprGenerator visitDotExp(JmmNode dotExp, Integer integer) {
        StringBuilder temps = new StringBuilder();
        JmmNode jmmLeft = dotExp.getJmmChild(0);
        JmmNode jmmRight = dotExp.getJmmChild(1);
        OllirExprGenerator left = visit(jmmLeft);
        temps.append(left.getTemps());

        if (jmmRight.getKind().equals(PROPERTY_LENGTH.toString())) {
            String aux = left.getFullExp();
            if (jmmLeft.getKind().equals(NEW_INT_ARRAY.toString())) {
                aux = newVar(left.getType());
                temps.append(ident()).append(newVarInstr(aux, left.getType(), left.getFullExp()));
            }
            String exp = "arraylength(" + aux + ").i32";
            return new OllirExprGenerator(exp, "i32", temps.toString());
        }
        else {
            OllirExprGenerator right = visit(jmmRight);
            temps.append(right.getTemps());

            StringBuilder fullExp = new StringBuilder();


            String callInstr = "invokevirtual";
            String firstArg = left.getFullExp();
            if (jmmLeft.getKind().equals(IDENTIFIER_LITERAL.toString())
                    && (isImported(jmmLeft.get("val"), symbolTable) || jmmLeft.get("val").equals(symbolTable.getClassName()))) {
                callInstr = "invokestatic";
                firstArg = jmmLeft.get("val");
            }

            fullExp.append(callInstr).append("(").append(firstArg)
                    .append(", \"").append(jmmRight.get("name")).append("\"");
            fullExp.append(right.getFullExp());
            fullExp.append(").");
            Type type = this.symbolTable.hasMethod(jmmRight.get("name"))
                    ? this.symbolTable.getReturnType(jmmRight.get("name"))
                    : new Type("void", false);
            String typeCode = getCode(type);
            fullExp.append(typeCode);

            return new OllirExprGenerator(fullExp.toString(), typeCode, temps.toString());
        }
    }
    private OllirExprGenerator visitFunctionCall(JmmNode jmmNode, Integer integer) {
        StringBuilder temps = new StringBuilder();
        StringBuilder fullExp = new StringBuilder();

        for(JmmNode child : jmmNode.getChildren()) {
            OllirExprGenerator exprGenerator = visit(child);
            temps.append(exprGenerator.getTemps());

            String t = newVar(exprGenerator.getType());
            temps.append(ident()).append(newVarInstr(t, exprGenerator.getType(), exprGenerator.getFullExp()));

            fullExp.append(", ");
            fullExp.append(t);
        }

        return new OllirExprGenerator(fullExp.toString(), "", temps.toString());
    }

    public static String newVar(String varType) {
        return "t" + (varAuxNumber++) + "." + varType;
    }

    public static String newVarInstr(String newVar, String newVarType, String expression) {
        return newVar + " :=." + newVarType + " " + expression + ";\n";
    }
}
