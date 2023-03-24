## EasySMF:JE Introduction

EasySMF:JE provides an API for accessing SMF data using Java. It provides Java classes to map SMF records and SMF record sections.

The Java classes provide access to the data in the record, without needing to understand the underlying record structure. The classes provide interfaces that are as consistent as possible across all the different record types. Techniques learned for one record type are easily transferrable to other record types.

### Getting SMF Records

SMF records are read using a [SmfRecordReader](https://static.blackhillsoftware.com/easysmf/javadoc/com/blackhillsoftware/smf/SmfRecordReader.html) to read from a JCL DD statement, z/OS dataset, a file or an InputStream.

You can interate or stream from the SmfRecordReader to get [SmfRecord](https://static.blackhillsoftware.com/easysmf/javadoc/com/blackhillsoftware/smf/SmfRecord.html)s. The SmfRecord class has the basic attributes common to all SMF records, e.g. recordType(). Once you know the record type you can create the specialized SMF record from the base SMF record:

```
try (SmfRecordReader reader = SmfRecordReader.fromName(args[0]))
{            
    for (SmfRecord record : reader)
    {
        if (record.recordType() == 30)
        {
            Smf30Record r30 = Smf30Record.from(record);
        }
    }
}
```

### Input Data Format

The SmfRecordReader can read data from various sources.

- z/OS SMF Dump Datasets 
Reading from z/OS SMF Dump datasets is straightforward. You can use the JZOS Batch Launcher to run the java program and point the SmfRecordReader to a DDNAME in the JCL.
- Unix or Windows files
SMF data in Unix or Windows files needs to include the Record Descriptor Word (RDW) and have been transferred in Binary mode i.e. **without** any EBCDIC to ASCII translation or record boundaries inserted. FTP on z/OS will strip the RDW by default, to override this setting use the **SITE RDW** option. The SmfRecordReader can also read data transferred in RECFM=U format, e.g. this is often used when transferring SMF data for use with SAS.
- InputStream
The SmfRecordReader can read form any source that implements an [InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html). Like Unix and Windows files, the InputStream must provide the data in binary format including the RDW.

### Data Conversion

Data in SMF record fields are converted to Java types using a consistent set of principles.

#### Dates and Times

- Dates and times are converted to [java.time](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) classes, depending on the type of data they contain. They could be a LocalDate, LocalTime, LocalDateTime or a ZonedDateTime with ZoneOffset.UTC. 
- Quantities of time e.g. CPU time, connect time are available as a Duration and as a floating point value in seconds.
- The raw (unconverted) value is also available.

EasySMF time conversions mean that you do not need to worry about the units for different fields when you are processing SMF data. Different data types for local and UTC date/times provide a defense against programming errors like comparing local and UTC time fields. Java has built in time zone support, so you can transform times between UTC and local time and between different time zones.

Java.time classes store values with nanosecond precision. This is more than adequate for most SMF fields. Where more precision is required e.g. STCK timestamps, you can choose to use the raw value to get the full precision.

#### Numeric values

- Binary fields up to 3 bytes unsigned or 4 bytes signed are converted to Java **int**
- Binary fields of 4-8 bytes are converted to Java **long**.
8 byte unsigned values are also available as **BigInteger** values. Accessing the **long** value will throw an exception if the value exceeds the maximum value for a signed long, i.e. if the high order bit is set. If this is possible you should use the BigInteger value.
- Floating point values are converted to Java **double** values.
- Packed decimal data is converted to **int**, **long** or **BigInteger** depending on the number of digits.

#### Text Values

Text data e.g. EBCDIC data is converted to a Java **String**. Some records contain UTF8 data, which is also extracted into a Java String.

### SMF Sections

Classes mapping SMF records and sections provide methods to access sections and subsections. 

#### Single Sections

If a record can have no more than one of a section the method to access the record will return the section if it exists, or null.

For example, the Smf30Record class has a [completionSection()](https://static.blackhillsoftware.com/easysmf/javadoc/com/blackhillsoftware/smf/smf30/Smf30Record.html#completionSection--) method to access the SMF 30 Completion Section. If there is no Completion Section in the record completionSection() method returns null.

There are a number of sections where it seems likely there can be only one instance, but there is a count field and no explicit documentation saying no more than one. These are typically set up as repeating sections with the number of entries in the list is expected to be one or zero.

#### Repeating Sections

Often a section can occur multiple times in a record e.g. sections located by a SMF **triplet**. In that case the sections are returned in a **List**.

An example is the [excpSections()](https://static.blackhillsoftware.com/easysmf/javadoc/com/blackhillsoftware/smf/smf30/Smf30Record.html#excpSections--) method in the Smf30Record.

 If the record does not contain any of that particular section i.e. the triplet has zero for the count the method returns an empty list.

Frequently this means that you don't need a special check whether the section is present, iterators will simply iterate zero times for an empty list.

### Java Rules

Java is strict about the naming of files. A class called **sample1** must be in a file called **sample1.java**.
It will be compiled to a file called **sample1.class**. Class names and file names are case sensitive.

To avoid name clashes, Java classes are usually contained in packages. **sample1** might be placed in package
**com.blackhillsoftware.samples**:

```
package com.blackhillsoftware.samples;
public class sample1 
{
   ...
}
```

The full class name becomes **com.blackhillsoftware.samples.sample1**. The source file needs to be
**com/blackhillsoftware/samples/sample1.java** and it will be compiled to file
**com/blackhillsoftware/samples/sample1.class**.

The samples in this tutorial use the **default package** i.e. no package. This means you don't have to search through subdirectories to find the code. It simplifies compiling and running the tutorials but is not recommended for production code.

