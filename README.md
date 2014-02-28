addis-core
==========

ADDIS 2.x core

Make sure the SASS submodule is present:

    git submodule init
    git submodule update

Make sure you've run `make` in the src/main/webapp/resources directory first to generate the CSS.

Make sure you have the bower components needen by running `bower install` from the root of the repository

Running with PostgreSQL
-----------------------


Set up the database:

```
sudo -u postgres psql -c "CREATE USER addiscore WITH PASSWORD 'develop'"
sudo -u postgres psql -c "CREATE DATABASE addiscore ENCODING 'utf-8' OWNER addiscore"
```

Set up the environment:

```
export ADDIS_CORE_DB_CHANGELOG=database.sql
export ADDIS_CORE_DB_DRIVER=org.postgresql.Driver
export ADDIS_CORE_DB_URL=jdbc:postgresql://localhost/addiscore
export ADDIS_CORE_DB_USERNAME=addiscore
export ADDIS_CORE_DB_PASSWORD=develop
export ADDIS_CORE_OAUTH_GOOGLE_SECRET=HU_-JxoYUvMbvk4vVRMhHibI
export ADDIS_CORE_OAUTH_GOOGLE_KEY=201346854981-3pcdhh96orc3lcdr8k4i1u58pvepjme4.apps.googleusercontent.com
export TRIALVERSE_DB_DRIVER=org.postgresql.Driver
export TRIALVERSE_DB_URL=jdbc:postgresql://localhost/trialverse
export TRIALVERSE_DB_USERNAME=trialverse
export TRIALVERSE_DB_PASSWORD=develop
export TRIPLESTORE_URI=http://localhost:3030/ds/query
```
