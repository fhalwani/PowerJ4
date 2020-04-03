package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

class LPending {
	int noUpdates = 0;
	private final long startTime = System.currentTimeMillis();
	private final String className = "Workflow";
	private MFacilities facilities;
	private MAccessions accessions;
	private MOrders masterOrders;
	private MSpecimens masterSpecimens;
	private Hashtable<Byte, PreparedStatement> pjStms = null;
	private Hashtable<Byte, PreparedStatement> apStms = null;
	private LBase pj;
	private DPowerJ dbPowerJ;
	private DPowerpath dbAP;
	private OCasePending thisCase = new OCasePending();
	private ArrayList<OCasePending> list = new ArrayList<OCasePending>();

	LPending(boolean firstRun, LBase pj) {
		LBase.busy.set(true);
		this.pj = pj;
		dbAP = pj.dbAP;
		dbPowerJ = pj.dbPowerJ;
		pj.log(LConstants.ERROR_NONE, className,
				pj.dates.formatter(LDates.FORMAT_DATETIME) + " - Workflow Manager Started...");
		try {
			apStms = dbAP.prepareStatements(LConstants.ACTION_LFLOW);
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				pjStms = dbPowerJ.prepareStatements(LConstants.ACTION_LFLOW);
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
				getLastUpdate();
			}
			if (pj.errorID == LConstants.ERROR_NONE && firstRun && (!pj.abort())) {
				deleteComplete();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				getAccessions();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				getCases();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort() && firstRun) {
				getCanceled();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				getGrossed();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				getEmbeded();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				getMicrotomed();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				getRouted();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				getFinal();
			}
			if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
				long noSeconds = (System.currentTimeMillis() - startTime) / 1000000000;
				if (list.size() > 0 && noSeconds > 0) {
					pj.log(LConstants.ERROR_NONE, className,
							"Updated the status of " + pj.numbers.formatNumber(list.size()) + " cases in "
									+ pj.numbers.formatNumber(noSeconds) + " seconds ("
									+ pj.numbers.formatNumber((list.size() * 60 / noSeconds)) + "/min)");
				}
			}
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, className, e);
		} finally {
			close();
		}
	}

	private void close() {
		list.clear();
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
		if (dbAP != null && apStms != null) {
			dbAP.close(apStms);
		}
		if (dbPowerJ != null && pjStms != null) {
			dbPowerJ.close(pjStms);
		}
		LBase.busy.set(false);
	}

	/** Sync the cache database when a case is deleted in PowerPath. **/
	private int deleteCase(long caseID) {
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_PND_DEL_ID), 1, caseID);
		return dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_DEL_ID));
	}

	/**
	 * Keep completed Cases from last 2 weeks to measure
	 * workload/turn-around/routing.
	 **/
	private void deleteComplete() {
		Calendar calDate = Calendar.getInstance();
		calDate.add(Calendar.DAY_OF_YEAR, -14);
		calDate.set(Calendar.HOUR_OF_DAY, 0);
		calDate.set(Calendar.MINUTE, 0);
		calDate.set(Calendar.SECOND, 0);
		calDate.set(Calendar.MILLISECOND, 1);
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_DEL_FN), 1, calDate.getTimeInMillis());
		noUpdates = dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_DEL_FN));
		if (noUpdates > 0) {
			pj.log(LConstants.ERROR_NONE, className,
					"Deleted " + pj.numbers.formatNumber(noUpdates) + " completed cases.");
		}
	}

	/** Retrieve new cases added since last run. */
	private void getAccessions() {
		noUpdates = 0;
		ResultSet rst = null;
		try {
			dbAP.setTime(apStms.get(DPowerpath.STM_CASES_ACCESS), 1, pj.lastUpdate);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASES_ACCESS));
			while (rst.next()) {
				if (accessions.doworkflow(rst.getShort("acc_type_id"))
						&& facilities.doworkflow(rst.getShort("facility_id"))) {
					thisCase = new OCasePending();
					thisCase.caseID = rst.getLong("id");
					thisCase.facID = rst.getShort("facility_id");
					thisCase.accessed.setTimeInMillis(rst.getTimestamp("created_date").getTime());
					thisCase.grossed.setTimeInMillis(0);
					thisCase.embeded.setTimeInMillis(0);
					thisCase.microed.setTimeInMillis(0);
					thisCase.routed.setTimeInMillis(0);
					thisCase.finaled.setTimeInMillis(0);
					thisCase.caseNo = rst.getString("accession_no");
					getSpecimens();
					if (thisCase.noSpec > 0) {
						if (insertCase() > 0) {
							noUpdates++;
						}
					}
				}
				if (pj.abort()) {
					break;
				}
				if (noUpdates % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, className, "Added " + pj.numbers.formatNumber(noUpdates) + " new cases.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	/** Delete Cancelled cases. */
	private void getCanceled() {
		Calendar now = Calendar.getInstance();
		noUpdates = 0;
		ResultSet rst = null;
		try {
			for (int i = list.size() - 1; i >= 0; i--) {
				dbAP.setLong(apStms.get(DPowerpath.STM_CASE_PROCESS), 1, list.get(i).caseID);
				rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_PROCESS));
				while (rst.next()) {
					if (rst.getString("description") != null) {
						if (rst.getString("description").toLowerCase().contains("cancel")) {
							if (deleteCase(list.get(i).caseID) > 0) {
								list.get(i).cancel = true;
								noUpdates++;
							}
						}
					}
				}
				rst.close();
				if (!list.get(i).cancel) {
					if (list.get(i).statusID < OCaseStatus.ID_ROUTE) {
						// delete abandoned cases
						if (pj.dates.getBusinessDays(list.get(i).accessed, now) > 40) {
							if (deleteCase(list.get(i).caseID) > 0) {
								list.get(i).cancel = true;
								noUpdates++;
							}
						}
					}
				}
				if (list.get(i).cancel) {
					list.remove(i);
				}
				if (pj.abort()) {
					break;
				} else if (i % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			if (noUpdates > 0) {
				pj.log(LConstants.ERROR_NONE, className,
						"Deleted " + pj.numbers.formatNumber(noUpdates) + " cancelled or abandoned cases.");
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	/** Get all pending cases from PowerJ. */
	private void getCases() {
		ResultSet rst = dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PND_SELECT));
		try {
			while (rst.next()) {
				if (rst.getByte("PNST") == OCaseStatus.ID_FINAL) {
					continue;
				}
				thisCase = new OCasePending();
				thisCase.caseID = rst.getLong("PNID");
				thisCase.accessed.setTimeInMillis(rst.getTimestamp("ACED").getTime());
				thisCase.statusID = rst.getByte("PNST");
				thisCase.subID = rst.getByte("SBID");
				thisCase.procID = rst.getByte("POID");
				thisCase.noSpec = rst.getByte("PNSP");
				thisCase.facID = rst.getShort("FAID");
				thisCase.value5 = rst.getInt("PNV5");
				thisCase.noBlocks = rst.getShort("PNBL");
				thisCase.noSlides = rst.getShort("PNSL");
				thisCase.mainSpec = rst.getShort("SMID");
				thisCase.caseNo = rst.getString("PNNO");
				if (thisCase.statusID > OCaseStatus.ID_ACCES) {
					thisCase.grossed.setTimeInMillis(rst.getTimestamp("GRED").getTime());
					thisCase.grossID = rst.getShort("GRID");
					thisCase.grossTAT = rst.getShort("GRTA");
				}
				if (thisCase.statusID > OCaseStatus.ID_GROSS) {
					thisCase.embeded.setTimeInMillis(rst.getTimestamp("EMED").getTime());
					thisCase.embedID = rst.getShort("EMID");
					thisCase.embedTAT = rst.getShort("EMTA");
				}
				if (thisCase.statusID > OCaseStatus.ID_EMBED) {
					thisCase.microed.setTimeInMillis(rst.getTimestamp("MIED").getTime());
					thisCase.microID = rst.getShort("MIID");
					thisCase.microTAT = rst.getShort("MITA");
				}
				if (thisCase.statusID > OCaseStatus.ID_MICRO) {
					thisCase.routed.setTimeInMillis(rst.getTimestamp("ROED").getTime());
					thisCase.routeID = rst.getShort("ROID");
					thisCase.routeTAT = rst.getShort("ROTA");
				}
				if (thisCase.statusID > OCaseStatus.ID_ROUTE) {
					thisCase.finaled.setTimeInMillis(rst.getTimestamp("FNED").getTime());
					thisCase.finalID = rst.getShort("FNID");
					thisCase.finalTAT = rst.getShort("FNTA");
				}
				list.add(thisCase);
				if (pj.abort()) {
					break;
				} else if (list.size() % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, className,
					"Scanning " + pj.numbers.formatNumber(list.size()) + " pending cases.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbPowerJ.close(rst);
		}
	}

	/** Update embedding status of grossed cases. */
	private void getEmbeded() {
		short noBlocks = 0;
		ResultSet rst = null;
		try {
			noUpdates = 0;
			for (int i = 0; i < list.size(); i++) {
				thisCase = list.get(i);
				if (!thisCase.cancel) {
					if (thisCase.statusID < OCaseStatus.ID_EMBED) {
						getSpecimens();
						if (thisCase.noSpec > 0) {
							dbAP.setLong(apStms.get(DPowerpath.STM_CASE_EMBEDED), 1, thisCase.caseID);
							rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_EMBEDED));
							noBlocks = 0;
							while (rst.next()) {
								noBlocks++;
								if (noBlocks >= thisCase.noBlocks) {
									thisCase.statusID = OCaseStatus.ID_EMBED;
									thisCase.embeded.setTimeInMillis(rst.getTimestamp("event_date").getTime());
									thisCase.embedID = rst.getShort("personnel_id");
									thisCase.update = true;
								}
							}
							rst.close();
							if (thisCase.update) {
								if (updateEmbeded() > 0) {
									thisCase.update = false;
									noUpdates++;
								}
							}
						} else if (deleteCase(thisCase.caseID) > 0) {
							thisCase.cancel = true;
						}
					}
				}
				if (pj.abort()) {
					break;
				} else if (i % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, className,
					"Updated " + pj.numbers.formatNumber(noUpdates) + " embeded cases.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	/** Update final status of routed cases. */
	private void getFinal() {
		String descr = "";
		ResultSet rst = null;
		try {
			noUpdates = 0;
			for (int i = 0; i < list.size(); i++) {
				thisCase = list.get(i);
				if (!thisCase.cancel) {
					dbAP.setLong(apStms.get(DPowerpath.STM_CASE_PROCESS), 1, thisCase.caseID);
					rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_PROCESS));
					while (rst.next()) {
						if (rst.getString("description") != null) {
							descr = rst.getString("description").trim().toLowerCase();
							if (descr.equals("final")) {
								if (rst.getTimestamp("completed_date") != null) {
									thisCase.statusID = OCaseStatus.ID_FINAL;
									thisCase.finaled.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
									thisCase.finalID = rst.getShort("assigned_to_id");
									thisCase.noSlides = getNoSlides();
									thisCase.update = true;
								} else if (thisCase.statusID >= OCaseStatus.ID_ROUTE) {
									thisCase.finalID = rst.getShort("assigned_to_id");
									thisCase.update = true;
								}
								break;
							} else if (descr.contains("microscopic") || descr.contains("pathologist")) {
								if (rst.getTimestamp("completed_date") != null) {
									thisCase.statusID = OCaseStatus.ID_DIAGN;
									thisCase.finaled.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
									thisCase.finalID = rst.getShort("assigned_to_id");
									thisCase.update = true;
								} else if (thisCase.statusID >= OCaseStatus.ID_ROUTE) {
									thisCase.finalID = rst.getShort("assigned_to_id");
									thisCase.update = true;
								}
							}
						}
					}
					rst.close();
					if (thisCase.update) {
						if (updateFinal() > 0) {
							thisCase.update = false;
							noUpdates++;
						}
					}
				}
				if (pj.abort()) {
					break;
				} else if (i % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, className,
					"Updated " + pj.numbers.formatNumber(noUpdates) + " diagnosis cases.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	/** Update grossing status of accessioned cases. */
	private void getGrossed() {
		String descr = "";
		ResultSet rst = null;
		try {
			noUpdates = 0;
			for (int i = 0; i < list.size(); i++) {
				thisCase = list.get(i);
				if (!thisCase.cancel) {
					if (thisCase.statusID < OCaseStatus.ID_GROSS) {
						getSpecimens();
						if (thisCase.noSpec > 0) {
							dbAP.setLong(apStms.get(DPowerpath.STM_CASE_PROCESS), 1, thisCase.caseID);
							rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_PROCESS));
							while (rst.next()) {
								if (rst.getString("description") != null) {
									if (rst.getTimestamp("completed_date") != null) {
										descr = rst.getString("description").trim().toLowerCase();
										if (descr.contains("gross") || descr.contains("screening")
												|| descr.contains("provisional")) {
											thisCase.statusID = OCaseStatus.ID_GROSS;
											thisCase.grossed.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
											thisCase.grossID = rst.getShort("assigned_to_id");
											thisCase.update = true;
											break;
										}
									}
								}
							}
							rst.close();
							if (thisCase.update) {
								if (updateGrossed() > 0) {
									thisCase.update = false;
									noUpdates++;
								}
							}
						} else if (deleteCase(thisCase.caseID) > 0) {
							thisCase.cancel = true;
						}
					}
				}
				if (pj.abort()) {
					break;
				} else if (i % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, className,
					"Updated " + pj.numbers.formatNumber(noUpdates) + " grossed cases.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	private void getLastUpdate() {
		long accession = dbPowerJ.getTime(pjStms.get(DPowerJ.STM_PND_SL_LST));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -2);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		if (cal.getTimeInMillis() < accession) {
			pj.lastUpdate = accession;
		} else {
			pj.lastUpdate = cal.getTimeInMillis();
		}
	}

	/** Update microtomy status of embeded cases. */
	private void getMicrotomed() {
		short noBlocks = 0;
		ResultSet rst = null;
		try {
			noUpdates = 0;
			for (int i = 0; i < list.size(); i++) {
				thisCase = list.get(i);
				if (!thisCase.cancel) {
					if (thisCase.statusID < OCaseStatus.ID_MICRO) {
						noBlocks = 0;
						dbAP.setLong(apStms.get(DPowerpath.STM_CASE_MICROTO), 1, thisCase.caseID);
						rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_MICROTO));
						while (rst.next()) {
							noBlocks++;
							if (noBlocks >= thisCase.noBlocks) {
								thisCase.statusID = OCaseStatus.ID_MICRO;
								thisCase.microed.setTimeInMillis(rst.getTimestamp("event_date").getTime());
								thisCase.microID = rst.getShort("personnel_id");
								thisCase.update = true;
							}
						}
						rst.close();
						if (thisCase.update) {
							thisCase.noSlides = getNoSlides();
							if (updateMicrotomed() > 0) {
								thisCase.update = false;
								noUpdates++;
							}
						}
					}
				}
				if (pj.abort()) {
					break;
				} else if (i % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, className,
					"Updated " + pj.numbers.formatNumber(noUpdates) + " microtomed cases.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
	}

	/** Update Number of blocks for a specimen. */
	private short getNoBlocks(long specID) {
		dbAP.setLong(apStms.get(DPowerpath.STM_CASE_BLOCKS), 1, specID);
		return dbAP.getShort(apStms.get(DPowerpath.STM_CASE_BLOCKS));
	}

	/** Update Number of stained slides for a case. */
	private short getNoSlides() {
		short noSlides = 0;
		ResultSet rst = null;
		try {
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_ORDERS), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_ORDERS));
			while (rst.next()) {
				if (masterOrders.matchOrder(rst.getShort("procedure_id"))) {
					switch (masterOrders.getOrderType()) {
					case OOrderType.SLIDE:
					case OOrderType.SLD_FS:
					case OOrderType.SS:
					case OOrderType.IHC:
					case OOrderType.FISH:
						noSlides += rst.getInt("quantity");
						break;
					default:
						// Ignore
					}
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbAP.close(rst);
		}
		return noSlides;
	}

	/** Update Routing status of cases. */
	private void getRouted() {
		ResultSet rst = null;
		try {
			noUpdates = 0;
			for (int i = 0; i < list.size(); i++) {
				thisCase = list.get(i);
				if (!thisCase.cancel) {
					if (thisCase.statusID < OCaseStatus.ID_ROUTE) {
						dbAP.setLong(apStms.get(DPowerpath.STM_CASE_ROUTING), 1, thisCase.caseID);
						rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_ROUTING));
						while (rst.next()) {
							thisCase.statusID = OCaseStatus.ID_ROUTE;
							thisCase.routed.setTimeInMillis(rst.getTimestamp("event_date").getTime());
							thisCase.routeID = rst.getShort("personnel_id");
							thisCase.update = true;
							break;
						}
						rst.close();
						if (thisCase.update) {
							thisCase.noSlides = getNoSlides();
							if (updateRouted() > 0) {
								thisCase.update = false;
								noUpdates++;
							}
						}
					}
				}
				if (pj.abort()) {
					break;
				} else if (i % 100 == 0) {
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException ignore) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, className,
					"Updated " + pj.numbers.formatNumber(noUpdates) + " routed cases.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
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
			// May be called from Grossing or Histology
			// re-validate specimens & specialty
			thisCase.noBlocks = 0;
			thisCase.mainBlks = 0;
			thisCase.noSpec = 0;
			thisCase.procID = 0;
			thisCase.subID = 0;
			thisCase.mainSpec = 0;
			thisCase.value5 = 0;
			dbAP.setLong(apStms.get(DPowerpath.STM_CASE_SPCMNS), 1, thisCase.caseID);
			rst = dbAP.getResultSet(apStms.get(DPowerpath.STM_CASE_SPCMNS));
			while (rst.next()) {
				if (masterSpecimens.matchSpecimens(rst.getShort("tmplt_profile_id"))) {
					noBlocks = getNoBlocks(rst.getInt("id"));
					thisCase.noSpec++;
					thisCase.noBlocks += noBlocks;
					thisCase.value5 += masterSpecimens.getValue5();
					if (thisCase.mainSpec == 0) {
						thisCase.procID = masterSpecimens.getProcedureID();
						thisCase.subID = masterSpecimens.getSubspecialtyID(rst.getString("label_name"),
								rst.getString("description"));
						thisCase.mainBlks = noBlocks;
						thisCase.mainSpec = rst.getShort("tmplt_profile_id");
					} else if (thisCase.procID < masterSpecimens.getProcedureID()) {
						thisCase.procID = masterSpecimens.getProcedureID();
						thisCase.subID = masterSpecimens.getSubspecialtyID(rst.getString("label_name"),
								rst.getString("description"));
						thisCase.mainBlks = noBlocks;
						thisCase.mainSpec = rst.getShort("tmplt_profile_id");
					} else if (thisCase.procID == masterSpecimens.getProcedureID()) {
						if (thisCase.subID < masterSpecimens.getSubspecialtyID()) {
							thisCase.subID = masterSpecimens.getSubspecialtyID(rst.getString("label_name"),
									rst.getString("description"));
							thisCase.mainBlks = noBlocks;
							thisCase.mainSpec = rst.getShort("tmplt_profile_id");
						} else if (thisCase.mainBlks < noBlocks) {
							thisCase.mainBlks = noBlocks;
							thisCase.mainSpec = rst.getShort("tmplt_profile_id");
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

	private int insertCase() {
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 1, thisCase.facID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 2, thisCase.subID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 3, thisCase.procID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 4, thisCase.mainSpec);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 5, thisCase.grossID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 6, thisCase.embedID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 7, thisCase.microID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 8, thisCase.routeID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 9, thisCase.finalID);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_INSERT), 10, thisCase.grossTAT);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_INSERT), 11, thisCase.embedTAT);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_INSERT), 12, thisCase.microTAT);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_INSERT), 13, thisCase.routeTAT);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_INSERT), 14, thisCase.finalTAT);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 15, thisCase.statusID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 16, thisCase.noSpec);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 17, thisCase.noBlocks);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_INSERT), 18, thisCase.noSlides);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_INSERT), 19, thisCase.value5);
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_INSERT), 20, thisCase.accessed.getTimeInMillis());
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_INSERT), 21, thisCase.grossed.getTimeInMillis());
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_INSERT), 22, thisCase.embeded.getTimeInMillis());
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_INSERT), 23, thisCase.microed.getTimeInMillis());
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_INSERT), 24, thisCase.routed.getTimeInMillis());
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_INSERT), 25, thisCase.finaled.getTimeInMillis());
		dbPowerJ.setString(pjStms.get(DPowerJ.STM_PND_INSERT), 26, thisCase.caseNo);
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_PND_INSERT), 27, thisCase.caseID);
		return dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_INSERT));
	}

	private int updateEmbeded() {
		thisCase.embedTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.embeded);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_EMB), 1, thisCase.subID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_EMB), 2, thisCase.procID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_EMB), 3, thisCase.mainSpec);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_EMB), 4, thisCase.embedID);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_UP_EMB), 5, thisCase.embedTAT);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_EMB), 6, thisCase.statusID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_EMB), 7, thisCase.noSpec);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_EMB), 8, thisCase.noBlocks);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_UP_EMB), 9, thisCase.value5);
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_UP_EMB), 10, thisCase.embeded.getTimeInMillis());
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_PND_UP_EMB), 11, thisCase.caseID);
		return dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_UP_EMB));
	}

	private int updateFinal() {
		thisCase.finalTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.finaled);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_FIN), 1, thisCase.finalID);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_UP_FIN), 2, thisCase.finalTAT);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_FIN), 3, thisCase.statusID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_FIN), 4, thisCase.noSlides);
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_UP_FIN), 5, thisCase.finaled.getTimeInMillis());
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_PND_UP_FIN), 6, thisCase.caseID);
		return dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_UP_FIN));
	}

	private int updateGrossed() {
		thisCase.grossTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.grossed);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_GRS), 1, thisCase.subID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_GRS), 2, thisCase.procID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_GRS), 3, thisCase.mainSpec);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_GRS), 4, thisCase.grossID);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_UP_GRS), 5, thisCase.grossTAT);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_GRS), 6, thisCase.statusID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_GRS), 7, thisCase.noSpec);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_GRS), 8, thisCase.noBlocks);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_UP_GRS), 9, thisCase.value5);
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_UP_GRS), 10, thisCase.grossed.getTimeInMillis());
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_PND_UP_GRS), 11, thisCase.caseID);
		return dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_UP_GRS));
	}

	private int updateMicrotomed() {
		thisCase.microTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.microed);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_MIC), 1, thisCase.microID);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_UP_MIC), 2, thisCase.microTAT);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_MIC), 3, thisCase.statusID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_MIC), 4, thisCase.noBlocks);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_MIC), 5, thisCase.noSlides);
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_UP_MIC), 6, thisCase.microed.getTimeInMillis());
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_PND_UP_MIC), 7, thisCase.caseID);
		return dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_UP_MIC));
	}

	private int updateRouted() {
		thisCase.routeTAT = pj.dates.getBusinessHours(thisCase.accessed, thisCase.routed);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_ROU), 1, thisCase.routeID);
		dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PND_UP_ROU), 2, thisCase.routeTAT);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_ROU), 3, thisCase.statusID);
		dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PND_UP_ROU), 4, thisCase.noSlides);
		dbPowerJ.setTime(pjStms.get(DPowerJ.STM_PND_UP_ROU), 5, thisCase.routed.getTimeInMillis());
		dbPowerJ.setLong(pjStms.get(DPowerJ.STM_PND_UP_ROU), 6, thisCase.caseID);
		return dbPowerJ.execute(pjStms.get(DPowerJ.STM_PND_UP_ROU));
	}
}