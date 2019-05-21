package at.ac.tuwien.ifs.prosci.provstarter.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileMonitor {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());

    private Process process;

    private boolean isRunning;
    public void start() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String[] fileMonitor_commands = {"bash", "-c", "java -jar filemonitor-1.0.2.jar"};
        process = runtime.exec(fileMonitor_commands);
        isRunning=true;
        LOGGER.info("start filemonitoring successfully.");

    }
    public void stop(){
        LOGGER.info("stop filemonitoring successfully.");
        process.destroy();
        isRunning=false;
    }

    public boolean isRunning(){
        return isRunning;
    }
}
