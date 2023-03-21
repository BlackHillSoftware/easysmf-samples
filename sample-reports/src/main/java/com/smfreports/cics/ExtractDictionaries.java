package com.smfreports.cics;

import java.io.*;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.SmfRecordWriter;
import com.blackhillsoftware.smf.cics.Smf110Record;

public class ExtractDictionaries
{
    private static void printUsage() {
        System.out.println("Usage: ExtractDictionaries input-file output-file");
        System.out.println("");
        System.out.println("Search for CICS Dictionaries in input-file and write them to output-file");
        System.out.println("");
        System.out.println("  input-file   File containing SMF records. Binary data, RECFM=U or V[B]");
        System.out.println("               including RDW.");
        System.out.println("  output-file  Output-file for dictionaries.");
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length < 2 || args[0].equals("--help") || args[0].equals("-h"))
        {
            printUsage();
            System.exit(0);
        }
                
        // Open reader and writer classes
        try (
            SmfRecordReader reader = SmfRecordReader.fromName(args[0]);                
            SmfRecordWriter writer = SmfRecordWriter.fromName(args[1])
            )        
        {
            reader.include(110, 1);
            int in = 0;
            int out = 0;
            for (SmfRecord record : reader)
            {
                Smf110Record r110 = Smf110Record.from(record);
                in++;
                if (r110.mnProductSection().monitoringClassDictionary())
                {
                    out++;
                    writer.write(record);
                }
            }
            System.out.format("Finished, %d records in, %d records out.%n", in, out);            
        }

        catch (Exception e)
        {
            printUsage();
            throw e;
        }
    }   
}
