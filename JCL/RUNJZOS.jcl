//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*                                                          Col 72 -> |
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a Java program using JZOS Batch Launcher
//*
// EXPORT SYMLIST=*
//*
//* Class to run with empty JAR and JARDIR, or
//* CLASS='-jar' with JARDIR and JAR values for an executable jar.
//*
// SET CLASS='com.smfreports.json.Smf30RecordToJson'
// SET JARDIR=''
// SET JAR=''
//*
//*SET CLASS='-jar'
//*SET JARDIR='./java/easysmf-je-2.0.1/samples/jar/'
//*SET JAR='smf-report-dups-1.1.0.jar'
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
//* EasySMF directory:
// SET EZSMFDIR='./java/easysmf-je-2.0.1'
//*
//* Location of JZOS batch launcher module JVMLDM80:
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0'
//*
//* SMF data to process
// SET SMFDATA=SMF.RECORDS
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM80,REGION=0M,
// PARM='/ &CLASS &JARDIR.&JAR.'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//MAINARGS DD *,SYMBOLS=JCLONLY
 //DD:INPUT
//INPUT    DD DISP=SHR,DSN=&SMFDATA
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//ABNLIGNR DD DUMMY
//*
//* EasySMF Key - get a 30 day trial from
//* https://www.blackhillsoftware.com/30-day-trial/
//* This sample key expires 2023-04-15
//*
//EZSMFKEY DD *
**License:
MQ0KMjAyMy0wNC0xNQ0KVGVtcG9yYXJ5IEtleQ0K
**Sig:
mcBCoJt2H/XErCI7kiWA647KJAUE++SJy3Q2rRCuQZzlbXoMu/hCLRoPa9UBloIz
g/ABXaBF23weg8PNSPTczlHFP6vrC8eBOx5SkGDzwR0JlTa0iDQ3tgH3gJqNnt4I
RO/BCP8rAUr4NjMA5yFSqxGFnLG8pIOs+/64jB6fXV4=
**End
//*
//* Configure for JZOS: based on JVMJCL80/JVMPRC80
//*
//STDENV   DD *,SYMBOLS=JCLONLY
. /etc/profile
export JAVA_HOME=&JAVA
export PATH=/bin:"${JAVA_HOME}"/bin

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="${LIBPATH}":"${JAVA_HOME}"/lib/s390
LIBPATH="${LIBPATH}":"${JAVA_HOME}"/lib/s390/j9vm
LIBPATH="${LIBPATH}":"${JAVA_HOME}"/bin/classic
export LIBPATH="${LIBPATH}":

CLASSPATH="${CLASSPATH}":"&TGT."
for i in "&TGT."/*.jar; do
    CLASSPATH="${CLASSPATH}":"${i}"
    done
for i in "&TGT./lib"/*.jar; do
    CLASSPATH="${CLASSPATH}":"${i}"
    done
for i in "&EZSMFDIR./jar"/*.jar; do
    CLASSPATH="${CLASSPATH}":"${i}"
    done
for i in "&EZSMFDIR./samples/jar"/*.jar; do
    CLASSPATH="${CLASSPATH}":"${i}"
    done
for i in "&EZSMFDIR./samples/jar/lib"/*.jar; do
    CLASSPATH="${CLASSPATH}":"${i}"
    done
export CLASSPATH="${CLASSPATH}":

IJO="-Xms512m -Xmx1024m"
export IBM_JAVA_OPTIONS="${IJO} "
