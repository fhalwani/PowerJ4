package ca.powerj;

class OAnnualNode {
	String   name = "";
	int[]    specs;
	int[]    blocks;
	int[]    slides;
	double[] fte1;
	double[] fte2;
	double[] fte3;
	double[] fte4;
	double[] fte5;
	int[]    specsf;
	int[]    blocksf;
	int[]    slidesf;
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