package com.smfreports.json.cics;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.json.util.CompositeEntry;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.statistics.*;
import com.blackhillsoftware.smf2json.cli.*;

/**
 * Write CICS statistics records as JSON
 * 
 * <p>
 * This class uses the Smf2JsonCLI class to provide a command line 
 * interface to handle input and output specified by command line 
 * options and generate the JSON. 
 */

public class CicsStatistics 
{
    public static void main(String[] args) throws IOException 
    {
        Smf2JsonCLI smf2JsonCli = Smf2JsonCLI.create()
            .description("Convert CICS Statistics Sections to JSON")
            .includeRecords(110);
        
        // exclude individual date/time fields, will be combined into time entry below 
        smf2JsonCli.easySmfGsonBuilder()
            .exclude(StProductSection.class, "smfstdat")
            .exclude(StProductSection.class, "smfstclt");
    
        // exclude some other non-interesting fields
        smf2JsonCli.easySmfGsonBuilder()
            .exclude(StProductSection.class, "smfstrsd")
            .exclude(StProductSection.class, "smfstrst")
            .exclude(StProductSection.class, "smfstcst")
            .exclude(StProductSection.class, "smfstrvn")
            .exclude(StProductSection.class, "smfstmfl")
            .exclude(StProductSection.class, "smfstpdn");
        
            smf2JsonCli.start(new Client(), args);
    }
    
    private static class Client implements Smf2JsonCLI.Client
    {
        @Override
        public List<Object> processRecord(SmfRecord record) 
        {
            Smf110Record r110 = Smf110Record.from(record);
            List<Object> result = new ArrayList<>();
            String system = r110.smfsid();
            for (StatisticsDataSection stats : r110.statisticsDataSections())
            {
                result.add(new CompositeEntry()
                        .add("time", r110.stProductSection().smfstdat()
                                .atTime(r110.stProductSection().smfstclt()))
                        .add("recordtype", "CICS Statistics")
                        .add("system", system)
                        .add(r110.stProductSection())
                        .add(stats));
            }
            return result;
        }

        @Override
        public List<Object> onEndOfData() 
        {
            System.err.println("Finished");
            return Collections.emptyList();
        }
    }
}
