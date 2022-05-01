package pt.up.fe.comp.ast;

import pt.up.fe.specs.util.SpecsStrings;

public enum AstNode {
    CLASS_DECLARATION,
    METHOD_DECLARATION,
    RETURN_STATEMENT,
    IDENTIFIER_LITERAL,
    THIS_LITERAL,
    ADD_EXP,
    SUB_EXP,
    MULT_EXP,
    DIV_EXP,
    AND_EXP,
    NOT_EXP,
    LESS_EXP,
    ARRAY_ACCESS_EXP,
    ASSIGNMENT_STATEMENT,
    NEW_INT_ARRAY,
    DOT_EXP,
    FUNCTION_CALL,
    NEW_OBJECT,
    VAR_DECLARATION,
    PROPERTY_LENGTH,
    CONDITION
    ;
    private final String name;
    private AstNode() {
        this.name = SpecsStrings.toCamelCase(name(), "_", true);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
