package com.smfreports.rmf;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf72.*;
import com.blackhillsoftware.smf.smf72.subtype3.*;

/*
 * This sample program calculates CPU usage for each service class for each time period.
 * 
 * The time period can be specified as month, day, or hour.
 * 
 * The program is designed to be run under Java 11+ using the single file source
 * code feature i.e. without a separate compile step. The list of systems and partitions
 * to include can be changed in the source code if required before running the program.
 */
public class CpuByServiceClass
{
	private static void printUsage()
	{
	    System.out.println("Usage: CpuByServiceClass [-month|-day|-hour] <input-name> [<input-name> ...]");
	    System.out.println("  Default: group by month.");
	    System.out.println("  <input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");
	}

	// Filter by system. If SMFIDs are specified, only those systems are included.
	// Otherwise all systems are included.
	// Uncomment and update as required.
	private static Set<String> includeSystems = 
			Set.of(
				// "SYSA",
				// "SYSB",
				// "SYSC"
			);

    /**
     * Main program entry
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length == 0)
        {
            printUsage();
            return;
        }

        Grouping grouping = Grouping.MONTH;
        List<String> inputNames;

        String first = args[0];
        if (first.startsWith("-"))
        {
            switch (first)
            {
                case "-month":
                    grouping = Grouping.MONTH;
                    break;
                case "-day":
                    grouping = Grouping.DAY;
                    break;
                case "-hour":
                    grouping = Grouping.HOUR;
                    break;
                default:
                    grouping = null;
                    break;
            }
            if (grouping == null)
            {
                System.err.println("Unknown grouping option: " + first);
                printUsage();
                return;
            }
            if (args.length < 2)
            {
                printUsage();
                return;
            }
            inputNames = Arrays.asList(args).subList(1, args.length);
        }
        else
        {
            inputNames = Arrays.asList(args);
        }

        if (inputNames.isEmpty())
        {
            printUsage();
            return;
        }

        // Create Map and process input files
        Map<LocalDateTime, Map<String, Map<String, CpuStats>>> stats = new HashMap<>();

        for (String name : inputNames)
        {
            try (SmfRecordReader reader = SmfRecordReader.fromName(name)
				.include(72, 3))
            {
                processFile(reader, stats, grouping);
            }
        }

        writeReport(stats, grouping);
    }

    /**
     * Collect information from each file
     * @param reader SmfRecordReader configured to only return SMF70 subtype 1
     * @param stats Map of statistics to update
     * @param grouping Grouping value (MONTH, DAY, HOUR)
     */
    private static void processFile(SmfRecordReader reader,
            Map<LocalDateTime, Map<String, Map<String, CpuStats>>> stats, 
            Grouping grouping)
	{
	    for (SmfRecord record : reader)
	    {
	        Smf72Record r72 = Smf72Record.from(record);
	    	
	    	if (includeRecord(r72))
	    	{
	    		if (!r72.workloadManagerControlSection().r723mrcl()) // not a report class
	    		{
    				LocalDateTime periodStart = grouping.bucketStart(r72.smfDateTime());
	        		CpuStats cpuStats = stats
	        				.computeIfAbsent(periodStart, ps -> new HashMap<>())
	        				.computeIfAbsent(r72.system(), ps -> new HashMap<>())
                            .computeIfAbsent(r72.workloadManagerControlSection().r723mcnm(), p -> new CpuStats());
	        		
		    		for (var periodData : r72.serviceReportClassPeriodDataSections())
		    		{
		    			cpuStats.add(periodData);
	    			}
	    		}
		        
	    	}
	    }
	}

	private static boolean includeRecord(Smf72Record r72) 
	{
		if (includeSystems.contains(r72.system())) return true; // this system explicitly specified
		if (!includeSystems.isEmpty()) return false; // systems are explicitly specified but not this one\
		return true;
	}

