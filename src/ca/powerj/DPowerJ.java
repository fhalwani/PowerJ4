package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

class DPowerJ extends DCore {
	static final byte STM_ACC_INSERT = 1;
	static final byte STM_ACC_SELECT = 2;
	static final byte STM_ACC_UPDATE = 3;
	static final byte STM_ADD_INSERT = 4;
	static final byte STM_ADD_SL_LST = 5;
	static final byte STM_ADD_SL_CID = 6;
	static final byte STM_ADD_SL_ORD = 7;
	static final byte STM_ADD_SL_SUM = 8;
	static final byte STM_CD1_INSERT = 9;
	static final byte STM_CD1_SELECT = 10;
	static final byte STM_CD1_UPDATE = 11;
	static final byte STM_CD2_INSERT = 12;
	static final byte STM_CD2_SELECT = 13;
	static final byte STM_CD2_UPDATE = 14;
	static final byte STM_CD3_INSERT = 15;
	static final byte STM_CD3_SELECT = 16;
	static final byte STM_CD3_UPDATE = 17;
	static final byte STM_CD4_INSERT = 18;
	static final byte STM_CD4_SELECT = 19;
	static final byte STM_CD4_UPDATE = 20;
	static final byte STM_CMT_INSERT = 21;
	static final byte STM_CMT_SELECT = 22;
	static final byte STM_CMT_UPDATE = 23;
	static final byte STM_CSE_INSERT = 24;
	static final byte STM_CSE_SELECT = 25;
	static final byte STM_CSE_SL_CID = 26;
	static final byte STM_CSE_SL_CNO = 27;
	static final byte STM_CSE_SL_DTE = 28;
	static final byte STM_CSE_SL_SPE = 29;
	static final byte STM_CSE_SL_SUM = 30;
	static final byte STM_CSE_SL_TAT = 31;
	static final byte STM_CSE_SL_WLD = 32;
	static final byte STM_CSE_SL_YER = 33;
	static final byte STM_CSE_UPDATE = 34;
	static final byte STM_ERR_DELETE = 35;
	static final byte STM_ERR_INSERT = 36;
	static final byte STM_ERR_SELECT = 37;
	static final byte STM_ERR_SL_CMT = 38;
	static final byte STM_ERR_SL_FXD = 39;
	static final byte STM_ERR_UPDATE = 40;
	static final byte STM_FAC_INSERT = 41;
	static final byte STM_FAC_SELECT = 42;
	static final byte STM_FAC_UPDATE = 43;
	static final byte STM_FRZ_INSERT = 44;
	static final byte STM_FRZ_SL_SID = 45;
	static final byte STM_FRZ_SL_SU5 = 46;
	static final byte STM_FRZ_SL_SUM = 47;
	static final byte STM_FRZ_UPDATE = 48;
	static final byte STM_ORD_INSERT = 49;
	static final byte STM_ORD_SELECT = 50;
	static final byte STM_ORD_UPDATE = 51;
	static final byte STM_ORG_INSERT = 52;
	static final byte STM_ORG_SELECT = 53;
	static final byte STM_ORG_UPDATE = 54;
	static final byte STM_ORM_INSERT = 55;
	static final byte STM_ORM_SELECT = 56;
	static final byte STM_ORM_UPDATE = 57;
	static final byte STM_ORT_INSERT = 58;
	static final byte STM_ORT_SELECT = 59;
	static final byte STM_PND_DEL_FN = 60;
	static final byte STM_PND_DEL_ID = 61;
	static final byte STM_PND_INSERT = 62;
	static final byte STM_PND_SELECT = 63;
	static final byte STM_PND_SL_LST = 64;
	static final byte STM_PND_SL_ROU = 65;
	static final byte STM_PND_UP_EMB = 66;
	static final byte STM_PND_UP_FIN = 67;
	static final byte STM_PND_UP_GRS = 68;
	static final byte STM_PND_UP_MIC = 69;
	static final byte STM_PND_UP_ROU = 70;
	static final byte STM_PND_UP_SCA = 71;
	static final byte STM_PRO_INSERT = 72;
	static final byte STM_PRO_SELECT = 73;
	static final byte STM_PRO_UPDATE = 74;
	static final byte STM_PRS_INSERT = 75;
	static final byte STM_PRS_SELECT = 76;
	static final byte STM_PRS_SL_PID = 77;
	static final byte STM_PRS_UPDATE = 78;
	static final byte STM_RUL_INSERT = 79;
	static final byte STM_RUL_SELECT = 80;
	static final byte STM_RUL_UPDATE = 81;
	static final byte STM_SCH_DELETE = 82;
	static final byte STM_SCH_INSERT = 83;
	static final byte STM_SCH_SL_MON = 84;
	static final byte STM_SCH_SL_SRV = 85;
	static final byte STM_SCH_SL_STA = 86;
	static final byte STM_SCH_SL_SUM = 87;
	static final byte STM_SCH_UPDATE = 88;
	static final byte STM_SPE_INSERT = 89;
	static final byte STM_SPE_SELECT = 90;
	static final byte STM_SPE_UPDATE = 91;
	static final byte STM_SPG_INSERT = 92;
	static final byte STM_SPG_SELECT = 93;
	static final byte STM_SPG_SL_SU5 = 94;
	static final byte STM_SPG_SL_SUM = 95;
	static final byte STM_SPG_UPD_V5 = 96;
	static final byte STM_SPG_UPDATE = 97;
	static final byte STM_SPM_INSERT = 98;
	static final byte STM_SPM_SELECT = 99;
	static final byte STM_SPM_UPDATE = 100;
	static final byte STM_SPY_INSERT = 101;
	static final byte STM_SPY_SELECT = 102;
	static final byte STM_SPY_UPDATE = 103;
	static final byte STM_SRV_INSERT = 104;
	static final byte STM_SRV_SELECT = 105;
	static final byte STM_SRV_UPDATE = 106;
	static final byte STM_STP_INSERT = 107;
	static final byte STM_STP_SELECT = 108;
	static final byte STM_STP_SL_SID = 109;
	static final byte STM_STP_UPDATE = 110;
	static final byte STM_SUB_INSERT = 111;
	static final byte STM_SUB_SELECT = 112;
	static final byte STM_SUB_UPDATE = 113;
	static final byte STM_TUR_INSERT = 114;
	static final byte STM_TUR_SELECT = 115;
	static final byte STM_TUR_UPDATE = 116;
	static final byte STM_WDY_INSERT = 117;
	static final byte STM_WDY_SELECT = 118;
	static final byte STM_WDY_SL_DTE = 119;
	static final byte STM_WDY_SL_LST = 120;
	static final byte STM_WDY_SL_NXT = 121;
	static final byte STM_WDY_SL_PRV = 122;

