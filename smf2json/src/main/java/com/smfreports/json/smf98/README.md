# Converting SMF type 98 High Frequency Throughput Statistics (HFTS) data to JSON format

These samples convert data from SMF 98 records to JSON format. The JSON data can be processed further using other reporting tools that process JSON, e.g. Splunk.

All programs use the Smf2JsonCLI class, which provides a command line interface to handle reading SMF data and creating and writing JSON using the EasySMF-JSON functions.

Smf2JsonCLI can read SMF data from files or z/OS DD names. Output can be written to a file, to a z/OS DD or to stdout. The program can run on z/OS as a batch job using the JZOS batch launcher or BPXBATCH, or on any Java platform where the SMF data is available.

## Reports

* Smf98Subtype1Records
* Smf98Subtype1024Records
* Smf98Subtype1025Records

These samples convert a single record of each subtype to JSON format using JSON "pretty printing" which illustrates the structure of the records and makes it easier to see relationships between different section.

### [Subtype 1: z/OS (./zos/)](./zos/)

Provides samples to convert the sections in the z/OS HFTS record to JSON:

* Environmental Section
* Utilization Section
* ECCC Section
* Spin Lock Summary
* Spin Lock Detail
* Suspend Lock Summary
* Suspend Lock Detail
* Local and CML Lock Detail
* Work Unit Priority Buckets
* Address Space Consumption - Execution Efficiency
* Address Space Consumption - Work Units
* Address Space Consumption - Spin Locks

### [Subtype 1024: CICS (./cics/)](./cics/)

Provides samples to convert the sections in the CICS HFTS record to JSON:

* WIC Aggregate Bucket 1
* WIC Aggregate Bucket 2
* Exceptional Jobs

### [Subtype 1025: IMS (./ims/)](./ims/)

Provides samples to convert the sections in the IMS HFTS record to JSON:

* WIC Aggregate Statistics by Priority/job Size
* WIC Aggregate CPU Times by CPU Category/Priority/Job Size
* Exceptional Jobs
