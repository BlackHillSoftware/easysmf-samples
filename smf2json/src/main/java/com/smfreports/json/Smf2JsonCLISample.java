package com.smfreports.json;

import java.io.*;
import java.util.*;
import com.blackhillsoftware.smf2json.cli.*;
import com.blackhillsoftware.json.util.CompositeEntry;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.*;

/**
 *
 * Sample program showing usage of Smf2JsonCLI class.
 * 
 * <p>
 * Smf2JsonCLI provides a simple framework to read SMF data and write JSON.
 * Smf2JsonCLI handles reading SMF data from a file or DD, and JSON writing to a
 * file, DD or stdout.
 * 
 * <p>
 * Smf2JsonCLI is controlled by command line arguments. It uses Apache Commons
 * CLI to set up command line arguments, generate a "Usage" message, and parse
 * the arguments provided.
 *
 */

public class Smf2JsonCLISample {
    public static void main(String[] args) throws IOException {
        // Create the Smf2JsonCLI instance, specify which record type(s) to include
        // and start processing using our CliClient class
        Smf2JsonCLI.create()
            .includeRecords(30, 5)
            .start(new CliClient(), args);
    }

    private static class CliClient implements Smf2JsonCLI.Client {
        @Override
        public List<?> processRecord(SmfRecord record) {
            Smf30Record r30 = Smf30Record.from(record);
            if (r30.completionSection() != null) {
                CompositeEntry composite = new CompositeEntry()
                        .add("time", r30.smfDateTime())
                        .add(r30.identificationSection())
                        .add(r30.completionSection());

                return Collections.singletonList(composite);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public List<?> onEndOfData() {
            return null;
        }
    }
}
