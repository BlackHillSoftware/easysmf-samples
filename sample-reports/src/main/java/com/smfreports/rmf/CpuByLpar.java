package com.smfreports.rmf;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf70.*;
import com.blackhillsoftware.smf.smf70.subtype1.*;

/*
 * This sample program calculates CPU usage for each LPAR for each time period.
 * 
 * The time period can be specified as month, day, or hour.
 * 
 * The program is designed to be run under Java 11+ using the single file source
 * code feature i.e. without a separate compile step. The list of systems and partitions
 * to include can be changed in the source code if required before running the program.
 */
public class CpuByLpar
{
	private static void printUsage()
	{
	    System.out.println("Usage: CpuByLpar [-month|-day|-hour] <input-name> [<input-name> ...]");
	    System.out.println("  Default: group by month.");
	    System.out.println("  <input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");
	}

	// We need data from only 1 system per CEC. Either specify the 
	// SMFIDs to include explicitly, or we will use the first system
	// encountered from each CEC.
	// Uncomment and update as required.
	private static Set<String> dataSystems = 
			Set.of(
				// "SYSA",
				// "SYSB",
				// "SYSC"
			);
	
	// map to record which system we are using for each CEC, if SMFIDs are 
	// not explicitly specified
	private static Map<String, String> dataSystemsByCEC = new HashMap<>();
	
