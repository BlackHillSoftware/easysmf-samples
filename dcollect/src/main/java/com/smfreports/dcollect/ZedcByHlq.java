package com.smfreports.dcollect;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;

/**
 * 
 * Report zEDC compressed dataset statistics by HLQ 
 *
 */
public class ZedcByHlq
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: ZedcByHlq <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Create a Map to keep the information by storage group
        Map<String, ZedcSummary> zedcByHlq = new HashMap<>();
        
        // open the file/dataset name passed on the command line
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create DCOLLECT record
                .filter(r -> r.dcurctyp().equals(DcollectType.D)) // check type
                .map(ActiveDataset::from) // create ActiveDataset record 
                .filter(r ->r.dcdcmptv() && r.dcdczedc()) // comp type is valid and comp type is zedc
                .forEach(record -> 
                {
                    String hlq = hlq(record.dcddsnam());
                    // create new entry for HLQ if it doesn't exist and add information from record
                    zedcByHlq.computeIfAbsent(hlq, key -> new ZedcSummary())
                        .add(record);                  
                });
        }
        
        // Write headings
        System.out.format("%-8s %8s %12s %12s %7s%n", 
                "HLQ",
                "Count",
                "Comp MB",
                "Uncomp MB",
                "Comp%");
        
        zedcByHlq.entrySet().stream()
            // sort entries comparing compressed size, a and b reversed to sort descending
            .sorted((a,b) -> Double.compare(b.getValue().compressed, a.getValue().compressed))
            .forEachOrdered(entry -> 
            {
                // write hlq details
                System.out.format("%-8s %,8d %,12.0f %,12.0f %7.1f%n", 
                        entry.getKey(), // key is hlq
                        entry.getValue().count,
                        entry.getValue().compressed,
                        entry.getValue().uncompressed,
                        entry.getValue().savedPct());
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
     * Collect zEDC summary information
     *
     */
    private static class ZedcSummary
    {
        void add(ActiveDataset ds)
        {
            count++;
            compressed += ds.dcdcudszMB();
            uncompressed += ds.dcdudsizMB();
        }
        
        int count = 0;
        double compressed = 0;
        double uncompressed = 0;
        
        /**
         * Return the compression percent (the percentage of the original space saved) 
         * @return the compression percent
         */
        double savedPct() {return (1 - (compressed / uncompressed)) * 100;}   
    }
}
