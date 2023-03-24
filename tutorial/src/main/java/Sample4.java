import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf30.Smf30Record;

/**
 * 
 * Sample 4 shows how to Group and Summarize SMF data.
 * This program produces various statistics by Program Name 
 * from SMF 30 subtype 4 (step end) records.
 *
 */

public class Sample4
{
    public static void main(String[] args) throws IOException
    {
        // Create a map of Program Names to ProgramStatistics entries 
        // to collect information about each program.

        Map<String, ProgramStatistics> programs = new HashMap<String, ProgramStatistics>();

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {
            // Only SMF 30 subtype 4 = Step End records
            reader.include(30, 4);
            
            for (SmfRecord record : reader)
            {
                Smf30Record r30 = Smf30Record.from(record);
                String programName = r30.identificationSection().smf30pgm();
            
                // Find the entry for the program name and accumulate the data
                ProgramStatistics program = programs.get(programName);
                
                if (program == null)
                {
                    // entry doesn't exist - create new and add to map
                    program = new ProgramStatistics();
                    programs.put(programName, program);
                }            
                program.accumulateData(r30);
            }
        }
        writeReport(programs);
    }

    /**
     * Write the report
     * 
     * @param programs
     *            The map of Program Names to Program Data
     */
    private static void writeReport(Map<String, ProgramStatistics> programs)
    {
        // Headings
        System.out.format("%n%-8s %8s %14s %14s %14s %14s %14s %14s %14s %14s %14s%n", 
            "Program", "Count", "CPU", "zIIP",
            "Connect", "Excp", "Avg CPU", "Avg zIIP",
            "Avg Connect", "Avg Excp", "CPU ms/IO");

        programs.entrySet().stream()
            // sort by CP Time
            // reversing a and b in the comparison so sort is descending
            .sorted((a, b) -> Double.compare(b.getValue().cpTime, a.getValue().cpTime))
            .limit(100) // take top 100
            .forEachOrdered(program ->
            {
                ProgramStatistics programinfo = program.getValue();
                // write detail line
                System.out.format("%-8s %,8d %14s %14s %14s %,14d %14s %14s %14s %,14d %14s%n", 
                    program.getKey(),
                    programinfo.count, 
                    hhhmmss(programinfo.cpTime), 
                    hhhmmss(programinfo.ziipTime),
                    hhhmmss(programinfo.connectTime), 
                    programinfo.excps, 
                    hhhmmss(programinfo.cpTime / programinfo.count),
                    hhhmmss(programinfo.ziipTime / programinfo.count), 
                    hhhmmss(programinfo.connectTime / programinfo.count),
                    programinfo.excps / programinfo.count,
                    programinfo.getCpuMsPerIO()
                        .map(value -> String.format("%14.3f",value))
                        .orElse("")  );
            });
    }
    
    /**
     * A class to accumulate information about a program.
     */
    private static class ProgramStatistics
    {       
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
                connectTime += r30.ioActivitySection().smf30aicSeconds();
            }
        }
        
        /**
         * Calculate CPU time (CP time + normalized zIIP time) in milliseconds
         * per I/O (EXCP). If EXCP count = 0, return Optional.empty.
         * @return Optional<Double> CPU milliseconds per I/O, or Optional.empty 
         */
        Optional<Double> getCpuMsPerIO()
        {
            return excps > 0 ? 
                    Optional.of((cpTime + normalizedZiipTime) * 1000  / excps)
                    : Optional.empty();
        }
        
        int    count                 = 0;
        double cpTime                = 0;
        double ziipTime              = 0;        
        double normalizedZiipTime    = 0;
        double connectTime           = 0;
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