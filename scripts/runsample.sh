#
# Instructions
#
# If the sample was built using the supplied Maven projects, the 
# output is in the target subdirectory and the dependencies 
# including EasySMF are in the target/lib subdirectory.
# In this case EASYSMFLOCATION variable is not important, but we
# set it to the target/lib directory for consistency.
#
# 1) Change EASYSMFLOCATION to the path where EasySMF was installed,
#    or the target/lib subdirectory if it was copied there by the 
#    Maven build.
# 2) Change EASYSMFKEY to the path of the file with the temporary
#    or permanent key.
#    Get a 30 day trial key from: 
#    https://www.blackhillsoftware.com/30-day-trial/
# 3) If you compiled your samples using the compilesample.sh script, 
#    change TARGET to match compilesample.sh.
#    If the specified class is not found in the TARGET directory and
#    you installed from the installation zip file the easysmf-je-samples
#    jar in EASYSMFLOCATION will be searched.
# 4) Ensure this file is executable:
#    chmod +x runsample
# 5) Run the script passing the sample and SMF data file as arguments e.g.:
#    ./runsample.sh com.blackhillsoftware.samples.RecordCount SMF.DATA
#
# This sample assumes that required Java environment variables e.g. JAVA_HOME 
# were set by the Java installation process.
#

# Directory containing EasySMF:
export EASYSMFLOCATION="/home/<userid>/java/easysmf-je-VERSION"

# File containing EasySMF Key:
export EASYSMFKEY="/home/<userid>/easysmfkey.txt"

# Optional, directory with compiled EasySMF Java programs:
export TARGET="/home/<userid>/java/target"

java -classpath "$TARGET:$TARGET/*:$TARGET/lib/*:$EASYSMFLOCATION/jar/*:$EASYSMFLOCATION/samples/jar/lib/*:$EASYSMFLOCATION/samples/jar/*" $@