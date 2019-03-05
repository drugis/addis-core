Setting up ADDIS 2 with dependencies
====================================

To run the addis-core application locally the following subsystems need to also be set up and run:

- [Jena-ES (study data repository)](https://github.com/drugis/jena-es/blob/master/README.md)
- [RabbitMQ message queue](https://github.com/drugis/patavi/blob/master/README.md). 
- [Patavi server (R task communication hub)](https://github.com/drugis/patavi/blob/master/server/docker/README.md)
- [Patavi worker image (R task worker base)](https://github.com/drugis/patavi/blob/master/worker/docker/README.md). Note that it is not necessary to build and run an example worker.
- [Patavi worker for GeMTC](https://github.com/drugis/gemtc-web/blob/master/README.md)
- [Patavi worker for MCDA](https://github.com/drugis/mcda-elicitation-web/blob/master/README.md)

And finally, once these systems are up and running, [addis-core](https://github.com/drugis/addis-core/blob/master/README.md)

Windows-specific tips (also potentially relevant for older MacOS builds)

- edit windows hosts file to redirect docker machine host IP (default is 192.168.99.100) to localdocker.com
- create (or receive from us) private CA to self-sign certificates for the local machines
- create (or receive from us) key/cert pair for CN=localdocker.com, signed by private CA
- alternative: CN=direct IP of docker platform host; may vary per installation

When linking containers together rather than having traffic go via direct IP it's necessary to generate SSL certificates with as CN whatever name the link has in your docker run command.