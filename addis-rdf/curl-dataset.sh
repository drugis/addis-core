INPUT=$1
STUDIES=`grep "^study:.*" $INPUT | sed 's/study://' | sed 's/ {//'`

for s in $STUDIES; do
  grep '@prefix' $INPUT > $s.ttl
  awk "/^}/{flag=0}flag;/^study:$s {/{flag=1}" $INPUT >> $s.ttl
  ./curl.sh http://trials.drugis.org/graphs/$s $s.ttl
done
