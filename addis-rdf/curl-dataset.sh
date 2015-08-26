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
MESSAGE=`echo -n "Create dataset" | base64`

curl -s -D 00-headers -X POST -H "X-EventSource-Creator: mailto:gert@gertvv.nl" -H "X-EventSource-Title: $MESSAGE" $SERVER/datasets > 00-body

DATASET=$(extractLocation < 00-headers)

echo "Created" $DATASET

MESSAGE=`echo -n "Import ADDIS 1.16 dataset" | base64`
curl -D 01-headers -X PUT -H "X-EventSource-Creator: mailto:gert@gertvv.nl" -H "X-EventSource-Title: $MESSAGE" -H "Content-Type: text/trig" --data-binary "@$INPUT" "$DATASET/dump"
