# Convert DCOLLECT records to JSON format

This sample generates JSON from DCOLLECT records.

Each DCOLLECT record type is written to a different DD name. Select which record types you want by including or omitting the output DD names in the JCL.

JSON records are created in a JSON array, so that they can be opened by programs like Excel, which expect multiple records in array format.

The sample must run under the JZOS batch launcher so that it has access to the DDs in the JCL.

[Dcollect2Json.java](./src/main/java/com/smfreports/dollect2json/Dcollect2Json.java)

## JCL

JCL to run the program can be found here: [JCL/DC2JSON.txt](./JCL/DC2JSON.txt)