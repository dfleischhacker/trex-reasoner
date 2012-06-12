package de.krkm.patterndebug.ui.cmdline;

import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * Defines command line parameters usable for starting the reasoner. Options parsing is done via args4j.
 */
public class CLIOptions {
    public File getInputOntology() {
        return inputOntology;
    }

    @Option(name = "-i", usage = "Ontology to perform reasoning on", required = true)
    private File inputOntology;
}
