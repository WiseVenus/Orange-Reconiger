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
	 * 一堆按钮 看名字吧~
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
    
    
    private Image image;//拖入图片或导入图片用
    
    private BufferedImage bufferedImage;
    
    WritableImage wwImage;//备份bufferedImage用跟显示圈起来后的图用
    
   
    
    private WritableImage wImage;//作为各种变换后的图片
    
    private FileChooser fileChooser;
    
    private LinkedList<Orange> orange;
    
    private List<Spot> comparison;
    
    private String path;
    
    private String name;
    
    private ArrayList<Image> StepMap ;
    
    private MainClass mainClass; //把主类加载过来方便操作 
    
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
		

		
		
		

		
	
	//设置imageView的拖拽图片监听 ----拖
	imageView.setOnDragOver(new EventHandler<DragEvent>() {

		@Override
		public void handle(DragEvent event) {
			if (event.getGestureSource() != imageView) {
				event.acceptTransferModes(TransferMode.ANY);//设置接收数据的类型。
			}				
		}
	});
	//放
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
						
						//设置步骤图
						if (StepMap==null) {
							StepMap = new ArrayList<>();
							
//							StepMap.add(image);
							
						}else{
							StepMap.clear();
						}
						
						
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
			}
		}
	});
	
	//拖出   
  imageView.setOnDragDetected(new EventHandler<MouseEvent>() { 
      public void handle(MouseEvent event) {
           
              Dragboard dragboard = imageView.startDragAndDrop(TransferMode.ANY); //开启拖出事件
              ClipboardContent content = new ClipboardContent(); //用来放拖出的内容
              content.clear();
              File file = new File(name+".png"); 
              try {
				file.createNewFile();
				ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png", file);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
              
             List<File> files = new ArrayList<>();
             files.add(file);
              content.putFiles(files); //将图片文件保存到面板里面 到时拖出来发现面板里有对应的文件就给会显示可以保存
              dragboard.setContent(content); //放入拖出内容
              event.consume();
              
           
      }
  });
  

  

  
  
  


	
	
	/**
	 * 一堆按钮监听
	 */
	//变亮监听
	bright.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(0);
		}
	});
	
	//变暗
	darker.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(1);
		}
	});
	
	//变灰
	gray.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(2);
		}
	});
	
	//颜色反转
	invert.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(3);
		}
	});
	
	//增加饱和度
	saturate.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(4);
		}
	});
	
	//减少饱和度
	desaturate.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
			pixWithImage(5);
		}
	});
	
	//原图呈现
	recover.setOnAction(new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent event) {
			imageView.setImage(image);
			
			
			
		}
	});
	
	//查找橘子
		check.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				check();
				
				
				
			}
		});
	
	//步骤图显示
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
				Boolean isOK = mainClass.showStepMapStage(StepMap);//isOK暂时没有用到 
				
				
			}
		});
	
	
	//导入
	Import.setOnAction(new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent event) {
	        
			fileChooser=new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("图片文件", "*.png","*.jpg", "*.bmp", "*.gif"));
			List<File> files= fileChooser.showOpenMultipleDialog(null);
			if (files != null) {
				try {
					imageView.setImage(new Image(new FileInputStream(files.get(0))));
					image=new Image(new FileInputStream(files.get(0)));
					bufferedImage=ImageIO.read(files.get(0));
					path=files.get(0).getAbsolutePath();
					
					number.setText("0");
					
					//设置步骤图
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
	
	
	//导出
	export.setOnAction(new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent event) {
			
			if(imageView.getImage()==null){
				Alert alert = new Alert(AlertType.INFORMATION);
	        	alert.setTitle("您好");
	        	alert.setHeaderText("o(^▽^)o");
	        	alert.setContentText("并没有图片可以导出");
	        	
	        	alert.show();
			}else{
			
			fileChooser=new FileChooser();
//			fileChooser.getExtensionFilters().add(new ExtensionFilter("图片文件", "*.png","*.jpg", "*.bmp", "*.gif"));
			java.util.List<String> list1= new ArrayList<>();
			list1.add("*.png");
			list1.add("*.jpg");
			list1.add("*.bmp");
			list1.add("*.gif");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("图片格式",list1));
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
	
	
	//6个按钮调用同一个的方法
	public void pixWithImage(int type){
		if (imageView.getImage()==null) {
			return;
			
		}
		
		PixelReader pixelReader = imageView.getImage().getPixelReader();//实例化图像框里的图片的像素读者 读者只能得到这个图片的像素
		// 创建一个新的image用来存放更改后的图片
        wImage = new WritableImage( 
                (int)image.getWidth(),
                (int)image.getHeight());//实例化一个可被写像素的图片 
        PixelWriter pixelWriter = wImage.getPixelWriter();//实例化这个图片的像素写者 有它就可以进行setColor 设置像素
        												
		
        
        //对图片每个像素进行相应操作
        for(int y = 0; y < image.getHeight(); y++){
        	for(int x = 0; x < image.getWidth(); x++){//这样循环的到每个像素
        		javafx.scene.paint.Color color = pixelReader.getColor(x, y);//得到像素 
        		
        		switch (type) {//根据不同按钮的点击会有不同的type传到这个方法 然后根据不同的type进行选择
				case 0:
					color = color.brighter();//变亮
					break;
				case 1:
					color = color.darker();//变暗
					break;
				case 2:
					color = color.grayscale();//灰度图
					break;
				case 3:
					color = color.invert();//颜色反转
					break;
				case 4:
					color = color.saturate();//增加饱和度
					break;
				case 5:
					color = color.desaturate();//减少饱和度
					break;
				default:
					break;
				}
        		//操作完放入新的 WritableImage里
        		pixelWriter.setColor(x, y, color);
        	}
        }
        //放入显示框imageView
        imageView.setImage(wImage);
	}
	
	//查橘子
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
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			
			return;
		}

		//--------------------------------------------------------------------------
		/*
		 * 美丽的分割线
		 */
		//--------------------------------------------------------------------------
		
		//这里还是在查橘子方法里
		
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
         
         //原像素数组
         int[] pixels = recognizer.getImagePixels(bufferedImage, width, height);
         StepMap.add(recognizer.getImage(width, height, pixels));
         
         
         
         //灰度像素数组
         int[] GrayPixelsTemp = new int[width * height];
         int[] grayPixels = recognizer.getGrayPixels(pixels, width, height, GrayPixelsTemp);
         StepMap.add(recognizer.getImage(width, height, GrayPixelsTemp));
         
         //二值数组
         int[] binaryPixels = recognizer.getBinaryPixels(grayPixels, pixels, width, height);
         StepMap.add(recognizer.getImage(width, height, binaryPixels));
         
//         膨胀测试
//         int[]ExpandsionPixels = recognizer.getExpandsionPixels(binaryPixels, width, height);
//         StepMap.add(recognizer.getImage(width, height, ExpandsionPixels));
//         //腐蚀测试
//         int[]CorrosionPixels = recognizer.getCorrosionPixels(binaryPixels, width, height);
//         StepMap.add(recognizer.getImage(width, height,CorrosionPixels));
        
        
         
         //标记数组
         int[][] pixelsTemp = new int[width][height]; 
         int[] tagBinaryPixels = recognizer.copyPixels(binaryPixels);
         tagBinaryPixels = recognizer.getTagPixels(tagBinaryPixels, width, height, pixelsTemp);
         StepMap.add(recognizer.getImage(width, height, tagBinaryPixels));
         
//         //填充数组
//         int[] fillBinaryPixels = recognizer.copyPixels(tagBinaryPixels);
//         fillBinaryPixels = recognizer.getfillPixels(tagBinaryPixels, width, height, pixelsTemp);
//         StepMap.add(recognizer.getImage(width, height, fillBinaryPixels));
//         
//         //边缘检测
//         int[] edgeBinaryPixels = recognizer.copyPixels(fillBinaryPixels);
//         edgeBinaryPixels = recognizer.getEdgePixels(edgeBinaryPixels, width, height);
//         StepMap.add(recognizer.getImage(width, height, edgeBinaryPixels));
         
         
         //边缘检测
       int[] edgeBinaryPixels = recognizer.copyPixels(tagBinaryPixels);
       edgeBinaryPixels = recognizer.getEdgePixels(edgeBinaryPixels, width, height);
       StepMap.add(recognizer.getImage(width, height, edgeBinaryPixels));
         
         //返回橘子链表
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
//        		//显示橘子数目
//                 number.setText(String.valueOf(comparison.size()));
//		    }
//        	 
//         }else{
//        	 
//        	 wbufferedImage = recognizer.draw(wbufferedImage, orange, null, 1);
//             
//             //显示橘子数目
//             number.setText(String.valueOf(orange.size()));
//        	 
//         }
//        
         comparison = Hoff.startAnlyze(wbufferedImage);
         
         int number2 = comparison.size()-orange.size();
         
         if(orange.size()>=3){ 
        	 	wbufferedImage = recognizer.draw(wbufferedImage, orange, null, 1);
         
        	 	//显示橘子数目
        	 	number.setText(String.valueOf(orange.size()));
        	 	} else if (number2>0 && number2<=13) {
        		 
        		 wbufferedImage = recognizer.draw(wbufferedImage, null, comparison, 2);
        		 
        		//显示橘子数目
                 number.setText(String.valueOf(comparison.size()));
        		 
				
			} else{
				 wbufferedImage = recognizer.draw(wbufferedImage, orange, null, 1);
	             
	             //显示橘子数目
	             number.setText(String.valueOf(orange.size()));
			}
        
         
         
         
         
         
         

		 //备份bufferedImage用跟显示圈起来后的图用
         wwImage=new WritableImage(wbufferedImage.getWidth(), wbufferedImage.getHeight());
         SwingFXUtils.toFXImage(wbufferedImage, wwImage);
         
         //画好的图放入imageView框里
         imageView.setImage(wwImage);
         
         //添加进检测步骤图里
         StepMap.add(wwImage);
         
//       //第二幅图是二值图 呈现的时候一片黑 删掉算了
//         StepMap.remove(1);         
         
        
        
         

         
         
         
}
	
}                        
