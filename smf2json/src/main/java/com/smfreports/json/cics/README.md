# Converting CICS SMF data to JSON format

These sample programs convert a number of different types of CICS SMF data to JSON format.
The JSON data can be processed further using other reporting tools that process JSON, e.g. Splunk.

All programs use the Smf2JsonCLI class, which provides a command line interface to handle reading SMF data and creating and writing JSON using the EasySMF-JSON functions.

Smf2JsonCLI can read SMF data from files or z/OS DD names. Output can be written to a file, to a z/OS DD or to stdout. The program can run on z/OS as a batch job using the JZOS batch launcher or BPXBATCH, or on any Java platform where the SMF data is available.

## CICS Dictionaries

CICS transaction reports require a dictionary to interpret the SMF records.

The CICS dictionary records need to be read before the transaction records. The simplest way to do that is to have a separate file/dataset with the dictionary records, and concatenate it ahead of the transaction data if using JCL or list the dictionary file before the transaction data file on the command line.

## Reports

The following sample reports are provided:

### CicsTransactions

List detailed transaction information similar to that provided by DFH$MOLS, but in JSON format.
Optionally select by
- SMF record time
- APPLID
- Transaction ID
- Elapsed time greater than a specified value
- Transactions that abended (fields ABCODEC or ABCODEO contain data)

### CicsTransactionSummary

Create a minute by minute summary of CICS transaction data, suitable for further processing by JSON reporting tools. This can provide the basis of many different transaction reports, with granularity down to a 1 minute interval

Data is grouped by a combination of the following fields:

* SMFMNPRN - Generic APPLID
* SMFMNSPN - Specific APPLID
* minute - Minute from the STOP time
* TRAN - Transaction identification
* TTYPE - Transaction start type
* RTYPE - Performance record type
* PGMNAME - Name of the first program
* SRVCLSNM - WLM service class name
* RPTCLSNM - WLM report class name
* TCLSNAME - Transaction class name

Fields can be added or deleted from the group key as required. The amount of output data increases based on the resulting number of groups, but the flexibility of the reporting also increases.

### CicsExceptions

Convert CICS Exception SMF records to JSON format.

### CicsStatistics

Convert CICS Statistics records to JSON format.

### CicsTransactionSummaryCustom

Similar to **CicsTransactionSummary**, except that transaction summary information is collected in a user provided *TransactionData* class instead of the supplied *CicsTransactionGroup* class.

The TransactionData class can be customized to control which data is collected. Collecting a smaller number of fields improves performance of the program.

## Running the Programs

### On z/OS

JCL to run on z/OS can be found in the [JCL](../../../../../JCL) directory.

- [CICSEXEP](../../../../../../../JCL/CICSEXEP.jcl) convert CICS exception records to JSON
- [CICSSTAT](../../../../../../../JCL/CICSSTAT.jcl) convert CICS statistics records to JSON
- [CICSTRAN](../../../../../../../JCL/CICSTRAN.jcl) convert CICS CMF transaction records to JSON

To run:

1. Build the Smf2Json project if required, and copy the jar files from the target directory to a directory on z/OS (using binary transfer options).
2. Set the **APPHOME** variable in the JCL to the location of the jar files
3. Set the **INDSN** and **OUTDSN** variables as required 
4. Check the **JAVAHOME** variable and update if required
5. Run the job

### On Windows/Linux:

1. Build the Smf2Json project if required
2. Run the Java program, specifying the class as required:   

   ```
   java -cp target/* com.smfreports.json.cics.CicsTransactionSummary --out json.txt <input1> <input2> ...   
   ```
   
   where \<input1\> \<input2\> ... are SMF data files. For CICS transactions reports, CICS dictionary records must be read before the CICS transaction records.
