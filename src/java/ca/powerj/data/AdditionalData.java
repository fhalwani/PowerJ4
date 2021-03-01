package ca.powerj.data;
import java.util.Calendar;

public class AdditionalData {
	private int value5 = 0;
	private double value1 = 0;
	private double value2 = 0;
	private double value3 = 0;
	private double value4 = 0;
	private String code = "";
	private String finalName = "";
	private String finalFull = "";
	private Calendar finaled = Calendar.getInstance();

	public String getCode() {
		return code;
	}

	public Calendar getFinaled() {
		return finaled;
	}

	public String getFinalFull() {
		return finalFull;
	}

	public String getFinalName() {
		return finalName;
	}

	public double getValue1() {
		return value1;
	}

	public double getValue2() {
		return value2;
	}

	public double getValue3() {
		return value3;
	}

	public double getValue4() {
		return value4;
	}

	public int getValue5() {
		return value5;
	}

	public void setCode(String value) {
		this.code = value;
	}

	public void setFinaled(long value) {
		this.finaled.setTimeInMillis(value);
	}

	public void setFinalFull(String value) {
		this.finalFull = value;
	}

	public void setFinalName(String value) {
		this.finalName = value;
	}

	public void setValue1(double value) {
		this.value1 = value;
	}

	public void setValue2(double value) {
		this.value2 = value;
	}

	public void setValue3(double value) {
		this.value3 = value;
	}

	public void setValue4(double value) {
		this.value4 = value;
	}

	public void setValue5(int value) {
		this.value5 = value;
	}

	@Override
	public String toString() {
		return code;
	}
}