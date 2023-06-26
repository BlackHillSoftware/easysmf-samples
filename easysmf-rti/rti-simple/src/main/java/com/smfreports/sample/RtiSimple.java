package com.smfreports.sample;

import java.io.*;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.realtime.*;

/**
 * A simple demonstration of the EasySMF-RTI connection to the
 * SMF Real Time Interface.
 * <p>
 * This program connects to an in memory resource, and prints
 * the date/time, record type and size for each record it receives.
 * It disconnects from the in memory resource and exits after
 * 100 records, or when the MVS STOP command is received.
 * <p>
 * If data is missed (the in memory resource wraps before the
 * data is read) it prints a message and suppresses the 
 * exception.
 *
 */
public class RtiSimple
{
    public static void main(String[] args) throws IOException
    {              
        if (args.length < 1)
        {
            System.out.println("Usage: RtiSimple <resource-name>");
            return;
        }
        
        String inMemoryResource = args[0];
        
        // Connect to the resource, specifying a missed data handler
        // and to disconnect when a STOP command is received.
        // try-with-resources block automatically closes the 
        // SmfConnection when leaving the block
        try (SmfConnection connection = 
                 SmfConnection.forResourceName(inMemoryResource)
                     .onMissedData(RtiSimple::handleMissedData)
                     .disconnectOnStop()
                     .connect())
        {
            // Receive records using Java Stream API
            connection.stream()
                .limit(100) // stop after 100 records
                .forEach(record ->
                {
                    // connection provides byte[] arrays - create a SMF record
                    SmfRecord smfrecord = new SmfRecord(record);
                    System.out.format("%-24s record type: %4d size: %5d%n",
                            smfrecord.smfDateTime(),
                            smfrecord.recordType(),
                            smfrecord.length());
                });
            // SmfConnection automatically closed here 
            // by try-with-resources block
        }
    }
    
    /**
     * Process the missed data event. This method prints a message
     * and indicates that an exception should not be thrown.
     * @param e the missed data event information
     */
    static void handleMissedData(MissedDataEvent e)
    {
        System.out.println("Missed Data!");
        // Suppress the exception
        e.throwException(false);    
    }
}
