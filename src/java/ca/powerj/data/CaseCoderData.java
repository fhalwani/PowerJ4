package ca.powerj.data;

public class CaseCoderData {
	private boolean hasError = false;
	private boolean isMalignant = false;
	private boolean isRadical = false;
	private boolean inclusive = false;
	private boolean codeBlocks = true;
	private byte errorID = 0;
	private short mainSpec = 0;
	private short coderID = 0;
	private double caseValue = 0;
	private double frozenValue = 0;
	private String comment = "";

	public String getComment() {
		return comment;
	}

	public short getCoderID() {
		return coderID;
	}

	public byte getErrorID() {
		return errorID;
	}

	public short getMainSpec() {
		return mainSpec;
	}

	public double getValue() {
		return caseValue;
	}

	public double getValueFrozen() {
		return frozenValue;
	}

	public boolean hasError() {
		return hasError;
	}

	public boolean isInclusive() {
		return inclusive;
	}

	public boolean isMalignant() {
		return isMalignant;
	}

	public boolean isRadical() {
		return isRadical;
	}

	public boolean isCodeBlocks() {
		return codeBlocks;
	}
	public void setCodeBlocks(boolean value) {
		this.codeBlocks = value;
	}

	public void setCoderID(short value) {
		this.coderID = value;
	}

	public void setComment(String value) {
		this.comment += value + "\n";
	}

	public void setErrorID(byte value) {
		this.errorID = value;
	}

	public void setHasError(boolean value) {
		this.hasError = value;
	}

	public void setInclusive(boolean value) {
		this.inclusive = value;
	}

	public void setMainSpec(int value) {
		if (caseValue < 0) {
			this.mainSpec = 0;
		} else if (caseValue > Short.MAX_VALUE) {
			this.mainSpec = Short.MAX_VALUE;
		} else {
			this.mainSpec = (short) (value -1);
		}
	}

	public void setMalignant(boolean value) {
		this.isMalignant = value;
	}

	public void setRadical(boolean value) {
		this.isRadical = value;
	}

	public void setValue(double value) {
		this.caseValue = value;
	}

	public void setValueFrozen(double value) {
		this.frozenValue = value;
	}
}