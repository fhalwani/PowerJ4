package ca.powerj.database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import ca.powerj.data.AccessionData;
import ca.powerj.data.AdditionalData;
import ca.powerj.data.CaseData;
import ca.powerj.data.CoderData;
import ca.powerj.data.DiagnosisData;
import ca.powerj.data.DiagnosisList;
import ca.powerj.data.ErrorData;
import ca.powerj.data.FacilityData;
import ca.powerj.data.ForecastData;
import ca.powerj.data.FrozenData;
import ca.powerj.data.ItemData;
import ca.powerj.data.OrderData;
import ca.powerj.data.OrderGroupData;
import ca.powerj.data.OrderMasterData;
import ca.powerj.data.OrganData;
import ca.powerj.data.OrganList;
import ca.powerj.data.PersonData;
import ca.powerj.data.ProcedureData;
import ca.powerj.data.ProcedureList;
import ca.powerj.data.ReportData;
import ca.powerj.data.RuleData;
import ca.powerj.data.ScheduleServiceData;
import ca.powerj.data.ScheduleStaffData;
import ca.powerj.data.ScheduleSumData;
import ca.powerj.data.ServiceData;
import ca.powerj.data.SpecialtyData;
import ca.powerj.data.SpecialtyList;
import ca.powerj.data.SpecimenCode5Data;
import ca.powerj.data.SpecimenData;
import ca.powerj.data.SpecimenGroupData;
import ca.powerj.data.SpecimenGroupSummary;
import ca.powerj.data.SpecimenMasterData;
import ca.powerj.data.StyleData;
import ca.powerj.data.SubspecialtyData;
import ca.powerj.data.SubspecialtyList;
import ca.powerj.data.TissueData;
import ca.powerj.data.TissueList;
import ca.powerj.data.TurnaroundData;
import ca.powerj.data.TurnaroundSum;
import ca.powerj.data.WorkdayData;
import ca.powerj.data.WorkloadData;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

public class DBPowerj extends DBBase {
	final byte STM_ACC_INSERT = 1;
	final byte STM_ACC_SELECT = 2;
	final byte STM_ACC_UPDATE = 3;
	final byte STM_ADD_INSERT = 4;
	final byte STM_ADD_SL_LST = 5;
	final byte STM_ADD_SL_CID = 6;
	final byte STM_ADD_SL_ORD = 7;
	final byte STM_ADD_SL_SPG = 8;
	final byte STM_ADD_SL_SUM = 9;
	final byte STM_ADD_SL_YER = 10;
	final byte STM_CD1_INSERT = 11;
	final byte STM_CD1_SELECT = 12;
	final byte STM_CD1_UPDATE = 13;
	final byte STM_CD2_INSERT = 14;
	final byte STM_CD2_SELECT = 15;
	final byte STM_CD2_UPDATE = 16;
	final byte STM_CD3_INSERT = 17;
	final byte STM_CD3_SELECT = 18;
	final byte STM_CD3_UPDATE = 19;
	final byte STM_CD4_INSERT = 20;
	final byte STM_CD4_SELECT = 21;
	final byte STM_CD4_UPDATE = 22;
	final byte STM_CMT_INSERT = 23;
	final byte STM_CMT_SELECT = 24;
	final byte STM_CMT_UPDATE = 25;
	final byte STM_CSE_INSERT = 26;
	final byte STM_CSE_SELECT = 27;
	final byte STM_CSE_SL_CID = 28;
	final byte STM_CSE_SL_CNO = 29;
	final byte STM_CSE_SL_DTE = 30;
	final byte STM_CSE_SL_SPE = 31;
	final byte STM_CSE_SL_SUM = 32;
	final byte STM_CSE_SL_TAT = 33;
	final byte STM_CSE_SL_WLD = 34;
	final byte STM_CSE_UPDATE = 35;
	final byte STM_ERR_DELETE = 36;
	final byte STM_ERR_INSERT = 37;
	final byte STM_ERR_SELECT = 38;
	final byte STM_ERR_SL_CMT = 39;
	final byte STM_ERR_SL_FXD = 40;
	final byte STM_ERR_UPDATE = 41;
	final byte STM_FAC_INSERT = 42;
	final byte STM_FAC_SELECT = 43;
	final byte STM_FAC_UPDATE = 44;
	final byte STM_FRZ_INSERT = 45;
	final byte STM_FRZ_SL_SID = 46;
	final byte STM_FRZ_SL_SPG = 47;
	final byte STM_FRZ_SL_SU5 = 48;
	final byte STM_FRZ_SL_SUM = 49;
	final byte STM_FRZ_SL_YER = 50;
	final byte STM_FRZ_UPDATE = 51;
	final byte STM_ORD_INSERT = 52;
	final byte STM_ORD_SELECT = 53;
	final byte STM_ORD_UPDATE = 54;
	final byte STM_ORG_INSERT = 55;
	final byte STM_ORG_SELECT = 56;
	final byte STM_ORG_UPDATE = 57;
	final byte STM_ORM_INSERT = 58;
	final byte STM_ORM_SELECT = 59;
	final byte STM_ORM_UPDATE = 60;
	final byte STM_ORT_INSERT = 61;
	final byte STM_ORT_SELECT = 62;
	final byte STM_PND_DEL_FN = 63;
	final byte STM_PND_DEL_ID = 64;
	final byte STM_PND_INSERT = 65;
	final byte STM_PND_SELECT = 66;
	final byte STM_PND_SL_LST = 67;
	final byte STM_PND_SL_ROU = 68;
	final byte STM_PND_UP_EMB = 69;
	final byte STM_PND_UP_FIN = 70;
	final byte STM_PND_UP_GRS = 71;
	final byte STM_PND_UP_MIC = 72;
	final byte STM_PND_UP_ROU = 73;
	final byte STM_PND_UP_SCA = 74;
	final byte STM_PRO_INSERT = 75;
	final byte STM_PRO_SELECT = 76;
	final byte STM_PRO_UPDATE = 77;
	final byte STM_PRS_INSERT = 78;
	final byte STM_PRS_SELECT = 79;
	final byte STM_PRS_SL_PID = 80;
	final byte STM_PRS_UPDATE = 81;
	final byte STM_RUL_INSERT = 82;
	final byte STM_RUL_SELECT = 83;
	final byte STM_RUL_UPDATE = 84;
	final byte STM_SCH_DELETE = 85;
	final byte STM_SCH_INSERT = 86;
	final byte STM_SCH_SL_MON = 87;
	final byte STM_SCH_SL_SRV = 88;
	final byte STM_SCH_SL_STA = 89;
	final byte STM_SCH_SL_SUM = 90;
	final byte STM_SCH_UPDATE = 91;
	final byte STM_SPE_INSERT = 92;
	final byte STM_SPE_SELECT = 93;
	final byte STM_SPE_UPDATE = 94;
	final byte STM_SPG_INSERT = 95;
	final byte STM_SPG_SELECT = 96;
	final byte STM_SPG_SL_SU5 = 97;
	final byte STM_SPG_SL_SUM = 98;
	final byte STM_SPG_SL_YER = 99;
	final byte STM_SPG_UPD_V5 = 100;
	final byte STM_SPG_UPDATE = 101;
	final byte STM_SPM_INSERT = 102;
	final byte STM_SPM_SELECT = 103;
	final byte STM_SPM_UPDATE = 104;
	final byte STM_SPY_INSERT = 105;
	final byte STM_SPY_SELECT = 106;
	final byte STM_SPY_UPDATE = 107;
	final byte STM_SRV_INSERT = 108;
	final byte STM_SRV_SELECT = 109;
	final byte STM_SRV_UPDATE = 110;
	final byte STM_STP_INSERT = 111;
	final byte STM_STP_SELECT = 112;
	final byte STM_STP_SL_SID = 113;
	final byte STM_STP_UPDATE = 114;
	final byte STM_SUB_INSERT = 115;
	final byte STM_SUB_SELECT = 116;
	final byte STM_SUB_UPDATE = 117;
	final byte STM_TUR_INSERT = 118;
	final byte STM_TUR_SELECT = 119;
	final byte STM_TUR_UPDATE = 120;
	final byte STM_WDY_INSERT = 121;
	final byte STM_WDY_SELECT = 122;
	final byte STM_WDY_SL_DTE = 123;
	final byte STM_WDY_SL_LST = 124;
	final byte STM_WDY_SL_NXT = 125;
	final byte STM_WDY_SL_PRV = 126;
	final byte STM_ORN_SELECT = 127;
	final byte STM_SBS_SELECT = 0;
	final byte STM_DIS_SELECT = -1;
	final byte STM_TIS_SELECT = -2;
	final byte STM_DIA_SELECT = -3;
	final byte STM_STY_DELETE = -4;
	final byte STM_STY_INSERT = -5;
	final byte STM_STY_SELECT = -6;
	final byte STM_STY_SL_PID = -7;
	final byte STM_STY_UPDATE = -8;
	final byte STM_UML_SL_CID = -9;
	final byte STM_TIS_SL_SID = -10;
	final byte STM_PRC_SELECT = -11;
	HashMap<Byte, PreparedStatement> systemStms = new HashMap<Byte, PreparedStatement>();
	HashMap<Byte, PreparedStatement> actionStms = new HashMap<Byte, PreparedStatement>();

	DBPowerj(LibBase base) {
		super(base);
		dbName = "DBPowerJ";
	}

