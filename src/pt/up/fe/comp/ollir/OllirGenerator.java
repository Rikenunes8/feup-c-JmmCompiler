package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.stream.Collectors;

// TODO Change many things
public class OllirGenerator extends AJmmVisitor<Integer, Integer> {

    private final StringBuilder code;
    private final SymbolTable symbolTable;

    public OllirGenerator(SymbolTable symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;

        addVisit("Program", this::visitProgram);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("PublicMain", this::visitMainMethod);
    }

    public String getCode() {
        return this.code.toString();
    }

    private Integer visitProgram(JmmNode program, Integer dummy) {
        for (var importStr : symbolTable.getImports())  {
            code.append("import ").append(importStr).append(";\n");
        }

        for (var child : program.getChildren()) {
            visit(child);
        }

        return 0;
    }

    private Integer visitClassDeclaration(JmmNode classDeclaration, Integer dummy) {

        code.append("public ").append(symbolTable.getClassName());
        var extendedClass = symbolTable.getSuper();
        if (extendedClass != null) {
            code.append(" extends ").append(extendedClass);
        }

        code.append(" {\n");
        // JMM visitor conseguir controlar quando é que os filhos são visitados
        for (var child : classDeclaration.getChildren()) {
            visit(child);
        }
        code.append("\n}\n");

        return 0;
    }

    private Integer visitMainMethod(JmmNode methodDeclaration, Integer dummy) {

        var methodSignature = "main";
        code.append("\n.method public static ").append(methodSignature).append("(");

        var params = symbolTable.getParameters(methodSignature);
        var paramCode = params.stream().map(symbol -> OllirUtils.getCode(symbol)).collect(Collectors.joining(", "));

        code.append(paramCode);
        code.append(").");
        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodSignature)));
        code.append(" {\n");

        // aqui as coisas para visitar os correspondentes

        code.append("}\n");

        return 0;
    }
}
