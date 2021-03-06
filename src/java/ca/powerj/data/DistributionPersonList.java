package ca.powerj.data;
import java.util.HashMap;

public class DistributionPersonList {
	private short prsID = 0;
	private int count = 0;
	private double fte = 0;
	private String prsName = "";
	private String prsFull = "";
	private HashMap<Short, Double> doubles = new HashMap<Short, Double>();
	private HashMap<Short, Integer> integers = new HashMap<Short, Integer>();

	public int getCount() {
		return count;
	}

	public Double getDouble(Short key) {
		return doubles.get(key);
	}

	public double getFte() {
		return fte;
	}

	public Integer getInteger(Short key) {
		return integers.get(key);
	}

	public String getPrsFull() {
		return prsFull;
	}

	public short getPrsID() {
		return prsID;
	}

	public String getPrsName() {
		return prsName;
	}

	public void setCount(int value) {
		count = value;
	}

	public void setDouble(Short key, Double value) {
		if (doubles.get(key) == null) {
			doubles.put(key, value);
		} else {
			doubles.replace(key, doubles.get(key) + value);
		}
	}

	public void setFte(double value) {
		fte = value;
	}

	public void setInteger(Short key, Integer value) {
		if (integers.get(key) == null) {
			integers.put(key, value);
		} else {
			integers.replace(key, integers.get(key) + value);
		}
	}

	public void setPrsFull(String value) {
		prsFull = value;
	}

	public void setPrsID(short value) {
		prsID = value;
	}

	public void setPrsName(String value) {
		prsName = value;
	}
}