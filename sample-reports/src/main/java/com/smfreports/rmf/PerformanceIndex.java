package com.smfreports.rmf;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.blackhillsoftware.smf.Importance;
import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.smf72.Smf72Record;
import com.blackhillsoftware.smf.smf72.subtype3.ServiceReportClassPeriodDataSection;

public class PerformanceIndex
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: PerformanceIndex <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
    	
        ArrayList<ServiceClassPeriod> highPI = new ArrayList<ServiceClassPeriod>();
        
        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {
            for (SmfRecord record : reader)
            {
                if (record.recordType() == 72 && record.subType() == 3)
                {
                    Smf72Record r72 = Smf72Record.from(record);
    
                    // skip report classes
                    if (!r72.workloadManagerControlSection().r723mrcl())
                    {
                        for (ServiceReportClassPeriodDataSection section : r72
                                .serviceReportClassPeriodDataSections())
                        {
                            // if performance index > 2 and:
                            if (section.performanceIndex() > 2
                                &&
                                // either not a velocity goal or
                                (!section.r723cvel() ||
                                // using + delay is > 5% of total samples
                                section.r723ctot() + section.r723ctou() > 
                                   r72.workloadManagerControlSection().r723mtvNum() / 20))
                            {
                                highPI.add(new ServiceClassPeriod(r72, section));
                            }
                        }
                    }
                }
            }
        }
        writeReport(highPI);
    }

    private static void writeReport(ArrayList<ServiceClassPeriod> highPI)
    {
        // Use custom comparator for complex sort order
        Collections.sort(highPI, new ServiceClassPeriodComparator());

        DateTimeFormatter timef = DateTimeFormatter.ISO_LOCAL_TIME;
        DateTimeFormatter datef = DateTimeFormatter.ISO_LOCAL_DATE;

        LocalDateTime currentTime = null;
        String currentSystem = null;
        Importance currentImportance = null;

        for (ServiceClassPeriod scInfo : highPI)
        {

            // group by System
            if (!scInfo.system.equals(currentSystem))
            {
                System.out.format("%n%s%n", scInfo.system);
                currentSystem = scInfo.system;
                currentTime = null;
                currentImportance = null;
            }
            if (!scInfo.time.equals(currentTime))
            {
                System.out.format("%n   %s %s%n", scInfo.time.format(datef),
                        scInfo.time.format(timef));
                currentTime = scInfo.time;
                currentImportance = null;
            }
            // Then by importance
            if (scInfo.importance != currentImportance)
            {
                System.out.format("      Importance: %s%n", scInfo.importance);
                currentImportance = scInfo.importance;
            }
            // detail line
            System.out.format("         %-8s Period %s %3.1f%n", scInfo.name,
                    scInfo.period, scInfo.perfIndex);
        }
    }

    /**
     *
     * Class to keep information about a service class period
     *
     */
    private static class ServiceClassPeriod
    {
        public ServiceClassPeriod(Smf72Record record,
                ServiceReportClassPeriodDataSection section)
        {
            system = record.system();
            // round to nearest minute
            time = record.smfDateTime().plusSeconds(30)
                    .truncatedTo(ChronoUnit.MINUTES);
            name = record.workloadManagerControlSection().r723mcnm();
            period = section.r723cper();
            importance = section.importance();
            perfIndex = section.performanceIndex();
        }

        String system;
        LocalDateTime time;
        String name;
        int period;
        Importance importance;
        double perfIndex;
    }

    /**
     * 
     * Comparator to implement custom sort order for report
     */
    private static class ServiceClassPeriodComparator implements
            Comparator<ServiceClassPeriod>
    {
        // sort by system,
        // then by time,
        // then by importance
        // then by performance index descending
        // finally by name and period
        public int compare(ServiceClassPeriod s1, ServiceClassPeriod s2)
        {
            int result = s1.system.compareTo(s2.system);
            if (result != 0)
                return result;
            result = s1.time.compareTo(s2.time);
            if (result != 0)
                return result;
            result = s2.importance.compareTo(s1.importance);
            if (result != 0)
                return result;
            // reversed to sort descending
            result = Double.compare(s2.perfIndex, s1.perfIndex);
            if (result != 0)
                return result;
            result = s1.name.compareTo(s2.name);
            if (result != 0)
                return result;
            result = Integer.compare(s1.period, s2.period);
            return result;
        }
    }
}