    /**
     * Write the report
     * @param stats Map of collected statistics
     * @param grouping Grouping value (MONTH, DAY, HOUR)
     */
	private static void writeReport(Map<LocalDateTime, Map<String, Map<String, CpuStats>>> stats,
			Grouping grouping)
	{
		// write heading
        System.out.format("%-19s %-24s %-12s %14s %14s %14s %14s%n", 
        		"Time", 
        		"System", 
        		"Class", 
        		"CP Time", 
        		"CP MSU",
                "zIIP Time", 
                "zIIP MSU");

	    stats.entrySet().stream()
	            .sorted(Map.Entry.comparingByKey()) // sort by time
	            .forEachOrdered(timeEntry ->
	    {
	        LocalDateTime time = timeEntry.getKey();
	        var systems = timeEntry.getValue();
	
			System.out.println();
			systems.entrySet().stream()
            		.sorted(Map.Entry.comparingByKey()) // sort by system
	                .forEachOrdered(systemEntry ->
	                {
	                	systemEntry.getValue().entrySet().stream()
	            		.sorted(Map.Entry.comparingByKey()) // sort by service class
		                .forEachOrdered(serviceClassEntry ->
		                {
		                	String serviceClass = serviceClassEntry.getKey();
		                    CpuStats cs = serviceClassEntry.getValue();
	                        System.out.format("%-19s %-24s %-12s %14s %14.2f %14s %14.2f%n", 
	                        		grouping.periodHeading(time),
	                        		systemEntry.getKey(),
	                        		serviceClass, 
				                    formatSeconds(cs.cpSeconds),
				                    cs.cpMsu,
				                    formatSeconds(cs.ziipSeconds),
				                    cs.ziipMsu);
		                });
	                });
	        CpuStats periodTotal = CpuStats.sum(systems.values());
	        System.out.format("%n%-19s %-24s %-12s %14s %14.2f %14s %14.2f%n",
	        		grouping.periodHeading(time),
	        		"",
	        		"Total",
	        		formatSeconds(periodTotal.cpSeconds),
	        		periodTotal.cpMsu,
	        		formatSeconds(periodTotal.ziipSeconds),
	        		periodTotal.ziipMsu);
	    });
	}

	private static class CpuStats
	{
	    static CpuStats sum(Collection<Map<String, CpuStats>> bySystem)
	    {
	    	CpuStats t = new CpuStats();
	    	for (Map<String, CpuStats> byClass : bySystem)
	    	{
	    		for (CpuStats s : byClass.values())
	    		{
	    			t.cpSeconds += s.cpSeconds;
	    			t.cpMsu += s.cpMsu;
	    			t.ziipSeconds += s.ziipSeconds;
	    			t.ziipMsu += s.ziipMsu;
	    		}
	    	}
	    	return t;
	    }

	    public void add(ServiceReportClassPeriodDataSection periodData)
	    {
	    	double ziipSu = periodData.r723csup() / periodData.controlSection().r723mcpu() * 10000;

	    	double cpSu = (periodData.r723ccpu() / periodData.controlSection().r723mcpu() * 10000)
	    			+ (periodData.r723csrb() / periodData.controlSection().r723msrb() * 10000)
	    			- (ziipSu * periodData.controlSection().r723nffs() / 256);
	    	
	    	double cpTime = cpSu * periodData.controlSection().r723madj() / 16 / 1000000;	    	 	
	    	cpTime += periodData.r723crctSeconds() + periodData.r723ciitSeconds() + periodData.r723chstSeconds();
	    	
	    	// calculated zIIP time can be more than the dispatch time from type 70 records
	    	// due to multithreading on zIIP processors
	    	double ziipTime = ziipSu * periodData.controlSection().r723madj() / 16 / 1000000;
	    	
	    	cpSeconds += cpTime;
	    	ziipSeconds += ziipTime;
	    	
	    	cpMsu += cpTime * 16 * periodData.controlSection().r723cpaScalingFactor() / periodData.controlSection().r723cpaActual();
	    	ziipMsu += ziipTime * 16 * periodData.controlSection().r723cpaScalingFactor() / periodData.controlSection().r723cpaActual()
	    			* periodData.controlSection().r723nffs() / 256;
	    }
	
	    double cpSeconds = 0;
		double cpMsu = 0;
	    double ziipSeconds = 0;
		double ziipMsu = 0;
	}

	/**
	 * Enum for grouping, calculates the bucket start time for a record time 
	 * and formats headings appropriately.
	 */
	private enum Grouping
	{
	    MONTH, DAY, HOUR;
	
	    LocalDateTime bucketStart(LocalDateTime smfDateTime)
	    {
	        switch (this)
	        {
	            case MONTH:
	                return LocalDateTime.of(YearMonth.from(smfDateTime).atDay(1), LocalTime.MIDNIGHT);
	            case DAY:
	                return smfDateTime.truncatedTo(ChronoUnit.DAYS);
	            case HOUR:
	                return smfDateTime.truncatedTo(ChronoUnit.HOURS);
	            default:
	                throw new IllegalStateException();
	        }
	    }
	
	    String periodHeading(LocalDateTime bucketStart)
	    {
	        switch (this)
	        {
	            case MONTH:
	                return YearMonth.from(bucketStart).toString();
	            case DAY:
	                return bucketStart.toLocalDate().toString();
	            case HOUR:
	                return bucketStart.toString().replace('T', ' ');
	            default:
	                throw new IllegalStateException();
	        }
	    }
	}

    /** Format total seconds as hhh:mm:ss (fractional seconds truncated). */
    private static String formatSeconds(double seconds)
    {
        long whole = (long) seconds;
        Duration d = Duration.ofSeconds(whole);
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();
        long secs = d.minusHours(hours).minusMinutes(minutes).getSeconds();
        return String.format("%d:%02d:%02d", hours, minutes, secs);
    }
}
