# rti-smt-switch

This sample reads type 70 and 72 SMF records from the SMF Real Time Interface, 
calculates a velocity for zIIP usage across all service classes, and decides
based on velocity thresholds whether SMT2 should be enabled or disabled.

If the SMT setting should be changed, it can either
- write a message to STDOUT
- issue a WTO, for information or action by your automation product
- issue a command to change the setting

### Disclaimer

 This program demonstrates usage of the EasySMF real time 
 interface. It is not a recommendation for how to manage your zIIP 
 processors.
 
 Consult your performance specialist to decide whether you should
 run with SMT2 enabled, disabled or dynamically switched.

### Velocity Values

The velocity values used to trigger a switch are values used for development of this program, and are 
not a recommendation for production use.

The values for your system will depend on workload, number of processors etc. Enabling SMT2 will change the achieved velocities, the thresholds should be far enough apart that the change in velocity doesn't result in cycling back and forth.

You can report on the calculated velocities for your system using the [ZiipVelocity](./src/main/java/com/smfreports/sample/ZiipVelocity.java) sample program.

## Build

### As a jar file

Build the easysmf-rti-smt-switch project using Maven:

```
mvn -f pom.xml clean package
```

The easysmf-rti-smt-switch jar file will be created in the ```./target``` directory. The project dependencies will be copied to ```./target/lib```.

#### JZOS Dependencies

Writing WTO messages requires the JZOS jar file as a dependency. If you want to use JZOS functions and build with Maven, you need to obtain the jar file from IBM and install it in the Maven repository on your build machine.

```
mvn install:install-file -Dfile=ibmjzos.jar -DgroupId=com.ibm.jzos -DartifactId=ibmjzos -Dversion=2.4.8 -Dpackaging=jar
```

Modify the jar file name and version as required. If your JZOS is a different version you will need to update the dependency in pom.xml to reflect the correct version.

To find the version of your ibmjzos.jar file:
```
unzip -p ibmjzos.jar META-INF/MANIFEST.MF
```
and check the Implementation-Version entry.

The project references version 2.4.8 which is quite old, but it means the program should work even with older Java installations.

Uncomment the ibmjzos dependency in [pom.xml](./pom.xml).

### Java 11 Single File Source Code

The source code is contained in a single file, so it can be run under Java 11 without a separate compile step. Copy the source to z/OS (or clone the samples using Git) and run under BPXBATCH using the [J11BPXB](./JCL/J11BPXB.jcl) JCL.

## z/OS prerequisites

In order to run the samples on z/OS, some preparation is required:

- SMF needs to be running in Logstream mode
- The in memory resource(s) need to be defined to SMF
- The user running the program needs RACF access to read from the in memory resource, and issue the MVS commands if you choose to do have the program change the setting.

## Run

JCL is provided to run the program using Java 11 as a single file source code program, 
or from a jar file under JAVA 8 (64 bit) or Java 11 JZOS.

* [J11BPXB.jcl](./JCL/J11BPXB.jcl) Java 11 single file source code
* [RUNJZOS.jcl](./JCL/RUNJZOS.jcl) Run from jar file using JZOS

The EasySMF:JE ```samples/jar/lib``` directory contains the dependencies needed to run the 
program. Set the classpath to point to this directory in the single file source code JCL.

If you build the program using Maven, dependencies will be copied to the ```./target```
and ```./target/lib``` directories.
Copy all the jar files from the ```./target```  and ```./target/lib``` directories to
corresponding directories on z/OS, and update the ```JARDIR``` in the RUNJZOS JCL.

