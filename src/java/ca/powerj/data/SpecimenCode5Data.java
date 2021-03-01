package ca.powerj.data;

public class SpecimenCode5Data {
	private short grpID = 0;
	private int qty = 0;
	private double totalCAP = 0;
	private double totalW2Q = 0;
	private double totalRCP = 0;
	private double totalCPT = 0;
	private int code5 = 0;
	private String name = "";

	public int getCode5() {
		return code5;
	}

	public short getGrpID() {
		return grpID;
	}

	public String getName() {
		return name;
	}

	public int getQty() {
		return qty;
	}

	public double getTotalCAP() {
		return totalCAP;
	}

	public double getTotalCPT() {
		return totalCPT;
	}

	public double getTotalRCP() {
		return totalRCP;
	}

	public double getTotalW2Q() {
		return totalW2Q;
	}

	public void setCode5(int value) {
		this.code5 = value;
	}

	public void setGrpID(short value) {
		this.grpID = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setQty(int value) {
		this.qty = value;
	}

	public void setTotalCAP(double value) {
		this.totalCAP += value;
	}

	public void setTotalCPT(double value) {
		this.totalCPT += value;
	}

	public void setTotalRCP(double value) {
		this.totalRCP += value;
	}

	public void setTotalW2Q(double value) {
		this.totalW2Q += value;
	}
}