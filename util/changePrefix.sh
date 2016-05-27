#!/bin/bash

NEWPREFIX=$1
OLDPREFIX=$2

if [[ -z "$OLDPREFIX" || -z "$NEWPREFIX" ]]; then
	echo "Usage: ./changePrefix.sh <old-prefix> <new-prefix>"
	exit 1
fi

cat changePrefix.sparql.tpl | replace "OLDPREFIX" "$OLDPREFIX" "NEWPREFIX" "$NEWPREFIX" > changePrefix.sparql
cat changePrefix.sql.tpl | replace "OLDPREFIX" "$OLDPREFIX" "NEWPREFIX" "$NEWPREFIX" > changePrefix.sql

echo -e "Now run\n  tdbupdate --loc=DB --update=changePrefix.sparql\nand\n  psql -U addiscore -f changePrefix.sql"
