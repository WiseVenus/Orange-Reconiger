package view;

import javafx.fxml.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import control.MainClass;
import javafx.embed.swing.*;
import javafx.event.*;
import javafx.stage.*;
import javafx.stage.FileChooser.*;
import model.Hoff;
import model.Orange;
import model.Spot;
import model.Recognizer;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;


public class MainViewController {

	/**
	 * һ�Ѱ�ť �����ְ�~
	 */
	@FXML
	private ImageView imageView;
	
    @FXML
    private Button bright ;
    @FXML
    private Button darker ;
    @FXML
    private Button gray ;
    @FXML
    private Button invert ;
    @FXML
    private Button saturate  ;
    @FXML
    private Button desaturate ;
    @FXML
    private Button recover ;
    
    @FXML
    private Button check ;
    
    @FXML
    private Button Import ;
    
    @FXML
    private Button export ;
    
    @FXML
    private Label number;
    
    @FXML
    private Button showStepMap;
    
    
    private Image image;//����ͼƬ����ͼƬ��
    
    private BufferedImage bufferedImage;
    
    WritableImage wwImage;//����bufferedImage�ø���ʾȦ�������ͼ��
    
   
    
    private WritableImage wImage;//��Ϊ���ֱ任���ͼƬ
    
    private FileChooser fileChooser;
    
    private LinkedList<Orange> orange;
    
    private List<Spot> comparison;
    
    private String path;
    
    private String name;
    
    private ArrayList<Image> StepMap ;
    
    private MainClass mainClass; //��������ع���������� 
    
    public void setMainClass(MainClass mainClass) {
		this.mainClass = mainClass;
	}
    


	public ArrayList<Image> getStepMap() {
		return StepMap;
	}









	public void setStepMap(ArrayList<Image> stepMap) {
		StepMap = stepMap;
	}









