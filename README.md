addis-core
==========

ADDIS 2.x core

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
```