package model;

public class Spot {//Hoff找出到的圆心

	public int num = 0;
	public int x;
	public int y;
	public int r = 0;

	Spot(int x, int y, int r, int num) {
		this.num = num;
		this.x = x;
		this.y = y;
		this.r = r;
	}

	Spot(int x, int y) {
		this.x = x;
		this.y = y;
	}

	Spot() {
		this.x = 0;
		this.y = 0;
	}




}
