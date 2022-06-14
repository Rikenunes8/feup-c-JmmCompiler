package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashSet;
import java.util.Set;

public class JasminLimits {
    // limit stack  - max length of the stack that we need to the method
    // limit locals - max number of registers we need to use

    static private int stackCounter = 0;
    static private int stackLimit = 0;

    static public int getLocals(Method method) {
        int max_regUsed = 0;
        for (var local : method.getVarTable().values()) {
            max_regUsed = Math.max(max_regUsed, local.getVirtualReg());
        }
        return max_regUsed + 1;
    }

    static public int getStack() {
        return stackLimit;
    }

    static public void resetStack() {
        stackCounter = 0;
        stackLimit = 0;
    }

    static public void incrementStack(int value) {
        stackCounter += value;
        stackLimit = Math.max(stackLimit, stackCounter);
    }

    static public void decrementStack(int value) {
        stackCounter -= value;
    }

    static public String changeMethodStack(String methodCode) {
        return methodCode.replace(".limit stack 0", ".limit stack " + stackLimit);
    }
}
