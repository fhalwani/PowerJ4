package ca.powerj.data;

public class ReportCaseData {
	private final char NODE_TYPE = 'C';
	private long caseID = 0;
	private String caseNo = "";

	public char getType() {
		return NODE_TYPE;
	}

	public long getCaseID() {
		return caseID;
	}

	public String getCaseNo() {
		return caseNo;
	}

	public void setCase(long caseID, String caseNo) {
		this.caseID = caseID;
		if (caseNo == null) {
			this.caseNo = "";
		} else {
			this.caseNo = caseNo.trim();
		}
	}

	@Override
	public String toString() {
		return caseNo;
	}
}