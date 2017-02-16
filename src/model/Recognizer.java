package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.ImageProducer;
import java.awt.image.Kernel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.LinkedList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;


//各种图像分析方法的集合类
public  class  Recognizer {


	// 像素数组生成图片的方法
		public  javafx.scene.image.Image getImage(int width, int height,int[] pixels) {

			ImageProducer ip = new MemoryImageSource(width, height, pixels, 0, width);
			java.awt.Image imageTemp = Toolkit.getDefaultToolkit().createImage(ip);	//得到新的图片
			
			BufferedImage bImageTemp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			bImageTemp.getGraphics().drawImage(imageTemp, 0, 0,width,height,null );//转化为BufferedImage
			
			WritableImage wwImage=new WritableImage(width, height);
			SwingFXUtils.toFXImage(bImageTemp, wwImage);//转化为WritableImage方便放入ImageView

			return wwImage;
		}



   

    // 复制数组
    public  int[] copyPixels(int[] pixelsAry) {
        int[] copyAry = new int[pixelsAry.length];
        for (int i = 0; i < copyAry.length; i++) {
            copyAry[i] = pixelsAry[i];
        }
        return copyAry;
    }

    // 得到图片像素数组 这里的像素数组是一个int类型包含有透明 红 绿 蓝 4值每值8个的像素组
    public  int[] getImagePixels(Image img, int width, int height) {
        int[] pixels = new int[width * height];

        PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);//实例化像素抓取工具
        try {
            pg.grabPixels();//抓取像素
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return pixels;
    }

   
	
    // 得到图片灰度化数组
    public  int[] getGrayPixels(int[] pixels, int width, int height,
            int[] GrayPixelsTemp) {

    	
        ColorModel cm = ColorModel.getRGBdefault();//实例化色值工具
        
        int[] grayPixels = new int[width * height];
        double compare = 0.2, bright = 2;
        int red, green, blue, alpha;

      //得到灰度图数组的算法
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {//循环顺序列遍历数组
                alpha = cm.getAlpha(pixels[j * width + i]);//透明度
                red = cm.getRed(pixels[j * width + i]);
                green = cm.getGreen(pixels[j * width + i]);
                blue = cm.getBlue(pixels[j * width + i]);

                grayPixels[j * width + i] = 2 * red - green - blue;//得到最初灰度化值   这样最高就510 超白
                													//橘子肯定红的多


                if (grayPixels[j * width + i] < 0) {//最初值小于0 置0 超黑了
                    red = 0;
                    blue = 0;
                    green = 0;//置黑
                    grayPixels[j * width + i] = 0;//表黑
                } else if (grayPixels[j * width + i] > 255) {//最初值大于255 置255 超白了 
                    red = 255;
                    green = 255;
                    blue = 255;//置白
                    grayPixels[j * width + i] = 255;//表白
                } else {
                    red = grayPixels[j * width + i];
                    green = grayPixels[j * width + i];
                    blue = grayPixels[j * width + i];//三色一致未最大(255)最小(0)置灰
                    //grayPixels[j * width + i]保持原值
                }
                
                //优化算法
                grayPixels[j * width + i] = (int) (compare * grayPixels[j * width + i] + bright); //这样最高就0-53之间 灰色
                									
                

                //缓存用  展示灰度图用到
                GrayPixelsTemp[j * width + i] = alpha << 24 | red << 16 | green << 8 | blue; 
                //								值左移24			左移16		左移8		
                //像素是个32位的真彩色                             第3字节                  第2字节             第1字节                第0字节
               //                               alpha         red         green        blue
                
            }
        }

//        for (int i = 0; i < width * height; i++) {//最终赋值
//            grayPixels[i] = (int) (compare * grayPixels[i] + bright); //这样最高就0-53之间
//        }

