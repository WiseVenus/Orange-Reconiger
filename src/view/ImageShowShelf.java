package view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.effect.ReflectionBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;
 
@SuppressWarnings("deprecation")
public   class ImageShowShelf  { 
	//包含两个子类
	
	public static Stage stepStage;
	
	
   
	 static Screen screen = Screen.getPrimary();  
     static Rectangle2D bounds = screen.getVisualBounds();//获取屏幕大小
     
    private static final double WIDTH = bounds.getWidth()/1.2, HEIGHT = bounds.getHeight()/1.2;//定好待会用的宽高
 

    public Stage init(ArrayList<Image> StepMap,Boolean isOK) {
        Group root = new Group();
        Stage StepMapStage = new Stage();
        StepMapStage.setResizable(true);
        
         
        
        StepMapStage.setScene(new Scene(root, WIDTH,HEIGHT));
        StepMapStage.setTitle("步骤查看&&图片展览");
        StepMapStage.getIcons().add(new Image("/images/StepSpider.png"));
      
        
       
        // 展示图片用的展览架
        DisplayShelf displayShelf = new DisplayShelf(StepMap);
        displayShelf.setPrefSize(WIDTH, HEIGHT);
        root.getChildren().add(displayShelf);
        
        isOK = true;
        stepStage = StepMapStage;
        return StepMapStage;
    }
 
    /**
     * 显示图像到展示书架上	子类1
     */
    public static class DisplayShelf extends Region {
        private static final Duration DURATION = Duration.millis(500);
        private static final Interpolator INTERPOLATOR = Interpolator.EASE_BOTH;
        private static final double SPACING = 180;//设置切片之间的空间
        private static final double LEFT_OFFSET = -120;
        private static final double RIGHT_OFFSET = 120;
        private static final double SCALE_SMALL = 0.6;//设置后面的缩小比
        private ArrayList<PerspectiveImage> items; //书架显示的东西
        private Group centered = new Group();
        private Group left = new Group();
        private Group center = new Group();
        private Group right = new Group();
        private int centerIndex = 0;
        private Timeline timeline;
        private ScrollBar scrollBar = new ScrollBar();
        private boolean localChange = false;
        private Rectangle clip = new Rectangle();
        int  position ; //新增图片点击用
 

        
        public DisplayShelf(ArrayList<Image> StepMap) {
            // 设置切片
            setClip(clip);
            // 用CSS设置背景
            setStyle("-fx-background-color: linear-gradient(to bottom," + " black 60, #141414 60.1%, black 100%);");
            // 设置scrollBar的ID
            scrollBar.setId("DisplayShelfScrollBar");
            // 创建书架上的东西
            items = new ArrayList<>();
            
            for (int i=0; i<StepMap.size(); i++) {
//            	Image image =  StepMap.get(i);
                PerspectiveImage item = new PerspectiveImage(StepMap.get(i));
                
                
               
                items.add(item);
                final int index = i;
                item.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent me) {
                        localChange = true;
                        scrollBar.setValue(index);
                        localChange = false;
                        shiftToCenter(item);
                    }
                });
//                item.setOnMouseClicked(new EventHandler<MouseEvent>() {
//
//					@Override
//					public void handle(MouseEvent event) {
//						// TODO 自动生成的方法存根
//
//						if (event.getButton()==MouseButton.SECONDARY) {
//							
//							FileChooser fileChooser1 = new FileChooser();
//						    fileChooser1.setTitle("Save Image");
//						    
//						    File file = fileChooser1.showSaveDialog(null);
//						    if (file != null) {
//						        try {
////                                       下面这步卡住  总说得不到image
//						            ImageIO.write(SwingFXUtils.fromFXImage(image,
//						                    null), "png", file);
//						        } catch (IOException ex) {
//						             System.out.println(ex.getMessage());
//						        }
//
//							
//						}
//					}
//					}
//				});
                    
               
                
               
                
           
            
            
            }
            // 设置scrollBar
            scrollBar.setMax(items.size()-1);
            scrollBar.setVisibleAmount(1);
            scrollBar.setUnitIncrement(1);
            scrollBar.setBlockIncrement(1);
            scrollBar.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if(!localChange)
                        shiftToCenter(items.get((int)scrollBar.getValue()));
                }
            });
            // 创建内容
            centered.getChildren().addAll(left, right, center);
            getChildren().addAll(centered,scrollBar);
            // 设置键盘的各种键监听
            setFocusTraversable(true);
            setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent ke) {
                	if (ke.getCode()==KeyCode.BACK_SPACE||ke.getCode()==KeyCode.DELETE) {
                		
                		items.remove(centerIndex);
                		update();
						
					}
                	if (ke.getCode()==KeyCode.TAB) {

                		items.clear();
                		update();
					}
                	if (ke.getCode()==KeyCode.ESCAPE) {

                		stepStage.close();
                		
					}
                    if (ke.getCode() == KeyCode.LEFT) {
                        shift(1);
                        localChange = true;
                        scrollBar.setValue(centerIndex);
                        localChange = false;
                    } else if (ke.getCode() == KeyCode.RIGHT) {
                        shift(-1);
                        localChange = true;
                        scrollBar.setValue(centerIndex);
                        localChange = false;
                    }
                }
            });
            
            
           
            setOnDragOver(new EventHandler<DragEvent>() {//拖

        		@Override
        		public void handle(DragEvent event) {
        			if (event.getGestureSource() != DisplayShelf.this ) {
        				event.acceptTransferModes(TransferMode.ANY);
        			}				
        		}
        	});
            
            
            setOnDragDropped(new EventHandler<DragEvent>() {

        		@Override
        		public void handle(DragEvent event) {
        			Dragboard dragboard = event.getDragboard();
        			
        			 List<File> files = dragboard.getFiles();
        			if(files.size() > 0){
        				try {
        	
        					for (  position = 0; position < files.size(); position++) {
								
        					 Image image=new Image(new FileInputStream(files.get(position)));
           					 PerspectiveImage item = new PerspectiveImage(image);
           					 
           			
           					 item.setOnMouseClicked(new EventHandler<MouseEvent>() {
           	                    public void handle(MouseEvent me) {
           	                        localChange = true;
           	                       
           	                       
           	                        scrollBar.setValue(position);
           	                        localChange = false;
           	                        shiftToCenter(item);
           	                       
           	                    }
           	                });
           					 items.add(item);
								
							}
        				
        					 update();
        
        				} catch (FileNotFoundException e) {
        					e.printStackTrace();
        				}
        				
        			}
        		}
        	});
            
