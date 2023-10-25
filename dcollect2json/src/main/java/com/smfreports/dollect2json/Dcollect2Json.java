package com.smfreports.dollect2json;

import java.io.*;

import com.blackhillsoftware.dcollect.*;
import com.blackhillsoftware.json.*;
import com.blackhillsoftware.json.util.MultiLineArray;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.zutil.io.TextRecordWriter;
import com.blackhillsoftware.zutil.io.ZFileX;
import com.google.gson.Gson;

/**
 *
 * Write DCOLLECT records to multiple datasets in JSON format.
 * 
 * Each record type is written to a different DDNAME. The program 
 * checks which DDNAMEs are actually allocated, and no JSON is 
 * generated for DDs which don't exist. (However if instead you 
 * allocate a DDNAME to DUMMY, the JSON will be generated.)
 * 
 * This needs to be run under the JZOS batch launcher so that
 * DD statements from the JCL can be accessed.
 *
 */
public class Dcollect2Json {

    public static void main(String[] args) throws IOException 
    {
        // We will use 2 different Gson configurations. One 
        // generates JSON in a single line for compactness.
        // The other uses pretty printing for readability, and
        // is used for Base Configuration and Accounting Information 
        // records.
        
        EasySmfGsonBuilder gsonBuilder = new EasySmfGsonBuilder()
            // Exclude record length fields that are not useful in our JSON 
            .exclude("recordLength")
            .exclude("dculeng")            
            .includeUnsetFlags(false);

        EasySmfGsonBuilder prettyGsonBuilder = new EasySmfGsonBuilder()
            .exclude("recordLength")
            .exclude("dculeng")   
            .includeUnsetFlags(false)
            .setPrettyPrinting();

        // All of these entries are in a try-with-resources statement, so 
        // they will all be closed automatically when we exit the block.
        try (
            VRecordReader reader = VRecordReader.fromDD("INPUT");

            // provide a Gson instance to each writer    
            JsonDD dWriter  = new JsonDD("OUTD" , gsonBuilder.createGson());
            JsonDD aWriter  = new JsonDD("OUTA" , gsonBuilder.createGson());
            JsonDD vWriter  = new JsonDD("OUTV" , gsonBuilder.createGson());
            JsonDD mWriter  = new JsonDD("OUTM" , gsonBuilder.createGson());
            JsonDD bWriter  = new JsonDD("OUTB" , gsonBuilder.createGson());
            JsonDD cWriter  = new JsonDD("OUTC" , gsonBuilder.createGson());
            JsonDD tWriter  = new JsonDD("OUTT" , gsonBuilder.createGson());
            JsonDD dcWriter = new JsonDD("OUTDC", gsonBuilder.createGson());
            JsonDD scWriter = new JsonDD("OUTSC", gsonBuilder.createGson());
            JsonDD mcWriter = new JsonDD("OUTMC", gsonBuilder.createGson());
            JsonDD bcWriter = new JsonDD("OUTBC", prettyGsonBuilder.createGson());
            JsonDD sgWriter = new JsonDD("OUTSG", gsonBuilder.createGson());
            JsonDD vlWriter = new JsonDD("OUTVL", gsonBuilder.createGson());
            JsonDD agWriter = new JsonDD("OUTAG", gsonBuilder.createGson());
            JsonDD drWriter = new JsonDD("OUTDR", gsonBuilder.createGson());
            JsonDD lbWriter = new JsonDD("OUTLB", gsonBuilder.createGson());
            JsonDD cnWriter = new JsonDD("OUTCN", gsonBuilder.createGson());
            JsonDD aiWriter = new JsonDD("OUTAI", prettyGsonBuilder.createGson());
            )
        {
            // Read the records and pass them to the appropriate writer.
            // The writer will convert to JSON and write the output only
            // if the corresponding DD was allocated to the job.
            for (VRecord record : reader)
            {
                DcollectRecord dcollect = DcollectRecord.from(record);
                switch (dcollect.dcurctyp())
                {
                    case A:
                        aWriter.write(dcollect);
                        break;
                    case AG:
                        agWriter.write(dcollect);
                        break;
                    case AI:
                        aiWriter.write(dcollect);
                        break;
                    case B:
                        bWriter.write(dcollect);
                        break;
                    case BC:
                        bcWriter.write(dcollect);
                        break;
                    case C:
                        cWriter.write(dcollect);
                        break;
                    case CN:
                        cnWriter.write(dcollect);
                        break;
                    case D:
                        dWriter.write(dcollect);
                        break;
                    case DC:
                        dcWriter.write(dcollect);
                        break;
                    case DR:
                        drWriter.write(dcollect);
                        break;
                    case LB:
                        lbWriter.write(dcollect);
                        break;
                    case M:
                        mWriter.write(dcollect);
                        break;
                    case MC:
                        mcWriter.write(dcollect);
                        break;
                    case SC:
                        scWriter.write(dcollect);
                        break;
                    case SG:
                        sgWriter.write(dcollect);
                        break;
                    case T:
                        tWriter.write(dcollect);
                        break;
                    case V:
                        vWriter.write(dcollect);
                        break;
                    case VL:
                        vlWriter.write(dcollect);
                        break;
                    default:
                        break;   
                }
            }
        } // All reader and writers automatically closed here
    }

