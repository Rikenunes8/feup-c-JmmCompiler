package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private HashMap<String, Descriptor> methodVarTable;

    private int stackCounter;
    private int labelCounter;

    private final FunctionClassMap<Instruction, String> instructionMap;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;

        this.methodVarTable = new HashMap<>();
        this.labelCounter = 0;

        this.instructionMap = new FunctionClassMap<>();
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

    public void setMethodVarTable(HashMap<String, Descriptor> methodVarTable) {
        this.methodVarTable = methodVarTable;
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
                this.stackCounter = 0;

                this.methodVarTable = method.getVarTable();
                // System.out.println(methodVarTable);
                jasminCode.append(this.getJasminCode(method));
            }
        }

        return jasminCode.toString();
    }

    private String getFullyQualifiedClassName(String className) {
        if (className.equals(this.classUnit.getClassName())) {
            return className;
        }

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
            code.append(this.getJasminCode(instruction, method.getLabels()));
        }

        // This may be improved in optimization STAGE by always adding an empty return instruction
        // in the AST before parsing to ollir as a last child when a method is void
        if (method.getReturnType().getTypeOfElement() == ElementType.VOID) {
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


    // construct jasmin code for:
    // 1. function calls
    // 2. arithmetic expression
    // 3. assignments
    // 4. conditional instructions (if and if-else)

    public String getJasminCode(Instruction instruction, HashMap<String, Instruction> methodLabels) {
        String instructionLabels = methodLabels.entrySet().stream()
                .filter(entry -> entry.getValue().equals(instruction))
                .map(entry -> "\t" + entry.getKey() + ":\n")
                .collect(Collectors.joining());

        return instructionLabels + this.instructionMap.apply(instruction);
    }

    public String getJasminCode(AssignInstruction instruction)  {
        StringBuilder code = new StringBuilder();

        if (instruction.getDest() instanceof ArrayOperand) {
            ArrayOperand arrayOperand = (ArrayOperand) instruction.getDest();
            // Load array
            code.append("\taload").append(this.getVariableVirtualRegister(arrayOperand.getName(), this.methodVarTable)).append("\n");
            // Load index
            code.append(loadElementCode(arrayOperand.getIndexOperands().get(0), this.methodVarTable));
        }

        code.append(getJasminCode(instruction.getRhs(), new HashMap<>()));

        // In case that on the right side of the assignment there is a call instruction for a new object - do not store yet
        if (!(instruction.getRhs() instanceof CallInstruction && instruction.getDest().getType().getTypeOfElement().equals(ElementType.OBJECTREF))) {
            code.append(this.storeElementCode((Operand) instruction.getDest(), this.methodVarTable));
        }

        return code.toString();
    }

    public String getJasminCode(CallInstruction instruction) {
        StringBuilder code = new StringBuilder();

        switch (instruction.getInvocationType()) {
            case invokestatic:
            case invokevirtual:
            case invokespecial:
                String methodClass = instruction.getFirstArg().getType().getTypeOfElement() == ElementType.CLASS
                        ? this.getFullyQualifiedClassName(((Operand) instruction.getFirstArg()).getName())
                        : this.getFullyQualifiedClassName(((ClassType) instruction.getFirstArg().getType()).getName());
                String methodCall = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");

                code.append("\t").append(instruction.getInvocationType().toString()).append(" ")
                        .append(methodClass).append("/").append(methodCall).append("(")
                        .append(instruction.getListOfOperands().stream()
                                .map(operand -> this.getJasminType(operand.getType()))
                                .collect(Collectors.joining()))
                        .append(")").append(this.getJasminType(instruction.getReturnType())).append("\n");

                return code.toString();
            case arraylength:
                return code.append(this.loadElementCode(instruction.getFirstArg(), this.methodVarTable))
                        .append("\tarraylength\n").toString();
            case NEW:
                if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.ARRAYREF) {
                    return code.append(this.loadElementCode(instruction.getListOfOperands().get(0), this.methodVarTable))
                            .append("\tnewarray int\n").toString();
                }
                if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.OBJECTREF) {
                    String qualifiedClassName = this.getFullyQualifiedClassName(((Operand) instruction.getFirstArg()).getName());
                    return code.append("\tnew ").append(qualifiedClassName).append("\n")
                            .append("\tdup\n").toString();
                }
                throw new RuntimeException("A new function call must reference an array or object");
            case ldc: // TODO
                System.out.println("---------");
                System.out.println(instruction);
                System.out.println("---------");
                return "";
            default:
                throw new NotImplementedException(instruction.getInvocationType());
        }
    }

    public String getJasminCode(GetFieldInstruction instruction) {
        StringBuilder code = new StringBuilder();

        code.append(this.loadElementCode(instruction.getFirstOperand(), this.methodVarTable));
        code.append("\tgetfield ").append(this.classUnit.getClassName()).append("/")
                .append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(this.getJasminType(instruction.getSecondOperand().getType())).append("\n");

        return code.toString();
    }

    public String getJasminCode(PutFieldInstruction instruction) {
        StringBuilder code = new StringBuilder();

        code.append(this.loadElementCode(instruction.getFirstOperand(), this.methodVarTable));
        code.append(this.loadElementCode(instruction.getThirdOperand(), this.methodVarTable));
        code.append("\tputfield ").append(this.classUnit.getClassName()).append("/")
                .append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(this.getJasminType(instruction.getSecondOperand().getType())).append("\n");

        return code.toString();
    }

    public String getJasminCode(BinaryOpInstruction instruction) {
        OperationType opType = instruction.getOperation().getOpType();

        if (Arrays.asList(OperationType.ADD, OperationType.SUB, OperationType.MUL, OperationType.DIV).contains(opType))
            return this.getBinaryIntOperationCode(instruction);
        else if (Arrays.asList(OperationType.EQ, OperationType.GTE, OperationType.GTH, OperationType.LTE,
                OperationType.LTH, OperationType.NEQ, OperationType.ANDB, OperationType.NOTB).contains(opType))
            return this.getBinaryBooleanOperationCode(instruction);

        throw new NotImplementedException(instruction.getOperation().getOpType());
    }

    private String getBinaryIntOperationCode(BinaryOpInstruction instruction) {
        StringBuilder code = new StringBuilder();

        code.append(this.loadElementCode(instruction.getLeftOperand(), this.methodVarTable));
        code.append(this.loadElementCode(instruction.getRightOperand(), this.methodVarTable));

        switch (instruction.getOperation().getOpType()) {
            case ADD:
                return code.append("\tiadd\n").toString();
            case SUB:
                return code.append("\tisub\n").toString();
            case MUL:
                return code.append("\timul\n").toString();
            case DIV:
                return code.append("\tidiv\n").toString();
            default:
                throw new NotImplementedException(instruction.getOperation().getOpType());
        }
    }

    private String getBinaryBooleanOperationCode(BinaryOpInstruction instruction) {
        StringBuilder code = new StringBuilder();

        String trueLabel = nextLabel();
        String endIfLabel = nextLabel();

        switch (instruction.getOperation().getOpType()) {
            case EQ:
            case GTE:
            case GTH:
            case LTE:
            case LTH:
            case NEQ:
                code.append(this.loadElementCode(instruction.getLeftOperand(), this.methodVarTable));
                code.append(this.loadElementCode(instruction.getRightOperand(), this.methodVarTable));

                String compInst = this.getComparisonInstructionCode(instruction.getOperation().getOpType());
                code.append("\t").append(compInst).append(" ").append(trueLabel).append("\n")
                        .append(this.getBinaryBooleanJumpsCode(trueLabel, endIfLabel));
                break;
            case ANDB:
                code.append(this.loadElementCode(instruction.getLeftOperand(), this.methodVarTable))
                        .append("\tifeq ").append(trueLabel).append("\n");
                code.append(this.loadElementCode(instruction.getRightOperand(), this.methodVarTable))
                        .append("\tifeq ").append(trueLabel).append("\n");

                code.append(this.getBinaryBooleanJumpsCode(trueLabel, endIfLabel));
                break;
            case NOTB:
                code.append(this.loadElementCode(instruction.getLeftOperand(), this.methodVarTable))
                        .append("\tifeq ").append(trueLabel).append("\n");

                code.append(this.getBinaryBooleanJumpsCode(trueLabel, endIfLabel));
                break;
            default:
                throw new NotImplementedException(instruction.getOperation().getOpType());
        }

        return code.toString();
    }

    private String getComparisonInstructionCode(OperationType operationType) {
        switch (operationType) {
            case EQ: return "if_icmpeq";
            case GTE: return "if_icmpge";
            case GTH: return "if_icmpgt";
            case LTE: return "if_icmple";
            case LTH: return "if_icmplt";
            case NEQ: return "if_icmpne";
            default: throw new NotImplementedException(operationType);
        }
    }

    private String getBinaryBooleanJumpsCode(String trueLabel, String endIfLabel) {

        return "\ticonst_1\n" +
                "\tgoto " + endIfLabel + "\n" +
                "\t" + trueLabel + ":\n" +
                "\ticonst_0\n" +
                "\t" + endIfLabel + ":\n";
    }

    public String getJasminCode(UnaryOpInstruction instruction) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(SingleOpInstruction instruction) {
        return this.loadElementCode(instruction.getSingleOperand(), this.methodVarTable);
    }

    public String getJasminCode(CondBranchInstruction instruction) {
        StringBuilder code = new StringBuilder();

        code.append(this.getJasminCode(instruction.getCondition(), new HashMap<>()));
        code.append("\tifeq ").append(instruction.getLabel()).append("\n");

        return code.toString();
    }

    public String getJasminCode(GotoInstruction instruction) {
        return "\tgoto " + instruction.getLabel() + "\n";
    }

    public String getJasminCode(ReturnInstruction instruction) {
        if (!instruction.hasReturnValue()) {
            return "\treturn\n";
        }

        switch (instruction.getOperand().getType().getTypeOfElement()) {
            case VOID:
                return "\treturn\n";
            case INT32:
            case BOOLEAN:
                return this.loadElementCode(instruction.getOperand(), this.methodVarTable) + "\tireturn\n";
            case ARRAYREF:
            case OBJECTREF:
                return this.loadElementCode(instruction.getOperand(), this.methodVarTable) + "\tareturn\n";
            default:
                throw new NotImplementedException(instruction.getElementType());
        }
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

    private String getVariableVirtualRegister(String variableName, HashMap<String, Descriptor> methodVarTable) {
        int virtualRegister = methodVarTable.get(variableName).getVirtualReg();

        return virtualRegister > 3 ? " " + virtualRegister : "_" + virtualRegister;
    }

    private String loadElementCode(Element element, HashMap<String, Descriptor> methodVarTable) {
        if (element instanceof LiteralElement) {
            return this.getConstantElementCode(((LiteralElement) element).getLiteral()) + "\n";
        }

        if (element instanceof ArrayOperand) {
            ArrayOperand arrayOperand = (ArrayOperand) element;

            // Load array + Load index + Load value
            return "\taload" + this.getVariableVirtualRegister(arrayOperand.getName(), methodVarTable) + "\n" +
                    loadElementCode(arrayOperand.getIndexOperands().get(0), methodVarTable) +
                    "\tiaload\n";
        }

        if (element instanceof Operand) {
            Operand operand = (Operand) element;

            switch (operand.getType().getTypeOfElement()) {
                case THIS:
                    return "\taload_0\n";
                case INT32:
                case BOOLEAN:
                    return "\tiload" + this.getVariableVirtualRegister(operand.getName(), this.methodVarTable) + "\n";
                case OBJECTREF:
                case ARRAYREF:
                    return "\taload" + this.getVariableVirtualRegister(operand.getName(), this.methodVarTable) + "\n";
                default:
                    throw new RuntimeException("Exception during load elements of type operand");
            }
        }

        throw new RuntimeException("Exception during load elements");
    }

    private String getConstantElementCode(String constantLiteral) {
        int number = Integer.parseInt(constantLiteral);

        if (number >= -1 && number <= 5)
            return "\ticonst_" + constantLiteral;
        else if (number >= -128 && number <= 127)
            return "\tbipush " + constantLiteral;
        else if (number >= -32768 && number <= 32767)
            return "\tsipush " + constantLiteral;
        else
            return "\tldc " + constantLiteral;
    }

    private String storeElementCode(Operand operand, HashMap<String, Descriptor> methodVarTable) {
        if (operand instanceof ArrayOperand) {
            return "\tiastore\n";
        }

        switch (operand.getType().getTypeOfElement()) {
            case INT32:
            case BOOLEAN:
                return "\tistore" + this.getVariableVirtualRegister(operand.getName(), methodVarTable) + "\n";
            case OBJECTREF:
            case ARRAYREF:
                return "\tastore" + this.getVariableVirtualRegister(operand.getName(), methodVarTable) + "\n";
            case STRING: // TODO
                return "\tldc " + operand.getName() + "\n";
            default:
                throw new RuntimeException("Exception during store elements  type" + operand.getType().getTypeOfElement());
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

    private String nextLabel() {
        return "label" + this.labelCounter++;
    }
}
