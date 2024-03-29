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
//* Run as an executable jar. Requires dependencies in ./lib
//* subdirectory relative to jar.
//*
// SET CLASS='-jar'
// SET JARDIR='./easysmf-je-2.2.1/samples/jar/'
// SET JAR='dcollect2json-1.0.0.jar'
//*
//* Location of JZOS batch launcher module JVMLDM80:
// SET JZOSLIB=JZOS.LINKLIBE
//*
//* Location of Java:
// SET JAVA='/usr/lpp/java/J8.0'
//*
//* DCOLLECT data to process
// SET DCOLDATA=DCOLLECT.DATA
//*
//* Run a Java program under JZOS Batch Launcher
//*
//G        EXEC PGM=JVMLDM80,REGION=0M,
// PARM='/ &CLASS &JARDIR.&JAR.'
//*
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//*
//INPUT    DD DISP=SHR,DSN=&DCOLDATA
//*
//* JSON output files. Uncomment based on the record types
//* required.
//* JSON is a good candidate for zEDC compression if
//* available.
//*
//OUTBC    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTBC,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTAI    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTAI,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTD     DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTD,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTA     DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTA,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTV     DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTV,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTM     DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTM,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTB     DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTB,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTVL    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTVL,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTC     DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTC,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//OUTT     DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTT,
//*     DATACLAS=ZEDC,
//      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTDC    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTDC,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTSC    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTSC,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTMC    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTMC,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTSG    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTSG,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTAG    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTAG,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTDR    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTDR,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTLB    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTLB,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*OUTCN    DD DISP=(NEW,CATLG),DSN=DCOLLECT.OUTCN,
//*      DATACLAS=ZEDC,
//*      SPACE=(1,(10,10)),AVGREC=M,LRECL=27998,RECFM=VB
//*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//ABNLIGNR DD DUMMY
//*
//* EasySMF Key - get a 30 day trial from
//* https://www.blackhillsoftware.com/30-day-trial/
//*
//EZSMFKEY DD *
**License:
MQoyMDIzLTExLTE4ClRlbXBvcmFyeSBLZXkKSkUsUlRJCg==
**Sig:
u7/jqmCGt1gtx4nXCH4wmincN0fkWeXJPmip2rbDTHg7q37TulJNG7owYb65PTqN
OgQbVm9cD59omgHqDj6NSqxyRozxCVnPQlHzJ3FZg0k/Dbbsv5b74n532MAiO+Ua
gzLVocxO8Zn/QEzrNGjHWRVnlO1doSd/ut/zncWjzXU=
**End
//*
//* Configure for JZOS: based on JVMJCL80/JVMPRC80
//* CLASSPATH is ignored for executable jar but required for JZOS
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

CLASSPATH="${CLASSPATH}":"&JARDIR."
for i in "&JARDIR."/*.jar; do
    CLASSPATH="${CLASSPATH}":"${i}"
    done
for i in "&JARDIR./lib"/*.jar; do
    CLASSPATH="${CLASSPATH}":"${i}"
    done
export CLASSPATH="${CLASSPATH}":

IJO="-Xms512m -Xmx1024m"
export IBM_JAVA_OPTIONS="${IJO} "
