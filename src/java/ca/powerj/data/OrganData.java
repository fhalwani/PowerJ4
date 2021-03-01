package ca.powerj.data;

public class OrganData {
	private byte spyID = 0;
	private byte subID = 0;
	private byte orgID = 0;
	private short ssoID = 0;
	private String name = "";

	public OrganData() {
	}

	public OrganData(byte spyID, byte subID, byte orgID, short ssoID, String name) {
		this.spyID = spyID;
		this.subID = subID;
		this.orgID = orgID;
		this.ssoID = ssoID;
		if (name == null) {
			this.name = "";
		} else {
			this.name = name.trim();
		}
	}

	public String getName() {
		return name;
	}

	public short getID() {
		return ssoID;
	}

	public byte getOrgID() {
		return orgID;
	}

	public byte getSpyID() {
		return spyID;
	}

	public byte getSubID() {
		return subID;
	}
}