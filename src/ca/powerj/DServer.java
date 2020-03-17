package ca.powerj;

class DServer extends DPowerJ {

	public DServer(LBase parent) {
		super(parent);
		dbName = "DBServer";
	}

	@Override
	void prepareBacklog() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
		pstms.put(STM_WDY_SL_NXT, prepareCallables(setSQL(STM_WDY_SL_NXT)));
		pstms.put(STM_WDY_SL_PRV, prepareCallables(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareBase() {
		pstms.put(STM_PND_SL_LST, prepareStatement(setSQL(STM_PND_SL_LST)));
		pstms.put(STM_STP_SELECT, prepareCallables(setSQL(STM_STP_SELECT)));
		pstms.put(STM_STP_SL_SID, prepareCallables(setSQL(STM_STP_SL_SID)));
		pstms.put(STM_STP_UPDATE, prepareStatement(setSQL(STM_STP_UPDATE)));
		pstms.put(STM_WDY_SL_DTE, prepareCallables(setSQL(STM_WDY_SL_DTE)));
	}

	@Override
	void prepareCasesSummary() {
		pstms.put(STM_ADD_SL_SUM, prepareCallables(setSQL(STM_ADD_SL_SUM)));
		pstms.put(STM_CSE_SL_SUM, prepareCallables(setSQL(STM_CSE_SL_SUM)));
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_FRZ_SL_SUM, prepareCallables(setSQL(STM_FRZ_SL_SUM)));
	}

	@Override
	void prepareDaily() {
		pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
		pstms.put(STM_WDY_SL_PRV, prepareCallables(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareEditor() {
		pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
	}

	@Override
	void prepareError() {
		pstms.put(STM_ERR_SELECT, prepareCallables(setSQL(STM_ERR_SELECT)));
		pstms.put(STM_ERR_SL_CMT, prepareCallables(setSQL(STM_ERR_SL_CMT)));
		pstms.put(STM_ERR_UPDATE, prepareStatement(setSQL(STM_ERR_UPDATE)));
		pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
	}

	@Override
	void prepareFinals() {
		pstms.put(STM_ADD_SL_CID, prepareCallables(setSQL(STM_ADD_SL_CID)));
		pstms.put(STM_CMT_SELECT, prepareCallables(setSQL(STM_CMT_SELECT)));
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_FRZ_SL_SID, prepareCallables(setSQL(STM_FRZ_SL_SID)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_ORD_SELECT, prepareCallables(setSQL(STM_ORD_SELECT)));
		pstms.put(STM_SPE_SELECT, prepareCallables(setSQL(STM_SPE_SELECT)));
	}

	@Override
	void prepareForecast() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_CSE_SL_YER, prepareCallables(setSQL(STM_CSE_SL_YER)));
	}

	@Override
	void prepareHistology() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_WDY_SL_PRV, prepareCallables(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareLogin() {
		pstms.put(STM_PRS_SL_PID,  prepareCallables(setSQL(STM_PRS_SL_PID)));
	}

	@Override
	void preparePending() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SELECT, prepareCallables(setSQL(STM_PND_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
	}

	@Override
	void prepareRoute() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PND_SL_ROU, prepareCallables(setSQL(STM_PND_SL_ROU)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_WDY_SL_NXT, prepareCallables(setSQL(STM_WDY_SL_NXT)));
		pstms.put(STM_WDY_SL_PRV, prepareCallables(setSQL(STM_WDY_SL_PRV)));
	}

	@Override
	void prepareSchedules(boolean canEdit) {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PRS_SELECT, prepareCallables(setSQL(STM_PRS_SELECT)));
		pstms.put(STM_SCH_SL_MON, prepareStatement(setSQL(STM_SCH_SL_MON)));
		pstms.put(STM_SCH_SL_SRV, prepareStatement(setSQL(STM_SCH_SL_SRV)));
		pstms.put(STM_SCH_SL_STA, prepareStatement(setSQL(STM_SCH_SL_STA)));
		pstms.put(STM_SRV_SELECT, prepareCallables(setSQL(STM_SRV_SELECT)));
		pstms.put(STM_WDY_SELECT, prepareCallables(setSQL(STM_WDY_SELECT)));
		if (canEdit) {
			pstms.put(STM_SCH_INSERT, prepareStatement(setSQL(STM_SCH_INSERT)));
			pstms.put(STM_SCH_UPDATE, prepareStatement(setSQL(STM_SCH_UPDATE)));
		}
	}

	@Override
	void prepareScheduleSummary() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_SCH_SL_SUM, prepareCallables(setSQL(STM_SCH_SL_SUM)));
	}

