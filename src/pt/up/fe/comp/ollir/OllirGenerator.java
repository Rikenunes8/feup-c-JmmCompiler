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
    private int whileNumber;

    public OllirGenerator(SymbolTable symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.whileNumber = 0;

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
        // VAR_DECLARATION,
        // PROPERTY_LENGTH,
        // CONDITION
        // IF_STATEMENT,
        // IF_BLOCK,
        // ELSE_BLOCK,
        // WHILE_STATEMENT,
        // WHILE_BLOCK,
        // SCOPE

        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECLARATION, this::visitClassDeclaration);
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
        addVisit(VAR_DECLARATION, this::visitVarDeclaration);
        addVisit(RETURN_STATEMENT, this::visitReturnStatement);
        addVisit(WHILE_STATEMENT, this::visitWhileStatement);
        addVisit(DOT_EXP, this::visitDotExp);

        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
        addVisit(NEW_OBJECT, this::visitNewObject);
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
        visitPrivateAttibutes();
        // JMM visitor conseguir controlar quando é que os filhos são visitados
        for (var child : classDeclaration.getChildren()) {
            visit(child);
        }
        code.append("\n}\n");

        return 0;
    }

    private Integer visitPrivateAttibutes() {
        for(Symbol field : symbolTable.getFields()){
            code.append("\t").append(".field private ");
            code.append(field.getName());
            code.append(".").append(OllirUtils.getCode(field.getType()));
            code.append(";\n");
        }
        return 0;
    }

    private Integer visitVarDeclaration(JmmNode varDeclaration, Integer dummy) {
        return 0;
    }

    private Integer visitReturnStatement(JmmNode returnStatement, Integer dummy) {
        JmmNode methodDeclaration = returnStatement.getJmmParent();
        String methodSignature = methodDeclaration.get("name");
        
        code.append("\t\t").append("ret.");
        var returnType = OllirUtils.getCode(symbolTable.getReturnType(methodSignature));
        code.append(returnType);
        if( returnType != "V")
        {
            code.append(" ");
            visit(returnStatement.getJmmChild(0));
            code.append(".").append(returnType);
        }

        
        code.append(";\n");
        
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
        //System.out.println("\nSTMTS :\n" + stmts);

        for (var stmt : stmts) {
            visit(stmt);
        }


        code.append("\t}\n");

        return 0;
    }

    private Integer visitDotExp(JmmNode dotExp, Integer dummy) {
        code.append("\t\t").append("invokestatic(");
        visit(dotExp.getJmmChild(0));
        // code.append( dotExp.getJmmChild(0).get("val"));
        code.append(", \"");
        // code.append( dotExp.getJmmChild(1).get("name"));
        visit(dotExp.getJmmChild(1));
        code.append("\"");
        if(dotExp.getNumChildren() > 2)
        {
            visit(dotExp.getJmmChild(2));
        }
        code.append(").").append("V;\n");

        return 0;
    }

    private Integer visitNewObject(JmmNode newObject, Integer dummy) {
        // TODO
        return 0;
    }

    private Integer visitIdentifierLiteral(JmmNode identifierLiteral, Integer dummy) {
        code.append(identifierLiteral.get("val"));
        return 0;
    }

    private Integer visitFunctionCall(JmmNode functionCall, Integer dummy) {
        code.append(functionCall.get("name"));
        return 0;
    }

    private Integer visitWhileStatement(JmmNode whileStatement, Integer dummy) {
        String number = Integer.toString(whileNumber);
        // condition
        visit(whileStatement.getJmmChild(0));
        JmmNode whileBlock = whileStatement.getChildren().get(1);

        code.append("\t ").append("Loop" + number + ":\n");
        return 0;
    }
}
