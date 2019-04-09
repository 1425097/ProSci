package at.ac.tuwien.ifs.prosci.graphvisualization.provo;

import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.xml.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.model.ProvoEntitiy;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OntologyCreator {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ResourceBundle path_mapping;
    private ProvFactory pFactory;
    private Namespace ns;

    private TreeMap<String, Entity> entities_from;
    private TreeMap<String, Entity> entities_to;
    private TreeMap<String, Entity> entities_delete;
    private Set<Activity> activitySet;
    private TreeMap<String, StatementOrBundle> statementOrBundles;
    private ArrayList<RevCommit> revCommits;

    private TreeMap<String, List<ProvoEntitiy>> entities;

    @Autowired
    private ProsciProperties prosciProperties;
    @Autowired
    private VersionChecker versionChecker;

    private String trace;
    private String input;
    private String log;
    private String systemInfo;

    public void init(){
        ns = new Namespace();
        this.pFactory = InteropFramework.newXMLProvFactory();
        this.input = prosciProperties.readProperties("workspace.current") + path_mapping.getString("input");
        this.trace = prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace");
        this.log = prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.log");
        this.systemInfo = prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.trace.systeminfo");
        entities_from = new TreeMap<>();
        entities_to = new TreeMap<>();
        entities_delete = new TreeMap<>();
        revCommits = new ArrayList<>();
        statementOrBundles = new TreeMap<>();
        entities= new TreeMap<>();
        activitySet = new TreeSet<>(new ActivityComp());
        ns.addKnownNamespaces();
        ns.register("prosci", "http://www.prosci.tuwein.ac.at#");
    }

    public QualifiedName qn(String n) {
        LOGGER.info("Qualified name: " +n);
        return ns.qualifiedName("prosci", n, pFactory);
    }

    public Document makeDocument() throws Exception {
        Document document = pFactory.newDocument();
        File file = new File(log);
        checkoutAllEntities();
        Agent agent=null;
        for (String f : file.list()) {
            if(!f.equals(".git")) {
                LOGGER.info("Reading file: " + f);
                File subFile = new File(log + f);
                BufferedReader reader = new BufferedReader(new FileReader(trace + "/systeminfo/" + f + ".txt"));
                String name = reader.readLine();
                String label = name + "\n";
                if (agent != null) {
                    String processId = getProcessorId(reader);
                    if (!agent.getId().equals(processId)) {
                        agent = pFactory.newAgent(qn(processId), label);
                    }
                } else {
                    agent = pFactory.newAgent(qn(getProcessorId(reader)), label);
                }
                Activity activity = null;
                String lastline = null;
                String line = null;
                String currentVerion = null;
                String logId = null;
                String commandStartAt = null;
                String lastVersion = null;
                File[] logs = subFile.listFiles();
                List fileList = Arrays.asList(logs);
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isDirectory() && o2.isFile())
                            return -1;
                        if (o1.isFile() && o2.isDirectory())
                            return 1;
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                int count = 0;
                if(f.startsWith("rerun")){
                    count=3;
                }
                for (File log : logs) {
                    count++;
                    if (count >= 3) {
                        String commandRecord = getCommandLineRecord(f, log.getName(),0);
                        if(commandRecord!=null) {
                            String[] commandRecordList = commandRecord.split("\\|");
                            if (commandRecordList.length > 3) {
                                currentVerion = commandRecordList[3];
                                if (commandRecordList.length == 5) {
                                    lastVersion = commandRecordList[4];
                                }
                            }
                            logId = commandRecordList[2];
                            commandStartAt = commandRecordList[0].split(" ")[0];

                        }


                        String command = null;
                        BufferedReader logReader = new BufferedReader(new FileReader(log));
                        String readline=null;
                        while ((readline = logReader.readLine()) != null) {
                            line=readline;
                            Pattern pattern = Pattern.compile("execve(.*) = 0");
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                if (activity != null) {
                                    createActivityRelated(activity, agent);
                                    entities_delete.clear();
                                    entities_to.clear();
                                    entities_from.clear();

                                    activity.setEndTime(getTime(commandStartAt+" "+geTimePoint(lastline)));
                                }

                                command = matcher.group(1);
                                activity = createActivity(commandStartAt+" "+geTimePoint(line), command);
                                createActivityAssociatedWith(agent, f, activity);
                            }

                            if (line.contains("read(")) {
                                if (line.contains(input)) {
                                    pattern = Pattern.compile("read\\(.?<(.*?/input/.*?)>,");
                                    matcher = pattern.matcher(line);
                                    if (matcher.find()) {
                                        String mat = matcher.group(1);
                                        if (!entities_from.containsKey(mat)) {
                                            Entity entity = getEntityFromList(mat, currentVerion);
                                            entities_from.put(mat, entity);
                                        }
                                    }

                                }

                            } else if (line.contains("write(")) {
                                if (line.contains(input)) {
                                    pattern = Pattern.compile("write\\(.?<(.*?/input/.*?)>,");
                                    matcher = pattern.matcher(line);
                                    if (matcher.find()) {
                                        String mat = matcher.group(1);
                                        if (!entities_to.containsKey(mat)) {
                                            Entity entity = getEntityFromList(mat, currentVerion);
                                            entities_to.put(mat, entity);
                                        }
                                    }

                                }

                            } else if (line.contains("unlinkat(")) {
                                if (line.contains(input)) {
                                    pattern = Pattern.compile("\"(.*?/input/.*?)\"");
                                    matcher = pattern.matcher(line);
                                    if (matcher.find()) {
                                        String mat = matcher.group(1);
                                        if (!entities_delete.containsKey(mat)) {
                                            Entity entity = getEntityFromList(mat, lastVersion);
                                            entities_delete.put(mat, entity);

                                        }
                                    }
                                }
                            }

                        }

                        createActivityRelated(activity, agent);
                        entities_delete.clear();
                        entities_to.clear();
                        entities_from.clear();
                    }

                    lastline = line;



                }
                XMLGregorianCalendar time= getTime(commandStartAt+" "+ geTimePoint(lastline));
                if(time!=null) {
                    activity.setEndTime(time);
                }
            }

            }
        createActivityInformedBy();
        document.getStatementOrBundle().addAll(statementOrBundles.values());
        document.setNamespace(ns);
        return document;
    }

    private String geTimePoint(String lastline){
        String endTime=null;
        try {
            endTime = lastline.split(" ")[0];
        } catch (Exception e) {
            if(endTime!=null) {
                endTime = lastline.split("  ")[0];
            }
        }
        return endTime;
    }

    private void createActivityInformedBy() throws IOException {
        Activity activity1 = null;
        Activity activity2 = null;
        int count=0;
        for(Activity activity:activitySet){
            if(count==0){
                activity1=activity;
                count++;
            }
            else {
                activity2 = activity;
                WasInformedBy wasInformedBy = pFactory.newWasInformedBy(null, activity2.getId(), activity1.getId());
                statementOrBundles.put(activity2.getId() + "-" + activity1.getId(), wasInformedBy);
                activity1 = activity2;
            }
        }

    }

    private Agent createActivityAssociatedWith(Agent agent, String f, Activity activity) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(trace + "/systeminfo/"+f + ".txt"));
        String name = reader.readLine();
        String label = name + "\n";
        String line = null;
        while (!(line = reader.readLine()).contains("firmware")) {
            label = label + line + "\n";
        }
        if (line != null) {
            WasAssociatedWith wasAssociatedWith = pFactory.newWasAssociatedWith(null, activity.getId(), agent.getId());
            statementOrBundles.put(agent.getId().toString(), agent);
            statementOrBundles.put(activity.getId() + "-" + agent.getId(), wasAssociatedWith);
        }
        return agent;
    }

    private void createActivityRelated(Activity activity, Agent agent) {
        for (Entity e : entities_to.values()) {
            WasGeneratedBy generatedBy = pFactory.newWasGeneratedBy(e, null,
                    activity);
            WasAttributedTo wasAttributedTo = pFactory.newWasAttributedTo(null, e.getId(), agent.getId());
            statementOrBundles.put( e.getId() + "-" + agent.getId(), wasAttributedTo);
            statementOrBundles.put(e.getId() + "-" + activity.getId(), generatedBy);
        }
        for (Entity e : entities_from.values()) {
            Used used = pFactory.newUsed(
                    activity.getId(),
                    e.getId());
            statementOrBundles.put(activity.getId() + "-" + e.getId(), used);
            for (Entity d : entities_to.values()) {
                WasDerivedFrom derivedFrom = pFactory.newWasDerivedFrom(
                        d.getId(),
                        e.getId()
                );
                statementOrBundles.put(d.getId() + "-" + e.getId(), derivedFrom);
            }
        }
        for (Entity e : entities_delete.values()) {
            WasInvalidatedBy wasInvalidatedBy = pFactory.newWasInvalidatedBy(null, e.getId(), activity.getId(), activity.getEndTime(), null);
                statementOrBundles.put(e.getId().toString(), wasInvalidatedBy);
        }
    }

    private Activity createActivity(String commandStartAt,String command) throws ParseException, DatatypeConfigurationException {

        XMLGregorianCalendar xmlStartDate = getTime(commandStartAt);
        Activity activity = null;
        Pattern pattern = Pattern.compile("(\\[\".*\"])");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()){
            command = matcher.group(1);
    }
        activity = pFactory.newActivity(qn("Activity"+UUID.randomUUID().toString().split("-")[1]),command.substring(1,command.length()-1));
        activity.setStartTime(xmlStartDate);

        statementOrBundles.put(activity.getId().toString(), activity);
        activitySet.add(activity);
        LOGGER.info(activity.toString());
        return activity;
    }

    private XMLGregorianCalendar getTime(String time) throws DatatypeConfigurationException, ParseException {
        DateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        time = time.split("\\.")[0];
try {
    Date startDate = format.parse(time);

    GregorianCalendar calenderStartDate = new GregorianCalendar();

    calenderStartDate.setTime(startDate);

    XMLGregorianCalendar xmlStartDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calenderStartDate);
    return xmlStartDate;
}catch (Exception e){
    return null;
}
    }

    public void doConversions(Document document, String file) throws IOException {
        InteropFramework intF = new InteropFramework();
        intF.writeDocument(file, document);
    }

    public void closingBanner() {
        statementOrBundles.clear();
        activitySet.clear();
        System.out.println("");
        System.out.println("*************************");
    }

    public void openingBanner() {
        System.out.println("*************************");
        System.out.println("* Converting document  ");
        System.out.println("*************************");
    }


    private void checkFirstInit(String commitID) throws IOException {
        LOGGER.info("Check files in init commit {}.", commitID);
        List<String> paths = versionChecker.getFiles(commitID);
        for (String path : paths) {
            LOGGER.debug("New file in commit: " + path);
            Entity generatedEntity = pFactory.newEntity(qn(contructePath(path,1)
            ));
            LOGGER.debug("Adding Entity: " + generatedEntity.getId());
            ProvoEntitiy provoEntitiy=new ProvoEntitiy(generatedEntity,1,path, commitID);
            setValue(generatedEntity, provoEntitiy);
            List<ProvoEntitiy> entitiyList=new ArrayList<>();
            entitiyList.add(provoEntitiy);
            entities.put(path,entitiyList);
            statementOrBundles.put("E-" + generatedEntity.getId(), generatedEntity);
        }
    }

    private void checkIfModified(String currentCommitId, String lastCommitId) throws IOException, GitAPIException, DatatypeConfigurationException {
        LOGGER.info("compare two commits: " + currentCommitId + "," + lastCommitId);
        List<DiffEntry> diff = versionChecker.getRevision(currentCommitId, lastCommitId);
        int commitIndex = versionChecker.getIndexOfVersionID(currentCommitId);
        for (DiffEntry e : diff) {
            LOGGER.debug("find {} file", e.getChangeType());
            if (e.getChangeType().equals(DiffEntry.ChangeType.MODIFY) || e.getChangeType().equals(DiffEntry.ChangeType.RENAME)) {
                List<ProvoEntitiy> provoEntitiys=entities.get(e.getOldPath());
                Entity usedEntity =provoEntitiys.get(provoEntitiys.size()-1).getEntity();
                Entity generatedEntity = pFactory.newEntity(qn(contructePath(e.getNewPath(),(provoEntitiys.size()+1))));
                ProvoEntitiy provoEntitiy=new ProvoEntitiy(generatedEntity,provoEntitiys.size()+1,e.getNewPath(),currentCommitId);

                setValue(generatedEntity, provoEntitiy);
                provoEntitiys.add(provoEntitiy);
                entities.put(e.getNewPath(),provoEntitiys);
                LOGGER.debug("Adding new entity: " + generatedEntity);
                statementOrBundles.put("E-" + generatedEntity.getId(), generatedEntity);

                if (usedEntity != null) {
                    Revision revision = new Revision();
                    revision.setUsedEntity(usedEntity.getId());
                    revision.setGeneratedEntity(generatedEntity.getId());
                    statementOrBundles.put("R-" + usedEntity.getId() + "-" + generatedEntity.getId(), revision);
                } else
                    LOGGER.error("can not find last version");

            } else if (e.getChangeType().equals(DiffEntry.ChangeType.ADD)) {
                Entity generatedEntity = pFactory.newEntity(qn(contructePath(e.getNewPath(),1)));
                List<ProvoEntitiy> provoEntitiys=new ArrayList<>();
                ProvoEntitiy provoEntitiy=new ProvoEntitiy(generatedEntity,1,e.getNewPath(),currentCommitId);
                provoEntitiys.add(provoEntitiy);
                setValue(generatedEntity, provoEntitiy);
                entities.put(e.getNewPath(),provoEntitiys);
                LOGGER.debug("Adding entity: " + generatedEntity);
                statementOrBundles.put("E-" + generatedEntity.getId(), generatedEntity);
            } else if (e.getChangeType().equals(DiffEntry.ChangeType.DELETE)) {
                List<ProvoEntitiy> provoEntitiys=entities.get(e.getOldPath());
                Entity deletedEntity = provoEntitiys.get(provoEntitiys.size()-1).getEntity();
                deletedEntity.setValue(new org.openprovenance.prov.xml.Value());
                LOGGER.info("Deleting file: " + e.getOldPath());
                if (deletedEntity != null) {
                    //for deleting file without running command
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTime(versionChecker.getCommitTime(currentCommitId));
                    XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                    WasInvalidatedBy wasInvalidatedBy = pFactory.newWasInvalidatedBy(null, deletedEntity.getId(), null, xmlDate, null);
                    LOGGER.info("------------------------------------------------------------------------------------------");
                    LOGGER.info("First:   I-" + deletedEntity.getId());
                    LOGGER.info("------------------------------------------------------------------------------------------");
                    statementOrBundles.put("I-" + deletedEntity.getId(), wasInvalidatedBy);
                } else
                   LOGGER.error("can not find last version");


            }
        }
    }

    private Entity setValue(Entity entity,ProvoEntitiy provoEntitiy)  {
        entity.setValue(pFactory.newValue(prosciProperties.readProperties("prosci")+path_mapping.getString("input") + provoEntitiy.getPath()+"\n"+provoEntitiy.getCommitID(),
                pFactory.getName().XSD_STRING));
        return entity;
    }

    private void checkoutAllEntities() throws IOException, GitAPIException, DatatypeConfigurationException {

        revCommits = versionChecker.reverseIterator();

        RevCommit firstCommit = revCommits.get(0);

        String firstCommitID = firstCommit.getName();
        checkFirstInit(firstCommitID);

        for (int i = 1; i < revCommits.size(); i++) {
            checkIfModified(revCommits.get(i).getName(), firstCommitID);
            firstCommitID = revCommits.get(i).getName();
        }
    }

    private Entity getEntityFromList(String mat, String currentVerion){

        Entity entity = findEntityInEntities(versionChecker.getIndexOfVersionID(currentVerion), mat.substring(
               input.length()));

        if (entity != null) {
            return entity;
        } else {
            LOGGER.error("Can not find Entity."+ mat);
        }
        return null;

    }


    private Entity findEntityInEntities(int commitIndex, String path) {
        Entity entity = null;
        List<ProvoEntitiy> provoEntitiys=entities.get(path);
            for(int i=provoEntitiys.size()-1;i>=0;i--){
                int currentCommitIndex=versionChecker.getIndexOfVersionID(provoEntitiys.get(i).getCommitID());
                if(currentCommitIndex<=commitIndex){
                    entity=provoEntitiys.get(i).getEntity();
                    return entity;
                }
            }
        return entity;
    }

    private String getCommandLineRecord(String filename, String logid, int retry) throws IOException, InterruptedException {
        LOGGER.info("Getting commandRecord for log from file {} with id {}",filename,logid);
        Scanner commandReader = new Scanner(new FileInputStream(trace + "/command.txt"));
        String commandRecord=null;
        while (commandReader.hasNextLine()) {
            commandRecord = commandReader.nextLine();
            String[] commandRecordList = commandRecord.split("\\|");
            if(commandRecordList[1].equalsIgnoreCase(filename)&&commandRecordList[2].equalsIgnoreCase(logid)){
                commandReader.close();
            return commandRecord;
            }
        }
        if(commandRecord==null&&retry<10) {
            Thread.sleep(2000);
            return getCommandLineRecord(filename, logid, retry + 1);
        }
        else{
            return null;
        }
    }

    class ActivityComp implements Comparator<Activity>{

        @Override
        public int compare(Activity e1, Activity e2) {
            return e1.getStartTime().compare(e2.getStartTime());
        }
    }

    private String getProcessorId(BufferedReader bufferedReader) throws Exception {
        String systemInfoLine=null;
        while ((systemInfoLine=bufferedReader.readLine())!=null) {
            if(systemInfoLine.startsWith("ProcessorID")){
            return systemInfoLine.substring(13);}
        }
        throw new Exception("Can't find ProcessorID");
    }

    private String contructePath(String path, int version){
        String[] pathString= path.split("\\.");

        String pathNew=pathString[0]+"_v"+version+"."+pathString[1];
        return pathNew;


    }



}
