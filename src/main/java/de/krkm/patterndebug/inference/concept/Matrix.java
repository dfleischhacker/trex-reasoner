package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.reasoner.OntologyNamingManager;
import de.krkm.patterndebug.reasoner.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Base class for concept level inference
 */
public class Matrix {
    private OWLOntology ontology;
    private Reasoner reasoner;
    private InferenceStepProvider inferenceStep;

    private OntologyNamingManager namingManager;

    private boolean[][] matrix;

    public Matrix(OWLOntology ontology, Reasoner reasoner, OntologyNamingManager namingManager, InferenceStepProvider inferenceStep) {
        this.ontology = ontology;
        this.reasoner = reasoner;
        this.inferenceStep = inferenceStep;
        this.namingManager = namingManager;

        matrix = new boolean[namingManager.getNumberOfConcepts()][namingManager.getNumberOfConcepts()];

        inferenceStep.initMatrix(ontology, reasoner, this);
    }

    /**
     * Returns the dimension of the matrix. The matrix is a square matrix.
     *
     * @return dimension of the matrix
     */
    public int getDimension() {
        return matrix.length;
    }

    /**
     * Sets the matrix entry for conceptA and conceptB to the given value <code>val</code>.
     *
     * @param conceptA first concept in two-concept axiom
     * @param conceptB second concept in two-concept axiom
     * @param val      value to set for concept pair
     * @return true if the value has changed, i.e., was not val before
     */
    public boolean set(String conceptA, String conceptB, boolean val) {
        int indexA = namingManager.getConceptId(conceptA);
        int indexB = namingManager.getConceptId(conceptB);

        if (matrix[indexA][indexB] == val) {
            return false;
        }

        matrix[indexA][indexB] = val;
        return true;
    }

    /**
     * Starts the materialization process
     */
    public void materialize() {
        boolean modified = true;

        while (modified) {
            modified = false;
            for (int i = 0; i < getDimension(); i++) {
                for (int j = 0; j < getDimension(); j++) {
                    modified = inferenceStep.infer(this, i, j);
                }
            }
        }
    }

    /**
     * Returns the matrix field value for the given combination of concepts
     *
     * @param conceptA first concept in two-concept axiom
     * @param conceptB second concept in two-concept axiom
     * @return value to for concept pair
     */
    public boolean get(String conceptA, String conceptB) {
        int indexA = namingManager.getConceptId(conceptA);
        int indexB = namingManager.getConceptId(conceptB);

        return matrix[indexA][indexB];
    }

    /**
     * Sets the matrix entry for conceptA and conceptB to the given value <code>val</code>.
     *
     * @param indexA index of first concept in two-concept axiom
     * @param indexB index of second concept in two-concept axiom
     * @param val    value to set for concept pair
     * @return true if the value has changed, i.e., was not val before
     */
    public boolean set(int indexA, int indexB, boolean val) {
        if (matrix[indexA][indexB] == val) {
            return false;
        }

        matrix[indexA][indexB] = val;
        return true;
    }

    /**
     * Returns the matrix field value for the given combination of concepts
     *
     * @param indexA index of first concept in two-concept axiom
     * @param indexB index of second concept in two-concept axiom
     * @return value to for concept pair
     */
    public boolean get(int indexA, int indexB) {
        return matrix[indexA][indexB];
    }

    public OntologyNamingManager getNamingManager() {
        return namingManager;
    }

    /**
     * Prints all axioms contained in the matrix
     */
    public void printAxioms() {
        for (int i = 0; i < getDimension(); i++) {
            for (int j = 0; j < getDimension(); j++) {
                inferenceStep.printAxiom(this, i, j);
            }
        }
    }
}
