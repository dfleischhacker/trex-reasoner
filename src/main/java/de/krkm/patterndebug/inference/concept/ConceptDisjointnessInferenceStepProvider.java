package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.reasoner.Reasoner;
import de.krkm.patterndebug.util.Util;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

import static de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer.*;

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
                for (int j = 0; j < i; j++) {
                    if (i == j) {
                        continue;
                    }
                    if (!disjointClasses[i].isAnonymous() && !disjointClasses[j].isAnonymous()) {
                        String iriI = Util.getFragment(disjointClasses[i].asOWLClass().getIRI().toString());
                        String iriJ = Util.getFragment(disjointClasses[j].asOWLClass().getIRI().toString());
                        matrix.set(iriI, iriJ, true);
                        int idI = matrix.getNamingManager().getConceptId(iriI);
                        int idJ = matrix.getNamingManager().getConceptId(iriJ);
                        matrix.set(iriI, iriJ, true);
                        matrix.addExplanation(idI, idJ,
                                or(and(literal(String.format("DisjointWith(%s, %s)", iriI, iriJ)))));
                    }
                }
            }
        }
    }

    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        for (int i = 0; i < matrix.getDimension(); i++) {
            if (reasoner.isSubClassOf(row, i) && matrix.get(i, col)) {
                boolean mod = matrix.set(row, col, true);
                matrix.addExplanation(row, col,
                        ExpressionMinimizer.flatten(reasoner.getConceptSubsumption().getExplanation(row, i),
                                matrix.getExplanation(i, col)));
                return mod;
            }
        }
        return false;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return String.format("DisjointWith(<%s>, <%s>)", matrix.getNamingManager().getConceptIRI(row),
                    matrix.getNamingManager().getConceptIRI(col));
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "DisjointWith";
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public int resolveIRI(String iri) {
        return reasoner.getNamingManager().getConceptId(iri);
    }

    @Override
    public String resolveID(int id) {
        return reasoner.getNamingManager().getConceptIRI(id);
    }
}
