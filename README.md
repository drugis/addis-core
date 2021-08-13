Addis-core
==========

ADDIS 2.x core

Before starting
-----------------------
The setup in this readme requires several components to already be installed and running. Please refer to the [OVERALL-README.md](./OVERALL-README.md) in this folder for more information on how to do so, and for other readmes that might be relevant.

Required software: 

 - yarn
 - compass (for changing css)
 - postgresql >= 9.5
 - maven
 - karma (for testing)

Run `yarn` from the root of the repository to retrieve the javascript dependencies.

Run `npm run build-dev` from the root of the repository to build the webapp.

Set up the database:

```
sudo -u postgres psql -c "CREATE USER addiscore WITH PASSWORD 'develop'"
sudo -u postgres psql -c "CREATE DATABASE addiscore ENCODING 'utf-8' OWNER addiscore"
```

The database structure is automatically created via liquibase maven plugin.

If you wish to allow programmatic access via the API, you can insert an API key into the addiscore database (note there should already be an account present to link the key to):
```
INSERT INTO applicationkey (secretkey, accountid, applicationname, creationdate, revocationdate) values ('[yourkey]', [accountid], '[yourname]', 'mm/dd/yyyy', 'mm/dd/yyyy');
```

It is assumed in the example environment settings below that instances of [Patavi](https://github.com/drugis/patavi) and [Jena-ES](https://github.com/drugis/jena-es) are running on ports 3000 and 3030, respecively.

To authenticate with Patavi, you need an API key, provided by whoever is hosting the Patavi server you wish to use. 

Set up the environment:

```
export TRIPLESTORE_BASE_URI=http://localhost:3030
export PATAVI_URI=http://localhost:3000
export PATAVI_API_TOKEN=sometokenhere
export PATAVI_TASK_DB_DRIVER=org.postgresql.Driver
export PATAVI_TASK_DB_HOST=localhost
export PATAVI_TASK_DB=patavitask
export PATAVI_TASK_DB_USERNAME=patavitask
export PATAVI_TASK_DB_PASSWORD=develop

export ADDIS_CORE_DB_CHANGELOG=database.sql
export ADDIS_CORE_DB_DRIVER=org.postgresql.Driver
export ADDIS_CORE_DB_HOST=localhost
export ADDIS_CORE_DB=addiscore
export ADDIS_CORE_DB_USERNAME=addiscore
export ADDIS_CORE_DB_PASSWORD=develop
export ADDIS_CORE_OAUTH_GOOGLE_SECRET=googleSecret
export ADDIS_CORE_OAUTH_GOOGLE_KEY=googleKey
export CLINICALTRIALS_IMPORTER_URL=importservicelocation
```

Running
-----------------------

```
mvn tomcat7:run
```

To run integration tests:
```
mvn test -Dtest=*IT
```

Backup and restore
------------------

To back up the ADDIS data:

 - stop the ADDIS application
 - stop the Jena-ES server
 - (if necessary) remove the `tdb.lock` file in the Jena-ES `DB` directory (it may remain after shutdown due to a bug)
 - `pg_dump` the contents of the ADDIS database
 - `tdbdump --loc=DB > backup.n4` the contents of the Jena-ES database

To restore the ADDIS data:

 - (if necessary) clear the SQL and Jena-ES databases
 - `\i backup.sql` in the `psql` client
 - `tdbloader --loc=DB backup.n4`

If not restoring to the same environment, use the `util/changePrefix.sh` script to change the triple store location.

Running ADDIS as a docker container
-----------------------------------

- check out the (dockerfiles repository)[https://github.com/drugis/dockerfiles]
- build addis WAR, using `mvn package`. Or: download a recent stable version from from https://drugis.org/files/addis-core.war
- build and run the dockerfile.

Example run script

```
docker run -d --name addis \
  --link postgres:postgres \
  --link patavi-server:patavi-server \
  --link jena-es:jena-es \
  -e JAVA_OPTS=" \
  -DADDIS_CORE_DB=addiscore \
  -DtomcatProxyScheme=https \
  -DtomcatProxyName=localhost \
  -DtomcatProxyPort=443 \
  -DADDIS_CORE_DB_DRIVER=org.postgresql.Driver \
  -DADDIS_CORE_DB_HOST=postgres \
  -DADDIS_CORE_DB=addiscore \
  -DADDIS_CORE_DB_USERNAME=addiscore \
  -DADDIS_CORE_DB_PASSWORD=<password> \
  -e PATAVI_URI=https://patavi-server:3000 \
  -e ADDIS_CORE_OAUTH_GOOGLE_SECRET=<google-secret> \
  -e ADDIS_CORE_OAUTH_GOOGLE_KEY=<google-key> \
  -e TRIPLESTORE_BASE_URI=https://jena-es:3030 \
  -e EVENT_SOURCE_URI_PREFIX=https://jena-es:3030 \
  -e CLINICALTRIALS_IMPORTER_URL=https://nct.drugis.org \
  -e PATAVI_API_KEY=someapikeyhere \
  -p 8081:8080 \
  -p 2223:22 \
  -t addis-tomcat
```

Styling
-------
ADDIS css is generated using SASS. If you wish to change the css, first make sure the SASS submodule is present:

    git submodule init
    git submodule update

Then run 'compass compile' from the root of the repository. 
