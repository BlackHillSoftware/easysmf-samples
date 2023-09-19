# How to Query CICS Transaction SMF Data

These are  sample programs for the video:

[How to Query CICS Transaction SMF Data using Java Streams and 
EasySMF:JE](https://youtu.be/OhyJTJ0QN1I)

The samples demonstrate techniques including
- Filtering by CICS transaction fields
- Finding the Top N transactions by various fields
- Grouping data and finding the Top N values for each group
- Counting and summing values
- Reading data from files, datasets and SMF logstreams

Samples provide multiple output formats:
- Simple transaction data
- JSON format
- A list of CICS Clock values where the value is more than 10% of the 
transaction elapsed time.

Uncomment the output format you prefer.

### [Query01ElapsedTime](./src/main/java/Query01ElapsedTime.java)

 This sample demonstrates Querying CICS transaction SMF data
 to find transactions that exceeded a specified elapsed time.

### [Query02TopValues](./src/main/java/Query02TopValues.java)

 This sample demonstrates Querying CICS transaction SMF data
 to find the top 10 transactions by elapsed time for each 
 transaction name.

### [Query03ByStartTime](./src/main/java/Query03ByStartTime.java)

 This sample demonstrates Querying CICS transaction SMF data
 to find the first 100 long running transactions by start time
 between 2 times.

### [Query04CountingAndSumming](./src/main/java/Query04CountingAndSumming.java)

 This sample demonstrates Querying CICS transaction SMF data,
 counting or summing data fields.

### [Query05ReadLogstream](./src/main/java/Query05ReadLogstream.java)

 This sample demonstrates Querying CICS transaction SMF data
 from a SMF logstream on z/OS.
  
 Transactions from a specific terminal are reported.

## JCL

JCL to run the samples as Java 11 single file source code programs under BPXBATCH can be found here:

[J11BPXB.txt](./JCL/J11BPXB.txt)

