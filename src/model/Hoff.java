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
        doZoom();//将图像转为BufferedImage类型 这个类型方法多 它可以利用画笔画上圈出橘子
        getImageRGB(imageTemp); //得到只含红绿蓝的像素点 
        doBinaryzation(); //得到阈值并 图像二值化
        doEdulcoration(); //二值化后 得到白的区域联通链表
        doFilling();//二值化后 得到黑色的区域联通链表
        doOutLine();//从而得到边缘像素点链表 除了边缘点其他地方都置黑
        circleCenter = hough(lineList, width, height, minR, maxR, sreachH());//赫夫变换圆检测
        return circleCenter;//返回圆链表
    }

    //得到只含红绿蓝的像素点
    private static void getImageRGB(BufferedImage image) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imageRGB[i][j] = image.getRGB(i, j) & 0xFFFFFF; //每个像素跟24位1与一遍 去除高8位的透明度
            }
        }
    }

    //图像二值化
    private static void doBinaryzation() {
        int t = getT(imageRGB, width, height);//得到阈值 用它来区分哪些置黑 哪些置白（区分哪些是背景 哪些是橘子）
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r = (imageRGB[i][j] & 0xFF0000) >> 16;//得到红色像素值
                int g = (imageRGB[i][j] & 0x00FF00) >> 8;//得到绿色像素值
                int b = (imageRGB[i][j] & 0x0000FF);//得到蓝色像素值
                r = (int) ((double) r - (double) g * 0.68 - (double) b * 0.32);//橘子肯定红色多 减出来会是>0
                if (r < 0) {
                    r = 0;
                }
                if (r > t) {//大于阈值
                    r = g = b = 255; //置白
                } else {//小于阈值
                    r = g = b = 0; //置黑
                }
                imageRGB[i][j] = r; //数值记录回数组相应位置
                imageTemp.setRGB(i, j, (r << 16) | (g << 8) | b);//再重新设回
            }
        }
    }
