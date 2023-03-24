package com.smfreports.json.smf98;

import java.io.IOException;
import java.util.*;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.smf98.Smf98Record;
import com.blackhillsoftware.smf2json.cli.Smf2JsonCLI;

/**
 * Format a SMF 98 subtype 1 (z/OS) record to show the different sections
 * <p>
 * This class uses the Smf2JsonCLI class to provide a command line 
 * interface to handle input and output specified by command line 
 * options and generate the JSON. 
 *
 */
public class Smf98Subtype1Records 
{
    public static void main(String[] args) throws IOException                                   
    {
        Smf2JsonCLI cli = Smf2JsonCLI.create()
                .includeRecords(98,1)
                .description("SMF 98 subtype 1 (z/OS) Records");
        
        cli.easySmfGsonBuilder()
            .includeSectionType(true)
            .setPrettyPrinting();
                
        cli.start(new CliClient(), args);
    }

    private static class CliClient implements Smf2JsonCLI.Client
    {
        @Override
        public List<Object> onEndOfData() 
        {
            return null;
        }

        @Override
        public List<Object> processRecord(SmfRecord record)
        {
            return Collections.singletonList(Smf98Record.from(record));
        }
    }
}
