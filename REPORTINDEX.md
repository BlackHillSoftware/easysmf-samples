# Report index

This index provides descriptions and links to the sample reports in this repository.

---

## Tutorial

For getting started information and samples, see the [tutorial](tutorial).

---

## easysmf-skeleton

Minimal EasySMF:JE project: [easysmf-skeleton](easysmf-skeleton).

| Description | Source |
|-------------|--------|
| Skeleton for a program to read SMF records. | [EasySmfSkeleton.java](easysmf-skeleton/src/main/java/com/smfreports/sample/EasySmfSkeleton.java) |

---

## SMF utilities

| Description | Source |
|-------------|--------|
| Counts and sizes by SMF type and subtype. | [RecordCount.java](sample-reports/src/main/java/com/smfreports/RecordCount.java) |
| Search raw SMF data for a string; print time, system, type, subtype. | [SmfTextSearch.java](sample-reports/src/main/java/com/smfreports/SmfTextSearch.java) |
| Report duplicated SMF data by minute and record type, from 1 or more input files. | [SmfReportDups.java](smf-report-dups/src/main/java/com/smfreports/SmfReportDups.java) |
| Copy SMF data to a new file with duplicate records removed, optionally writing duplicate records to another file. | [SmfDeDup.java](smf-de-dup/src/main/java/com/smfreports/SmfDeDup.java) |

---

## SMF type 30 Job Accounting

| Description | Source |
|-------------|--------|
| Compare program-level statistics from step-end records before and after a change. | [BeforeAfterProgramStatistics.java](sample-reports/src/main/java/com/smfreports/type30/BeforeAfterProgramStatistics.java) |
| List jobs whose total CPU time exceeds 60 seconds. | [CpuGt60.java](sample-reports/src/main/java/com/smfreports/type30/CpuGt60.java) |
| Search for jobs with different job names but the same JES2 calculated JCL ID value | [JobsByJclId.java](sample-reports/src/main/java/com/smfreports/type30/JobsByJclId.java) |
| Search for jobs with different job names but the same JES2 calculated JCL ID value. For entries with more than one job name, print job details including system, time and job id | [JobsByJclIdDetail.java](sample-reports/src/main/java/com/smfreports/type30/JobsByJclIdDetail.java) |
| Job statistics by job name. | [JobsByJobname.java](sample-reports/src/main/java/com/smfreports/type30/JobsByJobname.java) |
| CPU and MSU usage by job name from type 30 records, rolled up by month, day, or hour. | [CpuByJobname.java](sample-reports/src/main/java/com/smfreports/type30/CpuByJobname.java) |
| Statistics by program name from step-end records. | [ProgramNameStatistics.java](sample-reports/src/main/java/com/smfreports/type30/ProgramNameStatistics.java) |
| CPU and MSU usage by program name from type 30 records, rolled up by month, day, or hour. | [CpuByProgramName.java](sample-reports/src/main/java/com/smfreports/type30/CpuByProgramName.java) |
| Report the jobs/tasks that used the most CPU by job name during prime shift for each day of the week. | [PrimeShiftTopJobs.java](sample-reports/src/main/java/com/smfreports/type30/PrimeShiftTopJobs.java) |
| Create a report based on the flags introduced in APAR OA53355 : USERKEY COMMON MIGRATION SUPPORT. | [UserKeyCommon.java](sample-reports/src/main/java/com/smfreports/type30/UserKeyCommon.java) |
| Find jobs where there are instances of the same jobname with and without zEDC compression, and compare elapsed time, CPU and I/O statistics for the job with and without zEDC. | [ZedcBeforeAfter.java](sample-reports/src/main/java/com/smfreports/zedc/ZedcBeforeAfter.java) |
| List compression statistics for jobs using zEDC compression. | [ZedcByJob.java](sample-reports/src/main/java/com/smfreports/zedc/ZedcByJob.java) |
| List the jobs that used the most CPU time on each system in the 4 hours up to and including the top 5 4HRA MSU peaks. | [PeakR4HAJobs.java](sample-reports/src/main/java/com/smfreports/r4ha/PeakR4HAJobs.java) |

---

## RMF SMF types 70 and 72 (CPU, WLM)

