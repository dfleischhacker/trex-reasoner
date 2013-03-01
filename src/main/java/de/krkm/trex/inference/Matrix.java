package de.krkm.trex.inference;

import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
import de.krkm.trex.booleanexpressions.OrExpression;
import de.krkm.trex.reasoner.OntologyNamingManager;
import de.krkm.trex.reasoner.TRexReasoner;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for concept level inference
 */
public class Matrix {
    private OWLOntology ontology;
    private TRexReasoner reasoner;
    private InferenceStepProvider inferenceStep;

    private OntologyNamingManager namingManager;

    public boolean[][] matrix;
    private OrExpression[][] explanations;

    private final static Logger log = LoggerFactory.getLogger(Matrix.class);

    private boolean isSymmetric;
    public int dimensionRow;
    public int dimensionCol;

    private boolean generateExplanations;

    /**
     * Initializes the matrix to work on the given ontology with explanation support.
     *
     * @param ontology      ontology to work on
     * @param reasoner      reasoner this matrix belongs to
     * @param namingManager naming manager to resolve IRIs and IDs
     * @param inferenceStep implementing class for inference
     */
    public Matrix(OWLOntology ontology, TRexReasoner reasoner, OntologyNamingManager namingManager,
            InferenceStepProvider inferenceStep) {
        this(ontology, reasoner, namingManager, inferenceStep, true);
    }

    /**
     * Initializes the matrix to work on the given ontology,
     *
     * @param ontology             ontology to work on
     * @param reasoner             reasoner this matrix belongs to
     * @param namingManager        naming manager to resolve IRIs and IDs
     * @param inferenceStep        implementing class for inference
     * @param generateExplanations if true explanations are generated, otherwise no explanations are available
     */
    public Matrix(OWLOntology ontology, TRexReasoner reasoner, OntologyNamingManager namingManager,
            InferenceStepProvider inferenceStep, boolean generateExplanations) {
        this.ontology = ontology;
        this.reasoner = reasoner;
        this.inferenceStep = inferenceStep;
        this.namingManager = namingManager;
        this.isSymmetric = inferenceStep.isSymmetric();
        this.generateExplanations = generateExplanations;

//        dimension = namingManager.getNumberOfConcepts();
//        matrix = new boolean[dimensionRow][dimensionCol];
//        explanations = new OrExpression[dimensionRow][dimensionCol];

        log.debug("Initializing matrix {}", inferenceStep.getIdentifier());
        inferenceStep.initMatrix(ontology, reasoner, this);
        log.debug("Done initializing matrix {}", inferenceStep.getIdentifier());
    }

    /**
     * Returns the row dimension of the matrix.
     *
     * @return row dimension of the matrix
     * @deprecated use public member dimensionRow instead
     */
    @Deprecated
    public int getDimensionRow() {
        return matrix == null ? 0 : matrix.length;
    }

    /**
     * Returns the column dimension of the matrix.
     *
     * @return column dimension of the matrix
     * @deprecated use public member dimensionCol instead
     */
    @Deprecated
    public int getDimensionCol() {
        return dimensionCol;
    }

    public void setMatrix(boolean[][] matrix) {
        this.matrix = matrix;
        this.dimensionRow = matrix.length;
        this.dimensionCol = matrix[0].length;
    }

    public void setExplanations(OrExpression[][] explanations) {
        if (!generateExplanations) {
            return;
        }
        this.explanations = explanations;
    }

    /**
     * Adds the given expression as explanation for the axiom in the given matrix cell.
     *
     * @param row        row of matrix cell
     * @param col        column of matrix cell
     * @param expression explanation for axiom
     * @return true if the explanation for the given cell has changed, otherwise false
     */
    public boolean addExplanation(int row, int col, OrExpression expression) {
        if (!generateExplanations) {
            return false;
        }
        if (isSymmetric && row < col) {
            int temp = col;
            col = row;
            row = temp;
        }
        if (explanations[row][col] == null) {
            explanations[row][col] = new OrExpression();
        }
        OrExpression prev = explanations[row][col].copy();
        explanations[row][col].getExpressions().addAll(expression.getExpressions());
        ExpressionMinimizer.minimize(explanations[row][col], expression);
        return !prev.equals(explanations[row][col]);
    }

