package de.krkm.patterndebug.inference;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.booleanexpressions.OrExpression;
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
    private OrExpression[][] explanations;

    /**
     * Initializes the matrix to work on the given ontology,
     *
     * @param ontology      ontology to work on
     * @param reasoner      reasoner this matrix belongs to
     * @param namingManager naming manager to resolve IRIs and IDs
     * @param inferenceStep implementing class for inference
     */
    public Matrix(OWLOntology ontology, Reasoner reasoner, OntologyNamingManager namingManager,
            InferenceStepProvider inferenceStep) {
        this.ontology = ontology;
        this.reasoner = reasoner;
        this.inferenceStep = inferenceStep;
        this.namingManager = namingManager;

//        dimension = namingManager.getNumberOfConcepts();
//        matrix = new boolean[dimensionRow][dimensionCol];
//        explanations = new OrExpression[dimensionRow][dimensionCol];

        inferenceStep.initMatrix(ontology, reasoner, this);
    }

    /**
     * Returns the row dimension of the matrix.
     *
     * @return row dimension of the matrix
     */
    public int getDimensionRow() {
        return matrix == null ? 0 : matrix.length;
    }

    /**
     * Returns the column dimension of the matrix.
     *
     * @return column dimension of the matrix
     */
    public int getDimensionCol() {
        return (matrix == null || matrix.length == 0) ? 0 : matrix[0].length;
    }

    public void setMatrix(boolean[][] matrix) {
        this.matrix = matrix;
    }

    public void setExplanations(OrExpression[][] explanations) {
        this.explanations = explanations;
    }

    /**
     * Adds the given expression as explanation for the axiom in the given matrix cell.
     *
     * @param row        row of matrix cell
     * @param col        column of matrix cell
     * @param expression explanation for axiom
     */
    public void addExplanation(int row, int col, OrExpression expression) {
        if (inferenceStep.isSymmetric() && row < col) {
            int temp = col;
            col = row;
            row = temp;
        }
        if (explanations[row][col] == null) {
            explanations[row][col] = new OrExpression();
        }
        explanations[row][col].getExpressions().addAll(expression.getExpressions());
        ExpressionMinimizer.minimize(explanations[row][col]);
    }

    /**
     * Returns the explanation for the axiom in the given matrix cell.
     *
     * @param row row of matrix cell
     * @param col column of matrix cell
     * @return clause explanation for axiom
     */
    public OrExpression getExplanation(int row, int col) {
        if (inferenceStep.isSymmetric() && row < col) {
            int temp = col;
            col = row;
            row = temp;
        }
        return explanations[row][col];
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
        int indexA = inferenceStep.resolveIRI(conceptA);
        int indexB = inferenceStep.resolveIRI(conceptB);

        if (inferenceStep.isSymmetric() && indexA < indexB) {
            int temp = indexB;
            indexB = indexA;
            indexA = temp;
        }

        if (matrix[indexA][indexB] == val) {
            return false;
        }

        matrix[indexA][indexB] = val;
        return true;
    }

    /**
     * Starts the materialization process using the inference step provider for this matrix
     */
    public void materialize() {
        boolean modified = true;

        while (modified) {
            modified = false;
            for (int i = 0; i < getDimensionRow(); i++) {
                for (int j = 0; j < getDimensionCol(); j++) {
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
        int indexA = inferenceStep.resolveIRI(conceptA);
        int indexB = inferenceStep.resolveIRI(conceptB);

        if (inferenceStep.isSymmetric() && indexA < indexB) {
            int temp = indexB;
            indexB = indexA;
            indexA = temp;
        }

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

        if (inferenceStep.isSymmetric() && indexA < indexB) {
            int temp = indexB;
            indexB = indexA;
            indexA = temp;
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
        if (inferenceStep.isSymmetric() && indexA < indexB) {
            int temp = indexB;
            indexB = indexA;
            indexA = temp;
        }
        return matrix[indexA][indexB];
    }

    /**
     * Returns the naming manager for this matrix
     *
     * @return the naming manager used for this matrix
     */
    public OntologyNamingManager getNamingManager() {
        return namingManager;
    }

    /**
     * Returns the identifier for the axiom type managed by this matrix
     *
     * @return identifier for axiom type managed by this matrix
     */
    public String getAxiomTypeIdentifier() {
        return inferenceStep.getIdentifier();
    }

    /**
     * Returns a string containing all representations of axioms contained in this matrix.
     *
     * @return string representation of all axioms contained in this matrix
     */
    public String getAxioms() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getDimensionRow(); i++) {
            for (int j = 0; j < (inferenceStep.isSymmetric() ? i : getDimensionCol()); j++) {
                String axiom = inferenceStep.getAxiomRepresentation(this, i, j);
                if (axiom != null) {
                    sb.append(axiom).append(" -- ").append(explanations[i][j].toString()).append("\n");
                }
            }
        }

        return sb.toString();
    }
}
