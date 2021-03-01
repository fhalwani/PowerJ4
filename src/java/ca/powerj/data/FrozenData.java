package ca.powerj.data;

public class FrozenData {
	private short noBlocks = 0;
	private short noSlides = 0;
	private int value5 = 0;
	private long specID = 0;
	private double value1 = 0;
	private double value2 = 0;
	private double value3 = 0;
	private double value4 = 0;
	private String finalBy = "";
	private String name = "";

	public String getFinalBy() {
		return finalBy;
	}

	public String getName() {
		return name;
	}

	public short getNoBlocks() {
		return noBlocks;
	}

	public short getNoSlides() {
		return noSlides;
	}

	public long getSpecID() {
		return specID;
	}

	public double getValue1() {
		return value1;
	}

	public double getValue2() {
		return value2;
	}

	public double getValue3() {
		return value3;
	}

	public double getValue4() {
		return value4;
	}

	public int getValue5() {
		return value5;
	}

	public void setFinalBy(String value) {
		this.finalBy = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNoBlocks(short value) {
		this.noBlocks = value;
	}

	public void setNoSlides(short value) {
		this.noSlides = value;
	}

	public void setSpecID(long value) {
		this.specID = value;
	}

	public void setValue1(double value) {
		this.value1 = value;
	}

	public void setValue2(double value) {
		this.value2 = value;
	}

	public void setValue3(double value) {
		this.value3 = value;
	}

	public void setValue4(double value) {
		this.value4 = value;
	}

	public void setValue5(int value) {
		this.value5 = value;
	}
}