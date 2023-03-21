package com.smfreports.cics;

import java.io.*;
import java.util.*;
import static java.util.Comparator.comparing;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.cics.*;
import com.blackhillsoftware.smf.cics.statistics.FileControlStatistics;

public class CicsFileStatistics 
{
    public static void main(String[] args) throws IOException 
    {
        if (args.length < 1)
        {
            System.out.println("Usage: CicsFileStatistics <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        Map<String, Map<String, FileData>> applids = 
                new HashMap<String, Map<String, FileData>>();

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {
            reader.include(110, Smf110Record.SMFSTSTY);
            for (SmfRecord record : reader) 
            {
                Smf110Record r110 = Smf110Record.from(record);

                Map<String, FileData> applidFiles = 
                        applids.computeIfAbsent(r110.stProductSection().smfstprn(),
                        files -> new HashMap<String, FileData>());

                for (FileControlStatistics fileStats : r110.fileControlStatistics()) 
                {
                    String entryName = fileStats.a17fnam();
                    applidFiles.computeIfAbsent(entryName, 
                            x -> new FileData(entryName)).add(fileStats);
                }
            }
        }
        writeReport(applids);
    }

    private static void writeReport(Map<String, Map<String, FileData>> applidFiles) 
    {
        
        applidFiles.entrySet().stream()
            .filter(applid -> !applid.getValue().isEmpty())
            .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
            .forEachOrdered(applid -> 
            {
                // Headings
                System.out.format("%n%-8s", applid.getKey());

                System.out.format("%n%-8s %12s %12s %12s %12s %12s %12s %12s %12s%n%n", 
                        "ID", 
                        "Gets", 
                        "Get Upd",
                        "Browse", 
                        "Adds", 
                        "Updates", 
                        "Deletes", 
                        "Data EXCP", 
                        "Index EXCP");

                applid.getValue().entrySet().stream()
                    .map(files -> files.getValue())
                    .filter(file -> !file.noActivity())
                    .sorted(comparing(FileData::getTotalExcps)
                            .reversed())
                    .forEachOrdered(fileInfo -> 
                    {
                        // write detail line
                        System.out.format("%-8s %12d %12d %12d %12d %12d %12d %12d %12d%n", 
                                fileInfo.getId(),
                                fileInfo.getGets(), 
                                fileInfo.getGetUpd(), 
                                fileInfo.getBrowse(),
                                fileInfo.getAdds(), 
                                fileInfo.getUpdates(), 
                                fileInfo.getDeletes(),
                                fileInfo.getDataExcps(), 
                                fileInfo.getIndexExcps());
                    });
                });

    }

    private static class FileData 
    {
        public FileData(String fileId)
        {
            this.id = fileId;
        }

        public void add(FileControlStatistics fileStatistics) 
        {
            gets += fileStatistics.a17dsrd();
            getupd += fileStatistics.a17dsgu();
            browse += fileStatistics.a17dsbr();
            add = fileStatistics.a17dswra();
            update = fileStatistics.a17dswru();
            delete = fileStatistics.a17dsdel();
            dataexcp = fileStatistics.a17dsxcp();
            indexexcp = fileStatistics.a17dsixp();
            totalexcp += fileStatistics.a17dsxcp() 
                    + fileStatistics.a17dsixp();
        }

        public String getId() 
        {
            return id;
        }

        public long getGets() 
        {
            return gets;
        }

        public long getGetUpd() 
        {
            return getupd;
        }

        public long getBrowse() 
        {
            return browse;
        }

        public long getAdds() 
        {
            return add;
        }

        public long getUpdates() 
        {
            return update;
        }

        public long getDeletes() 
        {
            return delete;
        }

        public long getDataExcps() 
        {
            return dataexcp;
        }

        public long getIndexExcps() 
        {
            return indexexcp;
        }

        public long getTotalExcps() 
        {
            return totalexcp;
        }

        public boolean noActivity()
        {
            return gets == 0 
                    && getupd == 0
                    && browse == 0
                    && add == 0
                    && update == 0
                    && delete == 0
                    && dataexcp == 0
                    && indexexcp == 0
                    && totalexcp == 0;
        }
        
        private String id;
        private long gets = 0;
        private long getupd = 0;
        private long browse = 0;
        private long add = 0;
        private long update = 0;
        private long delete = 0;
        private long dataexcp = 0;
        private long indexexcp = 0;
        private long totalexcp = 0;
    }
}
