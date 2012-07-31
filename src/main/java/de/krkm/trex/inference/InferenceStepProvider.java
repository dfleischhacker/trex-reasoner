package de.krkm.trex.inference;

import de.krkm.trex.booleanexpressions.OrExpression;
import de.krkm.trex.reasoner.TRexReasoner;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides interface for encapsulating simple inference steps applicable to matrix representations.
 */
public abstract class InferenceStepProvider {
    protected final static Logger log = LoggerFactory.getLogger(InferenceStepProvider.class);

    /**
     * Uses the given ontology for initializing the matrix. In most cases this means adding explicitly stated knowledge
     * into the matrix.
     *
     * @param ontology ontology to extract matrix initialization data from
     * @param reasoner reasoner to use
     * @param matrix   matrix to initialize using the given ontology
     */
    public abstract void initMatrix(OWLOntology ontology, TRexReasoner reasoner, Matrix matrix);

    /**
     * Checks for inferences gained from the matrix for the given matrix field.
     *
     * @param matrix matrix to run inferencing on
     * @param col    column of matrix to check for inference
     * @param row    row of matrix to check for inference
     * @return inferred value for specified matrix field
     */
    public abstract boolean infer(Matrix matrix, int row, int col);

    /**
     * Returns a string representation of the axiom for the given matrix field.
     *
     * @param matrix matrix to return axiom representation for
     * @param row    row of matrix to return axiom representation for
     * @param col    column of matrix to return axiom representation for
     * @return string containing the axiom representation for the given matrix field, null if the field does not
     *         represent any axiom
     */
    public abstract String getAxiomRepresentation(Matrix matrix, int row, int col);

    /**
     * Returns the OWLAxiom for the given matrix cell if any, otherwise null.
     *
     * @param matrix matrix to return axiom for
     * @param row    row of matrix to return axiom for
     * @param col    column of matrix to return axiom for
     * @return OWLAxiom for the given matrix cell, null if the cell does not represent any axiom
     */
    public abstract OWLAxiom getAxiom(Matrix matrix, int row, int col);

    /**
     * Returns the unique identifier for the axiom type generated by this inference step provider.
     *
     * @return unique identifier for axiom type generated by this inference step provider
     */
    public abstract String getIdentifier();

    /**
     * Returns true if the resulting matrix is symmetric. In this case the matrix implementation may opt to only store
     * non-redundant values.
     *
     * @return true if resulting matrix is symmetric
     */
    public boolean isSymmetric() {
        return true;
    }

    /**
     * Resolves the given numerical row ID to the corresponding IRI
     */
    public abstract String resolveRowID(int id);

    /**
     * Resolves the given IRI to the corresponding row ID
     */
    public abstract int resolveRowIRI(String iri);

    /**
     * Resolves the given numerical column ID to the corresponding IRI
     */
    public String resolveColID(int id) {
        // dummy implementation for matrices using the same IDs in columns and rows
        return resolveRowID(id);
    }

    /**
     * Resolves the given IRI to the corresponding row ID
     */
    public int resolveColIRI(String iri) {
        // dummy implementation for matrices using the same IDs in columns and rows
        return resolveRowIRI(iri);
    }

    public String getIRIWithNamespace(String iri) {
        //return "http://www.semanticweb.org/daniel/ontologies/2012/5/untitled-ontology-6#" + iri;
        return iri;
    }

    /**
     * Returns the axiom type handled by this inference step provider
     *
     * @return axiom type handled by this inference step provider
     */
    public abstract AxiomType getAxiomType();

    /**
     * Checks if the given axiom is of the supported axiom type and if all used entities are non-anonymous ones.
     *
     * @param axiom axiom to check
     */
    public void isProcessable(OWLAxiom axiom) {
        if (axiom.getAxiomType() != getAxiomType()) {
            throw new UnsupportedOperationException(
                    "InferenceStepProvider unable to handle axiom type + " + axiom.getAxiomType());
        }

        for (OWLEntity e : axiom.getSignature()) {
            if (e.isOWLClass() && e.asOWLClass().isAnonymous()) {
                throw new UnsupportedOperationException("Anonymous classes are not supported");
            }

            if (e.isOWLObjectProperty() && e.asOWLObjectProperty().isAnonymous()) {
                throw new UnsupportedOperationException("Anonymous classes are not supported");
            }
        }
    }

    /**
     * Returns true if the given axiom is entailed
     *
     * @param axiom the axiom to check its entailment
     * @return true if the given axiom is entailed by the ontology
     */
    public abstract boolean isEntailed(OWLAxiom axiom);

    /**
     * Returns the explanation for the entailment of the given axiom.
     *
     * @param axiom axiom to return explanation for
     * @return explanation for given axiom
     */
    public abstract OrExpression getExplanation(OWLAxiom axiom);

    /**
     * Adds the given axiom into the matrix for this provider. Such axioms must only contain properties and/or concepts
     * which have been known previously.
     *
     * @param axiom axiom to add
     */
    public abstract void addAxiom(OWLAxiom axiom);
}