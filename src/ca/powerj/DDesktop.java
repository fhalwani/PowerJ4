package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

class DDesktop extends DPowerJ {

	DDesktop(LBase parent) {
		super(parent);
		dbName = "DBDesktop";
	}

	@Override
	Object[] getFacilities(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			PreparedStatement pstm = prepareStatement(setSQL(STM_FAC_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				if (rst.getString("fafl").equalsIgnoreCase("Y") || rst.getString("fald").equalsIgnoreCase("Y")) {
					list.add(new OItem(rst.getShort("faid"), rst.getString("fanm")));
				}
			}
			close(rst);
			close(pstm);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return list.toArray();
	}

	@Override
	Object[] getOrderGroupArray(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) -1, "* All *"));
		}
		try {
			PreparedStatement pstm = prepareStatement(setSQL(STM_ORG_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("ogid"), rst.getString("ognm")));
			}
			close(rst);
			close(pstm);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return list.toArray();
	}

	@Override
	Object[] getProcedures(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			PreparedStatement pstm = prepareStatement(setSQL(STM_PRO_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("poid"), rst.getString("ponm")));
			}
			close(rst);
			close(pstm);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return list.toArray();
	}

	@Override
	Object[] getSpecialties(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			PreparedStatement pstm = prepareStatement(setSQL(STM_SPY_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("syid"), rst.getString("synm")));
			}
			close(rst);
			close(pstm);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return list.toArray();
	}

	@Override
	Object[] getSubspecialties(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			PreparedStatement pstm = prepareStatement(setSQL(STM_SUB_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("sbid"), rst.getString("sbnm")));
			}
			close(rst);
			close(pstm);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
		return list.toArray();
	}

	@Override
	Hashtable<Byte, PreparedStatement> prepareStatements(byte id) {
		Hashtable<Byte, PreparedStatement> pstms = new Hashtable<Byte, PreparedStatement>();
		switch (id) {
		case LConstants.ACTION_ACCESSION:
			pstms.put(STM_ACC_SELECT, prepareStatement(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_ACC_UPDATE, prepareStatement(setSQL(STM_ACC_UPDATE)));
			break;
		case LConstants.ACTION_BACKLOG:
			pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
			pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_CODER1:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD1_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD1_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareStatement(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_CODER2:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD2_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD2_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareStatement(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_CODER3:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD3_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD3_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareStatement(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_CODER4:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD4_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD4_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareStatement(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_DAILY:
			pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
			pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_DISTRIBUTE:
			pstms.put(STM_ADD_SL_SUM, prepareStatement(setSQL(STM_ADD_SL_SUM)));
			pstms.put(STM_CSE_SL_SUM, prepareStatement(setSQL(STM_CSE_SL_SUM)));
			pstms.put(STM_FRZ_SL_SUM, prepareStatement(setSQL(STM_FRZ_SL_SUM)));
			break;
		case LConstants.ACTION_EDITOR:
			pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_ERROR:
			pstms.put(STM_ERR_SELECT, prepareStatement(setSQL(STM_ERR_SELECT)));
			pstms.put(STM_ERR_SL_CMT, prepareStatement(setSQL(STM_ERR_SL_CMT)));
			pstms.put(STM_ERR_UPDATE, prepareStatement(setSQL(STM_ERR_UPDATE)));
			pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_FACILITY:
			pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_FAC_UPDATE, prepareStatement(setSQL(STM_FAC_UPDATE)));
			break;
		case LConstants.ACTION_FINALS:
			pstms.put(STM_ADD_SL_CID, prepareStatement(setSQL(STM_ADD_SL_CID)));
			pstms.put(STM_CMT_SELECT, prepareStatement(setSQL(STM_CMT_SELECT)));
			pstms.put(STM_FRZ_SL_SID, prepareStatement(setSQL(STM_FRZ_SL_SID)));
			pstms.put(STM_ORD_SELECT, prepareStatement(setSQL(STM_ORD_SELECT)));
			pstms.put(STM_SPE_SELECT, prepareStatement(setSQL(STM_SPE_SELECT)));
			break;
		case LConstants.ACTION_FORECAST:
			pstms.put(STM_CSE_SL_YER, prepareStatement(setSQL(STM_CSE_SL_YER)));
			break;
		case LConstants.ACTION_HISTOLOGY:
			pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
			break;
		case LConstants.ACTION_ORDERGROUP:
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD2_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD3_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD4_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_ORG_INSERT, prepareStatement(setSQL(STM_ORG_INSERT)));
			pstms.put(STM_ORG_SELECT, prepareStatement(setSQL(STM_ORG_SELECT)));
			pstms.put(STM_ORG_UPDATE, prepareStatement(setSQL(STM_ORG_UPDATE)));
			pstms.put(STM_ORT_SELECT, prepareStatement(setSQL(STM_ORT_SELECT)));
			break;
		case LConstants.ACTION_ORDERMASTER:
			pstms.put(STM_ORG_SELECT, prepareStatement(setSQL(STM_ORG_SELECT)));
			pstms.put(STM_ORM_SELECT, prepareStatement(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_ORM_UPDATE, prepareStatement(setSQL(STM_ORM_UPDATE)));
			break;
		case LConstants.ACTION_PENDING:
			pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
			pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_PERSONNEL:
			pstms.put(STM_PRS_SELECT, prepareStatement(setSQL(STM_PRS_SELECT)));
			pstms.put(STM_PRS_UPDATE, prepareStatement(setSQL(STM_PRS_UPDATE)));
			break;
		case LConstants.ACTION_PROCEDURES:
			pstms.put(STM_PRO_INSERT, prepareStatement(setSQL(STM_PRO_INSERT)));
			pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
			pstms.put(STM_PRO_UPDATE, prepareStatement(setSQL(STM_PRO_UPDATE)));
			break;
		case LConstants.ACTION_ROUTING:
			pstms.put(STM_PND_SL_ROU, prepareStatement(setSQL(STM_PND_SL_ROU)));
			break;
		case LConstants.ACTION_RULES:
			pstms.put(STM_RUL_INSERT, prepareStatement(setSQL(STM_RUL_INSERT)));
			pstms.put(STM_RUL_SELECT, prepareStatement(setSQL(STM_RUL_SELECT)));
			pstms.put(STM_RUL_UPDATE, prepareStatement(setSQL(STM_RUL_UPDATE)));
			break;
		case LConstants.ACTION_SCHEDULE:
			pstms.put(STM_PRS_SELECT, prepareStatement(setSQL(STM_PRS_SELECT)));
			pstms.put(STM_SCH_DELETE, prepareStatement(setSQL(STM_SCH_DELETE)));
			pstms.put(STM_SCH_INSERT, prepareStatement(setSQL(STM_SCH_INSERT)));
			pstms.put(STM_SCH_SL_MON, prepareStatement(setSQL(STM_SCH_SL_MON)));
			pstms.put(STM_SCH_SL_SRV, prepareStatement(setSQL(STM_SCH_SL_SRV)));
			pstms.put(STM_SCH_SL_STA, prepareStatement(setSQL(STM_SCH_SL_STA)));
			pstms.put(STM_SCH_UPDATE, prepareStatement(setSQL(STM_SCH_UPDATE)));
			pstms.put(STM_SRV_SELECT, prepareStatement(setSQL(STM_SRV_SELECT)));
			pstms.put(STM_WDY_SELECT, prepareStatement(setSQL(STM_WDY_SELECT)));
			break;
		case LConstants.ACTION_SERVICES:
			pstms.put(STM_SRV_INSERT, prepareStatement(setSQL(STM_SRV_INSERT)));
			pstms.put(STM_SRV_SELECT, prepareStatement(setSQL(STM_SRV_SELECT)));
			pstms.put(STM_SRV_UPDATE, prepareStatement(setSQL(STM_SRV_UPDATE)));
			pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
			break;
		case LConstants.ACTION_SETUP:
			pstms.put(STM_STP_SELECT, prepareStatement(setSQL(STM_STP_SELECT)));
			break;
		case LConstants.ACTION_SPECGROUP:
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD2_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD3_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD4_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
			pstms.put(STM_SPG_INSERT, prepareStatement(setSQL(STM_SPG_INSERT)));
			pstms.put(STM_SPG_SELECT, prepareStatement(setSQL(STM_SPG_SELECT)));
			pstms.put(STM_SPG_UPDATE, prepareStatement(setSQL(STM_SPG_UPDATE)));
			pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
			break;
		case LConstants.ACTION_SPECIALTY:
			pstms.put(STM_SPY_INSERT, prepareStatement(setSQL(STM_SPY_INSERT)));
			pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
			pstms.put(STM_SPY_UPDATE, prepareStatement(setSQL(STM_SPY_UPDATE)));
			break;
		case LConstants.ACTION_SPECIMEN:
			pstms.put(STM_ADD_SL_SPG, prepareStatement(setSQL(STM_ADD_SL_SPG)));
			pstms.put(STM_FRZ_SL_SUM, prepareStatement(setSQL(STM_FRZ_SL_SUM)));
			pstms.put(STM_SPG_SL_SUM, prepareStatement(setSQL(STM_SPG_SL_SUM)));
			break;
		case LConstants.ACTION_SPECMASTER:
			pstms.put(STM_SPG_SELECT, prepareStatement(setSQL(STM_SPG_SELECT)));
			pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
			pstms.put(STM_SPM_UPDATE, prepareStatement(setSQL(STM_SPM_UPDATE)));
			pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_SUBSPECIAL:
			pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
			pstms.put(STM_SUB_INSERT, prepareStatement(setSQL(STM_SUB_INSERT)));
			pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
			pstms.put(STM_SUB_UPDATE, prepareStatement(setSQL(STM_SUB_UPDATE)));
			break;
		case LConstants.ACTION_TURNAROUND:
			pstms.put(STM_CSE_SL_TAT, prepareStatement(setSQL(STM_CSE_SL_TAT)));
			break;
		case LConstants.ACTION_TURNMASTER:
			pstms.put(STM_TUR_INSERT, prepareStatement(setSQL(STM_TUR_INSERT)));
			pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
			pstms.put(STM_TUR_UPDATE, prepareStatement(setSQL(STM_TUR_UPDATE)));
			break;
		case LConstants.ACTION_WORKDAYS:
			pstms.put(STM_SCH_SL_SUM, prepareStatement(setSQL(STM_SCH_SL_SUM)));
			break;
		case LConstants.ACTION_LBASE:
			pstms.put(STM_PND_SL_LST, prepareStatement(setSQL(STM_PND_SL_LST)));
			pstms.put(STM_STP_SL_SID, prepareStatement(setSQL(STM_STP_SL_SID)));
			pstms.put(STM_STP_UPDATE, prepareStatement(setSQL(STM_STP_UPDATE)));
			pstms.put(STM_WDY_SL_DTE, prepareStatement(setSQL(STM_WDY_SL_DTE)));
			pstms.put(STM_WDY_SL_NXT, prepareStatement(setSQL(STM_WDY_SL_NXT)));
			pstms.put(STM_WDY_SL_PRV, prepareStatement(setSQL(STM_WDY_SL_PRV)));
			break;
		case LConstants.ACTION_LDAYS:
			pstms.put(STM_WDY_INSERT, prepareStatement(setSQL(STM_WDY_INSERT)));
			pstms.put(STM_WDY_SL_LST, prepareStatement(setSQL(STM_WDY_SL_LST)));
			break;
		case LConstants.ACTION_LFLOW:
			pstms.put(STM_ACC_SELECT, prepareStatement(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_ORM_SELECT, prepareStatement(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_PRS_SELECT, prepareStatement(setSQL(STM_PRS_SELECT)));
			pstms.put(STM_PND_DEL_FN, prepareStatement(setSQL(STM_PND_DEL_FN)));
			pstms.put(STM_PND_DEL_ID, prepareStatement(setSQL(STM_PND_DEL_ID)));
			pstms.put(STM_PND_INSERT, prepareStatement(setSQL(STM_PND_INSERT)));
			pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
			pstms.put(STM_PND_SL_LST, prepareStatement(setSQL(STM_PND_SL_LST)));
			pstms.put(STM_PND_UP_EMB, prepareStatement(setSQL(STM_PND_UP_EMB)));
			pstms.put(STM_PND_UP_FIN, prepareStatement(setSQL(STM_PND_UP_FIN)));
			pstms.put(STM_PND_UP_GRS, prepareStatement(setSQL(STM_PND_UP_GRS)));
			pstms.put(STM_PND_UP_MIC, prepareStatement(setSQL(STM_PND_UP_MIC)));
			pstms.put(STM_PND_UP_ROU, prepareStatement(setSQL(STM_PND_UP_ROU)));
			pstms.put(STM_PND_UP_SCA, prepareStatement(setSQL(STM_PND_UP_SCA)));
			pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_LLOAD:
			pstms.put(STM_ACC_SELECT, prepareStatement(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_ADD_INSERT, prepareStatement(setSQL(STM_ADD_INSERT)));
			pstms.put(STM_ADD_SL_CID, prepareStatement(setSQL(STM_ADD_SL_CID)));
			pstms.put(STM_ADD_SL_LST, prepareStatement(setSQL(STM_ADD_SL_LST)));
			pstms.put(STM_ADD_SL_ORD, prepareStatement(setSQL(STM_ADD_SL_ORD)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD2_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD3_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD4_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_CMT_INSERT, prepareStatement(setSQL(STM_CMT_INSERT)));
			pstms.put(STM_CMT_UPDATE, prepareStatement(setSQL(STM_CMT_UPDATE)));
			pstms.put(STM_CSE_INSERT, prepareStatement(setSQL(STM_CSE_INSERT)));
			pstms.put(STM_CSE_SL_CID, prepareStatement(setSQL(STM_CSE_SL_CID)));
			pstms.put(STM_CSE_SL_WLD, prepareStatement(setSQL(STM_CSE_SL_WLD)));
			pstms.put(STM_CSE_SL_SPE, prepareStatement(setSQL(STM_CSE_SL_SPE)));
			pstms.put(STM_CSE_UPDATE, prepareStatement(setSQL(STM_CSE_UPDATE)));
			pstms.put(STM_ERR_DELETE, prepareStatement(setSQL(STM_ERR_DELETE)));
			pstms.put(STM_ERR_INSERT, prepareStatement(setSQL(STM_ERR_INSERT)));
			pstms.put(STM_ERR_SL_FXD, prepareStatement(setSQL(STM_ERR_SL_FXD)));
			pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_FRZ_INSERT, prepareStatement(setSQL(STM_FRZ_INSERT)));
			pstms.put(STM_FRZ_UPDATE, prepareStatement(setSQL(STM_FRZ_UPDATE)));
			pstms.put(STM_ORD_INSERT, prepareStatement(setSQL(STM_ORD_INSERT)));
			pstms.put(STM_ORD_UPDATE, prepareStatement(setSQL(STM_ORD_UPDATE)));
			pstms.put(STM_ORM_SELECT, prepareStatement(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_SPE_INSERT, prepareStatement(setSQL(STM_SPE_INSERT)));
			pstms.put(STM_SPE_UPDATE, prepareStatement(setSQL(STM_SPE_UPDATE)));
			pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_LLOGIN:
			pstms.put(STM_PRS_SL_PID, prepareStatement(setSQL(STM_PRS_SL_PID)));
			break;
		case LConstants.ACTION_LSYNC:
			pstms.put(STM_ACC_INSERT, prepareStatement(setSQL(STM_ACC_INSERT)));
			pstms.put(STM_ACC_SELECT, prepareStatement(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_ACC_UPDATE, prepareStatement(setSQL(STM_ACC_UPDATE)));
			pstms.put(STM_FAC_INSERT, prepareStatement(setSQL(STM_FAC_INSERT)));
			pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_FAC_UPDATE, prepareStatement(setSQL(STM_FAC_UPDATE)));
			pstms.put(STM_ORM_INSERT, prepareStatement(setSQL(STM_ORM_INSERT)));
			pstms.put(STM_ORM_SELECT, prepareStatement(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_ORM_UPDATE, prepareStatement(setSQL(STM_ORM_UPDATE)));
			pstms.put(STM_PRS_INSERT, prepareStatement(setSQL(STM_PRS_INSERT)));
			pstms.put(STM_PRS_SELECT, prepareStatement(setSQL(STM_PRS_SELECT)));
			pstms.put(STM_PRS_UPDATE, prepareStatement(setSQL(STM_PRS_UPDATE)));
			pstms.put(STM_SPM_INSERT, prepareStatement(setSQL(STM_SPM_INSERT)));
			pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
			pstms.put(STM_SPM_UPDATE, prepareStatement(setSQL(STM_SPM_UPDATE)));
			break;
		case LConstants.ACTION_LVAL5:
			pstms.put(STM_FRZ_SL_SU5, prepareStatement(setSQL(STM_FRZ_SL_SU5)));
			pstms.put(STM_SPG_SL_SU5, prepareStatement(setSQL(STM_SPG_SL_SU5)));
			pstms.put(STM_SPG_UPD_V5, prepareStatement(setSQL(STM_SPG_UPD_V5)));
			break;
		}
		return pstms;
	}

	@Override
	String setSQL(short id) {
		switch (id) {
		case STM_ACC_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvaccessions ORDER BY acnm";
		case STM_ADD_SL_CID:
			return "SELECT prid, adcd, adv5, adv1, adv2, adv3, adv4, addt, prnm, prls, prfr, cano "
					+ "FROM " + pj.pjSchema + ".udvadditionals WHERE caid = ? ORDER BY addt";
		case STM_ADD_SL_LST:
			return "SELECT MAX(addt) AS addt FROM " + pj.pjSchema + ".additionals WHERE adcd = ?";
		case STM_ADD_SL_SPG:
			return "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, "
					+ "f.fanm, COUNT(a.caid) AS qty, SUM(a.adv1) AS adv1, SUM(a.adv2) AS adv2, "
					+ "SUM(a.adv3) AS adv3, SUM(a.adv4) AS adv4, SUM(a.adv5) AS adv5 "
					+ "FROM " + pj.pjSchema + ".additionals AS a "
					+ "INNER JOIN " + pj.pjSchema + ".cases AS c ON c.caid = a.caid "
					+ "INNER JOIN " + pj.pjSchema + ".facilities AS f ON f.faid = c.faid "
					+ "INNER JOIN " + pj.pjSchema + ".specimaster AS m ON m.smid = c.smid "
					+ "INNER JOIN " + pj.pjSchema + ".specigroups AS g ON g.sgid = m.sgid "
					+ "INNER JOIN " + pj.pjSchema + ".procedures r ON r.poid = g.poid "
					+ "INNER JOIN " + pj.pjSchema + ".subspecial AS b ON b.sbid = g.sbid "
					+ "INNER JOIN " + pj.pjSchema + ".specialties AS y ON y.syid = b.syid "
					+ "WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm "
					+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid";
		case STM_ADD_SL_SUM:
			return "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(caid) AS adca, "
					+ "SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, SUM(adv3) AS adv3, SUM(adv4) AS adv4 "
					+ "FROM " + pj.pjSchema + ".udvadditionals WHERE (addt BETWEEN ? AND ?) "
					+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr "
					+ "ORDER BY faid, syid, sbid, poid, prid";
		case STM_CD1_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + pj.pjSchema + ".coder1 ORDER BY coid";
		case STM_CD2_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + pj.pjSchema + ".coder2 ORDER BY coid";
		case STM_CD3_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + pj.pjSchema + ".coder3 ORDER BY coid";
		case STM_CD4_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + pj.pjSchema + ".coder4 ORDER BY coid";
		case STM_CMT_SELECT:
			return "SELECT com1, com2, com3, com4 FROM " + pj.pjSchema + ".comments WHERE caid = ?";
		case STM_CSE_SL_CID:
			return "SELECT caid FROM " + pj.pjSchema + ".cases WHERE cano = ?";
		case STM_CSE_SL_CNO:
			return "SELECT cano FROM " + pj.pjSchema + ".cases WHERE caid = ?";
		case STM_CSE_SL_SPE:
			return "SELECT c.smid, c.fned, c.cano, s.spid FROM " + pj.pjSchema + ".cases AS c INNER JOIN " + pj.pjSchema + ".specimens AS s ON s.caid = c.caid "
					+ "AND s.smid = c.smid WHERE c.caid = ?";
		case STM_CSE_SL_SUM:
			return "SELECT faid, syid, sbid, poid, fnid, fanm, synm, sbnm, ponm, fnnm, fnls, fnfr, COUNT(caid) AS caca, "
					+ "SUM(CAST(casp as INT)) AS casp, SUM(CAST(cabl as INT)) AS cabl, SUM(CAST(casl as INT)) AS casl, SUM(CAST(cahe as INT)) AS cahe, "
					+ "SUM(CAST(cass as INT)) AS cass, SUM(CAST(caih as INT)) AS caih, SUM(CAST(camo as INT)) AS camo, SUM(CAST(cafs as INT)) AS cafs, "
					+ "SUM(CAST(casy as INT)) AS casy, SUM(CAST(grta as INT)) AS grta, SUM(CAST(emta as INT)) AS emta, SUM(CAST(mita as INT)) AS mita, "
					+ "SUM(CAST(rota as INT)) AS rota, SUM(CAST(fnta as INT)) AS fnta, SUM(CAST(cav5 as INT)) AS cav5, SUM(cav1) AS cav1, SUM(cav2) AS cav2, "
					+ "SUM(cav3) AS cav3, SUM(cav4) AS cav4 FROM " + pj.pjSchema + ".udvcases WHERE (fned BETWEEN ? AND ?) "
					+ "GROUP BY faid, syid, sbid, poid, fnid, fanm, synm, sbnm, ponm, fnnm, fnls, fnfr "
					+ "ORDER BY faid, syid, sbid, poid, fnid";
		case STM_ERR_SELECT:
			return "SELECT caid, erid, cano FROM " + pj.pjSchema + ".errors WHERE erid > 0 ORDER BY cano";
		case STM_ERR_SL_CMT:
			return "SELECT erdc FROM " + pj.pjSchema + ".errors WHERE caid = ?";
		case STM_ERR_SL_FXD:
			return "SELECT caid FROM " + pj.pjSchema + ".errors WHERE erid = 0 ORDER BY caid";
		case STM_FAC_SELECT:
			return "SELECT faid, fafl, fald, fanm, fadc FROM " + pj.pjSchema + ".facilities ORDER BY faid";
		case STM_FRZ_SL_SID:
			return "SELECT prid, frbl, frsl, frv5, frv1, frv2, frv3, frv4, prnm, prls, "
					+ "prfr, spdc, smnm FROM " + pj.pjSchema + ".udvfrozens WHERE spid = ?";
		case STM_FRZ_SL_SU5:
			return "SELECT COUNT(*) AS QTY, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
					+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 FROM " + pj.pjSchema + ".udvfrozens WHERE aced BETWEEN ? AND ?";
		case STM_FRZ_SL_SUM:
			return "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(spid) AS frsp, "
					+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl, SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
					+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 FROM " + pj.pjSchema + ".udvfrozens WHERE (aced BETWEEN ? AND ?) "
					+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr "
					+ "ORDER BY faid, syid, sbid, poid, prid";
		case STM_ORD_SELECT:
			return "SELECT orqy, orv1, orv2, orv3, orv4, ognm FROM " + pj.pjSchema + ".udvorders WHERE spid = ? ORDER BY ognm";
		case STM_ORG_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvordergroups ORDER BY ognm";
		case STM_ORM_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvordermaster ORDER BY omnm";
		case STM_PND_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvpending ORDER BY pnid";
		case STM_PND_SL_ROU:
			return "SELECT * FROM " + pj.pjSchema + ".udvpending WHERE roed BETWEEN ? AND ? ORDER BY pnid";
		case STM_PRO_SELECT:
			return "SELECT poid, ponm, podc FROM " + pj.pjSchema + ".procedures ORDER BY ponm";
		case STM_PRS_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".persons ORDER BY prnm";
		case STM_PRS_SL_PID:
			return "SELECT prvl FROM " + pj.pjSchema + ".persons WHERE prid = ?";
		case STM_RUL_SELECT:
			return "SELECT ruid, runm, rudc FROM " + pj.pjSchema + ".rules ORDER BY ruid";
		case STM_SCH_SL_SRV:
			return "SELECT wdid, srid, prid, prnm, srnm FROM " + pj.pjSchema + ".udvschedules WHERE (wddt BETWEEN ? AND ?) ORDER BY srnm, wdid";
		case STM_SCH_SL_SUM:
			return "SELECT * FROM " + pj.pjSchema + ".udvschedules WHERE (wddt BETWEEN ? AND ?) ORDER BY faid, prid, wdid, srid";
		case STM_SCH_SL_STA:
			return "SELECT wdid, srid, prid, prnm, srnm FROM " + pj.pjSchema + ".udvschedules WHERE (wddt BETWEEN ? AND ?) ORDER BY prnm, wdid, srnm";
		case STM_SPE_SELECT:
			return "SELECT spid, smid, spbl, spsl, spfr, sphe, spss, spih, spmo, spv5, spv1, spv2, spv3, "
					+ "spv4, spdc, smnm, smdc, ponm FROM " + pj.pjSchema + ".udvspecimens WHERE caid = ? ORDER BY spid";
		case STM_SPG_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvspecigroups ORDER BY sgdc";
		case STM_SPG_SL_SU5:
			return "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1, "
					+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4 "
					+ "FROM " + pj.pjSchema + ".specigroups g INNER JOIN " + pj.pjSchema + ".specimaster m ON g.sgid = m.sgid "
					+ "INNER JOIN " + pj.pjSchema + ".specimens s ON m.smid = s.smid INNER JOIN " + pj.pjSchema + ".cases c ON c.caid = s.caid "
					+ "WHERE c.fned BETWEEN ? AND ? GROUP BY g.sgid";
		case STM_SPG_SL_SUM:
			return "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, "
					+ "COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl, "
					+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1, "
					+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5 "
					+ "FROM " + pj.pjSchema + ".specigroups g INNER JOIN " + pj.pjSchema + ".specimaster m ON g.sgid = m.sgid "
					+ "INNER JOIN " + pj.pjSchema + ".specimens s ON m.smid = s.smid "
					+ "INNER JOIN " + pj.pjSchema + ".cases c ON c.caid = s.caid "
					+ "INNER JOIN " + pj.pjSchema + ".procedures r ON r.poid = g.poid "
					+ "INNER JOIN " + pj.pjSchema + ".subspecial b ON b.sbid = g.sbid "
					+ "INNER JOIN " + pj.pjSchema + ".specialties y ON y.syid = b.syid "
					+ "INNER JOIN " + pj.pjSchema + ".facilities f ON f.faid = c.faid WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm "
					+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid";
		case STM_SPM_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvspecimaster ORDER BY smnm";
		case STM_SPY_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".specialties ORDER BY synm";
		case STM_STP_SELECT:
			return "SELECT stid, stva FROM " + pj.pjSchema + ".setup ORDER BY stid";
		case STM_STP_SL_SID:
			return "SELECT stva FROM " + pj.pjSchema + ".setup WHERE stid = ?";
		case STM_SUB_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvsubspecial ORDER BY sbnm";
		case STM_TUR_SELECT:
			return "SELECT taid, grss, embd, micr, rout, finl, tanm FROM " + pj.pjSchema + ".turnaround ORDER BY tanm";
		case STM_WDY_SELECT:
			return "SELECT wdid, wdno, wdtp, wddt FROM " + pj.pjSchema + ".workdays WHERE wddt >= ? ORDER BY wddt";
		case STM_WDY_SL_DTE:
			return "SELECT wdno FROM " + pj.pjSchema + ".workdays WHERE wddt = ?";
		case STM_WDY_SL_NXT:
			return "SELECT MIN(wddt) AS wddt FROM " + pj.pjSchema + ".workdays WHERE wddt > ? AND wdtp = 'D'";
		case STM_WDY_SL_PRV:
			return "SELECT MAX(wddt) AS wddt FROM " + pj.pjSchema + ".workdays WHERE wddt < ? AND wdtp = 'D'";
		default:
			return super.setSQL(id);
		}
	}
}
