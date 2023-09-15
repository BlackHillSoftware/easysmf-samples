package com.smfreports.sample;

import java.io.*;
import java.time.*;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf72.*;
import com.blackhillsoftware.smf.smf72.subtype3.*;

/** 
 * 
 * Report calculated velocity combining zIIP using and zIIP delay values from
 * all service classes by system and RMF interval.
 *
 */
public class ZiipVelocity 
{
    public static void main(String[] args) throws IOException 
    {
        if (args.length < 1)
        {
            System.out.println("Usage: ZiipVelocity <input-name>");
            return;
        }

        // nested maps: System -> Interval -> ZiipVelocityData
        Map<String, Map<ZonedDateTime, ZiipVelocityData>> intervalsBySystem = new HashMap<>();
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])
                .include(72,3))           
        {
            for (SmfRecord record : reader) // read records
            {
                Smf72Record r72 = Smf72Record.from(record); // create type 72 record 
                if (!r72.workloadManagerControlSection().r723mrcl()) // not a report class
                {
                    // Get the entry for this system and interval, creating missing values
                    // in the map as we go using computeIfAbsent(...)
                    
                    ZiipVelocityData intervalData = intervalsBySystem
                            .computeIfAbsent(r72.system(), systemEntry -> new HashMap<>())
                            .computeIfAbsent(
                                    // we use smf72iet to identify the interval, but add the time zone information
                                    // from smf72lgo to express the value in local time
                                    r72.productSection().smf72iet().withZoneSameInstant(r72.productSection().smf72lgo()),
                                    intervalEntry -> new ZiipVelocityData());
                    
                    // process zero or more ServiceReportClassPeriodDataSections from the record
                    for (ServiceReportClassPeriodDataSection scPeriod : r72.serviceReportClassPeriodDataSections())
                    {
                        intervalData.add(scPeriod);
                    }
                }
            }
        }
        
        // write the report
        intervalsBySystem.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()) // sort by systemId
            .forEach(systemEntry -> 
            {
                // write system header
                System.out.format("%nSystem: %s%n%n", systemEntry.getKey());
                
                // write intervals
                systemEntry.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // sort by interval time
                    .forEach(intervalEntry -> 
                    {
                        System.out.format("%-35s %3.0f%n",
                                intervalEntry.getKey(),
                                intervalEntry.getValue().velocity().orElse(null)
                                );
                    });
            });
    }
    
    /**
     *
     * A class to collect zIIP using and delay values
     * and calculate the velocity 
     *
     */
    private static class ZiipVelocityData
    {
        private double using = 0;
        private double delay = 0;
        
        /**
         * Add service class period information 
         * @param scPeriod the service class period data section
         */
        void add(ServiceReportClassPeriodDataSection scPeriod)
        {
            using += scPeriod.r723supu();
            delay += scPeriod.r723supd();
        }
        
        /**
         * Calculate the velocity based on collected using and delay values.
         * @return Optional velocity value or Optional.empty() if all values are zero
         */
        Optional<Double> velocity() 
        {
            if (using == 0 && delay == 0) return Optional.empty();       
            return Optional.of(using / (using + delay) * 100);
        }
    }
}
