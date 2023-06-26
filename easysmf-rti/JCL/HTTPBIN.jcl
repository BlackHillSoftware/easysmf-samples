//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*                                                          Col 72 -> |
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a Java program using JZOS Batch Launcher
//* Must run under Java 11
//*
// EXPORT SYMLIST=*
//*
//* Class to run:
//*
// SET CLASS='com.smfreports.sample.RtiHttpBinary'
//*
//* Java target directory.
//* As distributed, relative to user's home directory.
//* The target directory will be searched first for
//* classes and dependencies, then target/lib.
//*
// SET TGT='./java/rti-http-binary'
//*
//* Location of JZOS batch launcher module JVMLDM16:
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J11.0_64'
//*
//* SMF data to process
// SET SMFINMEM=IFASMF.MYRECS
// SET URL='http://192.168.12.34:9999/easysmf'
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM16,REGION=0M,
// PARM='/ &CLASS'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//MAINARGS DD *,DLM=$$,SYMBOLS=JCLONLY
 &SMFINMEM &URL
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
//* Configure for JZOS: based on JVMJCL16/JVMPRC16
//*
//STDENV   DD *,SYMBOLS=JCLONLY
. /etc/profile
export JAVA_HOME=&JAVA
export PATH=/bin:"${JAVA_HOME}"/bin

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="${LIBPATH}":"${JAVA_HOME}"/lib
LIBPATH="${LIBPATH}":"${JAVA_HOME}"/lib/j9vm
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
