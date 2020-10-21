#
# Instructions
#
# 1) Change EASYSMFLOCATION to the path where EasySMF was installed.
# 2) Change EASYSMFKEY to the path of the file with the temporary
#    or permanent key.
#    Get a 30 day trial key from: 
#    https://www.blackhillsoftware.com/30-day-trial/
# 3) If you compiled your samples using the compilesample.sh script, 
#    change TARGET to match compilesample.sh. If the specified class 
#    is not found in the TARGET directory, the easysmf-je-samples jar
#    will be searched.
# 4) Ensure this file is executable:
#    chmod +x runsample
# 5) Run the script passing the sample and SMF data file as arguments e.g.:
#    ./runsample.sh com.blackhillsoftware.samples.RecordCount SMF.DATA
#
# This sample assumes that required Java environment variables e.g. JAVA_HOME 
# were set by the Java installation process.
#

# Directory containing EasySMF:
export EASYSMFLOCATION="./java/easysmf-je-VERSION"

# File containing EasySMF Key:
export EASYSMFKEY="./java/easysmfkey.txt"

# Optional, directory with compiled EasySMF Java programs:
export TARGET="./java/target"

# Directory containing EasySMF Samples jar file:
export EASYSMFSAMPLES="./java/easysmf-samples"

java -classpath "$TARGET:$EASYSMFSAMPLES/*:$EASYSMFLOCATION/jar/*" $@