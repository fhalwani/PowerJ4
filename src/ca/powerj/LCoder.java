package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

class LCoder extends LCoderA {
	private final short RULE_SYNOPTICS = 2;
	private final short RULE_FROZENS = 3;
	private final short RULE_FROZENS_BLOCKS = 4;
	private final short RULE_CORRELATIONS = 5;
	private final double MAX_VALUE = 99.9;
	private String comment = "";
	private OCaseFinal thisCase = new OCaseFinal();
	private OCaseCode caseCoder = new OCaseCode();
	private OSpecFinal thisSpecimen = new OSpecFinal();
	private OSpecCode specimenCoder = new OSpecCode();
	private OOrderCode ordersCoder = new OOrderCode();
	private OWorkcode wcode = new OWorkcode();
	private HashMap<Short, OWorkcode> masterCodes = new HashMap<Short, OWorkcode>();
	private ArrayList<OSpecCode> specimens = new ArrayList<OSpecCode>();
	private LBase pj;

	LCoder(LBase pj, PreparedStatement pstm, int coderID) {
		super(pj, coderID);
		this.pj = pj;
		readTables(pstm);
	}

	@Override
	void addOrder(short orderID, short groupID, short codeID, short qty, boolean isRoutine, boolean isAddlBlock,
			int specimenNo) {
		if (caseCoder.inclusive) {
			return;
		}
		wcode = masterCodes.get(codeID);
		if (wcode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = LConstants.ERROR_ORDER_UNKNOWN;
			comment = "ERROR: addOrder, " + thisCase.caseNo + ", Specimen " + specimenNo + ", Order " + codeID + ", "
					+ LConstants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + "\n";
			pj.log(caseCoder.errorID, coderName, comment);
			return;
		}
		switch (wcode.ruleID) {
		case ORule.RULE_UNIQUE_CASE_INCLUSIVE:
		case ORule.RULE_GROUP_CASE_INCLUSIVE:
		case ORule.RULE_AFTER_CASE_INCLUSIVE:
			thisSpecimen = thisCase.lstSpecimens.get(0);
			specimenCoder = specimens.get(0);
			break;
		default:
			thisSpecimen = thisCase.lstSpecimens.get(specimenNo);
			specimenCoder = specimens.get(specimenNo);
		}
		if (specimenCoder.inclusive) {
			return;
		}
		if (isAddlBlock && (!specimenCoder.codeBlocks || !caseCoder.codeBlocks)) {
			return;
		}
		ordersCoder = specimenCoder.lstOrders.get(groupID);
		if (ordersCoder == null) {
			ordersCoder = new OOrderCode();
			ordersCoder.codeID = codeID;
			ordersCoder.isAddlBlock = isAddlBlock;
			ordersCoder.name = wcode.name;
			specimenCoder.lstOrders.put(groupID, ordersCoder);
		} else if (isAddlBlock) {
			// Avoid changing it to false
			ordersCoder.isAddlBlock = true;
		}
		switch (wcode.ruleID) {
		case ORule.RULE_CASE_INCLUSIVE:
		case ORule.RULE_GROUP_CASE_INCLUSIVE:
		case ORule.RULE_SPECIMEN_INCLUSIVE:
		case ORule.RULE_GROUP_SPECIMEN_INCLUSIVE:
			ordersCoder.qty = 1;
			break;
		case ORule.RULE_UNIQUE_CASE_INCLUSIVE:
		case ORule.RULE_UNIQUE_SPECIMEN_INCLUSIVE:
			if (isOrderUnique(orderID)) {
				ordersCoder.qty += 1;
			}
			break;
		case ORule.RULE_UNIQUE_EVERY_X_MIN_MAX:
		case ORule.RULE_UNIQUE_1_2_X:
		case ORule.RULE_UNIQUE_1_2_PLUSX:
			// CPT
			if (isOrderUnique(orderID)) {
				ordersCoder.qty += qty;
			}
			break;
		case ORule.RULE_AFTER_CASE_INCLUSIVE:
		case ORule.RULE_AFTER_SPECIMEN_INCLUSIVE:
			// Once per case if ordered after routing
			if (!isRoutine) {
				ordersCoder.qty = 1;
			}
			break;
		case ORule.RULE_AFTER_EVERY_X_MIN_MAX:
		case ORule.RULE_AFTER_1_2_X:
		case ORule.RULE_AFTER_1_2_PLUSX:
			// CAP
			if (!isRoutine) {
				ordersCoder.qty += qty;
			}
			break;
		case ORule.RULE_GROUP_EVERY_X_MIN_MAX:
		case ORule.RULE_GROUP_1_2_X:
		case ORule.RULE_GROUP_1_2_PLUSX:
			// W2Q
			ordersCoder.qty += qty;
			break;
		default:
			// Ignore RCP
			ordersCoder.qty = 0;
		}
	}

