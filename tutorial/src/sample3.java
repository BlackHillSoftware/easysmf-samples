import java.io.IOException;
import com.blackhillsoftware.smf.SmfRecordReader;

/**
 * 
 * Sample 3 shows how you can search for specific text when you don't 
 * know which specific record types might be relevant.
 *
 */

public class sample3
{
    public static void main(String[] args) throws IOException                                   
    {                                       
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {       	
            reader
	            .stream()
	            // ignore type 14, 15 and 42 subtype 6
	            .filter(record -> record.recordType() != 14 && record.recordType() != 15)
	            .filter(record -> !(record.recordType() == 42 && record.subType() == 6))
	            // search for dataset name in the record
	            .filter(record -> record.toString().contains("SYS1.PARMLIB"))
	            .limit(100) // stop after 100 matches
	            .forEachOrdered(record -> 
	            {
	            	// print some record information
	                System.out.format("%-23s System: %-4s Record Type: %s Subtype: %s%n%n",                                  
	                		record.smfDateTime(), 
	                		record.system(),
	                		record.recordType(),
	                		record.hasSubtypes() ? record.subType() : "");
	                // dump the record.
	                System.out.format("%s%n%n",                                  
	                		record.dump());
	            });
        }
        System.out.println("Done");
    }
}
