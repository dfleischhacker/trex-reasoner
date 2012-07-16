package de.krkm.trex.booleanexpressions;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AndExpression extends BooleanExpression {
    Set<Literal> expressions;

    public AndExpression(Literal... expressions) {
        this.expressions = new HashSet<Literal>();
        Collections.addAll(this.expressions, expressions);
    }

    public AndExpression(Set<Literal> expressions) {
        this.expressions = new HashSet<Literal>(expressions);
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

    public Set<OWLAxiom> getConjunction() {
        Set<OWLAxiom> elements = new HashSet<OWLAxiom>();
        for (Literal e : expressions) {
            elements.add(e.getOWLAxiom());
        }
        return elements;
    }

    public boolean isAbsorbedBy(AndExpression o) {
        return expressions.containsAll(o.expressions);
    }

    public Set<Literal> getExpressions() {
        return expressions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AndExpression that = (AndExpression) o;

        if (!expressions.equals(that.expressions)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return expressions.hashCode();
    }

    public AndExpression copy() {
        return new AndExpression(new HashSet<Literal>(expressions));
    }
}
