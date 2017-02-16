package control;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import view.ImageShowShelf;
import view.MainViewController;


import javafx.scene.Scene;
import javafx.scene.image.Image;

import javafx.scene.layout.AnchorPane;


public class MainClass extends Application {
	private AnchorPane rootLayout;
	private MainViewController mainViewController;
	private ImageShowShelf imageShowShelf;
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("橘子辨别系统");
		primaryStage.getIcons().add(new Image("/images/Orange.png"));
		
//      //可以这样加载
//		Parent root = null;
//		try {
//			root = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		 // 找到对应的XML并加载panel         这样加载方便得到controller
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainClass.class.getResource("/view/MainView.fxml"));
        try {
			rootLayout = (AnchorPane) loader.load();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
        
		Scene scene = new Scene(rootLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	    mainViewController = loader.getController();
		
	    mainViewController.setMainClass(this);
	    
	    
		
	}

	//加载新窗口方法
	 public boolean showStepMapStage(ArrayList<Image> StepMap){
		 Boolean isOk = false;
		 

		// 用ImageShowShelf来创建并设置一个新的Stage 
		 imageShowShelf = new ImageShowShelf();
         Stage StepMapStage =imageShowShelf.init(StepMap,isOk);
         
         StepMapStage.showAndWait();
         
		 //待会记得修改下面
		 return isOk;
	 }
	 
	 
	public static void main(String[] args) {
		launch(args);
	}

	public File getPersonFilePath() {
		Preferences prefs = Preferences.userNodeForPackage(MainClass.class);
		String filePath = prefs.get("filePath", null);
		if (filePath != null) {
			return new File(filePath);
		} else {
			return new File("/orangePictures");
		}
	}
	
	public void setPersonFilePath(File file) {
		Preferences prefs = Preferences.userNodeForPackage(MainClass.class);
		if (file != null) {
			prefs.put("filePath", file.getPath());

		}
	}
}
	
