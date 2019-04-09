package at.ac.tuwien.ifs.prosci.provstarter.config;

import at.ac.tuwien.ifs.prosci.provstarter.command.*;
import at.ac.tuwien.ifs.prosci.provstarter.helper.InfoCollector;
import at.ac.tuwien.ifs.prosci.provstarter.helper.LogCreator;
import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public ProsciProperties prosciProperties(){
        return new ProsciProperties();
    }
    @Bean
    public Workspace getWorkspace(){
        return new Workspace();
    }
    @Bean
    public Start getStart(){
        return new Start();
    }
    @Bean
    public GraphicStarter getGraphicStarter(){
        return new GraphicStarter();
    }
    @Bean
    public FileMonitor getFileMonitor(){
        return new FileMonitor();
    }
    @Bean
    public Save getSave(){
        return new Save();
    }
    @Bean
    public Show getShow(){
        return new Show();
    }
    @Bean
    public InfoCollector getInfoCollector(){
        return new InfoCollector();
    }
    @Bean
    public LogCreator getLogCreator(){
        return new LogCreator();
    }

}
