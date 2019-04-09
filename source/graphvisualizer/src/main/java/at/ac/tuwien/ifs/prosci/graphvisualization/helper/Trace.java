package at.ac.tuwien.ifs.prosci.graphvisualization.helper;

import at.ac.tuwien.ifs.prosci.provstarter.helper.LogCreator;
import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.UUID;


public class Trace{
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    LogCreator logCreator;
    @Autowired
    private ProsciProperties prosciProperties;

    @Autowired
    private ResourceBundle path_mapping;




    public Process traceProcess(String command) throws IOException {

        LOGGER.debug("trace command : "+ command);

        UUID logId=UUID.randomUUID();

        Files.createDirectories(Paths.get(
                prosciProperties.readProperties("workspace.current")
                        + path_mapping.getString("prosci.trace.log")+ "rerun"+logId.getLeastSignificantBits() ));
        logCreator.createLog(
                prosciProperties.readProperties("workspace.current")
                        + path_mapping.getString("prosci.trace.systeminfo"),
                "rerun"+logId.getLeastSignificantBits()
        );


        //String[] strace_commands = {"bash", "-c", ResourceBundle.getBundle("application").getString("strace") +
          //      prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.log")+"rerun"+logId.getLeastSignificantBits()+ "/prosci_"+System.currentTimeMillis()+".log " +
            //    command};
       // ProcessBuilder builder=new ProcessBuilder().command(strace_commands);
        //builder.redirectErrorStream(true);
        //Process process = builder.start();


        LOGGER.debug("Opening Xterm.");
        String[] xterm_commands = {"bash", "-c", "xterm -e 'cd " +
                prosciProperties.readProperties("workspace.current") + path_mapping.getString("input") +command+" &&exit"+ " &&/bin/bash' & echo \"terminal_id=\"$! &&"};
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(xterm_commands);
        InputStreamReader input = new InputStreamReader(process.getInputStream());
        BufferedReader inputReader = new BufferedReader(input);
        String process_id = inputReader.readLine();

        LOGGER.debug("Starting to trace Xterm...");
        String[] strace_commands = {"bash", "-c", ResourceBundle.getBundle("application").getString("strace") +
                prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.log") +"rerun"+logId.getLeastSignificantBits()+ "/prosci_"+System.currentTimeMillis()+".log"};
        process = runtime.exec(strace_commands);


        return process;

    }


}
