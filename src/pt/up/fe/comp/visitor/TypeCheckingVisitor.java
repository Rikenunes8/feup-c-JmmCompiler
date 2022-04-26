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

public class TypeCheckingVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {

    public TypeCheckingVisitor() {
        addVisit("IdentifierLiteral", this::visitIdentifier);
        addVisit("AddExp", this::visitArithmeticExpression);
        addVisit("SubExp", this::visitArithmeticExpression);
        addVisit("MultExp", this::visitArithmeticExpression);
        addVisit("DivExp", this::visitArithmeticExpression);
        addVisit("AndExp", this::visitAndExpression);
        addVisit("NotExp", this::visitNotExpression);
        addVisit("LessExp", this::visitLessThanExpression);
        addVisit("ArrayAccess", this::visitArrayAccessExpression);
        addVisit("AssignmentStatement", this::visitAssignmentStatement);
        addVisit("IfStatement", this::visitIfStatement);
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

            if (jmmAnalyser.getSymbolTable().getLocalVariables(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a local variable
                return true;

            if (jmmAnalyser.getSymbolTable().getParameters(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a method parameter
                return true;
        } 

        if (jmmAnalyser.getSymbolTable().getFields().stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a field of the class
            return true;

        if (identifier.getJmmParent().getKind().equals("DotExp")) //it is a function call (not checked here)
            return true;

        jmmAnalyser.addReport(identifier, "Variable used is not declared.");
        return false;
    }

    private Boolean visitArithmeticExpression(JmmNode arithmeticExpression, JmmAnalyser jmmAnalyser) {
        JmmNode op1 = arithmeticExpression.getChildren().get(0);
        JmmNode op2 = arithmeticExpression.getChildren().get(1);

        Type op1Type = getType(op1, jmmAnalyser.getSymbolTable());
        Type op2Type = getType(op2, jmmAnalyser.getSymbolTable());

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (op1.getKind().equals("IdentifierLiteral") && !visitIdentifier(op1, jmmAnalyser)) //operand is not declared 
            return false;     

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (op2.getKind().equals("IdentifierLiteral") && !visitIdentifier(op2, jmmAnalyser)) //operand is not declared 
            return false;     

        if (op1Type.isArray() || op1Type.isArray()) {
            jmmAnalyser.addReport(arithmeticExpression, "Arrays cannot be used in arithmetic operations.");
            return false;
        }

        if ((!op1.getKind().equals("IntegerLiteral") && !op1Type.getName().equals("int")) || (!op2.getKind().equals("IntegerLiteral") && !op2Type.getName().equals("int"))){        
            jmmAnalyser.addReport(arithmeticExpression, "The operands in an arithmetic expression must be integers.");
            return false;
        }
        
        return true;
    }

    private Boolean visitAndExpression(JmmNode andExpression, JmmAnalyser jmmAnalyser) {
        JmmNode op1 = andExpression.getChildren().get(0);
        JmmNode op2 = andExpression.getChildren().get(1);

        Type op1Type = getType(op1, jmmAnalyser.getSymbolTable());
        Type op2Type = getType(op2, jmmAnalyser.getSymbolTable());  

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (op1.getKind().equals("IdentifierLiteral") && !visitIdentifier(op1, jmmAnalyser)) //operand is not declared 
            return false;     

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (op2.getKind().equals("IdentifierLiteral") && !visitIdentifier(op2, jmmAnalyser)) //operand is not declared 
            return false;  

        if ((!op1.getKind().equals("TrueLiteral") && !op1.getKind().equals("FalseLiteral") && !op1Type.getName().equals("boolean")) || (!op2.getKind().equals("TrueLiteral") && !op2.getKind().equals("FalseLiteral") && !op2Type.getName().equals("boolean"))) {
            jmmAnalyser.addReport(andExpression, "The operands in a logic expression must be booleans.");
            return false;
        }
        
        return true;
    }

    private Boolean visitNotExpression(JmmNode notExpression, JmmAnalyser jmmAnalyser) {
        JmmNode exp = notExpression.getChildren().get(0);
        Type expType = getType(exp, jmmAnalyser.getSymbolTable());

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (exp.getKind().equals("IdentifierLiteral") && !visitIdentifier(exp, jmmAnalyser)) //operand is not declared 
            return false;     

        if (!exp.getKind().equals("TrueLiteral") && !exp.getKind().equals("FalseLiteral") && !expType.getName().equals("boolean")) {
            jmmAnalyser.addReport(notExpression, "The operands in a logic expression must be booleans.");
            return false;
        }

        return true;
    }

    private Boolean visitLessThanExpression(JmmNode lessThanExpression, JmmAnalyser jmmAnalyser) {
        JmmNode op1 = lessThanExpression.getChildren().get(0);
        JmmNode op2 = lessThanExpression.getChildren().get(1);

        Type op1Type = getType(op1, jmmAnalyser.getSymbolTable());
        Type op2Type = getType(op2, jmmAnalyser.getSymbolTable());

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (op1.getKind().equals("IdentifierLiteral") && !visitIdentifier(op1, jmmAnalyser)) //operand is not declared 
            return false;     

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (op2.getKind().equals("IdentifierLiteral") && !visitIdentifier(op2, jmmAnalyser)) //operand is not declared 
            return false;  

        if ((!op1.getKind().equals("IntegerLiteral") && !op1Type.getName().equals("int")) || (!op2.getKind().equals("IntegerLiteral") && !op2Type.getName().equals("int"))) {
            jmmAnalyser.addReport(lessThanExpression, "The operands in a comparison expression must be integers.");
            return false;
        }
            
        return true; 
    }

    private Boolean visitArrayAccessExpression(JmmNode arrayAccessExpression, JmmAnalyser jmmAnalyser) {
        JmmNode array = arrayAccessExpression.getChildren().get(0);
        JmmNode index = arrayAccessExpression.getChildren().get(1);

        Type arrayType = getType(array, jmmAnalyser.getSymbolTable());
        Type indexType = getType(index, jmmAnalyser.getSymbolTable());

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (index.getKind().equals("IdentifierLiteral") && !visitIdentifier(index, jmmAnalyser)) //operand is not declared 
            return false;  
    
        if (!arrayType.isArray()){
            jmmAnalyser.addReport(arrayAccessExpression, "Array access must be done over an array.");
            return false;
        }

        if (!index.getKind().equals("IntegerLiteral") && !indexType.getName().equals("int")) {
            jmmAnalyser.addReport(arrayAccessExpression, "Array access index must be an expression of type integer.");
            return false;
        }

        return true;
    }

    private Boolean visitAssignmentStatement(JmmNode assignmentStatement, JmmAnalyser jmmAnalyser) {
        JmmNode assigned = assignmentStatement.getChildren().get(0);
        JmmNode assignee = assignmentStatement.getChildren().get(1);

        Type assignedType = getType(assigned, jmmAnalyser.getSymbolTable());
        Type assigneeType = getType(assignee, jmmAnalyser.getSymbolTable());

        JmmNode assignedIdentifier = assigned.getKind().equals("ArrayAccess") ? assigned.getChildren().get(0) : assigned;

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (!visitIdentifier(assignedIdentifier, jmmAnalyser)) //assigned is not declared 
            return false; 

        if (!assignedType.getName().equals(assigneeType.getName())){
            jmmAnalyser.addReport(assignmentStatement, "Type of the assignee must be compatible with the assigned.");
            return false;
        }
                
        return true;
    }

    private Boolean visitIfStatement(JmmNode ifStatement, JmmAnalyser jmmAnalyser) {
        JmmNode conditionExp = ifStatement.getChildren().get(0).getChildren().get(0);

        Type conditionExpType = getType(conditionExp, jmmAnalyser.getSymbolTable());

        // [TODO] isto está a fazer com que imprima o erro de 'variable not declared' 2 vezes
        if (conditionExp.getKind().equals("IdentifierLiteral") && !visitIdentifier(conditionExp, jmmAnalyser)) //operand is not declared 
            return false;    

        if (!conditionExp.getKind().equals("TrueLiteral") && !conditionExp.getKind().equals("FalseLiteral") && !conditionExpType.getName().equals("boolean")) {
            jmmAnalyser.addReport(conditionExp, "Expression in a condition must return a boolean.");
            return false;
        }
        
        return true;
    }

    private Type getType(JmmNode var, SimpleSymbolTable symbolTable) { 

        if (var.getKind().matches("TrueLiteral|FalseLiteral|AndExp|NotExp|LessExp|Condition"))
            return new Type("boolean", false);
            
        if (var.getKind().matches("IntegerLiteral|AddExp|SubExp|MultExp|DivExp|ArrayAccess"))
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
            List<Symbol> localVariables = symbolTable.getLocalVariables(methodSignature).stream()
                .filter(symbol -> symbol.getName().equals(var.get("val")))
                .collect(Collectors.toList());
            if (!localVariables.isEmpty()) //var is a local variable
                return localVariables.get(0).getType();

            List<Symbol> methodParameters = symbolTable.getParameters(methodSignature).stream()
                .filter(symbol -> symbol.getName().equals(var.get("val")))
                .collect(Collectors.toList());
            if (!methodParameters.isEmpty()) //var is a method parameter
                return methodParameters.get(0).getType();
        } 

        List<Symbol> classFields = symbolTable.getFields().stream()
            .filter(symbol -> symbol.getName().equals(var.get("val")))
            .collect(Collectors.toList());
        if (!classFields.isEmpty()) //var is a field of the class
            return classFields.get(0).getType();
        
        return new Type(null, false);
    }
}