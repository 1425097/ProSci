package at.ac.tuwien.ifs.prosci.graphvisualization;
import at.ac.tuwien.ifs.prosci.graphvisualization.entities.*;
import at.ac.tuwien.ifs.prosci.provstarter.helper.ProsciProperties;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.VersionChecker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class OntologyHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ProsciProperties prosciProperties;

    private Document doc;
    @Autowired
    private ResourceBundle path_mapping;
    @Autowired
    private VersionChecker versionChecker;

    Map<String, Activity> activitiesMap = new HashMap<>();
    Map<String, Entity> entitiesMap = new HashMap<>();
    Map<String, Agent> agentsMap = new HashMap<>();

    public void readElements() throws ParserConfigurationException, IOException, SAXException {

        File fXmlFile = new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.prov") + "prov.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
    }

    public List<Entity> readEntites() throws ParserConfigurationException, SAXException, IOException {
        List<Entity> entities = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:entity");
        logger.info("Length entites: " + nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String id = eElement.getAttribute("prov:id");
                if (id.length() > 0) {
                    String value = eElement.getElementsByTagName("prov:value").item(0).getTextContent();
                    String[] valueList=value.split("\n");
                    Entity entity = new Entity(id.replace("_2F","/").replace("__","_"), valueList[0].replace("_2F","/").replace("__","_"), valueList[1],null);
                    entities.add(entity);
                    entitiesMap.put(entity.getId(), entity);

                }
            }
        }

        return entities;
    }

    public List<Activity> readActivites() throws ParseException {
        List<Activity> activities = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:activity");
        logger.info("Length activities: " + nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String id = eElement.getAttribute("prov:id");

                if (id.length() > 0) {
                    String start = eElement.getElementsByTagName("prov:startTime").item(0).getTextContent();
                    String end = eElement.getElementsByTagName("prov:endTime").item(0).getTextContent();
                    String label = eElement.getElementsByTagName("prov:label").item(0).getTextContent();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date startDate = format.parse(start.replace("T", " "));
                    Date endDate = format.parse(end.replace("T", " "));
                    Activity activity = new Activity(id, label, null, startDate, endDate, new File(prosciProperties.readProperties("path.prosci.trace")  + "/systeminfo.txt"));

                    activities.add(activity);
                    activitiesMap.put(activity.getId(), activity);
                }
            }
        }
        logger.info("Reading activities: "+activities.size());
        return activities;
    }

    public List<Agent> readAgent() {
        List<Agent> agents = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:agent");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String id = eElement.getAttribute("prov:id");
                if (id.length() > 0) {
                    String label = eElement.getElementsByTagName("prov:label").item(0).getTextContent();
                    Agent agent = new Agent("Agent-"+id, label);
                    agents.add(agent);
                    agentsMap.put(agent.getId(), agent);
                }
            }
        }
        return agents;

    }

    public List<WasAssociatedWith> readWasAssociatedWith() {
        List<WasAssociatedWith> wasAssociatedWiths = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:wasAssociatedWith");
        logger.info("Length wasAssociatedWith:"+ nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                WasAssociatedWith wasAssociatedWith = new WasAssociatedWith(getActivity(nNode,"activity"),getAgent(nNode,"agent"));
                if(wasAssociatedWith.getActivity()!=null&&wasAssociatedWith.getAgent()!=null)
                    wasAssociatedWiths.add(wasAssociatedWith);
            }
        }
        return wasAssociatedWiths;

    }

    public List<WasDerivedFrom> readWasDerivedFrom() {
        List<WasDerivedFrom> wasDerivedFroms = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:wasDerivedFrom");
        logger.info("Length wasDerivedFrom:"+ nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                WasDerivedFrom wasDerivedFrom = new WasDerivedFrom(getEntity(nNode,"generatedEntity"),getEntity(nNode,"usedEntity"));
                if(wasDerivedFrom.getGeneratedEntity()!=null&&wasDerivedFrom.getUsedEntity()!=null)
                    wasDerivedFroms.add(wasDerivedFrom);

            }
        }
        return wasDerivedFroms;
    }

    public List<WasGeneratedBy> readWasGeneratedBy() {
        List<WasGeneratedBy> wasGeneratedBys = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:wasGeneratedBy");
        logger.info("Length wasGeneratedBy:"+ nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                WasGeneratedBy wasGeneratedBy = new WasGeneratedBy(getEntity(nNode,"entity"),getActivity(nNode,"activity"));
                if(wasGeneratedBy.getEntity()!=null&&wasGeneratedBy.getActivity()!=null)
                    wasGeneratedBys.add(wasGeneratedBy);

            }
        }

        return wasGeneratedBys;

    }


    public List<WasInformedBy> readWasInformedBy() {
        List<WasInformedBy> wasInformedBys = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:wasInformedBy");
        logger.info("Length wasInformedBy:"+ nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                WasInformedBy wasInformedBy = new WasInformedBy(getActivity(nNode,"informed"),getActivity(nNode,"informant"));
                if(wasInformedBy.getInformant()!=null&&wasInformedBy.getInformed()!=null)
                    wasInformedBys.add(wasInformedBy);

            }
        }
        return wasInformedBys;

    }


    public List<WasRevisonOf> readWasRevisonOf() {
        List<WasRevisonOf> wasRevisonOfs = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:wasRevisionOf");
        logger.info("Length wasRevisonOf:"+ nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                WasRevisonOf wasRevisonOf = new WasRevisonOf(getEntity(nNode,"generatedEntity"),getEntity(nNode,"usedEntity"));
                if(wasRevisonOf.getGeneratedEntity()!=null&&wasRevisonOf.getUsedEntity()!=null)
                    wasRevisonOfs.add(wasRevisonOf);

            }
        }
        return wasRevisonOfs;

    }

    public List<WasAttributedTo> readWasAttributedTo() {
        List<WasAttributedTo> wasAttributedTos = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:wasAttributedTo");
        logger.info("Length wasAttributedTo:"+ nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                WasAttributedTo wasAttributedTo = new WasAttributedTo(getEntity(nNode,"entity"),getAgent(nNode,"agent"));
                if(wasAttributedTo.getEntity()!=null&&wasAttributedTo.getAgent()!=null)
                    wasAttributedTos.add(wasAttributedTo);
            }
        }
        return wasAttributedTos;

    }

    public List<Used> readUsed() {
        List<Used> useds = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("prov:used");
        logger.info("Length used:"+ nList.getLength());
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Used used = new Used(getEntity(nNode,"entity"),getActivity(nNode,"activity"));
                if(used.getEntity()!=null&&used.getActivity()!=null)
                    useds.add(used);
            }
        }
        return useds;

    }

   private Entity getEntity(Node node, String tag){
       Element eElement = (Element) node;
       NodeList entities = eElement.getElementsByTagName("prov:"+tag);
       logger.info("Length entities:"+entities.getLength());
       if (entities != null && entities.item(0) != null && entities.item(0).getNodeType() == Node.ELEMENT_NODE) {
           Element generated = (Element) entities.item(0);
           String ref = generated.getAttribute("prov:ref");
           Entity refEntity= entitiesMap.get(ref.replace("_2F","/").replace("__","_"));
           return refEntity;
       }
       else return null;

   }

   private Activity getActivity(Node node, String tag){
       Element eElement = (Element) node;
       NodeList activities = eElement.getElementsByTagName("prov:"+tag);
       logger.info("Length activities:"+activities.getLength());
       if (activities != null && activities.item(0) != null && activities.item(0).getNodeType() == Node.ELEMENT_NODE) {
           Element activity = (Element) activities.item(0);
           String ref = activity.getAttribute("prov:ref");
           Activity refActivity = activitiesMap.get(ref);
           return refActivity;
       }
       else return null;
   }

   private Agent getAgent(Node node, String tag){
       Element eElement = (Element) node;
       NodeList agents = eElement.getElementsByTagName("prov:"+tag);
       logger.info("Length agents:"+agents.getLength());
       if (agents != null && agents.item(0) != null && agents.item(0).getNodeType() == Node.ELEMENT_NODE) {
           Element agent = (Element) agents.item(0);
           String ref = agent.getAttribute("prov:ref");
           Agent refAgent = agentsMap.get("Agent-"+ref);
           return refAgent;
       }
       else return null;


   }

   public ArrayList<Activity> sort() throws IOException, GitAPIException, ParseException {
       List<Activity> activities=readActivites();
       ArrayList<RevCommit> revCommits=versionChecker.reverseIterator();
       ArrayList<Activity> activities_temp=new ArrayList<>();
       for(RevCommit revCommit:revCommits){
           logger.info("commit id: "+revCommit.getName());
           activities_temp.addAll(findActivityForSort(activities,revCommit.getName()));
       }
       logger.info("Sorting: "+activities_temp.size());
       return activities_temp;
   }

   private List<Activity> findActivityForSort(List<Activity> activities, String version){
       List<Activity> found=new ArrayList<>();
       for(Activity activity:activities){
        if(activity.getVersion().equals(version)){
            found.add(activity);
        }
       }
       return found;
   }



}
