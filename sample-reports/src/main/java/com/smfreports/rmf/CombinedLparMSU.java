package com.smfreports.rmf;

import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf70.*;
import com.blackhillsoftware.smf.smf70.subtype1.*;
import com.blackhillsoftware.smf.summary.Top;

/*
 * This sample program calculates the combined MSU values for selected systems,
 * and reports the intervals with the highest values for both LAC (long term
 * average) and interval MSU.
 * 
 * Systems can be on the same CPC or on different CPCs. SMF 70 records are
 * required from each system to be reported.
 * 
 * The interval reported by the program can be the same as the RMF interval, or
 * can include multiple RMF intervals up to a maximum of 1 hour. (This limit
 * could be increased by changing the logic in the truncateTime method.)
 * 
 * To avoid the complexity of parsing command line arguments, various values
 * used by the program are hard coded at the start of the program.
 * 
 * The program is designed to be run under Java 11+ using the single file source
 * code feature i.e. without a separate compile step. The list of systems etc.
 * can simply be changed in the source code before running the program.
 * 
 * To run the program as a single file source code:
 * 
 * $ export CLASSPATH='/home/andrew/easysmf-je-2.2.1/jar/*'
 * $ java CombinedLparMSU.java <inputfile1> [inputfile2 ...]
 *
 */
public class CombinedLparMSU 
{
    // Data from the following systems will be combined to report
    // LAC/MSU values. All other systems will be excluded.
    private static Set<String> INCLUDESYSTEMS = new HashSet<>(
            Arrays.asList(
               // Uncomment to add names of specific systems      
               //  "SYSA" 
               // ,"SYSB"
               // ,"SYSC"
            ));
    
    // Length of the interval to report (max 60 minutes):
    private static int INTERVAL = 60;
    
