package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LFinals {
	static final byte ADDL_ADEND = 1;
	static final byte ADDL_COREL = 2;
	static final byte ADDL_ORDER = 3;
	private short noFrags = 0, noLesions = 0, number = 0;
	private int value5fs = 0;
	private int index = 0;
	private long lastUpdate = 0;
	private long maxDate = 0;
	private final String className = "Workload";
	private String string = "";
	private String comment = "";
	// Search strings for # of fragments in gross description
	private final String[] SEARCH_STRINGS = { "number of lesions removed", "number of excisions", "number of fragments",
			"number of pieces" };
	private Pattern pattern;
	private Matcher matcher;
	private MFacilities facilities;
	private MAccessions accessions;
	private MOrders masterOrders;
	private MSpecimens masterSpecimens;
	private Hashtable<Byte, PreparedStatement> pjStms = null;
	private Hashtable<Byte, PreparedStatement> apStms = null;
	private OCaseFinal thisCase = new OCaseFinal();
	private OSpecFinal thisSpecimen = new OSpecFinal();
	private OOrderFinal thisOrder = new OOrderFinal();
	private LCoderA coder1, coder2, coder3, coder4;
	private LBase pj;
	private DPowerJ dbPowerJ;
	private DPowerpath dbAP;

	LFinals(LBase pj) {
		new LFinals(0, "", pj);
	}

	LFinals(long caseID, String caseNo, LBase pj) {
		LBase.busy.set(true);
		this.pj = pj;
		dbAP = pj.dbAP;
		dbPowerJ = pj.dbPowerJ;
		pj.log(LConstants.ERROR_NONE, className,
				pj.dates.formatter(LDates.FORMAT_DATETIME) + " - Workload Manager Started...");
		try {
			apStms = dbAP.prepareStatements(LConstants.ACTION_LLOAD);
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				pjStms = dbPowerJ.prepareStatements(LConstants.ACTION_LLOAD);
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				accessions = new MAccessions(pj, pjStms.get(DPowerJ.STM_ACC_SELECT));
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				facilities = new MFacilities(pj, pjStms.get(DPowerJ.STM_FAC_SELECT));
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				masterOrders = new MOrders(pj, pjStms.get(DPowerJ.STM_ORM_SELECT));
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				masterSpecimens = new MSpecimens(pj, pjStms.get(DPowerJ.STM_SPM_SELECT));
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				if (pj.setup.getBoolean(LSetup.VAR_CODER1_ACTIVE)) {
					coder1 = new LCoder(pj, pjStms.get(DPowerJ.STM_CD1_SELECT), 1);
				} else {
					coder1 = new LCoderA(pj, 1);
					dbPowerJ.close(pjStms.get(DPowerJ.STM_CD1_SELECT));
				}
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				if (pj.setup.getBoolean(LSetup.VAR_CODER2_ACTIVE)) {
					coder2 = new LCoder(pj, pjStms.get(DPowerJ.STM_CD2_SELECT), 2);
				} else {
					coder2 = new LCoderA(pj, 2);
					dbPowerJ.close(pjStms.get(DPowerJ.STM_CD2_SELECT));
				}
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				if (pj.setup.getBoolean(LSetup.VAR_CODER3_ACTIVE)) {
					coder3 = new LCoder(pj, pjStms.get(DPowerJ.STM_CD3_SELECT), 3);
				} else {
					coder3 = new LCoderA(pj, 3);
					dbPowerJ.close(pjStms.get(DPowerJ.STM_CD3_SELECT));
				}
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				if (pj.setup.getBoolean(LSetup.VAR_CODER4_ACTIVE)) {
					coder4 = new LCoder(pj, pjStms.get(DPowerJ.STM_CD4_SELECT), 4);
				} else {
					coder4 = new LCoderA(pj, 4);
					dbPowerJ.close(pjStms.get(DPowerJ.STM_CD4_SELECT));
				}
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				value5fs = pj.setup.getInt(LSetup.VAR_V5_FROZEN);
				// Initialize the text parser that finds first number in a string
				String s = "String 17 String";
				pattern = Pattern.compile(".*?(\\d+).*");
				matcher = pattern.matcher(s);
				// Do the work
				if (caseID > 0) {
					// For Debug
					codeCase(caseID);
				} else if (caseNo.length() > 4) {
					// For Debug
					getCase(caseNo);
				} else {
					getLastCase();
					getMaxDate();
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						doCases();
					}
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						getLastAdditional(ADDL_ADEND);
					}
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						doAdenda();
					}
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						getLastAdditional(ADDL_ORDER);
					}
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						doAdditional();
					}
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						getLastAdditional(ADDL_COREL);
					}
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						doCorrelations();
					}
					if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
						doErrors();
					}
				}
			}
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, className, e);
		}
	}

	void close() {
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
		if (dbAP != null && apStms != null) {
			dbAP.close(apStms);
		}
		if (dbPowerJ != null && pjStms != null) {
			dbPowerJ.close(pjStms);
		}
		LBase.busy.set(false);
	}

	private boolean codeCase(long caseID) {
		boolean success = false;
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_DETAILS), 1, caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_DETAILS));
			while (rst.next()) {
				if (accessions.doWorkload(rst.getShort("acc_type_id"))
						&& facilities.doWorkload(rst.getShort("facility_id"))) {
					thisCase = new OCaseFinal();
					thisCase.caseID = caseID;
					thisCase.spyID = accessions.getSpecialty();
					thisCase.facID = rst.getShort("facility_id");
					thisCase.codeSpec = accessions.codeSpecimen();
					thisCase.caseNo = rst.getString("accession_no");
					thisCase.finalID = rst.getShort("assigned_to_id");
					thisCase.finaled.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
					thisCase.accessed.setTimeInMillis(rst.getTimestamp("created_date").getTime());
					pj.log(LConstants.ERROR_NONE, className, "Coding case " + thisCase.caseNo);
					// We need to know if the case is malignant before-hand
					dbAP.setLong(apStms.get(DPowerpath.STM_CASE_SYNOPTI), 1, thisCase.caseID);
					thisCase.noSynop = dbAP.getByte(apStms.get(DPowerpath.STM_CASE_SYNOPTI));
					coder1.newCase(thisCase);
					coder2.newCase(thisCase);
					coder3.newCase(thisCase);
					coder4.newCase(thisCase);
					getGrossed();
					getEmbeded();
					getMicrotomed();
					getRouted();
					getScanned();
					getSpecimens();
					if (thisCase.needFrag) {
						// Extract # of fragments from gross description
						getDescr();
					}
					coder1.codeCase();
					coder2.codeCase();
					coder3.codeCase();
					coder4.codeCase();
					success = true;
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
		return success;
	}

	private boolean dejavu() {
		boolean exists = false;
		ResultSet rst = null;
		try {
			dbPowerJ.setLong(pjStms.get(DPowerJ.STM_CSE_SL_SPE), 1, thisCase.caseID);
			rst = dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_SPE));
			while (rst.next()) {
				// Must also get a specimen for WLCoder
				if (masterSpecimens.matchSpecimens(rst.getShort("SMID"))) {
					thisCase.caseNo = rst.getString("CANO");
					thisCase.accessed.setTimeInMillis(rst.getTimestamp("FNED").getTime());
					thisSpecimen = new OSpecFinal();
					thisSpecimen.specID = rst.getLong("SPID");
					thisSpecimen.spmID = rst.getShort("SMID");
					thisCase.lstSpecimens.add(thisSpecimen);
					thisCase.noSpec = 1;
					thisCase.codeSpec = false;
					exists = true;
					break;
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbPowerJ.close(rst);
		}
		return exists;
	}

	private boolean deleteError(long caseID) {
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ERR_DELETE), 1, caseID);
		return (dbPowerJ.execute(apStms.get(DPowerJ.STM_ERR_DELETE)) > 0);
	}

	private void doAdditional() {
		final short qty = 1;
		boolean exists = false;
		short orderID = 0;
		int noCases = 0;
		long startTime = System.currentTimeMillis();
		double dValue1 = 0, dValue2 = 0, dValue3 = 0, dValue4 = 0;
		ResultSet rstOrders = null;
		try {
			thisCase = new OCaseFinal();
			dbAP.setTime(apStms.get(DPowerpath.STM_CASES_REVIEW), 1, lastUpdate);
			dbAP.setTime(apStms.get(DPowerpath.STM_CASES_REVIEW), 2, maxDate);
			rstOrders = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASES_REVIEW));
			while (rstOrders.next()) {
				if (thisCase.caseID != rstOrders.getLong("acc_id")) {
					thisCase = new OCaseFinal();
					thisCase.caseID = rstOrders.getLong("acc_id");
					// Must exist in PowerJ in Cases Table
					exists = dejavu();
				}
				if (!exists)
					continue;
				// Order must be be an additional rule
				orderID = rstOrders.getShort("procedure_id");
				if (masterOrders.matchOrder(orderID)) {
					// Order must be of category additional (OrderType in Groups)
					if (masterOrders.getOrderType() != OOrderType.ADDITINAL) {
						continue;
					}
					thisCase.finaled.setTimeInMillis(rstOrders.getTimestamp("created_date").getTime());
					thisCase.finalID = rstOrders.getShort("ordered_by_id");
					// Cannot exist in PowerJ in Additionals Table
					if (isDuplicate(orderID))
						continue;
					dValue1 = coder1.getAddl(masterOrders.getCodeID(1), qty);
					dValue2 = coder2.getAddl(masterOrders.getCodeID(2), qty);
					dValue3 = coder3.getAddl(masterOrders.getCodeID(3), qty);
					dValue4 = coder4.getAddl(masterOrders.getCodeID(4), qty);
					if (dValue1 > 0.001 || dValue2 > 0.001 || dValue3 > 0.001 || dValue4 > 0.001
							|| masterOrders.getCodeID(5) > 0) {
						pj.log(LConstants.ERROR_NONE, className, "Coding Additionals on case " + thisCase.caseID);
						dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ADD_INSERT), 1, thisCase.caseID);
						dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_INSERT), 2, thisCase.finalID);
						dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_INSERT), 3, orderID);
						dbPowerJ.setTime(pjStms.get(DPowerJ.STM_ADD_INSERT), 4, thisCase.finaled.getTimeInMillis());
						dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 5, dValue1);
						dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 6, dValue2);
						dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 7, dValue3);
						dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 8, dValue4);
						dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_INSERT), 9, masterOrders.getCodeID(5));
						if (dbPowerJ.execute(pjStms.get(DPowerJ.STM_ADD_INSERT)) > 0) {
							noCases++;
						}
					}
				}
				if (pj.errorID != LConstants.ERROR_NONE || pj.abort()) {
					break;
				} else if (noCases % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			long noSeconds = (System.currentTimeMillis() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				pj.log(LConstants.ERROR_NONE, className, "Workload Coded " + noCases + " additional orders in "
						+ noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rstOrders);
		}
	}

	private void doAdenda() {
		boolean exists = false;
		short orderID = 0;
		int noCases = 0;
		long startTime = System.currentTimeMillis();
		double dValue1 = 0, dValue2 = 0, dValue3 = 0, dValue4 = 0;
		String descr = "";
		ResultSet rstCases = null, rstOrders = null;
		try {
			thisCase = new OCaseFinal();
			dbAP.setTime(apStms.get(DPowerpath.STM_CASES_ADDEND), 1, lastUpdate);
			dbAP.setTime(apStms.get(DPowerpath.STM_CASES_ADDEND), 2, maxDate);
			rstCases = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASES_ADDEND));
			while (rstCases.next()) {
				descr = rstCases.getString("description").trim().toLowerCase();
				if (!descr.equals("amendment final")
						&& !descr.equals("addendum final")) {
					continue;
				}
				if (thisCase.caseID != rstCases.getLong("acc_id")) {
					thisCase = new OCaseFinal();
					thisCase.caseID = rstCases.getLong("acc_id");
					// Must exist in PowerJ in Cases Table
					exists = dejavu();
				}
				if (!exists)
					continue;
				thisCase.finaled.setTimeInMillis(rstCases.getTimestamp("completed_date").getTime());
				thisCase.finalID = rstCases.getShort("assigned_to_id");
				// Cannot exist in PowerJ in Additional Table
				if (isDuplicate(ADDL_ADEND))
					continue;
				pj.log(LConstants.ERROR_NONE, className, "Coding " + descr + ": " + thisCase.caseNo);
				coder1.newCase(thisCase);
				coder2.newCase(thisCase);
				coder3.newCase(thisCase);
				coder4.newCase(thisCase);
				coder1.addSpecimen(thisSpecimen.procID,
						masterSpecimens.getCoderID(LConstants.CODER_1, LConstants.CODER_B),
						masterSpecimens.getCoderID(LConstants.CODER_1, LConstants.CODER_M),
						masterSpecimens.getCoderID(LConstants.CODER_1, LConstants.CODER_R));
				coder2.addSpecimen(thisSpecimen.procID,
						masterSpecimens.getCoderID(LConstants.CODER_2, LConstants.CODER_B),
						masterSpecimens.getCoderID(LConstants.CODER_2, LConstants.CODER_M),
						masterSpecimens.getCoderID(LConstants.CODER_2, LConstants.CODER_R));
				coder3.addSpecimen(thisSpecimen.procID,
						masterSpecimens.getCoderID(LConstants.CODER_3, LConstants.CODER_B),
						masterSpecimens.getCoderID(LConstants.CODER_3, LConstants.CODER_M),
						masterSpecimens.getCoderID(LConstants.CODER_3, LConstants.CODER_R));
				coder4.addSpecimen(thisSpecimen.procID,
						masterSpecimens.getCoderID(LConstants.CODER_4, LConstants.CODER_B),
						masterSpecimens.getCoderID(LConstants.CODER_4, LConstants.CODER_M),
						masterSpecimens.getCoderID(LConstants.CODER_4, LConstants.CODER_R));
				dbAP.setLong(apStms.get(DPowerpath.STM_CASE_ORDERS), 1, thisCase.caseID);
				rstOrders = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_ORDERS));
				while (rstOrders.next()) {
					orderID = rstOrders.getShort("procedure_id");
					if (masterOrders.matchOrder(orderID)) {
						if (masterOrders.getOrderType() != OOrderType.IGNORE) {
							if (rstOrders.getTimestamp("created_date").getTime() > thisCase.accessed.getTimeInMillis()
									&& rstOrders.getTimestamp("created_date").getTime() < thisCase.finaled.getTimeInMillis()) {
								thisOrder = thisSpecimen.lstOrders.get(masterOrders.getGroupID());
								if (thisOrder == null) {
									thisOrder = new OOrderFinal();
									thisOrder.grpID = masterOrders.getGroupID();
									thisSpecimen.lstOrders.put(masterOrders.getGroupID(), thisOrder);
								}
								thisOrder.qty += rstOrders.getShort("quantity");
								coder1.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(1),
										thisOrder.qty, false, false, 0);
								coder2.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(2),
										thisOrder.qty, false, false, 0);
								coder3.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(3),
										thisOrder.qty, false, false, 0);
								coder4.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(4),
										thisOrder.qty, false, false, 0);
							}
						}
					}
				}
				rstOrders.close();
				coder1.codeCase();
				coder2.codeCase();
				coder3.codeCase();
				coder4.codeCase();
				dValue1 = coder1.getValue();
				dValue2 = coder2.getValue();
				dValue3 = coder3.getValue();
				dValue4 = coder4.getValue();
				if (dValue1 > 0.01 || dValue2 > 0.01 || dValue3 > 0.01 || dValue4 > 0.01) {
					dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ADD_INSERT), 1, thisCase.caseID);
					dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_INSERT), 2, thisCase.finalID);
					dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_INSERT), 3, ADDL_ADEND);
					dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_INSERT), 4, thisCase.finaled.getTimeInMillis());
					dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 5, dValue1);
					dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 6, dValue2);
					dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 7, dValue3);
					dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 8, dValue4);
					dbPowerJ.setInt(pjStms.get(DPowerJ.STM_ADD_INSERT), 9, 0);
					if (dbPowerJ.execute(pjStms.get(DPowerJ.STM_ADD_INSERT)) > 0) {
						noCases++;
					}
				}
				if (pj.errorID != LConstants.ERROR_NONE || pj.abort()) {
					break;
				} else if (noCases % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			long noSeconds = (System.currentTimeMillis() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				pj.log(LConstants.ERROR_NONE, className,
						"Workload Coded " + pj.numbers.formatNumber(noCases) + " amend/addend in "
								+ pj.numbers.formatNumber(noSeconds) + " seconds ("
								+ pj.numbers.formatNumber((noCases * 60 / noSeconds)) + "/min)");
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rstCases);
			dbPowerJ.close(rstOrders);
		}
	}

	private void doCases() {
		int noCases = 0;
		long caseID = 0;
		long startTime = System.currentTimeMillis();
		ResultSet rst = null;
		try {
			dbAP.setTime(apStms.get(DPowerpath.STM_CASES_FINAL), 1, lastUpdate);
			dbAP.setTime(apStms.get(DPowerpath.STM_CASES_FINAL), 2, maxDate);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASES_FINAL));
			while (rst.next()) {
				caseID = rst.getLong("CaseID");
				if (accessions.doWorkload(rst.getShort("acc_type_id"))
						&& facilities.doWorkload(rst.getShort("facility_id"))) {
					if (codeCase(caseID)) {
						if (pj.errorID == LConstants.ERROR_NONE) {
							saveCase(true);
							noCases++;
						}
					}
				}
				if (pj.errorID != LConstants.ERROR_NONE || pj.abort()) {
					break;
				} else if (noCases % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			long noSeconds = (System.currentTimeMillis() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				pj.log(LConstants.ERROR_NONE, className,
						"Workload Coded " + pj.numbers.formatNumber(noCases) + " new cases in "
								+ pj.numbers.formatNumber(noSeconds) + " seconds ("
								+ pj.numbers.formatNumber((noCases * 60 / noSeconds)) + "/min)");
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private void doCorrelations() {
		if (coder1.getCorrelations() < 0.001 && coder2.getCorrelations() < 0.001 && coder3.getCorrelations() < 0.001
				&& coder4.getCorrelations() < 0.001) {
			return;
		}
		boolean exists = false;
		int noCases = 0;
		long startTime = System.currentTimeMillis();
		double dValue1 = coder1.getCorrelations();
		double dValue2 = coder2.getCorrelations();
		double dValue3 = coder3.getCorrelations();
		double dValue4 = coder4.getCorrelations();
		ResultSet rstCases = null;
		try {
			thisCase = new OCaseFinal();
			dbAP.setTime(apStms.get(DPowerpath.STM_CORRELATIONS), 1, lastUpdate);
			dbAP.setTime(apStms.get(DPowerpath.STM_CORRELATIONS), 2, maxDate);
			rstCases = dbAP.getResultSet(apStms.get(DPowerpath.STM_CORRELATIONS));
			while (rstCases.next()) {
				if (thisCase.caseID != rstCases.getLong("acc_id")) {
					thisCase = new OCaseFinal();
					thisCase.caseID = rstCases.getLong("acc_id");
					// Must exist in PowerJ in Cases Table
					exists = dejavu();
				}
				if (!exists)
					continue;
				thisCase.finaled.setTimeInMillis(rstCases.getTimestamp("correlation_date").getTime());
				thisCase.finalID = rstCases.getShort("correlated_by_id");
				// Cannot exist in PowerJ in Additional Table
				if (isDuplicate(ADDL_COREL))
					continue;
				pj.log(LConstants.ERROR_NONE, className, "Coding Correlation on case " + thisCase.caseNo);
				dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ADD_INSERT), 1, thisCase.caseID);
				dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_INSERT), 2, thisCase.finalID);
				dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_INSERT), 3, ADDL_COREL);
				dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_INSERT), 4, thisCase.finaled.getTimeInMillis());
				dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 5, dValue1);
				dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 6, dValue2);
				dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 7, dValue3);
				dbPowerJ.setDouble(pjStms.get(DPowerJ.STM_ADD_INSERT), 8, dValue4);
				dbPowerJ.setInt(pjStms.get(DPowerJ.STM_ADD_INSERT), 9, 0);
				if (dbPowerJ.execute(pjStms.get(DPowerJ.STM_ADD_INSERT)) > 0) {
					noCases++;
				}
				if (pj.errorID != LConstants.ERROR_NONE || pj.abort()) {
					break;
				} else if (noCases % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			long noSeconds = (System.currentTimeMillis() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 6000) {
				pj.log(LConstants.ERROR_NONE, className,
						"Workload Coded " + pj.numbers.formatNumber(noCases) + " correlations in "
								+ pj.numbers.formatNumber(noSeconds) + " seconds ("
								+ pj.numbers.formatNumber((noCases * 60 / noSeconds)) + "/min)");
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rstCases);
		}
	}

	private void doErrors() {
		int noCases = 0;
		long caseID = 0;
		long startTime = System.currentTimeMillis();
		ResultSet rst = null;
		try {
			rst = dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ERR_SL_FXD));
			while (rst.next()) {
				caseID = rst.getLong("CAID");
				pj.log(LConstants.ERROR_NONE, className, "Re-coding Error case " + caseID);
				if (codeCase(caseID)) {
					if (pj.errorID == LConstants.ERROR_NONE) {
						if (deleteError(caseID)) {
							saveCase(false);
							noCases++;
						}
					}
				}
				if (pj.errorID != LConstants.ERROR_NONE || pj.abort()) {
					break;
				} else if (noCases % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			long noSeconds = (System.currentTimeMillis() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				pj.log(LConstants.ERROR_NONE, className, "Workload Coded " + noCases + " corrected errors in "
						+ noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbPowerJ.close(rst);
		}
	}

	private void getCase(String caseNo) {
		dbAP.setString(apStms.get(DPowerpath.STM_CASE_NUMBER), 1, caseNo);
		long caseID = dbAP.getLong(apStms.get(DPowerpath.STM_CASE_NUMBER));
		if (caseID > 0) {
			if (codeCase(caseID)) {
				if (pj.errorID == LConstants.ERROR_NONE) {
					saveCase(false);
				}
			}
		}
	}

	private void getDescr() {
		String grossDescr = "";
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_GROSS), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_GROSS));
			while (rst.next()) {
				// There are 2 fields, one is null
				if (rst.getString("finding") != null) {
					grossDescr = rst.getString("finding");
				} else if (rst.getString("finding_text") != null) {
					grossDescr = rst.getString("finding_text");
				}
			}
			rst.close();
			if (grossDescr.length() == 0) {
				// Some cases have gross combined with other fields
				dbAP.setLong(apStms.get(DPowerpath.STM_CASE_DIAGNOS), 1, thisCase.caseID);
				rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_DIAGNOS));
				while (rst.next()) {
					// There are 2 fields, one is null
					if (rst.getString("finding_text") != null) {
						grossDescr = rst.getString("finding_text");
					} else if (rst.getString("finding") != null) {
						grossDescr = rst.getString("finding");
					}
					if (grossDescr.length() > 5) {
						grossDescr = grossDescr.trim().toLowerCase();
						int i = grossDescr.indexOf("gross description:");
						if (i > -1) {
							grossDescr = grossDescr.substring(i, grossDescr.length());
							break;
						}
					}
				}
			}
			rst.close();
			grossDescr = grossDescr.trim().toLowerCase();
			if (grossDescr.length() > 5) {
				if (thisCase.noSpec > 1) {
					String[] grosses = grossDescr.split("the specimen is received in a container");
					// First element is empty
					if (grosses.length == thisCase.noSpec + 1) {
						for (int i = 0; i < thisCase.noSpec; i++) {
							thisSpecimen = thisCase.lstSpecimens.get(i);
							if (coder1.needsFragments(i) || coder2.needsFragments(i) || coder3.needsFragments(i)
									|| coder4.needsFragments(i)) {
								thisSpecimen.noFrags = getNoFragments(grosses[i + 1]);
							}
						}
					}
				} else {
					thisCase.lstSpecimens.get(0).noFrags = getNoFragments(grossDescr);
				}
			} else {
				for (int i = 0; i < thisCase.noSpec; i++) {
					thisSpecimen = thisCase.lstSpecimens.get(i);
					if (coder1.needsFragments(i) || coder2.needsFragments(i) || coder3.needsFragments(i)
							|| coder4.needsFragments(i)) {
						thisSpecimen.noFrags = 1;
					}
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	/** Update embedding status of grossed cases. */
	private void getEmbeded() {
		short noBlocks = 0;
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_EMBEDED), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_EMBEDED));
			while (rst.next()) {
				noBlocks++;
				if (noBlocks >= thisCase.noBlocks) {
					thisCase.embeded.setTimeInMillis(rst.getTimestamp("event_date").getTime());
					thisCase.embedID = rst.getShort("personnel_id");
					break;
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private void getGrossed() {
		String descr = "";
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_PROCESS), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_PROCESS));
			while (rst.next()) {
				if (rst.getString("description") != null) {
					if (rst.getTimestamp("completed_date") != null) {
						descr = rst.getString("description").trim().toLowerCase();
						if (descr.contains("gross") || descr.contains("screening") || descr.contains("provisional")) {
							thisCase.grossed.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
							thisCase.grossID = rst.getShort("assigned_to_id");
							break;
						}
					}
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private void getLastAdditional(short codeID) {
		// Earliest date is 1/1/2011 (1293858000001)
		long minWorkloadDate = pj.setup.getLong(LSetup.VAR_MIN_WL_DATE);
		if (codeID < ADDL_ORDER) {
			dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ADD_SL_LST), 1, codeID);
			// Add 1 millisecond to avoid duplicated 1st case
			lastUpdate = dbPowerJ.getTime(pjStms.get(DPowerJ.STM_ADD_SL_LST)) + 1;
		} else {
			lastUpdate = dbPowerJ.getTime(pjStms.get(DPowerJ.STM_ADD_SL_ORD)) + 1;
		}
		if (lastUpdate < minWorkloadDate) {
			lastUpdate = minWorkloadDate;
		}
	}

	private void getLastCase() {
		// Earliest date is 1/1/2011 (1293858000001)
		long minWorkloadDate = pj.setup.getLong(LSetup.VAR_MIN_WL_DATE);
		// Add 1 millisecond to avoid duplicated 1st case
		lastUpdate = dbPowerJ.getTime(pjStms.get(DPowerJ.STM_CSE_SL_WLD)) + 1;
		if (lastUpdate < minWorkloadDate) {
			lastUpdate = minWorkloadDate;
		}
	}
	private void getMaxDate() {
		// Maximum range is 7 days interval per run
		Calendar calLastDate = pj.dates.setMidnight(lastUpdate);
		Calendar calNow = Calendar.getInstance();
		int noDays = pj.dates.getNoDays(calLastDate, calNow);
		if (noDays > 30) {
			noDays = 30;
		} else if (noDays < 1) {
			noDays = 1;
		}
		calLastDate.add(Calendar.DAY_OF_YEAR, noDays);
		maxDate = calLastDate.getTimeInMillis();
	}

	private void getMicrotomed() {
		short noBlocks = 0;
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_MICROTO), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_MICROTO));
			while (rst.next()) {
				noBlocks++;
				if (noBlocks >= thisCase.noBlocks) {
					thisCase.microed.setTimeInMillis(rst.getTimestamp("event_date").getTime());
					thisCase.microID = rst.getShort("personnel_id");
					break;
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	/** Update Number of blocks for a specimen. */
	private short getNoBlocks(long specID) {
		dbAP.setLong(apStms.get(DPowerpath.STM_SPEC_BLOCKS), 1, specID);
		return dbAP.getShort(apStms.get(DPowerpath.STM_SPEC_BLOCKS));
	}

	private short getNoFragments(String gross) {
		noFrags = 0;
		noLesions = 0;
		for (int i = 0; i < SEARCH_STRINGS.length; i++) {
			index = gross.indexOf(SEARCH_STRINGS[i]);
			if (index > -1) {
				string = gross.substring(index);
				// use 1st line only
				String[] lines = string.split(System.getProperty("line.separator"));
				// Extract first number (string + : (.? = match any char 0-1 times) + 0/many
				// spaces (\\s*? = match 0 or more spaces)+ digit + 0-many characters
				pattern = Pattern.compile(SEARCH_STRINGS[i] + ".?\\s*?(\\d+).*");
				matcher = pattern.matcher(lines[0]);
				if (matcher.find()) {
					string = matcher.group(1);
					if (string.length() > 0) {
						number = Short.parseShort(string);
						if (number < 1) {
							// Nothing found (0) or malicious (-3)
							number = 1;
						} else if (number > 20) {
							// Max 20 to correct for fragmentation
							number = 20;
						}
						if (i < 2) {
							// No of lesions
							if (noLesions < number) {
								noLesions = number;
							}
						} else {
							// No of fragments
							if (noFrags < number) {
								noFrags = number;
								break;
							}
						}
					}
				} else if (i < 2) {
					// if lesions/excisions found, but no number next to it
					noLesions = 1;
				} else {
					// if fragments/pieces found, but no number next to it
					noFrags = 1;
				}
			}
		}
		if (noLesions > 0 && noFrags > noLesions) {
			// If both found, and both > 0, then use noLesions
			// Unless noLesions > noFragments (Montfort)
			noFrags = noLesions;
		} else if (noFrags < 1) {
			noFrags = 1;
		}
		return noFrags;
	}

	private void getOrders() {
		boolean isFS = false, isRoutine = false, isAddlBlock = false;
		short qty = 0, orderID = 0;
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_SPEC_ORDERS), 1, thisSpecimen.specID);
			dbAP.setTime(apStms.get(DPowerpath.STM_SPEC_ORDERS), 2, thisCase.finaled.getTimeInMillis());
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_SPEC_ORDERS));
			while (rst.next()) {
				orderID = rst.getShort("procedure_id");
				if (masterOrders.matchOrder(orderID)) {
					// Some orders do not count if routine
					isRoutine = (rst.getTimestamp("created_date").getTime() < thisCase.routed.getTimeInMillis());
					qty = rst.getShort("quantity");
					isAddlBlock = false;
					switch (masterOrders.getOrderType()) {
					case OOrderType.IGNORE:
						continue;
					case OOrderType.BLK:
						thisSpecimen.noBlocks += qty;
						thisCase.noBlocks += qty;
						isAddlBlock = !isRoutine;
						break;
					case OOrderType.SLIDE:
						thisSpecimen.noSlides += qty;
						thisSpecimen.noHE += qty;
						thisCase.noSlides += qty;
						thisCase.noHE += qty;
						break;
					case OOrderType.SS:
						thisSpecimen.noSlides += qty;
						thisSpecimen.noSS += qty;
						thisCase.noSlides += qty;
						thisCase.noSS += qty;
						break;
					case OOrderType.IHC:
						thisSpecimen.noSlides += qty;
						thisSpecimen.noIHC += qty;
						thisCase.noSlides += qty;
						thisCase.noIHC += qty;
						break;
					case OOrderType.FISH:
						thisSpecimen.noSlides += qty;
						thisSpecimen.noMOL += qty;
						thisCase.noSlides += qty;
						thisCase.noMol += qty;
						break;
					case OOrderType.BLK_FS:
						thisSpecimen.noBlocks += qty;
						thisSpecimen.noFSBlks += qty;
						thisCase.noBlocks += qty;
						thisCase.noFSBlks += qty;
						isFS = true;
						break;
					case OOrderType.SLD_FS:
						thisSpecimen.noFSSlds += qty;
						thisSpecimen.noSlides += qty;
						thisCase.noSlides += qty;
						thisCase.noFSSlds += qty;
						isFS = true;
						break;
					default:
						// EM, FCM, MOLEC, REV
					}
					thisOrder = thisSpecimen.lstOrders.get(masterOrders.getGroupID());
					if (thisOrder == null) {
						thisOrder = new OOrderFinal();
						thisOrder.grpID = masterOrders.getGroupID();
						thisOrder.name = masterOrders.getGroupName();
						thisSpecimen.lstOrders.put(masterOrders.getGroupID(), thisOrder);
					}
					thisOrder.qty += qty;
					coder1.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(1), qty, isRoutine,
							isAddlBlock, (thisCase.noSpec - 1));
					coder2.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(2), qty, isRoutine,
							isAddlBlock, (thisCase.noSpec - 1));
					coder3.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(3), qty, isRoutine,
							isAddlBlock, (thisCase.noSpec - 1));
					coder4.addOrder(orderID, masterOrders.getGroupID(), masterOrders.getCodeID(4), qty, isRoutine,
							isAddlBlock, (thisCase.noSpec - 1));
				} else {
					thisCase.hasError = true;
					thisSpecimen.errorID = LConstants.ERROR_ORDER_UNKNOWN;
					comment = "ERROR: getOrder, " + thisCase.caseNo + ", Specimen " + thisSpecimen.specID + ", Order "
							+ orderID + ", Code " + rst.getString("code") + ", "
							+ LConstants.ERROR_STRINGS[thisSpecimen.errorID];
					thisCase.comment += comment + "\n";
					pj.log(LConstants.ERROR_NONE, className, comment);
					break;
				}
			}
			if (isFS) {
				thisCase.noFSSpec++;
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private void getRouted() {
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_ROUTING), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_ROUTING));
			while (rst.next()) {
				thisCase.routed.setTimeInMillis(rst.getTimestamp("event_date").getTime());
				thisCase.routeID = rst.getShort("personnel_id");
				break;
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private void getScanned() {
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_SCANNED), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_SCANNED));
			while (rst.next()) {
				thisCase.scanned.setTimeInMillis(rst.getTimestamp("event_date").getTime());
				break;
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private void getSpecimens() {
		short noBlocks = 0;
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_SPCMNS), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_SPCMNS));
			while (rst.next()) {
				if (masterSpecimens.matchSpecimens(rst.getShort("tmplt_profile_id"))) {
					thisSpecimen = new OSpecFinal();
					thisSpecimen.specID = rst.getLong("id");
					thisSpecimen.spmID = rst.getShort("tmplt_profile_id");
					thisSpecimen.descr = rst.getString("description").trim();
					thisSpecimen.procID = masterSpecimens.getProcedureID();
					thisSpecimen.subID = masterSpecimens.getSubspecialtyID(rst.getString("label_name"),
							rst.getString("description"));
					thisSpecimen.value5 = masterSpecimens.getValue5();
					thisCase.lstSpecimens.add(thisSpecimen);
					thisCase.noSpec++;
					thisCase.value5 += thisSpecimen.value5;
					noBlocks = getNoBlocks(thisSpecimen.specID);
					// Find best fit sub-specialty
					if (thisCase.mainSpec == 0) {
						thisCase.procID = thisSpecimen.procID;
						thisCase.subID = thisSpecimen.subID;
						thisCase.mainBlocks = noBlocks;
						thisCase.mainSpec = rst.getShort("tmplt_profile_id");
					} else if (thisCase.procID < thisSpecimen.procID) {
						thisCase.procID = thisSpecimen.procID;
						thisCase.subID = thisSpecimen.subID;
						thisCase.mainBlocks = noBlocks;
						thisCase.mainSpec = rst.getShort("tmplt_profile_id");
					} else if (thisCase.procID == thisSpecimen.procID) {
						if (thisCase.subID < thisSpecimen.subID) {
							thisCase.subID = thisSpecimen.subID;
							thisCase.mainBlocks = noBlocks;
							thisCase.mainSpec = rst.getShort("tmplt_profile_id");
						} else if (thisCase.mainBlocks < thisSpecimen.noBlocks) {
							thisCase.subID = thisSpecimen.subID;
							thisCase.mainBlocks = noBlocks;
							thisCase.mainSpec = rst.getShort("tmplt_profile_id");
						}
					}
					coder1.addSpecimen(thisSpecimen.procID,
							masterSpecimens.getCoderID(LConstants.CODER_1, LConstants.CODER_B),
							masterSpecimens.getCoderID(LConstants.CODER_1, LConstants.CODER_M),
							masterSpecimens.getCoderID(LConstants.CODER_1, LConstants.CODER_R));
					coder2.addSpecimen(thisSpecimen.procID,
							masterSpecimens.getCoderID(LConstants.CODER_2, LConstants.CODER_B),
							masterSpecimens.getCoderID(LConstants.CODER_2, LConstants.CODER_M),
							masterSpecimens.getCoderID(LConstants.CODER_2, LConstants.CODER_R));
					coder3.addSpecimen(thisSpecimen.procID,
							masterSpecimens.getCoderID(LConstants.CODER_3, LConstants.CODER_B),
							masterSpecimens.getCoderID(LConstants.CODER_3, LConstants.CODER_M),
							masterSpecimens.getCoderID(LConstants.CODER_3, LConstants.CODER_R));
					coder4.addSpecimen(thisSpecimen.procID,
							masterSpecimens.getCoderID(LConstants.CODER_4, LConstants.CODER_B),
							masterSpecimens.getCoderID(LConstants.CODER_4, LConstants.CODER_M),
							masterSpecimens.getCoderID(LConstants.CODER_4, LConstants.CODER_R));
					getOrders();
				} else {
					thisCase.hasError = true;
					comment = "ERROR: getSpecimens, " + thisCase.caseNo + ", Specimen " + thisCase.noSpec
							+ ", Template " + rst.getInt("tmplt_profile_id") + ", Descr " + rst.getString("description")
							+ ", " + LConstants.ERROR_STRINGS[thisSpecimen.errorID];
					thisCase.comment += comment + "\n";
					pj.log(LConstants.ERROR_NONE, className, comment);
				}
			}
			if (thisCase.noSpec == 0) {
				thisCase.hasError = true;
				comment = "ERROR: getSpecimens, " + thisCase.caseNo + ", "
						+ LConstants.ERROR_STRINGS[LConstants.ERROR_SPECIMENS_COUNT_ZERO];
				thisCase.comment += comment + "\n";
				pj.log(LConstants.ERROR_NONE, className, comment);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private boolean isDuplicate(short codeID) {
		boolean duplicate = false;
		ResultSet rst = null;
		try {
			dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ADD_SL_CID), 1, thisCase.caseID);
			rst = dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_CID));
			while (rst.next()) {
				if (codeID == rst.getShort("ADCD")) {
					if (thisCase.finalID == rst.getShort("PRID")) {
						thisCase.accessed.setTimeInMillis(rst.getTimestamp("ADDT").getTime());
						if ((thisCase.finaled.getTimeInMillis() / 86400000)
								- (thisCase.accessed.getTimeInMillis() / 86400000) < 1) {
							// No duplicates on the same day
							duplicate = true;
							break;
						}
					}
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbPowerJ.close(rst);
		}
		return duplicate;
	}

	boolean isUpToDate() {
		return (System.currentTimeMillis() - maxDate < LDates.ONE_DAY);
	}

	private void saveCase(boolean isNew) {
		byte errorID = 0;
		if (thisCase.hasError) {
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
		if (thisCase.caseNo.length() > 12) {
			String temp = thisCase.caseNo;
			thisCase.caseNo = temp.substring(0, 7);
			temp = temp.substring(7);
			while (temp.length() > 5) {
				temp = temp.substring(1);
			}
			thisCase.caseNo += temp;
		}
		if (errorID > 0) {
			saveError(errorID);
		} else {
			if (thisCase.scanned.getTimeInMillis() > thisCase.finaled.getTimeInMillis()) {
				thisCase.scanned.setTimeInMillis(thisCase.finaled.getTimeInMillis());
			} else if (thisCase.scanned.getTimeInMillis() < thisCase.routed.getTimeInMillis()) {
				thisCase.scanned.setTimeInMillis(thisCase.finaled.getTimeInMillis());
			}
			if (thisCase.routed.getTimeInMillis() > thisCase.scanned.getTimeInMillis()) {
				// Use scan time (cytology does not route PAP/H&E, but routes SS/IHC)
				thisCase.routed.setTimeInMillis(thisCase.scanned.getTimeInMillis());
			} else if (thisCase.routed.getTimeInMillis() < thisCase.microed.getTimeInMillis()) {
				thisCase.routed.setTimeInMillis(thisCase.scanned.getTimeInMillis());
			}
			if (thisCase.microed.getTimeInMillis() > thisCase.routed.getTimeInMillis()) {
				thisCase.microed.setTimeInMillis(thisCase.routed.getTimeInMillis());
			} else if (thisCase.microed.getTimeInMillis() < thisCase.embeded.getTimeInMillis()) {
				thisCase.microed.setTimeInMillis(thisCase.routed.getTimeInMillis());
			}
			if (thisCase.embeded.getTimeInMillis() > thisCase.microed.getTimeInMillis()) {
				thisCase.embeded.setTimeInMillis(thisCase.microed.getTimeInMillis());
			} else if (thisCase.embeded.getTimeInMillis() < thisCase.grossed.getTimeInMillis()) {
				thisCase.embeded.setTimeInMillis(thisCase.microed.getTimeInMillis());
			}
			if (thisCase.grossed.getTimeInMillis() > thisCase.embeded.getTimeInMillis()) {
				thisCase.grossed.setTimeInMillis(thisCase.embeded.getTimeInMillis());
			} else if (thisCase.grossed.getTimeInMillis() < thisCase.accessed.getTimeInMillis()) {
				thisCase.grossed.setTimeInMillis(thisCase.embeded.getTimeInMillis());
			}
			thisCase.grossTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.grossed);
			thisCase.embedTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.embeded);
			thisCase.microTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.microed);
			thisCase.routeTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.routed);
			thisCase.finalTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.finaled);
			if (thisCase.grossTAT > Short.MAX_VALUE) {
				thisCase.grossTAT = Short.MAX_VALUE;
			}
			if (thisCase.embedTAT > Short.MAX_VALUE) {
				thisCase.embedTAT = Short.MAX_VALUE;
			}
			if (thisCase.microTAT > Short.MAX_VALUE) {
				thisCase.microTAT = Short.MAX_VALUE;
			}
			if (thisCase.routeTAT > Short.MAX_VALUE) {
				thisCase.routeTAT = Short.MAX_VALUE;
			}
			if (thisCase.finalTAT > Short.MAX_VALUE) {
				thisCase.finalTAT = Short.MAX_VALUE;
			}
			byte index = (isNew ? DPowerJ.STM_CSE_INSERT : DPowerJ.STM_CSE_UPDATE);
			dbPowerJ.setShort(pjStms.get(index), 1, thisCase.facID);
			dbPowerJ.setShort(pjStms.get(index), 2, thisCase.subID);
			dbPowerJ.setShort(pjStms.get(index), 3, thisCase.mainSpec);
			dbPowerJ.setShort(pjStms.get(index), 4, thisCase.grossID);
			dbPowerJ.setShort(pjStms.get(index), 5, thisCase.embedID);
			dbPowerJ.setShort(pjStms.get(index), 6, thisCase.microID);
			dbPowerJ.setShort(pjStms.get(index), 7, thisCase.routeID);
			dbPowerJ.setShort(pjStms.get(index), 8, thisCase.finalID);
			dbPowerJ.setInt(pjStms.get(index), 9, thisCase.grossTAT);
			dbPowerJ.setInt(pjStms.get(index), 10, thisCase.embedTAT);
			dbPowerJ.setInt(pjStms.get(index), 11, thisCase.microTAT);
			dbPowerJ.setInt(pjStms.get(index), 12, thisCase.routeTAT);
			dbPowerJ.setInt(pjStms.get(index), 13, thisCase.finalTAT);
			dbPowerJ.setShort(pjStms.get(index), 14, thisCase.noSpec);
			dbPowerJ.setShort(pjStms.get(index), 15, thisCase.noBlocks);
			dbPowerJ.setShort(pjStms.get(index), 16, thisCase.noSlides);
			dbPowerJ.setShort(pjStms.get(index), 17, thisCase.noSynop);
			dbPowerJ.setShort(pjStms.get(index), 18, thisCase.noFSSpec);
			dbPowerJ.setShort(pjStms.get(index), 19, thisCase.noHE);
			dbPowerJ.setShort(pjStms.get(index), 20, thisCase.noSS);
			dbPowerJ.setShort(pjStms.get(index), 21, thisCase.noIHC);
			dbPowerJ.setShort(pjStms.get(index), 22, thisCase.noMol);
			dbPowerJ.setInt(pjStms.get(index), 23, thisCase.value5);
			dbPowerJ.setTime(pjStms.get(index), 24, thisCase.accessed.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(index), 25, thisCase.grossed.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(index), 26, thisCase.embeded.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(index), 27, thisCase.microed.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(index), 28, thisCase.routed.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(index), 29, thisCase.finaled.getTimeInMillis());
			dbPowerJ.setDouble(pjStms.get(index), 30, coder1.getValue());
			dbPowerJ.setDouble(pjStms.get(index), 31, coder2.getValue());
			dbPowerJ.setDouble(pjStms.get(index), 32, coder3.getValue());
			dbPowerJ.setDouble(pjStms.get(index), 33, coder4.getValue());
			dbPowerJ.setString(pjStms.get(index), 34, thisCase.caseNo);
			dbPowerJ.setLong(pjStms.get(index), 35, thisCase.caseID);
			if (dbPowerJ.execute(pjStms.get(index)) > 0) {
				saveSpecimens(isNew);
				saveComment(isNew);
			}
		}
	}

	private void saveError(byte errorID) {
		switch (errorID) {
		case 1:
			comment = thisCase.comment;
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
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ERR_INSERT), 1, thisCase.caseID);
		dbPowerJ.setByte(pjStms.get(DPowerJ.STM_ERR_INSERT), 2, errorID);
		dbPowerJ.setString(pjStms.get(DPowerJ.STM_ERR_INSERT), 3, thisCase.caseNo);
		dbPowerJ.setString(pjStms.get(DPowerJ.STM_ERR_INSERT), 4, comment);
		dbPowerJ.execute(pjStms.get(DPowerJ.STM_ERR_INSERT));
	}

	private void saveComment(boolean isNew) {
		byte index = (isNew ? DPowerJ.STM_CMT_INSERT : DPowerJ.STM_CMT_UPDATE);
		comment = "";
		if (thisCase.comment.length() > 0) {
			comment = thisCase.comment + "\n--------------------------\n";
		} else if (coder1.hasComment()) {
			comment = coder1.getComment();
		}
		if (comment.length() > 2048) {
			comment = comment.substring(0, 2048);
		}
		dbPowerJ.setString(pjStms.get(index), 1, comment);
		comment = "";
		if (coder2.hasComment()) {
			comment = coder2.getComment();
			if (comment.length() > 2048) {
				comment = comment.substring(0, 2048);
			}
		}
		dbPowerJ.setString(pjStms.get(index), 2, comment);
		comment = "";
		if (coder3.hasComment()) {
			comment = coder3.getComment();
			if (comment.length() > 2048) {
				comment = comment.substring(0, 2048);
			}
		}
		dbPowerJ.setString(pjStms.get(index), 3, comment);
		comment = "";
		if (coder4.hasComment()) {
			comment = coder4.getComment();
			if (comment.length() > 2048) {
				comment = comment.substring(0, 2048);
			}
		}
		dbPowerJ.setString(pjStms.get(index), 4, comment);
		dbPowerJ.setLong(pjStms.get(index), 5, thisCase.caseID);
		dbPowerJ.execute(pjStms.get(index));
	}

	private void saveOrders(boolean isNew, int i) {
		byte index = (isNew ? DPowerJ.STM_ORD_INSERT : DPowerJ.STM_ORD_UPDATE);
		for (Entry<Short, OOrderFinal> orderEntry : thisSpecimen.lstOrders.entrySet()) {
			thisOrder = orderEntry.getValue();
			if (thisOrder.qty > 0) {
				thisOrder.value1 = coder1.getOrder(i, thisOrder.grpID);
				thisOrder.value2 = coder2.getOrder(i, thisOrder.grpID);
				thisOrder.value3 = coder3.getOrder(i, thisOrder.grpID);
				thisOrder.value4 = coder4.getOrder(i, thisOrder.grpID);
				dbPowerJ.setShort(pjStms.get(index), 1, thisOrder.qty);
				dbPowerJ.setDouble(pjStms.get(index), 2, thisOrder.value1);
				dbPowerJ.setDouble(pjStms.get(index), 3, thisOrder.value2);
				dbPowerJ.setDouble(pjStms.get(index), 4, thisOrder.value3);
				dbPowerJ.setDouble(pjStms.get(index), 5, thisOrder.value4);
				dbPowerJ.setShort(pjStms.get(index), 6, thisOrder.grpID);
				dbPowerJ.setLong(pjStms.get(index), 7, thisSpecimen.specID);
				dbPowerJ.execute(pjStms.get(index));
			}
		}
		// Save Frozen sections
		if (thisSpecimen.noFSBlks > 0 || thisSpecimen.noFSSlds > 0) {
			index = (isNew ? DPowerJ.STM_FRZ_INSERT : DPowerJ.STM_FRZ_UPDATE);
			dbPowerJ.setShort(pjStms.get(index), 1, thisSpecimen.noFSBlks);
			dbPowerJ.setShort(pjStms.get(index), 2, thisSpecimen.noFSSlds);
			dbPowerJ.setInt(pjStms.get(index), 3, 0);
			dbPowerJ.setInt(pjStms.get(index), 4, value5fs);
			dbPowerJ.setDouble(pjStms.get(index), 5, coder1.getFrozen(i));
			dbPowerJ.setDouble(pjStms.get(index), 6, coder2.getFrozen(i));
			dbPowerJ.setDouble(pjStms.get(index), 7, coder3.getFrozen(i));
			dbPowerJ.setDouble(pjStms.get(index), 8, coder4.getFrozen(i));
			dbPowerJ.setLong(pjStms.get(index), 9, thisSpecimen.specID);
			dbPowerJ.execute(pjStms.get(index));
		}
	}

	private void saveSpecimens(boolean isNew) {
		byte index = (isNew ? DPowerJ.STM_SPE_INSERT : DPowerJ.STM_SPE_UPDATE);
		for (int i = 0; i < thisCase.noSpec; i++) {
			thisSpecimen = thisCase.lstSpecimens.get(i);
			if (thisSpecimen.descr.length() > 64) {
				thisSpecimen.descr = thisSpecimen.descr.substring(0, 64);
			}
			dbPowerJ.setLong(pjStms.get(index), 1, thisCase.caseID);
			dbPowerJ.setShort(pjStms.get(index), 2, thisSpecimen.spmID);
			dbPowerJ.setShort(pjStms.get(index), 3, thisSpecimen.noBlocks);
			dbPowerJ.setShort(pjStms.get(index), 4, thisSpecimen.noSlides);
			dbPowerJ.setShort(pjStms.get(index), 5, thisSpecimen.noFrags);
			dbPowerJ.setShort(pjStms.get(index), 6, thisSpecimen.noHE);
			dbPowerJ.setShort(pjStms.get(index), 7, thisSpecimen.noSS);
			dbPowerJ.setShort(pjStms.get(index), 8, thisSpecimen.noIHC);
			dbPowerJ.setShort(pjStms.get(index), 9, thisSpecimen.noMOL);
			dbPowerJ.setInt(pjStms.get(index), 10, thisSpecimen.value5);
			dbPowerJ.setDouble(pjStms.get(index), 11, coder1.getValue(i));
			dbPowerJ.setDouble(pjStms.get(index), 12, coder2.getValue(i));
			dbPowerJ.setDouble(pjStms.get(index), 13, coder3.getValue(i));
			dbPowerJ.setDouble(pjStms.get(index), 14, coder4.getValue(i));
			dbPowerJ.setString(pjStms.get(index), 15, thisSpecimen.descr);
			dbPowerJ.setLong(pjStms.get(index), 16, thisSpecimen.specID);
			if (dbPowerJ.execute(pjStms.get(index)) > 0) {
				saveOrders(isNew, i);
			}
		}
	}
}