package ca.powerj.data;

public class OrderGroupData {
	private boolean newRow = false;
	private short grpID = 0;
	private short typeID = 0;
	private short value1 = 0;
	private short value2 = 0;
	private short value3 = 0;
	private short value4 = 0;
	private short value5 = 0;
	private String name = "";
	private String descr = "";

	public String getDescr() {
		return descr;
	}

	public short getGrpID() {
		return grpID;
	}

	public String getName() {
		return name;
	}

	public short getTypeID() {
		return typeID;
	}

	public short getValue1() {
		return value1;
	}

	public short getValue2() {
		return value2;
	}

	public short getValue3() {
		return value3;
	}

	public short getValue4() {
		return value4;
	}

	public short getValue5() {
		return value5;
	}

	public boolean isNewRow() {
		return newRow;
	}

	public void setDescr(String value) {
		this.descr = value;
	}

	public void setGrpID(short value) {
		this.grpID = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	public void setTypeID(short value) {
		this.typeID = value;
	}

	public void setValue1(short value) {
		this.value1 = value;
	}

	public void setValue2(short value) {
		this.value2 = value;
	}

	public void setValue3(short value) {
		this.value3 = value;
	}

	public void setValue4(short value) {
		this.value4 = value;
	}

	public void setValue5(short value) {
		this.value5 = value;
	}
}