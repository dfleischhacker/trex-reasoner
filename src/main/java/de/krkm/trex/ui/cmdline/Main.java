package de.krkm.trex.ui.cmdline;


import de.krkm.trex.reasoner.Reasoner;
import de.krkm.trex.util.OntologyReader;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException {
        Logger log = LoggerFactory.getLogger(Main.class);

        log.debug("Parse command line");
        CLIOptions options = new CLIOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException e) {
            System.out.println(e.getMessage());
            parser.printUsage(System.out);
            return;
        }
        log.info("Read ontology '{}'", options.getInputOntology().getAbsolutePath());
        OWLOntology ontology = OntologyReader.loadOntology(options.getInputOntology());
        log.debug("Done loading ontology");
        log.debug("Initialize reasoner");
        Reasoner r = new Reasoner(ontology);
        log.debug("Done initializing reasoner");

        // search for concepts disjoint to themselves
        log.info("Searching for concepts disjoint to themselves");
        for (int i = 0; i < r.getConceptDisjointness().getDimensionRow(); i++) {
            if (r.getConceptDisjointness().getExplanation(i, i).getExpressions().size() > 1) {
                System.out.println(
                        r.getNamingManager().getConceptIRI(i) + ": " + r.getConceptDisjointness().getExplanation(i, i));
            }
        }
    }
}
