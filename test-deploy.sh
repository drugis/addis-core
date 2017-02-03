

#!/bin/bash

function checkSuccess {
rc=$?
 if [[ $rc != 0 ]] ; then
   echo 'Could not perform' $1;
   echo "addis has not been deployed, Could not perform" $1 | sendxmpp -t -u addistestuser1 -o gmail.com osmosisch;
   exit $rc
 fi
}

echo "update bower"
bower update
checkSuccess 'bower update'
wait

echo "package addis, using 'mvn package'"
mvn clean package
checkSuccess 'package'
wait

echo "copy war to addis-test, using 'scp target/addis-core.war deploy@box006.drugis.org:addis-core/docker/ROOT.war'"
scp target/addis-core.war deploy@addis-test.drugis.org:addis-core/docker/ROOT.war
checkSuccess 'copy war over to test server'
wait

echo "build docker container in test server, using ssh "
ssh deploy@addis-test.drugis.org 'cd addis-core/docker && docker build --tag addis-tomcat .'
checkSuccess 'build docker image'
wait

echo "restart container on test"
ssh deploy@addis-test.drugis.org docker stop addis
checkSuccess 'stop docker container'
wait

ssh deploy@addis-test.drugis.org docker rm addis
checkSuccess 'remove docker container'
wait

ssh deploy@addis-test.drugis.org addis-core/addis-run.sh
checkSuccess 'run addis'
wait

echo "deployment completed"
wait

echo "addis has been deployed to https://addis-test.drugis.org" | sendxmpp -t -u addistestuser1 -o gmail.com osmosisch


