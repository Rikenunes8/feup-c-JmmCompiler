package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import static pt.up.fe.comp.Utils.getType;
import static pt.up.fe.comp.Utils.isIdentifierDeclared;


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
        addVisit("ArrayAccess", this::visitArrayAccessExpression);
        addVisit("AssignmentStatement", this::visitAssignmentStatement);
        addVisit("IfStatement", this::visitIfStatement);
    }
    
    private Boolean visitIdentifier(JmmNode identifier, SymbolTableBuilder symbolTable) {
        if (isIdentifierDeclared(identifier, symbolTable))
            return true;

        this.addReport(identifier, "Variable used is not declared.");
        return false;
    }

    private Boolean visitArithmeticExpression(JmmNode arithmeticExpression, SymbolTableBuilder symbolTable) {
        JmmNode op1 = arithmeticExpression.getChildren().get(0);
        JmmNode op2 = arithmeticExpression.getChildren().get(1);

        if (op1.getKind().equals("DotExp") || op2.getKind().equals("DotExp")) //[TODO] verificar tipo de DotExp com this
            return true;

        Type op1Type = getType(op1, symbolTable);
        Type op2Type = getType(op2, symbolTable);

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

        if (op1.getKind().equals("DotExp") || op2.getKind().equals("DotExp")) //[TODO] verificar tipo de DotExp com this
            return true;

        Type op1Type = getType(op1, symbolTable);
        Type op2Type = getType(op2, symbolTable);  

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

        if (exp.getKind().equals("DotExp")) //[TODO] verificar tipo de DotExp com this
            return true;

        Type expType = getType(exp, symbolTable);

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

        if (op1.getKind().equals("DotExp") || op2.getKind().equals("DotExp")) //[TODO] verificar tipo de DotExp com this
            return true;

        Type op1Type = getType(op1, symbolTable);
        Type op2Type = getType(op2, symbolTable);

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

    private Boolean visitAssignmentStatement(JmmNode assignmentStatement, SymbolTableBuilder symbolTable) {
        JmmNode assigned = assignmentStatement.getChildren().get(0);
        JmmNode assignee = assignmentStatement.getChildren().get(1);

        if (assignee.getKind().equals("DotExp")) //[TODO] verificar tipo de DotExp com this
            return true;

        Type assignedType = getType(assigned, symbolTable);
        Type assigneeType = getType(assignee, symbolTable);

        JmmNode assignedIdentifier = assigned.getKind().equals("ArrayAccess") ? assigned.getChildren().get(0) : assigned;

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

        if (conditionExp.getKind().equals("DotExp")) //[TODO] verificar tipo de DotExp com this
            return true;

        Type conditionExpType = getType(conditionExp, symbolTable);

        if (conditionExp.getKind().equals("IdentifierLiteral") && !isIdentifierDeclared(conditionExp, symbolTable)) //operand is not declared
            return false;    

        if (!conditionExp.getKind().equals("TrueLiteral") && !conditionExp.getKind().equals("FalseLiteral") && !conditionExpType.getName().equals("boolean")) {
            this.addReport(conditionExp, "Expression in a condition must return a boolean.");
            return false;
        }
        
        return true;
    }
}