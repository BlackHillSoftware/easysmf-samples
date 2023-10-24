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
//  SET CLASS='com.smfreports.dcollect.StorageGroups'
//* SET CLASS='com.smfreports.dcollect.MbByHlq'
//* SET CLASS='com.smfreports.dcollect.DatasetsByLastRef'
//* SET CLASS='com.smfreports.dcollect.AgedDatasets'
//* SET CLASS='com.smfreports.dcollect.DatasetsByMigratedDate'
//* SET CLASS='com.smfreports.dcollect.MigrationFrequency'
//* SET CLASS='com.smfreports.dcollect.ZedcByHlq'
//*
//* DCOLLECT comparison reports
//*
//* SET CLASS='com.smfreports.dcollect.DeltaStorageGroups'
//* SET CLASS='com.smfreports.dcollect.DeltaHlq'
//* SET CLASS='com.smfreports.dcollect.DeltaDatasets'
//*
//* EasySMF directory:
// SET EZSMFDIR='./easysmf-je-2.2.0'
//*
//* Location of JZOS batch launcher module JVMLDM80:
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0'
//*
//* DCOLLECT data to process
// SET DCDATA=DCOLLECT.DATA
//*
//* Uncomment second DCOLLECT data and DD below for comparison reports
//* SET DCDATA2=DCOLLECT.DATA2
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM80,REGION=0M,
// PARM='/ &CLASS'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//MAINARGS DD *,DLM=$$,SYMBOLS=JCLONLY
 //DD:INPUT //DD:INPUT2
$$
//INPUT    DD DISP=SHR,DSN=&DCDATA
//*INPUT2   DD DISP=SHR,DSN=&DCDATA2
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
