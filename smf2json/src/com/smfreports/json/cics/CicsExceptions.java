package com.smfreports.json.cics;

import java.io.IOException;
import java.util.*;

import com.blackhillsoftware.json.util.CompositeEntry;
import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.cics.Smf110Record;
import com.blackhillsoftware.smf.cics.monitoring.ExceptionData;
import com.blackhillsoftware.smf2json.cli.*;

/**
 * Write CICS exception records as JSON
 * 
 * <p>
 * This class uses the Smf2JsonCLI class to provide a command line 
 * interface to handle input and output specified by command line 
 * options and generate the JSON.  *
 */

public class CicsExceptions  
{
    public static void main(String[] args) throws IOException                         
    {
        Smf2JsonCLI cli = Smf2JsonCLI.create()
            .description("Convert CICS Exception Records to JSON")
            .includeRecords(110);
        
        cli.easySmfGsonBuilder()
            // exclude default formatted values for these fields and substitute a 
            // calculated value which formats them as hex strings
            .exclude(ExceptionData.class, "excmnnsx")
            .calculateEntry(ExceptionData.class, "excmnnsx", exData -> String.format("%16X", exData.excmnnsx()))
            .exclude(ExceptionData.class, "excmntrf")
            .calculateEntry(ExceptionData.class, "excmntrf", exData -> String.format("%16X", exData.excmntrf()))
            ;
        
        cli.start(new CliClient(), args);
    }
    
    private static class CliClient implements Smf2JsonCLI.Client
    {
        @Override
        public List<Object> processRecord(SmfRecord record) 
        {
            List<Object> result = new ArrayList<>();
            Smf110Record r110 = Smf110Record.from(record);
            for (ExceptionData exception : r110.exceptionData())
            {
                result.add(
                        new CompositeEntry()
                            .add("time", exception.excmnsto())
                            .add("recordtype", "CICS Exception")
                            .add("system", r110.smfsid())
                            .add("smfmnjbn", r110.mnProductSection().smfmnjbn())
                            .add("smfmnprn", r110.mnProductSection().smfmnprn())
                            .add("smfmnspn", r110.mnProductSection().smfmnspn())
                            .add(exception)
                            );
            }
            return result;
        }
        
        @Override
        public List<Object> onEndOfData() {
            System.err.println("Finished");
            return Collections.emptyList();
        }       
    }
}
