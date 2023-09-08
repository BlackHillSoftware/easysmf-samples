import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;
import com.blackhillsoftware.smf.summary.Top;
import com.blackhillsoftware.json.*;
import com.google.gson.Gson;

/**
 * 
 *  This sample demonstrates Querying CICS transaction SMF data
 *  to find the top 10 transactions by elapsed time for each 
 *  transaction name.
 * 
 *  Alternate output formats can be uncommented to produce 
 *  output in JSON format or a list of CICS clock values where
 *  the clock value is greater than 10% of elapsed time.
 *
 */
public class Query02TopValues {

    public static void main(String[] args) throws IOException {
        
        Smf110Record
            .dictionariesFromName("C:\\Users\\Andrew\\SMF\\dictionary.smf");
        // Dataset name format:
        //.dictionariesFromName("//'ANDREWR.DEMO.CICS.DICT'");
        
        // Gson instance used if the gson::toJson line is uncommented below 
        Gson gson = new EasySmfGsonBuilder()
            .includeEmptyStrings(false)    // omit empty string values from generated JSON
            .includeZeroValues(false)      // omit zero values from generated JSON
            .includeUnsetFlags(false)      // omit flag fields with value==false from generated JSON 
            .cicsClockDetail(false)        // only simple clock values 
            .avoidScientificNotation(true) // avoid scientific notation for numbers for readability
            .createGson();
        
        try (SmfRecordReader reader = SmfRecordReader
                .fromName("C:\\Users\\Andrew\\SMF\\cics.smf")
                //.fromName("//'ANDREWR.DEMO.CICS'")
                .include(110,1))
        {
            // put the result of this stream into a variable, the result is a
            // Map of transaction id->List of transactions i.e. grouping of 
            // transactions by transaction id.
            Map<String, List<PerformanceRecord>> groups = reader.stream()
                .map(record -> Smf110Record.from(record))
                .filter(r110 -> r110.mnProductSection().smfmnprn().equals("CICS2A8B"))
                .map(r110 -> r110.performanceRecords())
                .flatMap(List::stream) // merge multiple lists into single stream
                
                // Group by Field.TRAN, for each group
                // collect Top 10 transactions by elapsed time  
                .collect(Collectors.groupingBy(tx -> tx.getField(Field.TRAN), 
                        Top.values(10, Comparator.comparing(tx -> tx.elapsedSeconds()))));
            
            // stream the entries in the Map, sorted by key (transaction)
            groups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                // for each group print the key then list the values.
                .forEachOrdered(group ->
                {
                    System.out.println(group.getKey()); // transaction name 
                    
                    // getValue returns the list of transactions for this group
                    for (PerformanceRecord entry : group.getValue()) 
                    {
                        System.out.println(entry);
                        
                        // Uncomment for output in JSON format:
                        //System.out.println(gson.toJson(entry));
                        
                        // Alternate output format, uncomment to print CICS clock values 
                        // which are more than 10% of the elapsed time:
                        //printClocks(entry);
                        
                    }
                });     
        }
    }
    

    /**
     * Print transaction information and clock field values more than 10% of elapsed time.
     * Extracted into a separate method compared to Query01
     * @param tx the transaction performance record
     */
    private static void printClocks(PerformanceRecord tx)
    {
        // print a line header: transaction name, start time and elapsed seconds
        System.out.format("%-4s %-30s Elapsed : %f ", 
                tx.getField(Field.TRAN),
                tx.getField(Field.START),
                tx.elapsedSeconds()
                );

        // loop through dictionary entries for this transaction
        for (DictionaryEntry entry : tx.getDictionary().entries())
        {
            // if the dictionary entry is for a CICS clock field...
            if (entry.field() instanceof ClockField)
            {
                // get the value, and if the time is more than 10% of transaction elapsed
                // time, print the field name and value
                CicsClock clock = (CicsClock) tx.getField(entry);
                if (clock.timerSeconds() > tx.elapsedSeconds() * 0.1)
                {
                    System.out.format("%-8s : %f  ", entry.cmodhead(), clock.timerSeconds());
                }
            }
        }
        // end the line for this transaction
        System.out.println();
    }
}