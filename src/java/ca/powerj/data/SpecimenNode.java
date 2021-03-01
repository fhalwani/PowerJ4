package ca.powerj.data;

public class SpecimenNode {
	private int noSpecs = 0;
	private int noBlocks = 0;
	private int noSlides = 0;
	private int noHE = 0;
	private int noSS = 0;
	private int noIHC = 0;
	private double fte1 = 0;
	private double fte2 = 0;
	private double fte3 = 0;
	private double fte4 = 0;
	private double fte5 = 0;
	private String name = "";
	private Object[] children;

	public SpecimenNode(String name) {
		this.name = name;
	}

	public Object getChild(int index) {
		return children[index];
	}

	public Object[] getChildren() {
		return children;
	}

	public double getFte1() {
		return fte1;
	}

	public double getFte2() {
		return fte2;
	}

	public double getFte3() {
		return fte3;
	}

	public double getFte4() {
		return fte4;
	}

	public double getFte5() {
		return fte5;
	}

	public String getName() {
		return name;
	}

	public int getNoBlocks() {
		return noBlocks;
	}

	public int getNoChildren() {
		if (children != null) {
			return children.length;
		}
		return 0;
	}

	public int getNoHE() {
		return noHE;
	}

	public int getNoIHC() {
		return noIHC;
	}

	public int getNoSlides() {
		return noSlides;
	}

	public int getNoSpecs() {
		return noSpecs;
	}

	public int getNoSS() {
		return noSS;
	}

	public void setChild(int index, Object value) {
		this.children[index] = value;
	}

	public void setChildren(int value) {
		this.children = new Object[value];
	}

	public void setFte1(double value) {
		this.fte1 = value;
	}

	public void setFte2(double value) {
		this.fte2 = value;
	}

	public void setFte3(double value) {
		this.fte3 = value;
	}

	public void setFte4(double value) {
		this.fte4 = value;
	}

	public void setFte5(double value) {
		this.fte5 = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNoBlocks(int value) {
		this.noBlocks = value;
	}

	public void setNoHE(int value) {
		this.noHE = value;
	}

	public void setNoIHC(int value) {
		this.noIHC = value;
	}

	public void setNoSlides(int value) {
		this.noSlides = value;
	}

	public void setNoSpecs(int value) {
		this.noSpecs = value;
	}

	public void setNoSS(int value) {
		this.noSS = value;
	}

	@Override
	public String toString() {
		return this.name;
	}
}