#!/usr/bin/env bash
function extractLocation {
  str=$(grep "Location: " | sed 's/Location: //')
  if [ -z "$str" ]; then
    >&2 echo "NULL location"
    exit 1
  fi
  echo "$str" | tr -d '\r'
}

INPUT=$1
SERVER=$2
APIKEY=https://trialverse.org/apikeys/1
USER=gert@gertvv.nl
MESSAGE=`echo -n "Create dataset" | base64`

curl -s -D 00-headers -X POST -H "X-EventSource-Creator: $APIKEY" -H "X-EventSource-Title: $MESSAGE" $SERVER/datasets > 00-body

DATASET=$(extractLocation < 00-headers)

echo "Created" $DATASET

MESSAGE=`echo -n "Import ADDIS 1.16 dataset" | base64`
curl -D 01-headers -X PUT -H "X-EventSource-Creator: $APIKEY" -H "X-EventSource-Title: $MESSAGE" -H "Content-Type: text/trig" --data-binary "@$INPUT" "$DATASET/dump"

X=`grep 'dataset:[a-z0-9][a-z0-9-]*' $INPUT`
TVID="http://trials.drugis.org/datasets/${X#\ *dataset:}"
echo "INSERT INTO versionmapping (versioneddataseturl, owneruuid, trialversedataseturl) VALUES ('$DATASET', '$USER', '$TVID');"
