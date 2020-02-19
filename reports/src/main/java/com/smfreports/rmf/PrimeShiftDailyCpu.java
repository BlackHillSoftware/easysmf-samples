package com.smfreports.rmf;

import java.io.IOException;
import java.time.*;
import java.util.*;

import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf70.Smf70Record;
import com.blackhillsoftware.smf.smf70.subtype1.*;

public class PrimeShiftDailyCpu
{
    // Create a report of the sum of physical dispatch times for each
    // CPU type for each LPAR during prime shift, defined as
    // 8:30 to 17:30 Monday-Friday (using the time the SMF record was written).
    
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: PrimeShiftDailyCpu <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
    	
        // define start and end times
        LocalTime primestart = LocalTime.of(8, 30);
        LocalTime primeend = LocalTime.of(17, 30);
        List<DayOfWeek> primedays = 
                Arrays.asList(DayOfWeek.MONDAY, 
                        DayOfWeek.TUESDAY, 
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, 
                        DayOfWeek.FRIDAY);   

        // Use RMF data from these systems - typically one from each CEC.
        // Change to match your system name or you will not see any data!
        //                   |||
        //                   |||
        //                  VVVVV
        //                   VVV
        //                    V
        //        
        List<String> datasystems = Arrays.asList("SYSA", "SYSB");
        
        // map Date -> System -> CPU Type -> List of Durations
        Map<LocalDate, 
            Map<String, 
                Map<String, 
                    List<Duration>
                    >
                >
            > dailyCpuStatistics = new HashMap<LocalDate, Map<String, Map<String, List<Duration>>>>();
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
    	
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))
        { 
            reader
                .include(70,1)
                .stream()
                .filter(record ->
                     datasystems.contains(record.system())
                     && primedays.contains(record.smfDate().getDayOfWeek())
                     && record.smfTime().isAfter(primestart)
                     && record.smfTime().isBefore(primeend))
                .map(record -> Smf70Record.from(record))
                .forEach(r70 ->
                {
                    List<PrismLogicalProcessorDataSection> lpSections = r70.prismLogicalProcessorDataSections();
                    List<CpuIdentificationSection> cpuIdSections = r70.cpuIdentificationSections();
                    for (PrismPartitionDataSection partition : r70.prismPartitionDataSections())
                    {
                        int partitionLogicalProcessors = partition.smf70bdn();
                        int skipSections = (int)partition.smf70bds();
                        
                        for (int i=0; i < partitionLogicalProcessors; i++)
                        {
                            PrismLogicalProcessorDataSection lpSection = lpSections.get(skipSections + i);
                            String cputype = cpuIdSections.get(lpSection.smf70cix()-1)
                                    .smf70cin();                                    
                            dailyCpuStatistics
                                // get existing entry or add new entry to maps
                                .computeIfAbsent(r70.smfDate(), day -> new HashMap<>()) 
                                .computeIfAbsent(partition.smf70lpm(), system -> new HashMap<>())
                                .computeIfAbsent(cputype, cpu -> new ArrayList<>())
                                // add smf70pdt - physical dispatch time
                                .add(lpSection.smf70pdt());
                        }
                     
                    }
                });
        
        }

        writeReport(dailyCpuStatistics);
        System.out.println("Done");
    }

    private static void writeReport(Map<LocalDate, Map<String, Map<String, List<Duration>>>> dailyCpuStatistics)
    {
        dailyCpuStatistics.entrySet().stream()
            // sort by key (day)
           .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
           .forEachOrdered(day ->
           {
               // write date and day of week heading
               System.out.format("%n%s %s%n", 
                       day.getKey(), 
                       day.getKey().getDayOfWeek());
               
               day.getValue().entrySet().stream()
                   // sort daily information by key (LPAR name)
                   .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                   .forEachOrdered(lpar ->
                   {
                       // LPAR heading
                       System.out.format("%n    %-8s %12s%n", 
                               lpar.getKey(), "HHH:MM:SS");
                       
                       lpar.getValue().entrySet().stream()
                           // process entries for each cpu type
                           .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                           .forEachOrdered(cpuType ->
                           {
                               // combine all entries for this CPU type
                               // using Stream .reduce() :
                               // start with Duration.ZERO, then 
                               // use Duration.plus(Duration) to add each entry
                               // to the previous total giving a new total.
                               Duration total = cpuType.getValue().stream()
                                       .reduce(Duration.ZERO, (a, b) -> a.plus(b));
                               
                               // write the total for this (day/LPAR/CPU type)
                               System.out.format("        %-4s %12s%n", 
                                       cpuType.getKey(), hhhmmss(total));   
                           });
                   
                   });
           
           });
    }

    /**
     * Format a Duration as hhh:mm:ss. Fractional seconds are truncated to a whole number. 
     * @param The Duration to format
     * @return The formatted value
     */
    private static String hhhmmss(Duration dur)
    {
        // format Duration to hhh:mm:ss
        long hours = dur.toHours();
        long minutes = 
            dur.minus(Duration.ofHours(hours))
                .toMinutes();
        long seconds = 
            dur.minus(Duration.ofHours(hours))
               .minus(Duration.ofMinutes(minutes))
               .toMillis() / 1000; // No toSeconds function, so get milliseconds and divide
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
