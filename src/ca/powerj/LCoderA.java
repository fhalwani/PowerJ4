package ca.powerj;
// Blank coder for deactivated coders
class LCoderA {
	double dValue = 0;
	String coderName = "";

	LCoderA(LBase pj, int coderID) {
		switch (coderID) {
		case 1:
			coderName = pj.setup.getString(LSetup.VAR_CODER1_NAME);
			break;
		case 2:
			coderName = pj.setup.getString(LSetup.VAR_CODER2_NAME);
			break;
		case 3:
			coderName = pj.setup.getString(LSetup.VAR_CODER3_NAME);
			break;
		default:
			coderName = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		}
    }

	void addOrder(short orderID, short groupID, short codeID, short qty,
			boolean isRoutine, boolean isAddlBlock, int specimenNo) {
	}

	void addSpecimen(byte procedureID, short codeBenign, short codeMalignant, short codeRadical) {}
	void checkSpecimens() {}
	void close() {}
	void codeCase() {}

	double getAddl(short codeID, short qty) {
		return dValue;
	}

	String getComment() {
		return (coderName + "\n" + "--------------------------\n");
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

	void newCase(OCaseFinal thisCase) {
	}
}
