#
# Instructions
#
# 1) Change EASYSMFLOCATION to the path where EasySMF was installed.
# 2) Change TARGET directory as required. As distributed it is
#    relative to the current working directory.
# 3) Ensure this file is executable:
#    chmod +x runsample
# 4) Run the script passing the sample file as an argument e.g.:
#    ./compilesample.sh java/easysmf-je-VERSION/samples/source/com/blackhillsoftware/samples/RecordCount.java
#
# This sample assumes that required Java environment variables e.g. JAVA_HOME 
# were set by the Java installation process.
#

export EASYSMFLOCATION="./java/easysmf-je-VERSION"
export TARGET="./java/target"

javac -cp "$EASYSMFLOCATION/jar/*" -d "$TARGET" "$1"