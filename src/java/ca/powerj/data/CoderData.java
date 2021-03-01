package ca.powerj.data;

public class CoderData {
	private boolean newRow = false;
	private short   codeID = 0;
	private short   ruleID = 0;
	private short   count  = 0;
	private double  valueA = 0;
	private double  valueB = 0;
	private double  valueC = 0;
	private String  name   = "";
	private String  descr  = "";

	public short getCodeID() {
		return codeID;
	}

	public short getCount() {
		return count;
	}

	public String getDescr() {
		return descr;
	}

	public String getName() {
		return name;
	}

	public short getRuleID() {
		return ruleID;
	}

	public double getValueA() {
		return valueA;
	}

	public double getValueB() {
		return valueB;
	}

	public double getValueC() {
		return valueC;
	}

	public boolean isNewRow() {
		return newRow;
	}

	public void setCodeID(short value) {
		this.codeID = value;
	}

	public void setCount(short value) {
		this.count = value;
	}

	public void setDescr(String value) {
		if (value.length() > 256) {
			this.descr = value.substring(0, 256);
		} else {
			this.descr = value;
		}
	}

	public void setName(String value) {
		if (value.length() > 16) {
			this.name = value.substring(0, 16);
		} else {
			this.name = value;
		}
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	public void setRuleID(short value) {
		this.ruleID = value;
	}

	public void setValueA(double value) {
		this.valueA = value;
	}

	public void setValueB(double value) {
		this.valueB = value;
	}

	public void setValueC(double value) {
		this.valueC = value;
	}

	@Override
	public String toString() {
		return name;
	}
}