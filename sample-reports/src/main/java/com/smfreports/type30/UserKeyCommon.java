package com.smfreports.type30;

import java.io.*;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf30.Smf30Record;

/**
 * This sample creates a report based on the flags introduced in APAR
 * OA53355 : USERKEY COMMON MIGRATION SUPPORT
 * <p>
 * Common storage in a user key will not be supported after z/OS 2.3.
 * APAR OA53355 introduced flags in the type 30 SMF record that are set
 * if a task uses user key common storage.
 * <p>
 * This sample checks these flags and lists the job details if they are 
 * set. It also reports if there are no records with 
 * SMF30_USERKEYCOMMONAUDITENABLED set, which means that the report is
 * invalid because the other flags will not be set even if user key 
 * common storage was used.
 * <p>
 * The report checks all type 30 subtypes, on the assumption that there
 * will not be a large number of jobs using user key common storage, and
 * it could be useful to see when the flag was first set.
 * If there are a large number of records reported you could 
 * restrict the report to subtype 5 job end records:
 * <code>reader.include(30,5)</code>
 * 
 */

public class UserKeyCommon 
{
    public static void main(String[] args) throws IOException
    { 	
        if (args.length < 1)
        {
            System.out.println("Usage: UserKeyCommon <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
    	
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))                
        {
            boolean foundAuditEnabled = false;
            int userKeyCommonFound = 0;
            for (SmfRecord record : reader.include(30))
            {
                Smf30Record r30 = Smf30Record.from(record);
                if (r30.storageSection() != null)
                {
                    if (!foundAuditEnabled && r30.storageSection().smf30UserKeyCommonAuditEnabled())
                    {
                        foundAuditEnabled = true;
                    }
                    if (r30.storageSection().smf30UserKeyCommonAuditEnabled()
                        && (r30.storageSection().smf30UserKeyCsaUsage()
                            || r30.storageSection().smf30UserKeyCadsUsage()
                            || r30.storageSection().smf30UserKeyChangKeyUsage()))
                    {
                        userKeyCommonFound++;
                        System.out.format(
                            "%-23s %-4s %-8s %-8s %-10s %-10s %-10s%n",           
                            r30.smfDateTime(),
                            r30.system(),
                            r30.identificationSection().smf30jbn(),
                            r30.identificationSection().smf30jnm(),
                            r30.storageSection().smf30UserKeyCsaUsage() ? "CSA" : "",
                            r30.storageSection().smf30UserKeyCadsUsage() ? "CADS" : "",
                            r30.storageSection().smf30UserKeyChangKeyUsage() ? "KEYCHANGE" : "");                        
                    }
                }
            }
            if (!foundAuditEnabled)
            {
                System.out.println("No records found with User Key Common Audit Enabled!");
            }
            else
            {
                System.out.format("%d records flagged", userKeyCommonFound);
            }
        }
    }
}
