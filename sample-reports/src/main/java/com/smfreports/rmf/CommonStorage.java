package com.smfreports.rmf;

import java.io.*;
import java.util.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf78.*;

/**
 * Report CSA and SQA usage over time by system
 */
public class CommonStorage
{
	private static void printUsage()
	{
	    System.out.println("Usage: CommonStorage  <input-name> [<input-name> ...]");
	    System.out.println("  <input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");
	}

    public static void main(String[] args) throws IOException
    {
        if (args.length == 0)
        {
            printUsage();
            return;
        }

        Map<String, List<Smf78Record>> bySystem = new HashMap<>();
        
        for (String name : args)
        {
            try (SmfRecordReader reader = SmfRecordReader.fromName(name)
				.include(78, 2))
            {
        	    for (SmfRecord record : reader)
        	    {
        	        Smf78Record r78 = Smf78Record.from(record);
        	        bySystem.computeIfAbsent(r78.system(), key -> new ArrayList<>())
        	        	.add(r78);
        	    }
            }
        }

        bySystem.keySet().stream()
        	.sorted()
        	.forEachOrdered(system -> writeReport(system, bySystem.get(system)));
    }

    private static void writeReport(String system, List<Smf78Record> records)
    {
		System.out.format("%nSystem: %s%n", system);
		System.out.format("%-30s %12s %12s %12s %12s%n", 
				"Time",
				"CSA <16MB",
				"SQA <16MB",
				"CSA >16MB",
				"SQA >16MB"
				);
		records.stream()
			.sorted(Comparator.comparing(Smf78Record::smfDateTime))
			.forEachOrdered(r78 -> {
				for (var x : r78.virtualStorageCommonStorageDataSections())
				{
					System.out.format("%-30s %,12d %,12d %,12d %,12d%n", 
							r78.smfDateTime(),
							x.r782csau().vsdbmax(),
							x.r782sqau().vsdbmax(),
							x.r782csau().vsdamax(),
							x.r782sqau().vsdamax()
							);
				}
			});
    }
}
