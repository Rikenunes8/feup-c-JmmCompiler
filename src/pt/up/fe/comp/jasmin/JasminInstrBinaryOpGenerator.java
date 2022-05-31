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

        switch (this.instruction.getOperation().getOpType()) {
            case ADD:
                return code.append("\tiadd\n").toString();
            case SUB:
                return code.append("\tisub\n").toString();
            case MUL:
                return code.append("\timul\n").toString();
            case DIV:
                return code.append("\tidiv\n").toString();
            default:
                throw new NotImplementedException(this.instruction.getOperation().getOpType());
        }
    }

    private String getBinaryBooleanOperationCode() {
        StringBuilder code = new StringBuilder();

        switch (this.instruction.getOperation().getOpType()) {
            case EQ:
            case GTE:
            case GTH:
            case LTE:
            case LTH:
            case NEQ:
                String comparison = this.getComparisonInstructionCode(this.instruction.getOperation().getOpType());
                String trueLabel = nextLabel();
                String falseLabel = nextLabel();

                code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
                code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));
                code.append(this.getBinaryBooleanJumpsCode(comparison, trueLabel, falseLabel));
                break;
            case AND:
            case ANDB:
                code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
                code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));
                code.append("\tiand\n");
                break;
            case OR:
            case ORB:
                code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
                code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));
                code.append("\tior\n");
                break;
            case XOR:
                code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
                code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));
                code.append("\tixor\n");
                break;
            case NOT:
            case NOTB:
                code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
                code.append("\tineg\n");
                break;
            default:
                throw new NotImplementedException(this.instruction.getOperation().getOpType());
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

    public String getBinaryBooleanJumpsCode(String comparison, String trueLabel, String falseLabel) {

        return  "\t" + comparison + " " + trueLabel + "\n" +
                "\ticonst_0\n" +
                "\tgoto " + falseLabel + "\n" +
                "\t" + trueLabel + ":\n" +
                "\ticonst_1\n" +
                "\t" + falseLabel + ":\n";
    }
}
