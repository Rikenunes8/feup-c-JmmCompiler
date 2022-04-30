package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
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
        if (var.getAncestor("PublicMethod").isPresent()) {
            methodSignature = var.getAncestor("PublicMethod").get().get("name");
        }
        else if (var.getAncestor("PublicMain").isPresent()) {
            methodSignature = "main";
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

    static public Boolean isIdentifierDeclared(JmmNode identifier, JmmAnalyser jmmAnalyser) {
        String methodSignature = "";
        if (identifier.getAncestor("PublicMethod").isPresent()) {
            methodSignature = identifier.getAncestor("PublicMethod").get().get("name");
        }
        else if (identifier.getAncestor("PublicMain").isPresent()) {
            methodSignature = "main";
        }
        if (!methodSignature.isEmpty()) {

            if (jmmAnalyser.getSymbolTable().getLocalVariables(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a local variable
                return true;

            if (jmmAnalyser.getSymbolTable().getParameters(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a method parameter
                return true;
        }

        if (jmmAnalyser.getSymbolTable().getFields().stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a field of the class
            return true;

        if (identifier.getJmmParent().getKind().equals("DotExp")) //it is a function call (not checked here)
            return true;

        return false;
    }
}
