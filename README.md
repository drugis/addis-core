TrialVerse
==========

The TrialVerse repository is currently home to a number of prototyping efforts
for the ADDIS 2 project, all relating to how the structured database of
clinical trials will be set up:

 - `dbms`: the database structure (the data, but not the semantic meta-data)
 - `src`: a prototype REST repository to serve the content of the DBMS.
 - `triplestore`: the triplestore setup (the semantic meta-data)
 - `sparql`: a prototype interface for searching / browsing ontologies
 - `importer`: imports ADDIS 1.x datafiles into the DBMS, and generates the
   meta-data

Running
-------

NOTE: the TrialVerse spring prototype is currently not being used, and may
not run against the most recent database schema. It is left here mainly as
documentation. See drugis/addis-core for the current stub implementation.

	sudo apt-get install openjdk-7-jdk maven postgresql postgresql-client postgresql-contrib-9.1 libsaxonb-java
	sudo update-alternatives --config java # choose java 7

	sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'develop'"
	sudo -u postgres createuser -S -D -R trialverse
	sudo -u postgres psql -c "ALTER USER trialverse WITH PASSWORD 'develop'"

	cd dbms
	./rebuild.sh
	cd ..

	mvn jetty:run
