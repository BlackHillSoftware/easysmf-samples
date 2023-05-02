package com.smfreports.cics;

import java.io.*;
import java.time.*;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;
import com.blackhillsoftware.smf.smf72.Smf72Record;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 *
 * Print statistics for CICS transactions by Transaction, APPLID and Service Class.
 * 
 * This allows you to compare the response times for each transaction with the 
 * response times for the service class as a whole.
 * Transactions in the same service class should have similar characteristics.
 * If there are some transactions with a very different response time, they
 * might be better moved to a different service class.
 * 
 * If the input includes type 72 records, the service class goal information 
 * will be shown.
 *
 */
public class CicsServiceClass 
{
    public static void main(String[] args) throws IOException 
    {
        if (args.length < 1)
        {
            System.out.println("Usage: CicsServiceClass <input-name> <input-name2> ...");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
                
        // Nested maps to collect data by APPLID/service class/transaction
        // APPLID -> Service Class -> Transaction name -> data
        Map<String, Map<String, Map<String, TransactionData>>> txByApplidServiceClassTxName = new HashMap<>();
        
        // Nested maps to collect data by APPLID/service class
        // APPLID -> Service Class -> data
        Map<String, Map<String, TransactionData>> txByApplidServiceClassAll = new HashMap<>();

        // Map to collect service class information from type 72 records if available
        // Note: this doesn't cater for different goals on different systems with the 
        // same service class name.
        // Service Class Name -> Service Class information
        Map<String, ServiceClassInfo> serviceClasses = new HashMap<>();
        
        // keep some statistics
        int noDictionary = 0;
        int txCount = 0;

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        for (String name : args) // process input names provided as arguments
        {
            try (SmfRecordReader reader = SmfRecordReader.fromName(name)
                    .include(110, 1) // CICS records
                    .include(72,3))  // RMF records
            {     
                for (SmfRecord record : reader) 
                {
                    switch (record.recordType())
                    {
                    case 110:
                        // Process CICS record
                        Smf110Record r110 = Smf110Record.from(record);
                        
                        if (r110.haveDictionary()) 
                        {
                            String applid = r110.mnProductSection().smfmnprn();
        
                            for (PerformanceRecord transaction : r110.performanceRecords()) 
                            {
                                txCount++;
                                // collect data by APPLID/service class/transaction name 
                                txByApplidServiceClassTxName
                                        // get APPLID entry or create new
                                        .computeIfAbsent(applid, 
                                                key -> new HashMap<>())
                                        // get service class sub entry or create new
                                        .computeIfAbsent(transaction.getField(Field.SRVCLSNM), 
                                                key -> new HashMap<>())
                                        // get transaction sub entry or create new
                                        .computeIfAbsent(transaction.getField(Field.TRAN), 
                                                key -> new TransactionData())
                                        .add(transaction);

                                // collect data for all transactions in the APPLID/service class 
                                txByApplidServiceClassAll
                                    // get APPLID entry or create new
                                    .computeIfAbsent(applid, 
                                            key -> new HashMap<>())
                                    // get service class sub entry or create new
                                    .computeIfAbsent(transaction.getField(Field.SRVCLSNM), 
                                            key -> new TransactionData())
                                    .add(transaction);  
                            }
                        } 
                        else 
                        {
                            noDictionary++;
                        }
                        break;
                        
                    case 72:
                        // Collect Service Class information
                        Smf72Record r72 = Smf72Record.from(record);
                        if (!r72.workloadManagerControlSection().r723mrcl()) // not a report class
                        {
                            String serviceClass = r72.workloadManagerControlSection().r723mcnm();
                            
                            // update the information if we don't already have an entry for this
                            // service class, or if this information is newer
                            if(!serviceClasses.containsKey(serviceClass)
                                    || r72.smfDateTime()
                                            .isAfter(serviceClasses.get(serviceClass).getTime()))        
                            {
                                serviceClasses.put(serviceClass, new ServiceClassInfo(r72));
                            }
                        }
                    }
                }
            }
        }
        
        writeReport(txByApplidServiceClassAll, txByApplidServiceClassTxName, serviceClasses);
        
        // Write some statistics 
        System.out.format(
                "%n%nTotal Transactions: %,d%n", 
                txCount);
               
        if (noDictionary > 0) 
        {
            System.out.format(
                    "%n%nSkipped %,d records because no applicable dictionary was found.", 
                    noDictionary);
        }
        
        if (Smf110Record.getCompressedByteCount() > 0) 
        {
            System.out.format(
                    "%n%nCompressed bytes %,d, decompressed bytes %,d, compression %.1f%%.%n", 
                    Smf110Record.getCompressedByteCount(),
                    Smf110Record.getDecompressedByteCount(),
                    (double)(Smf110Record.getDecompressedByteCount() - Smf110Record.getCompressedByteCount()) 
                            / Smf110Record.getDecompressedByteCount() * 100);
        }
    }

    /**
     * Write the report. Gets the APPLID entries and calls another method to write 
     * the details for each APPLID.
     * 
     * @param txByApplidServiceClassAll data by APPLID and service class
     * @param txByApplidServiceClassTxName data by APPLID, service class and transaction name
     * @param serviceClasses the service class information from type 72 records
     */
    private static void writeReport(
            Map<String, Map<String, TransactionData>> txByApplidServiceClassAll,
            Map<String, Map<String, Map<String, TransactionData>>> txByApplidServiceClassTxName, 
            Map<String, ServiceClassInfo> serviceClasses) 
    {
        // Stream entries by APPLID and sort by key i.e. APPLID
        txByApplidServiceClassAll.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEachOrdered(applidEntry ->
            {
                // for each APPLID entry, write the APPLID section of the report
                reportApplid(
                        applidEntry.getKey(),
                        txByApplidServiceClassAll.get(applidEntry.getKey()),
                        txByApplidServiceClassTxName.get(applidEntry.getKey()),
                        serviceClasses
                        );
            });
    }

    /**
     * Write the APPLID section of the report. Gets the Service Class entries
     * for this APPLID and calls another method to write the details for
     * each service class.
     * 
     * @param applid the current APPLID
     * @param txByServiceClassAll data by service class
     * @param txByServiceClassTxName data by service class and transaction name
     * @param serviceClasses the service class information from type 72 records
     */
    private static void reportApplid(
            String applid,
            Map<String, TransactionData> txByServiceClassAll,
            Map<String, Map<String, TransactionData>> txByServiceClassTxName,
            Map<String, ServiceClassInfo> serviceClasses) 
    {
        System.out.format("%nAPPLID: %-8s%n", applid);
        
        txByServiceClassAll.entrySet().stream()
            // Stream entries by Service Class and sort by key i.e. Service Class
            .sorted(Map.Entry.comparingByKey())
            .forEachOrdered(servClassEntry -> 
            {
                // for each Service Class entry, write the Service Class section of the report
                reportServiceClass(
                        servClassEntry.getKey(),
                        txByServiceClassAll.get(servClassEntry.getKey()),
                        txByServiceClassTxName.get(servClassEntry.getKey()),
                        serviceClasses                 
                        );
            });
    }    

    /**
     * Write the service class section of the report. Writes service class information,
     * statistics for ALL transactions in the service class then statistics for 
     * each transaction in the service class.
     * 
     * @param serviceClass the current service class name
     * @param txAll data for all transactions in this service class
     * @param txByTxName data by transaction name
     * @param serviceClasses the service class information from type 72 records
     */
    private static void reportServiceClass(
            String serviceClass,
            TransactionData txAll,
            Map<String, TransactionData> txByTxName, 
            Map<String, ServiceClassInfo> serviceClasses) 
    {
        // write service class name and information from type 72 if available
        System.out.format("%n    Service Class: %-8s Description : %s Goal: %s%n", 
                serviceClass,
                serviceClasses.containsKey(serviceClass) ? 
                        serviceClasses.get(serviceClass).getDescription() 
                        : "",
                serviceClasses.containsKey(serviceClass) ? 
                        serviceClasses.get(serviceClass).getGoal() 
                        : ""
                );
         
        final String headerfmt =  "%n        %-4s %15s %15s %15s %15s %15s %15s%n%n";
        final String detailfmt =    "        %-4s %15d %15f %15f %15f %15f %15f%n";
        
        // Headings
        System.out.format(headerfmt, 
                "Name", 
                "Count", 
                "Tot CPU", 
                "Avg CPU", 
                "Avg Elapsed",                        
                "Max Elapsed",                        
                "Std Dev");                        

        // entry for all transactions in this service class
        System.out.format(detailfmt, 
                "ALL",
                txAll.getCount(), 
                txAll.getCpu(), 
                txAll.getAvgCpu(), 
                txAll.getAvgElapsed(),
                txAll.getMaxElapsed(),
                txAll.getStandardDeviation());
        System.out.println();
        
        txByTxName.entrySet().stream()
            // stream transactions and sort by name
            .sorted(Map.Entry.comparingByKey())
            // write statistics for each transaction
            .forEachOrdered(txEntry -> 
            {
                // write detail line
                System.out.format(detailfmt, 
                        txEntry.getKey(),
                        txEntry.getValue().getCount(), 
                        txEntry.getValue().getCpu(),
                        txEntry.getValue().getAvgCpu(),
                        txEntry.getValue().getAvgElapsed(),
                        txEntry.getValue().getMaxElapsed(),
                        txEntry.getValue().getStandardDeviation()); 
            });
    }
    
    /**
     * A class to collect information about a group of transactions -
     * either all transactions in a service class or a particular
     * transaction name. 
     */
    private static class TransactionData 
    {
        private int count = 0;
        private double totalElapsed = 0;
        private double maxElapsed = 0;
        private double cpu = 0;
        // create a population standard deviation
        private StandardDeviation sd = new StandardDeviation(false);
        
        public void add(PerformanceRecord txData) 
        {
            count++;
            double elapsed = Utils.ToSeconds(
                    Duration.between(txData.getField(Field.START), txData.getField(Field.STOP)));
            
            maxElapsed = maxElapsed > elapsed ? maxElapsed : elapsed;
            totalElapsed += elapsed;
            cpu += txData.getField(Field.USRCPUT).timerSeconds();
            sd.increment(elapsed);
        }

        public int    getCount()             { return count; }
        public Double getMaxElapsed()        { return maxElapsed; }       
        public Double getAvgElapsed()        { return count != 0 ? totalElapsed / count : null; }
        public Double getCpu()               { return cpu; }
        public Double getAvgCpu()            { return count != 0 ? cpu / count : null; }
        public Double getStandardDeviation() { return sd.getResult(); }
    }
    
    /**
     * Collect information about a service class from type 72 records 
     */
    private static class ServiceClassInfo
    {
        private LocalDateTime time; // SMF record time of this entry
        private String description; // service class description
        private String goal;        // service class goal
        
        ServiceClassInfo(Smf72Record r72)
        {
            this.time = r72.smfDateTime();
            this.description = r72.workloadManagerControlSection().r723mcde();
            this.goal = r72.serviceReportClassPeriodDataSections().get(0).goalDescription();
        }
        
        LocalDateTime getTime()        { return time; }
        String        getDescription() { return description; }
        String        getGoal()        { return goal; }
    }
}
