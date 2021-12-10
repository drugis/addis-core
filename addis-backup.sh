#!/bin/bash

function checkSuccess {
rc=$?
 if [[ $rc != 0 ]] ; then
   echo 'Could not addis backup' $1;
   exit $rc
 fi
}

echo "set maintenance"
mv /usr/share/nginx/html/addis_maintenance_off.html /usr/share/nginx/html/addis_maintenance_on.html | true
checkSuccess 'set maintenance'
wait
echo "stop addis"
docker stop addis
checkSuccess 'stop addis'
wait
echo "stop jena-es"
docker stop gertseki
checkSuccess 'stop jena-es'
wait
echo "make addis db backup"
pg_dump --dbname=postgresql://addiscore:pwd@psql-test.drugis.org:5432/addiscore -f addis-core/backups/$(date +%Y-%m-%d)_test_addis_backup.psql
checkSuccess 'make addis db backup'
wait
echo "make jena-es backup"
cd jena-es/docker/
rm -f DB/tdb.lock
/home/deploy/apache-jena-2.13.0/bin/tdbdump --loc=DB > /home/deploy/addis-core/backups/$(date +%Y-%m-%d)_test_jena_es_backup.n4
wait
gzip /home/deploy/addis-core/backups/$(date +%Y-%m-%d)_test_jena_es_backup.n4
rm -rf DB
mkdir DB
/home/deploy/apache-jena-2.13.0/bin/tdbloader --loc=DB /home/deploy/addis-core/backups/$(date +%Y-%m-%d)_test_jena_es_backup.n4.gz
cd
checkSuccess 'make jena-es backup'
wait
echo "restart jena-es"
docker start gertseki
checkSuccess 'start jena-es'
wait
echo "restart addis"
docker start addis
checkSuccess 'start jena-es'
echo "wait 20 seconds for addis to restart"
sleep 20
wait
echo "set maintenance"
mv /usr/share/nginx/html/addis_maintenance_on.html /usr/share/nginx/html/addis_maintenance_off.html
wait
echo "backup completed"
	

