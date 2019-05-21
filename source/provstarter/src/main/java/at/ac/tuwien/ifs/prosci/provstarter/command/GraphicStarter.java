package at.ac.tuwien.ifs.prosci.provstarter.command;

import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import at.ac.tuwien.ifs.prosci.provstarter.helper.StatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


public class GraphicStarter {

    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    ProsciProperties prosciProperties;

    public StatusCode start() {
        try {
        Runtime runtime = Runtime.getRuntime();
        LOGGER.debug("Opening Graphic visualization.");
        String[] graphicStarterCommand = {"bash", "-c", "java -Djava.awt.headless=false -jar graphvisualizer-1.0.1.jar"};
            Process graphicStarter = runtime.exec(graphicStarterCommand);
            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
         LOGGER.error("Can't start graphic visualization.");
        }
        return StatusCode.FAIL_SYSTEM;
    }


}
