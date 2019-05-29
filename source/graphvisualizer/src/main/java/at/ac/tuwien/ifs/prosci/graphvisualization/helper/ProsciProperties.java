package at.ac.tuwien.ifs.prosci.graphvisualization.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class ProsciProperties {
    private Properties props;
    private final Logger LOGGER = LogManager.getLogger(this.getClass());

    public ProsciProperties() {
        props = new Properties();
        try {
            readPropertiesFile();
        } catch (IOException e) {
            try {
                createPropertiesFile();
                readPropertiesFile();
            } catch (IOException e1) {
                LOGGER.error("Can't not init prosci.properties");
            }
        }
    }

    private void createPropertiesFile() {
        LOGGER.info("create procsi properties file:  " + (System.getProperty("user.home") + "/prosci.properties"));
        try {
            File file = new File(System.getProperty("user.home") + "/prosci.properties");
            FileOutputStream fileOut = new FileOutputStream(file);
        } catch (FileNotFoundException e1) {
            LOGGER.error("Can't not init prosci.properties");
        }
    }

    private void readPropertiesFile() throws IOException {
        FileInputStream in = new FileInputStream(System.getProperty("user.home") + "/prosci.properties");
        props.load(in);
        in.close();
    }

    public String readProperties(String properties) {
        return props.getProperty(properties);
    }


}