    /**
     * Class to write JSON to an allocated DD name.
     * 
     * This class checks whether the DD name exists.
     * If the DD does not exist, it will simply ignore the
     * DCOLLECT records.
     * 
     * If the DD exists, it is opened using the Black Hill
     * Software TextRecordWriter class. This class writes
     * EBCDIC data using the JZOS record writer class. It can
     * handle multiple lines produced by JSON pretty printing.
     * 
     * JSON records are wrapped in a MultiLineArray from 
     * EasySMF-JSON. Programs like Excel require multiple JSON 
     * records to be contained in an array. The MultiLineArray
     * class splits a compact JSON array into a line per record, 
     * so that it can be contained in a z/OS dataset with limited 
     * LRECL.
     * 
     * The class implements Closeable so it can
     * be automatically closed in a try-with-resources block. 
     * The close method will also close the JSON array.
     *
     */
    private static class JsonDD implements Closeable
    {
        TextRecordWriter writer = null;
        MultiLineArray<DcollectRecord> jsonArray;

        /**
         * Check if the specified DD exists, and create a writer and
         * begin an array if it does.
         * 
         * @param ddname the output DDNAME
         * @param gson a Gson instance to convert data to JSON
         * @throws IOException if an I/O error occurs
         */
        public JsonDD(String ddname, Gson gson) throws IOException 
        {
            if (ZFileX.ddExists(ddname))
            {
                writer = TextRecordWriter.newWriterForDD(ddname);
                
                // Use the EasySMF-JSON MultiLineArray array class to wrap
                // the records so the output can be opened by programs that
                // require records in a JSON array, e.g. Excel, but the
                // output hopefully fits in a z/OS dataset record.
                jsonArray = new MultiLineArray<>(gson);
            }
        }

        /**
         * Write a record as a JSON array element if a writer exists
         * for this DD name (ie the DD was allocated)
         * 
         * @param dcollectrecord the record to write
         * @throws IOException if an I/O error occurs
         */
        public void write(DcollectRecord dcollectrecord) throws IOException
        {
            if (writer != null)
            {
                writer.writeLine(jsonArray.element(dcollectrecord));
            }
        }

        /**
         * Close the output array and writer
         */
        @Override
        public void close() throws IOException 
        {
            if (writer != null) 
            {
                // Write the JSON end of array
                try
                {
                    writer.writeLine(jsonArray.endArray());
                }
                // If by chance writing the end of array threw an exception, 
                // we will still try to close the writer before re-throwing the
                // original exception
                catch (Exception e)
                {
                    writer.close();
                    writer = null;
                    throw e;
                }
                writer.close();
                writer = null;
            }
        }
    }
}
