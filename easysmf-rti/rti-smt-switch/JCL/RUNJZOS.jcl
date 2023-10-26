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
// SET CLASS='com.smfreports.sample.SmtSwitch'
// SET JARDIR=''
// SET JAR=''
//*
//*SET CLASS='-jar'
//*SET JARDIR='./easysmf-je-2.2.1/samples/jar/'
//*SET JAR='easysmf-rti-smt-switch-1.0.0.jar'
//*
//* EasySMF directory:
// SET EZSMFDIR='./easysmf-je-2.2.1'
//*
//* Location of JZOS batch launcher module JVMLDM80:
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0_64'
//*
//* SMF data to process or
// SET SMFDATA=IFASMF.ALLRECS
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM86,REGION=0M,
// PARM='/ &CLASS &JARDIR.&JAR.'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//MAINARGS DD *,DLM=$$,SYMBOLS=JCLONLY
 &SMFDATA
$$
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//ABNLIGNR DD DUMMY
//*
//* EasySMF Key - get a 30 day trial from
//* https://www.blackhillsoftware.com/30-day-trial/
//* This sample key expires 2023-10-14
//*
//EZSMFKEY DD *
**License:
MQoyMDIzLTEwLTE0ClRlbXBvcmFyeSBLZXkKSkUsUlRJCg==
**Sig:
n6gx3VtKr70v1Lt0bTvjPYjvSg0XwgU7EZyBw4Y807kjuDKB8+D819W2sdbQuqdI
ZDr/MrfnLisMJlp5VoZdf7LKhtfUEMz0kHBeJJZxKDo/LcsfRtcrLp5h0iEvzcmg
8ksTOC2bubn5Xvg7lhwXJB0q47ulWXLqErZY0NFyIrc=
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
