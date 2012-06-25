package de.krkm.patterndebug.reasoner;

import de.krkm.patterndebug.inference.concept.ConceptDisjointnessInferenceStepProvider;
import de.krkm.patterndebug.inference.concept.Matrix;
import de.krkm.patterndebug.inference.concept.SubClassOfInferenceStepProvider;
import de.krkm.patterndebug.inference.concept.SubPropertyOfInferenceStepProvider;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Initializes the reasoner to perform inference on the given ontology.
     *
     * @param ontology ontology to perform inference on
     */
    public Reasoner(OWLOntology ontology) {
        this.ontology = ontology;
        namingManager = new OntologyNamingManager(ontology);
        conceptSubsumption = new Matrix(ontology, this, namingManager, new SubClassOfInferenceStepProvider());
        materializeConceptSubsumption();

        conceptDisjointness = new Matrix(ontology, this, namingManager, new ConceptDisjointnessInferenceStepProvider());
        materializeConceptDisjointness();

        propertySubsumption = new Matrix(ontology, this, namingManager, new SubPropertyOfInferenceStepProvider());
        materializeConceptSubsumption();

        propertyDisjointness = new Matrix(ontology, this, namingManager,
                new ConceptDisjointnessInferenceStepProvider());
        materializeConceptDisjointness();
    }

    public Matrix getConceptSubsumption() {
        return conceptSubsumption;
    }

    public Matrix getConceptDisjointness() {
        return conceptDisjointness;
    }

    /**
     * Starts materialization derivable concept-subsumption axioms
     */
    public void materializeConceptSubsumption() {
        System.out.println("========================= BEFORE =========================");
        System.out.println(conceptSubsumption.getAxioms());
        System.out.println("========================= BEFORE =========================");
        conceptSubsumption.materialize();
        System.out.println("========================= AFTER  =========================");
        System.out.println(conceptSubsumption.getAxioms());
        System.out.println("========================= AFTER  =========================");
    }

    /**
     * Starts materializing derivable concept disjointness axioms
     */
    public void materializeConceptDisjointness() {
        System.out.println("========================= BEFORE =========================");
        System.out.println(conceptDisjointness.getAxioms());
        System.out.println("========================= BEFORE =========================");
        conceptDisjointness.materialize();
        System.out.println("========================= AFTER  =========================");
        System.out.println(conceptDisjointness.getAxioms());
        System.out.println("========================= AFTER  =========================");
    }

    /**
     * Starts materialization derivable property-subsumption axioms
     */
    public void materializePropertySubsumption() {
        System.out.println("========================= BEFORE =========================");
        System.out.println(propertySubsumption.getAxioms());
        System.out.println("========================= BEFORE =========================");
        propertySubsumption.materialize();
        System.out.println("========================= AFTER  =========================");
        System.out.println(propertySubsumption.getAxioms());
        System.out.println("========================= AFTER  =========================");
    }

    /**
     * Starts materializing derivable property disjointness axioms
     */
    public void materializePropertyDisjointness() {
        System.out.println("========================= BEFORE =========================");
        System.out.println(propertyDisjointness.getAxioms());
        System.out.println("========================= BEFORE =========================");
        propertyDisjointness.materialize();
        System.out.println("========================= AFTER  =========================");
        System.out.println(propertyDisjointness.getAxioms());
        System.out.println("========================= AFTER  =========================");
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
}
