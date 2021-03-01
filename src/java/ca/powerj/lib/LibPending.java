package ca.powerj.lib;
import java.util.ArrayList;
import java.util.Calendar;
import ca.powerj.app.PJServer;
import ca.powerj.data.AccessionsList;
import ca.powerj.data.CaseData;
import ca.powerj.data.FacilityList;
import ca.powerj.data.OrderData;
import ca.powerj.data.OrderMasterList;
import ca.powerj.data.PathologistList;
import ca.powerj.data.SpecimenData;
import ca.powerj.data.SpecimensList;

public class LibPending {
	private int noUpdates = 0;
	private int maxFte5 = 0;
	private final long startTime = System.currentTimeMillis();
	private final String className = "Workflow";
	private AccessionsList accessions;
	private FacilityList facilities;
	private PathologistList pathologists;
	private OrderMasterList masterOrders;
	private SpecimensList masterSpecimens;
	private PJServer base;
	private CaseData thisCase = new CaseData();
	private ArrayList<CaseData> list = new ArrayList<CaseData>();

	public LibPending(boolean firstRun, PJServer base) {
		this.base = base;
		base.setBusy(true);
		base.log(LibConstants.ERROR_NONE, className,
				base.dates.formatter(LibDates.FORMAT_DATETIME) + " - Workflow Manager Started...");
		try {
			maxFte5 = base.setup.getInt(LibSetup.VAR_V5_FTE) / 215;
			base.dbPath.setStatements(LibConstants.ACTION_LFLOW);
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				base.dbPowerJ.setStatements(LibConstants.ACTION_LFLOW);
			}
			if (base.errorID == LibConstants.ERROR_NONE && firstRun && (!base.isStopping())) {
				deleteComplete();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				accessions = new AccessionsList(base.dbPowerJ);
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				facilities = new FacilityList(base.dbPowerJ);
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				masterOrders = new OrderMasterList(base.dbPowerJ);
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				pathologists = new PathologistList(base.dbPowerJ);
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				masterSpecimens = new SpecimensList(base.dbPowerJ);
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getNewCases();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getPendingCases();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping() && firstRun) {
				getCanceled();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getGrossed();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getEmbeded();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getMicrotomed();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getRouted();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getScanned();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				getFinal();
			}
			if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
				long noSeconds = (System.currentTimeMillis() - startTime) / 1000;
				if (list.size() > 0 && noSeconds > 0) {
					base.log(LibConstants.ERROR_NONE, className,
						"Updated the status of " + base.numbers.formatNumber(list.size()) + " cases in "
						+ base.numbers.formatNumber(noSeconds) + " seconds ("
						+ base.numbers.formatNumber((list.size() * 60 / noSeconds)) + "/min)");
				}
			}
		} catch (Exception e) {
			base.log(LibConstants.ERROR_UNEXPECTED, className, e);
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
		if (pathologists != null) {
			pathologists.close();
		}
		if (facilities != null) {
			facilities.close();
		}
		if (accessions != null) {
			accessions.close();
		}
		if (base.dbPath != null) {
			base.dbPath.closeStms();
		}
		if (base.dbPowerJ != null) {
			base.dbPowerJ.closeStms();
		}
		base.setBusy(false);
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
		noUpdates = base.dbPowerJ.deletePendingFinal(calDate.getTimeInMillis());
		if (noUpdates > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					"Deleted " + base.numbers.formatNumber(noUpdates) + " completed cases.");
		}
	}

	private void getCanceled() {
		Calendar calDate = Calendar.getInstance();
		calDate.add(Calendar.DAY_OF_YEAR, -60);
		noUpdates = 0;
		for (int i = list.size() -1; i >= 0; i--) {
			thisCase = list.get(i);
			if (base.dbPath.getCanceled(thisCase.getCaseID())) {
				if (base.dbPowerJ.deletePendingCancelled(thisCase.getCaseID()) > 0) {
					list.remove(i);
					noUpdates++;
				}
			} else if (thisCase.getAccessTime() < calDate.getTimeInMillis()) {
				if (base.dbPowerJ.deletePendingCancelled(thisCase.getCaseID()) > 0) {
					list.remove(i);
					noUpdates++;
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className,
				"Deleted " + base.numbers.formatNumber(noUpdates) + " cancelled cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/** Update embedding status of grossed cases. */
	private void getEmbeded() {
		noUpdates = 0;
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			if (thisCase.getStatusID() < LibConstants.STATUS_EMBED || thisCase.getEmbedTAT() < 1) {
				getSpecimens();
				if (thisCase.getNoSpecs() > 0) {
					if (base.dbPath.getEmbedded(thisCase)) {
						if (thisCase.getStatusID() < LibConstants.STATUS_EMBED) {
							thisCase.setStatusID(LibConstants.STATUS_EMBED);
						}
						thisCase.setEmbedTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(),
								thisCase.getEmbedCalendar()));
						if (base.dbPowerJ.setPendingEmbeded(thisCase) > 0) {
							noUpdates++;
						}
					}
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className, "Updated " + base.numbers.formatNumber(noUpdates) + " embeded cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/** Update final status of routed cases. */
	private void getFinal() {
		boolean update = false;
		noUpdates = 0;
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			// QCH Cytology does not have histology nor routing
			getOrders();
			if (base.dbPath.getFinaled(thisCase)) {
				if (thisCase.getStatusID() == LibConstants.STATUS_FINAL) {
					update = true;
				} else if (thisCase.getStatusID() == LibConstants.STATUS_DIAGN
						&& pathologists.matchPathologist(thisCase.getFinalID())) {
					update = true;
				} else {
					update = false;
				}
				if (update) {
					thisCase.setFinalTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getFinalCalendar()));
					if (base.dbPowerJ.setPendingFinal(thisCase) > 0) {
						noUpdates++;
					}
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className, "Updated " + base.numbers.formatNumber(noUpdates) + " diagnosis cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/** Update grossing status of accessioned cases. */
	private void getGrossed() {
		noUpdates = 0;
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			if (thisCase.getStatusID() < LibConstants.STATUS_GROSS || thisCase.getGrossTAT() < 1) {
				// Not grossed or not verified yet (Zero or negative)
				getSpecimens();
				if (thisCase.getNoSpecs() > 0) {
					if (base.dbPath.getGrossed(thisCase)) {
						if (thisCase.getStatusID() < LibConstants.STATUS_GROSS) {
							thisCase.setStatusID(LibConstants.STATUS_GROSS);
						}
						thisCase.setGrossTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getGrossCalendar()));
						if (base.dbPowerJ.setPendingGrossed(thisCase) > 0) {
							noUpdates++;
						}
					}
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className, "Updated " + base.numbers.formatNumber(noUpdates) + " grossed cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/** Update microtomy status of embeded cases. */
	private void getMicrotomed() {
		noUpdates = 0;
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			if (thisCase.getStatusID() < LibConstants.STATUS_MICRO || thisCase.getMicroTAT() < 1) {
				getOrders();
				if (base.dbPath.getMicrotomed(thisCase)) {
					if (thisCase.getStatusID() < LibConstants.STATUS_MICRO) {
						thisCase.setStatusID(LibConstants.STATUS_MICRO);
					}
					thisCase.setMicroTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getMicroCalendar()));
					if (base.dbPowerJ.setPendingMicrotome(thisCase) > 0) {
						noUpdates++;
					}
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className, "Updated " + base.numbers.formatNumber(noUpdates) + " microtomed cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	private void getNewCases() {
		list = base.dbPath.getNewCases(base.getLastUpdate());
		noUpdates = 0;
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			if (accessions.doworkflow(thisCase.getTypeID())
					&& facilities.doworkflow(thisCase.getFacID())) {
				getSpecimens();
				if (thisCase.getNoSpecs() > 0) {
					if (base.dbPowerJ.setPendingNew(thisCase) > 0) {
						noUpdates++;
					}
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className, "Added " + base.numbers.formatNumber(noUpdates) + " new cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/** Update Number of blocks and stained slides for a case. */
	private void getOrders() {
		thisCase.setNoSlides((short) 0);
		ArrayList<OrderData> orders = base.dbPath.getCaseOrders(thisCase.getCaseID(), thisCase.getAccessTime(), startTime);
		for (int i = 0; i < orders.size(); i++) {
			if (masterOrders.matchOrder(orders.get(i).getOrmID())) {
				switch (masterOrders.getOrderType()) {
				case LibConstants.ORDER_TYPE_SLIDE:
				case LibConstants.ORDER_TYPE_SLDFS:
				case LibConstants.ORDER_TYPE_SS:
				case LibConstants.ORDER_TYPE_IHC:
				case LibConstants.ORDER_TYPE_FISH:
					thisCase.setNoSlides(thisCase.getNoSlides() + orders.get(i).getQty());
					break;
				case LibConstants.ORDER_TYPE_BLOCK:
				case LibConstants.ORDER_TYPE_BLKFS:
				default:
					// Ignore
				}
			}
		}
		orders.clear();
	}

	/** Get all pending cases from PowerJ. */
	private void getPendingCases() {
		list.clear();
		list = base.dbPowerJ.getPendings(LibConstants.STATUS_FINAL);
		base.log(LibConstants.ERROR_NONE, className,
				"Scanning " + base.numbers.formatNumber(list.size()) + " pending cases.");
	}

	/** Update Routing status of cases. */
	private void getRouted() {
		noUpdates = 0;
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			if (thisCase.getStatusID() < LibConstants.STATUS_ROUTE || thisCase.getRouteTAT() < 1) {
				getOrders();
				if (thisCase.getNoSlides() > 0) {
					if (1.0 * base.dbPath.getRouted(thisCase) / thisCase.getNoSlides() > 0.48) {
						// At least half the slides were routed (otherwise human error)
						if (thisCase.getStatusID() < LibConstants.STATUS_ROUTE) {
							thisCase.setStatusID(LibConstants.STATUS_ROUTE);
						}
						thisCase.setRouteTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getRouteCalendar()));
						if (base.dbPowerJ.setPendingRouted(thisCase) > 0) {
							noUpdates++;
						}
					}
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className, "Updated " + base.numbers.formatNumber(noUpdates) + " routed cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/** Update Routing status of cases that are not routed (cytology) by instead using the slide scanner event. */
	private void getScanned() {
		noUpdates = 0;
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			if (thisCase.getStatusID() < LibConstants.STATUS_ROUTE || thisCase.getRouteTAT() < 1) {
				if (base.dbPath.getScanned(thisCase, pathologists)) {
					if (thisCase.getStatusID() < LibConstants.STATUS_ROUTE) {
						thisCase.setStatusID(LibConstants.STATUS_ROUTE);
					}
					thisCase.setRouted(thisCase.getScanTime() - LibConstants.ONE_HOUR);
					thisCase.setRouteTAT(base.dates.getBusinessHours(thisCase.getAccessCalendar(), thisCase.getRouteCalendar()));
					if (base.dbPowerJ.setPendingScanned(thisCase) > 0) {
						noUpdates++;
					}
				}
			}
			if (base.isStopping()) {
				break;
			} else if (i > 0 && i % 100 == 0) {
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			}
		}
		base.log(LibConstants.ERROR_NONE, className, "Updated " + base.numbers.formatNumber(noUpdates) + " scanned cases.");
		if (!base.isStopping()) {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}

	/** May be called from Grossing or Histology, re-validate specimens & specialty. */
	private void getSpecimens() {
		SpecimenData specimen = new SpecimenData();
		thisCase.clearSpecimens();
		thisCase.setMainBlocks((short) 0);
		thisCase.setMainSpec((short) 0);
		thisCase.setNoBlocks((short) 0);
		thisCase.setNoSlides((short) 0);
		thisCase.setNoSpecs(0);
		thisCase.setProcID((byte) 0);
		thisCase.setSubID((byte) 0);
		thisCase.setValue5(0);
		ArrayList<SpecimenData> specimens = base.dbPath.getCaseSpecimens(thisCase.getCaseID());
		for (int i = 0; i < specimens.size(); i++) {
			specimen = specimens.get(i);
			if (masterSpecimens.matchSpecimens(specimen.getSpmID())) {
				specimen.setNoBlocks(base.dbPath.getSpecimenBlocks(specimen.getSpecID()));
				specimen.setValue5(masterSpecimens.getValue5());
				thisCase.setSpecimen(specimen);
				thisCase.setNoBlocks(thisCase.getNoBlocks() + specimen.getNoBlocks());
				thisCase.setValue5(thisCase.getValue5() + specimen.getValue5());
				if (thisCase.getMainSpec() == 0) {
					thisCase.setProcID(masterSpecimens.getProcedureID());
					thisCase.setSubID(masterSpecimens.getSubspecialtyID());
					thisCase.setMainSpec(specimen.getSpmID());
					thisCase.setMainBlocks(specimen.getNoBlocks());
				} else if (thisCase.getMainBlocks() < specimen.getNoBlocks()) {
					thisCase.setProcID(masterSpecimens.getProcedureID());
					thisCase.setSubID(masterSpecimens.getSubspecialtyID());
					thisCase.setMainSpec(specimen.getSpmID());
					thisCase.setMainBlocks(specimen.getNoBlocks());
				} else if (thisCase.getProcID() < masterSpecimens.getProcedureID()
						&& thisCase.getMainBlocks() == specimen.getNoBlocks()) {
					thisCase.setProcID(masterSpecimens.getProcedureID());
					thisCase.setSubID(masterSpecimens.getSubspecialtyID());
					thisCase.setMainSpec(specimen.getSpmID());
				} else if (thisCase.getSubID() < masterSpecimens.getSubspecialtyID()
						&& thisCase.getProcID() == masterSpecimens.getProcedureID()
						&& thisCase.getMainBlocks() == specimen.getNoBlocks()) {
					thisCase.setSubID(masterSpecimens.getSubspecialtyID());
					thisCase.setMainSpec(specimen.getSpmID());
				}
			}
		}
		if (thisCase.getValue5() > maxFte5) {
			thisCase.setValue5(maxFte5);
		}
	}
}