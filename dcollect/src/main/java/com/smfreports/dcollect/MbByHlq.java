package com.smfreports.dcollect;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;

/**
 * 
 * Report Level 0, Level 1 and Level 2 space by high level qualifier using
 * Active Dataset and Migrated Dataset DCOLLECT records.
 *
 */
public class MbByHlq
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: MbByHlq <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Create a Map to keep the summaries by HLQ
        Map<String, SpaceInformation> totalsByHlq = new HashMap<>();
        
        // open the file/dataset name passed on the command line
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create DCOLLECT record
                .forEach(record -> 
                {
                    // process Active Dataset and Migrated Dataset records, ignore others
                    switch (record.dcurctyp())
                    {
                    case D:
                    {
                        ActiveDataset dataset = ActiveDataset.from(record);
                        // create new entry for HLQ if it doesn't exist
                        totalsByHlq.computeIfAbsent( 
                                hlq(dataset.dcddsnam()), 
                                    key -> new SpaceInformation())
                            // add dataset information
                            .add(dataset);                  
                        break;
                    }
                    case M:
                        MigratedDataset dataset = MigratedDataset.from(record);
                       // create new entry for HLQ if it doesn't exist
                        totalsByHlq.computeIfAbsent(
                                hlq(dataset.umdsnam()), 
                                    key -> new SpaceInformation())
                            // add dataset information
                            .add(dataset);                          
                        break;
                    default:
                        break;
                    
                    }
                });
        }
        
        // Write headings
        System.out.format("%-8s %8s %10s %8s %10s %10s %8s %10s %8s %10s%n", 
                "HLQ",
                "Count",
                "Total MB",
                "Level0",
                "Alloc MB",
                "Used MB",
                "ML1",
                "ML1 MB",
                "ML2",
                "ML2 MB");
        
        // Write report
        totalsByHlq.entrySet().stream()
            // sort output comparing total space, a and b reversed to sort descending 
            .sorted((a,b) -> Double.compare(b.getValue().totalMB, a.getValue().totalMB))
            .forEachOrdered(entry -> {
                System.out.format("%-8s %,8d %10.0f %,8d %10.0f %10.0f %,8d %10.0f %,8d %10.0f%n", 
                        entry.getKey(),
                        entry.getValue().count,
                        entry.getValue().totalMB,
                        entry.getValue().level0Count,
                        entry.getValue().level0Alloc,
                        entry.getValue().level0UsedMB,
                        entry.getValue().level1Count,
                        entry.getValue().level1MB,
                        entry.getValue().level2Count,
                        entry.getValue().level2MB);
            });
    }
    
    /**
     * Extract the hlq from a dataset name
     * @param dsn the dataset name
     * @return the high level qualifier
     */
    private static String hlq(String dsn)
    {
        String[] qualifiers = dsn.split("\\.", 2);
        return qualifiers[0];
    }
    
    /**
     * 
     * Accumulate space information for a group of datasets
     *
     */
    private static class SpaceInformation
    {
        /**
         * Add information for an active dataset
         * @param datasetRecord the active dataset record
         */
        void add(ActiveDataset datasetRecord)
        {
            count++;
            level0Count++;
            totalMB += datasetRecord.dcdallsxMB();
            level0Alloc += datasetRecord.dcdallsxMB();
            level0UsedMB += datasetRecord.dcdusesxMB();
        }
        
        /**
         * Add information for a migrated dataset
         * @param datasetRecord the migrated dataset record
         */
        void add(MigratedDataset datasetRecord)
        {
            count++;
            
            // There are various space values in the record, we
            // will report the estimated space required if this
            // dataset was recalled to level 0       
            double space = datasetRecord.umrecspMB();
            
            totalMB += space;
            // record level 1 or level 2 information according to the record information
            switch (datasetRecord.umlevel())
            {
            case LEVEL1:
                level1Count++;
                level1MB += space;
                break;
            case LEVEL2:
                level2Count++;
                level2MB += space;
                break;
            default:
                throw new RuntimeException("Unexpected migration level: " + datasetRecord.umlevel());
            }
        }
        
        int count = 0;
        double totalMB = 0;

        int level0Count = 0;      
        double level0Alloc = 0;
        double level0UsedMB = 0;
        
        int level1Count = 0;
        double level1MB = 0;
        
        int level2Count = 0;
        double level2MB = 0;
    }
}
