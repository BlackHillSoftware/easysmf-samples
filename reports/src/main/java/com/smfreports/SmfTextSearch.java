package com.smfreports;

import java.io.IOException;
import com.blackhillsoftware.smf.SmfRecordReader;

/**
 *
 * Search all SMF records for a text string, 
 * and print Time, System, Type and Subtype for 
 * each record found.
 *
 */
public class SmfTextSearch                                                                            
{                                                                                               
    public static void main(String[] args) throws IOException                                   
    {
        if (args.length < 2)
        {
            System.out.println("Usage: SmfTextSearch string <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        String searchString = args[0];

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
    	
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[1]))
        { 
            reader
            .stream()
            // Optionally filter records 
            //.filter(record -> record.recordType() != 14) // Exclude type 14 (read dataset)
            .filter(record -> record.toString().contains(searchString))
            .limit(10000)
            .forEach(record -> 
                // print record time, system, type and subtype
                System.out.format("%-24s %s Type: %3d Subtype: %3s%n",                                  
                        record.smfDateTime(), 
                        record.system(),
                        record.recordType(),
                        record.hasSubtypes() ? 
                                Integer.toString(record.subType()) : ""));

            System.out.format("Done");                  
        }                                   
    }                                                                                           
}                                                                                               