        return grayPixels;//返回假灰度图数组 只为方便计算用
    }

    // 得到图片二值化数组
    public  int[] getBinaryPixels(int[] grayPixels, int[] pixels,
            int width, int height) {
        int[] binaryPixels = copyPixels(grayPixels);
//        int[] grayPixelsCount = new int[256];
        int[] grayPixelsCount = new int[54];

        for (int i = 0; i < width * height; i++) {
            grayPixelsCount[grayPixels[i]]++;//0-53哪个颜色有就+1
            
//            System.out.println(grayPixels[i]);
        }

        int threshold = getThreshold(grayPixelsCount);//得到阈值

        ColorModel cm = ColorModel.getRGBdefault();//实例化色值工具
        int red = 0, green = 0, blue = 0, alpha;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                alpha = cm.getAlpha(pixels[i]);
                if (grayPixels[j * width + i] > threshold) {//大于阈值（更亮）置黑 
                    red = 0;
                    green = 0;
                    blue = 0;
                } else {        //小于阈值（更黑）置白         //本来是橘子比较白背景比较黑 现在橘子变黑而背景变白 
                    red = 255;
                    green = 255;
                    blue = 255;
                }

                binaryPixels[j * width + i] = alpha << 24 | red << 16| green << 8 | blue;//返回纯真二值图 
            }
        }
        return binaryPixels;
    }

  
    //计算阈值
    public  int getThreshold(int[] grayCountAry) {
    	/*
    	 * 一堆初始化
    	 */
        int Y, Amount = 0;
        int PixelBack = 0, PixelFore = 0, PixelIntegralBack = 0, PixelIntegralFore = 0, PixelIntegral = 0;
        double OmegaBack, OmegaFore, MicroBack, MicroFore, SigmaB, Sigma; // 类间方差;
        int MinValue, MaxValue;
        int threshold = 0;

        
//        for (MinValue = 0; MinValue < 256 && grayCountAry[MinValue] == 0; MinValue++)
//			;
//        for (MaxValue = 255; MaxValue > MinValue && grayCountAry[MinValue] == 0; MaxValue--)
//			;
         //优化后计算量减少 
        for (MinValue = 0; MinValue <54 && grayCountAry[MinValue] == 0; MinValue++)
        	;
        for (MaxValue = 53; MaxValue > MinValue && grayCountAry[MinValue] == 0; MaxValue--)
        	;
        if (MaxValue == MinValue) {
        	//这种情况只有一种颜色
            return MaxValue; 
        }
        if (MinValue + 1 == MaxValue) {
        	
        	//这种情况就只有2种颜色  无需继续 以其中之一做阈值即可 
            return MinValue; 
        }
        for (Y = MinValue; Y <= MaxValue; Y++) {
            Amount += grayCountAry[Y]; // 总像素总数
        }
        
        PixelIntegral = 0;
        for (Y = MinValue; Y <= MaxValue; Y++) {
        	//Y像素
        	//grayCountAry[Y] 对应该像素有几个
            PixelIntegral += grayCountAry[Y] * Y; //总像素大小
        }
        
        
        SigmaB = -1;
        for (Y = MinValue; Y < MaxValue; Y++) {
            PixelBack = PixelBack + grayCountAry[Y];//Y前的像素(比Y像素小)的个数
            PixelFore = Amount - PixelBack;//Y后像素的个数
            
            OmegaBack = (double) PixelBack / Amount;//Y前的像素占总像素个数的比例
            OmegaFore = (double) PixelFore / Amount;//Y后像素占总像素的比例
            
                                    
            PixelIntegralBack += grayCountAry[Y] * Y;//Y像素个数 *该像素大小=该像素总大小
            PixelIntegralFore = PixelIntegral - PixelIntegralBack;//Y后像素总大小
            
            MicroBack = (double) PixelIntegralBack / PixelBack;//这个像素总大小/该像素前像素个数=前面像素平均大小
            MicroFore = (double) PixelIntegralFore / PixelFore;//剩余像素总大小/剩余像素个数=Y后像素平均大小
            
            													
            Sigma = OmegaBack * OmegaFore * (MicroBack - MicroFore)* (MicroBack - MicroFore);
            //      Y前像素占总像素个数的比例*Y后像素占总像素的比例*（Y前像素平均大小-Y后像素平均大小）的平方
            //      如果是Y是临界像素点  Y前像素占总像素个数的比例会达到最大 Y后像素占总像素的比例也会达到最大
            //      Y前像素平均大小-Y后像素平均大小 的绝对值也会最大  这里平方处理   所以 Sigma将全图最大 
            //		故而Sigma最大时候就是Y为临界像素点的时候
            
            if (Sigma > SigmaB) {
                SigmaB = Sigma;
                threshold = Y;//最大的Sigma对应的Y就是临界像素点
            }
        }
        return threshold;
    }

    // 返回区域标记后的数组
    public  int[] getTagPixels(int[] tagPixels, int width, int height, int[][] pixelsTemp) {
    	/*
    	 * 一堆初始化   
    	 */
        int[][] m = new int[width][height];
        int i, j, number = 0, maxHZ = 0;
        int[] HZofNumber = new int[10000];//HZ->频率 对应某个数字出现的频率
        LinkedList<Seed> p = new LinkedList<Seed>();//初始化前景链表
        ColorModel cm = ColorModel.getRGBdefault();//实例化色值工具
        for (i = 0; i < 10000; i++) {
            HZofNumber[i] = 0;
        }

        int[][] direction = new int[8][2];
        direction[0][0] = -1; direction[0][1] = -1;
        direction[1][0] = -1; direction[1][1] =  0;
        direction[2][0] = -1; direction[2][1] =  1;
        direction[3][0] =  0; direction[3][1] =  1;
        direction[4][0] =  1; direction[4][1] =  1;
        direction[5][0] =  1; direction[5][1] =  0;
        direction[6][0] =  1; direction[6][1] = -1;
        direction[7][0] =  0; direction[7][1] = -1;
 
        /**
         -1	-1
         -1	 0
         -1	 1
          0	 1
          1	 1
          1  0
          1	-1
          0 -1
         */
        
        //标记开始
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                if (cm.getRGB(tagPixels[j * width + i]) == -16777216//黑色
                        && m[i][j] == 0) { // 颜色是黑色并且没被标记
                    number++;
                    Seed point = new Seed(i, j);
                    p.add(point); // 将找到没标记的前景色点,加入队列
                    m[i][j] = number; // 标记上某个数字 反正是>0的
                    HZofNumber[number]++;
                    while (!p.isEmpty()) {//再检验
                        Seed q;
                        q = p.pollLast();//获取并移除此链表的最后一个元素 
                        for (int k = 0; k < 8; k++) {//  8邻域搜索 
                            int a = q.x + direction[k][0];
                            int b = q.y + direction[k][1];//找出的前景点+上这些值得到周围8个位置的点
                            if (a < width && a >= 0 && b < height && b >= 0) {//在图的范围内
                                if (cm.getRGB(tagPixels[b * width + a]) == -16777216
                                        && m[a][b] == 0) {//8点中某点也是黑色而且没被标记 就标记上代表同一区域的数字
                                    Seed tempPoint = new Seed(a, b);
                                    p.add(tempPoint); // 添加没标记的前景点 待会继续while循环
                                    m[a][b] = number; // 图上该点标记上某个数字
                                    HZofNumber[number]++;//对应某个数字出现的次数
                                    
                                    //图上某个位置放着某个数字 对应HZofNumber[number]就是这个数字出现的次数 
                                    //如果周围黑色联通区域很大的 这个数字出现会很多次
                                }
                            }
                        }
                    }
                    if (HZofNumber[number] > maxHZ) {
                        maxHZ = HZofNumber[number];
                    }
                }
            }
        }
		// 标记结束

        // 去掉比阀值小的区域  一些联通区域比较小不算是橘子当作背景去掉
        int threshold = (int) (maxHZ / 3);// 最大数/3做临界点
        int red, green, blue, alpha;

        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                alpha = cm.getAlpha(tagPixels[j * width + i]);
                if (HZofNumber[m[i][j]] < threshold) {//图上这个点对应的数字出现的数字如果小于临界点 置白去掉
                    red = 255;
                    green = 255;
                    blue = 255;
                    tagPixels[j * width + i] = alpha << 24 | red << 16| green << 8 | blue;
                }
            }
        }
        
        pixelsTemp = m;//标记粟海居 接下来区域填充(同色同域填)及边缘检测用  
        
        return tagPixels;

    }

    // 返回提取到的边缘的数组 Robert算子
    public  int[] getEdgePixels(int[] pixels, int width, int height) {

//        BufferedImage grayImage = new BufferedImage(width, height,
//                BufferedImage.TYPE_INT_RGB);
        // 对图像进行边缘提取  
        //Alpha值保持不变
        ColorModel cm = ColorModel.getRGBdefault();
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                // 对图像进行边缘提取 
            	/**
            	 * 3个交叉相减 黑白边缘会得到最大差值
            	 */
                int alpha = cm.getAlpha(pixels[i * width + j]);
                int red5 = cm.getRed(pixels[i * width + j]);
                int red6 = cm.getRed(pixels[i * width + j + 1]);
                int red8 = cm.getRed(pixels[(i + 1) * width + j]);
                int red9 = cm.getRed(pixels[(i + 1) * width + j + 1]);

                int robertRed = Math.max(Math.abs(red5 - red9),
                        Math.abs(red8 - red6));

                int green5 = cm.getGreen(pixels[i * width + j]);
                int green6 = cm.getGreen(pixels[i * width + j + 1]);
                int green8 = cm.getGreen(pixels[(i + 1) * width + j]);
                int green9 = cm.getGreen(pixels[(i + 1) * width + j + 1]);

                int robertGreen = Math.max(Math.abs(green5 - green9),
                        Math.abs(green8 - green6));

                int blue5 = cm.getBlue(pixels[i * width + j]);
                int blue6 = cm.getBlue(pixels[i * width + j + 1]);
                int blue8 = cm.getBlue(pixels[(i + 1) * width + j]);
                int blue9 = cm.getBlue(pixels[(i + 1) * width + j + 1]);

                int robertBlue = Math.max(Math.abs(blue5 - blue9),
                		Math.abs(blue8 - blue6));
                pixels[i * width + j] = alpha << 24 | robertRed << 16
                        | robertGreen << 8 | robertBlue;
                if ((255 - cm.getBlue(pixels[i * width + j])) < 30) {//白边(离255最近)置黑
                    robertRed = 0;
                    robertGreen = 0;
                    robertBlue = 0;
                } else {//其他情况就是全黑色的或者是全白色的 被255一减一般很大 置白
                    robertRed = 255;
                    robertGreen = 255;
                    robertBlue = 255;
                }
                pixels[i * width + j] = alpha << 24 | robertRed << 16 | robertGreen << 8 | robertBlue; //设置回数组
//                grayImage.setRGB(j, i, pixels[i * width + j]);
            }
        }

        return pixels;

    }

   
  

    //对二值图像进行减噪处理，图像形态学中的腐蚀和膨胀能很好的解决此问题
    // 边缘检测处理  返回腐蚀后的数组
    public  int[] getCorrosionPixels(int[] pixels, int width, int height) {
        int[] corrosionPixels = copyPixels(pixels);
        int[][] corrosion = new int[width][height];
        int k = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                corrosion[i][j] = corrosionPixels[k++];
            }
        }

        ColorModel cm = ColorModel.getRGBdefault();
        int red = 0, green = 0, blue = 0, alpha;
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                boolean flag = true;
                alpha = cm.getAlpha(corrosion[j][i]);
                for (int m = i - 1; m < i + 2 && flag; m++) {
                    for (int n = j - 1; n < j + 2 && flag; n++) {
                        if ((corrosion[j][i] & corrosion[n][m]) != 255) {
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    red = 255;
                    green = 255;
                    blue = 255;
                } else {
                    red = 0;
                    green = 0;
                    blue = 0;
                }

                corrosion[j][i] = alpha << 24 | red << 16 | green << 8 | blue;

            }
        }

        for (int j = 0; j < height; j++) {
            red = 255;
            green = 255;
            blue = 255;
            alpha = cm.getAlpha(corrosion[0][j]);
            corrosion[0][j] = alpha << 24 | red << 16 | green << 8 | blue;

            alpha = cm.getAlpha(corrosion[width - 1][j]);
            corrosion[width - 1][j] = alpha << 24 | red << 16 | green << 8
                    | blue;
        }

        for (int i = 0; i < width; i++) {
            red = 255;
            green = 255;
            blue = 255;
            alpha = cm.getAlpha(corrosion[i][0]);
            corrosion[i][0] = alpha << 24 | red << 16 | green << 8 | blue;

            alpha = cm.getAlpha(corrosion[i][height - 1]);
            corrosion[i][height - 1] = alpha << 24 | red << 16 | green << 8
                    | blue;
        }

        k = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                corrosionPixels[k++] = corrosion[i][j];
            }
        }

        return corrosionPixels;
    }

    // 返回膨胀之后的数组
    public  int[] getExpandsionPixels(int[] pixels, int width, int height) {
        int[] expandsionPixels = copyPixels(pixels);
        int[][] expandsion = new int[width][height];
        int k = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                expandsion[i][j] = expandsionPixels[k++];
            }
        }

        ColorModel cm = ColorModel.getRGBdefault();
        int red = 0, green = 0, blue = 0, alpha;
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                boolean flag = true;
                alpha = cm.getAlpha(expandsion[j][i]);
                for (int m = i - 1; m < i + 2 && flag; m++) {
                    for (int n = j - 1; n < j + 2 && flag; n++) {
                        if ((expandsion[j][i] & expandsion[n][m]) != 0) {
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    red = 0;
                    green = 0;
                    blue = 0;
                } else {
                    red = 255;
                    green = 255;
                    blue = 255;
                }

                expandsion[j][i] = alpha << 24 | red << 16 | green << 8 | blue;

            }
        }

        for (int j = 0; j < height; j++) {
            red = 255;
            green = 255;
            blue = 255;
            alpha = cm.getAlpha(expandsion[0][j]);
            expandsion[0][j] = alpha << 24 | red << 16 | green << 8 | blue;

            alpha = cm.getAlpha(expandsion[width - 1][j]);
            expandsion[width - 1][j] = alpha << 24 | red << 16 | green << 8
                    | blue;
        }

        for (int i = 0; i < width; i++) {
            red = 255;
            green = 255;
            blue = 255;
            alpha = cm.getAlpha(expandsion[i][0]);
            expandsion[i][0] = alpha << 24 | red << 16 | green << 8 | blue;

            alpha = cm.getAlpha(expandsion[i][height - 1]);
            expandsion[i][height - 1] = alpha << 24 | red << 16 | green << 8
                    | blue;
        }

        k = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                expandsionPixels[k++] = expandsion[i][j];
            }
        }

        return expandsionPixels;
    }

    // 边缘检测
    public  BufferedImage getEdgeImage(BufferedImage originalPic) {
        int imageWidth = originalPic.getWidth();
        int imageHeight = originalPic.getHeight();

        BufferedImage newPic = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_3BYTE_BGR);

        float[] elements = {0.0f, -1.0f, 0.0f, -1.0f, 4.0f, -1.0f, 0.0f,
            -1.0f, 0.0f};

        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        cop.filter(originalPic, newPic);
        return newPic;
    }

   
	
    // 图片锐化
    public  BufferedImage getSharperPicture(BufferedImage originalPic) {
        int imageWidth = originalPic.getWidth();
        int imageHeight = originalPic.getHeight();

        BufferedImage newPic = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_3BYTE_BGR);
        float[] data = {-1.0f, -1.0f, -1.0f, -1.0f, 10.0f, -1.0f, -1.0f,
            -1.0f, -1.0f};

        Kernel kernel = new Kernel(3, 3, data);
        ConvolveOp co = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        co.filter(originalPic, newPic);
        return newPic;
    }

	
    // 图片的平滑和过滤
    public  int[] laplace2DFileter(int[] data, int width, int height) {

        int filterData[] = new int[data.length];
        int min = 10000;
        int max = -10000;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == 0 || i == height - 1 || j == 0 || j == width - 1) {
                    filterData[i * width + j] = data[i * width + j];
                } else {
                    filterData[i * width + j] = 9 * data[i * width + j]
                            - data[i * width + j - 1] - data[i * width + j + 1]
                            - data[(i - 1) * width + j]
                            - data[(i - 1) * width + j - 1]
                            - data[(i - 1) * width + j + 1]
                            - data[(i + 1) * width + j]
                            - data[(i + 1) * width + j - 1]
                            - data[(i + 1) * width + j + 1];
                }
                if (filterData[i * width + j] < min) {
                    min = filterData[i * width + j];
                }
                if (filterData[i * width + j] > max) {
                    max = filterData[i * width + j];
                }
            }
        }

        for (int i = 0; i < width * height; i++) {
            filterData[i] = (filterData[i] - min) * 255 / (max - min);
        }
        return filterData;
    }

    // 返回区域填充后的数组
