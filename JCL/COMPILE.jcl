//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Compile a Java program using BPXBATCH
//*
// EXPORT SYMLIST=*
//*
//* Java source and target directories.
//* As distributed, they are relative to user's home directory
//*
// SET SRC='./java/easysmf-je-1-9-3/samples/source'
// SET TGT='./java/target'
//*
//* File to compile, relative to SRC directory
// SET CLASS='com/blackhillsoftware/samples/RecordCount.java'
//*
//* EasySMF directory:
// SET EZSMFDIR='./java/easysmf-je-1-9-3'
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0'
//*
//* Symbols from JCL are assigned to environment variables in STDENV,
//* then substituted by the shell to avoid JCL line length problems
//* that can occur with 2 long symbols on the same line.
//* Beware: Variables in STDENV can be overridden by /etc/profile or
//* .profile
//*
//COMPILE  EXEC PGM=BPXBATCH,REGION=0M
//STDPARM  DD *
SH ${JAVA}/bin/javac
 -Xlint -verbose
 -cp "${EZSMFDIR}/jar/*"
 -d ${TGT}
 ${SRC}/${CLASS}
//STDENV   DD *,SYMBOLS=JCLONLY
JAVA=&JAVA
CLASS=&CLASS
SRC=&SRC
TGT=&TGT
EZSMFDIR=&EZSMFDIR
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*