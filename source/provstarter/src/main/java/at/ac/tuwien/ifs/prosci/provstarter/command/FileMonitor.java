package at.ac.tuwien.ifs.prosci.provstarter.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class FileMonitor {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Process process;

    private boolean isRunning;
    public void start() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        LOGGER.debug("Opening Filemonitor.");
        String[] fileMonitor_commands = {"bash", "-c", "java -jar filemonitor-1.0.0.jar"};
        process = runtime.exec(fileMonitor_commands);
        isRunning=true;
        LOGGER.debug("start filemonitoring successfully.");

    }
    public void stop(){
        process.destroy();
        isRunning=false;
    }

    public boolean isRunning(){
        return isRunning;
    }
}
