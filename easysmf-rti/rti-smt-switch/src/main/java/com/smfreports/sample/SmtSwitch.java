package com.smfreports.sample;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.realtime.*;
import com.blackhillsoftware.smf.smf70.*;
import com.blackhillsoftware.smf.smf72.Smf72Record;
import com.blackhillsoftware.smf.smf72.subtype3.ServiceReportClassPeriodDataSection;

/**
 * Read SMF 70 and 72 records from the SMF Real Time Interface, and 
 * check the current SMT status against the zIIP velocity calculated 
 * from the zIIP using and delay samples for all service classes.
 * 
 * ZIIP velocity thresholds are defined for turning SMT on and off.
 *  
 * The program can issue a message for information or action by
 * automation, or issue a command to set SMT1 or SMT2.
 * 
 * This program demonstrates usage of the EasySMF-RTI SMF real time 
 * interface. It is not a recommendation for how to manage your zIIP 
 * processors.
 * 
 * Consult your performance specialist to decide whether you should
 * run with SMT2 enabled, disabled or dynamically switched. 
 *
 */
public class SmtSwitch
{
    /*
     * If velocity is high (little waiting) turn SMT2 off. If velocity
     * is low (more waiting) turn SMT2 on.
     * 
     * ********************************************************************
     * These values are plucked from the air and are not recommendations.
     * Your values will depend on the workload in your system, the number
     * of ZIIP processors available etc.
     * ********************************************************************
     * 
     * Enabling SMT2 will change the achieved velocities, OFF and ON 
     * thresholds should be far enough apart that the change in velocity 
     * doesn't result in cycling back and forth.
     *  
     */
    private static final double SMTOFF = 80;
    private static final double SMTON = 50;
    
    // we need to gather information from multiple records
    // for the interval - we have a class and variable to store it.
    private static Interval currentInterval = null;
        
    public static void main(String[] args) throws IOException
    {              
        if (args.length < 1)
        {
            System.out.println("Usage: SmtSwitch <resource-name>");
            return;
        }
        
        String inMemoryResource = args[0];
        // Connect to the resource, specifying a missed data handler
        // and to disconnect when a STOP command is received.
        // try-with-resources block automatically closes the 
        // SmfConnection when leaving the block
        try (SmfConnection connection = 
                 SmfConnection.forResourceName(inMemoryResource)
                     .onMissedData(SmtSwitch::handleMissedData)
                     .disconnectOnStop()
                     .connect();
             SmfRecordReader reader = SmfRecordReader.fromByteArrays(connection)
                     .include(70,1)
                     .include(72,3)
                     )
        {
            // read and process records
            readRecords(reader);
        }
    }

