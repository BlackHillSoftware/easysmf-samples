package com.smfreports.json.cics;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.blackhillsoftware.json.cics.*;
import com.blackhillsoftware.json.util.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;
import com.blackhillsoftware.smf2json.cli.*;

/**
 * Write a minute by minute summary of CICS transactions to JSON 
 * <p>
 * This class uses the Smf2JsonCLI class to provide a command line 
 * interface to handle input and output specified by command line 
 * options and generate the JSON.  
 * 
 */

public class CicsTransactionSummary 
{
    public static void main(String[] args) throws IOException
    {
        Smf2JsonCLI smf2JsonCli = Smf2JsonCLI.create()
            .description("Summarize CICS transactions into JSON")
            
            // call processRecord in parallel if the program is
            // invoked using the --parallel option
            .processRecordIsThreadSafe(true)
            
            .includeRecords(110, 1)
            ;
        
        smf2JsonCli.easySmfGsonBuilder()
            .includeZeroValues(false)
            .includeEmptyStrings(false)
            .includeUnsetFlags(false)
            ;
        
        smf2JsonCli.start(new CliClient(), args);
    }
    
    private static class CliClient implements Smf2JsonCLI.Client
    {
        public CliClient()
        {
            // Create and configure the factory to create new transaction groups.
            // Exclude fields used in the key from the grouped data.
            transactionGroupFactory = new CicsTransactionGroupFactory()
                    .exclude(Field.START)
                    .exclude(Field.STOP)
                    .exclude(Field.TRAN)
                    .exclude(Field.TTYPE)
                    .exclude(Field.RTYPE)
                    .exclude(Field.PGMNAME)
                    .exclude(Field.SRVCLSNM)
                    .exclude(Field.RPTCLSNM)
                    .exclude(Field.TCLSNAME)
                    ;
            
            transactionGroupFactory.clockDetail(false);
        }
             
        // Keep the transaction groups in a ConcurrentHashMap because we want to make 
        // the processRecord thread safe so we can process transactions in parallel         
        private Map<HashKey, CicsTransactionGroup> transactionGroups = new ConcurrentHashMap<>();
        
        // Keep track of instances seen without a dictionary
        // ConcurrentHashMap provides a thread safe Set
        private Set<CicsInstanceId> noDictionary = ConcurrentHashMap.newKeySet();
        
        private CicsTransactionGroupFactory transactionGroupFactory;
                
        @Override
        public List<Object> processRecord(SmfRecord record) 
        {
            // This method processes the individual records and adds them to the 
            // summary. There is no record by record output so this method 
            // returns null.
            // The output is created in the onEndOfData() method which returns
            // the transaction groups to be converted to JSON.
            
            Smf110Record r110 = Smf110Record.from(record);
            String smfmnprn = r110.mnProductSection().smfmnprn();
            String smfmnspn = r110.mnProductSection().smfmnspn();
            if (r110.haveDictionary())
            {
                r110.performanceRecords().stream()
                    .forEach(performanceRecord ->
                        {
                            // Create a key for the Map using fields as required
                            // There will be a separate entry for each combination of these
                            // fields.
                            HashKey key = HashKey
                                    .of("smfmnprn", smfmnprn)
                                    .and("smfmnspn", smfmnspn)
                                    .and("minute", performanceRecord.getField(Field.STOP).truncatedTo(ChronoUnit.MINUTES))
                                    .and("tran", performanceRecord.getField(Field.TRAN))
                                    .and("ttype", performanceRecord.getField(Field.TTYPE))
                                    .and("rtype", performanceRecord.getField(Field.RTYPE).trim())
                                    .and("pgmname", performanceRecord.getField(Field.PGMNAME))
                                    .and("srvclsnm", performanceRecord.getField(Field.SRVCLSNM))
                                    .and("rptclsnm", performanceRecord.getField(Field.RPTCLSNM))
                                    .and("tclsname", performanceRecord.getField(Field.TCLSNAME))
                                    ;
                            
                            // Get the existing group or create new.
                            // We used a ConcurrentHashMap so computeIfAbsent is thread safe,
                            // CicsTransactionGroup.add is also specified to be thread safe
                            transactionGroups.computeIfAbsent(
                                    key, 
                                    value -> transactionGroupFactory.createGroup())
                                 .add(performanceRecord);
                        }
                    );
            }
            else
            {
                // only print the error message once per instance
                if (noDictionary.add(r110.cicsInstance()))
                {
                    System.err.println("No dictionary for: " + r110.cicsInstance().toString() + ", skipping record(s)");
                }
            }
            return Collections.emptyList();
        }
        
        @Override
        public List<Object> onEndOfData() 
        {
            System.err.println("End of Data");
            
            return transactionGroups.entrySet().stream()
                    .map(entry -> 
                        new CompositeEntry()
                            .add("recordtype", "CICS Summary")
                            .add(entry.getKey())
                            .add(entry.getValue()))
                    .collect(Collectors.toList());
        }
    } 
}
