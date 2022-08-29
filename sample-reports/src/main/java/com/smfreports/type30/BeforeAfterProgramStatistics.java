package com.smfreports.type30;

import static java.util.Comparator.comparing;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf30.Smf30Record;

/**
 * This sample compares statistics by program name before and after a specified
 * time.

 * The program reports CP time, zIIP time, zIIP on CP time, EXCP count, 
 * zIIP and zIIP on CP time as a percentage of total (normalized) CPU time,
 * and CPU milliseconds per I/O for each Program Name.
 * 
 * The A and B values for zIIP%, zIIP on CP% and CPU milliseconds per I/O are  
 * compared and the difference is shown.
 *
 */

public class BeforeAfterProgramStatistics
{
    private static void printUsage() {
        System.out.println("Usage: BeforeAfterProgramStatistics <date-time> <input-file>");
        System.out.println("");
        System.out.println("  date-time    The local date-time to be used for the before/after split.");
        System.out.println("               This is parsed by java.time.LocalDateTime.parse() using");
        System.out.println("               DateTimeFormatter.ISO_LOCAL_DATE_TIME format");
        System.out.println("               e.g. 2020-01-31T12:30:00");
        System.out.println("");        
        System.out.println("  input-file   File containing SMF records. Binary data, RECFM=U or V[B]");
        System.out.println("               including RDW.");
        System.out.println("");
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length != 2 || args[0].equals("--help") || args[0].equals("-h"))
        {
            printUsage();
            System.exit(0);
        }
        
