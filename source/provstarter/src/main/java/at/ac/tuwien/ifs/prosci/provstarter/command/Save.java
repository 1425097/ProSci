package at.ac.tuwien.ifs.prosci.provstarter.command;

import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import at.ac.tuwien.ifs.prosci.provstarter.helper.StatusCode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class Save {
    private ResourceBundle path_mapping = ResourceBundle.getBundle("path_mapping");
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private ProsciProperties prosciProperties;

    public StatusCode save() {
        try {
            Git git_input = Git.open(new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("input")));
            Status status = git_input.status().call();
            if (!status.isClean()) {
                git_input.add().addFilepattern(".").call();
                git_input.commit().setAll(true).setMessage("save new files").call();
                LOGGER.info("commit done");
            } else {
                System.out.println("no change to commit.");
            }
            return StatusCode.SUCCESS;
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StatusCode.FAIL_SYSTEM;
    }
}