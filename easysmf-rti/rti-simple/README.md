# rti-simple

This sample demonstrates the core functions of EasySMF-RTI.

It connects to an in memory resource, reads records and prints the 
date/time, record type and length. It disconnects from the resource and exits 
after reading 100 records, or when a MVS STOP command is received.

## Build

Build the rti-simple project using Maven:

```
mvn -f pom.xml clean package
```

The easysmf-rti-simple jar file will be created in the ```./target``` directory. The project dependencies will also be copied to ```./target```.

## z/OS prerequisites

In order to run the samples on z/OS, some preparation is required:

- SMF needs to be running in Logstream mode
- The in memory resource(s) need to be defined to SMF
- The user running the program needs RACF access to read from the in memory resource
- The sample JCL uses JZOS, so the JZOS Batch Launcher needs to be set up.

## Run

Copy all the jar files from the ```./target``` directory to a directory on z/OS.

Run the sample using the [SIMPLE.jcl](../JCL/SIMPLE.jcl) JCL from the easysmf-rti/JCL directory.