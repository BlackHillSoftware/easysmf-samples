package com.smfreports;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;

/**
 *
 * Collect statistics for SMF records by type and subtype,
 * and print record count, megabytes and various statistics 
 * for each type/subtype 
 *
 */
public class RecordCount
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: RecordCount <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Use nested Maps to keep the statistics
        // Keys are SMF record type and subtype
        Map<Integer, Map<Integer, RecordStats>> statsMap = new HashMap<>();
        
        RecordStats totals = new RecordStats(-1, -1);
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))
        { 
            for (SmfRecord record : reader)
            {
                totals.add(record);
                int type = record.recordType();
                int subtype = record.hasSubtypes() ? record.subType() : 0;
                
                statsMap.computeIfAbsent(type, key -> new HashMap<>())
                    .computeIfAbsent(subtype, key -> new RecordStats(type, subtype))
                    .add(record);
            }
        }

        writeReport(statsMap, totals);
    }

    private static void writeReport(Map<Integer, Map<Integer, RecordStats>> statsMap, RecordStats totals)
    {
        // get the total bytes from all record types
        long totalbytes = statsMap.entrySet().stream()       // get Map entries (keyed by SMF type)
            .map(entry -> entry.getValue())                  // get inner Maps (keyed by subtype)
            .flatMap(entry -> entry.entrySet().stream())     // flatten Map contents into one stream         
            .mapToLong(entry -> entry.getValue().getBytes()) // Value is RecordStats entry 
            .sum();                                          // Sum total bytes for all records

        // write heading
        System.out.format("%5s %8s %11s %11s %7s %9s %9s %9s %n", 
            "Type", "Subtype", "Records", "MB", "Pct", "Min", "Max", "Avg");

        // write data
        statsMap.entrySet().stream()                     // get Map entries (keyed by SMF type)
            .map(entry -> entry.getValue())              // get inner Maps (keyed by subtype)
            .flatMap(entry -> entry.entrySet().stream()) // flatten Map contents into one stream 
            .map(entry -> entry.getValue())              // get value (RecordStats entry)
            // sort by total bytes descending
            .sorted(Comparator.comparingLong(RecordStats::getBytes).reversed())

            // alternative sort, by type and subtype
            // .sorted(Comparator
            // .comparingInt(RecordStats::getRecordtype)
            // .thenComparingInt(RecordStats::getSubtype))

            .forEachOrdered(entry -> 
                System.out.format("%5d %8d %11d %11d %7.1f %9d %9d %9d %n", 
                    entry.getRecordtype(),
                    entry.getSubtype(), 
                    entry.getCount(), 
                    entry.getBytes() / (1024 * 1024),              
                    (float) (entry.getBytes()) / totalbytes * 100, 
                    entry.getMinLength(), 
                    entry.getMaxLength(),
                    entry.getBytes() / entry.getCount()));
                            
        System.out.format("%n%-14s %11d %11d %7.1f %9d %9d %9d %n",
                "Total:",
                totals.getCount(), 
                totals.getBytes() / (1024 * 1024),             
                (float) (totals.getBytes()) / totalbytes * 100, 
                totals.getMinLength(), 
                totals.getMaxLength(),
                totals.getBytes() / totals.getCount());
        
    }

    /**
     * Statistics for a type/subtype combination
     */
    private static class RecordStats
    {
        /**
         * Initialize statistics for a new record type/subtype combination.
         * 
         */
        RecordStats(int recordType, int subType)
        {
            this.recordtype = recordType;
            this.subtype = subType;
        }

        /**
         * Add a record to the statistics
         * 
         * @param record a SMF record
         */
        public void add(SmfRecord record)
        {
            count++;
            int length = record.recordLength();
            bytes += length;
            minLength = (minLength == 0 || length < minLength) ? length : minLength;
            maxLength = length > maxLength ? length : maxLength;
        }

        private int  recordtype;
        private int  subtype;
        private int  count = 0;
        private long bytes = 0;
        private int  maxLength = 0;
        private int  minLength = 0;

        int getRecordtype() { return recordtype; }
        int getSubtype()    { return subtype; }
        int getCount()      { return count; }
        long getBytes()     { return bytes; }
        int getMaxLength()  { return maxLength; }
        int getMinLength()  { return minLength; }    
    }
}
