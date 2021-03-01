package ca.powerj.data;
import java.util.Calendar;

public class AdditionalOrderData {
	private long caseID = 0;
	private short finalID = 0;
	private short proID = 0;
	private Calendar finaled = Calendar.getInstance();

	public long getCaseID() {
		return caseID;
	}

	public Calendar getFinalCalendar() {
		return finaled;
	}

	public short getFinalID() {
		return finalID;
	}

	public long getFinalTime() {
		return finaled.getTimeInMillis();
	}

	public short getProID() {
		return proID;
	}

	public void setCaseID(long value) {
		this.caseID = value;
	}

	public void setFinaled(long value) {
		this.finaled.setTimeInMillis(value);
	}

	public void setFinalID(short value) {
		this.finalID = value;
	}

	public void setProID(short value) {
		this.proID = value;
	}
}