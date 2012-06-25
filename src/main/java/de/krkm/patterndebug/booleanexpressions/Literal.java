package de.krkm.patterndebug.booleanexpressions;

public class Literal extends BooleanExpression {
    private String name;

    public Literal(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public ExpressionType getType() {
        return ExpressionType.LITERAL;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Literal literal = (Literal) o;

        if (!name.equals(literal.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
