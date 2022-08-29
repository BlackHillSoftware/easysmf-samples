package com.smfreports;

import java.io.IOException;

import com.blackhillsoftware.json.EasySmfGsonBuilder;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf30.Smf30Record;
import com.google.gson.Gson;

/**
 * 
 * This sample demonstrates writing a complete SMF record to JSON format.
 * It looks at SMF 30 subtype 4 (step end) records, and prints the data from
 * the first record found where the program name is IEFBR14 (to minimize the
 * amount of data printed by the sample). 
 *
 */

public class Smf30RecordToJson 
{
    public static void main(String[] args) throws IOException                                   
    {
        Gson gson = new EasySmfGsonBuilder()
                .avoidScientificNotation() // make decimals more readable
                .setPrettyPrinting()       // pretty printing = human readable
                .createGson();
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])
                .include(30, 4)) 
        {
            reader.stream()
                .map(record -> Smf30Record.from(record))
                .filter(r30 -> r30.identificationSection()
                        .smf30pgm()
                        .equals("IEFBR14"))
                .limit(1)
                .forEach(r30 -> 
                    System.out.println(gson.toJson(r30))
                    );                                                 
        }
        System.out.println("Done");
    }
}
