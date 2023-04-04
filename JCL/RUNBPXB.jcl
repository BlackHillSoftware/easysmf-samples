//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*                                                          Col 72 -> |
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a Java program using BPXBATCH
//* JCL symbols are passed as environment variables and expanded
//* by the shell to avoid line length limitations on instream data.
//* Exceeding instream data line length with JCL symbols typically
//* result in a S001 abend.
//*
// EXPORT SYMLIST=*
//*
//* Class to run with empty JAR and JARDIR, or
//* CLASS='-jar' with JARDIR and JAR values for an executable jar.
//*
// SET CLASS='com.smfreports.RecordCount'
// SET JARDIR=''
// SET JAR=''
//*
//*SET CLASS='-jar'
//*SET JARDIR='./easysmf-je-2.0.3/samples/jar/'
//*SET JAR='smf-report-dups-1.2.0.jar'
//*
//* Java target directory
//* As distributed, relative to user's home directory
//* The target directory will be searched first for
//* classes and dependencies, then target/lib, then the
//* &EZSMFDIR./jar, &EZSMFDIR./samples/jar and
//* &EZSMFDIR./samples/jar/lib directories
//* All CLASSPATH values are ignored for an executable jar.
//*
// SET TGT='./java/target'
//*
//* EasySMF directory
// SET EZSMFDIR='./easysmf-je-2.0.3'
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0'
//*
//* SMF data to process
// SET SMFDATA=SMF.RECORDS
//*
//* Location of EasySMF Key e.g. FB 80 PDS
// SET KEYDSN=EASYSMF.PARMLIB(EZSMFKEY)
//*
//* Run a Java program under BPXBATCH
//*
//G   EXEC PGM=BPXBATCH,REGION=512M
//STDPARM DD *
SH CP="${TGT}";
 CP="${CP}":"${TGT}/*";
 CP="${CP}":"${TGT}/lib/*";
 CP="${CP}":"${EZSMFDIR}/jar/*";
 CP="${CP}":"${EZSMFDIR}/samples/jar/*";
 CP="${CP}":"${EZSMFDIR}/samples/jar/lib/*";
 export CLASSPATH="${CP}";
 ${JAVA}/bin/java
 ${CLASS} ${JARDIR}${JAR}
 "//'${SMFDATA}'"
//STDOUT    DD SYSOUT=*
//STDERR    DD SYSOUT=*
//STDENV    DD *,SYMBOLS=JCLONLY
TGT=&TGT
JAVA=&JAVA
EZSMFDIR=&EZSMFDIR
SMFDATA=&SMFDATA
CLASS=&CLASS
JAR=&JAR
JARDIR=&JARDIR
EASYSMFKEY=//'&KEYDSN'
/*
