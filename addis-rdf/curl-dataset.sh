INPUT=$1
STUDIES=`grep "^study:.*" $INPUT | sed 's/study://' | sed 's/ {//'`

DATASET=http://fuseki-test.drugis.org:3030/datasets/ec7cb093-6657-44de-9588-4c464ab3f068

for s in $STUDIES; do
  grep '@prefix' $INPUT > $s.ttl
  awk "/^}/{flag=0}flag;/^study:$s {/{flag=1}" $INPUT >> $s.ttl
  echo $s
  curl -X PUT "$DATASET/data?graph=http://trials.drugis.org/graphs/$s" \
	  -H "Content-Type: text/turtle" \
      --data-binary "@$s.ttl"
done

grep '@prefix' $INPUT > concepts.ttl
awk "/^}/{flag=0}flag;/^dataset:.* {/{flag=1}" $INPUT >> concepts.ttl
curl -X PUT "$DATASET/data?graph=http://trials.drugis.org/graphs/concepts" \
  -H "Content-Type: text/turtle" \
  --data-binary "@concepts.ttl"
