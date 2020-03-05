package ca.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

class DPowerJ extends DCore {
	static final byte STM_STP_SELECT = 1;
	static final byte STM_STP_SL_SID = 2;
	static final byte STM_STP_UPDATE = 3;
	static final byte STM_WDY_SL_DTE = 4;
	static final byte STM_WDY_SL_NXT = 5;
	static final byte STM_ACC_INSERT = 6;
	static final byte STM_ACC_SELECT = 7;
	static final byte STM_ACC_UPDATE = 8;
	static final byte STM_ADD_INSERT = 9;
	static final byte STM_ADD_SL_CID = 10;
	static final byte STM_ADD_SL_SUM = 11;
	static final byte STM_CD1_INSERT = 12;
	static final byte STM_CD1_SELECT = 13;
	static final byte STM_CD1_UPDATE = 14;
	static final byte STM_CD2_INSERT = 15;
	static final byte STM_CD2_SELECT = 16;
	static final byte STM_CD2_UPDATE = 17;
	static final byte STM_CD3_INSERT = 18;
	static final byte STM_CD3_SELECT = 19;
	static final byte STM_CD3_UPDATE = 20;
	static final byte STM_CD4_INSERT = 21;
	static final byte STM_CD4_SELECT = 22;
	static final byte STM_CD4_UPDATE = 23;
	static final byte STM_CMT_INSERT = 24;
	static final byte STM_CMT_SELECT = 25;
	static final byte STM_CMT_UPDATE = 26;
	static final byte STM_CSE_INSERT = 27;
	static final byte STM_CSE_SELECT = 28;
	static final byte STM_CSE_SL_CID = 29;
	static final byte STM_CSE_SL_CNO = 30;
	static final byte STM_CSE_SL_DTE = 31;
	static final byte STM_CSE_SL_LST = 32;
	static final byte STM_CSE_SL_SPE = 33;
	static final byte STM_CSE_SL_SUM = 34;
	static final byte STM_CSE_SL_TAT = 35;
	static final byte STM_CSE_SL_YER = 36;
	static final byte STM_CSE_UPDATE = 37;
	static final byte STM_ERR_DELETE = 38;
	static final byte STM_ERR_INSERT = 39;
	static final byte STM_ERR_SELECT = 40;
	static final byte STM_ERR_SL_CMT = 41;
	static final byte STM_ERR_SL_FXD = 42;
	static final byte STM_ERR_UPDATE = 43;
	static final byte STM_FAC_INSERT = 44;
	static final byte STM_FAC_SELECT = 45;
	static final byte STM_FAC_UPDATE = 46;
	static final byte STM_FRZ_INSERT = 47;
	static final byte STM_FRZ_SL_SID = 48;
	static final byte STM_FRZ_SL_SU5 = 49;
	static final byte STM_FRZ_SL_SUM = 50;
	static final byte STM_FRZ_UPDATE = 51;
	static final byte STM_ORD_INSERT = 52;
	static final byte STM_ORD_SELECT = 53;
	static final byte STM_ORD_UPDATE = 54;
	static final byte STM_ORG_INSERT = 55;
	static final byte STM_ORG_SELECT = 56;
	static final byte STM_ORG_UPDATE = 57;
	static final byte STM_ORM_INSERT = 58;
	static final byte STM_ORM_SELECT = 59;
	static final byte STM_ORM_UPDATE = 60;
	static final byte STM_ORT_INSERT = 61;
	static final byte STM_ORT_SELECT = 62;
	static final byte STM_PND_DEL_FN = 63;
	static final byte STM_PND_DEL_ID = 64;
	static final byte STM_PND_INSERT = 65;
	static final byte STM_PND_SELECT = 66;
	static final byte STM_PND_SL_LST = 67;
	static final byte STM_PND_SL_ROU = 68;
	static final byte STM_PND_UP_EMB = 69;
	static final byte STM_PND_UP_FIN = 70;
	static final byte STM_PND_UP_GRS = 71;
	static final byte STM_PND_UP_MIC = 72;
	static final byte STM_PND_UP_ROU = 73;
	static final byte STM_PRO_INSERT = 74;
	static final byte STM_PRO_SELECT = 75;
	static final byte STM_PRO_UPDATE = 76;
	static final byte STM_PRS_INSERT = 77;
	static final byte STM_PRS_SELECT = 78;
	static final byte STM_PRS_SL_PID = 79;
	static final byte STM_PRS_UPDATE = 80;
	static final byte STM_RUL_INSERT = 81;
	static final byte STM_RUL_SELECT = 82;
	static final byte STM_RUL_UPDATE = 83;
	static final byte STM_SCH_INSERT = 84;
	static final byte STM_SCH_SL_MON = 85;
	static final byte STM_SCH_SL_SRV = 86;
	static final byte STM_SCH_SL_SUM = 87;
	static final byte STM_SCH_SL_STA = 88;
	static final byte STM_SCH_UPDATE = 89;
	static final byte STM_SPE_INSERT = 90;
	static final byte STM_SPE_SELECT = 91;
	static final byte STM_SPE_UPDATE = 92;
	static final byte STM_SPG_INSERT = 93;
	static final byte STM_SPG_SELECT = 94;
	static final byte STM_SPG_SL_SUM = 95;
	static final byte STM_SPG_SL_SU5 = 96;
	static final byte STM_SPG_UPD_V5 = 97;
	static final byte STM_SPG_UPDATE = 98;
	static final byte STM_SPM_INSERT = 99;
	static final byte STM_SPM_SELECT = 100;
	static final byte STM_SPM_UPDATE = 101;
	static final byte STM_SPY_INSERT = 102;
	static final byte STM_SPY_SELECT = 103;
	static final byte STM_SPY_UPDATE = 104;
	static final byte STM_SRV_INSERT = 105;
	static final byte STM_SRV_SELECT = 106;
	static final byte STM_SRV_UPDATE = 107;
	static final byte STM_STP_INSERT = 108;
	static final byte STM_SUB_INSERT = 109;
	static final byte STM_SUB_SELECT = 110;
	static final byte STM_SUB_UPDATE = 111;
	static final byte STM_TUR_INSERT = 112;
	static final byte STM_TUR_SELECT = 113;
	static final byte STM_TUR_UPDATE = 114;
	static final byte STM_WDY_INSERT = 115;
	static final byte STM_WDY_SELECT = 116;
	static final byte STM_WDY_SL_LST = 117;
	static final byte STM_WDY_SL_PRV = 118;