//            setOnDragDetected(new EventHandler<MouseEvent>() { 
//                public void handle(MouseEvent event) {
//                    
//                	
//                    Dragboard dragboard = items.get(centerIndex).startDragAndDrop(TransferMode.COPY); //开启拖出事件
//                    ClipboardContent content = new ClipboardContent(); //用来放拖出的内容
//                    content.clear();
//                    File file = new File(centerIndex+".png"); 
//                    try {
//      				file.createNewFile();
//                         
                         //被这步卡住
//      				ImageIO.write(SwingFXUtils.fromFXImage(StepMap.get(centerIndex), null), "png", file);
//      			} catch (IOException e) {
//      				// TODO 自动生成的 catch 块
//      				e.printStackTrace();
//      			}
//                    
//                   List<File> files = new ArrayList<>();
//                   files.add(file);
//                    content.putFiles(files); //将图片文件保存到面板里面 到时拖出来发现面板里有对应的文件就给会显示可以保存
//                    dragboard.setContent(content); //放入拖出内容
//                    event.consume();
//                   
//                    
//                 
//            }
//        });
            
            // 更新一遍
            update();
            
           
        }
 
        /**
         * 重写方法
         */
        @Override protected void layoutChildren() {
            // 更新片段大小
            clip.setWidth(getWidth());
            clip.setHeight(getHeight());
            // 保持中心不变
            centered.setLayoutY((getHeight() - PerspectiveImage.HEIGHT) / 2);
            centered.setLayoutX((getWidth() - PerspectiveImage.WIDTH) / 2);
            // 设置scrollBar在界面地下
            scrollBar.setLayoutX(10);
            scrollBar.setLayoutY(getHeight()-25);
            scrollBar.resize(getWidth()-20,15);
        }
 
        private void update() {
            // 移动元素到新的队列里头
            left.getChildren().clear();
            center.getChildren().clear();
            right.getChildren().clear();
            for (int i = 0; i < centerIndex; i++) {
                left.getChildren().add(items.get(i));
            }
            if(items.isEmpty()==false){
            center.getChildren().add(items.get(centerIndex));
            for (int i = items.size() - 1; i > centerIndex; i--) {
                right.getChildren().add(items.get(i));
            }
            
            
            if (timeline!=null) timeline.stop();// 如果上一个的时间线还在运行则停止
            // 创建新的播放时间线给位置移动动画
            timeline = new Timeline();
            // 给左边的图片添加帧节动画
            final ObservableList<KeyFrame> keyFrames = timeline.getKeyFrames();
            for (int i = 0; i < left.getChildren().size(); i++) {
                final PerspectiveImage it = items.get(i);
                double newX = -left.getChildren().size() *
                SPACING + SPACING * i + LEFT_OFFSET;
                keyFrames.add(new KeyFrame(DURATION,
                        new KeyValue(it.translateXProperty(), newX, INTERPOLATOR),
                        new KeyValue(it.scaleXProperty(), SCALE_SMALL, INTERPOLATOR),
                        new KeyValue(it.scaleYProperty(), SCALE_SMALL, INTERPOLATOR),
                        new KeyValue(it.angle, 45.0, INTERPOLATOR)));
            }
            // 给中间的图片添加帧节动画
            final PerspectiveImage centerItem = items.get(centerIndex);
            keyFrames.add(new KeyFrame(DURATION,
                    new KeyValue(centerItem.translateXProperty(), 0, INTERPOLATOR),
                    new KeyValue(centerItem.scaleXProperty(), 1.0, INTERPOLATOR),
                    new KeyValue(centerItem.scaleYProperty(), 1.0, INTERPOLATOR),
                    new KeyValue(centerItem.angle, 90.0, INTERPOLATOR)));
            // 给右边的图片添加帧节动画
            for (int i = 0; i < right.getChildren().size(); i++) {
                final PerspectiveImage it = items.get(items.size() - i - 1);
                final double newX = right.getChildren().size() *
                SPACING - SPACING * i + RIGHT_OFFSET;
                keyFrames.add(new KeyFrame(DURATION,
                        new KeyValue(it.translateXProperty(), newX, INTERPOLATOR),
                        new KeyValue(it.scaleXProperty(), SCALE_SMALL, INTERPOLATOR),
                        new KeyValue(it.scaleYProperty(), SCALE_SMALL, INTERPOLATOR),
                        new KeyValue(it.angle, 135.0, INTERPOLATOR)));
            }
            // 播放动画 
            timeline.play();
            }
        }
 
        private void shiftToCenter(PerspectiveImage item) {
            for (int i = 0; i < left.getChildren().size(); i++) {
                if (left.getChildren().get(i) == item) {
                    int shiftAmount = left.getChildren().size() - i;
                    shift(shiftAmount);
                    return;
                }
            }
            if (center.getChildren().get(0) == item) {
                return;
            }
            for (int i = 0; i < right.getChildren().size(); i++) {
                if (right.getChildren().get(i) == item) {
                    int shiftAmount = -(right.getChildren().size() - i);
                    shift(shiftAmount);
                    return;
                }
            }
        }
 
        public void shift(int shiftAmount) {
            if (centerIndex <= 0 && shiftAmount > 0) return;
            if (centerIndex >= items.size() - 1 && shiftAmount < 0) return;
            centerIndex -= shiftAmount;
            update();
        }
    }
 
    /**
     * 2.5D视角显示一个绕Y轴旋转的图像   子类2
     */
    public static class PerspectiveImage extends Parent {
        private static final double REFLECTION_SIZE = 0.25;//视觉角度 上下调 这样才有倒影
        private static final double WIDTH = bounds.getWidth()/1.2/1.8;//设置图片大小
//        private static final double WIDTH = 500;//设置图片大小
        private static final double HEIGHT = WIDTH + (WIDTH*REFLECTION_SIZE);
        private static final double RADIUS_H = WIDTH / 2; //宽度压缩比
        private static final double BACK = WIDTH / 10;//背后图片宽度压缩比
        private PerspectiveTransform transform = new PerspectiveTransform();
        /** 角度特性设置 */
        private final DoubleProperty angle = new SimpleDoubleProperty(45) {
            @Override protected void invalidated() {
                // 当角度改变的时候 重新计算
                double lx = (RADIUS_H - Math.sin(Math.toRadians(angle.get())) * RADIUS_H - 1);
                double rx = (RADIUS_H + Math.sin(Math.toRadians(angle.get())) * RADIUS_H + 1);
                double uly = (-Math.cos(Math.toRadians(angle.get())) * BACK);
                double ury = -uly;
                transform.setUlx(lx);
                transform.setUly(uly);
                transform.setUrx(rx);
                transform.setUry(ury);
                transform.setLrx(rx);
                transform.setLry(HEIGHT + uly);
                transform.setLlx(lx);
                transform.setLly(HEIGHT + ury);
            }
        };
        
        public PerspectiveImage(Image image) {
            ImageView imageView = new ImageView(image);
            imageView.setEffect(ReflectionBuilder.create().fraction(REFLECTION_SIZE).build());
            setEffect(transform);
            getChildren().addAll(imageView);
        }
        
        
        
        public final double getAngle() { return angle.getValue(); }
        public final void setAngle(double value) { angle.setValue(value); }
        public final DoubleProperty angleModel() { return angle; }
 
       
    }
 
}
