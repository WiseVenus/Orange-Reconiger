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


//����ͼ����������ļ�����
public  class  Recognizer {


	// ������������ͼƬ�ķ���
		public  javafx.scene.image.Image getImage(int width, int height,int[] pixels) {

			ImageProducer ip = new MemoryImageSource(width, height, pixels, 0, width);
			java.awt.Image imageTemp = Toolkit.getDefaultToolkit().createImage(ip);	//�õ��µ�ͼƬ
			
			BufferedImage bImageTemp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			bImageTemp.getGraphics().drawImage(imageTemp, 0, 0,width,height,null );//ת��ΪBufferedImage
			
			WritableImage wwImage=new WritableImage(width, height);
			SwingFXUtils.toFXImage(bImageTemp, wwImage);//ת��ΪWritableImage�������ImageView

			return wwImage;
		}



   

    // ��������
    public  int[] copyPixels(int[] pixelsAry) {
        int[] copyAry = new int[pixelsAry.length];
        for (int i = 0; i < copyAry.length; i++) {
            copyAry[i] = pixelsAry[i];
        }
        return copyAry;
    }

    // �õ�ͼƬ�������� ���������������һ��int���Ͱ�����͸�� �� �� �� 4ֵÿֵ8����������
    public  int[] getImagePixels(Image img, int width, int height) {
        int[] pixels = new int[width * height];

        PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);//ʵ��������ץȡ����
        try {
            pg.grabPixels();//ץȡ����
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return pixels;
    }

   
	
    // �õ�ͼƬ�ҶȻ�����
    public  int[] getGrayPixels(int[] pixels, int width, int height,
            int[] GrayPixelsTemp) {

    	
        ColorModel cm = ColorModel.getRGBdefault();//ʵ����ɫֵ����
        
        int[] grayPixels = new int[width * height];
        double compare = 0.2, bright = 2;
        int red, green, blue, alpha;

      //�õ��Ҷ�ͼ������㷨
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {//ѭ��˳���б�������
                alpha = cm.getAlpha(pixels[j * width + i]);//͸����
                red = cm.getRed(pixels[j * width + i]);
                green = cm.getGreen(pixels[j * width + i]);
                blue = cm.getBlue(pixels[j * width + i]);

                grayPixels[j * width + i] = 2 * red - green - blue;//�õ�����ҶȻ�ֵ   ������߾�510 ����
                													//���ӿ϶���Ķ�


                if (grayPixels[j * width + i] < 0) {//���ֵС��0 ��0 ������
                    red = 0;
                    blue = 0;
                    green = 0;//�ú�
                    grayPixels[j * width + i] = 0;//���
                } else if (grayPixels[j * width + i] > 255) {//���ֵ����255 ��255 ������ 
                    red = 255;
                    green = 255;
                    blue = 255;//�ð�
                    grayPixels[j * width + i] = 255;//���
                } else {
                    red = grayPixels[j * width + i];
                    green = grayPixels[j * width + i];
                    blue = grayPixels[j * width + i];//��ɫһ��δ���(255)��С(0)�û�
                    //grayPixels[j * width + i]����ԭֵ
                }
                
                //�Ż��㷨
                grayPixels[j * width + i] = (int) (compare * grayPixels[j * width + i] + bright); //������߾�0-53֮�� ��ɫ
                									
                

                //������  չʾ�Ҷ�ͼ�õ�
                GrayPixelsTemp[j * width + i] = alpha << 24 | red << 16 | green << 8 | blue; 
                //								ֵ����24			����16		����8		
                //�����Ǹ�32λ�����ɫ                             ��3�ֽ�                  ��2�ֽ�             ��1�ֽ�                ��0�ֽ�
               //                               alpha         red         green        blue
                
            }
        }

//        for (int i = 0; i < width * height; i++) {//���ո�ֵ
//            grayPixels[i] = (int) (compare * grayPixels[i] + bright); //������߾�0-53֮��
//        }

