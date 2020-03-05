package ca.powerj;

class OAnnualNode {
	String   name = "";
	int[]    cases;
	int[]    specs;
	int[]    blocks;
	int[]    slides;
	int[]    he;
	int[]    ss;
	int[]    ihc;
	int[]    synopt;
	int[]    frozen;
	double[] fte1;
	double[] fte2;
	double[] fte3;
	double[] fte4;
	double[] fte5;
	int[]    casesf;
	int[]    specsf;
	int[]    blocksf;
	int[]    slidesf;
	int[]    hef;
	int[]    ssf;
	int[]    ihcf;
	int[]    synoptf;
	int[]    frozenf;
	double[] fte1f;
	double[] fte2f;
	double[] fte3f;
	double[] fte4f;
	double[] fte5f;
	Object[] children;

	@Override
	public String toString() {
		return this.name;
	}
}