    /**
     * Process the SMF records from the reader.
     * 
     * We collect SMF 70 and SMF 72 records for the interval. We keep the 
     * interval token smf70iet/smf72iet and compare it to the token in each 
     * record. When we see a new token, we start a new interval, whether or
     * not we considered the last interval complete.
     * 
     * We only run when a new SMF record is received, so we need to know when
     * we are processing the last of the SMF 70 and 72 records for the 
     * interval.
     * 
     * SMF 70 records contain the information we need to know how many records
     * to expect. SMF 72 records don't seem to have that information.
     * 
     * For SMF 72 records, we keep track of how many service classes were seen
     * in the previous interval. When we have seen the same number, we assume
     * that we have all the type 72 data for the interval. This means that:
     * - the first interval is only used to count the service classes, we don't
     * take any action
     * - if the number of service classes decreases we won't be able to tell 
     * that the interval was complete until the next interval, so we can't take 
     * any action for the first interval with the decreased number.
     * - if the number of service classes increases we will calculate the 
     * velocity based on an incomplete list of service classes. Normally there
     * should still be enough service classes to give a reasonable result.
     * The next interval will use the corrected number.
     * 
     * We don't take any action based on short intervals (less than 75% of the
     * original interval length value).
     * 
     * @param reader a SmfRecordReader for the SMF Real Time Interface connection
     */
    private static void readRecords(SmfRecordReader reader) 
    {
        for (SmfRecord record : reader)
        {
            switch (record.recordType())
            {
            case 70:
                {
                    Smf70Record r70 = Smf70Record.from(record);
                    // create a new interval first time round, of if the IET token has changed.
                    if (currentInterval == null 
                        || !currentInterval.intervalToken.equals(
                                r70.productSection().smf70iet()
                                    .withZoneSameInstant(r70.productSection().smf70lgo())))
                    {
                        currentInterval = new Interval(r70, currentInterval);
                    }
                    // add the data to the interval
                    currentInterval.add(r70);                    
                }
                break;
            case 72:
                {
                    Smf72Record r72 = Smf72Record.from(record);
                    // create a new interval first time round, of if the IET token has changed.
                    if (currentInterval == null 
                            || !currentInterval.intervalToken.equals(
                                    r72.productSection().smf72iet()
                                        .withZoneSameInstant(r72.productSection().smf72lgo())))
                    {
                        currentInterval = new Interval(r72, currentInterval);
                    }
                    // add the data to the interval
                    currentInterval.add(r72);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unexpected record type: " + record.recordType());
            }
            
            if (currentInterval.isComplete()
                && currentInterval.intervalLength > currentInterval.originalIntervalLength * 0.75 // skip short intervals
                && !currentInterval.processed)
            {
                // Process the interval, however it is possible that there are additional service classes
                // to come. We keep the current interval active so we can add service classes for an 
                // accurate number next interval.
                
                processInterval();
            }                
        }
    }

    private static void processInterval()
    {
        Optional<Double> ziipVelocity = currentInterval.ziipVelocity();       
        boolean smt2 = currentInterval.isSmt2();
        
        System.out.format("%-35s SMT2: %s zIIP Velocity: %3.0f Thresholds %2.0f %2.0f%n",
                currentInterval.intervalToken,
                smt2,
                ziipVelocity.orElse(null),
                SMTON, 
                SMTOFF);
        
        if (!ziipVelocity.isPresent()) return; // zIIP values were all zero - no velocity
                
        // Uncomment the MVS message and/or command as required.
        // Requires JZOS to build and run - uncomment JZOS dependency in pom.xml for Maven build
        
        // High velocity value implies that there is little time spent waiting for CPU, 
        // so may be better with single thread per CPU.
        if (smt2 && ziipVelocity.get() > SMTOFF)         
        {
            System.out.println("TURN SMT OFF");
                        
//            com.ibm.jzos.MvsConsole.wto("TURN SMT OFF",
//                    com.ibm.jzos.WtoConstants.ROUTCDE_MASTER_CONSOLE_INFORMATION,
//                    com.ibm.jzos.WtoConstants.DESC_SYSTEM_STATUS);
//            
//            mvsCmd("SET OPT=T1");                                                                                
            
        }

        // Lower velocity value implies that there is more time spent waiting for CPU, 
        // so we might benefit from SMT2 to dispatch more threads.
        else if (!smt2 && ziipVelocity.get() < SMTON)
        {
            System.out.println("TURN SMT ON");
            
//            com.ibm.jzos.MvsConsole.wto("TURN SMT ON",
//                    com.ibm.jzos.WtoConstants.ROUTCDE_MASTER_CONSOLE_INFORMATION,
//                    com.ibm.jzos.WtoConstants.DESC_SYSTEM_STATUS);
//            
//            mvsCmd("SET OPT=T2");                                                                                
    
        }
        
        // mark the interval as processed so we can add service classes if necessary, 
        // without repeating the actions
        currentInterval.processed = true;        
    }
    

    
    /**
     * Issue a MVS console command by running tsocmd console syscmd in a shell 
     * @param command the MVS command to issue
     */
    
    
    // Uncomment the MVS command as required.
    // Requires JZOS to build and run - uncomment JZOS dependency in pom.xml for Maven build
//    private static void mvsCmd(String command) 
//    {
//        String consoleid = com.ibm.jzos.ZUtil.getCurrentJobname();
//        
//        try {
//            // build and start the process to run the command
//            Process process = new ProcessBuilder()
//                    .command("/bin/sh", "-c", "tsocmd \"console syscmd(" + command + ") name(" + consoleid + ")\"")
//                    .directory(new File(System.getProperty("user.home")))                                    
//                    .inheritIO()                                                                             
//                    .start();
//            // allow 10 seconds then time out
//            // potential errors:
//            // - time out
//            // - abnormal exit value
//            // - interrupted
//            // - IOException from process start
//            // Throwing or re-throwing makes the error easier to debug. 
//            if (process.waitFor(10, TimeUnit.SECONDS))
//            {
//                if (process.exitValue() != 0)
//                {
//                    throw new RuntimeException("MVSCMD ended with exitValue " + process.exitValue());
//                }      
//            }
//            else
//            {
//                throw new RuntimeException("MVSCMD timed out");
//            }
//        } 
//        catch (IOException | InterruptedException e) 
//        {
//            throw new RuntimeException(e);
//        }
//    }
    
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
    
    
    /**
     * A private class to collect interval SMF records and calculations 
     *
     */
    private static class Interval
    {
        ZonedDateTime intervalToken;   //SMF7xIET
        double intervalLength;         //SMF7xINT
        double originalIntervalLength; //SMF7xOIL
        boolean processed = false;
        
        private List<Smf70Record> smf70Records = new ArrayList<>();
        private List<ServiceReportClassPeriodDataSection> serviceClasses = new ArrayList<>();
        private int expectedSmf70Records = 0; // updated after first SMF 70 for the interval
        private int expectedServiceClasses = 0; // updated after first interval
        
        /**
         * Create a new Interval, with expectedServiceClasses set to the number
         * seen in the previous interval, if available
         * @param r70 a SMF type 70 record
         * @param current the current interval if available, or null
         */
        Interval(Smf70Record r70, Interval current)
        {
            intervalToken = r70.productSection().smf70iet()
                    .withZoneSameInstant(r70.productSection().smf70lgo());
            intervalLength = r70.productSection().smf70intSeconds();
            originalIntervalLength = r70.productSection().smf70oilSeconds();
            if (current != null && current.serviceClasses.size() > 0)
            {
                expectedServiceClasses = current.serviceClasses.size();
            }
        }
        
        /**
         * Create a new Interval, with expectedServiceClasses set to the number
         * seen in the previous interval, if available
         * @param r72 a SMF type 72 record
         * @param current the current interval if available, or null
         */
        Interval(Smf72Record r72, Interval current)
        {
            intervalToken = r72.productSection().smf72iet()
                    .withZoneSameInstant(r72.productSection().smf72lgo());
            intervalLength = r72.productSection().smf72intSeconds();
            originalIntervalLength = r72.productSection().smf72oilSeconds();
            if (current != null && current.serviceClasses.size() > 0)
            {
                expectedServiceClasses = current.serviceClasses.size();
            }
        }
        
        /**
         * Add a SMF 70 record to the interval. The number of type 70
         * records expected is set based on the re-assembly area information
         * in the first record, or set to 1 if there is no re-assembly area.
         * @param r70 a SMF type 70 record
         */
        void add(Smf70Record r70)
        {
            if (expectedSmf70Records == 0)
            {
                expectedSmf70Records = r70.productSection().smf70ran() == 0 ? 
                        1 : 
                        r70.productSection().reassemblyArea().smf70rbr();
            }
            smf70Records.add(r70);                                
        }
        
        /**
         * Add service class information to the interval.
         * @param r72 a SMF type 72 record
         */
        void add(Smf72Record r72)
        {
            if (!r72.workloadManagerControlSection().r723mrcl()) // not a report class
            {
                serviceClasses.addAll(r72.serviceReportClassPeriodDataSections());
            } 
        }
        
        /**
         * Is the interval complete, ie do we know the expected numbers of SMF 70 
         * records and service classes, and have we collected that many?
         * @return true if the interval is complete
         */
        boolean isComplete()
        {
            return expectedServiceClasses != 0 
                    && expectedSmf70Records != 0
                    && serviceClasses.size() >= expectedServiceClasses 
                    && smf70Records.size() >= expectedSmf70Records;
        }
        
        /**
         * Is the system running SMT2 mode? True if we find a 
         * logical core data section with smf70CpuNum > 1.
         * @return true if SMT2 mode
         */
        boolean isSmt2() 
        {
            boolean smt2 = smf70Records.stream()
                    .map(r70 -> r70.logicalCoreDataSections())
                    .flatMap(List::stream)
                    .filter(core -> core.smf70CpuNum() > 1)
                    .findFirst()
                    .isPresent() == true;
            return smt2;
        }
        
        /**
         * Calculate a velocity for zIIP CPU usage across all service classes. 
         * @return an Optional velocity between 0 and 1, or Optional.empty() 
         * if all values are zero.
         */
        Optional<Double> ziipVelocity() 
        {
            double ziipUsing = 0;
            double ziipDelay = 0;
            for (ServiceReportClassPeriodDataSection serviceClass : serviceClasses)
            {
                ziipUsing += serviceClass.r723supu();
                ziipDelay += serviceClass.r723supd();          
            }

            if (ziipUsing == 0 && ziipDelay == 0) return Optional.empty();       
            return Optional.of(ziipUsing / (ziipUsing + ziipDelay) * 100);
        }
    }
}
