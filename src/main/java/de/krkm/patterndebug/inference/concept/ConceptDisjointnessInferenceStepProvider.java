package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.reasoner.Reasoner;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public class ConceptDisjointnessInferenceStepProvider implements InferenceStepProvider {
    private Reasoner reasoner;

    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        this.reasoner = reasoner;
        Set<OWLDisjointClassesAxiom> disjointClassesAxiomSet = ontology.getAxioms(AxiomType.DISJOINT_CLASSES);
        for (OWLDisjointClassesAxiom a : disjointClassesAxiomSet) {
            Set<OWLClass> disjointClassesSet = a.getClassesInSignature();
            OWLClass[] disjointClasses = disjointClassesSet.toArray(new OWLClass[disjointClassesSet.size()]);
            for (int i = 0; i < disjointClasses.length; i++) {
                for (int j = 0; j < disjointClasses.length; j++) {
                    if (i == j) {
                        continue;
                    }
                    if (!disjointClasses[i].isAnonymous() && !disjointClasses[j].isAnonymous()) {
                        matrix.set(disjointClasses[i].asOWLClass().getIRI().toString(),
                                disjointClasses[j].asOWLClass().getIRI().toString(), true);
                    }
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int col, int row) {
        for (int i = 0; i < matrix.getDimension(); i++) {
            if (reasoner.isSubClassOf(row, i) && matrix.get(i, col)) {
                boolean mod = matrix.set(row, col, true);
                if (mod) {
                    getAxiomRepresentation(matrix, row, col);
                }
                return mod;
            }
        }
        return false;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int col, int row) {
        if (matrix.get(row, col)) {
            return String.format("DisjointWith(<%s>, <%s>)", matrix.getNamingManager().getConceptIRI(row),
                    matrix.getNamingManager()
                          .getConceptIRI(col));
        }
        return null;
    }
}