//    public  int[] getfillPixels(int[] fillPixels, int width, int height,
//            int[][] m) {
//
//        int alpha;
//        int n;// 扫描到第几个
//        ColorModel cm = ColorModel.getRGBdefault();
//        
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                n = j;
//                if (cm.getBlue(fillPixels[j * width + i]) == 0) {//如果是黑色
//                    // 从后面开始往回找
//                    for (int k = height - 1; k > j; k--) {
//                        if (cm.getBlue(fillPixels[k * width + i]) == 0
//                                && m[i][j] == m[i][k]) {//同色同域填
//                            for (; n <= k; n++) {//一列填黑
//                                alpha = cm.getAlpha(fillPixels[n * width + i]);
//                                fillPixels[n * width + i] = alpha << 24
//                                        | 0 << 16 | 0 << 8 | 0;
//                            }
//                            break;
//                        }
//                    }
//                }
//
//                j = n;
//            }
//        }
//        return fillPixels;
//    }
    
    // 返回区域填充后的数组
    public  int[] getfillPixels(int[] fillPixels, int width, int height,
    		int[][] m) {
    	
    	int alpha;
    	int n;// 扫描到第几个
    	
    	ColorModel cm = ColorModel.getRGBdefault();
    	
    	for (int i = 0; i < width; i++) {
    		for (int j = 0; j < height; j++) {
    			n = j;
    			if (cm.getBlue(fillPixels[j * width + i]) == 0) {//如果是黑色
    				// 从后面开始往回找
    				for (int k = height - 1; k > j; k--) {
    					if (cm.getBlue(fillPixels[k * width + i]) == 0
    							&& m[i][j] == m[i][k]) {//同色同域填
    						for (; n <= k; n++) {//一列填黑
    							alpha = cm.getAlpha(fillPixels[n * width + i]);
    							fillPixels[n * width + i] = alpha << 24
    									| 0 << 16 | 0 << 8 | 0;
    						}
    						break;
    					}
    				}
    			}
    			
    			j = n;
    		}
    	}
    	
    	return fillPixels;
    }

    // 改进的Hough赫夫变换圆检测算法 
    public  LinkedList<Orange> hough(int[] edgePixels, int width, int height) {

        LinkedList<Seed> edgePointList = new LinkedList<>(); //边缘检测图中  集合了边界点的链表

        LinkedList<Orange> OrangeList = new LinkedList<>();  //检测到的橘子个数链表

        Seed point1, point2, point3, pos;//缓存用  下面用到
        
        int[] houghPiexls = copyPixels(edgePixels);
        
        double r; // r是所求圆的半径
        
        double mat1, mat2, mat3; //三个圆的参数 由他们组合表示出圆心 半径 
        
        int  Threshold ; //阈值 链表里的点少于这个值将无法构成圆
        
        int count = 0; // 计算满足条件的点的个数（点到圆心的距离 跟半径差值小于一定值即视为满足）
        
        Seed circle = new Seed(); //可以从周长处取三点确定圆的半径与圆心 Seed的a b都能表示就用它表示了 
        
        double rMax, rMin;//最大半径与最小半径  需要确定边缘点数才能确定 （一个点就是一个int类型 相当于1）
       
        ColorModel cm = ColorModel.getRGBdefault();//实例化色值工具
        
        //-----------------------开始找边缘点 找到放入边缘点链表----------
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                if (cm.getBlue(houghPiexls[j * width + i]) == 0) {//获取到黑色点
                    if (j == height - 1) {// 它是图片末尾 没法判断它下面是不是白色的了  这种情况它直接就是边界点了
                        pos = new Seed(i, j);
                        edgePointList.add(pos);
                        
                    } else if (cm.getBlue(houghPiexls[j * width + i + 1]) != 0) {//它下面不是黑色的点了 那就只有白色了  标明这个点是边界点
                        pos = new Seed(i, j);
                        edgePointList.add(pos);
                       
                    }
                }
            }
        }
      //------------------------------寻找结束-------------------------
        
        
        //有了周长 就可以确定最大最大半径 最小半径 跟最少多少个点才能组成圆了
        rMin = ((edgePointList.size() /3) / 3.14) / 2;//最小半径
        
        rMax = (edgePointList.size() / 3.14) / 2;//最大半径
        
        Threshold = (int) edgePointList.size() / 10; // 定阀值，当链表里的点少于m2时认为剩下的点不足以构成一个圆，可以停止检测
        
        int failCount = 0;//一个小缓存
       
        //开始检测
        while (edgePointList.size() > Threshold && failCount < rMax *4) {
            //  随机取三个点求参数后得到圆心半径
        	
            point1 = edgePointList.get((int) (Math.random() * edgePointList.size()));
            point2 = edgePointList.get((int) (Math.random() * edgePointList.size()));
            point3 = edgePointList.get((int) (Math.random() * edgePointList.size())); // 随机取出3个点
            
            //取到的点可能共线 共线的两种情况
            if (point1.y == point2.y || point2.y == point3.y) {//Y共线    重新开始取点
                continue;
            }
            double h1 = (point2.x - point1.x) / (point2.y - point1.y);
            double h2 = (point3.x - point2.x) / (point3.y - point2.y);
            if (h1 == h2) {//斜率相同=共线  重新开始取点
                continue;
            }

            
            // 求圆的参数 mat1 mat2 mat3 圆心位置跟半径由这三个参数表示得到
            mat1 = ((point2.x * point2.x + point2.y * point2.y) 
            		-(point1.x* point1.x + point1.y * point1.y))
                    *(2 * (point3.y - point1.y))    
                    - 
                    ((point3.x * point3.x + point3.y * point3.y) 
                    - (point1.x * point1.x + point1.y * point1.y))
                    * (2 * (point2.y - point1.y));
            
            mat2 = ((point3.x * point3.x + point3.y * point3.y) - (point1.x
                    * point1.x + point1.y * point1.y))
                     *(2 * (point2.x - point1.x))
                    - 
                    ((point2.x * point2.x + point2.y * point2.y) 
                     -(point1.x * point1.x + point1.y * point1.y))
                    *(2 * (point3.x - point1.x));
            
            mat3 = 4* ((point2.x - point1.x) * (point3.y - point1.y) - (point3.x - point1.x)
                    * (point2.y - point1.y));
            
			// 圆心位置和半径大小
            // circle为以这三点算出的待检测圆 看会不会小于最小半径 毕竟可能取得的三点离太近
            circle.x = (int) (mat1 / mat3);
            circle.y = (int) (mat2 / mat3);
            r = Math.sqrt((double) ((point1.x - circle.x) * (point1.x - circle.x) + (point1.y - circle.y)
                    * (point1.y - circle.y))); // 3点确定圆心和半径  

            
            // 如果半径小于最小半径，即那三点取得不好 （至少要有两点连线通过圆心） 重新while再取 
            if (r < rMin) {
            	failCount++;
                continue;
            }
            
     

            double C =  3.14 * r ;//周长
           
//            LinkedList<Seed> edgePointList1 = new LinkedList<Seed>();
            
            for (int i = 0; i < edgePointList.size(); i++) { // 所有边缘元素都对比一次
                pos = edgePointList.get(i); // 找出所确定的圆的外接正方形内的所有点，认为他们都是同一个圆上的点

                if (pos.x >= (circle.x - r) && pos.x <= (circle.x + r)
                        && pos.y <= (circle.y + r) && pos.y >= (circle.y - r)) {//如果这个点在圆的外接矩阵内
                	
       
                	
                    if (Math.abs(Math.sqrt((double) ((pos.x - circle.x)
                            * (pos.x - circle.x) + (pos.y - circle.y) * (pos.y - circle.y)))
                            - r) < 8) {//边缘点到圆中心距离的绝对值正负小于8就算是圆上的点了 count1++
                        count++;
                    }

                }
            }
            if (count >= C) { // 如果所求圆是真圆,那边缘元素应该大于周长    则把该圆加入圆的队列 
            
            	
                Orange orange = new Orange();
                orange.a = circle.x;
                orange.b = circle.y;
                orange.r = (int) r;
                OrangeList.add(orange);
                for (int i = 0; i < edgePointList.size(); i++) { // 所有元素都对比次
                    pos = edgePointList.get(i);

                    if (Math.abs(Math.sqrt((double) ((pos.x - circle.x)
                            * (pos.x - circle.x) + (pos.y - circle.y) * (pos.y - circle.y)))
                            - r) < 5) {//边缘点到这个圆中心距离的绝对值正负小于5就算是这个圆上的点了 
                                       // 这个圆已经确定 所以不需要这些边缘点链表移除这个点
                        edgePointList.remove(i);
                    }

                }
            } else {
            	failCount++;
            }
            count = 0;
        }


        return OrangeList;
    }
    
    
    //圈出橘子的方法
    public BufferedImage draw(BufferedImage wbufferedImage_For_CreateGraphics ,LinkedList<Orange> orange  ,List<Spot> comparison,int TYPE){
    	
    	
    	if(TYPE==1){
    	//圈出所有橘子用
		 Graphics2D g = wbufferedImage_For_CreateGraphics.createGraphics();
        g.setColor(new Color(100, 100, 200));
        g.setStroke(new BasicStroke(3));
        for (int i = 0; i < orange.size(); i++) {
            int x = orange.get(i).a;
            int y = orange.get(i).b;
            int r = orange.get(i).r;
            g.drawArc(x - r, y - r, 2 * r, 2 * r, 0, 360);
            g.drawLine(x + 5, y, x - 5, y);
            g.drawLine(x, y + 5, x, y - 5);
           
        }
        g.dispose();
 
    	}
    	
    	if(TYPE==2){
    		//圈出所有橘子用
   		    Graphics2D g = wbufferedImage_For_CreateGraphics.createGraphics();
            g.setColor(new Color(100, 100, 200));
            g.setStroke(new BasicStroke(3));
   		 for (int i = 0; i < comparison.size(); i++) {
                int x = comparison.get(i).x *4;
                int y = comparison.get(i).y *4;
                int r = comparison.get(i).r *4;
                g.drawArc(x - r, y - r, 2 * r, 2 * r, 0, 360);
                g.drawLine(x + 5, y, x - 5, y);
                g.drawLine(x, y + 5, x, y - 5);
			
		      }
   		 g.dispose();
    	}
    	
    	return wbufferedImage_For_CreateGraphics;
    }

