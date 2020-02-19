## Sample 3: Search SMF for a Text String 

[Sample 3 Source Code: sample3.java](./src/sample3.java)

Sample 3 shows how you can search for text when you don't know which specific record types might be relevant.

It is similar to sample 2, except that the processing is done on the base SMF record.

Filtering is applied to ignore types 14, 15 and 42 subtype 6 records because we already know the dataset name will
be found in those records.

```
reader
    .stream()
    .filter(record -> record.recordType() != 14 && record.recordType() != 15)
    .filter(record -> !(record.recordType() == 42 && record.subType() == 6))
    .filter(record -> record.toString().contains("SYS1.PARMLIB"))
    .limit(100) // stop after 100 matches
    .forEachOrdered(record -> 
    {
        System.out.format("%-23s System: %-4s Record Type: %s Subtype: %s%n%n",                                  
            record.smfDateTime(), 
            record.system(),
            record.recordType(),
            record.hasSubtypes() ? record.subType() : "");

        System.out.format("%s%n%n",                                  
            record.dump());
    });
```

For each match we write a header with the time, system, record type and subtype then dump the record. We stop
after 100 matches to avoid excessive output.

#### Notes

The entire record is translated to a Java string before searching for the text. For performance reasons it is best to do all other filtering e.g. by record type before the string search - this avoids doing the translation unnecessarily.

When running this sample, start with a limited amount of data and monitor the CPU time consumed. In testing, the program used about 10 seconds of CPU time per GB of SMF data on the IBM Dallas Remote Development system. 90% of that was on a zIIP.

[Sample 3 Source Code: sample3.java](./src/sample3.java)