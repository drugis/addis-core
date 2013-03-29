#!/bin/bash
PRIV_USER=postgres
PRIV_PWD=develop

APP_USER=trialverse
APP_PWD=develop

PORT=5432
HOST=localhost
USER=trialverse
PASSWORD=develop
DATABASE=trialverse

PGPASSWORD=$PRIV_PWD psql -h $HOST -p $PORT -U $PRIV_USER -c  "SELECT pg_terminate_backend(pg_stat_activity.procpid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '$DATABASE'";
PGPASSWORD=$PRIV_PWD psql -h $HOST -p $PORT -U $PRIV_USER -c "DROP DATABASE IF EXISTS $DATABASE"
PGPASSWORD=$PRIV_PWD psql -h $HOST -p $PORT -U $PRIV_USER -d postgres -c "CREATE DATABASE $DATABASE ENCODING 'utf-8' OWNER $USER"

PGPASSWORD=$APP_PWD psql -h $HOST -p $PORT -U $APP_USER -f structure.sql

#echo "Updating documentation"
#ssh addis@$HOST -p $SSH_PORT "postgresql_autodoc";
#scp -P $SSH_PORT addis@$HOST:~/addis.\* documentation/;
#dot -Tpdf documentation/addis.dot -o documentation/addis.pdf;

echo "Adding example files"
./transform.sh "depressionExample" "Hansen 2005" "Depression dataset based on the Hansen et al. (2005) systematic review"
PGPASSWORD=$APP_PWD psql -h $HOST -p $PORT -U $APP_USER -f depressionExample.sql

./transform.sh "hypertensionExample" "Edarbi EPAR" "Hypertension dataset based on the Edarbi EPAR"
PGPASSWORD=$APP_PWD psql -h $HOST -p $PORT -U $APP_USER -f hypertensionExample.sql

