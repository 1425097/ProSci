package at.ac.tuwien.ifs.prosci.provstarter.command;


import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import at.ac.tuwien.ifs.prosci.provstarter.helper.StatusCode;
import at.ac.tuwien.ifs.prosci.provstarter.helper.LogCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.UUID;

public class Start {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    ProsciProperties prosciProperties;
    @Autowired
    LogCreator logCreator;

    private ResourceBundle path_mapping = ResourceBundle.getBundle("path_mapping");

    public StatusCode startXterm() {
        if (!checkWorkspace()) {
            LOGGER.error("No workspace defined, please use the command: workspace [workspace name] [optional:workspace path]");
            return StatusCode.FAIL_SYSTEM;
        } else {
            UUID xtermId = UUID.randomUUID();
            try {
                Files.createDirectories(Paths.get(
                        prosciProperties.readProperties("workspace.current")
                                + path_mapping.getString("prosci.trace.log") + xtermId));

                logCreator.createLog(
                        prosciProperties.readProperties("workspace.current")
                                + path_mapping.getString("prosci.trace.systeminfo"),
                        xtermId.toString()
                );

                Runtime runtime = Runtime.getRuntime();

                LOGGER.debug("Opening Xterm.");
                String[] xterm_commands = {"bash", "-c", "xterm -e 'cd " +
                        prosciProperties.readProperties("workspace.current") + path_mapping.getString("input") + " &&/bin/bash' & echo \"terminal_id=\"$!"};
                Process process = runtime.exec(xterm_commands);
                InputStreamReader input = new InputStreamReader(process.getInputStream());
                BufferedReader inputReader = new BufferedReader(input);
                String process_id = inputReader.readLine();

                LOGGER.debug("Starting to trace Xterm...");
                String[] strace_commands = {"bash", "-c", ResourceBundle.getBundle("application").getString("strace") +
                        prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.log") + xtermId + "/prosci_" + System.currentTimeMillis() + ".log" +
                        " -p " + process_id.substring(12)};
                process = runtime.exec(strace_commands);

                return StatusCode.SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
                return StatusCode.FAIL_SYSTEM;
            }

        }
    }

    private boolean checkWorkspace() {
        String current_workspace = null;

            current_workspace = prosciProperties.readProperties("workspace.current");
            if (current_workspace == null) {
                return false;
            }
            return true;
    }

}