	public void initialize() {
		
//		imageView.setImage(new Image("/images/IronBack2.png"));
////		
//		image = imageView.getImage();
		

		
		
		

		
	
	//����imageView����קͼƬ���� ----��
	imageView.setOnDragOver(new EventHandler<DragEvent>() {

		@Override
		public void handle(DragEvent event) {
			if (event.getGestureSource() != imageView) {
				event.acceptTransferModes(TransferMode.ANY);//���ý������ݵ����͡�
			}				
		}
	});
	//��
	imageView.setOnDragDropped(new EventHandler<DragEvent>() {

		@Override
		public void handle(DragEvent event) {
			Dragboard dragboard = event.getDragboard();
			 List<File> files = dragboard.getFiles();
			if(files.size() > 0){
				try {
					imageView.setImage(new Image(new FileInputStream(files.get(0))));
					image=new Image(new FileInputStream(files.get(0)));
					try {
						bufferedImage=ImageIO.read(files.get(0));
						path=files.get(0).getAbsolutePath();
						name=files.get(0).getName();
						number.setText("0");
						
						//���ò���ͼ
						if (StepMap==null) {
							StepMap = new ArrayList<>();
							
//							StepMap.add(image);
							
						}else{
							StepMap.clear();
						}
						
						
					} catch (IOException e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
			}
		}
	});
	
	//�ϳ�   
  imageView.setOnDragDetected(new EventHandler<MouseEvent>() { 
      public void handle(MouseEvent event) {
           
              Dragboard dragboard = imageView.startDragAndDrop(TransferMode.ANY); //�����ϳ��¼�
              ClipboardContent content = new ClipboardContent(); //�������ϳ�������
              content.clear();
              File file = new File(name+".png"); 
              try {
				file.createNewFile();
				ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png", file);
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
              
             List<File> files = new ArrayList<>();
             files.add(file);
              content.putFiles(files); //��ͼƬ�ļ����浽������� ��ʱ�ϳ�������������ж�Ӧ���ļ��͸�����ʾ���Ա���
              dragboard.setContent(content); //�����ϳ�����
              event.consume();
              
           
      }
  });
  

  

  
  
  


	
	
	/**
	 * һ�Ѱ�ť����
	 */
	//��������
	bright.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(0);
		}
	});
	
	//�䰵
	darker.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(1);
		}
	});
	
	//���
	gray.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(2);
		}
	});
	
	//��ɫ��ת
	invert.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(3);
		}
	});
	
	//���ӱ��Ͷ�
	saturate.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(4);
		}
	});
	
	//���ٱ��Ͷ�
	desaturate.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(5);
		}
	});
	
	//ԭͼ����
	recover.setOnAction(new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent event) {
			imageView.setImage(image);
			
			
			
		}
	});
	
	//��������
		check.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				check();
				
				
				
			}
		});
	
	//����ͼ��ʾ
		showStepMap.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				if (StepMap==null) {
					
					StepMap = new ArrayList<>();
					
					
					StepMap.add(new Image("/images/orangeCrash.gif"));
					

					StepMap.add(new Image("/images/eat.gif"));
					
					
					
				}
				if (StepMap.isEmpty()) {
					
					StepMap.add(new Image("/images/orangeCrash.gif"));

					StepMap.add(new Image("/images/eat.gif"));
				}
				@SuppressWarnings("unused")
				Boolean isOK = mainClass.showStepMapStage(StepMap);//isOK��ʱû���õ� 
				
				
			}
		});
	
	
	//����
	Import.setOnAction(new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent event) {
	        
			fileChooser=new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("ͼƬ�ļ�", "*.png","*.jpg", "*.bmp", "*.gif"));
			List<File> files= fileChooser.showOpenMultipleDialog(null);
			if (files != null) {
				try {
					imageView.setImage(new Image(new FileInputStream(files.get(0))));
					image=new Image(new FileInputStream(files.get(0)));
					bufferedImage=ImageIO.read(files.get(0));
					path=files.get(0).getAbsolutePath();
					
					number.setText("0");
					
					//���ò���ͼ
					if (StepMap==null) {
						StepMap = new ArrayList<>();
//						StepMap.add(image);
						
					}else{
						StepMap.clear();
					}
					
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	});
	
	
	//����
	export.setOnAction(new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent event) {
			
			if(imageView.getImage()==null){
				Alert alert = new Alert(AlertType.INFORMATION);
	        	alert.setTitle("����");
	        	alert.setHeaderText("o(^��^)o");
	        	alert.setContentText("��û��ͼƬ���Ե���");
	        	
	        	alert.show();
			}else{
			
			fileChooser=new FileChooser();
//			fileChooser.getExtensionFilters().add(new ExtensionFilter("ͼƬ�ļ�", "*.png","*.jpg", "*.bmp", "*.gif"));
			java.util.List<String> list1= new ArrayList<>();
			list1.add("*.png");
			list1.add("*.jpg");
			list1.add("*.bmp");
			list1.add("*.gif");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("ͼƬ��ʽ",list1));
			File file = fileChooser.showSaveDialog(null);
			if (file != null) {
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png", file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
});
	
	
	
	
	}
	
	
	//6����ť����ͬһ���ķ���
	public void pixWithImage(int type){
		if (imageView.getImage()==null) {
			return;
			
		}
		
		PixelReader pixelReader = imageView.getImage().getPixelReader();//ʵ����ͼ������ͼƬ�����ض��� ����ֻ�ܵõ����ͼƬ������
		// ����һ���µ�image������Ÿ��ĺ��ͼƬ
        wImage = new WritableImage( 
                (int)image.getWidth(),
                (int)image.getHeight());//ʵ����һ���ɱ�д���ص�ͼƬ 
        PixelWriter pixelWriter = wImage.getPixelWriter();//ʵ�������ͼƬ������д�� �����Ϳ��Խ���setColor ��������
        												
		
        
        //��ͼƬÿ�����ؽ�����Ӧ����
        for(int y = 0; y < image.getHeight(); y++){
        	for(int x = 0; x < image.getWidth(); x++){//����ѭ���ĵ�ÿ������
        		javafx.scene.paint.Color color = pixelReader.getColor(x, y);//�õ����� 
        		
        		switch (type) {//���ݲ�ͬ��ť�ĵ�����в�ͬ��type����������� Ȼ����ݲ�ͬ��type����ѡ��
				case 0:
					color = color.brighter();//����
					break;
				case 1:
					color = color.darker();//�䰵
					break;
				case 2:
					color = color.grayscale();//�Ҷ�ͼ
					break;
				case 3:
					color = color.invert();//��ɫ��ת
					break;
				case 4:
					color = color.saturate();//���ӱ��Ͷ�
					break;
				case 5:
					color = color.desaturate();//���ٱ��Ͷ�
					break;
				default:
					break;
				}
        		//����������µ� WritableImage��
        		pixelWriter.setColor(x, y, color);
        	}
        }
        //������ʾ��imageView
        imageView.setImage(wImage);
	}
	
	//������
	public void check(){
		 number.setText("");
		
		 if(bufferedImage==null)
		 {
			 return ;
		 }
		 
		 if(!StepMap.isEmpty()) StepMap.clear();

		 
		 

		 BufferedImage wbufferedImage;
		try {
			wbufferedImage = ImageIO.read(new File(path));
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
			
			return;
		}

		//--------------------------------------------------------------------------
		/*
		 * �����ķָ���
		 */
		//--------------------------------------------------------------------------
		
		//���ﻹ���ڲ����ӷ�����
		
		 Recognizer recognizer = new Recognizer();
		
		 int width = bufferedImage.getWidth();
         int height = bufferedImage.getHeight();  
         
         //--------------------------test--------------------------------
//        BufferedImage bufferedImage2 = new BufferedImage(width, height,  BufferedImage.TYPE_BYTE_GRAY);
//        
//        
//        for(int i= 0 ; i < width ; i++){  
//        	        for(int j = 0 ; j < height; j++){  
//        	       int rgb = wbufferedImage.getRGB(i, j);  
//        	       bufferedImage2.setRGB(i, j, rgb);  
//        	       }  
//        	   }  
//        
//        wwImage=new WritableImage(width,height);
//        SwingFXUtils.toFXImage(bufferedImage2, wwImage);
//
//        
//        StepMap.add(wwImage);
//        
        
         //-------------------------test---------------------------------
         
         //ԭ��������
         int[] pixels = recognizer.getImagePixels(bufferedImage, width, height);
         StepMap.add(recognizer.getImage(width, height, pixels));
         
         
         
         //�Ҷ���������
         int[] GrayPixelsTemp = new int[width * height];
         int[] grayPixels = recognizer.getGrayPixels(pixels, width, height, GrayPixelsTemp);
         StepMap.add(recognizer.getImage(width, height, GrayPixelsTemp));
         
         //��ֵ����
         int[] binaryPixels = recognizer.getBinaryPixels(grayPixels, pixels, width, height);
         StepMap.add(recognizer.getImage(width, height, binaryPixels));
         
//         ���Ͳ���
//         int[]ExpandsionPixels = recognizer.getExpandsionPixels(binaryPixels, width, height);
//         StepMap.add(recognizer.getImage(width, height, ExpandsionPixels));
//         //��ʴ����
//         int[]CorrosionPixels = recognizer.getCorrosionPixels(binaryPixels, width, height);
//         StepMap.add(recognizer.getImage(width, height,CorrosionPixels));
        
        
         
         //�������
         int[][] pixelsTemp = new int[width][height]; 
         int[] tagBinaryPixels = recognizer.copyPixels(binaryPixels);
         tagBinaryPixels = recognizer.getTagPixels(tagBinaryPixels, width, height, pixelsTemp);
         StepMap.add(recognizer.getImage(width, height, tagBinaryPixels));
         
//         //�������
//         int[] fillBinaryPixels = recognizer.copyPixels(tagBinaryPixels);
//         fillBinaryPixels = recognizer.getfillPixels(tagBinaryPixels, width, height, pixelsTemp);
//         StepMap.add(recognizer.getImage(width, height, fillBinaryPixels));
//         
//         //��Ե���
//         int[] edgeBinaryPixels = recognizer.copyPixels(fillBinaryPixels);
//         edgeBinaryPixels = recognizer.getEdgePixels(edgeBinaryPixels, width, height);
//         StepMap.add(recognizer.getImage(width, height, edgeBinaryPixels));
         
         
         //��Ե���
       int[] edgeBinaryPixels = recognizer.copyPixels(tagBinaryPixels);
       edgeBinaryPixels = recognizer.getEdgePixels(edgeBinaryPixels, width, height);
       StepMap.add(recognizer.getImage(width, height, edgeBinaryPixels));
         
         //������������
         int[] houghBinaryPixels = recognizer.copyPixels(edgeBinaryPixels);
         orange = recognizer.hough(houghBinaryPixels, width, height);
         System.out.println(orange.size());
         
         

         
//         LinkedList<Orange> oranges = recognizer.getHoughPixels( edgeBinaryPixels, width, height);
//         System.out.println(oranges.size());
//         if (orange.size()==0) {
//        	 
//        	 comparison = Hoff.startAnlyze(wbufferedImage);
//        
//        	 if (comparison.size()!=0) {
//
//        	 		wbufferedImage = recognizer.draw(wbufferedImage, null, comparison, 2);
//        		 
//        		//��ʾ������Ŀ
//                 number.setText(String.valueOf(comparison.size()));
//		    }
//        	 
//         }else{
//        	 
//        	 wbufferedImage = recognizer.draw(wbufferedImage, orange, null, 1);
//             
//             //��ʾ������Ŀ
//             number.setText(String.valueOf(orange.size()));
//        	 
//         }
//        
         comparison = Hoff.startAnlyze(wbufferedImage);
         
         int number2 = comparison.size()-orange.size();
         
         if(orange.size()>=3){ 
        	 	wbufferedImage = recognizer.draw(wbufferedImage, orange, null, 1);
         
        	 	//��ʾ������Ŀ
        	 	number.setText(String.valueOf(orange.size()));
        	 	} else if (number2>0 && number2<=13) {
        		 
        		 wbufferedImage = recognizer.draw(wbufferedImage, null, comparison, 2);
        		 
        		//��ʾ������Ŀ
                 number.setText(String.valueOf(comparison.size()));
        		 
				
			} else{
				 wbufferedImage = recognizer.draw(wbufferedImage, orange, null, 1);
	             
	             //��ʾ������Ŀ
	             number.setText(String.valueOf(orange.size()));
			}
        
         
         
         
         
         
         

		 //����bufferedImage�ø���ʾȦ�������ͼ��
         wwImage=new WritableImage(wbufferedImage.getWidth(), wbufferedImage.getHeight());
         SwingFXUtils.toFXImage(wbufferedImage, wwImage);
         
         //���õ�ͼ����imageView����
         imageView.setImage(wwImage);
         
         //��ӽ���ⲽ��ͼ��
         StepMap.add(wwImage);
         
//       //�ڶ���ͼ�Ƕ�ֵͼ ���ֵ�ʱ��һƬ�� ɾ������
//         StepMap.remove(1);         
         
        
        
         

         
         
         
}
	
}                        