    // Report TOP N intervals 
    private static int TOP_N = 20;
    
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: CombinedLparMSU <input-name> [<input-name> ...]");
            System.out.println("<input-name> can be filename or //'DATASET.NAME'");          
            return;
        }
        
        Map<LocalDateTime, Interval> intervals = new HashMap<>();
        Set<String> excludedSystems = new HashSet<>();
        
        for (String input : args) // allow multiple input files to be specified
        {
            try (SmfRecordReader reader = SmfRecordReader.fromName(input)
                    .include(70,1))
            {               
                for (SmfRecord record : reader)
                {
                    // check if we want this record
                    if (INCLUDESYSTEMS.isEmpty() 
                            || INCLUDESYSTEMS.contains(record.system()))
                    {      
                        Smf70Record r70 = Smf70Record.from(record);
                        
                        // Truncate (round down) the interval start time to a multiple of
                        // the specified interval minutes
                        LocalDateTime intervalTime =
                                truncateTime(
                                    r70.productSection().smf70dat()
                                        .atTime(r70.productSection().smf70ist()),
                                    INTERVAL);
                        
                        // create a new Interval if required and add data from the record
                        intervals
                            .computeIfAbsent(intervalTime, key -> new Interval())
                            .add(r70);
                    }
                    else
                    {  
                        excludedSystems.add(record.system());
                    }
                }
            }
        }

        writeReport(intervals, excludedSystems);
    }
    
    /**
     * Truncate (round down) a LocalDateTime to a multiple of interval.
     * If interval >= 60 it will truncate to 1 hour.
     * @param time the time to truncate
     * @param interval the number of minutes
     * @return the truncated time value
     */
    private static LocalDateTime truncateTime(LocalDateTime time, int interval)
    {
        // use integer truncation to get the minute value truncated to a multiple
        // of the interval value
        int minutes = (time.getMinute() / interval) * interval;
        return time.truncatedTo(ChronoUnit.HOURS).plusMinutes(minutes);
    }

    /**
     * Report the top N intervals by LAC and MSU
     * @param intervals the interval data
     * @param excludedSystems the list of systems that were excluded
     */
    private static void writeReport(Map<LocalDateTime, Interval> intervals, Set<String> excludedSystems) 
    {
        // Write top N intervals by LAC
        System.out.format("%nTop %d intervals by LAC:%n", TOP_N);
        
        intervals.entrySet().stream()   
            .collect(Top.values(TOP_N, 
                    Comparator.comparingDouble(entry -> entry.getValue().totalLac())))
            .forEach(entry -> 
            {
                printInterval(entry);                
            });

        // Write top N intervals by MSU
        System.out.format("%nTop %d intervals by MSU:%n", TOP_N);
        
        intervals.entrySet().stream()   
            .collect(Top.values(TOP_N, 
                    Comparator.comparingDouble(entry -> entry.getValue().totalMsu())))
            .forEach(entry -> 
            {
                printInterval(entry);                
            });
        
        // Report any systems excluded (helpful if all systems are accidentally excluded!) 
        if (!excludedSystems.isEmpty())
        {
            System.out.format("%nThe following systems were excluded:%n%s%n", 
                    excludedSystems.toString());
        }
    }
    
    /**
     * Print the interval data 
     * @param intervalEntry the interval time and data 
     */
    private static void printInterval(Entry<LocalDateTime, Interval> intervalEntry) 
    {
        // Headings
        System.out.format("%n%-25s %7s %7s%n", 
                intervalEntry.getKey(), "LAC", "MSU");
        
        // Write values for each system
        intervalEntry.getValue().systems().stream()
            .sorted(Map.Entry.comparingByKey()) // sort by system name
            .forEach(system -> 
            {
                System.out.format("%-25s %7d %7.0f%n", 
                        system.getKey(), 
                        system.getValue().lac(), 
                        system.getValue().msu());
            }); 
        
        // Write totals for interval
        System.out.format("%n%-25s %7d %7.0f%n", "Total", 
                intervalEntry.getValue().totalLac(), 
                intervalEntry.getValue().totalMsu());
    }
    
    /**
     * A class to collect the data for multiple systems for an interval
     */
    private static class Interval
    {
        private Map<String, IntervalSystem> systems = new HashMap<>();
        
        /**
         * Add data from a SMF 70 record 
         * @param r70 the SMF 70 record
         */
        public void add(Smf70Record r70)
        {
            // find existing system entry or create new, and add data
            systems.computeIfAbsent(r70.system(), key -> new IntervalSystem())
                .add(r70);
        }
        
        public Set<Entry<String, IntervalSystem>> systems()
        {
            return systems.entrySet();
        }
        

        /*
         * Calculate totals. This will be called multiple times but probably not
         * excessively. If we were to e.g. use these values to sort the whole list it
         * would be worth storing the calculated value
         */
        public double totalMsu() { 
            return systems.values().stream()
                .collect(Collectors.summingDouble(IntervalSystem::msu)); 
        }
        public long totalLac() { 
            return systems.values().stream()
                .collect(Collectors.summingLong(IntervalSystem::lac));     
        }
    }
    
    /**
     * A class to collect the information for a system for the specified interval.
     * 
     * The interval is not the RMF interval - it is the interval specified at the 
     * beginning of this program. That may be the same as the RMF interval length
     * however values will be truncated to the exact minute.
     * 
     * The interval specified may include more than one RMF interval, and the
     * RMF interval length may vary (e.g. short intervals)
     *
     */
    private static class IntervalSystem
    {   
        private long maxLac = 0;
        private double cumulativeMsu = 0;
        
        /*
         * We need to know the total length of RMF intervals used to collect
         * the MSU data to calculate a MSU/hour value.
         * We might see an interval multiple times, (multiple type 70 records) 
         * so we will keep a map of interval lengths with interval expiration token 
         * as the key so we end up with one entry per interval.
         */        
        private Map<ZonedDateTime, Double> rmfIntervals = new HashMap<>();
        
        /**
         * Add data from a SMF 70 record 
         * @param r70 the SMF 70 record
         */
        public void add(Smf70Record r70)
        {
            // There are various ways we could combine multiple smf70lac values from a system 
            // e.g. (average, last, max).
            // Probably doesn't make much difference so we'll just use the max value
            maxLac = Math.max(maxLac, r70.cpuControlSection().smf70lac());
            
            // Add (or replace) the RMF interval length for this SMF70IET token
            rmfIntervals.put(r70.productSection().smf70iet(), 
                    r70.productSection().smf70intSeconds());            
            
            // Find the partition for the system that wrote the record. This means we 
            // need data from every system, but avoids dealing with data for the same 
            // LPAR from different systems and figuring out which to use.
            PrismPartitionDataSection myPartition = r70.prismPartitionDataSections().stream()
                    .filter(x -> x.smf70lpn() == r70.productSection().smf70ptn()
                            // smf70lpn doesn't match smf70ptn under VM - the following
                            // test works at least sometimes under VM and shouldn't cause
                            // errors in other cases
                            || (r70.prismPartitionDataSections().size() == 2 
                                    && !x.smf70lpm().equals("PHYSICAL")
                                    && r70.prismPartitionDataSections().get(1).smf70lpm().equals("PHYSICAL"))
                            )
                    .findFirst()
                    .orElse(null);
            
            if (myPartition != null) // if we found the partition (otherwise nothing to do)
            {
                // get the logical processor data sections, skip to the entries for this partition 
                // and loop through them based on smf70bds and smf70bdn
                List<PrismLogicalProcessorDataSection> processors = r70.prismLogicalProcessorDataSections();
                for (int i = (int)myPartition.smf70bds(); i < myPartition.smf70bdn(); i++)
                {
                    // check the CPU type from smf70cix indexing into the CPU identification section
                    if(r70.cpuIdentificationSections().get(
                                    processors.get(i).smf70cix()-1
                                    )
                            .smf70cin().equals("CP"))
                    {
                        // calculate msu for this processor based on effective dispatched time
                        cumulativeMsu += processors.get(i).smf70edtSeconds()
                                * 16 
                                / ((double)r70.cpuControlSection().smf70cpaActual() 
                                        / r70.cpuControlSection().smf70cpaScalingFactor()); 
                    }
                }
            }            
        }

        /**
         * LAC value for this interval (maximum value for the interval)
         * @return long LAC value
         */
        public long lac() { return maxLac; }  
        
        /**
         * MSU value for this interval (cumulative MSU scaled to 1 hour)
         * @return double MSU value
         */
        public double msu()
        {
            /*
             * Take the cumulative MSU and scale it to 1 hour based on the 
             * total length of the intervals that were used to calculate the
             * cumulative MSU.
             */
            double totalseconds = 0;
            for (Double seconds : rmfIntervals.values())
            {
                totalseconds += seconds.doubleValue();
            }
            return (cumulativeMsu / totalseconds * 3600);
        }
    }
}
