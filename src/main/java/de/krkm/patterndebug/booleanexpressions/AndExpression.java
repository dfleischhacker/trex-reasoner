package de.krkm.patterndebug.booleanexpressions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AndExpression extends BooleanExpression {
    Set<BooleanExpression> expressions;

    public AndExpression(BooleanExpression... expressions) {
        this.expressions = new HashSet<BooleanExpression>();
        Collections.addAll(this.expressions, expressions);
    }

    public AndExpression(Set<BooleanExpression> expressions) {
        this.expressions = new HashSet<BooleanExpression>(expressions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (BooleanExpression e : expressions) {
            if (sb.length() == 0) {
                sb.append("(").append(e.toString());
            }
            else {
                sb.append(" AND ").append(e.toString());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public ExpressionType getType() {
        return ExpressionType.AND;
    }

    public boolean isAbsorbedBy(AndExpression o) {
        return expressions.containsAll(o.expressions);
    }

    public Set<BooleanExpression> getExpressions() {
        return expressions;
    }
}
