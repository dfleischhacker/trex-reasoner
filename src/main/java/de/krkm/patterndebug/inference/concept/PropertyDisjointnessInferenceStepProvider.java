package de.krkm.patterndebug.inference.concept;

import de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer;
import de.krkm.patterndebug.booleanexpressions.OrExpression;
import de.krkm.patterndebug.inference.InferenceStepProvider;
import de.krkm.patterndebug.inference.Matrix;
import de.krkm.patterndebug.reasoner.Reasoner;
import de.krkm.patterndebug.util.Util;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

import static de.krkm.patterndebug.booleanexpressions.ExpressionMinimizer.*;

public class PropertyDisjointnessInferenceStepProvider extends InferenceStepProvider {
    private Reasoner reasoner;

    @Override
    public void initMatrix(OWLOntology ontology, Reasoner reasoner, Matrix matrix) {
        int dimension = matrix.getNamingManager().getNumberOfConcepts();
        matrix.setMatrix(new boolean[dimension][dimension]);
        matrix.setExplanations(new OrExpression[dimension][dimension]);

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
                        int idI = matrix.getNamingManager().getPropertyId(iriI);
                        int idJ = matrix.getNamingManager().getPropertyId(iriJ);
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
        boolean mod = false;
        for (int i = 0; i < matrix.getDimensionRow(); i++) {
            if (reasoner.isSubClassOf(row, i) && matrix.get(i, col)) {
                matrix.set(row, col, true);
                mod = matrix.addExplanation(row, col,
                        ExpressionMinimizer.flatten(reasoner.getConceptSubsumption().getExplanation(row, i),
                                matrix.getExplanation(i, col))) || mod;
            }
        }
        return mod;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return String.format("DisjointObjectProperty(%s, %s)", matrix.getNamingManager().getConceptIRI(row),
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


    @Override
    public int resolveRowIRI(String iri) {
        return reasoner.getNamingManager().getPropertyId(iri);
    }

    @Override
    public String resolveRowID(int id) {
        return reasoner.getNamingManager().getPropertyIRI(id);
    }
}
