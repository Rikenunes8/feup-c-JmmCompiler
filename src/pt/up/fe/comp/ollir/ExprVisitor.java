package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import static pt.up.fe.comp.jmm.ollir.OllirUtils.*;
import static pt.up.fe.comp.Utils.*;
import static pt.up.fe.comp.ast.AstNode.*;


public class ExprVisitor extends AJmmVisitor<Integer, OllirExprPair> {
    private final StringBuilder code;
    private final SymbolTableBuilder symbolTable;
    private static int varAuxNumber = 0;

    public ExprVisitor(SymbolTableBuilder symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;

        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
        // addVisit(INTEGER_LITERAL, this::visitIntegerLiteral);
        // addVisit(TRUE_LITERAL, this::visitTrueLiteral);
        // addVisit(FALSE_LITERAL , this::visitFalseLiteral);


        // THIS_LITERAL,
        // ADD_EXP,
        // SUB_EXP,
        // MULT_EXP,
        // DIV_EXP,
        // AND_EXP,
        // NOT_EXP,
        // LESS_EXP,
        // ARRAY_ACCESS_EXP,
        // ASSIGNMENT_STATEMENT,
        // NEW_INT_ARRAY,

        // NEW_OBJECT,
        // PROPERTY_LENGTH,


        // addVisit(DOT_EXP, this::visitDotExp);
        
        // addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
        // addVisit(NEW_OBJECT, this::visitNewObject);
        // addVisit(FUNCTION_CALL, this::visitFunctionCall);
        // addVisit(WHILE_STATEMENT, this::visitWhileStatement);
        // addVisit(CONDITION, this::visitCondition);

    }

    public OllirExprPair visitIdentifierLiteral(JmmNode identifier, Integer dummy) {
        Type type = getType(identifier, this.symbolTable);
        // getCode(type);
        return new OllirExprPair();
    }

    // public OllirExprPair visitIntegerLiteral(JmmNode identifier, Integer dummy) {
    // }

    // public OllirExprPair visitTrueLiteral(JmmNode identifier, Integer dummy) {
    // }

    // public OllirExprPair visitFalseLiteral(JmmNode identifier, Integer dummy) {
    // }

    // private OllirExprPair visitLessExp(JmmNode lessExp) {
    //     OllirExprPair result = new ArrayList<>();
    //     String variables = "";
    //     String expression = "";

    //     JmmNode leftNode = lessExp.getJmmChild(0);
    //     JmmNode rightNode = lessExp.getJmmChild(1);

    //     String left = newVarAux(leftNode, ".i32");
    //     String right = newVarAux(rightNode, ".i32");

    //     variables += (left + right);

    //     result.add(variables);
    //     result.add(expression);

    //     return result;
    // }

    // private String newVarAux(JmmNode node, String type){
    //     String value; 
    //     if(node.getKind().equals("DotExp"))
    //     {
    //         // value = visit(node);
    //         value = "template.i32";
    //     }
    //     else 
    //     {
    //         value = OllirUtils.getOllirExpression(node);
    //     }
    //     varAuxNumber++;
    //     return "\t\tt" + varAuxNumber + type + " :=" + type +" " + value + ";\n";
    // }
}