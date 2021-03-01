package ca.powerj.database;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import ca.powerj.data.ReportData;
import ca.powerj.data.SpecimenData;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

class DBServer extends DBPowerj {

	public DBServer(LibBase base) {
		super(base);
		dbName = "DBServer";
	}

	@Override
	public void getCaseUMLS(long caseID, short userID, ReportData reportData) {
		byte specimenNo = -1;
		byte tissueNo = -1;
		byte diagnosisNo = -1;
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_UML_SL_CID), 1, caseID);
			setShort(actionStms.get(STM_UML_SL_CID), 2, userID);
			rst = getResultSet(actionStms.get(STM_UML_SL_CID));
			while (rst.next()) {
				if (specimenNo != rst.getByte("splb")) {
					tissueNo = -1;
					diagnosisNo = -1;
					specimenNo = rst.getByte("splb");
//					reportData.addSpecimen((rst.getString("ticx").equals("Y") ? true: false),
//							rst.getByte("syid"), rst.getByte("sbid"),
//							rst.getShort("onid"), rst.getByte("poid"),
//							specimenNo, rst.getShort("tiid"), rst.getLong("spid"),
//							(rst.getString("spdc2") == null ? rst.getString("spdc1"): rst.getString("spdc2")),
//							rst.getString("splo"),
//							(rst.getString("sppo2") == null ? rst.getString("sppo1"): rst.getString("sppo2")),
//							rst.getString("spnm"));
				}
				if (tissueNo != rst.getByte("palb")) {
					diagnosisNo = -1;
					tissueNo = rst.getByte("palb");
					reportData.addTissue(specimenNo, tissueNo, rst.getShort("paid"),
							(rst.getString("tidc2") == null ? rst.getString("tidc1"): rst.getString("tidc2")),
							rst.getString("tinm"));
				}
				if (diagnosisNo != rst.getByte("dglb")) {
					diagnosisNo = rst.getByte("dglb");
					reportData.addDiagnosis(specimenNo, tissueNo, diagnosisNo, rst.getByte("dsid"), rst.getInt("dgid"),
							(rst.getString("dgdc2") == null ? rst.getString("dgdc1"): rst.getString("dgdc2")),
							(rst.getString("midc2") == null ? rst.getString("midc1"): rst.getString("midc2")),
							rst.getString("dgnm"));
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
	}

	@Override
	public void getSpecimenTissues(short userID, ReportData reportData, ArrayList<SpecimenData> specimens) {
		byte specimenNo = -1;
		byte tissueNo = -1;
		ResultSet rst = null;
		try {
			byte noSpecimens = (byte) specimens.size();
			for (byte i = 0; i < noSpecimens; i++) {
				SpecimenData specimen = specimens.get(i);
				setShort(actionStms.get(STM_TIS_SL_SID), 1, specimen.getSpmID());
				setShort(actionStms.get(STM_TIS_SL_SID), 2, userID);
				rst = getResultSet(actionStms.get(STM_TIS_SL_SID));
				while (rst.next()) {
					if (specimenNo != i) {
						tissueNo = -1;
						specimenNo = i;
//						reportData.addSpecimen((rst.getString("ticx").equals("Y") ? true: false),
//								rst.getByte("syid"), rst.getByte("sbid"),
//								rst.getShort("onid"), rst.getByte("poid"),
//								specimenNo, rst.getShort("tiid"), specimen.getSpecID(),
//								(rst.getString("spdc2") == null ? rst.getString("spdc1"): rst.getString("spdc2")),
//								specimen.getDescr(),
//								(rst.getString("sppo2") == null ? rst.getString("sppo1"): rst.getString("sppo2")),
//								rst.getString("spnm"));
					}
					if (tissueNo != rst.getByte("palb")) {
						tissueNo = rst.getByte("palb");
						reportData.addTissue(specimenNo, tissueNo, rst.getShort("paid"),
								(rst.getString("tidc2") == null ? rst.getString("tidc1"): rst.getString("tidc2")),
								rst.getString("tinm"));
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
	}

	@Override
	public String getSQL2(short id) {
		switch (id) {
		case STM_ACC_SELECT:
			return "{call <pjschema>.udpaccessions}";
		case STM_ADD_SL_LST:
			return "{call <pjschema>.udpaddlast(?)}";
		case STM_ADD_SL_CID:
			return "{call <pjschema>.udpadditionals(?)}";
		case STM_ADD_SL_SPG:
			return "{call <pjschema>.udpaddspg(?, ?)}";
		case STM_ADD_SL_SUM:
			return "{call <pjschema>.udpaddsum(?, ?)}";
		case STM_ADD_SL_YER:
			return "{call <pjschema>.udpaddyear(?, ?)}";
		case STM_CD1_SELECT:
			return "{call <pjschema>.udpcoder1}";
		case STM_CD2_SELECT:
			return "{call <pjschema>.udpcoder2}";
		case STM_CD3_SELECT:
			return "{call <pjschema>.udpcoder3}";
		case STM_CD4_SELECT:
			return "{call <pjschema>.udpcoder4}";
		case STM_CMT_SELECT:
			return "{call <pjschema>.udpcmt(?)}";
		case STM_CSE_SL_CID:
			return "{call <pjschema>.udpcseid(?)}";
		case STM_CSE_SL_CNO:
			return "{call <pjschema>.udpcseno(?)}";
		case STM_CSE_SL_SPE:
			return "{call <pjschema>.udpcsespe(?)}";
		case STM_CSE_SL_SUM:
			return "{call <pjschema>.udpcsesum(?, ?)}";
		case STM_DIS_SELECT:
			return "{call <pjschema>.udpdiseases}";
		case STM_ERR_SELECT:
			return "{call <pjschema>.udperrselect}";
		case STM_ERR_SL_CMT:
			return "{call <pjschema>.udperrcmt(?)}";
		case STM_ERR_SL_FXD:
			return "{call <pjschema>.udperrredo}";
		case STM_FAC_SELECT:
			return "{call <pjschema>.udpfacility}";
		case STM_FRZ_SL_SID:
			return "{call <pjschema>.udpfrzsid(?)}";
		case STM_FRZ_SL_SPG:
			return "{call <pjschema>.udpfrzspg(?, ?)}";
		case STM_FRZ_SL_SU5:
			return "{call <pjschema>.udpfrzsu5(?, ?)}";
		case STM_FRZ_SL_SUM:
			return "{call <pjschema>.udpfrzsum(?, ?)}";
		case STM_FRZ_SL_YER:
			return "{call <pjschema>.udpfrzyear(?, ?)}";
		case STM_ORD_SELECT:
			return "{call <pjschema>.udporder(?)}";
		case STM_ORG_SELECT:
			return "{call <pjschema>.udpordergroup}";
		case STM_ORM_SELECT:
			return "{call <pjschema>.udpordermaster}";
		case STM_ORN_SELECT:
			return "{call <pjschema>.udporgans}";
		case STM_PND_SELECT:
			return "{call <pjschema>.udppending}";
		case STM_PND_SL_ROU:
			return "{call <pjschema>.udppendingrouted(?, ?)}";
		case STM_PRO_SELECT:
			return "{call <pjschema>.udpprocedure}";
		case STM_PRS_SELECT:
			return "{call <pjschema>.udpprsname}";
		case STM_PRS_SL_PID:
			return "{call <pjschema>.udpprsid(?)}";
		case STM_RUL_SELECT:
			return "{call <pjschema>.udprule}";
		case STM_SBS_SELECT:
			return "{call <pjschema>.udpsubs}";
		case STM_SCH_SL_SRV:
			return "{call <pjschema>.udpschedserv(?, ?)}";
		case STM_SCH_SL_SUM:
			return "{call <pjschema>.udpschedsum(?, ?)}";
		case STM_SCH_SL_STA:
			return "{call <pjschema>.udpschedstaff(?, ?)}";
		case STM_SPE_SELECT:
			return "{call <pjschema>.udpspecimens(?)}";
		case STM_SPG_SELECT:
			return "{call <pjschema>.udpspecgroup}";
		case STM_SPG_SL_SU5:
			return "{call <pjschema>.udpspecsu5(?, ?)}";
		case STM_SPG_SL_SUM:
			return "{call <pjschema>.udpspecsum(?, ?)}";
		case STM_SPG_SL_YER:
			return "{call <pjschema>.udpspecyear(?, ?)}";
		case STM_SPM_SELECT:
			return "{call <pjschema>.udpspecmaster}";
		case STM_SPY_SELECT:
			return "{call <pjschema>.udpspecialty}";
		case STM_SRV_SELECT:
			return "{call <pjschema>.udpservice}";
		case STM_STP_SELECT:
			return "{call <pjschema>.udpsetup}";
		case STM_STP_SL_SID:
			return "{call <pjschema>.udpstpid(?)}";
		case STM_SUB_SELECT:
			return "{call <pjschema>.udpsubspecial}";
		case STM_TUR_SELECT:
			return "{call <pjschema>.udpturnaround}";
		case STM_WDY_SELECT:
			return "{call <pjschema>.udpwdy(?)}";
		case STM_WDY_SL_DTE:
			return "{call <pjschema>.udpwdydte(?)}";
		case STM_WDY_SL_NXT:
			return "{call <pjschema>.udpwdynxt(?)}";
		case STM_WDY_SL_PRV:
			return "{call <pjschema>.udpwdyprv(?)}";
		case STM_TIS_SELECT:
			return "{call <pjschema>.udporgans(?, ?, ?)}";
		case STM_STY_SELECT:
			return "{call <pjschema>.udpstyles}";
		case STM_STY_SL_PID:
			return "{call <pjschema>.udpstypeprs(?)}";
		case STM_UML_SL_CID:
			return "{call <pjschema>.udpcaseumls(?, ?)}";
		case STM_TIS_SL_SID:
			return "{call <pjschema>.udpspecimentissues(?, ?)}";
		default:
			return super.getSQL2(id);
		}
	}

	@Override
	public void setStatements(byte id) {
		close(actionStms);
		switch (id) {
		case LibConstants.ACTION_ACCESSION:
			actionStms.put(STM_ACC_SELECT, prepareCallables(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_ACC_UPDATE, prepareStatement(getSQL(STM_ACC_UPDATE)));
			break;
		case LibConstants.ACTION_BACKLOG:
			actionStms.put(STM_PND_SELECT, prepareCallables(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_TUR_SELECT, prepareCallables(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_CODER1:
			actionStms.put(STM_CD1_INSERT, prepareStatement(getSQL(STM_CD1_INSERT)));
			actionStms.put(STM_CD1_SELECT, prepareCallables(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD1_UPDATE, prepareStatement(getSQL(STM_CD1_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareCallables(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_CODER2:
			actionStms.put(STM_CD2_INSERT, prepareStatement(getSQL(STM_CD2_INSERT)));
			actionStms.put(STM_CD2_SELECT, prepareCallables(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD2_UPDATE, prepareStatement(getSQL(STM_CD2_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareCallables(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_CODER3:
			actionStms.put(STM_CD3_INSERT, prepareStatement(getSQL(STM_CD3_INSERT)));
			actionStms.put(STM_CD3_SELECT, prepareCallables(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD3_UPDATE, prepareStatement(getSQL(STM_CD3_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareCallables(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_CODER4:
			actionStms.put(STM_CD4_INSERT, prepareStatement(getSQL(STM_CD4_INSERT)));
			actionStms.put(STM_CD4_SELECT, prepareCallables(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_CD4_UPDATE, prepareStatement(getSQL(STM_CD4_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareCallables(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_DAILY:
			actionStms.put(STM_PND_SELECT, prepareCallables(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_TUR_SELECT, prepareCallables(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_DISTRIBUTE:
			actionStms.put(STM_ADD_SL_SUM, prepareCallables(getSQL(STM_ADD_SL_SUM)));
			actionStms.put(STM_CSE_SL_SUM, prepareCallables(getSQL(STM_CSE_SL_SUM)));
			actionStms.put(STM_FRZ_SL_SUM, prepareCallables(getSQL(STM_FRZ_SL_SUM)));
			break;
		case LibConstants.ACTION_EDITOR:
			actionStms.put(STM_SPM_SELECT, prepareCallables(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_ERROR:
			actionStms.put(STM_ERR_SELECT, prepareCallables(getSQL(STM_ERR_SELECT)));
			actionStms.put(STM_ERR_SL_CMT, prepareCallables(getSQL(STM_ERR_SL_CMT)));
			actionStms.put(STM_ERR_UPDATE, prepareStatement(getSQL(STM_ERR_UPDATE)));
			actionStms.put(STM_SPM_SELECT, prepareCallables(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_FACILITY:
			actionStms.put(STM_FAC_SELECT, prepareCallables(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_FAC_UPDATE, prepareStatement(getSQL(STM_FAC_UPDATE)));
			break;
		case LibConstants.ACTION_FINALS:
			actionStms.put(STM_ADD_SL_CID, prepareCallables(getSQL(STM_ADD_SL_CID)));
			actionStms.put(STM_CMT_SELECT, prepareCallables(getSQL(STM_CMT_SELECT)));
			actionStms.put(STM_FRZ_SL_SID, prepareCallables(getSQL(STM_FRZ_SL_SID)));
			actionStms.put(STM_ORD_SELECT, prepareCallables(getSQL(STM_ORD_SELECT)));
			actionStms.put(STM_SPE_SELECT, prepareCallables(getSQL(STM_SPE_SELECT)));
			break;
		case LibConstants.ACTION_FORECAST:
			actionStms.put(STM_ADD_SL_YER, prepareCallables(getSQL(STM_ADD_SL_YER)));
			actionStms.put(STM_FRZ_SL_YER, prepareCallables(getSQL(STM_FRZ_SL_YER)));
			actionStms.put(STM_SPG_SL_YER, prepareCallables(getSQL(STM_SPG_SL_YER)));
			break;
		case LibConstants.ACTION_HISTOLOGY:
			actionStms.put(STM_PND_SELECT, prepareCallables(getSQL(STM_PND_SELECT)));
			break;
		case LibConstants.ACTION_ORDERGROUP:
			actionStms.put(STM_CD1_SELECT, prepareCallables(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD2_SELECT, prepareCallables(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD3_SELECT, prepareCallables(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD4_SELECT, prepareCallables(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_ORG_INSERT, prepareStatement(getSQL(STM_ORG_INSERT)));
			actionStms.put(STM_ORG_SELECT, prepareCallables(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORG_UPDATE, prepareStatement(getSQL(STM_ORG_UPDATE)));
			actionStms.put(STM_ORT_SELECT, prepareStatement(getSQL(STM_ORT_SELECT)));
			break;
		case LibConstants.ACTION_ORDERMASTER:
			actionStms.put(STM_ORG_SELECT, prepareCallables(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORM_SELECT, prepareCallables(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_ORM_UPDATE, prepareStatement(getSQL(STM_ORM_UPDATE)));
			break;
		case LibConstants.ACTION_PENDING:
			actionStms.put(STM_PND_SELECT, prepareCallables(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_TUR_SELECT, prepareCallables(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_PERSONNEL:
			actionStms.put(STM_PRS_SELECT, prepareCallables(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_PRS_UPDATE, prepareStatement(getSQL(STM_PRS_UPDATE)));
			break;
		case LibConstants.ACTION_PROCEDURES:
			actionStms.put(STM_PRO_INSERT, prepareStatement(getSQL(STM_PRO_INSERT)));
			actionStms.put(STM_PRO_SELECT, prepareCallables(getSQL(STM_PRO_SELECT)));
			actionStms.put(STM_PRO_UPDATE, prepareStatement(getSQL(STM_PRO_UPDATE)));
			break;
		case LibConstants.ACTION_REPORT:
			actionStms.put(STM_DIA_SELECT, prepareCallables(getSQL(STM_DIA_SELECT)));
			actionStms.put(STM_STY_SL_PID, prepareCallables(getSQL(STM_STY_SL_PID)));
			actionStms.put(STM_TIS_SELECT, prepareCallables(getSQL(STM_TIS_SELECT)));
			break;
		case LibConstants.ACTION_ROUTING:
			actionStms.put(STM_PND_SL_ROU, prepareCallables(getSQL(STM_PND_SL_ROU)));
			break;
		case LibConstants.ACTION_RULES:
			actionStms.put(STM_RUL_INSERT, prepareStatement(getSQL(STM_RUL_INSERT)));
			actionStms.put(STM_RUL_SELECT, prepareCallables(getSQL(STM_RUL_SELECT)));
			actionStms.put(STM_RUL_UPDATE, prepareStatement(getSQL(STM_RUL_UPDATE)));
			break;
		case LibConstants.ACTION_SCHEDULE:
			actionStms.put(STM_PRS_SELECT, prepareCallables(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_SCH_DELETE, prepareStatement(getSQL(STM_SCH_DELETE)));
			actionStms.put(STM_SCH_INSERT, prepareStatement(getSQL(STM_SCH_INSERT)));
			actionStms.put(STM_SCH_SL_MON, prepareStatement(getSQL(STM_SCH_SL_MON)));
			actionStms.put(STM_SCH_SL_SRV, prepareCallables(getSQL(STM_SCH_SL_SRV)));
			actionStms.put(STM_SCH_SL_STA, prepareCallables(getSQL(STM_SCH_SL_STA)));
			actionStms.put(STM_SCH_UPDATE, prepareStatement(getSQL(STM_SCH_UPDATE)));
			actionStms.put(STM_SRV_SELECT, prepareCallables(getSQL(STM_SRV_SELECT)));
			actionStms.put(STM_WDY_SELECT, prepareCallables(getSQL(STM_WDY_SELECT)));
			break;
		case LibConstants.ACTION_SERVICES:
			actionStms.put(STM_SRV_INSERT, prepareStatement(getSQL(STM_SRV_INSERT)));
			actionStms.put(STM_SRV_SELECT, prepareCallables(getSQL(STM_SRV_SELECT)));
			actionStms.put(STM_SRV_UPDATE, prepareStatement(getSQL(STM_SRV_UPDATE)));
			actionStms.put(STM_SUB_SELECT, prepareCallables(getSQL(STM_SUB_SELECT)));
			break;
		case LibConstants.ACTION_SETUP:
			actionStms.put(STM_STP_SELECT, prepareCallables(getSQL(STM_STP_SELECT)));
			break;
		case LibConstants.ACTION_SPECGROUP:
			actionStms.put(STM_CD1_SELECT, prepareCallables(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD2_SELECT, prepareCallables(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD3_SELECT, prepareCallables(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD4_SELECT, prepareCallables(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_PRO_SELECT, prepareCallables(getSQL(STM_PRO_SELECT)));
			actionStms.put(STM_SPG_INSERT, prepareStatement(getSQL(STM_SPG_INSERT)));
			actionStms.put(STM_SPG_SELECT, prepareCallables(getSQL(STM_SPG_SELECT)));
			actionStms.put(STM_SPG_UPDATE, prepareStatement(getSQL(STM_SPG_UPDATE)));
			actionStms.put(STM_SUB_SELECT, prepareCallables(getSQL(STM_SUB_SELECT)));
			break;
		case LibConstants.ACTION_SPECIALTY:
			actionStms.put(STM_SPY_INSERT, prepareStatement(getSQL(STM_SPY_INSERT)));
			actionStms.put(STM_SPY_SELECT, prepareCallables(getSQL(STM_SPY_SELECT)));
			actionStms.put(STM_SPY_UPDATE, prepareStatement(getSQL(STM_SPY_UPDATE)));
			break;
		case LibConstants.ACTION_SPECIMEN:
			actionStms.put(STM_ADD_SL_SPG, prepareCallables(getSQL(STM_ADD_SL_SPG)));
			actionStms.put(STM_FRZ_SL_SPG, prepareCallables(getSQL(STM_FRZ_SL_SPG)));
			actionStms.put(STM_SPG_SL_SUM, prepareCallables(getSQL(STM_SPG_SL_SUM)));
			break;
		case LibConstants.ACTION_SPECMASTER:
			actionStms.put(STM_SPG_SELECT, prepareCallables(getSQL(STM_SPG_SELECT)));
			actionStms.put(STM_SPM_SELECT, prepareCallables(getSQL(STM_SPM_SELECT)));
			actionStms.put(STM_SPM_UPDATE, prepareStatement(getSQL(STM_SPM_UPDATE)));
			actionStms.put(STM_TUR_SELECT, prepareCallables(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_SUBSPECIAL:
			actionStms.put(STM_SPY_SELECT, prepareCallables(getSQL(STM_SPY_SELECT)));
			actionStms.put(STM_SUB_INSERT, prepareStatement(getSQL(STM_SUB_INSERT)));
			actionStms.put(STM_SUB_SELECT, prepareCallables(getSQL(STM_SUB_SELECT)));
			actionStms.put(STM_SUB_UPDATE, prepareStatement(getSQL(STM_SUB_UPDATE)));
			break;
		case LibConstants.ACTION_TURNAROUND:
			actionStms.put(STM_CSE_SL_TAT, prepareStatement(getSQL(STM_CSE_SL_TAT)));
			break;
		case LibConstants.ACTION_TURNMASTER:
			actionStms.put(STM_TUR_INSERT, prepareStatement(getSQL(STM_TUR_INSERT)));
			actionStms.put(STM_TUR_SELECT, prepareCallables(getSQL(STM_TUR_SELECT)));
			actionStms.put(STM_TUR_UPDATE, prepareStatement(getSQL(STM_TUR_UPDATE)));
			break;
		case LibConstants.ACTION_WORKDAYS:
			actionStms.put(STM_SCH_SL_SUM, prepareCallables(getSQL(STM_SCH_SL_SUM)));
			break;
		case LibConstants.ACTION_LDAYS:
			actionStms.put(STM_WDY_INSERT, prepareStatement(getSQL(STM_WDY_INSERT)));
			actionStms.put(STM_WDY_SL_LST, prepareStatement(getSQL(STM_WDY_SL_LST)));
			break;
		case LibConstants.ACTION_LFLOW:
			actionStms.put(STM_ACC_SELECT, prepareCallables(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_FAC_SELECT, prepareCallables(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_ORG_SELECT, prepareCallables(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORM_SELECT, prepareCallables(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_PRS_SELECT, prepareCallables(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_PND_DEL_FN, prepareStatement(getSQL(STM_PND_DEL_FN)));
			actionStms.put(STM_PND_DEL_ID, prepareStatement(getSQL(STM_PND_DEL_ID)));
			actionStms.put(STM_PND_INSERT, prepareStatement(getSQL(STM_PND_INSERT)));
			actionStms.put(STM_PND_SELECT, prepareCallables(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_PND_UP_EMB, prepareStatement(getSQL(STM_PND_UP_EMB)));
			actionStms.put(STM_PND_UP_FIN, prepareStatement(getSQL(STM_PND_UP_FIN)));
			actionStms.put(STM_PND_UP_GRS, prepareStatement(getSQL(STM_PND_UP_GRS)));
			actionStms.put(STM_PND_UP_MIC, prepareStatement(getSQL(STM_PND_UP_MIC)));
			actionStms.put(STM_PND_UP_ROU, prepareStatement(getSQL(STM_PND_UP_ROU)));
			actionStms.put(STM_PND_UP_SCA, prepareStatement(getSQL(STM_PND_UP_SCA)));
			actionStms.put(STM_SPG_SELECT, prepareCallables(getSQL(STM_SPG_SELECT)));
			actionStms.put(STM_SPM_SELECT, prepareCallables(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_LLOAD:
			actionStms.put(STM_ACC_SELECT, prepareCallables(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_ADD_INSERT, prepareStatement(getSQL(STM_ADD_INSERT)));
			actionStms.put(STM_ADD_SL_CID, prepareCallables(getSQL(STM_ADD_SL_CID)));
			actionStms.put(STM_ADD_SL_LST, prepareCallables(getSQL(STM_ADD_SL_LST)));
			actionStms.put(STM_ADD_SL_ORD, prepareCallables(getSQL(STM_ADD_SL_ORD)));
			actionStms.put(STM_CD1_SELECT, prepareCallables(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD2_SELECT, prepareCallables(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD3_SELECT, prepareCallables(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD4_SELECT, prepareCallables(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_CMT_INSERT, prepareStatement(getSQL(STM_CMT_INSERT)));
			actionStms.put(STM_CMT_UPDATE, prepareStatement(getSQL(STM_CMT_UPDATE)));
			actionStms.put(STM_CSE_INSERT, prepareStatement(getSQL(STM_CSE_INSERT)));
			actionStms.put(STM_CSE_SL_CID, prepareCallables(getSQL(STM_CSE_SL_CID)));
			actionStms.put(STM_CSE_SL_WLD, prepareStatement(getSQL(STM_CSE_SL_WLD)));
			actionStms.put(STM_CSE_SL_SPE, prepareCallables(getSQL(STM_CSE_SL_SPE)));
			actionStms.put(STM_CSE_UPDATE, prepareStatement(getSQL(STM_CSE_UPDATE)));
			actionStms.put(STM_ERR_DELETE, prepareStatement(getSQL(STM_ERR_DELETE)));
			actionStms.put(STM_ERR_INSERT, prepareStatement(getSQL(STM_ERR_INSERT)));
			actionStms.put(STM_ERR_SL_FXD, prepareCallables(getSQL(STM_ERR_SL_FXD)));
			actionStms.put(STM_FAC_SELECT, prepareCallables(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_FRZ_INSERT, prepareStatement(getSQL(STM_FRZ_INSERT)));
			actionStms.put(STM_FRZ_UPDATE, prepareStatement(getSQL(STM_FRZ_UPDATE)));
			actionStms.put(STM_ORD_INSERT, prepareStatement(getSQL(STM_ORD_INSERT)));
			actionStms.put(STM_ORD_UPDATE, prepareStatement(getSQL(STM_ORD_UPDATE)));
			actionStms.put(STM_ORG_SELECT, prepareCallables(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORM_SELECT, prepareCallables(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_SPE_INSERT, prepareStatement(getSQL(STM_SPE_INSERT)));
			actionStms.put(STM_SPE_SELECT, prepareCallables(getSQL(STM_SPE_SELECT)));
			actionStms.put(STM_SPE_UPDATE, prepareStatement(getSQL(STM_SPE_UPDATE)));
			actionStms.put(STM_SPG_SELECT, prepareCallables(getSQL(STM_SPG_SELECT)));
			actionStms.put(STM_SPM_SELECT, prepareCallables(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_LLOGIN:
			actionStms.put(STM_PRS_SL_PID, prepareCallables(getSQL(STM_PRS_SL_PID)));
			break;
		case LibConstants.ACTION_LSYNC:
			actionStms.put(STM_ACC_INSERT, prepareStatement(getSQL(STM_ACC_INSERT)));
			actionStms.put(STM_ACC_SELECT, prepareCallables(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_ACC_UPDATE, prepareStatement(getSQL(STM_ACC_UPDATE)));
			actionStms.put(STM_FAC_INSERT, prepareStatement(getSQL(STM_FAC_INSERT)));
			actionStms.put(STM_FAC_SELECT, prepareCallables(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_FAC_UPDATE, prepareStatement(getSQL(STM_FAC_UPDATE)));
			actionStms.put(STM_ORM_INSERT, prepareStatement(getSQL(STM_ORM_INSERT)));
			actionStms.put(STM_ORM_SELECT, prepareCallables(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_ORM_UPDATE, prepareStatement(getSQL(STM_ORM_UPDATE)));
			actionStms.put(STM_PRS_INSERT, prepareStatement(getSQL(STM_PRS_INSERT)));
			actionStms.put(STM_PRS_SELECT, prepareCallables(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_PRS_UPDATE, prepareStatement(getSQL(STM_PRS_UPDATE)));
			actionStms.put(STM_SPM_INSERT, prepareStatement(getSQL(STM_SPM_INSERT)));
			actionStms.put(STM_SPM_SELECT, prepareCallables(getSQL(STM_SPM_SELECT)));
			actionStms.put(STM_SPM_UPDATE, prepareStatement(getSQL(STM_SPM_UPDATE)));
			break;
		case LibConstants.ACTION_LVAL5:
			actionStms.put(STM_ADD_SL_SPG, prepareCallables(getSQL(STM_ADD_SL_SPG)));
			actionStms.put(STM_FRZ_SL_SU5, prepareCallables(getSQL(STM_FRZ_SL_SU5)));
			actionStms.put(STM_SPG_SL_SU5, prepareCallables(getSQL(STM_SPG_SL_SU5)));
			actionStms.put(STM_SPG_UPD_V5, prepareStatement(getSQL(STM_SPG_UPD_V5)));
			break;
		default:
		}
	}
}