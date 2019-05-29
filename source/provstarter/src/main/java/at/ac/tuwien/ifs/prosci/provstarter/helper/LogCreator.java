package at.ac.tuwien.ifs.prosci.provstarter.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogCreator {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private InfoCollector infoCollector;

    public void createLog(String file, String id) throws IOException {

        file = file + "/"+id+".txt";
        infoCollector.setFile(new File(file));

        logger.info("Initializing System...");
        SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write("" + os + "\n");
        fileWriter.close();

        logger.info("Checking computer system...");
        infoCollector.printComputerSystem(hal.getComputerSystem());

        logger.info("Checking Processor...");
        infoCollector.printProcessor(hal.getProcessor());

        logger.info("Checking Memory...");
        infoCollector.printMemory(hal.getMemory());

        logger.info("Checking CPU...");
        infoCollector.printCpu(hal.getProcessor());

        logger.info("Checking Disks...");
        infoCollector.printDisks(hal.getDiskStores());

        infoCollector.close();


    }


}
