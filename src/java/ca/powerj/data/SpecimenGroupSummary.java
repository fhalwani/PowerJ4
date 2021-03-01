package ca.powerj.data;

public class SpecimenGroupSummary {
	private byte spyID = 0;
	private byte subID = 0;
	private byte proID = 0;
	private short facID = 0;
	private short spgID = 0;
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
	private String facName = "";
	private String proName = "";
	private String spgName = "";
	private String spyName = "";
	private String subName = "";

	public short getFacID() {
		return facID;
	}

	public String getFacName() {
		return facName;
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

	public int getNoBlocks() {
		return noBlocks;
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

	public byte getProID() {
		return proID;
	}

	public String getProName() {
		return proName;
	}

	public short getSpgID() {
		return spgID;
	}

	public String getSpgName() {
		return spgName;
	}

	public byte getSpyID() {
		return spyID;
	}

	public String getSpyName() {
		return spyName;
	}

	public byte getSubID() {
		return subID;
	}

	public String getSubName() {
		return subName;
	}

	public void setFacID(short value) {
		this.facID = value;
	}

	public void setFacName(String value) {
		this.facName = value;
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

	public void setProID(byte value) {
		this.proID = value;
	}

	public void setProName(String value) {
		this.proName = value;
	}

	public void setSpgID(short value) {
		this.spgID = value;
	}

	public void setSpgName(String value) {
		this.spgName = value;
	}

	public void setSpyID(byte value) {
		this.spyID = value;
	}

	public void setSpyName(String value) {
		this.spyName = value;
	}

	public void setSubID(byte value) {
		this.subID = value;
	}

	public void setSubName(String value) {
		this.subName = value;
	}
}