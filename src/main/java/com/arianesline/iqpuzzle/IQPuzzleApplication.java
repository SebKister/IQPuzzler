package com.arianesline.iqpuzzle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.arianesline.iqpuzzle.IQPuzzleController.executorService;

public class IQPuzzleApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(IQPuzzleApplication.class.getResource("IQPuzzleView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("IQPuzzler Solver");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
        if (executorService != null) executorService.shutdownNow();

    }

    public static void main(String[] args) {
        launch();
    }
}