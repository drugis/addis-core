#!/bin/bash

./saxon-xslt -o $1.sql \
  -s:data/$1.addis \
  -xsl:transform.xsl \
  projectName="$2" \
  projectDescription="$3"
