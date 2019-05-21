package at.ac.tuwien.ifs.prosci.graphvisualization;

import at.ac.tuwien.ifs.prosci.graphvisualization.entities.*;
import at.ac.tuwien.ifs.prosci.graphvisualization.exception.TechnicalException;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.CommandExtracter;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.ProsciProperties;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.Trace;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.OntologyCreator;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.VersionChecker;
import com.google.common.base.Function;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.openprovenance.prov.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class Controller implements Initializable {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @FXML
    public TextArea simulator;
    @FXML
    public JFXButton graphButton;
    @FXML
    public Pane pane;
    @FXML
    public JFXListView<Button> list;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public HBox hBox;
    @FXML
    public VBox vBox_link;
    @FXML
    public JFXButton filesButton;
    @FXML
    public JFXButton agentsButton;
    @FXML
    public JFXButton activitiesButton;
    @FXML
    public VBox vBox_right;
    @FXML
    public JFXListView listView;
    @FXML
    public JFXButton title;
    public TextField searchtext;
    public JFXButton searchbutton;
    @Autowired
    OntologyHandler ontologyHandler;
    private String mode="Overview";

    @Bean
    public GraphDrawing getGraphDrawing(){
        return new GraphDrawing();
    }

    @Autowired
    GraphDrawing graphDrawing;
    @Autowired
    private ResourceBundle path_mapping ;
    private List<Activity> activities;
    private List<Entity> entities;
    private List<Agent> agents;
    private List<WasGeneratedBy> wasGeneratedBys;
    private List<WasAssociatedWith> wasAssociatedWiths;
    private List<WasInformedBy> wasInformedBys;
    private List<Used> useds;
    private List<WasAttributedTo> wasAttributedTos;
    private List<WasDerivedFrom> wasDerivedFroms;
    private List<WasRevisonOf> wasRevisonOfs;
    @Autowired
    private ProsciProperties prosciProperties;
    @Autowired
    private VersionChecker versionChecker;
    @Autowired
    private OntologyCreator ontologyCreator;
    @Autowired
    private Trace trace;
    @Autowired
    private CommandExtracter commandExtracter;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FileInputStream input = null;
        try {
            configWindow();
            simulator = new TextArea();
            String file_xml = prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.prov") + "prov.xml";
            ontologyCreator.init();
            ontologyCreator.openingBanner();
            Document document = ontologyCreator.makeDocument();
            ontologyCreator.doConversions(document, file_xml);
            ontologyCreator.closingBanner();

            ontologyHandler.readElements();

            activities = new ArrayList<>();
            entities = new ArrayList<>();
            agents = new ArrayList<>();
            wasGeneratedBys = new ArrayList<>();
            wasAssociatedWiths = new ArrayList<>();
            wasInformedBys = new ArrayList<>();
            useds = new ArrayList<>();
            wasAttributedTos = new ArrayList<>();
            wasDerivedFroms = new ArrayList<>();
            wasRevisonOfs = new ArrayList<>();

            activities = ontologyHandler.readActivites();
            entities = ontologyHandler.readEntites();
            agents = ontologyHandler.readAgent();
            wasGeneratedBys = ontologyHandler.readWasGeneratedBy();
            wasAssociatedWiths = ontologyHandler.readWasAssociatedWith();
            wasInformedBys = ontologyHandler.readWasInformedBy();
            useds = ontologyHandler.readUsed();
            wasAttributedTos = ontologyHandler.readWasAttributedTo();
            wasDerivedFroms = ontologyHandler.readWasDerivedFrom();
            wasRevisonOfs = ontologyHandler.readWasRevisonOf();

            list = new JFXListView<>();

            initListView(list);

            pane.setStyle("-fx-background-color: #212121;");

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void configWindow() {
        anchorPane.setMinSize(1000, 600);
        hBox.prefWidthProperty().bind(anchorPane.widthProperty());
        hBox.prefHeightProperty().bind(anchorPane.heightProperty());
        vBox_link.prefWidthProperty().bind(hBox.widthProperty().multiply(0.25));
        vBox_link.prefHeightProperty().bind(hBox.heightProperty());
        vBox_right.prefWidthProperty().bind(hBox.widthProperty().multiply(0.75));
        vBox_right.prefHeightProperty().bind(hBox.heightProperty());
        listView.prefWidthProperty().bind(pane.widthProperty());
        listView.prefHeightProperty().bind(pane.heightProperty());
        pane.prefWidthProperty().bind(vBox_right.widthProperty());
        pane.prefHeightProperty().bind(vBox_right.heightProperty());
        graphButton.prefWidthProperty().bind(vBox_link.widthProperty());
        graphButton.prefHeightProperty().bind(vBox_link.heightProperty().multiply(0.1));
        filesButton.prefWidthProperty().bind(vBox_link.widthProperty());
        filesButton.prefHeightProperty().bind(vBox_link.heightProperty().multiply(0.1));
        agentsButton.prefWidthProperty().bind(vBox_link.widthProperty());
        agentsButton.prefHeightProperty().bind(vBox_link.heightProperty().multiply(0.1));
        activitiesButton.prefWidthProperty().bind(vBox_link.widthProperty());
        activitiesButton.prefHeightProperty().bind(vBox_link.heightProperty().multiply(0.1));
        title.prefWidthProperty().bind(vBox_link.widthProperty());
        title.prefHeightProperty().bind(vBox_link.heightProperty().multiply(0.3));
        title.setDisable(true);
    }

    private void initListView(JFXListView<Button> list) {
        list.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                if (list.isExpanded()) {
                    list.setStyle("-jfx-expanded: false;");
                    list.setStyle("-j-jfx-vertical-gap: 0;");
                    list.depthProperty().set(0);

                } else {
                    list.setStyle("-jfx-expanded: true;");
                    list.setStyle("-j-jfx-vertical-gap: 5;");
                    list.setExpanded(true);
                    list.depthProperty().set(3);
                }
            }
        });

        list.prefHeightProperty().bind(pane.heightProperty());
        list.prefWidthProperty().bind(pane.widthProperty());
    }

    private void configButton(Button button, int type) {

        button.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        Ontology o = searchOntology(button.getText(), type);
                        showDetail(o);
                    }


                });
    }


    private int getIndexOfTheOntogoly(Ontology o) {
        if (o instanceof Entity) {
            for (int i = 0; i < entities.size(); i++) {
                if (entities.get(i).getId().equals(o.getId())) {
                    return i;
                }
            }
        } else if (o instanceof Activity) {
            for (int i = 0; i < activities.size(); i++) {
                if (activities.get(i).getId().equals(o.getId())) {
                    return i;
                }
            }
        } else if (o instanceof Agent) {
            for (int i = 0; i < agents.size(); i++) {
                if (agents.get(i).getId().equals(o.getId())) {
                    return i;
                }
            }
        }

        return -1;
    }

    private void checkWasAttributedTo(Accordion accordion, String id, int type) {
        if (type == 1) {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Was attributed to", listView);
            accordion.getPanes().add(t1);
            for (WasAttributedTo i : wasAttributedTos) {
                if (i.getEntity().getId().equals(id)) {
                    printAgent(i.getAgent(), listView);
                }
            }
        } else {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Attributed the files", listView);
            accordion.getPanes().add(t1);
            for (WasAttributedTo i : wasAttributedTos) {
                if (i.getAgent().getId().equals(id)) {
                    printEntity(i.getEntity(), listView);
                }
            }

        }


    }

    private void checkWasDerivedFrom(Accordion accordion, String id) {
        JFXListView<Button> listView = new JFXListView<>();
        TitledPane t1 = new TitledPane("Was derived from", listView);
        accordion.getPanes().add(t1);
        for (WasDerivedFrom i : wasDerivedFroms) {

            if (i.getGeneratedEntity().getId().equals(id)) {
                printEntity(i.getUsedEntity(), listView);
            }
        }
        JFXListView<Button> listView2 = new JFXListView<>();
        TitledPane t2 = new TitledPane("Derived the entities", listView2);
        accordion.getPanes().add(t2);
        for (WasDerivedFrom i : wasDerivedFroms) {
            if (i.getUsedEntity().getId().equals(id)) {
                printEntity(i.getGeneratedEntity(), listView2);
            }
        }


    }

    private void checkWasRevisionOf(Accordion accordion, String id) {
        JFXListView<Button> listView = new JFXListView<>();
        TitledPane t1 = new TitledPane("Last version", listView);
        accordion.getPanes().add(t1);
        for (WasRevisonOf i : wasRevisonOfs) {
            if (i.getGeneratedEntity().getId().equals(id)) {
                printEntity(i.getUsedEntity(), listView);
            }
        }
        JFXListView<Button> listView2 = new JFXListView<>();
        TitledPane t2 = new TitledPane("Next version", listView2);
        accordion.getPanes().add(t2);
        for (WasRevisonOf i : wasRevisonOfs) {
            if (i.getUsedEntity().getId().equals(id)) {
                printEntity(i.getGeneratedEntity(), listView2);
            }
        }

    }

    private void checkUsed(Accordion accordion, String id, int type) {
        if (type == 1) {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Used files", listView);
            accordion.getPanes().add(t1);
            for (Used i : useds) {
                if (i.getActivity().getId().equals(id)) {
                    printEntity(i.getEntity(), listView);
                }
            }
        } else {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Was used by activitys", listView);
            accordion.getPanes().add(t1);
            for (Used i : useds) {
                if (i.getEntity().getId().equals(id)) {
                    printActivity(i.getActivity(), listView);
                }
            }

        }

    }

    private void checkWasAssociatedWith(Accordion accordion, String id, int type) {
        JFXListView<Button> listView = new JFXListView<>();
        TitledPane t1 = new TitledPane("Was associated with", listView);
        accordion.getPanes().add(t1);
        for (WasAssociatedWith a : wasAssociatedWiths) {
            if (type == 1) {
                if (a.getActivity().getId().equals(id)) {
                    printAgent(a.getAgent(), listView);

                }
            } else {
                if (a.getAgent().getId().equals(id)) {
                    printActivity(a.getActivity(), listView);
                }

            }
        }

    }

    private void getInfo(JFXButton jfxButton, String text, String info) {
        jfxButton.setText(text);
        jfxButton.setOnMouseClicked(
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        StackPane stackPane = new StackPane();
                        JFXDialogLayout content = new JFXDialogLayout();
                        content.setBody(new TextArea(info));
                        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER, true);
                        stackPane.setPadding(new Insets(100, 200, 100, 100));
                        dialog.setAlignment(Pos.CENTER);
                        JFXButton button = new JFXButton("OKey");
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                dialog.close();
                            }
                        });
                        content.setActions(button);
                        stackPane.setMaxWidth(pane.getWidth());
                        stackPane.setMaxHeight(pane.getHeight());
                        pane.getChildren().add(stackPane);
                        dialog.show();
                    }
                });
    }

    private void checkWasInformedBy(Accordion accordion, String id) {
        JFXListView<Button> listView = new JFXListView<>();
        TitledPane t1 = new TitledPane("Was informed by activities", listView);
        accordion.getPanes().add(t1);
        for (WasInformedBy i : wasInformedBys) {

            if (i.getInformed().getId().equals(id)) {
                printActivity(i.getInformant(), listView);
            }

        }
        JFXListView<Button> listView2 = new JFXListView<>();
        TitledPane t2 = new TitledPane("Informed the activities", listView2);
        accordion.getPanes().add(t2);
        for (WasInformedBy i : wasInformedBys) {

            if (i.getInformant().getId().equals(id)) {
                printActivity(i.getInformed(), listView2);
            }
        }

    }

    private void checkWasGeneratedBy(Accordion accordion, String id, int type) {
        if (type == 1) {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Was generated by", listView);
            accordion.getPanes().add(t1);
            for (WasGeneratedBy i : wasGeneratedBys) {
                if (i.getEntity().getId().equals(id)) {
                    printActivity(i.getActivity(), listView);

                }
            }
        } else {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Generated the entities", listView);
            accordion.getPanes().add(t1);
            for (WasGeneratedBy i : wasGeneratedBys) {
                if (i.getActivity().getId().equals(id)) {
                    printEntity(i.getEntity(), listView);
                }

            }
        }
    }


    private Ontology searchOntology(String id, int type) {
        Ontology ontology = null;
        switch (type) {
            case 1:
                for (Ontology o : activities) {
                    if (o.getId().equals(id)) {
                        return o;
                    }
                }
                break;
            case 2:
                for (Ontology o : entities) {
                    if (o.getId().equals(id)) {
                        return o;
                    }
                }
                break;
            case 3:
                for (Ontology o : agents) {
                    if (o.getId().equals(id)) {
                        return o;
                    }
                }
                break;

        }
        return null;
    }

    private void addLoadFileButtonHandler(Button button, Entity entity) {
        button.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        try {
                            String filename = entity.getName();

                            String pattern = Pattern.quote(System.getProperty("file.separator"));
                            String[] splittedFileName = filename.split(pattern);
                            filename = splittedFileName[splittedFileName.length - 1];
                            File f = new File(prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.version")+filename);
                            if (f.exists() && !f.isDirectory()) {
                                int decisionCode = createConfirmation(
                                        "File exits",
                                        "A file with name " + filename + " exits in the path [" + f.getPath() + "], do you want to overwrite it?");
                                if (decisionCode == 1) {
                                    versionChecker.getFile(entities.get(getIndexOfTheOntogoly(entity)).getVersion(),filename, entity.getId().substring(7));
                                    createInformationDialogs("File successfully load", "The selected file is successfully loaded [" + f.getPath() + "].");
                                }
                            } else {
                                versionChecker.getFile(entities.get(getIndexOfTheOntogoly(entity)).getVersion(),filename, entity.getId().substring(7));
                                createInformationDialogs("File successfully load", "The selected file is successfully loaded [" + f.getPath() + "].");
                            }

                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    private void addRerunButtonHandler(Button rerun_button, Activity activity) {
        rerun_button.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        LOGGER.info("Rerun activity: " + activity.getCommand());
                        String command = activity.getCommand();
                        TextField textField = new TextField();

                        try {
                            textField.setText(commandExtracter.getCommand(activity.getCommand()));
                        } catch (TechnicalException e1) {
                            e1.printStackTrace();
                        }

                        Button button = new Button("Enter");
                        VBox vBox = new VBox();
                        vBox.getChildren().add(simulator);
                        vBox.getChildren().add(textField);
                        vBox.getChildren().add(button);

                        StackPane stackPane = new StackPane();
                        stackPane.setPadding(new Insets(200, 200, 100, 100));
                        JFXDialogLayout content = new JFXDialogLayout();
                        content.setBody(vBox);
                        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER, true);
                        JFXButton buttonOkey = new JFXButton("OKey");
                        buttonOkey.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                dialog.close();
                                initialize(null, null);

                            }
                        });
                        content.setActions(buttonOkey);
                        stackPane.setMaxWidth(pane.getWidth());
                        stackPane.setMaxHeight(pane.getHeight());
                        pane.getChildren().add(stackPane);
                        addEnterButtonHandler(button, textField);
                        dialog.show();
                    }
                });
    }

    public void overview(MouseEvent mouseEvent) throws IOException, ParserConfigurationException, SAXException, ParseException {
        searchtext.setVisible(true);
        searchbutton.setVisible(true);
        mode="Overview";
        SwingNode swingNode=new SwingNode();
        swingNode.setContent(graphDrawing.draw(false));
        pane.prefHeightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                try {
                    swingNode.setContent(graphDrawing.draw(false));
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                pane.getChildren().clear();
                pane.getChildren().add(swingNode);
            }
        });
        pane.prefWidthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                try {
                    swingNode.setContent(graphDrawing.draw(false));
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                pane.getChildren().clear();
                pane.getChildren().add(swingNode);
            }
        });


        pane.getChildren().clear();
        pane.getChildren().add(swingNode);

    }

    public void files(MouseEvent mouseEvent) throws  ParserConfigurationException, SAXException, IOException {
        searchtext.setVisible(true);
        searchbutton.setVisible(true);
        mode="Files";
        entities = ontologyHandler.readEntites();
        initListView(list);
        list.getItems().clear();
        ObservableList arrayList= FXCollections.observableArrayList();
        SortedList<Button> sortedList;

        for (int i = 0; i < entities.size(); i++) {
            JFXButton button = new JFXButton();
            button.setText(entities.get(i).getId());
            FontAwesomeIconView fontAwesomeIcon = new FontAwesomeIconView();
            fontAwesomeIcon.setGlyphName("FILE_TEXT_ALT");
            fontAwesomeIcon.setFill(Color.WHITE);
            configButton(button, 2);
            button.setGraphic(fontAwesomeIcon);
            button.prefWidthProperty().bind(list.widthProperty().multiply(0.93));
            arrayList.add(button);

        }
        sortedList=new SortedList<Button>(arrayList);
        sortedList.setComparator(new Comparator<Button>(){
            @Override
            public int compare(Button o1, Button o2) {
                return o1.getText().compareToIgnoreCase(o2.getText());
            }
        });
        list.getItems().addAll(sortedList);
        pane.getChildren().clear();
        pane.getChildren().add(list);

    }

    public void agents(MouseEvent mouseEvent) {
        searchtext.setVisible(true);
        searchbutton.setVisible(true);
        mode="Agents";
        agents = ontologyHandler.readAgent();
        initListView(list);
        list.getItems().clear();
        for (int i = 0; i < agents.size(); i++) {
            JFXButton button = new JFXButton();
            button.setText(agents.get(i).getId());
            configButton(button, 3);
            FontAwesomeIconView fontAwesomeIcon = new FontAwesomeIconView();
            fontAwesomeIcon.setGlyphName("USER_MD");
            fontAwesomeIcon.setFill(Color.WHITE);
            button.setGraphic(fontAwesomeIcon);
            button.prefWidthProperty().bind(list.widthProperty().multiply(0.93));
            list.getItems().add(button);
        }
        pane.getChildren().clear();
        pane.getChildren().add(list);
    }

    public void activities(MouseEvent mouseEvent) throws ParseException {
        searchtext.setVisible(true);
        searchbutton.setVisible(true);
        mode="Activities";
        activities = ontologyHandler.readActivites();
        initListView(list);
        list.getItems().clear();
        for (int i = 0; i < activities.size(); i++) {
            JFXButton button = new JFXButton();
            button.prefWidthProperty().bind(list.widthProperty().multiply(0.93));
            button.setText(activities.get(i).getId());
            configButton(button, 1);
            FontAwesomeIconView fontAwesomeIcon = new FontAwesomeIconView();
            fontAwesomeIcon.setGlyphName("BLACK_TIE");
            fontAwesomeIcon.setFill(Color.WHITE);
            button.setGraphic(fontAwesomeIcon);
            list.getItems().add(button);

        }
        pane.getChildren().clear();
        pane.getChildren().add(list);
    }

    public void search(MouseEvent mouseEvent) throws ParserConfigurationException, SAXException, ParseException, IOException {
        JFXListView<Button> toshow=new JFXListView<>();
        initListView(toshow);
        switch (mode){
            case "Overview":
                SwingNode swingNode=new SwingNode();
                swingNode.setContent(graphDrawing.draw(true));
                pane.getChildren().clear();
                pane.getChildren().add(swingNode);
                break;

            case "Files":
                for(Button b:list.getItems()){
                    if(b.getText().contains(searchtext.getText())||((Entity)searchOntology(b.getText(),2)).getName().contains(searchtext.getText())){
                        toshow.getItems().add(b);
                    }
                }
                break;
            case "Agents":
                for(Button b:list.getItems()){
                    if(b.getText().contains(searchtext.getText())||((Agent)searchOntology(b.getText(),3)).getDetail().contains(searchtext.getText())){
                        toshow.getItems().add(b);
                    }
                }
                break;
            case "Activities":
                for(Button b:list.getItems()){
                    if(b.getText().contains(searchtext.getText())
                            ||((Activity)searchOntology(b.getText(),1)).getCommand().contains(searchtext.getText())
                            ||((Activity)searchOntology(b.getText(),1)).getStartTime().toString().contains(searchtext.getText())
                            ||((Activity)searchOntology(b.getText(),1)).getEndTime().toString().contains(searchtext.getText())){
                        toshow.getItems().add(b);
                    }
                }
                break;

        }

        if(!mode.equals("Overview")){
            pane.getChildren().clear();
            pane.getChildren().add(toshow);
        }


    }

    class StreamGobbler extends Thread {
        private InputStream in;

        private StreamGobbler(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(in));
                int i;
                while ((i = input.read()) != -1) {
                    // converts integer to character
                    char c = (char) i;
                    Platform.runLater(() -> simulator.appendText("" + c));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addEnterButtonHandler(Button button, TextField textField) {
        button.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        if (textField.getText().length() > 0) {
                            try {
                                Process process = trace.traceProcess(textField.getText());
                                textField.clear();
                                StreamGobbler pOut = new StreamGobbler(process.getInputStream());
                                pOut.start();
                                OutputStream out = process.getOutputStream();
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
                                String command = textField.getText();
                                textField.clear();
                                if (command.length() != 0) {
                                    bw.write(command + "\n");
                                    bw.flush();
                                    simulator.appendText(command + "\n");
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }


    private int createConfirmation(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.setResizable(true);


        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return 1;
        } else {
            return 0;
        }
    }

    private void createInformationDialogs(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.showAndWait();

    }

    private void printActivity(Activity activity, JFXListView<Button> listView) {
        JFXButton jfxButton = new JFXButton();
        String info = "Command: " + activity.getCommand() + "\n" +
                "Start time: " + activity.getStartTime() + "\n" +
                "End time: " + activity.getEndTime() + "\n";
        getInfo(jfxButton, activity.getId(), info);
        listView.getItems().add(jfxButton);
    }

    private void printEntity(Entity entity, JFXListView<Button> listView) {
        JFXButton jfxButton = new JFXButton();
        String info = "Path: " + entity.getName() + "\n";
        getInfo(jfxButton, entity.getId(), info);
        listView.getItems().add(jfxButton);
    }

    private void printAgent(Agent agent, JFXListView<Button> listView) {
        JFXButton jfxButton = new JFXButton();
        String info = "Detail: " + agent.getDetail() + "\n";
        getInfo(jfxButton, agent.getId(), info);
        listView.getItems().add(jfxButton);
    }

    public void showDetail(Ontology o) {
        searchtext.setVisible(false);
        searchbutton.setVisible(false);
        VBox vBox = new VBox(10);
        TextArea baseInfo = new TextArea();
        baseInfo.prefHeightProperty().bind(vBox_right.heightProperty().multiply(0.3));
        baseInfo.prefWidthProperty().bind(vBox_right.widthProperty());

        baseInfo.setLayoutX(13);
        baseInfo.setLayoutY(560);
        baseInfo.setStyle("-fx-control-inner-background:#000000; -fx-font-family: Consolas;  -fx-highlight-text-fill: #000000; -fx-text-fill: white; -fx-border-color: black;");
        Accordion accordion = new Accordion();
        accordion.setPrefWidth(pane.getWidth());
        Button rerun_button = new JFXButton("Restore");
        rerun_button.setPrefHeight(25);
        rerun_button.prefWidthProperty().bind(vBox_right.widthProperty());
        rerun_button.setAlignment(Pos.BOTTOM_CENTER);
        rerun_button.setTextFill(Color.WHITE);
        rerun_button.setStyle("-fx-background-color: #4f3221;");
        FontAwesomeIconView fontAwesomeIcon = new FontAwesomeIconView();
        fontAwesomeIcon.setGlyphName("UNDO");
        fontAwesomeIcon.setFill(Color.WHITE);
        rerun_button.setGraphic(fontAwesomeIcon);
        pane.getChildren().clear();
        if (o instanceof Activity) {
            baseInfo.setText(
                    "ID: " + o.getId() + "\n" +
                            "Command: " + ((Activity) o).getCommand() + "\n" +
                            "Start time: " + ((Activity) o).getStartTime() + "\n" +
                            "End time: " + ((Activity) o).getEndTime() + "\n");
            checkWasAssociatedWith(accordion, o.getId(), 1);
            checkWasInformedBy(accordion, o.getId());
            checkWasGeneratedBy(accordion, o.getId(), 2);
            checkUsed(accordion, o.getId(), 1);

            addRerunButtonHandler(rerun_button, (Activity) o);

        } else if (o instanceof Entity) {
            baseInfo.setText("ID: " + o.getId() + "\n" +
                    "Path: " + ((Entity) o).getName() + "\n");
            checkUsed(accordion, o.getId(), 2);
            checkWasAttributedTo(accordion, o.getId(), 1);
            checkWasDerivedFrom(accordion, o.getId());
            checkWasGeneratedBy(accordion, o.getId(), 1);
            checkWasRevisionOf(accordion, o.getId());
            addLoadFileButtonHandler(rerun_button, (Entity) o);

        } else if (o instanceof Agent) {
            baseInfo.setText("ID: " + o.getId() + "\n" +
                    "Description: \n" + ((Agent) o).getDetail() + "\n");
            checkWasAssociatedWith(accordion, o.getId(), 2);
            checkWasAttributedTo(accordion, o.getId(), 2);

        }
        for (int i = 0; i < accordion.getPanes().size(); i++) {
            if (((JFXListView<Button>) accordion.getPanes().get(i).getContent()).getItems().size() == 0) {
                accordion.getPanes().get(i).setCollapsible(false);
            }
        }
        vBox.getChildren().add(baseInfo);
        vBox.getChildren().add(accordion);
        if(o instanceof Entity){
            vBox.getChildren().add(rerun_button);
        }



        pane.getChildren().add(vBox);


    }

    public class GraphDrawing extends JPanel {
        @Autowired
        private OntologyHandler ontologyHandler;

        private List<Entity> entities;

        private List<Agent> agents;

        private List<Activity> activities;

        VisualizationViewer<String, String> vv;

        public VisualizationViewer<String, String> draw(boolean withSearch) throws ParserConfigurationException, SAXException, IOException, ParseException {

            setLayout(new BorderLayout());
            Layout<String, String> layout = new FRLayout2<>(getGraph(withSearch));

            vv = new VisualizationViewer<String, String>(new DefaultVisualizationModel<>(layout), new Dimension((int)pane.getWidth(), (int)pane.getHeight()));
            vv.setBackground(java.awt.Color.white);


            vv.getRenderContext().setVertexFillPaintTransformer(new VertexPaint());
            vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
            vv.addGraphMouseListener(new TestGraphMouseListener<>());
            vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
            DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
            gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
            vv.setGraphMouse(gm);
            return vv;
        }

        private Graph getGraph(boolean withSearch) throws ParserConfigurationException, SAXException, IOException, ParseException {
            ontologyHandler.readElements();
            entities = ontologyHandler.readEntites();
            agents = ontologyHandler.readAgent();
            activities = ontologyHandler.readActivites();

            Graph<String, String> g = new SparseMultigraph<String, String>();
            ArrayList<String> color_red=new ArrayList<>();
            for (Entity entity : entities) {
                if(withSearch){
                    if(entity.getId().contains(searchtext.getText())||(entity.getName().contains(searchtext.getText()))){
                        g.addVertex(entity.getId()+"*");
                        color_red.add(entity.getId());
                    }
                    else {
                        g.addVertex(entity.getId());
                    }

                }else {
                    g.addVertex(entity.getId());
                }
            }
            for (Agent agent : agents) {
                if(withSearch){
                    if(agent.getId().contains(searchtext.getText())||(agent.getDetail().contains(searchtext.getText()))){
                        g.addVertex(agent.getId()+"*");
                        color_red.add(agent.getId());
                    }else {
                        g.addVertex(agent.getId());
                    }


                }else {
                    g.addVertex(agent.getId());
                }
            }
            for (Activity activity : activities) {
                if(withSearch){
                    if(activity.getId().contains(searchtext.getText())
                            ||(activity.getCommand().contains(searchtext.getText()))
                            ||activity.getStartTime().toString().contains(searchtext.getText())
                            ||activity.getEndTime().toString().contains(searchtext.getText())){
                        g.addVertex(activity.getId()+"*");
                        color_red.add(activity.getId());
                    }else {
                        g.addVertex(activity.getId());
                    }

                }else {
                    g.addVertex(activity.getId());
                }
            }
            int count = 0;
            for (WasAssociatedWith wasAssociatedWith : ontologyHandler.readWasAssociatedWith()) {

                ((SparseMultigraph<String, String>) g).addEdge("wasAssociatedWith-" + count,
                        color_red.contains(wasAssociatedWith.getActivity().getId())?wasAssociatedWith.getActivity().getId()+"*":wasAssociatedWith.getActivity().getId(),
                        color_red.contains(wasAssociatedWith.getAgent().getId())?wasAssociatedWith.getAgent().getId()+"*":wasAssociatedWith.getAgent().getId(), EdgeType.UNDIRECTED);
                count++;
            }
            for (WasAttributedTo wasAttributedTo : ontologyHandler.readWasAttributedTo()) {

                ((SparseMultigraph<String, String>) g).addEdge("wasAttributedTo-" + count,
                        color_red.contains(wasAttributedTo.getAgent().getId())?wasAttributedTo.getAgent().getId()+"*":wasAttributedTo.getAgent().getId(),
                        color_red.contains(wasAttributedTo.getEntity().getId())?wasAttributedTo.getEntity().getId()+"*":wasAttributedTo.getEntity().getId(), EdgeType.UNDIRECTED);
                count++;
            }
            for (WasDerivedFrom wasDerivedFrom : ontologyHandler.readWasDerivedFrom()) {

                ((SparseMultigraph<String, String>) g).addEdge("wasDerivedFrom-" + count,
                        color_red.contains(wasDerivedFrom.getGeneratedEntity().getId())?wasDerivedFrom.getGeneratedEntity().getId()+"*":wasDerivedFrom.getGeneratedEntity().getId(),
                        color_red.contains(wasDerivedFrom.getUsedEntity().getId())?wasDerivedFrom.getUsedEntity().getId()+"*":wasDerivedFrom.getUsedEntity().getId(), EdgeType.UNDIRECTED);
                count++;
            }

            for (WasGeneratedBy wasGeneratedBy : ontologyHandler.readWasGeneratedBy()) {

                ((SparseMultigraph<String, String>) g).addEdge("wasGeneratedBy-" + count,
                        color_red.contains(wasGeneratedBy.getActivity().getId())?wasGeneratedBy.getActivity().getId()+"*":wasGeneratedBy.getActivity().getId(),
                        color_red.contains(wasGeneratedBy.getEntity().getId())?wasGeneratedBy.getEntity().getId()+"*":wasGeneratedBy.getEntity().getId(), EdgeType.UNDIRECTED);
                count++;
            }
            for (WasInformedBy wasInformedBy : ontologyHandler.readWasInformedBy()) {

                ((SparseMultigraph<String, String>) g).addEdge("wasInformedBy-" + count,
                        color_red.contains(wasInformedBy.getInformant().getId())?wasInformedBy.getInformant().getId()+"*":wasInformedBy.getInformant().getId(),
                        color_red.contains(wasInformedBy.getInformed().getId())?wasInformedBy.getInformed().getId()+"*":wasInformedBy.getInformed().getId(),EdgeType.UNDIRECTED);
                count++;
            }
            for (WasRevisonOf wasRevisonOf : ontologyHandler.readWasRevisonOf()) {

                ((SparseMultigraph<String, String>) g).addEdge("wasRevisonOf-" + count,
                        color_red.contains(wasRevisonOf.getGeneratedEntity().getId())?wasRevisonOf.getGeneratedEntity().getId()+"*":wasRevisonOf.getGeneratedEntity().getId(),
                        color_red.contains(wasRevisonOf.getUsedEntity().getId())?wasRevisonOf.getUsedEntity().getId()+"*":wasRevisonOf.getUsedEntity().getId(),
                        EdgeType.UNDIRECTED);
                count++;
            }
            for (Used used : ontologyHandler.readUsed()) {

                ((SparseMultigraph<String, String>) g).addEdge("used-" + count,
                        color_red.contains(used.getActivity().getId())?used.getActivity().getId()+"*":used.getActivity().getId(),
                        color_red.contains(used.getEntity().getId())?used.getEntity().getId()+"*":used.getEntity().getId(),
                        EdgeType.UNDIRECTED);
                count++;
            }
            return g;
        }

        public class VertexPaint implements Function<String, Paint> {

            @NullableDecl
            @Override
            public Paint apply(@NullableDecl String s) {
                if(s.contains("*")){
                    return new java.awt.Color(252, 51, 51);
                } else if(s.contains("Activity")) {
                    return new java.awt.Color(102, 178, 255);
                } else if (s.contains("Agent")) {
                    return new java.awt.Color(255, 153, 204);
                } else {
                    return new java.awt.Color(153, 153, 255);
                }
            }
        }

        public class ToStringLabeller implements Function<String, String> {

            @NullableDecl
            @Override
            public String apply(@NullableDecl String s) {
                return s;
            }
        }

        class TestGraphMouseListener<N> implements GraphMouseListener<N> {

            public void graphClicked(N v, java.awt.event.MouseEvent me) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String name=v.toString();
                        if(name.contains("*")){
                            name=name.replace("*","");
                        }
                        if (name.contains("Activity")) {
                            showDetail(searchOntology(name, 1));
                        } else if (name.contains("Agent")) {
                            showDetail(searchOntology(name, 3));
                        } else {
                            showDetail(searchOntology(name, 2));
                        }
                    }
                });
            }


            @Override
            public void graphPressed(N n, java.awt.event.MouseEvent mouseEvent) {

            }

            @Override
            public void graphReleased(N n, java.awt.event.MouseEvent mouseEvent) {

            }
        }


    }


}