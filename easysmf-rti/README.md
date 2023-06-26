# EasySMF-RTI Samples

EasySMF-RTI is a Java package to read data from the z/OS SMF Real Time Interface.

The z/OS SMF Real Time Interface allows programs to read SMF records 
as they are written.
The interface is RACF protected and does not require exits or APF authorization.

These samples demonstrate various ways to use EasySMF-RTI to read and process
real time SMF data.

## rti-simple

[rti-simple](./rti-simple) is a simple demonstration of the core functions of the 
real time interface.

It connects to an in memory resource, reads records and prints the 
date/time, record type and length. It disconnects from the resource and exits 
after reading 100 records, or when a MVS STOP command is received.

## rti-http-binary

[rti-http-binary](./rti-http-binary) sends binary SMF records to a http(s) URL.

Multiple records can be combined into a single POST. The data is sent when there
are no records immediately available in the in memory resource, or when the POST
size exceeds a specified threshold.

You can test this sample using the rti-http-servlet project to receive the records.

This project requires Java 11 due to the use of the Java 11 HttpClient class.

## rti-http-json

[rti-http-json](./rti-http-json) sends job end records (SMF 30 subtype 5) in JSON format to a http(s) 
URL.

The program reads SMF 30 subtype 5 records from an in memory resource, formats 
the major sections into a JSON record and POSTs the JSON text to a http(s) URL.

You can test this sample using the rti-http-servlet project to receive the records.

This project requires Java 11 due to the use of the Java 11 HttpClient class.

## rti-notifications

[rti-notifications](./rti-notifications) sends SMS text message notifications for 
failed jobs using Twilio.

The program reads SMF 30 subtype 4 (step end) and 5 (job end) records from an in 
memory resource.

The job end records don't necessarily contain step failure information, so 
information about failed steps is saved waiting for the matching job end record. 
If the last step that executed (i.e. was not flushed) failed, a notification SMS 
message is sent using the Twilio API when the job end record is received.

## rti-http-servlet

[rti-http-servlet](./rti-http-servlet) is a project for testing the rti-http-binary 
and rti-http-json projects.

It uses the Jetty Maven plugin to start a simple web app to receive binary SMF data 
or JSON data from the rti-http projects. It receives the POST requests and prints 
information to the console.

The project isn't intended to be a "how-to" for creating a web application. It is 
just a test project to demonstrate that the data is being read and successfully 
sent over a http connection.