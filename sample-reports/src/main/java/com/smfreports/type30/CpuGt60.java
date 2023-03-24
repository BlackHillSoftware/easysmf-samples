package com.smfreports.type30;
                                                               
import java.io.IOException;                                                                     
import java.time.*;

import com.blackhillsoftware.smf.SmfRecord;                                                     
import com.blackhillsoftware.smf.SmfRecordReader;                                               
import com.blackhillsoftware.smf.smf30.ProcessorAccountingSection;                              
import com.blackhillsoftware.smf.smf30.Smf30Record;                                             
                                                                                                
public class CpuGt60                                                                            
{                                                                                               
    public static void main(String[] args) throws IOException                                   
    {   
        if (args.length < 1)
        {
            System.out.println("Usage: CpuGt60 <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        { 
            reader.include(30, 5);
            for (SmfRecord record : reader)                                                     
            {                                                                                   
                Smf30Record r30 = Smf30Record.from(record);                                      
                for (ProcessorAccountingSection procAcct 
                        : r30.processorAccountingSections())   
                {                                                                               
                    Duration cpuTime = procAcct.smf30cpt()
                            .plus(procAcct.smf30cps());           
                    if (cpuTime.getSeconds() >= 60)                                             
                    {                                                                           
                        System.out.format("%-23s %-8s %12s%n",                                  
                           r30.smfDateTime(), 
                           r30.identificationSection().smf30jbn(), 
                           cpuTime);  
                    }                                                                           
                }                                                                               
            }                                                                                   
        }
        System.out.println("Done");
    }                                                                                           
}                                                                                               