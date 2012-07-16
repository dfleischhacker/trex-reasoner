package de.krkm.patterndebug.inference.property;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.booleanexpressions.OrExpression;
import de.krkm.patterndebug.inference.InferenceStepProvider;
import de.krkm.patterndebug.inference.Matrix;
import de.krkm.patterndebug.reasoner.Reasoner;
import de.krkm.patterndebug.util.Util;
import org.semanticweb.owlapi.model.*;

import static de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer.*;

/**
 * Provides the inference step routines for property domains
 */
public class PropertyRangeInferenceStepProvider extends InferenceStepProvider {
    private Reasoner reasoner;
    private OWLDataFactory factory;
    private Matrix matrix;

    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        this.reasoner = reasoner;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.matrix = matrix;

        int dimensionCol = matrix.getNamingManager().getNumberOfConcepts();
        int dimensionRow = matrix.getNamingManager().getNumberOfProperties();
        matrix.setMatrix(new boolean[dimensionRow][dimensionCol]);
        matrix.setExplanations(new OrExpression[dimensionRow][dimensionCol]);

        for (OWLObjectPropertyRangeAxiom a : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)) {
            if (!a.getProperty().isAnonymous() && !a.getRange().isAnonymous()) {
                String propertyIRI = Util.getFragment(a.getProperty().asOWLObjectProperty().getIRI().toString());
                String rangeIRI = Util.getFragment(a.getRange().asOWLClass().getIRI().toString());
                matrix.set(propertyIRI, rangeIRI, true);
                int subId = matrix.getNamingManager().getPropertyId(propertyIRI);
                int superId = matrix.getNamingManager().getConceptId(rangeIRI);
                matrix.addExplanation(subId, superId,
                        or(and(literal(a.getAxiomWithoutAnnotations()))));
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        boolean mod = false;
        // propagate concept subsumption to property domain
        for (int i = 0; i < matrix.getDimensionCol(); i++) {
            if (matrix.get(row, i) && reasoner.getConceptSubsumption().get(col, i)) {
                matrix.set(row, col, true);
                mod = matrix.addExplanation(row, col,
                        ExpressionMinimizer.flatten(matrix.getExplanation(row, i),
                                reasoner.getConceptSubsumption().getExplanation(col, i))) || mod;
            }
        }

        // propagate property domain according to property subsumption hierarchy
        for (int i = 0; i < matrix.getDimensionRow(); i++) {
            if (matrix.get(i, col) && reasoner.getPropertySubsumption().get(row, i)) {
                matrix.set(row, col, true);
                mod = matrix.addExplanation(row, col,
                        ExpressionMinimizer.flatten(matrix.getExplanation(i,col),
                                reasoner.getPropertySubsumption().getExplanation(row,i))) || mod;
            }
        }

        return mod;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return String.format("ObjectPropertyRange(%s, %s)", matrix.getNamingManager().getPropertyIRI(row),
                    matrix.getNamingManager()
                          .getConceptIRI(col));
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "ObjectPropertyRange";
    }

    @Override
    public boolean isSymmetric() {
        return false;
    }

    @Override
    public String resolveRowID(int id) {
        return reasoner.getNamingManager().getPropertyIRI(id);
    }

    @Override
    public int resolveRowIRI(String iri) {
        return reasoner.getNamingManager().getPropertyId(iri);
    }

    @Override
    public String resolveColID(int id) {
        return reasoner.getNamingManager().getConceptIRI(id);
    }

    @Override
    public int resolveColIRI(String iri) {
        return reasoner.getNamingManager().getConceptId(iri);
    }

    @Override
    public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return factory.getOWLObjectPropertyRangeAxiom(
                    factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(matrix.getNamingManager().getPropertyIRI(row)))),
                    factory.getOWLClass(IRI.create(getIRIWithNamespace(matrix.getNamingManager().getConceptIRI(col)))));
        }
        return null;
    }

    @Override
    public AxiomType getAxiomType() {
        return AxiomType.OBJECT_PROPERTY_RANGE;
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLObjectPropertyRangeAxiom a = (OWLObjectPropertyRangeAxiom) axiom;

        String propertyIRI = Util.getFragment(a.getProperty().asOWLObjectProperty().getIRI().toString());
        String rangeIRI = Util.getFragment(a.getRange().asOWLClass().getIRI().toString());
        return matrix.get(propertyIRI, rangeIRI);
    }

    @Override
    public OrExpression getExplanation(OWLAxiom axiom) {
        isProcessable(axiom);

        OWLObjectPropertyRangeAxiom a = (OWLObjectPropertyRangeAxiom) axiom;

        int propertyID = resolveRowIRI(Util.getFragment(a.getProperty().asOWLObjectProperty().getIRI().toString()));
        int domainID = resolveColIRI(Util.getFragment(a.getRange().asOWLClass().getIRI().toString()));
        return matrix.getExplanation(propertyID, domainID);
    }
}
