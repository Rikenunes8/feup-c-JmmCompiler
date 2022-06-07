package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.HashMap;
import java.util.stream.Collectors;

public class JasminInstrCallGenerator {
    private final ClassUnit classUnit;
    private final CallInstruction instruction;
    private final HashMap<String, Descriptor> varTable;
    private final boolean inAssignment;

    public JasminInstrCallGenerator(ClassUnit classUnit, CallInstruction instruction, HashMap<String, Descriptor> varTable, boolean inAssignment) {
        this.classUnit = classUnit;
        this.instruction = instruction;
        this.varTable = varTable;
        this.inAssignment = inAssignment;
    }

    public String getJasminCode() {
        switch (this.instruction.getInvocationType()) {
            case invokespecial:
                return this.getCallInvokeSpecialCode();
            case invokestatic:
                return this.getCallInvokeStaticCode();
            case invokevirtual:
                return this.getCallInvokeVirtualCode();
            case arraylength:
                return this.getCallArrayLengthCode();
            case NEW:
                return this.getCallNewObjectCode();
            case ldc:
                return this.getCallLdcCode();
            default:
                throw new NotImplementedException(instruction.getInvocationType());
        }
    }

    private String getCallInvokeParametersCode() {

        return "(" +
                this.instruction.getListOfOperands().stream()
                        .map(operand -> JasminUtils.getJasminType(this.classUnit, operand.getType()))
                        .collect(Collectors.joining()) +
                ")" + JasminUtils.getJasminType(this.classUnit, this.instruction.getReturnType()) + "\n";
    }

    private String getCallInvokeOperandsLoadCode() {

        return this.instruction.getListOfOperands().stream()
                .map(operand -> JasminUtils.loadElementCode(operand, this.varTable))
                .collect(Collectors.joining());
    }

    private String getCallInvokeStaticCode() {
        StringBuilder code = new StringBuilder();

        // Load operands involved in the invocation
        code.append(this.getCallInvokeOperandsLoadCode());

        // Call invocation instruction
        String staticMethodCall = ((LiteralElement) this.instruction.getSecondArg()).getLiteral().replace("\"", "");

        code.append("\tinvokestatic ").append(((Operand) this.instruction.getFirstArg()).getName())
                .append("/").append(staticMethodCall).append(this.getCallInvokeParametersCode());
        JasminLimits.decrementStack(this.instruction.getListOfOperands().size());

        return code.toString();
    }

    private String getCallInvokeVirtualCode() {
        StringBuilder code = new StringBuilder();

        // Load arguments and operands involved in the invocation
        code.append(JasminUtils.loadElementCode(this.instruction.getFirstArg(), this.varTable));
        code.append(this.getCallInvokeOperandsLoadCode());

        // Call invocation instruction
        String methodClass = JasminUtils.getFullyQualifiedClassName(this.classUnit, ((ClassType) this.instruction.getFirstArg().getType()).getName());
        String virtualMethodCall = ((LiteralElement) this.instruction.getSecondArg()).getLiteral().replace("\"", "");

        code.append("\tinvokevirtual ").append(methodClass)
                .append("/").append(virtualMethodCall).append(this.getCallInvokeParametersCode());
        JasminLimits.decrementStack(this.instruction.getListOfOperands().size());

        if (!this.inAssignment && this.instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
            code.append("pop").append("\n");
            JasminLimits.decrementStack(1);
        }

        return code.toString();
    }

    private String getCallInvokeSpecialCode() {
        StringBuilder code = new StringBuilder();

        if (this.instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS) {
            // Load arguments and operands involved in the invocation
            code.append(JasminUtils.loadElementCode(this.instruction.getFirstArg(), this.varTable));
            code.append(this.getCallInvokeOperandsLoadCode());

            code.append("\tinvokespecial java/lang/Object/<init>").append(this.getCallInvokeParametersCode());
            JasminLimits.decrementStack(this.instruction.getListOfOperands().size());
        } else {
            // Load operands involved in the invocation
            code.append(this.getCallInvokeOperandsLoadCode());

            String methodClass = JasminUtils.getFullyQualifiedClassName(this.classUnit, ((ClassType) this.instruction.getFirstArg().getType()).getName());
            code.append("\tinvokespecial ").append(methodClass)
                    .append("/<init>").append(this.getCallInvokeParametersCode());
            JasminLimits.decrementStack(this.instruction.getListOfOperands().size());
            code.append(JasminUtils.storeElementCode((Operand) this.instruction.getFirstArg(), this.varTable));
        }

        return code.toString();
    }

    private String getCallArrayLengthCode() {
        return JasminUtils.loadElementCode(this.instruction.getFirstArg(), this.varTable) +
                "\tarraylength\n";
    }

    private String getCallNewObjectCode() {
        if (this.instruction.getFirstArg().getType().getTypeOfElement() == ElementType.ARRAYREF) {
            StringBuilder code = new StringBuilder();

            for (Element operand : this.instruction.getListOfOperands()) {
                code.append(JasminUtils.loadElementCode(operand, this.varTable));
            }

            int arrayDimensions = this.instruction.getNumOperands() - 1;
            if (arrayDimensions > 1) {
                String arrayType = JasminUtils.getJasminType(this.classUnit, this.instruction.getReturnType());
                code.append("\tmultianewarray ").append(arrayType).append(" ").append(arrayDimensions).append("\n");
            } else {
                ArrayType returnType = (ArrayType) this.instruction.getReturnType();
                if (returnType.getArrayType() == ElementType.OBJECTREF) {
                    code.append("\tanewarray ").append(returnType.getElementClass()).append("\n");
                } else {
                    code.append("\tnewarray int\n");
                }
            }

            return code.toString();
        }
        if (this.instruction.getFirstArg().getType().getTypeOfElement() == ElementType.OBJECTREF) {
            JasminLimits.incrementStack(2);
            String qualifiedClassName = JasminUtils.getFullyQualifiedClassName(this.classUnit, ((Operand) this.instruction.getFirstArg()).getName());
            return "\tnew " + qualifiedClassName + "\n" +
                    "\tdup\n";
        }
        throw new RuntimeException("A new function call must reference an array or object");
    }

    private String getCallLdcCode() {
        JasminLimits.incrementStack(1);
        return "\tldc " + ((LiteralElement) this.instruction.getFirstArg()).getLiteral() + "\n";
    }
}
