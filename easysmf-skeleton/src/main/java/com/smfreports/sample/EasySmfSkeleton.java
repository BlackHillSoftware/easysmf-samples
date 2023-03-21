package com.smfreports.sample;

import java.io.*;
import java.util.*;
import com.blackhillsoftware.smf.*;
// import SMF record classes as required e.g.:
import com.blackhillsoftware.smf.smf30.*;

public class EasySmfSkeleton
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: EasySmfSkeleton <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
        
        try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))
    	{
            int count = 0;
            for (SmfRecord record : reader)
            {
                count++;
            }
            System.out.format("Read %,d records%n", count);
    	}
    }
}
