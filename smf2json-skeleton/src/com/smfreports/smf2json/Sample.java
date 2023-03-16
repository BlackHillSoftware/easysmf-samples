package com.smfreports.smf2json;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf2json.cli.*;

/**
 * 
 * Skeleton program to use Smf2JsonCLI 
 *
 */

public class Sample 
{         
    public static void main(String[] args) throws IOException                                   
    {
        Smf2JsonCLI smf2JsonCli = Smf2JsonCLI.create()
            .description("Sample SMF 2 JSON program")
            .includeRecords(30); // change record type as required
        
        smf2JsonCli.start(new CliClient(), args);    
    }
    
    private static class CliClient implements Smf2JsonCLI.Client
    {        
        @Override
        public List<Object> processRecord(SmfRecord record) 
        {
            // Receive each record read.
            // Return a list of objects to be converted to JSON format,
            // an empty list or null.
            // Return Smf2JsonCLI.FINISHED to stop reading records.
            
            return Collections.singletonList(record);
        }
        
        @Override
        public List<Object> onEndOfData() 
        {
            // Called after all records have been read.
            // Return a list of objects to be converted to JSON format
            // e.g. a summary of the data, an empty list or null.
            
            System.err.println("Finished");
            return null;
        }
    }
}
