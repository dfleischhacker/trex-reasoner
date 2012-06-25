package de.krkm.patterndebug.booleanexpressions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OrExpression extends BooleanExpression {
    private Set<AndExpression> expressions;

    public OrExpression(AndExpression... expressions) {
        this.expressions = new HashSet<AndExpression>();
        Collections.addAll(this.expressions, expressions);
    }

    public OrExpression(Set<AndExpression> expressions) {
        this.expressions = new HashSet<AndExpression>(expressions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (BooleanExpression e : expressions) {
            if (sb.length() == 0) {
                sb.append("(").append(e.toString());
            } else {
                sb.append(" OR ").append(e.toString());
            }
        }
        if (sb.length() != 0) {
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public ExpressionType getType() {
        return ExpressionType.OR;
    }

    public Set<AndExpression> getExpressions() {
        return expressions;
    }

    public void addExpression(AndExpression expr) {
        expressions.add(expr);
    }
}
