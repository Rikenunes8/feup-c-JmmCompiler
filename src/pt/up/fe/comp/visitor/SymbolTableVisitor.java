package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

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

        List<Symbol> methodLocalVariables = new ArrayList<>();
        for (JmmNode children: publicMain.getChildren()) {
            if (children.getKind().equals("VarDeclaration")) {
                Symbol localVar = new Symbol(this.getType(children.get("type")), children.get("var"));
                methodLocalVariables.add(localVar);
            } else {
                break;
            }
        }
        jmmAnalyser.getSymbolTable().addMethodLocalVariables(methodName, methodLocalVariables);

        return true;
    }

    private Boolean visitPublicMethod(JmmNode publicMethod, JmmAnalyser jmmAnalyser) {
        String methodName = publicMethod.get("name");
        String methodType = publicMethod.get("type");
        jmmAnalyser.getSymbolTable().addMethodType(methodName, this.getType(methodType));

        List<Symbol> methodParameters = new ArrayList<>();
        List<Symbol> methodLocalVariables = new ArrayList<>();
        for (JmmNode children: publicMethod.getChildren()) {
            if (children.getKind().equals("MethodParameters")) {
                methodParameters = children.getChildren().stream()
                    .map(id -> new Symbol(this.getType(id.get("type")), id.get("var")))
                    .collect(Collectors.toList());
            } 
            else if (children.getKind().equals("VarDeclaration")) {
                Symbol localVar = new Symbol(this.getType(children.get("type")), children.get("var"));
                methodLocalVariables.add(localVar);
            }
            else {
                break;
            }
        }
        jmmAnalyser.getSymbolTable().addMethodParameters(methodName, methodParameters);
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
}

