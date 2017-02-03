INPUT=$1
DATASET_UUID=$2
GRAPH_UUID=$3

TITLE=`echo "Add study $GRAPH_UUID" | base64`

curl -X PUT -H "X-EventSource-Creator: https://trialverse.org/apikeys/1" \
  -H "X-EventSource-Title: $TITLE" \
  -H "Content-Type: text/turtle" \
  --data "@$INPUT" \
  http://localhost:8080/datasets/$DATASET_UUID/data\?graph=http://trials.drugis.org/graphs/$GRAPH_UUID
