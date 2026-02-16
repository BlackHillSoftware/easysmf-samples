import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.Comparator.comparing;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;

/**
 * CICS transaction response time by APPLID and TRAN.
 */
public class CicsTransactionResponse 
{
    public static void main(String[] args) throws IOException 
    {
        if (args.length < 2)
        {
            System.out.println("Usage: CicsTransactionResponse <dictionary-name> <input-name> <input-name2> ...");
            System.out.println("<dictionary-name> CICS dictionaries, can be filename, //DD:DDNAME or //'DATASET.NAME'");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        Smf110Record.dictionariesFromName(args[0]);
        
        Map<ApplidTran, TransactionData> data = new HashMap<>();

        int noDictionary = 0;
        int txCount = 0;

        for (int i = 1; i < args.length; i++)
        {
            String name = args[i];
            try (SmfRecordReader reader = SmfRecordReader.fromName(name)
            		.include(110, 1)) 
            {     
                for (SmfRecord smfrecord : reader) 
                {
                    Smf110Record r110 = Smf110Record.from(smfrecord);
                    
                    if (r110.haveDictionary()) 
                    {
                        String applid = r110.mnProductSection().smfmnprn();
                        for (PerformanceRecord txData : r110.performanceRecords()) 
                        {
                            String txName = txData.getField(Field.TRAN);
                            txCount++;
                            ApplidTran key = new ApplidTran(applid, txName);
                            data.computeIfAbsent(key, k -> new TransactionData()).add(txData);
                        }
                    } 
                    else 
                    {
                        noDictionary++;
                    }
                }
            }
        }
        
        writeReport(data);
        
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
                    (double)(Smf110Record.getDecompressedByteCount() - Smf110Record.getCompressedByteCount()) / Smf110Record.getDecompressedByteCount() * 100);
        }
    }

    private static void writeReport(Map<ApplidTran, TransactionData> data) 
    {
        data.entrySet().stream()
            .collect(Collectors.groupingBy(e -> e.getKey().applid()))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEachOrdered(applidSection -> 
            {
                System.out.format("%n%-10s %-4s %15s %15s %15s %15s %15s %15s %15s%n", 
                        "APPLID", 
                        "Tran", 
                        "Count", 
                        "CPU", 
                        "Avg CPU", 
                        "Avg Elapsed", 
                        "Avg Term Waits", 
                        "Avg Term Wait", 
                        "Avg Response");
                applidSection.getValue().stream()
                    .sorted(comparing(e -> e.getKey().tran()))
                    .forEachOrdered(e -> 
                    {
                        TransactionData tx = e.getValue();
                        System.out.format("%-10s %-4s %15d %15f %15f %15f %15.1f %15f %15f%n", 
                                e.getKey().applid(),
                                e.getKey().tran(),
                                tx.getCount(), 
                                tx.getSumCpu(), 
                                tx.getAvgCpu(), 
                                tx.getAvgElapsed(), 
                                tx.getAvgTciowttCount(), 
                                tx.getAvgTciowttTime(),
                                tx.getAvgResponse());
                    });
                System.out.println();
            });
    }

    private record ApplidTran(String applid, String tran) {}

    private static class TransactionData 
    {
        public void add(PerformanceRecord txData) 
        {
            count++;
            elapsed += Utils.ToSeconds(
                    Duration.between(txData.getField(Field.START), txData.getField(Field.STOP)));
            cpu += txData.getField(Field.USRCPUT).timerSeconds();
            tciowttCount += txData.getField(Field.TCIOWTT).count();
            tciowttTime += txData.getField(Field.TCIOWTT).timerSeconds();
        }

        public int getCount() 
        {
            return count;
        }

        public double getSumCpu() 
        {
            return cpu;
        }

        public Double getAvgCpu() 
        {
            return count != 0 ? cpu / count : null;
        }

        public Double getAvgElapsed() 
        {
            return count != 0 ? elapsed / count : null;
        }

        public Double getAvgTciowttCount() 
        {
            return count != 0 ? (double) tciowttCount / count : null;
        }

        public Double getAvgTciowttTime() 
        {
            return count != 0 ? tciowttTime / count : null;
        }

        public double getAvgResponse() 
        {
            long responseCount = count + tciowttCount;
            return responseCount != 0 ? (elapsed - tciowttTime) / responseCount : 0.0;
        }

        private int count = 0;
        private double elapsed = 0;
        private double cpu = 0;
        private long tciowttCount = 0;
        private double tciowttTime = 0;
    }
}
