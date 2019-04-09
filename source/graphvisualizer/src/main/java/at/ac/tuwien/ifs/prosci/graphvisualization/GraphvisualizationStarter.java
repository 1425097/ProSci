package at.ac.tuwien.ifs.prosci.graphvisualization;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class GraphvisualizationStarter extends Application {
    private ConfigurableApplicationContext springContext;
    private Parent root;

    public static void main(String[] args) {

        launch(GraphvisualizationStarter.class, args);
    }

    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(GraphvisualizationStarter.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/prosci.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        root = fxmlLoader.load();
        // ((Controller)springContext.getBean("controller")).initialize();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Prosci Terminal");
        Scene scene = new Scene(root, 1000, 600);

        scene.getStylesheets().add("/stylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(1000);

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        springContext.stop();
    }
}
