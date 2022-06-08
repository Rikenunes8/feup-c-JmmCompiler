package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.HashMap;
import java.util.stream.Collectors;

public class JasminUtils {

    static public String getFullyQualifiedClassName(ClassUnit classUnit, String className) {
        if (className.equals(classUnit.getClassName())) {
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

    static public String getJasminType(ClassUnit classUnit, Type type) {
        StringBuilder code = new StringBuilder();

        if (type instanceof ArrayType) {
            code.append("[".repeat(((ArrayType) type).getNumDimensions()));
        }

        ElementType elementType = (type instanceof ArrayType)
                ? ((ArrayType) type).getArrayType()
                : type.getTypeOfElement();

        switch (elementType) {
            case VOID:
                return code.append("V").toString();
            case INT32:
                return code.append("I").toString();
            case BOOLEAN:
                return code.append("Z").toString();
            case STRING:
                return code.append("Ljava/lang/String;").toString();
            case OBJECTREF:
                String className = (type instanceof ArrayType)
                        ? ((ArrayType) type).getElementClass()
                        : ((ClassType) type).getName();
                return code.append("L").append(JasminUtils.getFullyQualifiedClassName(classUnit, className)).append(";").toString();
            default:
                throw new NotImplementedException(type);
        }
    }

    static public String getElementTypePrefix(Element element) {
        switch (element.getType().getTypeOfElement()) {
            case INT32:
            case BOOLEAN:
                return "i";
            case ARRAYREF:
            case OBJECTREF:
                return "a";
            default:
                return null;
        }
    }

    static public String getVariableVirtualRegister(String variableName, HashMap<String, Descriptor> varTable) {
        int virtualRegister = varTable.get(variableName).getVirtualReg();

        return virtualRegister > 3 ? " " + virtualRegister : "_" + virtualRegister;
    }

    static public String loadElementCode(Element element, HashMap<String, Descriptor> varTable) {
        if (element instanceof LiteralElement) {
            return JasminUtils.loadElementCode((LiteralElement) element);
        }

        if (element instanceof ArrayOperand) {
            return JasminUtils.loadElementCode((ArrayOperand) element, varTable);
        }

        if (element instanceof Operand) {
            return JasminUtils.loadElementCode((Operand) element, varTable);
        }

        throw new RuntimeException("Exception during load elements");
    }

    static private String loadElementCode(LiteralElement element) {
        String literal = element.getLiteral();
        JasminLimits.incrementStack(1);

        int number = Integer.parseInt(literal);
        if (number >= -1 && number <= 5)
            return "\ticonst_" + literal + "\n";
        else if (number >= -128 && number <= 127)
            return "\tbipush " + literal + "\n";
        else if (number >= -32768 && number <= 32767)
            return "\tsipush " + literal + "\n";
        else
            return "\tldc " + literal + "\n";
    }

    static private String loadElementCode(ArrayOperand element, HashMap<String, Descriptor> varTable) {

        // Load array + Load index + Load value
        String code = "\taload" + JasminUtils.getVariableVirtualRegister(element.getName(), varTable) + "\n";
        JasminLimits.incrementStack(1);
        code += JasminUtils.loadElementCode(element.getIndexOperands().get(0), varTable);
        code += "\tiaload\n";
        JasminLimits.decrementStack(1);

        return code;
    }

    static private String loadElementCode(Operand element, HashMap<String, Descriptor> varTable) {

        JasminLimits.incrementStack(1);
        switch (element.getType().getTypeOfElement()) {
            case THIS:
                return "\taload_0\n";
            case VOID: // When in invokes we can not know the return type of the function called -> assume that is an INT
            case INT32:
            case BOOLEAN:
                return "\tiload" + JasminUtils.getVariableVirtualRegister(element.getName(), varTable) + "\n";
            case STRING:
            case OBJECTREF:
            case ARRAYREF:
                return "\taload" + JasminUtils.getVariableVirtualRegister(element.getName(), varTable) + "\n";
            default:
                throw new RuntimeException("Exception during load elements of type operand");
        }
    }

    static public String storeElementCode(Operand operand, HashMap<String, Descriptor> varTable) {
        if (operand instanceof ArrayOperand) {
            JasminLimits.decrementStack(1);
            return "\tiastore\n";
        }

        JasminLimits.decrementStack(1);
        switch (operand.getType().getTypeOfElement()) {
            case VOID: // When in invokes we can not know the return type of the function called -> assume that is an INT
            case INT32:
            case BOOLEAN:
                return "\tistore" + JasminUtils.getVariableVirtualRegister(operand.getName(), varTable) + "\n";
            case STRING:
            case OBJECTREF:
            case ARRAYREF:
                return "\tastore" + JasminUtils.getVariableVirtualRegister(operand.getName(), varTable) + "\n";
            default:
                throw new RuntimeException("Exception during store elements  type" + operand.getType().getTypeOfElement());
        }
    }

    static public boolean isIincOptimizable(AssignInstruction instruction) {
        if (instruction.getRhs() instanceof BinaryOpInstruction) {
            BinaryOpInstruction leftInstruction = (BinaryOpInstruction) instruction.getRhs();

            if (leftInstruction.getOperation().getOpType() == OperationType.ADD) {
                Element leftEl = leftInstruction.getLeftOperand();
                Element rightEl = leftInstruction.getRightOperand();

                // a = a (+) Literal or a = Literal (+) a
                return ((leftEl instanceof Operand && ((Operand) leftEl).getName()
                        .equals(((Operand) instruction.getDest()).getName()) && rightEl instanceof LiteralElement)
                    || rightEl instanceof Operand && ((Operand) rightEl).getName()
                        .equals(((Operand) instruction.getDest()).getName()) && leftEl instanceof LiteralElement);
            } else if (leftInstruction.getOperation().getOpType() == OperationType.SUB) {
                Element leftEl = leftInstruction.getLeftOperand();
                Element rightEl = leftInstruction.getRightOperand();

                // a = a (-) Literal [only]
                return (leftEl instanceof Operand && ((Operand) leftEl).getName()
                        .equals(((Operand) instruction.getDest()).getName()) && rightEl instanceof LiteralElement);
            }
        }

        return false;
    }
}
