package ca.powerj;

class OWorknode {
	int    noCases  = 0;
	int    noSpecs  = 0;
	int    noBlocks = 0;
	int    noSlides = 0;
	double fte1     = 0;
	double fte2     = 0;
	double fte3     = 0;
	double fte4     = 0;
	double fte5     = 0;
	String name     = "";
	Object[] children;

	OWorknode(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}