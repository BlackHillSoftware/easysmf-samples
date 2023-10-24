package com.smfreports.dcollect;

import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.Map.Entry;

import com.blackhillsoftware.dcollect.ActiveDataset;
import com.blackhillsoftware.dcollect.DcollectRecord;
import com.blackhillsoftware.dcollect.DcollectType;
import com.blackhillsoftware.smf.*;

/**
 * Report Active Datasets by age.
 * 
 * This program groups datasets by age, and reports the dataset count and
 * space usage of each group.
 *
 */
public class DatasetsByLastRef
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: DatasetsByLastRef <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }

        LocalDate rundate = LocalDate.now();
        
        // TreeMap allows us to find the smallest key greater than or
        // equal to a time. We can use this to group the datasets by age.
        // The times can be modified as required, however using
        // minusDays/Months/Years give neat values in the Period
        // calculation in the output.
        
        TreeMap<LocalDate, DatasetGroup> datasetsByAge = new TreeMap<>();       
        
        // Create entries for groups as required
        datasetsByAge.put(rundate, new DatasetGroup());
        datasetsByAge.put(rundate.minusDays(7), new DatasetGroup());
        datasetsByAge.put(rundate.minusMonths(1), new DatasetGroup());
        datasetsByAge.put(rundate.minusMonths(6), new DatasetGroup());
        datasetsByAge.put(rundate.minusYears(1), new DatasetGroup());
        datasetsByAge.put(rundate.minusYears(5), new DatasetGroup());
                
        // open the file/dataset name passed on the command line
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create DCOLLECT record
                .filter(r -> r.dcurctyp().equals(DcollectType.D)) // check type
                .map(ActiveDataset::from) // create ActiveDataset record
                .forEach(datasetRecord -> 
                {
                    // if last reference date is null, use creation date
                    LocalDate lastref = datasetRecord.dcdlstrf() != null ? 
                            datasetRecord.dcdlstrf() 
                            : datasetRecord.dcdcredt();
                    
                    // find smallest entry with key greater than or equal to the lastref date 
                    Entry<LocalDate, DatasetGroup> agegroup = datasetsByAge.ceilingEntry(lastref);
                    if (agegroup == null)
                    {
                        // Not expected to happen, but maybe last reference date in the future?
                        // Use last (newest) entry
                        agegroup = datasetsByAge.lastEntry();
                    }
                    // Add the information
                    agegroup.getValue().add(datasetRecord);
                });
        }
        
        // Write headings
        System.out.format("%-8s %8s %10s %10s%n",
                "Last Ref",
                "Count",
                "Alloc MB",
                "Used MB");
        
        // Report groups 
        datasetsByAge.entrySet().stream()
            .forEachOrdered(entry -> {
                System.out.format("%-8s %8d %10.1f %10.1f%n", 
                        Period.between(entry.getKey(), rundate),
                        entry.getValue().count,
                        entry.getValue().allocatedMB,
                        entry.getValue().usedMB);
            });
    }
    
    /**
     * 
     * Collect information for a group of datasets
     *
     */
    private static class DatasetGroup
    {
        // Add information from a dataset entry
        void add(ActiveDataset ds)
        {
            count++;
            allocatedMB += ds.dcdallsxMB();
            usedMB += ds.dcdusesxMB();
        }
        
        int count = 0;
        double allocatedMB = 0;
        double usedMB = 0;
    }
    
}
