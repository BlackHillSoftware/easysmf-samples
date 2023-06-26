# rti-notifications

**rti-notifications**  sends SMS text message notifications for 
failed jobs using Twilio.

The program reads SMF 30 subtype 4 (step end) and 5 (job end) records from an in 
memory resource.

The job end records don't necessarily contain step failure information, so 
information about failed steps is saved waiting for the matching job end record. 
If the last step that executed (i.e. was not flushed) failed, a notification SMS 
message is sent using the Twilio API when the job end record is received.

The sample is based around the Twilio Java Quickstart at: [https://www.twilio.com/docs/sms/quickstart/java](https://www.twilio.com/docs/sms/quickstart/java)

## Build

Build the rti-notifications project using Maven:

```
mvn -f pom.xml clean package
```

The easysmf-rti-notifications jar file will be created in the ```./target``` directory. The project dependencies will also be copied to ```./target```.

## Twilio Account

A Twilio account is required to send the SMS messages. You can sign up for a Twilio trial at
[https://www.twilio.com/try-twilio](https://www.twilio.com/try-twilio) which will allow you to
send SMS messages to a limited number of recipients.

## z/OS prerequisites

In order to run the samples on z/OS, some preparation is required:

- SMF needs to be running in Logstream mode
- The in memory resource(s) need to be defined to SMF
- The user running the program needs RACF access to read from the in memory resource
- The sample JCL uses JZOS, so the JZOS Batch Launcher needs to be set up.

## Run

Copy all the jar files from the ```./target``` directory to a directory on z/OS.

The sample keeps the Twilio authorization parameters in a separate file so they can be secured and do not appear in the JCL or output. They are set as environment variables before invoking the program.

The JCL specifies a filename of ```twilio_auth``` in the user's home directory,
this can obviously be changed as required. Contents should be of the form:

```
export TWILIO_ACCOUNT_SID=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
export TWILIO_AUTH_TOKEN=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
export TO_PHONE=+12345678901
export FROM_PHONE=+12345678901
```
Run the sample using the [NOTIFY.jcl](../JCL/NOTIFY.jcl) JCL from the easysmf-rti/JCL directory.