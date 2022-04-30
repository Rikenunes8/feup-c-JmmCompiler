package pt.up.fe.comp;

import pt.up.fe.comp.analysis.JmmAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    static public Type buildType(String typeSignature) {
        if (typeSignature.equals("int[]")) {
            return new Type("int", true);
        } else if (typeSignature.equals("String[]")) {
            return new Type("String", true);
        } else {
            return new Type(typeSignature, false);
        }
    }

    static public Type getType(JmmNode var, SymbolTable symbolTable) {

        if (var.getKind().matches("TrueLiteral|FalseLiteral|AndExp|NotExp|LessExp|Condition"))
            return new Type("boolean", false);

        if (var.getKind().matches("IntegerLiteral|AddExp|SubExp|MultExp|DivExp|ArrayAccess|ArrayAccessExp"))
            return new Type("int", false);

        if (var.getKind().equals("NewIntArray"))
            return new Type("int", true);

        if (var.getKind().equals("NewObject"))
            return new Type(var.get("name"), true);

        String methodSignature = "";
        if (var.getAncestor("MethodDeclaration").isPresent()) {
            methodSignature = var.getAncestor("MethodDeclaration").get().get("name");
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

    static public Boolean isIdentifierDeclared(JmmNode identifier, SymbolTable symbolTable) {
        String methodSignature = "";
        if (identifier.getAncestor("MethodDeclaration").isPresent()) {
            methodSignature = identifier.getAncestor("MethodDeclaration").get().get("name");
        }

        if (!methodSignature.isEmpty()) {

            if (symbolTable.getLocalVariables(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a local variable
                return true;

            if (symbolTable.getParameters(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a method parameter
                return true;
        }

        if (symbolTable.getFields().stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a field of the class
            return true;

        if (identifier.getJmmParent().getKind().equals("DotExp")) //it is a function call (not checked here)
            return true;

        return false;
    }
}
