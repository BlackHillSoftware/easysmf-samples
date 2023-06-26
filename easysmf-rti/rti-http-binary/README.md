# rti-http-binary

**rti-http-binary** sends binary SMF records to a http(s) URL.

Multiple records can be combined into a single POST. The data is sent when there
are no records immediately available in the in memory resource, or when the POST
size exceeds a specified threshold.

You can test this sample using the rti-http-servlet project to receive the records.

This project requires Java 11 due to the use of the Java 11 HttpClient class.

## Build

Build the rti-http-binary project using Maven:

```
mvn -f pom.xml clean package
```

The easysmf-rti-http-binary jar file will be created in the ```./target``` directory. The project dependencies will also be copied to ```./target```.

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

Run the sample using the [HTTPBIN.jcl](../JCL/HTTPBIN.jcl) JCL from the easysmf-rti/JCL directory.

