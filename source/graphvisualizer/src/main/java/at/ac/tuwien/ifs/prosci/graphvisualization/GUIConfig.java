package at.ac.tuwien.ifs.prosci.graphvisualization;

import at.ac.tuwien.ifs.prosci.graphvisualization.helper.ProsciProperties;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.CommandExtracter;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.Trace;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.OntologyCreator;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.VersionChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ResourceBundle;

@Configuration
public class GUIConfig {

    @Bean
    public ProsciProperties getProsciProperties() { return new ProsciProperties(); }

    @Bean
    public ResourceBundle path_mapping(){
    return ResourceBundle.getBundle("path_mapping");
}

    @Bean
    public VersionChecker getVersionChecker() {
        return new VersionChecker();
    }


    @Bean
    public OntologyCreator getOntologyCreator(){ return new OntologyCreator(); }

    @Bean
    public Trace getTrace(){ return new Trace(); }

    @Bean
    public CommandExtracter getCommandExtracter(){return new CommandExtracter();}


    @Bean
    public OntologyHandler getOntologyHandler(){
        return  new OntologyHandler();
    }

}
