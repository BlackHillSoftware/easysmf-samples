package com.smfreports.zedc;

import java.io.IOException;
import java.time.*;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.*;

/**
 * 
 * Find jobs where there are instances of the same jobname with and without zEDC
 * compression, and compare elapsed time, CPU and I/O statistics for the job
 * with and without zEDC.
 *
 */

public class ZedcBeforeAfter 
{
    public static void main(String[] args) throws IOException                                   
    {
        if (args.length < 1)
        {
            System.out.println("Usage: ZedcBeforeAfter <input-name>");
            System.out.println(
                    "<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // Maps of job names to JobInfo entries to collect information about each
        // group of jobs.

        Map<String, JobInfo> zedc = new HashMap<String, JobInfo>();
        Map<String, JobInfo> noZedc = new HashMap<String, JobInfo>();
        
        try (SmfRecordReader reader = 
                SmfRecordReader
                    .fromName(args[0])
                    .include(30,4))
        {
            reader.stream()
                .map(record -> Smf30Record.from(record))
                // assume zedc section is in the same record as completion section
                .filter(r30 -> r30.completionSection() != null)
                .filter(r30 -> r30.header().smf30wid().equals("JES2"))
                .forEach(r30 ->
                {
                    if (r30.zEdcUsageStatisticsSections().isEmpty())
                    {
                        noZedc.computeIfAbsent(
                                r30.identificationSection().smf30jbn(), 
                                jobName -> new JobInfo(jobName))    
                            .add(r30);     
                    }
                    else
                    {
                        zedc.computeIfAbsent(
                                r30.identificationSection().smf30jbn(), 
                                jobName -> new JobInfo(jobName))    
                            .add(r30);                            
                    }
                });         
        }
        
        String headingFormat = "%-8s %6s %9s %9s %9s %9s %9s %10s %8s %10s %8s%n";
        String detailFormat =  "%-8s %,6d %9.1f %9.2f %9.2f %9.2f %9d%n";
        String zedcDetailFormat =  "%-8s %,6d %9.1f %9.2f %9.2f %9.2f %9d %,10d %6.1f:1 %,10d %6.1f:1%n";
        String deltaFormat =  "%-8s %6s %+8.0f%% %+8.0f%% %+8.0f%% %+8.0f%% %+8.0f%%%n%n";
        
        System.out.format(headingFormat,
                "Jobname",
                "Count",               
                "Ave Elap",
                "Avg CP",
                "Avg zIIP",
                "Avg Conn",
                "Avg EXCP",                
                "Avg Rd MB",
                "Ratio",
                "Avg Wr MB",
                "Ratio"
                );                    

        // take job names from zEDC list, and retain all that also appear
        // in no zEDC list i.e. intersection of sets 
        Set<String> reportJobs = new HashSet<>(zedc.keySet());
        reportJobs.retainAll(noZedc.keySet());

        reportJobs.stream()
            .sorted()
            .forEachOrdered(jobname -> 
            {
                JobInfo dataset = noZedc.get(jobname);
                System.out.format(detailFormat,
                        dataset.getJobname(),
                        dataset.count,
                        dataset.avgElapsed(),
                        dataset.avgCp(),
                        dataset.avgZiip(),
                        dataset.avgConnect(),
                        dataset.avgExcp());
                
                JobInfo zedcDataset = zedc.get(jobname);
                System.out.format(zedcDetailFormat,
                        " ZEDC:",
                        zedcDataset.count,
                        zedcDataset.avgElapsed(),
                        zedcDataset.avgCp(),
                        zedcDataset.avgZiip(),                        
                        zedcDataset.avgConnect(),
                        zedcDataset.avgExcp(),
                        zedcDataset.avgUncompRead() / (1024 * 1024),
                        zedcDataset.readCompRatio(),
                        zedcDataset.avgUncompWrite() / (1024 * 1024),
                        zedcDataset.writeCompRatio());
                System.out.format(deltaFormat,
                        "",
                        "",
                        changePct(dataset.avgElapsed(),zedcDataset.avgElapsed()),
                        changePct(dataset.avgCp(),zedcDataset.avgCp()),
                        changePct(dataset.avgZiip(),zedcDataset.avgZiip()),
                        changePct(dataset.avgConnect(),zedcDataset.avgConnect()),
                        changePct(dataset.avgExcp(),zedcDataset.avgExcp()));
                
            });

        System.out.println("Done");                      
    }
    
    static double changePct(double before, double after)
    {
        if (before == 0) return 0;
        return (after - before) / before * 100;
    }
    
    static class JobInfo
    {
        String jobname;
        int count = 0;
        long compRead = 0;
        long uncompRead = 0;
        long compWrite = 0;
        long uncompWrite = 0;
        double elapsedSeconds = 0;
        double connectSeconds = 0;
        double cpSeconds = 0;
        double ziipSeconds = 0;
        long excp = 0;
        
        JobInfo(String datasetName)
        {
            jobname  = datasetName;
        }

        void add(Smf30Record r30)
        {
            count++;
            LocalDateTime start = r30.identificationSection().smf30std()
                    .atTime(r30.identificationSection().smf30sit());
            
            elapsedSeconds += 
                    (double)(Duration.between(start, r30.smfDateTime())
                            .toMillis()) / 1000;
            if (r30.ioActivitySection() != null)
            {
                excp += r30.ioActivitySection().smf30tex();
                connectSeconds += r30.ioActivitySection().smf30tcnSeconds(); 
            }
            if (r30.processorAccountingSection() != null)
            {
                cpSeconds += r30.processorAccountingSection().smf30cptSeconds() 
                        + r30.processorAccountingSection().smf30cpsSeconds();
                ziipSeconds += r30.processorAccountingSection().smf30TimeOnZiipSeconds();
            }  
            if (!r30.zEdcUsageStatisticsSections().isEmpty())
            {
                ZEdcUsageStatisticsSection zedc = r30.zEdcUsageStatisticsSections().get(0);
                compRead += zedc.smf30UsInfComprIn();
                uncompRead += zedc.smf30UsInfDecomprOut();
                uncompWrite += zedc.smf30UsDefUncomprIn();
                compWrite += zedc.smf30UsDefComprOut();
            }
        }
        
        String getJobname() {
            return jobname;
        }
        
        double avgElapsed() { return elapsedSeconds / count; }
        double avgConnect() { return connectSeconds / count; }
        double avgCp()     { return cpSeconds / count; }
        double avgZiip()     { return ziipSeconds / count; }
        long avgExcp()    { return excp / count; }
        long avgUncompRead() { return uncompRead / count; }
        long avgUncompWrite() { return uncompWrite / count; }
        
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
    }
}
