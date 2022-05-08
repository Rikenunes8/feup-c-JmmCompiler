package pt.up.fe.comp.ast;

import pt.up.fe.specs.util.SpecsStrings;

public enum AstNode {
    PROGRAM,
    CLASS_DECLARATION,
    METHOD_DECLARATION,
    METHOD_PARAMETERS,
    RETURN_STATEMENT,
    IDENTIFIER_LITERAL,
    INTEGER_LITERAL,
    THIS_LITERAL,
    TRUE_LITERAL,
    FALSE_LITERAL,
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
    CONDITION,
    IF_STATEMENT,
    IF_BLOCK,
    ELSE_BLOCK,
    WHILE_STATEMENT,
    WHILE_BLOCK,
    EXPRESSION_STATEMENT,
    SCOPE
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
