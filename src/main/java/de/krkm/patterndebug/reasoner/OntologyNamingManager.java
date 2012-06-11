package de.krkm.patterndebug.reasoner;

import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.Set;

/**
 * Provides mappings from URIs contained in the ontology to numeric ids and vice-versa.
 */
public class OntologyNamingManager {
    private HashMap<String, Integer> classToId = new HashMap<String, Integer>();
    private HashMap<Integer, String> idToClass = new HashMap<Integer, String>();
    private HashMap<String, Integer> propertyToId = new HashMap<String, Integer>();
    private HashMap<Integer, String> idToProperty = new HashMap<Integer, String>();
    private HashMap<String, Integer> instanceToId = new HashMap<String, Integer>();
    private HashMap<Integer, String> idToInstance = new HashMap<Integer, String>();

    /**
     * Initializes the manager with the entities contained in the given ontology
     *
     * @param ontology ontology to extract entities from
     */
    public OntologyNamingManager(OWLOntology ontology) {
        Set<OWLClass> classesInSignature = ontology.getClassesInSignature();
        OWLClass[] classes = classesInSignature.toArray(new OWLClass[classesInSignature.size()]);

        for (int i = 0; i < classes.length; i++) {
            classToId.put(classes[i].getIRI().toString(), i);
            idToClass.put(i, classes[i].getIRI().toString());
        }

        Set<OWLObjectProperty> objectPropertiesInSignature = ontology.getObjectPropertiesInSignature();
        OWLObjectProperty[] objectProperties = objectPropertiesInSignature
                .toArray(new OWLObjectProperty[objectPropertiesInSignature.size()]);

        for (int i = 0; i < objectProperties.length; i++) {
            propertyToId.put(objectProperties[i].getIRI().toString(), i);
            idToProperty.put(i, objectProperties[i].getIRI().toString());
        }

        Set<OWLNamedIndividual> instancesInSignature = ontology.getIndividualsInSignature();
        OWLNamedIndividual[] instances = instancesInSignature
                .toArray(new OWLNamedIndividual[instancesInSignature.size()]);

        for (int i = 0; i < instances.length; i++) {
            instanceToId.put(instances[i].getIRI().toString(), i);
            idToInstance.put(i, instances[i].getIRI().toString());
        }
    }

    /**
     * Returns the IRI for the given concept id
     *
     * @param id concept id to get IRI for
     * @return IRI for concept id
     */
    public String getConceptIRI(int id) {
        return idToClass.get(id);
    }

    /**
     * Returns the id for the given concept IRI.
     *
     * @param iri IRI to return concept id for
     * @return concept id for IRI
     */
    public int getConceptId(String iri) {
        return classToId.get(iri);
    }

    /**
     * Returns the IRI for the given property id
     *
     * @param id property id to get IRI for
     * @return IRI for property id
     */
    public String getPropertyIRI(int id) {
        return idToProperty.get(id);
    }

    /**
     * Returns the id for the given property IRI.
     *
     * @param iri IRI to return property id for
     * @return property id for IRI
     */
    public int getPropertyId(String iri) {
        return propertyToId.get(iri);
    }

    /**
     * Returns the IRI for the given instance id
     *
     * @param id instance id to get IRI for
     * @return IRI for instance id
     */
    public String getInstanceIRI(int id) {
        return idToInstance.get(id);
    }

    /**
     * Returns the id for the given instance IRI.
     *
     * @param iri IRI to return instance id for
     * @return instance id for IRI
     */
    public int getInstanceId(String iri) {
        return instanceToId.get(iri);
    }
}
