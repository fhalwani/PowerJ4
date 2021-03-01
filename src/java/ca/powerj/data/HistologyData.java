package ca.powerj.data;

public class HistologyData {
	private byte statusID = 0;
	private byte subID = 0;
	private byte procID = 0;
	private byte grossed = 0;
	private byte embeded = 0;
	private byte microed = 0;
	private byte routed = 0;
	private short facID = 0;
	private int noBlocks = 0;
	private int noSlides = 0;

	public byte getEmbeded() {
		return embeded;
	}

	public short getFacID() {
		return facID;
	}

	public byte getGrossed() {
		return grossed;
	}

	public byte getMicroed() {
		return microed;
	}

	public int getNoBlocks() {
		return noBlocks;
	}

	public int getNoSlides() {
		return noSlides;
	}

	public byte getProcID() {
		return procID;
	}

	public byte getRouted() {
		return routed;
	}

	public byte getStatusID() {
		return statusID;
	}

	public byte getSubID() {
		return subID;
	}

	public void setEmbeded(byte value) {
		this.embeded = value;
	}

	public void setFacID(short value) {
		this.facID = value;
	}

	public void setGrossed(byte value) {
		this.grossed = value;
	}

	public void setMicroed(byte value) {
		this.microed = value;
	}

	public void setNoBlocks(int value) {
		this.noBlocks = value;
	}

	public void setNoSlides(int value) {
		this.noSlides = value;
	}

	public void setProcID(byte value) {
		this.procID = value;
	}

	public void setRouted(byte value) {
		this.routed = value;
	}

	public void setStatusID(byte value) {
		this.statusID = value;
	}

	public void setSubID(byte value) {
		this.subID = value;
	}
}