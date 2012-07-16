package de.krkm.trex.booleanexpressions;

/**
 * Base interface for boolean expressions
 */
public abstract class BooleanExpression {
    enum ExpressionType {
        AND,
        OR,
        LITERAL
    }
    /**
     * Returns the string representation of the boolean expression
     *
     * @return string representation of this expression
     */
    public abstract String toString();

    /**
     * Returns the type of this boolean expression
     *
     * @return type of this expression
     */
    public abstract ExpressionType getType();
}
