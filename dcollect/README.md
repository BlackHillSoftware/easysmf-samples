# DCOLLECT Samples

These samples demonstrate various DCOLLECT reports. 
Java provides a fast and flexible way to process DCOLLECT data.

## DCOLLECT to JSON

See [DCOLLECT to JSON](../dcollect2json/) for a sample to generate JSON from DCOLLECT records

## Simple DCOLLECT reports

### Space by storage group

The report shows overall statistics for the storage group, followed by 
details of the volumes in each storage group.

[StorageGroups.java](./src/main/java/com/smfreports/dcollect/StorageGroups.java)

### Space by high level qualifier

Report Level 0, Level 1 and Level 2 space by high level qualifier using
Active Dataset and Migrated Dataset DCOLLECT records.

[MbByHlq.java](./src/main/java/com/smfreports/dcollect/MbByHlq.java)

### Datasets by last reference

Report DASD dataset statistics by time since last reference e.g. last 7 days, last month, last year, last 5 years.

```
Last Ref    Count   Alloc MB    Used MB
P5Y          1110    11829.6     2107.7
P1Y          2522    25589.4    14595.2
P6M           339    13042.4     3759.1
P1M           694    42279.2    14806.5
P7D           500    71510.7    22099.5
P0D           224    16177.5     2504.2
```
[DatasetsByLastRef.java](./src/main/java/com/smfreports/dcollect/DatasetsByLastRef.java)

### Aged datasets

Report datasets on DASD with a last reference date older than a cutoff value.

[AgedDatasets.java](./src/main/java/com/smfreports/dcollect/AgedDatasets.java)

### Datasets by last migrated date

Report migrated dataset statistics by time since migrated e.g. last 7 days, last month, last year, last 5 years.

[DatasetsByMigratedDate.java](./src/main/java/com/smfreports/dcollect/DatasetsByMigratedDate.java)

### Frequently migrated datasets

Finds datasets that were migrated in the last 3 months and have been migrated 3 times or more, and lists the top 100 by migration frequency (total migration count/time since created).

[MigrationFrequency.java](./src/main/java/com/smfreports/dcollect/MigrationFrequency.java)

### zEDC by HLQ

Lists zEDC compression statistics by high level qualifier.

[ZedcByHlq.java](./src/main/java/com/smfreports/dcollect/ZedcByHlq.java)

## Compare 2 DCOLLECT runs

DCOLLECT shows you the status at a point in time, but it can be useful to see what has changed between runs. If there has been a big change in free space, what caused it?

These reports can help you pinpoint the cause.

### Change by Storage Group

Reports the change in space usage for each storage group between 2 DCOLLECT runs.

```
Storage Group : DBCGSG                        
            Volumes     Tot MB    Free MB      Used%
      A:          1      4,377      2,388       45.4
      B:          1      4,377      2,388       45.4
 Change:         +0         +0         +0       +0.0

Storage Group : SG1                           
            Volumes     Tot MB    Free MB      Used%
      A:         10     19,659     10,026       49.0
      B:         10     19,659     11,205       43.0
 Change:         +0         +0     +1,179       -6.0
```

[DeltaStorageGroups.java](./src/main/java/com/smfreports/dcollect/DeltaStorageGroups.java)

### Change by High Level Qualifier

Reports the top 50 high level qualifiers by change in space usage between 2 DCOLLECT runs.

The datasets to be included can be filtered as required, e.g. to only report datasets in a particular storage group.
```
HLQ         Count A    Count B     Change         MB A         MB B    Change MB
ANDREWR         104        102         -2        6,594        5,442       -1,152
UTEST01           3          0         -3           32            0          -32
MVS1              7         10         +3           34           40           +6
```

[DeltaHlq.java](./src/main/java/com/smfreports/dcollect/DeltaHlq.java)

### Change by Dataset

Reports the top 100 datasets by change in space usage between 2 DCOLLECT runs.

The datasets to be included can be filtered as required, e.g. to only report datasets in a particular storage group or with a particular prefix.

GDG generations are masked so that all generations are reported as one entry.

```
Dataset                                              MB A         MB B    Change MB
ANDREWR.JVM.ANDREWR5.D231005.T191708.X001           1,109            0       -1,109
ANDREWR.DCOLLECT.OUTPUT2                                0          101         +101
UTEST01.EASYSMF.V1R9M8.INSTALL                         16            0          -16
UTEST01.EASYSMF.INSTALL.XMI                            16            0          -16
MVS1.SMF.RECORDS.G####V##                              34           40           +6
```

[DeltaDatasets.java](./src/main/java/com/smfreports/dcollect/DeltaDatasets.java)

## JCL

JCL to run the reports is provided:

* Run from compiled jar files under JZOS : [RUNJZOS.jcl](./JCL/RUNJZOS.jcl)
* Run from source code under Java 11 : [J11BPXB.txt](./JCL/J11BPXB.txt)

Running from source code under Java 11 is recommended, because several samples contain filter steps that can be modified between runs of the program. The Java 11 single file source code feature means that you can modify and run the programs without a separate compile step.