package ca.powerj.database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import ca.powerj.data.AccessionData;
import ca.powerj.data.CaseData;
import ca.powerj.data.AdditionalOrderData;
import ca.powerj.data.EventData;
import ca.powerj.data.FacilityData;
import ca.powerj.data.ItemData;
import ca.powerj.data.OrderData;
import ca.powerj.data.OrderMasterList;
import ca.powerj.data.OrderMasterData;
import ca.powerj.data.PathologistList;
import ca.powerj.data.PersonData;
import ca.powerj.data.SpecimenData;
import ca.powerj.data.SpecimenMasterData;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

public class DBPowerpath extends DBPath {
	private final byte STM_ACCESSIONS = 1;
	private final byte STM_CASE_DETAILS = 3;
	private final byte STM_CASE_DIAGNOS = 4;
	private final byte STM_CASE_EMBEDED = 5;
	private final byte STM_CASE_EVENTS = 6;
	private final byte STM_CASE_GROSS = 7;
	private final byte STM_CASE_LOCKED = 8;
	private final byte STM_CASE_MICROTO = 9;
	private final byte STM_CASE_NUMBER = 10;
	private final byte STM_CASE_ORDERS = 11;
	private final byte STM_CASE_PROCESS = 12;
	private final byte STM_CASE_ROUTING = 13;
	private final byte STM_CASE_SCANNED = 14;
	private final byte STM_CASE_SPCMNS = 15;
	private final byte STM_CASE_STAINED = 16;
	private final byte STM_CASE_SYNOPTI = 17;
	private final byte STM_CASES_ACCESS = 18;
	private final byte STM_CASES_ADDEND = 19;
	private final byte STM_CASES_FINAL = 20;
	private final byte STM_CASES_REVIEW = 21;
	private final byte STM_CORRELATIONS = 22;
	private final byte STM_FACILITIES = 23;
	private final byte STM_PERS_EVENTS = 24;
	private final byte STM_PERS_ID = 25;
	private final byte STM_PERS_LOGIN = 26;
	private final byte STM_PERSONNEL = 27;
	private final byte STM_PERS_ORDERS = 28;
	private final byte STM_PERS_PROCES = 29;
	private final byte STM_PERS_UNSCAN = 30;
	private final byte STM_PROCEDURES = 31;
	private final byte STM_SPECIMENS = 32;
	private final byte STM_SPEC_BLOCKS = 33;
	private final byte STM_SPEC_ORDERS = 34;
	private final byte STM_SPEC_UPDATE = 35;
	private HashMap<Byte, PreparedStatement> pstms = new HashMap<Byte, PreparedStatement>();
	// Search strings for # of fragments in gross description
	private final String[] SEARCH_STRINGS = { "number of lesions removed", "number of excisions",
			"number of fragments", "number of pieces" };
	private Pattern pattern;
	private Matcher matcher;

	public DBPowerpath(LibBase base) {
		super(base);
		dbName = "PowerPath";
		if (!base.isOffline()) {
			setConnection();
		}
		// Initialize the text parser that finds first number in a string
		String s = "String 17 String";
		pattern = Pattern.compile(".*?(\\d+).*");
		matcher = pattern.matcher(s);
	}

	@Override
	public void closeStms() {
		close(pstms);
	}

