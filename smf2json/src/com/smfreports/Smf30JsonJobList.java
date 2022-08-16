package com.smfreports;

import java.io.IOException;
import java.time.LocalDateTime;

import com.blackhillsoftware.json.EasySmfGsonBuilder;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf30.*;
import com.google.gson.Gson;

/**
 * 
 * This sample demonstrates writing information from SMF records 
 * to JSON format.
 * 
 * It reads SMF 30 subtype 5 (job end) records, and prints selected
 * data and sections from the records.
 * 
 * Data is collected into a separate class to show how to mix
 * specific fields and complete sections in the JSON. 
 *
 */

public class Smf30JsonJobList 
{
    public static void main(String[] args) throws IOException                                   
    {
        Gson gson = new EasySmfGsonBuilder()
                .avoidScientificNotation() // make decimals more readable
                .createGson();
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])
                .include(30, 5)) 
        {
            reader.stream()
                .map(record -> Smf30Record.from(record))
                .filter(r30 -> r30.completionSection() != null)
                .filter(r30 -> !r30.header().smf30wid().equals("OMVS"))
                .filter(r30 -> r30.identificationSection().smf30jbn().startsWith("ANDREWR"))
                .filter(r30 -> r30.identificationSection().smf30rud().startsWith("ANDREWR"))
                .limit(5)
                .forEach(r30 -> 
                {
                    // build JobInfo with the information required
                    JobInfo job = new JobInfo();
                    // Specific fields
                    job.time = r30.smfDateTime();
                    job.system = r30.system();
                    job.jobname = r30.identificationSection().smf30jbn();
                    job.jobid = r30.identificationSection().smf30jnm();
                    job.userid = r30.identificationSection().smf30rud();
                    // Whole sections
                    job.completion = r30.completionSection();
                    job.processorAccounting = r30.processorAccountingSection();
                    
                    System.out.println(gson.toJson(job));
                });                                                 
        }
        System.out.println("Done");
    }
    
    // A class to collect the information required from the record.
    static class JobInfo
    {
        LocalDateTime time;
        String system;
        String jobname;
        String jobid;
        String userid;
        CompletionSection completion;
        ProcessorAccountingSection processorAccounting;
    }   
}
