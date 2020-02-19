## Sample 2: Filter and print SMF data

[Sample 2 Source Code: sample2.java](./src/sample2.java)

Sample 2 shows how you can use Java Streams to filter and process SMF data.

Streams are a neat way to define a series of actions, but they have some restrictions.
Sometimes it is easier to use Streams, other times a traditional for(...) loop is easier. 

The sample searches for SMF type 14 (input) and 15 (output) dataset close records for dataset SYS1.PARMLIB, and
prints the time, system, jobname and the type of access.

The SmfRecordReader is opened in the same was as for sample1.

The Stream API involves a sequence of steps, where the output of each step is passed to the next step.

```
reader
    .include(14)            	
    .include(15)
    .stream()
    .map(record -> Smf14Record.from(record)) 
    .filter(r14 -> r14.smfjfcb1().jfcbdsnm().equals("SYS1.PARMLIB"))
    .limit(1000000)
    .forEachOrdered(r14 -> 
        System.out.format("%-23s %-4s %-8s %-6s%n",                                  
        r14.smfDateTime(), 
        r14.system(),
        r14.smf14jbn(),
        r14.smf14rty() == 14 ? "Read" : "Update"));    
```

This sequence tells the SmfRecordReader to include SMF type 14 and type 15 records, then streams the records
to the next steps.
- **map** creates a Smf14Record from the base SmfRecord (type 14 and 15 records have the same format).
- **filter** passes only the records that match the filter criteria: smfjfcb1() is the JFCB, which contains the
  dataset name in jfcbdsnm(). We want to list references to SYS1.PARMLIB.
- **limit** stop after this number of matches.
- **forEach** process each record. In this case, print the output.

[Sample 2 Source Code: sample2.java](./src/sample2.java)