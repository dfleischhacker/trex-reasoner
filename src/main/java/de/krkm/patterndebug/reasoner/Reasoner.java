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
        materializeConceptInferences();

        conceptDisjointness = new Matrix(ontology, this, namingManager, new ConceptDisjointnessInferenceStepProvider());
        materializeDisjointnessInferences();
    }

    /**
     * Starts materialization of concept-level inference
     */
    public void materializeConceptInferences() {
        System.out.println("========================= BEFORE =========================");
        conceptSubsumption.printAxioms();
        System.out.println("========================= BEFORE =========================");
        conceptSubsumption.materialize();
        System.out.println("========================= AFTER  =========================");
        conceptSubsumption.printAxioms();
        System.out.println("========================= AFTER  =========================");
    }

    public void materializeDisjointnessInferences() {
        System.out.println("========================= BEFORE =========================");
        conceptDisjointness.printAxioms();
        System.out.println("========================= BEFORE =========================");
        conceptDisjointness.materialize();
        System.out.println("========================= AFTER  =========================");
        conceptDisjointness.printAxioms();
        System.out.println("========================= AFTER  =========================");
    }

    public boolean isSubClassOf(String subClass, String superClass) {
        return conceptSubsumption.get(subClass, superClass);
    }

    public boolean isSubClassOf(int subClass, int superClass) {
        return conceptSubsumption.get(subClass, superClass);
    }
}
