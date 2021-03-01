package ca.powerj.data;
import java.util.HashMap;
import ca.powerj.database.DBPowerj;

public class AccessionsList {
	private short accID = 0;
	private AccessionData accession = new AccessionData();
	private HashMap<Short, AccessionData> accessions = new HashMap<Short, AccessionData>();

	public AccessionsList(DBPowerj dbPowerJ) {
		accessions = dbPowerJ.getAccessions();
	}

	public void close() {
		accessions.clear();
	}

	public boolean codeSpecimen() {
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.isCodeSpec();
		}
		return false;
	}

	public boolean doworkflow(short id) {
		if (accID != id) {
			accession = accessions.get(id);
			accID = id;
		}
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.isWorkflow();
		}
		return false;
	}

	public boolean doWorkload(short id) {
		if (accID != id) {
			accession = accessions.get(id);
			accID = id;
		}
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.isWorkload();
		}
		return false;
	}

	public byte getSpecialty() {
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.getSpyID();
		}
		return 0;
	}
}