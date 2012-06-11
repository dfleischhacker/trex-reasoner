package de.krkm.patterndebug.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Encapsulates the reasoning service
 * @author Daniel Fleischhacker <daniel@informatik.uni-mannheim.de>
 */
public class Reasoner {
    private OWLOntology ontology;

    /**
     * Initializes the reasoner to perform inference on the given ontology.
     *
     * @param ontology ontology to perform inference on
     */
    public Reasoner(OWLOntology ontology) {
        this.ontology = ontology;


    }

    /**
     * Starts materialization of concept-level inference
     */
    public void materializeConceptInferences() {

    }
}
