package ca.powerj.data;
import java.util.ArrayList;

public class ForecastList {
	private short id = 0;
	private String name = "";
	private int[] specimens;
	private int[] blocks;
	private int[] slides;
	private double[] fte1;
	private double[] fte2;
	private double[] fte3;
	private double[] fte4;
	private double[] fte5;
	private ArrayList<ForecastList> children = new ArrayList<ForecastList>();

	public ForecastList(String name, byte noYears, short id) {
		this.id = id;
		this.name = name;
		this.specimens = new int[noYears];
		this.blocks = new int[noYears];
		this.slides = new int[noYears];
		this.fte1 = new double[noYears];
		this.fte2 = new double[noYears];
		this.fte3 = new double[noYears];
		this.fte4 = new double[noYears];
		this.fte5 = new double[noYears];
	}

	public void addChild(ForecastList child) {
		children.add(child);
	}

	public int[] getBlocks() {
		return blocks;
	}

	public int getBlocks(int index) {
		return blocks[index];
	}

	public ForecastList getChild(int index) {
		return children.get(index);
	}

	public double[] getFte1() {
		return fte1;
	}

	public double getFte1(int index) {
		return fte1[index];
	}

	public double[] getFte2() {
		return fte2;
	}

	public double getFte2(int index) {
		return fte2[index];
	}

	public double[] getFte3() {
		return fte3;
	}

	public double getFte3(int index) {
		return fte3[index];
	}

	public double[] getFte4() {
		return fte4;
	}

	public double getFte4(int index) {
		return fte4[index];
	}

	public double[] getFte5() {
		return fte5;
	}

	public double getFte5(int index) {
		return fte5[index];
	}

	public short getId() {
		return id;
	}

	public int getNoChildren() {
		return children.size();
	}

	public int getNoYears() {
		return fte1.length;
	}

	public String getName() {
		return name;
	}

	public int[] getSlides() {
		return slides;
	}

	public int getSlides(int index) {
		return slides[index];
	}

	public int[] getSpecimens() {
		return specimens;
	}

	public int getSpecimens(int index) {
		return specimens[index];
	}

	public void removeChild(int index) {
		children.remove(index);
	}

	public void setBlocks(int index, int value) {
		blocks[index] = value;
	}

	public void setFte1(int index, double value) {
		fte1[index] = value;
	}

	public void setFte2(int index, double value) {
		fte2[index] = value;
	}

	public void setFte3(int index, double value) {
		fte3[index] = value;
	}

	public void setFte4(int index, double value) {
		fte4[index] = value;
	}

	public void setFte5(int index, double value) {
		fte5[index] = value;
	}

	public void setSlides(int index, int value) {
		slides[index] = value;
	}

	public void setSpecimens(int index, int value) {
		specimens[index] = value;
	}

	@Override
	public String toString() {
		return name;
	}
}