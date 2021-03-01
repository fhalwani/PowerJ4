package ca.powerj.data;
import java.util.Date;

public class PersonData {
	private boolean active = false;
	private boolean[] bits = new boolean[32];
	private short prsID = 0;
	private int access = 0;
	private Date started = new Date();
	private String code = "";
	private String initials = "";
	private String lastname = "";
	private String firstname = "";

	public int getAccess() {
		return access;
	}

	public boolean getBit(int key) {
		return bits[key];
	}

	public boolean[] getBits() {
		return bits;
	}

	public String getCode() {
		return code;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getInitials() {
		return initials;
	}

	public String getLastname() {
		return lastname;
	}

	public short getPrsID() {
		return prsID;
	}

	public Date getStarted() {
		return started;
	}

	public boolean isActive() {
		return active;
	}

	public void setAccess(int value) {
		this.access = value;
	}

	public void setActive(boolean value) {
		this.active = value;
	}

	public void setBit(int key, boolean value) {
		this.bits[key] = value;
	}

	public void setBits(boolean[] value) {
		this.bits = value;
	}

	public void setCode(String value) {
		if (value != null) {
			this.code = value.trim();
		}
	}

	public void setFirstname(String value) {
		if (value != null) {
			value = value.trim();
			if (value.length() > 30) {
				this.firstname = value.substring(0, 30);
			} else {
				this.firstname = value;
			}
		}
	}

	public void setInitials(String value) {
		if (value != null) {
			value = value.trim().toUpperCase();
			if (value.length() == 0) {
				this.initials = this.firstname.substring(0, 1).toUpperCase()
						+ this.lastname.substring(0, 2).toUpperCase();
			} else if (value.length() > 3) {
				this.initials = value.substring(0, 3);
			} else {
				this.initials = value;
			}
		}
	}

	public void setLastname(String value) {
		if (value != null) {
			value = value.trim();
			if (value.length() > 30) {
				this.lastname = value.substring(0, 30);
			} else {
				this.lastname = value;
			}
		}
	}

	public void setPrsID(short value) {
		this.prsID = value;
	}

	public void setStarted(long value) {
		this.started.setTime(value);
	}
}