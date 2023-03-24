## Scripts

Scripts and batch files are supplied to compile and run the samples on Windows and unix systems.

We strongly recommend building using [Apache Maven](https://maven.apache.org/) and an IDE e.g. Eclipse rather than these compile scripts, but they are provided for documentation purposes.

The run scripts provide samples of setting the classpath and environment variable for the EasySMF key.

### Windows

[compilesample.bat](../scripts/compilesample.bat)

[runsample.bat](../scripts/runsample.bat)

### Unix

[compilesample.sh](../scripts/compilesample.sh)

[runsample.sh](../scripts/runsample.sh)


### Compiling the Samples

Set the environment variables to reflect your environment.
 - EASYSMFLOCATION : The directory containing EasySMF, e.g. **./java/easysmf-je-2.0.1**
 - TARGET : the target directory for the compilation. The Java class files will be created here.

Run the script specifying the java file name to compile e.g.
```
./compilesample.sh java/easysmf-je-2.0.1/samples/sample-reports/src/main/java/com/blackhillsoftware/samples/RecordCount.java
```

### Running the Samples

Again, set the environment variables to reflect your environment.
 - EASYSMFLOCATION : The directory containing EasySMF, e.g. **./java/easysmf-je-2.0.1**
 - TARGET : location of the class files e.g. from **compilesample**. If you are running the samples from the distributed easysmf-je-samples jar this is not required.
 - EASYSMFKEY : the location of the file containing the EasySMF:JE temporary or permanent key. Get a 30 day trial key from:  
 [https://www.blackhillsoftware.com/30-day-trial/](https://www.blackhillsoftware.com/30-day-trial/)

Run the program specifying the full class name and the SMF data file e.g.
```
./runsample.sh com.smfreports.RecordCount SMF.DATA
```