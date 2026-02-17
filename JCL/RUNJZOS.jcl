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
// SET CLASS='com.smfreports.RecordCount'
// SET JARDIR=''
// SET JAR=''
//*
//*SET CLASS='-jar'
//*SET JARDIR='./easysmf-je-2.4.2/samples/jar/'
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
//* EasySMF directory:
// SET EZSMFDIR='./easysmf-je-2.4.2'
//*
//* Location of JZOS batch launcher module JVMLDM80:
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J17.0_64'
//*
//* SMF data to process
// SET SMFDATA=SMF.RECORDS
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM17,REGION=0M,
// PARM='/ &CLASS &JARDIR.&JAR.'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//MAINARGS DD *,DLM=$$,SYMBOLS=JCLONLY
 //DD:INPUT
$$
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
//* This sample key expires 2026-03-19
//*
//EZSMFKEY DD *
**License:
MQoyMDI2LTAzLTE5ClRlbXBvcmFyeSBLZXkKSkUsUlRJCg==
**Sig:
xRws4XX1QJEyBzw2Wbo7zMD/MwPGL+pqPzO8cvGsCymk3B4vqR2EygzwyfVKMAes
QCCuc+9Fqgo4XGq5ZQaZ2hbjGE1OL3K+/aa99YN/rRhxzCvpXLStfwajFmqcucoj
MFuvkmecN3Q75GLxwgbpS7NqV3uYs4pIubynJiJ7B04=
**End
//*
//* Configure for JZOS: based on JVMJCL17/JVMPRC17
//*
//STDENV   DD *,SYMBOLS=JCLONLY
. /etc/profile
export JAVA_HOME=&JAVA
export PATH=/bin:"${JAVA_HOME}"/bin
LIBPATH=/lib:/usr/lib
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/j9vm
export LIBPATH="$LIBPATH":

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
