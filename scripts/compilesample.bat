@REM     Instructions
@REM
@REM     1) Change EASYSMFLOCATION to the path where EasySMF was installed.
@REM     2) Change TARGET directory as required. As distributed it is 
@REM        relative to the current working directory.
@REM     3) Run the batch file passing the sample file as arguments e.g.:
@REM        compilesample java/easysmf-je-1-9-4/samples/source/com/blackhillsoftware/samples/RecordCount.java
@REM
@REM     This sample assumes that required Java environment variables e.g. JAVA_HOME 
@REM     were set by the Java installation process.
@REM

set "EASYSMFLOCATION=C:\path to\easysmf-je-VERSION"
set "TARGET=java\target"

javac -cp "%EASYSMFLOCATION%\jar\*" -d "%TARGET%" %1 
