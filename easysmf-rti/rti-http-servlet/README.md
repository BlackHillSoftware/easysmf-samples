# smf-servlet

This project provides a very simple web server and Java servlet to demonstrate that the http functions in the other EasySMF-RTI projects
are working.

It does nothing with the data other than print some information to show
that it was received. It does not include authentication or https support.

Note: The http sending functions **do** support https if a https URL is specified.

## Build and Run

Set the path to the EasySMF key file:
```
export EASYSMFKEY=/home/andrew/easysmfkey.txt
```
or
```
set EASYSMFKEY=C:\Users\Andrew\Documents\easysmfkey.txt
```

To start the server, change to the directory containing pom.xml and start the jetty-maven-plugin using Maven:
```
mvn jetty:run -Djetty.http.port=9999
```
This will build the project and start the Maven Jetty server, listening on port 9999.
Change the port as required, or accept the default port 8080.

Run the EasySMF-RTI projects specifying your IP address in the URL e.g.
```
http://192.168.12.34:9999/easysmf
```

Firewalls permitting, you should see the data arriving at the server.