	@Override
	void prepareSpecimen() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_SPG_SL_SUM, prepareCallables(setSQL(STM_SPG_SL_SUM)));
	}

	@Override
	void prepareStpAccessions() {
		pstms.put(STM_ACC_SELECT, prepareCallables(setSQL(STM_ACC_SELECT)));
		pstms.put(STM_ACC_UPDATE, prepareStatement(setSQL(STM_ACC_UPDATE)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
	}

	@Override
	void prepareStpCoder(byte coder) {
		switch (coder) {
		case 1:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD1_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD1_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD1_UPDATE)));
			break;
		case 2:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD2_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD2_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD2_UPDATE)));
			break;
		case 3:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD3_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD3_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD3_UPDATE)));
			break;
		default:
			pstms.put(STM_CD1_INSERT, prepareStatement(setSQL(STM_CD4_INSERT)));
			pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD4_SELECT)));
			pstms.put(STM_CD1_UPDATE, prepareStatement(setSQL(STM_CD4_UPDATE)));
		}
		pstms.put(STM_RUL_SELECT, prepareCallables(setSQL(STM_RUL_SELECT)));
	}

	@Override
	void prepareStpFacilities() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_FAC_UPDATE, prepareStatement(setSQL(STM_FAC_UPDATE)));
	}

	@Override
	void prepareStpOrdGroup() {
		pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD1_SELECT)));
		pstms.put(STM_CD2_SELECT, prepareCallables(setSQL(STM_CD2_SELECT)));
		pstms.put(STM_CD3_SELECT, prepareCallables(setSQL(STM_CD3_SELECT)));
		pstms.put(STM_CD4_SELECT, prepareCallables(setSQL(STM_CD4_SELECT)));
		pstms.put(STM_ORG_INSERT, prepareStatement(setSQL(STM_ORG_INSERT)));
		pstms.put(STM_ORG_SELECT, prepareCallables(setSQL(STM_ORG_SELECT)));
		pstms.put(STM_ORG_UPDATE, prepareStatement(setSQL(STM_ORG_UPDATE)));
		pstms.put(STM_ORT_SELECT, prepareStatement(setSQL(STM_ORT_SELECT)));
	}

	@Override
	void prepareStpOrdMstr() {
		pstms.put(STM_ORG_SELECT, prepareCallables(setSQL(STM_ORG_SELECT)));
		pstms.put(STM_ORM_SELECT, prepareCallables(setSQL(STM_ORM_SELECT)));
		pstms.put(STM_ORM_UPDATE, prepareStatement(setSQL(STM_ORM_UPDATE)));
	}

	@Override
	void prepareStpPersons() {
		pstms.put(STM_PRS_SELECT, prepareCallables(setSQL(STM_PRS_SELECT)));
		pstms.put(STM_PRS_UPDATE, prepareStatement(setSQL(STM_PRS_UPDATE)));
	}

	@Override
	void prepareStpProcedures() {
		pstms.put(STM_PRO_INSERT, prepareStatement(setSQL(STM_PRO_INSERT)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_PRO_UPDATE, prepareStatement(setSQL(STM_PRO_UPDATE)));
	}

	@Override
	void prepareStpRules() {
		pstms.put(STM_RUL_INSERT, prepareStatement(setSQL(STM_RUL_INSERT)));
		pstms.put(STM_RUL_SELECT, prepareCallables(setSQL(STM_RUL_SELECT)));
		pstms.put(STM_RUL_UPDATE, prepareStatement(setSQL(STM_RUL_UPDATE)));
	}

	@Override
	void prepareStpServices() {
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_SRV_INSERT, prepareStatement(setSQL(STM_SRV_INSERT)));
		pstms.put(STM_SRV_SELECT, prepareCallables(setSQL(STM_SRV_SELECT)));
		pstms.put(STM_SRV_UPDATE, prepareStatement(setSQL(STM_SRV_UPDATE)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
	}

	@Override
	void prepareStpSpecialties() {
		pstms.put(STM_SPY_INSERT, prepareStatement(setSQL(STM_SPY_INSERT)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SPY_UPDATE, prepareStatement(setSQL(STM_SPY_UPDATE)));
	}

	@Override
	void prepareStpSpeGroup() {
		pstms.put(STM_CD1_SELECT, prepareCallables(setSQL(STM_CD1_SELECT)));
		pstms.put(STM_CD2_SELECT, prepareCallables(setSQL(STM_CD2_SELECT)));
		pstms.put(STM_CD3_SELECT, prepareCallables(setSQL(STM_CD3_SELECT)));
		pstms.put(STM_CD4_SELECT, prepareCallables(setSQL(STM_CD4_SELECT)));
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPG_INSERT, prepareStatement(setSQL(STM_SPG_INSERT)));
		pstms.put(STM_SPG_SELECT, prepareCallables(setSQL(STM_SPG_SELECT)));
		pstms.put(STM_SPG_UPDATE, prepareStatement(setSQL(STM_SPG_UPDATE)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
	}

	@Override
	void prepareStpSpeMstr() {
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPG_SELECT, prepareCallables(setSQL(STM_SPG_SELECT)));
		pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
		pstms.put(STM_SPM_UPDATE, prepareStatement(setSQL(STM_SPM_UPDATE)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
	}

	@Override
	void prepareStpSubspecialty() {
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_INSERT, prepareStatement(setSQL(STM_SUB_INSERT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
		pstms.put(STM_SUB_UPDATE, prepareStatement(setSQL(STM_SUB_UPDATE)));
	}

	@Override
	void prepareStpTurnaround() {
		pstms.put(STM_TUR_INSERT, prepareStatement(setSQL(STM_TUR_INSERT)));
		pstms.put(STM_TUR_SELECT, prepareCallables(setSQL(STM_TUR_SELECT)));
		pstms.put(STM_TUR_UPDATE, prepareStatement(setSQL(STM_TUR_UPDATE)));
	}

	@Override
	void prepareSynchronizer() {
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
	}

	@Override
	void prepareTurnaround() {
		pstms.put(STM_CSE_SL_TAT, prepareStatement(setSQL(STM_CSE_SL_TAT)));
		pstms.put(STM_FAC_SELECT, prepareCallables(setSQL(STM_FAC_SELECT)));
		pstms.put(STM_PRO_SELECT, prepareCallables(setSQL(STM_PRO_SELECT)));
		pstms.put(STM_SPY_SELECT, prepareCallables(setSQL(STM_SPY_SELECT)));
		pstms.put(STM_SUB_SELECT, prepareCallables(setSQL(STM_SUB_SELECT)));
	}

	@Override
	void prepareValue5() {
		pstms.put(STM_FRZ_SL_SU5, prepareCallables(setSQL(STM_FRZ_SL_SU5)));
		pstms.put(STM_SPG_SL_SU5, prepareCallables(setSQL(STM_SPG_SL_SU5)));
		pstms.put(STM_SPG_UPD_V5, prepareStatement(setSQL(STM_SPG_UPD_V5)));
	}

	@Override
	void prepareWorkflow() {
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
	}

	@Override
	void prepareWorkload() {
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
		pstms.put(STM_SPG_UPD_V5, prepareStatement(setSQL(STM_SPG_UPD_V5)));
		pstms.put(STM_SPM_SELECT, prepareCallables(setSQL(STM_SPM_SELECT)));
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