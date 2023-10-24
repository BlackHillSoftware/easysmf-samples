package com.smfreports.dcollect;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.smf.*;

/**
 * 
 * Report space by storage group.
 * 
 * The report shows overall statistics for the storage group, followed by 
 * details of the volumes in each storage group.
 *
 */
public class StorageGroups
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: StorageGroups <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Create a Map to keep the information by storage group
        Map<String, StorageGroupInfo> storageGroups = new HashMap<>();
        
        // open the file/dataset name passed on the command line
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            reader.stream()
                .map(DcollectRecord::from) // create Dcollect Record
                .filter(record -> record.dcurctyp().equals(DcollectType.VL)) // check type
                .map(SmsVolume::from) // create SmsVolume record
                .forEach(volumerecord -> {
                    // create new entry for storage group if it doesn't exist
                    storageGroups.computeIfAbsent(volumerecord.dvlstgrp(), 
                            key -> new StorageGroupInfo())
                        // add the information for this volume to the storage group
                        .add(volumerecord);
                });
        }
        
        // write the summary report
        storageGroupSummary(storageGroups);  
        
        // write the storage group volumes report
        storageGroupVolumes(storageGroups);
        
    }

    /**
     * Write the storage group summary report
     * @param storageGroups the Map containing storage group information
     */
    private static void storageGroupSummary(Map<String, StorageGroupInfo> storageGroups) 
    {
        // write headings
        System.out.format("%-30s %10s %10s %10s %10s %10s%n",
                "Storage Group",
                "Volumes",
                "Tot MB",
                "Free MB",
                "Used%",
                "Lrgst MB");
        
        storageGroups.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()) // sort by storage group name
            .forEachOrdered(entry -> 
            {
                StorageGroupInfo storagegroup = entry.getValue();             
                
                System.out.format("%-30s %,10d %,10d %,10d %10.1f %,10d%n",
                        entry.getKey(), // key is storage group name
                        storagegroup.volCount,
                        storagegroup.spaceMB,
                        storagegroup.freeMB,
                        storagegroup.usedPct(),
                        storagegroup.largestMB);
                
            });
    }
    
    /**
     * Write the storage group volumes report
     * @param storageGroups the Map containing storage group information
     */
    private static void storageGroupVolumes(Map<String, StorageGroupInfo> storageGroups) 
    {
        storageGroups.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()) // key is storage group name
            .forEachOrdered(entry -> {
                StorageGroupInfo storagegroup = entry.getValue();
                // write headings for storage group
                System.out.format("%nStorage Group: %s%n%n", 
                        entry.getKey());
                
                System.out.format("%-8s %10s %10s %10s %10s%n", 
                        "Volume",
                        "Tot MB",
                        "Free MB",
                        "Used%",
                        "Lrgst MB");
                
                // write volume details
                storagegroup.volumes.stream()
                    .sorted(Comparator.comparing(vol -> vol.volser))
                    .forEachOrdered(vol -> {
                        
                        System.out.format("%-8s %,10d %,10d %10.1f %,10d%n", 
                                vol.volser,
                                vol.spaceMB,
                                vol.freeMB,
                                vol.usedPct(),
                                vol.largestMB);
                        
                    });            
            });
    }
    
    /**
     * 
     * A class to collect storage group information
     *
     */
    private static class StorageGroupInfo
    {    
        int volCount = 0;
        long spaceMB = 0;
        long freeMB = 0;
        long largestMB = 0;
        
        // collect information for the volumes in this storage group
        List<VolumeInfo> volumes = new ArrayList<>();
        
        /**
         * Add information for a volume to this storage group
         * @param smsVolumeRecord the SMS volume record
         */
        void add(SmsVolume smsVolumeRecord)
        {
            // extract the volume information
            VolumeInfo volumeInfo = new VolumeInfo(smsVolumeRecord);
            
            // also add the information to the storage group totals
            volCount++;
            spaceMB += volumeInfo.spaceMB;
            freeMB += volumeInfo.freeMB;
            largestMB = Math.max(largestMB, volumeInfo.largestMB);
            
            volumes.add(volumeInfo);
        }
        
        /**
         * Calculate the percent used for the storage group
         * @return double the percent used
         */
        double usedPct()
        {
            return (double)(spaceMB - freeMB) / spaceMB * 100;
        }
    }
    
    /**
     * 
     * Collect information for a volume
     *
     */
    private static class VolumeInfo
    {
        String volser;
        long spaceMB;
        long freeMB;
        long largestMB;
        
        public VolumeInfo(SmsVolume smsVolumeRecord)
        {
            volser = smsVolumeRecord.dvlvser();
            spaceMB = smsVolumeRecord.dvlntcpy();
            freeMB = smsVolumeRecord.dvlnfree();
            largestMB = smsVolumeRecord.dvlnlext();
        }
        
        /**
         * Calculate the percent used for the volume
         * @return double the percent used
         */
        double usedPct()
        {
            return (double)(spaceMB - freeMB) / spaceMB * 100;
        }
    }
}