| Description | Source |
|-------------|--------|
| CPU by LPAR from type 70 subtype 1, rolled up by month, day, or hour. | [CpuByLpar.java](sample-reports/src/main/java/com/smfreports/rmf/CpuByLpar.java) |
| CPU by Service Class from type 72 subtype 3, rolled up by month, day, or hour. | [CpuByServiceClass.java](sample-reports/src/main/java/com/smfreports/rmf/CpuByServiceClass.java) |
| Calculate the combined MSU values for selected systems, and reports the intervals with the highest values for both LAC (long term average) and interval MSU. Systems can be on the same CPC or on different CPCs. | [CombinedLparMSU.java](sample-reports/src/main/java/com/smfreports/rmf/CombinedLparMSU.java) |
| Performance Index from type 72. | [PerformanceIndex.java](sample-reports/src/main/java/com/smfreports/rmf/PerformanceIndex.java) |
| Prime-shift daily CPU from type 70. | [PrimeShiftDailyCpu.java](sample-reports/src/main/java/com/smfreports/rmf/PrimeShiftDailyCpu.java) |
| Calculate zIIP velocity from type 72 subtype 3. | [ZiipVelocity.java](easysmf-rti/rti-smt-switch/src/main/java/com/smfreports/sample/ZiipVelocity.java) |

---

## SMF type 110 - CICS

| Description | Source |
|-------------|--------|
| CICS file statistics (SMF 110 statistics subtype). | [CicsFileStatistics.java](sample-reports/src/main/java/com/smfreports/cics/CicsFileStatistics.java) |
| Filter CICS transaction records to extract a subset of records. | [FilterRecords.java](sample-reports/src/main/java/com/smfreports/cics/FilterRecords.java) |
| Search for CICS transactions matching various criteria. | [CicsTransactionSearch.java](sample-reports/src/main/java/com/smfreports/cics/CicsTransactionSearch.java) |
| Summarize CICS transaction statistics. | [CicsTransactionSummary.java](sample-reports/src/main/java/com/smfreports/cics/CicsTransactionSummary.java) |
| Find CICS dictionary records and copy them to a new file. | [ExtractDictionaries.java](sample-reports/src/main/java/com/smfreports/cics/ExtractDictionaries.java) |
| CICS transaction response time by APPLID and TRAN, allowing for terminal waits in conversational transactions. | [CicsTransactionResponse.java](CICS/cics-transaction-stats/source/CicsTransactionResponse.java) |
| Print statistics for CICS transactions by Transaction, APPLID and Service Class. | [CicsServiceClass.java](sample-reports/src/main/java/com/smfreports/cics/CicsServiceClass.java) |

---

## SMF type 119 - TCP/IP  & zERT

| Description | Source |
|-------------|--------|
| zERT detail records (subtype 11) to JSON. | [ZertToJson.java](sample-reports/src/main/java/com/smfreports/tcpip/ZertToJson.java) |
| List certificates used, certificate expiry date and the last time each certificate was used by job name from zERT detail records. | [ZertCertificatesByJobname.java](sample-reports/src/main/java/com/smfreports/tcpip/ZertCertificatesByJobname.java) |

---

## SMF types 14–18, 61–65, and 15 - Dataset Activity

| Description | Source |
|-------------|--------|
| Dataset and VSAM activity from types 14–18 and 61–65 (type 14 when flagged for TDS). | [DatasetActivity.java](sample-reports/src/main/java/com/smfreports/dataset/DatasetActivity.java) |
| Search dataset update (type 15) records for a data set name. | [SmfSearch.java](sample-reports/src/main/java/com/smfreports/dataset/SmfSearch.java) |
| List compression statistics for datasets written using zEDC compression. | [ZedcByDataset.java](sample-reports/src/main/java/com/smfreports/zedc/ZedcByDataset.java) |
| Investigate a performance problem in zEDC compressed datasets. This program searches SMF 15 records for compressed datasets where SMF14CDL (compressed bytes written) is greater than SMF14CDS (compressed data size) and lists the results, along with the job and program names. | [ZedcWriteRatio.java](sample-reports/src/main/java/com/smfreports/zedc/ZedcWriteRatio.java) |

---

## Convert SMF records to JSON format

### smf2json

SMF to JSON samples using **Smf2JsonCLI** and related APIs: [smf2json](smf2json).

| Description | Source |
|-------------|--------|
| Sample program showing usage of Smf2JsonCLI class. | [Smf2JsonCLISample.java](smf2json/src/main/java/com/smfreports/json/Smf2JsonCLISample.java) |
| Demonstrates writing information from SMF records to JSON format, mixing specific fields and whole sections. | [Smf30JsonJobList.java](smf2json/src/main/java/com/smfreports/json/Smf30JsonJobList.java) |
| This sample demonstrates writing a complete SMF record to JSON format. | [Smf30RecordToJson.java](smf2json/src/main/java/com/smfreports/json/Smf30RecordToJson.java) |

---

### smf2json-skeleton

Starter project for **Smf2JsonCLI**: [smf2json-skeleton](smf2json-skeleton).

| Description | Source |
|-------------|--------|
| Minimal `Smf2JsonCLI` skeleton (default includes type 30). | [Sample.java](smf2json-skeleton/src/main/java/com/smfreports/json/sample/Sample.java) |

---

### SMF type 30

