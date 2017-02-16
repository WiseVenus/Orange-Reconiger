package control;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.effect.ReflectionBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;
 
public   class ImageShowShelf  { 
	//包含两个子类
   
    private static final double WIDTH = 1000, HEIGHT = 800;
 
//    private void init(Stage primaryStage) {
//        Group root = new Group();
//        primaryStage.setResizable(false);
//
//        primaryStage.setScene(new Scene(root, 1000,600));
//        primaryStage.setTitle("检测步骤查看");
//        // load images
//        Image[] images = new Image[30];
//        for (int i = 0; i < images.length; i++) {
//            images[i] = new Image( ImageShowShelf.class.getResource("/images/Orange.png").toExternalForm(),false);
//        }
//        // 展示图片用的展览架
//        DisplayShelf displayShelf = new DisplayShelf(images);
//        displayShelf.setPrefSize(WIDTH, HEIGHT);
//        root.getChildren().add(displayShelf);
//    }
    
    public Stage init(ArrayList<Image> StepMap,Boolean isOK) {
        Group root = new Group();
        Stage StepMapStage = new Stage();
        StepMapStage.setResizable(false);
        StepMapStage.setScene(new Scene(root, 1000,800));
        StepMapStage.setTitle("步骤查看");
      
        
       
        // 展示图片用的展览架
        DisplayShelf displayShelf = new DisplayShelf(StepMap);
        displayShelf.setPrefSize(WIDTH, HEIGHT);
        root.getChildren().add(displayShelf);
        System.out.println("到这");
        
        isOK = true;
        return StepMapStage;
    }
 
    /**
     * 显示图像到展示书架上	子类1
     */
    public static class DisplayShelf extends Region {
        private static final Duration DURATION = Duration.millis(500);
        private static final Interpolator INTERPOLATOR = Interpolator.EASE_BOTH;
        private static final double SPACING = 60;
        private static final double LEFT_OFFSET = -110;
        private static final double RIGHT_OFFSET = 110;
        private static final double SCALE_SMALL = 0.8;//设置后面的缩小比
        private PerspectiveImage[] items;
        private Group centered = new Group();
        private Group left = new Group();
        private Group center = new Group();
        private Group right = new Group();
        private int centerIndex = 0;
        private Timeline timeline;
        private ScrollBar scrollBar = new ScrollBar();
        private boolean localChange = false;
        private Rectangle clip = new Rectangle();
 
//        public DisplayShelf(Image[] images) {
//            // 设置切片
//            setClip(clip);
//            // 用CSS设置背景
//            setStyle("-fx-background-color: linear-gradient(to bottom," + " black 60, #141414 60.1%, black 100%);");
//            // 设置scrollBar的ID
//            scrollBar.setId("DisplayShelfScrollBar");
//            // 创建书架上的东西
//            items = new PerspectiveImage[images.length];
//            for (int i=0; i<images.length; i++) {
//                final PerspectiveImage item = items[i] = new PerspectiveImage(images[i]);
//                final double index = i;
//                item.setOnMouseClicked(new EventHandler<MouseEvent>() {
//                    public void handle(MouseEvent me) {
//                        localChange = true;
//                        scrollBar.setValue(index);
//                        localChange = false;
//                        shiftToCenter(item);
//                    }
//                });
//            }
        
