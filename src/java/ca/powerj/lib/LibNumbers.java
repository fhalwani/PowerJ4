package ca.powerj.lib;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class LibNumbers {
	private int noFractions = 3;
	private NumberFormat ftNumber;
	private DecimalFormat ftDouble;

	LibNumbers() {
		ftNumber = NumberFormat.getNumberInstance();
		ftDouble = (DecimalFormat) NumberFormat.getNumberInstance();
		ftDouble.setMaximumFractionDigits(noFractions);
		ftDouble.setMinimumFractionDigits(noFractions);
		// Use Banker's rounding method
		ftDouble.setRoundingMode(RoundingMode.HALF_EVEN);
	}
	
	public int booleanToInt(boolean[] bits) {
		int value = 0;
		for (byte i = 0; i < 32; i++) {
			if (bits[i]) {
				value |= (1 << i);
			}
		}
		return value;
	}

	public short booleanToShort(boolean[] bits) {
		short value = 0;
		for (byte i = 0; i < 16; i++) {
			if (bits[i]) {
				value |= (1 << i);
			}
		}
		return value;
	}

	public int byteToInt(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException();
		}
		return byteToInt(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	public int byteToInt(byte byte1, byte byte2, byte byte3, byte byte4) {
		int value = byte1 << 24 |
			(byte2 & 0xFF) << 16 |
			(byte3 & 0xFF) << 8 |
			(byte4 & 0xFF);
		return value;
	}

	public int ceiling(int x, int y) {
		// Dividing 2 integers always rounds down, ie, 4/3 = 1
		// But we need ceiling, not floor, ie, 4/3 = 2
		int counter = 0;
		if (y > 0) {
			counter = x/y;
			if (x % y > 0) {
				counter++;
			}
		}
		return counter;
	}
	
	public int doubleToInt(double value) {
		if (value > 0) {
	        return (int) (value + 0.5);
	    } else {
	        return (int) (value - 0.5);
	    }
	}

	public int doubleToInt(int decimals, double value) {
		int i = 0;
		switch (decimals) {
		case 1:
			i = (int) ((value + 0.5) * 10);
			i = i /10;
			break;
		case 2:
			i = (int) ((value + 0.05) * 100);
			i = i /100;
			break;
		default:
			i = (int) ((value + 0.005) * 1000);
			i = i /1000;
		}
		return i;
	}

	public String formatDouble(int fractions, double value) {
		if (noFractions != fractions) {
			noFractions = fractions;
			ftDouble.setMaximumFractionDigits(noFractions);
			ftDouble.setMinimumFractionDigits(noFractions);
		}
		return ftDouble.format(value);
	}
	
	public String formatNumber(long value) {
		return ftNumber.format(value);
	}

	public boolean[] intToBoolean(int value) {
		boolean[] bits = new boolean[32];
		for (byte i = 0; i < 32; i++) {
			bits[i] = (value & (1 << i)) != 0;
		}
		return bits;
	}

	public byte[] intToBytes(int value) {
		byte[] aBytes = new byte[] { 
	        (byte) (value >> 24),
	        (byte) (value >> 16),
	        (byte) (value >> 8),
	        (byte) value };
		return aBytes;
	}

	public double intToDouble(int i, int value) {
		double d = 0;
		switch (i) {
		case 1:
			d = (1.0 * value / 10);
			break;
		case 2:
			d = (1.0 * value / 100);
			break;
		default:
			d = (1.0 * value / 1000);
		}
		return d;
	}

	public double minMax(double value, double min, double max) {
		// Unlinked: Min <= value <= Max
		double result = value;
		if (result < min) {
			result = min;
		}
		if (max > 0 && result > max) {
			result = max;
		}
		return result;
	}

	public double minMax(double value, double accumulated, double expected, double min, double max) {
		// Linked: Min <= value+accumulated <= Max
		double result = 0;
		if (max > 0 && value + accumulated > max) {
			// Max
			result = max - accumulated;
		} else if (value + accumulated < min) {
			// Min
			result = min - accumulated;
		} else if (value + accumulated > expected) {
			// Adjustment for Min
			result = expected - accumulated;
			if (result < 0) result = 0;
		} else {
			result = value;
		}
		return result;
	}

	public double parseDouble(String value) {
		try {
			return ftDouble.parse(value).doubleValue();
		} catch (ParseException e) {
			return 0d;
		}
	}

	public byte parseByte(String value) {
		try {
			return ftNumber.parse(value).byteValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	public int parseInt(String value) {
		try {
			return ftNumber.parse(value).intValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	public long parseLong(String value) {
		try {
			return ftNumber.parse(value).longValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	public short parseShort(String value) {
		try {
			return ftNumber.parse(value).shortValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	public boolean[] shortToBoolean(short value) {
		boolean[] bits = new boolean[16];
		for (byte i = 0; i < 16; i++) {
			bits[i] = (value & (1 << i)) != 0;
		}
		return bits;
	}
}