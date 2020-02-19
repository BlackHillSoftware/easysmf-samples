package com.smfreports.type30;

import java.io.IOException;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.Smf30Record;

/**
 * Summarize Job statistics by job name
 *
 */
public class JobsByJobname
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: JobsByJobname <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
    	
        // A map of Job Names to JobData entries to collect information about each
        // group of jobs.

        Map<String, JobData> jobs = new HashMap<String, JobData>();

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
    	
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))                
        {
        	// SMF 30 subtype 5 = Job End records
        	reader.include(30,5);
        	
        	for (SmfRecord record : reader)
        	{
        		Smf30Record r30 = Smf30Record.from(record); 
        	
                // Optionally filter here, e.g. to include only jobs running in job class A:
                // if (r30.identificationSection().smf30cl8().equals("A"))
                // {

        		// computeIfAbsent : returns an existing entry or a new entry created 
        		// using the supplied function  
                JobData jobentry = jobs
                	.computeIfAbsent(
                			r30.identificationSection().smf30jbn(), // jobname
                			x -> new JobData());
                jobentry.accumulateData(r30);                 
        	}
        }
        writeReport(jobs);
    }

    /**
     * Write the report
     * 
     * @param jobs
     *            The map of Job Names to Job Data
     */
    private static void writeReport(Map<String, JobData> jobs)
    {
        // Headings
        System.out.format("%n%-8s %6s %14s %14s %14s %14s %14s %14s %14s %14s%n", 
            "Jobname", "Count", "CPU", "zIIP",
            "Connect", "Excp", "Avg CPU", "Avg zIIP",
            "Avg Connect", "Avg Excp");

        jobs.entrySet().stream()
            // If multiple SMF records were written for a job but we 
            // didn't see the first record, count might be 0. Ignore those jobs.
            .filter(entry -> entry.getValue().count > 0)
            // sort by CP Time
            // reversing a and b in the comparison so sort is descending
            .sorted((a, b) -> Double.compare(b.getValue().cpTime, a.getValue().cpTime))
            .limit(100) // take top 100
            .forEachOrdered(entry ->
            {
                String jobname = entry.getKey();
                JobData jobinfo = entry.getValue();
                // write detail line
                System.out.format("%-8s %,6d %14s %14s %14s %,14d %14s %14s %14s %,14d%n", 
                    jobname,
                    jobinfo.count, 
                    hhhmmss(jobinfo.cpTime), 
                    hhhmmss(jobinfo.ziipTime),
                    hhhmmss(jobinfo.connectTime), 
                    jobinfo.excps, hhhmmss(jobinfo.cpTime / jobinfo.count),
                    hhhmmss(jobinfo.ziipTime / jobinfo.count), 
                    hhhmmss(jobinfo.connectTime / jobinfo.count),
                    jobinfo.excps / jobinfo.count);
            });
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

    /**
     * A class to accumulate information about a group of jobs.
     */
    private static class JobData
    {
        /**
         * Add information from a SMF 30 record.
         * One job can have many SMF records so we might get called multiple times
         * for the same job, but some of the SMF sections will occur only
         * once per job e.g. ProcessorAccountingSection.
         * 
         * @param r30
         *            The Smf30Record
         */
        public void accumulateData(Smf30Record r30)
        {
            if (r30.processorAccountingSection() != null)
            {
                count++; // pick a section that only occurs once and use to count jobs
                cpTime += r30.processorAccountingSection().smf30cptSeconds()
                    + r30.processorAccountingSection().smf30cpsSeconds();
                ziipTime += r30.processorAccountingSection().smf30TimeOnZiipSeconds();
            }
            if (r30.ioActivitySection() != null)
            {
                excps += r30.ioActivitySection().smf30tex();
                connectTime += r30.ioActivitySection().smf30aicSeconds();
            }
        }

        int    count       = 0;
        double cpTime      = 0;
        double ziipTime    = 0;
        double connectTime = 0;
        long   excps       = 0;
    }   
}
