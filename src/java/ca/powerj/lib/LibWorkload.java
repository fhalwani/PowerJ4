package ca.powerj.lib;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import ca.powerj.data.CaseCoderData;
import ca.powerj.data.CaseData;
import ca.powerj.data.OrderCoderData;
import ca.powerj.data.SpecimenCodeData;
import ca.powerj.data.SpecimenData;
import ca.powerj.database.DBPowerj;
import ca.powerj.data.CoderData;

class LibWorkload extends LibInactive {
	private final short RULE_SYNOPTICS = 2;
	private final short RULE_FROZENS = 3;
	private final short RULE_FROZENS_BLOCKS = 4;
	private final short RULE_CORRELATIONS = 5;
	private final double MAX_VALUE = 99.9;
	private String comment = "";
	private CaseData thisCase = new CaseData();
	private CaseCoderData caseCoder = new CaseCoderData();
	private SpecimenData thisSpecimen = new SpecimenData();
	private SpecimenCodeData specimenCoder = new SpecimenCodeData();
	private OrderCoderData ordersCoder = new OrderCoderData();
	private CoderData wcode = new CoderData();
	private HashMap<Short, CoderData> coders = new HashMap<Short, CoderData>();
	private ArrayList<SpecimenCodeData> specimenCoders = new ArrayList<SpecimenCodeData>();
	private LibBase base;

	LibWorkload(byte coderID, LibBase base, String name, DBPowerj dbPowerJ) {
		super(name);
		this.base = base;
		getData(coderID, dbPowerJ);
	}

	@Override
	void addOrder(short orderID, short groupID, short codeID, short qty,
			boolean isRoutine, boolean isAddlBlock, int specimenNo) {
		if (caseCoder.isInclusive()) {
			return;
		}
		wcode = coders.get(codeID);
		if (wcode == null) {
			caseCoder.setHasError(true);
			caseCoder.setErrorID(LibConstants.ERROR_ORDER_UNKNOWN);
			comment = "ERROR: addOrder, " + thisCase.getCaseNo() + ", Specimen "
					+ specimenNo + ", Order " + codeID + ", "
					+ LibConstants.ERROR_STRINGS[LibConstants.ERROR_ORDER_UNKNOWN];
			caseCoder.setComment(comment);
			base.log(LibConstants.ERROR_ORDER_UNKNOWN, name, comment);
			return;
		}
		switch (wcode.getRuleID()) {
		case LibConstants.RULE_UNIQUE_CASE_INCLUSIVE:
		case LibConstants.RULE_GROUP_CASE_INCLUSIVE:
		case LibConstants.RULE_AFTER_CASE_INCLUSIVE:
			thisSpecimen = thisCase.getSpecimen(caseCoder.getMainSpec());
			specimenCoder = specimenCoders.get(caseCoder.getMainSpec());
			break;
		default:
			thisSpecimen = thisCase.getSpecimen(specimenNo);
			specimenCoder = specimenCoders.get(specimenNo);
		}
		if (specimenCoder.isInclusive()) {
			return;
		}
		if (isAddlBlock && (!specimenCoder.isCodeBlocks() || !caseCoder.isCodeBlocks())) {
			return;
		}
		ordersCoder = specimenCoder.getOrder(groupID);
		if (ordersCoder == null) {
			ordersCoder = new OrderCoderData();
			ordersCoder.setCodeID(codeID);
			ordersCoder.setAddlBlock(isAddlBlock);
			ordersCoder.setName(wcode.getName());
			specimenCoder.setOrder(groupID, ordersCoder);
		} else if (isAddlBlock) {
			// Avoid changing it to false
			ordersCoder.setAddlBlock(true);
		}
		switch (wcode.getRuleID()) {
		case LibConstants.RULE_CASE_INCLUSIVE:
		case LibConstants.RULE_GROUP_CASE_INCLUSIVE:
		case LibConstants.RULE_SPECIMEN_INCLUSIVE:
		case LibConstants.RULE_GROUP_SPECIMEN_INCLUSIVE:
			ordersCoder.setQty((short) 1);
			break;
		case LibConstants.RULE_UNIQUE_CASE_INCLUSIVE:
		case LibConstants.RULE_UNIQUE_SPECIMEN_INCLUSIVE:
			if (isOrderUnique(orderID)) {
				ordersCoder.setQty((short) (ordersCoder.getQty() +1));
			}
			break;
		case LibConstants.RULE_UNIQUE_EVERY_X_MIN_MAX:
		case LibConstants.RULE_UNIQUE_1_2_X:
		case LibConstants.RULE_UNIQUE_1_2_PLUSX:
			// CPT
			if (isOrderUnique(orderID)) {
				ordersCoder.setQty((short) (ordersCoder.getQty() +1));
			}
			break;
		case LibConstants.RULE_AFTER_CASE_INCLUSIVE:
		case LibConstants.RULE_AFTER_SPECIMEN_INCLUSIVE:
			// Once per case if ordered after routing
			if (!isRoutine) {
				ordersCoder.setQty((short) 1);
			}
			break;
		case LibConstants.RULE_AFTER_EVERY_X_MIN_MAX:
		case LibConstants.RULE_AFTER_1_2_X:
		case LibConstants.RULE_AFTER_1_2_PLUSX:
			// CAP
			if (!isRoutine) {
				ordersCoder.setQty((short) (ordersCoder.getQty() +qty));
			}
			break;
		case LibConstants.RULE_GROUP_EVERY_X_MIN_MAX:
		case LibConstants.RULE_GROUP_1_2_X:
		case LibConstants.RULE_GROUP_1_2_PLUSX:
			// W2Q
			ordersCoder.setQty((short) (ordersCoder.getQty() +qty));
			break;
		default:
			// Ignore RCP
			ordersCoder.setQty((short) 0);
		}
	}

