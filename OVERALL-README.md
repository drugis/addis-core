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
