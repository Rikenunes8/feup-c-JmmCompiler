package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.SimpleSymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {
    public SymbolTableVisitor() {
        addVisit("ImportStatement", this::visitImportStatements);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("PublicMain", this::visitPublicMain);
        addVisit("PublicMethod", this::visitPublicMethod);
        addVisit("IdentifierLiteral", this::visitIdentifier);
        addVisit("AddExp", this::visitArithmeticExpression);
        addVisit("SubExp", this::visitArithmeticExpression);
        addVisit("MultExp", this::visitArithmeticExpression);
        addVisit("DivExp", this::visitArithmeticExpression);
        addVisit("AndExp", this::visitAndExpression);
        addVisit("NotExp", this::visitNotExpression);
        addVisit("LessExp", this::visitLessThanExpression);
        addVisit("ArrayAccessExp", this::visitArrayAccessExpression);
        addVisit("AssignmentStatement", this::visitAssignmentStatement);
        addVisit("IfStatement", this::visitIfStatement);
    }

    private Boolean visitImportStatements(JmmNode importStatement, JmmAnalyser jmmAnalyser) {
        String importString = importStatement.getChildren().stream()
                .map(id -> id.get("val"))
                .collect(Collectors.joining("."));
        jmmAnalyser.getSymbolTable().addImport(importString);
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode classDeclaration, JmmAnalyser jmmAnalyser) {
        String className = classDeclaration.get("name");
        String extendedClass = null;
        try {
            extendedClass = classDeclaration.get("extends");
        }
        catch(NullPointerException e) { }

        jmmAnalyser.getSymbolTable().setClassName(className);
        jmmAnalyser.getSymbolTable().setSuper(extendedClass);

        for (JmmNode children: classDeclaration.getChildren()) {
            if (children.getKind().equals("VarDeclaration")) {
                Symbol field = new Symbol(this.getType(children.get("type")), children.get("var"));
                jmmAnalyser.getSymbolTable().addField(field);
            } else {
                break;
            }
        }

        return true;
    }

    private Boolean visitPublicMain(JmmNode publicMain, JmmAnalyser jmmAnalyser) {
        String methodName = "main";
        jmmAnalyser.getSymbolTable().addMethodType(methodName, this.getType("void"));
        Symbol parameter = new Symbol(new Type("String", true), publicMain.get("args"));
        jmmAnalyser.getSymbolTable().addMethodParameters(methodName, Arrays.asList(parameter));

        List<Symbol> methodLocalVariables = publicMain.getChildren().stream()
            .filter(children -> children.getKind().equals("VarDeclaration"))
            .map(id -> new Symbol(this.getType(id.get("type")), id.get("var")))
            .collect(Collectors.toList());
        jmmAnalyser.getSymbolTable().addMethodLocalVariables(methodName, methodLocalVariables);

        return true;
    }

    private Boolean visitPublicMethod(JmmNode publicMethod, JmmAnalyser jmmAnalyser) {
        String methodName = publicMethod.get("name");
        String methodType = publicMethod.get("type");
        jmmAnalyser.getSymbolTable().addMethodType(methodName, this.getType(methodType));

        List<Symbol> methodParameters = new ArrayList<>();
        if (!publicMethod.getChildren().isEmpty() && publicMethod.getChildren().get(0).getKind().equals("MethodParameters")) {
            methodParameters = publicMethod.getChildren().get(0).getChildren().stream()
                .map(id -> new Symbol(this.getType(id.get("type")), id.get("var")))
                .collect(Collectors.toList());
        }
        jmmAnalyser.getSymbolTable().addMethodParameters(methodName, methodParameters);

        List<Symbol> methodLocalVariables = publicMethod.getChildren().stream()
            .filter(children -> children.getKind().equals("VarDeclaration"))
            .map(id -> new Symbol(this.getType(id.get("type")), id.get("var")))
            .collect(Collectors.toList());
        jmmAnalyser.getSymbolTable().addMethodLocalVariables(methodName, methodLocalVariables);

        return true;
    }

    private Type getType(String typeSignature) {
      if (typeSignature.equals("int[]")) {
          return new Type("int", true);
      } else { 
          return new Type(typeSignature, false);
      }
    }

    private Boolean visitIdentifier(JmmNode identifier, JmmAnalyser jmmAnalyser) {
        String methodSignature = "";
        if (identifier.getAncestor("PublicMethod").isPresent()) {
            methodSignature = identifier.getAncestor("PublicMethod").get().get("name");
        } 
        else if (identifier.getAncestor("PublicMain").isPresent()) {
            methodSignature = "main";
        }
        if (!methodSignature.isEmpty()) {
            //identifier is a local variable
            if (jmmAnalyser.getSymbolTable().getLocalVariables(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val"))))
                return true;

            //identifier is a method parameter
            if (jmmAnalyser.getSymbolTable().getParameters(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val"))))
                return true;
        } 

        //identifier is a field of the class
        if (jmmAnalyser.getSymbolTable().getFields().stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val"))))
            return true;

        //it is a function call
        if (identifier.getJmmParent().getKind().equals("DotExp"))
            return true;

        System.out.println("Semantic Error: " + identifier.get("val") + " was not declared.");
        return false; // [TODO] retornar erro
    }

    private Boolean visitArithmeticExpression(JmmNode arithmeticExpression, JmmAnalyser jmmAnalyser) {
        JmmNode op1 = arithmeticExpression.getChildren().get(0);
        JmmNode op2 = arithmeticExpression.getChildren().get(1);

        Type op1Type = getType(op1, jmmAnalyser.getSymbolTable());
        Type op2Type = getType(op2, jmmAnalyser.getSymbolTable());

        if (op1Type.isArray() || op1Type.isArray()) {
            System.out.println("Semantic Error: array cannot be used in arithmetic operations.");
                return false;
        }

        if ((op1.getKind().equals("IntegerLiteral") || op1Type.getName().equals("int")) && (op2.getKind().equals("IntegerLiteral") || op2Type.getName().equals("int")))
            return true;
        
        System.out.println("Semantic Error: type missmatch in arithmetic expression.");
        return false; // [TODO] retornar erro
    }

    private Boolean visitAndExpression(JmmNode andExpression, JmmAnalyser jmmAnalyser) {
        JmmNode op1 = andExpression.getChildren().get(0);
        JmmNode op2 = andExpression.getChildren().get(1);

        Type op1Type = getType(op1, jmmAnalyser.getSymbolTable());
        Type op2Type = getType(op2, jmmAnalyser.getSymbolTable());

        if ((op1.getKind().equals("TrueLiteral") || op1.getKind().equals("FalseLiteral") || op1Type.getName().equals("boolean")) && (op2.getKind().equals("TrueLiteral") || op2.getKind().equals("FalseLiteral") || op2Type.getName().equals("boolean")))
            return true;
        
        System.out.println("Semantic Error: type missmatch in and expression.");
        return false; // [TODO] retornar erro
    }

    private Boolean visitNotExpression(JmmNode notExpression, JmmAnalyser jmmAnalyser) {
        JmmNode exp = notExpression.getChildren().get(0);
        Type expType = getType(exp, jmmAnalyser.getSymbolTable());

        if (exp.getKind().equals("TrueLiteral") || exp.getKind().equals("FalseLiteral") || expType.getName().equals("boolean"))
            return true;
        
        System.out.println("Semantic Error: type missmatch in not expression.");
        return false; // [TODO] retornar erro
    }

    private Boolean visitLessThanExpression(JmmNode lessThanExpression, JmmAnalyser jmmAnalyser) {
        JmmNode op1 = lessThanExpression.getChildren().get(0);
        JmmNode op2 = lessThanExpression.getChildren().get(1);

        Type op1Type = getType(op1, jmmAnalyser.getSymbolTable());
        Type op2Type = getType(op2, jmmAnalyser.getSymbolTable());

        if ((op1.getKind().equals("IntegerLiteral") || op1Type.getName().equals("int")) && (op2.getKind().equals("IntegerLiteral") || op2Type.getName().equals("int")))
            return true;
        
        System.out.println("Semantic Error: type missmatch in less than expression.");
        return false; // [TODO] retornar erro
    }

    private Boolean visitArrayAccessExpression(JmmNode arrayAccessExpression, JmmAnalyser jmmAnalyser) {
        JmmNode array = arrayAccessExpression.getChildren().get(0);
        JmmNode index = arrayAccessExpression.getChildren().get(1);

        Type arrayType = getType(array, jmmAnalyser.getSymbolTable());
        Type indexType = getType(index, jmmAnalyser.getSymbolTable());

        if (arrayType.isArray()){
            System.out.println("Semantic Error: array access must be done over an array.");
            return false; // [TODO] retornar erro
        }
        
        if (index.getKind().equals("IntegerLiteral") || indexType.getName().equals("int")) {
            System.out.println("Semantic Error: array access index must be an expression of type integer.");
            return false; // [TODO] retornar erro
        }

        return true;
    }

    private Boolean visitAssignmentStatement(JmmNode assignmentStatement, JmmAnalyser jmmAnalyser) {
        JmmNode op1 = assignmentStatement.getChildren().get(0);
        JmmNode op2 = assignmentStatement.getChildren().get(1);

        Type op1Type = getType(op1, jmmAnalyser.getSymbolTable());
        Type op2Type = getType(op2, jmmAnalyser.getSymbolTable());

        if (op1Type.getName().equals(op2Type.getName()))
            return true;
        
        System.out.println("Semantic Error: type of the assignee must be compatible with the assigned.");
        return false; // [TODO] retornar erro
    }

    private Boolean visitIfStatement(JmmNode ifStatement, JmmAnalyser jmmAnalyser) { // [TODO] está errado (só está a analisar o primeiro filho)
        JmmNode conditionExp = ifStatement.getChildren().get(0).getChildren().get(0);

        Type conditionExpType = getType(conditionExp, jmmAnalyser.getSymbolTable());

        if (conditionExp.getKind().equals("TrueLiteral") || conditionExp.getKind().equals("FalseLiteral") || conditionExpType.getName().equals("boolean"))
            return true;
    
        System.out.println("Semantic Error: expressions in conditions must return a boolean.");
        return false; // [TODO] retornar erro
    }

    private Type getType(JmmNode var, SimpleSymbolTable symbolTable) { 

        if (var.getKind().equals("TrueLiteral") || var.getKind().equals("FalseLiteral") || var.getKind().equals("AndExp") || 
            var.getKind().equals("NotExp") || var.getKind().equals("LessExp") || var.getKind().equals("Condition"))
            return new Type("boolean", false);
            
        if (var.getKind().equals("IntegerLiteral") || var.getKind().equals("AddExp") || var.getKind().equals("SubExp") || 
            var.getKind().equals("MultExp") || var.getKind().equals("DivExp") || var.getKind().equals("ArrayAccess"))
            return new Type("int", false);

        if (var.getKind().equals("NewIntArray"))
            return new Type("int", true);

        String methodSignature = "";
        if (var.getAncestor("PublicMethod").isPresent()) {
            methodSignature = var.getAncestor("PublicMethod").get().get("name");
        } 
        else if (var.getAncestor("PublicMain").isPresent()) {
            methodSignature = "main";
        }
        if (!methodSignature.isEmpty()) {
            //var is a local variable
            List<Symbol> localVariables = symbolTable.getLocalVariables(methodSignature).stream()
                .filter(symbol -> symbol.getName().equals(var.get("val")))
                .collect(Collectors.toList());
            if (!localVariables.isEmpty())
                return localVariables.get(0).getType();

            //var is a method parameter
            List<Symbol> methodParameters = symbolTable.getParameters(methodSignature).stream()
                .filter(symbol -> symbol.getName().equals(var.get("val")))
                .collect(Collectors.toList());
            if (!methodParameters.isEmpty())
                return methodParameters.get(0).getType();
        } 

        //var is a field of the class
        List<Symbol> classFields = symbolTable.getFields().stream()
            .filter(symbol -> symbol.getName().equals(var.get("val")))
            .collect(Collectors.toList());
        if (!classFields.isEmpty())
            return classFields.get(0).getType();
        
        return new Type(null, false);
    }

}

