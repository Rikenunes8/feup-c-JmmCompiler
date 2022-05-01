package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.Utils.*;


public class TypeCheckingVisitor extends SemanticAnalyserVisitor {
    public TypeCheckingVisitor() {
        super();
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
        addVisit("NewIntArray", this::visitNewIntArray);
        addVisit("DotExp", this::visitDotExp);
    }

    private Boolean visitDotExp(JmmNode dotExp, SymbolTableBuilder symbolTable) {
        JmmNode left = dotExp.getJmmChild(0);
        JmmNode right = dotExp.getJmmChild(1);

        Type leftType = getType(left, symbolTable);

        if (leftType == null)
            return true;

        if (right.getKind().equals("PropertyLength")) {
            if (leftType.isArray())
                return true;
            addReport(left, "length is a property of an array");
            return false;
        }

        if (!isBuiltInType(leftType))
            return true;
        addReport(left, "Built in types has no methods");
        return false;
    }

    private Boolean visitIdentifier(JmmNode identifier, SymbolTableBuilder symbolTable) {
        if (isIdentifierDeclared(identifier, symbolTable))
            return true;

        if (identifier.getJmmParent().getKind().equals("DotExp"))
            return true;

        this.addReport(identifier, "Variable used is not declared.");
        return false;
    }

    private Boolean visitArithmeticExpression(JmmNode arithmeticExpression, SymbolTableBuilder symbolTable) {
        JmmNode op1 = arithmeticExpression.getChildren().get(0);
        JmmNode op2 = arithmeticExpression.getChildren().get(1);

        Type op1Type = getType(op1, symbolTable);
        Type op2Type = getType(op2, symbolTable);

        if (op1Type == null || op2Type == null)
            return true;

        if (op1.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(op1, symbolTable)) //operand is not declared
            return false;     

        if (op2.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(op2, symbolTable)) //operand is not declared
            return false;     

        if (op1Type.isArray() || op2Type.isArray()) {
            this.addReport(arithmeticExpression, "Arrays cannot be used in arithmetic operations.");
            return false;
        }

        if ((!op1.getKind().equals("IntegerLiteral") && !op1Type.getName().equals("int")) || (!op2.getKind().equals("IntegerLiteral") && !op2Type.getName().equals("int"))){        
            this.addReport(arithmeticExpression, "The operands in an arithmetic expression must be integers.");
            return false;
        }
        
        return true;
    }

    private Boolean visitAndExpression(JmmNode andExpression, SymbolTableBuilder symbolTable) {
        JmmNode op1 = andExpression.getChildren().get(0);
        JmmNode op2 = andExpression.getChildren().get(1);

        Type op1Type = getType(op1, symbolTable);
        Type op2Type = getType(op2, symbolTable);

        if (op1Type == null || op2Type == null)
            return true;

        if (op1.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(op1, symbolTable)) //operand is not declared
            return false;     

        if (op2.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(op2, symbolTable)) //operand is not declared
            return false;  

        if ((!op1.getKind().equals("TrueLiteral") && !op1.getKind().equals("FalseLiteral") && !op1Type.getName().equals("boolean"))
                || (!op2.getKind().equals("TrueLiteral") && !op2.getKind().equals("FalseLiteral") && !op2Type.getName().equals("boolean"))) {
            this.addReport(andExpression, "The operands in a logic expression must be booleans.");
            return false;
        }
        
        return true;
    }

    private Boolean visitNotExpression(JmmNode notExpression, SymbolTableBuilder symbolTable) {
        JmmNode exp = notExpression.getChildren().get(0);

        Type expType = getType(exp, symbolTable);

        if (expType == null)
            return true;

        if (exp.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(exp, symbolTable)) //operand is not declared
            return false;     

        if (!exp.getKind().equals("TrueLiteral") && !exp.getKind().equals("FalseLiteral") && !expType.getName().equals("boolean")) {
            this.addReport(notExpression, "The operands in a logic expression must be booleans.");
            return false;
        }

        return true;
    }

