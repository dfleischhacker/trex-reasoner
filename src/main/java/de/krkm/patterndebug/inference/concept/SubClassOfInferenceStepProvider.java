package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.reasoner.Reasoner;
import org.semanticweb.owlapi.model.*;

import java.util.Set;

/**
 * Implements the inference step for SubClassOf axioms.
 */
public class SubClassOfInferenceStepProvider implements InferenceStepProvider {
    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        // stated subsumption
        for (OWLSubClassOfAxiom a : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            if (!a.getSubClass().isAnonymous() && !a.getSuperClass().isAnonymous()) {
                matrix.set(a.getSubClass().asOWLClass().getIRI().toString(),
                        a.getSuperClass().asOWLClass().getIRI().toString(),
                        true);
            }
        }

        // stated class equivalence
        for (OWLEquivalentClassesAxiom a : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
            Set<OWLClass> equivalentClassesSet = a.getNamedClasses();
            OWLClass[] equivalentClasses = equivalentClassesSet.toArray(new OWLClass[equivalentClassesSet.size()]);

            for (int i = 0; i < equivalentClasses.length; i++) {
                for (int j = 0; j < equivalentClasses.length; j++) {
                    if (i == j) {
                        continue;
                    }
                    matrix.set(equivalentClasses[i].asOWLClass().getIRI().toString(),
                            equivalentClasses[j].asOWLClass().getIRI().toString(),
                            true);
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int col, int row) {
        for (int i = 0; i < matrix.getDimension(); i++) {
            if (matrix.get(row, i) && matrix.get(i, col)) {
                boolean mod = matrix.set(row, col, true);
                if (mod) {
                    printAxiom(matrix, row, col);
                }
                return mod;
            }
        }

        return false;
    }

    @Override
    public void printAxiom(Matrix matrix, int col, int row) {
        if (matrix.get(row, col)) {
            System.out.println(matrix.getNamingManager().getConceptIRI(row) + " subClassOf " + matrix.getNamingManager()
                                                                                                     .getConceptIRI(
                                                                                                             col));
        }
    }


}
