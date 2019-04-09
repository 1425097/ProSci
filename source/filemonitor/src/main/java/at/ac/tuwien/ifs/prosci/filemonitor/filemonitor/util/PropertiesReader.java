package at.ac.tuwien.ifs.prosci.filemonitor.filemonitor.util;

import at.ac.tuwien.ifs.prosci.filemonitor.filemonitor.Exception.PropertiesReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

@Component
public class PropertiesReader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ResourceBundle path_mapping = ResourceBundle.getBundle("path_mapping");

    private ResourceBundle error_message = ResourceBundle.getBundle("error_message");

    private Properties props;

    public Properties readProsciProperties() throws PropertiesReaderException {
        try {
            logger.info("reading properties: "+ System.getProperty("user.home") + path_mapping.getString("properties"));
            FileInputStream in = new FileInputStream(System.getProperty("user.home") + path_mapping.getString("properties"));
            props = new Properties();
            props.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            throw new PropertiesReaderException(error_message.getString("error.PropertiesReaderException") + System.getProperty("user.home"));
        } catch (IOException e) {
            throw new PropertiesReaderException(error_message.getString("error.PropertiesReaderException") + System.getProperty("user.home"));
        }
        return props;
    }
/**
    public void writeProsciProperties(String comments) throws PropertiesReaderException {
        try {
            FileOutputStream out = new FileOutputStream(System.getProperty("user.home") + "/prosci.properties");
            props.store(out, comments);
            out.close();
        } catch (FileNotFoundException e) {
            throw new PropertiesReaderException("Can't not find properties with path: " + System.getProperty("user.home") + "/prosci.properties");
        } catch (IOException e) {
            throw new PropertiesReaderException("Can't not write properties in path: " +  System.getProperty("user.home") + "/prosci.properties");
        }
    }
**/

}
