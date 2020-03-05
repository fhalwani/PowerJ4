package ca.powerj;
import java.util.ArrayList;

class OAnnualList {
	short  id   = 0;
	String name = "";
	int[] cases;
	int[] specs;
	int[] blocks;
	int[] slides;
	int[] he;
	int[] ss;
	int[] ihc;
	int[] synopt;
	int[] frozen;
	double[] fte1;
	double[] fte2;
	double[] fte3;
	double[] fte4;
	double[] fte5;
	ArrayList<OAnnualList> children = new ArrayList<OAnnualList>();

	OAnnualList(String name, byte noYears, short id) {
		this.id     = id;
		this.name   = name;
		this.cases  = new int[noYears];
		this.specs  = new int[noYears];
		this.blocks = new int[noYears];
		this.slides = new int[noYears];
		this.he     = new int[noYears];
		this.ss     = new int[noYears];
		this.ihc    = new int[noYears];
		this.synopt = new int[noYears];
		this.frozen = new int[noYears];
		this.fte5   = new double[noYears];
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