	@Override
	public ArrayList<CaseData> getAddenda(long fromTime, long toTime) {
		ArrayList<CaseData> cases = new ArrayList<CaseData>();
		CaseData thisCase = new CaseData();
		String descr = "";
		ResultSet rst = null;
		try {
			setTime(pstms.get(STM_CASES_ADDEND), 1, fromTime);
			setTime(pstms.get(STM_CASES_ADDEND), 2, toTime);
			rst = getResultSet(pstms.get(STM_CASES_ADDEND));
			while (rst.next()) {
				if (rst.getString("description") != null) {
					descr = rst.getString("description").trim().toLowerCase();
					if (descr.equals("amendment final")
							|| descr.equals("addendum final")) {
						thisCase = new CaseData();
						thisCase.setCaseID(rst.getLong("acc_id"));
						thisCase.setFinalID(rst.getShort("assigned_to_id"));
						thisCase.setFinaled(rst.getTimestamp("completed_date").getTime());
						thisCase.setStatusName(descr);
						cases.add(thisCase);
					}
				}
			}
			if (!base.isStopping()) {
				Thread.sleep(LibConstants.SLEEP_TIME);
			}
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return cases;
	}

	@Override
	public HashMap<Short, AccessionData> getAccessions() {
		HashMap<Short, AccessionData> accessions = new HashMap<Short, AccessionData>();
		AccessionData accession = new AccessionData();
		ResultSet rst = null;
		try {
			rst = getResultSet(pstms.get(STM_ACCESSIONS));
			while (rst.next()) {
				if (rst.getString("name") != null) {
					accession = new AccessionData();
					accession.setAccID(rst.getShort("id"));
					accession.setName(rst.getString("name"));
					if (accession.getAccID() > 0 && accession.getName().length() > 0) {
						accessions.put(accession.getAccID(), accession);
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return accessions;
	}

	@Override
	public ArrayList<AdditionalOrderData> getAdditionals(long fromTime, long toTime, OrderMasterList masterOrders) {
		ArrayList<AdditionalOrderData> additionals = new ArrayList<AdditionalOrderData>();
		AdditionalOrderData additional = new AdditionalOrderData();
		ResultSet rst = null;
		try {
			setTime(pstms.get(STM_CASES_REVIEW), 1, fromTime);
			setTime(pstms.get(STM_CASES_REVIEW), 2, toTime);
			rst = getResultSet(pstms.get(STM_CASES_REVIEW));
			while (rst.next()) {
				if (masterOrders.matchOrder(rst.getShort("procedure_id"))) {
					// Order must be of category additional (OrderType in Groups)
					if (masterOrders.getOrderType() == LibConstants.ORDER_TYPE_ADD) {
						additional = new AdditionalOrderData();
						additional.setCaseID(rst.getLong("acc_id"));
						additional.setProID(rst.getShort("procedure_id"));
						additional.setFinalID(rst.getShort("ordered_by_id"));
						additional.setFinaled(rst.getTimestamp("created_date").getTime());
						additionals.add(additional);
					}
				}
			}
			if (!base.isStopping()) {
				Thread.sleep(LibConstants.SLEEP_TIME);
			}
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return additionals;
	}

	@Override
	public CaseData getCaseDetails(long caseID) {
		CaseData thisCase = new CaseData();
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_DETAILS), 1, caseID);
			rst = getResultSet(pstms.get(STM_CASE_DETAILS));
			while (rst.next()) {
				if (rst.getString("accession_no") != null) {
						thisCase.setCaseID(caseID);
						thisCase.setFacID(rst.getShort("facility_id"));
						thisCase.setTypeID(rst.getShort("acc_type_id"));
						thisCase.setFinalID(rst.getShort("assigned_to_id"));
						thisCase.setAccessed(rst.getTimestamp("created_date").getTime());
						thisCase.setFinaled(rst.getTimestamp("completed_date").getTime());
						thisCase.setCaseNo(rst.getString("accession_no"));
					}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return thisCase;
	}

	@Override
	public ArrayList<EventData> getCaseEvents(long caseID) {
		ArrayList<EventData> events = new ArrayList<EventData>();
		EventData event = new EventData();
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_EVENTS), 1, caseID);
			rst = getResultSet(pstms.get(STM_CASE_EVENTS));
			while (rst.next()) {
				if (rst.getString("source_rec_type") != null
						&& rst.getString("event_type") != null
						&& rst.getString("material_label") != null) {
					if (rst.getString("source_rec_type").trim().equals("L")
							&& rst.getString("event_type").trim().toLowerCase().equals("folder_scanned")) {
						continue;
					}
					event = new EventData();
					event.setDescription(rst.getString("event_description"));
					event.setLocation(rst.getString("event_location"));
					event.setMaterial(rst.getString("source_rec_type"), rst.getString("material_label"));
					event.setTime(rst.getTimestamp("event_date").getTime());
					events.add(event);
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return events;
	}

	@Override
	public long getCaseID(String caseNo) {
		if (caseNo != null) {
			setString(pstms.get(STM_CASE_NUMBER), 1, caseNo);
			return getLong(pstms.get(STM_CASE_NUMBER));
		}
		return 0;
	}

	@Override
	public CaseData getCaseLocked() {
		CaseData thisCase = new CaseData();
		ResultSet rst = null;
		try {
			setShort(pstms.get(STM_CASE_LOCKED), 1, base.getUserID());
			rst = getResultSet(pstms.get(STM_CASE_LOCKED));
			while (rst.next()) {
				thisCase.setCaseID(rst.getLong("acc_id"));
				thisCase.setCaseNo(rst.getString("accession_no"));
				if (rst.getString("status_final") == "N") {
					thisCase.setStatusID(LibConstants.STATUS_FINAL);
				} else {
					thisCase.setStatusID(LibConstants.STATUS_ROUTE);
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return thisCase;
	}

	@Override
	public ArrayList<OrderData> getCaseOrders(long caseID, long fromTime, long toTime) {
		ArrayList<OrderData> orders = new ArrayList<OrderData>();
		OrderData order = new OrderData();
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_ORDERS), 1, caseID);
			setTime(pstms.get(STM_CASE_ORDERS), 2, fromTime);
			setTime(pstms.get(STM_CASE_ORDERS), 3, toTime);
			rst = getResultSet(pstms.get(STM_CASE_ORDERS));
			while (rst.next()) {
				order = new OrderData();
				order.setOrmID(rst.getShort("procedure_id"));
				order.setQty(rst.getShort("quantity"));
				order.setName(rst.getString("code"));
				order.setCreatedTime(rst.getTimestamp("created_date").getTime());
				orders.add(order);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return orders;
	}

	@Override
	public ArrayList<SpecimenData> getCaseSpecimens(long caseID) {
		ArrayList<SpecimenData> specimens = new ArrayList<SpecimenData>();
		SpecimenData specimen = new SpecimenData();
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_SPCMNS), 1, caseID);
			rst = getResultSet(pstms.get(STM_CASE_SPCMNS));
			while (rst.next()) {
				if (rst.getShort("tmplt_profile_id") > 0) {
					specimen = new SpecimenData();
					specimen.setSpecID(rst.getLong("id"));
					specimen.setSpmID(rst.getShort("tmplt_profile_id"));
					specimen.setLabel(rst.getByte("specimen_label"));
					specimen.setReceived(rst.getTimestamp("recv_date").getTime());
					if (rst.getString("code") != null) {
						specimen.setName(rst.getString("code"));
					}
					if (rst.getString("description") != null) {
						specimen.setDescr(rst.getString("description"));
					}
					if (rst.getTimestamp("collection_date") != null) {
						specimen.setCollected(rst.getTimestamp("collection_date").getTime());
					} else {
						specimen.setCollected(specimen.getReceived().getTimeInMillis());
					}
					specimen.setMaster(new ItemData(specimen.getSpmID(), specimen.getName()));
					specimens.add(specimen);
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return specimens;
	}

	@Override
	public boolean getCanceled(long caseID) {
		boolean cancelled = false;
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_PROCESS), 1, caseID);
			rst = getResultSet(pstms.get(STM_CASE_PROCESS));
			while (rst.next()) {
				if (rst.getString("description") != null) {
					if (rst.getString("description").toLowerCase().contains("cancel")) {
						cancelled = true;
						break;
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return cancelled;
	}

	@Override
	public ArrayList<CaseData> getCorrelations(long fromTime, long toTime) {
		ArrayList<CaseData> correlations = new ArrayList<CaseData>();
		CaseData thisCase = new CaseData();
		ResultSet rst = null;
		try {
			setTime(pstms.get(STM_CORRELATIONS), 1, fromTime);
			setTime(pstms.get(STM_CORRELATIONS), 2, toTime);
			rst = getResultSet(pstms.get(STM_CORRELATIONS));
			while (rst.next()) {
				thisCase = new CaseData();
				thisCase.setCaseID(rst.getLong("acc_id"));
				thisCase.setFinalID(rst.getShort("correlated_by_id"));
				thisCase.setFinaled(rst.getTimestamp("correlation_date").getTime());
				correlations.add(thisCase);
			}
			if (!base.isStopping()) {
				Thread.sleep(LibConstants.SLEEP_TIME);
			}
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return correlations;
	}

	@Override
	public boolean getEmbedded(CaseData thisCase) {
		boolean embedded = false;
		short noBlocks = 0;
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_EMBEDED), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_EMBEDED));
			while (rst.next()) {
				noBlocks++;
				if (noBlocks == thisCase.getNoBlocks()) {
					thisCase.setEmbedID(rst.getShort("personnel_id"));
					thisCase.setEmbeded(rst.getTimestamp("event_date").getTime());
					embedded = true;
					break;
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return embedded;
	}

	@Override
	public HashMap<Short, FacilityData> getFacilities() {
		HashMap<Short, FacilityData> facilities = new HashMap<Short, FacilityData>();
		FacilityData facility = new FacilityData();
		ResultSet rst = null;
		try {
			rst = getResultSet(pstms.get(STM_FACILITIES));
			while (rst.next()) {
				if (rst.getString("code") != null && rst.getString("name") != null) {
					facility = new FacilityData();
					facility.setFacID(rst.getShort("id"));
					facility.setName(rst.getString("code"));
					facility.setDescr(rst.getString("name"));
					if (facility.getFacID() > 0 && facility.getName().length() > 0
							&& facility.getDescr().length() > 0) {
						facilities.put(facility.getFacID(), facility);
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return facilities;
	}

	@Override
	public ArrayList<Long> getFinaled(long fromTime, long toTime) {
		ArrayList<Long> finals = new ArrayList<Long>();
		ResultSet rst = null;
		try {
			setTime(pstms.get(STM_CASES_FINAL), 1, fromTime);
			setTime(pstms.get(STM_CASES_FINAL), 2, toTime);
			rst = getResultSet(pstms.get(STM_CASES_FINAL));
			while (rst.next()) {
				finals.add(Long.valueOf(rst.getLong("CaseID")));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return finals;
	}

	@Override
	public boolean getFinaled(CaseData thisCase) {
		boolean finaled = false;
		String descr = "";
		ResultSet rst = null;
		try {
			// QCH Cytology does not have histology nor routing
			setLong(pstms.get(STM_CASE_PROCESS), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_PROCESS));
			while (rst.next()) {
				if (rst.getTimestamp("completed_date") != null) {
					if (rst.getString("description") != null) {
						descr = rst.getString("description").trim().toLowerCase();
						if (descr.equals("final")) {
							// Cytotechs can finalize a cytology case
							thisCase.setFinaled(rst.getTimestamp("completed_date").getTime());
							thisCase.setFinalID(rst.getShort("assigned_to_id"));
							thisCase.setStatusID(LibConstants.STATUS_FINAL);
							finaled = true;
							break;
						} else if (descr.contains("microscopic") || descr.contains("pathologist")) {
							thisCase.setFinaled(rst.getTimestamp("completed_date").getTime());
							thisCase.setFinalID(rst.getShort("assigned_to_id"));
							thisCase.setStatusID(LibConstants.STATUS_DIAGN);
							finaled = true;
							// Don't break
						}
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return finaled;
	}

	@Override
	public boolean getGrossed(CaseData thisCase) {
		boolean grossed = false;
		String descr = "";
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_PROCESS), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_PROCESS));
			while (rst.next()) {
				if (rst.getTimestamp("completed_date") != null) {
					if (rst.getString("description") != null) {
						descr = rst.getString("description").trim().toLowerCase();
						if (descr.contains("gross") || descr.contains("screening")
								|| descr.contains("provisional")) {
							thisCase.setGrossed(rst.getTimestamp("completed_date").getTime());
							thisCase.setGrossID(rst.getShort("assigned_to_id"));
							grossed = true;
							break;
						}
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return grossed;
	}

	@Override
	public short getLoginID() {
		short loginID = 0;
		PreparedStatement pstm = null;
		try {
			pstm = prepareStatement(getSQL(STM_PERS_LOGIN));
			pstm.setString(1, base.getProperty("apUser"));
			loginID = getShort(pstm);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(pstm);
		}
		return loginID;
	}

	@Override
	public boolean getMicrotomed(CaseData thisCase) {
		boolean microtomed = false;
		short noBlocks = 0;
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_MICROTO), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_MICROTO));
			while (rst.next()) {
				noBlocks++;
				if (noBlocks == thisCase.getNoBlocks()) {
					thisCase.setMicroID(rst.getShort("personnel_id"));
					thisCase.setMicroed(rst.getTimestamp("event_date").getTime());
					microtomed = true;
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return microtomed;
	}

	@Override
	public ArrayList<CaseData> getNewCases(long fromTime) {
		ArrayList<CaseData> list = new ArrayList<CaseData>();
		CaseData thisCase = new CaseData();
		ResultSet rst = null;
		try {
			setTime(pstms.get(STM_CASES_ACCESS), 1, fromTime);
			rst = getResultSet(pstms.get(STM_CASES_ACCESS));
			while (rst.next()) {
				if (rst.getString("accession_no") != null
					|| rst.getShort("acc_type_id") > 0
					|| rst.getShort("facility_id") > 0) {
					// This is the tail of the table where a case is currently being added
					// So we stop & continue on the next run
					thisCase = new CaseData();
					thisCase.setCaseID(rst.getLong("id"));
					thisCase.setFacID(rst.getShort("facility_id"));
					thisCase.setTypeID(rst.getShort("acc_type_id"));
					thisCase.setCaseNo(rst.getString("accession_no"));
					thisCase.setAccessed(rst.getTimestamp("created_date").getTime());
					list.add(thisCase);
				}
			}
			if (!base.isStopping()) {
				Thread.sleep(LibConstants.SLEEP_TIME);
			}
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	@Override
	public void getNoFrags(CaseData thisCase) {
		String grossDescr = "";
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_GROSS), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_GROSS));
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
				setLong(pstms.get(STM_CASE_DIAGNOS), 1, thisCase.getCaseID());
				rst = getResultSet(pstms.get(STM_CASE_DIAGNOS));
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
				if (thisCase.getNoSpecs() > 1) {
					String[] grosses = grossDescr.split("the specimen is received in a container");
					// First element is empty
					if (grosses.length == thisCase.getNoSpecs() + 1) {
						for (short i = 0; i < thisCase.getNoSpecs(); i++) {
							SpecimenData specimen = thisCase.getSpecimen(i);
							if (specimen.getNoFrags() > 0) {
								specimen.setNoFrags(getNoFragments(grosses[i + 1]));
							}
						}
					}
				} else {
					thisCase.getSpecimen(0).setNoFrags(getNoFragments(grossDescr));
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
	}

	private short getNoFragments(String gross) {
		short noFrags = 0;
		short noLesions = 0;
		short number = 0;
		int index = 0;
		String string = "";
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

	@Override
	public byte getNoSynoptics(long caseID) {
		setLong(pstms.get(STM_CASE_SYNOPTI), 1, caseID);
		return getByte(pstms.get(STM_CASE_SYNOPTI));
	}

	@Override
	public HashMap<Short, OrderMasterData> getOrders() {
		HashMap<Short, OrderMasterData> orders = new HashMap<Short, OrderMasterData>();
		OrderMasterData order = new OrderMasterData();
		ResultSet rst = null;
		try {
			rst = getResultSet(pstms.get(STM_PROCEDURES));
			while (rst.next()) {
				if (rst.getString("code") != null && rst.getString("description") != null) {
					order = new OrderMasterData();
					order.setOrdID(rst.getShort("id"));
					order.setName(rst.getString("code"));
					order.setDescr(rst.getString("description"));
					if (order.getOrdID() > 0 && order.getName().length() > 0 && order.getDescr().length() > 0) {
						orders.put(order.getOrdID(), order);
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return orders;
	}

	@Override
	public HashMap<Short, PersonData> getPersons() {
		HashMap<Short, PersonData> persons = new HashMap<Short, PersonData>();
		PersonData person = new PersonData();
		ResultSet rst = null;
		try {
			rst = getResultSet(pstms.get(STM_PERSONNEL));
			while (rst.next()) {
				if (rst.getString("persnl_class_id") != null
						&& rst.getString("first_name") != null
						&& rst.getString("last_name") != null) {
					person = new PersonData();
					person.setPrsID(rst.getShort("id"));
					person.setCode(rst.getString("persnl_class_id"));
					person.setFirstname(rst.getString("first_name"));
					person.setLastname(rst.getString("last_name"));
					if (person.getPrsID() > 0 && person.getCode().length() > 0
							&& person.getFirstname().length() > 0
							&& person.getLastname().length() > 0) {
						persons.put(person.getPrsID(), person);
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return persons;
	}

	@Override
	public short getRouted(CaseData thisCase) {
		short noSlides = 0;
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_ROUTING), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_ROUTING));
			while (rst.next()) {
				noSlides++;
				if (1.0 * noSlides / thisCase.getNoSlides() > 0.48) {
					// At least half the slides were routed (otherwise human error)
					thisCase.setRouteID(rst.getShort("personnel_id"));
					thisCase.setRouted(rst.getTimestamp("event_date").getTime());
					break;
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return noSlides;
	}

	@Override
	public boolean getScanned(CaseData thisCase, PathologistList pathologists) {
		boolean scanned = false;
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_SCANNED), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_SCANNED));
			while (rst.next()) {
				if (pathologists.matchPathologist(rst.getShort("personnel_id"))) {
					thisCase.setScanned(rst.getTimestamp("event_date").getTime());
					thisCase.setFinalID(rst.getShort("personnel_id"));
					scanned = true;
					break;
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return scanned;
	}

	@Override
	public boolean getScanned(CaseData thisCase) {
		boolean scanned = false;
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_CASE_SCANNED), 1, thisCase.getCaseID());
			rst = getResultSet(pstms.get(STM_CASE_SCANNED));
			while (rst.next()) {
				if (thisCase.getFinalID() == rst.getShort("personnel_id")) {
					thisCase.setScanned(rst.getTimestamp("event_date").getTime());
					scanned = true;
					break;
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return scanned;
	}

	@Override
	public short getSpecimenBlocks(long specID) {
		setLong(pstms.get(STM_SPEC_BLOCKS), 1, specID);
		return getShort(pstms.get(STM_SPEC_BLOCKS));
	}

	@Override
	public ArrayList<OrderData> getSpecimenOrders(long specID, long finalTime) {
		ArrayList<OrderData> orders = new ArrayList<OrderData>();
		OrderData order = new OrderData();
		ResultSet rst = null;
		try {
			setLong(pstms.get(STM_SPEC_ORDERS), 1, specID);
			setTime(pstms.get(STM_SPEC_ORDERS), 2, finalTime);
			rst = getResultSet(pstms.get(STM_SPEC_ORDERS));
			while (rst.next()) {
				order = new OrderData();
				order.setOrmID(rst.getShort("procedure_id"));
				order.setQty(rst.getShort("quantity"));
				order.setName(rst.getString("code"));
				order.setCreatedTime(rst.getTimestamp("created_date").getTime());
				orders.add(order);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return orders;
	}

	@Override
	public HashMap<Short, SpecimenMasterData> getSpecimens() {
		HashMap<Short, SpecimenMasterData> specimens = new HashMap<Short, SpecimenMasterData>();
		SpecimenMasterData specimen = new SpecimenMasterData();
		ResultSet rst = null;
		try {
			rst = getResultSet(pstms.get(STM_SPECIMENS));
			while (rst.next()) {
				if (rst.getString("code") != null
						&& rst.getString("description") != null) {
					specimen = new SpecimenMasterData();
					specimen.setSpmID(rst.getShort("id"));
					specimen.setName(rst.getString("code"));
					specimen.setDescr(rst.getString("description"));
					if (specimen.getSpmID() > 0 && specimen.getName().length() > 0
							&& specimen.getDescr().length() > 0) {
						specimens.put(specimen.getSpmID(), specimen);
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return specimens;
	}

	private String getSQL(short id) {
		switch (id) {
		case STM_ACCESSIONS:
			return "SELECT id, name FROM acc_type ORDER BY id";
		case STM_SPEC_BLOCKS:
			return "SELECT count(*) AS Blocks FROM acc_block WITH (NOLOCK) WHERE acc_specimen_id = ?";
		case STM_CASE_DETAILS:
			return "SELECT a.accession_no, a.created_date, a.facility_id, a.acc_type_id, s.completed_date, s.assigned_to_id "
					+ "FROM accession_2 AS a WITH (NOLOCK) INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id "
					+ "INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id WHERE a.id = ? AND p.description = 'Final'";
		case STM_CASE_DIAGNOS:
			return "SELECT finding, finding_text FROM acc_results WITH (NOLOCK) WHERE acc_id = ?";
		case STM_CASE_EMBEDED:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK) "
					+ "WHERE acc_id = ? AND source_rec_type = 'B' AND amp_mode = 'embedding' AND event_type = 'material_scan' "
					+ "ORDER BY event_date";
		case STM_CASE_EVENTS:
			return "SELECT event_date, source_rec_type, material_label, event_type, event_location, event_description "
					+ "FROM acc_amp_event WITH (NOLOCK) WHERE acc_id = ? ORDER BY event_date";
		case STM_CASE_GROSS:
			return "SELECT r.finding, r.finding_text FROM acc_results AS r WITH (NOLOCK) "
					+ "INNER JOIN path_rpt_heading AS h WITH (NOLOCK) ON h.id = r.heading_id "
					+ "WHERE r.acc_id = ? AND h.name LIKE 'gross%'";
		case STM_CASE_LOCKED:
			return " SELECT l.acc_id, l.spid, l.login_time, a.status_final, a.accession_no "
					+ "FROM acc_lock AS l WITH (NOLOCK) "
					+ "INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = l.acc_id\n"
					+ "WHERE l.personnel_id = ?"
					+ "";
		case STM_CASE_MICROTO:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK) "
					+ "WHERE acc_id = ? AND source_rec_type = 'B' AND amp_mode = 'histology' AND event_type = 'material_scan' "
					+ "ORDER BY event_date";
		case STM_CASE_NUMBER:
			return "SELECT id FROM accession_2 WITH (NOLOCK) WHERE accession_no = ?";
		case STM_CASE_ORDERS:
			return "SELECT o.procedure_id, o.quantity, o.created_date, p.code FROM acc_order AS o WITH (NOLOCK) "
					+ "INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id "
					+ "WHERE o.acc_id = ? AND o.created_date < ? AND o.created_date > ? ORDER BY o.procedure_id";
		case STM_CASE_PROCESS:
			return "SELECT a.assigned_to_id, a.completed_date, s.description FROM acc_process_step AS a WITH (NOLOCK) "
					+ "INNER JOIN process_step AS s WITH (NOLOCK) ON s.id = a.step_id "
					+ "WHERE a.acc_id = ? ORDER BY s.sort_ord";
		case STM_CASE_ROUTING:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK) "
					+ "WHERE acc_id = ? AND source_rec_type = 'L' AND amp_mode = 'slide distribution' "
					+ "AND event_type = 'material_routed' ORDER BY event_date";
		case STM_CASE_SCANNED:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK) "
					+ "WHERE acc_id = ? AND source_rec_type = 'L' AND amp_mode = 'Diagnostician' "
					+ "AND event_type = 'material_scan' ORDER BY event_date";
		case STM_CASE_SPCMNS:
			// Get specimens of a case (use left outer join, or no specimens in autopsies)
			return "SELECT s.id, s.specimen_label, s.tmplt_profile_id, s.description, s.collection_date, s.recv_date, t.code, c.label_name "
					+ "FROM acc_specimen AS s WITH (NOLOCK) LEFT OUTER JOIN tmplt_profile AS t WITH (NOLOCK) ON t.id = s.tmplt_profile_id "
					+ "LEFT OUTER JOIN specimen_category AS c WITH (NOLOCK) ON c.id = s.specimen_category_id "
					+ "WHERE s.acc_id = ? ORDER BY s.specimen_label";
		case STM_CASE_STAINED:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK) WHERE acc_id = ? "
					+ "AND source_rec_type = 'L' AND amp_mode = 'slide distribution' AND event_type = 'slide_completed' "
					+ "ORDER BY event_date";
		case STM_CASE_SYNOPTI:
			return "SELECT count(*) AS Synoptics FROM acc_worksheet WITH (NOLOCK) WHERE acc_id = ?";
		case STM_CASES_ACCESS:
			return "SELECT id, acc_type_id, facility_id, created_date, accession_no FROM accession_2 WITH (NOLOCK) "
					+ "WHERE created_date > ? AND imported_case = 'N' ORDER BY created_date";
		case STM_CASES_ADDEND:
			return "SELECT a.acc_id, a.assigned_to_id, a.completed_date, s.description FROM acc_process_step AS a WITH (NOLOCK) "
					+ "INNER JOIN process_step AS s WITH (NOLOCK) ON s.id = a.step_id WHERE (a.completed_date BETWEEN ? AND ?) AND s.type = 'F' "
					+ "ORDER BY a.acc_id, a.completed_date";
		case STM_CASES_FINAL:
			return "SELECT a.id AS CaseID, a.acc_type_id, a.facility_id FROM accession_2 AS a WITH (NOLOCK) "
					+ "INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id "
					+ "INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id "
					+ "WHERE (s.completed_date BETWEEN ? AND ?) AND p.description = 'Final' ORDER BY s.completed_date";
		case STM_CASES_REVIEW:
			return "SELECT o.acc_id, o.created_date, o.procedure_id, o.quantity, o.ordered_by_id, p.code "
					+ "FROM acc_order AS o WITH (NOLOCK) INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id "
					+ "WHERE o.created_date BETWEEN ? AND ? ORDER BY o.acc_id, o.created_date";
		case STM_CORRELATIONS:
			return "SELECT c.correlation_date, c.correlated_by_id, s.acc_id FROM acc_correlation AS c WITH (NOLOCK) "
					+ "INNER JOIN acc_specimen AS s WITH (NOLOCK) ON s.id = c.correlated_specimen_id "
					+ "WHERE (c.correlation_date BETWEEN ? AND ?) AND c.correlation_status = 'C' ORDER BY s.acc_id";
		case STM_FACILITIES:
			return "SELECT id, code, name FROM facility ORDER BY id";
		case STM_PERS_EVENTS:
			return "SELECT e.event_date, e.source_rec_type, e.material_label, e.event_location, "
					+ "e.event_description, e.amp_mode, e.event_type, a.accession_no "
					+ "FROM acc_amp_event AS e WITH (NOLOCK) INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = e.acc_id "
					+ "WHERE (e.event_date BETWEEN ? AND ?) AND e.personnel_id = ? ORDER BY e.event_date";
		case STM_PERS_ID:
			return "SELECT last_name, first_name FROM personnel_2 WITH (NOLOCK) WHERE id = ?";
		case STM_PERS_LOGIN:
			return "SELECT id FROM personnel_2 WHERE login_name = ?";
		case STM_PERSONNEL:
			return "SELECT id, persnl_class_id, last_name, first_name FROM personnel_2 ORDER BY id";
		case STM_PERS_ORDERS:
			return "SELECT o.created_date, p.code, a.accession_no FROM acc_order AS o WITH (NOLOCK) "
					+ "INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id "
					+ "INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = o.acc_id "
					+ "WHERE (o.created_date BETWEEN ? AND ?) AND o.ordered_by_id = ? ORDER BY o.created_date";
		case STM_PERS_PROCES:
			return "SELECT aps.completed_date, ps.description, a.accession_no FROM acc_process_step AS aps WITH (NOLOCK) "
					+ "INNER JOIN process_step AS ps WITH (NOLOCK) ON ps.id = aps.step_id "
					+ "INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = aps.acc_id "
					+ "WHERE (aps.completed_date BETWEEN ? AND ?) AND aps.assigned_to_id = ? ORDER BY aps.completed_date";
		case STM_PERS_UNSCAN:
			return "SELECT DISTINCT a.id, a.accession_no, s.completed_date, pr.last_name, pr.first_name "
					+ "FROM accession_2 AS a WITH (NOLOCK) INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id "
					+ "INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id "
					+ "INNER JOIN personnel_2 AS pr WITH (NOLOCK) ON pr.id = s.assigned_to_id "
					+ "WHERE s.completed_date BETWEEN ? AND ? AND p.description = 'Final' "
					+ "AND a.accession_no NOT LIKE 'CN%' AND a.accession_no NOT LIKE 'NSW%' "
					+ "AND a.accession_no NOT LIKE 'OIM%' AND a.id NOT IN (SELECT DISTINCT a.id FROM accession_2 AS a WITH (NOLOCK) "
					+ "INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id INNER JOIN acc_amp_event AS e WITH (NOLOCK) ON e.acc_id = a.id "
					+ "WHERE e.event_date BETWEEN ? AND ? AND e.personnel_id = s.assigned_to_id AND e.event_type = 'material_scan') "
					+ "ORDER BY pr.last_name, s.completed_date";
		case STM_PROCEDURES:
			return "SELECT id, code, description FROM lab_procedure ORDER BY id";
		case STM_SPECIMENS:
			return "SELECT id, code, description FROM tmplt_profile WHERE type = 'S' ORDER BY id";
		case STM_SPEC_ORDERS:
			return "SELECT o.procedure_id, o.quantity, o.created_date, p.code FROM acc_order AS o WITH (NOLOCK) "
					+ "INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id "
					+ "WHERE o.acc_specimen_id = ? AND o.created_date < ? "
					+ "ORDER BY o.procedure_id";
		default:
			return "UPDATE acc_specimen SET tmplt_profile_id = ? WHERE id = ?";
		}
	}

	private void setConnection() {
		SQLServerDataSource ds = null;
		Statement stm = null;
		try {
			ds = new SQLServerDataSource();
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			ds.setPortNumber(Integer.parseInt(base.getProperty("apPort")));
			ds.setServerName(base.getProperty("apHost"));
			ds.setDatabaseName(base.getProperty("apSche"));
			ds.setUser(base.getProperty("apUser"));
			ds.setPassword(base.getProperty("apPass"));
			connection = ds.getConnection();
			execute("USE Powerpath");
			base.log(LibConstants.ERROR_NONE, dbName, "Connected to Powerpath.");
		} catch (SQLServerException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(stm);
		}
	}

	@Override
	public int setSpecimenID(short masterID, long specID) {
		setShort(pstms.get(STM_SPEC_UPDATE), 1, masterID);
		setLong(pstms.get(STM_SPEC_UPDATE), 2, specID);
		return execute(pstms.get(STM_SPEC_UPDATE));
	}

	@Override
	public void setStatements(byte id) {
		close(pstms);
		switch (id) {
		case LibConstants.ACTION_EDITOR:
			pstms.put(STM_CASE_EVENTS, prepareStatement(getSQL(STM_CASE_EVENTS)));
			pstms.put(STM_CASE_NUMBER, prepareStatement(getSQL(STM_CASE_NUMBER)));
			pstms.put(STM_CASE_SPCMNS, prepareStatement(getSQL(STM_CASE_SPCMNS)));
			pstms.put(STM_SPEC_UPDATE, prepareStatement(getSQL(STM_SPEC_UPDATE)));
			break;
		case LibConstants.ACTION_REPORT:
			pstms.put(STM_CASE_LOCKED, prepareStatement(getSQL(STM_CASE_LOCKED)));
			pstms.put(STM_CASE_SPCMNS, prepareStatement(getSQL(STM_CASE_SPCMNS)));
			break;
		case LibConstants.ACTION_LFLOW:
			pstms.put(STM_SPEC_BLOCKS, prepareStatement(getSQL(STM_SPEC_BLOCKS)));
			pstms.put(STM_CASE_EMBEDED, prepareStatement(getSQL(STM_CASE_EMBEDED)));
			pstms.put(STM_CASE_MICROTO, prepareStatement(getSQL(STM_CASE_MICROTO)));
			pstms.put(STM_CASE_ORDERS, prepareStatement(getSQL(STM_CASE_ORDERS)));
			pstms.put(STM_CASE_PROCESS, prepareStatement(getSQL(STM_CASE_PROCESS)));
			pstms.put(STM_CASE_ROUTING, prepareStatement(getSQL(STM_CASE_ROUTING)));
			pstms.put(STM_CASE_SCANNED, prepareStatement(getSQL(STM_CASE_SCANNED)));
			pstms.put(STM_CASE_SPCMNS, prepareStatement(getSQL(STM_CASE_SPCMNS)));
			pstms.put(STM_CASES_ACCESS, prepareStatement(getSQL(STM_CASES_ACCESS)));
			break;
		case LibConstants.ACTION_LLOAD:
			pstms.put(STM_SPEC_BLOCKS, prepareStatement(getSQL(STM_SPEC_BLOCKS)));
			pstms.put(STM_CASE_DETAILS, prepareStatement(getSQL(STM_CASE_DETAILS)));
			pstms.put(STM_CASE_DIAGNOS, prepareStatement(getSQL(STM_CASE_DIAGNOS)));
			pstms.put(STM_CASE_EMBEDED, prepareStatement(getSQL(STM_CASE_EMBEDED)));
			pstms.put(STM_CASE_GROSS, prepareStatement(getSQL(STM_CASE_GROSS)));
			pstms.put(STM_CASE_MICROTO, prepareStatement(getSQL(STM_CASE_MICROTO)));
			pstms.put(STM_CASE_NUMBER, prepareStatement(getSQL(STM_CASE_NUMBER)));
			pstms.put(STM_CASE_ORDERS, prepareStatement(getSQL(STM_CASE_ORDERS)));
			pstms.put(STM_CASE_PROCESS, prepareStatement(getSQL(STM_CASE_PROCESS)));
			pstms.put(STM_CASE_ROUTING, prepareStatement(getSQL(STM_CASE_ROUTING)));
			pstms.put(STM_CASE_SCANNED, prepareStatement(getSQL(STM_CASE_SCANNED)));
			pstms.put(STM_CASE_SPCMNS, prepareStatement(getSQL(STM_CASE_SPCMNS)));
			pstms.put(STM_CASE_SYNOPTI, prepareStatement(getSQL(STM_CASE_SYNOPTI)));
			pstms.put(STM_CASES_ADDEND, prepareStatement(getSQL(STM_CASES_ADDEND)));
			pstms.put(STM_CASES_FINAL, prepareStatement(getSQL(STM_CASES_FINAL)));
			pstms.put(STM_CASES_REVIEW, prepareStatement(getSQL(STM_CASES_REVIEW)));
			pstms.put(STM_CORRELATIONS, prepareStatement(getSQL(STM_CORRELATIONS)));
			pstms.put(STM_SPEC_ORDERS, prepareStatement(getSQL(STM_SPEC_ORDERS)));
			break;
		case LibConstants.ACTION_LLOGIN:
			pstms.put(STM_PERS_LOGIN, prepareStatement(getSQL(STM_PERS_LOGIN)));
			break;
		case LibConstants.ACTION_LSYNC:
			pstms.put(STM_ACCESSIONS, prepareStatement(getSQL(STM_ACCESSIONS)));
			pstms.put(STM_FACILITIES, prepareStatement(getSQL(STM_FACILITIES)));
			pstms.put(STM_PERSONNEL, prepareStatement(getSQL(STM_PERSONNEL)));
			pstms.put(STM_PROCEDURES, prepareStatement(getSQL(STM_PROCEDURES)));
			pstms.put(STM_SPECIMENS, prepareStatement(getSQL(STM_SPECIMENS)));
			break;
		default:
		}
	}
}