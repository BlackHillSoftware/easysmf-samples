import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;

public class Cics1BParallel
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 2)
        {
            System.out.println("Usage: Cics1BParallel <dictionary-name> <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        var start = Instant.now();
        
        Map<TransactionKey, TransactionData> transactions = new ConcurrentHashMap<>();
        
        LongAdder nodictionary = new LongAdder();
        LongAdder txcount = new LongAdder();
        
        Smf110Record.dictionariesFromName(args[0]);
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[1])
        		.include(110, 1))
        {            
            reader.stream()
            	.parallel()
            	.forEach(smfRecord -> 
            	{
                    var r110 = Smf110Record.from(smfRecord);
                    if (r110.haveDictionary())
                    {
                    	String applid = r110.mnProductSection().smfmnprn();
                    	for (var txData : r110.performanceRecords())
                    	{
                    		txcount.increment();
                    		String txName = txData.getField(Field.TRAN);
                    		var key = new TransactionKey(applid, txName);
                    		transactions.computeIfAbsent(key, x -> new TransactionData())
                    			.add(txData);
                    	}                   	
                    }
                    else
                    {
                    	nodictionary.increment();
                    }          		
            	});
        }
        
        writeReport(transactions);
        
        System.out.format(
                "%nTotal Transactions: %,d%n", 
                txcount.sum());
               
        if (nodictionary.sum() > 0) 
        {
            System.out.format(
                    "%nSkipped %,d records because no applicable dictionary was found.%n", 
                    nodictionary.sum());
        }
        
        if (Smf110Record.getCompressedByteCount() > 0) 
        {
            System.out.format(
                    "%nCompressed bytes %,d, decompressed bytes %,d, compression %.1f%%.%n", 
                    Smf110Record.getCompressedByteCount(),
                    Smf110Record.getDecompressedByteCount(),
                    (double)(Smf110Record.getDecompressedByteCount() - Smf110Record.getCompressedByteCount()) / Smf110Record.getDecompressedByteCount() * 100);
        }
        
        System.out.format("Run Time: %s%n", Duration.between(start, Instant.now()));
    }
    
    private static void writeReport(Map<TransactionKey, TransactionData> transactions)
    {
    	System.out.format("%-8s %-4s %15s %15s %15s %15s %15s %15s%n%n", 
                "APPLID", 
                "TRAN", 
                "Count", 
                "Min Elapsed", 
                "Max Elapsed", 
                "Avg Elapsed", 
                "Total CPU", 
                "Avg CPU");
    	
    	transactions.entrySet().stream()
	        .sorted(Map.Entry.comparingByKey(
	                Comparator.comparing(TransactionKey::applid)
	                    .thenComparing(TransactionKey::transaction)))
	        .forEachOrdered(txInfo -> 
	        {
	        	System.out.format("%-8s %-4s %,15d %15f %15f %15f %15f %15f%n",
	        			txInfo.getKey().applid(),
	        			txInfo.getKey().transaction(),
	        			txInfo.getValue().count,
	        			txInfo.getValue().minElapsed,
	        			txInfo.getValue().maxElapsed,
	        			txInfo.getValue().avgElapsed(),
	        			txInfo.getValue().cpu,
	        			txInfo.getValue().avgCpu());        	
	        });	
    }
    
    private static record TransactionKey(String applid, String transaction) {};
    
    private static class TransactionData
    {
    	public synchronized void add(PerformanceRecord txData)
    	{
    		count++;
    		double elapsed = txData.elapsedSeconds();
    		totalElapsed += elapsed;
    		minElapsed = Math.min(minElapsed, elapsed);
    		maxElapsed = Math.max(maxElapsed, elapsed);
    		cpu += txData.getField(Field.USRCPUT).timerSeconds();    		
    	}
    	
    	long count = 0;
    	double totalElapsed = 0;
    	double minElapsed = Double.POSITIVE_INFINITY;
    	double maxElapsed = Double.NEGATIVE_INFINITY;
    	double cpu = 0;
    	
    	double avgElapsed() { return totalElapsed / count; } 
    	double avgCpu() { return cpu / count ; }
    } 
}
