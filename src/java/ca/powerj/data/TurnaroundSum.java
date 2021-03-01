package ca.powerj.data;

public class TurnaroundSum {
	private byte spyID = 0;
	private byte subID = 0;
	private byte proID = 0;
	private byte month = 0;
	private short facID = 0;
	private short year = 0;
	private int qty = 0;
	private int gross = 0;
	private int embed = 0;
	private int micro = 0;
	private int route = 0;
	private int diagn = 0;

	public int getDiagn() {
		return diagn;
	}

	public int getEmbed() {
		return embed;
	}

	public short getFacID() {
		return facID;
	}

	public int getGross() {
		return gross;
	}

	public int getMicro() {
		return micro;
	}

	public byte getMonth() {
		return month;
	}

	public byte getProID() {
		return proID;
	}

	public int getQty() {
		return qty;
	}

	public int getRoute() {
		return route;
	}

	public byte getSpyID() {
		return spyID;
	}

	public byte getSubID() {
		return subID;
	}

	public short getYear() {
		return year;
	}

	public void setDiagn(int diagn) {
		this.diagn = diagn;
	}

	public void setEmbed(int embed) {
		this.embed = embed;
	}

	public void setFacID(short facID) {
		this.facID = facID;
	}

	public void setGross(int gross) {
		this.gross = gross;
	}

	public void setMicro(int micro) {
		this.micro = micro;
	}

	public void setMonth(byte month) {
		this.month = month;
	}

	public void setProID(byte proID) {
		this.proID = proID;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public void setRoute(int route) {
		this.route = route;
	}

	public void setSpyID(byte spyID) {
		this.spyID = spyID;
	}

	public void setSubID(byte subID) {
		this.subID = subID;
	}

	public void setYear(short year) {
		this.year = year;
	}
}