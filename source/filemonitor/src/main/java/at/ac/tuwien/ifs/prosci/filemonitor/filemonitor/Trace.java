package at.ac.tuwien.ifs.prosci.filemonitor.filemonitor;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.itextpdf.text.pdf.PdfReader;
@Component
public class Trace implements Runnable {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    private Git git_log;
    private Git git_input;
    private final static DateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private ResourceBundle path_mapping = ResourceBundle.getBundle("path_mapping");

    private ProsciProperties prosciProperties;
    private String lastCommit="";
    private String commitVersion() throws GitAPIException {
        String commitAndLastCommit = "";
        Status status = git_input.status().call();
        Status status_log = git_input.status().call();
        boolean added=false;
        if (!status.isClean()) {
            LOGGER.info("1  "+ new Date());
            for(String s:status_log.getUntracked()){
                boolean add=true;
                try {
                    if(s.substring(s.lastIndexOf(".")+1).equals("pdf")) {
                        PdfReader pdfReader = new PdfReader(prosciProperties.readProperties("workspace.current") + path_mapping.getString("input")+s);
                        String textFromPdfFilePageOne = PdfTextExtractor.getTextFromPage(pdfReader, 1);
                    }
                } catch ( Exception e ) {
                    add=false;
                }
                if(add) {
                    git_input.add().addFilepattern(s).call();
                    added=true;
                }
            }for(String s:status_log.getModified()){
                boolean add=true;
                try {
                    if(s.substring(s.lastIndexOf(".")+1).equals("pdf")) {
                        PdfReader pdfReader = new PdfReader(prosciProperties.readProperties("workspace.current") + path_mapping.getString("input")+s);
                        String textFromPdfFilePageOne = PdfTextExtractor.getTextFromPage(pdfReader, 1);
                    }
                } catch ( Exception e ) {
                    add=false;
                }
                if(add) {
                    git_input.add().addFilepattern(s).call();
                    added=true;
                }
            }
            LOGGER.info("2  "+new Date());
        }

        if(added){
            LOGGER.info("3  "+new Date());
            RevCommit commit=git_input.commit().setMessage("commit files").call();
            if(commit.getId().getName().length()>0) {
                commitAndLastCommit = commit.getId().getName() + "|" + lastCommit + "|";
                lastCommit = commit.getId().getName();
            }
            LOGGER.info("4  "+new Date());
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
            while (true) {

                WatchService watchService
                        = FileSystems.getDefault().newWatchService();

                Path path = Paths.get(prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.log"));
                for (File f : path.toFile().listFiles()) {
                    f.toPath().register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE);
                }

                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        String logToCommandFile="";
                        String commitVersion=commitVersion();
                        logToCommandFile=logToCommandFile+format.format(new Date()) +"|"+ Path dir = keys.get(key);+ "|" +event.context().toString() +"|" + commitVersion + "\n";
                        System.out.printf("kind=%s, count=%d, context=%s Context type=%s%n ",
                                event.kind(),
                                event.count(), event.context(),
                                ((Path) event.context()).getClass());
                        fileWriter(logToCommandFile);
                    }
                    key.reset();
                }
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
        git_log = Git.open(new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.log")));
        git_input = Git.open(new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("input")));

    }

    private void readPropertyFile(){
        prosciProperties=new ProsciProperties();

    }

    private void fileWriter(String logToCommandFile) throws  IOException {
        FileWriter fileWriter = new FileWriter(prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.command"), true);
        fileWriter.write(logToCommandFile);
        fileWriter.close();
    }
    class LogComp implements Comparator<String>{

        @Override
        public int compare(String e1, String e2) {
            int e1_num = Integer.parseInt(e1.split("\\.")[2]);
            int e2_num = Integer.parseInt(e2.split("\\.")[2]);
            return e1_num-e2_num;

        }
    }


}