        try
        {
            LocalDateTime boundary = LocalDateTime.parse(args[0]);
                   
            Map<String, ProgramStatistics> beforePrograms = new HashMap<String, ProgramStatistics>();
            Map<String, ProgramStatistics> afterPrograms  = new HashMap<String, ProgramStatistics>();
    
            // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
            // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
            
            try (SmfRecordReader reader = SmfRecordReader.fromName(args[1])) 
            {
                // SMF 30 subtype 4 = Step End records
                reader.include(30, 4);
                
                for (SmfRecord record : reader)
                {
                    Smf30Record r30 = Smf30Record.from(record);
                    String programName = r30.identificationSection().smf30pgm();
                    
                    // get the target map based on whether this record is before or after the boundary
                    Map<String, ProgramStatistics> target = 
                            r30.smfDateTime().isBefore(boundary) ? beforePrograms : afterPrograms;
                
                    // Find or create the entry for the program name and accumulate the data
                    target
                        .computeIfAbsent(programName, key -> new ProgramStatistics(programName))
                        .accumulateData(r30);
                }
            }
            writeReport(beforePrograms, afterPrograms);
        }
        catch (Exception e)
        {
            printUsage();
            throw e;
        }
    }
    
    /**
     * Write the report
     * 
     * @param beforePrograms
     *            The map of Program Names to Program Data Before
     * @param afterPrograms
     *            The map of Program Names to Program Data After
     */
    private static void writeReport(
            Map<String, ProgramStatistics> beforePrograms, 
            Map<String, ProgramStatistics> afterPrograms)
    {
        // format strings for heading and detail lines.
        String headerFormatString =  "%n%-8s %8s %14s %14s %14s %14s %14s %14s %14s%n";
        String detailFormatString =  "%-8s %,8d %14s %14s %14s %,14d %13.1f%% %13.1f%% %14s%n";
        String changeFormatString =  "%-77s %14s %14s %14s%n%n";

        // Headings
        System.out.format(headerFormatString, 
            "Program", "Count", "CP", "zIIP", "zIIP on CP",
            "EXCP", "zIIP%", "zIIP on CP%", "CPU ms/IO");

        // Take entries Before
        beforePrograms.values().stream()
              
            // Ignore any entries where there is no After equivalent 
            .filter(beforeProgramsEntry -> afterPrograms.containsKey(beforeProgramsEntry.getName()))
            
            // sort by CP Time descending
            .sorted(comparing(ProgramStatistics::getCpTime).reversed())
            
            .limit(100) // take top 100 by CP time
            .forEachOrdered(beforeInfo ->
            {
                // Get matching After statistics
                ProgramStatistics afterInfo = afterPrograms.get(beforeInfo.getName());
                
                System.out.format("%-8s%n", 
                        beforeInfo.getName()); // Program name
                
                // write Before detail line
                System.out.format(detailFormatString, 
                    "Before:",
                    beforeInfo.getCount(), 
                    hhhmmss(beforeInfo.getCpTime()), 
                    hhhmmss(beforeInfo.getZiipTime()),
                    hhhmmss(beforeInfo.getZiipOnCpTime()),
                    beforeInfo.getExcps(), 
                    beforeInfo.getZiipPct(), 
                    beforeInfo.getZiipOnCpPct(), 
                    beforeInfo.getCpuMsPerIO()
                        .map(value -> String.format("%.3f",value))
                        .orElse("") );               
                
                // write After detail line
                System.out.format(detailFormatString, 
                    "After:",
                    afterInfo.getCount(), 
                    hhhmmss(afterInfo.getCpTime()), 
                    hhhmmss(afterInfo.getZiipTime()),
                    hhhmmss(afterInfo.getZiipOnCpTime()),
                    afterInfo.getExcps(), 
                    afterInfo.getZiipPct(), 
                    afterInfo.getZiipOnCpPct(), 
                    afterInfo.getCpuMsPerIO()
                        .map(value -> String.format("%.3f",value))
                        .orElse("") );
                
                // write differences
                // Differences are type Optional<Double>. If values are zero
                // the percentage change is not useful so we don't report it.
                // If the Optional has a value, format the difference for output.
                // If not, leave the field blank.
                System.out.format(changeFormatString, 
                    "Change:",
                    afterInfo.getZiipPctChange(beforeInfo)
                            .map(value -> String.format("%+.1f%%",value))
                            .orElse(""),
                    afterInfo.getZiipOnCpPctChange(beforeInfo)
                            .map(value -> String.format("%+.1f%%",value))
                            .orElse(""),
                    afterInfo.getCpuMsPerIOChange(beforeInfo)
                            .map(value -> String.format("%+.0f%%",value))
                            .orElse("") );
            });
    }
    
    /**
     * A class to accumulate information about a program.
     */
    private static class ProgramStatistics
    {
        public ProgramStatistics(String name)
        {
            this.name = name;   
        }
        
        /**
         * Add information from a SMF 30 record.
         * 
         * @param r30
         *            The Smf30Record
         */
        public void accumulateData(Smf30Record r30)
        {
            // One step can have many SMF records so we might get called multiple times
            // for the same job step, but some of the SMF sections will occur only
            // once per step e.g. ProcessorAccountingSection.
            
            if (r30.processorAccountingSection() != null)
            {
                count++; // pick a section that only occurs once and use to count job steps
                cpTime += r30.processorAccountingSection().smf30cptSeconds()
                    + r30.processorAccountingSection().smf30cpsSeconds();
                ziipTime += r30.processorAccountingSection().smf30TimeOnZiipSeconds();
                ziipOnCpTime += r30.processorAccountingSection().smf30TimeZiipOnCpSeconds();
                
                // According to the SMF manual the Processor Accounting section and 
                // Performance section can only occur in the first SMF record.
                // Assume a Performance section will always accompany the 
                // Processor Accounting section.
                normalizedZiipTime += 
                    r30.processorAccountingSection().smf30TimeOnZiipSeconds() 
                        * r30.performanceSection().smf30snf() / 256;
                
            }
            if (r30.ioActivitySection() != null)
            {
                excps += r30.ioActivitySection().smf30tex();
            }
        }
               
        String getName()               { return name; }
        int    getCount()              { return count; }
        double getCpTime()             { return cpTime; }
        double getZiipOnCpTime()       { return ziipOnCpTime; }
        double getZiipTime()           { return ziipTime; }
        double getNormalizedZiipTime() { return normalizedZiipTime; }
        long getExcps() { return excps; }
        
        /**
         * Calculate CPU time (CP time + normalized zIIP time) in milliseconds
         * per I/O (EXCP). If EXCP count = 0, return Optional.empty.
         * @return Optional<Double> CPU milliseconds per I/O, or Optional.empty 
         * if it cannot be calculated.
         */
        Optional<Double> getCpuMsPerIO()
        {
            return excps > 0 ? 
                    Optional.of((cpTime + normalizedZiipTime) * 1000  / excps)
                    : Optional.empty();
        }
        
        /**
         * Calculate the percentage change in CPU time per I/O. 
         * If this or the previous value are zero,
         * no value will be calculated and Optional.empty will be returned.
         * 
         * @param prev the previous ProgramStatistics for comparison 
         * @return Optional<Double> percentage change, or Optional.empty
         *  if the value cannot be calculated
         */
        Optional<Double> getCpuMsPerIOChange(ProgramStatistics prev)
        {
            Optional<Double> oldvalue = prev.getCpuMsPerIO();
            Optional<Double> newvalue = this.getCpuMsPerIO();
            
            return (oldvalue.isPresent() 
                    && newvalue.isPresent() 
                    && oldvalue.get() > 0 
                    && newvalue.get() > 0) ?            
                        Optional.of(((newvalue.get() / oldvalue.get()) - 1) * 100)
                        : Optional.empty();
        }
        
        double getZiipPct()
        {
            return (cpTime + normalizedZiipTime) > 0 ? normalizedZiipTime / (cpTime + normalizedZiipTime) * 100 : 0;
        }
        
        /**
         * Calculate the change in zIIP time as a percentage of total CPU time. 
         * If this or the previous value are zero,
         * no value will be calculated and Optional.empty will be returned.
         * 
         * @param prev the previous ProgramStatistics for comparison 
         * @return Optional<Double> percentage change, or Optional.empty
         *  if the value cannot be calculated
         */
        Optional<Double> getZiipPctChange(ProgramStatistics prev)
        {
            return (this.getZiipPct() > 0 || prev.getZiipPct() > 0) ? 
                    Optional.of(this.getZiipPct() - prev.getZiipPct())
                    : Optional.empty();
        }
        
        double getZiipOnCpPct()
        {
            return (cpTime + normalizedZiipTime) > 0 ? ziipOnCpTime / (cpTime + normalizedZiipTime) * 100 : 0;
        }
        
        /**
         * Calculate the change in zIIP on CP time as a percentage of total CPU time. 
         * If this or the previous value are zero,
         * no value will be calculated and Optional.empty will be returned.
         * 
         * @param prev the previous ProgramStatistics for comparison 
         * @return Optional<Double> percentage change, or Optional.empty
         *  if the value cannot be calculated
         */
        Optional<Double> getZiipOnCpPctChange(ProgramStatistics prev)
        {
            return (this.getZiipOnCpPct() > 0 || prev.getZiipOnCpPct() > 0) ? 
                    Optional.of(this.getZiipOnCpPct() - prev.getZiipOnCpPct()) 
                    : Optional.empty();
        }

        String name;
        int    count                 = 0;
        double cpTime                = 0;
        double ziipOnCpTime          = 0;
        double ziipTime              = 0;        
        double normalizedZiipTime    = 0;
        long   excps                 = 0;
    }   
    
    /**
     * Format seconds as hhh:mm:ss.hh Seconds value is reported
     * to 2 decimal places.
     * 
     * @param totalseconds
     * @return The formatted value.
     */
    private static String hhhmmss(double totalseconds)
    {
        final int SECONDS_PER_MINUTE = 60;
        final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;

        int hours = (int) (totalseconds / SECONDS_PER_HOUR);
        int minutes = (int) ((totalseconds % SECONDS_PER_HOUR)) / SECONDS_PER_MINUTE;
        double seconds = totalseconds % SECONDS_PER_MINUTE;

        return String.format("%d:%02d:%05.2f", hours, minutes, seconds);
    }  
    
}