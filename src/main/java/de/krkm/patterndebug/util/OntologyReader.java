package de.krkm.patterndebug.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Utility class for loading an ontology using the OWL API
 */
public class OntologyReader {

    public static OWLOntology loadOntology(FileInputStream input) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(input);

        return ontology;
    }

    public static OWLOntology loadOntology(File file) throws FileNotFoundException, OWLOntologyCreationException {
        return loadOntology(new FileInputStream(file));
    }

    public static OWLOntology loadOntology(String fileName) throws FileNotFoundException, OWLOntologyCreationException {
        return loadOntology(new FileInputStream(fileName));
    }
}
