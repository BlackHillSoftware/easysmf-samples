package com.smfreports.cics;

import java.io.*;
import java.time.*;
import java.util.Comparator;

import com.blackhillsoftware.json.EasySmfGsonBuilder;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;
import com.blackhillsoftware.smf.summary.Top;
import com.google.gson.Gson;

/**
 * This class provides various options to search for and display 
 * CICS transactions using Java Streams filtering and processing. 
 * 
 * There are many different attributes that can be used to select
 * a transaction.
 * 
 * Various examples are provided and commented out. Uncomment the 
 * ones that match what you want to do, and modify as required.
 * 
 * Likewise, examples of various output formats are provided.
 * Uncomment the ones you want to use. 
 *
 * CICS Dictionaries
 * -----------------
 * CICS dictionary records must be read first to extract the 
 * transaction information. A dataset containing the dictionary
 * records can be concatenated ahead of the transaction data, or
 * in a separate DD name given as the first command line argument.
 */
public class CicsTransactionSearch 
{
    public static void main(String[] args) throws IOException 
    {
        if (args.length < 1)
        {
            System.out.println(
                "Usage: CicsTransactionSearch <input-name> <input-name2> ...");
            System.out.println(
                "<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // set up a Gson instance for optional JSON output 
        Gson gson = new EasySmfGsonBuilder() // set up a Gson instance for optional 
                .setPrettyPrinting()
                .avoidScientificNotation(true) // make decimals more readable
                .cicsClockDetail(false)
                .includeZeroValues(false)
                .includeUnsetFlags(false)
                .includeEmptyStrings(false)
                .createGson();
                
        for (String name : args) // process arguments as file/DD names in turn
        {
            // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
            // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'

            try (SmfRecordReader reader = SmfRecordReader.fromName(name)
                    .include(110, 1)) 
            {     
                reader.stream()
                                
                    // Filter by SMF record attributes (fastest)
                
                    // Filter by SMF record time 
//                    .filter(record -> record.smfDateTime()
//                         .isAfter(LocalDateTime.of(2023, 04, 26, 12, 50, 0)))
//                    .filter(record -> record.smfDateTime()
//                          .isBefore(LocalDateTime.of(2023, 04, 26, 14, 10, 0)))
                
                    // Filter by SMF ID
//                    .filter(record -> record.system().equals("S0W1"))
                
                    .map(r110 -> Smf110Record.from(r110))
                    .filter(r110 -> r110.haveDictionary())
                    
                    // Filter by APPLID 
//                    .filter(r110 -> r110.mnProductSection()
//                        .smfmnprn().equals("CICSABCD"))
                    
                    
                    // get the list of PerformanceRecord/transactions
                    .map(r110 -> r110.performanceRecords())
                    // turn the multiple lists into a stream of transactions
                    .flatMap(entries -> entries.stream()) 
 
                    // Filter by transaction attributes
                    
                    // Filter by transaction time. Field is a ZonedDateTime (UTC)
                    // so we need to specify a ZoneId for the comparison. It doesn't
                    // have to be the same zone. It could be
                    // UTC "Z"
                    // An offset "-5"
                    // An IANA time zone name
//                    .filter(record -> record.getField(Field.STOP)
//                         .isAfter(ZonedDateTime.of(2023, 04, 26, 12, 50, 0, 0, 
//                                                   ZoneId.of("US/Pacific"))))
//                    .filter(record -> record.getField(Field.STOP)
//                          .isBefore(ZonedDateTime.of(2023, 04, 26, 14, 10, 0, 0, 
//                                                     ZoneId.of("US/Pacific"))))      
                    
                    // Filter by transaction
//                    .filter(tx -> tx.getField(Field.TRAN).equals("IHEL"))
                    
                    // Filter by program
//                    .filter(tx -> tx.getField(Field.PGMNAME).equals("ICC$HEL"))

                    // filter by elapsed time
                    .filter(tx -> tx.elapsedSeconds() > 5)
                    
                    // filter by field values, e.g. USRCPUT, DSPDELAY, IP Address
//                    .filter(tx -> tx.getField(Field.DSPDELAY).timerSeconds() > 0.1)
//                    .filter(tx -> tx.getField(Field.USRCPUT).timerSeconds() > 1)
//                    .filter(tx -> !tx.getField(Field.CLIPADDR).equals("") 
//                          && !tx.getField(Field.CLIPADDR).startsWith(("172.")))
                    
                    // Sort 
                    // Note 1: sort means that all data matching the filter to this
                    // point will be retained in memory
                    // Note 2: methods to get a value are probably called for each 
                    // comparison, so it is useful to minimize the number of entries.
                    
                    // Sort e.g. by transaction start time
//                    .sorted((a,b) -> a.getField(Field.START)
//                            .compareTo(b.getField(Field.START)))

                    
                    // Find and report only top values by a field 
                    
//                     .collect(Top.values(10, 
//                            Comparator.comparing(tx -> tx.getField(Field.USRCPUT))))
//                     .stream()
                                        
                    // Limit number of matching entries
                    .limit(1000)
                    
                    .forEachOrdered(tx ->  // "Ordered" in case we applied a sort 
                    {
                        // Optional output types - uncomment as required
                        
                        // Text
                        System.out.println(tx.toString());
 
                        // JSON
//                        System.out.println(gson.toJson(tx));
                        
                        // Custom format
//                        System.out.format("%-8s %-4s %-8s %-24s %-24s %s%n", 
//                                tx.smfmnprn(),
//                                tx.getField(Field.TRAN),
//                                tx.getField(Field.PGMNAME),
//                                tx.getField(Field.START),
//                                tx.getField(Field.STOP),
//                                tx.elapsed());
                    
                        // Write any CICS Clock fields where the time represents
                        // more than 5% of the elapsed time
//                        for (DictionaryEntry entry : tx.getDictionary().entries())
//                        {
//                           if (entry.getFieldId() instanceof ClockField 
//                                    && tx.getClockField(entry).timerSeconds() > tx.elapsedSeconds() * 0.05)
//                            {
//                                System.out.println(entry.cmodhead() + " : " + tx.getClockField(entry).timer());
//                            }  
//                        }
                        
                    });
            }
        }
    }
}
