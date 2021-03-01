package ca.powerj.data;

public class TissueData {
	private boolean newRow = false;
	private boolean altered = false;
	private boolean complex = false;
	private byte spyID = 0;
	private short orgID = 0;
	private short proID = 0;
	private short tisID = 0;
	private String name = "";
	private String description = "";
	private String procedure = "";

	public TissueData() {
		newRow = true;
	}

	public TissueData(short tisID, byte spyID, short orgID, short proID, boolean complex, String name,
			String description1, String procedure1, String description2, String procedure2) {
		this(tisID, spyID, orgID, proID, name, description1, procedure1, description2, procedure2);
		this.complex = complex;
	}

	public TissueData(short tisID, byte spyID, short orgID, short proID, String name, String description1,
			String procedure1, String description2, String procedure2) {
		this(tisID, proID, name, description1, procedure1, description2, procedure2);
		this.spyID = spyID;
		this.orgID = orgID;
	}

	public TissueData(short tisID, byte spyID, short orgID, short proID, String complex, String name,
			String description1, String procedure1, String description2, String procedure2) {
		this(tisID, spyID, orgID, proID, name, description1, procedure1, description2, procedure2);
		setComplex(complex);
	}

	public TissueData(short tisID, short proID, boolean complex, String name, String description1, String procedure1,
			String description2, String procedure2) {
		this(tisID, proID, name, description1, procedure1, description2, procedure2);
		this.complex = complex;
	}

	public TissueData(short tisID, short proID, String name, String description1, String procedure1,
			String description2, String procedure2) {
		this.tisID = tisID;
		this.proID = proID;
		setName(name);
		if (procedure2 == null || procedure2.trim().length() == 0) {
			setProcedure(procedure1);
		} else {
			setProcedure(procedure2);
		}
		if (description2 == null || description2.trim().length() == 0) {
			setDescription(description1);
		} else {
			setDescription(description2);
		}
	}

	public TissueData(short tisID, short proID, String complex, String name, String description1, String procedure1,
			String description2, String procedure2) {
		this(tisID, proID, name, description1, procedure1, description2, procedure2);
		setComplex(complex);
	}

	public String getDescription() {
		return description;
	}

	public short getID() {
		return tisID;
	}

	public String getName() {
		return name;
	}

	public short getOrgID() {
		return orgID;
	}

	public String getProcedure() {
		return procedure;
	}

	public short getProID() {
		return proID;
	}

	public byte getSpyID() {
		return spyID;
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

	public void setDescription(String value) {
		if (value == null) {
			this.description = "";
		} else {
			this.description = value.trim();
		}
	}

	public void setID(short value) {
		this.tisID = value;
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

	public void setProcedure(String value) {
		if (value == null) {
			this.procedure = "";
		} else {
			this.procedure = value.trim();
		}
	}

	public void setProID(short value) {
		this.proID = value;
	}

	public void setSpyID(byte value) {
		this.spyID = value;
	}

	@Override
	public String toString() {
		return name;
	}
}