| Description | Source |
|-------------|--------|
| Step and job end sections to JSON. | [StepAndJobEnd.java](smf2json/src/main/java/com/smfreports/json/smf30/StepAndJobEnd.java) |


### SMF type 110 (CICS)

| Description | Source |
|-------------|--------|
| CICS statistics records to JSON. | [CicsStatistics.java](smf2json/src/main/java/com/smfreports/json/cics/CicsStatistics.java) |
| CICS exception records to JSON. | [CicsExceptions.java](smf2json/src/main/java/com/smfreports/json/cics/CicsExceptions.java) |
| Filtered CICS transactions to JSON. | [CicsTransactions.java](smf2json/src/main/java/com/smfreports/json/cics/CicsTransactions.java) |
| Minute by minute CICS transaction summary to JSON. | [CicsTransactionSummary.java](smf2json/src/main/java/com/smfreports/json/cics/CicsTransactionSummary.java) |
| Minute by minute CICS transaction summary JSON specifying fields explicitly for improved performance compared with collecting all fields. | [CicsTransactionSummaryCustom.java](smf2json/src/main/java/com/smfreports/json/cics/CicsTransactionSummaryCustom.java) |

### SMF type 98 - subtype 1 (z/OS sections)

| Description | Source |
|-------------|--------|
| ASC execution efficiency sections to JSON. | [AscExecutionEfficiency.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/AscExecutionEfficiency.java) |
| ASC spinlock data to JSON. | [AscSpinlock.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/AscSpinlock.java) |
| ASC work unit sections to JSON. | [AscWorkUnit.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/AscWorkUnit.java) |
| Environment sections to JSON. | [EnvironmentSections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/EnvironmentSections.java) |
| ECCC sections to JSON. | [EcccSections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/EcccSections.java) |
| Local-CML lock detail to JSON. | [LocalCmlLockDetailSections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/LocalCmlLockDetailSections.java) |
| Spin lock detail to JSON. | [SpinLockDetailSections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/SpinLockDetailSections.java) |
| Spin lock summary to JSON. | [SpinLockSummarySections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/SpinLockSummarySections.java) |
| Suspended lock detail to JSON. | [SuspLockDetailSections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/SuspLockDetailSections.java) |
| Suspended lock summary to JSON. | [SuspLockSummarySections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/SuspLockSummarySections.java) |
| Utilization sections to JSON. | [UtilizationSections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/UtilizationSections.java) |
| Workload unit priority bucket sections to JSON. | [WuPriorityBucketSections.java](smf2json/src/main/java/com/smfreports/json/smf98/zos/WuPriorityBucketSections.java) |
| Whole type 98 subtype 1 records to JSON. | [Smf98Subtype1Records.java](smf2json/src/main/java/com/smfreports/json/smf98/Smf98Subtype1Records.java) |

### SMF type 98 - subtype 1024 (CICS sections)

| Description | Source |
|-------------|--------|
| WIC bucket 1 sections to JSON. | [WicBucket1.java](smf2json/src/main/java/com/smfreports/json/smf98/cics/WicBucket1.java) |
| WIC bucket 2 sections to JSON. | [WicBucket2.java](smf2json/src/main/java/com/smfreports/json/smf98/cics/WicBucket2.java) |
| Exceptional Jobs section (CICS) to JSON. | [ExceptionalJobs.java](smf2json/src/main/java/com/smfreports/json/smf98/cics/ExceptionalJobs.java) |
| Whole type 98 subtype 1024 records to JSON. | [Smf98Subtype1024Records.java](smf2json/src/main/java/com/smfreports/json/smf98/Smf98Subtype1024Records.java) |

### SMF type 98 - subtype 1025 (IMS sections)

| Description | Source |
|-------------|--------|
| Aggregate CPU sections to JSON. | [AggregateCpuSections.java](smf2json/src/main/java/com/smfreports/json/smf98/ims/AggregateCpuSections.java) |
| Aggregate statistics sections to JSON. | [AggregateStatisticsSections.java](smf2json/src/main/java/com/smfreports/json/smf98/ims/AggregateStatisticsSections.java) |
| Exceptional Jobs section (IMS) to JSON. | [ExceptionalJobs.java](smf2json/src/main/java/com/smfreports/json/smf98/ims/ExceptionalJobs.java) |
| Whole type 98 subtype 1025 records to JSON. | [Smf98Subtype1025Records.java](smf2json/src/main/java/com/smfreports/json/smf98/Smf98Subtype1025Records.java) |

---

## EasySMF RTI

Read data from the z/OS SMF Real Time Interface: [easysmf-rti](easysmf-rti).

