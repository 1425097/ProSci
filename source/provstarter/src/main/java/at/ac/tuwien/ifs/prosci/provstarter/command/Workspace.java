package at.ac.tuwien.ifs.prosci.provstarter.command;

import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import at.ac.tuwien.ifs.prosci.provstarter.helper.StatusCode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Workspace {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());

    @Autowired
    ProsciProperties prosciProperties;
    private ResourceBundle path_mapping = ResourceBundle.getBundle("path_mapping");

    public StatusCode initWorkspace(String projectname, String pathString) throws GitAPIException, IOException {
        boolean currentWorkspace = checkWorkspace(projectname, pathString);
        if (!currentWorkspace) {
            if(pathString!=null){
            return createWorkspace(projectname, pathString);
            }
            else{
                LOGGER.error("Please check workspace command arguments.");
                return StatusCode.FAIL_SYSTEM;
            }
        } else {
            LOGGER.info("Change workspace.");
            String current_project=prosciProperties.readProperties(projectname);
            writePathToProjectProperty(path_mapping.getString("workspace.current"),current_project);
            return StatusCode.SUCCESS;

        }
    }

    private StatusCode createWorkspace(String projectname, String pathString) throws GitAPIException, IOException {
        Path path = Paths.get(pathString);

        //if directory exists?
        if (!Files.exists(path)) {
            LOGGER.info("Given path not exits, creating folder:[]" , path);
            path = Files.createDirectories(path);
            Files.createDirectories(Paths.get(path + path_mapping.getString("input")));
            Files.createDirectories(Paths.get(path + path_mapping.getString("prosci")));
            Files.createDirectories(Paths.get(path + path_mapping.getString("prosci.trace")));
            Files.createDirectories(Paths.get(path + path_mapping.getString("prosci.prov")));
            Files.createDirectories(Paths.get(path + path_mapping.getString("prosci.version")));
            Files.createDirectories(Paths.get(path + path_mapping.getString("prosci.trace.log")));
            Files.createDirectories(Paths.get(path + path_mapping.getString("prosci.trace.systeminfo")));

            LOGGER.info("create command history txt file:  " + (path + path_mapping.getString("prosci.trace.command")));
            Files.createFile(Paths.get(path + path_mapping.getString("prosci.trace.command")));
            writePathToProjectProperty(projectname, pathString);
            writePathToProjectProperty(path_mapping.getString("workspace.current"), pathString);
            LOGGER.info("create prosci repository..");
            Git git_input = Git.init().setDirectory(new File(path + path_mapping.getString("input"))).call();
            Git git_log = Git.init().setDirectory(new File(path + path_mapping.getString("prosci.trace.log"))).call();
            return StatusCode.SUCCESS;
        } else {
            LOGGER.debug("Given path exits");
            return StatusCode.FAIL_SYSTEM;
        }

    }

    private void writePathToProjectProperty(String key, String value)  {
        LOGGER.info("write the properties to prosci.properties file:{} = {} " , key, value);
        prosciProperties.setProperties(key, value);
        prosciProperties.writeProsciProperties("initial procsi project properties");

    }

    private boolean checkWorkspace(String projectname, String pathString) {
        if (prosciProperties.readProperties(projectname) == null) {
            return false;
        } else {
            if (pathString == null) {
                return true;
            } else {
                LOGGER.error("There is already a workspace with name " + projectname + " exist!");
                return true;
            }
        }
    }


}