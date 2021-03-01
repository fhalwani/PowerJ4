package ca.powerj.data;

public class ErrorData {
	private byte errID = 0;
	private long caseID = 0;
	private String caseNo = "";

	public long getCaseID() {
		return caseID;
	}

	public String getCaseNo() {
		return caseNo;
	}

	public byte getErrID() {
		return errID;
	}

	public void setCaseID(long value) {
		this.caseID = value;
	}

	public void setCaseNo(String value) {
		caseNo = value;
	}

	public void setErrID(byte value) {
		errID = value;
	}

	@Override
	public String toString() {
		return caseNo;
	}
}