	// Specify the LPARs to include, leave empty for all LPARs
	// Uncomment and update as required.
    private static Set<String> includeLpars = 
    		Set.of(
   			    // "LPARL1",
   			    // "LPARL2",
   			    // "LPARL3",
   			    // "LPARL4",
   			    // "LPARL5",
   			    // "LPARL6"
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
        Map<LocalDateTime, Map<String, CpuStats>> stats = new HashMap<>();

        for (String name : inputNames)
        {
            try (SmfRecordReader reader = SmfRecordReader.fromName(name)
				.include(70, 1))
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
            Map<LocalDateTime, Map<String, CpuStats>> stats, 
            Grouping grouping)
	{
	    for (SmfRecord record : reader)
	    {
	        Smf70Record r70 = Smf70Record.from(record);
	    	
	    	if (includeRecord(r70))
	    	{
		        LocalDateTime periodStart = grouping.bucketStart(r70.smfDateTime());
				CpuControlSection ccs = r70.cpuControlSection();
		        List<CpuIdentificationSection> idSections = r70.cpuIdentificationSections();
		        List<PrismLogicalProcessorDataSection> lpdSections = r70.prismLogicalProcessorDataSections();
		
		        for (PrismPartitionDataSection partition : r70.prismPartitionDataSections())
		        {
		        	if (partition.smf70bdn() == 0) continue; // skip partitions with no CPUs configured
		        	String lpar = partition.smf70lpm();
		        	if (includeLpars.isEmpty() || includeLpars.contains(lpar))
		        	{
						// combine CEC and partition name, because partition names can be duplicated
						// e.g. "PHYSICAL"
		        		String cecPartition = ccs.smf70csc() + ":" + lpar;
	                    // create new time period and partition entries if required
		        		CpuStats cpuStats = stats
		        				.computeIfAbsent(periodStart, ps -> new HashMap<>())
                                .computeIfAbsent(cecPartition, p -> new CpuStats());
		                // skip to the Logical Processor Data sections for this partition, and process
		                // the number of sections indicated by smf70bdn
		                for (int cpunum = (int) partition.smf70bds();
		                        cpunum < (int) partition.smf70bds() + partition.smf70bdn();
		                        cpunum++)
		                {
		                    PrismLogicalProcessorDataSection lpd = lpdSections.get(cpunum);
		                    String cpuType = idSections.get(lpd.smf70cix() - 1).smf70cin();
                            cpuStats.add(partition, lpd, cpuType, ccs);
		                }
		        	}
		        }
	    	}
	    }
	}

	private static boolean includeRecord(Smf70Record r70) 
	{
		if (dataSystems.contains(r70.system())) return true; // this system explicitly specified
		if (!dataSystems.isEmpty()) return false; // systems are explicitly specified but not this one
		// get the system for this CEC, if it doesn't exist in the map insert and return this system
		String systemForCec = dataSystemsByCEC.computeIfAbsent(r70.cpuControlSection().smf70csc(), x -> r70.system());
		return systemForCec.equals(r70.system());
	}

    /**
     * Write the report
     * @param stats Map of collected statistics
     * @param grouping Grouping value (MONTH, DAY, HOUR)
     */
	private static void writeReport(Map<LocalDateTime, Map<String, CpuStats>> stats,
			Grouping grouping)
	{
		// write heading
        System.out.format("%-19s %-24s %-12s %14s %14s %14s %14s%n", 
        		"Time", 
        		"Partition", 
        		"System", 
        		"CP EDT", 
        		"CP MSU",
                "zIIP EDT", 
                "zIIP MSU");

	    stats.entrySet().stream()
	            .sorted(Map.Entry.comparingByKey()) // sort by time
	            .forEachOrdered(timeEntry ->
	    {
	        LocalDateTime time = timeEntry.getKey();
	        Map<String, CpuStats> lpars = timeEntry.getValue();
	
			System.out.println();
	        lpars.entrySet().stream()
            		.sorted(Map.Entry.comparingByKey()) // sort by csc:lpm key
	                .forEachOrdered(lparEntry ->
	                {
	                	String partitionKey = lparEntry.getKey();
	                    CpuStats cs = lparEntry.getValue();
                        System.out.format("%-19s %-24s %-12s %14s %14.2f %14s %14.2f%n", 
                        		grouping.periodHeading(time), 
                        		stripleadingZeros(partitionKey), 
                        		cs.system != null ? cs.system : "",        		
			                    formatSeconds(cs.cpEdtSeconds),
			                    cs.cpMsu,
			                    formatSeconds(cs.iipEdtSeconds),
			                    cs.iipMsu);
	                });
	        CpuStats periodTotal = CpuStats.sum(lpars.values());
	        System.out.format("%n%-19s %-24s %-12s %14s %14.2f %14s %14.2f%n",
	        		grouping.periodHeading(time),
	        		"Total",
	        		"",
	        		formatSeconds(periodTotal.cpEdtSeconds),
	        		periodTotal.cpMsu,
	        		formatSeconds(periodTotal.iipEdtSeconds),
	        		periodTotal.iipMsu);
	    });
	}

	/**
	 * Remove leading zeros from a string.
	 * @param value The string
	 * @return The string with leading zeros removed.
	 */

	private static String stripleadingZeros(String value)
	{
		if (value == null || value.isEmpty())
		{
			return value;
		}
		String stripped = LEADING_ZEROS.matcher(value).replaceFirst("");
		return stripped.isEmpty() ? "0" : stripped;
	}
	private static final Pattern LEADING_ZEROS = Pattern.compile("^0+");

	private static class CpuStats
	{
	    static CpuStats sum(Iterable<CpuStats> parts)
	    {
	    	CpuStats t = new CpuStats();
	    	for (CpuStats s : parts)
	    	{
	    		t.cpEdtSeconds += s.cpEdtSeconds;
	    		t.cpMsu += s.cpMsu;
	    		t.iipEdtSeconds += s.iipEdtSeconds;
	    		t.iipMsu += s.iipMsu;
	    	}
	    	return t;
	    }

	    public void add(PrismPartitionDataSection partition, PrismLogicalProcessorDataSection lpd, String cpuType, CpuControlSection ccs)
	    {
			if (system == null)
			{
				system = partition.smf70stn();
			}
	        if ("CP".equals(cpuType))
	        {
	            cpEdtSeconds += lpd.smf70edtSeconds();
				cpMsu += lpd.smf70edtSeconds() 
						* 16 
						/ ((double)ccs.smf70cpaActual() 
								/ ccs.smf70cpaScalingFactor()); 
	        }
	        else if ("IIP".equals(cpuType))
	        {
	            iipEdtSeconds += lpd.smf70edtSeconds();
				iipMsu += lpd.smf70edtSeconds() 
						* ccs.smf70nrm() / 256 
						* 16 
						/ ((double)ccs.smf70cpaActual() 
								/ ccs.smf70cpaScalingFactor()) ; 
	        }
	    }
	
		String system = null;
	    double cpEdtSeconds = 0;
		double cpMsu = 0;
	    double iipEdtSeconds = 0;
		double iipMsu = 0;
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
