# SMF to JSON Skeleton Project

This project provides a skeleton project for a program using the Smf2JsonCLI.

To use the project to create a program:

Prerequisite:
[Apache Maven](https://maven.apache.org/) to build the project.

1) Clone the repository using git or download as a zip file
2) Change to the smf2json-skeleton directory
3) Run the Maven command: ```mvn clean package```   
   The first time you run Maven it will download many components required for the build. These are cached on your machine.
   This builds a jar file for the program in the ```target``` subdirectory,
   and copies the dependencies (other jar files) to the same directory.
4) Run the program:   
   ```java -cp target/* com.smfreports.smf2json.Sample```   
   should give you a "Usage" page.  
   ```java -cp target/* com.smfreports.smf2json.Sample mydata.smf```  
   will read the mydata.smf file and produce JSON output.
5) Modify the program to process different record types and sections as required.





