package com.smfreports.type30;

import java.io.IOException;
import java.time.*;
import java.util.*;

import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf30.ProcessorAccountingSection;
import com.blackhillsoftware.smf.smf30.Smf30Record;

/**
 * 
 * Report the jobs/tasks that used the most CPU by job name 
 * during prime shift for each day of the week.  
 * 
 */
public class PrimeShiftTopJobs
{
    // First day of the week is DayOfWeek.MONDAY
    // We could test >= Monday && <= Friday, which works as long as 
    // we don't include the Sunday-Monday boundary.
    // Listing days specifically in an array is more tolerant of
    // varied combinations of days.
    
    static final List<DayOfWeek> primeDays = 
        Arrays.asList( DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY );
    static final LocalTime primeStartTime = LocalTime.of(8, 30);
    static final LocalTime primeEndTime = LocalTime.of(17, 30);
    
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: PrimeShiftTopJobs <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
    	
        // Create nested maps, DayOfWeek -> Jobname -> Job Data to collect information            
        Map<DayOfWeek, HashMap<String, JobData>> jobsByDay = new HashMap<DayOfWeek, HashMap<String, JobData>>();

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
    	
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))
        { 
            reader
                // collect data from subtype 2 interval, and subtype 3 last interval
                .include(30, 2)
                .include(30, 3)
                .stream()
                .filter(record -> primeDays.contains(record.smfDate().getDayOfWeek())
                    && record.smfTime().isAfter(primeStartTime)
                    && record.smfTime().isBefore(primeEndTime))
                .map(record -> Smf30Record.from(record))
                .forEach(r30 ->
                {
                    jobsByDay
                        // Find entry for day of week or add new entry
                        .computeIfAbsent(r30.smfDate().getDayOfWeek(), day -> new HashMap<>())
                        // Find entry for job name or add new entry 
                        .computeIfAbsent(r30.identificationSection().smf30jbn(), job -> new JobData())
                        .add(r30);                        
                });
        }

        writeReport(jobsByDay);
    }

    /**
     * Write the report from the collected data
     * @param dailyJobs nested maps : DayOfWeek -> Job Name -> Job Data
     */
    private static void writeReport(Map<DayOfWeek, HashMap<String, JobData>> dailyJobs)
    {
        dailyJobs.entrySet().stream()
            // Days of the week : sort ascending
           .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
           .forEachOrdered(day -> 
           {
               // calculate total CPU for the day
               double totalDayCp = day.getValue().entrySet().stream()
                       .mapToDouble(job -> job.getValue().cpTime)
                       .sum();
               // Headings
               System.out.format("%n%s%n", day.getKey().toString());
               System.out.format("%-8s %11s %5s %11s%n", "Name", "CPU",
                       "CPU%", "zIIP");
               
               day.getValue().entrySet().stream()
                   // sort jobs by CPU Time, the comparison is reversed to sort descending
                   .sorted((a, b) -> Double.compare(b.getValue().cpTime, a.getValue().cpTime))
                   // take top 10 and print information
                   .limit(10)
                   .forEachOrdered(entry ->
                       {
                           JobData jobinfo = entry.getValue();    
                           // write detail line     
                           System.out.format(
                                   "%-8s %11s %4.0f%% %11s%n",
                                   entry.getKey(), // key is jobname
                                   hhhmmss(jobinfo.cpTime),
                                   jobinfo.cpTime / totalDayCp * 100,
                                   hhhmmss(jobinfo.ziipTime));      
                       });               
           });
    }
   
    /**
     * Class to collect information about a group of jobs.
     */
    private static class JobData
    {
        /**
         * Add information from a SMF type 30 record.
         * @param r30 The SMF record.
         */
        
        public void add(Smf30Record r30)
        {
            ProcessorAccountingSection procAcct = r30.processorAccountingSection();
            if (procAcct != null)   
            {
                cpTime += procAcct.smf30cptSeconds() 
                    + procAcct.smf30cpsSeconds();
                ziipTime += procAcct.smf30TimeOnZiipSeconds();
            }
        }

        double cpTime = 0;
        double ziipTime = 0;
    }

    /**
     * Format seconds as hhh:mm:ss. Seconds value is reported
     * to 2 decimal places.
     * 
     * @param totalseconds
     * @return The formatted value.
     */
    private static String hhhmmss(double totalseconds)
    {
        final int SECONDS_PER_MINUTE = 60;
        final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;

        int hours = (int) (totalseconds / SECONDS_PER_HOUR);
        int minutes = (int) ((totalseconds % SECONDS_PER_HOUR)) / SECONDS_PER_MINUTE;
        double seconds = totalseconds % SECONDS_PER_MINUTE;

        return String.format("%d:%02d:%05.2f", hours, minutes, seconds);
    }
    
}
