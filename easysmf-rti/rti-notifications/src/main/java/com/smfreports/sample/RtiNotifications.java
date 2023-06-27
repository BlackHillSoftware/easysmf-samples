package com.smfreports.sample;

import java.io.*;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.realtime.*;
import com.blackhillsoftware.smf.smf30.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

/**
 * This sample class demonstrates sending SMS notifications for
 * failed jobs from the mainframe using the EasySMF API for
 * the SMF Real Time Interface and the Twilio Java API.
 * <p>
 * The sample monitors jobs of type STC and JES2 (not OMVS or
 * TSO). Notifications are generated for JES2 jobs in a list of 
 * production job classes. This could be changed to e.g. production
 * userids, specific jobnames etc.
 * <p>
 * Jobs are considered failed if the last step to execute abended
 * (except for S222 i.e. cancelled) or the condition code was greater
 * than 8. If the job continues and subsequent steps run it is 
 * not considered a failed job.
 * <p>
 * The completion information is not necessarily available in the 
 * subtype 5 job end record. So we need to track failed steps, and 
 * check for failed steps when the subtype 5 record is encountered.  
 *
 */
public class RtiNotifications
{
    // Twilio parameters supplied via environment variables
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String TO_PHONE = System.getenv("TO_PHONE");
    private static final String FROM_PHONE = System.getenv("FROM_PHONE");

