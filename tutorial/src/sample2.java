import java.io.IOException;

import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf14.Smf14Record;

/**
 * 
 * Sample 2 shows how you can use Java Streams to filter and process SMF data.
 *
 * The sample searches for SMF type 14 (input) and 15 (output) dataset close 
 * records for dataset SYS1.PARMLIB, and prints the time, system, jobname and 
 * the type of access.
 *
 */

public class sample2
{
    public static void main(String[] args) throws IOException                                   
    {                                       
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {           
            reader
                .include(14)
                .include(15)
                .stream() // use java streams
                 // create a Smf14Record - also maps type 15
                .map(record -> Smf14Record.from(record))
                 // filter by dataset name
                .filter(r14 -> r14.smfjfcb1().jfcbdsnm().equals("SYS1.PARMLIB"))
                .limit(1000000) // limit the number of matches
                // for each match, print information
                .forEachOrdered(r14 -> 
                    System.out.format("%-23s %-4s %-8s %-6s%n",                                  
                        r14.smfDateTime(), 
                        r14.system(),
                        r14.smf14jbn(),
                        r14.smf14rty() == 14 ? "Read" : "Update"));                                                                           
        }
        System.out.println("Done");
    }
}
