package com.smfreports.zedc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf15.Smf15Record;

/**
 *
 * Performance problems have been observed for zEDC compressed datasets.
 * 
 * This seems to occur when programs do their own optimized e.g. block level
 * I/O. The problem has been observed in Java RecordWriter, IEBGENER when output
 * DCB information is the same as input, ICEGENER and SORT.
 * 
 * SMF data indicates the number of compressed bytes written (SMF14CDL) for a
 * dataset is many times the compressed data size (SMF14CDS).
 * 
 * This program searches SMF 15 records for compressed datasets where SMF14CDL
 * is greater than SMF14CDS and lists the results, along with the job and
 * program names.
 *
 */
public class ZedcWriteRatio                                                                            
{                                                                                               
    public static void main(String[] args) throws IOException                                   
    {
        if (args.length < 1)
        {
            System.out.println("Usage: ZedcWriteRatio <input-name>");
            System.out.println(
            		"<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
    	
    	String headingFormat = "%-6s %-24s %-8s %-8s %-44s %8s %13s %13s %6s%n";
    	String detailFormat =  "%-6s %-24s %-8s %-8s %-44s %8d %,13d %,13d %6.1f%n";
    	
        System.out.format(headingFormat,
        		"System",
        		"Time",
        		"Job",
        		"Program",
        		"Dataset",
        		"BLKSIZE",
        		"C-Size MB",
        		"C-Written MB",
        		"Ratio"
        		);
    	
        try (SmfRecordReader reader = 
        		SmfRecordReader
        			.fromName(args[0])
        			.include(15))
        {
            reader.stream()
	            .map(record -> Smf15Record.from(record))          
	            .filter(r15 -> r15.compressedFormatDatasetSections().size() > 0)
	            .map(r15 -> new CompressionInfo(r15))
	            .filter(compressionInfo -> compressionInfo.writeRatio() > 1)
	            .sorted(
	            	Comparator.comparingDouble(CompressionInfo::writeRatio)
	            		.reversed())
	            .limit(1000)
	            .forEachOrdered(dataset ->
		    		System.out.format(detailFormat,                             
		            		dataset.system,
		            		dataset.dateTime,
		            		dataset.jobname,
		            		dataset.program,
		            		dataset.dataset,
		            		dataset.blksize,
		            		dataset.sizeMB,
		            		dataset.writtenMB,
		            		dataset.writeRatio()
		                    ));
        	System.out.println("Done");                  
        }                                   
    }
    
    static class CompressionInfo
    {
    	CompressionInfo(Smf15Record r15)
    	{
        	dateTime = r15.smfDateTime();
        	system   = r15.system();
        	jobname  = r15.smf14jbn();
        	program  = r15.stepInformationSections().get(0).smf14pgn();
        	dataset  = r15.smfjfcb1().jfcbdsnm();
        	blksize  = r15.smfjfcb1().jfcblksi();
        	sizeMB   = r15.compressedFormatDatasetSections().get(0)
        			.smf14cds() / (1024*1024);
        	writtenMB = r15.compressedFormatDatasetSections().get(0)
        			.smf14cdl() / (1024*1024);
    	}
    	LocalDateTime dateTime;
    	String system;
    	String jobname;
    	String program;
    	String dataset;
    	int blksize;
    	long sizeMB;
    	long writtenMB;
    	float writeRatio()
    	{
    		if (sizeMB == 0) return 0;
    		return (float) writtenMB / sizeMB;
    	}
    }
}                                                                                               