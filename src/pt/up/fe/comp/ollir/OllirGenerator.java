package pt.up.fe.comp.ollir;
import pt.up.fe.comp.analysis.SymbolTableBuilder;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;
import static pt.up.fe.comp.ast.AstNode.*;
import static pt.up.fe.comp.ollir.OllirExprVisitor.newVar;
import static pt.up.fe.comp.ollir.OllirExprVisitor.newVarInstr;


public class OllirGenerator extends AJmmVisitor<Integer, Integer> {

    private final StringBuilder code;
    private final SymbolTableBuilder symbolTable;
    private final OllirExprVisitor ollirExprVisitor;
    public static int identention = 0;
    private int whileNumber;
    private int ifNumber;

    public OllirGenerator(SymbolTableBuilder symbolTable) {
        this.ollirExprVisitor = new OllirExprVisitor(symbolTable);
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.whileNumber = 0;
        this.ifNumber = 0;

        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECLARATION, this::visitClassDeclaration);
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
        addVisit(RETURN_STATEMENT, this::visitReturnStatement);

        addVisit(CONDITION, this::visitCondition);
        addVisit(WHILE_BLOCK, this::visitWhileBlock);

        addVisit(EXPRESSION_STATEMENT, this::visitExpressionStatement);
        addVisit(IF_STATEMENT, this::visitIfStatement);
        addVisit(WHILE_STATEMENT, this::visitWhileStatement);
        addVisit(ASSIGNMENT_STATEMENT, this::visitAssignmentStatement);
        addVisit(SCOPE, this::visitScope);
    }

    public String getCode() {
        return this.code.toString();
    }

    private Integer visitProgram(JmmNode program, Integer dummy) {
        for (var importStr : symbolTable.getImports())  {
            code.append("import ").append(importStr).append(";\n");
        }

        for (var child : program.getChildren()) {
            visit(child);
        }

        return 0;
    }

    private Integer visitClassDeclaration(JmmNode classDeclaration, Integer dummy) {
        code.append("public ").append(symbolTable.getClassName());
        String extendedClass = symbolTable.getSuper();
        if (extendedClass != null) {
            code.append(" extends ").append(extendedClass);
        }

        code.append(" {\n");
        identention++;

        visitPrivateAttibutes();
        visitConstructor();

        for (var child : classDeclaration.getChildren()) {
            if (child.getKind().equals(METHOD_DECLARATION.toString()))
                visit(child);
        }
        identention--;
        code.append("}\n");

        return 0;
    }

    private void visitPrivateAttibutes() {
        for(Symbol field : symbolTable.getFields()){
            code.append(ident()).append(".field private ")
                    .append(OllirUtils.getCode(field))
                    .append(";\n");
        }
    }
    private void visitConstructor() {
        code.append(ident()).append(".construct ").append(symbolTable.getClassName()).append("().V {\n");
        identention++;
        code.append(ident()).append("invokespecial(this, \"<init>\").V;\n");
        identention--;
        code.append(ident()).append("}\n");
    }
    
    private Integer visitMethodDeclaration(JmmNode methodDeclaration, Integer dummy) {

        String methodSignature = methodDeclaration.get("name");
        boolean isStatic = Boolean.parseBoolean(methodDeclaration.get("static"));
        code.append(ident()).append(".method public ");
        if (isStatic) code.append("static ");
        code.append(methodSignature).append("(");

        List<Symbol> params = symbolTable.getParameters(methodSignature);
        String paramCode = params.stream().map(symbol -> OllirUtils.getCode(symbol)).collect(Collectors.joining(", "));

        code.append(paramCode);
        code.append(").");
        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodSignature)));
        code.append(" {\n");
        identention++;


        int firstExpressionIndex = 0;
        while (firstExpressionIndex < methodDeclaration.getNumChildren() &&
                (methodDeclaration.getJmmChild(firstExpressionIndex).getKind().equals(METHOD_PARAMETERS.toString())
                || methodDeclaration.getJmmChild(firstExpressionIndex).getKind().equals(VAR_DECLARATION.toString()))) {
            firstExpressionIndex++;
        }

        List<JmmNode> stmts = methodDeclaration.getChildren().subList(firstExpressionIndex, methodDeclaration.getNumChildren());
        System.out.println("\nSTMTS :\n" + stmts);

        for (JmmNode stmt : stmts) {
            visit(stmt);
        }
        identention--;
        code.append(ident()).append("}\n");

        return 0;
    }

    private Integer visitReturnStatement(JmmNode returnStatement, Integer dummy) {
        OllirExprGenerator result = visitExpression(returnStatement.getJmmChild(0));
        JmmNode methodDeclaration = returnStatement.getJmmParent();
        String methodSignature = methodDeclaration.get("name");
        
        code.append(result.getTemps());
        code.append(ident()).append("ret.");
        String returnType = OllirUtils.getCode(symbolTable.getReturnType(methodSignature));
        code.append(returnType);
        if(!returnType.equals("V")) {
            code.append(" ");
            code.append(result.getFullExp());
        }

        code.append(";\n");
        
        return 0;
    }

    private Integer visitExpressionStatement(JmmNode expressionStatement, Integer dummy) {
        OllirExprGenerator exprPair = this.visitExpression(expressionStatement.getJmmChild(0));
        code.append(exprPair.getTemps());
        code.append(ident()).append(exprPair.getFullExp()).append(";\n");
        return 0;
    }

    private Integer visitIfStatement(JmmNode ifStatement, Integer dummy) {
        JmmNode condition = ifStatement.getJmmChild(0);
        JmmNode ifBlock = ifStatement.getJmmChild(1);
        JmmNode elseBlock = ifStatement.getJmmChild(2);

        visit(condition);
        code.append(ident(true)).append("Then").append(ifNumber).append(": \n");
        for(JmmNode ifChild : ifBlock.getChildren()){
            visit(ifChild);
        }

        code.append(ident()).append("goto EndIf").append(ifNumber).append(";\n");
        code.append(ident(true)).append("Else").append(ifNumber).append(": \n");

        for(JmmNode elseChild : elseBlock.getChildren()){
            visit(elseChild);
        }

        code.append(ident(true)).append("EndIf").append(ifNumber).append(":\n");
        ifNumber++;
        return 0;
    }

    private Integer visitWhileStatement(JmmNode whileStatement, Integer dummy) {
        JmmNode condition = whileStatement.getJmmChild(0);
        JmmNode whileBlock = whileStatement.getChildren().get(1);

        code.append(ident(true)).append("Loop").append(whileNumber).append(":\n");

        visit(condition);
        visit(whileBlock);

        code.append(ident(true)).append("EndLoop").append(whileNumber).append(":\n");
        whileNumber++;
        return 0;
    }

    private Integer visitAssignmentStatement(JmmNode assignmentStatement, Integer dummy) {
        OllirExprGenerator left = visitExpression(assignmentStatement.getJmmChild(0));
        OllirExprGenerator right = visitExpression(assignmentStatement.getJmmChild(1));

        code.append(left.getTemps());
        code.append(right.getTemps());

        code.append(ident())
            .append(left.getFullExp())
            .append(" :=.").append(left.getType())
            .append(" ").append(right.getFullExp()).append(";\n");

        return 0;
    }

    private Integer visitScope(JmmNode scope, Integer dummy) {
        for(JmmNode scopeChild : scope.getChildren()) {
            visit(scopeChild);
        }

        return 0;
    }

    private Integer visitCondition(JmmNode condition, Integer dummy) {
        StringBuilder temps = new StringBuilder();
        OllirExprGenerator cond = visitExpression(condition.getJmmChild(0));

        String t1 = newVar(cond.getType());
        temps.append(cond.getTemps());
        temps.append(ident()).append(newVarInstr(t1, cond.getType(), cond.getFullExp()));

        switch (condition.getJmmParent().getKind()) {
            case "IfStatement" -> {
                code.append(temps);
                code.append(ident()).append("if (").append(t1).append(") goto Then").append(ifNumber).append(";\n")
                    .append(ident()).append("goto Else").append(ifNumber).append(";\n");
            }
            case "WhileStatement" -> {
                code.append(temps);
                code.append(ident()).append("if (").append(t1).append(") goto Body").append(whileNumber).append(";\n")
                    .append(ident()).append("goto EndLoop").append(whileNumber).append(";\n");
            }
            default -> {
            }
        }

        return 0;
    }

    private Integer visitWhileBlock(JmmNode whileBlock, Integer dummy) {
        code.append(ident(true)).append("Body").append(whileNumber).append(":\n");
        visit(whileBlock.getJmmChild(0));
        code.append(ident()).append("goto Loop").append(whileNumber).append(";\n");

        return 0;
    }

    private OllirExprGenerator visitExpression(JmmNode expression) {
        return this.ollirExprVisitor.visit(expression);
    }

    static String ident() {
        return "\t".repeat(Math.max(0, identention));
    }
    static String ident(boolean label) {
        return "\t".repeat(Math.max(0, identention - 1)) + "  ";
    }

}
