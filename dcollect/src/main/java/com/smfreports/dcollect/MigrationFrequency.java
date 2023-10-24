package com.smfreports.dcollect;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.summary.Top;

/**
 * 
 * Report frequently migrated datasets.
 * 
 * This report finds datasets that were migrated in the last 3 months,
 * and have been migrated 3 or more times in total.
 * 
 * It reports the top 100 datasets by migration frequency, defined as
 * the number of times the dataset has been migrated divided by the
 * time since its creation date. 
 *
 */
public class MigrationFrequency
{
    /**
     * Filter to allow datasets to be excluded from the report.
     * @param dataset dataset record to check
     * @return true if it should be considered for the report
     */
    private static boolean includeDataset(MigratedDataset dataset)
    {
        String datasetName = dataset.umdsnam();
        //if (datasetName.startsWith("ABCD")) return false;
        
        return true;
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: MigrationFrequency <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }

        LocalDate runtime = LocalDate.now(); 
        LocalDate cutoff = runtime.minusMonths(3); 

        // Write headings
        System.out.format("%-44s %-15s %-15s %7s %7s %10s%n",
                "Dataset",
                "Created",
                "Migrated",
                "Count",
                "Freq",
                "Size MB");
                
        // open the file/dataset name passed on the command line
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create DcollectRecord 
                .filter(r -> r.dcurctyp().equals(DcollectType.M)) // check type
                .map(MigratedDataset::from) // create MigratedDataset record
                
                .filter(r -> r.umnmig() > 2) // check number of migrations
                .filter(r -> r.umdate().isAfter(cutoff)) // check if migrated recently
                
                 // creation date only provided for SMS managed datasets - check storage class
                .filter(r -> r.umstgcl().length() > 0) // 
                .filter(r -> includeDataset(r)) // specific include/exclude
                
                // collect top 100 comparing by calculated migration frequency
                .collect(Top.values(100, Comparator.comparing(r -> frequency(r, runtime))))
                
                // print opt 100 records
                .forEach(record -> 
                {
                    System.out.format("%-44s %-15s %-15s %7d %7.1f %,10.1f%n", 
                            record.umdsnam(),
                            record.umcredt(),
                            record.ummdate().toLocalDate(),
                            record.umnmig(),
                            frequency(record, runtime), 
                            record.umrecspMB());
                });
        }
    }
        
    /**
     * Calculate the migration frequency of a dataset in migrations/year
     * @param dataset the migrated dataset record
     * @param runtime the time used to calculate the dataset age 
     * @return a frequency in migrations per year
     */
    private static double frequency(MigratedDataset dataset, LocalDate runtime)
    {
        // get time between creation date and current time in days
        long days = dataset.umcredt().until(runtime, ChronoUnit.DAYS);
        
        // divide migrations by age in days and convert to years
        return (double)dataset.umnmig() / days * 365;
    }    
}
