package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;
import java.util.stream.Collectors;

import static pt.up.fe.comp.analysis.AnalysisUtils.buildType;

public class SymbolTableVisitor extends SemanticAnalyserVisitor {
    public SymbolTableVisitor() {
        super();
        addVisit("ImportStatement", this::visitImportStatements);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
    }

    private Boolean visitImportStatements(JmmNode importStatement, SymbolTableBuilder symbolTable) {
        String importString = importStatement.getChildren().stream()
                .map(id -> id.get("val"))
                .collect(Collectors.joining("."));
        symbolTable.addImport(importString);
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode classDeclaration, SymbolTableBuilder symbolTable) {
        String className = classDeclaration.get("name");
        symbolTable.setClassName(className);
        classDeclaration.getOptional("extends").ifPresent(symbolTable::setSuper);

        for (JmmNode children: classDeclaration.getChildren()) {
            if (children.getKind().equals("VarDeclaration")) {
                Symbol field = new Symbol(buildType(children.get("type")), children.get("var"));
                List<String> fieldsNames = symbolTable.getFields().stream()
                        .map(Symbol::getName).collect(Collectors.toList());
                if (fieldsNames.contains(field.getName()))
                    this.addReport(children, "Duplicated field declaration");
                else
                    symbolTable.addField(field);
            } else {
                break;
            }
        }

        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode methodDecl, SymbolTableBuilder symbolTable) {
        String methodName = methodDecl.get("name");
        String methodType = methodDecl.get("type");

        if (symbolTable.hasMethod(methodName)) {
            this.addReport(methodDecl, "Duplicated method "+methodName);
            return false;
        }

        List<Symbol> methodParameters = new ArrayList<>();
        if (!methodDecl.getChildren().isEmpty() && methodDecl.getJmmChild(0).getKind().equals("MethodParameters")) {
            methodParameters = methodDecl.getChildren().get(0).getChildren().stream()
                    .map(id -> new Symbol(buildType(id.get("type")), id.get("var")))
                    .collect(Collectors.toList());
        }

        // Check duplicated parameters
        Set<String> setMethodParameters = methodParameters.stream().map(Symbol::getName).collect(Collectors.toSet());
        if (methodParameters.size() != setMethodParameters.size()) {
            this.addReport(methodDecl, "Parameters with the same name");
        }


        List<Symbol> methodLocalVariables = methodDecl.getChildren().stream()
                .filter(children -> children.getKind().equals("VarDeclaration"))
                .map(id -> new Symbol(buildType(id.get("type")), id.get("var")))
                .collect(Collectors.toList());

        // Check duplicated local variables
        Set<String> setMethodLocalVariables = methodLocalVariables.stream().map(Symbol::getName).collect(Collectors.toSet());
        if (methodLocalVariables.size() != setMethodLocalVariables.size())
            this.addReport(methodDecl, "Duplicated local variables");

        symbolTable.addMethod(methodName, buildType(methodType), methodParameters, methodLocalVariables, methodDecl.get("static").equals("true"));
        return true;
    }
}
