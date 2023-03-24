## Sample 4: Group and Summarize SMF data

[Sample 4 Source Code: sample4.java](./src/main/java/Sample4.java)

Sample 4 shows how group and summarize SMF data in Java.

The program reports statistics for each program name, taken from SMF type 30 subtype 4 (Step End) records.
Data does not need to be sorted, and the program information is collected in a `java.util.HashMap<>`. This means that CPU usage will scale approximately linearly with the amount of data processed, and memory requirements will depend on the number of different program names encountered.

Statistics for each program are collected in a class called **ProgramStatistics**.
There is an instance of the class for each program, kept in the HashMap with the program name as the key.

```
Map<String, ProgramStatistics> programs = new HashMap<String, ProgramStatistics>();
```

### Processing the Data

We read and process the records the same way as in Sample 1.

We attempt to get an existing entry from the Map for the program name. If it is not present, a new instance is created and added to the map with the corresponding key. 

```
String programName = r30.identificationSection().smf30pgm();
ProgramStatistics program = programs.get(programName);
                
if (program == null)
{
    program = new ProgramStatistics();
    programs.put(programName, program);
}            
```

### Gathering Statistics

We then accumulate the information from the record into the ProgramStatistics entry:
```
program.accumulateData(r30);
```

```
private static class ProgramStatistics
{
    ...
    public void accumulateData(Smf30Record r30)
    {            
        if (r30.processorAccountingSection() != null)
        {
            count++;
            cpTime += r30.processorAccountingSection().smf30cptSeconds()
                    + r30.processorAccountingSection().smf30cpsSeconds();
            ziipTime += r30.processorAccountingSection().smf30TimeOnZiipSeconds();   
            normalizedZiipTime += 
                r30.processorAccountingSection().smf30TimeOnZiipSeconds() 
                    * r30.performanceSection().smf30snf() / 256;
        }
        if (r30.ioActivitySection() != null)
        {
            excps += r30.ioActivitySection().smf30tex();
            connectTime += r30.ioActivitySection().smf30aicSeconds();
        }
    }
    ...
}
```

### Writing the Report

We use Java Streams again to write the report:
```
programs.entrySet().stream()
    .sorted((a, b) -> Double.compare(b.getValue().cpTime, a.getValue().cpTime))
    .limit(100)
    .forEachOrdered(program ->
    {
        ProgramStatistics programinfo = program.getValue();
        System.out.format("%-8s %,8d %14s %14s %14s %,14d %14s %14s %14s %,14d %14.3f%n", 
            program.getKey(),
            programinfo.count, 
            ...
    });
```
- stream the entries from the map,
- sort by total CP Time (reversing a and b to sort descending)
- take the first 100 entries
- print the statistics.

**forEachOrdered(...)** guarantees that the order of the entries is maintained - which is important after the sort. 
**forEach** does not guarantee that the order is maintained - although in simple cases it seems to be.  

The principles used in this sample can be adapted to many different situations, simply by changing the data accumulated in the Statistics class and the data used for the key in the Map.

[Sample 4 Source Code: sample4.java](./src/main/java/Sample4.java)