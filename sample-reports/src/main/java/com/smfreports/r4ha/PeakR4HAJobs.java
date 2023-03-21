package com.smfreports.r4ha;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.*;
import com.blackhillsoftware.smf.smf70.*;

/**
 * List the jobs by job name that used the most CPU time on each system 
 * in the 4 hours up to and including the top 5 4HRA MSU peaks.
 * MSU usage for each job name is also estimated by calculating the 
 * CPU time for the job name as a proportion of all CPU time seen for
 * those hours, and apportioning the SMF70LAC MSU value. This will not be 
 * totally accurate due to time not captured in type 30 records and 
 * jobs that don't write type 30.2 and 30.3 records e.g. system tasks
 * that write 30.6.   
 * 
 * The report requires SMF 30 data from the 4 hours up to each 4HRA peak
 * otherwise results will be incorrect. 
 * 
 * The report uses SMF 70 subtype 1 for SMF70LAC values, and SMF 30 subtypes 
 * 2 and 3 for interval CPU usage.
 * 
 * Only 1 pass of the data is required, and data does not need to be sorted.
 * 
 * The program gathers data using nested HashMaps. One set of Maps organizes
 * SMF70LAC by System and Hour. The second set of Maps organizes Job CPU 
 * totals by System, Hour and Job Name. The key for the hourly information is
 * the LocalDateTime truncated to the hour (retaining the date information).
 * 
 */

public class PeakR4HAJobs 
{
    public static void main(String[] args) throws IOException                                   
    {
        if (args.length < 1)
        {
            System.out.println("Usage: PeakR4HAJobs <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Create nested Maps so we have a hierarchy of 
        // System -> Hour -> SMF70LAC
        Map< String, 
            Map< LocalDateTime, 
                HourlyLac > > 
            systemHourLAC 
                    = new HashMap<String, Map<LocalDateTime, HourlyLac>>();
        
        // System -> Hour -> Jobname -> Totals
        Map< String, 
            Map< LocalDateTime, 
                Map< String, 
                    JobnameTotals > > > 
            systemHourJobnameTotals 
                        = new HashMap<String, Map<LocalDateTime, Map<String, JobnameTotals>>>();
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))
        { 
            reader
                .include(70,1)
                .include(30,2)
                .include(30,3); 
            
            // Accumulate type 30 and type 70 data
            for (SmfRecord record : reader)                                                     
            {
                switch (record.recordType())
                {
                case 70:
                    {
                        Smf70Record r70 = Smf70Record.from(record);
                        String system = r70.system();
                        LocalDateTime hour = r70.smfDateTime().truncatedTo(ChronoUnit.HOURS);
                        
                        systemHourLAC
                            // computeIfAbsent creates a new entry if the key is not found,
                            // otherwise returns the existing entry
                        
                            // Map of systems
                            .computeIfAbsent(system, key -> new HashMap<>())
                            // Nested map of hour -> hourly LAC for this system
                            .computeIfAbsent(hour, key -> new HourlyLac(system, hour))
                            .add(r70); // Add the record to the HourlyLac entry for this System:Time
                        break;
                    }
                case 30:    
                    {
                        Smf30Record r30 = Smf30Record.from(record);
                        String system = r30.system();
                        LocalDateTime hour = r30.smfDateTime().truncatedTo(ChronoUnit.HOURS);
                        String jobname = r30.identificationSection().smf30jbn();
                        
                        systemHourJobnameTotals
                            // System
                            .computeIfAbsent(system, key -> new HashMap<>())
                            // Hour
                            .computeIfAbsent(hour, key -> new HashMap<>())
                            // Job name
                            .computeIfAbsent(jobname, key -> new JobnameTotals(jobname))                     
                            .add(r30); // Add the record to the HourlyJobTotals entry for this System:Time:Jobname
                        break;
                    }
                default:
                    break;
                }
            }
        }
        
        // get a sorted list of system names
        List<String> systems = systemHourLAC.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
                
        for (String system: systems)
        {
            writeReport(system, systemHourLAC.get(system), systemHourJobnameTotals.get(system));
        }
        
    }

    private static void writeReport(
            String system,
            Map<LocalDateTime, HourlyLac> hourlyLAC,
            Map<LocalDateTime, Map<String, JobnameTotals>> hourlyJobTotals) 
    {
        System.out.format("%n%nSystem: %s%n", system); // header
    
        hourlyLAC.values().stream() // Information for each hour
            // Sort entries
            // comparing the average LAC for the hour, descending 
            .sorted(Comparator.comparingLong(HourlyLac::hourAverageLAC).reversed())
            .limit(5) // take the first (top) 5 entries
            .forEachOrdered(hourEntry -> // for each of the top hours
            {
                // write information about the hour
                LocalDateTime hour = hourEntry.getHour();
                long fourHourMSU = hourEntry.hourAverageLAC();

                System.out.format("%n    %-19s %21s%n", "Hour","4H MSU");
            
                System.out.format("    %10s %8s %21d%n", 
                        hour.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        hour.format(DateTimeFormatter.ISO_LOCAL_TIME),
                        fourHourMSU);

                // Get jobs for previous 4 hours
                List<JobnameTotals> fourHourJobs = new ArrayList<>();       
                for (int i = 0; i < 4; i++)
                {
                    if (hourlyJobTotals.containsKey(hour.minusHours(i)))
                    {
                        fourHourJobs.addAll(hourlyJobTotals.get(hour.minusHours(i)).values());
                    }
                }
                               
                // Calculate total CP time for all jobs during the 4 hours
                double fourHourTotalCpTime = 
                    fourHourJobs
                        .stream()
                        .collect(Collectors.summingDouble(JobnameTotals::getCpTime));
                
                report4HourTopCpJobs(fourHourMSU, fourHourJobs, fourHourTotalCpTime);
                report4HourTopZiipOnCpJobs(fourHourMSU, fourHourJobs, fourHourTotalCpTime);    
            });
    }

