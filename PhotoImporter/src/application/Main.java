package application;
	
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	private ArrayList<File> foundFiles;
	private static String[] acceptedFileTypes = {"png", "jpg", "jpeg", "bmp"};
	private VBox driveRadioButtons;
	private RadioButton picSelection, customSelection;
	private TextField picFolderName, customPath;
	private CheckBox delPics;
	private ToggleGroup driveTG;
	private int numFiles;
	private String path;
	private Scene step1, step2, step3;
	
	@Override
	public void start(Stage primaryStage) {
		
		//step 1 components
		//title
		Label title = new Label("Import Pictures");
		title.setFont(new Font(24));
		//find and display available drives
		displayDriveSelection();
		//continue button
		Button continueButton = new Button("Continue");
		continueButton.setOnAction(event -> {
			RadioButton selected = (RadioButton) driveTG.getSelectedToggle();
			foundFiles = getFiles(selected.getText(), new ArrayList<File>());
			System.out.printf("Found %d files\n", foundFiles.size() - 1);
			numFiles = foundFiles.size() - 1;
			primaryStage.setScene(step2);
		});
		//cancel button
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(event -> System.exit(0));
		
		//window setup for file source selection step
		HBox buttonArea = new HBox(10, continueButton, cancelButton);
		buttonArea.setAlignment(Pos.CENTER);
		VBox mainArea1 = new VBox(20, title, driveRadioButtons, buttonArea);
		mainArea1.setAlignment(Pos.CENTER);
		step1 = new Scene(mainArea1,400,250);
		step1.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		//step 2 components
		Label title2 = new Label("Import Pictures");
		title2.setFont(new Font(24));
		Label step2Instructions = new Label("Specify where the files should be imported to:");
		ToggleGroup saveLocType = new ToggleGroup();
		picSelection = new RadioButton("A folder in Pictures named: ");
		customSelection = new RadioButton("A folder at: ");
		picSelection.setToggleGroup(saveLocType);
		customSelection.setToggleGroup(saveLocType);
		picSelection.setSelected(true);
		picFolderName = new TextField();
		customPath = new TextField();
		customPath.setPrefWidth(250);
		Button importButton = new Button("Import");
		importButton.setOnAction(event -> {
			importFiles();
			primaryStage.setScene(step3);
		});
		Button cancelButton2 = new Button("Cancel");
		cancelButton2.setOnAction(event -> System.exit(0));
		delPics = new CheckBox();
		delPics.setSelected(true);
		Label delText = new Label("Delete files after importing?");
		HBox picSelGroup = new HBox(10, picSelection, picFolderName);
		HBox cusSelGroup = new HBox(10, customSelection, customPath);
		HBox buttonArea2 = new HBox(10, importButton, cancelButton2);
		HBox delGroup = new HBox(10, delPics, delText);
		step2Instructions.setAlignment(Pos.CENTER);
		picSelGroup.setAlignment(Pos.CENTER);
		cusSelGroup.setAlignment(Pos.CENTER);
		buttonArea2.setAlignment(Pos.CENTER);
		delGroup.setAlignment(Pos.CENTER);
		
		//window setup for file destination selection step
		VBox mainArea2 = new VBox(15, title2, step2Instructions, picSelGroup, 
				cusSelGroup, delGroup, buttonArea2);
		mainArea2.setAlignment(Pos.CENTER);
		step2 = new Scene(mainArea2, 400, 250);
		step2.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		//step 3 components
		
		Label title3 = new Label("Completed");
		title3.setFont(new Font(24));
		Button viewFiles = new Button("Open Folder");
		viewFiles.setOnAction(event -> {
			try {
				Runtime.getRuntime().exec("explorer.exe " + path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		});
		Button close = new Button("Close");
		close.setOnAction(event -> System.exit(0));
		HBox buttonArea3 = new HBox(10, viewFiles, close);
		buttonArea3.setAlignment(Pos.CENTER);
		VBox mainArea3 = new VBox(20, title3, buttonArea3);
		mainArea3.setAlignment(Pos.CENTER);
		step3 = new Scene(mainArea3, 400, 250);
		step3.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		primaryStage.setScene(step1);
		primaryStage.show();
		primaryStage.setTitle("Importing Pictures");
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private static ArrayList<File> getFiles(String dir, ArrayList<File> files) {
		File curDir = new File(dir);
		//files.remove(new File(dir));
		files.addAll(Arrays.asList(curDir.listFiles()));
		for(int i = 0; i < files.size(); i++) {
			//directories
			if(files.get(i).isDirectory()) {
				String dirName = files.get(i).getAbsolutePath();
				files.remove(i);
				return getFiles(dirName, files);
			//files
			}else{
				boolean isSupportedType = false;
				for(String str : acceptedFileTypes) {
					if(files.get(i).getName().endsWith(str)) {
						isSupportedType = true;
					}
				}
				//removes file from list
				if(!isSupportedType) {
					files.remove(i);
				}
			}
		}
		return files;
	}
	
	private void displayDriveSelection() {
		driveTG = new ToggleGroup();
		Label driveInstruction = new Label("Select the drive to import from:");
		driveRadioButtons = new VBox(10, driveInstruction);
		driveRadioButtons.setPadding(new Insets(10));
		driveRadioButtons.setAlignment(Pos.CENTER);
		File[] drives =  File.listRoots();
		for(File file : drives) {
			RadioButton button = new RadioButton(file.getAbsolutePath());
			driveTG.getToggles().add(button);
			button.setSelected(true);
			driveRadioButtons.getChildren().add(button);
		}
	}
	
	private void importFiles() {
		//establish destination path
		if(picSelection.isSelected()) {
			path = System.getProperty("user.home") + File.separator + "Pictures" 
				+ File.separator + picFolderName.getText() + File.separator;
		}else {
			path = customPath.getText() + File.separator;
		}
		for(int i = 0; i < path.length(); i++) {
			if(path.charAt(i) == ' ') {
				path = path.substring(0, i) + "_" + path.substring(i + 1);
			}
		}
		System.out.println(path);
		//import the files
		new File(path).mkdirs();
		int fileNumCounter = 1;
		for(File file : foundFiles) {
			String ext;
			try {
				ext = file.getName().substring(file.getName().lastIndexOf("."));
			}catch(StringIndexOutOfBoundsException e) {
				ext = "";
			}
			try {
				Files.copy(file.toPath(), new File(path + "img_" + fileNumCounter + ext).toPath(), StandardCopyOption.REPLACE_EXISTING);
				fileNumCounter++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//delete the original files
		if(delPics.isSelected()) {
			for(File file : foundFiles) {
				try {
					Files.delete(new File(file.getAbsolutePath()).toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
