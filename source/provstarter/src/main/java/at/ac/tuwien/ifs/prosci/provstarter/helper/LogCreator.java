package at.ac.tuwien.ifs.prosci.provstarter.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogCreator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private InfoCollector infoCollector;

    public void createLog(String file, String id) throws IOException {

        file = file + "/"+id+".txt";
        infoCollector.setFile(new File(file));

        // Options: ERROR > WARN > INFO > DEBUG > TRACE

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

        logger.info("Checking Processes...");
        infoCollector.printProcesses(os, hal.getMemory());

        logger.info("Checking Sensors...");
        infoCollector.printSensors(hal.getSensors());

        logger.info("Checking Power sources...");
        infoCollector.printPowerSources(hal.getPowerSources());

        logger.info("Checking Disks...");
        infoCollector.printDisks(hal.getDiskStores());

        logger.info("Checking File System...");
        infoCollector.printFileSystem(os.getFileSystem());

        logger.info("Checking Network interfaces...");
        infoCollector.printNetworkInterfaces(hal.getNetworkIFs());

        logger.info("Checking Network parameterss...");
        infoCollector.printNetworkParameters(os.getNetworkParams());

        // hardware: displays
        logger.info("Checking Displays...");
        infoCollector.printDisplays(hal.getDisplays());

        // hardware: USB devices
        logger.info("Checking USB Devices...");
        infoCollector.printUsbDevices(hal.getUsbDevices(true));

        infoCollector.close();


    }


}
