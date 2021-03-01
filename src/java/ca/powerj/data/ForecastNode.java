package ca.powerj.data;

public class ForecastNode {
	private int[] blocks;
	private int[] slides;
	private int[] specimens;
	private int[] blocksForecast;
	private int[] slidesForecast;
	private int[] specimensForecast;
	private double[] fte1;
	private double[] fte2;
	private double[] fte3;
	private double[] fte4;
	private double[] fte5;
	private double[] fte1Forecast;
	private double[] fte2Forecast;
	private double[] fte3Forecast;
	private double[] fte4Forecast;
	private double[] fte5Forecast;
	private String name = "";
	private Object[] children;

	public int[] getBlocks() {
		return blocks;
	}

	public int getBlocks(int index) {
		return blocks[index];
	}

	public int[] getBlocksf() {
		return blocksForecast;
	}

	public int getBlocksf(int index) {
		return blocksForecast[index];
	}

	public Object getChild(int index) {
		return children[index];
	}

	public double[] getFte1() {
		return fte1;
	}

	public double getFte1(int index) {
		return fte1[index];
	}

	public double[] getFte1f() {
		return fte1Forecast;
	}

	public double getFte1f(int index) {
		return fte1Forecast[index];
	}

	public double[] getFte2() {
		return fte2;
	}

	public double getFte2(int index) {
		return fte2[index];
	}

	public double[] getFte2f() {
		return fte2Forecast;
	}

	public double getFte2f(int index) {
		return fte2Forecast[index];
	}

	public double[] getFte3() {
		return fte3;
	}

	public double getFte3(int index) {
		return fte3[index];
	}

	public double[] getFte3f() {
		return fte3Forecast;
	}

	public double getFte3f(int index) {
		return fte3Forecast[index];
	}

	public double[] getFte4() {
		return fte4;
	}

	public double getFte4(int index) {
		return fte4[index];
	}

	public double[] getFte4f() {
		return fte4Forecast;
	}

	public double getFte4f(int index) {
		return fte4Forecast[index];
	}

	public double[] getFte5() {
		return fte5;
	}

	public double getFte5(int index) {
		return fte5[index];
	}

	public double[] getFte5f() {
		return fte5Forecast;
	}

	public double getFte5f(int index) {
		return fte5Forecast[index];
	}

	public String getName() {
		return name;
	}

	public int getNoChildren() {
		if (children != null) {
			return children.length;
		}
		return 0;
	}

	public int[] getSlides() {
		return slides;
	}

	public int getSlides(int index) {
		return slides[index];
	}

	public int[] getSlidesf() {
		return slidesForecast;
	}

	public int getSlidesf(int index) {
		return slidesForecast[index];
	}

	public int[] getSpecimens() {
		return specimens;
	}

	public int getSpecimens(int index) {
		return specimens[index];
	}

	public int[] getSpecsf() {
		return specimensForecast;
	}

	public int getSpecimensf(int index) {
		return specimensForecast[index];
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setSpecimens(int[] value) {
		this.specimens = value;
	}

	public void setBlocks(int[] value) {
		this.blocks = value;
	}

	public void setSlides(int[] value) {
		this.slides = value;
	}

	public void setFte1(double[] value) {
		this.fte1 = value;
	}

	public void setFte2(double[] value) {
		this.fte2 = value;
	}

	public void setFte3(double[] value) {
		this.fte3 = value;
	}

	public void setFte4(double[] value) {
		this.fte4 = value;
	}

	public void setFte5(double[] value) {
		this.fte5 = value;
	}

	public void setSpecsf(int[] value) {
		this.specimensForecast = value;
	}

	public void setBlocksf(int[] value) {
		this.blocksForecast = value;
	}

	public void setSlidesf(int[] value) {
		this.slidesForecast = value;
	}

	public void setFte1f(double[] value) {
		this.fte1Forecast = value;
	}

	public void setFte2f(double[] value) {
		this.fte2Forecast = value;
	}

	public void setFte3f(double[] value) {
		this.fte3Forecast = value;
	}

	public void setFte4f(double[] value) {
		this.fte4Forecast = value;
	}

	public void setFte5f(double[] value) {
		this.fte5Forecast = value;
	}

	public void setChild(int key, Object value) {
		children[key] = value;
	}

	public void setChildren(int value) {
		children = new Object[value];
	}

	@Override
	public String toString() {
		return name;
	}
}