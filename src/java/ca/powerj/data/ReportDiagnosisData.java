package ca.powerj.data;

public class ReportDiagnosisData {
	private final char NODE_TYPE = 'T';
	private byte disID = 0;
	private byte ordID = 0;
	private byte parent = 0;
	private int diaID = 0;
	private String diagnosis = "";
	private String microscopic = "";
	private String name = "";

	ReportDiagnosisData(byte parent) {
		this.parent = parent;
	}

	byte getDiseaseID() {
		return disID;
	}

	String getDiagnosis() {
		return diagnosis;
	}

	int getDiagnosisID() {
		return diaID;
	}

	String getMicroscopic() {
		return microscopic;
	}

	byte getOrderID() {
		return ordID;
	}

	byte getParent() {
		return parent;
	}

	char getType() {
		return NODE_TYPE;
	}

	void setDiagnosis(byte orderID, byte diseaseID, int diagnosisID, String diagnosis, String microscopic, String name) {
		this.ordID = orderID;
		this.disID = diseaseID;
		this.diaID = diagnosisID;
		if (diagnosis == null) {
			this.diagnosis = "";
		} else {
			this.diagnosis = diagnosis.trim();
		}
		if (microscopic == null) {
			this.microscopic = "";
		} else {
			this.microscopic = microscopic.trim();
		}
		if (name == null) {
			this.name = "";
		} else {
			this.name = name.trim();
		}
		setName();
	}

	private void setName() {
		if (name.length() > 0) {
			if (name.length() > 10) {
				name = name.substring(0, 10);
			}
		} else if (diagnosis.length() > 0) {
			if (diagnosis.length() > 10) {
				name = diagnosis.substring(0, 10);
			} else {
				name = diagnosis;
			}
		} else if (microscopic.length() > 0) {
			if (microscopic.length() > 10) {
				name = microscopic.substring(0, 10);
			} else {
				name = microscopic;
			}
		}
	}

	void setParent(byte parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return name;
	}
}