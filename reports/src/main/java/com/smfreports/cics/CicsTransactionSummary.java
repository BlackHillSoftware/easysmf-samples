package com.smfreports.cics;

import java.io.*;
import java.time.*;
import java.util.*;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;

public class CicsTransactionSummary 
{
    public static void main(String[] args) throws IOException 
    {
        if (args.length < 1)
        {
            System.out.println("Usage: CicsTransactionSummary <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        Map<String, Map<String, TransactionData>> applids = 
                new HashMap<String, Map<String, TransactionData>>();

        int noDictionary = 0;
        int txCount = 0;

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {     
            reader.include(110, Smf110Record.SMFMNSTY);
            for (SmfRecord record : reader) 
            {
                Smf110Record r110 = Smf110Record.from(record);
                
                if (r110.haveDictionary()) 
                {
                    Map<String, TransactionData> applidTransactions = 
                        applids.computeIfAbsent(
                            r110.mnProductSection().smfmnprn(), 
                            transactions -> new HashMap<String, TransactionData>());

                    for (PerformanceRecord mn : r110.performanceRecords()) 
                    {
                        String txName = mn.getField(Field.TRAN);
                        txCount++;
                        applidTransactions.computeIfAbsent(
                                txName, 
                                x -> new TransactionData(txName)).add(mn);
                    }
                } 
                else 
                {
                    noDictionary++;
                }
            }
        }
        
        writeReport(applids);
        
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
                    "%n%nCompressed bytes %,d, decompressed bytes %,d, compression %.1f%%.", 
                    Smf110Record.getCompressedByteCount(),
                    Smf110Record.getDecompressedByteCount(),
                    (double)(Smf110Record.getDecompressedByteCount() - Smf110Record.getCompressedByteCount()) / Smf110Record.getDecompressedByteCount() * 100);
        }
    }

    private static void writeReport(Map<String, Map<String, TransactionData>> transactions) 
    {
        transactions.entrySet().stream()
            .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
            .forEachOrdered(applid -> 
            {
                // Headings
                System.out.format("%n%-8s", applid.getKey());
    
                System.out.format("%n%-4s %15s %15s %15s %15s %15s %15s%n%n", 
                        "Name", 
                        "Count", 
                        "Avg Elapsed", 
                        "CPU", 
                        "Avg CPU", 
                        "Avg Disp.", 
                        "Avg Disp Wait");
    
                applid.getValue().entrySet().stream()
                    .map(x -> x.getValue())
                    .sorted(comparing(TransactionData::getCpu, reverseOrder())
                            .thenComparing(TransactionData::getCount, reverseOrder()))
                    .forEachOrdered(txInfo -> 
                    {
                        // write detail line
                        System.out.format("%-4s %15d %15f %15f %15f %15f %15f%n", 
                                txInfo.getName(),
                                txInfo.getCount(), 
                                txInfo.getAvgElapsed(), 
                                txInfo.getCpu(),
                                txInfo.getAvgCpu(), 
                                txInfo.getAvgDispatch(),
                                txInfo.getAvgDispatchWait());

                    });
            });

    }

    private static class TransactionData 
    {
        public TransactionData(String name) 
        {
            this.name = name;
        }

        public void add(PerformanceRecord perfdata) 
        {
            count++;
            elapsed += Utils.ToSeconds(
                    Duration.between(perfdata.getField(Field.START), perfdata.getField(Field.STOP)));
            dispatch += perfdata.getFieldTimerSeconds(Field.USRDISPT);
            dispatchWait += perfdata.getFieldTimerSeconds(Field.DISPWTT);
            cpu += perfdata.getFieldTimerSeconds(Field.USRCPUT);
        }

        public String getName() 
        {
            return name;
        }

        public int getCount() 
        {
            return count;
        }

        public double getCpu() 
        {
            return cpu;
        }

        public Double getAvgElapsed() 
        {
            return count != 0 ? elapsed / count : null;
        }

        public Double getAvgDispatch() 
        {
            return count != 0 ? dispatch / count : null;
        }

        public Double getAvgDispatchWait() 
        {
            return count != 0 ? dispatchWait / count : null;
        }

        public Double getAvgCpu() 
        {
            return count != 0 ? cpu / count : null;
        }

        private String name;
        private int count = 0;
        private double elapsed = 0;
        private double dispatch = 0;
        private double dispatchWait = 0;
        private double cpu = 0;
    }
}
