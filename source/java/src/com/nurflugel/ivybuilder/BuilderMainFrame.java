package com.nurflugel.ivybuilder;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import com.nurflugel.ivybrowser.ui.NewComponentDialog;
import com.nurflugel.ivybuilder.handlers.FileHandler;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import static com.nurflugel.common.ui.Version.VERSION;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.paint.Color.LIGHTGREY;

/** Created by IntelliJ IDEA. User: dbulla Date: Jan 18, 2008 Time: 9:36:34 PM To change this template use File | Settings | File Templates. */
public class BuilderMainFrame extends Application
{
  private static final String     REPOSITORY_LOCATION          = "repositoryLocation";
  private static final String     REPOSITORY                   = "repository";
  public static final String      IVY_REPOSITORY_IS_LOCATED_AT = "Ivy repository is located at: ";
  private File                    repositoryDir;
  private List<IvyRepositoryItem> ivyPackages                  = new ArrayList<IvyRepositoryItem>();
  private Preferences             preferences;
  private final Label             repositoryLocationLabel      = new Label(IVY_REPOSITORY_IS_LOCATED_AT);

  // --------------------------- CONSTRUCTORS ---------------------------
  // public BuilderMainFrame()
  // {
  // setTitle("IvyBuild v. " + VERSION);
  // setContentPane(contentsPanel);
  // addListeners();
  // setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
  // centerApp(this);
  // preferences = AppPreferences.userNodeForPackage(BuilderMainFrame.class);
  // loadPreferences();
  // centerApp(this);
  // setVisible(true);
  // findFiles();
  // }
  public void start(final Stage primaryStage) throws Exception
  {
    Group root  = new Group();
    Scene scene = new Scene(root, LIGHTGREY);

    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();

    // primaryStage.setResizable(false);
    primaryStage.setTitle("IvyBuild v. " + VERSION);
    preferences = Preferences.userNodeForPackage(BuilderMainFrame.class);

    // loadPreferences();
    BorderPane borderPane = new BorderPane();
    VBox       midVbox    = new VBox();

    midVbox.setFillWidth(true);
    primaryStage.setWidth(400);
    primaryStage.setHeight(200);

    TitledPane titledPane = new TitledPane("Ivy Repository Location", midVbox);

    titledPane.setStyle("-fx-border-color: white, grey; -fx-border-width: 2, 1; -fx-border-insets: 0, 0 1 1 0");
    titledPane.setExpanded(true);
    titledPane.setPrefWidth(400);
    midVbox.setSpacing(5);
    midVbox.setAlignment(CENTER);
    midVbox.getChildren().add(repositoryLocationLabel);

    final TextField ivyDirField = new TextField("Select Ivy Repository Dir");

    ivyDirField.setPromptText("Enter the locatin of the Ivy repository");
    ivyDirField.setOnAction(new EventHandler<javafx.event.ActionEvent>()
      {
        @Override
        public void handle(javafx.event.ActionEvent actionEvent)
        {
          findRepositoryDir(primaryStage, ivyDirField.getText());
        }
      });
    midVbox.getChildren().add(ivyDirField);
    borderPane.setTop(titledPane);

    // borderPane.setCenter(midVbox);
    VBox buttonBox = new VBox();

    buttonBox.setAlignment(CENTER);
    buttonBox.setSpacing(5);

    Button addNewButton = new Button("Add New Component");

    addNewButton.setOnAction(new EventHandler<javafx.event.ActionEvent>()
      {
        @Override
        public void handle(javafx.event.ActionEvent actionEvent)
        {
          addNewComponent();
        }
      });

    Button helpButton = new Button("Help");
    Button quitButton = new Button("Quit");

    quitButton.setOnAction(new EventHandler<javafx.event.ActionEvent>()
      {
        @Override
        public void handle(javafx.event.ActionEvent actionEvent)
        {
          doQuitAction();
        }
      });
    buttonBox.getChildren().add(addNewButton);
    buttonBox.getChildren().add(helpButton);
    buttonBox.getChildren().add(quitButton);
    borderPane.setCenter(buttonBox);
    root.getChildren().add(borderPane);
    primaryStage.show();
    findFiles();
  }

  // addWindowListener(new WindowAdapter()
  // {
  // @Override
  // public void windowClosing(WindowEvent e)
  // {
  // super.windowClosing(e);
  // doQuitAction();
  // }
  // });
  // addHelpListener("ivyBuilderHelp.hs", helpButton, this);
  private void findRepositoryDir(Stage primaryStage, String text)
  {
    FileChooser chooser = new FileChooser();

    chooser.setTitle("Select the repository dir");

    File file = chooser.showOpenDialog(null);

    // File file = new File(text);
    if (file != null)
    {
      String dirName = file.getName();

      if (dirName.contains(REPOSITORY))
      {
        repositoryDir = file;
        repositoryLocationLabel.setText(IVY_REPOSITORY_IS_LOCATED_AT + file.getAbsolutePath());
      }
      else
      {
        // showConfirmDialog(this, "The dir must be named \"" + REPOSITORY + "\"");
      }
    }

    validateIvyRepositoryLocation(file.getAbsolutePath());
    findFiles();
  }

  private void validateIvyRepositoryLocation(String dirName)
  {
    // addNewComponentButton.setEnabled(false);
    if (dirName != null)
    {
      File dir = new File(dirName);

      if (dir.exists() && dir.isDirectory())
      {
        repositoryDir = dir;
        // ivyReposLabel.setText(repositoryDir.getAbsolutePath());
        // addNewComponentButton.setEnabled(true);
      }
    }
  }

  private void addNewComponent()
  {
    NewComponentDialog dialog = new NewComponentDialog(ivyPackages, repositoryDir, preferences);

    dialog.setVisible(true);
  }

  private void doQuitAction()
  {
    savePreferences();

    // dispose();
    System.exit(0);
  }

  private void savePreferences()
  {
    if (repositoryDir != null)
    {
      preferences.put(REPOSITORY_LOCATION, repositoryDir.getAbsolutePath());
    }
  }

  private void loadPreferences()
  {
    String dirName = preferences.get(REPOSITORY_LOCATION, null);

    validateIvyRepositoryLocation(dirName);
  }

  private void findFiles()
  {
    ivyPackages = new ArrayList<IvyRepositoryItem>();

    if (repositoryDir != null)
    {
      FileHandler fileHandler = new FileHandler(this, repositoryDir, ivyPackages);

      fileHandler.execute();
    }
  }

  // -------------------------- OTHER METHODS --------------------------
  public void showNormal() {}

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    launch(args);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public void setStatusLabel(String text)
  {
    // statusLabel.setText(text);
  }
}
