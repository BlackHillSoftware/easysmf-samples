//JOBNAME  JOB CLASS=A,
//             MSGCLASS=H,
//             NOTIFY=&SYSUID
//*                                                           Col 72 ->|
//* ***** Edit with CAPS OFF and NUMBER OFF *****
//*
//* Run a single file Java program under Java 21 using BPXBATCH
//*
//G   EXEC PGM=BPXBATCH,REGION=0M
//STDOUT    DD SYSOUT=*
//STDERR    DD SYSOUT=*
//STDPARM DD *
SH /usr/lpp/java/J21.0_64/bin/java
 -cp "/home/andrewr/easysmf-je-3.0.0/samples/jar/lib/*"
 /home/andrewr/java/src/DeltaStorageGroups.java
 "//'DCOLLECT.DATA'"
 "//'DCOLLECT.DATA2'"
//STDENV    DD *,SYMBOLS=JCLONLY
EASYSMFKEY=//'VENDOR.PARMLIB(EZSMFKEY)'
/*
