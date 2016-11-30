Overal readme for setting up addis and all necessary components
=====================================================

This tutorial assumes you use the default values used in the readmes. To run the addis-core application locally you  need to go through the following readmes in order, in order to set up all necessary components:

Jena-es:
--------------
/jena-es/README.md 

Patavi:
--------------
For Patavi there are three readmes to go through. The only important thing in this file is how to get an instance of RabbitMQ running.<br>
/patavi/README.md 

Next you will need to get the Patavi server running.<br>
/patavi/server/docker/README.md 

And for the last Patavi readme, you will get a worker up and running. Please note that it is not necessary to build and run an example worker.<br>
/patavi/worker/docker/README.md 
Gemtc-web:
--------------
For gemtc-web it is necessary that Patavi is running.
/gemtc-web/README.md

Addis-core:
--------------
In order to go through the next readme, it is necessary that Jena-es, Patavi and Gemtc-web are running. 
/addis-core/README.md 

Non-essential components
--------------
**Addis 1:**<br>
/addis/README.md

**Mcda:**<br>
/mcda-elicitation-web/README.md

**Trialverse:**<br>
/addis-core/trialverse/README.md

Other
--------------
Commands to run everything from your root repository, assuming default settings, and having run the docker containers before:<br>
*docker restart my-rabbit*<br>
*docker restart amqp-gemtc*<br>
*docker restart patavi-server-amqp*<br>
*jena-es/mvn spring-boot:run*<br>
*addis-core/tomcat7:run -Dmaven.tomcat.port=8090 -Djavax.net.ssl.trustStore=/home/joris/git/addis-core/ssl/truststore.jks*

It might be useful to create symlink to Gemtc-web from Addis-core, so when you change something in Gemtc-web you don't need to do a bower update for Addis-core this can be done using a command similar to:<br>
*ln -s <path>/addis-core/src/main/webapp/resources/app/js/bower_components/gemtc-web <path>/gemtc-web*