    private static void report4HourTopCpJobs(long msuvalue, List<JobnameTotals> fourHourJobs, double fourHourTotalCpTime) {
        // Heading
        System.out.format("%n        %-12s %11s %12s%n", 
                "Jobname", "CPU%", "Est. MSU");
        
        // Build and print detail lines      
        fourHourJobs
            .stream()
            // Each job name might have entries from multiple hours
            // Group by job name, and calculate sum of CP time for each job name 
            .collect(
                Collectors.groupingBy(JobnameTotals::getJobname, 
                        Collectors.summingDouble(JobnameTotals::getCpTime)))
            // process each job name
            .entrySet().stream()
            // sort job names by CP time, reversed to sort descending
            .sorted((jobATotal,jobBTotal) -> jobBTotal.getValue().compareTo(jobATotal.getValue()))
            // take top 5
            .limit(5) 
            // write detail lines
            .forEachOrdered(jobCpTime -> 
                System.out.format("        %-12s %10.1f%% %12.1f%n", 
                        // job name
                        jobCpTime.getKey(), 
                        // Average job CPU %
                        jobCpTime.getValue() / Duration.ofHours(4).getSeconds() * 100,
                        // Estimated MSU: 4 hour job CPU time / 4 hour all CPU time * 4 hour MSU 
                        jobCpTime.getValue() / fourHourTotalCpTime * msuvalue));
    }

    private static void report4HourTopZiipOnCpJobs(long msuvalue, List<JobnameTotals> fourHourJobs, double fourHourTotalCpTime) {
        // Heading
        System.out.format("%n%n        %-12s %11s %12s%n", 
                "Jobname", "zIIP On CP%", "Est. MSU");
        
        // Build and print detail lines      
        fourHourJobs
            .stream()
            // Each job name might have entries from multiple hours
            // Group by job name, and calculate sum of zIIP on CP time for each job name 
            .collect(
                Collectors.groupingBy(JobnameTotals::getJobname, 
                        Collectors.summingDouble(JobnameTotals::getZiipOnCpTime)))
            // process each job name
            .entrySet().stream()
            // sort job names by zIIP on CP time, reversed to sort descending
            .sorted((jobATotal,jobBTotal) -> jobBTotal.getValue().compareTo(jobATotal.getValue()))
            // take top 5
            .limit(5) 
            // write detail lines
            .forEachOrdered(jobCpTime -> 
                System.out.format("        %-12s %10.1f%% %12.1f%n", 
                        // job name
                        jobCpTime.getKey(), 
                        // Average job CPU %
                        jobCpTime.getValue() / Duration.ofHours(4).getSeconds() * 100,
                        // Estimated MSU: 4 hour zIIP on CP time / 4 hour all CPU time * 4 hour MSU 
                        jobCpTime.getValue() / fourHourTotalCpTime * msuvalue));
    }
    
    /**
     * A class to accumulate information for jobs with a particular jobname 
     *
     */
    private static class JobnameTotals
    {
        private double cpTime = 0;
        private double ziipOnCpTime = 0;
        private String jobname;
        
        /**
         * Constructor
         * @param jobname
         */
        public JobnameTotals(String jobname)
        {
            this.jobname = jobname;
        }
        
        /**
         * Accumulate information from a SMF record
         * @param r30 A type 30 record with information for this jobname
         */
        public void add(Smf30Record r30)
        {
            if (!jobname.equals(r30.identificationSection().smf30jbn()))        
            {
                throw new IllegalArgumentException("Wrong job name: " + r30.identificationSection().smf30jbn());
            }
            ProcessorAccountingSection pacct = r30.processorAccountingSection();
            if (pacct != null)
            {
                cpTime = cpTime
                    + pacct.smf30cptSeconds()
                    + pacct.smf30cpsSeconds()
                    + pacct.smf30icuSeconds()
                    + pacct.smf30isbSeconds()
                    + pacct.smf30iipSeconds()
                    + pacct.smf30rctSeconds()
                    + pacct.smf30hptSeconds()
                    ;
                ziipOnCpTime = ziipOnCpTime 
                    + pacct.smf30TimeZiipOnCpSeconds();
            }
        }
        
        public double getCpTime() { return cpTime; }
        public double getZiipOnCpTime() { return ziipOnCpTime; }
        public String getJobname() { return jobname; }
    }
    
    /**
     * Keep a weighted average smf70lac.
     * The average is weighted by the number of samples (smf70sam)
     * to eliminate the effect of short intervals.
     *
     */
    private static class HourlyLac
    {
        public HourlyLac(String system, LocalDateTime hour)
        {
            this.system = system;
            this.hour = hour;
        }
        private String system;
        private LocalDateTime hour;
        private long smf70lac = 0;
        private long smf70sam = 0;
        
        public String getSystem() { return system; }

        public LocalDateTime getHour() { return hour; }
        
        /**
         * Accumulate information from a SMF 70 record.
         * @param r70 the SMF type 70 record 
         */
        public void add(Smf70Record r70)
        {
            smf70lac += r70.cpuControlSection().smf70lac() * r70.productSection().smf70sam();
            smf70sam += r70.productSection().smf70sam();
        }
        
        /**
         * Get the weighted average LAC
         * @return weighted average, or 0 if we have no samples
         */
        public long hourAverageLAC()
        {
            return smf70sam > 0 ? smf70lac / smf70sam : 0;
        }
    }
}
