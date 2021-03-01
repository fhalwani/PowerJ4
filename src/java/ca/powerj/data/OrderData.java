package ca.powerj.data;

public class OrderData {
	private short orgID = 0;
	private short ormID = 0;
	private short qty = 0;
	private long createTime = 0;
	private double value1 = 0;
	private double value2 = 0;
	private double value3 = 0;
	private double value4 = 0;
	private String name = "";

	public long getCreatedTime() {
		return createTime;
	}

	public String getName() {
		return name;
	}

	public short getOrgID() {
		return orgID;
	}

	public short getOrmID() {
		return ormID;
	}

	public short getQty() {
		return qty;
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

	public void setCreatedTime(long value) {
		this.createTime = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setOrgID(short value) {
		this.orgID = value;
	}

	public void setOrmID(short value) {
		this.ormID = value;
	}

	public void setQty(int value) {
		if (value > Short.MAX_VALUE) {
			this.qty = Short.MAX_VALUE;
		} else {
			this.qty = (short) value;
		}
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
}