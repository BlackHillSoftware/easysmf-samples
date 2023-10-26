# Dataset Reports

These reports take information from various SMF records to show activity against datasets.

### SmfSearch

A very simple program to demonstrate searching SMF data.

This program searches SMF type 15 (Output Dataset Activity) records for a dataset name. The dataset name and SMF input source are passed as arguments to the program.

### DatasetActivity

A more complex report of dataset activity. The report shows activity from SMF record types:
- 14 - Read
- 15 - Update
- 17 - Scratch
- 18 - Rename
- 61 - ICF Define
- 62 - VSAM Open
- 64 - VSAM Status
- 65 - ICF Delete

#### Arguments

 - -r : Optional, indicates that read activity should be included. Otherwise only datasets opened for write access are included.
 - dataset-pattern : The datasets to be reported.
 - input-name : The source of the input data. Can be **filename**, **//DD:DDNAME** or **//'DATASET.NAME'** 

Wildcards can be used in **dataset-pattern**:
- % - represents a single character in a qualifier
- \* - Zero or more characters in a single qualifier
- \*\* - Zero or more characters in one or more qualifiers 

e.g Read or write activity against SYS1.PARMLIB:

```
java com.smfreports.dataset.DatasetActivity -r SYS1.PARMLIB //'SMF.DUMP.DAILY'
```

or for update activity for datasets starting with "SYS" and ending with ".*LIB":

```
java com.smfreports.dataset.DatasetActivity SYS**.*LIB //'SMF.DUMP.DAILY'
```

#### JCL

The following JCL can be used to run the Dataset Activity report.

```
//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
// EXPORT SYMLIST=*
//*
//* Class to run and command line arguments
// SET CLASS='com.smfreports.dataset.DatasetActivity'
// SET ARGS='-r SYS*.**LIB'
//*
//* SMF dataset:
// SET SMFDATA=SMF.RECORDS
//*
//* EasySMF directory and jar file:
// SET EZSMFDIR='./java/easysmf-je-2.2.1'
// SET EZSMFJAR='easysmf-je-2.2.1.jar'
//*
//* Sample directory and jar file:
// SET SAMPLDIR='./java'
// SET SAMPLJAR='easysmf-je-samples-2.2.1.jar'
//*
//* Location of JZOS batch launcher module JVMLDM80: 
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0'
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM80,REGION=0M,
// PARM='/ &CLASS &ARGS //DD:INPUT'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//INPUT    DD DISP=SHR,DSN=&SMFDATA
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//STDOUT   DD SYSOUT=*,LRECL=259
//STDERR   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//ABNLIGNR DD DUMMY
//*
//* EasySMF Key - get a 30 day trial from
//* https://www.blackhillsoftware.com/30-day-trial/
//* This sample key expires 2020-04-17
//*
//EZSMFKEY DD *
**License:
MQ0KMjAyMC0wNC0xNw0KVGVtcG9yYXJ5IEtleQ0K
**Sig:
GVsMgXw9uP2/lTawJanRw+z/4dEDPV3vxCvYsG6wy8SSEIb7cdcmFpRXLQtvsHKd
MA47p8Nn8BUnEwScBG2pp6lKPQK5GFOik/gsHREJvT5gVvpA2nabrqFpFdCcywJY
Inp8rU9qI3W6fnrDf8RP1S1MBiCcjmX8gDbYDO/Gm/k=
**End
//*
//* Configure for JZOS: based on JVMJCL80/JVMPRC80
//*
//STDENV   DD *,SYMBOLS=JCLONLY
. /etc/profile
export JAVA_HOME=&JAVA
export PATH=/bin:"${JAVA_HOME}"/bin

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390/j9vm
LIBPATH="$LIBPATH":"${JAVA_HOME}"/bin/classic
export LIBPATH="$LIBPATH":

CP="&EZSMFDIR./jar/&EZSMFJAR."
CP="${CP}":"&SAMPLDIR./&SAMPLJAR."
CP="${CP}":"&EZSMFDIR./jar/slf4j-api-2.0.9.jar"
CP="${CP}":"&EZSMFDIR./jar/slf4j-simple-2.0.9.jar"
export CLASSPATH="${CP}"

IJO="-Xms128m -Xmx512m"
export IBM_JAVA_OPTIONS="$IJO "

```