package ca.powerj.data;

public class ServiceSumData {
	private short noDays = 0;
	private String srvName = "";

	public short getNoDays() {
		return noDays;
	}

	public String getSrvName() {
		return srvName;
	}

	public void setNoDays() {
		this.noDays++;
	}

	public void setSrvName(String value) {
		this.srvName = value;
	}
}