package de.krkm.patterndebug.reasoner;

import de.krkm.patterndebug.inference.concept.ConceptDisjointnessInferenceStepProvider;
import de.krkm.patterndebug.inference.concept.Matrix;
import de.krkm.patterndebug.inference.concept.SubClassOfInferenceStepProvider;
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
     * Returns true if the concept identified by the IRI <code>subClass</code> is a subconcept of the concept identified
     * by the IRI <code>superClass</code>.
     *
     * @param subClass IRI of potential subconcept
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
     * @param subClass ID of potential subconcept
     * @param superClass ID of potential superconcept
     * @return true if subClass is subconcept of superClass
     */
    public boolean isSubClassOf(int subClass, int superClass) {
        return conceptSubsumption.get(subClass, superClass);
    }
}
