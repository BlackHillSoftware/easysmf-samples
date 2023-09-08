import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;

/**
 *  This sample demonstrates Querying CICS transaction SMF data,
 *  counting or summing data fields.
 *
 */
public class Query04CountingAndSumming {

    public static void main(String[] args) throws IOException {
        
        // read CICS dictionary records from separate file
        Smf110Record
            .dictionariesFromName("C:\\Users\\Andrew\\SMF\\dictionary.smf");
            // Dataset name format:
            //.dictionariesFromName("//'ANDREWR.DEMO.CICS.DICT'");
                
        try (SmfRecordReader reader = SmfRecordReader
                .fromName("C:\\Users\\Andrew\\SMF\\cics.smf")
                //.fromName("//'ANDREWR.DEMO.CICS'")
                .include(110,1)) // include only type 110 subtype 1
        {
            reader.stream()
                .map(record -> Smf110Record.from(record))
                .filter(r110 -> r110.mnProductSection().smfmnprn().equals("CICS2A8B"))
                .map(r110 -> r110.performanceRecords())
                .flatMap(List::stream) // merge multiple lists into single stream
                
                // group by transaction and collect a result 
                .collect(Collectors.groupingBy(tx -> tx.getField(Field.TRAN),
                        // count transactions in each group
                        Collectors.counting()))
                
                        // alternatively, uncomment to sum USRCPUT for each group:              
                        //Collectors.summingDouble(tx -> tx.getField(Field.USRCPUT).timerSeconds())))
                
                // stream groups
                .entrySet().stream()
                
                // sort by value (i.e. count, sum) descending and print results
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(System.out::println);   
        }
    }
}