package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.reasoner.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Provides interface for encapsulating simple inference steps applicable to matrix representations.
 */
public interface InferenceStepProvider {
    /**
     * Uses the given ontology for initializing the matrix. In most cases this means adding explicitly stated knowledge
     * into the matrix.
     *
     * @param ontology ontology to extract matrix initialization data from
     * @param reasoner reasoner to use
     * @param matrix   matrix to initialize using the given ontology
     */
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix);

    /**
     * Checks for inferences gained from the matrix for the given matrix field.
     *
     * @param matrix matrix to run inferencing on
     * @param col    column of matrix to check for inference
     * @param row    row of matrix to check for inference
     * @return inferred value for specified matrix field
     */
    public boolean infer(Matrix matrix, int row, int col);

    /**
     * Returns a string representation of the axiom for the given matrix field.
     *
     * @param matrix matrix to return axiom representation for
     * @param col    column of matrix to return axiom representation for
     * @param row    row of matrix to return axiom representation for
     * @return string containing the axiom representation for the given matrix field, null if the field does not
     *         represent any axiom
     */
    public String getAxiomRepresentation(Matrix matrix, int row, int col);

    /**
     * Returns the unique identifier for the axiom type generated by this inference step provider.
     *
     * @return unique identifier for axiom type generated by this inference step provider
     */
    public String getIdentifier();

    /**
     * Returns true if the resulting matrix is symmetric. In this case the matrix implementation may opt to only store
     * non-redundant values.
     *
     * @return true if resulting matrix is symmetric
     */
    public boolean isSymmetric();
}
