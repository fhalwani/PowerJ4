package ca.powerj.data;
import java.util.ArrayList;

public class WorkloadList {
	private short id = 0;
	private int noBlocks = 0;
	private int noCases = 0;
	private int noSlides = 0;
	private int noSpecs = 0;
	private double fte1 = 0;
	private double fte2 = 0;
	private double fte3 = 0;
	private double fte4 = 0;
	private double fte5 = 0;
	private String name = "";
	private ArrayList<WorkloadList> children = new ArrayList<WorkloadList>();

	public WorkloadList getChild(int index) {
		return children.get(index);
	}

	public ArrayList<WorkloadList> getChildren() {
		return children;
	}

	public double getFte1() {
		return fte1;
	}

	public double getFte2() {
		return fte2;
	}

	public double getFte3() {
		return fte3;
	}

	public double getFte4() {
		return fte4;
	}

	public double getFte5() {
		return fte5;
	}

	public short getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getNoBlocks() {
		return noBlocks;
	}

	public int getNoCases() {
		return noCases;
	}

	public int getNoChildren() {
		return children.size();
	}

	public int getNoSlides() {
		return noSlides;
	}

	public int getNoSpecs() {
		return noSpecs;
	}

	public void setChild(WorkloadList value) {
		this.children.add(value);
	}

	public void setChildren(ArrayList<WorkloadList> value) {
		this.children = value;
	}

	public void setFte1(double value) {
		this.fte1 = value;
	}

	public void setFte2(double value) {
		this.fte2 = value;
	}

	public void setFte3(double value) {
		this.fte3 = value;
	}

	public void setFte4(double value) {
		this.fte4 = value;
	}

	public void setFte5(double value) {
		this.fte5 = value;
	}

	public void setId(short value) {
		this.id = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNoBlocks(int value) {
		this.noBlocks = value;
	}

	public void setNoCases(int value) {
		this.noCases = value;
	}

	public void setNoSlides(int value) {
		this.noSlides = value;
	}

	public void setNoSpecs(int value) {
		this.noSpecs = value;
	}
}