import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;
import com.blackhillsoftware.json.*;
import com.google.gson.Gson;
    
/**
 *  This sample demonstrates Querying CICS transaction SMF data
 *  from a SMF logstream on z/OS.
 *  
 *  Transactions from a specific terminal are reported.
 *
 *  Alternate output formats can be uncommented to produce 
 *  output in JSON format or a list of CICS clock values where
 *  the clock value is greater than 10% of elapsed time.
 */
public class Query05ReadLogstream {

    public static void main(String[] args) throws IOException {
        
        // read CICS dictionary records from separate file
        Smf110Record
            .dictionariesFromName("//'ANDREWR.DEMO.CICS.DICT'");
        
        // Gson instance used if the gson::toJson line is uncommented below 
        Gson gson = new EasySmfGsonBuilder()
            .includeEmptyStrings(false)    // omit empty string values from generated JSON
            .includeZeroValues(false)      // omit zero values from generated JSON
            .includeUnsetFlags(false)      // omit flag fields with value==false from generated JSON 
            .cicsClockDetail(false)        // only simple clock values 
            .avoidScientificNotation(true) // avoid scientific notation for numbers for readability
            .createGson();
        
        // use a SmfRecordReaderBuilder to specify LOGR subsystem
        // parameters and build a SmfRecordReader
        SmfRecordReaderBuilder builder = 
           SmfRecordReader.logstreamBuilder("IFASMF.CICS") // logstream name
           
           // specify optional from and to times using
           // ZonedDateTime or LocalDateTime or LocalTime 
           .from(LocalDateTime.of(2023,8,18,2,0)) 
           .to(LocalDateTime.of(2023,8,18,23,0))  
           
           // optional logger subsystem SID value
           .sid("S0W1")
           
           .include(110,1); // include only type 110 subtype 1
        
        // Build SmfRecordReader from the logstreamBuilder
        try (SmfRecordReader reader = builder.build())
        {
            reader.stream()
                .map(record -> Smf110Record.from(record))
                .filter(r110 -> r110.mnProductSection().smfmnprn().equals("CICSCTAA"))
                .map(r110 -> r110.performanceRecords())
                .flatMap(List::stream)
                
                // filter by TERM
                .filter(tx -> tx.getField(Field.TERM).equals("0520"))
                
                // sort by start time
                .sorted(Comparator.comparing(tx -> tx.getField(Field.START)))
                
                // Uncomment for output in JSON format:
                //.map(gson::toJson)
                
                // print values
                .forEach(System.out::println);
            
                // Alternate output format, uncomment to print CICS clock values 
                // which are more than 10% of the elapsed time:
                //.forEach(tx -> printClocks(tx));
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