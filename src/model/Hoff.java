package model;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Hoff {

    private static BufferedImage imageTemp = null;

    private static int height;
    private static int width;
    private static int minR = 0;
    private static int maxR = 0;
    private static int[][] imageRGB;
    private static List<Point> lineList;
    private static List<model.Spot> circleCenter;

    public static List<model.Spot> startAnlyze(BufferedImage image) {
        height = image.getHeight() / 4;
        width = image.getWidth() / 4;
        imageRGB = new int[width][height];
        imageTemp = image;
        doZoom();//��ͼ��תΪBufferedImage���� ������ͷ����� ���������û��ʻ���Ȧ������
        getImageRGB(imageTemp); //�õ�ֻ�������������ص� 
        doBinaryzation(); //�õ���ֵ�� ͼ���ֵ��
        doEdulcoration(); //��ֵ���� �õ��׵�������ͨ����
        doFilling();//��ֵ���� �õ���ɫ��������ͨ����
        doOutLine();//�Ӷ��õ���Ե���ص����� ���˱�Ե�������ط����ú�
        circleCenter = hough(lineList, width, height, minR, maxR, sreachH());//�շ�任Բ���
        return circleCenter;//����Բ����
    }

    //�õ�ֻ�������������ص�
    private static void getImageRGB(BufferedImage image) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imageRGB[i][j] = image.getRGB(i, j) & 0xFFFFFF; //ÿ�����ظ�24λ1��һ�� ȥ����8λ��͸����
            }
        }
    }

    //ͼ���ֵ��
    private static void doBinaryzation() {
        int t = getT(imageRGB, width, height);//�õ���ֵ ������������Щ�ú� ��Щ�ðף�������Щ�Ǳ��� ��Щ�����ӣ�
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r = (imageRGB[i][j] & 0xFF0000) >> 16;//�õ���ɫ����ֵ
                int g = (imageRGB[i][j] & 0x00FF00) >> 8;//�õ���ɫ����ֵ
                int b = (imageRGB[i][j] & 0x0000FF);//�õ���ɫ����ֵ
                r = (int) ((double) r - (double) g * 0.68 - (double) b * 0.32);//���ӿ϶���ɫ�� ����������>0
                if (r < 0) {
                    r = 0;
                }
                if (r > t) {//������ֵ
                    r = g = b = 255; //�ð�
                } else {//С����ֵ
                    r = g = b = 0; //�ú�
                }
                imageRGB[i][j] = r; //��ֵ��¼��������Ӧλ��
                imageTemp.setRGB(i, j, (r << 16) | (g << 8) | b);//���������
            }
        }
    }
