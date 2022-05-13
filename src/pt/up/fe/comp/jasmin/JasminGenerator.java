package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.classmap.BiFunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.HashMap;
import java.util.stream.Collectors;

public class JasminGenerator {

    private final ClassUnit classUnit;
    private final JasminInstrBinaryOpGenerator instrBinaryOpGenerator;

    // private int stackCounter;

    private final BiFunctionClassMap<Instruction, HashMap<String, Descriptor>, String> instructionMap;

    public JasminGenerator(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.instrBinaryOpGenerator = new JasminInstrBinaryOpGenerator();

        // this.labelCounter = 0;

        this.instructionMap = new BiFunctionClassMap<>();
        this.instructionMap.put(AssignInstruction.class, this::getJasminCode);
        this.instructionMap.put(CallInstruction.class, this::getJasminCode);
        this.instructionMap.put(GetFieldInstruction.class, this::getJasminCode);
        this.instructionMap.put(PutFieldInstruction.class, this::getJasminCode);
        this.instructionMap.put(BinaryOpInstruction.class, this::getJasminCode);
        this.instructionMap.put(UnaryOpInstruction.class, this::getJasminCode);
        this.instructionMap.put(SingleOpInstruction.class, this::getJasminCode);
        this.instructionMap.put(CondBranchInstruction.class, this::getJasminCode);
        this.instructionMap.put(GotoInstruction.class, this::getJasminCode);
        this.instructionMap.put(ReturnInstruction.class, this::getJasminCode);
    }

    public String getJasminCode() {
        StringBuilder jasminCode = new StringBuilder();

        String extendedClass = (this.classUnit.getSuperClass() == null) ?
                "java/lang/Object" : JasminUtils.getFullyQualifiedClassName(this.classUnit, this.classUnit.getSuperClass());

        jasminCode.append(".class public ").append(this.classUnit.getClassName()).append("\n");
        jasminCode.append(".super ").append(extendedClass).append("\n");

        if (!this.classUnit.getFields().isEmpty()) jasminCode.append("\n");
        for (Field field : this.classUnit.getFields()) {
            jasminCode.append(this.getJasminCode(field));
        }

        jasminCode.append(this.getJasminConstructorCode(extendedClass));

        for (Method method : this.classUnit.getMethods()) {
            if (!method.isConstructMethod()) {
                // this.stackCounter = 0;
                jasminCode.append(this.getJasminCode(method));
            }
        }

        return jasminCode.toString();
    }

    public String getJasminCode(Field field) {
        StringBuilder code = new StringBuilder();

        String accessAnnotation = field.getFieldAccessModifier() == AccessModifiers.DEFAULT ?
                "" : field.getFieldAccessModifier().name().toLowerCase() + " ";
        String staticAnnotation = field.isStaticField() ? "static " : "";
        String finalAnnotation  = field.isFinalField() ? "final " : "";

        code.append(".field ").append(accessAnnotation).append(staticAnnotation).append(finalAnnotation)
                .append(field.getFieldName()).append(" ").append(JasminUtils.getJasminType(this.classUnit, field.getFieldType())).append("\n");

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
                        .map(parameter -> JasminUtils.getJasminType(this.classUnit, parameter.getType()))
                        .collect(Collectors.joining()))
                .append(")").append(JasminUtils.getJasminType(this.classUnit, method.getReturnType())).append("\n");

        // Method Limits
        code.append(this.getMethodLimitsCode(method));

        // Method Instructions
        for (Instruction instruction : method.getInstructions()) {
            code.append(this.getJasminCode(instruction, method.getLabels(), method.getVarTable()));
        }

