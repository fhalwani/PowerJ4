package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

class DServer extends DPowerJ {

	public DServer(LBase parent) {
		super(parent);
		dbName = "DBServer";
	}

	@Override
	Object[] getFacilities(boolean isFilter) {
		ArrayList<OItem> list = new ArrayList<OItem>();
		if (isFilter) {
			list.add(new OItem((short) 0, "* All *"));
		}
		try {
			PreparedStatement pstm = prepareCallables(setSQL(STM_FAC_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				if (rst.getString("FAFL").equalsIgnoreCase("Y") || rst.getString("FALD").equalsIgnoreCase("Y")) {
					list.add(new OItem(rst.getShort("FAID"), rst.getString("FANM")));
				}
			}
			close(rst);
			close(pstm);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} finally {
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
			PreparedStatement pstm = prepareCallables(setSQL(STM_ORG_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("OGID"), rst.getString("OGNM")));
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
			PreparedStatement pstm = prepareCallables(setSQL(STM_PRO_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("POID"), rst.getString("PONM")));
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
			PreparedStatement pstm = prepareCallables(setSQL(STM_SPY_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SYID"), rst.getString("SYNM")));
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
			PreparedStatement pstm = prepareCallables(setSQL(STM_SUB_SELECT));
			ResultSet rst = getResultSet(pstm);
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SBID"), rst.getString("SBNM")));
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
			pstms.put(STM_ACC_SELECT, prepareCallables(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_ACC_UPDATE, prepareStatement(setSQL(STM_ACC_UPDATE)));
			break;
		case LConstants.ACTION_BACKLOG:
			pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
			pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_CODER1:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD1_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD1_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareCallables(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_CODER2:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD2_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD2_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareCallables(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_CODER3:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD3_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD3_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareCallables(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_CODER4:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD4_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD4_UPDATE)));
			pstms.put(STM_RUL_SELECT, prepareCallables(setSQL(STM_RUL_SELECT)));
			break;
		case LConstants.ACTION_DAILY:
			pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
			pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_DISTRIBUTE:
			pstms.put(STM_ADD_SL_SUM, prepareCallables(setSQL(STM_ADD_SL_SUM)));
			pstms.put(STM_CSE_SL_SUM, prepareCallables(setSQL(STM_CSE_SL_SUM)));
			pstms.put(STM_FRZ_SL_SUM, prepareCallables(setSQL(STM_FRZ_SL_SUM)));
			break;
		case LConstants.ACTION_EDITOR:
			pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_ERROR:
			pstms.put(STM_ERR_SELECT, prepareCallables(setSQL(STM_ERR_SELECT)));
			pstms.put(STM_ERR_SL_CMT, prepareCallables(setSQL(STM_ERR_SL_CMT)));
			pstms.put(STM_ERR_UPDATE, prepareStatement(setSQL(STM_ERR_UPDATE)));
			pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_FACILITY:
			pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_FAC_UPDATE, prepareStatement(setSQL(STM_FAC_UPDATE)));
			break;
		case LConstants.ACTION_FINALS:
			pstms.put(STM_ADD_SL_CID, prepareCallables(setSQL(STM_ADD_SL_CID)));
			pstms.put(STM_CMT_SELECT, prepareCallables(setSQL(STM_CMT_SELECT)));
			pstms.put(STM_FRZ_SL_SID, prepareCallables(setSQL(STM_FRZ_SL_SID)));
			pstms.put(STM_ORD_SELECT, prepareCallables(setSQL(STM_ORD_SELECT)));
			pstms.put(STM_SPE_SELECT, prepareCallables(setSQL(STM_SPE_SELECT)));
			break;
		case LConstants.ACTION_FORECAST:
			pstms.put(STM_CSE_SL_YER, prepareCallables(setSQL(STM_CSE_SL_YER)));
			break;
		case LConstants.ACTION_HISTOLOGY:
			pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
			break;
		case LConstants.ACTION_ORDERGROUP:
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD2_SELECT, prepareCallables(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD3_SELECT, prepareCallables(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD4_SELECT, prepareCallables(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_ORG_INSERT, prepareStatement(setSQL(STM_ORG_INSERT)));
			pstms.put(STM_ORG_SELECT, prepareCallables(setSQL(STM_ORG_SELECT)));
			pstms.put(STM_ORG_UPDATE, prepareStatement(setSQL(STM_ORG_UPDATE)));
			pstms.put(STM_ORT_SELECT, prepareStatement(setSQL(STM_ORT_SELECT)));
			break;
		case LConstants.ACTION_ORDERMASTER:
			pstms.put(STM_ORG_SELECT, prepareCallables(setSQL(STM_ORG_SELECT)));
			pstms.put(STM_ORM_SELECT, prepareCallables(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_ORM_UPDATE, prepareStatement(setSQL(STM_ORM_UPDATE)));
			break;
		case LConstants.ACTION_PENDING:
			pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
			pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_PERSONNEL:
			pstms.put(STM_PRS_SELECT, prepareCallables(setSQL(STM_PRS_SELECT)));
			pstms.put(STM_PRS_UPDATE, prepareStatement(setSQL(STM_PRS_UPDATE)));
			break;
		case LConstants.ACTION_PROCEDURES:
			pstms.put(STM_PRO_INSERT, prepareStatement(setSQL(STM_PRO_INSERT)));
			pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
			pstms.put(STM_PRO_UPDATE, prepareStatement(setSQL(STM_PRO_UPDATE)));
			break;
		case LConstants.ACTION_ROUTING:
			pstms.put(STM_PND_SL_ROU, prepareCallables(setSQL(STM_PND_SL_ROU)));
			break;
		case LConstants.ACTION_RULES:
			pstms.put(STM_RUL_INSERT, prepareStatement(setSQL(STM_RUL_INSERT)));
			pstms.put(STM_RUL_SELECT, prepareCallables(setSQL(STM_RUL_SELECT)));
			pstms.put(STM_RUL_UPDATE, prepareStatement(setSQL(STM_RUL_UPDATE)));
			break;
		case LConstants.ACTION_SCHEDULE:
			pstms.put(STM_PRS_SELECT, prepareCallables(setSQL(STM_PRS_SELECT)));
			pstms.put(STM_SCH_INSERT, prepareStatement(setSQL(STM_SCH_INSERT)));
			pstms.put(STM_SCH_SL_MON, prepareStatement(setSQL(STM_SCH_SL_MON)));
			pstms.put(STM_SCH_SL_SRV, prepareStatement(setSQL(STM_SCH_SL_SRV)));
			pstms.put(STM_SCH_SL_STA, prepareStatement(setSQL(STM_SCH_SL_STA)));
			pstms.put(STM_SCH_UPDATE, prepareStatement(setSQL(STM_SCH_UPDATE)));
			pstms.put(STM_SRV_SELECT, prepareCallables(setSQL(STM_SRV_SELECT)));
			pstms.put(STM_WDY_SELECT, prepareCallables(setSQL(STM_WDY_SELECT)));
			break;
		case LConstants.ACTION_SERVICES:
			pstms.put(STM_SRV_INSERT, prepareStatement(setSQL(STM_SRV_INSERT)));
			pstms.put(STM_SRV_SELECT, prepareCallables(setSQL(STM_SRV_SELECT)));
			pstms.put(STM_SRV_UPDATE, prepareStatement(setSQL(STM_SRV_UPDATE)));
			pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
			break;
		case LConstants.ACTION_SETUP:
			pstms.put(STM_STP_SELECT, prepareCallables(setSQL(STM_STP_SELECT)));
			break;
		case LConstants.ACTION_SPECGROUP:
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD2_SELECT, prepareCallables(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD3_SELECT, prepareCallables(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD4_SELECT, prepareCallables(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
			pstms.put(STM_SPG_INSERT, prepareStatement(setSQL(STM_SPG_INSERT)));
			pstms.put(STM_SPG_SELECT, prepareCallables(setSQL(STM_SPG_SELECT)));
			pstms.put(STM_SPG_UPDATE, prepareStatement(setSQL(STM_SPG_UPDATE)));
			pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
			break;
		case LConstants.ACTION_SPECIALTY:
			pstms.put(STM_SPY_INSERT, prepareStatement(setSQL(STM_SPY_INSERT)));
			pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
			pstms.put(STM_SPY_UPDATE, prepareStatement(setSQL(STM_SPY_UPDATE)));
			break;
		case LConstants.ACTION_SPECIMEN:
			pstms.put(STM_SPG_SL_SUM, prepareCallables(setSQL(STM_SPG_SL_SUM)));
			break;
		case LConstants.ACTION_SPECMASTER:
			pstms.put(STM_SPG_SELECT, prepareCallables(setSQL(STM_SPG_SELECT)));
			pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
			pstms.put(STM_SPM_UPDATE, prepareStatement(setSQL(STM_SPM_UPDATE)));
			pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
			break;
		case LConstants.ACTION_SUBSPECIAL:
			pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
			pstms.put(STM_SUB_INSERT, prepareStatement(setSQL(STM_SUB_INSERT)));
			pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
			pstms.put(STM_SUB_UPDATE, prepareStatement(setSQL(STM_SUB_UPDATE)));
			break;
		case LConstants.ACTION_TURNAROUND:
			pstms.put(STM_CSE_SL_TAT, prepareStatement(setSQL(STM_CSE_SL_TAT)));
			break;
		case LConstants.ACTION_TURNMASTER:
			pstms.put(STM_TUR_INSERT, prepareStatement(setSQL(STM_TUR_INSERT)));
			pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
			pstms.put(STM_TUR_UPDATE, prepareStatement(setSQL(STM_TUR_UPDATE)));
			break;
		case LConstants.ACTION_WORKDAYS:
			pstms.put(STM_SCH_SL_SUM, prepareCallables(setSQL(STM_SCH_SL_SUM)));
			break;
		case LConstants.ACTION_LBASE:
			pstms.put(STM_PND_SL_LST, prepareStatement(setSQL(STM_PND_SL_LST)));
			pstms.put(STM_STP_SL_SID, prepareCallables(setSQL(STM_STP_SL_SID)));
			pstms.put(STM_STP_UPDATE, prepareStatement(setSQL(STM_STP_UPDATE)));
			pstms.put(STM_WDY_SL_DTE, prepareCallables(setSQL(STM_WDY_SL_DTE)));
			pstms.put(STM_WDY_SL_NXT, prepareCallables(setSQL(STM_WDY_SL_NXT)));
			pstms.put(STM_WDY_SL_PRV, prepareCallables(setSQL(STM_WDY_SL_PRV)));
			break;
		case LConstants.ACTION_LDAYS:
			pstms.put(STM_WDY_INSERT, prepareStatement(setSQL(STM_WDY_INSERT)));
			pstms.put(STM_WDY_SL_LST, prepareStatement(setSQL(STM_WDY_SL_LST)));
			break;
		case LConstants.ACTION_LFLOW:
			pstms.put(STM_ACC_SELECT, prepareCallables(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_ORM_SELECT, prepareCallables(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_PND_DEL_FN, prepareStatement(setSQL(STM_PND_DEL_FN)));
			pstms.put(STM_PND_DEL_ID, prepareStatement(setSQL(STM_PND_DEL_ID)));
			pstms.put(STM_PND_INSERT, prepareStatement(setSQL(STM_PND_INSERT)));
			pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
			pstms.put(STM_PND_SL_LST, prepareStatement(setSQL(STM_PND_SL_LST)));
			pstms.put(STM_PND_UP_EMB, prepareStatement(setSQL(STM_PND_UP_EMB)));
			pstms.put(STM_PND_UP_FIN, prepareStatement(setSQL(STM_PND_UP_FIN)));
			pstms.put(STM_PND_UP_GRS, prepareStatement(setSQL(STM_PND_UP_GRS)));
			pstms.put(STM_PND_UP_MIC, prepareStatement(setSQL(STM_PND_UP_MIC)));
			pstms.put(STM_PND_UP_ROU, prepareStatement(setSQL(STM_PND_UP_ROU)));
			pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_LLOAD:
			pstms.put(STM_ACC_SELECT, prepareCallables(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_ADD_INSERT, prepareStatement(setSQL(STM_ADD_INSERT)));
			pstms.put(STM_ADD_SL_CID, prepareCallables(setSQL(STM_ADD_SL_CID)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD2_SELECT, prepareCallables(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD3_SELECT, prepareCallables(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD4_SELECT, prepareCallables(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_CMT_INSERT, prepareStatement(setSQL(STM_CMT_INSERT)));
			pstms.put(STM_CMT_UPDATE, prepareStatement(setSQL(STM_CMT_UPDATE)));
			pstms.put(STM_CSE_INSERT, prepareStatement(setSQL(STM_CSE_INSERT)));
			pstms.put(STM_CSE_SL_CID, prepareCallables(setSQL(STM_CSE_SL_CID)));
			pstms.put(STM_CSE_SL_LST, prepareStatement(setSQL(STM_CSE_SL_LST)));
			pstms.put(STM_CSE_SL_SPE, prepareCallables(setSQL(STM_CSE_SL_SPE)));
			pstms.put(STM_CSE_UPDATE, prepareStatement(setSQL(STM_CSE_UPDATE)));
			pstms.put(STM_ERR_DELETE, prepareStatement(setSQL(STM_ERR_DELETE)));
			pstms.put(STM_ERR_INSERT, prepareStatement(setSQL(STM_ERR_INSERT)));
			pstms.put(STM_ERR_SL_FXD, prepareCallables(setSQL(STM_ERR_SL_FXD)));
			pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_FRZ_INSERT, prepareStatement(setSQL(STM_FRZ_INSERT)));
			pstms.put(STM_FRZ_UPDATE, prepareStatement(setSQL(STM_FRZ_UPDATE)));
			pstms.put(STM_ORD_INSERT, prepareStatement(setSQL(STM_ORD_INSERT)));
			pstms.put(STM_ORD_UPDATE, prepareStatement(setSQL(STM_ORD_UPDATE)));
			pstms.put(STM_ORM_SELECT, prepareCallables(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_SPE_INSERT, prepareStatement(setSQL(STM_SPE_INSERT)));
			pstms.put(STM_SPE_UPDATE, prepareStatement(setSQL(STM_SPE_UPDATE)));
			pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
			break;
		case LConstants.ACTION_LLOGIN:
			pstms.put(STM_PRS_SL_PID, prepareCallables(setSQL(STM_PRS_SL_PID)));
			break;
		case LConstants.ACTION_LSYNC:
			pstms.put(STM_ACC_INSERT, prepareStatement(setSQL(STM_ACC_INSERT)));
			pstms.put(STM_ACC_SELECT, prepareCallables(setSQL(STM_ACC_SELECT)));
			pstms.put(STM_ACC_UPDATE, prepareStatement(setSQL(STM_ACC_UPDATE)));
			pstms.put(STM_FAC_INSERT, prepareStatement(setSQL(STM_FAC_INSERT)));
			pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
			pstms.put(STM_FAC_UPDATE, prepareStatement(setSQL(STM_FAC_UPDATE)));
			pstms.put(STM_ORM_INSERT, prepareStatement(setSQL(STM_ORM_INSERT)));
			pstms.put(STM_ORM_SELECT, prepareCallables(setSQL(STM_ORM_SELECT)));
			pstms.put(STM_ORM_UPDATE, prepareStatement(setSQL(STM_ORM_UPDATE)));
			pstms.put(STM_PRS_INSERT, prepareStatement(setSQL(STM_PRS_INSERT)));
			pstms.put(STM_PRS_SELECT, prepareCallables(setSQL(STM_PRS_SELECT)));
			pstms.put(STM_PRS_UPDATE, prepareStatement(setSQL(STM_PRS_UPDATE)));
			pstms.put(STM_SPM_INSERT, prepareStatement(setSQL(STM_SPM_INSERT)));
			pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
			pstms.put(STM_SPM_UPDATE, prepareStatement(setSQL(STM_SPM_UPDATE)));
			break;
		case LConstants.ACTION_LVAL5:
			pstms.put(STM_FRZ_SL_SU5, prepareCallables(setSQL(STM_FRZ_SL_SU5)));
			pstms.put(STM_SPG_SL_SU5, prepareCallables(setSQL(STM_SPG_SL_SU5)));
			pstms.put(STM_SPG_UPD_V5, prepareStatement(setSQL(STM_SPG_UPD_V5)));
			break;
		}
		return pstms;
	}

	@Override
	String setSQL(short id) {
		switch (id) {
		case STM_ACC_SELECT:
			return "{call udpAccessions}";
		case STM_ADD_SL_CID:
			return "{call udpAdditionals(?)}";
		case STM_ADD_SL_SUM:
			return "{call udpAddSum(?, ?)}";
		case STM_CD1_SELECT:
			return "{call udpCoder1}";
		case STM_CD2_SELECT:
			return "{call udpCoder2}";
		case STM_CD3_SELECT:
			return "{call udpCoder3}";
		case STM_CD4_SELECT:
			return "{call udpCoder4}";
		case STM_CMT_SELECT:
			return "{call udpCmt(?)}";
		case STM_CSE_SL_CID:
			return "{call udpCseID(?)}";
		case STM_CSE_SL_CNO:
			return "{call udpCseNo(?)}";
		case STM_CSE_SL_SPE:
			return "{call udpCseSpe(?)}";
		case STM_CSE_SL_SUM:
			return "{call udpCseSum(?, ?)}";
		case STM_CSE_SL_YER:
			return "{call udpCseYear(?, ?)}";
		case STM_ERR_SELECT:
			return "{call udpErrSelect}";
		case STM_ERR_SL_CMT:
			return "{call udpErrCmt(?)}";
		case STM_ERR_SL_FXD:
			return "{call udpErrRedo}";
		case STM_FAC_SELECT:
			return "{call udpFacility}";
		case STM_FRZ_SL_SID:
			return "{call udpFrzSID(?)}";
		case STM_FRZ_SL_SUM:
			return "{call udpFrzSum(?, ?)}";
		case STM_ORD_SELECT:
			return "{call udpOrder(?)}";
		case STM_ORG_SELECT:
			return "{call udpOrderGroup}";
		case STM_ORM_SELECT:
			return "{call udpOrderMaster}";
		case STM_PND_SELECT:
			return "{call udpPending}";
		case STM_PND_SL_ROU:
			return "{call udpPendingRouted(?, ?)}";
		case STM_PRO_SELECT:
			return "{call udpProcedure}";
		case STM_PRS_SELECT:
			return "{call udpPrsName}";
		case STM_PRS_SL_PID:
			return "{call udpPrsID(?)}";
		case STM_RUL_SELECT:
			return "{call udpRule}";
		case STM_SCH_SL_SRV:
			return "{call udpSchedServ(?, ?)}";
		case STM_SCH_SL_SUM:
			return "{call udpSchedSum(?, ?)}";
		case STM_SCH_SL_STA:
			return "{call udpSchedStaff(?, ?)}";
		case STM_SPE_SELECT:
			return "{call udpSpecimens(?)}";
		case STM_SPG_SELECT:
			return "{call udpSpecGroup}";
		case STM_SPG_SL_SU5:
			return "{call udpSpecSu5(?, ?)}";
		case STM_SPG_SL_SUM:
			return "{call udpSpecSum(?, ?)}";
		case STM_SPM_SELECT:
			return "{call udpSpecMaster}";
		case STM_SPY_SELECT:
			return "{call udpSpecialty}";
		case STM_SRV_SELECT:
			return "{call udpService}";
		case STM_STP_SELECT:
			return "{call udpSetup}";
		case STM_STP_SL_SID:
			return "{call udpStpID(?)}";
		case STM_SUB_SELECT:
			return "{call udpSubspecial}";
		case STM_TUR_SELECT:
			return "{call udpTurnaround}";
		case STM_WDY_SELECT:
			return "{call udpWdy(?)}";
		case STM_WDY_SL_DTE:
			return "{call udpWdyDte(?)}";
		case STM_WDY_SL_NXT:
			return "{call udpWdyNxt(?)}";
		case STM_WDY_SL_PRV:
			return "{call udpWdyPrv(?)}";
		default:
			return super.setSQL(id);
		}
	}
}