import java.io.IOException;
import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf30.Smf30Record;

/**
 * 
 * This sample shows the basics of reading SMF data and extracting sections and fields.
 * Various CPU times are extracted and printed from the Processor Accounting section 
 * in the SMF type 30 subtype 5 (Job End) records. The data is printed in CSV format.
 *
 */

public class sample1 
{
    public static void main(String[] args) throws IOException                                   
    {                                       
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {
        	// Write headings
        	System.out.format("%s,%s,%s,%s,%s,%s,%s%n",
                    "Time", 
                    "System",
                    "Job",
                    "Job Number",
                    "CP Time",
                	"zIIP Time",
                	"zIIP on CP"
                	);
        	
        	reader.include(30,5); // SMF type 30 subtype 5 : Job End records
        	
        	// read and process records
        	for (SmfRecord record : reader)
        	{
        		// create a type 30 record 
        		Smf30Record r30 = Smf30Record.from(record);
        		
        		// if Processor Accounting section exists, print various data
        		if (r30.processorAccountingSection() != null)
        		{
                    System.out.format("%s,%s,%s,%s,%.2f,%.2f,%.2f%n",                                  
                            r30.smfDateTime(), 
                            r30.system(),
                            r30.identificationSection().smf30jbn(),
                            r30.identificationSection().smf30jnm(),
                            r30.processorAccountingSection().smf30cptSeconds()
                        		+ r30.processorAccountingSection().smf30cpsSeconds(),
                        	r30.processorAccountingSection().smf30TimeOnZiipSeconds(),
                        	r30.processorAccountingSection().smf30TimeZiipOnCpSeconds()
                        	);
        		}
        	}                                                                           
        }
        System.out.println("Done");
    }
}
