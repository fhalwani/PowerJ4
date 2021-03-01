package ca.powerj.data;
import java.util.ArrayList;

public class OrderCoderData {
	private boolean addlBlock = false;
	private short codeID = 0;
	private short qty = 0;
	private double value = 0;
	private String name = "";
	private ArrayList<Short> orders = new ArrayList<Short>();

	public short getCodeID() {
		return codeID;
	}

	public String getName() {
		return name;
	}

	public short getQty() {
		return qty;
	}

	public double getValue() {
		return value;
	}

	public boolean isAddlBlock() {
		return addlBlock;
	}

	public void setAddlBlock(boolean value) {
		this.addlBlock = value;
	}

	public boolean isOrderUnique(short orderID) {
		for (int i = 0; i < orders.size(); i++) {
			if (orders.get(i) == orderID) {
				return false;
			}
		}
		orders.add(orderID);
		return true;
	}

	public void setCodeID(short value) {
		this.codeID = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setQty(short value) {
		this.qty = value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}