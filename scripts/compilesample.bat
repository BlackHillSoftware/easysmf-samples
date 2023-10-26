@REM     This is a script to manually compile a Java program.
@REM     We strongly recommend you use Apache Maven to build the samples 
@REM     instead.
@REM
@REM     Instructions
@REM
@REM     1) Change EASYSMFLOCATION to the path where EasySMF was installed.
@REM     2) Change TARGET directory as required.
@REM     3) Run the batch file passing the sample file as arguments e.g.:
@REM        compilesample sample-reports\src\main\java\com\smfreports\RecordCount.java
@REM
@REM     This sample assumes that required Java environment variables e.g. JAVA_HOME 
@REM     were set by the Java installation process.
@REM

set "EASYSMFLOCATION=C:\path to\easysmf-je-2.2.1"
set "TARGET=C:\path to\java\target"

javac -cp "%EASYSMFLOCATION%\jar\*;%EASYSMFLOCATION%\samples\jar\lib\*" -d "%TARGET%" %1 
