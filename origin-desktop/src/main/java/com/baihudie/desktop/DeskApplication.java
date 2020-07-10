package com.baihudie.desktop;


import com.baihudie.desktop.config.DesktopSplashScreen;
import com.baihudie.desktop.view.MainStageView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DeskApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {

        launch(DeskApplication.class, MainStageView.class, new DesktopSplashScreen(), args);
    }

    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        stage.setTitle("ORIGIN DESKTOP");
        stage.setWidth(800);
        stage.setHeight(600);
    }

}

