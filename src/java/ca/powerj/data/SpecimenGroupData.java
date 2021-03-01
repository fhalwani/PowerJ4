package ca.powerj.data;

public class SpecimenGroupData {
	private boolean newRow = false;
	private boolean hasLN = false;
	private byte subID = 0;
	private byte spyID = 0;
	private byte proID = 0;
	private short grpID = 0;
	private int value5 = 0;
	private String descr = "";
	private String procedure = "";
	private String specialty = "";
	private String subspecial = "";
	private ItemData[][] codes = new ItemData[3][4];

	public SpecimenGroupData() {
		for (short i = 0; i < 3; i++) {
			for (short j = 0; j < 4; j++) {
				codes[i][j] = new ItemData((short) 0, "");
			}
		}
	}

	public ItemData getCode(int i, int j) {
		return codes[i][j];
	}

	public short getCodeId(int i, int j) {
		return (short)codes[i][j].getID();
	}

	public String getCodeName(int i, int j) {
		return codes[i][j].getName();
	}

	public String getDescr() {
		return descr;
	}

	public short getGrpID() {
		return grpID;
	}

	public String getProcedure() {
		return procedure;
	}

	public byte getProID() {
		return proID;
	}

	public String getSpecialty() {
		return specialty;
	}

	public byte getSpyID() {
		return spyID;
	}

	public byte getSubID() {
		return subID;
	}

	public String getSubspecial() {
		return subspecial;
	}

	public int getValue5() {
		return value5;
	}

	public boolean isHasLN() {
		return hasLN;
	}

	public boolean isNewRow() {
		return newRow;
	}

	public void setCode(int i, int j, ItemData value) {
		this.codes[i][j] = value;
	}

	public void setCode(int i, int j, short key, String value) {
		this.codes[i][j].setID(key);
		this.codes[i][j].setName(value.trim());
	}

	public void setDescr(String value) {
		value = value.trim();
		if (value.length() > 64) {
			this.descr = value.substring(0, 64);
		} else {
			this.descr = value;
		}
	}

	public void setGrpID(short value) {
		this.grpID = value;
	}

	public void setHasLN(boolean value) {
		this.hasLN = value;
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	public void setProcedure(String value) {
		this.procedure = value.trim();
	}

	public void setProID(byte value) {
		this.proID = value;
	}

	public void setSpecialty(String value) {
		this.specialty = value.trim();
	}

	public void setSpyID(byte value) {
		this.spyID = value;
	}

	public void setSubID(byte value) {
		this.subID = value;
	}

	public void setSubspecial(String value) {
		this.subspecial = value.trim();
	}

	public void setValue5(int value) {
		this.value5 = value;
	}
}