package com.smfreports.type30;
                                                               
import java.io.IOException;                                                                     
import java.util.*;

import com.blackhillsoftware.smf.*;                                                     
import com.blackhillsoftware.smf.smf30.*;                                             
                                                                                                
/**
 * Search for jobs with different job names but the same 
 * JES2 calculated JCL ID value.  
 *
 */

public class JobsByJclId                                                                            
{                                                                                               
    public static void main(String[] args) throws IOException                                   
    {   
        if (args.length < 1)
        {
            System.out.println("Usage: JobsByJclId <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        // Map JCL id values to a set of job names. 
        Map<Token, Set<String>> jobsByJclId = new HashMap<>();
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])
                .include(30,5)) 
        { 
            for (SmfRecord record : reader)
            {
                Smf30Record r30 = Smf30Record.from(record);
                // check the identification section is long enough for the JCL ID fields
                // (older records will be skipped) and the JCL ID has a value i.e. not all zeros
                if (!r30.header().smf30wid().equals("TSO") // not interesting
                        && r30.identificationSection().length() > IdentificationSection.SMF30ID_Len_V1
                        && !r30.identificationSection().smf30Jclid1().isZeros())
                {
                    // Add a new set of jobnames if this is the first instance of the JCL ID value.
                    // Add the jobname to the set if not already present
                    jobsByJclId.computeIfAbsent(r30.identificationSection().smf30Jclid1(), 
                            entry -> new HashSet<>())
                        .add(r30.identificationSection().smf30jbn());
                }
            }                                                                         
        }
        
        // stream the JCL ID entries
        jobsByJclId.entrySet().stream()
            // ignore any with only one jobname
            .filter(entry -> entry.getValue().size() > 1)
            .forEach(entry -> 
                {
                    // write the JCL ID value
                    System.out.format("%n%s%n", entry.getKey());
                    // sort and print the jobnames
                    entry.getValue().stream()
                        .sorted()
                        .forEachOrdered(jobname -> System.out.format("   %s%n", jobname));
                });
        
        System.out.println("Done");
    }                                                                                           
}                                                                                               