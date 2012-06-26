package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.booleanexpressions.OrExpression;
import de.krkm.patterndebug.inference.InferenceStepProvider;
import de.krkm.patterndebug.inference.Matrix;
import de.krkm.patterndebug.reasoner.Reasoner;
import de.krkm.patterndebug.util.Util;
import org.semanticweb.owlapi.model.*;

import java.util.Set;

import static de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer.*;

/**
 * Implements the inference step for SubClassOf axioms.
 */
public class SubClassOfInferenceStepProvider extends InferenceStepProvider {
    private Reasoner reasoner;

    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        int dimension = matrix.getNamingManager().getNumberOfConcepts();
        matrix.setMatrix(new boolean[dimension][dimension]);
        matrix.setExplanations(new OrExpression[dimension][dimension]);

        this.reasoner = reasoner;
        // stated subsumption
        for (OWLSubClassOfAxiom a : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            if (!a.getSubClass().isAnonymous() && !a.getSuperClass().isAnonymous()) {
                String subClassIRI = Util.getFragment(a.getSubClass().asOWLClass().getIRI().toString());
                String superClassIRI = Util.getFragment(a.getSuperClass().asOWLClass().getIRI().toString());
                matrix.set(subClassIRI, superClassIRI, true);
                int subId = matrix.getNamingManager().getConceptId(subClassIRI);
                int superId = matrix.getNamingManager().getConceptId(superClassIRI);
                matrix.addExplanation(subId, superId, or(and(literal(String.format("SubConceptOf(%s, %s)", subClassIRI,
                        superClassIRI)))));
            }
        }

        // stated class equivalence
        for (OWLEquivalentClassesAxiom a : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
            Set<OWLClass> equivalentClassesSet = a.getNamedClasses();
            OWLClass[] equivalentClasses = equivalentClassesSet.toArray(new OWLClass[equivalentClassesSet.size()]);

            for (int i = 0; i < equivalentClasses.length; i++) {
                for (int j = 0; j < equivalentClasses.length; j++) {
                    if (i == j) {
                        continue;
                    }
                    String iriI = Util.getFragment(equivalentClasses[i].asOWLClass().getIRI().toString());
                    int idI = matrix.getNamingManager().getConceptId(iriI);
                    String iriJ = Util.getFragment(equivalentClasses[j].asOWLClass().getIRI().toString());
                    int idJ = matrix.getNamingManager().getConceptId(iriJ);
                    matrix.set(iriI, iriJ, true);
                    matrix.addExplanation(idI, idJ,
                            or(and(literal(String.format("SubConceptOf(%s, %s)", iriI, iriJ)))));
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        for (int i = 0; i < matrix.getDimensionRow(); i++) {
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
            return String.format("SubClassOf(%s, %s)", matrix.getNamingManager().getConceptIRI(row),
                    matrix.getNamingManager()
                          .getConceptIRI(col));
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "SubClassOf";
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public int resolveRowIRI(String iri) {
        return reasoner.getNamingManager().getConceptId(iri);
    }

    @Override
    public String resolveRowID(int id) {
        return reasoner.getNamingManager().getConceptIRI(id);
    }
}
