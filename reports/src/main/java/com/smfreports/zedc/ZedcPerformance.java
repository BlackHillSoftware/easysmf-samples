package com.smfreports.zedc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf15.Smf15Record;

/**
 *
 * Search for datasets with zEDC performance issue.
 *
 */
public class ZedcPerformance                                                                            
{                                                                                               
    public static void main(String[] args) throws IOException                                   
    {
    	
        if (args.length < 1)
        {
            System.out.println("Usage: ZedcPerformance <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
    	
        try (SmfRecordReader reader = 
        		SmfRecordReader.fromName(args[0])
        			.include(15))
        {   
        	List<DatasetInfo> datasets = 
	            reader.stream()
		            .map(record -> Smf15Record.from(record))          
		            .filter(r15 -> r15.compressedFormatDatasetSections().size() > 0)
		            .map(r15-> new DatasetInfo(r15))
		            .filter(datasetInfo -> datasetInfo.ratio() > 1)
		            .limit(10000)
		            .collect(Collectors.toList());
        	
        	writeReport(datasets);

            System.out.format("Done");                  
        }                                   
    }
    
    static void writeReport(List<DatasetInfo> datasets)
    {
    	String headingFormat = "%-6s %-24s %-8s %-8s %-44s %13s %13s %6s%n";
    	String detailFormat =  "%-6s %-24s %-8s %-8s %-44s %,13d %,13d %6.1f%n";
        System.out.format(headingFormat,
        		"System",
        		"Time",
        		"Job",
        		"Program",
        		"Dataset",
        		"Size MB",
        		"Written MB",
        		"Ratio"
        		);
    	for (DatasetInfo dataset : datasets)
    	{
    		System.out.format(detailFormat,                             
            		dataset.system,
            		dataset.dateTime,
            		dataset.jobname,
            		dataset.program,
            		dataset.dataset,
            		dataset.sizeMB,
            		dataset.writtenMB,
            		dataset.ratio()
                    );
    	}
    }
    
    static class DatasetInfo
    {
    	DatasetInfo(Smf15Record r15)
    	{
        	dateTime = r15.smfDateTime();
        	system = r15.system();
        	jobname = r15.smf14jbn();
        	program = r15.stepInformationSections().get(0).smf14pgn();
        	dataset = r15.smfjfcb1().jfcbdsnm();
        	sizeMB = r15.compressedFormatDatasetSections().get(0).smf14cds() / (1024*1024);
        	writtenMB = r15.compressedFormatDatasetSections().get(0).smf14cdl() / (1024*1024);
    	}
    	LocalDateTime dateTime;
    	String system;
    	String jobname;
    	String program;
    	String dataset;
    	long sizeMB;
    	long writtenMB;
    	float ratio()
    	{
    		if (sizeMB == 0) return 0;
    		return (float) writtenMB / sizeMB;
    	}
    }
    
}                                                                                               