addis-core
==========

ADDIS 2.x core

Make sure the SASS and Bower submodules are present:

    git submodule init
    git submodule update

Make sure you've run `make` in the src/main/webapp/resources directory first to generate the CSS.

Make sure you have the bower components needed by running `bower update` from the root of the repository

Running with PostgreSQL
-----------------------

Set up the database:

```
sudo -u postgres psql -c "CREATE USER addiscore WITH PASSWORD 'develop'"
sudo -u postgres psql -c "CREATE DATABASE addiscore ENCODING 'utf-8' OWNER addiscore"

sudo -u postgres psql -c "CREATE USER patavitask WITH PASSWORD 'develop'"
sudo -u postgres psql -c "CREATE DATABASE patavitask ENCODING 'utf-8' OWNER patavitask"

```

Set up the environment:

```
export KEYSTORE_PATH=/path/to/key/store
export KEYSTORE_PASSWORD="develop"
export TRIPLESTORE_BASE_URI=http://localhost:3030
export PATAVI_URI=http://localhost:3000

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
export ADDIS_CORE_OAUTH_GOOGLE_SECRET=HU_-JxoYUvMbvk4vVRMhHibI
export ADDIS_CORE_OAUTH_GOOGLE_KEY=201346854981-3pcdhh96orc3lcdr8k4i1u58pvepjme4.apps.googleusercontent.com
```

Run the Tomcat server:

```
mvn tomcat7:run -Djavax.net.ssl.trustStore=/path/to/trust/store
```

To run integration tests:
```
mvn test -Dtest=*IT
```
