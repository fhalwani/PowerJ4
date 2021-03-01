package ca.powerj.data;

public class WorkdaySumData {
	private short prsID = 0;
	private int noDays = 0;
	private String prsFull = "";
	private String prsName = "";
	private int[] noServices;

	public int getNoDays() {
		return noDays;
	}

	public int getNoServices(int index) {
		return noServices[index];
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

	public void setNoDays(int value) {
		this.noDays = value;
	}

	public void setNoServices(int value) {
		this.noServices = new int[value];
	}

	public void setNoServiceDays(int index, int value) {
		this.noServices[index] = value;
	}

	public void setPrsFull(String value) {
		this.prsFull = value;
	}

	public void setPrsID(short value) {
		this.prsID = value;
	}

	public void setPrsName(String value) {
		this.prsName = value;
	}
}