	DPowerJ(LBase parent) {
		super(parent);
		dbName = "DBPowerJ";
	}

	Object[] getFacilities(boolean isFilter) {
		return null;
	}

	Object[] getOrderGroupArray(boolean isFilter) {
		return null;
	}

	HashMap<Short, String> getOrderGroupMap(PreparedStatement pstm) {
		ResultSet rst = getResultSet(pstm);
		HashMap<Short, String> map = new HashMap<Short, String>();
		try {
			while (rst.next()) {
				map.put(rst.getShort("OGID"), rst.getString("OGDC"));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
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
		return null;
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
		return null;
	}

	Object[] getSpecimenMaster(boolean isFilter, PreparedStatement pstm) {
		ResultSet rst = getResultSet(pstm);
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SMID"), rst.getString("SMNM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list.toArray();
	}

	Object[] getSubspecialties(boolean isFilter) {
		return null;
	}

	Hashtable<Byte, PreparedStatement> prepareStatements(byte id) {
		return null;
	}

	String setSQL(short id) {
		switch (id) {
		case STM_ACC_INSERT:
			return "INSERT INTO Accessions (SYID, ACFL, ACLD, ACNM, ACID) VALUES (?, ?, ?, ?, ?)";
		case STM_ACC_UPDATE:
			return "UPDATE Accessions SET SYID = ?, ACFL = ?, ACLD = ?, ACNM = ? WHERE ACID = ?";
		case STM_ADD_INSERT:
			return "INSERT INTO Additionals (CAID, PRID, ADCD, ADDT, ADV1, ADV2, ADV3, ADV4, ADV5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_ADD_SL_ORD:
			return "SELECT ADDT FROM udvAddLastOrder";
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
			return "INSERT INTO Cases (FAID, SBID, SMID, GRID, EMID, MIID, ROID, FNID, GRTA, EMTA, MITA, "
					+ "ROTA, FNTA, CASP, CABL, CASL, CASY, CAFS, CAHE, CASS, CAIH, CAMO, CAV5, ACED, GRED, EMED, MIED, ROED, "
					+ "FNED, CAV1, CAV2, CAV3, CAV4, CANO, CAID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CSE_SL_WLD:
			return "SELECT FNED FROM udvCasesLast";
		case STM_CSE_SL_TAT:
			return "SELECT * FROM udvCasesTA ORDER BY FAID, SYID, SBID, POID, FNYEAR, FNMONTH";
		case STM_CSE_UPDATE:
			return "UPDATE Cases SET FAID = ?, SBID = ?, SMID = ?, GRID = ?, EMID = ?, MIID = ?, ROID = ?, FNID = ?, "
					+ "GRTA = ?, EMTA = ?, MITA = ?, ROTA = ?, FNTA = ?, CASP = ?, CABL = ?, CASL = ?, CASY = ?, CAFS = ?, CAHE = ?, CASS = ?, "
					+ "CAIH = ?, CAMO = ?, CAV5 = ?, ACED = ?, GRED = ?, EMED = ?, MIED = ?, ROED = ?, FNED = ?, CAV1 = ?, CAV2 = ?, CAV3 = ?, "
					+ "CAV4 = ?, CANO = ? WHERE CAID = ?";
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
			return "INSERT INTO Pending (FAID, SBID, POID, SMID, GRID, EMID, MIID, ROID, FNID, GRTA, EMTA, "
					+ "MITA, ROTA, FNTA, PNST, PNSP, PNBL, PNSL, PNV5, ACED, GRED, EMED, MIED, ROED, FNED, PNNO, PNID) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_PND_SL_LST:
			return "SELECT ACED FROM udvPendingLast";
		case STM_PND_UP_EMB:
			return "UPDATE Pending SET SBID = ?, POID = ?, SMID = ?, EMID = ?, EMTA = ?, PNST = ?, PNSP = ?, "
					+ "PNBL = ?, PNV5 = ?, EMED = ? WHERE PNID = ?";
		case STM_PND_UP_FIN:
			return "UPDATE Pending SET FNID = ?, FNTA = ?, PNST = ?, PNBL = ?, PNSL = ?, FNED = ? WHERE PNID = ?";
		case STM_PND_UP_GRS:
			return "UPDATE Pending SET SBID = ?, POID = ?, SMID = ?, GRID = ?, GRTA = ?, PNST = ?, PNSP = ?, "
					+ "PNBL = ?, PNV5 = ?, GRED = ? WHERE PNID = ?";
		case STM_PND_UP_MIC:
			return "UPDATE Pending SET MIID = ?, MITA = ?, PNST = ?, PNBL = ?, PNSL = ?, MIED = ? WHERE PNID = ?";
		case STM_PND_UP_ROU:
			return "UPDATE Pending SET ROID = ?, ROTA = ?, PNST = ?, PNBL = ?, PNSL = ?, ROED = ? WHERE PNID = ?";
		case STM_PND_UP_SCA:
			return "UPDATE Pending SET FNID = ?, ROTA = ?, PNST = ?, PNBL = ?, PNSL = ?, ROED = ? WHERE PNID = ?";
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
		case STM_SCH_DELETE:
			return "DELETE FROM Schedules WHERE SRID = ? AND WDID = ?";
		case STM_SCH_INSERT:
			return "INSERT INTO Schedules (PRID, SRID, WDID) VALUES (?, ?, ?)";
		case STM_SCH_SL_MON:
			return "SELECT WDDT FROM udvSchedWeeks ORDER BY WDDT";
		case STM_SCH_UPDATE:
			return "UPDATE Schedules SET PRID = ? WHERE SRID = ? AND WDID = ?";
		case STM_SPE_INSERT:
			return "INSERT INTO Specimens (CAID, SMID, SPBL, SPSL, SPFR, SPHE, SPSS, SPIH, SPMO, SPV5, SPV1, SPV2, "
					+ "SPV3, SPV4, SPDC, SPID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPE_UPDATE:
			return "UPDATE Specimens SET CAID = ?, SMID = ?, SPBL = ?, SPSL = ?, SPFR = ?, SPHE = ?, "
					+ "SPSS = ?, SPIH = ?, SPMO = ?, SPV5 = ?, SPV1 = ?, SPV2 = ?, SPV3 = ?, SPV4 = ?, SPDC = ? WHERE SPID = ?";
		case STM_SPG_INSERT:
			return "INSERT INTO SpeciGroups (SBID, POID, SG1B, SG1M, SG1R, SG2B, SG2M, SG2R, SG3B, SG3M, SG3R, SG4B, SG4M, SG4R, "
					+ "SGLN, SGDC, SGID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPG_UPDATE:
			return "UPDATE SpeciGroups SET SBID = ?, POID = ?, SG1B = ?, SG1M = ?, SG1R = ?, SG2B = ?, SG2M = ?, SG2R = ?, "
					+ "SG3B = ?, SG3M = ?, SG3R = ?, SG4B = ?, SG4M = ?, SG4R = ?, SGLN = ?, SGDC = ? WHERE SGID = ?";
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