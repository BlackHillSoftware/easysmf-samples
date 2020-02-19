@REM     Instructions
@REM
@REM     1) Change EASYSMFLOCATION to the path where EasySMF was installed.
@REM     2) Change EASYSMFKEY to the path of the file with the temporary
@REM        or permanent key.
@REM        Get a 30 day trial key from: 
@REM        https://www.blackhillsoftware.com/30-day-trial/
@REM     3) If you compiled your samples using the compilesample.bat script, 
@REM        change TARGET to match compilesample.bat. If the specified class 
@REM        is not found in the TARGET directory, the easysmf-je-samples jar
@REM        will be searched.
@REM     4) Run the batch file passing the sample and SMF data file as arguments e.g.:
@REM        runsample com.blackhillsoftware.samples.RecordCount SMF.DATA
@REM
@REM     This sample assumes that required Java environment variables e.g. JAVA_HOME 
@REM     were set by the Java installation process.
@REM

set "EASYSMFLOCATION=C:\path to\easysmf-je-VERSION"
set "EASYSMFKEY=C:\path to your\key.txt"
set "TARGET=java\target"


java -classpath "%TARGET%;%EASYSMFLOCATION%\samples\*;%EASYSMFLOCATION%\jar\*" %1 %2
