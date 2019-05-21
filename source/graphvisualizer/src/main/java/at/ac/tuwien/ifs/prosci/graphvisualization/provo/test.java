package at.ac.tuwien.ifs.prosci.graphvisualization.provo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main(String[] args) {
        String line="17:43:00.128257 read(</home/feng/test0305/input/try.sh>, \"#!/bin/bash\\nsed -i s/test/1/ sample.tex\\nfor c in $(seq 30)\\ndo \\nj=`expr $c + 1`\\nsed -i s/$c/$j/ sample.tex\\npdflatex sample.tex \\nsleep 1\\n\\ndone\\n\\n\\n\\n\\n\\n\", 8192) = 146";
        Pattern pattern=null;
        Matcher matcher=null;
        pattern = Pattern.compile("read\\(.*?<(.*?/input/.*?)>,");
        matcher = pattern.matcher(line);
        if (matcher.find()) {
            String mat = matcher.group(1);
            System.out.println("find"+mat);
        }else{
            System.out.println("not find");
        }
    }
}
