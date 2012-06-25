package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.reasoner.Reasoner;
import de.krkm.patterndebug.util.Util;
import org.semanticweb.owlapi.model.*;

import java.util.Set;

import static de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer.*;

public class PropertyDisjointnessInferenceStepProvider implements InferenceStepProvider {
    private Reasoner reasoner;

    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        this.reasoner = reasoner;
        Set<OWLDisjointObjectPropertiesAxiom> disjointPropertyAxiomSet = ontology.getAxioms(
                AxiomType.DISJOINT_OBJECT_PROPERTIES);
        for (OWLDisjointObjectPropertiesAxiom a : disjointPropertyAxiomSet) {
            Set<OWLObjectProperty> disjointPropertySet = a.getObjectPropertiesInSignature();
            OWLObjectProperty[] disjointProperties = disjointPropertySet.toArray(
                    new OWLObjectProperty[disjointPropertySet.size()]);
            for (int i = 0; i < disjointProperties.length; i++) {
                for (int j = 0; j < disjointProperties.length; j++) {
                    if (i == j) {
                        continue;
                    }
                    if (!disjointProperties[i].isAnonymous() && !disjointProperties[j].isAnonymous()) {
                        String iriI = Util.getFragment(disjointProperties[i].asOWLClass().getIRI().toString());
                        String iriJ = Util.getFragment(disjointProperties[j].asOWLClass().getIRI().toString());
                        matrix.set(iriI, iriJ, true);
                        int idI = matrix.getNamingManager().getConceptId(iriI);
                        int idJ = matrix.getNamingManager().getConceptId(iriJ);
                        matrix.set(iriI, iriJ, true);
                        matrix.addExplanation(idI, idJ,
                                or(and(literal(String.format("DisjointObjectProperty(%s, %s)", iriI, iriJ)))));
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
                        ExpressionMinimizer.flatten(reasoner.getConceptSubsumption().getExplanation(row, i), matrix.getExplanation(i, col)));
                return mod;
            }
        }
        return false;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return String.format("DisjointObjectProperty(<%s>, <%s>)", matrix.getNamingManager().getConceptIRI(row),
                    matrix.getNamingManager().getConceptIRI(col));
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "DisjointObjectProperty";
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}