//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*                                                           Col 72 ->|
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a single file Java program under Java 11 using BPXBATCH
//*
// EXPORT SYMLIST=*
//*
//* The Java file to run.
//* As distributed, DIR is relative to user's home directory.
//* The split of the path between the DIR and PATH variables is not
//* important, the path is separated and passed to the shell
//* as 2 variables to avoid JCL line length limitations.
//*
// SET DIR='./git/easysmf-samples/sample-reports/src/main/java'
// SET FILE='com/smfreports/RecordCount.java'
//*
//* EasySMF directory
// SET EZSMFDIR='./easysmf-je-2.0.3'
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J11.0_64'
//*
//* SMF data to process
// SET SMFDATA=SMF.RECORDS
//*
//* Location of EasySMF Key e.g. FB 80 PDS
// SET KEYDSN=EASYSMF.PARMLIB(EZSMFKEY)
//*
//* Run a Java program under BPXBATCH
//*
//G   EXEC PGM=BPXBATCH,REGION=0M
//STDOUT    DD SYSOUT=*
//STDERR    DD SYSOUT=*
//STDPARM DD *
SH ${JAVA}/bin/java
 -cp "${EZSMFDIR}/jar/*:${EZSMFDIR}/samples/jar/lib/*"
 ${DIR}/${FILE}
 "//'${SMFDATA}'"
//* Passing DIR and FILE to the shell as environment variables
//* avoids JCL line length problems from concatenation.
//* Pass the other symbols the same way for consistency
//STDENV    DD *,SYMBOLS=JCLONLY
JAVA=&JAVA
EZSMFDIR=&EZSMFDIR
SMFDATA=&SMFDATA
DIR=&DIR
FILE=&FILE
EASYSMFKEY=//'&KEYDSN'
/*
