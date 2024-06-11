package com.smfreports.dcollect;

import java.io.*;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.json.EasySmfGsonBuilder;
import com.blackhillsoftware.smf.VRecordReader;
import com.google.gson.Gson;

/**
 * Print details of SMF data, storage and management 
 * classes in JSON format.
 * 
 */
public class SmsClasses 
{
    public static void main(String[] args) throws IOException     
    {
        if (args.length < 1)
        {
            System.out.println("Usage: SmsClasses <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
                
        try (VRecordReader reader = VRecordReader.fromName(args[0]))
        {
            Map<DcollectType, List<DcollectRecord>> smsClasses = 
                    reader.stream()
                .map(DcollectRecord::from) // create DCOLLECT record
                .filter(r -> 
                    r.dcurctyp().equals(DcollectType.DC)
                    || r.dcurctyp().equals(DcollectType.SC)
                    || r.dcurctyp().equals(DcollectType.MC))                     
                .collect(Collectors.groupingBy(r -> r.dcurctyp()));
        
            Gson gson = new EasySmfGsonBuilder()
                    // options to reduce the size of the output
                    .includeZeroValues(false)
                    .includeEmptyStrings(false)
                    .includeUnsetFlags(false)
                    // exclude some fields
                    .exclude("recordLength")
                    .exclude("dculeng")
                    
                    .setPrettyPrinting()
                    .createGson();
                    
            System.out.println(gson.toJson(smsClasses));        
        }
    }
}
