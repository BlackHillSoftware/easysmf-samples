package com.smfreports;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;

public class SmfReportDups
{
    private static void printUsage() {
        System.out.println("Usage: SmfReportDups <input-file> [input-file2 ...]");
        System.out.println("");
        System.out.println("Search for duplicated data in input-file.");
        System.out.println("");
        System.out.println("  input-file        File containing SMF records. Binary data, RECFM=U or");
        System.out.println("                    V[B] records including RDW.");
        System.out.println("  input-file2 ...   Additional input file(s), to search for duplicates");
        System.out.println("                    across multiple files.");
        System.out.println("");
        System.out.println("Records and duplicate records are grouped and counted by system and minute.");
        System.out.println("If the number of duplicate records in a minute is greater than or equal");
        System.out.println("to the number of unique records in that minute, data for that minute is");
        System.out.println("likely to have been duplicated and the counts for that minute are reported.");
        System.out.println("");
        System.out.println("Specific record types might also be included multiple times in the data.");
        System.out.println("Data for each minute is checked by record type. Again, any record type");
        System.out.println("where the number of duplicate records is greater than or equal to the");
        System.out.println("number of unique records might be duplicated data so the counts are");
        System.out.println("reported.");
        System.out.println("Minutes appearing in the first part of the report are excluded to avoid");
        System.out.println("reporting every record type for those minutes.");
    }
    
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException
    {
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h"))
        {
            printUsage();
            System.exit(0);
        }
        
        // We don't need a cryptographically secure hash, SHA-1 might be 
        // faster than SHA-256 hashes and should be adequate to find duplicates
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        Set<Digest> recordHashes = new HashSet<>();
        
        // Map to count by SystemID->Minute
        Map< String, Map<LocalDateTime, RecordStats>> bySystemByMinute = new HashMap<>();
        
        // Map to count by SystemID->Record Type->Minute 
        Map< String, Map<LocalDateTime, Map<Integer, RecordStats>>> bySystemByMinuteByType = new HashMap<>();
               
        int in = 0;
        int dups = 0;
        try
        {
            // Multiple input file/datasets can be specified to find duplicates
            // across files.
            for (int i = 0; i < args.length; i++)
            {
                try (SmfRecordReader reader = SmfRecordReader.fromName(args[i]))                
                {
                    for (SmfRecord record : reader)
                    {
                        in++;
                        String system = record.system();
                        LocalDateTime minute = record.smfDateTime()
                                .truncatedTo(ChronoUnit.MINUTES);
                        Integer recordtype = record.recordType();
                        
                        // get a stats entry for this system and minute
                        RecordStats minuteStats = bySystemByMinute
                                .computeIfAbsent(system, key -> new HashMap<>())
                                .computeIfAbsent(minute, key -> new RecordStats(null, minute));
                        
                        
                        // get a stats entry for this system, minute and record type
                        RecordStats minuteRecordTypeStats = bySystemByMinuteByType
                                .computeIfAbsent(system, key -> new HashMap<>())
                                .computeIfAbsent(minute, key -> new HashMap<>())
                                .computeIfAbsent(recordtype, key -> new RecordStats(recordtype, minute));
                        
                        // Is is a duplicate of one already seen?
                        if (recordHashes.add(new Digest(sha1.digest(record.getBytes()))))
                        {
                            // no
                            minuteStats.countUnique();
                            minuteRecordTypeStats.countUnique();
                        }
                        else
                        {
                            // yes
                            dups++;
                            minuteStats.countDuplicate();
                            minuteRecordTypeStats.countDuplicate();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            printUsage();
            throw e;
        }
        
        System.out.format("Finished, %d records in, %d duplicates.%n", in, dups);
        
        writeReport(bySystemByMinute, bySystemByMinuteByType);

    }

    private static void writeReport(
            Map<String, Map<LocalDateTime, RecordStats>> bySystemByMinute,
            Map<String, Map<LocalDateTime, Map<Integer, RecordStats>>> bySystemByMinuteByType) 
    {
        // get a list of all systems
        List<String> systems = bySystemByMinute.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        
        for (String system : systems)
        {
            // get entries by minute and by minute/recordtype for this system
            
            Map<LocalDateTime, RecordStats> byMinute = 
                    bySystemByMinute.get(system);
            
            Map<LocalDateTime, Map<Integer, RecordStats>> byMinuteByType = 
                    bySystemByMinuteByType.get(system);
            
            // Find minutes where duplicate count is greater than or equal to unique record count
            List<RecordStats> duplicateMinutesBySystem = 
                byMinute.values().stream()
                    .filter(entry -> entry.dupPercent() >= 100)
                    .sorted(Comparator.comparing(RecordStats::getMinute))
                    .collect(Collectors.toList());
                        
            // We don't want to report duplicates for every record type when 
            // we report duplicates by system
            // Remove entries for those minutes from the map by record type.   

            for (RecordStats entry : duplicateMinutesBySystem)
            {
                byMinuteByType.remove(entry.getMinute());
            }
            
            // Flatmap into one list and select entries with duplicates.
            // Sort by minute and record type
    
            List<RecordStats> duplicatesByMinuteByType = byMinuteByType.values().stream()
                    .flatMap(entry -> entry.values().stream())
                    .filter(entry -> entry.dupPercent() >= 100)
                    .sorted(Comparator.comparing(RecordStats::getMinute)
                            .thenComparing(RecordStats::getRecordtype))
                    .collect(Collectors.toList());
            
            // Write reports.
            
            reportBySystem(system, duplicateMinutesBySystem);
            reportByRecordType(system, duplicatesByMinuteByType);
        }
    }
    
    private static void reportBySystem(String system, List<RecordStats> duplicateMinutesBySystem) 
    {
        if (!duplicateMinutesBySystem.isEmpty())
        {
            System.out.format("%nSystem : %s%n", system);
            
            System.out.format("%n%-20s %8s %8s %6s%n%n",
                    "Minute",
                    "Records",
                    "Dup",
                    "Dup%"
                    );
            for (RecordStats minuteEntry : duplicateMinutesBySystem)
            {
                System.out.format("%-20s %8d %8d %6.0f%n", 
                        minuteEntry.getMinute(), 
                        minuteEntry.getTotal(), 
                        minuteEntry.getDuplicates(),
                        minuteEntry.dupPercent());
            }
        }
    }
    
    private static void reportByRecordType(String system, List<RecordStats> duplicatesByMinuteByType) 
    {
        if (!duplicatesByMinuteByType.isEmpty())
        {
            System.out.format("%nSystem : %s%n", system);
            System.out.format("%n%-20s %4s %8s %8s %6s%n%n",
                    "Minute",
                    "Type",
                    "Records",
                    "Dup",
                    "Dup%"
                    );
            
            for (RecordStats recordTypeEntry : duplicatesByMinuteByType)
            {
                
                System.out.format("%-20s %4d %8d %8d %6.0f%n",
                        recordTypeEntry.getMinute(),
                        recordTypeEntry.getRecordtype(), 
                        recordTypeEntry.getTotal(), 
                        recordTypeEntry.getDuplicates(),
                        recordTypeEntry.dupPercent());
            }
        }
    }
    
    private static class RecordStats
    {
        RecordStats(Integer recordtype, LocalDateTime minute)
        {
            this.recordtype = recordtype;
            this.minute = minute;
        }
        
        private Integer recordtype;
        private int unique = 0;
        private int duplicates = 0;
        private LocalDateTime minute;

        public void countUnique()
        {
            unique++;
        }
        
        public void countDuplicate()
        {
            duplicates++;
        }

        private Integer getRecordtype() { return recordtype; }    
        private LocalDateTime getMinute() { return minute; }
        private int getDuplicates() { return duplicates; }

        private int getTotal() { return unique + duplicates; }    
        private double dupPercent() { return (double)duplicates / unique * 100; }
    }
    
    /**
     * A class to store hashes in the set, providing hashcode and equals implementations
     * We need multiple matches before we report duplicate data, so we can use 
     * a 64 bit integer for the hash code without significant danger of incorrect 
     * reports. This allows more data to be processed in available memory.
     */
    private static class Digest
    {
        public Digest(byte[] digest)
        {
            // Use the first 8 bytes of the SHA-1 hash 
            hashCode = ByteBuffer.wrap(digest).getLong();
        }
        
        @Override
        public int hashCode() 
        {
            // simply truncate the 64 bit value
            return (int) hashCode;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Digest other = (Digest) obj;
            return this.hashCode == other.hashCode;    
        }
        
        private long hashCode;    
    }
}
