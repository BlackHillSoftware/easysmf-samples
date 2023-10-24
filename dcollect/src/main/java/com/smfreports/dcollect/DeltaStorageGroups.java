package com.smfreports.dcollect;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;

/**
 * Compare 2 DCOLLECT runs, showing the change in space for each storage group.
 *
 */
public class DeltaStorageGroups
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 2)
        {
            System.out.println("Usage: DeltaStorageGroups <input-name-1> <input-name-2>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Create a map to keep information by HLQ
        Map<String, StorageGroupInfo> storageGroups = new HashMap<>();
        
        // Stream first DCOLLECT file 
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create Dcollect record
                .filter(record -> record.dcurctyp().equals(DcollectType.VL)) // check type
                .map(SmsVolume::from) // create SmsVolume record
                .forEach(volumerecord -> 
                {
                    // Add A values, creating new entry for storage group if required
                    storageGroups.computeIfAbsent(volumerecord.dvlstgrp(), 
                            key -> new StorageGroupInfo())
                        
                        .addA(volumerecord);
                });
        }
        
        // Repeat for second DCOLLECT file 
        try (VRecordReader reader = VRecordReader.fromName(args[1]))
        {
            reader.stream()
                .map(DcollectRecord::from)
                .filter(record -> record.dcurctyp().equals(DcollectType.VL))
                .map(SmsVolume::from)
                .forEach(volumerecord -> 
                {
                    // Add B values, creating new entry for storage group if required
                    storageGroups.computeIfAbsent(volumerecord.dvlstgrp(),
                            key -> new StorageGroupInfo())
                    
                        .addB(volumerecord);
                });
        }
        
        // write report
        reportDeltas(storageGroups);  
               
    }

    /**
     * Write the report for change in space by storage group
     * @param storageGroups
     */
    private static void reportDeltas(Map<String, StorageGroupInfo> storageGroups) 
    {        
        storageGroups.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()) // sort by storage group name
            .forEachOrdered(entry -> 
            {                
                StorageGroupInfo storagegroup = entry.getValue();             
             
                // write headings
                System.out.format("%nStorage Group : %-30s%n",
                        entry.getKey());
                
                System.out.format("%8s %10s %10s %10s %10s%n",
                        "",
                        "Volumes",
                        "Tot MB",
                        "Free MB",
                        "Used%");
                
                // Write statistics for each storage group
                System.out.format("%8s %,10d %,10d %,10d %10.1f%n",
                        "A:",
                        storagegroup.volCountA,
                        storagegroup.spaceMBA,
                        storagegroup.freeMBA,
                        storagegroup.usedA());
                
                System.out.format("%8s %,10d %,10d %,10d %10.1f%n",
                        "B:",
                        storagegroup.volCountB,
                        storagegroup.spaceMBB,
                        storagegroup.freeMBB,
                        storagegroup.usedB());
                
                // Then a line with the changes
                System.out.format("%8s %+,10d %+,10d %+,10d %+10.1f%n",
                        "Change:",
                        storagegroup.deltaVolumes(),
                        storagegroup.deltaSpace(),
                        storagegroup.deltaFree(),
                        storagegroup.deltaUsedPct());
            });
    }
    
    /**
     * 
     * Collect information for a storage group
     * 
     * A and B entries are collected using separate methods for simplicity.
     * 
     * If an entry only exists in the A data or B data, the values for the 
     * other run will be zero.
     *
     */
    private static class StorageGroupInfo
    {    
        int volCountA = 0;
        long spaceMBA = 0;
        long freeMBA = 0;
        
        int volCountB = 0;
        long spaceMBB = 0;
        long freeMBB = 0;
        
        // calculate percent used values
        double usedA() {return (double)(spaceMBA - freeMBA) / spaceMBA * 100; }
        double usedB() {return (double)(spaceMBB - freeMBB) / spaceMBB * 100; }
        
        // calculate changes between A and B 
        int    deltaVolumes() { return volCountB - volCountA; }
        long   deltaSpace()   { return spaceMBB - spaceMBA; }
        long   deltaFree()    { return freeMBB - freeMBA; }
        double deltaUsedPct() { return usedB() - usedA(); }
        
        void addA(SmsVolume ds)
        {
            volCountA++;
            spaceMBA += ds.dvlntcpy();
            freeMBA += ds.dvlnfree();
        }
        
        void addB(SmsVolume ds)
        {
            volCountB++;
            spaceMBB += ds.dvlntcpy();
            freeMBB += ds.dvlnfree();
        }
    }

}
