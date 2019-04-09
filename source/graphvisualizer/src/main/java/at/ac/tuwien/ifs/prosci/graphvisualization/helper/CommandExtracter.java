package at.ac.tuwien.ifs.prosci.graphvisualization.helper;



import at.ac.tuwien.ifs.prosci.graphvisualization.exception.TechnicalException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExtracter {
    public String getCommand(String command) throws TechnicalException {
        String extractedCommand="";
        Pattern pattern = Pattern.compile("\"(.+?)\"");
        Matcher matcher = pattern.matcher(command);
        while (matcher.find()) {
            String find=matcher.group(0);
            extractedCommand=extractedCommand+find.substring(1,find.length()-1)+" ";
        }
        if(extractedCommand.length()!=0){
            return extractedCommand.substring(0,extractedCommand.length()-1);
        }
        else {
            throw new TechnicalException("can't extract the command");
        }

    }

}
