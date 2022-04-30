package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.JmmAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;
import java.util.stream.Collectors;

import static pt.up.fe.comp.Utils.buildType;

public class SymbolTableVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {
    public SymbolTableVisitor() {
        addVisit("ImportStatement", this::visitImportStatements);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
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
        jmmAnalyser.getSymbolTable().setClassName(className);
        classDeclaration.getOptional("extends").ifPresent(superClass -> jmmAnalyser.getSymbolTable().setSuper(superClass));

        for (JmmNode children: classDeclaration.getChildren()) {
            if (children.getKind().equals("VarDeclaration")) {
                Symbol field = new Symbol(buildType(children.get("type")), children.get("var"));
                List<String> fieldsNames = jmmAnalyser.getSymbolTable().getFields().stream()
                        .map(Symbol::getName).collect(Collectors.toList());
                if (fieldsNames.contains(field.getName()))
                    jmmAnalyser.addReport(children, "Duplicated field declaration");
                else
                    jmmAnalyser.getSymbolTable().addField(field);
            } else {
                break;
            }
        }

        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode methodDecl, JmmAnalyser jmmAnalyser) {
        String methodName = methodDecl.get("name");
        String methodType = methodDecl.get("type");

        if (jmmAnalyser.getSymbolTable().hasMethod(methodName)) {
            jmmAnalyser.addReport(methodDecl, "Duplicated method "+methodName);
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
            jmmAnalyser.addReport(methodDecl, "Parameters with the same name");
        }


        List<Symbol> methodLocalVariables = methodDecl.getChildren().stream()
                .filter(children -> children.getKind().equals("VarDeclaration"))
                .map(id -> new Symbol(buildType(id.get("type")), id.get("var")))
                .collect(Collectors.toList());

        // Check duplicated local variables
        Set<String> setMethodLocalVariables = methodLocalVariables.stream().map(Symbol::getName).collect(Collectors.toSet());
        if (methodLocalVariables.size() != setMethodLocalVariables.size())
            jmmAnalyser.addReport(methodDecl, "Duplicated local variables");

        jmmAnalyser.getSymbolTable().addMethod(methodName, buildType(methodType), methodParameters, methodLocalVariables);
        return true;
    }
}
