Setting up ADDIS 2 with dependencies
====================================

This tutorial assumes you use the default values used in the readmes. To run the addis-core application locally you  need to go through the following readmes in order, in order to set up all necessary components:

Jena-es:
--------------
[/jena-es/README.md](https://github.com/drugis/jena-es/blob/master/README.md)

Patavi:
--------------
For Patavi there are three readmes to go through. The only important thing in this file is how to get an instance of RabbitMQ running.

[/patavi/README.md](https://github.com/drugis/patavi/blob/master/README.md)

Next you will need to get the Patavi server running.


[/patavi/server/docker/README.md](https://github.com/drugis/patavi/blob/master/server/docker/README.md)

And for the last Patavi readme, you will get a worker up and running. Please note that it is not necessary to build and run an example worker.


[/patavi/worker/docker/README.md](https://github.com/drugis/patavi/blob/master/worker/docker/README.md)

Patavi workers
----------

You will need to set up the Patavi workers for GeMTC and MCDA:
 - [/gemtc-web/README.md](https://github.com/drugis/gemtc-web/blob/master/README.md)
 - [/mcda-elicitation-web/README.md](https://github.com/drugis/mcda-elicitation-web/blob/master/README.md)

Addis-core:
--------------
In order to go through the next readme, it is necessary that Jena-es, Patavi and the Patavi workers are running. 
[/addis-core/README.md](https://github.com/drugis/addis-core/edit/master/README.md)

Other
--------------
Commands to run everything from your root repository, assuming default settings, and having run the docker containers before:
```
docker restart my-rabbit
docker restart amqp-gemtc
docker restart patavi-server-amqp
(cd jena-es && mvn spring-boot:run) # (or run from docker?)
(cd addis-core && tomcat7:run -Dmaven.tomcat.port=8090 -Djavax.net.ssl.trustStore=ssl/truststore.jks)
```

It might be useful to link to gemtc-web or mcda-web from addis-core, so when you change something in gemtc-web you don't need to do a bower update. Use `yarn link` for this.