	DPowerJ(LBase parent) {
		super(parent);
		dbName = "DBPowerJ";
	}

	Object[] getFacilities(boolean isFilter) {
		ResultSet rst = getResultSet(DPowerJ.STM_FAC_SELECT);
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			while (rst.next()) {
				if (rst.getString("FAFL").equalsIgnoreCase("Y")
						|| rst.getString("FALD").equalsIgnoreCase("Y")) {
					list.add(new OItem(rst.getShort("FAID"),
							rst.getString("FANM")));
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return list.toArray();
	}

	Object[] getOrderGroupArray(boolean isFilter) {
		ResultSet rst = getResultSet(DPowerJ.STM_ORG_SELECT);
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) -1, "* All *"));
		}
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("OGID"),
					rst.getString("OGNM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return list.toArray();
	}

	HashMap<Short, String> getOrderGroupMap() {
		ResultSet rst = getResultSet(DPowerJ.STM_ORG_SELECT);
		HashMap<Short, String> map = new HashMap<Short, String>();
		try {
			while (rst.next()) {
				map.put(rst.getShort("OGID"), rst.getString("OGDC"));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return map;
	}

	Object[] getOrderTypes(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((byte) 0, "* All *"));
		}
		for (byte i = 1; i < OOrderType.TYPES.length; i++) {
			list.add(new OItem(i, OOrderType.TYPES[i]));
		}
		return list.toArray();
	}

	Object[] getPersonCodes(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem(OPersonJob.ALL, OPersonJob.CODES[0]));
		}
		for (byte i = 1; i < OPersonJob.CODES.length; i++) {
			list.add(new OItem(i, OPersonJob.CODES[i]));
		}
		return list.toArray();
	}

	Object[] getProcedures(boolean isFilter) {
		ResultSet rst = getResultSet(DPowerJ.STM_PRO_SELECT);
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("POID"),
					rst.getString("PONM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return list.toArray();
	}

	ResultSet getResultSet(short index, String filters) {
		return getResultSet(setSQL(index).replaceFirst(" _WHERE_", filters));
	}

	ResultSet getResultSet(short index, long timeFrom, long timeTo, String filters) {
		Timestamp ts = new Timestamp(timeFrom);
		String sql = setSQL(index).replaceFirst("_FROM_", ts.toString());
		ts = new Timestamp(timeTo);
		sql = sql.replaceFirst("_TO_", ts.toString());
		sql = sql.replaceFirst("_AND_", filters);
		return getResultSet(sql);
	}

	Object[] getSpecialties(boolean isFilter) {
		ResultSet rst = getResultSet(DPowerJ.STM_SPY_SELECT);
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SYID"),
					rst.getString("SYNM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return list.toArray();
	}

	Object[] getSpecimenMaster(boolean isFilter) {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPM_SELECT);
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SMID"),
					rst.getString("SMNM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return list.toArray();
	}

	Object[] getSubspecialties(boolean isFilter) {
		ResultSet rst = getResultSet(DPowerJ.STM_SUB_SELECT);
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SBID"),
					rst.getString("SBNM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			closeRst(rst);
		}
		return list.toArray();
	}

	void prepareBacklog() {}
	void prepareBase() {}
	void prepareCasesSummary() {}
	void prepareDaily() {}
	void prepareEditor() {}
	void prepareError() {}
	void prepareFinals() {}
	void prepareForecast() {}
	void prepareHistology() {}
	void prepareLogin() {}
	void preparePending() {}
	void prepareRoute() {}
	void prepareSchedules(boolean canEdit) {}
	void prepareScheduleSummary() {}
	void prepareSpecimen() {}
	void prepareStpAccessions() {}
	void prepareStpCoder(byte coder) {}
	void prepareStpFacilities() {}
	void prepareStpOrdGroup() {}
	void prepareStpOrdMstr() {}
	void prepareStpPersons() {}
	void prepareStpProcedures() {}
	void prepareStpRules() {}
	void prepareStpServices() {}
	void prepareStpSpecialties() {}
	void prepareStpSpeGroup() {}
	void prepareStpSpeMstr() {}
	void prepareStpSubspecialty() {}
	void prepareStpTurnaround() {}
	void prepareSynchronizer() {}
	void prepareTurnaround() {}
	void prepareValue5() {}
	void prepareWorkflow() {}
	void prepareWorkload() {}
	void prepareWorkdays() {}

	String setSQL(short id) {
		switch (id) {
		case STM_ACC_INSERT:
			return "INSERT INTO Accessions (SYID, ACFL, ACLD, ACNM, ACID) VALUES (?, ?, ?, ?, ?)";
		case STM_ACC_UPDATE:
			return "UPDATE Accessions SET SYID = ?, ACFL = ?, ACLD = ?, ACNM = ? WHERE ACID = ?";
		case STM_ADD_INSERT:
			return "INSERT INTO Additionals (CAID, PRID, ADCD, ADDT, ADV1, ADV2, ADV3, ADV4, ADV5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD1_INSERT:
			return "INSERT INTO Coder1 (RUID, COQY, COV1, COV2, COV3, CONM, CODC, COID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD1_UPDATE:
			return "UPDATE Coder1 SET RUID = ?, COQY = ?, COV1 = ?, COV2 = ?, COV3 = ?, CONM = ?, CODC = ? WHERE COID = ?";
		case STM_CD2_INSERT:
			return "INSERT INTO Coder2 (RUID, COQY, COV1, COV2, COV3, CONM, CODC, COID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD2_UPDATE:
			return "UPDATE Coder2 SET RUID = ?, COQY = ?, COV1 = ?, COV2 = ?, COV3 = ?, CONM = ?, CODC = ? WHERE COID = ?";
		case STM_CD3_INSERT:
			return "INSERT INTO Coder3 (RUID, COQY, COV1, COV2, COV3, CONM, CODC, COID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD3_UPDATE:
			return "UPDATE Coder3 SET RUID = ?, COQY = ?, COV1 = ?, COV2 = ?, COV3 = ?, CONM = ?, CODC = ? WHERE COID = ?";
		case STM_CD4_INSERT:
			return "INSERT INTO Coder4 (RUID, COQY, COV1, COV2, COV3, CONM, CODC, COID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD4_UPDATE:
			return "UPDATE Coder4 SET RUID = ?, COQY = ?, COV1 = ?, COV2 = ?, COV3 = ?, CONM = ?, CODC = ? WHERE COID = ?";
		case STM_CMT_INSERT:
			return "INSERT INTO Comments (COM1, COM2, COM3, COM4, CAID) VALUES (?, ?, ?, ?, ?)";
		case STM_CMT_UPDATE:
			return "UPDATE Comments set COM1 = ?, COM2 = ?, COM3 = ?, COM4 = ? WHERE CAID = ?";
		case STM_CSE_INSERT:
			return "INSERT INTO Cases (FAID, SBID, SMID, GRID, EMID, MIID, ROID, FNID, GRTA, EMTA, MITA, " +
			"ROTA, FNTA, CASP, CABL, CASL, CASY, CAFS, CAHE, CASS, CAIH, CAMO, CAV5, ACED, GRED, EMED, MIED, ROED, " +
			"FNED, CAV1, CAV2, CAV3, CAV4, CANO, CAID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
			"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CSE_SL_LST:
			return "SELECT FNED FROM udvCasesLast";
		case STM_CSE_SL_TAT:
			return "SELECT * FROM udvCasesTA ORDER BY FAID, SYID, SBID, POID, FNYEAR, FNMONTH";
		case STM_CSE_UPDATE:
			return "UPDATE Cases SET FAID = ?, SBID = ?, SMID = ?, GRID = ?, EMID = ?, MIID = ?, ROID = ?, FNID = ?, " +
			"GRTA = ?, EMTA = ?, MITA = ?, ROTA = ?, FNTA = ?, CASP = ?, CABL = ?, CASL = ?, CASY = ?, CAFS = ?, CAHE = ?, CASS = ?, " +
			"CAIH = ?, CAMO = ?, CAV5 = ?, ACED = ?, GRED = ?, EMED = ?, MIED = ?, ROED = ?, FNED = ?, CAV1 = ?, CAV2 = ?, CAV3 = ?, " +
			"CAV4 = ?, CANO = ? WHERE CAID = ?";
		case STM_ERR_DELETE:
			return "DELETE FROM Errors WHERE CAID = ?";
		case STM_ERR_INSERT:
			return "INSERT INTO Errors (CAID, ERID, CANO, ERDC) VALUES (?, ?, ?, ?)";
		case STM_ERR_UPDATE:
			return "UPDATE Errors SET ERID = 0 WHERE CAID = ?";
		case STM_FAC_INSERT:
			return "INSERT INTO Facilities (FAFL, FALD, FANM, FADC, FAID) VALUES (?, ?, ?, ?, ?)";
		case STM_FAC_UPDATE:
			return "UPDATE Facilities SET FAFL = ?, FALD = ?, FANM = ?, FADC = ? WHERE FAID = ?";
		case STM_FRZ_INSERT:
			return "INSERT INTO Frozens (FRBL, FRSL, PRID, FRV5, FRV1, FRV2, FRV3, FRV4, SPID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_FRZ_UPDATE:
			return "UPDATE Frozens SET FRBL = ?, FRSL = ?, PRID = ?, FRV5 = ?, FRV1 = ?, FRV2 = ?, FRV3 = ?, FRV4 = ? WHERE SPID = ?";
		case STM_ORD_INSERT:
			return "INSERT INTO Orders (ORQY, ORV1, ORV2, ORV3, ORV4, OGID, SPID) VALUES (?, ?, ?, ?, ?, ?, ?)";
		case STM_ORD_UPDATE:
			return "UPDATE Orders SET ORQY = ?, ORV1 = ?, ORV2 = ?, ORV3 = ?, ORV4 = ? WHERE OGID = ? AND SPID = ?";
		case STM_ORG_INSERT:
			return "INSERT INTO OrderGroups (OTID, OGC1, OGC2, OGC3, OGC4, OGNM, OGDC, OGID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_ORG_UPDATE:
			return "UPDATE OrderGroups SET OTID = ?, OGC1 = ?, OGC2 = ?, OGC3 = ?, OGC4 = ?, OGNM = ?, OGDC = ? WHERE OGID = ?";
		case STM_ORM_INSERT:
			return "INSERT INTO OrderMaster (OGID, OMNM, OMDC, OMID) VALUES (?, ?, ?, ?)";
		case STM_ORM_UPDATE:
			return "UPDATE OrderMaster SET OGID = ?, OMNM = ?, OMDC = ? WHERE OMID = ?";
		case STM_ORT_INSERT:
			return "INSERT INTO OrderTypes (OTID, OTNM) VALUES (?, ?)";
		case STM_ORT_SELECT:
			return "SELECT OTID, OTNM FROM OrderTypes ORDER BY OTNM";
		case STM_PND_DEL_FN:
			return "DELETE FROM Pending WHERE PNST = 6 AND FNED < ?";
		case STM_PND_DEL_ID:
			return "DELETE FROM Pending WHERE PNID = ?";
		case STM_PND_INSERT:
			return "INSERT INTO Pending (FAID, SBID, POID, SMID, GRID, EMID, MIID, ROID, FNID, GRTA, EMTA, " +
			"MITA, ROTA, FNTA, PNST, PNSP, PNBL, PNSL, PNV5, ACED, GRED, EMED, MIED, ROED, FNED, PNNO, PNID) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_PND_SL_LST:
			return "SELECT ACED FROM udvPendingLast";
		case STM_PND_UP_EMB:
			return "UPDATE Pending SET SBID = ?, POID = ?, SMID = ?, EMID = ?, EMTA = ?, PNST = ?, PNSP = ?, " +
					"PNBL = ?, PNV5 = ?, EMED = ? WHERE PNID = ?";
		case STM_PND_UP_FIN:
			return "UPDATE Pending SET FNID = ?, FNTA = ?, PNST = ?, PNSL = ?, FNED = ? WHERE PNID = ?";
		case STM_PND_UP_GRS:
			return "UPDATE Pending SET SBID = ?, POID = ?, SMID = ?, GRID = ?, GRTA = ?, PNST = ?, PNSP = ?, " +
					"PNBL = ?, PNV5 = ?, GRED = ? WHERE PNID = ?";
		case STM_PND_UP_MIC:
			return "UPDATE Pending SET MIID = ?, MITA = ?, PNST = ?, PNBL = ?, PNSL = ?, MIED = ? WHERE PNID = ?";
		case STM_PND_UP_ROU:
			return "UPDATE Pending SET ROID = ?, ROTA = ?, PNST = ?, PNSL = ?, ROED = ? WHERE PNID = ?";
		case STM_PRO_INSERT:
			return "INSERT INTO Procedures (PONM, PODC, POID) VALUES (?, ?, ?)";
		case STM_PRO_UPDATE:
			return "UPDATE Procedures SET PONM = ?, PODC = ? WHERE POID = ?";
		case STM_PRS_INSERT:
			return "INSERT INTO Persons (PRVL, PRDT, PRCD, PRAC, PRNM, PRLS, PRFR, PRID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_PRS_UPDATE:
			return "UPDATE Persons SET PRVL = ?, PRDT = ?, PRCD = ?, PRAC = ?, PRNM = ?, PRLS = ?, PRFR = ? WHERE PRID = ?";
		case STM_RUL_INSERT:
			return "INSERT INTO Rules (RUNM, RUDC, RUID) VALUES (?, ?, ?)";
		case STM_RUL_UPDATE:
			return "UPDATE Rules SET RUNM = ?, RUDC = ? WHERE RUID = ?";
		case STM_SCH_INSERT:
			return "INSERT INTO Schedules (PRID, SRID, WDID) VALUES (?, ?, ?)";
		case STM_SCH_SL_MON:
			return "SELECT WDDT FROM udvSchedWeeks ORDER BY WDDT";
		case STM_SCH_UPDATE:
			return "UPDATE Schedules SET PRID = ? WHERE SRID = ? AND WDID = ?";
		case STM_SPE_INSERT:
			return "INSERT INTO Specimens (CAID, SMID, SPBL, SPSL, SPFR, SPHE, SPSS, SPIH, SPMO, SPV5, SPV1, SPV2, " +
			"SPV3, SPV4, SPDC, SPID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPE_UPDATE:
			return "UPDATE Specimens SET CAID = ?, SMID = ?, SPBL = ?, SPSL = ?, SPFR = ?, SPHE = ?, " +
			"SPSS = ?, SPIH = ?, SPMO = ?, SPV5 = ?, SPV1 = ?, SPV2 = ?, SPV3 = ?, SPV4 = ?, SPDC = ? WHERE SPID = ?";
		case STM_SPG_INSERT:
			return "INSERT INTO SpeciGroups (SBID, POID, SG1B, SG1M, SG1R, SG2B, SG2M, SG2R, SG3B, SG3M, SG3R, SG4B, SG4M, SG4R, " +
			"SGV5, SGLN, SGDC, SGID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPG_UPDATE:
			return "UPDATE SpeciGroups SET SBID = ?, POID = ?, SG1B = ?, SG1M = ?, SG1R = ?, SG2B = ?, SG2M = ?, SG2R = ?, " +
			"SG3B = ?, SG3M = ?, SG3R = ?, SG4B = ?, SG4M = ?, SG4R = ?, SGV5 = ?, SGLN = ?, SGDC = ? WHERE SGID = ?";
		case STM_SPG_UPD_V5:
			return "UPDATE SpeciGroups SET SGV5 = ? WHERE SGID = ?";
		case STM_SPM_INSERT:
			return "INSERT INTO SpeciMaster (SGID, TAID, SMNM, SMDC, SMID) VALUES (?, ?, ?, ?, ?)";
		case STM_SPM_UPDATE:
			return "UPDATE SpeciMaster SET SGID = ?, TAID = ?, SMNM = ?, SMDC = ? WHERE SMID = ?";
		case STM_SPY_INSERT:
			return "INSERT INTO Specialties (SYFL, SYLD, SYSP, SYNM, SYID) VALUES (?, ?, ?, ?, ?)";
		case STM_SPY_UPDATE:
			return "UPDATE Specialties SET SYFL = ?, SYLD = ?, SYSP = ?, SYNM = ? WHERE SYID = ?";
		case STM_SRV_INSERT:
			return "INSERT INTO Services (FAID, SBID, SRCD, SRNM, SRDC, SRID) VALUES (?, ?, ?, ?, ?, ?)";
		case STM_SRV_SELECT:
			return "SELECT * FROM udvServices ORDER BY SRNM";
		case STM_SRV_UPDATE:
			return "UPDATE Services SET FAID = ?, SBID = ?, SRCD = ?, SRNM = ?, SRDC = ? WHERE SRID = ?";
		case STM_STP_INSERT:
			return "INSERT INTO Setup (STVA, STID) VALUES (?, ?)";
		case STM_STP_UPDATE:
			return "UPDATE Setup SET STVA = ? WHERE STID = ?";
		case STM_SUB_INSERT:
			return "INSERT INTO Subspecial (SYID, SBNM, SBDC, SBID) VALUES (?, ?, ?, ?)";
		case STM_SUB_UPDATE:
			return "UPDATE Subspecial SET SYID = ?, SBNM = ?, SBDC = ? WHERE SBID = ?";
		case STM_TUR_INSERT:
			return "INSERT INTO Turnaround (GRSS, EMBD, MICR, ROUT, FINL, TANM, TAID) VALUES (?, ?, ?, ?, ?, ?, ?)";
		case STM_TUR_UPDATE:
			return "UPDATE Turnaround SET GRSS = ?, EMBD = ?, MICR = ?, ROUT = ?, FINL = ?, TANM = ? WHERE TAID = ?";
		case STM_WDY_INSERT:
			return "INSERT INTO Workdays (WDDT, WDTP, WDNO, WDID) VALUES (?, ?, ?, ?)";
		case STM_WDY_SL_LST:
			return "SELECT * FROM udvWorkdayLast";
		default:
			return null;
		}
	}
}