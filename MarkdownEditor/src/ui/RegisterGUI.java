package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RegisterGUI extends Application {
	private Stage mStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		mStage = primaryStage;
		setGUI();
		
		primaryStage.setTitle("зЂВс");
		primaryStage.show();
	}
	
	private void setGUI() {
		GridPane root = new GridPane();
		root.setAlignment(Pos.CENTER);
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(25, 25, 25, 25));
		
		Scene scene = new Scene(root, 500, 350);
		scene.getStylesheets().add(RegisterGUI.class.getResource("style.css").toExternalForm());
		mStage.setScene(scene);
		
		Text title = new Text("Welcome");
		title.setId("title");
		root.add(title, 0, 0, 2, 1);
		
		Label userName = new Label("User Name:");
		root.add(userName, 0, 1);
		 
		TextField userTextField = new TextField();
		root.add(userTextField, 1, 1);
		 
		Label pw = new Label("Password:");
		root.add(pw, 0, 2);
		 
		PasswordField pwBox = new PasswordField();
		root.add(pwBox, 1, 2);
		
		Label pw2 = new Label("Confirm:");
		root.add(pw2, 0, 3);
		
		PasswordField pwBox2 = new PasswordField();
		root.add(pwBox2, 1, 3);
		
		Button btn = new Button("Sign Up");
		HBox hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);
		root.add(hbBtn, 1, 4);
	}
	
	public static void main(String[] args) {
		new RegisterGUI().launch(args);
	}
}
