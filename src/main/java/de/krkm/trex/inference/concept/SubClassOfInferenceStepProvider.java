package de.krkm.trex.inference.concept;

import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
import de.krkm.trex.booleanexpressions.OrExpression;
import de.krkm.trex.inference.InferenceStepProvider;
import de.krkm.trex.inference.Matrix;
import de.krkm.trex.reasoner.Reasoner;
import de.krkm.trex.util.Util;
import org.semanticweb.owlapi.model.*;

import java.util.Set;

import static de.krkm.trex.booleanexpressions.ExpressionMinimizer.*;

/**
 * Implements the inference step for SubClassOf axioms.
 */
public class SubClassOfInferenceStepProvider extends InferenceStepProvider {
    private Reasoner reasoner;
    private OWLDataFactory factory;
    private Matrix matrix;

    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        int dimension = matrix.getNamingManager().getNumberOfConcepts();
        matrix.setMatrix(new boolean[dimension][dimension]);
        matrix.setExplanations(new OrExpression[dimension][dimension]);

        this.matrix = matrix;

        this.reasoner = reasoner;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        // stated subsumption
        for (OWLSubClassOfAxiom a : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            if (!a.getSubClass().isAnonymous() && !a.getSuperClass().isAnonymous()) {
                String subClassIRI = Util.getFragment(a.getSubClass().asOWLClass().getIRI().toString());
                String superClassIRI = Util.getFragment(a.getSuperClass().asOWLClass().getIRI().toString());
                matrix.set(subClassIRI, superClassIRI, true);
                int subId = matrix.getNamingManager().getConceptId(subClassIRI);
                int superId = matrix.getNamingManager().getConceptId(superClassIRI);
                matrix.addExplanation(subId, superId, or(and(literal(a.getAxiomWithoutAnnotations()))));
            }
        }

        // stated class equivalence
        for (OWLEquivalentClassesAxiom a : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
            for (OWLEquivalentClassesAxiom p : a.asPairwiseAxioms()) {
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
                                or(and(literal(p))));
                    }
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        boolean mod = false;
//        log.debug("Inferencing for {} {}", row, col);
        for (int i = 0; i < matrix.getDimensionRow(); i++) {
            if (matrix.get(row, i) && matrix.get(i, col)) {
                matrix.set(row, col, true);
                mod = matrix.addExplanation(row, col, ExpressionMinimizer
                        .flatten(matrix.getExplanation(row, i), matrix.getExplanation(i, col))) || mod;
            }
        }

        return mod;
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
        return false;
    }

    @Override
    public int resolveRowIRI(String iri) {
        return reasoner.getNamingManager().getConceptId(iri);
    }

    @Override
    public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return factory.getOWLSubClassOfAxiom(
                    factory.getOWLClass(IRI.create(getIRIWithNamespace(matrix.getNamingManager().getConceptIRI(row)))),
                    factory.getOWLClass(IRI.create(getIRIWithNamespace(matrix.getNamingManager().getConceptIRI(col)))));
        }
        return null;
    }

    @Override
    public String resolveRowID(int id) {
        return reasoner.getNamingManager().getConceptIRI(id);
    }

    @Override
    public AxiomType getAxiomType() {
        return AxiomType.SUBCLASS_OF;
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLSubClassOfAxiom a = (OWLSubClassOfAxiom) axiom;

        String subClassIRI = Util.getFragment(a.getSubClass().asOWLClass().getIRI().toString());
        String superClassIRI = Util.getFragment(a.getSuperClass().asOWLClass().getIRI().toString());
        return matrix.get(subClassIRI, superClassIRI);
    }

    @Override
    public OrExpression getExplanation(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLSubClassOfAxiom a = (OWLSubClassOfAxiom) axiom;

        int subClassID = resolveRowIRI(Util.getFragment(a.getSubClass().asOWLClass().getIRI().toString()));
        int superClassID = resolveColIRI(Util.getFragment(a.getSuperClass().asOWLClass().getIRI().toString()));
        return matrix.getExplanation(subClassID, superClassID);
    }

    @Override
    public void addAxiom(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLSubClassOfAxiom a = (OWLSubClassOfAxiom) axiom;

        String subClassIRI = Util.getFragment(a.getSubClass().asOWLClass().getIRI().toString());
        String superClassIRI = Util.getFragment(a.getSuperClass().asOWLClass().getIRI().toString());
        int indexA = resolveRowIRI(subClassIRI);
        int indexB = resolveColIRI(superClassIRI);
        matrix.addExplanation(indexA, indexB, or(and(literal(axiom))));
        matrix.set(subClassIRI, superClassIRI, true);
    }
}