| Description | Source |
|-------------|--------|
| Demonstrate using the SMF Real Time Interface. Print date/time, record type, and size for each record as it is received. | [RtiSimple.java](easysmf-rti/rti-simple/src/main/java/com/smfreports/sample/RtiSimple.java) |
| Read data from a SMF in memory resource and send the complete SMF records in binary format over HTTP. | [RtiHttpBinary.java](easysmf-rti/rti-http-binary/src/main/java/com/smfreports/sample/RtiHttpBinary.java) |
| Read data from a SMF in memory resource, and post information from job end records to a HTTP server in JSON format. | [RtiHttpJson.java](easysmf-rti/rti-http-json/src/main/java/com/smfreports/sample/RtiHttpJson.java) |
| Jetty servlet to receive data for testing RtiHttpBinary and RtiHttpJson. | [RtiServlet.java](easysmf-rti/rti-http-servlet/src/main/java/com/smfreports/sample/RtiServlet.java) |
| Send SMS notifications for failed jobs from the mainframe using the EasySMF API for the SMF Real Time Interface and the Twilio Java API | [RtiNotifications.java](easysmf-rti/rti-notifications/src/main/java/com/smfreports/sample/RtiNotifications.java) |
| Track type 70 and 72 intervals and SMT-related metrics, and issue message based on zIIP Velocity (i.e. wait time for zIIP) that could be used to trigger turning SMT on or off. | [SmtSwitch.java](easysmf-rti/rti-smt-switch/src/main/java/com/smfreports/sample/SmtSwitch.java) |

---

## DCOLLECT 

| Description | Source |
|-------------|--------|
| Datasets with last reference older than a cutoff. | [AgedDatasets.java](dcollect/src/main/java/com/smfreports/dcollect/AgedDatasets.java) |
| Migrated datasets by time since migration. | [DatasetsByMigratedDate.java](dcollect/src/main/java/com/smfreports/dcollect/DatasetsByMigratedDate.java) |
| Datasets by time since last reference. | [DatasetsByLastRef.java](dcollect/src/main/java/com/smfreports/dcollect/DatasetsByLastRef.java) |
| Compare 2 DCOLLECT runs, showing the top 100 datasets by change in size. | [DeltaDatasets.java](dcollect/src/main/java/com/smfreports/dcollect/DeltaDatasets.java) |
| Compare 2 DCOLLECT runs, showing the top 50 HLQ by change in size. | [DeltaHlq.java](dcollect/src/main/java/com/smfreports/dcollect/DeltaHlq.java) |
| Compare 2 DCOLLECT runs, showing the change in space for each storage group. | [DeltaStorageGroups.java](dcollect/src/main/java/com/smfreports/dcollect/DeltaStorageGroups.java) |
| Frequently migrated datasets. | [MigrationFrequency.java](dcollect/src/main/java/com/smfreports/dcollect/MigrationFrequency.java) |
| Report Migration Level 0, Level 1 and Level 2 space by high level qualifier. | [MbByHlq.java](dcollect/src/main/java/com/smfreports/dcollect/MbByHlq.java) |
| Print details of SMF data, storage and management classes in JSON format. | [SmsClasses.java](dcollect/src/main/java/com/smfreports/dcollect/SmsClasses.java) |
| Space by storage group and volume. | [StorageGroups.java](dcollect/src/main/java/com/smfreports/dcollect/StorageGroups.java) |
| zEDC compressed dataset statistics by HLQ. | [ZedcByHlq.java](dcollect/src/main/java/com/smfreports/dcollect/ZedcByHlq.java) |
| Write DCOLLECT records to JSON, using different DDs for each record type. | [Dcollect2Json.java](dcollect2json/src/main/java/com/smfreports/dollect2json/Dcollect2Json.java) |

---

## Videos

Samples from Youtube videos.

### [How to Query CICS Transaction SMF Data using EasySMF:JE and Java Streams](https://www.youtube.com/watch?v=OhyJTJ0QN1I)

| Description | Source |
|-------------|--------|
| Query CICS transaction SMF data to find transactions that exceeded a specified elapsed time. | [Query01ElapsedTime.java](videos/query-cics-transactions/src/main/java/Query01ElapsedTime.java) |
| Query CICS transaction SMF data to find the top 10 transactions by elapsed time for each transaction name. | [Query02TopValues.java](videos/query-cics-transactions/src/main/java/Query02TopValues.java) |
| Query CICS transaction SMF data to find the first 100 long running transactions by start time between 2 times. | [Query03ByStartTime.java](videos/query-cics-transactions/src/main/java/Query03ByStartTime.java) |
| Query CICS transaction SMF data, counting or summing data fields. | [Query04CountingAndSumming.java](videos/query-cics-transactions/src/main/java/Query04CountingAndSumming.java) |
| Query CICS transaction SMF data from a SMF logstream on z/OS. | [Query05ReadLogstream.java](videos/query-cics-transactions/src/main/java/Query05ReadLogstream.java) |

---

## Build and Run

Build and run instructions are in the root [README.md](README.md).
