package com.smfreports.json;

import java.io.*;
import java.time.*;

import com.blackhillsoftware.json.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.*;
import com.google.gson.*;

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
                .avoidScientificNotation(true) // make decimals more readable
                .createGson();
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])
                .include(30, 5)) 
        {
            reader.stream()
                .map(record -> Smf30Record.from(record))
                .filter(r30 -> r30.completionSection() != null)
                .forEach(r30 -> 
                {
                    JobInfo job = new JobInfo(r30);
                    System.out.println(gson.toJson(job));
                });                                                 
        }
        System.out.println("Done");
    }
    
    // A class to collect the information required from the record.
    static class JobInfo
    {
        JobInfo(Smf30Record r30)
        {
            // Specific fields
            time = r30.smfDateTime();
            system = r30.system();
            jobname = r30.identificationSection().smf30jbn();
            jobid = r30.identificationSection().smf30jnm();
            userid = r30.identificationSection().smf30rud();
            // Whole sections
            completion = r30.completionSection();
            processorAccounting = r30.processorAccountingSection();
        }
        
        LocalDateTime time;
        String system;
        String jobname;
        String jobid;
        String userid;
        CompletionSection completion;
        ProcessorAccountingSection processorAccounting;
    }   
}
