package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.reasoner.Reasoner;
import de.krkm.patterndebug.util.Util;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import static de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer.*;

/**
 * Implements the inference step for SubClassOf axioms.
 */
public class SubPropertyOfInferenceStepProvider implements InferenceStepProvider {
    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        // stated subsumption
        for (OWLSubObjectPropertyOfAxiom a : ontology.getAxioms(AxiomType.SUB_OBJECT_PROPERTY)) {
            if (!a.getSubProperty().isAnonymous() && !a.getSuperProperty().isAnonymous()) {
                String subPropertyIRI = Util.getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString());
                String superPropertyIRI = Util.getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString());
                matrix.set(subPropertyIRI, superPropertyIRI, true);
                int subId = matrix.getNamingManager().getConceptId(subPropertyIRI);
                int superId = matrix.getNamingManager().getConceptId(superPropertyIRI);
                matrix.addExplanation(subId, superId, or(and(literal(String.format("SubPropertyOf(%s, %s)", subPropertyIRI,
                        superPropertyIRI)))));
            }
        }

        // stated class equivalence
        for (OWLEquivalentObjectPropertiesAxiom equiv : ontology.getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES)) {
            for (OWLSubObjectPropertyOfAxiom a : equiv.asSubObjectPropertyOfAxioms()) {
                if (!a.getSubProperty().isAnonymous() && !a.getSuperProperty().isAnonymous()) {
                    String subPropertyIRI = Util.getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString());
                    String superPropertyIRI = Util.getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString());
                    matrix.set(subPropertyIRI, superPropertyIRI, true);
                    int subId = matrix.getNamingManager().getConceptId(subPropertyIRI);
                    int superId = matrix.getNamingManager().getConceptId(superPropertyIRI);
                    matrix.addExplanation(subId, superId, or(and(literal(String.format("SubPropertyOf(%s, %s)", subPropertyIRI,
                            superPropertyIRI)))));
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        for (int i = 0; i < matrix.getDimension(); i++) {
            if (matrix.get(row, i) && matrix.get(i, col)) {
                boolean mod = matrix.set(row, col, true);
//                if (mod) {
//                    getAxiomRepresentation(matrix, row, col);
//                }
                matrix.addExplanation(row, col,
                        ExpressionMinimizer.flatten(matrix.getExplanation(row, i), matrix.getExplanation(i, col)));
                return mod;
            }
        }

        return false;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return String.format("SubPropertyOf(%s, %s)", matrix.getNamingManager().getPropertyIRI(row),
                    matrix.getNamingManager()
                          .getConceptIRI(col));
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "SubPropertyOf";
    }

    @Override
    public boolean isSymmetric() {
        return false;
    }
}
