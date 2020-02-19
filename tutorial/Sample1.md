## Sample 1: Read, extract and print SMF data

[Sample 1 Source Code: sample1.java](./src/sample1.java)

Sample 1 shows the basics of reading SMF data and extracting sections and fields.

Various CPU times are extracted and printed from the Processor Accounting section in the SMF type 30 subtype 5 (Job End) records. The data is printed in CSV format.

#### Reading SMF Data

SMF Records are read using the SmfRecordReader class. This class implements AutoCloseable and should be used in a **try-with-resources** block so it is automatically closed before the program exits.

The samples can run as a z/OS batch job under the JZOS Batch Launcher reading from a JCL DD, or on another platform (Windows, Linux, or z/OS BPXBATCH) with the input source passed as a command line argument to the program.

The first command line argument is used as a name to create the SmfRecordReader. Different name formats are used for different types of input:  

| Syntax                 | File or dataset            |
|------------------------|----------------------------|
| `//DD:DDNAME`          | Open a preallocated DDNAME |
| `//'MVS.DATASET.NAME'` | Open a MVS dataset by name |
| Anything else          | Open a file name           |

```
try (SmfRecordReader reader = SmfRecordReader.fromName(args[0])) 
{
    ...                                                                          
}
```

We tell the SmfRecordReader to include only type 30 subtype 5 records: 

```
reader.include(30,5);
```

#### Processing the Data

Read the records and create a Smf30Record object from each base SmfRecord:

```
for (SmfRecord record : reader)
{
    Smf30Record r30 = Smf30Record.from(record);
    ...
} 

```

Process each record. Check whether it has a Processor Accounting Section (some records don't e.g. when a job has more data than will fit in a single SMF record) and if found print information about the job.   

```
if (r30.processorAccountingSection() != null)
{
    System.out.format("%s,%s,%s,%s,%.2f,%.2f,%.2f%n",                                  
        r30.smfDateTime(), 
        r30.system(),
        r30.identificationSection().smf30jbn(),
        r30.identificationSection().smf30jnm(),
        r30.processorAccountingSection().smf30cptSeconds()
            + r30.processorAccountingSection().smf30cpsSeconds(),
        r30.processorAccountingSection().smf30TimeOnZiipSeconds(),
        r30.processorAccountingSection().smf30TimeZiipOnCpSeconds()
        );
}
```

#### Notes

- Methods are provided to extract the specific SMF sections e.g. `identificationSection()`,
`processorAccountingSection()`. If there may be more than one section the method returns a List of
sections, e.g. `excpSections()` returns `List<ExcpSection>`. 
- Methods that return a list return an **empty list** if there a none of that section in the record.
This means you can iterate over the lists without specifically checking whether sections are present - an empty list simply interates 0 times.
- Records and sections have methods to extract the specific fields.
  - Text values return a String e.g. `smf30jbn()`
  - Numeric values return either a 32 bit **int** value (for unsigned fields less than 4 bytes and signed fields up to 4 bytes) or a 64 bit **long** value for 4-8 byte unsigned fields and 5-8 byte signed fields.
  Unsigned 8 byte fields will throw an exception if the value exceeds the maximum for a signed 64 bit value.
  Unsigned 8 byte field values are also available as a **BigInteger**. Use the BigInteger value if the value can exceed the maximum signed 64 bit value. 
  - Values representing an amount of time e.g. a CPU time are available both as a **java.time.Duration** and converted to seconds as a floating point **double** value. e.g. `Duration smf30cpt()` and `double smf30cptSeconds()`.
  - Time values may be converted to **java.time.LocalTime**, **java.time.LocalDate**, **java.time.LocalDateTime**
  or **java.time.ZonedDateTime** (typically with ZoneOffset.UTC) depending on the type of value they represent.
  This prevents e.g. accidentally comparing local time fields with UTC time fields.
  You can convert a LocalDateTime to a ZonedDateTime by applying a time zone. You can then validly compare these fields with UTC values, and even compare times from systems running with different time zones by specifying the correct timezone for each system.
- The objective is to provide consistent methods and values across all SMF record types, so that techniques for accessing the data are transferable from one record type to another.
- Data about the meaning of the fields can be found in the various IBM documentation. Documenting the meaning of the fields is beyond the scope of the product.

[Sample 1 Source Code: sample1.java](./src/sample1.java)