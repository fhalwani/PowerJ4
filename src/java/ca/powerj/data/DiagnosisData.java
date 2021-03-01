package ca.powerj.data;

public class DiagnosisData {
	private boolean newRow = false;
	private boolean altered = false;
	private boolean complex = false;
	private byte disID = 0;
	private short orgID = 0;
	private int diaID = 0;
	private String name = "";
	private String diagnosis = "";
	private String microscopic = "";

	public DiagnosisData() {
		newRow = true;
	}

	public DiagnosisData(int diaID, byte disID, boolean complex, String name, String diagnosis1, String microscopic1,
			String diagnosis2, String microscopic2) {
		this(diaID, disID, name, diagnosis1, microscopic1, diagnosis2, microscopic2);
		this.complex = complex;
	}

	public DiagnosisData(int diaID, byte disID, String name, String diagnosis1, String microscopic1, String diagnosis2,
			String microscopic2) {
		this.diaID = diaID;
		this.disID = disID;
		setName(name);
		if (diagnosis2 == null || diagnosis2.trim().length() == 0) {
			setDiagnosis(diagnosis1);
		} else {
			setDiagnosis(diagnosis2);
		}
		if (microscopic2 == null || microscopic2.trim().length() == 0) {
			setMicroscopic(microscopic1);
		} else {
			setMicroscopic(microscopic2);
		}
	}

	public DiagnosisData(int diaID, byte disID, String complex, String name, String diagnosis1, String microscopic1,
			String diagnosis2, String microscopic2) {
		this(diaID, disID, name, diagnosis1, microscopic1, diagnosis2, microscopic2);
		setComplex(complex);
	}

	public DiagnosisData(int diaID, short orgID, byte disID, boolean complex, String name, String diagnosis1,
			String microscopic1, String diagnosis2, String microscopic2) {
		this(diaID, orgID, disID, name, diagnosis1, microscopic1, diagnosis2, microscopic2);
		this.complex = complex;
	}

	public DiagnosisData(int diaID, short orgID, byte disID, String name, String diagnosis1, String microscopic1,
			String diagnosis2, String microscopic2) {
		this(diaID, disID, name, diagnosis1, microscopic1, diagnosis2, microscopic2);
		this.orgID = orgID;
	}

	public DiagnosisData(int diaID, short orgID, byte disID, String complex, String name, String diagnosis1,
			String microscopic1, String diagnosis2, String microscopic2) {
		this(diaID, orgID, disID, name, diagnosis1, microscopic1, diagnosis2, microscopic2);
		setComplex(complex);
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public byte getDisID() {
		return disID;
	}

	public int getID() {
		return diaID;
	}

	public String getMicroscopic() {
		return microscopic;
	}

	public String getName() {
		return name;
	}

	public short getOrgID() {
		return orgID;
	}

	public boolean isAltered() {
		return altered;
	}

	public boolean isComplex() {
		return complex;
	}

	public boolean isNewRow() {
		return newRow;
	}

	public void setAltered(boolean value) {
		this.altered = value;
	}

	public void setComplex(boolean value) {
		this.complex = value;
	}

	public void setComplex(String value) {
		if (value != null) {
			this.complex = (value.trim().equals("Y") ? true : false);
		}
	}

	public void setDiagnosis(String value) {
		if (value == null) {
			this.diagnosis = "";
		} else {
			this.diagnosis = value.trim();
		}
	}

	public void setDisID(byte value) {
		this.disID = value;
	}

	public void setID(int value) {
		this.diaID = value;
	}

	public void setMicroscopic(String value) {
		if (value == null) {
			this.microscopic = "";
		} else {
			this.microscopic = value.trim();
		}
	}

	public void setName(String value) {
		if (value == null) {
			this.name = "";
		} else {
			this.name = value.trim();
		}
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	public void setOrgID(short value) {
		this.orgID = value;
	}
}