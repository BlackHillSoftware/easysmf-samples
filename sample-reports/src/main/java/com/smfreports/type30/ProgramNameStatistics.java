package com.smfreports.type30;

import java.io.*;
import java.util.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf30.Smf30Record;

public class ProgramNameStatistics
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: ProgramNameStatistics <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        Map<String, ProgramData> programs = new HashMap<String, ProgramData>();

        // SmfRecordReader.fromName(...) accepts a filename, a DD name in the
        // format //DD:DDNAME or MVS dataset name in the form //'DATASET.NAME'
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
        {  
            reader
                .include(30, 4)
                .stream()
                .map(record -> Smf30Record.from(record))
                .filter(r30 -> r30.completionSection() != null 
                    && !r30.completionSection().smf30flh()) // not flushed
                .filter(r30 -> r30.header().smf30wid().equals("JES2"))
                .forEach(r30 ->
                {
                    String programName = r30.identificationSection().smf30pgm();
                    programs
                        .computeIfAbsent(programName, x -> new ProgramData(programName))
                        .add(r30);
                });
        }

        writeReport(programs);
    }

    private static void writeReport(Map<String, ProgramData> programs)
    {
        // Headings
        System.out.format("%n%-8s %11s %11s %11s %11s %14s %11s %11s %11s %11s %11s%n", 
                "Name", "Count", 
                "CPU(s)", "zIIP(s)", "Connect(s)", "Excp",
                "Avg CPU", "Avg zIIP", "Avg Connect", "Avg Excp",
                "CPU/IO");
        programs.values().stream()
        .sorted(Comparator.comparingDouble(ProgramData::getCpTime).reversed())
        .limit(100)
        .forEachOrdered(program -> 
            System.out.format(
                    "%-8s %11d %11.0f %11.0f %11.0f %14d %11.2f %11.2f %11.2f %11d %11f%n",
                    program.getName(), 
                    program.getCount(),
                    program.getCpTime(),
                    program.getZiipTime(),
                    program.getConnect(),
                    program.getExcps(),
                    program.getAvgCpTime(),
                    program.getAvgZiipTime(),
                    program.getAvgConnect(),
                    program.getAvgExcps(),
                    program.getCpuPerIo())                     
        );
    }

    private static class ProgramData
    {    
        public ProgramData(String programName)
        {
            name = programName;
        }

        public void add(Smf30Record record)
        {
            if (record.processorAccountingSection() != null)
            {
                count++; // increment count only for processor accounting sections
                cpTime += record.processorAccountingSection().smf30cptSeconds() 
                        + record.processorAccountingSection().smf30cpsSeconds();
                ziipTime += record.processorAccountingSection().smf30TimeOnZiipSeconds();
            }

            if (record.ioActivitySection() != null)
            {
                excps += record.ioActivitySection().smf30tex();
                connect +=  record.ioActivitySection().smf30aicSeconds();
            }
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public double getCpTime() {
            return cpTime;
        }

        public double getZiipTime() {
            return ziipTime;
        }

        public double getConnect() {
            return connect;
        }

        public long getExcps() {
            return excps;
        }

        public Double getAvgCpTime() {
            return count == 0 ? null : cpTime / count;
        }

        public Double getAvgZiipTime() {
            return count == 0 ? null : ziipTime / count;
        }

        public Double getAvgConnect() {
            return count == 0 ? null : connect / count;
        }

        public Long getAvgExcps() {
            return count == 0 ? null : excps / count;
        }

        public Double getCpuPerIo() {
            return excps == 0 ? null : (cpTime + ziipTime) / excps;
        }

        private String name;
        private int count = 0;
        private double cpTime = 0;
        private double ziipTime = 0;
        private double connect = 0;
        private long excps = 0;
    }
}
