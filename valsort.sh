#!/bin/bash

# Usage: . valsort.sh [output folder path] [output file count - 1]
# e.g. . valsort.sh /home/blue/output 299
# Need valsort executable file in the path ./64/valsort

shopt -s extglob

rm $1/*.sum

for outputfile in $1/output.+([0-9])!(.sum);
do
  echo $outputfile
  ./64/valsort -o $outputfile.sum $outputfile
done

for i in $(seq 0 $2);
do
  echo $1/output.$i.sum
  cat $1/output.$i.sum >> $1/total.sum
done
./64/valsort -s $1/total.sum