package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;

public class JasminLimits {
    // limit stack  - max length of the stack that we need to the method
    // limit locals - max number of registers we need to use

    static private int stackCounter = 0;
    static private int stackLimit = -1;

    static public int getLocals(Method method) {
        return (method.getVarTable().containsKey("this") || method.isStaticMethod())
                ? method.getVarTable().size()
                : method.getVarTable().size() + 1;
    }

    static public int getStack() {
        return stackLimit;
    }

    static public void resetStack() {
        stackCounter = 0;
        stackLimit = -1;
    }

    static public void incrementStack(int value) {
        stackCounter += value;
        stackLimit = Math.max(stackLimit, stackCounter);
    }

    static public void decrementStack(int value) {
        stackCounter -= value;
    }

    static public String changeMethodStack(String methodCode) {
        stackLimit = 99; // TODO remove once stack limit is computed by increment and decrement stack counter here needed
        return methodCode.replace(".limit stack -1", ".limit stack " + stackLimit);
    }
}