    /**
     * Returns the explanation for the axiom in the given matrix cell.
     *
     * @param row row of matrix cell
     * @param col column of matrix cell
     * @return clause explanation for axiom
     */
    public OrExpression getExplanation(int row, int col) {
        if (!generateExplanations) {
            throw new UnsupportedOperationException(
                    "Trying to retrieve explanations from an reasoner with disabled explanation support");
        }
        if (isSymmetric && row < col) {
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
        int indexA = inferenceStep.resolveRowIRI(conceptA);
        int indexB = inferenceStep.resolveColIRI(conceptB);

        if (isSymmetric && indexA < indexB) {
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
        log.debug("Materializing matrix {}", inferenceStep.getIdentifier());
        boolean modified = true;

        int run = 0;
        while (modified) {
            log.debug("Inference for {}, iteration {}", inferenceStep.getIdentifier(), run++);
            modified = false;
            for (int i = 0; i < dimensionRow; i++) {
                for (int j = 0; j < dimensionCol; j++) {
                    modified = inferenceStep.infer(this, i, j) || modified;
                }
            }
        }

        log.debug("Done materializing matrix {}", inferenceStep.getIdentifier());
    }

    /**
     * Adds the given axiom to this matrix. The axiom must not introduce new concepts or properties but may only add
     * knowledge about already existing ones.
     *
     * @param axiom axiom to add
     */
    public void addAxiom(OWLAxiom axiom) {
        inferenceStep.addAxiom(axiom);
    }

    /**
     * Returns the matrix field value for the given combination of concepts
     *
     * @param conceptA first concept in two-concept axiom
     * @param conceptB second concept in two-concept axiom
     * @return value to for concept pair
     */
    public boolean get(String conceptA, String conceptB) {
        int indexA = inferenceStep.resolveRowIRI(conceptA);
        int indexB = inferenceStep.resolveColIRI(conceptB);

        if (isSymmetric && indexA < indexB) {
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
        if (isSymmetric && indexA < indexB) {
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
     * Returns the matrix field value for the given combination of concepts
     *
     * @param indexA index of first concept in two-concept axiom
     * @param indexB index of second concept in two-concept axiom
     * @return value to for concept pair
     */
    public boolean get(int indexA, int indexB) {
        if (isSymmetric && indexA < indexB) {
            return matrix[indexB][indexA];
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
        for (int i = 0; i < dimensionRow; i++) {
            for (int j = 0; j < (isSymmetric ? i : dimensionCol); j++) {
                String axiom = inferenceStep.getAxiomRepresentation(this, i, j);
                if (axiom != null) {
                    sb.append(axiom).append(" -- ").append(explanations[i][j].toString()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Returns the set of all axioms contained in this matrix
     */
    public Set<OWLAxiom> getOWLAxioms() {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (int i = 0; i < dimensionRow; i++) {
            for (int j = 0; j < (isSymmetric ? i : dimensionCol); j++) {
                OWLAxiom axiom = inferenceStep.getAxiom(this, i, j);
                if (axiom != null) {
                    axioms.add(axiom);
                }
            }
        }

        return axioms;
    }

    /**
     * Returns the axiom type handled by this matrix
     *
     * @return axiom type handled by this matrix
     */
    public AxiomType getAxiomType() {
        return inferenceStep.getAxiomType();
    }

    /**
     * Returns true if the given axiom is entailed by this matrix
     */
    public boolean isEntailed(OWLAxiom axiom) {
        return inferenceStep.isEntailed(axiom);
    }

    /**
     * Returns the explanation for the entailment of the given axiom
     */
    public OrExpression getExplanation(OWLAxiom axiom) {
        return inferenceStep.getExplanation(axiom);
    }
}
