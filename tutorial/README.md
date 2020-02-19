# Tutorial : Processing SMF Data using Java

EasySMF:JE provides an API for accessing SMF data using Java. It provides Java classes to map SMF records and SMF record sections.

These tutorials are provided to illustrate the use of the EasySMF:JE Java API to process SMF data.

## Contents

1. [EasySMF:JE Introduction](Introduction.md)  
The [Introduction](Introduction.md) describes the basic principles of the API, including data and date-time conversions, and how to access record sections and subsections.

1. [Installation](https://www.blackhillsoftware.com/javasmf/easysmfje-java-quickstart/)  
Quick Start Installation Instructions can be found [here](https://www.blackhillsoftware.com/javasmf/easysmfje-java-quickstart/) 

1. [JCL](JCL.md) to compile and run the code on z/OS.

1. [Scripts](Scripts.md) to compile and run the code on Windows and unix systems.

1. Samples
Most samples are self-contained in a single file and use the default package to make compiling and running the code on z/OS as simple as possible. You can transfer (or even cut and paste) the samples to a unix file on z/OS and use the supplied JCL to compile and run the code.

    - [Sample 1: Read, extract and print SMF data](Sample1.md)
[Sample 1](Sample1.md) shows the basics of reading SMF data and extracting sections and fields.
Various CPU times are extracted and printed from the Processor Accounting section in the SMF type 30 subtype 5 (Job End) records. The data is output in CSV format.

    - [Sample 2: Filter SMF data](Sample2.md)
[Sample 2](Sample2.md) shows how you can use Java Streams to filter and process SMF data.
The sample searches for SMF type 14 (input) and 15 (output) dataset close records for dataset SYS1.PARMLIB, and prints the time, system, jobname and the type of access.

    - [Sample 3: Search SMF for a Text String](Sample3.md)
[Sample 3](Sample3.md) shows how you can search for specific text when you don't know which specific record types might be relevant.

    - [Sample 4: Group and Summarize SMF data](Sample4.md)
[Sample 4](Sample4.md) shows how group and summarize SMF data in Java.
The program reports statistics for each program name, taken from SMF type 30 subtype 4 (Step End) records.

    - [Sample 5: Repeating Record Sections](Sample5.md)
When a SMF record has a repeating section (typically described by a offset-length-count triplet), the sections will be returned in a `List<T>`. Sometimes a record will have no instances of a particular section - in that case an empty list is returned. 
[Sample 5](Sample5.md) generates a report based on the SMF type 30 EXCP Section.

    - [Sample 6: A/B Reporting](Sample6.md)
[Sample 6](Sample6.md) Shows how to create an A/B report on SMF data, e.g. to compare statistics before and after a change. The sample collects statistics by program name, and reports:
        - CP Time
        - zIIP Time
        - zIIP on CP Time
        - EXCP count
        - Change in zIIP% of total CPU time
        - Change in zIIP on CP% of total CPU time
        - Change in CPU milliseconds per I/O