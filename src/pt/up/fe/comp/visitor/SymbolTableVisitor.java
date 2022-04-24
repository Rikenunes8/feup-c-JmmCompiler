package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {
    public SymbolTableVisitor() {
        addVisit("ImportStatement", this::visitImportStatements);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("VarDeclaration", this::visitVarDeclaration);
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
        String extendedClass = classDeclaration.get("extends"); // TODO check if it works when there is no extended class
        jmmAnalyser.getSymbolTable().setClassName(className);
        jmmAnalyser.getSymbolTable().setSuper(extendedClass)
        return true;
    }

    private Boolean visitPublicMain(JmmNode publicMain, JmmAnalyser jmmAnalyser) {
        jmmAnalyser.getSymbolTable().addMethodType("main", this.getType("void"));
        Symbol parameter = new Symbol(new Type("String", true), publicMain.get("args"));
        jmmAnalyser.getSymbolTable().addMethodParameters("main", Arrays.asList(parameter));
        return true;
    }

    private Boolean visitPublicMethod(JmmNode publicMethod, JmmAnalyser jmmAnalyser) {
        List<JmmNode> methodChildren = publicMethod.getChildren();
        String methodName = publicMethod.get("name");
        String methodType = publicMethod.get("type");
        jmmAnalyser.getSymbolTable().addMethodType(methodName, this.getType(methodType));

        if (!methodChildren.isEmpty() && methodChildren.get(0).getKind().equals("MethodParameters")) {
            List<Symbol> methodParameters = methodChildren.get(0).getChildren().stream()
                    .map(id -> new Symbol(this.getType(id.get("type")), id.get("var")))
                    .collect(Collectors.toList());
            jmmAnalyser.getSymbolTable().addMethodParameters(methodName, methodParameters);
        }
        return true;
    }

    private Boolean visitVarDeclaration(JmmNode varDeclaration, JmmAnalyser jmmAnalyser) {
        Type type = this.getType(varDeclaration.get("type"));
        Symbol symbol = new Symbol(type, varDeclaration.get("var"));
        JmmNode parent = varDeclaration.getJmmParent();

        if (parent.getKind().equals("ClassDeclaration")) {
            jmmAnalyser.getSymbolTable().addField(symbol);
        } else if (parent.getKind().equals("PublicMain")) {
            jmmAnalyser.getSymbolTable().addMethodLocalVariable("main", symbol);
        } else if (parent.getKind().equals("PublicMethod")) {
            String method = parent.get("name");
            jmmAnalyser.getSymbolTable().addMethodLocalVariable(method, symbol);
        }
        
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

