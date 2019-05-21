#!/bin/bash
#step 1
wget -O FL_insurance_sample.csv.zip "http://spatialkeydocs.s3.amazonaws.com/FL_insurance_sample.csv.zip" &&
#step 2
unzip FL_insurance_sample.csv.zip -d FL_insurance_sample.csv && 
#step 3
#wget -O ~/Downloads/weka-3-9-1.zip https://sourceforge.net/projects/weka/files/weka-3-9/3.9.1/weka-3-9-1.zip/download &&
#unzip ~/Downloads/weka-3-9-1.zip -d ~/Downloads
#step 4
java -cp ~/Downloads/weka-3-8-3/weka.jar weka.classifiers.rules.ZeroR -t FL_insurance_sample.csv/FL_insurance_sample.csv > out.txt &&

pdflatex sample.tex &&

java -cp ~/Downloads/weka-3-9-1/weka.jar weka.classifiers.rules.ZeroR -t FL_insurance_sample.csv/FL_insurance_sample.csv > out.txt &&

pdflatex sample.tex &&

csvcut -c 1,2,3,4,5,6 FL_insurance_sample.csv/FL_insurance_sample.csv > FL_insurance_sample.csv/FL_insurance_sample_sub.csv &&

java -cp ~/Downloads/weka-3-8-3/weka.jar weka.classifiers.rules.ZeroR -t FL_insurance_sample.csv/FL_insurance_sample_sub.csv > out.txt &&

pdflatex sample.tex &&

java -cp ~/Downloads/weka-3-9-1/weka.jar weka.classifiers.rules.ZeroR -t FL_insurance_sample.csv/FL_insurance_sample_sub.csv > out.txt &&

pdflatex sample.tex




