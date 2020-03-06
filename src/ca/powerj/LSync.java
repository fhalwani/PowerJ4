package ca.powerj;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

class LSync {
	private final String className = "Sync";
	private LBase pj;
	private DPowerJ dbPowerJ;
	private DPowerpath dbAP;

	LSync(LBase pjcore) {
		this.pj = pjcore;
		dbAP = pj.dbAP;
		dbPowerJ = pj.dbPowerJ;
		pj.log(LConstants.ERROR_NONE, className,
				pj.dates.formatter(LDates.FORMAT_DATETIME) + " - Sync Manager Started...");
		dbAP.prepareSynchronizer();
		if (pj.errorID == LConstants.ERROR_NONE && !pj.abort()) {
			dbPowerJ.prepareSynchronizer();
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
		dbPowerJ.closeStms(false);
		dbAP.closeStms(false);
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
			rstPJ = pj.dbPowerJ.getResultSet(DPowerJ.STM_ACC_SELECT);
			while (rstPJ.next()) {
				accession = new OAccession();
				accession.accID = rstPJ.getShort("ACID");
				accession.name = rstPJ.getString("ACNM").trim();
				hashMap.put(accession.accID, accession);
			}
			rstAP = pj.dbAP.getResultSet(DPowerpath.STM_ACCESSIONS);
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
			pj.dbPowerJ.closeRst(rstAP);
			pj.dbPowerJ.closeRst(rstPJ);
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
			rstPJ = pj.dbPowerJ.getResultSet(DPowerJ.STM_FAC_SELECT);
			while (rstPJ.next()) {
				facility = new OFacility();
				facility.facID = rstPJ.getShort("FAID");
				facility.name = rstPJ.getString("FANM").trim();
				facility.descr = rstPJ.getString("FADC").trim();
				hashMap.put(facility.facID, facility);
			}
			rstAP = pj.dbAP.getResultSet(DPowerpath.STM_FACILITIES);
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
			pj.dbPowerJ.closeRst(rstAP);
			pj.dbPowerJ.closeRst(rstPJ);
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
			rstPJ = pj.dbPowerJ.getResultSet(DPowerJ.STM_ORM_SELECT);
			while (rstPJ.next()) {
				ordermaster = new OOrderMaster();
				ordermaster.ordID = rstPJ.getShort("OMID");
				ordermaster.grpID = rstPJ.getShort("OGID");
				ordermaster.name = rstPJ.getString("OMNM").trim();
				ordermaster.descr = rstPJ.getString("OMDC").trim();
				hashMap.put(ordermaster.ordID, ordermaster);
			}
			rstAP = pj.dbAP.getResultSet(DPowerpath.STM_PROCEDURES);
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
			pj.dbPowerJ.closeRst(rstAP);
			pj.dbPowerJ.closeRst(rstPJ);
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
			rstPJ = pj.dbPowerJ.getResultSet(DPowerJ.STM_PRS_SELECT);
			while (rstPJ.next()) {
				person = new OPerson();
				person.prsID = rstPJ.getShort("PRID");
				person.access = rstPJ.getInt("PRVL");
				person.code = rstPJ.getString("PRCD").trim();
				person.initials = rstPJ.getString("PRNM").trim();
				person.firstname = rstPJ.getString("PRFR").trim();
				person.lastname = rstPJ.getString("PRLS").trim();
				person.active = (rstPJ.getString("PRAC").equalsIgnoreCase("Y"));
				person.bits = pj.numbers.intToBoolean(person.access);
				person.started.setTime(rstPJ.getDate("PRDT").getTime());
				hashMap.put(person.prsID, person);
			}
			rstAP = pj.dbAP.getResultSet(DPowerpath.STM_PERSONNEL);
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
			pj.dbPowerJ.closeRst(rstAP);
			pj.dbPowerJ.closeRst(rstPJ);
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
			rstPJ = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPM_SELECT);
			while (rstPJ.next()) {
				specimenmaster = new OSpecMaster();
				specimenmaster.spcID = rstPJ.getShort("SMID");
				specimenmaster.grpID = rstPJ.getShort("SGID");
				specimenmaster.turID = rstPJ.getByte("TAID");
				specimenmaster.name = rstPJ.getString("SMNM").trim();
				specimenmaster.descr = rstPJ.getString("SMDC").trim();
				hashMap.put(specimenmaster.spcID, specimenmaster);
			}
			rstAP = pj.dbAP.getResultSet(DPowerpath.STM_SPECIMENS);
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
			pj.dbPowerJ.closeRst(rstAP);
			pj.dbPowerJ.closeRst(rstPJ);
		}
	}

	private boolean save(boolean newRow, OAccession accession) {
		byte index = (newRow ? DPowerJ.STM_ACC_INSERT : DPowerJ.STM_ACC_UPDATE);
		if (accession.name.length() > 30) {
			accession.name = accession.name.substring(0, 30);
		}
		pj.dbPowerJ.setByte(index, 1, accession.spyID);
		pj.dbPowerJ.setString(index, 2, "N");
		pj.dbPowerJ.setString(index, 3, "N");
		pj.dbPowerJ.setString(index, 4, accession.name);
		pj.dbPowerJ.setShort(index, 5, accession.accID);
		if (pj.dbPowerJ.execute(index) > 0) {
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
		pj.dbPowerJ.setString(index, 1, "N");
		pj.dbPowerJ.setString(index, 2, "N");
		pj.dbPowerJ.setString(index, 3, facility.name);
		pj.dbPowerJ.setString(index, 4, facility.descr);
		pj.dbPowerJ.setShort(index, 5, facility.facID);
		if (pj.dbPowerJ.execute(index) > 0) {
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
		pj.dbPowerJ.setShort(index, 1, ordermaster.grpID);
		pj.dbPowerJ.setString(index, 2, ordermaster.name);
		pj.dbPowerJ.setString(index, 3, ordermaster.descr);
		pj.dbPowerJ.setShort(index, 4, ordermaster.ordID);
		if (pj.dbPowerJ.execute(index) > 0) {
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
		pj.dbPowerJ.setInt(index, 1, person.access);
		pj.dbPowerJ.setDate(index, 2, person.started.getTime());
		pj.dbPowerJ.setString(index, 3, person.code);
		pj.dbPowerJ.setString(index, 4, "Y");
		pj.dbPowerJ.setString(index, 5, person.initials);
		pj.dbPowerJ.setString(index, 6, person.lastname);
		pj.dbPowerJ.setString(index, 7, person.firstname);
		pj.dbPowerJ.setShort(index, 8, person.prsID);
		if (pj.dbPowerJ.execute(index) > 0) {
			return true;
		}
		return false;
	}

	private boolean save(boolean newRow, OSpecMaster specimenmaster) {
		byte index = (newRow ? DPowerJ.STM_SPM_INSERT : DPowerJ.STM_SPM_UPDATE);
		pj.dbPowerJ.setShort(index, 1, specimenmaster.grpID);
		pj.dbPowerJ.setShort(index, 2, specimenmaster.turID);
		pj.dbPowerJ.setString(index, 3, specimenmaster.name.trim());
		pj.dbPowerJ.setString(index, 4, specimenmaster.descr.trim());
		pj.dbPowerJ.setShort(index, 5, specimenmaster.spcID);
		if (pj.dbPowerJ.execute(index) > 0) {
			return true;
		}
		return false;
	}
}