import java.io.*;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;
import com.blackhillsoftware.json.*;
import com.google.gson.Gson;

/**
 *  This sample demonstrates Querying CICS transaction SMF data
 *  to find transactions that exceeded a specified elapsed time.
 * 
 *  Alternate output formats can be uncommented to produce 
 *  output in JSON format or a list of CICS clock values where
 *  the clock value is greater than 10% of elapsed time.
 *
 */
public class Query01ElapsedTime {

	public static void main(String[] args) throws IOException {
	    
	    // read CICS dictionary records from separate file
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
				.include(110,1))           // include only type 110 subtype 1
		{
			reader.stream()
			    // create Smf110Record from SmfRecord
				.map(record -> Smf110Record.from(record))
				
				// filter by APPLID (product section field SMFMNPRN)
				.filter(r110 -> r110.mnProductSection().smfmnprn().equals("CICS2A8B"))
				.map(r110 -> r110.performanceRecords()) // multiple transaction records per SMF record
				.flatMap(List::stream)                  // merge lists into single stream
				.filter(tx -> tx.elapsedSeconds() > 1.5)  // filter by transaction elapsed time
				
				// Uncomment for output in JSON format:
				//.map(gson::toJson)
				
				.forEach(System.out::println);
			
				// Alternate output format, uncomment to print CICS clock values 
				// which are more than 10% of the elapsed time
				
//                .forEach(tx -> 
//                {
//                    // print a line header: transaction name, start time and elapsed seconds
//                    System.out.format("%-4s %-30s Elapsed : %f ", 
//                        tx.getField(Field.TRAN),
//                        tx.getField(Field.START),
//                        tx.elapsedSeconds()
//                    );
//                    
//                    // loop through dictionary entries for this transaction
//                    for (DictionaryEntry entry : tx.getDictionary().entries())
//                    {
//                        // if the dictionary entry is for a CICS clock field...
//                        if (entry.field() instanceof ClockField)
//                        {
//                            // get the value, and if the time is more than 10% of transaction elapsed
//                            // time, print the field name and value
//                            CicsClock clock = (CicsClock) tx.getField(entry);
//                            if (clock.timerSeconds() > tx.elapsedSeconds() * 0.1)
//                            {
//                                System.out.format("%-8s : %f  ", entry.cmodhead(), clock.timerSeconds());
//                            }
//                        }
//                    }
//                    // end the line for this transaction
//                    System.out.println();
//                });
		}
	}
}