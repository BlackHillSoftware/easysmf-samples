@REM     Instructions
@REM
@REM     If the sample was built using the supplied Maven projects, the 
@REM     output is in the target subdirectory and the dependencies 
@REM     including EasySMF are in the target/lib subdirectory.
@REM     In this case EASYSMFLOCATION variable is not important, but we
@REM     set it to the target/lib directory for consistency.
@REM
@REM     1) Change EASYSMFLOCATION to the path where EasySMF was installed,
@REM        or the target/lib subdirectory if it was copied there by the 
@REM        Maven build.
@REM     2) Change EASYSMFKEY to the path of the file with the temporary
@REM        or permanent key.
@REM        Get a 30 day trial key from: 
@REM        https://www.blackhillsoftware.com/30-day-trial/
@REM     3) If you compiled your samples using the compilesample.bat script, 
@REM        change TARGET to match compilesample.bat. 
@REM        If the specified class is not found in the TARGET directory and
@REM        you installed from the installation zip file the easysmf-je-samples
@REM        jar in EASYSMFLOCATION will be searched.
@REM     4) Run the batch file passing the sample and SMF data file as 
@REM        arguments e.g.:
@REM        runsample com.smfreports.RecordCount SMF.DATA
@REM
@REM     This sample assumes that required Java environment variables e.g. JAVA_HOME 
@REM     were set by the Java installation process.
@REM

@REM     Directory containing EasySMF, or target\lib subdirectory for Maven build:
set "EASYSMFLOCATION=C:\path to\easysmf-je-VERSION"


@REM     File containing EasySMF Key:
set "EASYSMFKEY=C:\path to your\key.txt"

@REM     Optional, directory with compiled EasySMF Java programs:
set "TARGET=C:\path to\java\target"

java -classpath "%TARGET%;"%TARGET%\*;"%TARGET%\lib\*;%EASYSMFLOCATION%\jar\*;%EASYSMFLOCATION%\samples\jar\lib\*;%EASYSMFLOCATION%\samples\jar\*" %*
