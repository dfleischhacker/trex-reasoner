package de.krkm.trex.inference.property;

import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
import de.krkm.trex.booleanexpressions.OrExpression;
import de.krkm.trex.inference.InferenceStepProvider;
import de.krkm.trex.inference.Matrix;
import de.krkm.trex.reasoner.TRexReasoner;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Provides inferencing for object property incoherence caused by domain and range restrictions in combination with
 * concept disjointness.
 */
public class PropertyUnsatisfiabilityInferenceProvider extends InferenceStepProvider {
    private TRexReasoner reasoner;
    private OWLDataFactory factory;
    private Matrix matrix;

    @Override
    public void initMatrix(OWLOntology ontology, TRexReasoner reasoner, Matrix matrix) {
        this.reasoner = reasoner;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.matrix = matrix;

        // matrix contains one entry for each property, marking if the property is unsatisfiable due
        // a combination of concept disjointness and domain or range restrictions
        int dimension = matrix.getNamingManager().getNumberOfProperties();
        matrix.setMatrix(new boolean[1][dimension]);
        matrix.setExplanations(new OrExpression[1][dimension]);


        for (int k = 0; k < dimension; k++) {
            for (int i = 0; i < reasoner.conceptDisjointness.dimensionRow; i++) {
                for (int j = 0; j < i; j++) {
                    if (!reasoner.conceptDisjointness.matrix[i][j]) {
                        continue;
                    }
                    System.out.println(k + " " + i + "  " + j);
                    if (reasoner.propertyDomain.matrix[k][i] && reasoner.propertyDomain.matrix[k][j]) {
                        System.out.println("Domain");
                        matrix.matrix[0][k] = true;
                        matrix.addExplanation(0, k, ExpressionMinimizer.flatten(ExpressionMinimizer
                                .flatten(reasoner.conceptDisjointness.getExplanation(i, j),
                                        reasoner.propertyDomain.getExplanation(k, i)),
                                matrix.getExplanation(0, k) == null ? reasoner.propertyDomain.getExplanation(k, i) :
                                        ExpressionMinimizer.flatten(reasoner.propertyDomain.getExplanation(k, i),
                                                matrix.getExplanation(0, k))));
                    }
                    if (reasoner.propertyRange.matrix[k][i] && reasoner.propertyRange.matrix[k][j]) {
                        System.out.println("Range");
                        matrix.matrix[0][k] = true;
                        matrix.addExplanation(0, k,
                                ExpressionMinimizer.flatten(
                                        ExpressionMinimizer.flatten(
                                                reasoner.conceptDisjointness.getExplanation(i, j),
                                                reasoner.propertyRange.getExplanation(k, i)
                                        ),
                                        matrix.getExplanation(0, k) == null ?
                                                reasoner.propertyRange.getExplanation(k, i) :
                                                ExpressionMinimizer.flatten(
                                                        reasoner.propertyRange.getExplanation(k, i),
                                                        matrix.getExplanation(0, k)
                                                )
                                )
                        );
                    }
                }
            }

        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        return matrix.matrix[row][col];
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return String.format("DisjointObjectProperty(%s, %s)", matrix.getNamingManager().getConceptIRI(col),
                    matrix.getNamingManager().getConceptIRI(col));
        }
        return null;
    }

    @Override
    public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            HashSet<OWLObjectProperty> props = new HashSet<OWLObjectProperty>();
            props.add(factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(
                    matrix.getNamingManager().getPropertyIRI(col)))));
            props.add(factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(
                    matrix.getNamingManager().getPropertyIRI(col)))));
            return factory.getOWLDisjointObjectPropertiesAxiom(props);
        }

        return null;
    }

    @Override
    public String getIdentifier() {
        return "PropertyUnsatisfiability";
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
    public AxiomType getAxiomType() {
        return AxiomType.DISJOINT_OBJECT_PROPERTIES;
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        isProcessable(axiom);

        ArrayList<OWLObjectProperty> objectProperties = new ArrayList<OWLObjectProperty>(
                axiom.getObjectPropertiesInSignature());
        if (objectProperties.size() != 1) {
            return false;
        }

        int id = matrix.getNamingManager().getPropertyId(objectProperties.get(0).getIRI().toString());
        return matrix.matrix[0][id];
    }

    @Override
    public OrExpression getExplanation(OWLAxiom axiom) {
        isProcessable(axiom);

        ArrayList<OWLObjectProperty> objectProperties = new ArrayList<OWLObjectProperty>(
                axiom.getObjectPropertiesInSignature());
        if (objectProperties.size() != 1) {
            return null;
        }

        int id = matrix.getNamingManager().getPropertyId(objectProperties.get(0).getIRI().toString());
        return matrix.getExplanation(0, id);
    }

    @Override
    public void addAxiom(OWLAxiom axiom) {
        // do nothing since matrix does not represent any axiom directly
    }

    @Override
    public boolean isSymmetric() {
        return false;    //To change body of overridden methods use File | Settings | File Templates.
    }
}
