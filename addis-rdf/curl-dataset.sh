INPUT=$1
STUDIES=`grep "^study:.*" $INPUT | sed 's/study://' | sed 's/ {//'`

DATASET=http://fuseki-test.drugis.org:3030/datasets/466ab6cd-0962-49fd-bfd3-e034b9942a1d

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
