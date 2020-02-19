//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a Java program using BPXBATCH
//*
// EXPORT SYMLIST=*
//*
//* Class to run
//*
// SET CLASS='com.blackhillsoftware.samples.RecordCount'
//*
//* Java target directory.
//* As distributed, relative to user's home directory
//* The target directory will be searched first for 
//* a locally compiled copy of the class, if the class is
//* not found there the EasySMF Samples jar file will be 
//* searched.
//*
// SET TGT='./java/target'
//*
//* EasySMF directory
// SET EZSMFDIR='./java/easysmf-je-1-9-3'
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0'
//*
//* SMF data to process
// SET SMFDATA=SMF.RECORDS
//*
//* Location of EasySMF Key e.g. FB 80 PDS
// SET KEYDSN=EASYSMF.INSTALL(EZSMFK)
//*
//* Run a Java program under BPXBATCH
//*
//G   EXEC PGM=BPXBATCH,REGION=512M
//STDPARM DD *,SYMBOLS=JCLONLY
SH CP="&TGT.";
 CP="$CP":"&EZSMFDIR./jar/*";
 CP="$CP":"&EZSMFDIR./samples/*";
 export CLASSPATH="$CP";
 &JAVA./bin/java
 &CLASS
 "//'&SMFDATA'"
//STDOUT    DD SYSOUT=*
//STDERR    DD SYSOUT=*
//STDENV    DD *,SYMBOLS=JCLONLY
EASYSMFKEY=//'&KEYDSN'
/*