#!/bin/bash

OLDPREFIX=$1
NEWPREFIX=$2

if [[ -z "$OLDPREFIX" || -z "$NEWPREFIX" ]]; then
	echo "Usage: ./changePrefix.sh <old-prefix> <new-prefix>"
	exit 1
fi

cat changePrefix.sparql.tpl | sed "s|OLDPREFIX|$OLDPREFIX|g" | sed "s|NEWPREFIX|$NEWPREFIX|g" > changePrefix.sparql
cat changePrefix.sql.tpl | sed "s|OLDPREFIX|$OLDPREFIX|g" | sed "s|NEWPREFIX|$NEWPREFIX|g" > changePrefix.sql

echo -e "Now run\n  tdbupdate --loc=DB --update=changePrefix.sparql\nand\n  psql -U addiscore -f changePrefix.sql"
