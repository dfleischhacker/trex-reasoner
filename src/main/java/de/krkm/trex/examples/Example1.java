package de.krkm.trex.examples;

import de.krkm.trex.reasoner.TRexReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Set;

/**
 * Example showing the usage of TRex for getting unsatisfiable classes and properties including explanations.
 */
public class Example1 {
    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create("/home/trex/myontology.owl"));

        // init TRex with all features enabled
        TRexReasoner trex = new TRexReasoner(ontology);

        // get all unsatisfiable classes
        Set<OWLClass> incoherentClasses = trex.getUnsatisfiableClasses();

        for (OWLClass c : incoherentClasses) {
            // print explanation for unsatisfiable class
            System.out.println(trex.getUnsatisfiabilityExplanation(c));
        }

        // get all unsatisfiable object properties
        Set<OWLObjectProperty> incoherentProperties = trex.getUnsatisfiableProperties();

        for (OWLObjectProperty p : incoherentProperties) {
            // print explanation for unsatisfiable property
            System.out.println(trex.getUnsatisfiabilityExplanation(p));
        }

    }
}
