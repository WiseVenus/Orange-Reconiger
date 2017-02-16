package model;

public class Seed {//保存区域标记中没被标记的像素位置 方便后续检测圆并找出圆心

	
	public int num = 0;
	public int x;
	public int y;
	public int r = 0;

	Seed(int x, int y, int r, int num) {
		this.num = num;
		this.x = x;
		this.y = y;
		this.r = r;
	}

	Seed(int x, int y) {
		this.x = x;
		this.y = y;
	}

	Seed() {
		this.x = 0;
		this.y = 0;
	}




}