	@Override
	void addSpecimen(byte procedureID, short codeBenign, short codeMalignant, short codeRadical) {
		thisSpecimen = thisCase.lstSpecimens.get(thisCase.noSpec - 1);
		specimenCoder = new OSpecCode();
		specimenCoder.procID = procedureID;
		specimenCoder.coderID = codeBenign;
		specimens.add(specimenCoder);
		if (thisCase.noSynop > 0) {
			specimenCoder.coderMalig = codeMalignant;
			specimenCoder.coderRadical = codeRadical;
		}
		if (!thisCase.codeSpec) {
			// Molecular
			specimenCoder.codeBlocks = false;
			return;
		}
		wcode = masterCodes.get(specimenCoder.coderID);
		if (wcode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = LConstants.ERROR_SPECIMEN_UNKNOWN;
			comment = "ERROR: addSpecimen, " + thisCase.caseNo + ", Specimen " + thisCase.noSpec + ", Coder "
					+ specimenCoder.coderID + ", " + LConstants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + "\n";
			pj.log(caseCoder.errorID, coderName, comment);
			return;
		}
		switch (wcode.ruleID) {
		case ORule.RULE_CASE_INCLUSIVE:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			caseCoder.codeBlocks = false;
			caseCoder.inclusive = true;
			break;
		case ORule.RULE_CASE_FRAGS_X_MIN_MAX:
		case ORule.RULE_CASE_FRAGS_1_2_X:
		case ORule.RULE_CASE_FRAGS_1_2_PLUSX:
		case ORule.RULE_CASE_FRAGS_BLOCKS:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			caseCoder.codeBlocks = false;
			thisCase.needFrag = true;
			break;
		case ORule.RULE_CASE_GROSS_MICRO:
		case ORule.RULE_CASE_BLOCKS_X_MIN_MAX:
		case ORule.RULE_CASE_BLOCKS_1_2_X:
		case ORule.RULE_CASE_BLOCKS_1_2_PLUSX:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			caseCoder.codeBlocks = false;
			break;
		case ORule.RULE_CASE_FIXED:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			break;
		case ORule.RULE_SPECIMEN_INCLUSIVE:
		case ORule.RULE_LINKED_INCLUSIVE:
			specimenCoder.inclusive = true;
			specimenCoder.codeBlocks = false;
			break;
		case ORule.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
		case ORule.RULE_SPECIMEN_FRAGS_1_2_X:
		case ORule.RULE_SPECIMEN_FRAGS_1_2_PLUSX:
		case ORule.RULE_SPECIMEN_FRAGS_BLOCKS:
		case ORule.RULE_LINKED_FRAGS_X_MIN_MAX:
		case ORule.RULE_LINKED_FRAGS_1_2_X:
		case ORule.RULE_LINKED_FRAGS_1_2_PLUSX:
		case ORule.RULE_LINKED_FRAGS_BLOCKS:
			// CAP GI Polyps, skin resections
			thisCase.needFrag = true;
			specimenCoder.needFrag = true;
			specimenCoder.codeBlocks = false;
			break;
		case ORule.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
		case ORule.RULE_SPECIMEN_BLOCKS_1_2_X:
		case ORule.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
		case ORule.RULE_LINKED_BLOCKS_X_MIN_MAX:
		case ORule.RULE_LINKED_BLOCKS_1_2_X:
		case ORule.RULE_LINKED_BLOCKS_1_2_PLUSX:
			specimenCoder.codeBlocks = false;
			break;
		case ORule.RULE_SPECIMEN_FIXED:
		case ORule.RULE_SPECIMEN_GROSS_MICRO:
		case ORule.RULE_SPECIMEN_EVERY_X_MIN_MAX:
		case ORule.RULE_SPECIMEN_1_2_X:
		case ORule.RULE_SPECIMEN_1_2_PLUSX:
		case ORule.RULE_LINKED_FIXED:
		case ORule.RULE_LINKED_EVERY_X_MIN_MAX:
		case ORule.RULE_LINKED_1_2_X:
		case ORule.RULE_LINKED_1_2_PLUSX:
			break;
		default:
			// Nothing for specimen (molecular, Derm IF, Iron Quant)
		}
		caseCoder.comment += "Specimen " + thisCase.noSpec + ", Rule " + wcode.ruleID + ", Name: " + wcode.name
				+ ", AddlBlk: " + (specimenCoder.codeBlocks ? "T" : "F") + ", Frags: "
				+ (specimenCoder.needFrag ? "T" : "F") + "\n";
	}

	@Override
	void checkSpecimens() {
		for (short i = 0; i < thisCase.noSpec; i++) {
			thisSpecimen = thisCase.lstSpecimens.get(i);
			specimenCoder = specimens.get(i);
			if (thisCase.noSynop > 0) {
				// Malignant or radical case
				if (thisCase.noSynop > 1 || thisCase.hasLN) {
					// Radical is 2 synoptics or 1 synoptic + node dissection
					if (!caseCoder.isRadical) {
						wcode = masterCodes.get(specimenCoder.coderRadical);
						if (wcode != null) {
							if (wcode.ruleID > ORule.RULE_IGNORE) {
								// Use this specimen as first choice for coding malignancy
								caseCoder.mainSpec = thisCase.noSpec;
								thisCase.procID = specimenCoder.procID;
								thisCase.mainSpec = thisSpecimen.spmID;
								specimenCoder.coderID = specimenCoder.coderRadical;
								caseCoder.isRadical = true;
								break;
							}
						}
					}
				}
				if (!caseCoder.isRadical && !caseCoder.isMalignant) {
					wcode = masterCodes.get(specimenCoder.coderMalig);
					if (wcode != null) {
						if (wcode.ruleID > ORule.RULE_IGNORE) {
							// Use this specimen as first choice for coding malignancy
							caseCoder.mainSpec = thisCase.noSpec;
							thisCase.procID = specimenCoder.procID;
							thisCase.mainSpec = thisSpecimen.spmID;
							specimenCoder.coderID = specimenCoder.coderMalig;
							caseCoder.isMalignant = true;
							break;
						}
					}
				}
			}
		}
		wcode = masterCodes.get(specimenCoder.coderID);
		if (wcode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = LConstants.ERROR_SPECIMEN_UNKNOWN;
			comment = "ERROR: addSpecimen, " + thisCase.caseNo + ", Specimen " + thisCase.noSpec + ", Coder "
					+ specimenCoder.coderID + ", " + LConstants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + "\n";
			pj.log(caseCoder.errorID, coderName, comment);
			return;
		}
		switch (wcode.ruleID) {
		case ORule.RULE_CASE_INCLUSIVE:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			caseCoder.codeBlocks = false;
			caseCoder.inclusive = true;
			break;
		case ORule.RULE_CASE_FRAGS_X_MIN_MAX:
		case ORule.RULE_CASE_FRAGS_1_2_X:
		case ORule.RULE_CASE_FRAGS_1_2_PLUSX:
		case ORule.RULE_CASE_FRAGS_BLOCKS:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			caseCoder.codeBlocks = false;
			thisCase.needFrag = true;
			break;
		case ORule.RULE_CASE_GROSS_MICRO:
		case ORule.RULE_CASE_BLOCKS_X_MIN_MAX:
		case ORule.RULE_CASE_BLOCKS_1_2_X:
		case ORule.RULE_CASE_BLOCKS_1_2_PLUSX:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			caseCoder.codeBlocks = false;
			break;
		case ORule.RULE_CASE_FIXED:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpec = thisCase.noSpec;
			break;
		case ORule.RULE_SPECIMEN_INCLUSIVE:
		case ORule.RULE_LINKED_INCLUSIVE:
			specimenCoder.inclusive = true;
			specimenCoder.codeBlocks = false;
			break;
		case ORule.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
		case ORule.RULE_SPECIMEN_FRAGS_1_2_X:
		case ORule.RULE_SPECIMEN_FRAGS_1_2_PLUSX:
		case ORule.RULE_SPECIMEN_FRAGS_BLOCKS:
		case ORule.RULE_LINKED_FRAGS_X_MIN_MAX:
		case ORule.RULE_LINKED_FRAGS_1_2_X:
		case ORule.RULE_LINKED_FRAGS_1_2_PLUSX:
		case ORule.RULE_LINKED_FRAGS_BLOCKS:
			// CAP GI Polyps, skin resections
			thisCase.needFrag = true;
			specimenCoder.needFrag = true;
			specimenCoder.codeBlocks = false;
			break;
		case ORule.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
		case ORule.RULE_SPECIMEN_BLOCKS_1_2_X:
		case ORule.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
		case ORule.RULE_LINKED_BLOCKS_X_MIN_MAX:
		case ORule.RULE_LINKED_BLOCKS_1_2_X:
		case ORule.RULE_LINKED_BLOCKS_1_2_PLUSX:
			specimenCoder.codeBlocks = false;
			break;
		case ORule.RULE_SPECIMEN_FIXED:
		case ORule.RULE_SPECIMEN_GROSS_MICRO:
		case ORule.RULE_SPECIMEN_EVERY_X_MIN_MAX:
		case ORule.RULE_SPECIMEN_1_2_X:
		case ORule.RULE_SPECIMEN_1_2_PLUSX:
		case ORule.RULE_LINKED_FIXED:
		case ORule.RULE_LINKED_EVERY_X_MIN_MAX:
		case ORule.RULE_LINKED_1_2_X:
		case ORule.RULE_LINKED_1_2_PLUSX:
			break;
		default:
			// Nothing for specimen (molecular, Derm IF, Iron Quant)
		}
		caseCoder.comment += "Malignant/Radical Specimen " + thisCase.noSpec + ", Rule " + wcode.ruleID + ", "
				+ ", Name: " + wcode.name + ", AddlBlk: " + (specimenCoder.codeBlocks ? "T" : "F") + ", Frags: "
				+ (specimenCoder.needFrag ? "T" : "F") + "\n";
		for (int i = 0; i < thisCase.noSpec; i++) {
			thisSpecimen = thisCase.lstSpecimens.get(i);
			specimenCoder = specimens.get(i);
			for (Entry<Short, OOrderCode> orderEntry : specimenCoder.lstOrders.entrySet()) {
				ordersCoder = orderEntry.getValue();
				if (ordersCoder.isAddlBlock && (!specimenCoder.codeBlocks || !caseCoder.codeBlocks)) {
					ordersCoder.qty = 0;
				} else if (caseCoder.inclusive || specimenCoder.inclusive) {
					ordersCoder.qty = 0;
				}
			}
		}
	}

