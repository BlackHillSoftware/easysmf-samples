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
//* Class to run:
//*
// SET CLASS='com.smfreports.sample.RtiSimple'
//*
//* Java target directory.
//* As distributed, relative to user's home directory.
//* The target directory will be searched first for
//* classes and dependencies, then target/lib.
//*
// SET TGT='./java/rti-simple'
//*
//* Location of JZOS batch launcher module JVMLDM86:
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0_64'
//*
//* SMF data to process
// SET SMFINMEM=IFASMF.MYRECS
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM86,REGION=0M,
// PARM='/ &CLASS'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//MAINARGS DD *,DLM=$$,SYMBOLS=JCLONLY
 &SMFINMEM
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
//* This sample key expires 2023-07-26
//*
//EZSMFKEY DD *
**License:
MQ0KMjAyMy0wNy0yNg0KVGVtcG9yYXJ5IEtleQ0KSkUsUlRJDQo=
**Sig:
mrTbKshR+WfytUSES6uCWijvmH00oThTqY+VHag0x92z3q5/s1xMYv0hP+p1d/fl
bfs7xAdjI0XOpUr8KpdYrotpm8H7BoW5zTj06hOIPASOu+v0UMVLEBO7binrjjZQ
IhY5Btu79q7ol6+saC6DKVkweUVzX4Vb7GLMhJkIH+U=
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
export CLASSPATH="${CLASSPATH}":

IJO="-Xms512m -Xmx1024m"
export IBM_JAVA_OPTIONS="${IJO} "
