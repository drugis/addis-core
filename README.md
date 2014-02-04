addis-core
==========

ADDIS 2.x core

Running with PostgreSQL
-----------------------


Set up the database:

```
sudo -u postgres psql -c "CREATE USER mcdaweb WITH PASSWORD 'develop'"
sudo -u postgres psql -c "CREATE DATABASE mcdaweb ENCODING 'utf-8' OWNER mcdaweb"
```

Set up the environment:

```
export ADDIS_CORE_DB_CHANGELOG=database.pg.sql
export ADDIS_CORE_DB_DRIVER=org.postgresql.Driver
export ADDIS_CORE_DB_URL=jdbc:postgresql://localhost/addiscore
export ADDIS_CORE_DB_USERNAME=addiscore
export ADDIS_CORE_DB_PASSWORD=develop
export ADDIS_CORE_OAUTH_GOOGLE_SECRET=w0rp7-_Z_JQk_T0YcvMe3Aky
export ADDIS_CORE_OAUTH_GOOGLE_KEY=501575320185-sjffuboubldeaio8ngl1hrgfdj5a2nia.apps.googleusercontent.com
```