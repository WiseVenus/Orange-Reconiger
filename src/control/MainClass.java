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
		primaryStage.setTitle("���ӱ��ϵͳ");
		primaryStage.getIcons().add(new Image("/images/Orange.png"));
		
//      //������������
//		Parent root = null;
//		try {
//			root = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		 // �ҵ���Ӧ��XML������panel         �������ط���õ�controller
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainClass.class.getResource("/view/MainView.fxml"));
        try {
			rootLayout = (AnchorPane) loader.load();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
        
		Scene scene = new Scene(rootLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	    mainViewController = loader.getController();
		
	    mainViewController.setMainClass(this);
	    
	    
		
	}

	//�����´��ڷ���
	 public boolean showStepMapStage(ArrayList<Image> StepMap){
		 Boolean isOk = false;
		 

		// ��ImageShowShelf������������һ���µ�Stage 
		 imageShowShelf = new ImageShowShelf();
         Stage StepMapStage =imageShowShelf.init(StepMap,isOk);
         
         StepMapStage.showAndWait();
         
		 //����ǵ��޸�����
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
	
