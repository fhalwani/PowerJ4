package ca.powerj;

class DDesktop extends DPowerJ {

	DDesktop(LBase parent) {
		super(parent);
		dbName = "DBDesktop";
	}

	@Override
	void prepareBacklog() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
		pstms.put(STM_WDY_SL_NXT, prepareStatement(setSQL(STM_WDY_SL_NXT)));
		pstms.put(STM_WDY_SL_PRV, prepareStatement(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareBase() {
		pstms.put(STM_PND_SL_LST, prepareStatement(setSQL(STM_PND_SL_LST)));
		pstms.put(STM_STP_SELECT, prepareStatement(setSQL(STM_STP_SELECT)));
		pstms.put(STM_STP_SL_SID, prepareStatement(setSQL(STM_STP_SL_SID)));
		pstms.put(STM_STP_UPDATE, prepareStatement(setSQL(STM_STP_UPDATE)));
		pstms.put(STM_WDY_SL_DTE, prepareStatement(setSQL(STM_WDY_SL_DTE)));
	}

	@Override
	void prepareCasesSummary() {
		pstms.put(STM_ADD_SL_SUM, prepareStatement(setSQL(STM_ADD_SL_SUM)));
		pstms.put(STM_CSE_SL_SUM, prepareStatement(setSQL(STM_CSE_SL_SUM)));
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_FRZ_SL_SUM, prepareStatement(setSQL(STM_FRZ_SL_SUM)));
	}

	@Override
	void prepareDaily() {
		pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
		pstms.put(STM_WDY_SL_PRV, prepareStatement(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareEditor() {
		pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
	}

	@Override
	void prepareError() {
		pstms.put(STM_ERR_SELECT, prepareStatement(setSQL(STM_ERR_SELECT)));
		pstms.put(STM_ERR_SL_CMT, prepareStatement(setSQL(STM_ERR_SL_CMT)));
		pstms.put(STM_ERR_UPDATE, prepareStatement(setSQL(STM_ERR_UPDATE)));
		pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
	}

	@Override
	void prepareFinals() {
		pstms.put(STM_ADD_SL_CID, prepareStatement(setSQL(STM_ADD_SL_CID)));
		pstms.put(STM_CMT_SELECT, prepareStatement(setSQL(STM_CMT_SELECT)));
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_FRZ_SL_SID, prepareStatement(setSQL(STM_FRZ_SL_SID)));
		pstms.put(STM_ORD_SELECT, prepareStatement(setSQL(STM_ORD_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPE_SELECT, prepareStatement(setSQL(STM_SPE_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
	}

	@Override
	void prepareForecast() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_CSE_SL_YER, prepareStatement(setSQL(STM_CSE_SL_YER)));
	}

	@Override
	void prepareHistology() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_WDY_SL_PRV, prepareStatement(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareLogin() {
		pstms.put(STM_PRS_SL_PID,  prepareStatement(setSQL(STM_PRS_SL_PID)));
	}

	@Override
	void preparePending() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SELECT, prepareStatement(setSQL(STM_PND_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
	}

	@Override
	void prepareRoute() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SL_ROU, prepareStatement(setSQL(STM_PND_SL_ROU)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_WDY_SL_NXT, prepareStatement(setSQL(STM_WDY_SL_NXT)));
		pstms.put(STM_WDY_SL_PRV, prepareStatement(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareSchedules(boolean canEdit) {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PRS_SELECT, prepareStatement(setSQL(STM_PRS_SELECT)));
		pstms.put(STM_SCH_SL_MON, prepareStatement(setSQL(STM_SCH_SL_MON)));
		pstms.put(STM_SCH_SL_SRV, prepareStatement(setSQL(STM_SCH_SL_SRV)));
		pstms.put(STM_SCH_SL_STA, prepareStatement(setSQL(STM_SCH_SL_STA)));
		pstms.put(STM_SRV_SELECT, prepareStatement(setSQL(STM_SRV_SELECT)));
		pstms.put(STM_WDY_SELECT, prepareStatement(setSQL(STM_WDY_SELECT)));
		if (canEdit) {
			pstms.put(STM_SCH_INSERT, prepareStatement(setSQL(STM_SCH_INSERT)));
			pstms.put(STM_SCH_UPDATE, prepareStatement(setSQL(STM_SCH_UPDATE)));
		}
	}

	@Override
	void prepareScheduleSummary() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_SCH_SL_SUM, prepareStatement(setSQL(STM_SCH_SL_SUM)));
	}

	@Override
	void prepareSpecimen() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_SPG_SL_SUM, prepareStatement(setSQL(STM_SPG_SL_SUM)));
	}

	@Override
	void prepareStpAccessions() {
		pstms.put(STM_ACC_SELECT, prepareStatement(setSQL(STM_ACC_SELECT)));
		pstms.put(STM_ACC_UPDATE, prepareStatement(setSQL(STM_ACC_UPDATE)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
	}

	@Override
	void prepareStpCoder(byte coder) {
		switch (coder) {
		case 1:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD1_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD1_UPDATE)));
			break;
		case 2:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD2_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD2_UPDATE)));
			break;
		case 3:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD3_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD3_UPDATE)));
			break;
		default:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD4_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD4_UPDATE)));
		}
		pstms.put(STM_RUL_SELECT, prepareStatement(setSQL(STM_RUL_SELECT)));
	}

	@Override
	void prepareStpFacilities() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_FAC_UPDATE, prepareStatement(setSQL(STM_FAC_UPDATE)));
	}

	@Override
	void prepareStpOrdGroup() {
		pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
		pstms.put(STM_CD2_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
		pstms.put(STM_CD3_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
		pstms.put(STM_CD4_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
		pstms.put(STM_ORG_INSERT, prepareStatement(setSQL(STM_ORG_INSERT)));
		pstms.put(STM_ORG_SELECT, prepareStatement(setSQL(STM_ORG_SELECT)));
		pstms.put(STM_ORG_UPDATE, prepareStatement(setSQL(STM_ORG_UPDATE)));
		pstms.put(STM_ORT_SELECT, prepareStatement(setSQL(STM_ORT_SELECT)));
	}

	@Override
	void prepareStpOrdMstr() {
		pstms.put(STM_ORG_SELECT, prepareStatement(setSQL(STM_ORG_SELECT)));
		pstms.put(STM_ORM_SELECT, prepareStatement(setSQL(STM_ORM_SELECT)));
		pstms.put(STM_ORM_UPDATE, prepareStatement(setSQL(STM_ORM_UPDATE)));
	}

	@Override
	void prepareStpPersons() {
		pstms.put(STM_PRS_SELECT, prepareStatement(setSQL(STM_PRS_SELECT)));
		pstms.put(STM_PRS_UPDATE, prepareStatement(setSQL(STM_PRS_UPDATE)));
	}

	@Override
	void prepareStpProcedures() {
		pstms.put(STM_PRO_INSERT, prepareStatement(setSQL(STM_PRO_INSERT)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_PRO_UPDATE, prepareStatement(setSQL(STM_PRO_UPDATE)));
	}

	@Override
	void prepareStpRules() {
		pstms.put(STM_RUL_INSERT, prepareStatement(setSQL(STM_RUL_INSERT)));
		pstms.put(STM_RUL_SELECT, prepareStatement(setSQL(STM_RUL_SELECT)));
		pstms.put(STM_RUL_UPDATE, prepareStatement(setSQL(STM_RUL_UPDATE)));
	}

	@Override
	void prepareStpServices() {
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_SRV_INSERT, prepareStatement(setSQL(STM_SRV_INSERT)));
		pstms.put(STM_SRV_SELECT, prepareStatement(setSQL(STM_SRV_SELECT)));
		pstms.put(STM_SRV_UPDATE, prepareStatement(setSQL(STM_SRV_UPDATE)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
	}

	@Override
	void prepareStpSpecialties() {
		pstms.put(STM_SPY_INSERT, prepareStatement(setSQL(STM_SPY_INSERT)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SPY_UPDATE, prepareStatement(setSQL(STM_SPY_UPDATE)));
	}

	@Override
	void prepareStpSpeGroup() {
		pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
		pstms.put(STM_CD2_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
		pstms.put(STM_CD3_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
		pstms.put(STM_CD4_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPG_INSERT, prepareStatement(setSQL(STM_SPG_INSERT)));
		pstms.put(STM_SPG_SELECT, prepareStatement(setSQL(STM_SPG_SELECT)));
		pstms.put(STM_SPG_UPDATE, prepareStatement(setSQL(STM_SPG_UPDATE)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
	}

	@Override
	void prepareStpSpeMstr() {
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPG_SELECT, prepareStatement(setSQL(STM_SPG_SELECT)));
		pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
		pstms.put(STM_SPM_UPDATE, prepareStatement(setSQL(STM_SPM_UPDATE)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
	}

	@Override
	void prepareStpSubspecialty() {
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_INSERT, prepareStatement(setSQL(STM_SUB_INSERT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_SUB_UPDATE, prepareStatement(setSQL(STM_SUB_UPDATE)));
	}

	@Override
	void prepareStpTurnaround() {
		pstms.put(STM_TUR_INSERT, prepareStatement(setSQL(STM_TUR_INSERT)));
		pstms.put(STM_TUR_SELECT, prepareStatement(setSQL(STM_TUR_SELECT)));
		pstms.put(STM_TUR_UPDATE, prepareStatement(setSQL(STM_TUR_UPDATE)));
	}

	@Override
	void prepareSynchronizer() {
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
	}

	@Override
	void prepareTurnaround() {
		pstms.put(STM_CSE_SL_TAT, prepareStatement(setSQL(STM_CSE_SL_TAT)));
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareStatement(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareStatement(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareStatement(setSQL(STM_SUB_SELECT)));
	}

	@Override
	void prepareValue5() {
		pstms.put(STM_FRZ_SL_SU5, prepareStatement(setSQL(STM_FRZ_SL_SU5)));
		pstms.put(STM_SPG_SL_SU5, prepareStatement(setSQL(STM_SPG_SL_SU5)));
		pstms.put(STM_SPG_UPD_V5, prepareStatement(setSQL(STM_SPG_UPD_V5)));
	}

	@Override
	void prepareWorkflow() {
		pstms.put(STM_ACC_SELECT, prepareStatement(setSQL(STM_ACC_SELECT)));
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_ORM_SELECT, prepareStatement(setSQL(STM_ORM_SELECT)));
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
		pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
	}

	@Override
	void prepareWorkload() {
		pstms.put(STM_ACC_SELECT, prepareStatement(setSQL(STM_ACC_SELECT)));
		pstms.put(STM_ADD_INSERT, prepareStatement(setSQL(STM_ADD_INSERT)));
		pstms.put(STM_ADD_SL_CID, prepareStatement(setSQL(STM_ADD_SL_CID)));
		pstms.put(STM_CD1_SELECT, prepareStatement(setSQL(STM_CD1_SELECT)));
		pstms.put(STM_CD2_SELECT, prepareStatement(setSQL(STM_CD2_SELECT)));
		pstms.put(STM_CD3_SELECT, prepareStatement(setSQL(STM_CD3_SELECT)));
		pstms.put(STM_CD4_SELECT, prepareStatement(setSQL(STM_CD4_SELECT)));
		pstms.put(STM_CMT_INSERT, prepareStatement(setSQL(STM_CMT_INSERT)));
		pstms.put(STM_CMT_UPDATE, prepareStatement(setSQL(STM_CMT_UPDATE)));
		pstms.put(STM_CSE_INSERT, prepareStatement(setSQL(STM_CSE_INSERT)));
		pstms.put(STM_CSE_SL_CID, prepareStatement(setSQL(STM_CSE_SL_CID)));
		pstms.put(STM_CSE_SL_LST, prepareStatement(setSQL(STM_CSE_SL_LST)));
		pstms.put(STM_CSE_SL_SPE, prepareStatement(setSQL(STM_CSE_SL_SPE)));
		pstms.put(STM_CSE_UPDATE, prepareStatement(setSQL(STM_CSE_UPDATE)));
		pstms.put(STM_ERR_DELETE, prepareStatement(setSQL(STM_ERR_DELETE)));
		pstms.put(STM_ERR_INSERT, prepareStatement(setSQL(STM_ERR_INSERT)));
		pstms.put(STM_ERR_SELECT, prepareStatement(setSQL(STM_ERR_SELECT)));
		pstms.put(STM_FAC_SELECT, prepareStatement(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_FRZ_INSERT, prepareStatement(setSQL(STM_FRZ_INSERT)));
		pstms.put(STM_FRZ_UPDATE, prepareStatement(setSQL(STM_FRZ_UPDATE)));
		pstms.put(STM_ORD_INSERT, prepareStatement(setSQL(STM_ORD_INSERT)));
		pstms.put(STM_ORD_UPDATE, prepareStatement(setSQL(STM_ORD_UPDATE)));
		pstms.put(STM_ORM_SELECT, prepareStatement(setSQL(STM_ORM_SELECT)));
		pstms.put(STM_SPE_INSERT, prepareStatement(setSQL(STM_SPE_INSERT)));
		pstms.put(STM_SPE_UPDATE, prepareStatement(setSQL(STM_SPE_UPDATE)));
		pstms.put(STM_SPM_SELECT, prepareStatement(setSQL(STM_SPM_SELECT)));
	}

	@Override
	void prepareWorkdays() {
		pstms.put(STM_WDY_INSERT, prepareStatement(setSQL(STM_WDY_INSERT)));
		pstms.put(STM_WDY_SL_LST, prepareStatement(setSQL(STM_WDY_SL_LST)));
	}

	@Override
	String setSQL(short id) {
		switch (id) {
		case STM_ACC_SELECT:
			return "SELECT * FROM udvAccessions ORDER BY ACNM";
		case STM_ADD_SL_CID:
			return "SELECT PRID, ADCD, ADV5, ADV1, ADV2, ADV3, ADV4, ADDT, PRNM, PRLS, PRFR, CANO " +
			"FROM udvAdditionals WHERE CAID = ? ORDER BY ADDT";
		case STM_ADD_SL_SUM:
			return "SELECT FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR, COUNT(CAID) AS ADCA, " +
			"SUM(CAST(ADV5 as INT)) AS ADV5, SUM(ADV1) AS ADV1, SUM(ADV2) AS ADV2, SUM(ADV3) AS ADV3, SUM(ADV4) AS ADV4 " +
			"FROM udvAdditionals WHERE (ADDT BETWEEN ? AND ?) " +
			"GROUP BY FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR " +
			"ORDER BY FAID, SYID, SBID, POID, PRID";
		case STM_CD1_SELECT:
			return "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder1 ORDER BY COID";
		case STM_CD2_SELECT:
			return "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder2 ORDER BY COID";
		case STM_CD3_SELECT:
			return "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder3 ORDER BY COID";
		case STM_CD4_SELECT:
			return "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder4 ORDER BY COID";
		case STM_CMT_SELECT:
			return "SELECT COM1, COM2, COM3, COM4 FROM Comments WHERE CAID = ?";
		case STM_CSE_SL_CID:
			return "SELECT CAID FROM Cases WHERE CANO = ?";
		case STM_CSE_SL_CNO:
			return "SELECT CANO FROM Cases WHERE CAID = ?";
		case STM_CSE_SL_SPE:
			return "SELECT c.SMID, c.FNED, c.CANO, s.SPID FROM Cases AS c INNER JOIN Specimens AS s ON s.CAID = c.CAID " +
					"AND s.SMID = c.SMID WHERE c.CAID = ?";
		case STM_CSE_SL_SUM:
			return "SELECT FAID, SYID, SBID, POID, FNID, FANM, SYNM, SBNM, PONM, FNNM, FNLS, FNFR, COUNT(CAID) AS CACA, " +
			"SUM(CAST(CASP as INT)) AS CASP, SUM(CAST(CABL as INT)) AS CABL, SUM(CAST(CASL as INT)) AS CASL, SUM(CAST(CAHE as INT)) AS CAHE, " +
			"SUM(CAST(CASS as INT)) AS CASS, SUM(CAST(CAIH as INT)) AS CAIH, SUM(CAST(CAMO as INT)) AS CAMO, SUM(CAST(CAFS as INT)) AS CAFS, " +
			"SUM(CAST(CASY as INT)) AS CASY, SUM(CAST(GRTA as INT)) AS GRTA, SUM(CAST(EMTA as INT)) AS EMTA, SUM(CAST(MITA as INT)) AS MITA, " +
			"SUM(CAST(ROTA as INT)) AS ROTA, SUM(CAST(FNTA as INT)) AS FNTA, SUM(CAST(CAV5 as INT)) AS CAV5, SUM(CAV1) AS CAV1, SUM(CAV2) AS CAV2, " +
			"SUM(CAV3) AS CAV3, SUM(CAV4) AS CAV4 FROM udvCases WHERE (FNED BETWEEN ? AND ?) " +
			"GROUP BY FAID, SYID, SBID, POID, FNID, FANM, SYNM, SBNM, PONM, FNNM, FNLS, FNFR " +
			"ORDER BY FAID, SYID, SBID, POID, FNID";
		case STM_ERR_SELECT:
			return "SELECT CAID, ERID, CANO FROM Errors WHERE ERID > 0 ORDER BY CANO";
		case STM_ERR_SL_CMT:
			return "SELECT ERDC FROM Errors WHERE CAID = ?";
		case STM_ERR_SL_FXD:
			return "SELECT CAID FROM Errors WHERE ERID = 0 ORDER BY CAID";
		case STM_FAC_SELECT:
			return "SELECT FAID, FAFL, FALD, FANM, FADC FROM Facilities ORDER BY FAID";
		case STM_FRZ_SL_SID:
			return "SELECT PRID, FRBL, FRSL, FRV5, FRV1, FRV2, FRV3, FRV4, PRNM, PRLS, " + 
			"PRFR, SPDC, SMNM FROM udvFrozens WHERE SPID = ?";
		case STM_FRZ_SL_SU5:
			return "SELECT COUNT(*) AS QTY, SUM(FRV1) AS FRV1, SUM(FRV2) AS FRV2, " +
					"SUM(FRV3) AS FRV3, SUM(FRV4) AS FRV4 " +
			"FROM udvFrozens WHERE WHERE ACED BETWEEN ? AND ?";
		case STM_FRZ_SL_SUM:
			return "SELECT FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR, COUNT(SPID) AS FRSP, " +
			"SUM(CAST(FRBL as INT)) AS FRBL, SUM(CAST(FRSL as INT)) AS FRSL, SUM(CAST(FRV5 as INT)) AS FRV5, SUM(FRV1) AS FRV1, SUM(FRV2) AS FRV2, " +
			"SUM(FRV3) AS FRV3, SUM(FRV4) AS FRV4 FROM udvFrozens WHERE (ACED BETWEEN ? AND ?) " +
			"GROUP BY FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR " +
			"ORDER BY FAID, SYID, SBID, POID, PRID";
		case STM_ORD_SELECT:
			return "SELECT ORQY, ORV1, ORV2, ORV3, ORV4, OGNM FROM udvOrders WHERE SPID = ? ORDER BY OGNM";
		case STM_ORG_SELECT:
			return "SELECT * FROM udvOrderGroups ORDER BY OGNM";
		case STM_ORM_SELECT:
			return "SELECT * FROM udvOrderMaster ORDER BY OMNM";
		case STM_PND_SELECT:
			return "SELECT * FROM udvPending ORDER BY PNID";
		case STM_PND_SL_ROU:
			return "SELECT * FROM udvPending WHERE ROED BETWEEN ? AND ? ORDER BY PNID";
		case STM_PRO_SELECT:
			return "SELECT POID, PONM, PODC FROM Procedures ORDER BY PONM";
		case STM_PRS_SELECT:
			return "SELECT * FROM Persons ORDER BY PRNM";
		case STM_PRS_SL_PID:
			return "SELECT PRVL FROM Persons WHERE PRID = ?";
		case STM_RUL_SELECT:
			return "SELECT RUID, RUNM, RUDC FROM Rules ORDER BY RUID";
		case STM_SCH_SL_SRV:
			return "SELECT WDID, SRID, PRID, PRNM, SRNM FROM udvSchedules WHERE (WDDT BETWEEN ? AND ?) ORDER BY SRNM, WDID";
		case STM_SCH_SL_SUM:
			return "SELECT * FROM udvSchedules WHERE (WDDT BETWEEN ? AND ?) ORDER BY FAID, PRID, WDID, SRID";
		case STM_SCH_SL_STA:
			return "SELECT WDID, SRID, PRID, PRNM, SRNM FROM udvSchedules WHERE (WDDT BETWEEN ? AND ?) ORDER BY PRNM, WDID, SRNM";
		case STM_SPE_SELECT:
			return "SELECT SPID, SMID, SPBL, SPSL, SPFR, SPHE, SPSS, SPIH, SPMO, SPV5, SPV1, SPV2, SPV3, " + 
			"SPV4, SPDC, SMNM, SMDC, PONM FROM udvSpecimens WHERE CAID = ? ORDER BY SPID";
		case STM_SPG_SELECT:
			return "SELECT * FROM udvSpeciGroups ORDER BY SGDC";
		case STM_SPG_SL_SU5:
			return "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1, " +
			"SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4 " +
			"FROM specigroups g INNER JOIN specimaster m ON g.sgid = m.sgid " +
			"INNER JOIN specimens s ON m.smid = s.smid INNER JOIN cases c ON c.caid = s.caid " +
			"WHERE c.fned BETWEEN ? AND ? " +
			"GROUP BY g.sgid";
		case STM_SPG_SL_SUM:
			return "SELECT b.syid, g.sbid, g.sgid, c.faid, c.fnid, y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm, " +
					"p.prnm, p.prls, p.prfr, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl, " +
					"SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1, " +
					"SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5 " +
					"FROM specigroups g INNER JOIN specimaster m ON g.sgid = m.sgid " +
					"INNER JOIN specimens s ON m.smid = s.smid INNER JOIN cases c ON c.caid = s.caid " +
					"INNER JOIN subspecial b ON b.sbid = g.sbid INNER JOIN specialties y ON y.syid = b.syid " +
					"INNER JOIN facilities f ON f.faid = c.faid INNER JOIN persons p ON p.prid = c.fnid " +
					"WHERE c.fned BETWEEN ? AND ? " +
					"GROUP BY b.syid, g.sbid, g.sgid, c.faid, c.fnid, y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm, p.prnm, p.prls, p.prfr " +
					"ORDER BY y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm, p.prnm, p.prls, p.prfr";
		case STM_SPM_SELECT:
			return "SELECT * FROM udvSpeciMaster ORDER BY SMNM";
		case STM_SPY_SELECT:
			return "SELECT * FROM Specialties ORDER BY SYNM";
		case STM_STP_SELECT:
			return "SELECT STID, STVA FROM Setup ORDER BY STID";
		case STM_STP_SL_SID:
			return "SELECT STVA FROM Setup WHERE STID = ?";
		case STM_SUB_SELECT:
			return "SELECT * FROM udvSubspecial ORDER BY SBNM";
		case STM_TUR_SELECT:
			return "SELECT TAID, GRSS, EMBD, MICR, ROUT, FINL, TANM FROM Turnaround ORDER BY TANM";
		case STM_WDY_SELECT:
			return "SELECT WDID, WDNO, WDTP, WDDT FROM Workdays WHERE WDDT >= ? ORDER BY WDDT";
		case STM_WDY_SL_DTE:
			return "SELECT WDNO FROM Workdays WHERE WDDT = ?";
		case STM_WDY_SL_NXT:
			return "SELECT MIN(WDDT) AS WDDT FROM Workdays WHERE WDDT > ? AND WDTP = 'D'";
		case STM_WDY_SL_PRV:
			return "SELECT MAX(WDDT) AS WDDT FROM Workdays WHERE WDDT < ? AND WDTP = 'D'";
		default:
			return super.setSQL(id);
		}
	}
}