    private Boolean visitLessThanExpression(JmmNode lessThanExpression, SymbolTableBuilder symbolTable) {
        JmmNode op1 = lessThanExpression.getChildren().get(0);
        JmmNode op2 = lessThanExpression.getChildren().get(1);

        Type op1Type = getType(op1, symbolTable);
        Type op2Type = getType(op2, symbolTable);

        if (op1Type == null || op2Type == null)
            return true;

        if (op1.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(op1, symbolTable)) //operand is not declared
            return false;     

        if (op2.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(op2, symbolTable)) //operand is not declared
            return false;  

        if ((!op1.getKind().equals("IntegerLiteral") && !op1Type.getName().equals("int")) || (!op2.getKind().equals("IntegerLiteral") && !op2Type.getName().equals("int"))) {
            this.addReport(lessThanExpression, "The operands in a comparison expression must be integers.");
            return false;
        }
            
        return true; 
    }

    private Boolean visitArrayAccessExpression(JmmNode arrayAccessExpression, SymbolTableBuilder symbolTable) {
        JmmNode array = arrayAccessExpression.getChildren().get(0);
        JmmNode index = arrayAccessExpression.getChildren().get(1);

        Type arrayType = getType(array, symbolTable);
        Type indexType = getType(index, symbolTable);

        if (arrayType == null || indexType == null)
            return true;

        if (index.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(index, symbolTable)) //operand is not declared
            return false;  
    
        if (!arrayType.isArray()){
            this.addReport(arrayAccessExpression, "Array access must be done over an array.");
            return false;
        }

        if (!index.getKind().equals("IntegerLiteral") && !indexType.getName().equals("int")) {
            this.addReport(arrayAccessExpression, "Array access index must be an expression of type integer.");
            return false;
        }

        return true;
    }

    private Boolean visitNewIntArray(JmmNode newIntArray, SymbolTableBuilder symbolTable) {
        JmmNode index = newIntArray.getJmmChild(0);
        Type indexType = getType(index, symbolTable);

        if (indexType == null)
            return true;

        if (!indexType.getName().equals("int")) {
            this.addReport(index, "New Int Array size must be an expression of type integer.");
            return false;
        }
        return true;
    }

    private Boolean visitAssignmentStatement(JmmNode assignmentStatement, SymbolTableBuilder symbolTable) {
        JmmNode assigned = assignmentStatement.getJmmChild(0);
        JmmNode assignee = assignmentStatement.getJmmChild(1);

        Type assignedType = getType(assigned, symbolTable);
        Type assigneeType = getType(assignee, symbolTable);

        if (assigneeType == null || assignedType == null)
            return true;

        JmmNode assignedIdentifier = assigned.getKind().equals("ArrayAccess") ? assigned.getJmmChild(0) : assigned;

        if (!isIdentifierDeclared(assignedIdentifier, symbolTable)) //assigned is not declared
            return false; 

        if (!assignedType.getName().equals(assigneeType.getName())){
            this.addReport(assignmentStatement, "Type of the assignee must be compatible with the assigned.");
            return false;
        }
                
        return true;
    }

    private Boolean visitIfStatement(JmmNode ifStatement, SymbolTableBuilder symbolTable) {
        JmmNode conditionExp = ifStatement.getChildren().get(0).getChildren().get(0);

        Type conditionExpType = getType(conditionExp, symbolTable);

        if (conditionExpType == null)
            return true;

        if (conditionExp.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(conditionExp, symbolTable)) //operand is not declared
            return false;    

        if (!conditionExp.getKind().equals("TrueLiteral") && !conditionExp.getKind().equals("FalseLiteral") && !conditionExpType.getName().equals("boolean")) {
            this.addReport(conditionExp, "Expression in a condition must return a boolean.");
            return false;
        }
        
        return true;
    }
}