    // Job classes that require notifications
    private static Set<String> productionClasses = new HashSet<>(
            Arrays.asList(
                    "A",
                    "P"
                    ));

    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out.println("Usage: RtiNotifications <resource-name>");
            return;
        }
        
        String inMemoryResource = args[0];
        
        // Open SMF RTI connection and SmfRecordReader in 
        // try-with-resources block so they will be automatically closed

        try (SmfConnection connection = 
                 SmfConnection.forResourceName(inMemoryResource)
                     .onMissedData(RtiNotifications::handleMissedData)
                     .disconnectOnStop()
                     .connect();

             // Set up SmfrecordReader to read type 30 subtypes 4 and 5    
             SmfRecordReader reader = 
                 SmfRecordReader.fromByteArrays(connection)
                     .include(30,4)
                     .include(30,5))
        {
            // Failures are mostly recorded in step end records (subtype 4)
            // but we want to notify at end of job (subtype 5), so we save
            // failed step information and either remove it if another step 
            // executes successfully, or notify if the last step failed.
            Map<JobKey, Smf30Record> failedSteps = new HashMap<>(); 

            // Read SMF records from RTI using SmfRecordReader
            for (SmfRecord record : reader)
            {
                Smf30Record r30 = Smf30Record.from(record);
                
                // check we have the completion section - a job/step can have multiple
                // records, completion section is only in the first
                if (r30.completionSection() != null
                        // and we are generating notifications for this job
                        && includeJob(r30))
                {
                    switch (record.subType())
                    {
                    case 4: // subtype 4 : step end
                        if (failed(r30)) 
                        {
                            // if the step failed, save the record for later -
                            // this replaces existing entry if present
                            failedSteps.put(new JobKey(r30), r30);
                        }
                        // else it didn't fail, check it wasn't flushed
                        else if (!r30.completionSection().smf30flh()) 
                        {
                            // If the step wasn't flushed, remove any earlier failed step
                            
                            // check the step and substep numbers to see if there was an earlier step, 
                            // no point in checking if there wasn't
                            if (r30.identificationSection().smf30stn() > 1 
                                    || r30.identificationSection().smf30ssn() > 0)
                            {
                                // after a successful step, remove failed step information if present
                                failedSteps.remove(new JobKey(r30));
                            }
                        }
                        break;
                        
                    case 5: // subtype 5 : job end
                        
                        // Check if the last executed step failed
                        JobKey key = new JobKey(r30);                        
                        if (failedSteps.containsKey(key))
                        {
                            // send notification using information from failed step 
                            sendNotification(failedSteps.get(key));                            
                            failedSteps.remove(key); // finished with this
                        }
                        // or if the type 5 record indicates failure
                        else if(failed(r30))
                        {
                            // send notification using information from job
                            sendNotification(r30);                            
                        }
                        break;

                    default:
                        throw new IllegalArgumentException("Unexpected subtype: " + Integer.toString(record.subType()));
                    }
                }
            }
        }
    }

    /**
     * Check whether this is a job we are generating notifications for.
     * In this case it is all STC, and jobs in production job classes but
     * the criteria can be whatever you need
     * @param r30 the SMF 30 record
     * @return true if we are monitoring this job
     */
    private static boolean includeJob(Smf30Record r30) 
    {
        return r30.header().smf30wid().equals("STC")
                || (r30.header().smf30wid().equals("JES2") 
                        && productionClasses.contains(r30.identificationSection().smf30cl8()));
    }

    /**
     * Check whether we consider this job or step as failed.
     * @param r30 the job or step record
     * @return true if this is a failure record
     */
    private static boolean failed(Smf30Record r30) 
    {
        return 
                // abended except for S222 (cancelled)
                (r30.completionSection().smf30abd() && r30.completionSection().smf30scc() != 0x222)
                // or condition code > 8
                || r30.completionSection().smf30scc() > 8
                // or post execution error
                || r30.completionSection().smf30sye();
    }

    /**
     * Send the notification via Twilio
     * @param r30 the job or step record
     */
    private static void sendNotification(Smf30Record r30) 
    {
        String messagetext = 
                String.format("%s Job failed: %s %s Step: %d %s Program: %s CC: %s",
                        r30.smfDateTime().toString(),
                        r30.identificationSection().smf30jbn(), // job name
                        r30.identificationSection().smf30jnm(), // job number
                        r30.identificationSection().smf30stn(), // step number
                        r30.identificationSection().smf30stm(), // step name
                        r30.identificationSection().smf30pgm(), // program
                        r30.completionSection().completionDescription());
        
        // Send a SMS notification through Twilio
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(TO_PHONE),
                new com.twilio.type.PhoneNumber(FROM_PHONE),
                messagetext)
                .create();
        System.out.println("Message Sent: " + message.getSid());
    }

    /**
     * Process the SMF missed data event. This method prints a message
     * and indicates that an exception should not be thrown.
     * 
     * @param e the missed data event information
     */
    private static void handleMissedData(MissedDataEvent e)
    {
        System.out.println("Missed Data!");
        e.throwException(false);    
    }

    /**
     * A class to use as a key to identify records from the same job.
     * <p>
     * A job is identified by a combination of system, job name, 
     * job number and read date/time.
     * <p>
     * The JobKey is used as the key in the HashMap to relate 
     * subtype 4 and subtype 5 records.
     * It needs correctly implemented hashCode and equals methods
     * for use as a hash key. Eclipse will generate them form the
     * fields in the class.  
     */
    private static class JobKey
    {
        String system;
        String jobname;
        String jobnumber;
        long readtime;
        int readdate;

        /**
         * Create a new key from a type 30 record
         * @param r30 the type 30 record
         */
        JobKey(Smf30Record r30)
        {
            system = r30.system();
            jobname = r30.identificationSection().smf30jbn();
            jobnumber = r30.identificationSection().smf30jnm();
            readtime = r30.identificationSection().smf30rstRawValue();
            readdate = r30.identificationSection().smf30rsdRawValue();
        }

        // hashCode and equals generated using Eclipse
        @Override
        public int hashCode() {
            return Objects.hash(jobname, jobnumber, readdate, readtime, system);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            JobKey other = (JobKey) obj;
            return Objects.equals(jobname, other.jobname) 
                    && Objects.equals(jobnumber, other.jobnumber)
                    && readdate == other.readdate 
                    && readtime == other.readtime 
                    && Objects.equals(system, other.system);
        }
    }
}
