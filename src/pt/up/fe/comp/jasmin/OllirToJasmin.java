package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.classmap.BiFunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    private int stackCounter;
    private int labelCounter;

    private final BiFunctionClassMap<Instruction, HashMap<String, Descriptor>, String> instructionMap;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;

        this.labelCounter = 0;

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
            code.append(this.getJasminCode(instruction, method.getLabels(), method.getVarTable()));
        }

        // This may be improved in optimization STAGE by always adding an empty return instruction
        // in the AST before parsing to ollir as a last child when a method is void
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


    // construct jasmin code for:
    // 1. function calls
    // 2. arithmetic expression
    // 3. assignments
    // 4. conditional instructions (if and if-else)

    public String getJasminCode(Instruction instruction, HashMap<String, Instruction> methodLabels, HashMap<String, Descriptor> methodVarTable) {
        String instructionLabels = methodLabels.entrySet().stream()
                .filter(entry -> entry.getValue().equals(instruction))
                .map(entry -> "\t" + entry.getKey() + ":\n")
                .collect(Collectors.joining());

        return instructionLabels + this.instructionMap.apply(instruction, methodVarTable);
    }

    public String getJasminCode(AssignInstruction instruction, HashMap<String, Descriptor> varTable)  {
        StringBuilder code = new StringBuilder();

        if (instruction.getDest() instanceof ArrayOperand) {
            ArrayOperand arrayOperand = (ArrayOperand) instruction.getDest();
            // Load array
            code.append("\taload").append(this.getVariableVirtualRegister(arrayOperand.getName(), varTable)).append("\n");
            // Load index
            code.append(loadElementCode(arrayOperand.getIndexOperands().get(0), varTable));
        }

        code.append(getJasminCode(instruction.getRhs(), new HashMap<>(), varTable));

        // In case that on the right side of the assignment there is a call instruction for a new object - do not store yet
        if (!(instruction.getRhs() instanceof CallInstruction && instruction.getDest().getType().getTypeOfElement().equals(ElementType.OBJECTREF))) {
            code.append(this.storeElementCode((Operand) instruction.getDest(), varTable));
        }

        return code.toString();
    }

    public String getJasminCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        switch (instruction.getInvocationType()) {
            case invokespecial:
                return this.getCallInvokeSpecialCode(instruction, varTable);
            case invokestatic:
                return this.getCallInvokeStaticCode(instruction, varTable);
            case invokevirtual:
                return this.getCallInvokeVirtualCode(instruction, varTable);
            case arraylength:
                return code.append(this.loadElementCode(instruction.getFirstArg(), varTable))
                        .append("\tarraylength\n").toString();
            case NEW:
                if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.ARRAYREF) {
                    return code.append(this.loadElementCode(instruction.getListOfOperands().get(0), varTable))
                            .append("\tnewarray int\n").toString();
                }
                if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.OBJECTREF) {
                    String qualifiedClassName = this.getFullyQualifiedClassName(((Operand) instruction.getFirstArg()).getName());
                    return code.append("\tnew ").append(qualifiedClassName).append("\n")
                            .append("\tdup\n").toString();
                }
                throw new RuntimeException("A new function call must reference an array or object");
            case ldc: // TODO check
                return code.append("\tldc ").append(((LiteralElement) instruction.getFirstArg()).getLiteral()).toString();
            default:
                throw new NotImplementedException(instruction.getInvocationType());
        }
    }

    private String getCallInvokeParametersCode(CallInstruction instruction) {

        return "(" +
                instruction.getListOfOperands().stream()
                        .map(operand -> this.getJasminType(operand.getType()))
                        .collect(Collectors.joining()) +
                ")" + this.getJasminType(instruction.getReturnType()) + "\n";
    }

    private String getCallInvokeOperandsLoadCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {

        return instruction.getListOfOperands().stream()
                .map(operand -> this.loadElementCode(operand, varTable))
                .collect(Collectors.joining());
    }

    private String getCallInvokeStaticCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        // Load operands involved in the invocation
        code.append(this.getCallInvokeOperandsLoadCode(instruction, varTable));

        String staticMethodCall = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");

        // Call invocation instruction
        code.append("\tinvokestatic ").append(((Operand) instruction.getFirstArg()).getName())
                .append("/").append(staticMethodCall).append(this.getCallInvokeParametersCode(instruction));

        return code.toString();
    }

    private String getCallInvokeVirtualCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        // Load arguments and operands involved in the invocation
        code.append(this.loadElementCode(instruction.getFirstArg(), varTable));
        code.append(this.getCallInvokeOperandsLoadCode(instruction, varTable));

        // Call invocation instruction
        String methodClass = this.getFullyQualifiedClassName(((ClassType) instruction.getFirstArg().getType()).getName());
        String virtualMethodCall = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");

        code.append("\tinvokevirtual ").append(methodClass)
                .append("/").append(virtualMethodCall).append(this.getCallInvokeParametersCode(instruction));

        return code.toString();
    }

    private String getCallInvokeSpecialCode(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS) {
            // Load arguments and operands involved in the invocation
            code.append(this.loadElementCode(instruction.getFirstArg(), varTable));
            code.append(this.getCallInvokeOperandsLoadCode(instruction, varTable));

            code.append("\tinvokespecial java/lang/Object/<init>")
                    .append(this.getCallInvokeParametersCode(instruction));
        } else {
            // Load operands involved in the invocation
            code.append(this.getCallInvokeOperandsLoadCode(instruction, varTable));

            String methodClass =this.getFullyQualifiedClassName(((ClassType) instruction.getFirstArg().getType()).getName());
            code.append("\tinvokespecial ").append(methodClass)
                    .append("/<init>").append(this.getCallInvokeParametersCode(instruction));
            code.append(this.storeElementCode((Operand) instruction.getFirstArg(), varTable));
        }

        return code.toString();
    }

    public String getJasminCode(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        code.append(this.loadElementCode(instruction.getFirstOperand(), varTable));
        code.append("\tgetfield ").append(this.classUnit.getClassName()).append("/")
                .append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(this.getJasminType(instruction.getSecondOperand().getType())).append("\n");

        return code.toString();
    }

    public String getJasminCode(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        code.append(this.loadElementCode(instruction.getFirstOperand(), varTable));
        code.append(this.loadElementCode(instruction.getThirdOperand(), varTable));
        code.append("\tputfield ").append(this.classUnit.getClassName()).append("/")
                .append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(this.getJasminType(instruction.getSecondOperand().getType())).append("\n");

        return code.toString();
    }

    public String getJasminCode(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        OperationType opType = instruction.getOperation().getOpType();

        if (Arrays.asList(OperationType.ADD, OperationType.SUB, OperationType.MUL, OperationType.DIV).contains(opType))
            return this.getBinaryIntOperationCode(instruction, varTable);
        else if (Arrays.asList(OperationType.EQ, OperationType.GTE, OperationType.GTH, OperationType.LTE,
                OperationType.LTH, OperationType.NEQ, OperationType.ANDB, OperationType.NOTB).contains(opType))
            return this.getBinaryBooleanOperationCode(instruction, varTable);

        throw new NotImplementedException(instruction.getOperation().getOpType());
    }

    private String getBinaryIntOperationCode(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder code = new StringBuilder();

        code.append(this.loadElementCode(instruction.getLeftOperand(), varTable));
        code.append(this.loadElementCode(instruction.getRightOperand(), varTable));

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

    private String getBinaryBooleanOperationCode(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
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
                code.append(this.loadElementCode(instruction.getLeftOperand(), varTable));
                code.append(this.loadElementCode(instruction.getRightOperand(), varTable));

                String compInst = this.getComparisonInstructionCode(instruction.getOperation().getOpType());
                code.append("\t").append(compInst).append(" ").append(trueLabel).append("\n")
                        .append(this.getBinaryBooleanJumpsCode(trueLabel, endIfLabel));
                break;
            case ANDB:
                code.append(this.loadElementCode(instruction.getLeftOperand(), varTable))
                        .append("\tifeq ").append(trueLabel).append("\n");
                code.append(this.loadElementCode(instruction.getRightOperand(), varTable))
                        .append("\tifeq ").append(trueLabel).append("\n");

                code.append(this.getBinaryBooleanJumpsCode(trueLabel, endIfLabel));
                break;
            case NOTB:
                code.append(this.loadElementCode(instruction.getLeftOperand(), varTable))
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

    public String getJasminCode(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        throw new NotImplementedException(instruction.getInstType());
    }

    public String getJasminCode(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        return this.loadElementCode(instruction.getSingleOperand(), varTable);
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
                return this.loadElementCode(instruction.getOperand(), varTable) + "\tireturn\n";
            case ARRAYREF:
            case OBJECTREF:
                return this.loadElementCode(instruction.getOperand(), varTable) + "\tareturn\n";
            default:
                throw new NotImplementedException(instruction.getElementType());
        }
    }

    // TODO error when array of object reference or array of array
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

    private String getVariableVirtualRegister(String variableName, HashMap<String, Descriptor> varTable) {
        int virtualRegister = varTable.get(variableName).getVirtualReg();

        return virtualRegister > 3 ? " " + virtualRegister : "_" + virtualRegister;
    }

    private String loadElementCode(Element element, HashMap<String, Descriptor> varTable) {
        if (element instanceof LiteralElement) {
            return this.getConstantElementCode(((LiteralElement) element).getLiteral()) + "\n";
        }

        if (element instanceof ArrayOperand) {
            ArrayOperand arrayOperand = (ArrayOperand) element;

            // Load array + Load index + Load value
            return "\taload" + this.getVariableVirtualRegister(arrayOperand.getName(), varTable) + "\n" +
                    loadElementCode(arrayOperand.getIndexOperands().get(0), varTable) +
                    "\tiaload\n";
        }

        if (element instanceof Operand) {
            Operand operand = (Operand) element;

            switch (operand.getType().getTypeOfElement()) {
                case THIS:
                    return "\taload_0\n";
                case INT32:
                case BOOLEAN:
                    return "\tiload" + this.getVariableVirtualRegister(operand.getName(), varTable) + "\n";
                case OBJECTREF:
                case ARRAYREF:
                    return "\taload" + this.getVariableVirtualRegister(operand.getName(), varTable) + "\n";
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

    private String storeElementCode(Operand operand, HashMap<String, Descriptor> varTable) {
        if (operand instanceof ArrayOperand) {
            return "\tiastore\n";
        }

        switch (operand.getType().getTypeOfElement()) {
            case INT32:
            case BOOLEAN:
                return "\tistore" + this.getVariableVirtualRegister(operand.getName(), varTable) + "\n";
            case OBJECTREF:
            case ARRAYREF:
                return "\tastore" + this.getVariableVirtualRegister(operand.getName(), varTable) + "\n";
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
