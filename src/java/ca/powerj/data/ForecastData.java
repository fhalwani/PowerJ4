package ca.powerj.data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ForecastData {
	private byte proID = 0;
	private byte spyID = 0;
	private byte subID = 0;
	private short facID = 0;
	private short spgID = 0;
	private String facName = "";
	private String proName = "";
	private String spgName = "";
	private String spyName = "";
	private String subName = "";
	private HashMap<Short, Integer> noSpecs = new HashMap<Short, Integer>();
	private HashMap<Short, Integer> noBlocks = new HashMap<Short, Integer>();
	private HashMap<Short, Integer> noSlides = new HashMap<Short, Integer>();
	private HashMap<Short, Double> fte1 = new HashMap<Short, Double>();
	private HashMap<Short, Double> fte2 = new HashMap<Short, Double>();
	private HashMap<Short, Double> fte3 = new HashMap<Short, Double>();
	private HashMap<Short, Double> fte4 = new HashMap<Short, Double>();
	private HashMap<Short, Double> fte5 = new HashMap<Short, Double>();

	public short getFacID() {
		return facID;
	}

	public String getFacName() {
		return facName;
	}

	public double getFte1(short key) {
		if (fte1.get(key) == null) {
			return 0;
		} else {
			return fte1.get(key);
		}
	}

	public double getFte2(short key) {
		if (fte2.get(key) == null) {
			return 0;
		} else {
			return fte2.get(key);
		}
	}

	public double getFte3(short key) {
		if (fte3.get(key) == null) {
			return 0;
		} else {
			return fte3.get(key);
		}
	}

	public double getFte4(short key) {
		if (fte4.get(key) == null) {
			return 0;
		} else {
			return fte4.get(key);
		}
	}

	public double getFte5(short key) {
		if (fte5.get(key) == null) {
			return 0;
		} else {
			return fte5.get(key);
		}
	}

	public int getNoBlocks(short key) {
		if (noBlocks.get(key) == null) {
			return 0;
		} else {
			return noBlocks.get(key);
		}
	}

	public int getNoSlides(short key) {
		if (noSlides.get(key) == null) {
			return 0;
		} else {
			return noSlides.get(key);
		}
	}

	public int getNoSpecs(short key) {
		if (noSpecs.get(key) == null) {
			return 0;
		} else {
			return noSpecs.get(key);
		}
	}

	public byte getProID() {
		return proID;
	}

	public String getProName() {
		return proName;
	}

	public short getSpgID() {
		return spgID;
	}

	public String getSpgName() {
		return spgName;
	}

	public byte getSpyID() {
		return spyID;
	}

	public String getSpyName() {
		return spyName;
	}

	public byte getSubID() {
		return subID;
	}

	public String getSubName() {
		return subName;
	}

	public ArrayList<Short> getYears() {
		ArrayList<Short> years = new ArrayList<Short>();
		for (Map.Entry entry : noSpecs.entrySet()) {
			years.add((Short) entry.getKey());
		}
		return years;
	}

	public void setFacID(short value) {
		facID = value;
	}

	public void setFacName(String value) {
		facName = value;
	}

	public void setFte1(short key, double value) {
		if (fte1.get(key) == null) {
			fte1.put(key, value);
		} else {
			fte1.replace(key, fte1.get(key) + value);
		}
	}

	public void setFte2(short key, double value) {
		if (fte2.get(key) == null) {
			fte2.put(key, value);
		} else {
			fte2.replace(key, fte2.get(key) + value);
		}
	}

	public void setFte3(short key, double value) {
		if (fte3.get(key) == null) {
			fte3.put(key, value);
		} else {
			fte3.replace(key, fte3.get(key) + value);
		}
	}

	public void setFte4(short key, double value) {
		if (fte4.get(key) == null) {
			fte4.put(key, value);
		} else {
			fte4.replace(key, fte4.get(key) + value);
		}
	}

	public void setFte5(short key, double value) {
		if (fte5.get(key) == null) {
			fte5.put(key, value);
		} else {
			fte5.replace(key, fte5.get(key) + value);
		}
	}

	public void setNoBlocks(short key, int value) {
		if (noBlocks.get(key) == null) {
			noBlocks.put(key, value);
		} else {
			noBlocks.replace(key, noBlocks.get(key) + value);
		}
	}

	public void setNoSlides(short key, int value) {
		if (noSlides.get(key) == null) {
			noSlides.put(key, value);
		} else {
			noSlides.replace(key, noSlides.get(key) + value);
		}
	}

	public void setNoSpecs(short key, int value) {
		if (noSpecs.get(key) == null) {
			noSpecs.put(key, value);
		} else {
			noSpecs.replace(key, noSpecs.get(key) + value);
		}
	}

	public void setProID(byte value) {
		proID = value;
	}

	public void setProName(String value) {
		proName = value;
	}

	public void setSpgID(short value) {
		spgID = value;
	}

	public void setSpgName(String value) {
		spgName = value;
	}

	public void setSpyID(byte value) {
		spyID = value;
	}

	public void setSpyName(String value) {
		spyName = value;
	}

	public void setSubID(byte value) {
		subID = value;
	}

	public void setSubName(String value) {
		subName = value;
	}

	@Override
	public String toString() {
		return spgName;
	}
}