////      Hough赫夫变换
//    public  LinkedList<Orange> getHoughPixels( int[] edgePixels,
//            int width, int height) {
//
//        int[] houghPixels = copyPixels(edgePixels);
//
//        // 边缘队列
//        LinkedList<Seed> v = new LinkedList<>();
//        // 圆的队列
//        LinkedList<Orange> p = new LinkedList<>();
//
//        ColorModel cm = ColorModel.getRGBdefault();
//
//        // 测试点
//        Seed pos;
//
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//				// 按列查找
//                // 这像素点为边缘点  
//            	//一个像素宽度的点
//                if (cm.getBlue(houghPixels[j * width + i]) == 0) {
//                    pos = new Seed(i, j);
//                    // pos.out();
//                    v.add(pos);
//
//                }
//
//            }
//        }
//
//       
//
//        Seed point1, point2, point3;
//        int m1, m2, m3, k = 0, kmax = 9000;
//        // 三个参数分别为最小半径，最大半径
//        double min_radius, max_radius;
//
//        min_radius = (int) ((v.size() / 3) / 3.14) / 2;
//        max_radius = (int) (v.size() / 3.14) / 2;
//        System.out.println("min_radius = " + min_radius + ",max_radius = "
//                + max_radius);
//
//        while (k < kmax) {
//
//            System.out.println("k = " + k);
//
//            int d = (int) (Math.random() * v.size());
//            point1 = v.get(d);
//            int b = (int) (v.size() / 20);
//            if (d + b >= v.size()) {
//                b = v.size() - d - 1;
//            }
//            point2 = v.get(d + b);
//            if (d - b < 0) {
//                b = d;
//            }
//            point3 = v.get(d - b);
//
//
//            // 共线的点不算;
//            if (point1.y == point2.y || point2.y == point3.y) {
//                k++;
//                continue;
//
//            }
//            double h1 = (point2.x - point1.x) / (point2.y - point1.y);
//            double h2 = (point3.x - point2.x) / (point3.y - point2.y);
//            if (h1 == h2) {
//                k++;
//                continue;
//            }
//
//            Orange ctest = new Orange();
//            // 求圆的参数
//            m1 = ((point2.x * point2.x + point2.y * point2.y) - (point1.x
//                    * point1.x + point1.y * point1.y))
//                    * (2 * (point3.y - point1.y))
//                    - ((point3.x * point3.x + point3.y * point3.y) - (point1.x
//                    * point1.x + point1.y * point1.y))
//                    * (2 * (point2.y - point1.y));
//            m2 = (2 * (point2.x - point1.x))
//                    * ((point3.x * point3.x + point3.y * point3.y) - (point1.x
//                    * point1.x + point1.y * point1.y))
//                    - (2 * (point3.x - point1.x))
//                    * ((point2.x * point2.x + point2.y * point2.y) - (point1.x
//                    * point1.x + point1.y * point1.y));
//            m3 = 4 * ((point2.x - point1.x) * (point3.y - point1.y) - (point3.x - point1.x)
//                    * (point2.y - point1.y));
//
//            // 圆心位置和半径大小
//            ctest.a = (int) (m1 / m3);
//            ctest.b = (int) (m2 / m3);
//            double r = 0;
//            r = Math.sqrt((double) ((point1.x - ctest.a) * (point1.x - ctest.a) + (point1.y - ctest.b)
//                    * (point1.y - ctest.b)));
//            r += Math.sqrt((double) ((point2.x - ctest.a)
//                    * (point2.x - ctest.a) + (point2.y - ctest.b)
//                    * (point2.y - ctest.b)));
//            r += Math.sqrt((double) ((point3.x - ctest.a)
//                    * (point3.x - ctest.a) + (point3.y - ctest.b)
//                    * (point3.y - ctest.b)));
//            ctest.r = (int) (r / 3);
//
//            if (ctest.r < min_radius || ctest.r > max_radius) {
//                k++;
//                continue;
//            }
//            int i;
//            for (i = 0; i < p.size(); i++) {
//                if (ctest.r < p.get(i).r) {
//                    p.add(i, ctest);
//                    break;
//                }
//            }
//            if (i == p.size()) {
//                p.add(ctest);
//
//
//            }
//            k++;
//        }
//
//       
//        // 圆的队列
//        LinkedList<Orange> pc = new LinkedList<>();
//      //（半径相近，圆心相近的圆）
//        for (int i = 0; i < p.size(); i++) {
//            for (int j = 0; j < p.size(); j++) {
//                if (j != i) {
//                    int x = 3;
//
//                    
//                    if (Math.abs(p.get(j).r - p.get(i).r) < 20) {
//                        if ((Math.abs(p.get(j).a - p.get(i).a) < 25)
//                                && (Math.abs(p.get(j).b - p.get(i).b) < 25)) {
//                            p.get(i).number++;
//                            p.remove(j);
//                        }
//                    } else {
//                        break;
//                    }
//                }
//            }
//            if (p.get(i).number != 0) {
//                pc.add(p.get(i));
//            }
//        }
//
//
//        // 统计圆心相近的圆个数
//        LinkedList<Orange> pt = new LinkedList<>();
//        for (int i = 0; i < pc.size(); i++) {
//
//            double ca = pc.get(i).a, cb = pc.get(i).b, cr = pc.get(i).r;
//            // 用于求平均值
//            int num = 1;
//            for (int j = i + 1; j < pc.size(); j++) {
//                if ((Math.abs(pc.get(i).a - pc.get(j).a)) < 10
//                        && (Math.abs(pc.get(i).b - pc.get(j).b)) < 10) {
//                    ca += pc.get(j).a;
//                    cb += pc.get(j).b;
//                    cr += pc.get(j).r;
//                    pc.get(i).number += pc.get(j).number;
//                    num++;
//                    pc.remove(j);
//                    j--;
//                }
//            }
//            pc.get(i).a = (int) (ca / num);
//            pc.get(i).b = (int) (cb / num);
//            pc.get(i).r = (int) (cr / num);
//            if (num > 1 || pc.get(i).number > 1) {
//                pt.add(pc.get(i));
//            }
//        }
//
//        return pt;
//    }

}
