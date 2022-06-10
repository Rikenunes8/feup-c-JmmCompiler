package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.Arrays;
import java.util.HashMap;

public class JasminInstrBinaryOpGenerator {
    private BinaryOpInstruction instruction;
    private HashMap<String, Descriptor> varTable;

    private int labelCounter;

    public JasminInstrBinaryOpGenerator() {
        this.labelCounter = 0;
    }

    public JasminInstrBinaryOpGenerator(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        this.instruction = instruction;
        this.varTable = varTable;

        this.labelCounter = 0;
    }

    public String nextLabel() {
        return "label" + this.labelCounter++;
    }

    public void setInstruction(BinaryOpInstruction instruction) {
        this.instruction = instruction;
    }

    public void setVarTable(HashMap<String, Descriptor> varTable) {
        this.varTable = varTable;
    }

    public void resetLabelCounter() {
        this.labelCounter = 0;
    }

    public String getJasminCode() {
        OperationType opType = this.instruction.getOperation().getOpType();

        if (Arrays.asList(OperationType.ADD, OperationType.SUB, OperationType.MUL, OperationType.DIV).contains(opType))
            return this.getBinaryIntOperationCode();
        else if (Arrays.asList(OperationType.EQ, OperationType.GTE, OperationType.GTH, OperationType.LTE,
                OperationType.LTH, OperationType.NEQ, OperationType.AND, OperationType.ANDB,
                OperationType.OR, OperationType.ORB, OperationType.NOT, OperationType.NOTB, OperationType.XOR).contains(opType))
            return this.getBinaryBooleanOperationCode();

        throw new NotImplementedException(this.instruction.getOperation().getOpType());
    }

    private String getBinaryIntOperationCode() {
        StringBuilder code = new StringBuilder();

        code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
        code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));

        String typePrefix = JasminUtils.getElementTypePrefix(this.instruction.getLeftOperand());
        switch (this.instruction.getOperation().getOpType()) {
            case ADD:
                code.append("\t").append(typePrefix).append("add\n"); break;
            case SUB:
                code.append("\t").append(typePrefix).append("sub\n"); break;
            case MUL:
                code.append("\t").append(typePrefix).append("mul\n"); break;
            case DIV:
                code.append("\t").append(typePrefix).append("div\n"); break;
            default:
                throw new NotImplementedException(this.instruction.getOperation().getOpType());
        }

        JasminLimits.decrementStack(1);
        return code.toString();
    }

    private String getBinaryBooleanOperationCode() {
        StringBuilder code = new StringBuilder();
        OperationType operationType = this.instruction.getOperation().getOpType();

        code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
        if (operationType != OperationType.NOT && operationType != OperationType.NOTB
                && operationType != OperationType.ANDB && operationType != OperationType.ORB) {
            code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));
        }

        String typePrefix = JasminUtils.getElementTypePrefix(this.instruction.getLeftOperand());
        switch (operationType) {
            case EQ:
            case GTE:
            case GTH:
            case LTE:
            case LTH:
            case NEQ:
                String comparison = this.getComparisonInstructionCode(operationType);
                code.append(this.getBinaryBooleanJumpsCode(comparison, nextLabel(), nextLabel()));
                break;
            case AND:
                code.append("\t").append(typePrefix).append("and\n");
                JasminLimits.decrementStack(1);
                break;
            case OR:
                code.append("\t").append(typePrefix).append("or\n");
                JasminLimits.decrementStack(1);
                break;
            case XOR:
                code.append("\t").append(typePrefix).append("xor\n");
                JasminLimits.decrementStack(1);
                break;
            case NOT:
                code.append("\t").append(typePrefix).append("neg\n");
                break;
            case ANDB:
                String trueLabel = nextLabel();
                String falseLabel = nextLabel();

                code.append("\tifeq ").append(falseLabel).append("\n");
                JasminLimits.decrementStack(1);
                code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));
                code.append(this.getBinaryBooleanJumpsCode("ifne", trueLabel, falseLabel));
                break;
            case ORB:
                String trueLabelOr = nextLabel();
                String falseLabelOr = nextLabel();

                code.append("\tifne ").append(trueLabelOr).append("\n");
                JasminLimits.decrementStack(1);
                code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));
                code.append(this.getBinaryBooleanJumpsCode("ifne", trueLabelOr, falseLabelOr));
                break;
            case NOTB:
                code.append(this.getBinaryBooleanJumpsCode("ifeq", nextLabel(), nextLabel()));
                break;
            default:
                throw new NotImplementedException(this.instruction.getOperation().getOpType());
        }

        return code.toString();
    }

    private String getComparisonInstructionCode(OperationType operationType) {
        if (operationType == OperationType.ANDB || operationType == OperationType.NOTB) {
            JasminLimits.decrementStack(1);
        } else {
            JasminLimits.decrementStack(2);
        }

        switch (operationType) {
            case EQ: return "if_icmpeq";
            case GTE: return "if_icmpge";
            case GTH: return "if_icmpgt";
            case LTE: return "if_icmple";
            case LTH: return "if_icmplt";
            case NEQ: return "if_icmpne";
            case ANDB: return "ifne";
            case NOTB: return "ifeq";
            default: throw new NotImplementedException(operationType);
        }
    }

    public String getBinaryBooleanJumpsCode(String comparison, String trueLabel, String falseLabel) {
        String endLabel = this.nextLabel();

        JasminLimits.incrementStack(1);
        return  "\t" + comparison + " " + trueLabel + "\n" +
                "\t" + falseLabel + ":\n" +
                "\ticonst_0\n" +
                "\tgoto " + endLabel + "\n" +
                "\t" + trueLabel + ":\n" +
                "\ticonst_1\n" +
                "\t" + endLabel + ":\n";
    }
}
