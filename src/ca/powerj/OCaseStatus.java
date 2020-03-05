package ca.powerj;

class OCaseStatus {
	// Cases status ordered by hierarchy
	static final byte ID_ACCES = 0;
	static final byte ID_GROSS = 1;
	static final byte ID_EMBED = 2;
	static final byte ID_MICRO = 3;
	static final byte ID_ROUTE = 4;
	static final byte ID_DIAGN = 5;
	static final byte ID_FINAL = 6;
	static final byte ID_HISTO = 7;
	static final byte ID_ALL   = 8;
	static final String[] NAMES_ALL = {
			"Accession", "Gross", "Embeded",
			"Microtomy", "Routed", "Diagnosis",
			"Final", "Histology"};
}