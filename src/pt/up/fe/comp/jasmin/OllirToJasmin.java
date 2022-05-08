package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final List<Report> reports;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.reports = new ArrayList<>();
    }

    // construct jasmin code for:
    // 1. function calls*
    // 2. arithmetic expression
    // 3. if the else commands
    // 4. assignments*
    // 5. command sequences*

    public List<Report> getReports() {
        return this.reports;
    }

    public String getJasminCode() {
        StringBuilder jasminCode = new StringBuilder();

        jasminCode.append(this.getInitClassCode());

        for (Method method : this.classUnit.getMethods()) {
            if (!method.isConstructMethod()) {
                jasminCode.append(this.getMethodCode(method));
            }
        }

        return jasminCode.toString();
    }

    private String getInitClassCode() {
        StringBuilder code = new StringBuilder();

        String extendedClass = (this.classUnit.getSuperClass() == null) ?
                "java/lang/Object" : this.getFullyQualifiedClassName(this.classUnit.getSuperClass());

        code.append(".class public ").append(this.classUnit.getClassName()).append("\n");
        code.append(".super ").append(extendedClass).append("\n");

        if (!this.classUnit.getFields().isEmpty()) code.append("\n");
        for (Field field : this.classUnit.getFields()) {
            code.append(this.getFieldCode(field));
        }

        code.append(this.getJasminConstructorCode(extendedClass));

        return code.toString();
    }

    private String getFullyQualifiedClassName(String className) {
        var extendImport = classUnit.getImports().stream()
                .filter(importStr -> importStr.substring(importStr.lastIndexOf('.') + 1).equals(className))
                .collect(Collectors.toList());

        if (extendImport.size() != 1) {
            // TODO saved in the reports?
            throw new RuntimeException("There is not a exact match for the import of the class " + className);
        }

        return extendImport.get(0).replace('.', '/');
    }

    private String getFieldCode(Field field) {
        StringBuilder code = new StringBuilder();

        String accessAnnotation = field.getFieldAccessModifier() == AccessModifiers.DEFAULT ?
                "" : field.getFieldAccessModifier().name().toLowerCase() + " ";
        String staticAnnotation = field.isStaticField() ? "static " : "";
        String finalAnnotation  = field.isFinalField() ? "final " : "";

        code.append(".field ").append(accessAnnotation).append(staticAnnotation).append(finalAnnotation)
                .append(field.getFieldName()).append(" ").append(this.getJasminType(field.getFieldType())).append("\n");

        return code.toString();
    }

    private String getJasminConstructorCode(String extendedClass) {
        return  "\n.method public <init>()V\n" +
                "\taload_0\n" +
                "\tinvokenonvirtual " + extendedClass + "/<init>()V\n" +
                "\treturn\n" +
                ".end method\n";
    }

    private String getMethodCode(Method method) {
        StringBuilder code = new StringBuilder();

        // Method header
        String accessAnnotation = method.getMethodAccessModifier() == AccessModifiers.DEFAULT ?
                "" : method.getMethodAccessModifier().name().toLowerCase() + " ";
        String staticAnnotation = method.isStaticMethod() ? "static " : "";
        String finalAnnotation  = method.isFinalMethod() ? "final " : "";

        code.append("\n.method ").append(accessAnnotation).append(staticAnnotation).append(finalAnnotation)
                .append(method.getMethodName()).append("(")
                .append(method.getParams().stream()
                        .map(parameter -> this.getJasminType(parameter.getType()))
                        .collect(Collectors.joining()))
                .append(")").append(this.getJasminType(method.getReturnType())).append("\n");

        // Method Limits
        code.append(this.getMethodLimitsCode(method));

        // Method Instructions
        for (Instruction instruction : method.getInstructions()) {
            code.append(this.getMethodInstructionCode(method, instruction));
        }

        // Method End
        code.append(".end method\n");

        return code.toString();
    }

    private String getMethodLimitsCode(Method method) {
        // limit stack - max length of the stack that we need to the method
        // limit locals - max number of registers we need to use

        return "\t.limit stack " + 99 + "\n" +
                "\t.limit locals " + 99 + "\n\n";
        // NOTE: Now we can use 99, but this will be changed for checkpoint 3
    }

    private String getMethodInstructionCode(Method method, Instruction instruction) {
        return "";
    }

    private String getJasminType(Type type) {
        ElementType elementType = type.getTypeOfElement() == ElementType.ARRAYREF
                ? ((ArrayType) type).getTypeOfElements() : type.getTypeOfElement();
        String jasminType = type.getTypeOfElement() == ElementType.ARRAYREF ? "[" : "";

        // TODO consultar no site
        switch (elementType) {
            case VOID:
                return "V";
            case INT32:
                return jasminType + "I";
            case BOOLEAN:
                return jasminType + "Z";
            case STRING:
                return jasminType + "Ljava/lang/String;";
            case CLASS:
            case OBJECTREF:
                String className = ((ClassType) type).getName();
                return jasminType + "L" + this.getFullyQualifiedClassName(className) + ";";
            case THIS:
                return "THIS"; // TODO check
            default:
                // TODO add to reports
                throw new RuntimeException("There is not in jasmin match for the type " + type);
        }
    }
}
