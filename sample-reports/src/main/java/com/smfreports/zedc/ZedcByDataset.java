package com.smfreports.zedc;

import java.io.IOException;
import java.util.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf15.*;


/**
 *
 * List compression statistics for datasets written using zEDC compression. 
 *
 */
public class ZedcByDataset 
{
    public static void main(String[] args) throws IOException                                   
    {
        if (args.length < 1)
        {
            System.out.println("Usage: ZedcByDataset <input-name>");
            System.out.println(
                    "<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        // A map of dataset names to DatasetInfo entries to collect information about each
        // dataset.
        Map<String, DatasetInfo> datasets = new HashMap<String, DatasetInfo>();
        
        try (SmfRecordReader reader = 
                SmfRecordReader
                    .fromName(args[0])
                    .include(15))
        {
            for (SmfRecord record : reader)
            {
                Smf15Record r15 = Smf15Record.from(record);
                if (r15.compressedFormatDatasetSections().size() > 0)
                {
                    datasets.computeIfAbsent(r15.smfjfcb1().jfcbdsnm(), 
                            datasetName -> new DatasetInfo(datasetName))
                            .add(r15);
                }
            }         
        }
        
        String headingFormat = "%-44s %8s %13s %13s %8s%n";
        String detailFormat =  "%-44s %,8d %,13d %,13d %6.1f:1%n";
        
        System.out.format(headingFormat,
                "Dataset",
                "Count",
                "Uncomp MB",
                "Comp MB",
                "Ratio"
                );
        
        // Write report
        datasets.entrySet()
            .stream()
            .map(entry -> entry.getValue())
            .sorted(Comparator.comparing(DatasetInfo::getDatasetName))
            .forEachOrdered(dataset ->
            {
                System.out.format(detailFormat,
                        dataset.getDatasetName(),
                        dataset.count,
                        dataset.uncomp / (1024 * 1024),
                        dataset.comp / (1024 * 1024),
                        dataset.compRatio());
            });
        System.out.println("Done");                      
    }
    
    static class DatasetInfo
    {
        String datasetName;
        int count = 0;
        long comp = 0;
        long uncomp = 0;
        
        DatasetInfo(String datasetName)
        {
            this.datasetName  = datasetName;
        }

        void add(Smf15Record r15)
        {
            count++;
            comp += r15.compressedFormatDatasetSections().get(0)
                    .smf14cds();
            uncomp += r15.compressedFormatDatasetSections().get(0)
                    .smf14cdl();
        }

        String getDatasetName() {
            return datasetName;
        }
        
        float compRatio()
        {
            if (comp == 0) return 0;
            return (float) uncomp / comp;
        }
    }
}
