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
	static final byte STM_ADD_SL_SPG = 8;
	static final byte STM_ADD_SL_SUM = 9;
	static final byte STM_ADD_SL_YER = 10;
	static final byte STM_CD1_INSERT = 11;
	static final byte STM_CD1_SELECT = 12;
	static final byte STM_CD1_UPDATE = 13;
	static final byte STM_CD2_INSERT = 14;
	static final byte STM_CD2_SELECT = 15;
	static final byte STM_CD2_UPDATE = 16;
	static final byte STM_CD3_INSERT = 17;
	static final byte STM_CD3_SELECT = 18;
	static final byte STM_CD3_UPDATE = 19;
	static final byte STM_CD4_INSERT = 20;
	static final byte STM_CD4_SELECT = 21;
	static final byte STM_CD4_UPDATE = 22;
	static final byte STM_CMT_INSERT = 23;
	static final byte STM_CMT_SELECT = 24;
	static final byte STM_CMT_UPDATE = 25;
	static final byte STM_CSE_INSERT = 26;
	static final byte STM_CSE_SELECT = 27;
	static final byte STM_CSE_SL_CID = 28;
	static final byte STM_CSE_SL_CNO = 29;
	static final byte STM_CSE_SL_DTE = 30;
	static final byte STM_CSE_SL_SPE = 31;
	static final byte STM_CSE_SL_SUM = 32;
	static final byte STM_CSE_SL_TAT = 33;
	static final byte STM_CSE_SL_WLD = 34;
	static final byte STM_CSE_UPDATE = 35;
	static final byte STM_ERR_DELETE = 36;
	static final byte STM_ERR_INSERT = 37;
	static final byte STM_ERR_SELECT = 38;
	static final byte STM_ERR_SL_CMT = 39;
	static final byte STM_ERR_SL_FXD = 40;
	static final byte STM_ERR_UPDATE = 41;
	static final byte STM_FAC_INSERT = 42;
	static final byte STM_FAC_SELECT = 43;
	static final byte STM_FAC_UPDATE = 44;
	static final byte STM_FRZ_INSERT = 45;
	static final byte STM_FRZ_SL_SID = 46;
	static final byte STM_FRZ_SL_SPG = 47;
	static final byte STM_FRZ_SL_SU5 = 48;
	static final byte STM_FRZ_SL_SUM = 49;
	static final byte STM_FRZ_SL_YER = 50;
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
	static final byte STM_PND_UP_SCA = 74;
	static final byte STM_PRO_INSERT = 75;
	static final byte STM_PRO_SELECT = 76;
	static final byte STM_PRO_UPDATE = 77;
	static final byte STM_PRS_INSERT = 78;
	static final byte STM_PRS_SELECT = 79;
	static final byte STM_PRS_SL_PID = 80;
	static final byte STM_PRS_UPDATE = 81;
	static final byte STM_RUL_INSERT = 82;
	static final byte STM_RUL_SELECT = 83;
	static final byte STM_RUL_UPDATE = 84;
	static final byte STM_SCH_DELETE = 85;
	static final byte STM_SCH_INSERT = 86;
	static final byte STM_SCH_SL_MON = 87;
	static final byte STM_SCH_SL_SRV = 88;
	static final byte STM_SCH_SL_STA = 89;
	static final byte STM_SCH_SL_SUM = 90;
	static final byte STM_SCH_UPDATE = 91;
	static final byte STM_SPE_INSERT = 92;
	static final byte STM_SPE_SELECT = 93;
	static final byte STM_SPE_UPDATE = 94;
	static final byte STM_SPG_INSERT = 95;
	static final byte STM_SPG_SELECT = 96;
	static final byte STM_SPG_SL_SU5 = 97;
	static final byte STM_SPG_SL_SUM = 98;
	static final byte STM_SPG_SL_YER = 99;
	static final byte STM_SPG_UPD_V5 = 100;
	static final byte STM_SPG_UPDATE = 101;
	static final byte STM_SPM_INSERT = 102;
	static final byte STM_SPM_SELECT = 103;
	static final byte STM_SPM_UPDATE = 104;
	static final byte STM_SPY_INSERT = 105;
	static final byte STM_SPY_SELECT = 106;
	static final byte STM_SPY_UPDATE = 107;
	static final byte STM_SRV_INSERT = 108;
	static final byte STM_SRV_SELECT = 109;
	static final byte STM_SRV_UPDATE = 110;
	static final byte STM_STP_INSERT = 111;
	static final byte STM_STP_SELECT = 112;
	static final byte STM_STP_SL_SID = 113;
	static final byte STM_STP_UPDATE = 114;
	static final byte STM_SUB_INSERT = 115;
	static final byte STM_SUB_SELECT = 116;
	static final byte STM_SUB_UPDATE = 117;
	static final byte STM_TUR_INSERT = 118;
	static final byte STM_TUR_SELECT = 119;
	static final byte STM_TUR_UPDATE = 120;
	static final byte STM_WDY_INSERT = 121;
	static final byte STM_WDY_SELECT = 122;
	static final byte STM_WDY_SL_DTE = 123;
	static final byte STM_WDY_SL_LST = 124;
	static final byte STM_WDY_SL_NXT = 125;
	static final byte STM_WDY_SL_PRV = 126;

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
				map.put(rst.getShort("ogid"), rst.getString("ogdc"));
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
				list.add(new OItem(rst.getShort("smid"), rst.getString("smnm")));
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
			return "INSERT INTO " + pj.pjSchema + ".accessions (syid, acfl, acld, acnm, acid) VALUES (?, ?, ?, ?, ?)";
		case STM_ACC_UPDATE:
			return "UPDATE " + pj.pjSchema + ".accessions SET syid = ?, acfl = ?, acld = ?, acnm = ? WHERE acid = ?";
		case STM_ADD_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".additionals (caid, prid, adcd, addt, adv1, adv2, adv3, adv4, adv5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_ADD_SL_ORD:
			return "SELECT addt FROM " + pj.pjSchema + ".udvaddlastorder";
		case STM_CD1_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".coder1 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD1_UPDATE:
			return "UPDATE " + pj.pjSchema + ".coder1 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CD2_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".coder2 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD2_UPDATE:
			return "UPDATE " + pj.pjSchema + ".coder2 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CD3_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".coder3 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD3_UPDATE:
			return "UPDATE " + pj.pjSchema + ".coder3 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CD4_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".coder4 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD4_UPDATE:
			return "UPDATE " + pj.pjSchema + ".coder4 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CMT_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".comments (com1, com2, com3, com4, caid) VALUES (?, ?, ?, ?, ?)";
		case STM_CMT_UPDATE:
			return "UPDATE " + pj.pjSchema + ".comments set com1 = ?, com2 = ?, com3 = ?, com4 = ? WHERE caid = ?";
		case STM_CSE_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".cases (faid, sbid, smid, grid, emid, miid, roid, fnid, grta, emta, mita, "
					+ "rota, fnta, casp, cabl, casl, casy, cafs, cahe, cass, caih, camo, cav5, aced, gred, emed, mied, roed, "
					+ "fned, cav1, cav2, cav3, cav4, cano, caid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CSE_SL_WLD:
			return "SELECT fned FROM " + pj.pjSchema + ".udvcaseslast";
		case STM_CSE_SL_TAT:
			return "SELECT * FROM " + pj.pjSchema + ".udvcasesta ORDER BY faid, syid, sbid, poid, fnyear, fnmonth";
		case STM_CSE_UPDATE:
			return "UPDATE " + pj.pjSchema + ".cases SET faid = ?, sbid = ?, smid = ?, grid = ?, emid = ?, miid = ?, roid = ?, fnid = ?, "
					+ "grta = ?, emta = ?, mita = ?, rota = ?, fnta = ?, casp = ?, cabl = ?, casl = ?, casy = ?, cafs = ?, cahe = ?, cass = ?, "
					+ "caih = ?, camo = ?, cav5 = ?, aced = ?, gred = ?, emed = ?, mied = ?, roed = ?, fned = ?, cav1 = ?, cav2 = ?, cav3 = ?, "
					+ "cav4 = ?, cano = ? WHERE caid = ?";
		case STM_ERR_DELETE:
			return "DELETE FROM " + pj.pjSchema + ".errors WHERE caid = ?";
		case STM_ERR_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".errors (caid, erid, cano, erdc) VALUES (?, ?, ?, ?)";
		case STM_ERR_UPDATE:
			return "UPDATE " + pj.pjSchema + ".errors SET erid = 0 WHERE caid = ?";
		case STM_FAC_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".facilities (fafl, fald, fanm, fadc, faid) VALUES (?, ?, ?, ?, ?)";
		case STM_FAC_UPDATE:
			return "UPDATE " + pj.pjSchema + ".facilities SET fafl = ?, fald = ?, fanm = ?, fadc = ? WHERE faid = ?";
		case STM_FRZ_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".frozens (frbl, frsl, prid, frv5, frv1, frv2, frv3, frv4, spid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_FRZ_UPDATE:
			return "UPDATE " + pj.pjSchema + ".frozens SET frbl = ?, frsl = ?, prid = ?, frv5 = ?, frv1 = ?, frv2 = ?, frv3 = ?, frv4 = ? WHERE spid = ?";
		case STM_ORD_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".orders (orqy, orv1, orv2, orv3, orv4, ogid, spid) VALUES (?, ?, ?, ?, ?, ?, ?)";
		case STM_ORD_UPDATE:
			return "UPDATE " + pj.pjSchema + ".orders SET orqy = ?, orv1 = ?, orv2 = ?, orv3 = ?, orv4 = ? WHERE ogid = ? AND spid = ?";
		case STM_ORG_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".ordergroups (otid, ogc1, ogc2, ogc3, ogc4, ogc5, ognm, ogdc, ogid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_ORG_UPDATE:
			return "UPDATE " + pj.pjSchema + ".ordergroups SET otid = ?, ogc1 = ?, ogc2 = ?, ogc3 = ?, ogc4 = ?, ogc5 = ?, ognm = ?, ogdc = ? WHERE ogid = ?";
		case STM_ORM_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".ordermaster (ogid, omnm, omdc, omid) VALUES (?, ?, ?, ?)";
		case STM_ORM_UPDATE:
			return "UPDATE " + pj.pjSchema + ".ordermaster SET ogid = ?, omnm = ?, omdc = ? WHERE omid = ?";
		case STM_ORT_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".ordertypes (otid, otnm) VALUES (?, ?)";
		case STM_ORT_SELECT:
			return "SELECT otid, otnm FROM " + pj.pjSchema + ".ordertypes ORDER BY otnm";
		case STM_PND_DEL_FN:
			return "DELETE FROM " + pj.pjSchema + ".pending WHERE pnst = 6 AND fned < ?";
		case STM_PND_DEL_ID:
			return "DELETE FROM " + pj.pjSchema + ".pending WHERE pnid = ?";
		case STM_PND_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".pending (faid, sbid, poid, smid, grid, emid, miid, roid, fnid, grta, emta, "
					+ "mita, rota, fnta, pnst, pnsp, pnbl, pnsl, pnv5, aced, gred, emed, mied, roed, fned, pnno, pnid) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_PND_SL_LST:
			return "SELECT aced FROM " + pj.pjSchema + ".udvpendinglast";
		case STM_PND_UP_EMB:
			return "UPDATE " + pj.pjSchema + ".pending SET sbid = ?, poid = ?, smid = ?, emid = ?, emta = ?, pnst = ?, pnsp = ?, "
					+ "pnbl = ?, pnv5 = ?, emed = ? WHERE pnid = ?";
		case STM_PND_UP_FIN:
			return "UPDATE " + pj.pjSchema + ".pending SET fnid = ?, fnta = ?, pnst = ?, pnbl = ?, pnsl = ?, fned = ? WHERE pnid = ?";
		case STM_PND_UP_GRS:
			return "UPDATE " + pj.pjSchema + ".pending SET sbid = ?, poid = ?, smid = ?, grid = ?, grta = ?, pnst = ?, pnsp = ?, "
					+ "pnbl = ?, pnv5 = ?, gred = ? WHERE pnid = ?";
		case STM_PND_UP_MIC:
			return "UPDATE " + pj.pjSchema + ".pending SET miid = ?, mita = ?, pnst = ?, pnbl = ?, pnsl = ?, mied = ? WHERE pnid = ?";
		case STM_PND_UP_ROU:
			return "UPDATE " + pj.pjSchema + ".pending SET roid = ?, rota = ?, pnst = ?, pnbl = ?, pnsl = ?, roed = ? WHERE pnid = ?";
		case STM_PND_UP_SCA:
			return "UPDATE " + pj.pjSchema + ".pending SET fnid = ?, rota = ?, pnst = ?, pnbl = ?, pnsl = ?, roed = ? WHERE pnid = ?";
		case STM_PRO_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".procedures (ponm, podc, poid) VALUES (?, ?, ?)";
		case STM_PRO_UPDATE:
			return "UPDATE " + pj.pjSchema + ".procedures SET ponm = ?, podc = ? WHERE poid = ?";
		case STM_PRS_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".persons (prvl, prdt, prcd, prac, prnm, prls, prfr, prid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_PRS_UPDATE:
			return "UPDATE " + pj.pjSchema + ".persons SET prvl = ?, prdt = ?, prcd = ?, prac = ?, prnm = ?, prls = ?, prfr = ? WHERE prid = ?";
		case STM_RUL_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".rules (runm, rudc, ruid) VALUES (?, ?, ?)";
		case STM_RUL_UPDATE:
			return "UPDATE " + pj.pjSchema + ".rules SET runm = ?, rudc = ? WHERE ruid = ?";
		case STM_SCH_DELETE:
			return "DELETE FROM " + pj.pjSchema + ".schedules WHERE srid = ? AND wdid = ?";
		case STM_SCH_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".schedules (prid, srid, wdid) VALUES (?, ?, ?)";
		case STM_SCH_SL_MON:
			return "SELECT wddt FROM " + pj.pjSchema + ".udvschedweeks ORDER BY wddt";
		case STM_SCH_UPDATE:
			return "UPDATE " + pj.pjSchema + ".schedules SET prid = ? WHERE srid = ? AND wdid = ?";
		case STM_SPE_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".specimens (caid, smid, spbl, spsl, spfr, sphe, spss, spih, spmo, spv5, spv1, spv2, "
					+ "spv3, spv4, spdc, spid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPE_UPDATE:
			return "UPDATE " + pj.pjSchema + ".specimens SET caid = ?, smid = ?, spbl = ?, spsl = ?, spfr = ?, sphe = ?, "
					+ "spss = ?, spih = ?, spmo = ?, spv5 = ?, spv1 = ?, spv2 = ?, spv3 = ?, spv4 = ?, spdc = ? WHERE spid = ?";
		case STM_SPG_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".specigroups (sbid, poid, sg1b, sg1m, sg1r, sg2b, sg2m, sg2r, sg3b, sg3m, sg3r, sg4b, sg4m, sg4r, "
					+ "sgv5, sgln, sgdc, sgid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPG_UPDATE:
			return "UPDATE " + pj.pjSchema + ".specigroups SET sbid = ?, poid = ?, sg1b = ?, sg1m = ?, sg1r = ?, sg2b = ?, sg2m = ?, sg2r = ?, "
					+ "sg3b = ?, sg3m = ?, sg3r = ?, sg4b = ?, sg4m = ?, sg4r = ?, sgv5 = ?, sgln = ?, sgdc = ? WHERE sgid = ?";
		case STM_SPG_UPD_V5:
			return "UPDATE " + pj.pjSchema + ".specigroups SET sgv5 = ? WHERE sgid = ?";
		case STM_SPM_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".specimaster (sgid, taid, smnm, smdc, smid) VALUES (?, ?, ?, ?, ?)";
		case STM_SPM_UPDATE:
			return "UPDATE " + pj.pjSchema + ".specimaster SET sgid = ?, taid = ?, smnm = ?, smdc = ? WHERE smid = ?";
		case STM_SPY_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".specialties (syfl, syld, sysp, synm, syid) VALUES (?, ?, ?, ?, ?)";
		case STM_SPY_UPDATE:
			return "UPDATE " + pj.pjSchema + ".specialties SET syfl = ?, syld = ?, sysp = ?, synm = ? WHERE syid = ?";
		case STM_SRV_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".services (faid, sbid, srcd, srnm, srdc, srid) VALUES (?, ?, ?, ?, ?, ?)";
		case STM_SRV_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvservices ORDER BY srnm";
		case STM_SRV_UPDATE:
			return "UPDATE " + pj.pjSchema + ".services SET faid = ?, sbid = ?, srcd = ?, srnm = ?, srdc = ? WHERE srid = ?";
		case STM_STP_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".setup (stva, stid) VALUES (?, ?)";
		case STM_STP_UPDATE:
			return "UPDATE " + pj.pjSchema + ".setup SET stva = ? WHERE stid = ?";
		case STM_SUB_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".subspecial (syid, sbnm, sbdc, sbid) VALUES (?, ?, ?, ?)";
		case STM_SUB_UPDATE:
			return "UPDATE " + pj.pjSchema + ".subspecial SET syid = ?, sbnm = ?, sbdc = ? WHERE sbid = ?";
		case STM_TUR_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".turnaround (grss, embd, micr, rout, finl, tanm, taid) VALUES (?, ?, ?, ?, ?, ?, ?)";
		case STM_TUR_UPDATE:
			return "UPDATE " + pj.pjSchema + ".turnaround SET grss = ?, embd = ?, micr = ?, rout = ?, finl = ?, tanm = ? WHERE taid = ?";
		case STM_WDY_INSERT:
			return "INSERT INTO " + pj.pjSchema + ".workdays (wddt, wdtp, wdno, wdid) VALUES (?, ?, ?, ?)";
		case STM_WDY_SL_LST:
			return "SELECT * FROM " + pj.pjSchema + ".udvworkdaylast";
		default:
			return null;
		}
	}
}