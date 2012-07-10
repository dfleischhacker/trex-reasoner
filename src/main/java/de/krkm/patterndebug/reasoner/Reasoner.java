package de.krkm.patterndebug.reasoner;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.booleanexpressions.OrExpression;
import de.krkm.patterndebug.inference.Matrix;
import de.krkm.patterndebug.inference.concept.ConceptDisjointnessInferenceStepProvider;
import de.krkm.patterndebug.inference.concept.PropertyDisjointnessInferenceStepProvider;
import de.krkm.patterndebug.inference.concept.SubClassOfInferenceStepProvider;
import de.krkm.patterndebug.inference.concept.SubPropertyOfInferenceStepProvider;
import de.krkm.patterndebug.inference.property.PropertyDomainInferenceStepProvider;
import de.krkm.patterndebug.inference.property.PropertyRangeInferenceStepProvider;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates the reasoning service
 *
 * @author Daniel Fleischhacker <daniel@informatik.uni-mannheim.de>
 */
public class Reasoner {
    private OWLOntology ontology;

    private OntologyNamingManager namingManager;

    private Logger log = LoggerFactory.getLogger(Reasoner.class);

    private Matrix conceptSubsumption;
    private Matrix conceptDisjointness;
    private Matrix propertySubsumption;
    private Matrix propertyDisjointness;
    private Matrix propertyDomain;
    private Matrix propertyRange;

    private HashMap<AxiomType, Matrix> typeToMatrix = new HashMap<AxiomType, Matrix>();


    /**
     * Initializes the reasoner to perform inference on the given ontology.
     *
     * @param ontology ontology to perform inference on
     */
    public Reasoner(OWLOntology ontology) {
        this.ontology = ontology;
        namingManager = new OntologyNamingManager(ontology);
        ExpressionMinimizer.setDataFactory(ontology.getOWLOntologyManager().getOWLDataFactory());
        conceptSubsumption = new Matrix(ontology, this, namingManager, new SubClassOfInferenceStepProvider());
        registerType(conceptSubsumption);
        materializeConceptSubsumption();

        conceptDisjointness = new Matrix(ontology, this, namingManager, new ConceptDisjointnessInferenceStepProvider());
        registerType(conceptDisjointness);
        materializeConceptDisjointness();

        propertySubsumption = new Matrix(ontology, this, namingManager, new SubPropertyOfInferenceStepProvider());
        registerType(propertySubsumption);
        materializePropertySubsumption();

        propertyDisjointness = new Matrix(ontology, this, namingManager,
                new PropertyDisjointnessInferenceStepProvider());
        registerType(propertyDisjointness);
        materializePropertyDisjointness();

        propertyDomain = new Matrix(ontology, this, namingManager,
                new PropertyDomainInferenceStepProvider());
        registerType(propertyDomain);
        materializePropertyDomain();

        propertyRange = new Matrix(ontology, this, namingManager,
                new PropertyRangeInferenceStepProvider());
        registerType(propertyRange);
        materializePropertyRange();


    }

    public Matrix getConceptSubsumption() {
        return conceptSubsumption;
    }

    public Matrix getConceptDisjointness() {
        return conceptDisjointness;
    }

    public Matrix getPropertySubsumption() {
        return propertySubsumption;
    }

    public Matrix getPropertyDisjointness() {
        return propertyDisjointness;
    }

    public Matrix getPropertyDomain() {
        return propertyDomain;
    }

    public Matrix getPropertyRange() {
        return propertyRange;
    }

    public void registerType(Matrix matrix) {
        typeToMatrix.put(matrix.getAxiomType(), matrix);
    }

    public boolean isEntailed(OWLAxiom axiom) {
        Matrix relevantMatrix = typeToMatrix.get(axiom.getAxiomType());
        if (relevantMatrix == null) {
            throw new UnsupportedOperationException("Reasoner unable to handle axiom type: " + axiom.getAxiomType());
        }

        return relevantMatrix.isEntailed(axiom);
    }

