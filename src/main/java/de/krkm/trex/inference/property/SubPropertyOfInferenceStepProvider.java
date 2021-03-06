package de.krkm.trex.inference.property;

import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
import de.krkm.trex.booleanexpressions.OrExpression;
import de.krkm.trex.inference.InferenceStepProvider;
import de.krkm.trex.inference.Matrix;
import de.krkm.trex.reasoner.TRexReasoner;
import de.krkm.trex.util.Util;
import org.semanticweb.owlapi.model.*;

import static de.krkm.trex.booleanexpressions.ExpressionMinimizer.*;

/**
 * Implements the inference step for SubClassOf axioms.
 */
public class SubPropertyOfInferenceStepProvider extends InferenceStepProvider {
    private TRexReasoner reasoner;
    private OWLDataFactory factory;
    private Matrix matrix;
    private boolean generateExplanations;

    @Override
    public void initMatrix(OWLOntology ontology, TRexReasoner reasoner, Matrix matrix) {
        int dimension = matrix.getNamingManager().getNumberOfProperties();
        matrix.setMatrix(new boolean[dimension][dimension]);
        this.generateExplanations = reasoner.isGenerateExplanations();

        if (generateExplanations) {
            matrix.setExplanations(new OrExpression[dimension][dimension]);
        }

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
                if (generateExplanations) {
                    int subId = matrix.getNamingManager().getPropertyId(subPropertyIRI);
                    int superId = matrix.getNamingManager().getPropertyId(superPropertyIRI);
                    matrix.addExplanation(subId, superId,
                            or(and(literal(a.getAxiomWithoutAnnotations()))));
                }
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
                    if (generateExplanations) {
                        int subId = matrix.getNamingManager().getPropertyId(subPropertyIRI);
                        int superId = matrix.getNamingManager().getPropertyId(superPropertyIRI);
                        matrix.addExplanation(subId, superId,
                                or(and(literal(a))));
                    }
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        boolean mod = false;
        for (int i = 0; i < matrix.dimensionRow; i++) {
            if (matrix.matrix[row][i] && matrix.matrix[i][col]) {
                mod = matrix.set(row, col, true) || mod;
                if (generateExplanations) {
                    mod = matrix.addExplanation(row, col, ExpressionMinimizer
                            .flatten(matrix.getExplanation(row, i), matrix.getExplanation(i, col))) || mod;
                }
            }
        }

        return mod;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.matrix[row][col]) {
            return String.format("SubPropertyOf(%s, %s)", matrix.getNamingManager().getPropertyIRI(row),
                    matrix.getNamingManager()
                            .getPropertyIRI(col));
        }
        return null;
    }

    @Override
    public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
        if (matrix.matrix[row][col]) {
            return factory.getOWLSubObjectPropertyOfAxiom(
                    factory.getOWLObjectProperty(
                            IRI.create(getIRIWithNamespace(matrix.getNamingManager().getPropertyIRI(row)))),
                    factory.getOWLObjectProperty(
                            IRI.create(getIRIWithNamespace(matrix.getNamingManager().getPropertyIRI(col)))));
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
        if (!generateExplanations) {
            throw new UnsupportedOperationException(
                    "Trying to retrieve explanations from an reasoner with disabled explanation support");
        }
        isProcessable(axiom);

        OWLSubObjectPropertyOfAxiom a = (OWLSubObjectPropertyOfAxiom) axiom;

        int subClassID = resolveRowIRI(Util.getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString()));
        int superClassID = resolveColIRI(
                Util.getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString()));

        return matrix.getExplanation(subClassID, superClassID);
    }

    @Override
    public void addAxiom(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLSubObjectPropertyOfAxiom a = (OWLSubObjectPropertyOfAxiom) axiom;

        String subClassIRI = Util.getFragment(a.getSubProperty().asOWLObjectProperty().getIRI().toString());
        String superClassIRI = Util.getFragment(a.getSuperProperty().asOWLObjectProperty().getIRI().toString());
        matrix.set(subClassIRI, superClassIRI, true);
        int indexA = resolveRowIRI(subClassIRI);
        int indexB = resolveColIRI(superClassIRI);

        if (generateExplanations) {
            matrix.addExplanation(indexA, indexB, or(and(literal(axiom))));
        }
    }
}
