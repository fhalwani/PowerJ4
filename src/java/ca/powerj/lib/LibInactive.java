package ca.powerj.lib;
import ca.powerj.data.CaseData;
import ca.powerj.data.SpecimenData;

// Blank coder for deactivated coders
public class LibInactive {
	double dValue = 0;
	String name = "";

	public LibInactive(String name) {
		this.name = name;
    }

	void addOrder(short orderID, short groupID, short codeID, short qty,
			boolean isRoutine, boolean isAddlBlock, int specimenNo) {
	}

	void addSpecimen(SpecimenData thisSpecimen, short codeBenign, short codeMalignant, short codeRadical) {}

	void checkSpecimens() {}

	void close() {}

	void codeCase() {}

	double getAddl(short codeID, short qty) {
		return dValue;
	}

	String getComment() {
		return (name + "\n" + "--------------------------\n");
	}

	double getCorrelations() {
		return dValue;
	}

	double getFrozen() {
		return dValue;
	}

	double getFrozen(int specimenNo) {
		return dValue;
	}

	double getOrder(int specimenNo, short groupID) {
		return dValue;
	}

	double getValue() {
		return dValue;
	}

	double getValue(int specimenNo) {
		return dValue;
	}

	boolean hasComment() {
		return false;
	}

	boolean hasError() {
		return false;
	}

	boolean isOrderUnique(short orderID) {
		return true;
	}

	boolean needsFragments(int specimenNo) {
		return false;
	}

	void newCase(CaseData thisCase) {
	}
}
