package com.smfreports.type30;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.Smf30Record;

/*
 * This sample program calculates CPU usage for each job name for each time period.
 * 
 * The time period can be specified as month, day, or hour.
 * 
 * The program is designed to be run under Java 11+ using the single file source
 * code feature i.e. without a separate compile step. The list of systems and partitions
 * to include can be changed in the source code if required before running the program.
 */
public class CpuByJobname
{
	// Limit the number of jobs to display in the report.
	private static final int TOP_LIMIT = 50;

	private static void printUsage()
	{
	    System.out.println("Usage: CpuByJobname [-month|-day|-hour] <input-name> [<input-name> ...]");
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
	
	// Filter by jobname.
	
	// Include jobs: If jobs are specified, only those jobs will be included.
	// Uncomment and update as required.
	private static Set<String> includeJobs = 
			Set.of(
					// "ABCDE",
					// "ABCDF",
					// "ABCDG"
			);
	
	// Exclude jobs: Any jobs specified will be excluded.
	// Uncomment and update as required.
	private static Set<String> excludeJobs = 
			Set.of(
					// "ABCDE",
					// "ABCDF",
					// "ABCDG"
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
    				.include(30, 2)
    				.include(30, 3)
    				.include(30, 5))
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
	        Smf30Record r30 = Smf30Record.from(record);
	    	
	    	if (includeRecord(r30))
	    	{
				LocalDateTime periodStart = grouping.bucketStart(r30.smfDateTime());
        		CpuStats cpuStats = stats
        				.computeIfAbsent(periodStart, ps -> new HashMap<>())
        				.computeIfAbsent(r30.system(), ps -> new HashMap<>())
                        .computeIfAbsent(r30.identificationSection().smf30jbn(), p -> new CpuStats());
        		
	    		cpuStats.add(r30);
	    	}
	    }
	}

	private static boolean includeRecord(Smf30Record r30) 
	{
		// job is specifically excluded
		if (excludeJobs.contains(r30.identificationSection().smf30jbn())) return false;
		
		// the list of jobs to include is not empty, but does not include this job
		if (!includeJobs.isEmpty() && !includeJobs.contains(r30.identificationSection().smf30jbn())) return false;

		// the list of systems to include is not empty, but does not include this system
		if (!includeSystems.isEmpty() && !includeSystems.contains(r30.system())) return false;

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
        System.out.format("%-19s %-12s %-12s %14s %14s %14s %14s%n", 
        		"Time", 
        		"System", 
        		"Jobname", 
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
	                	List<Map.Entry<String, CpuStats>> sortedJobs = systemEntry.getValue().entrySet().stream()
	                		.sorted(Comparator
	                				.<Map.Entry<String, CpuStats>>comparingDouble(entry -> entry.getValue().totalCpuTime())
	                				.reversed()
	                				.thenComparing(Map.Entry.comparingByKey()))
	                		.collect(Collectors.toList());
	                	
	                	int printedCount = Math.min(TOP_LIMIT, sortedJobs.size());
	                	for (int i = 0; i < printedCount; i++)
	                	{
	                		Map.Entry<String, CpuStats> jobEntry = sortedJobs.get(i);
	                		String jobname = jobEntry.getKey();
	                		CpuStats cs = jobEntry.getValue();
	                        System.out.format("%-19s %-12s %-12s %14s %14.2f %14s %14.2f%n", 
	                        		grouping.periodHeading(time),
	                        		systemEntry.getKey(),
	                        		jobname, 
				                    formatSeconds(cs.cpSeconds),
				                    cs.cpMsu,
				                    formatSeconds(cs.ziipSeconds),
				                    cs.ziipMsu);
	                	}
	                	
	                	if (sortedJobs.size() > TOP_LIMIT)
	                	{
	                		CpuStats other = CpuStats.sum(sortedJobs.subList(TOP_LIMIT, sortedJobs.size()).stream()
	                				.map(Map.Entry::getValue)
	                				.collect(Collectors.toList()));
	                        System.out.format("%-19s %-12s %-12s %14s %14.2f %14s %14.2f%n", 
	                        		grouping.periodHeading(time),
	                        		systemEntry.getKey(),
	                        		"OTHER", 
				                    formatSeconds(other.cpSeconds),
				                    other.cpMsu,
				                    formatSeconds(other.ziipSeconds),
				                    other.ziipMsu);
	                	}
	                	System.out.println();
	                });
	        CpuStats periodTotal = CpuStats.sum(systems.values());
	        System.out.format("%n%-19s %-12s %-12s %14s %14.2f %14s %14.2f%n",
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
		double totalCpuTime()
		{
			return cpSeconds + ziipSeconds;
		}

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
	    
	    static CpuStats sum(Iterable<CpuStats> parts)
	    {
	    	CpuStats t = new CpuStats();
	    	for (CpuStats s : parts)
	    	{
	    		t.cpSeconds += s.cpSeconds;
	    		t.cpMsu += s.cpMsu;
	    		t.ziipSeconds += s.ziipSeconds;
	    		t.ziipMsu += s.ziipMsu;
	    	}
	    	return t;
	    }

	    public void add(Smf30Record r30)
	    {
	    	var processorAccountingSection = r30.processorAccountingSection();
	    	if (processorAccountingSection != null)
	    	{
		    	double cpTime = 0;
		    	double ziipTime = 0;
		    	switch (r30.subType())
		    	{
			    	case 2:
			    	case 3:
			    		// take most CPU time from subtypes 2 and 3 to capture running jobs/steps 
			    		cpTime = processorAccountingSection.smf30cptSeconds() 
			    			+ processorAccountingSection.smf30cpsSeconds()
			    			+ processorAccountingSection.smf30iipSeconds() 
			    			+ processorAccountingSection.smf30rctSeconds() 
			    			+ processorAccountingSection.smf30hptSeconds();
			    		ziipTime = processorAccountingSection.smf30TimeOnZiipSeconds(); 
			    		break;
			    	case 5:
			    		// take initiator CPU time form the subtype 5 record (most complete)
			    		cpTime = processorAccountingSection.smf30icuSeconds() 
			    			+ processorAccountingSection.smf30isbSeconds(); 
			    		break;
		    		default:
		    			break;
		    	}
		    	
		    	cpSeconds += cpTime;
		    	ziipSeconds += ziipTime;
		    	
		    	if (r30.performanceSection().smf30RctpcpuaActual() != 0)
		    	{
			    	cpMsu += cpTime * 16 * r30.performanceSection().smf30RctpcpuaScalingFactor() / r30.performanceSection().smf30RctpcpuaActual();
			    	ziipMsu += ziipTime * 16 * r30.performanceSection().smf30RctpcpuaScalingFactor() / r30.performanceSection().smf30RctpcpuaActual()
			    			* r30.performanceSection().smf30snf() / 256;
		    	}
	    	}
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
