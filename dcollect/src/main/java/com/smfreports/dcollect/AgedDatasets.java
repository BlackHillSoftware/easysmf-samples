package com.smfreports.dcollect;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;

/**
 *
 * Report datasets from the ActiveDataset records with a last reference
 * date older than the cutoff value (eg 5 years), or creation date
 * if there is no last reference date.
 *
 */
public class AgedDatasets
{
    /**
     * Filter the datasets so that some datasets can be excluded from the 
     * report if required. 
     * 
     * @param datasetRecord the ActiveDataset record
     * @return true if the dataset should be included
     */
    private static boolean includeDataset(ActiveDataset datasetRecord)
    {
        String datasetName = datasetRecord.dcddsnam();
        if (datasetName.startsWith("SYS1.VTOCIX")) return false;
        if (datasetName.startsWith("SYS1.VVDS")) return false;
        
        return true;
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: AgedDatasets <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }

        LocalDate cutoff = LocalDate.now().minusYears(5); 

        // Write headings
        System.out.format("%-44s %-6s %15s %15s %10s%n",
                "Dataset",
                "VOLSER",
                "Created",
                "Last Ref",
                "Alloc MB");
                
        // open the file/dataset name passed on the command line
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create DCOLLECT record
                .filter(r -> r.dcurctyp().equals(DcollectType.D)) // check type
                .map(ActiveDataset::from) // create the Active Dataset record
                
                // check the last reference date if available, creation date if not
                .filter(record -> (record.dcdlstrf() != null && record.dcdlstrf().isBefore(cutoff)
                                    || record.dcdlstrf() == null && record.dcdcredt().isBefore(cutoff)))
                .filter(AgedDatasets::includeDataset)
                 
                // Optional sort - will result in all matching records being stored
                // in memory to be sorted. This is probably OK for any usable 
                // size of report.
                
                // .sorted(Comparator.comparing(record -> record.dcddsnam()))

                // alternate sort - by allocated space
                //.sorted(Comparator.comparing(record -> record.dcdscalxMB(), Comparator.reverseOrder()))

                // alternate sort - by reference date if available, otherwise create date
                //.sorted(Comparator.comparing(  
                //        record -> record.dcdlstrf() != null ? record.dcdlstrf() : record.dcdcredt()))
                
                .forEach(record -> 
                {
                    // Report the datasets
                    System.out.format("%-44s %-6s %15s %15s %,10.1f%n", 
                            record.dcddsnam(),
                            record.dcdvolsr(),
                            record.dcdcredt(),
                            record.dcdlstrf(),
                            record.dcdscalxMB());

                });
        }
    }
}