//获得阈值 用它来区分哪些置黑 哪些置白（区分哪些是背景 哪些是橘子）
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
                temp[i] = (RGB[i][j] & 0xFF0000) >> 16;//获得红色像素值
            }
        }
        while (et != ot) {
            et = ot;
            for (int i = 0; i < temp.length; i++) {
                if (temp[i] <= ot) {//每个红色像素点的值跟127相比 
                    sub1.add(temp[i]);
                } else {
                    sub2.add(temp[i]);
                }
            }
            means1 = getMeans(sub1);//得到每个链表的平均值
            means2 = getMeans(sub2);
            sub1.clear();
            sub2.clear();
            ot = (means1 + means2) / 2;//在循环中 当两个链表平均值相加后/2等于上一次的值时 代表这点已经是阈值了
//        ||-----||表示链表里的数            
//            ||------|| 阈值 ||-------||
//              平均值	  +     平均值
//            最后肯定有个=阈值
        }
        return et;//
    }

    //得到链表的平均值
    private static int getMeans(List<Integer> data) {
        int result = 0;
        int size = data.size();
        for (int i = 0; i < data.size(); i++) {
            result += data.get(i);
        }
        if (size == 0) {
            size = 1;//免得除0错误
        }
        return (result / size);
    }

  //得到白的区域联通链表并将白色的橘子区域置黑
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void doEdulcoration() {
        List<List> sqList = new ArrayList<List>();
        sqList = (List<List>) regionalizationn(255);//得到白的区域联通链表
        for (int i = 0; i < sqList.size(); i++) {
            List<Point> l = (List<Point>) (sqList.get(i));
            if (l.size() < ((width / 8) * (height / 8))) {//得到符合条件的联通区域
                for (int j = 0; j < l.size(); j++) {
                    imageRGB[((Point) (l.get(j))).x][((Point) (l.get(j))).y] = 0;//标记 数值上的假置黑
                    imageTemp.setRGB(((Point) (l.get(j))).x,
                            ((Point) (l.get(j))).y, (0 << 16) | (0 << 8) | 0);//像素置黑

                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void doFilling() {
        List<List> sqList = new ArrayList<List>();
        sqList = (List<List>) regionalizationn(0);//得到黑的区域
        for (int i = 0; i < sqList.size(); i++) {
            List<Point> l = (List<Point>) (sqList.get(i));
            if (l.size() < ((width / 4) * (height / 4))) {//得到符合条件的联通区域
                for (int j = 0; j < l.size(); j++) {
                    imageRGB[((Point) (l.get(j))).x][((Point) (l.get(j))).y] = 255;//标记 数值上的假置白
                    imageTemp.setRGB(((Point) (l.get(j))).x,
                            ((Point) (l.get(j))).y, (255 << 16) | (255 << 8)| 255);//置白
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
                        ptList.add(p);//这个点添加进链表 
                        for (int k = -1; k < 2; k++) {//8联通区域检测并标记 其实是9个 中间那个靠a数组的标记舍弃掉
                            for (int l = -1; l < 2; l++) {
                                if (p.x + k >= 0 && p.x + k < width//中间的点的x加上k后没有超过图像
                                        && p.y + l >= 0 && p.y + l < height//中间的点的y加上k后没有超过图像
                                        && imageRGB[p.x + k][p.y + l] == flag//并且该点的数值等于所规定数值
                                        && a[p.x + k][p.y + l] == 0) {//而且这个点没有被标记过 能够区分中间那点
                                    a[p.x + k][p.y + l] = 1;//标记上
                                    pointQ.offer(new Point(p.x + k, p.y + l));//添加并进入下一次循环 一直到联通的都标记过
                                }
                            }
                        }
                    }
                    sqList.add(ptList);//链表链表添加进这个链表
                }
            }
        }
        return sqList;
    }

    private static void doOutLine() {
        List<Point> markList = new ArrayList<Point>();
        markList = inLineMark();//得到白色区域联通像素链表 而且是左右上下都是白的区域
        sreachR();
        for (int i = 0; i < markList.size(); i++) {
            Point p = (Point) (markList.get(i));
            imageRGB[p.x][p.y] = 0;
            imageTemp.setRGB(p.x, p.y, (0 << 16) | (0 << 8) | 0);//将得到的白色区域 相当于背景置黑
        }
        for (int i = lineList.size() - 1; i >= 0; i--) {
            int x = lineList.get(i).x, y = lineList.get(i).y;
            if (imageRGB[x][y] == 255) {
                int flag = 0;
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {//8区域检测
                        if (x + k >= 0 && x + k < width && y + l >= 0
                                && y + l < height) {
                            if (imageRGB[x + k][y + l] == 255) {//周围有白的flag++
                                flag++;
                            }
                        }
                    }
                }
                if (flag > 2) {
                    imageRGB[x][y] = 0;
                    imageTemp.setRGB(x, y, (0 << 16) | (0 << 8) | 0);//如果是白的出现3次及以上 说明除了自己 周边联通区还有另一个白点 说明不是边缘点 置黑
                    lineList.remove(i);//移除这个点
                }

            }
        }
    }

    //得到白色区域联通像素链表 而且是左右上下都是白的区域
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

    //将我们的图像转为BufferedImage类型
    private static void doZoom() {
        Image img = imageTemp.getScaledInstance(width, height, Image.SCALE_DEFAULT);//创建图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//创建一个跟图像一样大小的BufferedImage
        Graphics2D g = image.createGraphics();
        g.drawImage(img, 0, 0, null);//用画笔在BufferedImage的图像画上我们的图像
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
        minR = k / 3;//最小的是最大的K/3
        maxR = (int) (k);
    }

    private static int sreachH() {
        return width / 6 + height / 6 - width / 100;
    }
    
    //赫夫圆检测算法
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