    public OrExpression getExplanation(OWLAxiom axiom) {
        Matrix relevantMatrix = typeToMatrix.get(axiom.getAxiomType());
        if (relevantMatrix == null) {
            throw new UnsupportedOperationException("Reasoner unable to handle axiom type: " + axiom.getAxiomType());
        }

        return relevantMatrix.getExplanation(axiom);
    }

    /**
     * Starts materialization derivable concept-subsumption axioms
     */
    public void materializeConceptSubsumption() {
        conceptSubsumption.materialize();
    }

    /**
     * Starts materializing derivable concept disjointness axioms
     */
    public void materializeConceptDisjointness() {
        conceptDisjointness.materialize();
    }

    /**
     * Starts materialization derivable property-subsumption axioms
     */
    public void materializePropertySubsumption() {
        propertySubsumption.materialize();
    }

    /**
     * Starts materializing derivable property disjointness axioms
     */
    public void materializePropertyDisjointness() {
        propertyDisjointness.materialize();
    }

    /**
     * Starts materializing derivable property disjointness axioms
     */
    public void materializePropertyDomain() {
        propertyDomain.materialize();
    }

    /**
     * Starts materializing derivable property disjointness axioms
     */
    public void materializePropertyRange() {
        propertyRange.materialize();
    }

    /**
     * Returns true if the concept identified by the IRI <code>subClass</code> is a subconcept of the concept identified
     * by the IRI <code>superClass</code>.
     *
     * @param subClass   IRI of potential subconcept
     * @param superClass IRI of potential superconcept
     * @return true if subClass is subconcept of superClass
     */
    public boolean isSubClassOf(String subClass, String superClass) {
        return conceptSubsumption.get(subClass, superClass);
    }

    /**
     * Returns true if the concept identified by the id <code>subClass</code> is a subconcept of the concept identified
     * by the id <code>superClass</code>.
     *
     * @param subClass   ID of potential subconcept
     * @param superClass ID of potential superconcept
     * @return true if subClass is subconcept of superClass
     */
    public boolean isSubClassOf(int subClass, int superClass) {
        return conceptSubsumption.get(subClass, superClass);
    }

    public boolean areDisjointClasses(String class1, String class2) {
        return conceptDisjointness.get(class1, class2);
    }

    public boolean areDisjointClasses(int class1, int class2) {
        return conceptDisjointness.get(class1, class2);
    }

    /**
     * Returns true if the concept identified by the IRI <code>subProperty</code> is a subproperty of the property
     * identified by the IRI <code>superProperty</code>.
     *
     * @param subProperty   IRI of potential subproperty
     * @param superProperty IRI of potential superproperty
     * @return true if subProperty is subproperty of superProperty
     */
    public boolean isSubPropertyOf(String subProperty, String superProperty) {
        return propertySubsumption.get(subProperty, superProperty);
    }

    /**
     * Returns true if the concept identified by the id <code>subProperty</code> is a subproperty of the property
     * identified by the id <code>superProperty</code>.
     *
     * @param subProperty   ID of potential subproperty
     * @param superProperty ID of potential superproperty
     * @return true if subProperty is subproperty of superProperty
     */
    public boolean isSubPropertyOf(int subProperty, int superProperty) {
        return propertySubsumption.get(subProperty, superProperty);
    }

    public boolean areDisjointProperties(String property1, String property2) {
        return propertyDisjointness.get(property1, property2);
    }

    public boolean areDisjointProperties(int property1, int property2) {
        return propertyDisjointness.get(property1, property2);
    }

    public OntologyNamingManager getNamingManager() {
        return namingManager;
    }

    /**
     * Returns all axioms contained in this reasoner
     *
     * @return
     */
    public Set<OWLAxiom> getAxioms() {
        HashSet<OWLAxiom> res = new HashSet<OWLAxiom>();
        res.addAll(conceptSubsumption.getOWLAxioms());
        res.addAll(conceptDisjointness.getOWLAxioms());
        res.addAll(propertySubsumption.getOWLAxioms());
        res.addAll(propertyDomain.getOWLAxioms());
        res.addAll(propertyDisjointness.getOWLAxioms());
        res.addAll(propertyRange.getOWLAxioms());

        return res;
    }
}
