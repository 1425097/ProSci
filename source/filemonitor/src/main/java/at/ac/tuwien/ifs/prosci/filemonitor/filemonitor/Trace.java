package at.ac.tuwien.ifs.prosci.filemonitor.filemonitor;

import at.ac.tuwien.ifs.prosci.filemonitor.filemonitor.Exception.PropertiesReaderException;
import at.ac.tuwien.ifs.prosci.filemonitor.filemonitor.util.PropertiesReader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Trace implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private Git git_log;
    private Git git_input;
    private String commitAndLastCommit="";
    private String logToCommandFile="";
    private final static DateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private ResourceBundle path_mapping = ResourceBundle.getBundle("path_mapping");

    @Autowired
    private PropertiesReader propertiesReader;

    private String commitVersion(String id) throws IOException, GitAPIException, PropertiesReaderException {
        String commitAndLastCommit = "";
        Status status = git_input.status().call();
        int count = 1;
        if (!status.isClean()) {
            git_input.add().addFilepattern(".").call();
            git_input.commit().setAll(true).setMessage(id).call();
            LOGGER.info("commit done");
            count=2;
        }
        try {
            Iterable<RevCommit> logs = git_input.log()
                    .call();
            for (RevCommit rev : logs) {
                LOGGER.info("Commit: " + rev + ", name: " + rev.getName() + ", id: " + rev.getId().getName());
                commitAndLastCommit = commitAndLastCommit + rev.getId().getName() + "|";
                count--;
                if (count == 0) break;
            }

        }catch (Exception e){
            return commitAndLastCommit;
        }
        return commitAndLastCommit;

    }

    @Override
    public void run() {
        try {
            initLogRepository();
            while (true) {
                Status status_log = git_log.status().call();
                if (status_log.getUntracked().size() > 0) {
                    TreeSet<String> logs=new TreeSet<>(new LogComp());
                    logs.addAll(status_log.getUntracked());
                    int count=0;
                    logToCommandFile="";
                    List<String> ids=new ArrayList<>();
                    for(String id:logs){
                        if(count==0){
                            String commitVersion=commitVersion(id);
                            if(commitVersion.length()>0){
                                commitAndLastCommit = commitVersion;
                            }
                            logToCommandFile=logToCommandFile+format.format(new Date()) + "|" + id.replace("/","|") +"|" + commitAndLastCommit + "\n";
                            count++;
                        }else{
                            logToCommandFile=logToCommandFile+format.format(new Date()) + "|" + id.replace("/","|") + "|" + commitAndLastCommit + "\n";

                            logToCommandFile=logToCommandFile+format.format(new Date()) + "|" + id.replace("/","|") + "|" + commitAndLastCommit + "\n";

                        }
                        ids.add(id);

                    }
                    fileWriter(logToCommandFile);
                    for(String id: ids) {
                        git_log.add().addFilepattern(id).call();
                    }
                    LOGGER.info("Writing finish.");

                    git_log.commit().setMessage(commitAndLastCommit).call();
                }

            }
        } catch (PropertiesReaderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoFilepatternException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

    }


    private void initLogRepository() throws PropertiesReaderException, IOException, GitAPIException {
        Properties properties = propertiesReader.readProsciProperties();
        git_log = Git.open(new File(properties.getProperty("workspace.current") + path_mapping.getString("prosci.trace.log")));
        git_input = Git.open(new File(properties.getProperty("workspace.current") + path_mapping.getString("input")));

    }

    class LogComp implements Comparator<String>{

        @Override
        public int compare(String e1, String e2) {
                int e1_num = Integer.parseInt(e1.split("\\.")[2]);
                int e2_num = Integer.parseInt(e2.split("\\.")[2]);
                return e1_num-e2_num;

        }
    }

    private void fileWriter(String logToCommandFile) throws PropertiesReaderException, IOException {
        FileWriter fileWriter = new FileWriter(propertiesReader.readProsciProperties().getProperty("workspace.current") + path_mapping.getString("prosci.trace.command"), true);
        fileWriter.write(logToCommandFile);
        fileWriter.close();
    }


}