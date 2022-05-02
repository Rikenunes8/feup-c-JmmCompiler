package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;

import static pt.up.fe.comp.ast.AstNode.*;

public class UndefinedVarsVisitor extends SemanticAnalyserVisitor {
    private Map<String, Boolean> fields;
    private Map<String, Boolean> params;
    private Map<String, Boolean> locals;

    public UndefinedVarsVisitor() {
        super();
        this.fields = new HashMap<>();
        this.params = new HashMap<>();
        this.locals = new HashMap<>();
        addVisit(CLASS_DECLARATION, this::visitClassDeclaration);
        addVisit(METHOD_DECLARATION, this::visitMethodDeclaration);
        addVisit(ASSIGNMENT_STATEMENT, this::visitAssignmentStatement);
        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
    }

    private Boolean visitClassDeclaration(JmmNode jmmNode, SymbolTableBuilder symbolTableBuilder) {
        for (Symbol field : symbolTableBuilder.getFields()) {
            this.fields.put(field.getName(), false);
        }
        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode jmmNode, SymbolTableBuilder symbolTableBuilder) {
        this.params = new HashMap<>();
        this.locals = new HashMap<>();
        for (Symbol var : symbolTableBuilder.getParameters(jmmNode.get("name"))) {
            this.params.put(var.getName(), true);
        }
        for (Symbol var : symbolTableBuilder.getLocalVariables(jmmNode.get("name"))) {
            this.locals.put(var.getName(), false);
        }
        return true;
    }

    private Boolean visitAssignmentStatement(JmmNode jmmNode, SymbolTableBuilder symbolTable) {
        JmmNode assigned = jmmNode.getJmmChild(0);
        JmmNode assignee = jmmNode.getJmmChild(1);
        evaluateUndefinedVarsInExpression(assignee);

        if (assigned.getKind().equals(ARRAY_ACCESS_EXP.toString())) {
            evaluateUndefinedIdentifier(assigned.getJmmChild(0));
            evaluateUndefinedVarsInExpression(assigned.getJmmChild(1));
        }
        else if (assigned.getKind().equals(IDENTIFIER_LITERAL.toString())) {
            String name = assigned.get("val");
            if (this.locals.containsKey(name))
                this.locals.replace(name, true);
            else if (this.params.containsKey(name))
                this.params.replace(name, true);
            else if (this.fields.containsKey(name))
                this.fields.replace(name, true);
        }
        return true;
    }

    private Boolean visitIdentifierLiteral(JmmNode identifier, SymbolTableBuilder symbolTableBuilder) {
        if (identifier.getAncestor(ASSIGNMENT_STATEMENT.toString()).isPresent()
                || identifier.getAncestor(METHOD_PARAMETERS.toString()).isPresent()
                || identifier.getAncestor(VAR_DECLARATION.toString()).isPresent())
            return true;
        return evaluateUndefinedIdentifier(identifier);
    }

    private Boolean evaluateUndefinedIdentifier(JmmNode identifier) {
        String name = identifier.get("val");
        if (this.locals.containsKey(name)) {
            if (!this.locals.get(name))
                addReport(identifier, "undefined var " + name);
        }
        else if (this.params.containsKey(name)) {
            if (!this.params.get(name))
                addReport(identifier, "undefined var " + name);
        }
        else if (this.fields.containsKey(name)) {
            if (!this.fields.get(name))
                addReport(identifier, "undefined var " + name);
        }
        else {
            return false;
        }
        return true;
    }


    private Boolean evaluateUndefinedVarsInExpression(JmmNode expression) {
        if (expression.getKind().equals(IDENTIFIER_LITERAL.toString())) {
            evaluateUndefinedIdentifier(expression);
        }
        else {
            for (JmmNode child : expression.getChildren()) {
                evaluateUndefinedVarsInExpression(child);
            }
        }
        return true;
    }
}
