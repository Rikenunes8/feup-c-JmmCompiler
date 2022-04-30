package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final List<Report> reports = new ArrayList<>();

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    // construct jasmin code for:
    // 1. function calls*
    // 2. arithmetic expression
    // 3. if the else commands
    // 4. assignments*
    // 5. command sequences*

    public String getJasminCode() {
        StringBuilder jasminCode = new StringBuilder();

        jasminCode.append(this.getInitClassCode());

        for (Method method : this.classUnit.getMethods()) {
            jasminCode.append(this.getMethodCode(method));
        }

        return jasminCode.toString();
    }

    private String getInitClassCode() {
        StringBuilder code = new StringBuilder();

        String extendedClass = (this.classUnit.getSuperClass() == null) ?
                "java/lang/Object" : getFullyExtendedClassName(this.classUnit.getSuperClass());

        code.append(".class public ").append(this.classUnit.getClassName()).append("\n");
        code.append(".super ").append(extendedClass).append("/\n");

        for (Field field : this.classUnit.getFields()) {
            code.append(this.getFieldCode(field));
        }

        code.append(jasminConstructor(extendedClass));

        return code.toString();

    }

    public String getFullyExtendedClassName(String className) { // TODO CHANGE!!
        var extendImport = classUnit.getImports().stream()
                .filter(importStr -> importStr.substring(importStr.lastIndexOf('.') + 1).equals(className))
                .collect(Collectors.toList());

        if (extendImport.isEmpty()) {
            throw new RuntimeException("Could not find import for class " + className); // TODO não devia ser mandado nos reports??
        }

        if (extendImport.size() > 1) {
            throw new RuntimeException("Too many matches for possible imports for class " + className);
        }

        return extendImport.get(0).replace('.', '/');
    }

    private String jasminConstructor(String extendedClass) {
        return  "\n.method public <init>()V\n" +
                "    aload_0\n" +
                "    invokenonvirtual " + extendedClass + "/<init>()V\n" +
                "    return\n" +
                ".end method\n";
    }

    public String getFieldCode(Field field) {
        return "";
    }

    public String getMethodCode(Method method) {
        return "";
    }
}
