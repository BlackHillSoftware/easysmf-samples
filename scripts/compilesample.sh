# This is a script to manually compile a Java program.
# We strongly recommend you use Apache Maven to build the samples 
# instead.
#
# Instructions
#
# 1) Change EASYSMFLOCATION to the path where EasySMF was installed.
# 2) Change TARGET directory as required.
# 3) Ensure this file is executable:
#    chmod +x runsample
# 4) Run the script passing the sample file as an argument e.g.:
#    ./compilesample.sh sample-reports/src/main/java/com/smfreports/RecordCount.java
#
# This sample assumes that required Java environment variables e.g. JAVA_HOME 
# were set by the Java installation process.
#

export EASYSMFLOCATION="/home/<userid>/easysmf-je-VERSION"
export TARGET="/home/<userid>/java/target"

javac -cp "$EASYSMFLOCATION/jar/*:$EASYSMFLOCATION/samples/jar/lib/*" -d "$TARGET" "$1"