	@Override
	void addSpecimen(SpecimenData thisSpecimen, short codeBenign, short codeMalignant, short codeRadical) {
		this.thisSpecimen = thisSpecimen;
		specimenCoder = new SpecimenCodeData();
		specimenCoder.setProcID(thisSpecimen.getProcID());
		specimenCoder.setCoderID(codeBenign);
		specimenCoders.add(specimenCoder);
		if (thisCase.getNoSynop() > 0) {
			if (!caseCoder.isRadical() && (thisCase.getNoSynop() > 1 || thisCase.isHasLN())) {
				// Radical is 2 synoptics or 1 synoptic + node dissection
				wcode = coders.get(codeRadical);
				if (wcode != null) {
					if (wcode.getRuleID() > LibConstants.RULE_IGNORE) {
						// Use this specimen as first choice for coding radical cases
						specimenCoder.setCoderID(codeRadical);
						caseCoder.setMainSpec(thisCase.getNoSpecs());
						caseCoder.setRadical(true);
						caseCoder.setComment("Radical Specimen " + thisCase.getNoSpecs()
							+ ", Rule " + wcode.getRuleID() + ", " + ", Name: " + wcode.getName());
					}
				}
			} else if (!caseCoder.isMalignant()) {
				wcode = coders.get(codeMalignant);
				if (wcode != null) {
					if (wcode.getRuleID() > LibConstants.RULE_IGNORE) {
						// Use this specimen as first choice for coding malignancy
						specimenCoder.setCoderID(codeMalignant);
						caseCoder.setMainSpec(thisCase.getNoSpecs());
						caseCoder.setMalignant(true);
						caseCoder.setComment("Malignant Specimen " + thisCase.getNoSpecs()
							+ ", Rule " + wcode.getRuleID() + ", " + ", Name: " + wcode.getName());
					}
				}
			}
		}
		if (!thisCase.isCodeSpec()) {
			// Molecular
			specimenCoder.setCodeBlocks(false);
			return;
		}
		wcode = coders.get(specimenCoder.getCoderID());
		if (wcode == null) {
			caseCoder.setHasError(true);
			caseCoder.setErrorID(LibConstants.ERROR_SPECIMEN_UNKNOWN);
			comment = "ERROR: addSpecimen, " + thisCase.getCaseNo() + ", Specimen " + thisCase.getNoSpecs() + ", Coder "
					+ specimenCoder.getCoderID() + ", " + LibConstants.ERROR_STRINGS[LibConstants.ERROR_SPECIMEN_UNKNOWN];
			caseCoder.setComment(comment);
			base.log(LibConstants.ERROR_SPECIMEN_UNKNOWN, name, comment);
			return;
		}
		switch (wcode.getRuleID()) {
		case LibConstants.RULE_CASE_INCLUSIVE:
			caseCoder.setCoderID(specimenCoder.getCoderID());
			caseCoder.setMainSpec(thisCase.getNoSpecs());
			caseCoder.setCodeBlocks(false);
			caseCoder.setInclusive(true);
			break;
		case LibConstants.RULE_CASE_FRAGS_X_MIN_MAX:
		case LibConstants.RULE_CASE_FRAGS_1_2_X:
		case LibConstants.RULE_CASE_FRAGS_1_2_PLUSX:
		case LibConstants.RULE_CASE_FRAGS_BLOCKS:
			caseCoder.setCoderID(specimenCoder.getCoderID());
			caseCoder.setMainSpec(thisCase.getNoSpecs());
			caseCoder.setCodeBlocks(false);
			thisCase.setHasFrag(true);
			break;
		case LibConstants.RULE_CASE_GROSS_MICRO:
		case LibConstants.RULE_CASE_BLOCKS_X_MIN_MAX:
		case LibConstants.RULE_CASE_BLOCKS_1_2_X:
		case LibConstants.RULE_CASE_BLOCKS_1_2_PLUSX:
			caseCoder.setCoderID(specimenCoder.getCoderID());
			caseCoder.setMainSpec(thisCase.getNoSpecs());
			caseCoder.setCodeBlocks(false);
			break;
		case LibConstants.RULE_CASE_FIXED:
			caseCoder.setCoderID(specimenCoder.getCoderID());
			caseCoder.setMainSpec(thisCase.getNoSpecs());
			break;
		case LibConstants.RULE_SPECIMEN_INCLUSIVE:
		case LibConstants.RULE_LINKED_INCLUSIVE:
			specimenCoder.setInclusive(true);
			specimenCoder.setCodeBlocks(false);
			break;
		case LibConstants.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
		case LibConstants.RULE_SPECIMEN_FRAGS_1_2_X:
		case LibConstants.RULE_SPECIMEN_FRAGS_1_2_PLUSX:
		case LibConstants.RULE_SPECIMEN_FRAGS_BLOCKS:
		case LibConstants.RULE_LINKED_FRAGS_X_MIN_MAX:
		case LibConstants.RULE_LINKED_FRAGS_1_2_X:
		case LibConstants.RULE_LINKED_FRAGS_1_2_PLUSX:
		case LibConstants.RULE_LINKED_FRAGS_BLOCKS:
			// CAP GI Polyps, skin resections
			thisCase.setHasFrag(true);
			specimenCoder.setNeedFrag(true);
			specimenCoder.setCodeBlocks(false);
			break;
		case LibConstants.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
		case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_X:
		case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
		case LibConstants.RULE_LINKED_BLOCKS_X_MIN_MAX:
		case LibConstants.RULE_LINKED_BLOCKS_1_2_X:
		case LibConstants.RULE_LINKED_BLOCKS_1_2_PLUSX:
			specimenCoder.setCodeBlocks(false);
			break;
		default:
			// Nothing for specimen (W2Q, RCP, CPT, molecular, Derm IF, Iron Quant)
		}
		caseCoder.setComment("Specimen " + thisCase.getNoSpecs() + ", Rule " + wcode.getRuleID() + ", Name: " + wcode.getName()
				+ ", AddlBlk: " + (specimenCoder.isCodeBlocks() ? "T" : "F") + ", Frags: "
				+ (specimenCoder.isNeedFrag() ? "T" : "F"));
	}

	@Override
	void checkSpecimens() {
		for (short i = 0; i < thisCase.getNoSpecs(); i++) {
			thisSpecimen = thisCase.getSpecimen(i);
			specimenCoder = specimenCoders.get(i);
			for (Entry<Short, OrderCoderData> orderEntry : specimenCoder.getOrders().entrySet()) {
				ordersCoder = orderEntry.getValue();
				if (ordersCoder.isAddlBlock() && (!specimenCoder.isCodeBlocks() || !caseCoder.isCodeBlocks())) {
					ordersCoder.setQty((short) 0);
				} else if (caseCoder.isInclusive() || specimenCoder.isInclusive()) {
					ordersCoder.setQty((short) 0);
				}
			}
		}
	}

	@Override
	void close() {
		coders.clear();
		specimenCoders.clear();
	}

	@Override
	void codeCase() {
		checkSpecimens();
		// Code each specimen as if unique so Coder5 can get accurate data
		if (thisCase.isCodeSpec()) {
			// Else, Molecular case, only code orders
			codeSpecimens();
		}
		if (caseCoder.getCoderID() > 0) {
			codeMain();
		}
		codeOrders();
		if (thisCase.getNoSynop() > 0) {
			// W2Q adds workload for actual synoptics
			codeSynoptics();
		}
		if (thisCase.getNoFSSpec() > 0) {
			codeFrozens();
		}
	}

