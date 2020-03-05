package ca.powerj;

class OSpecnode {
	int    noSpecs  = 0;
	int    noBlocks = 0;
	int    noSlides = 0;
	int    noHE     = 0;
	int    noSS     = 0;
	int    noIHC    = 0;
	double fte1     = 0;
	double fte2     = 0;
	double fte3     = 0;
	double fte4     = 0;
	double fte5     = 0;
	String name     = "";
	Object[] children;

	OSpecnode(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}