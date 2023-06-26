# rti-http-json

**rti-http-json** sends job end records (SMF 30 subtype 5) in JSON format to a http(s) 
URL.

The program reads SMF 30 subtype 5 records from an in memory resource, formats 
the major sections into a JSON record and POSTs the JSON text to a http(s) URL.

You can test this sample using the rti-http-servlet project to receive the records.

This project requires Java 11 due to the use of the Java 11 HttpClient class.

## Build

Build the rti-http-json project using Maven:

```
mvn -f pom.xml clean package
```

The easysmf-rti-http-json jar file will be created in the ```./target``` directory. The project dependencies will also be copied to ```./target```.

## z/OS prerequisites

In order to run the samples on z/OS, some preparation is required:

- Java 11 is required
- SMF needs to be running in Logstream mode
- The in memory resource(s) need to be defined to SMF
- The user running the program needs RACF access to read from the in memory resource
- The sample JCL uses JZOS, so the JZOS Batch Launcher needs to be set up.

## Run

Copy all the jar files from the ```./target``` directory to a directory on z/OS.

Start the [rti-http-servlet](../rti-http-servlet/) project on a system reachable via TCP/IP from z/OS.

Run the sample using the [HTTPJSON.jcl](../JCL/HTTPJSON.jcl) JCL from the easysmf-rti/JCL directory.