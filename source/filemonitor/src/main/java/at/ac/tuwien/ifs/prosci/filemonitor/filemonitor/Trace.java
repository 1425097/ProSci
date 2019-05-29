package at.ac.tuwien.ifs.prosci.filemonitor.filemonitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

@Component
public class Trace implements Runnable {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    private Git git_input;
    private final static DateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private ResourceBundle path_mapping = ResourceBundle.getBundle("path_mapping");

    private ProsciProperties prosciProperties;
    private String lastCommit = "";

    private String commitVersion() throws GitAPIException {
        String commitAndLastCommit = "";
        Status status_log = git_input.status().call();
        boolean added = false;
        if (!status_log.isClean()) {
            for (String s : status_log.getUntracked()) {
                File file = new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("input") + s);
                if (file.length() != 0) {
                    git_input.add().addFilepattern(s).call();
                    added = true;
                }
            }
            for (String s : status_log.getModified()) {
                File file = new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("input") + s);
                if (file.length() != 0) {
                    git_input.add().addFilepattern(s).call();
                    added = true;
                }
            }
        }

        if (added) {
            RevCommit commit = git_input.commit().setMessage("commit files").call();
            if (commit.getId().getName().length() > 0) {
                commitAndLastCommit = commit.getId().getName() + "|" + lastCommit + "|";
                lastCommit = commit.getId().getName();
            }
        } else {
            commitAndLastCommit = lastCommit + "|";
        }

        return commitAndLastCommit;

    }

    @Override
    public void run() {
        try {
            readPropertyFile();
            initLogRepository();
            commitVersion();

            WatchService watchService
                    = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.log"));
            for (File f : path.toFile().listFiles()) {
                f.toPath().register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE);
            }
            WatchKey key;
            String eventContext_old = "start";
            while ((key = watchService.take()) != null) {
                Path dir = (Path) key.watchable();
                for (WatchEvent<?> event : key.pollEvents()) {
                    String eventContext_new = event.context().toString();
                    if(eventContext_old.equals("start")){
                        commitVersion();
                    }
                    if (!eventContext_old.equals("start") && !eventContext_old.equals("stop.lock")
                            && !eventContext_new.equals(eventContext_old)) {
                        String[] pathname = dir.resolve(event.context().toString()).toString().split("/");
                        String logToCommandFile = "";
                        String commitVersion = commitVersion();
                        logToCommandFile = logToCommandFile + format.format(new Date()) + "|" + pathname[pathname.length - 2] + "|" + eventContext_old + "|" + commitVersion + "\n";
                        fileWriter(logToCommandFile);
                    }
                    eventContext_old = eventContext_new;
                    if (eventContext_old.equals("stop.lock")) {
                        Files.deleteIfExists(Paths.get(prosciProperties.readProperties("workspace.current")
                                + path_mapping.getString("prosci.trace.log")
                                + prosciProperties.readProperties(prosciProperties.readProperties("workspace.current")+".log")
                                + path_mapping.getString("prosci.trace.stop")));
                    }
                }
                key.reset();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }


    private void initLogRepository() throws IOException {
        git_input = Git.open(new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("input")));
    }

    private void readPropertyFile() {
        prosciProperties = new ProsciProperties();
    }

    private void fileWriter(String logToCommandFile) throws IOException {
        FileWriter fileWriter = new FileWriter(prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.command"), true);
        fileWriter.write(logToCommandFile);
        fileWriter.close();
    }

}