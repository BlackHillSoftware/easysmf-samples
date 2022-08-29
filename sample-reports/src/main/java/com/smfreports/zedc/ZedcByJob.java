package com.smfreports.zedc;

import java.io.IOException;
import java.util.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.*;

public class ZedcByJob 
{
    /**
    *
    * List compression statistics for jobs using zEDC compression. 
    *
    */
    public static void main(String[] args) throws IOException                                   
    {
        if (args.length < 1)
        {
            System.out.println("Usage: ZedcByJob <input-name>");
            System.out.println(
                    "<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // A map of job names to JobInfo entries to collect information about each
        // group of jobs.
        Map<String, JobInfo> jobs = new HashMap<String, JobInfo>();
        
        try (SmfRecordReader reader = 
                SmfRecordReader
                    .fromName(args[0])
                    .include(30,5))
        {
            for (SmfRecord record : reader)
            {
                Smf30Record r30 = Smf30Record.from(record);
                if (!r30.zEdcUsageStatisticsSections().isEmpty())
                {
                    jobs.computeIfAbsent(
                            r30.identificationSection().smf30jbn(), 
                            jobname -> new JobInfo(jobname))
                        .add(r30);                                    
                }
            }         
        }
        
        String headingFormat = "%-8s %8s %13s %13s %8s %13s %13s %8s%n";
        String detailFormat =  "%-8s %,8d %,13d %,13d %6.1f:1 %,13d %,13d %6.1f:1%n";
        
        System.out.format(headingFormat,
                "Jobname",
                "Count",
                "RD Comp MB",
                "Uncomp MB",
                "Ratio",
                "WR Uncomp MB",
                "Comp MB",
                "Ratio"
                );
        
        // Write report
        jobs.entrySet()
            .stream()
            .map(entry -> entry.getValue())
            .sorted(Comparator.comparing(JobInfo::getJobname))
            .forEachOrdered(jobname ->
            {
                System.out.format(detailFormat,
                        jobname.getJobname(),
                        jobname.count,
                        jobname.compRead / (1024 * 1024),
                        jobname.uncompRead / (1024 * 1024),
                        jobname.readCompRatio(),
                        jobname.uncompWrite / (1024 * 1024),
                        jobname.compWrite / (1024 * 1024),
                        jobname.writeCompRatio());
            });
        System.out.println("Done");                      
    }
    
    static class JobInfo
    {
        String jobname;
        int count = 0;
        long compRead = 0;
        long uncompRead = 0;
        long compWrite = 0;
        long uncompWrite = 0;
        
        JobInfo(String jobName)
        {
            this.jobname  = jobName;
        }
        
        void add(Smf30Record r30)
        {
            count++;
            ZEdcUsageStatisticsSection zedc = r30.zEdcUsageStatisticsSections().get(0);
            compRead += zedc.smf30UsInfComprIn();
            uncompRead += zedc.smf30UsInfDecomprOut();
            uncompWrite += zedc.smf30UsDefUncomprIn();
            compWrite += zedc.smf30UsDefComprOut();
        }
        
        float readCompRatio()
        {
            if (compRead == 0) return 0;
            return (float) uncompRead / compRead;
        }
     
        float writeCompRatio()
        {
            if (compWrite == 0) return 0;
            return (float) uncompWrite / compWrite;
        }

        String getJobname() 
        {
            return jobname;
        }
    }
}
