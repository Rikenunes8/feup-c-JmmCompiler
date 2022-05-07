package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;
import static pt.up.fe.comp.ast.AstNode.*;


// TODO Change many things
public class OllirGenerator extends AJmmVisitor<Integer, Integer> {

    private final StringBuilder code;
    private final SymbolTable symbolTable;

    public OllirGenerator(SymbolTable symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;

        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECLARATION, this::visitClassDeclaration);
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
        addVisit(DOT_EXP, this::visitExprStmt);

        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
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
        // JMM visitor conseguir controlar quando é que os filhos são visitados
        for (var child : classDeclaration.getChildren()) {
            visit(child);
        }
        code.append("\n}\n");

        return 0;
    }

    private Integer visitMethodDeclaration(JmmNode methodDeclaration, Integer dummy) {

        String methodSignature = methodDeclaration.get("name");
        boolean isStatic = Boolean.parseBoolean(methodDeclaration.get("static"));
        code.append("\n\t.method public ");
        if (isStatic) code.append("static ");
        code.append(methodSignature).append("(");

        List<Symbol> params = symbolTable.getParameters(methodSignature);
        String paramCode = params.stream().map(symbol -> OllirUtils.getCode(symbol)).collect(Collectors.joining(", "));

        code.append(paramCode);
        code.append(").");
        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodSignature)));
        code.append(" {\n");

        // aqui as coisas para visitar os correspondentes
        
        int lastParamIndex = -1;
        for (int i = 0; i< methodDeclaration.getNumChildren(); i++)
        {
            if(methodDeclaration.getJmmChild(i).getKind().equals("MethodParameters"))
            {
                lastParamIndex = i;
            }
        }

        var stmts = methodDeclaration.getChildren().subList(lastParamIndex + 1, methodDeclaration.getNumChildren());
        System.out.println("\nSTMTS :\n" + stmts);

        for (var stmt : stmts) {
            visit(stmt);
        }


        code.append("\t}\n");

        return 0;
    }

    private Integer visitExprStmt(JmmNode exprStmt, Integer dummy) {
        code.append("invokestatic(");
        visit(exprStmt.getJmmChild(0));
        // code.append( exprStmt.getJmmChild(0).get("val"));
        code.append(", \"");
        // code.append( exprStmt.getJmmChild(1).get("name"));
        visit(exprStmt.getJmmChild(1));
        code.append("\"");
        if(exprStmt.getNumChildren() > 2)
        {
            visit(exprStmt.getJmmChild(2));
        }
        code.append(").").append("V;");

        return 0;
    }

    private Integer visitIdentifierLiteral(JmmNode identifierLiteral, Integer dummy) {
        code.append( identifierLiteral.get("val"));
        return 0;
    }

    private Integer visitFunctionCall(JmmNode functionCall, Integer dummy) {
        code.append( functionCall.get("name"));
        return 0;
    }

}