//�����ֵ ������������Щ�ú� ��Щ�ðף�������Щ�Ǳ��� ��Щ�����ӣ�
    private static int getT(int[][] RGB, int width, int height) {
        int ot = 127;
        int et = 0;
        int temp[] = new int[width * height];
        List<Integer> sub1 = new ArrayList<Integer>();
        List<Integer> sub2 = new ArrayList<Integer>();
        int means1 = 0;
        int means2 = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                temp[i] = (RGB[i][j] & 0xFF0000) >> 16;//��ú�ɫ����ֵ
            }
        }
        while (et != ot) {
            et = ot;
            for (int i = 0; i < temp.length; i++) {
                if (temp[i] <= ot) {//ÿ����ɫ���ص��ֵ��127��� 
                    sub1.add(temp[i]);
                } else {
                    sub2.add(temp[i]);
                }
            }
            means1 = getMeans(sub1);//�õ�ÿ�������ƽ��ֵ
            means2 = getMeans(sub2);
            sub1.clear();
            sub2.clear();
            ot = (means1 + means2) / 2;//��ѭ���� ����������ƽ��ֵ��Ӻ�/2������һ�ε�ֵʱ ��������Ѿ�����ֵ��
//        ||-----||��ʾ���������            
//            ||------|| ��ֵ ||-------||
//              ƽ��ֵ	  +     ƽ��ֵ
//            ���϶��и�=��ֵ
        }
        return et;//
    }

    //�õ������ƽ��ֵ
    private static int getMeans(List<Integer> data) {
        int result = 0;
        int size = data.size();
        for (int i = 0; i < data.size(); i++) {
            result += data.get(i);
        }
        if (size == 0) {
            size = 1;//��ó�0����
        }
        return (result / size);
    }

  //�õ��׵�������ͨ��������ɫ�����������ú�
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void doEdulcoration() {
        List<List> sqList = new ArrayList<List>();
        sqList = (List<List>) regionalizationn(255);//�õ��׵�������ͨ����
        for (int i = 0; i < sqList.size(); i++) {
            List<Point> l = (List<Point>) (sqList.get(i));
            if (l.size() < ((width / 8) * (height / 8))) {//�õ�������������ͨ����
                for (int j = 0; j < l.size(); j++) {
                    imageRGB[((Point) (l.get(j))).x][((Point) (l.get(j))).y] = 0;//��� ��ֵ�ϵļ��ú�
                    imageTemp.setRGB(((Point) (l.get(j))).x,
                            ((Point) (l.get(j))).y, (0 << 16) | (0 << 8) | 0);//�����ú�

                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void doFilling() {
        List<List> sqList = new ArrayList<List>();
        sqList = (List<List>) regionalizationn(0);//�õ��ڵ�����
        for (int i = 0; i < sqList.size(); i++) {
            List<Point> l = (List<Point>) (sqList.get(i));
            if (l.size() < ((width / 4) * (height / 4))) {//�õ�������������ͨ����
                for (int j = 0; j < l.size(); j++) {
                    imageRGB[((Point) (l.get(j))).x][((Point) (l.get(j))).y] = 255;//��� ��ֵ�ϵļ��ð�
                    imageTemp.setRGB(((Point) (l.get(j))).x,
                            ((Point) (l.get(j))).y, (255 << 16) | (255 << 8)| 255);//�ð�
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static List<List> regionalizationn(int flag) {
        List<List> sqList = new ArrayList<List>();
        List<Point> ptList = null;
        Queue<Point> pointQ = new LinkedList<Point>();
        int[][] a = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                a[i][j] = 0;
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (imageRGB[i][j] == flag && a[i][j] == 0) {
                    a[i][j] = 1;
                    ptList = new ArrayList<Point>();
                    pointQ.offer(new Point(i, j));
                    while (!pointQ.isEmpty()) {
                        Point p = pointQ.poll();
                        ptList.add(p);//�������ӽ����� 
                        for (int k = -1; k < 2; k++) {//8��ͨ�����Ⲣ��� ��ʵ��9�� �м��Ǹ���a����ı��������
                            for (int l = -1; l < 2; l++) {
                                if (p.x + k >= 0 && p.x + k < width//�м�ĵ��x����k��û�г���ͼ��
                                        && p.y + l >= 0 && p.y + l < height//�м�ĵ��y����k��û�г���ͼ��
                                        && imageRGB[p.x + k][p.y + l] == flag//���Ҹõ����ֵ�������涨��ֵ
                                        && a[p.x + k][p.y + l] == 0) {//���������û�б���ǹ� �ܹ������м��ǵ�
                                    a[p.x + k][p.y + l] = 1;//�����
                                    pointQ.offer(new Point(p.x + k, p.y + l));//��Ӳ�������һ��ѭ�� һֱ����ͨ�Ķ���ǹ�
                                }
                            }
                        }
                    }
                    sqList.add(ptList);//����������ӽ��������
                }
            }
        }
        return sqList;
    }

    private static void doOutLine() {
        List<Point> markList = new ArrayList<Point>();
        markList = inLineMark();//�õ���ɫ������ͨ�������� �������������¶��ǰ׵�����
        sreachR();
        for (int i = 0; i < markList.size(); i++) {
            Point p = (Point) (markList.get(i));
            imageRGB[p.x][p.y] = 0;
            imageTemp.setRGB(p.x, p.y, (0 << 16) | (0 << 8) | 0);//���õ��İ�ɫ���� �൱�ڱ����ú�
        }
        for (int i = lineList.size() - 1; i >= 0; i--) {
            int x = lineList.get(i).x, y = lineList.get(i).y;
            if (imageRGB[x][y] == 255) {
                int flag = 0;
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {//8������
                        if (x + k >= 0 && x + k < width && y + l >= 0
                                && y + l < height) {
                            if (imageRGB[x + k][y + l] == 255) {//��Χ�а׵�flag++
                                flag++;
                            }
                        }
                    }
                }
                if (flag > 2) {
                    imageRGB[x][y] = 0;
                    imageTemp.setRGB(x, y, (0 << 16) | (0 << 8) | 0);//����ǰ׵ĳ���3�μ����� ˵�������Լ� �ܱ���ͨ��������һ���׵� ˵�����Ǳ�Ե�� �ú�
                    lineList.remove(i);//�Ƴ������
                }

            }
        }
    }

    //�õ���ɫ������ͨ�������� �������������¶��ǰ׵�����
    private static List<Point> inLineMark() {
        List<Point> markList = new ArrayList<Point>();
        lineList = new ArrayList<Point>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (imageRGB[i][j] == 255) {
                    if (i - 1 >= 0 && i + 1 < width && j - 1 >= 0
                            && j + 1 < height) {
                        if (imageRGB[i - 1][j] == 255
                                && imageRGB[i + 1][j] == 255
                                && imageRGB[i][j - 1] == 255
                                && imageRGB[i][j + 1] == 255) {
                            markList.add(new Point(i, j));
                        } else {
                            lineList.add(new Point(i, j));
                        }
                    }
                }
            }
        }
        return markList;
    }

    //�����ǵ�ͼ��תΪBufferedImage����
    private static void doZoom() {
        Image img = imageTemp.getScaledInstance(width, height, Image.SCALE_DEFAULT);//����ͼ��
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//����һ����ͼ��һ����С��BufferedImage
        Graphics2D g = image.createGraphics();
        g.drawImage(img, 0, 0, null);//�û�����BufferedImage��ͼ�������ǵ�ͼ��
        g.dispose();
        imageTemp = image;
    }


    private static void sreachR() {
        int k = 0;
        int i = 1;
        if (lineList.isEmpty()) {
			return;
		}
        Point p = lineList.get(0);
        for (i = 1; i < lineList.size(); i++) {
            if (lineList.get(i).y == p.y) {
                k = lineList.get(i).x - p.x;
                if (k > width / 8) {
                    break;
                }
            }
        }
        minR = k / 3;//��С��������K/3
        maxR = (int) (k);
    }

    private static int sreachH() {
        return width / 6 + height / 6 - width / 100;
    }
    
    //�շ�Բ����㷨
    public static List<model.Spot> hough(List<Point> lineList, int width,int height, int Rmin, int Rmax, int H) {
		List<model.Spot> pl = new ArrayList<model.Spot>();
		List<model.Spot> cl = new ArrayList<model.Spot>();
		int r;
		int[][] acc = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				acc[i][j] = 0;
			}
		}
		for (r = Rmin; r <= Rmax; r++) { 
			int x0, y0;
			double t;
			for (int i = 0; i < lineList.size(); i++) {
				Point p = lineList.get(i);
				for (int theta = 0; theta < 360; theta++) {
					t = (theta * 3.14159265) / 180; 
					x0 = (int) Math.round(p.x - r * Math.cos(t));
					y0 = (int) Math.round(p.y - r * Math.sin(t));
					if (x0 < width && x0 >= 0 && y0 < height && y0 >= 0) {
						acc[x0][y0] += 1;
					}
				}
			}

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (acc[x][y] > H)
						pl.add(new model.Spot(x, y, r, acc[x][y]));
					acc[x][y] = 0;
				}
			}
		}
		while(!pl.isEmpty()){
			model.Spot c = pl.get(pl.size()-1);
			int q = pl.size()-1;
			for(int i = pl.size()-2;i>=0;i--){
				if (Point.distance(c.x,c.y,pl.get(i).x, pl.get(i).y) < Rmin){
					if(c.num > pl.get(i).num){
						pl.remove(i);
						q--;
					}
					else{
						c = pl.get(i);
						pl.remove(q);
						q = i;
					}
				}
			}
			cl.add(pl.get(q));
			pl.remove(q);
		}
		return cl;
	}
}
