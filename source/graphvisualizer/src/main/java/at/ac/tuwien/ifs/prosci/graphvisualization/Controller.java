package at.ac.tuwien.ifs.prosci.graphvisualization;

import at.ac.tuwien.ifs.prosci.graphvisualization.entities.*;
import at.ac.tuwien.ifs.prosci.graphvisualization.exception.TechnicalException;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.CommandExtracter;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.ProsciProperties;
import at.ac.tuwien.ifs.prosci.graphvisualization.helper.Trace;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.OntologyCreator;
import at.ac.tuwien.ifs.prosci.graphvisualization.provo.VersionChecker;
import com.google.common.base.Function;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.List;

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
    public RadioButton picking;
    public RadioButton transforming;
    public VBox modetype;
    @Autowired
    OntologyHandler ontologyHandler;
    @FXML
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
            Files.createFile(Paths.get(prosciProperties.readProperties("workspace.current")
                            + path_mapping.getString("prosci.trace.log")
                    + prosciProperties.readProperties(prosciProperties.readProperties("workspace.current")+".log")
                    + path_mapping.getString("prosci.trace.stop")));
            configWindow();
            simulator = new TextArea();
            String file_xml = prosciProperties.readProperties("workspace.current") + path_mapping.getString("prosci.prov") + "prov.xml";
            ontologyCreator.init();
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
        //searchtext.prefHeightProperty().bind(vBox_right.heightProperty());
        searchtext.prefWidthProperty().bind(vBox_right.widthProperty().multiply(0.6));
        searchbutton.prefWidthProperty().bind(vBox_right.widthProperty().multiply(0.2));
        modetype.prefWidthProperty().bind(vBox_right.widthProperty().multiply(0.2));
        picking.prefHeightProperty().bind(modetype.widthProperty().multiply(0.5));
        transforming.prefHeightProperty().bind(modetype.widthProperty().multiply(0.5));

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
            listView.prefWidthProperty().bind(t1.widthProperty());
            listView.prefHeightProperty().bind(t1.heightProperty());
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
        listView.prefWidthProperty().bind(t1.widthProperty());
        listView.prefHeightProperty().bind(t1.heightProperty());
        accordion.getPanes().add(t1);
        for (WasDerivedFrom i : wasDerivedFroms) {

            if (i.getGeneratedEntity().getId().equals(id)) {
                printEntity(i.getUsedEntity(), listView);
            }
        }
        JFXListView<Button> listView2 = new JFXListView<>();
        TitledPane t2 = new TitledPane("Derived the entities", listView2);
        listView2.prefWidthProperty().bind(t2.widthProperty());
        listView2.prefHeightProperty().bind(t2.heightProperty());
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
        listView.prefWidthProperty().bind(t1.widthProperty());
        listView.prefHeightProperty().bind(t1.heightProperty());
        accordion.getPanes().add(t1);
        for (WasRevisonOf i : wasRevisonOfs) {
            if (i.getGeneratedEntity().getId().equals(id)) {
                printEntity(i.getUsedEntity(), listView);
            }
        }
        JFXListView<Button> listView2 = new JFXListView<>();
        TitledPane t2 = new TitledPane("Next version", listView2);
        listView2.prefWidthProperty().bind(t2.widthProperty());
        listView2.prefHeightProperty().bind(t2.heightProperty());
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
            listView.prefWidthProperty().bind(t1.widthProperty());
            listView.prefHeightProperty().bind(t1.heightProperty());
            accordion.getPanes().add(t1);
            for (Used i : useds) {
                if (i.getActivity().getId().equals(id)) {
                    printEntity(i.getEntity(), listView);
                }
            }
        } else {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Was used by activitys", listView);
            listView.prefWidthProperty().bind(t1.widthProperty());
            listView.prefHeightProperty().bind(t1.heightProperty());
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
        listView.prefWidthProperty().bind(t1.widthProperty());
        listView.prefHeightProperty().bind(t1.heightProperty());
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
        listView.prefWidthProperty().bind(t1.widthProperty());
        listView.prefHeightProperty().bind(t1.heightProperty());
        accordion.getPanes().add(t1);
        for (WasInformedBy i : wasInformedBys) {

            if (i.getInformed().getId().equals(id)) {
                printActivity(i.getInformant(), listView);
            }

        }
        JFXListView<Button> listView2 = new JFXListView<>();
        TitledPane t2 = new TitledPane("Informed the activities", listView2);
        listView2.prefWidthProperty().bind(t2.widthProperty());
        listView2.prefHeightProperty().bind(t2.heightProperty());
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
            listView.prefWidthProperty().bind(t1.widthProperty());
            listView.prefHeightProperty().bind(t1.heightProperty());
            accordion.getPanes().add(t1);
            for (WasGeneratedBy i : wasGeneratedBys) {
                if (i.getEntity().getId().equals(id)) {
                    printActivity(i.getActivity(), listView);

                }
            }
        } else {
            JFXListView<Button> listView = new JFXListView<>();
            TitledPane t1 = new TitledPane("Generated the entities", listView);
            listView.prefWidthProperty().bind(t1.widthProperty());
            listView.prefHeightProperty().bind(t1.heightProperty());
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
                                versionChecker.getFile(entities.get(getIndexOfTheOntogoly(entity)).getVersion(),entity.getName(), entity.getId());
                                createInformationDialogs("File successfully load", "The selected file is successfully loaded [" + entity.getId() + "].");
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
        transforming.setVisible(true);
        picking.setVisible(true);
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
        transforming.setVisible(false);
        picking.setVisible(false);
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
        transforming.setVisible(false);
        picking.setVisible(false);
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
        transforming.setVisible(false);
        picking.setVisible(false);
        mode="Activities";
        activities = ontologyHandler.readActivites();
        Collections.sort(activities);
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
        transforming.setVisible(false);
        picking.setVisible(false);
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
        } else if (o instanceof Entity) {
            baseInfo.setText("ID: " + o.getId() + "\n" +
                    "Path: " + ((Entity) o).getValue() + "\n");
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
            LayoutAlgorithm layoutAlgorithm = new FRLayoutAlgorithm();
            vv = new VisualizationViewer<>(
                    new BaseVisualizationModel(
                            getGraph(withSearch),
                            layoutAlgorithm,
                            new RandomLocationTransformer<>((int)pane.getWidth(), (int)pane.getHeight(), 0),
                            new Dimension((int)pane.getWidth(), (int)pane.getHeight())),
                    new Dimension((int)pane.getWidth(), (int)pane.getHeight()));

            vv.setBackground(java.awt.Color.DARK_GRAY);


            vv.getRenderContext().setNodeFillPaintFunction(new VertexPaint());
            vv.getRenderContext().setEdgeDrawPaintFunction(new Function<String, Paint>() {
                @NullableDecl
                @Override
                public Paint apply(@NullableDecl String input) {
                    return java.awt.Color.white;
                }
            });
            vv.getRenderContext().setNodeLabelFunction(new ToStringLabeller());
            vv.getRenderContext().setNodeLabelDrawPaintFunction(new Function<String, Paint>() {
                @NullableDecl
                @Override
                public Paint apply(@NullableDecl String input) {
                    return java.awt.Color.white;
                }
            });

            vv.getRenderContext().setNodeLabelRenderer(new DefaultNodeLabelRenderer(java.awt.Color.red));
            vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.AUTO);
            vv.getRenderContext().setNodeDrawPaintFunction(new Function<String, Paint>() {
                @NullableDecl
                @Override
                public Paint apply(@NullableDecl String input) {
                    return java.awt.Color.white;
                }
            });
            DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
            gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
            vv.setGraphMouse(gm);
            PickedState<String> pvs = new MultiPickedState<>();
            vv.setPickedNodeState(pvs);
            vv.getRenderContext()
                    .setNodeFillPaintFunction(new VertexPaint());
            vv.getRenderContext().setNodeStrokeFunction(new TestGraphMouseListener(pvs));
            ToggleGroup group = new ToggleGroup();
            picking.setToggleGroup(group);
            transforming.setToggleGroup(group);
            transforming.setSelected(true);
            picking.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    gm.setMode(ModalGraphMouse.Mode.PICKING);
                }
            });
            transforming.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
                }
            });
            return vv;
        }

        private Network getGraph(boolean withSearch) throws ParserConfigurationException, SAXException, IOException, ParseException {
            ontologyHandler.readElements();
            entities = ontologyHandler.readEntites();
            agents = ontologyHandler.readAgent();
            activities = ontologyHandler.readActivites();

            MutableNetwork<String, String> g = NetworkBuilder.undirected().allowsParallelEdges(true).allowsSelfLoops(true).build();
            ArrayList<String> color_red=new ArrayList<>();
            for (Entity entity : entities) {
                if(withSearch){
                    if(entity.getId().contains(searchtext.getText())||(entity.getName().contains(searchtext.getText()))){
                        g.addNode(entity.getId()+"*");
                        color_red.add(entity.getId());
                    }
                    else {
                        g.addNode(entity.getId());
                    }

                }else {
                    g.addNode(entity.getId());
                }
            }
            for (Agent agent : agents) {
                if(withSearch){
                    if(agent.getId().contains(searchtext.getText())||(agent.getDetail().contains(searchtext.getText()))){
                        g.addNode(agent.getId()+"*");
                        color_red.add(agent.getId());
                    }else {
                        g.addNode(agent.getId());
                    }


                }else {
                    g.addNode(agent.getId());
                }
            }
            for (Activity activity : activities) {
                if(withSearch){
                    if(activity.getId().contains(searchtext.getText())
                            ||(activity.getCommand().contains(searchtext.getText()))
                            ||activity.getStartTime().toString().contains(searchtext.getText())
                            ||activity.getEndTime().toString().contains(searchtext.getText())){
                        g.addNode(activity.getId()+"*");
                        color_red.add(activity.getId());
                    }else {
                        g.addNode(activity.getId());
                    }

                }else {
                    g.addNode(activity.getId());
                }
            }
            int count = 0;
            for (WasAssociatedWith wasAssociatedWith : ontologyHandler.readWasAssociatedWith()) {

                ((MutableNetwork<String, String>) g).addEdge(
                        color_red.contains(wasAssociatedWith.getActivity().getId())?wasAssociatedWith.getActivity().getId()+"*":wasAssociatedWith.getActivity().getId(),
                        color_red.contains(wasAssociatedWith.getAgent().getId())?wasAssociatedWith.getAgent().getId()+"*":wasAssociatedWith.getAgent().getId(),
                        "wasAssociatedWith-" + count);
                count++;
            }
            for (WasAttributedTo wasAttributedTo : ontologyHandler.readWasAttributedTo()) {

                ((MutableNetwork<String, String>) g).addEdge(
                        color_red.contains(wasAttributedTo.getAgent().getId())?wasAttributedTo.getAgent().getId()+"*":wasAttributedTo.getAgent().getId(),
                        color_red.contains(wasAttributedTo.getEntity().getId())?wasAttributedTo.getEntity().getId()+"*":wasAttributedTo.getEntity().getId(),
                        "wasAttributedTo-" + count);
                count++;
            }
            for (WasDerivedFrom wasDerivedFrom : ontologyHandler.readWasDerivedFrom()) {

                ((MutableNetwork<String, String>) g).addEdge(
                        color_red.contains(wasDerivedFrom.getGeneratedEntity().getId())?wasDerivedFrom.getGeneratedEntity().getId()+"*":wasDerivedFrom.getGeneratedEntity().getId(),
                        color_red.contains(wasDerivedFrom.getUsedEntity().getId())?wasDerivedFrom.getUsedEntity().getId()+"*":wasDerivedFrom.getUsedEntity().getId(),
                        "wasDerivedFrom-" + count);
                count++;
            }

            for (WasGeneratedBy wasGeneratedBy : ontologyHandler.readWasGeneratedBy()) {

                ((MutableNetwork<String, String>) g).addEdge(
                        color_red.contains(wasGeneratedBy.getActivity().getId())?wasGeneratedBy.getActivity().getId()+"*":wasGeneratedBy.getActivity().getId(),
                        color_red.contains(wasGeneratedBy.getEntity().getId())?wasGeneratedBy.getEntity().getId()+"*":wasGeneratedBy.getEntity().getId(),
                        "wasGeneratedBy-" + count);
                count++;
            }
            for (WasInformedBy wasInformedBy : ontologyHandler.readWasInformedBy()) {

                ((MutableNetwork<String, String>) g).addEdge(
                        color_red.contains(wasInformedBy.getInformant().getId())?wasInformedBy.getInformant().getId()+"*":wasInformedBy.getInformant().getId(),
                        color_red.contains(wasInformedBy.getInformed().getId())?wasInformedBy.getInformed().getId()+"*":wasInformedBy.getInformed().getId(),
                        "wasInformedBy-" + count);
                count++;
            }
            for (WasRevisonOf wasRevisonOf : ontologyHandler.readWasRevisonOf()) {

                ((MutableNetwork<String, String>) g).addEdge(
                        color_red.contains(wasRevisonOf.getGeneratedEntity().getId())?wasRevisonOf.getGeneratedEntity().getId()+"*":wasRevisonOf.getGeneratedEntity().getId(),
                        color_red.contains(wasRevisonOf.getUsedEntity().getId())?wasRevisonOf.getUsedEntity().getId()+"*":wasRevisonOf.getUsedEntity().getId(),
                        "wasRevisonOf-" + count
                       );
                count++;
            }
            for (Used used : ontologyHandler.readUsed()) {

                ((MutableNetwork<String, String>) g).addEdge(
                        color_red.contains(used.getActivity().getId())?used.getActivity().getId()+"*":used.getActivity().getId(),
                        color_red.contains(used.getEntity().getId())?used.getEntity().getId()+"*":used.getEntity().getId(),
                        "used-" + count);
                count++;
            }
            return g;
        }

        public class VertexPaint implements Function<String, Paint> {

            @NullableDecl
            @Override
            public Paint apply(@NullableDecl String s) {
                if(s.contains("*")){
                    return new java.awt.Color(250, 237, 38);
                } else if(s.contains("#")) {
                    return new java.awt.Color(90, 85, 96);
                } else if (s.contains("Agent")) {
                    return new java.awt.Color(157, 141, 143);
                } else {
                    return new java.awt.Color(155, 120, 111);
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

        class TestGraphMouseListener implements Function<String, Stroke> {
            protected PickedInfo<String> pi;
            private String selected="";
            public TestGraphMouseListener(PickedState<String> pvs) {
                this.pi=pvs;
            }

            @Override
           public Stroke apply(String v) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (pi.isPicked(v)) {
                            String name = v.toString();
                            if (name.contains("*")) {
                                name = name.replace("*", "");
                            }

                            if(!selected.equals(name)) {
                                int showDetail = createConfirmation(name, "Show Detail?");
                                selected = name;
                                if (showDetail == 1) {
                                    if (name.contains("#")) {
                                        showDetail(searchOntology(name, 1));
                                    } else if (name.contains("Agent")) {
                                        showDetail(searchOntology(name, 3));
                                    } else {
                                        showDetail(searchOntology(name, 2));
                                    }
                                }
                            }
                        }
                    }
                });
                return new BasicStroke(0);
            }


        }


    }


}