        // To guarantee that there is always a return statement before the .end method instruction
        if (method.getReturnType().getTypeOfElement() == ElementType.VOID
                && method.getInstructions().stream().noneMatch(instruction -> instruction.getInstType() == InstructionType.RETURN)) {
            code.append("\treturn\n");
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

    public String getJasminCode(Instruction instruction, HashMap<String, Instruction> methodLabels, HashMap<String, Descriptor> methodVarTable) {
        String instructionLabels = methodLabels.entrySet().stream()
                .filter(entry -> entry.getValue().equals(instruction))
                .map(entry -> "\t" + entry.getKey() + ":\n")
                .collect(Collectors.joining());

        return instructionLabels + this.instructionMap.apply(instruction, methodVarTable);
    }

    public String getJasminCode(AssignInstruction instruction, HashMap<String, Descriptor> varTable)  {
        StringBuilder code = new StringBuilder();

        if (instruction.getDest() instanceof ArrayOperand) { // Load Array + Load Index
            ArrayOperand arrayOperand = (ArrayOperand) instruction.getDest();
            code.append("\taload").append(JasminUtils.getVariableVirtualRegister(arrayOperand.getName(), varTable)).append("\n");
            code.append(JasminUtils.loadElementCode(arrayOperand.getIndexOperands().get(0), varTable));
        }

        code.append(getJasminCode(instruction.getRhs(), new HashMap<>(), varTable));

        // In case that on the right side of the assignment there is a call instruction for a new object - do not store yet
        if (!(instruction.getRhs() instanceof CallInstruction
                && instruction.getDest().getType().getTypeOfElement().equals(ElementType.OBJECTREF))) {
            code.append(JasminUtils.storeElementCode((Operand) instruction.getDest(), varTable));
        }

        return code.toString();
    }

    public String getJasminCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        JasminInstrCallGenerator callInstrGenerator = new JasminInstrCallGenerator(this.classUnit, instruction, varTable);
        return callInstrGenerator.getJasminCode();
    }

    public String getJasminCode(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        String classField = ((Operand) instruction.getFirstOperand()).getName();
        String className = (classField.equals("this"))
                ? this.classUnit.getClassName()
                : JasminUtils.getFullyQualifiedClassName(this.classUnit, classField);

        code.append(JasminUtils.loadElementCode(instruction.getFirstOperand(), varTable));
        code.append("\tgetfield ").append(className).append("/")
                .append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(JasminUtils.getJasminType(this.classUnit, instruction.getSecondOperand().getType())).append("\n");
        System.out.println(code.toString());

        return code.toString();
    }

    public String getJasminCode(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        String classField = ((Operand) instruction.getFirstOperand()).getName();
        String className = (classField.equals("this"))
                ? this.classUnit.getClassName()
                : JasminUtils.getFullyQualifiedClassName(this.classUnit, classField);

        code.append(JasminUtils.loadElementCode(instruction.getFirstOperand(), varTable));
        code.append(JasminUtils.loadElementCode(instruction.getThirdOperand(), varTable));
        code.append("\tputfield ").append(className).append("/")
                .append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(JasminUtils.getJasminType(this.classUnit, instruction.getSecondOperand().getType())).append("\n");
        System.out.println(code.toString());
        return code.toString();
    }

    public String getJasminCode(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        instrBinaryOpGenerator.setInstruction(instruction);
        instrBinaryOpGenerator.setVarTable(varTable);
        return instrBinaryOpGenerator.getJasminCode();
    }

    public String getJasminCode(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        return JasminUtils.loadElementCode(instruction.getSingleOperand(), varTable);
    }

    // TODO CHECK
    public String getJasminCode(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        code.append(this.getJasminCode(instruction.getCondition(), new HashMap<>(), varTable));
        code.append("\tifeq ").append(instruction.getLabel()).append("\n");

        return code.toString();
    }

    public String getJasminCode(GotoInstruction instruction, HashMap<String, Descriptor> varTable) {
        return "\tgoto " + instruction.getLabel() + "\n";
    }

    public String getJasminCode(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        if (!instruction.hasReturnValue()) {
            return "\treturn\n";
        }

        switch (instruction.getOperand().getType().getTypeOfElement()) {
            case VOID:
                return "\treturn\n";
            case INT32:
            case BOOLEAN:
                return JasminUtils.loadElementCode(instruction.getOperand(), varTable) + "\tireturn\n";
            case ARRAYREF:
            case OBJECTREF:
                return JasminUtils.loadElementCode(instruction.getOperand(), varTable) + "\tareturn\n";
            default:
                throw new NotImplementedException(instruction.getElementType());
        }
    }

    /*
    private void incrementStackCounter(int value) {
        this.stackCounter += value;
    }

    private void decrementStackCounter(int value) {
        this.stackCounter -= value;
    }
    */
}
