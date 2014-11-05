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

Create a trialverse user with password 'develop'

	sudo -u postgres createuser -S -D -R trialverse
	sudo -u postgres psql -c "ALTER USER trialverse WITH PASSWORD 'develop'"

Configure environment variables (NB google secret/key assumes localhost:8090)

    export TRIALVERSE_DB_CHANGELOG=database.sql
    export TRIALVERSE_DB_DRIVER=org.postgresql.Driver
    export TRIALVERSE_DB_URL=jdbc:postgresql://localhost/trialverse
    export TRIALVERSE_DB_USERNAME=trialverse
    export TRIALVERSE_DB_PASSWORD=develop
    export TRIALVERSE_OAUTH_GOOGLE_SECRET=JMta9pPfckdE9GMnxKvTm3We
    export TRIALVERSE_OAUTH_GOOGLE_KEY=356525985053-j71rekspvj3ds507700srb8hl7955m32.apps.googleusercontent.com

Run tomcat:

    mvn tomcat7:run