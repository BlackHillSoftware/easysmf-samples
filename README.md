# easysmf-samples

This repository contains sample code for EasySMF:JE.

EasySMF:JE is a commercial product developed by [Black Hill Software](https://www.blackhillsoftware.com) which provides a Java API to map z/OS SMF records. To run these samples, you will require the EasySMF:JE jar file and a EasySMF:JE license key.
## 30 day Trial

A free, 30 day trial is available. You can get a trial key here: [EasySMF 30 Day Trial](https://www.blackhillsoftware.com/30-day-trial/)

## Getting Started

There is a [Tutorial](./tutorial) to help you get started and understand how EasySMF processes SMF records. It demonstrates the basic principles behind reading SMF records, extracting the data sections and fields.

The Tutorial can be found here: [EasySMF Tutorial](./tutorial)

## Building the Samples

The samples are set up to build using [Apache Maven](https://maven.apache.org/). To build the samples:

1. Install Apache Maven
1. Clone this repository using Git or download as a zip file
1. Build the samples using the pom for the project you want to build:
   ```mvn -f sample-reports/pom.xml clean package```      

The first time you run Maven it will download many packages used by Maven to build the project, plus any dependencies for the project itself. These are cached on your machine so they don't need to be downloaded each time.

The output will be a jar file in the "target" subdirectory e.g. ```sample-reports/target```. Additional jar files for the project dependencies will be copied to the target/lib directory.

## Running the samples

### Windows

1. Set the environment variable for the EasySMF key file e.g.

    ```set EASYSMFKEY=C:\Users\Andrew\Documents\easysmfkey.txt```

1. Run the program

    ```java -cp sample-reports/target/*;sample-reports/target/lib/* com.smfreports.RecordCount smfdata.smf```
    
    where
    - ```-cp sample-reports/target/*;sample-reports/target/lib/*``` : sets the CLASSPATH to the output directories containing the output jar file and dependencies
    - ```com.smfreports.RecordCount``` : is the full name including the package of the class to run
    - ```smfdata.smf``` : is the file containing SMF data

### Linux

1. Set the environment variable for the EasySMF key file e.g.

    ```export EASYSMFKEY=C:\Users\Andrew\Documents\easysmfkey.txt```

1. Run the program

    ```java -cp 'sample-reports/target/*:sample-reports/target/lib/*' com.smfreports.RecordCount smfdata.smf```
    
    where
    - ```-cp 'sample-reports/target/*:sample-reports/target/lib/*'``` : sets the CLASSPATH to the output directories containing the output jar file and dependencies
    - ```com.smfreports.RecordCount``` : is the full name including the package of the class to run
    - ```smfdata.smf``` : is the file containing SMF data

### Runnable Jars

Some of the projects specify a **mainClass** in the pom.xml file. This creates a runnable jar where you can specify the jar name rather than the class you want to run. The runnable jar sets its own classpath relative to the main jar file.

To run these programs, specify the ```-jar``` java option:

```java -jar smf-de-dup/target/smf-de-dup-1.0.1.jar smfdata.smf ```

### On z/OS under OMVS

Transfer/copy the jar files in binary mode to a unix directory on z/OS. Then run the programs using the same procedures as Linux. A dataset name can be specified using the syntax:

```java -cp  'sample-reports/target/*:sample-reports/target/lib/*' com.smfreports.RecordCount //"'MVS1.SMF.RECORDS'"```

### On z/OS in batch

#### Using JZOS Batch Launcher

The JZOS Batch Launcher is probably the easiest way to run Java on z/OS, because it runs as a normal batch job and DD statements can be used in the JCL to define the input and output.

1. Install the JZOS batch launcher according to the IBM installation instructions. Installation consists of copying the JZOS load modules from the Java filesystem to a PDS/E, and copying sample JCL and PROCs.

2. JCL to run under JZOS is provided here:      
   [JCL/RUNJZOS.jcl](./JCL/RUNJZOS.jcl)

#### Under BPXBATCH

JCL to run the samples using BPXBATCH is available here:

[JCL/RUNBPXB.jcl](./JCL/RUNBPXB.jcl)

### Java 11 Single File Source Code Program

Many of the sample programs are in a single Java file, which makes them candidates for the single file source code feature in Java 11.

Programs can be executed by specifying the source file name to Java.

#### Batch

JCL to run single file source code programs under BPXBATCH is available here:

[JCL/J11BPXB.jcl](./JCL/J11BPXB.jcl)

#### OMVS

To run a single file source code program under OMVS:
```
java -cp 'sample-reports/target/lib/*' sample-reports/src/main/java/com/smfreports/RecordCount.java smfdata.smf
```

## Compiling the samples

The easiest way to compile the samples is to clone or download the samples from Github, and use [Apache Maven](https://maven.apache.org/) to run the build.

After installing Maven, change to the directory containing the pom.xml file and enter
```
mvn clean package
```
The first time you run Maven it downloads many plugins and components used for the build. These are cached on your machine so they are not downloaded every time.

The resulting jar file will be in the ```target``` directory, and dependencies in the ```target/lib``` directory.

The jar files can be uploaded to z/OS as binary files using your favorite file transfer program.

### Compiling on z/OS

If you want to compile samples on z/OS, JCL is provided here:

[JCL/COMPILE.jcl](./JCL/COMPILE.jcl)

## Sample Reports

There are a number of sample reports to show how Java can be used to process SMF data.

1. [Counting SMF Records](#counting-smf-records)
1. [Analyzing Duplicate SMF records](#analyzing-duplicate-smf-records)
1. [Summarizing Data by Jobname](#summarizing-data-by-jobname)
1. [Highest Contributors to R4HA Peak](#highest-contributors-to-r4ha-peak)
1. [User Key Common](#user-key-common)
1. [A/B (Before/After) Comparison](#ab-beforeafter-comparison)
1. [Dataset Activity](#dataset-activity)

### Counting SMF Records

Source: [RecordCount.java](./reports/src/main/java/com/smfreports/RecordCount.java)

SMF record counts by type and subtype.

### Analyzing Duplicate SMF records

These reports were prompted by a question about whether it was possible to remove duplicate SMF records when data had been duplicated due to a processing error.

That led to another question: Are legitimate duplicate SMF records likely to occur?

The short answer is: Yes, duplicate SMF records occur surprisingly often. The timestamps in SMF records are not granular enough to prevent duplicate records from being written when the same event occurs multiple times. Duplicate records seemed particularly common for
- Type 30 subtype 1 (Job Start). These were probably generated by unix processes where address spaces are reused multiple times and the job id does not change.
- RACF records

**SmfDeDup**

Source: [SmfDeDup.java](./SmfDeDup/src/main/java/com/smfreports/SmfDeDup.java)

SmfDeDup reports whether a SMF dataset contains duplicate records, with duplicate counts by record type. 

Optionally, it can write a new dataset/file with the duplicates removed (even though they might be legitimate data), and write the duplicates to another file for further analysis.

**SmfReportDups**

Source: [SmfReportDups.java](./SmfReportDups/src/main/java/com/smfreports/SmfReportDups.java)

SmfReportDups attempts to better answer the question of whether data has been duplicated due to a processing error.

Records are grouped by SMF ID, record type and minute and a count is kept of unique and duplicate records.

Duplicate data is flagged for any minute where the number of duplicates is greater than or equal to the number of unique records.

Duplicates are checked:
- for each SMF ID to find instances where all data from a system is duplicated
- by SMF ID and record type to find instances where particular record types are duplicated e.g. if a record type is copied into a separate dataset which is copied again back into the main stream.

### Summarizing Data by Jobname

Source: [JobsByJobname.java](./reports/src/main/java/com/smfreports/type30/JobsByJobname.java)

Summary of CP time, zIIP time, Connect time and EXCP count by job name.

### Highest Contributors to R4HA Peak

Source: [PeakR4HAJobs.java](./reports/src/main/java/com/smfreports/r4ha/PeakR4HAJobs.java)

Show the jobs and address spaces that used the most CPU during the R4HA peak periods.

The program reads type 30 and type 70 SMF records. CPU information from the type 30 records is collected by jobname by hour, and the type 70 records are used to find the R4HA peaks.

For each of the top 5 peaks on each system, sum the CP time used by each job in the 4 hours leading to that peak and report the top 5 jobs in the list. An estimated MSU value is also calculated based on the each jobs percentage of the total CPU time multiplied by the actual MSU.

Do the same thing for zIIP on CP time.

### User Key Common

Source: [UserKeyCommon.java](./reports/src/main/java/com/smfreports/type30/UserKeyCommon.java)

Search type 30 records for jobs with User Key Common flags set.

Common storage in a user key is not supported after z/OS 2.3.
APAR OA53355 introduced flags in the type 30 SMF record that are set
if a task uses user key common storage.

### A/B (Before/After) Comparison

Source: [BeforeAfterProgramStatistics.java](./reports/src/main/java/com/smfreports/type30/BeforeAfterProgramStatistics.java)

Produce a report by program name showing changes in zIIP%, zIIP on CP% and CPU milliseconds per I/O before and after a specified date. This type of report may help to evaluate the inpact of e.g. hardware or configuration changes.

### Dataset Activity

Source: [DatasetActivity.java](./reports/src/main/java/com/smfreports/dataset/DatasetActivity.java)

List activity against datasets (read, write, update etc.) Additional documentation is available here: 
[Dataset Reports](./reports/src/main/java/com/smfreports/dataset)
