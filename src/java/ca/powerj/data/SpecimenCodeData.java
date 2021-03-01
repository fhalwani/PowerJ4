package ca.powerj.data;
import java.util.HashMap;

public class SpecimenCodeData {
	private boolean codeBlocks = true;
	private boolean inclusive = false;
	private boolean needFrag = false;
	private byte procID = 0;
	private short coderID = 0;
	private double value = 0;
	private double valueFS = 0;
	private HashMap<Short, OrderCoderData> lstOrders = new HashMap<Short, OrderCoderData>();

	public short getCoderID() {
		return coderID;
	}

	public OrderCoderData getOrder(short key) {
		return lstOrders.get(key);
	}

	public HashMap<Short, OrderCoderData> getOrders() {
		return lstOrders;
	}

	public byte getProcID() {
		return procID;
	}

	public double getValue() {
		return value;
	}

	public double getValueFS() {
		return valueFS;
	}

	public boolean isCodeBlocks() {
		return codeBlocks;
	}

	public boolean isInclusive() {
		return inclusive;
	}

	public boolean isNeedFrag() {
		return needFrag;
	}

	public void setCodeBlocks(boolean value) {
		this.codeBlocks = value;
	}

	public void setCoderID(short value) {
		this.coderID = value;
	}

	public void setInclusive(boolean value) {
		this.inclusive = value;
	}

	public void setNeedFrag(boolean value) {
		this.needFrag = value;
	}

	public void setOrder(short key, OrderCoderData value) {
		lstOrders.put(key, value);
	}

	public void setProcID(byte value) {
		this.procID = value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setValueFS(double value) {
		this.valueFS = value;
	}
}