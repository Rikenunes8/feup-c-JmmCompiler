package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.Utils.*;
import static pt.up.fe.comp.ast.AstNode.*;


public class TypeCheckingVisitor extends SemanticAnalyserVisitor {
    public TypeCheckingVisitor() {
        super();
        addVisit(IDENTIFIER_LITERAL, this::visitIdentifier);
        addVisit(ADD_EXP, this::visitArithmeticExpression);
        addVisit(SUB_EXP, this::visitArithmeticExpression);
        addVisit(MULT_EXP, this::visitArithmeticExpression);
        addVisit(DIV_EXP, this::visitArithmeticExpression);
        addVisit(AND_EXP, this::visitAndExpression);
        addVisit(NOT_EXP, this::visitNotExpression);
        addVisit(LESS_EXP, this::visitLessThanExpression);
        addVisit(ARRAY_ACCESS_EXP, this::visitArrayAccessExpression);
        addVisit(ASSIGNMENT_STATEMENT, this::visitAssignmentStatement);
        addVisit(CONDITION, this::visitCondition);
        addVisit(NEW_INT_ARRAY, this::visitNewIntArray);
        addVisit(DOT_EXP, this::visitDotExp);
    }

    private Boolean visitDotExp(JmmNode dotExp, SymbolTableBuilder symbolTable) {
        JmmNode left = dotExp.getJmmChild(0);
        JmmNode right = dotExp.getJmmChild(1);

        Type leftType = getType(left, symbolTable);

        if (leftType == null)
            return true;

        if (right.getKind().equals(PROPERTY_LENGTH.toString())) {
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

        if (identifier.getJmmParent().getKind().equals(DOT_EXP.toString()))
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

        if (op1.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(op1, symbolTable)) //operand is not declared
            return false;

        if (op2.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(op2, symbolTable)) //operand is not declared
            return false;

        if (op1Type.isArray() || op2Type.isArray()) {
            this.addReport(arithmeticExpression, "Arrays cannot be used in arithmetic operations.");
            return false;
        }

        if (!op1Type.getName().equals("int") || !op2Type.getName().equals("int")){
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

        if (op1.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(op1, symbolTable)) //operand is not declared
            return false;

        if (op2.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(op2, symbolTable)) //operand is not declared
            return false;

        if (!op1Type.getName().equals("boolean") || !op2Type.getName().equals("boolean")) {
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

        if (exp.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(exp, symbolTable)) //operand is not declared
            return false;

        if (!expType.getName().equals("boolean")) {
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

        if (op1.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(op1, symbolTable)) //operand is not declared
            return false;

        if (op2.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(op2, symbolTable)) //operand is not declared
            return false;

        if (!op1Type.getName().equals("int") || !op2Type.getName().equals("int")) {
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

        if (index.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(index, symbolTable)) //operand is not declared
            return false;

        if (!arrayType.isArray()){
            this.addReport(arrayAccessExpression, "Array access must be done over an array.");
            return false;
        }

        if (!indexType.getName().equals("int")) {
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

        JmmNode assignedIdentifier = assigned.getKind().equals(ARRAY_ACCESS_EXP.toString()) ? assigned.getJmmChild(0) : assigned;

        if (!isIdentifierDeclared(assignedIdentifier, symbolTable)) //assigned is not declared
            return false;


        String className = symbolTable.getClassName();
        String superClassName = symbolTable.getSuper();

        if (!(assignedType.getName().equals(assigneeType.getName()) || (superClassName != null
                && (assignedType.getName().equals(superClassName) && assigneeType.getName().equals(className)))
                || (isImported(assignedType.getName(), symbolTable) && isImported(assigneeType.getName(), symbolTable)))) {
            this.addReport(assignmentStatement, "Type of the assignee must be compatible with the assigned.");
            return false;
        }

        return true;
    }

    private Boolean visitCondition(JmmNode conditionExp, SymbolTableBuilder symbolTable) {
        Type conditionExpType = getType(conditionExp.getJmmChild(0), symbolTable);

        System.out.println(conditionExpType.getName());

        if (conditionExpType == null)
            return true;

        if (conditionExp.getKind().equals(IDENTIFIER_LITERAL.toString()) && !isIdentifierDeclared(conditionExp, symbolTable)) //operand is not declared
            return false;    

        if (!conditionExpType.getName().equals("boolean")) {
            this.addReport(conditionExp, "Expression in a condition must return a boolean.");
            return false;
        }

        return true;
    }
}