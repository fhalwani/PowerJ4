package ca.powerj.data;

public class ReportSpecimenData {
	private final char NODE_TYPE = 'S';
	private boolean composite = false;
	private byte order = 0;
	private byte orgID = 0;
	private byte proID = 0;
	private byte spyID = 0;
	private byte subID = 0;
	private long specID = 0;
	private String specimen = "";
	private String location = "";
	private String procedure = "";
	private String name = "";

	ReportSpecimenData() {
	}

	String getLocation() {
		return location;
	}

	byte getOrderID() {
		return order;
	}

	short getOrganID() {
		return orgID;
	}

	String getProcedure() {
		return procedure;
	}

	byte getProcedureID() {
		return proID;
	}

	byte getSpecialtyID() {
		return spyID;
	}

	String getSpecimen() {
		return specimen;
	}

	long getSpecimenID() {
		return specID;
	}

	byte getSubspecialtyID() {
		return subID;
	}

	char getType() {
		return NODE_TYPE;
	}

	boolean isComposite() {
		return composite;
	}

	void setComposite(boolean value) {
		this.composite = value;
	}

	void setSpecimen(byte spyID, byte subID, byte orgID, byte procID, byte order,
			long specID, String specimen, String location, String procedure, String name) {
		this.spyID = spyID;
		this.subID = subID;
		this.orgID = orgID;
		this.order = order;
		this.proID = procID;
		this.specID = specID;
		if (specimen == null) {
			this.specimen = "";
		} else {
			this.specimen = specimen.trim();
		}
		if (location == null) {
			this.location = "";
		} else {
			this.location = location.trim();
		}
		if (procedure == null) {
			this.procedure = "";
		} else {
			this.procedure = procedure.trim();
		}
		if (name == null) {
			this.name = "";
		} else {
			this.name = name.trim();
		}
		if (name.length() > 0) {
			if (name.length() > 10) {
				name = name.substring(0, 10);
			}
		} else if (location.length() > 0) {
			if (location.length() > 10) {
				name = location.substring(0, 10);
			} else {
				name = location;
			}
		} else if (specimen.length() > 0) {
			if (specimen.length() > 10) {
				name = specimen.substring(0, 10);
			} else {
				name = specimen;
			}
		} else if (procedure.length() > 0) {
			if (procedure.length() > 10) {
				name = procedure.substring(0, 10);
			} else {
				name = procedure;
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}
}