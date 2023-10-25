package com.smfreports.dcollect;

import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.Map.Entry;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;

/**
 * Report Migrated Datasets by migration date.
 * 
 * This program groups datasets by migration date, and reports the dataset count and
 * space usage of each group.
 *
 */
public class DatasetsByMigratedDate
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: DatasetsByMigratedDate <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }

        LocalDate rundate = LocalDate.now();
        
        // TreeMap allows us to find the smallest key greater than or
        // equal to a time. We can use this to group the datasets by migration
        // date.
        // The times can be modified as required, however using
        // minusDays/Months/Years give neat values in the Period
        // calculation in the output.
        TreeMap<LocalDate, DatasetGroup> datasetsByMigDate = new TreeMap<>();
        
        // Create entries for groups as required
        datasetsByMigDate.put(rundate, new DatasetGroup());
        datasetsByMigDate.put(rundate.minusDays(7), new DatasetGroup());
        datasetsByMigDate.put(rundate.minusMonths(1), new DatasetGroup());
        datasetsByMigDate.put(rundate.minusMonths(6), new DatasetGroup());
        datasetsByMigDate.put(rundate.minusYears(1), new DatasetGroup());
        datasetsByMigDate.put(rundate.minusYears(5), new DatasetGroup());
                
        // open the file/dataset name passed on the command line
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create Dcollect record
                .filter(r -> r.dcurctyp().equals(DcollectType.M)) // check type
                .map(MigratedDataset::from) // create MigratedDataset record
                .forEach(record -> 
                {
                    // find smallest entry with key greater than or equal to the migration date 
                    Entry<LocalDate, DatasetGroup> agegroup = datasetsByMigDate.ceilingEntry(record.umdate());
                    if (agegroup == null)
                    {
                        // Not expected to happen, but maybe migrate date in the future?
                        // Use last (newest) entry
                        agegroup = datasetsByMigDate.lastEntry();
                    }
                    // Add the information
                    agegroup.getValue().add(record);
                });
        }
        
        // Write headers
        System.out.format("%-8s %8s %10s %10s%n",
                "Migrated",
                "Count",
                "Migrat MB",
                "Size MB");
        
        // Write report
        datasetsByMigDate.entrySet().stream()
            .forEachOrdered(entry -> {
                System.out.format("%-8s %8d %10.1f %10.1f%n", 
                        Period.between(entry.getKey(), rundate),
                        entry.getValue().count,
                        entry.getValue().migratedMB,
                        entry.getValue().recallSpaceMB);
            });
    }
    
    private static class DatasetGroup
    {
        // Add information from a migrated dataset entry
        void add(MigratedDataset ds)
        {
            count++;
            migratedMB += ds.umdsizeMB(); // migrated size
            recallSpaceMB += ds.umrecspMB(); // space required to recall
        }
        
        int count = 0;
        double migratedMB = 0;
        double recallSpaceMB = 0;
    }
    
}
