package ca.powerj.data;

public class WorkflowData {
	private short prsID = 0;
	private short noCases = 0;
	private short noSpecs = 0;
	private short noSlides = 0;
	private int noPending = 0;
	private int noIn = 0;
	private int noOut = 0;
	private int value5 = 0;
	private double ftPending = 0;
	private double ftIn = 0;
	private double ftOut = 0;
	private String name = "";
	private String full = "";

	public double getFtIn() {
		return ftIn;
	}

	public double getFtOut() {
		return ftOut;
	}

	public double getFtPending() {
		return ftPending;
	}

	public String getFull() {
		return full;
	}

	public String getName() {
		return name;
	}

	public short getNoCases() {
		return noCases;
	}

	public int getNoIn() {
		return noIn;
	}

	public int getNoOut() {
		return noOut;
	}

	public int getNoPending() {
		return noPending;
	}

	public short getNoSlides() {
		return noSlides;
	}

	public short getNoSpecs() {
		return noSpecs;
	}

	public short getPrsID() {
		return prsID;
	}

	public int getValue5() {
		return value5;
	}

	public void setFtIn(double value) {
		this.ftIn = value;
	}

	public void setFtOut(double value) {
		this.ftOut = value;
	}

	public void setFtPending(double value) {
		this.ftPending = value;
	}

	public void setFull(String value) {
		this.full = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNoCases(short value) {
		this.noCases = value;
	}

	public void setNoIn(int value) {
		this.noIn = value;
	}

	public void setNoOut(int value) {
		this.noOut = value;
	}

	public void setNoPending(int value) {
		this.noPending = value;
	}

	public void setNoSlides(short value) {
		this.noSlides = value;
	}

	public void setNoSpecs(short value) {
		this.noSpecs = value;
	}

	public void setPrsID(short value) {
		this.prsID = value;
	}

	public void setValue5(int value) {
		this.value5 = value;
	}
}