package com.smfreports.type30;
                                                               
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.blackhillsoftware.smf.*;                                                     
import com.blackhillsoftware.smf.smf30.*;                                             
                                                                                 
/**
 * Search for jobs with different job names but the same 
 * JES2 calculated JCL ID value.
 * For entries with more than one job name, print job details 
 * including system, time and job id.
 * 
 * This program requires memory for every job seen, but
 * this is unlikely to be a problem unless you are processing 
 * millions of jobs. (Very rough guesstimate, 100 bytes per job?)
 *
 */
public class JobsByJclIdDetail                                                                            
{                                                                                               
    public static void main(String[] args) throws IOException                                   
    {   
        if (args.length < 1)
        {
            System.out.println("Usage: JobsByJclIdDetail <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }

        // Map JCL id values to lists of job details. 
        Map<Token, List<JobDetail>> jobsByJclId = new HashMap<>();

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'       
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])
                .include(30,5)) 
        { 
            for (SmfRecord record : reader)
            {
                Smf30Record r30 = Smf30Record.from(record);

                // check the identification section is long enough for the JCL ID fields
                // (older records will be skipped) and the JCL ID has a value i.e. not all zeros
                if (!r30.header().smf30wid().equals("TSO") // not interesting
                        && r30.identificationSection().length() > IdentificationSection.SMF30ID_Len_V1
                        && !r30.identificationSection().smf30Jclid1().isZeros())
                {
                    // Add a new list of jobs if this is the first instance of the JCL ID value.
                    // Add the job details to the list
                    jobsByJclId.computeIfAbsent(r30.identificationSection().smf30Jclid1(), 
                            entry -> new ArrayList<>())
                        .add(new JobDetail(r30));
                }
            }                                                                         
        }
        
        // stream the JCL ID entries
        jobsByJclId.entrySet().stream()
            // filter out any with only 1 job
            .filter(jclIdEntry -> 
                jclIdEntry.getValue().size() > 1)
            // stream the list of jobs and count the distinct job name values
            // filter out entries with only one job name
            .filter(jclIdEntry -> 
                jclIdEntry.getValue().stream()
                    .map(job -> job.jobname)
                    .distinct()
                    .count() > 1)
            .forEach(jclIdEntry -> 
                {
                    // print the JCL ID value
                    System.out.format("%n%s%n", jclIdEntry.getKey());

                    // We will break the list down by job name, so create a map
                    // with the job details for this JCL ID value grouped by job name 
                    Map<String, List<JobDetail>> jobsByName = jclIdEntry.getValue().stream()
                            .collect(Collectors.groupingBy(JobDetail::getJobname));
                            
                    // stream the job name entries
                    jobsByName.entrySet().stream()
                        // sort by key, which is the job name
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(jobNameEntry -> 
                        {
                            // print the name
                            System.out.format("    %s%n", jobNameEntry.getKey());
                            
                            // print each job details entry, sorted by job time
                            jobNameEntry.getValue().stream()
                                .sorted(Comparator.comparing(JobDetail::getTime))
                                .forEachOrdered(job -> 
                                    System.out.format("        %-4s %-24s %-8s%n", 
                                            job.system,
                                            job.time,
                                            job.jobnumber
                                            ));
                        });
                });
        
        System.out.println("Done");
    }
    
    /**
     * Class to collect job detail information 
     *
     */
    private static class JobDetail
    {
        // Extract the details we are interested in
        JobDetail(Smf30Record r30)
        {
            jobname = r30.identificationSection().smf30jbn();
            jobnumber = r30.identificationSection().smf30jnm();
            system = r30.system();
            time = r30.smfDateTime();
        }

        // Most fields here are not encapsulated, just to reduce the amount
        // of code for you to read. There are methods for the jobname and
        // time fields because a method name simplifies the syntax of the 
        // comparing(... and groupingBy(... methods, and seems to be 
        // required if you want to add another field to the sort with 
        // thenComparing(...
        
        String getJobname() {
            return jobname;
        }

        LocalDateTime getTime() {
            return time;
        }

        String jobname;
        String jobnumber;
        String system;
        LocalDateTime time;   
    }
}                                                                                               