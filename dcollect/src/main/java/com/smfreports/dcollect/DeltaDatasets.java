package com.smfreports.dcollect;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.summary.Top;

/**
 * Compare 2 DCOLLECT runs, showing the top 100 datasets by change in size.
 * 
 * Datasets can be filtered by any criteria (prefix, storage group etc) in the 
 * includeDataset method.
 * 
 * GDG generations are masked so all generations are reported in a single
 * entry with the suffix .G####V##. You could use a similar technique for
 * other dataset names if you want related datasets to appear in one entry.
 *
 */
public class DeltaDatasets
{
    /**
     * Filter the datasets to be included in the report
     * @param datasetRecord an ActiveDataset record
     * @return true if the dataset should be included
     */
    private static boolean includeDataset(ActiveDataset datasetRecord)
    {
        // Modify criteria here as required.
        if (
               //datasetRecord.dcddsnam().startsWith("ABCD")
               //&&
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
            System.out.println("Usage: DeltaDatasets <input-name-1> <input-name-2>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Create a Map to store information by dataset name
        Map<String, DatasetInfo> datasets = new HashMap<>();
        
        // Stream first DCOLLECT file 
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create Dcollect Record
                .filter(record -> record.dcurctyp().equals(DcollectType.D)) // check type
                .map(ActiveDataset::from) // create ActiveDataset record
                .filter(DeltaDatasets::includeDataset) // filter name
                .forEach(datasetrecord -> 
                {
                    // mask the dataset name if it is a GDG generation
                    String name = maskIfGDG(datasetrecord.dcddsnam());
                    
                    // Add A values, creating entries if they don't exist
                    datasets.computeIfAbsent(name, 
                            key -> new DatasetInfo())
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
                .filter(DeltaDatasets::includeDataset)                
                .forEach(datasetrecord -> 
                {
                    String name = maskIfGDG(datasetrecord.dcddsnam());
                    
                    // Add B values, creating entries if they don't exist
                    datasets.computeIfAbsent(name, 
                            key -> new DatasetInfo())
                        .addB(datasetrecord);
                });
        }
        
        reportDeltas(datasets);              
    }

    static Pattern gdgPattern  = Pattern.compile("(.+)\\.G\\d{4}V\\d{2}$");
    /**
     * Mask the dataset name if it matches the GDG pattern 
     * @param maybeGdg a dataset name that might be a GDG
     * @return the dataset name, with the GDG generation masked if it matches the pattern
     */
    public static String maskIfGDG(String maybeGdg)
    {
        Matcher m = gdgPattern.matcher(maybeGdg);
        if (m.matches())
        {
            // replace suffix with GDG mask
            return m.group(1) + ".G####V##";
        }
        // didn't match - return original name
        return maybeGdg;
    }
    
    /**
     * Write the report for the top datasets by change in space
     * @param datasets the map containing dataset information
     */
    private static void reportDeltas(Map<String, DatasetInfo> datasets) 
    {        
        // Write headings
        System.out.format("%-44s %12s %12s %12s%n",
                "Dataset",
                "MB A",
                "MB B",
                "Change MB"
                );
        
        datasets.entrySet().stream()
            // discard anything unchanged
            .filter(entry -> entry.getValue().absChange() > 0)
            
            // collect top 100 values by absChange i.e. positive or negative
            .collect(Top.values(100, 
                    Comparator.comparing(entry -> entry.getValue().absChange())))
            .forEach(entry -> 
            {
                DatasetInfo dataset = entry.getValue();             

                // write detail line
                System.out.format("%-44s %,12d %,12d %+,12d%n",
                        entry.getKey(), // key is dataset name
                        dataset.spaceMBA,
                        dataset.spaceMBB,
                        dataset.deltaSpace());
            });
    }
    
    /**
     * 
     * Collect information for a dataset name.
     * 
     * A and B entries are collected using separate methods for simplicity.
     * Typically, there will be only one record added per dataset name, 
     * but GDGs are masked so that all generations are reported in the
     * same entry.
     * 
     * If an entry only exists in the A data or B data, the values for the 
     * other run will be zero.
     *
     */
    private static class DatasetInfo
    {    
        long spaceMBA = 0;        
        long spaceMBB = 0;
        
        long deltaSpace() { return spaceMBB - spaceMBA; }   
        public long absChange() { return Math.abs(deltaSpace()); }
        
        void addA(ActiveDataset ds)
        {
            spaceMBA += ds.dcdallsxMB();
        }
        
        void addB(ActiveDataset ds)
        {
            spaceMBB += ds.dcdallsxMB();
        }
    }

}
