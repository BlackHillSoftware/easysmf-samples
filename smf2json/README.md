# smf2json

These samples demonstrate converting SMF data to JSON format.

## Dependencies

The JSON conversion uses **easysmf-json** to produce JSON from the EasySMF SMF data classes.

Easysmf-json in turn uses [Gson](https://github.com/google/gson) to write the JSON text.

If you build the project using Maven the dependencies will be resolved automatically and placed in the output directory:

```$ mvn clean package```

If you import the Maven project into Eclipse the dependencies should also be resolved.

If you want to build and run the project without using Maven you need the **easysmf-json** and **gson** jars in the classpath e.g. easysmf-json-2.0.1.jar, gson-2.9.1.jar

## Basic Usage

Samples to show simple usage are located here: [Basic Usage](./src/main/java/com/smfreports/json/)

## SMF 2 JSON Command Line Interface (Smf2JsonCLI)

All these programs have a common structure - they read SMF data and use EasySMF-JSON to write JSON.

To reduce the amount of duplicated code, EasySMF-JSON provides the Smf2JsonCLI class. Smf2JsonJCLI provides a framework to:

- Specify the source of data (file or z/OS DDNAME) and read records
- Select records based on a time range
- Specify the destination for the JSON data (file, z/OS DDNAME or stdout)
- Convert data to JSON and write it to the selected destination

All programs using Smf2JsonJCLI need to do is:

- Receive the SMF records as they are read
- Return Objects (SMF records, SMF record sections or objects built from the data in the SMF record) to be converted to JSON.

## CICS Reports

Samples are provided to write [CICS SMF data in JSON format](./src/main/java/com/smfreports/json/cics/).

Samples include:

- CICS Statistics Records to JSON
- CICS Exception records to JSON
- CICS Transactions   
  Write all information for CICS transactions to JSON.
  Transactions can optionally be selected by:
  - Elapsed time. Only transactions with an elapsed time greater than a specified number of milliseconds will be included.
  - Abended. Only transactions with values in fields ABCODEC or ABCODEO will be included.
- CICS transaction summary   
  Create a minute by minute summary of CICS transaction data, grouped by transaction, program, APPLID etc. This data is designed to be further summarized by reporting programs that process JSON data so you could e.g. report by hour, applid, transaction, program...