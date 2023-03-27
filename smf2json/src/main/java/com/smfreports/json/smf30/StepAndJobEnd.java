package com.smfreports.json.smf30;

import java.io.IOException;
import java.util.*;

import com.blackhillsoftware.json.util.CompositeEntry;
import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.smf30.*;
import com.blackhillsoftware.smf2json.cli.Smf2JsonCLI;

/**
 * Format the major sections from SMF 30 job and step end records
 * <p>
 * This class uses the Smf2JsonCLI class to provide a command line 
 * interface to handle input and output specified by command line 
 * options and generate the JSON. 
 *
 */
public class StepAndJobEnd 
{
    public static void main(String[] args) throws IOException                                   
    {
        Smf2JsonCLI cli = Smf2JsonCLI.create()
                .includeRecords(30,4)
                .includeRecords(30,5)
                .description("Format SMF 30 job and step end records");
        
        cli.easySmfGsonBuilder()
            .includeZeroValues(false)
            .includeEmptyStrings(false)
            .includeUnsetFlags(false)
            
            // exclude individual date/time fields and combine into 1 entry 
            .exclude("smf30rsd")
            .exclude("smf30rst")
            .calculateEntry(IdentificationSection.class, "readStart", 
                    section -> section.smf30rsd().atTime(section.smf30rst()))
            
            .exclude("smf30red")
            .exclude("smf30ret")
            .calculateEntry(IdentificationSection.class, "readEnd", 
                    section -> section.smf30red().atTime(section.smf30ret()))
            
            .exclude("smf30std")
            .exclude("smf30sit")
            .calculateEntry(IdentificationSection.class, "start", 
                    section -> section.smf30std().atTime(section.smf30sit()))
            
            .exclude("smf30cor")
            .exclude("smf30casOa54589")
            .exclude("smf30srv") // use smf30srvL
            .exclude("smf30csu") // use smf30csuL
            .exclude("smf30srb") // use smf30srbL
            .exclude("smf30io")  // use smf30ioL
            .exclude("smf30mso") // use smf30msoL
            .exclude("smf30esu") // use smf30esuL
            .exclude("smf30tep") // use smf30tex
            ;
                        
        cli.start(new CliClient(), args);
    }

    private static class CliClient implements Smf2JsonCLI.Client
    {
        @Override
        public List<Object> onEndOfData() 
        {
            return null;
        }

        @Override
        public List<Object> processRecord(SmfRecord record)
        {
            Smf30Record r30 = Smf30Record.from(record);
            
            // We will only create one JSON record per job, so we check whether this is 
            // the first record as indicated by the SMF 30 documentation.
            // Certain sections are documented to only occur in the first record.
            // Sections where there are multiple sections which may flow into subsequent
            // records are not included for simplicity.
 
            // Check if first record
            if (r30.header().smf30ton() > 0     // completion section
                || r30.header().smf30con() > 0  // processor accounting section
                || r30.header().smf30pon() > 0  // performance section
                || r30.header().smf30ron() > 0  // storage and paging section
                || r30.header().smf30uon() > 0  // I/O activity
                || r30.header().smf30oon() > 0  // Operator section
                || r30.header().smf30aon() > 0  // Accounting
                || r30.header().smf30arn() > 0  // APPC/MVS Cumulative Resource Section
                || r30.header().smf30drn() > 0  // APPC/MVS Resource Section
            )
            {
                CompositeEntry result = new CompositeEntry();
                
                result.add("event", r30.subType() == 5 ? "Job End" : "Step End")
                    .add("time", r30.smfDateTime())
                    .add("system", r30.system())
                    .add("sysplex", r30.subSystemSection().smf30syp())
                    .add("smf30wid", r30.header().smf30wid())
                    .add(r30.identificationSection())
                    .add(r30.completionSection())
                    .add(r30.processorAccountingSection())
                    .add(r30.performanceSection())
                    .add(r30.storageSection())
                    .add(r30.ioActivitySection())
                    .add(r30.operatorSection())
                    .add(r30.appcCumulativeResourceSection())
                    .add(r30.appcResourceSection());
                
                if (r30.accountingSections() != null && r30.accountingSections().size() > 0)
                {
                    result.add("accounting",r30.accountingSections());
                }
                return Collections.singletonList(result);
            }
            else
            {
                return Collections.emptyList();
            }
        } 
    }
}
