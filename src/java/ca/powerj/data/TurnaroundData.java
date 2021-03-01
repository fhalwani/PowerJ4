package ca.powerj.data;

public class TurnaroundData {
	private boolean newRow = false;
	private byte turID = 0;
	private short gross = 0;
	private short embed = 0;
	private short micro = 0;
	private short route = 0;
	private short diagn = 0;
	private String name = "";

	public short getDiagnosis() {
		return diagn;
	}

	public short getEmbed() {
		return embed;
	}

	public short getGross() {
		return gross;
	}

	public short getMicrotomy() {
		return micro;
	}

	public String getName() {
		return name;
	}

	public short getRoute() {
		return route;
	}

	public byte getTurID() {
		return turID;
	}

	public boolean isNewRow() {
		return newRow;
	}

	public void setDiagnosis(short value) {
		this.diagn = value;
	}

	public void setEmbed(short value) {
		this.embed = value;
	}

	public void setGross(short value) {
		this.gross = value;
	}

	public void setMicrotomy(short value) {
		this.micro = value;
	}

	public void setName(String value) {
		value = value.trim();
		if (value.length() > 16) {
			this.name = value.substring(0, 16);
		} else {
			this.name = value;
		}
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	public void setRoute(short value) {
		this.route = value;
	}

	public void setTurID(byte value) {
		this.turID = value;
	}
}