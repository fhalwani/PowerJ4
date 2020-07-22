package ca.powerj;
import java.util.ArrayList;

class OAnnualList {
	short  id   = 0;
	String name = "";
	int[] specs;
	int[] blocks;
	int[] slides;
	double[] fte1;
	double[] fte2;
	double[] fte3;
	double[] fte4;
	double[] fte5;
	ArrayList<OAnnualList> children = new ArrayList<OAnnualList>();

	OAnnualList(String name, byte noYears, short id) {
		this.id     = id;
		this.name   = name;
		this.specs  = new int[noYears];
		this.blocks = new int[noYears];
		this.slides = new int[noYears];
		this.fte1   = new double[noYears];
		this.fte2   = new double[noYears];
		this.fte3   = new double[noYears];
		this.fte4   = new double[noYears];
		this.fte5   = new double[noYears];
	}

	@Override
	public String toString() {
		return this.name;
	}
}