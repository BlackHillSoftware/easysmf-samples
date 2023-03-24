## Sample 6: A/B Reporting

[Sample 6 Source Code: sample6.java](./src/main/java/Sample6.java)

Sample 6 creates an A/B report showing changes in statistics by program name before and after a specific date.

This could be used to see the effect of a processor upgrade, zIIP configuration change etc.

The report shows statistics for the top 100 programs by sample A CP time where there is a matching sample B entry.

### Gathering Statistics

Statistics are collected in the same way as [Sample 4](Sample4.md), but 2 Maps are used to collect A and B (before/after) data separately.

```
Map<String, ProgramStatistics> aPrograms = new HashMap<String, ProgramStatistics>();
Map<String, ProgramStatistics> bPrograms = new HashMap<String, ProgramStatistics>();

```
We use a simple routine to decide whether a particular record belongs to group A or B:
```
private static boolean isA(Smf30Record r30)
{
    final LocalDateTime boundary = LocalDateTime.of(2019, 05, 24, 0, 0);
    return r30.smfDateTime().isBefore(boundary);       
}
```
and select the A or B group based on the result:
```
Map<String, ProgramStatistics> target = isA(r30) ? aPrograms : bPrograms;

```

Then accumulate the data:
```
ProgramStatistics program = target.get(programName);
if (program == null)
{
    program = new ProgramStatistics(programName);
    target.put(programName, program);
}            
program.accumulateData(r30);
```

### Writing the Report

Again, Java Streams are used to write the report. The 2 maps relating program name to program statistics are **aPrograms** and **bPrograms**. 

We added **get** methods to the **ProgramStatistics** class, which simplifies the Streams code because we can refer to these methods directly.

```
double getCpTime() {
    return cpTime;
}
```

Stream **aPrograms**, get the ProgramStatistics entries, exclude any without matching bPrograms entries, sort them by CP time descending and take the top 100:

```
aPrograms.entrySet().stream()
    .map(entry -> entry.getValue())
    .filter(aProgramsEntry -> bPrograms.containsKey(aProgramsEntry.getName()))
    .sorted(comparing(ProgramStatistics::getCpTime).reversed())
    .limit(100)
    ...
```

For each program, we write 3 lines:
- A statistics
- B statistics
- The precentage change in values

```
    ...
    .forEachOrdered(programAInfo ->
    {
        // Get matching B statistics
        ProgramStatistics programBInfo = bPrograms.get(programAInfo.getName());
                
        System.out.format("%-8s%n", programAInfo.getName()); // Program name
                
        System.out.format(detailFormatString, 
            "A:",
            programAInfo.getCount(), 
            hhhmmss(programAInfo.getCpTime()), 
            hhhmmss(programAInfo.getZiipTime()),
            // etc
            ...
        // Repeat for programBinfo
```
Then the differences:
```
        System.out.format(changeFormatString, 
            "Change:",
            programBInfo.getZiipPctChange(programAInfo)
                .map(value -> String.format("%+.1f%%",value))
                .orElse(""),
            programBInfo.getZiipOnCpPctChange(programAInfo)
                .map(value -> String.format("%+.1f%%",value))
                .orElse(""),
            programBInfo.getCpuMsPerIOChange(programAInfo)
                .map(value -> String.format("%+.0f%%",value))
                .orElse("") );
```

#### Optional, orElse()

The report uses **java.util.Optional**, introduced in Java 8. Optional allows for values that might not exist, and gives the **orElse()** construct to handle non-existent values.

This report has several elements that might not exist or might not make any sense. CPU milliseconds per I/O can't be calculated if the I/O count is zero. The change in percentage values just clutter the report if both percentages are zero.

The methods that calculate these elements return an Optional value The output code formats the value if it exists, **orElse** inserts an empty string in its place.

[Sample 6 Source Code: sample6.java](./src/main/java/Sample6.java)