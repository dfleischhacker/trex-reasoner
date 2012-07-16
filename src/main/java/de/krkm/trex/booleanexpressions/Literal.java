package de.krkm.trex.booleanexpressions;

import org.semanticweb.owlapi.model.OWLAxiom;

public class Literal extends BooleanExpression {
    private OWLAxiom axiom;

    public Literal(OWLAxiom axiom) {
        this.axiom = axiom;
    }

    @Override
    public String toString() {
        return axiom.toString();
    }

    @Override
    public ExpressionType getType() {
        return ExpressionType.LITERAL;
    }

    public OWLAxiom getOWLAxiom() {
        return axiom;
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

        if (!axiom.equals(literal.axiom)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return axiom.hashCode();
    }
}
