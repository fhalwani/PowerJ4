package ca.powerj.lib;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map.Entry;
import ca.powerj.app.PJServer;
import ca.powerj.data.AccessionsList;
import ca.powerj.data.CaseData;
import ca.powerj.data.AdditionalOrderData;
import ca.powerj.data.FacilityList;
import ca.powerj.data.FrozenData;
import ca.powerj.data.OrderData;
import ca.powerj.data.OrderMasterList;
import ca.powerj.data.SpecimenData;
import ca.powerj.data.SpecimensList;

public class LibFinals {
	static final byte ADDL_ADEND = 1;
	static final byte ADDL_COREL = 2;
	static final byte ADDL_ORDER = 3;
	private int value5fs = 0;
	private int maxFte5 = 0;
	private long lastUpdate = 0;
	private long maxDate = 0;
	private final String className = "Workload";
	private String comment = "";
	private FacilityList facilities;
	private AccessionsList accessions;
	private OrderMasterList masterOrders;
	private SpecimensList masterSpecimens;
	private CaseData thisCase = new CaseData();
	private SpecimenData thisSpecimen = new SpecimenData();
	private OrderData thisOrder = new OrderData();
	private LibInactive coder1, coder2, coder3, coder4;
	private PJServer base;

	public LibFinals(boolean debug, String caseNo, PJServer base) {
		this.base = base;
		base.setBusy(true);
		base.log(LibConstants.ERROR_NONE, className,
				base.dates.formatter(LibDates.FORMAT_DATETIME) + " - Workload Manager Started...");
		base.dbPath.setStatements(LibConstants.ACTION_LLOAD);
		if (base.errorID == LibConstants.ERROR_NONE) {
			base.dbPowerJ.setStatements(LibConstants.ACTION_LLOAD);
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			accessions = new AccessionsList(base.dbPowerJ);
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			facilities = new FacilityList(base.dbPowerJ);
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			masterOrders = new OrderMasterList(base.dbPowerJ);
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			masterSpecimens = new SpecimensList(base.dbPowerJ);
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			if (base.setup.getBoolean(LibSetup.VAR_CODER1_ACTIVE)) {
				coder1 = new LibWorkload(LibConstants.ACTION_CODER1, base, base.getProperty("coder1"), base.dbPowerJ);
			} else {
				coder1 = new LibInactive(base.getProperty("coder1"));
			}
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			if (base.setup.getBoolean(LibSetup.VAR_CODER2_ACTIVE)) {
				coder2 = new LibWorkload(LibConstants.ACTION_CODER2, base, base.getProperty("coder2"), base.dbPowerJ);
			} else {
				coder2 = new LibInactive(base.getProperty("coder2"));
			}
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			if (base.setup.getBoolean(LibSetup.VAR_CODER3_ACTIVE)) {
				coder3 = new LibWorkload(LibConstants.ACTION_CODER3, base, base.getProperty("coder3"), base.dbPowerJ);
			} else {
				coder3 = new LibInactive(base.getProperty("coder3"));
			}
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			if (base.setup.getBoolean(LibSetup.VAR_CODER4_ACTIVE)) {
				coder4 = new LibWorkload(LibConstants.ACTION_CODER4, base, base.getProperty("coder4"), base.dbPowerJ);
			} else {
				coder4 = new LibInactive(base.getProperty("coder4"));
			}
		}
		if (base.errorID == LibConstants.ERROR_NONE) {
			value5fs = base.setup.getInt(LibSetup.VAR_V5_FROZEN);
			maxFte5 = base.setup.getInt(LibSetup.VAR_V5_FTE) / 215;
		}
		if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
			// Do the work
			if (debug) {
				// For Debug of many cases
				debug();
			} else if (caseNo.length() > 0) {
				// For Debug of 1 case
				getCase(caseNo);
			} else {
				getLastCase();
				getMaxDate();
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					base.log(LibConstants.ERROR_NONE, "Coding cases...");
					doCases();
				}
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					getLastAdditional(ADDL_ADEND);
				}
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					base.log(LibConstants.ERROR_NONE, "Coding addenda...");
					doAdenda();
				}
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					getLastAdditional(ADDL_ORDER);
				}
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					base.log(LibConstants.ERROR_NONE, "Coding additionals...");
					doAdditional();
				}
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					getLastAdditional(ADDL_COREL);
				}
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					base.log(LibConstants.ERROR_NONE, "Coding correlations...");
					doCorrelations();
				}
				if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
					base.log(LibConstants.ERROR_NONE, "Coding errors...");
					doErrors();
				}
			}
		}
		close();
	}

	private void close() {
		if (masterSpecimens != null) {
			masterSpecimens.close();
		}
		if (masterOrders != null) {
			masterOrders.close();
		}
		if (facilities != null) {
			facilities.close();
		}
		if (accessions != null) {
			accessions.close();
		}
		if (coder1 != null) {
			coder1.close();
		}
		if (coder2 != null) {
			coder2.close();
		}
		if (coder3 != null) {
			coder3.close();
		}
		if (coder4 != null) {
			coder4.close();
		}
		if (base.dbPath != null) {
			base.dbPath.closeStms();
		}
		if (base.dbPowerJ != null) {
			base.dbPowerJ.closeStms();
		}
		base.setBusy(false);
		base.log(LibConstants.ERROR_NONE, "Done coding cases...");
	}

	private void debug() {
		int noCases = 0;
		String filter = " WHERE fned BETWEEN '2021-02-01' AND '2021-02-015'";
		ArrayList<CaseData> cases = base.dbPowerJ.getCases(filter);
		ArrayList<SpecimenData> specimens = new ArrayList<SpecimenData>();
		CaseData oldCase = new CaseData();
		for (int i = 0; i < cases.size(); i++) {
			oldCase = cases.get(i);
			if (doCase(oldCase.getCaseID())) {
				if (base.errorID == LibConstants.ERROR_NONE) {
					thisCase.setValue1(coder1.getValue());
					thisCase.setValue2(coder2.getValue());
					thisCase.setValue3(coder3.getValue());
					thisCase.setValue4(coder4.getValue());
					if (oldCase.getValue1() != thisCase.getValue1()
							|| oldCase.getValue2() != thisCase.getValue2()
							|| oldCase.getValue3() != thisCase.getValue3()
							|| oldCase.getValue4() != thisCase.getValue4()) {
						System.out.printf("%1$s: %n", cases.get(i).getCaseNo());
						System.out.printf("\t CAP %1$.2f = %2$.2f %n", oldCase.getValue1(), coder1.getValue());
						System.out.printf("\t W2Q %1$.2f = %2$.2f %n", oldCase.getValue2(), coder2.getValue());
						System.out.printf("\t RCP %1$.2f = %2$.2f %n", oldCase.getValue3(), coder3.getValue());
						System.out.printf("\t CPT %1$.2f = %2$.2f %n", oldCase.getValue4(), coder4.getValue());
						System.out.printf("\t CD5 %1$d = %2$d %n", oldCase.getValue5(), thisCase.getValue5());
					}
					specimens = base.dbPowerJ.getSpecimens(oldCase.getCaseID());
					for (byte j = 0; j < specimens.size(); j++) {
						if (coder1.getValue(j) != specimens.get(j).getValue1()) {
							System.out.printf("%1$d: CAP %2$.2f = %3$.2f %n", j, specimens.get(j).getValue1(), coder1.getValue(j));
						}
						if (coder2.getValue(j) != specimens.get(j).getValue2()) {
							System.out.printf("%1$d: W2Q %2$.2f = %3$.2f %n", j, specimens.get(j).getValue2(), coder2.getValue(j));
						}
						if (coder3.getValue(j) != specimens.get(j).getValue3()) {
							System.out.printf("%1$d: RCP %2$.2f = %3$.2f %n", j, specimens.get(j).getValue3(), coder3.getValue(j));
						}
						if (coder4.getValue(j) != specimens.get(j).getValue4()) {
							System.out.printf("%1$d: CPT %2$.2f = %3$.2f %n", j, specimens.get(j).getValue4(), coder4.getValue(j));
						}
					}
				}
			}
			noCases++;
			if (noCases > 0 && noCases % 10 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException e) {}
			}
		}
		cases.clear();
	}

	private boolean dejavu() {
		if (base.dbPowerJ.caseExists(thisCase)) {
			thisSpecimen = thisCase.getSpecimen(0);
			if (masterSpecimens.matchSpecimens(thisSpecimen.getSpmID())) {
				return true;
			}
			
		}
		return false;
	}

	private void doAdditional() {
		final short qty = 1;
		int noCases = 0;
		long startTime = System.currentTimeMillis();
		ArrayList<AdditionalOrderData> reviews = base.dbPath.getAdditionals(lastUpdate, maxDate, masterOrders);
		AdditionalOrderData review = new AdditionalOrderData();
		thisCase = new CaseData();
		for (int i = 0; i < reviews.size(); i++) {
			review = reviews.get(i);
			if (thisCase.getCaseID() != review.getCaseID()) {
				thisCase = new CaseData();
				thisCase.setCaseID(review.getCaseID());
				// Must exist in PowerJ in Cases Table
				if (!dejavu()) {
					continue;
				}
			}
			thisCase.setFinaled(review.getFinalTime());
			thisCase.setFinalID(review.getFinalID());
			// Cannot exist in PowerJ in Additionals Table
			if (!base.dbPowerJ.isDuplicate(review.getProID(), thisCase)) {
				if (masterOrders.matchOrder(review.getProID())) {
					thisCase.setTypeID(review.getProID());
					thisCase.setValue1(coder1.getAddl(masterOrders.getCodeID(1), qty));
					thisCase.setValue2(coder2.getAddl(masterOrders.getCodeID(2), qty));
					thisCase.setValue3(coder3.getAddl(masterOrders.getCodeID(3), qty));
					thisCase.setValue4(coder4.getAddl(masterOrders.getCodeID(4), qty));
					thisCase.setValue5(masterOrders.getCodeID(5));
					if (thisCase.getValue1() > 0.001 || thisCase.getValue2() > 0.001
							|| thisCase.getValue3() > 0.001 || thisCase.getValue4() > 0.001
							|| thisCase.getValue5() > 0) {
						base.log(LibConstants.ERROR_NONE, className, "Coding Additionals on case " + thisCase.getCaseID());
						if (base.dbPowerJ.setAdditional(thisCase) > 0) {
							noCases++;
						}
					}
				}
			}
			if (base.errorID != LibConstants.ERROR_NONE || base.isStopping()) {
				break;
			} else if (noCases > 0 && noCases % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
		}
		long noSeconds = (System.currentTimeMillis() - startTime) / 1000;
		if (noCases > 0 && noSeconds > 0) {
			base.log(LibConstants.ERROR_NONE, className, "Workload Coded " + noCases + " additional orders in "
					+ noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
		}
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	private void doAdenda() {
		short orderID = 0;
		int noCases = 0;
		long startTime = System.currentTimeMillis();
		ArrayList<CaseData> cases = base.dbPath.getAddenda(lastUpdate, maxDate);
		ArrayList<OrderData> orders = new ArrayList<OrderData>();
		for (int i = 0; i < cases.size(); i++) {
			thisCase = cases.get(i);
			// Must exist in PowerJ in Cases Table
			if (dejavu()) {
				// Cannot exist in PowerJ in Additional Table
				if (!base.dbPowerJ.isDuplicate(ADDL_ADEND, thisCase)) {
					base.log(LibConstants.ERROR_NONE, className, "Coding "
							+ thisCase.getStatusName() + ": " + thisCase.getCaseNo());
					coder1.newCase(thisCase);
					coder2.newCase(thisCase);
					coder3.newCase(thisCase);
					coder4.newCase(thisCase);
					coder1.addSpecimen(thisSpecimen, masterSpecimens.getCoderID(0, 0),
							masterSpecimens.getCoderID(1, 0), masterSpecimens.getCoderID(2, 0));
					coder2.addSpecimen(thisSpecimen, masterSpecimens.getCoderID(0, 1),
							masterSpecimens.getCoderID(1, 1), masterSpecimens.getCoderID(2, 1));
					coder3.addSpecimen(thisSpecimen, masterSpecimens.getCoderID(0, 2),
							masterSpecimens.getCoderID(1, 2), masterSpecimens.getCoderID(2, 2));
					coder4.addSpecimen(thisSpecimen, masterSpecimens.getCoderID(0, 3),
							masterSpecimens.getCoderID(1, 3), masterSpecimens.getCoderID(2, 3));
					orders = base.dbPath.getCaseOrders(thisCase.getCaseID(),
							thisCase.getAccessTime(), thisCase.getFinalTime());
					for (int j = 0; j < orders.size(); j++) {
						orderID = orders.get(j).getOrmID();
						if (masterOrders.getOrderType() != LibConstants.ORDER_TYPE_IGNORE) {
							thisOrder = thisSpecimen.getOrder(masterOrders.getGroupID());
							if (thisOrder == null) {
								thisOrder = new OrderData();
								thisOrder.setOrgID(masterOrders.getGroupID());
								thisSpecimen.setOrder(masterOrders.getGroupID(), thisOrder);
							}
							thisOrder.setQty(thisOrder.getQty() + orders.get(j).getQty());
							coder1.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(1),
									thisOrder.getQty(), false, false, 0);
							coder2.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(2),
									thisOrder.getQty(), false, false, 0);
							coder3.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(3),
									thisOrder.getQty(), false, false, 0);
							coder4.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(4),
									thisOrder.getQty(), false, false, 0);
						}
					}
					coder1.codeCase();
					coder2.codeCase();
					coder3.codeCase();
					coder4.codeCase();
					thisCase.setTypeID(ADDL_ADEND);
					thisCase.setValue1(coder1.getValue());
					thisCase.setValue2(coder2.getValue());
					thisCase.setValue3(coder3.getValue());
					thisCase.setValue4(coder4.getValue());
					thisCase.setValue5(0);
					if (thisCase.getValue1() > 0.001 || thisCase.getValue2() > 0.001
							|| thisCase.getValue3() > 0.001 || thisCase.getValue4() > 0.001
							|| thisCase.getValue5() > 0) {
						if (base.dbPowerJ.setAdditional(thisCase) > 0) {
							noCases++;
						}
					}
					if (base.errorID != LibConstants.ERROR_NONE || base.isStopping()) {
						break;
					} else if (noCases > 0 && noCases % 100 == 0) {
						try {
							Thread.sleep(LibConstants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
		long noSeconds = (System.currentTimeMillis() - startTime) / 1000;
		if (noCases > 0 && noSeconds > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					"Workload Coded " + base.numbers.formatNumber(noCases) + " amend/addend in "
							+ base.numbers.formatNumber(noSeconds) + " seconds ("
							+ base.numbers.formatNumber((noCases * 60 / noSeconds)) + "/min)");
		}
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	private boolean doCase(long caseID) {
		thisCase = base.dbPath.getCaseDetails(caseID);
		if (accessions.doworkflow(thisCase.getTypeID())
				&& facilities.doworkflow(thisCase.getFacID())) {
			base.log(LibConstants.ERROR_NONE, className, "Coding case " + thisCase.getCaseNo());
			thisCase.setSpyID(accessions.getSpecialty());
			thisCase.setCodeSpec(accessions.codeSpecimen());
			// We need to know if the case is malignant before-hand
			thisCase.setNoSynop(base.dbPath.getNoSynoptics(caseID));
			coder1.newCase(thisCase);
			coder2.newCase(thisCase);
			coder3.newCase(thisCase);
			coder4.newCase(thisCase);
			base.dbPath.getGrossed(thisCase);
			base.dbPath.getEmbedded(thisCase);
			base.dbPath.getMicrotomed(thisCase);
			base.dbPath.getRouted(thisCase);
			base.dbPath.getScanned(thisCase);
			getSpecimens();
			if (thisCase.isHasFrag()) {
				// Extract # of fragments from the gross description
				for (short i = 0; i < thisCase.getNoSpecs(); i++) {
					if (coder1.needsFragments(i) || coder2.needsFragments(i)
							|| coder3.needsFragments(i) || coder4.needsFragments(i)) {
						// default 1 fragment
						thisCase.getSpecimen(i).setNoFrags(1);
					}
				}
				// extract from gross description
				base.dbPath.getNoFrags(thisCase);
			}
			coder1.codeCase();
			coder2.codeCase();
			coder3.codeCase();
			coder4.codeCase();
			return true;
		}
		return false;
	}

	private void doCases() {
		int noCases = 0;
		long caseID = 0;
		long startTime = System.currentTimeMillis();
		ArrayList<Long> finals = base.dbPath.getFinaled(lastUpdate, maxDate);
		for (int i = 0; i < finals.size(); i++) {
			caseID = finals.get(i);
			if (doCase(caseID)) {
				if (base.errorID == LibConstants.ERROR_NONE) {
					saveCase(true);
					noCases++;
				}
			}
			if (base.errorID != LibConstants.ERROR_NONE || base.isStopping()) {
				break;
			} else if (noCases > 0 && noCases % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
		}
		long noSeconds = (System.currentTimeMillis() - startTime) / 1000;
		if (noCases > 0 && noSeconds > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					"Workload Coded " + base.numbers.formatNumber(noCases) + " new cases in "
							+ base.numbers.formatNumber(noSeconds) + " seconds ("
							+ base.numbers.formatNumber((noCases * 60 / noSeconds)) + "/min)");
		}
		finals.clear();
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	private void doCorrelations() {
		int noCases = 0;
		long startTime = System.currentTimeMillis();
		double dValue1 = coder1.getCorrelations();
		double dValue2 = coder2.getCorrelations();
		double dValue3 = coder3.getCorrelations();
		double dValue4 = coder4.getCorrelations();
		if (dValue1 < 0.001 && dValue2 < 0.001 && dValue3 < 0.001 && dValue4 < 0.001) {
			return;
		}
		ArrayList<CaseData> correlations = base.dbPath.getCorrelations(lastUpdate, maxDate);
		for (int i = 0; i < correlations.size(); i++) {
			if (thisCase.getCaseID() != correlations.get(i).getCaseID()) {
				thisCase = correlations.get(i);
				// Must exist in PowerJ in Cases Table
				if (!dejavu()) {
					continue;
				}
			}
			thisCase.setFinaled(correlations.get(i).getFinalTime());
			thisCase.setFinalID(correlations.get(i).getFinalID());
			if (!base.dbPowerJ.isDuplicate(ADDL_COREL, thisCase)) {
				base.log(LibConstants.ERROR_NONE, className, "Coding Correlation on case " + thisCase.getCaseNo());
				thisCase.setTypeID(ADDL_COREL);
				thisCase.setValue1(dValue1);
				thisCase.setValue2(dValue1);
				thisCase.setValue3(dValue1);
				thisCase.setValue4(dValue1);
				thisCase.setValue5(0);
				if (base.dbPowerJ.setAdditional(thisCase) > 0) {
					noCases++;
				}
			}
			if (base.errorID != LibConstants.ERROR_NONE || base.isStopping()) {
				break;
			} else if (noCases > 0 && noCases % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
		}
		long noSeconds = (System.currentTimeMillis() - startTime) / 1000;
		if (noCases > 0 && noSeconds > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					"Workload Coded " + base.numbers.formatNumber(noCases) + " correlations in "
					+ base.numbers.formatNumber(noSeconds) + " seconds ("
					+ base.numbers.formatNumber((noCases * 60 / noSeconds)) + "/min)");
		}
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	private void doErrors() {
		int noCases = 0;
		long caseID = 0;
		long startTime = System.currentTimeMillis();
		ArrayList<Long> errors = base.dbPowerJ.getErrorFixed();
		for (int i = 0; i < errors.size(); i++) {
			caseID = errors.get(i);
			base.log(LibConstants.ERROR_NONE, className, "Re-coding Error case " + caseID);
			if (doCase(caseID)) {
				if (base.errorID == LibConstants.ERROR_NONE) {
					if (base.dbPowerJ.deleteError(caseID) > 0) {
						saveCase(false);
						noCases++;
					}
				}
			}
			if (base.errorID != LibConstants.ERROR_NONE || base.isStopping()) {
				break;
			} else if (noCases % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
		}
		long noSeconds = (System.currentTimeMillis() - startTime) / 1000;
		if (noCases > 0 && noSeconds > 0) {
			base.log(LibConstants.ERROR_NONE, className, "Workload corrected " + noCases + " errors in "
					+ noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
		}
	}

	private void getCase(String caseNo) {
		long caseID = base.dbPath.getCaseID(caseNo);
		if (caseID > 0) {
			if (doCase(caseID)) {
				if (base.errorID == LibConstants.ERROR_NONE) {
					saveCase(false);
				}
			}
		}
	}

	private void getLastAdditional(short codeID) {
		long minWorkloadDate = 0;
		if (codeID < ADDL_ORDER) {
			// Earliest date is 1/1/2011
			minWorkloadDate = base.setup.getLong(LibSetup.VAR_MIN_WL_DATE);
			lastUpdate = base.dbPowerJ.getLastAdditional(codeID) +1;
		} else {
			// Earliest date is 1/5/2017
			Calendar calLast = base.dates.setMidnight(null);
			calLast.set(Calendar.YEAR, 2017);
			calLast.set(Calendar.MONTH, Calendar.MAY);
			calLast.set(Calendar.DAY_OF_MONTH, 1);
			minWorkloadDate = calLast.getTimeInMillis();
			lastUpdate = base.dbPowerJ.getLastOrder() +1;
		}
		if (lastUpdate < minWorkloadDate) {
			lastUpdate = minWorkloadDate;
		}
	}

	private void getLastCase() {
		// Earliest date is 1/1/2011 (1293858000001)
		long minWorkloadDate = base.setup.getLong(LibSetup.VAR_MIN_WL_DATE);
		lastUpdate = base.dbPowerJ.getLastWorkload() +1;
		if (lastUpdate < minWorkloadDate) {
			lastUpdate = minWorkloadDate;
		}
	}

	private void getMaxDate() {
		Calendar calLastDate = base.dates.setMidnight(lastUpdate);
		// Maximum range is 30 days interval per run
		Calendar calNow = Calendar.getInstance();
		int noDays = base.dates.getNoDays(calLastDate, calNow);
		if (noDays > 30) {
			noDays = 30;
		} else if (noDays <= 1) {
			base.setUptodate();
			noDays = 1;
		}
		calLastDate.add(Calendar.DAY_OF_YEAR, noDays);
		maxDate = calLastDate.getTimeInMillis();
	}

	private void getOrders(int specimenNo) {
		boolean isFS = false, isAddlBlock = false, isRoutine = true;
		ArrayList<OrderData> orders = base.dbPath.getSpecimenOrders(thisSpecimen.getSpecID(), thisCase.getFinalTime());
		OrderData currentOrder = new OrderData();
		for (int i = 0; i < orders.size(); i++) {
			currentOrder = orders.get(i);
			if (masterOrders.matchOrder(currentOrder.getOrmID())) {
				isRoutine = (thisCase.getRouteTime() > currentOrder.getCreatedTime());
				isAddlBlock = false;
				switch (masterOrders.getOrderType()) {
				case LibConstants.ORDER_TYPE_IGNORE:
					continue;
				case LibConstants.ORDER_TYPE_BLOCK:
					thisSpecimen.setNoBlocks(thisSpecimen.getNoBlocks() + currentOrder.getQty());
					thisCase.setNoBlocks(thisCase.getNoBlocks() + currentOrder.getQty());
					isAddlBlock = !isRoutine;
					break;
				case LibConstants.ORDER_TYPE_SLIDE:
					thisSpecimen.setNoSlides(thisSpecimen.getNoSlides() + currentOrder.getQty());
					thisSpecimen.setNoHE(thisSpecimen.getNoHE() + currentOrder.getQty());
					thisCase.setNoSlides(thisCase.getNoSlides() + currentOrder.getQty());
					thisCase.setNoHE(thisCase.getNoHE() + currentOrder.getQty());
					break;
				case LibConstants.ORDER_TYPE_SS:
					thisSpecimen.setNoSlides(thisSpecimen.getNoSlides() + currentOrder.getQty());
					thisSpecimen.setNoSS(thisSpecimen.getNoSS() + currentOrder.getQty());
					thisCase.setNoSlides(thisCase.getNoSlides() + currentOrder.getQty());
					thisCase.setNoSS(thisCase.getNoSS() + currentOrder.getQty());
					break;
				case LibConstants.ORDER_TYPE_IHC:
					thisSpecimen.setNoSlides(thisSpecimen.getNoSlides() + currentOrder.getQty());
					thisSpecimen.setNoIHC(thisSpecimen.getNoIHC() + currentOrder.getQty());
					thisCase.setNoSlides(thisCase.getNoSlides() + currentOrder.getQty());
					thisCase.setNoIHC(thisCase.getNoIHC() + currentOrder.getQty());
					break;
				case LibConstants.ORDER_TYPE_FISH:
					thisSpecimen.setNoSlides(thisSpecimen.getNoSlides() + currentOrder.getQty());
					thisSpecimen.setNoMOL(thisSpecimen.getNoMOL() + currentOrder.getQty());
					thisCase.setNoSlides(thisCase.getNoSlides() + currentOrder.getQty());
					thisCase.setNoMol(thisCase.getNoMol() + currentOrder.getQty());
					break;
				case LibConstants.ORDER_TYPE_BLKFS:
					thisSpecimen.setNoBlocks(thisSpecimen.getNoBlocks() + currentOrder.getQty());
					thisSpecimen.setNoFSBlks(thisSpecimen.getNoFSBlks() + currentOrder.getQty());
					thisCase.setNoBlocks(thisCase.getNoBlocks() + currentOrder.getQty());
					thisCase.setNoFSBlks(thisCase.getNoFSBlks() + currentOrder.getQty());
					isFS = true;
					break;
				case LibConstants.ORDER_TYPE_SLDFS:
					thisSpecimen.setNoFSSlds(thisSpecimen.getNoFSSlds() + currentOrder.getQty());
					thisSpecimen.setNoSlides(thisSpecimen.getNoSlides() + currentOrder.getQty());
					thisCase.setNoSlides(thisCase.getNoSlides() + currentOrder.getQty());
					thisCase.setNoFSSlds(thisCase.getNoFSSlds() + currentOrder.getQty());
					isFS = true;
					break;
				default:
					// EM, FCM, MOLEC, REV
				}
				thisOrder = thisSpecimen.getOrder(masterOrders.getGroupID());
				if (thisOrder == null) {
					thisOrder = new OrderData();
					thisOrder.setOrgID(masterOrders.getGroupID());
					thisOrder.setName(masterOrders.getGroupName());
					thisSpecimen.setOrder(masterOrders.getGroupID(), thisOrder);
				}
				thisOrder.setQty(thisOrder.getQty() + currentOrder.getQty());
				coder1.addOrder(currentOrder.getOrmID(), masterOrders.getGroupID(),
						masterOrders.getCodeID(1), currentOrder.getQty(),
						isRoutine, isAddlBlock, specimenNo);
				coder2.addOrder(currentOrder.getOrmID(), masterOrders.getGroupID(),
						masterOrders.getCodeID(2), currentOrder.getQty(),
						isRoutine, isAddlBlock, specimenNo);
				coder3.addOrder(currentOrder.getOrmID(), masterOrders.getGroupID(),
						masterOrders.getCodeID(3), currentOrder.getQty(),
						isRoutine, isAddlBlock, specimenNo);
				coder4.addOrder(currentOrder.getOrmID(), masterOrders.getGroupID(),
						masterOrders.getCodeID(4), currentOrder.getQty(),
						isRoutine, isAddlBlock, specimenNo);
			} else {
				thisCase.setHasError(true);
				thisSpecimen.setErrorID(LibConstants.ERROR_ORDER_UNKNOWN);
				comment = "ERROR: " + thisCase.getCaseNo() + ", Specimen " + thisSpecimen.getSpecID() + ", Order "
						+ currentOrder.getOrmID() + ", Code " + currentOrder.getName() + ", "
						+ LibConstants.ERROR_STRINGS[thisSpecimen.getErrorID()] + " is invalid.";
				thisCase.addComment(comment);
				break;
			}
		}
		if (isFS) {
			thisCase.setNoFSSpec(thisCase.getNoFSSpec() +1);
		}
	}

	private void getSpecimens() {
		ArrayList<SpecimenData> specimens = base.dbPath.getCaseSpecimens(thisCase.getCaseID());
		for (int i = 0; i < specimens.size(); i++) {
			thisSpecimen = specimens.get(i);
			if (masterSpecimens.matchSpecimens(thisSpecimen.getSpmID())) {
				thisSpecimen.setProcID(masterSpecimens.getProcedureID());
				thisSpecimen.setSubID(masterSpecimens.getSubspecialtyID());
				thisSpecimen.setValue5(masterSpecimens.getValue5());
				thisCase.setSpecimen(thisSpecimen);
				thisCase.setValue5(thisCase.getValue5() + thisSpecimen.getValue5());
				coder1.addSpecimen(thisSpecimen,
						masterSpecimens.getCoderID(0, 0),
						masterSpecimens.getCoderID(1, 0),
						masterSpecimens.getCoderID(2, 0));
				coder2.addSpecimen(thisSpecimen,
						masterSpecimens.getCoderID(0, 1),
						masterSpecimens.getCoderID(1, 1),
						masterSpecimens.getCoderID(2, 1));
				coder3.addSpecimen(thisSpecimen,
						masterSpecimens.getCoderID(0, 2),
						masterSpecimens.getCoderID(1, 2),
						masterSpecimens.getCoderID(2, 2));
				coder4.addSpecimen(thisSpecimen,
						masterSpecimens.getCoderID(0, 3),
						masterSpecimens.getCoderID(1, 3),
						masterSpecimens.getCoderID(2, 3));
				getOrders(i);
				thisCase.setNoBlocks(thisCase.getNoBlocks() + thisSpecimen.getNoBlocks());
				if (thisCase.getMainSpec() == 0) {
					thisCase.setProcID(masterSpecimens.getProcedureID());
					thisCase.setSubID(masterSpecimens.getSubspecialtyID());
					thisCase.setMainSpec(thisSpecimen.getSpmID());
					thisCase.setMainBlocks(thisSpecimen.getNoBlocks());
				} else if (thisCase.getMainBlocks() < thisSpecimen.getNoBlocks()) {
					thisCase.setProcID(masterSpecimens.getProcedureID());
					thisCase.setSubID(masterSpecimens.getSubspecialtyID());
					thisCase.setMainSpec(thisSpecimen.getSpmID());
					thisCase.setMainBlocks(thisSpecimen.getNoBlocks());
				} else if (thisCase.getMainBlocks() == thisSpecimen.getNoBlocks()) {
					if (thisCase.getProcID() < masterSpecimens.getProcedureID()) {
						thisCase.setProcID(masterSpecimens.getProcedureID());
						thisCase.setSubID(masterSpecimens.getSubspecialtyID());
						thisCase.setMainSpec(thisSpecimen.getSpmID());
					} else if (thisCase.getProcID() == masterSpecimens.getProcedureID()) {
						if (thisCase.getSubID() < masterSpecimens.getSubspecialtyID()) {
							thisCase.setSubID(masterSpecimens.getSubspecialtyID());
							thisCase.setMainSpec(thisSpecimen.getSpmID());
						}
					}
				}
			}
		}
		if (thisCase.getNoSpecs() == 0) {
			thisCase.setHasError(true);
			comment = "ERROR: " + thisCase.getCaseNo() + ", "
					+ LibConstants.ERROR_STRINGS[LibConstants.ERROR_SPECIMENS_COUNT_ZERO];
			thisCase.addComment(comment);
			base.log(LibConstants.ERROR_NONE, className, comment);
		} else if (thisCase.getValue5() > maxFte5) {
			thisCase.setValue5(maxFte5);
		}
	}

	private void saveCase(boolean isNew) {
		byte errorID = 0;
		if (thisCase.isHasError()) {
			// Specimen Error
			errorID = 1;
		} else if (coder1.hasError()) {
			errorID = 2;
		} else if (coder2.hasError()) {
			errorID = 3;
		} else if (coder3.hasError()) {
			errorID = 4;
		} else if (coder4.hasError()) {
			errorID = 5;
		} else {
			errorID = 0;
		}
		if (thisCase.getCaseNo().length() > 12) {
			String temp = thisCase.getCaseNo();
			thisCase.setCaseNo(temp.substring(0, 7));
			temp = temp.substring(7);
			while (temp.length() > 5) {
				temp = temp.substring(1);
			}
			thisCase.setCaseNo(thisCase.getCaseNo() + temp);
		}
		if (errorID > 0) {
			saveError(errorID);
		} else {
			if (thisCase.getScanTime() > thisCase.getFinalTime()) {
				thisCase.setScanned(thisCase.getFinalTime());
			} else if (thisCase.getScanTime() < thisCase.getRouteTime()) {
				thisCase.setScanned(thisCase.getFinalTime());
			}
			if (thisCase.getRouteTime() > thisCase.getScanTime()) {
				// Use scan time (cytology does not route PAP/H&E, but routes SS/IHC)
				thisCase.setRouted(thisCase.getScanTime());
			} else if (thisCase.getRouteTime() < thisCase.getMicroTime()) {
				thisCase.setRouted(thisCase.getScanTime());
			}
			if (thisCase.getMicroTime() > thisCase.getRouteTime()) {
				thisCase.setMicroed(thisCase.getRouteTime());
			} else if (thisCase.getMicroTime() < thisCase.getEmbedTime()) {
				thisCase.setMicroed(thisCase.getRouteTime());
			}
			if (thisCase.getEmbedTime() > thisCase.getMicroTime()) {
				thisCase.setEmbeded(thisCase.getMicroTime());
			} else if (thisCase.getEmbedTime() < thisCase.getGrossTime()) {
				thisCase.setEmbeded(thisCase.getMicroTime());
			}
			if (thisCase.getGrossTime() > thisCase.getEmbedTime()) {
				thisCase.setGrossed(thisCase.getEmbedTime());
			} else if (thisCase.getGrossTime() < thisCase.getAccessTime()) {
				thisCase.setGrossed(thisCase.getEmbedTime());
			}
			thisCase.setGrossTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getGrossCalendar()));
			thisCase.setEmbedTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getEmbedCalendar()));
			thisCase.setMicroTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getMicroCalendar()));
			thisCase.setRouteTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getRouteCalendar()));
			thisCase.setFinalTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getFinalCalendar()));
			thisCase.setValue1(coder1.getValue());
			thisCase.setValue2(coder2.getValue());
			thisCase.setValue3(coder3.getValue());
			thisCase.setValue4(coder4.getValue());
			if (base.dbPowerJ.setFinal(isNew, thisCase) > 0) {
				saveSpecimens(isNew);
			}
		}
	}

	private void saveError(byte errorID) {
		switch (errorID) {
		case 1:
			comment = thisCase.getComment();
			break;
		case 2:
			comment = coder1.getComment();
			break;
		case 3:
			comment = coder2.getComment();
			break;
		case 4:
			comment = coder3.getComment();
			break;
		default:
			comment = coder4.getComment();
		}
		if (comment.length() > 2048) {
			comment = comment.substring(0, 2048);
		}
		base.dbPowerJ.setError(thisCase, errorID, comment);
	}

	private void saveOrders(boolean isNew, int i) {
		for (Entry<Short, OrderData> orderEntry : thisSpecimen.getOrders().entrySet()) {
			thisOrder = orderEntry.getValue();
			if (thisOrder.getQty() > 0) {
				thisOrder.setValue1(coder1.getOrder(i, thisOrder.getOrgID()));
				thisOrder.setValue2(coder2.getOrder(i, thisOrder.getOrgID()));
				thisOrder.setValue3(coder3.getOrder(i, thisOrder.getOrgID()));
				thisOrder.setValue4(coder4.getOrder(i, thisOrder.getOrgID()));
				base.dbPowerJ.setOrder(isNew, thisSpecimen.getSpecID(), thisOrder);
			}
		}
		// Save Frozen sections
		if (thisSpecimen.getNoFSBlks() > 0 || thisSpecimen.getNoFSSlds() > 0) {
			FrozenData frozen = new FrozenData();
			frozen.setSpecID(thisSpecimen.getSpecID());
			frozen.setNoBlocks(thisSpecimen.getNoFSBlks());
			frozen.setNoSlides(thisSpecimen.getNoFSSlds());
			frozen.setValue1(coder1.getFrozen(i));
			frozen.setValue2(coder2.getFrozen(i));
			frozen.setValue3(coder3.getFrozen(i));
			frozen.setValue4(coder4.getFrozen(i));
			frozen.setValue5(value5fs);
			base.dbPowerJ.setFrozen(isNew, frozen);
		}
	}

	private void saveSpecimens(boolean isNew) {
		for (short i = 0; i < thisCase.getNoSpecs(); i++) {
			thisSpecimen = thisCase.getSpecimen(i);
			thisSpecimen.setCaseID(thisCase.getCaseID());
			thisSpecimen.setValue1(coder1.getValue(i));
			thisSpecimen.setValue2(coder2.getValue(i));
			thisSpecimen.setValue3(coder3.getValue(i));
			thisSpecimen.setValue4(coder4.getValue(i));
			if (base.dbPowerJ.setSpecimen(isNew, thisSpecimen) > 0) {
				saveOrders(isNew, i);
			}
		}
	}
}