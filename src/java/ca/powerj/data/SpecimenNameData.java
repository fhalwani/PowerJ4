package ca.powerj.data;

public class SpecimenNameData {
	private String procedure = "";
	private String specialty = "";
	private String subspecial = "";

	public String getProcedure() {
		return procedure;
	}

	public String getSpecialty() {
		return specialty;
	}

	public String getSubspecial() {
		return subspecial;
	}

	public void setProcedure(String value) {
		this.procedure = value;
	}

	public void setSpecialty(String value) {
		this.specialty = value;
	}

	public void setSubspecial(String value) {
		this.subspecial = value;
	}
}