Trialverse
==========

The Trialverse repository is currently home to a number of prototyping efforts
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
	sudo -u postgres psql -c "CREATE DATABASE trialverse ENCODING 'utf-8' OWNER trialverse"

Configure environment variables (NB google secret/key assumes localhost:8090)

    export TRIALVERSE_DB_CHANGELOG=database.sql
    export TRIALVERSE_DB_DRIVER=org.postgresql.Driver
    export TRIALVERSE_DB_URL=jdbc:postgresql://localhost/trialverse
    export TRIALVERSE_DB_USERNAME=trialverse
    export TRIALVERSE_DB_PASSWORD=develop
    export TRIPLESTORE_BASE_URI=http://localhost:3030
    export TRIALVERSE_OAUTH_GOOGLE_SECRET=JMta9pPfckdE9GMnxKvTm3We
    export TRIALVERSE_OAUTH_GOOGLE_KEY=356525985053-j71rekspvj3ds507700srb8hl7955m32.apps.googleusercontent.com

Install the bower components:

  bower install

Run tomcat:

    mvn tomcat7:run

Note that there should be live fuseki endpoint referenced by the triplestore.

Release notes
==========

Release 7 (date: 11-27-215, tag 0.2.5)
-------

- [versioning]search studies
-- A user can to search Trialverse for studies that match my search terms, either in title, description, or unique identifiers.
- [versioning] add study from other dataset to my dataset 
-- A user can copy a study extracted by another user.

