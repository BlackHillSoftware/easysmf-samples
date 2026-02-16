package com.smfreports.cics;

import java.io.*;
import java.util.*;
import java.time.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.Smf110Record;

public class FilterRecords
{
    private static void printUsage() {
        System.out.println("Usage: FilterRecords <input-file> <output-file>");
        System.out.println("");
        System.out.println("Filter CICS SMF records to exclude records that are not required and speed up subsequent reports");
        System.out.println("");
        System.out.println("  input-file   File containing SMF records. Binary data, RECFM=U or V[B]");
        System.out.println("               including RDW.");
        System.out.println("  output-file  Output-file.");
    }
       
    public static void main(String[] args) throws IOException
    {
        if (args.length < 2 || args[0].equals("--help") || args[0].equals("-h"))
        {
            printUsage();
            System.exit(0);
        }
                
        // Open reader and writer 
        try (
            SmfRecordReader reader = SmfRecordReader.fromName(args[0])
            	.include(110);                
            SmfRecordWriter writer = SmfRecordWriter.fromName(args[1])
            )        
        {
            int in = 0;
            int out = 0;
            for (SmfRecord record : reader)
            {
                Smf110Record r110 = Smf110Record.from(record);
                in++;
                if (include(r110))
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
 
    /**
     * Test a record to see whether it should be included in the output.
     * Data from the product section e.g. applid can be read without  
     * decompressing the record, but references to other sections will
     * cause the record to be decompressed with a significant increase
     * in CPU time.
     *   
     * @param r110 the CICS record
     * @return true if the record should be included 
     */
	private static boolean include(Smf110Record r110) 
	{
	    // Include only data from specific applids on specific days.		
		if (!applids.contains(applid(r110))) return false;
		if (!days.contains(r110.smfDate())) return false;
		return true;
	}
	
	// A list of APPLIDS to include
    private static List<String> applids = Arrays.asList(
    		"CN1PPA1",
    		"CN1PPA2"
    		);

	// A list of days to include
    private static List<LocalDate> days = Arrays.asList(
    		LocalDate.of(2025,  9, 10),
    		LocalDate.of(2025,  9, 17)
    		);
	
    /**
     * Get the applid from the product section according to record subtype
     * @param r110
     * @return the APPLID
     */
	private static String applid(Smf110Record r110) 
	{
		switch (r110.subType())
		{	
			case 0: 
				return r110.jcProductSection().smfpsprn();
			case 1: 
				return r110.mnProductSection().smfmnprn();
			case 2: 
			case 3: 
			case 4: 
			case 5: 
				return r110.stProductSection().smfstprn();
			default: 
				return "Unknown";
		}
	}   
}