        public DisplayShelf(ArrayList<Image> StepMap) {
            // 设置切片
            setClip(clip);
            // 用CSS设置背景
            setStyle("-fx-background-color: linear-gradient(to bottom," + " black 60, #141414 60.1%, black 100%);");
            // 设置scrollBar的ID
            scrollBar.setId("DisplayShelfScrollBar");
            // 创建书架上的东西
            items = new PerspectiveImage[StepMap.size()];
            for (int i=0; i<StepMap.size(); i++) {
                final PerspectiveImage item = items[i] = new PerspectiveImage(StepMap.get(i));
                final double index = i;
                item.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent me) {
                        localChange = true;
                        scrollBar.setValue(index);
                        localChange = false;
                        shiftToCenter(item);
                    }
                });
            }
            // 设置scrollBar
            scrollBar.setMax(items.length-1);
            scrollBar.setVisibleAmount(1);
            scrollBar.setUnitIncrement(1);
            scrollBar.setBlockIncrement(1);
            scrollBar.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if(!localChange)
                        shiftToCenter(items[(int)scrollBar.getValue()]);
                }
            });
            // 创建内容
            centered.getChildren().addAll(left, right, center);
            getChildren().addAll(centered,scrollBar);
            // 设置键盘的方向键监听
            setFocusTraversable(true);
            setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent ke) {
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
                left.getChildren().add(items[i]);
            }
            center.getChildren().add(items[centerIndex]);
            for (int i = items.length - 1; i > centerIndex; i--) {
                right.getChildren().add(items[i]);
            }
            
            if (timeline!=null) timeline.stop();// 如果上一个的时间线还在运行则停止
            // 创建新的播放时间线给位置移动动画
            timeline = new Timeline();
            // 给左边的图片添加帧节动画
            final ObservableList<KeyFrame> keyFrames = timeline.getKeyFrames();
            for (int i = 0; i < left.getChildren().size(); i++) {
                final PerspectiveImage it = items[i];
                double newX = -left.getChildren().size() *
                SPACING + SPACING * i + LEFT_OFFSET;
                keyFrames.add(new KeyFrame(DURATION,
                        new KeyValue(it.translateXProperty(), newX, INTERPOLATOR),
                        new KeyValue(it.scaleXProperty(), SCALE_SMALL, INTERPOLATOR),
                        new KeyValue(it.scaleYProperty(), SCALE_SMALL, INTERPOLATOR),
                        new KeyValue(it.angle, 45.0, INTERPOLATOR)));
            }
            // 给中间的图片添加帧节动画
            final PerspectiveImage centerItem = items[centerIndex];
            keyFrames.add(new KeyFrame(DURATION,
                    new KeyValue(centerItem.translateXProperty(), 0, INTERPOLATOR),
                    new KeyValue(centerItem.scaleXProperty(), 1.0, INTERPOLATOR),
                    new KeyValue(centerItem.scaleYProperty(), 1.0, INTERPOLATOR),
                    new KeyValue(centerItem.angle, 90.0, INTERPOLATOR)));
            // 给右边的图片添加帧节动画
            for (int i = 0; i < right.getChildren().size(); i++) {
                final PerspectiveImage it = items[items.length - i - 1];
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
            if (centerIndex >= items.length - 1 && shiftAmount < 0) return;
            centerIndex -= shiftAmount;
            update();
        }
    }
 
    /**
     * 2.5D视角显示一个绕Y轴旋转的图像   子类2
     */
    public static class PerspectiveImage extends Parent {
        private static final double REFLECTION_SIZE = 0.25;
        private static final double WIDTH = 370;//200
        private static final double HEIGHT = WIDTH + (WIDTH*REFLECTION_SIZE);
        private static final double RADIUS_H = WIDTH / 2;
        private static final double BACK = WIDTH / 10;
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
        public final double getAngle() { return angle.getValue(); }
        public final void setAngle(double value) { angle.setValue(value); }
        public final DoubleProperty angleModel() { return angle; }
 
        public PerspectiveImage(Image image) {
            ImageView imageView = new ImageView(image);
            imageView.setEffect(ReflectionBuilder.create().fraction(REFLECTION_SIZE).build());
            setEffect(transform);
            getChildren().addAll(imageView);
        }
    }
 
//    public double getSampleWidth() {
//        return 800;//      495*2 
//    }
// 
//    public double getSampleHeight() {
//        return 500; //      300 
//    }
 
     
//    @Override public void start(Stage primaryStage) throws Exception {
//        init(primaryStage);
//        primaryStage.show();
//    }
//    public static void main(String[] args) {
//        launch(args); 
//    }
}