	public boolean caseExists(CaseData thisCase) {
		boolean exists = false;
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_CSE_SL_SPE), 1, thisCase.getCaseID());
			rst = getResultSet(actionStms.get(STM_CSE_SL_SPE));
			while (rst.next()) {
				thisCase.setCaseNo(rst.getString("cano"));
				thisCase.setAccessed(rst.getTimestamp("fned").getTime());
				SpecimenData thisSpecimen = new SpecimenData();
				thisSpecimen.setSpecID(rst.getLong("spid"));
				thisSpecimen.setSpmID(rst.getShort("smid"));
				thisCase.setSpecimen(thisSpecimen);
				thisCase.setCodeSpec(false);
				exists = true;
				break;
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return exists;
	}

	@Override
	public void close() {
		close(systemStms);
		super.close();
	}

	public void closeStms() {
		close(actionStms);
	}

	public int deleteError(long caseID) {
		setLong(actionStms.get(STM_ERR_DELETE), 1, caseID);
		return execute(actionStms.get(STM_ERR_DELETE));
	}

	/** Sync the cache database when a case is deleted in PowerPath. **/
	public int deletePendingCancelled(long caseID) {
		setLong(actionStms.get(STM_PND_DEL_ID), 1, caseID);
		return execute(actionStms.get(STM_PND_DEL_ID));
	}

	public int deletePendingFinal(long time) {
		setTime(actionStms.get(STM_PND_DEL_FN), 1, time);
		return execute(actionStms.get(STM_PND_DEL_FN));
	}

	public int deleteSchedule(ScheduleServiceData value) {
		setShort(actionStms.get(STM_SCH_DELETE), 1, value.getSrvID());
		setInt(actionStms.get(STM_SCH_DELETE), 2, value.getWdID());
		return execute(actionStms.get(STM_SCH_DELETE));
	}

	public HashMap<Short, AccessionData> getAccessions() {
		HashMap<Short, AccessionData> list = new HashMap<Short, AccessionData>();
		AccessionData item = new AccessionData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_ACC_SELECT));
			while (rst.next()) {
				item = new AccessionData();
				item.setSpyID(rst.getByte("syid"));
				item.setCodeSpec((rst.getString("sysp").toUpperCase().equals("Y")));
				// Both Accessions and Specialties must be active
				item.setWorkflow((rst.getString("acfl").toUpperCase().equals("Y")
						&& rst.getString("syfl").toUpperCase().equals("Y")));
				item.setWorkload((rst.getString("acld").toUpperCase().equals("Y")
						&& rst.getString("syld").toUpperCase().equals("Y")));
				item.setName(rst.getString("acnm"));
				list.put(rst.getShort("acid"), item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<AccessionData> getAccessionsList() {
		ArrayList<AccessionData> list = new ArrayList<AccessionData>();
		AccessionData item = new AccessionData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_ACC_SELECT));
			while (rst.next()) {
				item = new AccessionData();
				item.setAccID(rst.getShort("acid"));
				item.setSpyID(rst.getByte("syid"));
				item.setName(rst.getString("acnm"));
				item.setWorkflow((rst.getString("acfl").equalsIgnoreCase("Y")));
				item.setWorkload((rst.getString("acld").equalsIgnoreCase("Y")));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public HashMap<Short, SpecimenCode5Data> getAdditionalCode5(long timeStart, long timeEnd) {
		HashMap<Short, SpecimenCode5Data> list = new HashMap<Short, SpecimenCode5Data>();
		SpecimenCode5Data item = new SpecimenCode5Data();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_ADD_SL_SPG), 1, timeStart);
			setTime(actionStms.get(STM_ADD_SL_SPG), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_ADD_SL_SPG));
			while (rst.next()) {
				item = new SpecimenCode5Data();
				item.setGrpID(rst.getShort("sgid"));
				item.setTotalCAP(rst.getDouble("adv1"));
				item.setTotalW2Q(rst.getDouble("adv2"));
				item.setTotalRCP(rst.getDouble("adv3"));
				item.setTotalCPT(rst.getDouble("adv4"));
				list.put(rst.getShort("sgid"), item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ForecastData> getAdditionalForecast(long timeStart, long timeEnd) {
		ArrayList<ForecastData> list = new ArrayList<ForecastData>();
		ForecastData item = new ForecastData();
		boolean exists = false;
		short yearID = 0;
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_ADD_SL_YER), 1, timeStart);
			setTime(actionStms.get(STM_ADD_SL_YER), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_ADD_SL_YER));
			while (rst.next()) {
				exists = false;
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).getFacID() == rst.getShort("faid") && list.get(i).getSpyID() == rst.getByte("syid")
							&& list.get(i).getSubID() == rst.getByte("sbid") && list.get(i).getProID() == rst.getByte("poid")
							&& list.get(i).getSpgID() == rst.getShort("sgid")) {
						item = list.get(i);
						exists = true;
						break;
					}
				}
				if (!exists) {
					item = new ForecastData();
					item.setFacID(rst.getShort("faid"));
					item.setSpgID(rst.getShort("sgid"));
					item.setSpyID(rst.getByte("syid"));
					item.setSubID(rst.getByte("sbid"));
					item.setProID(rst.getByte("poid"));
					item.setFacName(rst.getString("fanm"));
					item.setSpyName(rst.getString("synm"));
					item.setSubName(rst.getString("sbnm"));
					item.setProName(rst.getString("ponm"));
					item.setSpgName(rst.getString("sgdc"));
					list.add(item);
				}
				yearID = rst.getShort("yearid");
				item.setNoSpecs(yearID, 0);
				item.setNoBlocks(yearID, 0);
				item.setNoSlides(yearID, 0);
				item.setFte1(yearID, rst.getDouble("adv1"));
				item.setFte2(yearID, rst.getDouble("adv2"));
				item.setFte3(yearID, rst.getDouble("adv3"));
				item.setFte4(yearID, rst.getDouble("adv4"));
				item.setFte5(yearID, rst.getDouble("adv5"));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<SpecimenGroupSummary> getAdditionalSpecimens(long timeStart, long timeTo) {
		ArrayList<SpecimenGroupSummary> list = new ArrayList<SpecimenGroupSummary>();
		SpecimenGroupSummary item = new SpecimenGroupSummary();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_ADD_SL_SPG), 1, timeStart);
			setTime(actionStms.get(STM_ADD_SL_SPG), 2, timeTo);
			rst = getResultSet(actionStms.get(STM_ADD_SL_SPG));
			while (rst.next()) {
				item = new SpecimenGroupSummary();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setProID(rst.getByte("poid"));
				item.setFacID(rst.getShort("faid"));
				item.setSpgID(rst.getShort("sgid"));
				item.setFte1(rst.getDouble("adv1"));
				item.setFte2(rst.getDouble("adv2"));
				item.setFte3(rst.getDouble("adv3"));
				item.setFte4(rst.getDouble("adv4"));
				item.setFte5(rst.getDouble("adv5"));
				item.setFacName(rst.getString("fanm"));
				item.setSpyName(rst.getString("synm"));
				item.setSubName(rst.getString("sbnm"));
				item.setProName(rst.getString("ponm"));
				item.setSpgName(rst.getString("sgdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<WorkloadData> getAdditionalSums(long timeStart, long timeTo) {
		ArrayList<WorkloadData> list = new ArrayList<WorkloadData>();
		WorkloadData item = new WorkloadData();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_ADD_SL_SUM), 1, timeStart);
			setTime(actionStms.get(STM_ADD_SL_SUM), 2, timeTo);
			rst = getResultSet(actionStms.get(STM_ADD_SL_SUM));
			while (rst.next()) {
				item = new WorkloadData();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setFacID(rst.getShort("faid"));
				item.setPrsID(rst.getShort("prid"));
				item.setFte1(rst.getDouble("adv1"));
				item.setFte2(rst.getDouble("adv2"));
				item.setFte3(rst.getDouble("adv3"));
				item.setFte4(rst.getDouble("adv4"));
				item.setFte5(rst.getDouble("adv5"));
				item.setFacName(rst.getString("fanm"));
				item.setSpyName(rst.getString("synm"));
				item.setSubName(rst.getString("sbnm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<AdditionalData> getCaseAdditionals(long caseID) {
		ArrayList<AdditionalData> list = new ArrayList<AdditionalData>();
		AdditionalData item = new AdditionalData();
		final String[] codes = {"NIL", "AMND", "ADDN", "CORR", "REVW"};
		byte codeID = 0;
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_ADD_SL_CID), 1, caseID);
			rst = getResultSet(actionStms.get(STM_ADD_SL_CID));
			while (rst.next()) {
				if (rst.getShort("adcd") > 4) {
					// Reviews
					codeID = 4;
				} else if (rst.getShort("adcd") < 0) {
					codeID = 0;
				} else {
					codeID = rst.getByte("adcd");
				}
				item = new AdditionalData();
				item.setCode(codes[codeID]);
				item.setValue5(rst.getInt("adv5"));
				item.setValue1(rst.getDouble("adv1"));
				item.setValue2(rst.getDouble("adv2"));
				item.setValue3(rst.getDouble("adv3"));
				item.setValue4(rst.getDouble("adv4"));
				item.setFinalName(rst.getString("prnm"));
				item.setFinalFull(rst.getString("prls") + ", " + rst.getString("prfr").substring(0, 1));
				item.getFinaled().setTimeInMillis(rst.getTimestamp("addt").getTime());
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public String getCaseComment(long caseID) {
		String value = "";
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_CMT_SELECT), 1, caseID);
			rst = getResultSet(actionStms.get(STM_CMT_SELECT));
			while (rst.next()) {
				if (rst.getString("com1") != null && rst.getString("com1").length() > 2) {
					value = rst.getString("com1");
				}
				if (rst.getString("com2") != null && rst.getString("com2").length() > 2) {
					value += rst.getString("com2");
				}
				if (rst.getString("com3") != null && rst.getString("com3").length() > 2) {
					value += rst.getString("com3");
				}
				if (rst.getString("com4") != null && rst.getString("com4").length() > 2) {
					value += rst.getString("com4");
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return value;
	}

	public ArrayList<CaseData> getCases(String filter) {
		ArrayList<CaseData> list = new ArrayList<CaseData>();
		CaseData item = new CaseData();
		String sql = getSQL(STM_CSE_SELECT).replaceFirst(" _WHERE_", filter);
		ResultSet rst = null;
		try {
			rst = getResultSet(sql);
			while (rst.next()) {
				item = new CaseData();
				item.setNoSynop(rst.getByte("casy"));
				item.setNoSpecs(rst.getByte("casp"));
				item.setNoFSSpec(rst.getByte("cafs"));
				item.setNoBlocks(rst.getShort("cabl"));
				item.setNoSlides(rst.getShort("casl"));
				item.setNoHE(rst.getShort("cahe"));
				item.setNoSS(rst.getShort("cass"));
				item.setNoIHC(rst.getShort("caih"));
				item.setNoMol(rst.getShort("camo"));
				item.setGrossTAT(rst.getShort("grta"));
				item.setEmbedTAT(rst.getShort("emta"));
				item.setMicroTAT(rst.getShort("mita"));
				item.setRouteTAT(rst.getShort("rota"));
				item.setFinalTAT(rst.getShort("fnta"));
				item.setValue5(rst.getInt("cav5"));
				item.setCaseID(rst.getLong("caid"));
				item.setValue1(rst.getDouble("cav1"));
				item.setValue2(rst.getDouble("cav2"));
				item.setValue3(rst.getDouble("cav3"));
				item.setValue4(rst.getDouble("cav4"));
				item.setAccessed(rst.getTimestamp("aced").getTime());
				item.setGrossed(rst.getTimestamp("gred").getTime());
				item.setEmbeded(rst.getTimestamp("emed").getTime());
				item.setMicroed(rst.getTimestamp("mied").getTime());
				item.setRouted(rst.getTimestamp("roed").getTime());
				item.setFinaled(rst.getTimestamp("fned").getTime());
				item.setCaseNo(rst.getString("cano"));
				item.setFacName(rst.getString("fanm"));
				item.setSpyName(rst.getString("synm"));
				item.setSubName(rst.getString("sbnm"));
				item.setProcName(rst.getString("ponm"));
				item.setSpecName(rst.getString("smnm"));
				item.setGrossName(rst.getString("GRNM"));
				item.setEmbedName(rst.getString("EMNM"));
				item.setMicroName(rst.getString("MINM"));
				item.setRouteName(rst.getString("RONM"));
				item.setFinalName(rst.getString("fnnm"));
				item.setGrossFull(rst.getString("GRFR").trim() + " " + rst.getString("GRLS").trim());
				item.setEmbedFull(rst.getString("EMFR").trim() + " " + rst.getString("EMLS").trim());
				item.setMicroFull(rst.getString("MIFR").trim() + " " + rst.getString("MILS").trim());
				item.setRouteFull(rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim());
				item.setFinalFull(rst.getString("fnfr").trim() + " " + rst.getString("fnls").trim());
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<WorkloadData> getCaseSums(boolean showName, short loginID, long timeStart, long timeTo) {
		ArrayList<WorkloadData> list = new ArrayList<WorkloadData>();
		WorkloadData item = new WorkloadData();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_CSE_SL_SUM), 1, timeStart);
			setTime(actionStms.get(STM_CSE_SL_SUM), 2, timeTo);
			rst = getResultSet(actionStms.get(STM_CSE_SL_SUM));
			while (rst.next()) {
				item = new WorkloadData();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setFacID(rst.getShort("faid"));
				item.setPrsID(rst.getShort("fnid"));
				item.setNoCases(rst.getInt("caca"));
				item.setNoSpecs(rst.getInt("casp"));
				item.setNoBlocks(rst.getInt("cabl"));
				item.setNoSlides(rst.getInt("casl"));
				item.setFte1(rst.getDouble("cav1"));
				item.setFte2(rst.getDouble("cav2"));
				item.setFte3(rst.getDouble("cav3"));
				item.setFte4(rst.getDouble("cav4"));
				item.setFte5(rst.getDouble("cav5"));
				item.setFacName(rst.getString("fanm"));
				item.setSpyName(rst.getString("synm"));
				item.setSubName(rst.getString("sbnm"));
				if (showName || item.getPrsID() == loginID) {
					// Hide names except the current user
					item.setPrsName(rst.getString("fnnm"));
					item.setPrsFull(rst.getString("fnfr").trim() + " " + rst.getString("fnls").trim());
				}
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public void getCaseUMLS(long caseID, short userID, ReportData reportData) {
		// Must override in DBDesktop & DBServer
	}

	public ArrayList<CoderData> getCoder(byte index) {
		ArrayList<CoderData> list = new ArrayList<CoderData>();
		CoderData coder = new CoderData();
		ResultSet rst = null;
		try {
			switch (index) {
			case LibConstants.ACTION_CODER1:
				rst = getResultSet(actionStms.get(STM_CD1_SELECT));
				break;
			case LibConstants.ACTION_CODER2:
				rst = getResultSet(actionStms.get(STM_CD2_SELECT));
				break;
			case LibConstants.ACTION_CODER3:
				rst = getResultSet(actionStms.get(STM_CD3_SELECT));
				break;
			case LibConstants.ACTION_CODER4:
				rst = getResultSet(actionStms.get(STM_CD4_SELECT));
				break;
			default:
			}
			while (rst.next()) {
				coder = new CoderData();
				coder.setCodeID(rst.getShort("coid"));
				coder.setRuleID(rst.getShort("ruid"));
				coder.setCount(rst.getShort("coqy"));
				coder.setValueA(rst.getDouble("cov1"));
				coder.setValueB(rst.getDouble("cov2"));
				coder.setValueC(rst.getDouble("cov3"));
				coder.setName(rst.getString("conm").trim());
				coder.setDescr(rst.getString("codc").trim());
				list.add(coder);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public DiagnosisList getDiagnosis(short prsID, short orgID) {
		DiagnosisList list = new DiagnosisList();
		DiagnosisData item;
		ResultSet rst = null;
		try {
			setShort(actionStms.get(STM_DIA_SELECT), 1, prsID);
			setShort(actionStms.get(STM_DIA_SELECT), 2, orgID);
			rst = getResultSet(actionStms.get(STM_TIS_SELECT));
			while (rst.next()) {
				item = new DiagnosisData(rst.getInt("diid"),
					rst.getByte("dsid"), rst.getString("dgcx"),
					rst.getString("dinm"), rst.getString("dgdc"),
					rst.getString("midc"), rst.getString("dgdc2"),
					rst.getString("midc2"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ItemData> getDiseases() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_DIS_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				list.add(new ItemData(rst.getByte("dsid"), rst.getString("dsnm")));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public String getErrorComment(long caseID) {
		String value = "";
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_ERR_SL_CMT), 1, caseID);
			rst = getResultSet(actionStms.get(STM_ERR_SL_CMT));
			while (rst.next()) {
				if (rst.getString("erdc") != null) {
					value = rst.getString("erdc");
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return value;
	}

	public ArrayList<Long> getErrorFixed() {
		ArrayList<Long> list = new ArrayList<Long>();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_ERR_SL_FXD));
			while (rst.next()) {
				list.add(rst.getLong("caid"));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ErrorData> getErrorPending() {
		ArrayList<ErrorData> list = new ArrayList<ErrorData>();
		ErrorData item = new ErrorData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_ERR_SELECT));
			while (rst.next()) {
				item = new ErrorData();
				item.setCaseID(rst.getLong("caid"));
				item.setErrID(rst.getByte("erid"));
				item.setCaseNo(rst.getString("cano"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<FacilityData> getFacilities(boolean getAll) {
		ArrayList<FacilityData> list = new ArrayList<FacilityData>();
		FacilityData item = new FacilityData();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_FAC_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				item = new FacilityData();
				item.setWorkflow((rst.getString("fafl").equalsIgnoreCase("Y")));
				item.setWorkload((rst.getString("fald").equalsIgnoreCase("Y")));
				if (getAll || item.isWorkflow() || item.isWorkload()) {
					item.setFacID(rst.getShort("faid"));
					item.setName(rst.getString("fanm"));
					item.setDescr(rst.getString("fadc"));
					list.add(item);
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public SpecimenCode5Data getFrozenCode5(long timeStart, long timeEnd) {
		SpecimenCode5Data value = new SpecimenCode5Data();
		ResultSet rst = null;
		try {
			value.setGrpID((short) 9999);
			setTime(actionStms.get(STM_FRZ_SL_SU5), 1, timeStart);
			setTime(actionStms.get(STM_FRZ_SL_SU5), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_FRZ_SL_SU5));
			while (rst.next()) {
				value.setQty(value.getQty() + rst.getInt("qty"));
				value.setTotalCAP(value.getTotalCAP() + rst.getDouble("frv1"));
				value.setTotalW2Q(value.getTotalW2Q() + rst.getDouble("frv2"));
				value.setTotalRCP(value.getTotalRCP() + rst.getDouble("frv3"));
				value.setTotalCPT(value.getTotalCPT() + rst.getDouble("frv4"));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return value;
	}

	public ArrayList<ForecastData> getFrozenForecast(long timeStart, long timeEnd) {
		ArrayList<ForecastData> list = new ArrayList<ForecastData>();
		ForecastData item = new ForecastData();
		boolean exists = false;
		short yearID = 0;
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_FRZ_SL_YER), 1, timeStart);
			setTime(actionStms.get(STM_FRZ_SL_YER), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_FRZ_SL_YER));
			while (rst.next()) {
				exists = false;
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).getFacID() == rst.getShort("faid") && list.get(i).getSpyID() == rst.getByte("syid")
							&& list.get(i).getSubID() == rst.getByte("sbid") && list.get(i).getProID() == rst.getByte("poid")
							&& list.get(i).getSpgID() == rst.getShort("sgid")) {
						item = list.get(i);
						exists = true;
						break;
					}
				}
				if (!exists) {
					item = new ForecastData();
					item.setFacID(rst.getShort("faid"));
					item.setSpgID(rst.getShort("sgid"));
					item.setSpyID(rst.getByte("syid"));
					item.setSubID(rst.getByte("sbid"));
					item.setProID(rst.getByte("poid"));
					item.setFacName(rst.getString("fanm"));
					item.setSpyName(rst.getString("synm"));
					item.setSubName(rst.getString("sbnm"));
					item.setProName(rst.getString("ponm"));
					item.setSpgName(rst.getString("sgdc"));
					list.add(item);
				}
				yearID = rst.getShort("yearid");
				item.setNoSpecs(yearID, rst.getInt("frsp"));
				item.setNoBlocks(yearID, rst.getInt("frbl"));
				item.setNoSlides(yearID, rst.getInt("frsl"));
				item.setFte1(yearID, rst.getDouble("frv1"));
				item.setFte2(yearID, rst.getDouble("frv2"));
				item.setFte3(yearID, rst.getDouble("frv3"));
				item.setFte4(yearID, rst.getDouble("frv4"));
				item.setFte5(yearID, rst.getDouble("frv5"));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<SpecimenGroupSummary> getFrozenSpecimens(long timeStart, long timeTo) {
		ArrayList<SpecimenGroupSummary> list = new ArrayList<SpecimenGroupSummary>();
		SpecimenGroupSummary item = new SpecimenGroupSummary();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_FRZ_SL_SUM), 1, timeStart);
			setTime(actionStms.get(STM_FRZ_SL_SUM), 2, timeTo);
			rst = getResultSet(actionStms.get(STM_FRZ_SL_SUM));
			while (rst.next()) {
				item = new SpecimenGroupSummary();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setProID(rst.getByte("poid"));
				item.setFacID(rst.getShort("faid"));
				item.setSpgID(rst.getShort("sgid"));
				item.setNoSpecs(rst.getInt("frsp"));
				item.setNoBlocks(rst.getInt("frbl"));
				item.setNoSlides(rst.getInt("frsl"));
				item.setFte1(rst.getDouble("frv1"));
				item.setFte2(rst.getDouble("frv2"));
				item.setFte3(rst.getDouble("frv3"));
				item.setFte4(rst.getDouble("frv4"));
				item.setFte5(rst.getDouble("frv5"));
				item.setFacName(rst.getString("fanm"));
				item.setSpyName(rst.getString("synm"));
				item.setSubName(rst.getString("sbnm"));
				item.setProName(rst.getString("ponm"));
				item.setSpgName(rst.getString("sgdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<WorkloadData> getFrozenSums(boolean showName, short loginID, long timeStart, long timeTo) {
		ArrayList<WorkloadData> list = new ArrayList<WorkloadData>();
		WorkloadData item = new WorkloadData();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_FRZ_SL_SUM), 1, timeStart);
			setTime(actionStms.get(STM_FRZ_SL_SUM), 2, timeTo);
			rst = getResultSet(actionStms.get(STM_FRZ_SL_SUM));
			while (rst.next()) {
				item = new WorkloadData();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setFacID(rst.getShort("faid"));
				item.setPrsID(rst.getShort("prid"));
				item.setFte1(rst.getDouble("frv1"));
				item.setFte2(rst.getDouble("frv2"));
				item.setFte3(rst.getDouble("frv3"));
				item.setFte4(rst.getDouble("frv4"));
				item.setFte5(rst.getDouble("frv5"));
				item.setFacName(rst.getString("fanm"));
				item.setSpyName(rst.getString("synm"));
				item.setSubName(rst.getString("sbnm"));
				if (showName || item.getPrsID() == loginID) {
					// Hide names except the current user
					item.setPrsName(rst.getString("prnm"));
					item.setPrsFull(rst.getString("prfr").trim() + " " + rst.getString("prls").trim());
				}
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public long getLastAdditional(short codeID) {
		setShort(actionStms.get(STM_ADD_SL_LST), 1, codeID);
		return getTime(actionStms.get(STM_ADD_SL_LST));
	}

	public long getLastOrder() {
		return getTime(actionStms.get(STM_ADD_SL_ORD));
	}

	public long getLastPending() {
		long accession = getTime(systemStms.get(STM_PND_SL_LST));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -60);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		if (cal.getTimeInMillis() < accession) {
			return accession;
		} else {
			return cal.getTimeInMillis();
		}
	}

	public WorkdayData getLastWorkday() {
		WorkdayData value = new WorkdayData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_WDY_SL_LST));
			while (rst.next()) {
				value.setDayID(rst.getInt("wdid"));
				value.setDayNo(rst.getInt("wdno"));
				value.setDate(rst.getDate("wddt").getTime());
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return value;
	}

	public long getLastWorkload() {
		return getTime(actionStms.get(STM_CSE_SL_WLD));
	}

	public int getLoginAccess(short userID) {
		setStatements(LibConstants.ACTION_LLOGIN);
		setShort(actionStms.get(STM_PRS_SL_PID), 1, userID);
		int i = getInt(actionStms.get(STM_PRS_SL_PID));
		closeStms();
		return i;
	}

	public ArrayList<OrderGroupData> getOrderGroups() {
		ArrayList<OrderGroupData> list = new ArrayList<OrderGroupData>();
		OrderGroupData item = new OrderGroupData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_ORG_SELECT));
			while (rst.next()) {
				item = new OrderGroupData();
				item.setGrpID(rst.getShort("ogid"));
				item.setTypeID(rst.getByte("otid"));
				item.setValue1(rst.getShort("ogc1"));
				item.setValue2(rst.getShort("ogc2"));
				item.setValue3(rst.getShort("ogc3"));
				item.setValue4(rst.getShort("ogc4"));
				item.setValue5(rst.getShort("ogc5"));
				item.setName(rst.getString("ognm"));
				item.setDescr(rst.getString("ogdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<OrderMasterData> getOrderMasters() {
		ArrayList<OrderMasterData> list = new ArrayList<OrderMasterData>();
		OrderMasterData item = new OrderMasterData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_ORM_SELECT));
			while (rst.next()) {
				item = new OrderMasterData();
				item.setOrdID(rst.getShort("omid"));
				item.setGrpID(rst.getShort("ogid"));
				item.setName(rst.getString("omnm"));
				item.setDescr(rst.getString("omdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ItemData> getOrderTypes() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		for (byte i = 1; i < LibConstants.ORDER_TYPE_STRINGS.length; i++) {
			list.add(new ItemData(i, LibConstants.ORDER_TYPE_STRINGS[i]));
		}
		return list;
	}

	public OrganList getOrgans() {
		OrganList list = new OrganList();
		OrganData item = new OrganData();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_ORN_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				item = new OrganData(rst.getByte("syid"), rst.getByte("sbid"),
						rst.getByte("onid"), rst.getShort("ssoid"), rst.getString("onnm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public ArrayList<CaseData> getPendings(byte statusID) {
		ArrayList<CaseData> list = new ArrayList<CaseData>();
		CaseData item = new CaseData();
		final String[] statuses = LibConstants.STATUS_STRINGS;
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_PND_SELECT));
			while (rst.next()) {
				if (statusID == LibConstants.STATUS_ALL || rst.getByte("pnst") < statusID) {
					item = new CaseData();
					item.setStatusID(rst.getByte("pnst"));
					item.setSpyID(rst.getByte("syid"));
					item.setSubID(rst.getByte("sbid"));
					item.setProcID(rst.getByte("poid"));
					item.setNoSpecs(rst.getByte("pnsp"));
					item.setTurnaroundID(rst.getByte("taid"));
					item.setFacID(rst.getShort("faid"));
					item.setNoBlocks(rst.getShort("pnbl"));
					item.setNoSlides(rst.getShort("pnsl"));
					item.setMainSpec(rst.getShort("smid"));
					item.setValue5(rst.getInt("pnv5"));
					item.setCaseID(rst.getLong("pnid"));
					item.setCaseNo(rst.getString("pnno"));
					item.setFacName(rst.getString("fanm"));
					item.setSpyName(rst.getString("synm"));
					item.setSubName(rst.getString("sbnm"));
					item.setProcName(rst.getString("ponm"));
					item.setSpecName(rst.getString("smnm"));
					item.setStatusName(statuses[item.getStatusID()]);
					item.setAccessed(rst.getTimestamp("aced").getTime());
					if (item.getStatusID() > LibConstants.STATUS_ACCES) {
						item.setGrossed(rst.getTimestamp("gred").getTime());
						item.setGrossID(rst.getShort("grid"));
						item.setGrossTAT(rst.getShort("grta"));
						item.setGrossName(rst.getString("GRNM"));
						item.setGrossFull(rst.getString("GRFR").trim() + " " + rst.getString("GRLS").trim());
					}
					if (item.getStatusID() > LibConstants.STATUS_GROSS) {
						item.setEmbeded(rst.getTimestamp("emed").getTime());
						item.setEmbedID(rst.getShort("emid"));
						item.setEmbedTAT(rst.getShort("emta"));
						item.setEmbedName(rst.getString("EMNM"));
						item.setEmbedFull(rst.getString("EMFR").trim() + " " + rst.getString("EMLS").trim());
					}
					if (item.getStatusID() > LibConstants.STATUS_EMBED) {
						item.setMicroed(rst.getTimestamp("mied").getTime());
						item.setMicroID(rst.getShort("miid"));
						item.setMicroTAT(rst.getShort("mita"));
						item.setMicroName(rst.getString("MINM"));
						item.setMicroFull(rst.getString("MIFR").trim() + " " + rst.getString("MILS").trim());
					}
					if (item.getStatusID() > LibConstants.STATUS_MICRO) {
						item.setRouted(rst.getTimestamp("roed").getTime());
						item.setRouteID(rst.getShort("roid"));
						item.setRouteTAT(rst.getShort("rota"));
						item.setRouteName(rst.getString("RONM"));
						item.setRouteFull(rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim());
						item.setFinalID(rst.getShort("fnid"));
						item.setFinalTAT(rst.getShort("fnta"));
						item.setFinalName(rst.getString("fnnm"));
						item.setFinalFull(rst.getString("fnfr").trim() + " " + rst.getString("fnls").trim());
					}
					if (item.getStatusID() > LibConstants.STATUS_ROUTE) {
						item.setFinaled(rst.getTimestamp("fned").getTime());
					}
					list.add(item);
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<PersonData> getPersons() {
		ArrayList<PersonData> list = new ArrayList<PersonData>();
		PersonData item = new PersonData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_PRS_SELECT));
			while (rst.next()) {
				item = new PersonData();
				item.setPrsID(rst.getShort("prid"));
				item.setActive(rst.getString("prac").equalsIgnoreCase("Y"));
				item.setAccess(rst.getInt("prvl"));
				item.setCode(rst.getString("prcd"));
				item.setFirstname(rst.getString("prfr"));
				item.setLastname(rst.getString("prls"));
				item.setInitials(rst.getString("prnm"));
				item.setBits(base.numbers.intToBoolean(rst.getInt("prvl")));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ProcedureList getProcedures(boolean isFilter) {
		ProcedureList list = new ProcedureList(isFilter);
		ProcedureData item;
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_PRO_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				item = new ProcedureData(rst.getByte("poid"),
					rst.getString("ponm"), rst.getString("podc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public ProcedureList getProcs() {
		ProcedureList list = new ProcedureList(false);
		ProcedureData item;
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_PRC_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				item = new ProcedureData(rst.getByte("poid"),
						rst.getByte("syid"), rst.getString("ponm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public ArrayList<CaseData> getRouting(long timeStart, long timeEnd) {
		ArrayList<CaseData> list = new ArrayList<CaseData>();
		CaseData item = new CaseData();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_PND_SL_ROU), 1, timeStart);
			setTime(actionStms.get(STM_PND_SL_ROU), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_PND_SL_ROU));
			while (rst.next()) {
				if (rst.getShort("fnid") > 0) {
					// else, not assigned yet
					item = new CaseData();
					item.setSpyID(rst.getByte("syid"));
					item.setSubID(rst.getByte("sbid"));
					item.setNoSpecs(rst.getByte("pnsp"));
					item.setFinalID(rst.getShort("fnid"));
					item.setFacID(rst.getShort("faid"));
					item.setNoBlocks(rst.getShort("pnbl"));
					item.setNoSlides(rst.getShort("pnsl"));
					item.setValue5(rst.getInt("pnv5"));
					item.setCaseNo(rst.getString("pnno"));
					item.setFacName(rst.getString("fanm"));
					item.setSpyName(rst.getString("synm"));
					item.setSubName(rst.getString("sbnm"));
					item.setProcName(rst.getString("ponm"));
					item.setSpecName(rst.getString("smnm"));
					item.setRouteName(rst.getString("RONM"));
					item.setFinalName(rst.getString("fnnm"));
					item.setRouteFull(rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim());
					item.setFinalFull(rst.getString("fnfr").trim() + " " + rst.getString("fnls").trim());
					item.setAccessed(rst.getTimestamp("aced").getTime());
					item.setRouted(rst.getTimestamp("roed").getTime());
					list.add(item);
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<RuleData> getRules() {
		ArrayList<RuleData> list = new ArrayList<RuleData>();
		RuleData item;
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_RUL_SELECT));
			while (rst.next()) {
				item = new RuleData(rst.getShort("ruid"),
					rst.getString("runm"),
					rst.getString("rudc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<Long> getScheduleDates() {
		ArrayList<Long> list = new ArrayList<Long>();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_SCH_SL_MON));
			while (rst.next()) {
				list.add(rst.getDate("wddt").getTime());
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ScheduleServiceData> getScheduleServices(long timeStart, long timeEnd) {
		ArrayList<ScheduleServiceData> list = new ArrayList<ScheduleServiceData>();
		ScheduleServiceData item = new ScheduleServiceData();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_SCH_SL_SRV), 1, timeStart);
			setTime(actionStms.get(STM_SCH_SL_SRV), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_SCH_SL_SRV));
			while (rst.next()) {
				item = new ScheduleServiceData();
				item.setSrvID(rst.getShort("srid"));
				item.setWdID(rst.getInt("wdid"));
				item.setPerson(rst.getShort("prid"), rst.getString("prnm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ScheduleStaffData> getScheduleStaff(long timeStart, long timeEnd) {
		ArrayList<ScheduleStaffData> list = new ArrayList<ScheduleStaffData>();
		ScheduleStaffData item = new ScheduleStaffData();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_SCH_SL_STA), 1, timeStart);
			setTime(actionStms.get(STM_SCH_SL_STA), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_SCH_SL_STA));
			while (rst.next()) {
				item = new ScheduleStaffData();
				item.setPrsID(rst.getShort("prid"));
				item.setWdID(rst.getInt("wdid"));
				item.setName(rst.getString("prnm"));
				item.setService(0, rst.getString("srnm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ScheduleSumData> getScheduleSums(long timeStart, long timeEnd) {
		ArrayList<ScheduleSumData> list = new ArrayList<ScheduleSumData>();
		ScheduleSumData item = new ScheduleSumData();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_SCH_SL_SUM), 1, timeStart);
			setTime(actionStms.get(STM_SCH_SL_SUM), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_SCH_SL_SUM));
			while (rst.next()) {
				item = new ScheduleSumData();
				item.setFacID(rst.getShort("faid"));
				item.setPrsID(rst.getShort("prid"));
				item.setSubID(rst.getShort("sbid"));
				item.setSrvID(rst.getShort("srid"));
				item.setDayID(rst.getInt("wdid"));
				item.setPrsName(rst.getString("prnm"));
				item.setSrvName(rst.getString("srnm"));
				item.setSubName(rst.getString("sbnm"));
				item.setPrsFull(rst.getString("prfr"), rst.getString("prls"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ServiceData> getServices() {
		ArrayList<ServiceData> list = new ArrayList<ServiceData>();
		ServiceData item = new ServiceData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_SRV_SELECT));
			while (rst.next()) {
				item = new ServiceData();
				item.setSrvID(rst.getByte("srid"));
				item.setSubID(rst.getByte("sbid"));
				item.setFacID(rst.getShort("faid"));
				item.setCodeID(rst.getShort("srcd"));
				item.setName(rst.getString("srnm"));
				item.setDescr(rst.getString("srdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public String getSetup(byte id) {
		setByte(systemStms.get(STM_STP_SL_SID), 1, id);
		return getString(systemStms.get(STM_STP_SL_SID));
	}

	public ArrayList<ItemData> getSetups() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_STP_SELECT));
			while (rst.next()) {
				list.add(new ItemData(rst.getShort("stid"), rst.getString("stva").trim()));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public SpecialtyList getSpecialties(boolean isFilter) {
		SpecialtyList list = new SpecialtyList(isFilter);
		SpecialtyData item = new SpecialtyData();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_SPY_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				item = new SpecialtyData(rst.getByte("syid"),
					(rst.getString("syfl").equalsIgnoreCase("Y")),
					(rst.getString("syld").equalsIgnoreCase("Y")),
					(rst.getString("sysp").equalsIgnoreCase("Y")),
					rst.getString("synm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public ArrayList<FrozenData> getSpecimenFrozens(long specID) {
		ArrayList<FrozenData> list = new ArrayList<FrozenData>();
		FrozenData item = new FrozenData();
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_FRZ_SL_SID), 1, specID);
			rst = getResultSet(actionStms.get(STM_FRZ_SL_SID));
			while (rst.next()) {
				item = new FrozenData();
				item.setNoBlocks(rst.getShort("frbl"));
				item.setNoSlides(rst.getShort("frsl"));
				item.setValue5(rst.getInt("frv5"));
				item.setValue1(rst.getDouble("frv1"));
				item.setValue2(rst.getDouble("frv2"));
				item.setValue3(rst.getDouble("frv3"));
				item.setValue4(rst.getDouble("frv4"));
				item.setFinalBy(rst.getString("prnm"));
				item.setName(rst.getString("prls") + ", "
						+ rst.getString("prfr").substring(0, 1));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public HashMap<Short, SpecimenCode5Data> getSpecimenGroupCode5(long timeStart, long timeEnd) {
		HashMap<Short, SpecimenCode5Data> list = new HashMap<Short, SpecimenCode5Data>();
		SpecimenCode5Data item = new SpecimenCode5Data();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_SPG_SL_SU5), 1, timeStart);
			setTime(actionStms.get(STM_SPG_SL_SU5), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_SPG_SL_SU5));
			while (rst.next()) {
				item = new SpecimenCode5Data();
				item.setGrpID(rst.getShort("sgid"));
				item.setQty(rst.getInt("QTY"));
				item.setTotalCAP(rst.getDouble("spv1"));
				item.setTotalW2Q(rst.getDouble("spv2"));
				item.setTotalRCP(rst.getDouble("spv3"));
				item.setTotalCPT(rst.getDouble("spv4"));
				item.setName(rst.getString("sgdc"));
				list.put(rst.getShort("sgid"), item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<ForecastData> getSpecimenGroupForecast(long timeStart, long timeEnd) {
		ArrayList<ForecastData> list = new ArrayList<ForecastData>();
		ForecastData item = new ForecastData();
		boolean exists = false;
		short yearID = 0;
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_SPG_SL_YER), 1, timeStart);
			setTime(actionStms.get(STM_SPG_SL_YER), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_SPG_SL_YER));
			while (rst.next()) {
				exists = false;
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).getFacID() == rst.getShort("faid") && list.get(i).getSpyID() == rst.getByte("syid")
							&& list.get(i).getSubID() == rst.getByte("sbid") && list.get(i).getProID() == rst.getByte("poid")
							&& list.get(i).getSpgID() == rst.getShort("sgid")) {
						item = list.get(i);
						exists = true;
						break;
					}
				}
				if (!exists) {
					item = new ForecastData();
					item.setFacID(rst.getShort("faid"));
					item.setSpgID(rst.getShort("sgid"));
					item.setSpyID(rst.getByte("syid"));
					item.setSubID(rst.getByte("sbid"));
					item.setProID(rst.getByte("poid"));
					item.setFacName(rst.getString("fanm"));
					item.setSpyName(rst.getString("synm"));
					item.setSubName(rst.getString("sbnm"));
					item.setProName(rst.getString("ponm"));
					item.setSpgName(rst.getString("sgdc"));
					list.add(item);
				}
				yearID = rst.getShort("yearid");
				item.setNoSpecs(yearID, rst.getInt("qty"));
				item.setNoBlocks(yearID, rst.getInt("spbl"));
				item.setNoSlides(yearID, rst.getInt("spsl"));
				item.setFte1(yearID, rst.getDouble("spv1"));
				item.setFte2(yearID, rst.getDouble("spv2"));
				item.setFte3(yearID, rst.getDouble("spv3"));
				item.setFte4(yearID, rst.getDouble("spv4"));
				item.setFte5(yearID, (double) rst.getInt("spv5"));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<SpecimenGroupData> getSpecimenGroups() {
		ArrayList<SpecimenGroupData> list = new ArrayList<SpecimenGroupData>();
		SpecimenGroupData item = new SpecimenGroupData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_SPG_SELECT));
			while (rst.next()) {
				item = new SpecimenGroupData();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setProID(rst.getByte("poid"));
				item.setGrpID(rst.getShort("sgid"));
				item.setValue5(rst.getInt("sgv5"));
				item.setDescr(rst.getString("sgdc"));
				item.setProcedure(rst.getString("ponm"));
				item.setSpecialty(rst.getString("synm"));
				item.setSubspecial(rst.getString("sbnm"));
				item.setHasLN((rst.getString("sgln").toUpperCase().equals("Y")));
				item.setCode(0, 0, rst.getShort("sg1b"), rst.getString("C1NB"));
				item.setCode(0, 1, rst.getShort("sg2b"), rst.getString("C2NB"));
				item.setCode(0, 2, rst.getShort("sg3b"), rst.getString("C3NB"));
				item.setCode(0, 3, rst.getShort("sg4b"), rst.getString("C4NB"));
				item.setCode(1, 0, rst.getShort("sg1m"), rst.getString("C1NM"));
				item.setCode(1, 1, rst.getShort("sg2m"), rst.getString("C2NM"));
				item.setCode(1, 2, rst.getShort("sg3m"), rst.getString("C3NM"));
				item.setCode(1, 3, rst.getShort("sg4m"), rst.getString("C4NM"));
				item.setCode(2, 0, rst.getShort("sg1r"), rst.getString("C1NR"));
				item.setCode(2, 1, rst.getShort("sg2r"), rst.getString("C2NR"));
				item.setCode(2, 2, rst.getShort("sg3r"), rst.getString("C3NR"));
				item.setCode(2, 3, rst.getShort("sg4r"), rst.getString("C4NR"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<SpecimenGroupSummary> getSpecimenGroupSums(long timeStart, long timeEnd) {
		ArrayList<SpecimenGroupSummary> list = new ArrayList<SpecimenGroupSummary>();
		SpecimenGroupSummary item = new SpecimenGroupSummary();
		ResultSet rst = null;
		try {
			setTime(actionStms.get(STM_SPG_SL_SUM), 1, timeStart);
			setTime(actionStms.get(STM_SPG_SL_SUM), 2, timeEnd);
			rst = getResultSet(actionStms.get(STM_SPG_SL_SUM));
			while (rst.next()) {
				item = new SpecimenGroupSummary();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setProID(rst.getByte("poid"));
				item.setFacID(rst.getShort("faid"));
				item.setSpgID(rst.getShort("sgid"));
				item.setNoSpecs(rst.getInt("qty"));
				item.setNoBlocks(rst.getInt("spbl"));
				item.setNoSlides(rst.getInt("spsl"));
				item.setNoHE(rst.getInt("sphe"));
				item.setNoSS(rst.getInt("spss"));
				item.setNoIHC(rst.getInt("spih"));
				item.setFte1(rst.getDouble("spv1"));
				item.setFte2(rst.getDouble("spv2"));
				item.setFte3(rst.getDouble("spv3"));
				item.setFte4(rst.getDouble("spv4"));
				item.setFte5(rst.getDouble("spv5"));
				item.setFacName(rst.getString("fanm"));
				item.setSpyName(rst.getString("synm"));
				item.setSubName(rst.getString("sbnm"));
				item.setProName(rst.getString("ponm"));
				item.setSpgName(rst.getString("sgdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<SpecimenMasterData> getSpecimenMasters() {
		ArrayList<SpecimenMasterData> list = new ArrayList<SpecimenMasterData>();
		SpecimenMasterData item = new SpecimenMasterData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_SPM_SELECT));
			while (rst.next()) {
				item = new SpecimenMasterData();
				item.setSpmID(rst.getShort("smid"));
				item.setSpgID(rst.getShort("sgid"));
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setTurID(rst.getByte("taid"));
				item.setProID(rst.getByte("poid"));
				item.setName(rst.getString("smnm"));
				item.setDescr(rst.getString("smdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<OrderData> getSpecimenOrders(long specID) {
		ArrayList<OrderData> list = new ArrayList<OrderData>();
		OrderData item = new OrderData();
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_ORD_SELECT), 1, specID);
			rst = getResultSet(actionStms.get(STM_ORD_SELECT));
			while (rst.next()) {
				item = new OrderData();
				item.setQty(rst.getShort("orqy"));
				item.setValue1(rst.getDouble("orv1"));
				item.setValue2(rst.getDouble("orv2"));
				item.setValue3(rst.getDouble("orv3"));
				item.setValue4(rst.getDouble("orv4"));
				item.setName(rst.getString("ognm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<SpecimenData> getSpecimens(long caseID) {
		ArrayList<SpecimenData> list = new ArrayList<SpecimenData>();
		SpecimenData item = new SpecimenData();
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_SPE_SELECT), 1, caseID);
			rst = getResultSet(actionStms.get(STM_SPE_SELECT));
			while (rst.next()) {
				item = new SpecimenData();
				item.setNoBlocks(rst.getShort("spbl"));
				item.setNoSlides(rst.getShort("spsl"));
				item.setNoHE(rst.getShort("sphe"));
				item.setNoSS(rst.getShort("spss"));
				item.setNoIHC(rst.getShort("spih"));
				item.setNoMOL(rst.getShort("spmo"));
				item.setNoFrags(rst.getShort("spfr"));
				item.setValue5(rst.getInt("spv5"));
				item.setSpecID(rst.getLong("spid"));
				item.setValue1(rst.getDouble("spv1"));
				item.setValue2(rst.getDouble("spv2"));
				item.setValue3(rst.getDouble("spv3"));
				item.setValue4(rst.getDouble("spv4"));
				item.setName(rst.getString("smnm"));
				item.setDescr(rst.getString("spdc"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public void getSpecimenTissues(short userID, ReportData reportData, ArrayList<SpecimenData> specimens) {
		// Must override in DBDesktop & DBServer
	}

	public String getSQL(short id) {
		return getSQL2(id).replaceAll("<pjschema>", base.getProperty("pjSche"));
	}

	public String getSQL2(short id) {
		switch (id) {
		case STM_ACC_INSERT:
			return "INSERT INTO <pjschema>.accessions (syid, acfl, acld, acnm, acid) VALUES (?, ?, ?, ?, ?)";
		case STM_ACC_UPDATE:
			return "UPDATE <pjschema>.accessions SET syid = ?, acfl = ?, acld = ?, acnm = ? WHERE acid = ?";
		case STM_ADD_INSERT:
			return "INSERT INTO <pjschema>.additionals (caid, prid, adcd, addt, adv1, adv2, adv3, adv4, adv5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_ADD_SL_ORD:
			return "SELECT addt FROM <pjschema>.udvaddlastorder";
		case STM_CD1_INSERT:
			return "INSERT INTO <pjschema>.coder1 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD1_UPDATE:
			return "UPDATE <pjschema>.coder1 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CD2_INSERT:
			return "INSERT INTO <pjschema>.coder2 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD2_UPDATE:
			return "UPDATE <pjschema>.coder2 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CD3_INSERT:
			return "INSERT INTO <pjschema>.coder3 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD3_UPDATE:
			return "UPDATE <pjschema>.coder3 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CD4_INSERT:
			return "INSERT INTO <pjschema>.coder4 (ruid, coqy, cov1, cov2, cov3, conm, codc, coid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CD4_UPDATE:
			return "UPDATE <pjschema>.coder4 SET ruid = ?, coqy = ?, cov1 = ?, cov2 = ?, cov3 = ?, conm = ?, codc = ? WHERE coid = ?";
		case STM_CMT_INSERT:
			return "INSERT INTO <pjschema>.comments (com1, com2, com3, com4, caid) VALUES (?, ?, ?, ?, ?)";
		case STM_CMT_UPDATE:
			return "UPDATE <pjschema>.comments set com1 = ?, com2 = ?, com3 = ?, com4 = ? WHERE caid = ?";
		case STM_CSE_INSERT:
			return "INSERT INTO <pjschema>.cases (faid, sbid, smid, grid, emid, miid, roid, fnid, grta, emta, mita, "
					+ "rota, fnta, casp, cabl, casl, casy, cafs, cahe, cass, caih, camo, cav5, aced, gred, emed, mied, roed, "
					+ "fned, cav1, cav2, cav3, cav4, cano, caid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_CSE_SL_WLD:
			return "SELECT fned FROM <pjschema>.udvcaseslast";
		case STM_CSE_SL_TAT:
			return "SELECT * FROM <pjschema>.udvcasesta ORDER BY faid, syid, sbid, poid, fnyear, fnmonth";
		case STM_CSE_UPDATE:
			return "UPDATE <pjschema>.cases SET faid = ?, sbid = ?, smid = ?, grid = ?, emid = ?, miid = ?, roid = ?, fnid = ?, "
					+ "grta = ?, emta = ?, mita = ?, rota = ?, fnta = ?, casp = ?, cabl = ?, casl = ?, casy = ?, cafs = ?, cahe = ?, cass = ?, "
					+ "caih = ?, camo = ?, cav5 = ?, aced = ?, gred = ?, emed = ?, mied = ?, roed = ?, fned = ?, cav1 = ?, cav2 = ?, cav3 = ?, "
					+ "cav4 = ?, cano = ? WHERE caid = ?";
		case STM_ERR_DELETE:
			return "DELETE FROM <pjschema>.errors WHERE caid = ?";
		case STM_ERR_INSERT:
			return "INSERT INTO <pjschema>.errors (caid, erid, cano, erdc) VALUES (?, ?, ?, ?)";
		case STM_ERR_UPDATE:
			return "UPDATE <pjschema>.errors SET erid = 0 WHERE caid = ?";
		case STM_FAC_INSERT:
			return "INSERT INTO <pjschema>.facilities (fafl, fald, fanm, fadc, faid) VALUES (?, ?, ?, ?, ?)";
		case STM_FAC_UPDATE:
			return "UPDATE <pjschema>.facilities SET fafl = ?, fald = ?, fanm = ?, fadc = ? WHERE faid = ?";
		case STM_FRZ_INSERT:
			return "INSERT INTO <pjschema>.frozens (frbl, frsl, prid, frv5, frv1, frv2, frv3, frv4, spid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_FRZ_UPDATE:
			return "UPDATE <pjschema>.frozens SET frbl = ?, frsl = ?, prid = ?, frv5 = ?, frv1 = ?, frv2 = ?, frv3 = ?, frv4 = ? WHERE spid = ?";
		case STM_ORD_INSERT:
			return "INSERT INTO <pjschema>.orders (orqy, orv1, orv2, orv3, orv4, ogid, spid) VALUES (?, ?, ?, ?, ?, ?, ?)";
		case STM_ORD_UPDATE:
			return "UPDATE <pjschema>.orders SET orqy = ?, orv1 = ?, orv2 = ?, orv3 = ?, orv4 = ? WHERE ogid = ? AND spid = ?";
		case STM_ORG_INSERT:
			return "INSERT INTO <pjschema>.ordergroups (otid, ogc1, ogc2, ogc3, ogc4, ogc5, ognm, ogdc, ogid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_ORG_UPDATE:
			return "UPDATE <pjschema>.ordergroups SET otid = ?, ogc1 = ?, ogc2 = ?, ogc3 = ?, ogc4 = ?, ogc5 = ?, ognm = ?, ogdc = ? WHERE ogid = ?";
		case STM_ORM_INSERT:
			return "INSERT INTO <pjschema>.ordermaster (ogid, omnm, omdc, omid) VALUES (?, ?, ?, ?)";
		case STM_ORM_UPDATE:
			return "UPDATE <pjschema>.ordermaster SET ogid = ?, omnm = ?, omdc = ? WHERE omid = ?";
		case STM_ORT_INSERT:
			return "INSERT INTO <pjschema>.ordertypes (otid, otnm) VALUES (?, ?)";
		case STM_ORT_SELECT:
			return "SELECT otid, otnm FROM <pjschema>.ordertypes ORDER BY otnm";
		case STM_PRC_SELECT:
			return "SELECT poid, syid, ponm FROM <pjschema>.procs ORDER BY ponm";
		case STM_PND_DEL_FN:
			return "DELETE FROM <pjschema>.pending WHERE pnst = 6 AND fned < ?";
		case STM_PND_DEL_ID:
			return "DELETE FROM <pjschema>.pending WHERE pnid = ?";
		case STM_PND_INSERT:
			return "INSERT INTO <pjschema>.pending (faid, sbid, poid, smid, grid, emid, miid, roid, fnid, grta, emta, "
					+ "mita, rota, fnta, pnst, pnsp, pnbl, pnsl, pnv5, aced, gred, emed, mied, roed, fned, pnno, pnid) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_PND_SL_LST:
			return "SELECT aced FROM <pjschema>.udvpendinglast";
		case STM_PND_UP_EMB:
			return "UPDATE <pjschema>.pending SET sbid = ?, poid = ?, smid = ?, emid = ?, emta = ?, pnst = ?, pnsp = ?, "
					+ "pnbl = ?, pnv5 = ?, emed = ? WHERE pnid = ?";
		case STM_PND_UP_FIN:
			return "UPDATE <pjschema>.pending SET fnid = ?, fnta = ?, pnst = ?, pnbl = ?, pnsl = ?, fned = ? WHERE pnid = ?";
		case STM_PND_UP_GRS:
			return "UPDATE <pjschema>.pending SET sbid = ?, poid = ?, smid = ?, grid = ?, grta = ?, pnst = ?, pnsp = ?, "
					+ "pnbl = ?, pnv5 = ?, gred = ? WHERE pnid = ?";
		case STM_PND_UP_MIC:
			return "UPDATE <pjschema>.pending SET miid = ?, mita = ?, pnst = ?, pnbl = ?, pnsl = ?, mied = ? WHERE pnid = ?";
		case STM_PND_UP_ROU:
			return "UPDATE <pjschema>.pending SET roid = ?, rota = ?, pnst = ?, pnbl = ?, pnsl = ?, roed = ? WHERE pnid = ?";
		case STM_PND_UP_SCA:
			return "UPDATE <pjschema>.pending SET fnid = ?, rota = ?, pnst = ?, pnbl = ?, pnsl = ?, roed = ? WHERE pnid = ?";
		case STM_PRO_INSERT:
			return "INSERT INTO <pjschema>.procedures (ponm, podc, poid) VALUES (?, ?, ?)";
		case STM_PRO_UPDATE:
			return "UPDATE <pjschema>.procedures SET ponm = ?, podc = ? WHERE poid = ?";
		case STM_PRS_INSERT:
			return "INSERT INTO <pjschema>.persons (prvl, prdt, prcd, prac, prnm, prls, prfr, prid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_PRS_UPDATE:
			return "UPDATE <pjschema>.persons SET prvl = ?, prdt = ?, prcd = ?, prac = ?, prnm = ?, prls = ?, prfr = ? WHERE prid = ?";
		case STM_RUL_INSERT:
			return "INSERT INTO <pjschema>.rules (runm, rudc, ruid) VALUES (?, ?, ?)";
		case STM_RUL_UPDATE:
			return "UPDATE <pjschema>.rules SET runm = ?, rudc = ? WHERE ruid = ?";
		case STM_SCH_DELETE:
			return "DELETE FROM <pjschema>.schedules WHERE srid = ? AND wdid = ?";
		case STM_SCH_INSERT:
			return "INSERT INTO <pjschema>.schedules (prid, srid, wdid) VALUES (?, ?, ?)";
		case STM_SCH_SL_MON:
			return "SELECT wddt FROM <pjschema>.udvschedweeks ORDER BY wddt";
		case STM_SCH_UPDATE:
			return "UPDATE <pjschema>.schedules SET prid = ? WHERE srid = ? AND wdid = ?";
		case STM_SPE_INSERT:
			return "INSERT INTO <pjschema>.specimens (caid, smid, spbl, spsl, spfr, sphe, spss, spih, spmo, spv5, spv1, spv2, "
					+ "spv3, spv4, spdc, spid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPE_UPDATE:
			return "UPDATE <pjschema>.specimens SET caid = ?, smid = ?, spbl = ?, spsl = ?, spfr = ?, sphe = ?, "
					+ "spss = ?, spih = ?, spmo = ?, spv5 = ?, spv1 = ?, spv2 = ?, spv3 = ?, spv4 = ?, spdc = ? WHERE spid = ?";
		case STM_SPG_INSERT:
			return "INSERT INTO <pjschema>.specigroups (sbid, poid, sg1b, sg1m, sg1r, sg2b, sg2m, sg2r, sg3b, sg3m, sg3r, sg4b, sg4m, sg4r, "
					+ "sgv5, sgln, sgdc, sgid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		case STM_SPG_UPDATE:
			return "UPDATE <pjschema>.specigroups SET sbid = ?, poid = ?, sg1b = ?, sg1m = ?, sg1r = ?, sg2b = ?, sg2m = ?, sg2r = ?, "
					+ "sg3b = ?, sg3m = ?, sg3r = ?, sg4b = ?, sg4m = ?, sg4r = ?, sgv5 = ?, sgln = ?, sgdc = ? WHERE sgid = ?";
		case STM_SPG_UPD_V5:
			return "UPDATE <pjschema>.specigroups SET sgv5 = ? WHERE sgid = ?";
		case STM_SPM_INSERT:
			return "INSERT INTO <pjschema>.specimaster (sgid, taid, smnm, smdc, smid) VALUES (?, ?, ?, ?, ?)";
		case STM_SPM_UPDATE:
			return "UPDATE <pjschema>.specimaster SET sgid = ?, taid = ?, smnm = ?, smdc = ? WHERE smid = ?";
		case STM_SPY_INSERT:
			return "INSERT INTO <pjschema>.specialties (syfl, syld, sysp, synm, syid) VALUES (?, ?, ?, ?, ?)";
		case STM_SPY_UPDATE:
			return "UPDATE <pjschema>.specialties SET syfl = ?, syld = ?, sysp = ?, synm = ? WHERE syid = ?";
		case STM_SRV_INSERT:
			return "INSERT INTO <pjschema>.services (faid, sbid, srcd, srnm, srdc, srid) VALUES (?, ?, ?, ?, ?, ?)";
		case STM_SRV_SELECT:
			return "SELECT * FROM <pjschema>.udvservices ORDER BY srnm";
		case STM_SRV_UPDATE:
			return "UPDATE <pjschema>.services SET faid = ?, sbid = ?, srcd = ?, srnm = ?, srdc = ? WHERE srid = ?";
		case STM_STP_INSERT:
			return "INSERT INTO <pjschema>.setup (stva, stid) VALUES (?, ?)";
		case STM_STP_UPDATE:
			return "UPDATE <pjschema>.setup SET stva = ? WHERE stid = ?";
		case STM_STY_INSERT:
			return "INSERT INTO <pjschema>.styles (cadi, calo, cami, capr, casp, cati, lidi, lisp, liti, font, "
					+ "tgdi, tgmi, tgsp, tgti, tgsl, txsp, txsl, prid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?)";
		case STM_STY_UPDATE:
			return "UPDATE <pjschema>.styles set cadi = ?, calo = ?, cami = ?, capr = ?, casp = ?, cati = ?, "
					+ "lidi = ?, lisp = ?, liti = ?, font = ?, tgdi = ?, tgmi = ?, tgsp = ?, tgti = ?, tgsl = ?, "
					+ "txsp = ?, txsl = ? WHERE prid = ?";
		case STM_SUB_INSERT:
			return "INSERT INTO <pjschema>.subspecial (syid, sbnm, sbdc, sbid) VALUES (?, ?, ?, ?)";
		case STM_SUB_UPDATE:
			return "UPDATE <pjschema>.subspecial SET syid = ?, sbnm = ?, sbdc = ? WHERE sbid = ?";
		case STM_TUR_INSERT:
			return "INSERT INTO <pjschema>.turnaround (grss, embd, micr, rout, finl, tanm, taid) VALUES (?, ?, ?, ?, ?, ?, ?)";
		case STM_TUR_UPDATE:
			return "UPDATE <pjschema>.turnaround SET grss = ?, embd = ?, micr = ?, rout = ?, finl = ?, tanm = ? WHERE taid = ?";
		case STM_WDY_INSERT:
			return "INSERT INTO <pjschema>.workdays (wddt, wdtp, wdno, wdid) VALUES (?, ?, ?, ?)";
		case STM_WDY_SL_LST:
			return "SELECT * FROM <pjschema>.udvworkdaylast";
		default:
			return null;
		}
	}

	public void getStyle(short prsID, StyleData style) {
		ResultSet rst = null;
		try {
			setShort(actionStms.get(STM_STY_SL_PID), 1, prsID);
			rst = getResultSet(actionStms.get(STM_STY_SL_PID));
			while (rst.next()) {
				style.setStyle(rst.getByte("slid"), rst.getByte("cadi"), rst.getByte("calo"),
					rst.getByte("cami"), rst.getByte("capo"), rst.getByte("casp"),
					rst.getByte("cati"), rst.getByte("lidi"), rst.getByte("lisp"),
					rst.getByte("liti"), rst.getString("font"), rst.getString("tgdi"),
					rst.getString("tgmi"), rst.getString("tgsp"), rst.getString("tgti"),
					rst.getString("tgsl"), rst.getString("txsp"), rst.getString("txsl"));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
	}

	public ArrayList<ItemData> getStyles() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		ItemData item;
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_STY_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				item = new ItemData(
						rst.getShort("prid"),
						rst.getString("prnm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public ArrayList<ItemData> getSubs() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_SBS_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				list.add(new ItemData(rst.getByte("sbid"), rst.getString("sbnm")));
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public SubspecialtyList getSubspecialties(boolean isFilter) {
		SubspecialtyList list = new SubspecialtyList(isFilter);
		SubspecialtyData item = new SubspecialtyData();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = prepareStatement(getSQL(STM_SUB_SELECT));
			rst = getResultSet(stm);
			while (rst.next()) {
				item = new SubspecialtyData(
						rst.getByte("sbid"),
						rst.getByte("syid"),
						rst.getString("sbnm"),
						rst.getString("sbdc"),
						rst.getString("synm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
			close(stm);
		}
		return list;
	}

	public TissueList getTissues(short prsID, byte spyID, short orgID) {
		TissueList list = new TissueList();
		TissueData item;
		ResultSet rst = null;
		try {
			setShort(actionStms.get(STM_TIS_SELECT), 1, prsID);
			setByte(actionStms.get(STM_TIS_SELECT), 2, spyID);
			setShort(actionStms.get(STM_TIS_SELECT), 3, orgID);
			rst = getResultSet(actionStms.get(STM_TIS_SELECT));
			while (rst.next()) {
				item = new TissueData(rst.getShort("tiid"),
						rst.getShort("poid"), rst.getString("ticx"), 
						rst.getString("tinm"), rst.getString("tidc"),
						rst.getString("podc"), rst.getString("tidc2"),
						rst.getString("podc2"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<TurnaroundData> getTurnarounds() {
		ArrayList<TurnaroundData> list = new ArrayList<TurnaroundData>();
		TurnaroundData item = new TurnaroundData();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_TUR_SELECT));
			while (rst.next()) {
				item = new TurnaroundData();
				item.setTurID(rst.getByte("taid"));
				item.setGross(rst.getShort("grss"));
				item.setEmbed(rst.getShort("embd"));
				item.setMicrotomy(rst.getShort("micr"));
				item.setRoute(rst.getShort("rout"));
				item.setDiagnosis(rst.getShort("finl"));
				item.setName(rst.getString("tanm"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public ArrayList<TurnaroundSum> getTurnaroundSum() {
		ArrayList<TurnaroundSum> list = new ArrayList<TurnaroundSum>();
		TurnaroundSum item = new TurnaroundSum();
		ResultSet rst = null;
		try {
			rst = getResultSet(actionStms.get(STM_CSE_SL_TAT));
			while (rst.next()) {
				item = new TurnaroundSum();
				item.setSpyID(rst.getByte("syid"));
				item.setSubID(rst.getByte("sbid"));
				item.setProID(rst.getByte("poid"));
				item.setMonth(rst.getByte("fnmonth"));
				item.setYear(rst.getShort("fnyear"));
				item.setFacID(rst.getShort("faid"));
				item.setQty(rst.getInt("CASES"));
				item.setGross(rst.getInt("grta"));
				item.setEmbed(rst.getInt("emta"));
				item.setMicro(rst.getInt("mita"));
				item.setRoute(rst.getInt("rota"));
				item.setDiagn(rst.getInt("fnta"));
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public long getWorkdayNext(long date) {
		setDate(systemStms.get(STM_WDY_SL_NXT), 1, date);
		return getTime(systemStms.get(STM_WDY_SL_NXT));
	}

	public int getWorkdayNo(long date) {
		setDate(systemStms.get(STM_WDY_SL_DTE), 1, date);
		return getInt(systemStms.get(STM_WDY_SL_DTE));
	}

	public long getWorkdayPrevious(long date) {
		setDate(systemStms.get(STM_WDY_SL_PRV), 1, date);
		return getTime(systemStms.get(STM_WDY_SL_PRV));
	}

	public ArrayList<WorkdayData> getWorkdays(long date, int noDays) {
		ArrayList<WorkdayData> list = new ArrayList<WorkdayData>();
		WorkdayData item = new WorkdayData();
		ResultSet rst = null;
		try {
			setDate(actionStms.get(STM_WDY_SELECT), 1, date);
			rst = getResultSet(actionStms.get(STM_WDY_SELECT));
			while (rst.next() && list.size() < noDays) {
				item = new WorkdayData();
				item.setDayID(rst.getInt("wdid"));
				item.setType(rst.getString("wdtp"));
				item.setDate(rst.getDate("wddt").getTime());
				list.add(item);
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return list;
	}

	public boolean isDuplicate(short codeID, CaseData value) {
		boolean duplicate = false;
		ResultSet rst = null;
		try {
			setLong(actionStms.get(STM_ADD_SL_CID), 1, value.getCaseID());
			rst = getResultSet(actionStms.get(STM_ADD_SL_CID));
			while (rst.next()) {
				if (codeID == rst.getShort("adcd")) {
					if (value.getFinalID() == rst.getShort("prid")) {
						if ((value.getFinalTime() / 86400000)
								- (value.getAccessTime() / 86400000) < 1) {
							// No duplicates on the same day
							duplicate = true;
							break;
						}
						if (value.getAccessTime() < rst.getTimestamp("addt").getTime()) {
							value.setAccessed(rst.getTimestamp("addt").getTime());
						}
					}
				}
			}
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} finally {
			close(rst);
		}
		return duplicate;
	}

	public int setAccession(boolean newRow, AccessionData value) {
		byte index = (newRow ? STM_ACC_INSERT : STM_ACC_UPDATE);
		setByte(actionStms.get(index), 1, value.getSpyID());
		setString(actionStms.get(index), 2, (value.isWorkflow() ? "Y" : "N"));
		setString(actionStms.get(index), 3, (value.isWorkload() ? "Y" : "N"));
		setString(actionStms.get(index), 4, value.getName());
		setShort(actionStms.get(index), 5, value.getAccID());
		return execute(actionStms.get(index));
	}

	public int setAdditional(CaseData value) {
		setLong(actionStms.get(STM_ADD_INSERT), 1, value.getCaseID());
		setShort(actionStms.get(STM_ADD_INSERT), 2, value.getFinalID());
		setShort(actionStms.get(STM_ADD_INSERT), 3, value.getTypeID());
		setTime(actionStms.get(STM_ADD_INSERT), 4, value.getFinalTime());
		setDouble(actionStms.get(STM_ADD_INSERT), 5, value.getValue1());
		setDouble(actionStms.get(STM_ADD_INSERT), 6, value.getValue2());
		setDouble(actionStms.get(STM_ADD_INSERT), 7, value.getValue3());
		setDouble(actionStms.get(STM_ADD_INSERT), 8, value.getValue4());
		setInt(actionStms.get(STM_ADD_INSERT), 9, value.getValue5());
		return execute(actionStms.get(STM_ADD_INSERT));
	}

	public int setCoder(byte index, CoderData value) {
		byte coderID = 0;
		switch (index) {
		case LibConstants.ACTION_CODER1:
			coderID = (value.isNewRow() ? STM_CD1_INSERT : STM_CD1_UPDATE);
			break;
		case LibConstants.ACTION_CODER2:
			coderID = (value.isNewRow() ? STM_CD2_INSERT : STM_CD2_UPDATE);
			break;
		case LibConstants.ACTION_CODER3:
			coderID = (value.isNewRow() ? STM_CD3_INSERT : STM_CD3_UPDATE);
			break;
		case LibConstants.ACTION_CODER4:
			coderID = (value.isNewRow() ? STM_CD4_INSERT : STM_CD4_UPDATE);
			break;
		default:
		}
		setShort(actionStms.get(coderID), 1, value.getRuleID());
		setShort(actionStms.get(coderID), 2, value.getCount());
		setDouble(actionStms.get(coderID), 3, value.getValueA());
		setDouble(actionStms.get(coderID), 4, value.getValueB());
		setDouble(actionStms.get(coderID), 5, value.getValueC());
		setString(actionStms.get(coderID), 6, value.getName());
		setString(actionStms.get(coderID), 7, value.getDescr());
		setShort(actionStms.get(coderID), 8, value.getCodeID());
		return execute(actionStms.get(coderID));
	}

	public int setComment(boolean isNew, long caseID, String comment1,
			String comment2, String comment3, String comment4) {
		byte index = (isNew ? STM_CMT_INSERT : STM_CMT_UPDATE);
		setString(actionStms.get(index), 1, comment1);
		setString(actionStms.get(index), 2, comment2);
		setString(actionStms.get(index), 3, comment3);
		setString(actionStms.get(index), 4, comment4);
		setLong(actionStms.get(index), 5, caseID);
		return execute(actionStms.get(index));
	}

	public void setDB(byte value) {
		systemStms.put(STM_PND_SL_LST, prepareStatement(getSQL(STM_PND_SL_LST)));
		systemStms.put(STM_STP_UPDATE, prepareStatement(getSQL(STM_STP_UPDATE)));
		if (value < LibConstants.DB_MARIA) {
			systemStms.put(STM_STP_SL_SID, prepareStatement(getSQL(STM_STP_SL_SID)));
			systemStms.put(STM_WDY_SL_DTE, prepareStatement(getSQL(STM_WDY_SL_DTE)));
			systemStms.put(STM_WDY_SL_NXT, prepareStatement(getSQL(STM_WDY_SL_NXT)));
			systemStms.put(STM_WDY_SL_PRV, prepareStatement(getSQL(STM_WDY_SL_PRV)));
		} else {
			systemStms.put(STM_STP_SL_SID, prepareCallables(getSQL(STM_STP_SL_SID)));
			systemStms.put(STM_WDY_SL_DTE, prepareCallables(getSQL(STM_WDY_SL_DTE)));
			systemStms.put(STM_WDY_SL_NXT, prepareCallables(getSQL(STM_WDY_SL_NXT)));
			systemStms.put(STM_WDY_SL_PRV, prepareCallables(getSQL(STM_WDY_SL_PRV)));
		}
	}

	public int setError(CaseData value, byte errorID, String comment) {
		setLong(actionStms.get(STM_ERR_INSERT), 1, value.getCaseID());
		setByte(actionStms.get(STM_ERR_INSERT), 2, errorID);
		setString(actionStms.get(STM_ERR_INSERT), 3, value.getCaseNo());
		setString(actionStms.get(STM_ERR_INSERT), 4, comment);
		return execute(actionStms.get(STM_ERR_INSERT));
	}

	public int setErrorFixed(long value) {
		setLong(actionStms.get(STM_ERR_UPDATE), 1, value);
		return execute(actionStms.get(STM_ERR_UPDATE));
	}

	public int setFacility(boolean newRow, FacilityData value) {
		byte index = (newRow ? STM_FAC_INSERT : STM_FAC_UPDATE);
		setString(actionStms.get(index), 1, value.isWorkflow() ? "Y" : "N");
		setString(actionStms.get(index), 2, value.isWorkload() ? "Y" : "N");
		setString(actionStms.get(index), 3, value.getName());
		setString(actionStms.get(index), 4, value.getDescr());
		setShort(actionStms.get(index), 5, value.getFacID());
		return execute(actionStms.get(index));
	}

	public int setFinal(boolean isNew, CaseData value) {
		byte index = (isNew ? STM_CSE_INSERT : STM_CSE_UPDATE);
		setShort(actionStms.get(index), 1, value.getFacID());
		setShort(actionStms.get(index), 2, value.getSubID());
		setShort(actionStms.get(index), 3, value.getMainSpec());
		setShort(actionStms.get(index), 4, value.getGrossID());
		setShort(actionStms.get(index), 5, value.getEmbedID());
		setShort(actionStms.get(index), 6, value.getMicroID());
		setShort(actionStms.get(index), 7, value.getRouteID());
		setShort(actionStms.get(index), 8, value.getFinalID());
		setInt(actionStms.get(index), 9, value.getGrossTAT());
		setInt(actionStms.get(index), 10, value.getEmbedTAT());
		setInt(actionStms.get(index), 11, value.getMicroTAT());
		setInt(actionStms.get(index), 12, value.getRouteTAT());
		setInt(actionStms.get(index), 13, value.getFinalTAT());
		setInt(actionStms.get(index), 14, value.getNoSpecs());
		setInt(actionStms.get(index), 15, value.getNoBlocks());
		setInt(actionStms.get(index), 16, value.getNoSlides());
		setInt(actionStms.get(index), 17, value.getNoSynop());
		setInt(actionStms.get(index), 18, value.getNoFSSpec());
		setInt(actionStms.get(index), 19, value.getNoHE());
		setInt(actionStms.get(index), 20, value.getNoSS());
		setInt(actionStms.get(index), 21, value.getNoIHC());
		setInt(actionStms.get(index), 22, value.getNoMol());
		setInt(actionStms.get(index), 23, value.getValue5());
		setTime(actionStms.get(index), 24, value.getAccessTime());
		setTime(actionStms.get(index), 25, value.getGrossTime());
		setTime(actionStms.get(index), 26, value.getEmbedTime());
		setTime(actionStms.get(index), 27, value.getMicroTime());
		setTime(actionStms.get(index), 28, value.getRouteTime());
		setTime(actionStms.get(index), 29, value.getFinalTime());
		setDouble(actionStms.get(index), 30, value.getValue1());
		setDouble(actionStms.get(index), 31, value.getValue2());
		setDouble(actionStms.get(index), 32, value.getValue3());
		setDouble(actionStms.get(index), 33, value.getValue4());
		setString(actionStms.get(index), 34, value.getCaseNo());
		setLong(actionStms.get(index), 35, value.getCaseID());
		return execute(actionStms.get(index));
	}

	public int setFrozen(boolean isNew, FrozenData value) {
		byte index = (isNew ? STM_FRZ_INSERT : STM_FRZ_UPDATE);
		setShort(actionStms.get(index), 1, value.getNoBlocks());
		setShort(actionStms.get(index), 2, value.getNoSlides());
		setInt(actionStms.get(index), 3, 0);
		setInt(actionStms.get(index), 4, value.getValue5());
		setDouble(actionStms.get(index), 5, value.getValue5());
		setDouble(actionStms.get(index), 6, value.getValue5());
		setDouble(actionStms.get(index), 7, value.getValue5());
		setDouble(actionStms.get(index), 8, value.getValue5());
		setLong(actionStms.get(index), 9, value.getSpecID());
		return execute(actionStms.get(index));
	}

	public int setOrder(boolean isNew, long specID, OrderData value) {
		byte index = (isNew ? STM_ORD_INSERT : STM_ORD_UPDATE);
		setShort(actionStms.get(index), 1, value.getQty());
		setDouble(actionStms.get(index), 2, value.getValue1());
		setDouble(actionStms.get(index), 3, value.getValue2());
		setDouble(actionStms.get(index), 4, value.getValue3());
		setDouble(actionStms.get(index), 5, value.getValue4());
		setShort(actionStms.get(index), 6, value.getOrgID());
		setLong(actionStms.get(index), 7, specID);
		return execute(actionStms.get(index));
	}

	public int setOrderGroup(OrderGroupData value) {
		byte index = (value.isNewRow() ? STM_ORG_INSERT : STM_ORG_UPDATE);
		setShort(actionStms.get(index), 1, value.getTypeID());
		setShort(actionStms.get(index), 2, value.getValue1());
		setShort(actionStms.get(index), 3, value.getValue2());
		setShort(actionStms.get(index), 4, value.getValue3());
		setShort(actionStms.get(index), 5, value.getValue4());
		setInt(actionStms.get(index), 6, 0);	// deprecated
		setString(actionStms.get(index), 7, value.getName());
		setString(actionStms.get(index), 8, value.getDescr());
		setShort(actionStms.get(index), 9, value.getGrpID());
		return execute(actionStms.get(index));
	}

	public int setOrderMaster(boolean newRow, OrderMasterData value) {
		byte index = (newRow ? STM_ORM_INSERT : STM_ORM_UPDATE);
		setShort(actionStms.get(index), 1, value.getGrpID());
		setString(actionStms.get(index), 2, value.getName());
		setString(actionStms.get(index), 3, value.getDescr());
		setShort(actionStms.get(index), 4, value.getOrdID());
		return execute(actionStms.get(index));
	}

	public int setPendingEmbeded(CaseData value) {
		setByte(actionStms.get(STM_PND_UP_EMB), 1, value.getSubID());
		setByte(actionStms.get(STM_PND_UP_EMB), 2, value.getProcID());
		setShort(actionStms.get(STM_PND_UP_EMB), 3, value.getMainSpec());
		setShort(actionStms.get(STM_PND_UP_EMB), 4, value.getEmbedID());
		setInt(actionStms.get(STM_PND_UP_EMB), 5, value.getEmbedTAT());
		setByte(actionStms.get(STM_PND_UP_EMB), 6, value.getStatusID());
		setInt(actionStms.get(STM_PND_UP_EMB), 7, value.getNoSpecs());
		setInt(actionStms.get(STM_PND_UP_EMB), 8, value.getNoBlocks());
		setInt(actionStms.get(STM_PND_UP_EMB), 9, value.getValue5());
		setTime(actionStms.get(STM_PND_UP_EMB), 10, value.getEmbedTime());
		setLong(actionStms.get(STM_PND_UP_EMB), 11, value.getCaseID());
		return execute(actionStms.get(STM_PND_UP_EMB));
	}

	public int setPendingFinal(CaseData value) {
		setShort(actionStms.get(STM_PND_UP_FIN), 1, value.getFinalID());
		setInt(actionStms.get(STM_PND_UP_FIN), 2, value.getFinalTAT());
		setByte(actionStms.get(STM_PND_UP_FIN), 3, value.getStatusID());
		setInt(actionStms.get(STM_PND_UP_FIN), 4, value.getNoBlocks());
		setInt(actionStms.get(STM_PND_UP_FIN), 5, value.getNoSlides());
		setTime(actionStms.get(STM_PND_UP_FIN), 6, value.getFinalTime());
		setLong(actionStms.get(STM_PND_UP_FIN), 7, value.getCaseID());
		return execute(actionStms.get(STM_PND_UP_FIN));
	}

	public int setPendingGrossed(CaseData value) {
		setByte(actionStms.get(STM_PND_UP_GRS), 1, value.getSubID());
		setByte(actionStms.get(STM_PND_UP_GRS), 2, value.getProcID());
		setShort(actionStms.get(STM_PND_UP_GRS), 3, value.getMainSpec());
		setShort(actionStms.get(STM_PND_UP_GRS), 4, value.getGrossID());
		setInt(actionStms.get(STM_PND_UP_GRS), 5, value.getGrossTAT());
		setByte(actionStms.get(STM_PND_UP_GRS), 6, value.getStatusID());
		setInt(actionStms.get(STM_PND_UP_GRS), 7, value.getNoSpecs());
		setInt(actionStms.get(STM_PND_UP_GRS), 8, value.getNoBlocks());
		setInt(actionStms.get(STM_PND_UP_GRS), 9, value.getValue5());
		setTime(actionStms.get(STM_PND_UP_GRS), 10, value.getGrossTime());
		setLong(actionStms.get(STM_PND_UP_GRS), 11, value.getCaseID());
		return execute(actionStms.get(STM_PND_UP_GRS));
	}

	public int setPendingMicrotome(CaseData value) {
		setShort(actionStms.get(STM_PND_UP_MIC), 1, value.getMicroID());
		setInt(actionStms.get(STM_PND_UP_MIC), 2, value.getMicroTAT());
		setByte(actionStms.get(STM_PND_UP_MIC), 3, value.getStatusID());
		setInt(actionStms.get(STM_PND_UP_MIC), 4, value.getNoBlocks());
		setInt(actionStms.get(STM_PND_UP_MIC), 5, value.getNoSlides());
		setTime(actionStms.get(STM_PND_UP_MIC), 6, value.getMicroTime());
		setLong(actionStms.get(STM_PND_UP_MIC), 7, value.getCaseID());
		return execute(actionStms.get(STM_PND_UP_MIC));
	}

	public int setPendingNew(CaseData value) {
		setShort(actionStms.get(STM_PND_INSERT), 1, value.getFacID());
		setByte(actionStms.get(STM_PND_INSERT), 2, value.getSubID());
		setByte(actionStms.get(STM_PND_INSERT), 3, value.getProcID());
		setShort(actionStms.get(STM_PND_INSERT), 4, value.getMainSpec());
		setShort(actionStms.get(STM_PND_INSERT), 5, value.getGrossID());
		setShort(actionStms.get(STM_PND_INSERT), 6, value.getEmbedID());
		setShort(actionStms.get(STM_PND_INSERT), 7, value.getMicroID());
		setShort(actionStms.get(STM_PND_INSERT), 8, value.getRouteID());
		setShort(actionStms.get(STM_PND_INSERT), 9, value.getFinalID());
		setInt(actionStms.get(STM_PND_INSERT), 10, value.getGrossTAT());
		setInt(actionStms.get(STM_PND_INSERT), 11, value.getEmbedTAT());
		setInt(actionStms.get(STM_PND_INSERT), 12, value.getMicroTAT());
		setInt(actionStms.get(STM_PND_INSERT), 13, value.getRouteTAT());
		setInt(actionStms.get(STM_PND_INSERT), 14, value.getFinalTAT());
		setByte(actionStms.get(STM_PND_INSERT), 15, value.getStatusID());
		setInt(actionStms.get(STM_PND_INSERT), 16, value.getNoSpecs());
		setInt(actionStms.get(STM_PND_INSERT), 17, value.getNoBlocks());
		setInt(actionStms.get(STM_PND_INSERT), 18, value.getNoSlides());
		setInt(actionStms.get(STM_PND_INSERT), 19, value.getValue5());
		setTime(actionStms.get(STM_PND_INSERT), 20, value.getAccessTime());
		setTime(actionStms.get(STM_PND_INSERT), 21, value.getGrossTime());
		setTime(actionStms.get(STM_PND_INSERT), 22, value.getEmbedTime());
		setTime(actionStms.get(STM_PND_INSERT), 23, value.getMicroTime());
		setTime(actionStms.get(STM_PND_INSERT), 24, value.getRouteTime());
		setTime(actionStms.get(STM_PND_INSERT), 25, value.getFinalTime());
		setString(actionStms.get(STM_PND_INSERT), 26, value.getCaseNo());
		setLong(actionStms.get(STM_PND_INSERT), 27, value.getCaseID());
		return execute(actionStms.get(STM_PND_INSERT));
	}

	public int setPendingRouted(CaseData value) {
		setShort(actionStms.get(STM_PND_UP_ROU), 1, value.getRouteID());
		setInt(actionStms.get(STM_PND_UP_ROU), 2, value.getRouteTAT());
		setShort(actionStms.get(STM_PND_UP_ROU), 3, value.getStatusID());
		setInt(actionStms.get(STM_PND_UP_ROU), 4, value.getNoBlocks());
		setInt(actionStms.get(STM_PND_UP_ROU), 5, value.getNoSlides());
		setTime(actionStms.get(STM_PND_UP_ROU), 6, value.getRouteTime());
		setLong(actionStms.get(STM_PND_UP_ROU), 7, value.getCaseID());
		return execute(actionStms.get(STM_PND_UP_ROU));
	}

	public int setPendingScanned(CaseData value) {
		setShort(actionStms.get(STM_PND_UP_SCA), 1, value.getFinalID());
		setInt(actionStms.get(STM_PND_UP_SCA), 2, value.getRouteTAT());
		setShort(actionStms.get(STM_PND_UP_SCA), 3, value.getStatusID());
		setInt(actionStms.get(STM_PND_UP_SCA), 4, value.getNoBlocks());
		setInt(actionStms.get(STM_PND_UP_SCA), 5, value.getNoSlides());
		setTime(actionStms.get(STM_PND_UP_SCA), 6, value.getRouteTime());
		setLong(actionStms.get(STM_PND_UP_SCA), 7, value.getCaseID());
		return execute(actionStms.get(STM_PND_UP_SCA));
	}

	public int setPerson(boolean newRow, PersonData value) {
		byte index = (newRow ? STM_PRS_INSERT : STM_PRS_UPDATE);
		setInt(actionStms.get(index), 1, value.getAccess());
		setDate(actionStms.get(index), 2, value.getStarted().getTime());
		setString(actionStms.get(index), 3, value.getCode());
		setString(actionStms.get(index), 4, "Y");
		setString(actionStms.get(index), 5, value.getInitials());
		setString(actionStms.get(index), 6, value.getLastname());
		setString(actionStms.get(index), 7, value.getFirstname());
		setShort(actionStms.get(index), 8, value.getPrsID());
		return execute(actionStms.get(index));
	}

	public int setProcedure(ProcedureData item) {
		byte index = (item.isNewRow() ? STM_PRS_INSERT : STM_PRS_UPDATE);
		setString(actionStms.get(index), 1, item.getName());
		setString(actionStms.get(index), 2, item.getDescription());
		setShort(actionStms.get(index), 3, item.getID());
		return execute(actionStms.get(index));
	}

	public int setRule(RuleData value) {
		setString(actionStms.get(STM_RUL_UPDATE), 1, value.getName());
		setString(actionStms.get(STM_RUL_UPDATE), 2, value.getDescription());
		setShort(actionStms.get(STM_RUL_UPDATE), 3, value.getID());
		return execute(actionStms.get(STM_RUL_UPDATE));
	}

	public int setSchedule(ScheduleServiceData value) {
		byte index = (value.isNew() ? STM_SCH_INSERT : STM_SCH_UPDATE);
		setShort(actionStms.get(index), 1, value.getPersonId());
		setShort(actionStms.get(index), 2, value.getSrvID());
		setInt(actionStms.get(index), 3, value.getWdID());
		return execute(actionStms.get(index));
	}

	public int setSchedule(short prsID, short srvID, int dayID) {
		setShort(actionStms.get(STM_SCH_INSERT), 1, prsID);
		setShort(actionStms.get(STM_SCH_INSERT), 2, srvID);
		setInt(actionStms.get(STM_SCH_INSERT), 3, dayID);
		return execute(actionStms.get(STM_SCH_INSERT));
	}

	public int setService(ServiceData value) {
		byte index = (value.isNewRow() ? STM_SPY_INSERT : STM_SPY_UPDATE);
		setShort(actionStms.get(index), 1, value.getFacID());
		setShort(actionStms.get(index), 2, value.getSubID());
		setShort(actionStms.get(index), 3, value.getCodeID());
		setString(actionStms.get(index), 4, value.getName());
		setString(actionStms.get(index), 5, value.getDescr());
		setShort(actionStms.get(index), 6, value.getSrvID());
		return execute(actionStms.get(index));
	}

	public int setSetup(byte id, String value) {
		setString(systemStms.get(STM_STP_UPDATE), 1, value);
		setByte(systemStms.get(STM_STP_UPDATE), 2, id);
		return execute(systemStms.get(STM_STP_UPDATE));
	}

	public int setSpecialty(SpecialtyData value) {
		byte index = (value.isNewRow() ? STM_SPY_INSERT : STM_SPY_UPDATE);
		setString(actionStms.get(index), 1, (value.isWorkflow() ? "Y" : "N"));
		setString(actionStms.get(index), 2, (value.isWorkload() ? "Y" : "N"));
		setString(actionStms.get(index), 3, (value.codeSpecimen() ? "Y" : "N"));
		setString(actionStms.get(index), 4, value.getName());
		setByte(actionStms.get(index), 5, value.getID());
		return execute(actionStms.get(index));
	}

	public int setSpecimen(boolean isNew, SpecimenData value) {
		byte index = (isNew ? STM_SPE_INSERT : STM_SPE_UPDATE);
		setLong(actionStms.get(index), 1, value.getCaseID());
		setShort(actionStms.get(index), 2, value.getSpmID());
		setShort(actionStms.get(index), 3, value.getNoBlocks());
		setShort(actionStms.get(index), 4, value.getNoSlides());
		setShort(actionStms.get(index), 5, value.getNoFrags());
		setShort(actionStms.get(index), 6, value.getNoHE());
		setShort(actionStms.get(index), 7, value.getNoSS());
		setShort(actionStms.get(index), 8, value.getNoIHC());
		setShort(actionStms.get(index), 9, value.getNoMOL());
		setInt(actionStms.get(index), 10, value.getValue5());
		setDouble(actionStms.get(index), 11, value.getValue1());
		setDouble(actionStms.get(index), 12, value.getValue2());
		setDouble(actionStms.get(index), 13, value.getValue3());
		setDouble(actionStms.get(index), 14, value.getValue4());
		setString(actionStms.get(index), 15, value.getDescr());
		setLong(actionStms.get(index), 16, value.getSpecID());
		return execute(actionStms.get(index));
	}

	public int setSpecimenGroup(SpecimenGroupData value) {
		byte index = (value.isNewRow() ? STM_SPG_INSERT : STM_SPG_UPDATE);
		setShort(actionStms.get(index), 1, value.getSubID());
		setShort(actionStms.get(index), 2, value.getProID());
		setShort(actionStms.get(index), 3, value.getCodeId(0, 0));
		setShort(actionStms.get(index), 4, value.getCodeId(1, 0));
		setShort(actionStms.get(index), 5, value.getCodeId(2, 0));
		setShort(actionStms.get(index), 6, value.getCodeId(0, 1));
		setShort(actionStms.get(index), 7, value.getCodeId(1, 1));
		setShort(actionStms.get(index), 8, value.getCodeId(2, 1));
		setShort(actionStms.get(index), 9, value.getCodeId(0, 2));
		setShort(actionStms.get(index), 10, value.getCodeId(1, 2));
		setShort(actionStms.get(index), 11, value.getCodeId(2, 2));
		setShort(actionStms.get(index), 12, value.getCodeId(0, 3));
		setShort(actionStms.get(index), 13, value.getCodeId(1, 3));
		setShort(actionStms.get(index), 14, value.getCodeId(2, 3));
		setInt(actionStms.get(index), 15, value.getValue5());
		setString(actionStms.get(index), 16, (value.isHasLN() ? "Y" : "N"));
		setString(actionStms.get(index), 17, value.getDescr());
		setShort(actionStms.get(index), 18, value.getGrpID());
		return execute(actionStms.get(index));
	}

	public int setSpecimenGroupValue5(short spgID, int value) {
		setInt(actionStms.get(STM_SPG_UPD_V5), 1, value);
		setShort(actionStms.get(STM_SPG_UPD_V5), 2, spgID);
		return execute(actionStms.get(STM_SPG_UPD_V5));
	}

	public int setSpecimenMaster(boolean newRow, SpecimenMasterData value) {
		byte index = (newRow ? STM_SPM_INSERT : STM_SPM_UPDATE);
		setShort(actionStms.get(index), 1, value.getSpgID());
		setShort(actionStms.get(index), 2, value.getTurID());
		setString(actionStms.get(index), 3, value.getName());
		setString(actionStms.get(index), 4, value.getDescr());
		setShort(actionStms.get(index), 5, value.getSpmID());
		return execute(actionStms.get(index));
	}

	@Override
	public void setStatements(byte id) {
	}

	public int setSubspecialty(SubspecialtyData value) {
		byte index = (value.isNewRow() ? STM_SUB_INSERT : STM_SUB_UPDATE);
		setByte(actionStms.get(index), 1, value.getSpyID());
		setString(actionStms.get(index), 2, value.getName());
		setString(actionStms.get(index), 3, value.getDescr());
		setByte(actionStms.get(index), 4, value.getID());
		return execute(actionStms.get(index));
	}

	public int setTurnaround(TurnaroundData value) {
		byte index = (value.isNewRow() ? STM_TUR_INSERT : STM_TUR_UPDATE);
		setShort(actionStms.get(index), 1, value.getGross());
		setShort(actionStms.get(index), 2, value.getEmbed());
		setShort(actionStms.get(index), 3, value.getMicrotomy());
		setShort(actionStms.get(index), 4, value.getRoute());
		setShort(actionStms.get(index), 5, value.getDiagnosis());
		setString(actionStms.get(index), 6, value.getName());
		setByte(actionStms.get(index), 7, value.getTurID());
		return execute(actionStms.get(index));
	}

	public int setWorkday(int dayID, int dayNo, long date, String dayType) {
		setDate(actionStms.get(STM_WDY_INSERT), 1, date);
		setString(actionStms.get(STM_WDY_INSERT), 2, dayType);
		setInt(actionStms.get(STM_WDY_INSERT), 3, dayNo);
		setInt(actionStms.get(STM_WDY_INSERT), 4, dayID);
		return execute(systemStms.get(STM_WDY_INSERT));
	}
}