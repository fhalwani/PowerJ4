package ca.powerj.data;
import java.util.HashMap;

public class WorkdayTempData {
	private short prsID = 0;
	private int noDays = 0;
	private String prsFull = "";
	private String prsName = "";
	private HashMap<Integer, ServiceSumData> services = new HashMap<Integer, ServiceSumData>();

	public int getNoDays() {
		return noDays;
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

	public ServiceSumData getService(int index) {
		return services.get(index);
	}

	public void setNoDays(int value) {
		this.noDays = value;
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

	public void setService(int key, ServiceSumData value) {
		this.services.put(key, value);
	}
}