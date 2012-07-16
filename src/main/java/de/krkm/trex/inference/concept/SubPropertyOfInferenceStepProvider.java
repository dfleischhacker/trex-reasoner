package de.krkm.trex.inference.concept;

import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
import de.krkm.trex.booleanexpressions.OrExpression;
import de.krkm.trex.inference.InferenceStepProvider;
import de.krkm.trex.inference.Matrix;
import de.krkm.trex.reasoner.Reasoner;
import de.krkm.trex.util.Util;
import org.semanticweb.owlapi.model.*;

import static de.krkm.trex.booleanexpressions.ExpressionMinimizer.*;

/**
 * Implements the inference step for SubClassOf axioms.
 */
public class SubPropertyOfInferenceStepProvider extends InferenceStepProvider {
    private Reasoner reasoner;
    private OWLDataFactory factory;
    private Matrix matrix;

    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        int dimension = matrix.getNamingManager().getNumberOfProperties();
        matrix.setMatrix(new boolean[dimension][dimension]);
        matrix.setExplanations(new OrExpression[dimension][dimension]);

        this.matrix = matrix;

        this.reasoner = reasoner;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        // stated subsumption
        for (OWLSubObjectPropertyOfAxiom a : ontology.getAxioms(AxiomType.SUB_OBJECT_PROPERTY)) {
            if (!a.getSubProperty().isAnonymous() && !a.getSuperProperty().isAnonymous()) {
                String subPropertyIRI = Util.getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString());
                String superPropertyIRI = Util
                        .getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString());
                matrix.set(subPropertyIRI, superPropertyIRI, true);
                int subId = matrix.getNamingManager().getPropertyId(subPropertyIRI);
                int superId = matrix.getNamingManager().getPropertyId(superPropertyIRI);
                matrix.addExplanation(subId, superId,
                        or(and(literal(a.getAxiomWithoutAnnotations()))));
            }
        }

        // stated class equivalence
        for (OWLEquivalentObjectPropertiesAxiom equiv : ontology.getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES)) {
            for (OWLSubObjectPropertyOfAxiom a : equiv.asSubObjectPropertyOfAxioms()) {
                if (!a.getSubProperty().isAnonymous() && !a.getSuperProperty().isAnonymous()) {
                    String subPropertyIRI = Util
                            .getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString());
                    String superPropertyIRI = Util
                            .getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString());
                    matrix.set(subPropertyIRI, superPropertyIRI, true);
                    int subId = matrix.getNamingManager().getPropertyId(subPropertyIRI);
                    int superId = matrix.getNamingManager().getPropertyId(superPropertyIRI);
                    matrix.addExplanation(subId, superId,
                            or(and(literal(a))));
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        boolean mod = false;
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
            return String.format("SubPropertyOf(%s, %s)", matrix.getNamingManager().getPropertyIRI(row),
                    matrix.getNamingManager()
                          .getPropertyIRI(col));
        }
        return null;
    }

    @Override
    public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return factory.getOWLSubObjectPropertyOfAxiom(
                    factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(matrix.getNamingManager().getPropertyIRI(row)))),
                    factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(matrix.getNamingManager().getPropertyIRI(col)))));
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

    @Override
    public int resolveRowIRI(String iri) {
        return reasoner.getNamingManager().getPropertyId(iri);
    }

    @Override
    public String resolveRowID(int id) {
        return reasoner.getNamingManager().getPropertyIRI(id);
    }

    @Override
    public AxiomType getAxiomType() {
        return AxiomType.SUB_OBJECT_PROPERTY;
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLSubObjectPropertyOfAxiom a = (OWLSubObjectPropertyOfAxiom) axiom;

        String subClassIRI = Util.getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString());
        String superClassIRI = Util.getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString());
        return matrix.get(subClassIRI, superClassIRI);
    }

    @Override
    public OrExpression getExplanation(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLSubObjectPropertyOfAxiom a = (OWLSubObjectPropertyOfAxiom) axiom;

        int subClassID = resolveRowIRI(Util.getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString()));
        int superClassID = resolveColIRI(
                Util.getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString()));

        return matrix.getExplanation(subClassID, superClassID);
    }
}