        return grayPixels;//���ؼٻҶ�ͼ���� ֻΪ���������
    }

    // �õ�ͼƬ��ֵ������
    public  int[] getBinaryPixels(int[] grayPixels, int[] pixels,
            int width, int height) {
        int[] binaryPixels = copyPixels(grayPixels);
//        int[] grayPixelsCount = new int[256];
        int[] grayPixelsCount = new int[54];

        for (int i = 0; i < width * height; i++) {
            grayPixelsCount[grayPixels[i]]++;//0-53�ĸ���ɫ�о�+1
            
//            System.out.println(grayPixels[i]);
        }

        int threshold = getThreshold(grayPixelsCount);//�õ���ֵ

        ColorModel cm = ColorModel.getRGBdefault();//ʵ����ɫֵ����
        int red = 0, green = 0, blue = 0, alpha;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                alpha = cm.getAlpha(pixels[i]);
                if (grayPixels[j * width + i] > threshold) {//������ֵ���������ú� 
                    red = 0;
                    green = 0;
                    blue = 0;
                } else {        //С����ֵ�����ڣ��ð�         //���������ӱȽϰױ����ȽϺ� �������ӱ�ڶ�������� 
                    red = 255;
                    green = 255;
                    blue = 255;
                }

                binaryPixels[j * width + i] = alpha << 24 | red << 16| green << 8 | blue;//���ش����ֵͼ 
            }
        }
        return binaryPixels;
    }

  
    //������ֵ
    public  int getThreshold(int[] grayCountAry) {
    	/*
    	 * һ�ѳ�ʼ��
    	 */
        int Y, Amount = 0;
        int PixelBack = 0, PixelFore = 0, PixelIntegralBack = 0, PixelIntegralFore = 0, PixelIntegral = 0;
        double OmegaBack, OmegaFore, MicroBack, MicroFore, SigmaB, Sigma; // ��䷽��;
        int MinValue, MaxValue;
        int threshold = 0;

        
//        for (MinValue = 0; MinValue < 256 && grayCountAry[MinValue] == 0; MinValue++)
//			;
//        for (MaxValue = 255; MaxValue > MinValue && grayCountAry[MinValue] == 0; MaxValue--)
//			;
         //�Ż������������ 
        for (MinValue = 0; MinValue <54 && grayCountAry[MinValue] == 0; MinValue++)
        	;
        for (MaxValue = 53; MaxValue > MinValue && grayCountAry[MinValue] == 0; MaxValue--)
        	;
        if (MaxValue == MinValue) {
        	//�������ֻ��һ����ɫ
            return MaxValue; 
        }
        if (MinValue + 1 == MaxValue) {
        	
        	//���������ֻ��2����ɫ  ������� ������֮һ����ֵ���� 
            return MinValue; 
        }
        for (Y = MinValue; Y <= MaxValue; Y++) {
            Amount += grayCountAry[Y]; // ����������
        }
        
        PixelIntegral = 0;
        for (Y = MinValue; Y <= MaxValue; Y++) {
        	//Y����
        	//grayCountAry[Y] ��Ӧ�������м���
            PixelIntegral += grayCountAry[Y] * Y; //�����ش�С
        }
        
        
        SigmaB = -1;
        for (Y = MinValue; Y < MaxValue; Y++) {
            PixelBack = PixelBack + grayCountAry[Y];//Yǰ������(��Y����С)�ĸ���
            PixelFore = Amount - PixelBack;//Y�����صĸ���
            
            OmegaBack = (double) PixelBack / Amount;//Yǰ������ռ�����ظ����ı���
            OmegaFore = (double) PixelFore / Amount;//Y������ռ�����صı���
            
                                    
            PixelIntegralBack += grayCountAry[Y] * Y;//Y���ظ��� *�����ش�С=�������ܴ�С
            PixelIntegralFore = PixelIntegral - PixelIntegralBack;//Y�������ܴ�С
            
            MicroBack = (double) PixelIntegralBack / PixelBack;//��������ܴ�С/������ǰ���ظ���=ǰ������ƽ����С
            MicroFore = (double) PixelIntegralFore / PixelFore;//ʣ�������ܴ�С/ʣ�����ظ���=Y������ƽ����С
            
            													
            Sigma = OmegaBack * OmegaFore * (MicroBack - MicroFore)* (MicroBack - MicroFore);
            //      Yǰ����ռ�����ظ����ı���*Y������ռ�����صı���*��Yǰ����ƽ����С-Y������ƽ����С����ƽ��
            //      �����Y���ٽ����ص�  Yǰ����ռ�����ظ����ı�����ﵽ��� Y������ռ�����صı���Ҳ��ﵽ���
            //      Yǰ����ƽ����С-Y������ƽ����С �ľ���ֵҲ�����  ����ƽ������   ���� Sigma��ȫͼ��� 
            //		�ʶ�Sigma���ʱ�����YΪ�ٽ����ص��ʱ��
            
            if (Sigma > SigmaB) {
                SigmaB = Sigma;
                threshold = Y;//����Sigma��Ӧ��Y�����ٽ����ص�
            }
        }
        return threshold;
    }

    // ���������Ǻ������
    public  int[] getTagPixels(int[] tagPixels, int width, int height, int[][] pixelsTemp) {
    	/*
    	 * һ�ѳ�ʼ��   
    	 */
        int[][] m = new int[width][height];
        int i, j, number = 0, maxHZ = 0;
        int[] HZofNumber = new int[10000];//HZ->Ƶ�� ��Ӧĳ�����ֳ��ֵ�Ƶ��
        LinkedList<Seed> p = new LinkedList<Seed>();//��ʼ��ǰ������
        ColorModel cm = ColorModel.getRGBdefault();//ʵ����ɫֵ����
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
        
        //��ǿ�ʼ
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                if (cm.getRGB(tagPixels[j * width + i]) == -16777216//��ɫ
                        && m[i][j] == 0) { // ��ɫ�Ǻ�ɫ����û�����
                    number++;
                    Seed point = new Seed(i, j);
                    p.add(point); // ���ҵ�û��ǵ�ǰ��ɫ��,�������
                    m[i][j] = number; // �����ĳ������ ������>0��
                    HZofNumber[number]++;
                    while (!p.isEmpty()) {//�ټ���
                        Seed q;
                        q = p.pollLast();//��ȡ���Ƴ�����������һ��Ԫ�� 
                        for (int k = 0; k < 8; k++) {//  8�������� 
                            int a = q.x + direction[k][0];
                            int b = q.y + direction[k][1];//�ҳ���ǰ����+����Щֵ�õ���Χ8��λ�õĵ�
                            if (a < width && a >= 0 && b < height && b >= 0) {//��ͼ�ķ�Χ��
                                if (cm.getRGB(tagPixels[b * width + a]) == -16777216
                                        && m[a][b] == 0) {//8����ĳ��Ҳ�Ǻ�ɫ����û����� �ͱ���ϴ���ͬһ���������
                                    Seed tempPoint = new Seed(a, b);
                                    p.add(tempPoint); // ���û��ǵ�ǰ���� �������whileѭ��
                                    m[a][b] = number; // ͼ�ϸõ�����ĳ������
                                    HZofNumber[number]++;//��Ӧĳ�����ֳ��ֵĴ���
                                    
                                    //ͼ��ĳ��λ�÷���ĳ������ ��ӦHZofNumber[number]����������ֳ��ֵĴ��� 
                                    //�����Χ��ɫ��ͨ����ܴ�� ������ֳ��ֻ�ܶ��
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
		// ��ǽ���

        // ȥ���ȷ�ֵС������  һЩ��ͨ����Ƚ�С���������ӵ�������ȥ��
        int threshold = (int) (maxHZ / 3);// �����/3���ٽ��
        int red, green, blue, alpha;

        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                alpha = cm.getAlpha(tagPixels[j * width + i]);
                if (HZofNumber[m[i][j]] < threshold) {//ͼ��������Ӧ�����ֳ��ֵ��������С���ٽ�� �ð�ȥ��
                    red = 255;
                    green = 255;
                    blue = 255;
                    tagPixels[j * width + i] = alpha << 24 | red << 16| green << 8 | blue;
                }
            }
        }
        
        pixelsTemp = m;//����ں��� �������������(ͬɫͬ����)����Ե�����  
        
        return tagPixels;

    }

    // ������ȡ���ı�Ե������ Robert����
    public  int[] getEdgePixels(int[] pixels, int width, int height) {

//        BufferedImage grayImage = new BufferedImage(width, height,
//                BufferedImage.TYPE_INT_RGB);
        // ��ͼ����б�Ե��ȡ  
        //Alphaֵ���ֲ���
        ColorModel cm = ColorModel.getRGBdefault();
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                // ��ͼ����б�Ե��ȡ 
            	/**
            	 * 3��������� �ڰױ�Ե��õ�����ֵ
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
                if ((255 - cm.getBlue(pixels[i * width + j])) < 30) {//�ױ�(��255���)�ú�
                    robertRed = 0;
                    robertGreen = 0;
                    robertBlue = 0;
                } else {//�����������ȫ��ɫ�Ļ�����ȫ��ɫ�� ��255һ��һ��ܴ� �ð�
                    robertRed = 255;
                    robertGreen = 255;
                    robertBlue = 255;
                }
                pixels[i * width + j] = alpha << 24 | robertRed << 16 | robertGreen << 8 | robertBlue; //���û�����
//                grayImage.setRGB(j, i, pixels[i * width + j]);
            }
        }

        return pixels;

    }

   
  

    //�Զ�ֵͼ����м��봦��ͼ����̬ѧ�еĸ�ʴ�������ܺܺõĽ��������
    // ��Ե��⴦��  ���ظ�ʴ�������
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

    // ��������֮�������
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

    // ��Ե���
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

   
	
    // ͼƬ��
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

	
    // ͼƬ��ƽ���͹���
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

    // �����������������
//    public  int[] getfillPixels(int[] fillPixels, int width, int height,
//            int[][] m) {
//
//        int alpha;
//        int n;// ɨ�赽�ڼ���
//        ColorModel cm = ColorModel.getRGBdefault();
//        
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                n = j;
//                if (cm.getBlue(fillPixels[j * width + i]) == 0) {//����Ǻ�ɫ
//                    // �Ӻ��濪ʼ������
//                    for (int k = height - 1; k > j; k--) {
//                        if (cm.getBlue(fillPixels[k * width + i]) == 0
//                                && m[i][j] == m[i][k]) {//ͬɫͬ����
//                            for (; n <= k; n++) {//һ�����
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
    
    // �����������������
    public  int[] getfillPixels(int[] fillPixels, int width, int height,
    		int[][] m) {
    	
    	int alpha;
    	int n;// ɨ�赽�ڼ���
    	
    	ColorModel cm = ColorModel.getRGBdefault();
    	
    	for (int i = 0; i < width; i++) {
    		for (int j = 0; j < height; j++) {
    			n = j;
    			if (cm.getBlue(fillPixels[j * width + i]) == 0) {//����Ǻ�ɫ
    				// �Ӻ��濪ʼ������
    				for (int k = height - 1; k > j; k--) {
    					if (cm.getBlue(fillPixels[k * width + i]) == 0
    							&& m[i][j] == m[i][k]) {//ͬɫͬ����
    						for (; n <= k; n++) {//һ�����
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

    // �Ľ���Hough�շ�任Բ����㷨 
    public  LinkedList<Orange> hough(int[] edgePixels, int width, int height) {

        LinkedList<Seed> edgePointList = new LinkedList<>(); //��Ե���ͼ��  �����˱߽�������

        LinkedList<Orange> OrangeList = new LinkedList<>();  //��⵽�����Ӹ�������

        Seed point1, point2, point3, pos;//������  �����õ�
        
        int[] houghPiexls = copyPixels(edgePixels);
        
        double r; // r������Բ�İ뾶
        
        double mat1, mat2, mat3; //����Բ�Ĳ��� ��������ϱ�ʾ��Բ�� �뾶 
        
        int  Threshold ; //��ֵ ������ĵ��������ֵ���޷�����Բ
        
        int count = 0; // �������������ĵ�ĸ������㵽Բ�ĵľ��� ���뾶��ֵС��һ��ֵ����Ϊ���㣩
        
        Seed circle = new Seed(); //���Դ��ܳ���ȡ����ȷ��Բ�İ뾶��Բ�� Seed��a b���ܱ�ʾ��������ʾ�� 
        
        double rMax, rMin;//���뾶����С�뾶  ��Ҫȷ����Ե��������ȷ�� ��һ�������һ��int���� �൱��1��
       
        ColorModel cm = ColorModel.getRGBdefault();//ʵ����ɫֵ����
        
        //-----------------------��ʼ�ұ�Ե�� �ҵ������Ե������----------
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                if (cm.getBlue(houghPiexls[j * width + i]) == 0) {//��ȡ����ɫ��
                    if (j == height - 1) {// ����ͼƬĩβ û���ж��������ǲ��ǰ�ɫ����  ���������ֱ�Ӿ��Ǳ߽����
                        pos = new Seed(i, j);
                        edgePointList.add(pos);
                        
                    } else if (cm.getBlue(houghPiexls[j * width + i + 1]) != 0) {//�����治�Ǻ�ɫ�ĵ��� �Ǿ�ֻ�а�ɫ��  ����������Ǳ߽��
                        pos = new Seed(i, j);
                        edgePointList.add(pos);
                       
                    }
                }
            }
        }
      //------------------------------Ѱ�ҽ���-------------------------
        
        
        //�����ܳ� �Ϳ���ȷ��������뾶 ��С�뾶 �����ٶ��ٸ���������Բ��
        rMin = ((edgePointList.size() /3) / 3.14) / 2;//��С�뾶
        
        rMax = (edgePointList.size() / 3.14) / 2;//���뾶
        
        Threshold = (int) edgePointList.size() / 10; // ����ֵ����������ĵ�����m2ʱ��Ϊʣ�µĵ㲻���Թ���һ��Բ������ֹͣ���
        
        int failCount = 0;//һ��С����
       
        //��ʼ���
        while (edgePointList.size() > Threshold && failCount < rMax *4) {
            //  ���ȡ�������������õ�Բ�İ뾶
        	
            point1 = edgePointList.get((int) (Math.random() * edgePointList.size()));
            point2 = edgePointList.get((int) (Math.random() * edgePointList.size()));
            point3 = edgePointList.get((int) (Math.random() * edgePointList.size())); // ���ȡ��3����
            
            //ȡ���ĵ���ܹ��� ���ߵ��������
            if (point1.y == point2.y || point2.y == point3.y) {//Y����    ���¿�ʼȡ��
                continue;
            }
            double h1 = (point2.x - point1.x) / (point2.y - point1.y);
            double h2 = (point3.x - point2.x) / (point3.y - point2.y);
            if (h1 == h2) {//б����ͬ=����  ���¿�ʼȡ��
                continue;
            }

            
            // ��Բ�Ĳ��� mat1 mat2 mat3 Բ��λ�ø��뾶��������������ʾ�õ�
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
            
			// Բ��λ�úͰ뾶��С
            // circleΪ������������Ĵ����Բ ���᲻��С����С�뾶 �Ͼ�����ȡ�õ�������̫��
            circle.x = (int) (mat1 / mat3);
            circle.y = (int) (mat2 / mat3);
            r = Math.sqrt((double) ((point1.x - circle.x) * (point1.x - circle.x) + (point1.y - circle.y)
                    * (point1.y - circle.y))); // 3��ȷ��Բ�ĺͰ뾶  

            
            // ����뾶С����С�뾶����������ȡ�ò��� ������Ҫ����������ͨ��Բ�ģ� ����while��ȡ 
            if (r < rMin) {
            	failCount++;
                continue;
            }
            
     

            double C =  3.14 * r ;//�ܳ�
           
//            LinkedList<Seed> edgePointList1 = new LinkedList<Seed>();
            
            for (int i = 0; i < edgePointList.size(); i++) { // ���б�ԵԪ�ض��Ա�һ��
                pos = edgePointList.get(i); // �ҳ���ȷ����Բ������������ڵ����е㣬��Ϊ���Ƕ���ͬһ��Բ�ϵĵ�

                if (pos.x >= (circle.x - r) && pos.x <= (circle.x + r)
                        && pos.y <= (circle.y + r) && pos.y >= (circle.y - r)) {//����������Բ����Ӿ�����
                	
       
                	
                    if (Math.abs(Math.sqrt((double) ((pos.x - circle.x)
                            * (pos.x - circle.x) + (pos.y - circle.y) * (pos.y - circle.y)))
                            - r) < 8) {//��Ե�㵽Բ���ľ���ľ���ֵ����С��8������Բ�ϵĵ��� count1++
                        count++;
                    }

                }
            }
            if (count >= C) { // �������Բ����Բ,�Ǳ�ԵԪ��Ӧ�ô����ܳ�    ��Ѹ�Բ����Բ�Ķ��� 
            
            	
                Orange orange = new Orange();
                orange.a = circle.x;
                orange.b = circle.y;
                orange.r = (int) r;
                OrangeList.add(orange);
                for (int i = 0; i < edgePointList.size(); i++) { // ����Ԫ�ض��Աȴ�
                    pos = edgePointList.get(i);

                    if (Math.abs(Math.sqrt((double) ((pos.x - circle.x)
                            * (pos.x - circle.x) + (pos.y - circle.y) * (pos.y - circle.y)))
                            - r) < 5) {//��Ե�㵽���Բ���ľ���ľ���ֵ����С��5���������Բ�ϵĵ��� 
                                       // ���Բ�Ѿ�ȷ�� ���Բ���Ҫ��Щ��Ե�������Ƴ������
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
    
    
    //Ȧ�����ӵķ���
    public BufferedImage draw(BufferedImage wbufferedImage_For_CreateGraphics ,LinkedList<Orange> orange  ,List<Spot> comparison,int TYPE){
    	
    	
    	if(TYPE==1){
    	//Ȧ������������
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
    		//Ȧ������������
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

////      Hough�շ�任
//    public  LinkedList<Orange> getHoughPixels( int[] edgePixels,
//            int width, int height) {
//
//        int[] houghPixels = copyPixels(edgePixels);
//
//        // ��Ե����
//        LinkedList<Seed> v = new LinkedList<>();
//        // Բ�Ķ���
//        LinkedList<Orange> p = new LinkedList<>();
//
//        ColorModel cm = ColorModel.getRGBdefault();
//
//        // ���Ե�
//        Seed pos;
//
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//				// ���в���
//                // �����ص�Ϊ��Ե��  
//            	//һ�����ؿ�ȵĵ�
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
//        // ���������ֱ�Ϊ��С�뾶�����뾶
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
//            // ���ߵĵ㲻��;
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
//            // ��Բ�Ĳ���
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
//            // Բ��λ�úͰ뾶��С
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
//        // Բ�Ķ���
//        LinkedList<Orange> pc = new LinkedList<>();
//      //���뾶�����Բ�������Բ��
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
//        // ͳ��Բ�������Բ����
//        LinkedList<Orange> pt = new LinkedList<>();
//        for (int i = 0; i < pc.size(); i++) {
//
//            double ca = pc.get(i).a, cb = pc.get(i).b, cr = pc.get(i).r;
//            // ������ƽ��ֵ
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
