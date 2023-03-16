package com.smfreports.json.cics;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import com.blackhillsoftware.json.util.CompositeEntry;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.monitoring.*;
import com.blackhillsoftware.smf.cics.monitoring.fields.*;
import com.blackhillsoftware.smf2json.cli.*;

/**
 * Write CICS transaction information to JSON.
 * 
 * <p>
 * This class uses the Smf2JsonCLI class to provide a command line 
 * interface to handle input and output specified by command line 
 * options and generate the JSON. 
 * 
 */
public class CicsTransactions 
{          
    public static void main(String[] args) throws IOException
    {
        Smf2JsonCLI smf2JsonCli = Smf2JsonCLI.create()
            .description("Convert CICS transaction information to JSON")
            .includeRecords(110, 1);
        
        setupCommandLineArgs(smf2JsonCli); 
        Configuration config = readCommandLineArgs(args, smf2JsonCli);
        
        // Specify options for creating JSON
        smf2JsonCli.easySmfGsonBuilder()
            .cicsClockDetail(true)
            .includeZeroValues(false)
            .includeEmptyStrings(false)
            .includeUnsetFlags(false)
            ;
        
        smf2JsonCli.start(new CliClient(config), args);    
    }

    private static void setupCommandLineArgs(Smf2JsonCLI smf2JsonCli) 
    {
        smf2JsonCli.options().addOption(
                Option.builder()
                    .longOpt("applid")
                    .hasArgs()
                    .valueSeparator(',')
                    .desc("select applid(s): --applid=AAABAAA[,BBBBBBB...]")
                    .build());
        
        smf2JsonCli.options().addOption(
                Option.builder()
                    .longOpt("xapplid")
                    .hasArgs()
                    .valueSeparator(',')
                    .desc("exclude applid(s):  --xapplid=AAABAAA[,BBBBBBB...]")
                    .build());
        
        smf2JsonCli.options().addOption(
                Option.builder("ms")
                    .longOpt("milliseconds")
                    .hasArg(true)
                    .desc("report transactions longer than this duration")
                    .build());
        
        smf2JsonCli.options().addOption(
                Option.builder()
                    .longOpt("abend")
                    .hasArg(false)
                    .desc("only report abended transactions (ABCODEC or ABCODEO has a value)")
                    .build());
        
        smf2JsonCli.options().addOption(
                Option.builder()
                    .longOpt("tranid")
                    .hasArgs()
                    .valueSeparator(',')
                    .desc("select specific transactions:  --tranid=AAAA[,BBBB...]")
                    .build());
        
        smf2JsonCli.options().addOption(
                Option.builder()
                    .longOpt("xtranid")
                    .hasArgs()
                    .valueSeparator(',')
                    .desc("exclude specific transactions: --xtranid=AAAA[,BBBB...]")
                    .build());
        
    }
    
    private static Configuration readCommandLineArgs(String[] args, Smf2JsonCLI smf2JsonCli) 
    {
        CommandLine commandLine = smf2JsonCli.commandLine(args);
        Configuration config = new Configuration();
        
        config.includeApplids = getValues(commandLine, "applid");
        config.excludeApplids = getValues(commandLine, "xapplid");        
        config.includeTransactions= getValues(commandLine, "tranid");
        config.excludeTransactions = getValues(commandLine, "xtranid");

        config.abendsOnly = commandLine.hasOption("abend");
        
        if (commandLine.hasOption("ms"))
        {
            try
            {
                
                config.thresholdSeconds = Double.parseDouble(
                        smf2JsonCli.commandLine(args).getOptionValue("ms")) 
                            / 1000;
            }
            catch (NumberFormatException ex)
            {
                System.err.println("Failed to parse ms option: " + ex.toString());
                System.exit(0);
            }
        }
        
        return config;
    }

    private static Set<String> getValues(CommandLine commandLine, String option) 
    {
        if (!commandLine.hasOption(option)) return Collections.emptySet();
        Set<String> result = new HashSet<>();
        for (String value : commandLine.getOptionValues(option))
        {
            result.add(value);
        }
        return result;
    }
    
    private static class Configuration
    {
        Set<String> includeApplids = Collections.emptySet();
        Set<String> includeTransactions = Collections.emptySet();
        Set<String> excludeApplids = Collections.emptySet();
        Set<String> excludeTransactions = Collections.emptySet();
        double thresholdSeconds = 0;
        boolean abendsOnly = false;
    }
    
    private static class CliClient implements Smf2JsonCLI.Client
    {
        private Configuration config;
        
        CliClient(Configuration config)
        {
            this.config = config;
        }
        
        @Override
        public List<Object> processRecord(SmfRecord record) 
        {
            List<Object> result = new ArrayList<>();
            Smf110Record r110 = Smf110Record.from(record);
            
            if (includeApplid(r110.mnProductSection().smfmnprn()))
            {
                for (PerformanceRecord transaction : r110.performanceRecords())
                {
                    if (includeTransaction(transaction))
                    {
                        CompositeEntry entry = new CompositeEntry()
                                .add("time", transaction.getField(Field.STOP))
                                .add("recordtype", "CICS Transaction")
                                .add("system", r110.smfsid())
                                .add("smfmnjbn", r110.mnProductSection().smfmnjbn())
                                .add("smfmnprn", r110.mnProductSection().smfmnprn())
                                .add("smfmnspn", r110.mnProductSection().smfmnspn())
                                .add(transaction);
                        result.add(entry);
                    }
                }
            }
            return result;
        }

        private boolean includeApplid(String smfmnprn) 
        {
            if (config.excludeApplids.contains(smfmnprn)) return false;
            
            // if we include specific applids, all others are excluded
            if (!config.includeApplids.isEmpty() 
                    && !config.includeApplids.contains(smfmnprn)) return false;
            return true;
        }
        
        private boolean includeTransaction(PerformanceRecord transaction)
        {
            if (config.excludeTransactions.contains(transaction.getField(Field.TRAN)))
            {
                return false;
            }
            
            // if we include specific transactions, all others are excluded
            if (!config.includeTransactions.isEmpty() 
                    && !config.includeTransactions
                        .contains(transaction.getField(Field.TRAN)))
            {
                return false;
            }
            if (config.thresholdSeconds > 0 
                    && !(transaction.elapsedSeconds() > config.thresholdSeconds))
            {
                return false;
            }
            if (config.abendsOnly  
                    && transaction.getField(Field.ABCODEC).length() == 0  
                    &&  transaction.getField(Field.ABCODEO).length() == 0)
            {
                return false;
            }
            return true;
        }
        
        @Override
        public List<Object> onEndOfData() {
            return Collections.emptyList();
        }
    }
}
