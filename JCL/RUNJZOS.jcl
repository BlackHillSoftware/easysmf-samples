//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a Java program using JZOS Batch Launcher
//*
// EXPORT SYMLIST=*
//*
//* Class to run
// SET CLASS='com.blackhillsoftware.samples.RecordCount'
//*
//* Java target directory
//* As distributed, relative to user's home directory
//* The target directory will be searched first for 
//* a locally compiled copy of the class, if the class is
//* not found there the EasySMF Samples jar file will be 
//* searched.
//*
// SET TGT='./java/target'
//*
//* EasySMF directory and jar file:
// SET EZSMFDIR='./java/easysmf-je-1-9-3'
// SET EZSMFJAR='easysmf-je-1.9.3.jar'
// SET EZSMFSAM='easysmf-je-samples-1.9.1.jar'
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
//   PARM='/ &CLASS //DD:INPUT'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
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
//* This sample key expires 2019-10-05
//*
//EZSMFKEY DD *
**License:
MQ0KMjAxOS0xMC0wNQ0KVGVtcG9yYXJ5IEtleQ0K
**Sig:
FS6Z60HpHaO3I4GooiBHljVNCdRZVMU5exyuNe/UA8z/AZuhhedHOxGjssK2FYDu
mwAYn+nVWcujg63fVLlbA5pRYMHrFOfImd8soqipSAKliMGqXbYAqYHC3kg+QgJD
oLr+whv9NDDky3tHzNGl1etaHAc/vuY47iRnyJ9Lf2Y=
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

APP_HOME=&TGT
CP="${APP_HOME}"
CP="${CP}":"&EZSMFDIR./jar/&EZSMFJAR."
CP="${CP}":"&EZSMFDIR./samples/&EZSMFSAM."
CP="${CP}":"&EZSMFDIR./jar/slf4j-api-1.7.21.jar"
CP="${CP}":"&EZSMFDIR./jar/slf4j-simple-1.7.21.jar"
export CLASSPATH="${CP}"

IJO="-Xms16m -Xmx128m"
export IBM_JAVA_OPTIONS="$IJO "
