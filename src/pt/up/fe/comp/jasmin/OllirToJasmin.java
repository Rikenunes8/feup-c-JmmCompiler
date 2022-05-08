package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final List<Report> reports;

    private final FunctionClassMap<Instruction, String> instructionMap;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.reports = new ArrayList<>();

        this.instructionMap = new FunctionClassMap<>();

        this.instructionMap.put(AssignInstruction.class, this::getJasminCode);
        this.instructionMap.put(CallInstruction.class, this::getJasminCode);
        this.instructionMap.put(GetFieldInstruction.class, this::getJasminCode);
        this.instructionMap.put(OpInstruction.class, this::getJasminCode);
        this.instructionMap.put(SingleOpInstruction.class, this::getJasminCode);
        this.instructionMap.put(CondBranchInstruction.class, this::getJasminCode);
        this.instructionMap.put(GotoInstruction.class, this::getJasminCode);
        this.instructionMap.put(ReturnInstruction.class, this::getJasminCode);
    }

    public List<Report> getReports() {
        return this.reports;
    }

    public String getJasminCode() {
        StringBuilder jasminCode = new StringBuilder();

        String extendedClass = (this.classUnit.getSuperClass() == null) ?
                "java/lang/Object" : this.getFullyQualifiedClassName(this.classUnit.getSuperClass());

        jasminCode.append(".class public ").append(this.classUnit.getClassName()).append("\n");
        jasminCode.append(".super ").append(extendedClass).append("\n");

        if (!this.classUnit.getFields().isEmpty()) jasminCode.append("\n");
        for (Field field : this.classUnit.getFields()) {
            jasminCode.append(this.getJasminCode(field));
        }

        jasminCode.append(this.getJasminConstructorCode(extendedClass));

        for (Method method : this.classUnit.getMethods()) {
            if (!method.isConstructMethod()) {
                jasminCode.append(this.getJasminCode(method));
            }
        }

        return jasminCode.toString();
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

    public String getJasminCode(Field field) {
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

    public String getJasminCode(Method method) {
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
            code.append(this.getJasminCode(instruction));
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


    // construct jasmin code for:
    // 1. function calls*
    // 2. arithmetic expression
    // 3. if the else commands
    // 4. assignments*
    // 5. command sequences*

    public String getJasminCode(Instruction instruction) {
        return this.instructionMap.apply(instruction);
    }

    public String getJasminCode(AssignInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(CallInstruction instruction) {
        return "";
    }

    public String getJasminCode(GetFieldInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(OpInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(SingleOpInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(CondBranchInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(GotoInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(ReturnInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    private String getJasminType(Type type) {
        if (type instanceof ArrayType) {
            return "[" + this.getJasminType(((ArrayType) type).getTypeOfElements());
        }

        if (type instanceof ClassType) {
            String className = ((ClassType) type).getName();
            return "L" + this.getFullyQualifiedClassName(className) + ";";
        }

        return this.getJasminType(type.getTypeOfElement());
    }

    private String getJasminType(ElementType type) {
        switch (type) {
            case VOID:
                return "V";
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            default:
                throw new NotImplementedException(type);
        }
    }
}