	@Override
	void close() {
		masterCodes.clear();
		specimens.clear();
	}

	@Override
	void codeCase() {
		if (thisCase.noSynop > 0) {
			checkSpecimens();
		}
		if (caseCoder.coderID > 0) {
			codeMain();
		} else if (thisCase.codeSpec) {
			// Else, Molecular case, only code orders
			codeSpecimen();
		}
		codeOrders();
		if (thisCase.noSynop > 0) {
			// W2Q adds workload for actual synoptics
			codeSynoptics();
		}
		if (thisCase.noFSSpec > 0) {
			codeFrozen();
		}
	}

	private void codeFrozen() {
		boolean caseFixed = false;
		boolean codeBlocks = true;
		double dLinks = 0, dExpect = 0;
		short noLinks = 0;
		wcode = masterCodes.get(RULE_FROZENS);
		if (wcode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = LConstants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: codeFrozen, " + thisCase.caseNo + ", " + LConstants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + "\n";
			pj.log(caseCoder.errorID, coderName, comment);
			return;
		}
		if (wcode.ruleID == ORule.RULE_IGNORE) {
			return;
		}
		for (int i = 0; i < thisCase.noSpec; i++) {
			if (caseFixed)
				break;
			thisSpecimen = thisCase.lstSpecimens.get(i);
			if (thisSpecimen.noFSBlks < 1 && thisSpecimen.noFSSlds < 1) {
				continue;
			}
			dValue = 0;
			switch (wcode.ruleID) {
			case ORule.RULE_CASE_INCLUSIVE:
			case ORule.RULE_LINKED_INCLUSIVE:
				specimenCoder.valueFS = wcode.valueA;
				caseFixed = true;
				codeBlocks = false;
				break;
			case ORule.RULE_CASE_FIXED:
			case ORule.RULE_LINKED_FIXED:
				specimenCoder.valueFS = wcode.valueA;
				caseFixed = true;
				break;
			case ORule.RULE_CASE_GROSS_MICRO:
				if (thisCase.noFSSlds > 0) {
					specimenCoder.valueFS = wcode.valueB;
				} else {
					specimenCoder.valueFS = wcode.valueA;
				}
				caseFixed = true;
				codeBlocks = false;
				break;
			case ORule.RULE_LINKED_EVERY_X_MIN_MAX:
			case ORule.RULE_SPECIMEN_EVERY_X_MIN_MAX:
				noLinks++;
				dExpect = wcode.valueA * pj.numbers.ceiling(noLinks, wcode.count);
				dValue = dExpect - dLinks;
				specimenCoder.valueFS = pj.numbers.minMax(dValue, dLinks, dExpect, wcode.valueB, wcode.valueC);
				dLinks += specimenCoder.valueFS;
				break;
			case ORule.RULE_LINKED_1_2_X:
			case ORule.RULE_SPECIMEN_1_2_X:
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.valueFS = wcode.valueA;
				} else if (noLinks == 2) {
					specimenCoder.valueFS = wcode.valueB;
				} else if (noLinks == wcode.count) {
					specimenCoder.valueFS = wcode.valueC;
				}
				break;
			case ORule.RULE_LINKED_1_2_PLUSX:
			case ORule.RULE_SPECIMEN_1_2_PLUSX:
				noLinks++;
				specimenCoder.valueFS = wcode.valueA;
				if (noLinks > 1) {
					specimenCoder.valueFS += wcode.valueB;
					if (noLinks > wcode.count) {
						specimenCoder.valueFS += wcode.valueC * (noLinks - wcode.count);
					}
				}
				break;
			case ORule.RULE_CASE_BLOCKS_X_MIN_MAX:
			case ORule.RULE_LINKED_BLOCKS_X_MIN_MAX:
				noLinks += thisSpecimen.noFSBlks;
				dExpect = wcode.valueA * pj.numbers.ceiling(noLinks, wcode.count);
				dValue = dExpect - dLinks;
				specimenCoder.valueFS = pj.numbers.minMax(dValue, dLinks, dExpect, wcode.valueB, wcode.valueC);
				dLinks += specimenCoder.valueFS;
				codeBlocks = false;
				break;
			case ORule.RULE_CASE_BLOCKS_1_2_X:
			case ORule.RULE_LINKED_BLOCKS_1_2_X:
				noLinks += thisSpecimen.noFSBlks;
				if (noLinks >= 1 && dLinks < wcode.valueA) {
					specimenCoder.valueFS += wcode.valueA;
				}
				if (noLinks >= 2 && dLinks < wcode.valueA + wcode.valueB) {
					specimenCoder.valueFS += wcode.valueB;
				}
				if (noLinks >= wcode.count && dLinks < wcode.valueA + wcode.valueB + wcode.valueC) {
					specimenCoder.valueFS += wcode.valueC;
				}
				dLinks += specimenCoder.valueFS;
				codeBlocks = false;
				break;
			case ORule.RULE_CASE_BLOCKS_1_2_PLUSX:
			case ORule.RULE_LINKED_BLOCKS_1_2_PLUSX:
				noLinks += thisSpecimen.noFSBlks;
				specimenCoder.valueFS = wcode.valueA;
				if (noLinks > 1 && dLinks < wcode.valueA + wcode.valueB) {
					specimenCoder.valueFS += wcode.valueB;
					if (noLinks > wcode.count
							&& dLinks < wcode.valueA + wcode.valueB + (wcode.valueC * (noLinks - wcode.count))) {
						specimenCoder.valueFS += (wcode.valueC * (noLinks - wcode.count));
					}
				}
				dLinks += specimenCoder.valueFS;
				codeBlocks = false;
				break;
			case ORule.RULE_SPECIMEN_INCLUSIVE:
				specimenCoder.valueFS = wcode.valueA;
				codeBlocks = false;
				break;
			case ORule.RULE_SPECIMEN_FIXED:
				specimenCoder.valueFS = wcode.valueA;
				break;
			case ORule.RULE_SPECIMEN_GROSS_MICRO:
				if (thisSpecimen.noFSSlds > 0) {
					specimenCoder.valueFS = wcode.valueB;
				} else {
					specimenCoder.valueFS = wcode.valueA;
				}
				break;
			case ORule.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
				dValue = wcode.valueA * pj.numbers.ceiling(thisSpecimen.noFSBlks, wcode.count);
				specimenCoder.valueFS = pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
				codeBlocks = false;
				break;
			case ORule.RULE_SPECIMEN_BLOCKS_1_2_X:
				if (thisSpecimen.noFSBlks >= 1) {
					specimenCoder.valueFS = wcode.valueA;
				}
				if (thisSpecimen.noFSBlks >= 2) {
					specimenCoder.valueFS += wcode.valueB;
				}
				if (thisSpecimen.noFSBlks >= wcode.count) {
					specimenCoder.valueFS += wcode.valueC;
				}
				codeBlocks = false;
				break;
			case ORule.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
				if (thisSpecimen.noFSBlks >= 1) {
					specimenCoder.valueFS = wcode.valueA;
					if (thisSpecimen.noFSBlks >= 2) {
						specimenCoder.valueFS += wcode.valueB;
						if (thisSpecimen.noFSBlks > wcode.count) {
							specimenCoder.valueFS += (wcode.valueC * (thisSpecimen.noFSBlks - wcode.count));
						}
					}
				}
				codeBlocks = false;
				break;
			default:
				caseCoder.hasError = true;
				caseCoder.errorID = LConstants.ERROR_CODING_RULE_UNKNOWN;
				comment = "ERROR: codeFrozen, " + thisCase.caseNo + ", " + LConstants.ERROR_STRINGS[caseCoder.errorID];
				caseCoder.comment += comment + "\n";
				pj.log(caseCoder.errorID, coderName, comment);
			}
			caseCoder.valueFS += specimenCoder.valueFS;
			if (codeBlocks && thisSpecimen.noFSBlks > 0) {
				codeBlocks = codeFrozenBlocks();
				wcode = masterCodes.get(RULE_FROZENS);
			}
			caseCoder.comment += "Frozen Sections: Specimen " + (i + 1) + ", Blocks " + thisSpecimen.noFSBlks
					+ ", Value: " + pj.numbers.formatDouble(3, specimenCoder.valueFS) + ", Case: "
					+ pj.numbers.formatDouble(3, caseCoder.valueFS) + "\n";
		}
	}

	private boolean codeFrozenBlocks() {
		boolean codeBlocks = true;
		// Same specimen as codeFrozen
		dValue = 0;
		wcode = masterCodes.get(RULE_FROZENS_BLOCKS);
		if (wcode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = LConstants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: codeFrozenBlocks, " + thisCase.caseNo + ", "
					+ LConstants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + "\n";
			pj.log(caseCoder.errorID, coderName, comment);
			codeBlocks = false;
		} else {
			switch (wcode.ruleID) {
			case ORule.RULE_IGNORE:
				codeBlocks = false;
				dValue = 0;
				break;
			case ORule.RULE_CASE_BLOCKS_X_MIN_MAX:
			case ORule.RULE_LINKED_BLOCKS_X_MIN_MAX:
			case ORule.RULE_LINKED_EVERY_X_MIN_MAX:
				dValue = wcode.valueA * pj.numbers.ceiling(thisCase.noFSBlks, wcode.count);
				dValue = pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
				codeBlocks = false;
				break;
			case ORule.RULE_CASE_BLOCKS_1_2_X:
			case ORule.RULE_LINKED_BLOCKS_1_2_X:
			case ORule.RULE_LINKED_1_2_X:
				if (thisCase.noFSBlks >= 1) {
					dValue = wcode.valueA;
					if (thisCase.noFSBlks >= 2) {
						dValue += wcode.valueB;
						if (thisCase.noFSBlks >= wcode.count) {
							dValue += wcode.valueC;
						}
					}
				}
				codeBlocks = false;
				break;
			case ORule.RULE_CASE_BLOCKS_1_2_PLUSX:
			case ORule.RULE_LINKED_BLOCKS_1_2_PLUSX:
			case ORule.RULE_LINKED_1_2_PLUSX:
				if (thisCase.noFSBlks >= 1) {
					dValue = wcode.valueA;
					if (thisCase.noFSBlks >= 2) {
						dValue += wcode.valueB;
						if (thisCase.noFSBlks > wcode.count) {
							dValue += (wcode.valueC * (thisCase.noFSBlks - wcode.count));
						}
					}
				}
				codeBlocks = false;
				break;
			case ORule.RULE_SPECIMEN_INCLUSIVE:
			case ORule.RULE_SPECIMEN_FIXED:
				dValue = wcode.valueA;
				break;
			case ORule.RULE_SPECIMEN_EVERY_X_MIN_MAX:
			case ORule.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
			case ORule.RULE_GROUP_EVERY_X_MIN_MAX:
				dValue = wcode.valueA * pj.numbers.ceiling(thisSpecimen.noFSBlks, wcode.count);
				dValue += pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
				break;
			case ORule.RULE_SPECIMEN_1_2_X:
			case ORule.RULE_SPECIMEN_BLOCKS_1_2_X:
			case ORule.RULE_GROUP_1_2_X:
				if (thisSpecimen.noFSBlks >= 1) {
					dValue = wcode.valueA;
					if (thisSpecimen.noFSBlks >= 2) {
						dValue += wcode.valueB;
						if (thisSpecimen.noFSBlks >= wcode.count) {
							dValue += wcode.valueC;
						}
					}
				}
				break;
			case ORule.RULE_SPECIMEN_1_2_PLUSX:
			case ORule.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
			case ORule.RULE_GROUP_1_2_PLUSX:
				if (thisSpecimen.noFSBlks >= 1) {
					dValue = wcode.valueA;
					if (thisSpecimen.noFSBlks >= 2) {
						dValue += wcode.valueB;
						if (thisSpecimen.noFSBlks > wcode.count) {
							dValue += (wcode.valueC * (thisSpecimen.noFSBlks - wcode.count));
						}
					}
				}
				break;
			default:
				caseCoder.hasError = true;
				caseCoder.errorID = LConstants.ERROR_CODING_RULE_UNKNOWN;
				comment = "ERROR: codeFrozenBlocks, " + thisCase.caseNo + ", "
						+ LConstants.ERROR_STRINGS[caseCoder.errorID];
				caseCoder.comment += comment + "\n";
				pj.log(caseCoder.errorID, coderName, comment);
			}
			if (dValue > 0) {
				specimenCoder.valueFS += dValue;
				caseCoder.valueFS += dValue;
				caseCoder.comment += "Frozen Blocks: " + dValue + ", Specimen: "
						+ pj.numbers.formatDouble(3, specimenCoder.valueFS) + ", Case: "
						+ pj.numbers.formatDouble(3, caseCoder.valueFS) + "\n";
			}
		}
		return codeBlocks;
	}

	private void codeMain() {
		specimenCoder = specimens.get(caseCoder.mainSpec - 1);
		wcode = masterCodes.get(caseCoder.coderID);
		switch (wcode.ruleID) {
		case ORule.RULE_CASE_INCLUSIVE:
		case ORule.RULE_CASE_FIXED:
			specimenCoder.value = wcode.valueA;
			break;
		case ORule.RULE_CASE_GROSS_MICRO:
			if (thisCase.noSlides > 0) {
				specimenCoder.value = wcode.valueB;
			} else {
				specimenCoder.value = wcode.valueA;
			}
			break;
		case ORule.RULE_CASE_BLOCKS_X_MIN_MAX:
			dValue = wcode.valueA * pj.numbers.ceiling(thisCase.noBlocks, wcode.count);
			specimenCoder.value = pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
			break;
		case ORule.RULE_CASE_BLOCKS_1_2_X:
			specimenCoder.value = wcode.valueA;
			if (thisCase.noBlocks > 1) {
				specimenCoder.value += wcode.valueB;
				if (thisCase.noBlocks >= wcode.count) {
					specimenCoder.value += wcode.valueC;
				}
			}
			break;
		case ORule.RULE_CASE_BLOCKS_1_2_PLUSX:
			specimenCoder.value = wcode.valueA;
			if (thisCase.noBlocks > 1) {
				specimenCoder.value += wcode.valueB;
				if (thisCase.noBlocks > wcode.count) {
					specimenCoder.value += (wcode.valueC * (thisCase.noBlocks - wcode.count));
				}
			}
			break;
		default:
			// Case Fragments
			short counter = 0;
			for (int i = 0; i < thisCase.noSpec; i++) {
				counter += thisCase.lstSpecimens.get(i).noFrags;
			}
			switch (wcode.ruleID) {
			case ORule.RULE_CASE_FRAGS_X_MIN_MAX:
				specimenCoder.value = wcode.valueA * pj.numbers.ceiling(counter, wcode.count);
				specimenCoder.value = pj.numbers.minMax(specimenCoder.value, 0d, specimenCoder.value, wcode.valueB,
						wcode.valueC);
				break;
			case ORule.RULE_CASE_FRAGS_1_2_X:
				specimenCoder.value = wcode.valueA;
				if (counter > 1) {
					specimenCoder.value += wcode.valueB;
					if (counter >= wcode.count) {
						specimenCoder.value += wcode.valueC;
					}
				}
				break;
			case ORule.RULE_CASE_FRAGS_1_2_PLUSX:
				specimenCoder.value = wcode.valueA;
				if (counter > 1) {
					specimenCoder.value += wcode.valueB;
					if (counter > wcode.count) {
						specimenCoder.value += (wcode.valueC * (counter - wcode.count));
					}
				}
				break;
			default:
				// RULE_CASE_FRAGS_BLOCKS
				specimenCoder.value = wcode.valueA * pj.numbers.ceiling(counter, wcode.count);
				dValue = wcode.valueB * pj.numbers.ceiling(thisCase.noBlocks, wcode.count);
				caseCoder.comment += "Fragments: " + counter + " - " + pj.numbers.formatDouble(3, specimenCoder.value)
						+ ", Blocks: " + thisCase.noBlocks + " - " + pj.numbers.formatDouble(3, dValue) + "\n";
				if (specimenCoder.value < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.value = dValue;
				}
			}
		}
		caseCoder.value = specimenCoder.value;
		caseCoder.comment += "Case Rule: " + wcode.ruleID + ", Value: " + pj.numbers.formatDouble(3, caseCoder.value)
				+ "\n";
	}

	private void codeOrders() {
		if (caseCoder.inclusive) {
			return;
		}
		for (int i = 0; i < thisCase.noSpec; i++) {
			specimenCoder = specimens.get(i);
			if (specimenCoder.inclusive) {
				continue;
			}
			for (Entry<Short, OOrderCode> orderEntry : specimenCoder.lstOrders.entrySet()) {
				ordersCoder = orderEntry.getValue();
				if (ordersCoder.qty == 0) {
					continue;
				}
				if (ordersCoder.isAddlBlock && (!specimenCoder.codeBlocks || !caseCoder.codeBlocks)) {
					continue;
				}
				wcode = masterCodes.get(ordersCoder.codeID);
				switch (wcode.ruleID) {
				case ORule.RULE_AFTER_EVERY_X_MIN_MAX:
				case ORule.RULE_GROUP_EVERY_X_MIN_MAX:
				case ORule.RULE_UNIQUE_EVERY_X_MIN_MAX:
					ordersCoder.value = wcode.valueA * pj.numbers.ceiling(ordersCoder.qty, wcode.count);
					ordersCoder.value = pj.numbers.minMax(ordersCoder.value, wcode.valueB, wcode.valueC);
					break;
				case ORule.RULE_AFTER_1_2_X:
				case ORule.RULE_GROUP_1_2_X:
				case ORule.RULE_UNIQUE_1_2_X:
					ordersCoder.value = wcode.valueA;
					if (ordersCoder.qty > 1) {
						ordersCoder.value += wcode.valueB;
						if (ordersCoder.qty >= wcode.count) {
							ordersCoder.value += wcode.valueC;
						}
					}
					break;
				case ORule.RULE_AFTER_1_2_PLUSX:
				case ORule.RULE_GROUP_1_2_PLUSX:
				case ORule.RULE_UNIQUE_1_2_PLUSX:
					ordersCoder.value = wcode.valueA;
					if (ordersCoder.qty > 1) {
						ordersCoder.value += wcode.valueB;
						if (ordersCoder.qty > wcode.count) {
							ordersCoder.value += (wcode.valueC * (ordersCoder.qty - wcode.count));
						}
					}
					break;
				default:
					ordersCoder.value = wcode.valueA * ordersCoder.qty;
				}
				specimenCoder.value += ordersCoder.value;
				caseCoder.value += ordersCoder.value;
				caseCoder.comment += "codeOrders: Specimen " + (i + 1) + ", Orders: " + ordersCoder.name + ", Qty: "
						+ ordersCoder.qty + ", Value: " + pj.numbers.formatDouble(3, ordersCoder.value)
						+ ", Case Value: " + pj.numbers.formatDouble(3, caseCoder.value) + "\n";
			}
		}
	}

	private void codeSpecimen() {
		short spmID = 0, noSpecs = 0, noLinks = 0, noFrags = 0, noBlocks = 0, prevRule = 0;
		double dExpect = 0, dSpecs = 0, dLinks = 0, dFrags = 0, dBlocks = 0;
		for (int i = 0; i < thisCase.noSpec; i++) {
			specimenCoder = specimens.get(i);
			thisSpecimen = thisCase.lstSpecimens.get(i);
			wcode = masterCodes.get(specimenCoder.coderID);
			switch (wcode.ruleID) {
			case ORule.RULE_IGNORE:
				// Nothing for this particular specimen (molecular, Derm IF, Iron Quant)
				break;
			case ORule.RULE_SPECIMEN_INCLUSIVE:
			case ORule.RULE_SPECIMEN_FIXED:
				// Example, autopsy, refer-out
				specimenCoder.value = wcode.valueA;
				break;
			case ORule.RULE_SPECIMEN_EVERY_X_MIN_MAX:
				if (spmID != thisSpecimen.spmID || prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					spmID = thisSpecimen.spmID;
					dSpecs = 0;
					noSpecs = 0;
				}
				noSpecs++;
				dExpect = wcode.valueA * pj.numbers.ceiling(noSpecs, wcode.count);
				dValue = dExpect - dSpecs;
				specimenCoder.value = pj.numbers.minMax(dValue, dSpecs, dExpect, wcode.valueB, wcode.valueC);
				dSpecs += specimenCoder.value;
				break;
			case ORule.RULE_SPECIMEN_1_2_X:
				if (spmID != thisSpecimen.spmID || prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					spmID = thisSpecimen.spmID;
					noSpecs = 0;
				}
				noSpecs++;
				specimenCoder.value = wcode.valueA;
				if (noSpecs > 1) {
					specimenCoder.value += wcode.valueB;
					if (noSpecs > wcode.count) {
						specimenCoder.value += wcode.valueC;
					}
				}
				break;
			case ORule.RULE_SPECIMEN_1_2_PLUSX:
				if (spmID != thisSpecimen.spmID || prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					spmID = thisSpecimen.spmID;
					noSpecs = 0;
				}
				noSpecs++;
				specimenCoder.value = wcode.valueA;
				if (noSpecs > 1) {
					specimenCoder.value += wcode.valueB;
					if (noSpecs > wcode.count) {
						specimenCoder.value += (wcode.valueC * (noSpecs - wcode.count));
					}
				}
				break;
			case ORule.RULE_SPECIMEN_GROSS_MICRO:
				// Femoral heads
				if (thisSpecimen.noSlides > 0) {
					specimenCoder.value = wcode.valueB;
				} else {
					specimenCoder.value = wcode.valueA;
				}
				break;
			case ORule.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
				// CAP Medical GI biopsies
				dValue = wcode.valueA * pj.numbers.ceiling(thisSpecimen.noFrags, wcode.count);
				specimenCoder.value = pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
				break;
			case ORule.RULE_SPECIMEN_FRAGS_1_2_X:
				specimenCoder.value = wcode.valueA;
				if (thisSpecimen.noFrags > 1) {
					specimenCoder.value += wcode.valueB;
					if (thisSpecimen.noFrags >= wcode.count) {
						specimenCoder.value += wcode.valueC;
					}
				}
				break;
			case ORule.RULE_SPECIMEN_FRAGS_1_2_PLUSX:
				specimenCoder.value = wcode.valueA;
				if (thisSpecimen.noFrags > 1) {
					specimenCoder.value += wcode.valueB;
					if (thisSpecimen.noFrags > wcode.count) {
						specimenCoder.value += (wcode.valueC * (thisSpecimen.noFrags - wcode.count));
					}
				}
				break;
			case ORule.RULE_SPECIMEN_FRAGS_BLOCKS:
				// CAP GI Polyps, skin resections
				specimenCoder.value = wcode.valueA * pj.numbers.ceiling(thisSpecimen.noFrags, wcode.count);
				dValue = wcode.valueB * pj.numbers.ceiling(thisSpecimen.noBlocks, wcode.count);
				if (specimenCoder.value < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.value = dValue;
				}
				break;
			case ORule.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
				dValue = wcode.valueA * pj.numbers.ceiling(thisSpecimen.noBlocks, wcode.count);
				specimenCoder.value = pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
				break;
			case ORule.RULE_SPECIMEN_BLOCKS_1_2_X:
				specimenCoder.value = wcode.valueA;
				if (thisSpecimen.noBlocks > 1) {
					specimenCoder.value += wcode.valueB;
					if (thisSpecimen.noBlocks >= wcode.count) {
						specimenCoder.value += wcode.valueC;
					}
				}
				break;
			case ORule.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
				specimenCoder.value = wcode.valueA;
				if (thisSpecimen.noBlocks > 1) {
					specimenCoder.value += wcode.valueB;
					if (thisSpecimen.noBlocks > wcode.count) {
						specimenCoder.value += (wcode.valueC * (thisSpecimen.noBlocks - wcode.count));
					}
				}
				break;
			case ORule.RULE_LINKED_INCLUSIVE:
			case ORule.RULE_LINKED_FIXED:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					noLinks = 0;
				}
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.value = wcode.valueA;
				}
				break;
			case ORule.RULE_LINKED_FRAGS_X_MIN_MAX:
				// CAP Breast/Prostate Bx (max 20 cores per case = 10 L4E)
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.noFrags;
				dExpect = wcode.valueA * pj.numbers.ceiling(noFrags, wcode.count);
				dValue = dExpect - dFrags;
				specimenCoder.value = pj.numbers.minMax(dValue, dLinks, dExpect, wcode.valueB, wcode.valueC);
				dFrags += specimenCoder.value;
				break;
			case ORule.RULE_LINKED_FRAGS_1_2_X:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.noFrags;
				if (noFrags >= 1 && dFrags < wcode.valueA) {
					specimenCoder.value += wcode.valueA;
				}
				if (noFrags >= 2 && dFrags < wcode.valueA + wcode.valueB) {
					specimenCoder.value += wcode.valueB;
				}
				if (noFrags >= wcode.count && dFrags < wcode.valueA + wcode.valueB + wcode.valueC) {
					specimenCoder.value += wcode.valueC;
				}
				dFrags += specimenCoder.value;
				break;
			case ORule.RULE_LINKED_FRAGS_1_2_PLUSX:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.noFrags;
				dExpect = wcode.valueA;
				for (int j = 2; j <= wcode.count; j++) {
					if (j <= noFrags) {
						dExpect += wcode.valueB;
					}
				}
				if (noFrags > wcode.count) {
					dExpect += (wcode.valueC * (noFrags -wcode.count));
				}
				specimenCoder.value = dExpect - dFrags;
				dFrags = dExpect;
				break;
			case ORule.RULE_LINKED_FRAGS_BLOCKS:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dBlocks = 0;
					dFrags = 0;
					noFrags = 0;
					noBlocks = 0;
				}
				noFrags += thisSpecimen.noFrags;
				noBlocks += thisSpecimen.noBlocks;
				specimenCoder.value = (wcode.valueA * pj.numbers.ceiling(noFrags, wcode.count)) - dFrags;
				dValue = (wcode.valueB * pj.numbers.ceiling(noBlocks, wcode.count)) - dBlocks;
				dFrags += specimenCoder.value;
				dBlocks += dValue;
				if (specimenCoder.value < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.value = dValue;
				}
			case ORule.RULE_LINKED_BLOCKS_X_MIN_MAX:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.noBlocks;
				dExpect = wcode.valueA * pj.numbers.ceiling(noBlocks, wcode.count);
				dValue = dExpect - dBlocks;
				specimenCoder.value = pj.numbers.minMax(dValue, dBlocks, dExpect, wcode.valueB, wcode.valueC);
				dBlocks += specimenCoder.value;
				break;
			case ORule.RULE_LINKED_BLOCKS_1_2_X:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.noBlocks;
				if (noBlocks >= 1 && dBlocks < wcode.valueA) {
					specimenCoder.value += wcode.valueA;
				}
				if (noBlocks >= 2 && dBlocks < wcode.valueA + wcode.valueB) {
					specimenCoder.value += wcode.valueB;
				}
				if (noBlocks >= wcode.count && dBlocks < wcode.valueA + wcode.valueB + wcode.valueC) {
					specimenCoder.value += wcode.valueC;
				}
				dBlocks += specimenCoder.value;
				break;
			case ORule.RULE_LINKED_BLOCKS_1_2_PLUSX:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.noBlocks;
				if (noBlocks >= 1 && dBlocks < wcode.valueA) {
					specimenCoder.value += wcode.valueA;
				}
				if (noBlocks >= 2 && dBlocks < wcode.valueA + wcode.valueB) {
					specimenCoder.value += wcode.valueB;
				}
				if (noBlocks > wcode.count
						&& dBlocks < wcode.valueA + wcode.valueB + (wcode.valueC * (noBlocks - wcode.count))) {
					specimenCoder.value += (wcode.valueC * (noBlocks - wcode.count));
				}
				dBlocks += specimenCoder.value;
				break;
			case ORule.RULE_LINKED_EVERY_X_MIN_MAX:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					dLinks = 0;
					noLinks = 0;
				}
				noLinks++;
				dExpect = wcode.valueA * pj.numbers.ceiling(noLinks, wcode.count);
				dValue = dExpect - dLinks;
				specimenCoder.value = pj.numbers.minMax(dValue, dLinks, dExpect, wcode.valueB, wcode.valueC);
				dLinks += specimenCoder.value;
				break;
			case ORule.RULE_LINKED_1_2_X:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					noLinks = 0;
				}
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.value = wcode.valueA;
				} else if (noLinks == 2) {
					specimenCoder.value = wcode.valueB;
				} else if (noLinks == wcode.count) {
					specimenCoder.value = wcode.valueC;
				}
				break;
			case ORule.RULE_LINKED_1_2_PLUSX:
				if (prevRule != wcode.ruleID) {
					prevRule = wcode.ruleID;
					noLinks = 0;
				}
				noLinks++;
				specimenCoder.value = wcode.valueA;
				if (noLinks > 1) {
					specimenCoder.value += wcode.valueB;
					if (noLinks > wcode.count) {
						specimenCoder.value += (wcode.valueC * (noLinks - wcode.count));
					}
				}
				break;
			default:
				caseCoder.hasError = true;
				caseCoder.errorID = LConstants.ERROR_CODING_RULE_UNKNOWN;
			}
			caseCoder.value += specimenCoder.value;
			caseCoder.comment += "Specimen " + (i + 1) + ": Links " + noLinks + ", Rule " + wcode.ruleID + ", Value "
					+ pj.numbers.formatDouble(3, specimenCoder.value) + ", Case "
					+ pj.numbers.formatDouble(3, caseCoder.value) + "\n";
		}
	}

	private void codeSynoptics() {
		dValue = 0;
		wcode = masterCodes.get(RULE_SYNOPTICS);
		switch (wcode.ruleID) {
		case ORule.RULE_IGNORE:
			// RCP-UK
			break;
		case ORule.RULE_CASE_INCLUSIVE:
		case ORule.RULE_GROUP_CASE_INCLUSIVE:
		case ORule.RULE_UNIQUE_CASE_INCLUSIVE:
		case ORule.RULE_AFTER_CASE_INCLUSIVE:
			dValue = wcode.valueA;
			break;
		case ORule.RULE_CASE_FIXED:
			dValue = caseCoder.value * thisCase.noSynop;
			break;
		case ORule.RULE_SPECIMEN_INCLUSIVE:
		case ORule.RULE_SPECIMEN_FIXED:
		case ORule.RULE_LINKED_INCLUSIVE:
		case ORule.RULE_LINKED_FIXED:
		case ORule.RULE_GROUP_SPECIMEN_INCLUSIVE:
		case ORule.RULE_UNIQUE_SPECIMEN_INCLUSIVE:
		case ORule.RULE_AFTER_SPECIMEN_INCLUSIVE:
			if (thisCase.noSpec > thisCase.noSynop) {
				dValue = caseCoder.value * thisCase.noSynop;
			} else {
				dValue = caseCoder.value * thisCase.noSpec;
			}
			break;
		case ORule.RULE_SPECIMEN_EVERY_X_MIN_MAX:
		case ORule.RULE_LINKED_EVERY_X_MIN_MAX:
		case ORule.RULE_GROUP_EVERY_X_MIN_MAX:
		case ORule.RULE_UNIQUE_EVERY_X_MIN_MAX:
		case ORule.RULE_AFTER_EVERY_X_MIN_MAX:
			// W2Q
			dValue = wcode.valueA * pj.numbers.ceiling(thisCase.noSynop, wcode.count);
			dValue = pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
			break;
		case ORule.RULE_SPECIMEN_1_2_X:
		case ORule.RULE_LINKED_1_2_X:
		case ORule.RULE_GROUP_1_2_X:
		case ORule.RULE_UNIQUE_1_2_X:
		case ORule.RULE_AFTER_1_2_X:
			dValue = wcode.valueA;
			if (thisCase.noSynop > 1) {
				// 2nd synoptic report (0=ignore)
				dValue += wcode.valueB;
				if (thisCase.noSynop >= wcode.count) {
					// synoptic report X
					dValue += wcode.valueC;
				}
			}
			break;
		case ORule.RULE_SPECIMEN_1_2_PLUSX:
		case ORule.RULE_LINKED_1_2_PLUSX:
		case ORule.RULE_GROUP_1_2_PLUSX:
		case ORule.RULE_UNIQUE_1_2_PLUSX:
		case ORule.RULE_AFTER_1_2_PLUSX:
			dValue = wcode.valueA;
			if (thisCase.noSynop > 1) {
				// 2nd synoptic report (0=ignore)
				dValue += wcode.valueB;
				if (thisCase.noSynop > wcode.count) {
					// 3+ synoptic reports
					dValue += (wcode.valueC * (thisCase.noSynop - wcode.count));
				}
			}
			break;
		default:
			caseCoder.hasError = true;
			caseCoder.errorID = LConstants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: codeSynoptics, " + thisCase.caseNo + ", " + LConstants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + "\n";
			pj.log(caseCoder.errorID, coderName, comment);
		}
		caseCoder.value += dValue;
		caseCoder.comment += "Synoptic: Rule " + wcode.ruleID + ", Value " + pj.numbers.formatDouble(3, dValue)
				+ ", Case " + pj.numbers.formatDouble(3, caseCoder.value) + "\n";
	}

	@Override
	double getAddl(short codeID, short qty) {
		wcode = masterCodes.get(codeID);
		switch (wcode.ruleID) {
		case ORule.RULE_ADDL_CASE_INCLUSIVE:
		case ORule.RULE_ADDL_SPECIMEN_INCLUSIVE:
			dValue = wcode.valueA;
			break;
		case ORule.RULE_ADDL_EVERY_X_MIN_MAX:
			dValue = wcode.valueA * pj.numbers.ceiling(qty, wcode.count);
			dValue = pj.numbers.minMax(dValue, wcode.valueB, wcode.valueC);
			break;
		case ORule.RULE_ADDL_1_2_X:
			dValue = wcode.valueA;
			if (qty > 1) {
				dValue += wcode.valueB;
				if (qty >= wcode.count) {
					dValue += wcode.valueC;
				}
			}
			break;
		case ORule.RULE_ADDL_1_2_PLUSX:
			dValue = wcode.valueA;
			if (qty > 1) {
				dValue += wcode.valueB;
				if (qty > wcode.count) {
					dValue += (wcode.valueC * (qty - wcode.count));
				}
			}
			break;
		default:
			dValue = 0;
		}
		if (dValue > MAX_VALUE) {
			dValue = MAX_VALUE;
		}
		return dValue;
	}

	@Override
	String getComment() {
		return (coderName + "\n" + caseCoder.comment + "--------------------------\n");
	}

	@Override
	double getCorrelations() {
		dValue = 0;
		wcode = masterCodes.get(RULE_CORRELATIONS);
		if (wcode != null) {
			dValue = wcode.valueA;
			if (dValue > MAX_VALUE) {
				dValue = MAX_VALUE;
			}
		}
		return dValue;
	}

	@Override
	double getFrozen() {
		dValue = caseCoder.valueFS;
		if (dValue > MAX_VALUE) {
			dValue = MAX_VALUE;
		}
		return dValue;
	}

	@Override
	double getFrozen(int specimenNo) {
		dValue = specimens.get(specimenNo).valueFS;
		if (dValue > MAX_VALUE) {
			dValue = MAX_VALUE;
		}
		return dValue;
	}

	@Override
	double getOrder(int specimenNo, short groupID) {
		dValue = 0;
		ordersCoder = specimens.get(specimenNo).lstOrders.get(groupID);
		if (ordersCoder != null) {
			dValue = ordersCoder.value;
			if (dValue > MAX_VALUE) {
				dValue = MAX_VALUE;
			}
		}
		return dValue;
	}

	@Override
	double getValue() {
		dValue = caseCoder.value;
		if (dValue > MAX_VALUE) {
			dValue = MAX_VALUE;
		}
		return dValue;
	}

	@Override
	double getValue(int specimenNo) {
		dValue = specimens.get(specimenNo).value;
		if (dValue > MAX_VALUE) {
			dValue = MAX_VALUE;
		}
		return dValue;
	}

	@Override
	boolean hasComment() {
		return (caseCoder.comment.length() > 0);
	}

	@Override
	boolean hasError() {
		return caseCoder.hasError;
	}

	@Override
	boolean isOrderUnique(short orderID) {
		for (int i = 0; i < ordersCoder.orders.size(); i++) {
			if (ordersCoder.orders.get(i) == orderID) {
				return false;
			}
		}
		ordersCoder.orders.add(orderID);
		return true;
	}

	@Override
	boolean needsFragments(int specimenNo) {
		return specimens.get(specimenNo).needFrag;
	}

	@Override
	void newCase(OCaseFinal thisCase) {
		specimens.clear();
		caseCoder = new OCaseCode();
		thisSpecimen = new OSpecFinal();
		specimenCoder = new OSpecCode();
		ordersCoder = new OOrderCode();
		this.thisCase = thisCase;
	}

	private void readTables(PreparedStatement pstm) {
		ResultSet rst = pj.dbPowerJ.getResultSet(pstm);
		try {
			while (rst.next()) {
				wcode = new OWorkcode();
				wcode.ruleID = rst.getShort("RUID");
				wcode.count = rst.getShort("COQY");
				wcode.valueA = rst.getDouble("COV1");
				wcode.valueB = rst.getDouble("COV2");
				wcode.valueC = rst.getDouble("COV3");
				wcode.name = rst.getString("CONM");
				masterCodes.put(rst.getShort("COID"), wcode);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, coderName, e);
		} finally {
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.close(pstm);
		}
	}
}