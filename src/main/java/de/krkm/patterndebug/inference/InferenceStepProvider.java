package de.krkm.patterndebug.inference;

import de.krkm.patterndebug.reasoner.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Provides interface for encapsulating simple inference steps applicable to matrix representations.
 */
public abstract class InferenceStepProvider {
    /**
     * Uses the given ontology for initializing the matrix. In most cases this means adding explicitly stated knowledge
     * into the matrix.
     *
     * @param ontology ontology to extract matrix initialization data from
     * @param reasoner reasoner to use
     * @param matrix   matrix to initialize using the given ontology
     */
    public abstract void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix);

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
        return "http://www.semanticweb.org/daniel/ontologies/2012/5/untitled-ontology-6#" + iri;
    }
}
