## JCL

[JCL](../JCL/) is supplied to compile and run the code on z/OS.

The [compile job](../JCL/COMPILE.jcl) uses BPXBATCH.

JCL is provided to run the program under the [JZOS Batch Launcher](../JCL/RUNJZOS.jcl) (recommended) and [BPXBATCH](../JCL/RUNBPXB.jcl). 

The JZOS Batch Launcher allows the Java program to use regular z/OS JCL, with DD statements to define input and output files. BPXBATCH runs Java under a Unix shell, which means you do not have access to most DDs defined in the JCL.

The supplied JCL assumes that EasySMF is installed in the subdirectory **java/easysmf-je-[V-R-M]** under your unix home directory. The samples are in the subdirectory **java/easysmf-je-[V-R-M]/samples/source** and the java compiler creates class files in **java/target**.

### Compile using BPXBATCH

[Compile JCL](../JCL/COMPILE.jcl)

The sample JCL uses symbolic parameters to customize file and path names.

Java classes in a package need to be in subdirectories matching the package name, which means that the full path can be lengthy and a problem to specify in JCL. To reduce the length problem this job specifies the java file name in 2 parts: the source directory name, and the class name including the package.
  
JCL parameters are assigned to unix environment variables via the STDENV DD and the environment variables are substituted by the shell where length isn't a problem.

Customize the parameters:

- SRC - Location of the source to compile e.g. './java/easysmf-je-1-9-3/samples/source'
- TGT - Destination for the compiled .class files e.g. './java/target'
- CLASS - The path of the java file to compile, relative to the SRC directory e.g. 'com/blackhillsoftware/samples/RecordCount.java'
- EZSMFDIR - The EasySMF:JE installation directory e.g. './java/easysmf-je-1-9-3'
- JAVA - The location to find Java e.g. '/usr/lpp/java/J8.0'

### Run using JZOS Batch Launcher

[JZOS JCL](../JCL/RUNJZOS.jcl)

Installation instructions for the JZOS Batch Launcher can be found here:
[JZOS Batch Launcher and Toolkit Installation and User's Guide](ftp://public.dhe.ibm.com//software/Java/Java80/JZOS/jzos_users_guide_v8.pdf)

Installation of the JZOS Batch Launcher is very simple. It consists of copying the load module from the Java directory to a PDS/E, plus some sample JCL.

The EasySMF sample JCL is based on the sample JCL supplied with JZOS.

Again JCL symbolic parameters are used to customize the JCL.

- CLASS - The java class to run e.g. 'com.blackhillsoftware.samples.RecordCount'
- TGT - The location of locally compiled Java classes e.g. './java/target'
  This is searched ahead of the jar containing EasySMF samples. You can use the JCL to run the compiled samples distributed with EasySMF, or run programs compiled using the **Compile using BPXBATCH** sample JCL.
- EZSMFDIR - The EasySMF:JE installation directory e.g. './java/easysmf-je-1-9-3'
- EZSMFJAR - The EasySMF:JE jar file name e.g. 'easysmf-je-1.9.3.jar'
- EZSMFSAM - The EasySMF:JE samples jar file name e.g. 'easysmf-je-samples-1.9.1.jar'
- JZOSLIB - The PDS/E containing the JZOS load module e.g. JZOS.LINKLIBE
- JAVA - The location to find Java e.g. '/usr/lpp/java/J8.0'
- SMFDATA - The z/OS dataset containing your SMF data e.g. SMF.RECORDS


### Run using BPXBATCH

[BPXBATCH JCL](../JCL/RUNBPXB.jcl)

Java programs can also be run under BPXBATCH. The program runs as a command line program in the unix shell so some facilities like z/OS DDNAMEs are not available.

The MVS dataset containing the SMF data needs to be passed by name on the command line, using the syntax: 
```"//'MVS.DATASET.NAME'"```

The location of the EasySMF license key is set in an environment variable. 

JCL symbolic parameters are used to customize the JCL.

- CLASS - The java class to run e.g. 'com.blackhillsoftware.samples.RecordCount'
- TGT - The location of locally compiled Java classes e.g. './java/target'
  This is searched ahead of the jar containing EasySMF samples. You can use the JCL to run the compiled samples distributed with EasySMF, or run programs compiled using the **Compile using BPXBATCH** sample JCL.
- EZSMFDIR - The EasySMF:JE installation directory e.g. './java/easysmf-je-1-9-3'
- JAVA - The location to find Java e.g. '/usr/lpp/java/J8.0'
- SMFDATA - The z/OS dataset containing your SMF data e.g. SMF.RECORDS
- KEYDSN - the dataset name containing the EasySMF license key e.g. EASYSMF.INSTALL(EZSMFK)