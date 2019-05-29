#!/bin/bash
sleep 1
for c in $(seq 99)
do 
j=`expr $c + 1`
sed -i s/$c/$j/ sample.tex
pdflatex sample.tex 
done







