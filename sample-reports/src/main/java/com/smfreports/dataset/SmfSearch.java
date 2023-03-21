package com.smfreports.dataset;

import java.io.IOException;                                                                     
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf15.Smf15Record;

/**
 * Search SMF type 15 (Output Dataset Activity) for a specific dataset name.
 * Prints the time, system and jobname of jobs that opened the dataset for output. 
 */

public class SmfSearch                                                                            
{                                                                                               
    public static void main(String[] args) throws IOException                                   
    {                                       
        if (args.length < 2)
        {
            System.out.println("Usage: SmfSearch DATASET.NAME <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        String searchString = args[0];

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[1]))
        { 
            reader.include(15)
                .stream()
                .map(record -> Smf15Record.from(record))
                .filter(r15 -> r15.smfjfcb1().jfcbdsnm().equals(searchString))
                .limit(1000)
                .forEach(r15 -> 
                    System.out.format("%s %s %s%n",                                  
                        r15.smfDateTime(), 
                        r15.system(),
                        r15.smf14jbn()));                                                                                 
        }
        System.out.println("Done");
    }                                                                                           
}                                                                                               