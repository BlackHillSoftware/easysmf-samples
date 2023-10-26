//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*                                                           Col 72 ->|
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a single file Java program under Java 11 using BPXBATCH
//*
//G   EXEC PGM=BPXBATCH,REGION=0M
//STDOUT    DD SYSOUT=*
//STDERR    DD SYSOUT=*
//STDPARM DD *
SH /usr/lpp/java/J11.0_64/bin/java
 -cp "/home/andrewr/easysmf-je-2.2.1/samples/jar/lib/*"
 /home/andrewr/java/src/DeltaStorageGroups.java
 "//'DCOLLECT.DATA'"
 "//'DCOLLECT.DATA2'"
//STDENV    DD *,SYMBOLS=JCLONLY
EASYSMFKEY=//'VENDOR.PARMLIB(EZSMFKEY)'
/*
