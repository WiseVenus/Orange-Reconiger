package model;

public class Seed {//������������û����ǵ�����λ�� ����������Բ���ҳ�Բ��

	
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
