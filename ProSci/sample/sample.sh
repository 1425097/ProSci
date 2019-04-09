#!/bin/bash
#step 1
wget -O ~/prosci/input/FL_insurance_sample.csv.zip "http://spatialkeydocs.s3.amazonaws.com/FL_insurance_sample.csv.zip" &&
#step 2
unzip ~/prosci/input/FL_insurance_sample.csv.zip -d ~/prosci/input/FL_insurance_sample.csv && 
#step 3
#wget -O ~/Downloads/weka-3-9-1.zip https://sourceforge.net/projects/weka/files/weka-3-9/3.9.1/weka-3-9-1.zip/download &&
#unzip ~/Downloads/weka-3-9-1.zip -d ~/Downloads
#step 4
java -cp ~/Downloads/weka-3-9-1/weka.jar weka.classifiers.rules.ZeroR -t ~/prosci/input/FL_insurance_sample.csv/FL_insurance_sample.csv > ~/prosci/input/out.txt &&
#step 5
pdflatex -output-directory ~/prosci/input/ ~/prosci/input/sample.tex 



