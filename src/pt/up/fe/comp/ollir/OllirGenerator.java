package pt.up.fe.comp.ollir;
import pt.up.fe.comp.analysis.SymbolTableBuilder;

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
    private final SymbolTableBuilder symbolTable;
    private final ExprVisitor exprVisitor;
    private int whileNumber;
    private int ifNumber;

    public OllirGenerator(SymbolTableBuilder symbolTable) {
        this.exprVisitor = new ExprVisitor(symbolTable);
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.whileNumber = 0;
        this.ifNumber = 0;

        // THIS_LITERAL,
        // ADD_EXP,
        // SUB_EXP,
        // MULT_EXP,
        // DIV_EXP,
        // AND_EXP,
        // NOT_EXP,
        // LESS_EXP,
        // NEW_INT_ARRAY,

        // NEW_OBJECT,
        // VAR_DECLARATION,
        // PROPERTY_LENGTH,
        // IF_BLOCK,
        // ELSE_BLOCK,
        // ,
        // SCOPE

        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECLARATION, this::visitClassDeclaration);
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
        addVisit(RETURN_STATEMENT, this::visitReturnStatement);
        addVisit(DOT_EXP, this::visitDotExp);
        
        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
        addVisit(NEW_OBJECT, this::visitNewObject);
        addVisit(FUNCTION_CALL, this::visitFunctionCall);
        addVisit(CONDITION, this::visitCondition);
        addVisit(WHILE_BLOCK, this::visitWhileBlock);
        
        
        addVisit(IF_STATEMENT, this::visitIfStatement);
        addVisit(WHILE_STATEMENT, this::visitWhileStatement);
        addVisit(SCOPE, this::visitScope);
        addVisit(ASSIGNMENT_STATEMENT, this::visitAssignmentStatement);
        addVisit(EXPRESSION_STATEMENT, this::visitExpressionStatement);
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
            if (child.getKind().equals(METHOD_DECLARATION.toString()))
                visit(child);
        }
        code.append("\n}\n");

        return 0;
    }

    private Integer visitPrivateAttibutes() {
        for(Symbol field : symbolTable.getFields()){
            code.append("\t").append(".field private ");
            code.append(OllirUtils.getCode(field));
            //code.append(field.getName());
            //code.append(".").append(OllirUtils.getCode(field.getType()));
            code.append(";\n");
        }
        return 0;
    }

    private Integer visitReturnStatement(JmmNode returnStatement, Integer dummy) {
        OllirExprPair result = visitExpression(returnStatement.getJmmChild(0));
        JmmNode methodDeclaration = returnStatement.getJmmParent();
        String methodSignature = methodDeclaration.get("name");
        
        code.append(result.getTemps());
        code.append("\t\t").append("ret.");
        var returnType = OllirUtils.getCode(symbolTable.getReturnType(methodSignature));
        code.append(returnType);
        if( returnType != "V")
        {
            code.append(" ");
            code.append(result.getExpression());
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
            if(!methodDeclaration.getJmmChild(i).getKind().equals(METHOD_PARAMETERS.toString()) &&
                !methodDeclaration.getJmmChild(i).getKind().equals(VAR_DECLARATION.toString()))
            {
                lastParamIndex = i;
                break;
            }
        }

        var stmts = methodDeclaration.getChildren().subList(lastParamIndex, methodDeclaration.getNumChildren());
        System.out.println("\nSTMTS :\n" + stmts);

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
        code.append("\t ").append("Loop" + number + ":\n");

        JmmNode condition = whileStatement.getJmmChild(0);
        visit(condition);

        JmmNode whileBlock = whileStatement.getChildren().get(1);
        visit(whileBlock);
        
        code.append("\t ").append("EndLoop" + number + ":\n");
        whileNumber++;
        return 0;
    }

    private Integer visitIfStatement(JmmNode ifStatement, Integer dummy) {
        String number = Integer.toString(ifNumber);

        JmmNode condition = ifStatement.getJmmChild(0);
        visit(condition);

        JmmNode ifBlock = ifStatement.getJmmChild(1);

        for(JmmNode ifChild : ifBlock.getChildren()){
            visit(ifChild);
        }
        
        code.append("\t\t").append("goto EndIf" + number + ";\n");
        code.append("\t").append("Else" + number +": \n");
        
        JmmNode elseBlock = ifStatement.getJmmChild(2);
        for(JmmNode elseChild : elseBlock.getChildren()){
            visit(elseChild);
        }

        code.append("\t").append("EndIf" + number + ":\n");
        ifNumber++;
        return 0;
    }

    private Integer visitAssignmentStatement(JmmNode assignmentStatement, Integer dummy) {
        JmmNode left = assignmentStatement.getJmmChild(0); 
        JmmNode right = assignmentStatement.getJmmChild(1); 

        OllirExprPair resultLeft = visitExpression(left);
        OllirExprPair resultRight = visitExpression(right);

        String type = OllirUtils.getType(resultLeft.getExpression());
        code.append(resultLeft.getTemps());
        code.append(resultRight.getTemps());
    
        code.append("\t\t").append(resultLeft.getExpression());
        code.append(" :=.").append(type);
        code.append(" ").append(resultRight.getExpression()).append(";\n");
       
        return 0;
    }

    private Integer visitExpressionStatement(JmmNode expressionStatement, Integer dummy) {
        OllirExprPair exprPair = this.visitExpression(expressionStatement.getJmmChild(0));
        System.out.println(exprPair);
        code.append(exprPair.getTemps());
        code.append("\t\t").append(exprPair.getExpression()).append(";\n");
        return 0;
    }

    private Integer visitCondition(JmmNode condition, Integer dummy) {
        String number = ""; 
        
        var child = condition.getJmmChild(0);
        OllirExprPair conditionCode = visitExpression(child);
        
        String newAuxVariables = "", ollirCondition = "";
        newAuxVariables = conditionCode.getTemps();
        ollirCondition = conditionCode.getExpression();
        
        
        if(child.getKind().equals("LessExp") || child.getKind().equals("AndExp") || child.getKind().equals("NotExp"))
        {
            ollirCondition += " &&.bool 1.bool";
        }
        
        switch(condition.getJmmParent().getKind())
        {
            case "IfStatement":
                number = Integer.toString(ifNumber);
                code.append(newAuxVariables).append("\n");
                code.append("\t\t").append("if (" + ollirCondition + ") " + "goto Else" + number + ";\n");
                break;
            case "WhileStatement":
                number = Integer.toString(whileNumber);
                code.append(newAuxVariables).append("\n");
                code.append("\t\t").append("if (");
                code.append(ollirCondition);
                code.append(") goto Body" + number + ";\n") ;
                code.append("\t\t").append("goto EndLoop" + number + ";\n");
                code.append("\t\t").append("Body" + number + ":\n");
                break;
            default:
                break;
        }



        return 0;
    }

    private Integer visitWhileBlock(JmmNode whileBlock, Integer dummy) {
        String number = Integer.toString(whileNumber);
        code.append("\t ").append("Body" + number + ":\n");
        
        visit(whileBlock.getJmmChild(0));

        code.append("\t ").append("goto Loop" + number + ";\n");

        return 0;
    }

    private Integer visitScope(JmmNode scope, Integer dummy) {
        
        List<JmmNode> scopeChilds = scope.getChildren();

        for(JmmNode scopeChild : scopeChilds)
        {
            visit(scopeChild);
        }

        return 0;
    }

    private OllirExprPair visitExpression(JmmNode expression) {
        return this.exprVisitor.visit(expression);
    }
}
