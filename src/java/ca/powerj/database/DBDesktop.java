package ca.powerj.database;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ca.powerj.data.ReportData;
import ca.powerj.data.SpecimenData;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

class DBDesktop extends DBPowerj {

	DBDesktop(LibBase base) {
		super(base);
		dbName = "DBDesktop";
	}

	@Override
	public void getCaseUMLS(long caseID, short userID, ReportData reportData) {
		byte specimenNo = -1;
		byte tissueNo = -1;
		byte diagnosisNo = -1;
		ResultSet rst = null;
		try {
			setShort(actionStms.get(STM_UML_SL_CID), 1, userID);
			setShort(actionStms.get(STM_UML_SL_CID), 2, userID);
			setShort(actionStms.get(STM_UML_SL_CID), 3, userID);
			setLong(actionStms.get(STM_UML_SL_CID), 4, caseID);
			rst = getResultSet(actionStms.get(STM_UML_SL_CID));
			while (rst.next()) {
				if (specimenNo != rst.getByte("splb")) {
					tissueNo = -1;
					diagnosisNo = -1;
					specimenNo = rst.getByte("splb");
//					reportData.addSpecimen(rst.getByte("syid"), rst.getByte("sbid"),
//							rst.getByte("onid"), rst.getByte("poid"),
//							specimenNo, rst.getLong("spid"),
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
				setShort(actionStms.get(STM_TIS_SL_SID), 1, userID);
				setShort(actionStms.get(STM_TIS_SL_SID), 2, userID);
				setShort(actionStms.get(STM_TIS_SL_SID), 3, specimen.getSpmID());
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
			return "SELECT * FROM <pjschema>.udvaccessions ORDER BY acnm";
		case STM_ADD_SL_CID:
			return "SELECT prid, adcd, adv5, adv1, adv2, adv3, adv4, addt, prnm, prls, prfr, cano "
					+ "FROM <pjschema>.udvadditionals WHERE caid = ? ORDER BY addt";
		case STM_ADD_SL_LST:
			return "SELECT MAX(addt) AS addt FROM <pjschema>.additionals WHERE adcd = ?";
		case STM_ADD_SL_SPG:
			return "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, "
					+ "f.fanm, COUNT(a.caid) AS qty, SUM(a.adv1) AS adv1, SUM(a.adv2) AS adv2, "
					+ "SUM(a.adv3) AS adv3, SUM(a.adv4) AS adv4, SUM(a.adv5) AS adv5 "
					+ "FROM <pjschema>.additionals AS a "
					+ "INNER JOIN <pjschema>.cases AS c ON c.caid = a.caid "
					+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = c.faid "
					+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = c.smid "
					+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid "
					+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid "
					+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = g.sbid "
					+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid "
					+ "WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm "
					+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid";
		case STM_ADD_SL_SUM:
			return "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(caid) AS adca, "
					+ "SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, SUM(adv3) AS adv3, SUM(adv4) AS adv4 "
					+ "FROM <pjschema>.udvadditionals WHERE (addt BETWEEN ? AND ?) "
					+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr "
					+ "ORDER BY faid, syid, sbid, poid, prid";
		case STM_CD1_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder1 ORDER BY coid";
		case STM_CD2_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder2 ORDER BY coid";
		case STM_CD3_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder3 ORDER BY coid";
		case STM_CD4_SELECT:
			return "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder4 ORDER BY coid";
		case STM_CMT_SELECT:
			return "SELECT com1, com2, com3, com4 FROM <pjschema>.comments WHERE caid = ?";
		case STM_CSE_SL_CID:
			return "SELECT caid FROM <pjschema>.cases WHERE cano = ?";
		case STM_CSE_SL_CNO:
			return "SELECT cano FROM <pjschema>.cases WHERE caid = ?";
		case STM_CSE_SL_SPE:
			return "SELECT c.smid, c.fned, c.cano, s.spid FROM <pjschema>.cases AS c INNER JOIN <pjschema>.specimens AS s ON s.caid = c.caid "
					+ "AND s.smid = c.smid WHERE c.caid = ?";
		case STM_CSE_SL_SUM:
			return "SELECT faid, syid, sbid, poid, fnid, fanm, synm, sbnm, ponm, fnnm, fnls, fnfr, COUNT(caid) AS caca, "
					+ "SUM(CAST(casp as INT)) AS casp, SUM(CAST(cabl as INT)) AS cabl, SUM(CAST(casl as INT)) AS casl, SUM(CAST(cahe as INT)) AS cahe, "
					+ "SUM(CAST(cass as INT)) AS cass, SUM(CAST(caih as INT)) AS caih, SUM(CAST(camo as INT)) AS camo, SUM(CAST(cafs as INT)) AS cafs, "
					+ "SUM(CAST(casy as INT)) AS casy, SUM(CAST(grta as INT)) AS grta, SUM(CAST(emta as INT)) AS emta, SUM(CAST(mita as INT)) AS mita, "
					+ "SUM(CAST(rota as INT)) AS rota, SUM(CAST(fnta as INT)) AS fnta, SUM(CAST(cav5 as INT)) AS cav5, SUM(cav1) AS cav1, SUM(cav2) AS cav2, "
					+ "SUM(cav3) AS cav3, SUM(cav4) AS cav4 FROM <pjschema>.udvcases WHERE (fned BETWEEN ? AND ?) "
					+ "GROUP BY faid, syid, sbid, poid, fnid, fanm, synm, sbnm, ponm, fnnm, fnls, fnfr "
					+ "ORDER BY faid, syid, sbid, poid, fnid";
		case STM_DIS_SELECT:
			return "SELECT dsid, dsnm FROM <pjschema>.diseases ORDER BY dsnm";
		case STM_ERR_SELECT:
			return "SELECT caid, erid, cano FROM <pjschema>.errors WHERE erid > 0 ORDER BY cano";
		case STM_ERR_SL_CMT:
			return "SELECT erdc FROM <pjschema>.errors WHERE caid = ?";
		case STM_ERR_SL_FXD:
			return "SELECT caid FROM <pjschema>.errors WHERE erid = 0 ORDER BY caid";
		case STM_FAC_SELECT:
			return "SELECT faid, fafl, fald, fanm, fadc FROM <pjschema>.facilities ORDER BY faid";
		case STM_FRZ_SL_SID:
			return "SELECT prid, frbl, frsl, frv5, frv1, frv2, frv3, frv4, prnm, prls, "
					+ "prfr, spdc, smnm FROM <pjschema>.udvfrozens WHERE spid = ?";
		case STM_FRZ_SL_SPG:
			return "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, COUNT(spid) AS frsp, "
			+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl, SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
			+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 FROM <pjschema>.udvfrozens WHERE (aced BETWEEN ? AND ?) "
			+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc "
			+ "ORDER BY faid, syid, sbid, poid, sgid";
		case STM_FRZ_SL_SU5:
			return "SELECT COUNT(*) AS qty, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
					+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 FROM <pjschema>.udvfrozens WHERE aced BETWEEN ? AND ?";
		case STM_FRZ_SL_SUM:
			return "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(spid) AS frsp, "
					+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl, SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
					+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 FROM <pjschema>.udvfrozens WHERE (aced BETWEEN ? AND ?) "
					+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr "
					+ "ORDER BY faid, syid, sbid, poid, prid";
		case STM_ORD_SELECT:
			return "SELECT orqy, orv1, orv2, orv3, orv4, ognm FROM <pjschema>.udvorders WHERE spid = ? ORDER BY ognm";
		case STM_ORG_SELECT:
			return "SELECT * FROM <pjschema>.udvordergroups ORDER BY ognm";
		case STM_ORM_SELECT:
			return "SELECT * FROM <pjschema>.udvordermaster ORDER BY omnm";
		case STM_ORN_SELECT:
			return "SELECT ssoid, syid, sbid, onid, onnm FROM <pjschema>.udvorgans ORDER BY syid, sbid, onid";
		case STM_PND_SELECT:
			return "SELECT * FROM <pjschema>.udvpending ORDER BY pnid";
		case STM_PND_SL_ROU:
			return "SELECT * FROM <pjschema>.udvpending WHERE roed BETWEEN ? AND ? ORDER BY pnid";
		case STM_PRO_SELECT:
			return "SELECT poid, ponm, podc FROM <pjschema>.procedures ORDER BY ponm";
		case STM_PRS_SELECT:
			return "SELECT * FROM <pjschema>.persons ORDER BY prnm";
		case STM_PRS_SL_PID:
			return "SELECT prvl FROM <pjschema>.persons WHERE prid = ?";
		case STM_RUL_SELECT:
			return "SELECT ruid, runm, rudc FROM <pjschema>.rules ORDER BY ruid";
		case STM_SBS_SELECT:
			return "SELECT sbid, sbnm FROM <pjschema>.subs ORDER BY sbnm";
		case STM_SCH_SL_SRV:
			return "SELECT wdid, srid, prid, prnm, srnm FROM <pjschema>.udvschedules WHERE (wddt BETWEEN ? AND ?) ORDER BY srnm, wdid";
		case STM_SCH_SL_SUM:
			return "SELECT * FROM <pjschema>.udvschedules WHERE (wddt BETWEEN ? AND ?) ORDER BY faid, prid, wdid, srid";
		case STM_SCH_SL_STA:
			return "SELECT wdid, srid, prid, prnm, srnm FROM <pjschema>.udvschedules WHERE (wddt BETWEEN ? AND ?) ORDER BY prnm, wdid, srnm";
		case STM_SPE_SELECT:
			return "SELECT spid, smid, spbl, spsl, spfr, sphe, spss, spih, spmo, spv5, spv1, spv2, spv3, "
					+ "spv4, spdc, smnm, smdc, ponm FROM <pjschema>.udvspecimens WHERE caid = ? ORDER BY spid";
		case STM_SPG_SELECT:
			return "SELECT * FROM <pjschema>.udvspecigroups ORDER BY sgdc";
		case STM_SPG_SL_SU5:
			return "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1, "
					+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4 "
					+ "FROM <pjschema>.specigroups g INNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid "
					+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid INNER JOIN <pjschema>.cases c ON c.caid = s.caid "
					+ "WHERE c.fned BETWEEN ? AND ? GROUP BY g.sgid";
		case STM_SPG_SL_SUM:
			return "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, "
					+ "COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl, "
					+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1, "
					+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5 "
					+ "FROM <pjschema>.specigroups g INNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid "
					+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid "
					+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid "
					+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid "
					+ "INNER JOIN <pjschema>.subspecial b ON b.sbid = g.sbid "
					+ "INNER JOIN <pjschema>.specialties y ON y.syid = b.syid "
					+ "INNER JOIN <pjschema>.facilities f ON f.faid = c.faid WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm "
					+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid";
		case STM_SPM_SELECT:
			return "SELECT * FROM <pjschema>.udvspecimaster ORDER BY smnm";
		case STM_SPY_SELECT:
			return "SELECT * FROM <pjschema>.specialties ORDER BY synm";
		case STM_STP_SELECT:
			return "SELECT stid, stva FROM <pjschema>.setup ORDER BY stid";
		case STM_STP_SL_SID:
			return "SELECT stva FROM <pjschema>.setup WHERE stid = ?";
		case STM_SUB_SELECT:
			return "SELECT * FROM <pjschema>.udvsubspecial ORDER BY sbnm";
		case STM_TUR_SELECT:
			return "SELECT taid, grss, embd, micr, rout, finl, tanm FROM <pjschema>.turnaround ORDER BY tanm";
		case STM_WDY_SELECT:
			return "SELECT wdid, wdno, wdtp, wddt FROM <pjschema>.workdays WHERE wddt >= ? ORDER BY wddt";
		case STM_WDY_SL_DTE:
			return "SELECT wdno FROM <pjschema>.workdays WHERE wddt = ?";
		case STM_WDY_SL_NXT:
			return "SELECT MIN(wddt) AS wddt FROM <pjschema>.workdays WHERE wddt > ? AND wdtp = 'D'";
		case STM_WDY_SL_PRV:
			return "SELECT MAX(wddt) AS wddt FROM <pjschema>.workdays WHERE wddt < ? AND wdtp = 'D'";
		case STM_TIS_SELECT:
			return "SELECT t.tiid, t.poid, t.ticx, t.tinm, t.tidc, t.podc, s.tidc AS tidc2, s.podc AS podc2\n"
					+ "FROM <pjschema>.tissues AS t\n"
					+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS s ON s.tiid = p.tiid AND s.prid = ?\n"
					+ "WHERE t.syid = ? AND t.onid = ?";
		case STM_STY_SELECT:
			return "SELECT s.prid, p.prnm\n"
					+ "FROM <pjschema>.styles AS s\n"
					+ "INNER JOIN <pjschema>.persons AS p ON p.prid = s.prid\n"
					+ "ORDER BY p.prnm";
		case STM_STY_SL_PID:
			return "SELECT * FROM <pjschema>.styles WHERE prid = ?";
		case STM_UML_SL_CID:
			return "SELECT ud.spid, ud.dgid, ud.paid, ud.dglb, ud.palb,\n"
					+ "di.dsid, di.dgnm\n"
					+ "us.tiid, us.splb, us.splo,\n"
					+ "t1.poid, t1.onid, t1.sbid, t1.syid, t1.ticx,\n"
					+ "t1.tinm AS spnm, t1.tidc AS spdc1, t1.podc AS sppo1\n"
					+ "t2.tinm AS tinm, t2.tidc AS tidc1\n"
					+ "od.dgdc AS dgdc1, od.midc AS midc1\n"
					+ "p1.tidc AS spdc2, p1.podc AS sppo2\n"
					+ "p2.tidc AS tidc2\n"
					+ "dp.dgdc AS dgdc2, dp.midc AS midc2\n"
					+ "FROM <pjschema>.umlsdia AS ud\n"
					+ "INNER JOIN <pjschema>.diagnosis AS di ON di.dgid = ud.dgid\n"
					+ "INNER JOIN <pjschema>.umlsspc AS us ON us.spid = ud.spid\n"
					+ "INNER JOIN <pjschema>.tissues AS t1 ON t1.tiid = us.tiid\n"
					+ "INNER JOIN <pjschema>.tissueparts AS tp ON tp.paid = ud.paid\n"
					+ "INNER JOIN <pjschema>.tissues AS t2 ON t2.tiid = tp.tiid\n"
					+ "INNER JOIN <pjschema>.organdiagnosis AS od ON od.onid = t2.onid AND od.dgid = ud.dgid\n"
					+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p1 ON p1.prid = ? AND p1.tiid = t1.tiid\n"
					+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p2 ON p2.prid = ? AND p2.tiid = t2.tiid\n"
					+ "LEFT OUTER JOIN <pjschema>.diagnosispersons AS dp ON dp.prid = ? AND dp.onid = t2.onid AND dp.dgid = ud.dgid\n"
					+ "WHERE us.caid = ?\n"
					+ "ORDER BY us.splb, ud.tilb, ud.dglb";
		case STM_TIS_SL_SID:
			return "SELECT st.tiid, tp.paid, tp.palb,\n"
					+ "t1.poid, t1.onid, t1.sbid, t1.syid, t1.ticx,\n"
					+ "t1.tinm AS spnm, t1.tidc AS spdc1, t1.podc AS sppo1\n"
					+ "t2.tinm AS tinm, t2.tidc AS tidc1\n"
					+ "p1.tidc AS spdc2, p1.podc AS sppo2\n"
					+ "p2.tidc AS tidc2\n"
					+ "FROM <pjschema>.specimentissues AS st\n"
					+ "INNER JOIN <pjschema>.tissues AS t1 ON t1.tiid = st.tiid\n"
					+ "INNER JOIN <pjschema>.tissueparts AS tp ON tp.tiid = st.tiid\n"
					+ "INNER JOIN <pjschema>.tissues AS t2 ON t2.tiid = tp.paid\n"
					+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p1 ON p1.prid = ? AND p1.tiid = st.tiid\n"
					+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p2 ON p2.prid = ? AND p2.tiid = tp.paid\n"
					+ "WHERE st.spid = ?\n"
					+ "ORDER BY tp.palb";
		default:
			return super.getSQL2(id);
		}
	}

	@Override
	public void setStatements(byte id) {
		close(actionStms);
		switch (id) {
		case LibConstants.ACTION_ACCESSION:
			actionStms.put(STM_ACC_SELECT, prepareStatement(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_ACC_UPDATE, prepareStatement(getSQL(STM_ACC_UPDATE)));
			break;
		case LibConstants.ACTION_BACKLOG:
			actionStms.put(STM_PND_SELECT, prepareStatement(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_TUR_SELECT, prepareStatement(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_CODER1:
			actionStms.put(STM_CD1_INSERT, prepareStatement(getSQL(STM_CD1_INSERT)));
			actionStms.put(STM_CD1_SELECT, prepareStatement(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD1_UPDATE, prepareStatement(getSQL(STM_CD1_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareStatement(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_CODER2:
			actionStms.put(STM_CD2_INSERT, prepareStatement(getSQL(STM_CD2_INSERT)));
			actionStms.put(STM_CD2_SELECT, prepareStatement(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD2_UPDATE, prepareStatement(getSQL(STM_CD2_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareStatement(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_CODER3:
			actionStms.put(STM_CD3_INSERT, prepareStatement(getSQL(STM_CD3_INSERT)));
			actionStms.put(STM_CD3_SELECT, prepareStatement(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD3_UPDATE, prepareStatement(getSQL(STM_CD3_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareStatement(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_CODER4:
			actionStms.put(STM_CD4_INSERT, prepareStatement(getSQL(STM_CD4_INSERT)));
			actionStms.put(STM_CD4_SELECT, prepareStatement(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_CD4_UPDATE, prepareStatement(getSQL(STM_CD4_UPDATE)));
			actionStms.put(STM_RUL_SELECT, prepareStatement(getSQL(STM_RUL_SELECT)));
			break;
		case LibConstants.ACTION_DAILY:
			actionStms.put(STM_PND_SELECT, prepareStatement(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_TUR_SELECT, prepareStatement(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_DISTRIBUTE:
			actionStms.put(STM_ADD_SL_SUM, prepareStatement(getSQL(STM_ADD_SL_SUM)));
			actionStms.put(STM_CSE_SL_SUM, prepareStatement(getSQL(STM_CSE_SL_SUM)));
			actionStms.put(STM_FRZ_SL_SUM, prepareStatement(getSQL(STM_FRZ_SL_SUM)));
			break;
		case LibConstants.ACTION_EDITOR:
			actionStms.put(STM_SPM_SELECT, prepareStatement(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_ERROR:
			actionStms.put(STM_ERR_SELECT, prepareStatement(getSQL(STM_ERR_SELECT)));
			actionStms.put(STM_ERR_SL_CMT, prepareStatement(getSQL(STM_ERR_SL_CMT)));
			actionStms.put(STM_ERR_UPDATE, prepareStatement(getSQL(STM_ERR_UPDATE)));
			actionStms.put(STM_SPM_SELECT, prepareStatement(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_FACILITY:
			actionStms.put(STM_FAC_SELECT, prepareStatement(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_FAC_UPDATE, prepareStatement(getSQL(STM_FAC_UPDATE)));
			break;
		case LibConstants.ACTION_FINALS:
			actionStms.put(STM_ADD_SL_CID, prepareStatement(getSQL(STM_ADD_SL_CID)));
			actionStms.put(STM_CMT_SELECT, prepareStatement(getSQL(STM_CMT_SELECT)));
			actionStms.put(STM_FRZ_SL_SID, prepareStatement(getSQL(STM_FRZ_SL_SID)));
			actionStms.put(STM_ORD_SELECT, prepareStatement(getSQL(STM_ORD_SELECT)));
			actionStms.put(STM_SPE_SELECT, prepareStatement(getSQL(STM_SPE_SELECT)));
			break;
		case LibConstants.ACTION_FORECAST:
			actionStms.put(STM_ADD_SL_YER, prepareStatement(getSQL(STM_ADD_SL_YER)));
			actionStms.put(STM_FRZ_SL_YER, prepareStatement(getSQL(STM_FRZ_SL_YER)));
			actionStms.put(STM_SPG_SL_YER, prepareStatement(getSQL(STM_SPG_SL_YER)));
			break;
		case LibConstants.ACTION_HISTOLOGY:
			actionStms.put(STM_PND_SELECT, prepareStatement(getSQL(STM_PND_SELECT)));
			break;
		case LibConstants.ACTION_ORDERGROUP:
			actionStms.put(STM_CD1_SELECT, prepareStatement(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD2_SELECT, prepareStatement(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD3_SELECT, prepareStatement(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD4_SELECT, prepareStatement(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_ORG_INSERT, prepareStatement(getSQL(STM_ORG_INSERT)));
			actionStms.put(STM_ORG_SELECT, prepareStatement(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORG_UPDATE, prepareStatement(getSQL(STM_ORG_UPDATE)));
			actionStms.put(STM_ORT_SELECT, prepareStatement(getSQL(STM_ORT_SELECT)));
			break;
		case LibConstants.ACTION_ORDERMASTER:
			actionStms.put(STM_ORG_SELECT, prepareStatement(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORM_SELECT, prepareStatement(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_ORM_UPDATE, prepareStatement(getSQL(STM_ORM_UPDATE)));
			break;
		case LibConstants.ACTION_PENDING:
			actionStms.put(STM_PND_SELECT, prepareStatement(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_TUR_SELECT, prepareStatement(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_PERSONNEL:
			actionStms.put(STM_PRS_SELECT, prepareStatement(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_PRS_UPDATE, prepareStatement(getSQL(STM_PRS_UPDATE)));
			break;
		case LibConstants.ACTION_PROCEDURES:
			actionStms.put(STM_PRO_INSERT, prepareStatement(getSQL(STM_PRO_INSERT)));
			actionStms.put(STM_PRO_SELECT, prepareStatement(getSQL(STM_PRO_SELECT)));
			actionStms.put(STM_PRO_UPDATE, prepareStatement(getSQL(STM_PRO_UPDATE)));
			break;
		case LibConstants.ACTION_REPORT:
			actionStms.put(STM_DIA_SELECT, prepareStatement(getSQL(STM_DIA_SELECT)));
			actionStms.put(STM_STY_SL_PID, prepareStatement(getSQL(STM_STY_SL_PID)));
			actionStms.put(STM_TIS_SELECT, prepareStatement(getSQL(STM_TIS_SELECT)));
			break;
		case LibConstants.ACTION_ROUTING:
			actionStms.put(STM_PND_SL_ROU, prepareStatement(getSQL(STM_PND_SL_ROU)));
			break;
		case LibConstants.ACTION_RULES:
			actionStms.put(STM_RUL_INSERT, prepareStatement(getSQL(STM_RUL_INSERT)));
			actionStms.put(STM_RUL_SELECT, prepareStatement(getSQL(STM_RUL_SELECT)));
			actionStms.put(STM_RUL_UPDATE, prepareStatement(getSQL(STM_RUL_UPDATE)));
			break;
		case LibConstants.ACTION_SCHEDULE:
			actionStms.put(STM_PRS_SELECT, prepareStatement(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_SCH_DELETE, prepareStatement(getSQL(STM_SCH_DELETE)));
			actionStms.put(STM_SCH_INSERT, prepareStatement(getSQL(STM_SCH_INSERT)));
			actionStms.put(STM_SCH_SL_MON, prepareStatement(getSQL(STM_SCH_SL_MON)));
			actionStms.put(STM_SCH_SL_SRV, prepareStatement(getSQL(STM_SCH_SL_SRV)));
			actionStms.put(STM_SCH_SL_STA, prepareStatement(getSQL(STM_SCH_SL_STA)));
			actionStms.put(STM_SCH_UPDATE, prepareStatement(getSQL(STM_SCH_UPDATE)));
			actionStms.put(STM_SRV_SELECT, prepareStatement(getSQL(STM_SRV_SELECT)));
			actionStms.put(STM_WDY_SELECT, prepareStatement(getSQL(STM_WDY_SELECT)));
			break;
		case LibConstants.ACTION_SERVICES:
			actionStms.put(STM_SRV_INSERT, prepareStatement(getSQL(STM_SRV_INSERT)));
			actionStms.put(STM_SRV_SELECT, prepareStatement(getSQL(STM_SRV_SELECT)));
			actionStms.put(STM_SRV_UPDATE, prepareStatement(getSQL(STM_SRV_UPDATE)));
			actionStms.put(STM_SUB_SELECT, prepareStatement(getSQL(STM_SUB_SELECT)));
			break;
		case LibConstants.ACTION_SETUP:
			actionStms.put(STM_STP_SELECT, prepareStatement(getSQL(STM_STP_SELECT)));
			break;
		case LibConstants.ACTION_SPECGROUP:
			actionStms.put(STM_CD1_SELECT, prepareStatement(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD2_SELECT, prepareStatement(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD3_SELECT, prepareStatement(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD4_SELECT, prepareStatement(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_PRO_SELECT, prepareStatement(getSQL(STM_PRO_SELECT)));
			actionStms.put(STM_SPG_INSERT, prepareStatement(getSQL(STM_SPG_INSERT)));
			actionStms.put(STM_SPG_SELECT, prepareStatement(getSQL(STM_SPG_SELECT)));
			actionStms.put(STM_SPG_UPDATE, prepareStatement(getSQL(STM_SPG_UPDATE)));
			actionStms.put(STM_SUB_SELECT, prepareStatement(getSQL(STM_SUB_SELECT)));
			break;
		case LibConstants.ACTION_SPECIALTY:
			actionStms.put(STM_SPY_INSERT, prepareStatement(getSQL(STM_SPY_INSERT)));
			actionStms.put(STM_SPY_SELECT, prepareStatement(getSQL(STM_SPY_SELECT)));
			actionStms.put(STM_SPY_UPDATE, prepareStatement(getSQL(STM_SPY_UPDATE)));
			break;
		case LibConstants.ACTION_SPECIMEN:
			actionStms.put(STM_ADD_SL_SPG, prepareStatement(getSQL(STM_ADD_SL_SPG)));
			actionStms.put(STM_FRZ_SL_SPG, prepareStatement(getSQL(STM_FRZ_SL_SPG)));
			actionStms.put(STM_SPG_SL_SUM, prepareStatement(getSQL(STM_SPG_SL_SUM)));
			break;
		case LibConstants.ACTION_SPECMASTER:
			actionStms.put(STM_SPG_SELECT, prepareStatement(getSQL(STM_SPG_SELECT)));
			actionStms.put(STM_SPM_SELECT, prepareStatement(getSQL(STM_SPM_SELECT)));
			actionStms.put(STM_SPM_UPDATE, prepareStatement(getSQL(STM_SPM_UPDATE)));
			actionStms.put(STM_TUR_SELECT, prepareStatement(getSQL(STM_TUR_SELECT)));
			break;
		case LibConstants.ACTION_SUBSPECIAL:
			actionStms.put(STM_SPY_SELECT, prepareStatement(getSQL(STM_SPY_SELECT)));
			actionStms.put(STM_SUB_INSERT, prepareStatement(getSQL(STM_SUB_INSERT)));
			actionStms.put(STM_SUB_SELECT, prepareStatement(getSQL(STM_SUB_SELECT)));
			actionStms.put(STM_SUB_UPDATE, prepareStatement(getSQL(STM_SUB_UPDATE)));
			break;
		case LibConstants.ACTION_TURNAROUND:
			actionStms.put(STM_CSE_SL_TAT, prepareStatement(getSQL(STM_CSE_SL_TAT)));
			break;
		case LibConstants.ACTION_TURNMASTER:
			actionStms.put(STM_TUR_INSERT, prepareStatement(getSQL(STM_TUR_INSERT)));
			actionStms.put(STM_TUR_SELECT, prepareStatement(getSQL(STM_TUR_SELECT)));
			actionStms.put(STM_TUR_UPDATE, prepareStatement(getSQL(STM_TUR_UPDATE)));
			break;
		case LibConstants.ACTION_WORKDAYS:
			actionStms.put(STM_SCH_SL_SUM, prepareStatement(getSQL(STM_SCH_SL_SUM)));
			break;
		case LibConstants.ACTION_LDAYS:
			actionStms.put(STM_WDY_INSERT, prepareStatement(getSQL(STM_WDY_INSERT)));
			actionStms.put(STM_WDY_SL_LST, prepareStatement(getSQL(STM_WDY_SL_LST)));
			break;
		case LibConstants.ACTION_LFLOW:
			actionStms.put(STM_ACC_SELECT, prepareStatement(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_FAC_SELECT, prepareStatement(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_ORG_SELECT, prepareStatement(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORM_SELECT, prepareStatement(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_PRS_SELECT, prepareStatement(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_PND_DEL_FN, prepareStatement(getSQL(STM_PND_DEL_FN)));
			actionStms.put(STM_PND_DEL_ID, prepareStatement(getSQL(STM_PND_DEL_ID)));
			actionStms.put(STM_PND_INSERT, prepareStatement(getSQL(STM_PND_INSERT)));
			actionStms.put(STM_PND_SELECT, prepareStatement(getSQL(STM_PND_SELECT)));
			actionStms.put(STM_PND_UP_EMB, prepareStatement(getSQL(STM_PND_UP_EMB)));
			actionStms.put(STM_PND_UP_FIN, prepareStatement(getSQL(STM_PND_UP_FIN)));
			actionStms.put(STM_PND_UP_GRS, prepareStatement(getSQL(STM_PND_UP_GRS)));
			actionStms.put(STM_PND_UP_MIC, prepareStatement(getSQL(STM_PND_UP_MIC)));
			actionStms.put(STM_PND_UP_ROU, prepareStatement(getSQL(STM_PND_UP_ROU)));
			actionStms.put(STM_PND_UP_SCA, prepareStatement(getSQL(STM_PND_UP_SCA)));
			actionStms.put(STM_SPM_SELECT, prepareStatement(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_LLOAD:
			actionStms.put(STM_ACC_SELECT, prepareStatement(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_ADD_INSERT, prepareStatement(getSQL(STM_ADD_INSERT)));
			actionStms.put(STM_ADD_SL_CID, prepareStatement(getSQL(STM_ADD_SL_CID)));
			actionStms.put(STM_ADD_SL_LST, prepareStatement(getSQL(STM_ADD_SL_LST)));
			actionStms.put(STM_ADD_SL_ORD, prepareStatement(getSQL(STM_ADD_SL_ORD)));
			actionStms.put(STM_CD1_SELECT, prepareStatement(getSQL(STM_CD1_SELECT)));
			actionStms.put(STM_CD2_SELECT, prepareStatement(getSQL(STM_CD2_SELECT)));
			actionStms.put(STM_CD3_SELECT, prepareStatement(getSQL(STM_CD3_SELECT)));
			actionStms.put(STM_CD4_SELECT, prepareStatement(getSQL(STM_CD4_SELECT)));
			actionStms.put(STM_CMT_INSERT, prepareStatement(getSQL(STM_CMT_INSERT)));
			actionStms.put(STM_CMT_UPDATE, prepareStatement(getSQL(STM_CMT_UPDATE)));
			actionStms.put(STM_CSE_INSERT, prepareStatement(getSQL(STM_CSE_INSERT)));
			actionStms.put(STM_CSE_SL_CID, prepareStatement(getSQL(STM_CSE_SL_CID)));
			actionStms.put(STM_CSE_SL_WLD, prepareStatement(getSQL(STM_CSE_SL_WLD)));
			actionStms.put(STM_CSE_SL_SPE, prepareStatement(getSQL(STM_CSE_SL_SPE)));
			actionStms.put(STM_CSE_UPDATE, prepareStatement(getSQL(STM_CSE_UPDATE)));
			actionStms.put(STM_ERR_DELETE, prepareStatement(getSQL(STM_ERR_DELETE)));
			actionStms.put(STM_ERR_INSERT, prepareStatement(getSQL(STM_ERR_INSERT)));
			actionStms.put(STM_ERR_SL_FXD, prepareStatement(getSQL(STM_ERR_SL_FXD)));
			actionStms.put(STM_FAC_SELECT, prepareStatement(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_FRZ_INSERT, prepareStatement(getSQL(STM_FRZ_INSERT)));
			actionStms.put(STM_FRZ_UPDATE, prepareStatement(getSQL(STM_FRZ_UPDATE)));
			actionStms.put(STM_ORD_INSERT, prepareStatement(getSQL(STM_ORD_INSERT)));
			actionStms.put(STM_ORD_UPDATE, prepareStatement(getSQL(STM_ORD_UPDATE)));
			actionStms.put(STM_ORG_SELECT, prepareStatement(getSQL(STM_ORG_SELECT)));
			actionStms.put(STM_ORM_SELECT, prepareStatement(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_SPE_INSERT, prepareStatement(getSQL(STM_SPE_INSERT)));
			actionStms.put(STM_SPE_SELECT, prepareCallables(getSQL(STM_SPE_SELECT)));
			actionStms.put(STM_SPE_UPDATE, prepareStatement(getSQL(STM_SPE_UPDATE)));
			actionStms.put(STM_SPM_SELECT, prepareStatement(getSQL(STM_SPM_SELECT)));
			break;
		case LibConstants.ACTION_LLOGIN:
			actionStms.put(STM_PRS_SL_PID, prepareStatement(getSQL(STM_PRS_SL_PID)));
			break;
		case LibConstants.ACTION_LSYNC:
			actionStms.put(STM_ACC_INSERT, prepareStatement(getSQL(STM_ACC_INSERT)));
			actionStms.put(STM_ACC_SELECT, prepareStatement(getSQL(STM_ACC_SELECT)));
			actionStms.put(STM_ACC_UPDATE, prepareStatement(getSQL(STM_ACC_UPDATE)));
			actionStms.put(STM_FAC_INSERT, prepareStatement(getSQL(STM_FAC_INSERT)));
			actionStms.put(STM_FAC_SELECT, prepareStatement(getSQL(STM_FAC_SELECT)));
			actionStms.put(STM_FAC_UPDATE, prepareStatement(getSQL(STM_FAC_UPDATE)));
			actionStms.put(STM_ORM_INSERT, prepareStatement(getSQL(STM_ORM_INSERT)));
			actionStms.put(STM_ORM_SELECT, prepareStatement(getSQL(STM_ORM_SELECT)));
			actionStms.put(STM_ORM_UPDATE, prepareStatement(getSQL(STM_ORM_UPDATE)));
			actionStms.put(STM_PRS_INSERT, prepareStatement(getSQL(STM_PRS_INSERT)));
			actionStms.put(STM_PRS_SELECT, prepareStatement(getSQL(STM_PRS_SELECT)));
			actionStms.put(STM_PRS_UPDATE, prepareStatement(getSQL(STM_PRS_UPDATE)));
			actionStms.put(STM_SPM_INSERT, prepareStatement(getSQL(STM_SPM_INSERT)));
			actionStms.put(STM_SPM_SELECT, prepareStatement(getSQL(STM_SPM_SELECT)));
			actionStms.put(STM_SPM_UPDATE, prepareStatement(getSQL(STM_SPM_UPDATE)));
			break;
		case LibConstants.ACTION_LVAL5:
			actionStms.put(STM_ADD_SL_SPG, prepareStatement(getSQL(STM_ADD_SL_SPG)));
			actionStms.put(STM_FRZ_SL_SU5, prepareStatement(getSQL(STM_FRZ_SL_SU5)));
			actionStms.put(STM_SPG_SL_SU5, prepareStatement(getSQL(STM_SPG_SL_SU5)));
			actionStms.put(STM_SPG_UPD_V5, prepareStatement(getSQL(STM_SPG_UPD_V5)));
			break;
		}
	}
}