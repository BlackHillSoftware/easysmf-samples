package com.smfreports.dcollect;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.summary.Top;

/**
 * Compare 2 DCOLLECT runs, showing the top 50 HLQ by change in size.
 *
 */
public class DeltaHlq
{
    /**
     * Filter the datasets to be included in the report
     * @param datasetRecord an ActiveDataset record
     * @return true if the dataset should be included
     */
    private static boolean includeDataset(ActiveDataset datasetRecord)
    {
        // Change criteria as required, or simply:
        // return true;
        
        if (
                datasetRecord.dcdstogp().equals("SG1")           
           )
        {
            return true;
        }
        return false;
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length < 2)
        {
            System.out.println("Usage: DeltaHlq <input-name-1> <input-name-2>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Create a map to keep information by HLQ
        Map<String, HlqInfo> hlqs = new HashMap<>();
        
        // Stream first DCOLLECT file 
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create Dcollect Record
                .filter(record -> record.dcurctyp().equals(DcollectType.D)) // check type
                .map(ActiveDataset::from) // create ActiveDataset record
                .filter(DeltaHlq::includeDataset) 
                .forEach(datasetrecord -> {
                    // Add A values, creating new entry for hlq if required
                    hlqs.computeIfAbsent(hlq(datasetrecord.dcddsnam()), 
                            key -> new HlqInfo())
                        .addA(datasetrecord);
                });
        }
        
        // Repeat for second DCOLLECT file 
        try (VRecordReader reader = VRecordReader.fromName(args[1]))
        {
            reader.stream()
                .map(DcollectRecord::from)
                .filter(record -> record.dcurctyp().equals(DcollectType.D))
                .map(ActiveDataset::from)
                .filter(DeltaHlq::includeDataset)                
                .forEach(datasetrecord -> {
                    // Add B values, creating new entry for hlq if required
                    hlqs.computeIfAbsent(hlq(datasetrecord.dcddsnam()), 
                            key -> new HlqInfo())
                        .addB(datasetrecord);
                });
        }
        
        // Write report
        reportDeltas(hlqs);              
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
     * Write the report for the top HLQ by change in space
     * @param datasets the map containing HLQ information
     */
    private static void reportDeltas(Map<String, HlqInfo> hlqs) 
    {
        // Write headings
        System.out.format("%-8s %10s %10s %10s %12s %12s %12s%n",
                "HLQ",
                "Count A",
                "Count B",
                "Change",
                "MB A",
                "MB B",
                "Change MB"
                );
        
        hlqs.entrySet().stream()
            .filter(entry -> entry.getValue().absChange() > 0) // discard entries with no change
            
           // collect top 50 values by absChange i.e. positive or negative
            .collect(Top.values(50, 
                    Comparator.comparing(entry -> entry.getValue().absChange())))
            .forEach(entry -> 
            {
                HlqInfo hlqinfo = entry.getValue();             

                // Write detail
                System.out.format("%-8s %,10d %,10d %+,10d %,12d %,12d %+,12d%n",
                        entry.getKey(),
                        hlqinfo.countA,
                        hlqinfo.countB,
                        hlqinfo.deltaCount(),
                        hlqinfo.spaceMBA,
                        hlqinfo.spaceMBB,
                        hlqinfo.deltaSpace());
            });
    }
    
    /**
     * 
     * Collect information for a high level qualifier.
     * 
     * A and B entries are collected using separate methods for simplicity.
     * 
     * If an entry only exists in the A data or B data, the values for the 
     * other run will be zero.
     *
     */
    private static class HlqInfo
    {    
        int countA = 0;
        long spaceMBA = 0;
        
        int countB = 0;
        long spaceMBB = 0;
        
        int deltaCount() { return countB - countA; }
        long deltaSpace() { return spaceMBB - spaceMBA; }
        
        public long absChange() { return Math.abs(deltaSpace()); }
        
        void addA(ActiveDataset ds)
        {
            countA++;
            spaceMBA += ds.dcdallsxMB();
        }
        
        void addB(ActiveDataset ds)
        {
            countB++;
            spaceMBB += ds.dcdallsxMB();
        }
    }

}