	private void codeFrozens() {
		boolean caseFixed = false;
		boolean codeBlocks = true;
		double dLinks = 0, dExpect = 0;
		short noLinks = 0;
		wcode = coders.get(RULE_FROZENS);
		if (wcode == null) {
			caseCoder.setHasError(true);
			caseCoder.setErrorID(LibConstants.ERROR_CODING_RULE_UNKNOWN);
			comment = "ERROR: codeFrozen, " + thisCase.getCaseNo() + ", " + LibConstants.ERROR_STRINGS[LibConstants.ERROR_CODING_RULE_UNKNOWN];
			caseCoder.setComment(comment);
			base.log(LibConstants.ERROR_CODING_RULE_UNKNOWN, name, comment);
			return;
		}
		if (wcode.getRuleID() == LibConstants.RULE_IGNORE) {
			return;
		}
		for (short i = 0; i < thisCase.getNoSpecs(); i++) {
			if (caseFixed)
				break;
			thisSpecimen = thisCase.getSpecimen(i);
			if (thisSpecimen.getNoFSBlks() < 1 && thisSpecimen.getNoFSSlds() < 1) {
				continue;
			}
			dValue = 0;
			specimenCoder = specimenCoders.get(i);
			switch (wcode.getRuleID()) {
			case LibConstants.RULE_CASE_INCLUSIVE:
			case LibConstants.RULE_LINKED_INCLUSIVE:
				specimenCoder.setValueFS(wcode.getValueA());
				caseFixed = true;
				codeBlocks = false;
				break;
			case LibConstants.RULE_CASE_FIXED:
			case LibConstants.RULE_LINKED_FIXED:
				specimenCoder.setValueFS(wcode.getValueA());
				caseFixed = true;
				break;
			case LibConstants.RULE_CASE_GROSS_MICRO:
				if (thisCase.getNoFSSlds() > 0) {
					specimenCoder.setValueFS(wcode.getValueB());
				} else {
					specimenCoder.setValueFS(wcode.getValueA());
				}
				caseFixed = true;
				codeBlocks = false;
				break;
			case LibConstants.RULE_LINKED_EVERY_X_MIN_MAX:
			case LibConstants.RULE_SPECIMEN_EVERY_X_MIN_MAX:
				noLinks++;
				dExpect = wcode.getValueA() * base.numbers.ceiling(noLinks, wcode.getCount());
				dValue = dExpect - dLinks;
				specimenCoder.setValueFS(base.numbers.minMax(dValue, dLinks, dExpect, wcode.getValueB(), wcode.getValueC()));
				dLinks += specimenCoder.getValueFS();
				break;
			case LibConstants.RULE_LINKED_1_2_X:
			case LibConstants.RULE_SPECIMEN_1_2_X:
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.setValueFS(wcode.getValueA());
				} else if (noLinks == 2) {
					specimenCoder.setValueFS(wcode.getValueB());
				} else if (noLinks == wcode.getCount()) {
					specimenCoder.setValueFS(wcode.getValueC());
				}
				break;
			case LibConstants.RULE_LINKED_1_2_PLUSX:
			case LibConstants.RULE_SPECIMEN_1_2_PLUSX:
				noLinks++;
				specimenCoder.setValueFS(wcode.getValueA());
				if (noLinks > 1) {
					specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueB());
					if (noLinks > wcode.getCount()) {
						specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueC() * (noLinks - wcode.getCount()));
					}
				}
				break;
			case LibConstants.RULE_CASE_BLOCKS_X_MIN_MAX:
			case LibConstants.RULE_LINKED_BLOCKS_X_MIN_MAX:
				noLinks += thisSpecimen.getNoFSBlks();
				dExpect = wcode.getValueA() * base.numbers.ceiling(noLinks, wcode.getCount());
				dValue = dExpect - dLinks;
				specimenCoder.setValueFS(base.numbers.minMax(dValue, dLinks, dExpect, wcode.getValueB(), wcode.getValueC()));
				dLinks += specimenCoder.getValueFS();
				codeBlocks = false;
				break;
			case LibConstants.RULE_CASE_BLOCKS_1_2_X:
			case LibConstants.RULE_LINKED_BLOCKS_1_2_X:
				noLinks += thisSpecimen.getNoFSBlks();
				if (noLinks >= 1 && dLinks < wcode.getValueA()) {
					specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueA());
				}
				if (noLinks >= 2 && dLinks < wcode.getValueA() + wcode.getValueB()) {
					specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueB());
				}
				if (noLinks >= wcode.getCount() && dLinks < wcode.getValueA() + wcode.getValueB() + wcode.getValueC()) {
					specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueC());
				}
				dLinks += specimenCoder.getValueFS();
				codeBlocks = false;
				break;
			case LibConstants.RULE_CASE_BLOCKS_1_2_PLUSX:
			case LibConstants.RULE_LINKED_BLOCKS_1_2_PLUSX:
				noLinks += thisSpecimen.getNoFSBlks();
				specimenCoder.setValueFS(wcode.getValueA());
				if (noLinks > 1 && dLinks < wcode.getValueA() + wcode.getValueB()) {
					specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueB());
					if (noLinks > wcode.getCount()
							&& dLinks < wcode.getValueA() + wcode.getValueB() + (wcode.getValueC() * (noLinks - wcode.getCount()))) {
						specimenCoder.setValueFS(specimenCoder.getValueFS() + (wcode.getValueC() * (noLinks - wcode.getCount())));
					}
				}
				dLinks += specimenCoder.getValueFS();
				codeBlocks = false;
				break;
			case LibConstants.RULE_SPECIMEN_INCLUSIVE:
				specimenCoder.setValueFS(wcode.getValueA());
				codeBlocks = false;
				break;
			case LibConstants.RULE_SPECIMEN_FIXED:
				specimenCoder.setValueFS(wcode.getValueA());
				break;
			case LibConstants.RULE_SPECIMEN_GROSS_MICRO:
				if (thisSpecimen.getNoFSSlds() > 0) {
					specimenCoder.setValueFS(wcode.getValueB());
				} else {
					specimenCoder.setValueFS(wcode.getValueA());
				}
				break;
			case LibConstants.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
				dValue = wcode.getValueA() * base.numbers.ceiling(thisSpecimen.getNoFSBlks(), wcode.getCount());
				specimenCoder.setValueFS(base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC()));
				codeBlocks = false;
				break;
			case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_X:
				if (thisSpecimen.getNoFSBlks() > 0) {
					specimenCoder.setValueFS(wcode.getValueA());
				}
				if (thisSpecimen.getNoFSBlks() > 1) {
					specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueB());
				}
				if (thisSpecimen.getNoFSBlks() >= wcode.getCount()) {
					specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueC());
				}
				codeBlocks = false;
				break;
			case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
				if (thisSpecimen.getNoFSBlks() > 0) {
					specimenCoder.setValueFS(wcode.getValueA());
					if (thisSpecimen.getNoFSBlks() > 1) {
						specimenCoder.setValueFS(specimenCoder.getValueFS() + wcode.getValueB());
						if (thisSpecimen.getNoFSBlks() > wcode.getCount()) {
							specimenCoder.setValueFS(specimenCoder.getValueFS() + (wcode.getValueC() * (thisSpecimen.getNoFSBlks() - wcode.getCount())));
						}
					}
				}
				codeBlocks = false;
				break;
			default:
				caseCoder.setHasError(true);
				caseCoder.setErrorID(LibConstants.ERROR_CODING_RULE_UNKNOWN);
				comment = "ERROR: codeFrozen, " + thisCase.getCaseNo() + ", " + LibConstants.ERROR_STRINGS[LibConstants.ERROR_CODING_RULE_UNKNOWN];
				caseCoder.setComment(comment);
				base.log(LibConstants.ERROR_CODING_RULE_UNKNOWN, name, comment);
			}
			caseCoder.setValueFrozen(caseCoder.getValueFrozen() + specimenCoder.getValueFS());
			if (codeBlocks && thisSpecimen.getNoFSBlks() > 0) {
				codeBlocks = codeFrozenBlocks();
				wcode = coders.get(RULE_FROZENS);
			}
			caseCoder.setComment("Frozen Sections: Specimen " + (i + 1) + ", Blocks " + thisSpecimen.getNoFSBlks()
					+ ", Value: " + base.numbers.formatDouble(3, specimenCoder.getValueFS()) + ", Case: "
					+ base.numbers.formatDouble(3, specimenCoder.getValueFS()));
		}
	}

	private boolean codeFrozenBlocks() {
		boolean codeBlocks = true;
		// Same specimen as codeFrozen
		dValue = 0;
		wcode = coders.get(RULE_FROZENS_BLOCKS);
		if (wcode == null) {
			caseCoder.setHasError(true);
			caseCoder.setErrorID(LibConstants.ERROR_CODING_RULE_UNKNOWN);
			comment = "ERROR: codeFrozenBlocks, " + thisCase.getCaseNo() + ", "
					+ LibConstants.ERROR_STRINGS[LibConstants.ERROR_CODING_RULE_UNKNOWN];
			caseCoder.setComment(comment);
			base.log(LibConstants.ERROR_CODING_RULE_UNKNOWN, name, comment);
			codeBlocks = false;
		} else {
			switch (wcode.getRuleID()) {
			case LibConstants.RULE_IGNORE:
				codeBlocks = false;
				dValue = 0;
				break;
			case LibConstants.RULE_CASE_BLOCKS_X_MIN_MAX:
			case LibConstants.RULE_LINKED_BLOCKS_X_MIN_MAX:
			case LibConstants.RULE_LINKED_EVERY_X_MIN_MAX:
				dValue = wcode.getValueA() * base.numbers.ceiling(thisCase.getNoFSBlks(), wcode.getCount());
				dValue = base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC());
				codeBlocks = false;
				break;
			case LibConstants.RULE_CASE_BLOCKS_1_2_X:
			case LibConstants.RULE_LINKED_BLOCKS_1_2_X:
			case LibConstants.RULE_LINKED_1_2_X:
				if (thisCase.getNoFSBlks() >= 1) {
					dValue = wcode.getValueA();
					if (thisCase.getNoFSBlks() >= 2) {
						dValue += wcode.getValueB();
						if (thisCase.getNoFSBlks() >= wcode.getCount()) {
							dValue += wcode.getValueC();
						}
					}
				}
				codeBlocks = false;
				break;
			case LibConstants.RULE_CASE_BLOCKS_1_2_PLUSX:
			case LibConstants.RULE_LINKED_BLOCKS_1_2_PLUSX:
			case LibConstants.RULE_LINKED_1_2_PLUSX:
				if (thisCase.getNoFSBlks() >= 1) {
					dValue = wcode.getValueA();
					if (thisCase.getNoFSBlks() >= 2) {
						dValue += wcode.getValueB();
						if (thisCase.getNoFSBlks() > wcode.getCount()) {
							dValue += (wcode.getValueC() * (thisCase.getNoFSBlks() - wcode.getCount()));
						}
					}
				}
				codeBlocks = false;
				break;
			case LibConstants.RULE_SPECIMEN_INCLUSIVE:
			case LibConstants.RULE_SPECIMEN_FIXED:
				dValue = wcode.getValueA();
				break;
			case LibConstants.RULE_SPECIMEN_EVERY_X_MIN_MAX:
			case LibConstants.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
			case LibConstants.RULE_GROUP_EVERY_X_MIN_MAX:
				dValue = wcode.getValueA() * base.numbers.ceiling(thisSpecimen.getNoFSBlks(), wcode.getCount());
				dValue += base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC());
				break;
			case LibConstants.RULE_SPECIMEN_1_2_X:
			case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_X:
			case LibConstants.RULE_GROUP_1_2_X:
				if (thisSpecimen.getNoFSBlks() > 0) {
					dValue = wcode.getValueA();
					if (thisSpecimen.getNoFSBlks() > 1) {
						dValue += wcode.getValueB();
						if (thisSpecimen.getNoFSBlks() >= wcode.getCount()) {
							dValue += wcode.getValueC();
						}
					}
				}
				break;
			case LibConstants.RULE_SPECIMEN_1_2_PLUSX:
			case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
			case LibConstants.RULE_GROUP_1_2_PLUSX:
				if (thisSpecimen.getNoFSBlks() > 0) {
					dValue = wcode.getValueA();
					if (thisSpecimen.getNoFSBlks() > 1) {
						dValue += wcode.getValueB();
						if (thisSpecimen.getNoFSBlks() > wcode.getCount()) {
							dValue += (wcode.getValueC() * (thisSpecimen.getNoFSBlks() - wcode.getCount()));
						}
					}
				}
				break;
			default:
				caseCoder.setHasError(true);
				caseCoder.setErrorID(LibConstants.ERROR_CODING_RULE_UNKNOWN);
				comment = "ERROR: codeFrozenBlocks, " + thisCase.getCaseNo() + ", "
						+ LibConstants.ERROR_STRINGS[LibConstants.ERROR_CODING_RULE_UNKNOWN];
				caseCoder.setComment(comment);
				base.log(LibConstants.ERROR_CODING_RULE_UNKNOWN, name, comment);
			}
			if (dValue > 0) {
				specimenCoder.setValueFS(specimenCoder.getValueFS() + dValue);
				caseCoder.setValueFrozen(caseCoder.getValueFrozen() + dValue);
				caseCoder.setComment("Frozen Blocks: " + dValue + ", Specimen: "
						+ base.numbers.formatDouble(3, specimenCoder.getValueFS()) + ", Case: "
						+ base.numbers.formatDouble(3, caseCoder.getValueFrozen()));
			}
		}
		return codeBlocks;
	}

	private void codeMain() {
		wcode = coders.get(caseCoder.getCoderID());
		switch (wcode.getRuleID()) {
		case LibConstants.RULE_CASE_INCLUSIVE:
		case LibConstants.RULE_CASE_FIXED:
			caseCoder.setValue(wcode.getValueA());
			break;
		case LibConstants.RULE_CASE_GROSS_MICRO:
			if (thisCase.getNoSlides() > 0) {
				caseCoder.setValue(wcode.getValueB());
			} else {
				caseCoder.setValue(wcode.getValueA());
			}
			break;
		case LibConstants.RULE_CASE_BLOCKS_X_MIN_MAX:
			dValue = wcode.getValueA() * base.numbers.ceiling(thisCase.getNoBlocks(), wcode.getCount());
			caseCoder.setValue(base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC()));
			break;
		case LibConstants.RULE_CASE_BLOCKS_1_2_X:
			caseCoder.setValue(wcode.getValueA());
			if (thisCase.getNoBlocks() > 1) {
				caseCoder.setValue(caseCoder.getValue() + wcode.getValueB());
				if (thisCase.getNoBlocks() >= wcode.getCount()) {
					caseCoder.setValue(caseCoder.getValue() + wcode.getValueC());
				}
			}
			break;
		case LibConstants.RULE_CASE_BLOCKS_1_2_PLUSX:
			caseCoder.setValue(wcode.getValueA());
			if (thisCase.getNoBlocks() > 1) {
				caseCoder.setValue(caseCoder.getValue() + wcode.getValueB());
				if (thisCase.getNoBlocks() > wcode.getCount()) {
					caseCoder.setValue(caseCoder.getValue() + (wcode.getValueC() * (thisCase.getNoBlocks() - wcode.getCount())));
				}
			}
			break;
		default:
			// Case Fragments
			short counter = 0;
			for (short i = 0; i < thisCase.getNoSpecs(); i++) {
				counter += thisCase.getSpecimen(i).getNoFrags();
			}
			switch (wcode.getRuleID()) {
			case LibConstants.RULE_CASE_FRAGS_X_MIN_MAX:
				caseCoder.setValue(wcode.getValueA() * base.numbers.ceiling(counter, wcode.getCount()));
				caseCoder.setValue(base.numbers.minMax(caseCoder.getValue(), 0d, caseCoder.getValue(), wcode.getValueB(),
						wcode.getValueC()));
				break;
			case LibConstants.RULE_CASE_FRAGS_1_2_X:
				caseCoder.setValue(wcode.getValueA());
				if (counter > 1) {
					caseCoder.setValue(caseCoder.getValue() + wcode.getValueB());
					if (counter >= wcode.getCount()) {
						caseCoder.setValue(caseCoder.getValue() + wcode.getValueC());
					}
				}
				break;
			case LibConstants.RULE_CASE_FRAGS_1_2_PLUSX:
				caseCoder.setValue(wcode.getValueA());
				if (counter > 1) {
					caseCoder.setValue(caseCoder.getValue() + wcode.getValueB());
					if (counter > wcode.getCount()) {
						caseCoder.setValue(caseCoder.getValue() + (wcode.getValueC() * (counter - wcode.getCount())));
					}
				}
				break;
			default:
				// RULE_CASE_FRAGS_BLOCKS
				caseCoder.setValue(wcode.getValueA() * base.numbers.ceiling(counter, wcode.getCount()));
				dValue = wcode.getValueB() * base.numbers.ceiling(thisCase.getNoBlocks(), wcode.getCount());
				caseCoder.setComment("Fragments: " + counter + " - " + base.numbers.formatDouble(3, caseCoder.getValue())
						+ ", Blocks: " + thisCase.getNoBlocks() + " - " + base.numbers.formatDouble(3, dValue));
				if (caseCoder.getValue() < dValue) {
					// Use # blocks to get the higher value
					caseCoder.setValue(dValue);
				}
			}
		}
		caseCoder.setComment("Case Rule: " + wcode.getRuleID() + ", Value: " + base.numbers.formatDouble(3, caseCoder.getValue()));
	}

	private void codeOrders() {
		for (int i = 0; i < thisCase.getNoSpecs(); i++) {
			specimenCoder = specimenCoders.get(i);
			if (specimenCoder.isInclusive()) {
				continue;
			}
			for (Entry<Short, OrderCoderData> orderEntry : specimenCoder.getOrders().entrySet()) {
				ordersCoder = orderEntry.getValue();
				if (ordersCoder.getQty() == 0) {
					continue;
				}
				if (ordersCoder.isAddlBlock() && (!specimenCoder.isCodeBlocks() || !caseCoder.isCodeBlocks())) {
					continue;
				}
				wcode = coders.get(ordersCoder.getCodeID());
				switch (wcode.getRuleID()) {
				case LibConstants.RULE_AFTER_EVERY_X_MIN_MAX:
				case LibConstants.RULE_GROUP_EVERY_X_MIN_MAX:
				case LibConstants.RULE_UNIQUE_EVERY_X_MIN_MAX:
					ordersCoder.setValue(wcode.getValueA() * base.numbers.ceiling(ordersCoder.getQty(), wcode.getCount()));
					ordersCoder.setValue(base.numbers.minMax(ordersCoder.getValue(), wcode.getValueB(), wcode.getValueC()));
					break;
				case LibConstants.RULE_AFTER_1_2_X:
				case LibConstants.RULE_GROUP_1_2_X:
				case LibConstants.RULE_UNIQUE_1_2_X:
					ordersCoder.setValue(wcode.getValueA());
					if (ordersCoder.getQty() > 1) {
						ordersCoder.setValue(ordersCoder.getValue() + wcode.getValueB());
						if (ordersCoder.getQty() >= wcode.getCount()) {
							ordersCoder.setValue(ordersCoder.getValue() + wcode.getValueC());
						}
					}
					break;
				case LibConstants.RULE_AFTER_1_2_PLUSX:
				case LibConstants.RULE_GROUP_1_2_PLUSX:
				case LibConstants.RULE_UNIQUE_1_2_PLUSX:
					ordersCoder.setValue(wcode.getValueA());
					if (ordersCoder.getQty() > 1) {
						ordersCoder.setValue(ordersCoder.getValue() + wcode.getValueB());
						if (ordersCoder.getQty() > wcode.getCount()) {
							ordersCoder.setValue(ordersCoder.getValue() + (wcode.getValueC() * (ordersCoder.getQty() - wcode.getCount())));
						}
					}
					break;
				default:
					ordersCoder.setValue(wcode.getValueA() * ordersCoder.getQty());
				}
				specimenCoder.setValue(specimenCoder.getValue() + ordersCoder.getValue());
				caseCoder.setComment("codeOrders: Specimen " + (i + 1) + ", Orders: " + ordersCoder.getName() + ", Qty: "
						+ ordersCoder.getQty() + ", Value: " + base.numbers.formatDouble(3, ordersCoder.getValue())
						+ ", Case Value: " + base.numbers.formatDouble(3, caseCoder.getValue()));
				if (!caseCoder.isInclusive()) {
					caseCoder.setValue(caseCoder.getValue() + ordersCoder.getValue());
				}
			}
		}
	}

	private void codeSpecimens() {
		short spmID = 0, noSpecs = 0, noLinks = 0, noFrags = 0, noBlocks = 0, prevRule = 0;
		double dExpect = 0, dSpecs = 0, dLinks = 0, dFrags = 0, dBlocks = 0;
		for (short i = 0; i < thisCase.getNoSpecs(); i++) {
			specimenCoder = specimenCoders.get(i);
			thisSpecimen = thisCase.getSpecimen(i);
			wcode = coders.get(specimenCoder.getCoderID());
			switch (wcode.getRuleID()) {
			case LibConstants.RULE_IGNORE:
				// Nothing for this particular specimen (molecular, Derm IF, Iron Quant)
				break;
			case LibConstants.RULE_SPECIMEN_INCLUSIVE:
			case LibConstants.RULE_SPECIMEN_FIXED:
			case LibConstants.RULE_CASE_INCLUSIVE:
			case LibConstants.RULE_CASE_FIXED:
				// Example, autopsy, refer-out
				specimenCoder.setValue(wcode.getValueA());
				break;
			case LibConstants.RULE_SPECIMEN_EVERY_X_MIN_MAX:
				if (spmID != thisSpecimen.getSpmID() || prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					spmID = thisSpecimen.getSpmID();
					dSpecs = 0;
					noSpecs = 0;
				}
				noSpecs++;
				dExpect = wcode.getValueA() * base.numbers.ceiling(noSpecs, wcode.getCount());
				dValue = dExpect - dSpecs;
				specimenCoder.setValue(base.numbers.minMax(dValue, dSpecs, dExpect, wcode.getValueB(), wcode.getValueC()));
				dSpecs += specimenCoder.getValue();
				break;
			case LibConstants.RULE_SPECIMEN_1_2_X:
				if (spmID != thisSpecimen.getSpmID() || prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					spmID = thisSpecimen.getSpmID();
					noSpecs = 0;
				}
				noSpecs++;
				specimenCoder.setValue(wcode.getValueA());
				if (noSpecs > 1) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
					if (noSpecs > wcode.getCount()) {
						specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueC());
					}
				}
				break;
			case LibConstants.RULE_SPECIMEN_1_2_PLUSX:
				if (spmID != thisSpecimen.getSpmID() || prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					spmID = thisSpecimen.getSpmID();
					noSpecs = 0;
				}
				noSpecs++;
				specimenCoder.setValue(wcode.getValueA());
				if (noSpecs > 1) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
					if (noSpecs > wcode.getCount()) {
						specimenCoder.setValue(specimenCoder.getValue() + (wcode.getValueC() * (noSpecs - wcode.getCount())));
					}
				}
				break;
			case LibConstants.RULE_SPECIMEN_GROSS_MICRO:
			case LibConstants.RULE_CASE_GROSS_MICRO:
				// Femoral heads
				if (thisSpecimen.getNoSlides() > 0) {
					specimenCoder.setValue(wcode.getValueB());
				} else {
					specimenCoder.setValue(wcode.getValueA());
				}
				break;
			case LibConstants.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
			case LibConstants.RULE_CASE_FRAGS_X_MIN_MAX:
				// CAP Medical GI biopsies
				dValue = wcode.getValueA() * base.numbers.ceiling(thisSpecimen.getNoFrags(), wcode.getCount());
				specimenCoder.setValue(base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC()));
				break;
			case LibConstants.RULE_SPECIMEN_FRAGS_1_2_X:
			case LibConstants.RULE_CASE_FRAGS_1_2_X:
				specimenCoder.setValue(wcode.getValueA());
				if (thisSpecimen.getNoFrags() > 1) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
					if (thisSpecimen.getNoFrags() >= wcode.getCount()) {
						specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueC());
					}
				}
				break;
			case LibConstants.RULE_SPECIMEN_FRAGS_1_2_PLUSX:
			case LibConstants.RULE_CASE_FRAGS_1_2_PLUSX:
				specimenCoder.setValue(wcode.getValueA());
				if (thisSpecimen.getNoFrags() > 1) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
					if (thisSpecimen.getNoFrags() > wcode.getCount()) {
						specimenCoder.setValue(specimenCoder.getValue() + (wcode.getValueC() * (thisSpecimen.getNoFrags() - wcode.getCount())));
					}
				}
				break;
			case LibConstants.RULE_SPECIMEN_FRAGS_BLOCKS:
			case LibConstants.RULE_CASE_FRAGS_BLOCKS:
				// CAP GI Polyps, skin resections
				specimenCoder.setValue(wcode.getValueA() * base.numbers.ceiling(thisSpecimen.getNoFrags(), wcode.getCount()));
				dValue = wcode.getValueB() * base.numbers.ceiling(thisSpecimen.getNoBlocks(), wcode.getCount());
				if (specimenCoder.getValue() < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.setValue(dValue);
				}
				break;
			case LibConstants.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
			case LibConstants.RULE_CASE_BLOCKS_X_MIN_MAX:
				dValue = wcode.getValueA() * base.numbers.ceiling(thisSpecimen.getNoBlocks(), wcode.getCount());
				specimenCoder.setValue(base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC()));
				break;
			case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_X:
			case LibConstants.RULE_CASE_BLOCKS_1_2_X:
				specimenCoder.setValue(wcode.getValueA());
				if (thisSpecimen.getNoBlocks() > 1) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
					if (thisSpecimen.getNoBlocks() >= wcode.getCount()) {
						specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueC());
					}
				}
				break;
			case LibConstants.RULE_SPECIMEN_BLOCKS_1_2_PLUSX:
			case LibConstants.RULE_CASE_BLOCKS_1_2_PLUSX:
				specimenCoder.setValue(wcode.getValueA());
				if (thisSpecimen.getNoBlocks() > 1) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
					if (thisSpecimen.getNoBlocks() > wcode.getCount()) {
						specimenCoder.setValue(specimenCoder.getValue() + (wcode.getValueC() * (thisSpecimen.getNoBlocks() - wcode.getCount())));
					}
				}
				break;
			case LibConstants.RULE_LINKED_INCLUSIVE:
			case LibConstants.RULE_LINKED_FIXED:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					noLinks = 0;
				}
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.setValue(wcode.getValueA());
				}
				break;
			case LibConstants.RULE_LINKED_FRAGS_X_MIN_MAX:
				// CAP Breast/Prostate Bx (max 20 cores per case = 10 L4E)
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.getNoFrags();
				dExpect = wcode.getValueA() * base.numbers.ceiling(noFrags, wcode.getCount());
				dValue = dExpect - dFrags;
				specimenCoder.setValue(base.numbers.minMax(dValue, dLinks, dExpect, wcode.getValueB(), wcode.getValueC()));
				dFrags += specimenCoder.getValue();
				break;
			case LibConstants.RULE_LINKED_FRAGS_1_2_X:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.getNoFrags();
				if (noFrags >= 1 && dFrags < wcode.getValueA()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueA());
				}
				if (noFrags >= 2 && dFrags < wcode.getValueA() + wcode.getValueB()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
				}
				if (noFrags >= wcode.getCount() && dFrags < wcode.getValueA() + wcode.getValueB() + wcode.getValueC()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueC());
				}
				dFrags += specimenCoder.getValue();
				break;
			case LibConstants.RULE_LINKED_FRAGS_1_2_PLUSX:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.getNoFrags();
				dExpect = wcode.getValueA();
				for (int j = 2; j <= wcode.getCount(); j++) {
					if (j <= noFrags) {
						dExpect += wcode.getValueB();
					}
				}
				if (noFrags > wcode.getCount()) {
					dExpect += (wcode.getValueC() * (noFrags -wcode.getCount()));
				}
				specimenCoder.setValue(dExpect - dFrags);
				dFrags = dExpect;
				break;
			case LibConstants.RULE_LINKED_FRAGS_BLOCKS:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dBlocks = 0;
					dFrags = 0;
					noFrags = 0;
					noBlocks = 0;
				}
				noFrags += thisSpecimen.getNoFrags();
				noBlocks += thisSpecimen.getNoBlocks();
				specimenCoder.setValue((wcode.getValueA() * base.numbers.ceiling(noFrags, wcode.getCount())) - dFrags);
				dValue = (wcode.getValueB() * base.numbers.ceiling(noBlocks, wcode.getCount())) - dBlocks;
				dFrags += specimenCoder.getValue();
				dBlocks += dValue;
				if (specimenCoder.getValue() < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.setValue(dValue);
				}
			case LibConstants.RULE_LINKED_BLOCKS_X_MIN_MAX:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.getNoBlocks();
				dExpect = wcode.getValueA() * base.numbers.ceiling(noBlocks, wcode.getCount());
				dValue = dExpect - dBlocks;
				specimenCoder.setValue(base.numbers.minMax(dValue, dBlocks, dExpect, wcode.getValueB(), wcode.getValueC()));
				dBlocks += specimenCoder.getValue();
				break;
			case LibConstants.RULE_LINKED_BLOCKS_1_2_X:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.getNoBlocks();
				if (noBlocks >= 1 && dBlocks < wcode.getValueA()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueA());
				}
				if (noBlocks >= 2 && dBlocks < wcode.getValueA() + wcode.getValueB()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
				}
				if (noBlocks >= wcode.getCount() && dBlocks < wcode.getValueA() + wcode.getValueB() + wcode.getValueC()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueC());
				}
				dBlocks += specimenCoder.getValue();
				break;
			case LibConstants.RULE_LINKED_BLOCKS_1_2_PLUSX:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.getNoBlocks();
				if (noBlocks >= 1 && dBlocks < wcode.getValueA()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueA());
				}
				if (noBlocks >= 2 && dBlocks < wcode.getValueA() + wcode.getValueB()) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
				}
				if (noBlocks > wcode.getCount()
						&& dBlocks < wcode.getValueA() + wcode.getValueB() + (wcode.getValueC() * (noBlocks - wcode.getCount()))) {
					specimenCoder.setValue(specimenCoder.getValue() + (wcode.getValueC() * (noBlocks - wcode.getCount())));
				}
				dBlocks += specimenCoder.getValue();
				break;
			case LibConstants.RULE_LINKED_EVERY_X_MIN_MAX:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					dLinks = 0;
					noLinks = 0;
				}
				noLinks++;
				dExpect = wcode.getValueA() * base.numbers.ceiling(noLinks, wcode.getCount());
				dValue = dExpect - dLinks;
				specimenCoder.setValue(base.numbers.minMax(dValue, dLinks, dExpect, wcode.getValueB(), wcode.getValueC()));
				dLinks += specimenCoder.getValue();
				break;
			case LibConstants.RULE_LINKED_1_2_X:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					noLinks = 0;
				}
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.setValue(wcode.getValueA());
				} else if (noLinks == 2) {
					specimenCoder.setValue(wcode.getValueB());
				} else if (noLinks == wcode.getCount()) {
					specimenCoder.setValue(wcode.getValueC());
				}
				break;
			case LibConstants.RULE_LINKED_1_2_PLUSX:
				if (prevRule != wcode.getRuleID()) {
					prevRule = wcode.getRuleID();
					noLinks = 0;
				}
				noLinks++;
				specimenCoder.setValue(wcode.getValueA());
				if (noLinks > 1) {
					specimenCoder.setValue(specimenCoder.getValue() + wcode.getValueB());
					if (noLinks > wcode.getCount()) {
						specimenCoder.setValue(specimenCoder.getValue() + (wcode.getValueC() * (noLinks - wcode.getCount())));
					}
				}
				break;
			default:
				caseCoder.setHasError(true);
				caseCoder.setErrorID(LibConstants.ERROR_CODING_RULE_UNKNOWN);
			}
			caseCoder.setValue(caseCoder.getValue() + specimenCoder.getValue());
			caseCoder.setComment("Specimen " + (i + 1) + ": Links " + noLinks
					+ ", Rule " + wcode.getRuleID()
					+ ", Value " + base.numbers.formatDouble(3, specimenCoder.getValue())
					+ ", Case " + base.numbers.formatDouble(3, caseCoder.getValue()));
		}
	}

	private void codeSynoptics() {
		dValue = 0;
		wcode = coders.get(RULE_SYNOPTICS);
		switch (wcode.getRuleID()) {
		case LibConstants.RULE_IGNORE:
			// RCP-UK
			break;
		case LibConstants.RULE_CASE_INCLUSIVE:
		case LibConstants.RULE_GROUP_CASE_INCLUSIVE:
		case LibConstants.RULE_UNIQUE_CASE_INCLUSIVE:
		case LibConstants.RULE_AFTER_CASE_INCLUSIVE:
			dValue = wcode.getValueA();
			break;
		case LibConstants.RULE_CASE_FIXED:
			dValue = caseCoder.getValue() * thisCase.getNoSynop();
			break;
		case LibConstants.RULE_SPECIMEN_INCLUSIVE:
		case LibConstants.RULE_SPECIMEN_FIXED:
		case LibConstants.RULE_LINKED_INCLUSIVE:
		case LibConstants.RULE_LINKED_FIXED:
		case LibConstants.RULE_GROUP_SPECIMEN_INCLUSIVE:
		case LibConstants.RULE_UNIQUE_SPECIMEN_INCLUSIVE:
		case LibConstants.RULE_AFTER_SPECIMEN_INCLUSIVE:
			if (thisCase.getNoSpecs() > thisCase.getNoSynop()) {
				dValue = caseCoder.getValue() * thisCase.getNoSynop();
			} else {
				dValue = caseCoder.getValue() * thisCase.getNoSpecs();
			}
			break;
		case LibConstants.RULE_SPECIMEN_EVERY_X_MIN_MAX:
		case LibConstants.RULE_LINKED_EVERY_X_MIN_MAX:
		case LibConstants.RULE_GROUP_EVERY_X_MIN_MAX:
		case LibConstants.RULE_UNIQUE_EVERY_X_MIN_MAX:
		case LibConstants.RULE_AFTER_EVERY_X_MIN_MAX:
			// W2Q
			dValue = wcode.getValueA() * base.numbers.ceiling(thisCase.getNoSynop(), wcode.getCount());
			dValue = base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC());
			break;
		case LibConstants.RULE_SPECIMEN_1_2_X:
		case LibConstants.RULE_LINKED_1_2_X:
		case LibConstants.RULE_GROUP_1_2_X:
		case LibConstants.RULE_UNIQUE_1_2_X:
		case LibConstants.RULE_AFTER_1_2_X:
			dValue = wcode.getValueA();
			if (thisCase.getNoSynop() > 1) {
				// 2nd synoptic report (0=ignore)
				dValue += wcode.getValueB();
				if (thisCase.getNoSynop() >= wcode.getCount()) {
					// synoptic report X
					dValue += wcode.getValueC();
				}
			}
			break;
		case LibConstants.RULE_SPECIMEN_1_2_PLUSX:
		case LibConstants.RULE_LINKED_1_2_PLUSX:
		case LibConstants.RULE_GROUP_1_2_PLUSX:
		case LibConstants.RULE_UNIQUE_1_2_PLUSX:
		case LibConstants.RULE_AFTER_1_2_PLUSX:
			dValue = wcode.getValueA();
			if (thisCase.getNoSynop() > 1) {
				// 2nd synoptic report (0=ignore)
				dValue += wcode.getValueB();
				if (thisCase.getNoSynop() > wcode.getCount()) {
					// 3+ synoptic reports
					dValue += (wcode.getValueC() * (thisCase.getNoSynop() - wcode.getCount()));
				}
			}
			break;
		default:
			caseCoder.setHasError(true);
			caseCoder.setErrorID(LibConstants.ERROR_CODING_RULE_UNKNOWN);
			comment = "ERROR: codeSynoptics, " + thisCase.getCaseNo() + ", " + LibConstants.ERROR_STRINGS[LibConstants.ERROR_CODING_RULE_UNKNOWN];
			caseCoder.setComment(comment);
			base.log(LibConstants.ERROR_CODING_RULE_UNKNOWN, name, comment);
		}
		specimenCoder = specimenCoders.get(caseCoder.getMainSpec());
		specimenCoder.setValue(specimenCoder.getValue() + dValue);
		caseCoder.setValue(caseCoder.getValue() + dValue);
		caseCoder.setComment("Synoptic: Rule " + wcode.getRuleID() + ", Value " + base.numbers.formatDouble(3, dValue)
				+ ", Case " + base.numbers.formatDouble(3, caseCoder.getValue()));
	}

	@Override
	double getAddl(short codeID, short qty) {
		wcode = coders.get(codeID);
		switch (wcode.getRuleID()) {
		case LibConstants.RULE_ADDL_CASE_INCLUSIVE:
		case LibConstants.RULE_ADDL_SPECIMEN_INCLUSIVE:
			dValue = wcode.getValueA();
			break;
		case LibConstants.RULE_ADDL_EVERY_X_MIN_MAX:
			dValue = wcode.getValueA() * base.numbers.ceiling(qty, wcode.getCount());
			dValue = base.numbers.minMax(dValue, wcode.getValueB(), wcode.getValueC());
			break;
		case LibConstants.RULE_ADDL_1_2_X:
			dValue = wcode.getValueA();
			if (qty > 1) {
				dValue += wcode.getValueB();
				if (qty >= wcode.getCount()) {
					dValue += wcode.getValueC();
				}
			}
			break;
		case LibConstants.RULE_ADDL_1_2_PLUSX:
			dValue = wcode.getValueA();
			if (qty > 1) {
				dValue += wcode.getValueB();
				if (qty > wcode.getCount()) {
					dValue += (wcode.getValueC() * (qty - wcode.getCount()));
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
		return (name + "\n" + caseCoder.getComment() + "--------------------------\n");
	}

	@Override
	double getCorrelations() {
		wcode = coders.get(RULE_CORRELATIONS);
		if (wcode != null) {
			if (wcode.getValueA() > MAX_VALUE) {
				return MAX_VALUE;
			}
			return wcode.getValueA();
		}
		return 0;
	}

	private void getData(byte index, DBPowerj dbPowerJ) {
		ArrayList<CoderData> temp = dbPowerJ.getCoder(index);
		for (int i = 0; i < temp.size(); i++) {
			coders.put(temp.get(i).getCodeID(), temp.get(i));
		}
		temp.clear();
	}

	@Override
	double getFrozen() {
		if (caseCoder.getValueFrozen() > MAX_VALUE) {
			return MAX_VALUE;
		}
		return caseCoder.getValueFrozen();
	}

	@Override
	double getFrozen(int specimenNo) {
		if (specimenCoders.get(specimenNo).getValueFS() > MAX_VALUE) {
			return MAX_VALUE;
		}
		return specimenCoders.get(specimenNo).getValueFS();
	}

	@Override
	double getOrder(int specimenNo, short groupID) {
		ordersCoder = specimenCoders.get(specimenNo).getOrder(groupID);
		if (ordersCoder != null) {
			if (ordersCoder.getValue() > MAX_VALUE) {
				return MAX_VALUE;
			}
			return ordersCoder.getValue();
		}
		return 0;
	}

	@Override
	double getValue() {
		if (caseCoder.getValue() > MAX_VALUE) {
			return MAX_VALUE;
		}
		return caseCoder.getValue();
	}

	@Override
	double getValue(int specimenNo) {
		if (specimenCoders.get(specimenNo).getValue() > MAX_VALUE) {
			return MAX_VALUE;
		}
		return specimenCoders.get(specimenNo).getValue();
	}

	@Override
	boolean hasComment() {
		return (caseCoder.getComment().length() > 0);
	}

	@Override
	boolean hasError() {
		return caseCoder.hasError();
	}

	@Override
	boolean isOrderUnique(short orderID) {
		return ordersCoder.isOrderUnique(orderID);
	}

	@Override
	boolean needsFragments(int specimenNo) {
		return specimenCoders.get(specimenNo).isNeedFrag();
	}

	@Override
	void newCase(CaseData thisCase) {
		specimenCoders.clear();
		caseCoder = new CaseCoderData();
		thisSpecimen = new SpecimenData();
		specimenCoder = new SpecimenCodeData();
		ordersCoder = new OrderCoderData();
		this.thisCase = thisCase;
	}
}