# EasySMF:JE Skeleton Project

This sample provides a skeleton for a Maven project to build a SMF reporting application 
with EasySMF:JE.

The supplied program simply reads the SMF data and writes a count of the number of 
records in the input. This verifies that the compile is successful and the 
required jar file dependencies can be found at runtime.

## Build

Build the project using Apache maven:
```
mvn clean package
```

## Run

The project creates an executable jar file which can be run with the command:
```
java -jar target\easysmf-skeleton-1.0.0.jar inputdata.smf
```

### CLASSPATH and dependencies

The executable jar defines its own CLASSPATH. It does not use the CLASSPATH specified by e.g. 
the CLASSPATH environment variable. It will look for its dependencies in the same directory as the executable jar file.

The Maven build copies the dependencies to the build target directory where the executable easysmf-skeleton jar is created. Keep the jar files together if you copy them to another location.

## Modify

Modify the code in [EasySmfSkeleton.java](./src/main/java/com/smfreports/sample/EasySmfSkeleton.java) 
as required.

If you rename the EasySmfSkeleton class, you need to also change the value for the **mainClass** in the Maven POM (case sensitive!):

 [pom.xml](./pom.xml)

```
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.5</version>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>com.smfreports.sample.EasySmfSkeleton</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

Other dependencies can be added to the **dependencies** section of the POM.

```
<dependencies>
    <dependency>
        <groupId>com.blackhillsoftware.smf</groupId>
        <artifactId>easysmf-je</artifactId>
        <version>2.0.0</version>
    </dependency>
    ...
</dependencies>
```
