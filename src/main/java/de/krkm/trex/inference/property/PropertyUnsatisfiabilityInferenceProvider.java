package de.krkm.trex.inference.property;

import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
import de.krkm.trex.booleanexpressions.OrExpression;
import de.krkm.trex.inference.InferenceStepProvider;
import de.krkm.trex.inference.Matrix;
import de.krkm.trex.reasoner.TRexReasoner;
import org.semanticweb.owlapi.model.*;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Provides inferencing for object property incoherence caused by domain and range restrictions in combination with
 * concept disjointness.
 */
public class PropertyUnsatisfiabilityInferenceProvider extends InferenceStepProvider {
    private TRexReasoner reasoner;
    private OWLDataFactory factory;
    private Matrix matrix;

    @Override
    public void initMatrix(OWLOntology ontology, TRexReasoner reasoner, Matrix matrix) {
        FileWriter out = null;
        try {
            out = new FileWriter("/home/daniel/propunsat.log");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.reasoner = reasoner;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.matrix = matrix;

        // matrix contains one entry for each property, marking if the property is unsatisfiable due
        // a combination of concept disjointness and domain or range restrictions
        int dimension = matrix.getNamingManager().getNumberOfProperties();
        matrix.setMatrix(new boolean[1][dimension]);
        matrix.setExplanations(new OrExpression[1][dimension]);


        // create list of disjoint concepts
        ArrayList<Integer[]> disjointConcepts = new ArrayList<Integer[]>();
        for (int i = 0; i < reasoner.conceptDisjointness.dimensionRow; i++) {
            for (int j = 0; j < i; j++) {
                if (reasoner.conceptDisjointness.matrix[i][j]) {
                    disjointConcepts.add(new Integer[]{i, j});
                }
            }
        }

        for (int k = 0; k < dimension; k++) {
            for (Integer[] cood : disjointConcepts) {
                int i = cood[0];
                int j = cood[1];
                if (reasoner.propertyDomain.matrix[k][i] && reasoner.propertyDomain.matrix[k][j]) {
                    matrix.matrix[0][k] = true;
                    OrExpression curExplanation = matrix.getExplanation(0, k);
                    try {
                        out.write("************************************* Domain ******************\n" +
                                "Unsat Prop: " + reasoner.getNamingManager().getPropertyIRI(k) + "\n" +
                                "Previous explanation:" + String
                                        .valueOf(curExplanation) + "\n");

                        out.write("\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    OrExpression explDisjoint = reasoner.conceptDisjointness.getExplanation(i, j);
                    OrExpression explDomainKI = reasoner.propertyDomain.getExplanation(k, i);
                    OrExpression explDomainKJ = reasoner.propertyDomain.getExplanation(k, j);


                    OrExpression flattenedDomain = ExpressionMinimizer
                            .flatten(explDomainKJ,
                                    explDomainKI);
                    OrExpression flattenedDomainDisjoint = ExpressionMinimizer.flatten(flattenedDomain,
                            explDisjoint);
                    try {
                        out.write("Disjoint Expl: " + explDisjoint + "\n");
                        out.write("Domain KI Expl: " + explDomainKI + "\n");
                        out.write("Domain KJ Expl: " + explDomainKJ + "\n");
                        out.write("Flattened Domain: " + flattenedDomain + "\n");
                        out.write("Flattened DD: " + flattenedDomainDisjoint + "\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    matrix.addExplanation(0, k, flattenedDomainDisjoint);
                    try {
                        out.write("Resulting Explanation: " + matrix.getExplanation(0, k));
                        out.write("\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                if (reasoner.propertyRange.matrix[k][i] && reasoner.propertyRange.matrix[k][j]) {
                    matrix.matrix[0][k] = true;
                    OrExpression curExplanation = matrix.getExplanation(0, k);
                    try {
                        out.write("************************************* Range *********************\n" +
                                "Unsat Prop: " + reasoner.getNamingManager().getPropertyIRI(k) + "\n" +
                                "Previous explanation:" + String
                                .valueOf(curExplanation) + "\n");

                        out.write("\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    OrExpression explDisjoint = reasoner.conceptDisjointness.getExplanation(i, j);
                    OrExpression explRangeKI = reasoner.propertyRange.getExplanation(k, i);
                    OrExpression explRangeKJ = reasoner.propertyRange.getExplanation(k, j);


                    OrExpression flattenedRange = ExpressionMinimizer
                            .flatten(explRangeKJ,
                                    explRangeKI);
                    OrExpression flattenedRangeDisjoint = ExpressionMinimizer.flatten(flattenedRange,
                            explDisjoint);
                    try {
                        out.write("Disjoint Expl: " + explDisjoint + "\n");
                        out.write("Range KI Expl: " + explRangeKI + "\n");
                        out.write("Range KJ Expl: " + explRangeKJ + "\n");
                        out.write("Flattened Range: " + flattenedRange + "\n");
                        out.write("Flattened RD: " + flattenedRangeDisjoint + "\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    matrix.addExplanation(0, k, flattenedRangeDisjoint);
                    try {
                        out.write("Resulting Explanation: " + matrix.getExplanation(0, k));
                        out.write("\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
        if (out != null) {
            try {
                out.close();
            }
            catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    @Override
    public boolean infer(Matrix matrix, int row, int col) {
        return false;
    }

    @Override
    public String getAxiomRepresentation(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            return String.format("DisjointObjectProperty(%s, %s)", matrix.getNamingManager().getConceptIRI(col),
                    matrix.getNamingManager().getConceptIRI(col));
        }
        return null;
    }

    @Override
    public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
        if (matrix.get(row, col)) {
            HashSet<OWLObjectProperty> props = new HashSet<OWLObjectProperty>();
            props.add(factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(
                    matrix.getNamingManager().getPropertyIRI(col)))));
            props.add(factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(
                    matrix.getNamingManager().getPropertyIRI(col)))));
            return factory.getOWLDisjointObjectPropertiesAxiom(props);
        }

        return null;
    }

    @Override
    public String getIdentifier() {
        return "PropertyUnsatisfiability";
    }

    @Override
    public String resolveRowID(int id) {
        return reasoner.getNamingManager().getPropertyIRI(id);
    }

    @Override
    public int resolveRowIRI(String iri) {
        return reasoner.getNamingManager().getPropertyId(iri);
    }

    @Override
    public AxiomType getAxiomType() {
        return AxiomType.DISJOINT_OBJECT_PROPERTIES;
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        isProcessable(axiom);

        ArrayList<OWLObjectProperty> objectProperties = new ArrayList<OWLObjectProperty>(
                axiom.getObjectPropertiesInSignature());
        if (objectProperties.size() != 1) {
            return false;
        }

        int id = matrix.getNamingManager().getPropertyId(objectProperties.get(0).getIRI().toString());
        return matrix.matrix[0][id];
    }

    @Override
    public OrExpression getExplanation(OWLAxiom axiom) {
        isProcessable(axiom);

        ArrayList<OWLObjectProperty> objectProperties = new ArrayList<OWLObjectProperty>(
                axiom.getObjectPropertiesInSignature());
        if (objectProperties.size() != 1) {
            return null;
        }

        int id = matrix.getNamingManager().getPropertyId(objectProperties.get(0).getIRI().toString());
        return matrix.getExplanation(0, id);
    }

    @Override
    public void addAxiom(OWLAxiom axiom) {
        // do nothing since matrix does not represent any axiom directly
    }

    @Override
    public boolean isSymmetric() {
        return false;    //To change body of overridden methods use File | Settings | File Templates.
    }
}
