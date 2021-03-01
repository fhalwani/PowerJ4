package ca.powerj.data;

public class WorkloadData {
	private byte proID = 0;
	private byte spyID = 0;
	private byte subID = 0;
	private short facID = 0;
	private short prsID = 0;
	private int noBlocks = 0;
	private int noCases = 0;
	private int noSlides = 0;
	private int noSpecs = 0;
	private double fte1 = 0;
	private double fte2 = 0;
	private double fte3 = 0;
	private double fte4 = 0;
	private double fte5 = 0;
	private String facName = "";
	private String proName = "";
	private String prsName = "";
	private String prsFull = "";
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

	public int getNoCases() {
		return noCases;
	}

	public int getNoSlides() {
		return noSlides;
	}

	public int getNoSpecs() {
		return noSpecs;
	}

	public byte getProID() {
		return proID;
	}

	public String getProName() {
		return proName;
	}

	public String getPrsFull() {
		return prsFull;
	}

	public short getPrsID() {
		return prsID;
	}

	public String getPrsName() {
		return prsName;
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

	public void setFacID(short facID) {
		this.facID = facID;
	}

	public void setFacName(String facName) {
		this.facName = facName;
	}

	public void setFte1(double fte1) {
		this.fte1 = fte1;
	}

	public void setFte2(double fte2) {
		this.fte2 = fte2;
	}

	public void setFte3(double fte3) {
		this.fte3 = fte3;
	}

	public void setFte4(double fte4) {
		this.fte4 = fte4;
	}

	public void setFte5(double fte5) {
		this.fte5 = fte5;
	}

	public void setNoBlocks(int noBlocks) {
		this.noBlocks = noBlocks;
	}

	public void setNoCases(int noCases) {
		this.noCases = noCases;
	}

	public void setNoSlides(int noSlides) {
		this.noSlides = noSlides;
	}

	public void setNoSpecs(int noSpecs) {
		this.noSpecs = noSpecs;
	}

	public void setProID(byte proID) {
		this.proID = proID;
	}

	public void setProName(String proName) {
		this.proName = proName;
	}

	public void setPrsFull(String value) {
		this.prsFull = value;
	}

	public void setPrsID(short prsID) {
		this.prsID = prsID;
	}

	public void setPrsName(String prsName) {
		this.prsName = prsName;
	}

	public void setSpyID(byte spyID) {
		this.spyID = spyID;
	}

	public void setSpyName(String spyName) {
		this.spyName = spyName;
	}

	public void setSubID(byte subID) {
		this.subID = subID;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}
}