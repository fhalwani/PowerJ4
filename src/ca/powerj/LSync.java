package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;

class LSync {
	private final String className = "Sync";
	private Hashtable<Byte, PreparedStatement> pjStms = null;
	private Hashtable<Byte, PreparedStatement> apStms = null;
	private LBase pj;
	private DPowerJ dbPowerJ;
	private DPowerpath dbAP;

	LSync(LBase pjcore) {
		LBase.busy.set(true);
		this.pj = pjcore;
		dbAP = pj.dbAP;
		dbPowerJ = pj.dbPowerJ;
		pj.log(LConstants.ERROR_NONE, className,
				pj.dates.formatter(LDates.FORMAT_DATETIME) + " - Sync Manager Started...");
		apStms = dbAP.prepareStatements(LConstants.ACTION_LSYNC);
		if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
			pjStms = dbPowerJ.prepareStatements(LConstants.ACTION_LSYNC);
		}
		if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
			getAccessions();
		}
		if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
			getFacilities();
		}
		if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
			getOrders();
		}
		if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
			getPersons();
		}
		if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
			getSpecimens();
		}
		close();
	}

	private void close() {
		if (dbAP != null && apStms != null) {
			dbAP.close(apStms);
		}
		if (dbPowerJ != null && pjStms != null) {
			dbPowerJ.close(pjStms);
		}
		LBase.busy.set(false);
	}

	private void getAccessions() {
		short accID = 0;
		short noInserts = 0, noAltered = 0;
		OAccession accession = new OAccession();
		HashMap<Short, OAccession> hashMap = new HashMap<Short, OAccession>();
		ResultSet rstPJ = null;
		ResultSet rstAP = null;
		try {
			rstPJ = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ACC_SELECT));
			while (rstPJ.next()) {
				accession = new OAccession();
				accession.accID = rstPJ.getShort("acid");
				accession.name = rstPJ.getString("acnm").trim();
				hashMap.put(accession.accID, accession);
			}
			rstAP = pj.dbAP.getResultSet(apStms.get(DPowerpath.STM_ACCESSIONS));
			while (rstAP.next()) {
				if (rstAP.getString("name") != null) {
					accID = rstAP.getShort("id");
					accession = hashMap.get(accID);
					if (accession == null) {
						accession = new OAccession();
						accession.accID = accID;
						accession.name = rstAP.getString("name").trim();
						if (save(true, accession)) {
							noInserts++;
						}
					} else if (!accession.name.equals(rstAP.getString("name").trim())) {
						accession.name = rstAP.getString("name").trim();
						if (save(false, accession)) {
							noAltered++;
						}
					}
				}
			}
			if (noAltered > 0 || noInserts > 0) {
				String message = String.format("Found %d new and %d modified accessions.", noInserts, noAltered);
				pj.log(LConstants.ERROR_NONE, className, message);
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			hashMap.clear();
			pj.dbAP.close(rstAP);
			pj.dbPowerJ.close(rstPJ);
		}
	}

	private void getFacilities() {
		short facID = 0;
		short noInserts = 0, noAltered = 0;
		OFacility facility = new OFacility();
		HashMap<Short, OFacility> hashMap = new HashMap<Short, OFacility>();
		ResultSet rstPJ = null;
		ResultSet rstAP = null;
		try {
			rstPJ = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FAC_SELECT));
			while (rstPJ.next()) {
				facility = new OFacility();
				facility.facID = rstPJ.getShort("faid");
				facility.name = rstPJ.getString("fanm").trim();
				facility.descr = rstPJ.getString("fadc").trim();
				hashMap.put(facility.facID, facility);
			}
			rstAP = pj.dbAP.getResultSet(apStms.get(DPowerpath.STM_FACILITIES));
			while (rstAP.next()) {
				if (rstAP.getString("code") != null && rstAP.getString("name") != null) {
					facID = rstAP.getShort("id");
					facility = hashMap.get(facID);
					if (facility == null) {
						facility = new OFacility();
						facility.facID = facID;
						facility.name = rstAP.getString("code").trim();
						facility.descr = rstAP.getString("name").trim();
						if (save(true, facility)) {
							noInserts++;
						}
					} else if (!facility.descr.equals(rstAP.getString("name").trim())) {
						facility.descr = rstAP.getString("name").trim();
						if (save(false, facility)) {
							noAltered++;
						}
					}
				}
			}
			if (noAltered > 0 || noInserts > 0) {
				String message = String.format("Found %d new and %d modified facilities.", noInserts, noAltered);
				pj.log(LConstants.ERROR_NONE, className, message);
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			hashMap.clear();
			pj.dbAP.close(rstAP);
			pj.dbPowerJ.close(rstPJ);
		}
	}

	private void getOrders() {
		short ormID = 0;
		short noInserts = 0, noAltered = 0;
		OOrderMaster ordermaster = new OOrderMaster();
		HashMap<Short, OOrderMaster> hashMap = new HashMap<Short, OOrderMaster>();
		ResultSet rstAP = null;
		ResultSet rstPJ = null;
		try {
			rstPJ = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ORM_SELECT));
			while (rstPJ.next()) {
				ordermaster = new OOrderMaster();
				ordermaster.ordID = rstPJ.getShort("omid");
				ordermaster.grpID = rstPJ.getShort("ogid");
				ordermaster.name = rstPJ.getString("omnm").trim();
				ordermaster.descr = rstPJ.getString("omdc").trim();
				hashMap.put(ordermaster.ordID, ordermaster);
			}
			rstAP = pj.dbAP.getResultSet(apStms.get(DPowerpath.STM_PROCEDURES));
			while (rstAP.next()) {
				if (rstAP.getString("code") != null && rstAP.getString("description") != null) {
					ormID = rstAP.getShort("id");
					ordermaster = hashMap.get(ormID);
					if (ordermaster == null) {
						ordermaster = new OOrderMaster();
						ordermaster.ordID = ormID;
						ordermaster.name = rstAP.getString("code").trim();
						ordermaster.descr = rstAP.getString("description").trim();
						if (save(true, ordermaster)) {
							noInserts++;
						}
					} else if (!(ordermaster.name.equals(rstAP.getString("code").trim())
							&& ordermaster.descr.equals(rstAP.getString("description").trim()))) {
						ordermaster.name = rstAP.getString("code").trim();
						ordermaster.descr = rstAP.getString("description").trim();
						if (save(false, ordermaster)) {
							noAltered++;
						}
					}
				}
			}
			if (noAltered > 0 || noInserts > 0) {
				String message = String.format("Found %d new and %d modified orders.", noInserts, noAltered);
				pj.log(LConstants.ERROR_NONE, className, message);
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			hashMap.clear();
			pj.dbAP.close(rstAP);
			pj.dbPowerJ.close(rstPJ);
		}
	}

	private void getPersons() {
		short persID = 0;
		short noInserts = 0, noAltered = 0;
		OPerson person = new OPerson();
		HashMap<Short, OPerson> hashMap = new HashMap<Short, OPerson>();
		ResultSet rstAP = null;
		ResultSet rstPJ = null;
		try {
			rstPJ = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PRS_SELECT));
			while (rstPJ.next()) {
				person = new OPerson();
				person.prsID = rstPJ.getShort("prid");
				person.access = rstPJ.getInt("prvl");
				person.code = rstPJ.getString("prcd").trim();
				person.initials = rstPJ.getString("prnm").trim();
				person.firstname = rstPJ.getString("prfr").trim();
				person.lastname = rstPJ.getString("prls").trim();
				person.active = (rstPJ.getString("prac").equalsIgnoreCase("Y"));
				person.bits = pj.numbers.intToBoolean(person.access);
				person.started.setTime(rstPJ.getDate("prdt").getTime());
				hashMap.put(person.prsID, person);
			}
			rstAP = pj.dbAP.getResultSet(apStms.get(DPowerpath.STM_PERSONNEL));
			while (rstAP.next()) {
				if (rstAP.getString("persnl_class_id") != null && rstAP.getString("first_name") != null
						&& rstAP.getString("last_name") != null) {
					persID = rstAP.getShort("id");
					person = hashMap.get(persID);
					if (person == null) {
						person = new OPerson();
						person.prsID = persID;
						person.code = rstAP.getString("persnl_class_id").trim();
						person.firstname = rstAP.getString("first_name").trim();
						person.lastname = rstAP.getString("last_name").trim();
						if (save(true, person)) {
							noInserts++;
						}
					} else if (!person.code.equals(rstAP.getString("persnl_class_id").trim())
							|| !person.firstname.equals(rstAP.getString("first_name").trim())
							|| !person.lastname.equals(rstAP.getString("last_name").trim())) {
						person.code = rstAP.getString("persnl_class_id").trim();
						person.firstname = rstAP.getString("first_name").trim();
						person.lastname = rstAP.getString("last_name").trim();
						if (save(false, person)) {
							noAltered++;
						}
					}
				}
			}
			if (noAltered > 0 || noInserts > 0) {
				String message = String.format("Found %d new and %d modified persons.", noInserts, noAltered);
				pj.log(LConstants.ERROR_NONE, className, message);
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			hashMap.clear();
			pj.dbAP.close(rstAP);
			pj.dbPowerJ.close(rstPJ);
		}
	}

	private void getSpecimens() {
		short spmID = 0;
		short noInserts = 0, noAltered = 0;
		OSpecMaster specimenmaster = new OSpecMaster();
		HashMap<Short, OSpecMaster> hashMap = new HashMap<Short, OSpecMaster>();
		ResultSet rstAP = null;
		ResultSet rstPJ = null;
		try {
			rstPJ = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SPM_SELECT));
			while (rstPJ.next()) {
				specimenmaster = new OSpecMaster();
				specimenmaster.spcID = rstPJ.getShort("smid");
				specimenmaster.grpID = rstPJ.getShort("sgid");
				specimenmaster.turID = rstPJ.getByte("taid");
				specimenmaster.name = rstPJ.getString("smnm").trim();
				specimenmaster.descr = rstPJ.getString("smdc").trim();
				hashMap.put(specimenmaster.spcID, specimenmaster);
			}
			rstAP = pj.dbAP.getResultSet(apStms.get(DPowerpath.STM_SPECIMENS));
			while (rstAP.next()) {
				if (rstAP.getString("code") != null && rstAP.getString("description") != null) {
					spmID = rstAP.getShort("id");
					specimenmaster = hashMap.get(spmID);
					if (specimenmaster == null) {
						specimenmaster = new OSpecMaster();
						specimenmaster.spcID = spmID;
						specimenmaster.name = rstAP.getString("code").trim();
						specimenmaster.descr = rstAP.getString("description").trim();
						if (save(true, specimenmaster)) {
							noInserts++;
						}
					} else if (!(specimenmaster.name.equals(rstAP.getString("code").trim())
							&& specimenmaster.descr.equals(rstAP.getString("description").trim()))) {
						specimenmaster.name = rstAP.getString("code").trim();
						specimenmaster.descr = rstAP.getString("description").trim();
						if (save(false, specimenmaster)) {
							noAltered++;
						}
					}
				}
			}
			if (noAltered > 0 || noInserts > 0) {
				String message = String.format("Found %d new and %d modified specimens.", noInserts, noAltered);
				pj.log(LConstants.ERROR_NONE, className, message);
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			hashMap.clear();
			pj.dbAP.close(rstAP);
			pj.dbPowerJ.close(rstPJ);
		}
	}

	private boolean save(boolean newRow, OAccession accession) {
		byte index = (newRow ? DPowerJ.STM_ACC_INSERT : DPowerJ.STM_ACC_UPDATE);
		if (accession.name.length() > 30) {
			accession.name = accession.name.substring(0, 30);
		}
		pj.dbPowerJ.setByte(pjStms.get(index), 1, accession.spyID);
		pj.dbPowerJ.setString(pjStms.get(index), 2, "N");
		pj.dbPowerJ.setString(pjStms.get(index), 3, "N");
		pj.dbPowerJ.setString(pjStms.get(index), 4, accession.name);
		pj.dbPowerJ.setShort(pjStms.get(index), 5, accession.accID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			return true;
		}
		return false;
	}

	private boolean save(boolean newRow, OFacility facility) {
		byte index = (newRow ? DPowerJ.STM_FAC_INSERT : DPowerJ.STM_FAC_UPDATE);
		if (facility.name.length() > 4) {
			facility.name = facility.name.substring(0, 4);
		}
		if (facility.descr.length() > 80) {
			facility.descr = facility.descr.substring(0, 80);
		}
		pj.dbPowerJ.setString(pjStms.get(index), 1, "N");
		pj.dbPowerJ.setString(pjStms.get(index), 2, "N");
		pj.dbPowerJ.setString(pjStms.get(index), 3, facility.name);
		pj.dbPowerJ.setString(pjStms.get(index), 4, facility.descr);
		pj.dbPowerJ.setShort(pjStms.get(index), 5, facility.facID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			return true;
		}
		return false;
	}

	private boolean save(boolean newRow, OOrderMaster ordermaster) {
		byte index = (newRow ? DPowerJ.STM_ORM_INSERT : DPowerJ.STM_ORM_UPDATE);
		if (ordermaster.name.length() > 15) {
			ordermaster.name = ordermaster.name.substring(0, 15);
		}
		if (ordermaster.descr.length() > 80) {
			ordermaster.descr = ordermaster.descr.substring(0, 80);
		}
		pj.dbPowerJ.setShort(pjStms.get(index), 1, ordermaster.grpID);
		pj.dbPowerJ.setString(pjStms.get(index), 2, ordermaster.name);
		pj.dbPowerJ.setString(pjStms.get(index), 3, ordermaster.descr);
		pj.dbPowerJ.setShort(pjStms.get(index), 4, ordermaster.ordID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			return true;
		}
		return false;
	}

	private boolean save(boolean newRow, OPerson person) {
		byte index = (newRow ? DPowerJ.STM_PRS_INSERT : DPowerJ.STM_PRS_UPDATE);
		if (person.firstname.length() > 30) {
			person.firstname = person.firstname.substring(0, 30);
		}
		if (person.lastname.length() > 30) {
			person.lastname = person.lastname.substring(0, 30);
		}
		if (person.initials.length() == 0 && person.firstname.length() > 0 && person.lastname.length() > 0) {
			person.initials = person.firstname.substring(0, 1).toUpperCase()
					+ person.lastname.substring(0, 1).toUpperCase();
		}
		pj.dbPowerJ.setInt(pjStms.get(index), 1, person.access);
		pj.dbPowerJ.setDate(pjStms.get(index), 2, person.started.getTime());
		pj.dbPowerJ.setString(pjStms.get(index), 3, person.code);
		pj.dbPowerJ.setString(pjStms.get(index), 4, "Y");
		pj.dbPowerJ.setString(pjStms.get(index), 5, person.initials);
		pj.dbPowerJ.setString(pjStms.get(index), 6, person.lastname);
		pj.dbPowerJ.setString(pjStms.get(index), 7, person.firstname);
		pj.dbPowerJ.setShort(pjStms.get(index), 8, person.prsID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			return true;
		}
		return false;
	}

	private boolean save(boolean newRow, OSpecMaster specimenmaster) {
		byte index = (newRow ? DPowerJ.STM_SPM_INSERT : DPowerJ.STM_SPM_UPDATE);
		pj.dbPowerJ.setShort(pjStms.get(index), 1, specimenmaster.grpID);
		pj.dbPowerJ.setShort(pjStms.get(index), 2, specimenmaster.turID);
		pj.dbPowerJ.setString(pjStms.get(index), 3, specimenmaster.name.trim());
		pj.dbPowerJ.setString(pjStms.get(index), 4, specimenmaster.descr.trim());
		pj.dbPowerJ.setShort(pjStms.get(index), 5, specimenmaster.spcID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			return true;
		}
		return false;
	}
}