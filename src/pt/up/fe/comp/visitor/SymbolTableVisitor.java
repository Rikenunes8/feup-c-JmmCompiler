package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;
import java.util.stream.Collectors;

import static pt.up.fe.comp.visitor.Utils.buildType;

public class SymbolTableVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {
    public SymbolTableVisitor() {
        addVisit("ImportStatement", this::visitImportStatements);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("PublicMain", this::visitPublicMain);
        addVisit("PublicMethod", this::visitPublicMethod);
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

    private Boolean visitPublicMain(JmmNode publicMain, JmmAnalyser jmmAnalyser) {
        String methodName = "main";
        jmmAnalyser.getSymbolTable().addMethodType(methodName, buildType("void"));
        Symbol parameter = new Symbol(new Type("String", true), publicMain.get("args"));
        jmmAnalyser.getSymbolTable().addMethodParameters(methodName, Arrays.asList(parameter));

        List<Symbol> methodLocalVariables = publicMain.getChildren().stream()
            .filter(children -> children.getKind().equals("VarDeclaration"))
            .map(id -> new Symbol(buildType(id.get("type")), id.get("var")))
            .collect(Collectors.toList());
        jmmAnalyser.getSymbolTable().addMethodLocalVariables(methodName, methodLocalVariables);

        // Check duplicated local variables
        Set<String> setMethodLocalVariables = methodLocalVariables.stream().map(Symbol::getName).collect(Collectors.toSet());
        if (methodLocalVariables.size() != setMethodLocalVariables.size())
            jmmAnalyser.addReport(publicMain, "Duplicated local variables");

        return true;
    }

    private Boolean visitPublicMethod(JmmNode publicMethod, JmmAnalyser jmmAnalyser) {
        String methodName = publicMethod.get("name");
        String methodType = publicMethod.get("type");
        jmmAnalyser.getSymbolTable().addMethodType(methodName, buildType(methodType));

        List<Symbol> methodParameters = new ArrayList<>();
        if (!publicMethod.getChildren().isEmpty() && publicMethod.getJmmChild(0).getKind().equals("MethodParameters")) {
            methodParameters = publicMethod.getChildren().get(0).getChildren().stream()
                    .map(id -> new Symbol(buildType(id.get("type")), id.get("var")))
                    .collect(Collectors.toList());
        }
        jmmAnalyser.getSymbolTable().addMethodParameters(methodName, methodParameters);

        // Check duplicated parameters
        Set<String> setMethodParameters = methodParameters.stream().map(Symbol::getName).collect(Collectors.toSet());
        if (methodParameters.size() != setMethodParameters.size()) {
            jmmAnalyser.addReport(publicMethod, "Parameters with the same name");
        }


        List<Symbol> methodLocalVariables = publicMethod.getChildren().stream()
            .filter(children -> children.getKind().equals("VarDeclaration"))
            .map(id -> new Symbol(buildType(id.get("type")), id.get("var")))
            .collect(Collectors.toList());
        jmmAnalyser.getSymbolTable().addMethodLocalVariables(methodName, methodLocalVariables);

        // Check duplicated local variables
        Set<String> setMethodLocalVariables = methodLocalVariables.stream().map(Symbol::getName).collect(Collectors.toSet());
        if (methodLocalVariables.size() != setMethodLocalVariables.size())
            jmmAnalyser.addReport(publicMethod, "Duplicated local variables");

        return true;
    }
}

