rancher kubectl delete secret addis-secrets -n drugis-test
rancher kubectl create secret generic addis-secrets \
  -n drugis-test \
  --from-literal=ADDIS_DB_PASSWORD=develop \
  --from-literal=ADDIS_CORE_OAUTH_GOOGLE_SECRET=WFU_VvlxrsyNLVUDkkTVgvfQ \
  --from-literal=ADDIS_CORE_OAUTH_GOOGLE_KEY=290619536014-abnf3o5knc423o0n25939ql4ga0m0809.apps.googleusercontent.com \
  --from-literal=JENA_API_KEY=cooljenakeybro

rancher kubectl delete secret db-credentials -n drugis-test
rancher kubectl create secret generic db-credentials \
  -n drugis-test \
  --from-literal=POSTGRES_PASSWORD=develop \
  --from-literal=PATAVI_DB_PASSWORD=develop 

rancher kubectl delete configmap addis-settings -n drugis-test
rancher kubectl create configmap addis-settings \
  -n drugis-test \
  --from-literal=ADDIS_CORE_DB_CHANGELOG=database.sql \
  --from-literal=ADDIS_CORE_DB_DRIVER=org.postgresql.Driver \
  --from-literal=ADDIS_CORE_DB=addis \
  --from-literal=ADDIS_CORE_DB_HOST=postgres \
  --from-literal=ADDIS_CORE_DB_USERNAME=addis \
  --from-literal=CLINICALTRIALS_IMPORTER_URL=https://nct-test.edge.molgenis.org \
  --from-literal=EVENT_SOURCE_URI_PREFIX=https://fuseki-test.edge.molgenis.org \
  --from-literal=TRIPLESTORE_BASE_URI=https://fuseki-test.edge.molgenis.org \
  --from-literal=PATAVI_URI=https://patavi-test.edge.molgenis.org \
  --from-literal=SECURE_TRAFFIC=true

rancher kubectl delete secret passwords -n drugis-test
rancher kubectl create secret generic passwords \
 -n drugis-test \
 --from-literal=GITHUB_AUTHENTICATION_TOKEN=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 \
 --from-literal=rabbit-password=develop \
 --from-literal=PATAVI_API_KEY=coolkeybro

rancher kubectl delete configmap patavi-settings -n drugis-test
rancher kubectl create configmap patavi-settings \
  -n drugis-test \
  --from-literal=PATAVI_DB_HOST=postgres \
  --from-literal=PATAVI_DB_NAME=patavi \
  --from-literal=PATAVI_DB_USER=patavi \
  --from-literal=PATAVI_PORT=3000 \
  --from-literal=PATAVI_HOST=patavi-enterprise-test.edge.molgenis.org \
  --from-literal=PATAVI_BROKER_HOST=guest:develop@rabbitmq \
  --from-literal=PATAVI_BROKER_USER=guest \
  --from-literal=PATAVI_BROKER_PASSWORD=develop \
  --from-literal=SECURE_TRAFFIC=true \
  --from-literal=PATAVI_PROXY_HOST=patavi-enterprise-test.edge.molgenis.org 

rancher kubectl apply -f postgres.yaml
rancher kubectl apply -f rabbitmq.yaml
rancher kubectl apply -f patavi-server.yaml
rancher kubectl apply -f patavi-db-init.yaml
rancher kubectl apply -f patavi-smaa-worker.yaml
rancher kubectl apply -f patavi-gemtc-worker.yaml
rancher kubectl apply -f addis-db-init.yaml
rancher kubectl apply -f nct-importer.yaml
rancher kubectl apply -f jena-es.yaml
rancher kubectl apply -f addis.yaml