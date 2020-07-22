package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

class LValue5 {
	final String className = "Code5";
	private Hashtable<Byte, PreparedStatement> pjStms = null;
	LBase pj;
	DPowerJ dbPowerJ;

	LValue5(LBase pj) {
		LBase.busy.set(true);
		this.pj = pj;
		dbPowerJ = pj.dbPowerJ;
		pj.log(LConstants.ERROR_NONE, className,
				pj.dates.formatter(LDates.FORMAT_DATETIME) + " - Code5 Manager Started...");
		pjStms = dbPowerJ.prepareStatements(LConstants.ACTION_LVAL5);
		update();
		close();
	}

	private void close() {
		if (dbPowerJ != null && pjStms != null) {
			dbPowerJ.close(pjStms);
		}
		LBase.busy.set(false);
	}

	private void update() {
		byte interval = pj.setup.getByte(LSetup.VAR_V5_INTERVAL);
		if (interval < 1) {
			interval = 1;
		} else if (interval > 24) {
			interval = 24;
		}
		Calendar calFrom = pj.dates.setMidnight(pj.setup.getLong(LSetup.VAR_V5_LAST));
		Calendar calTo = pj.dates.setMidnight(0);
		int noMonths = pj.dates.getNoMonths(calFrom, calTo);
		if (noMonths < interval) {
			return;
		}
		byte noYears = pj.setup.getByte(LSetup.VAR_V5_UPDATE);
		double annualCAP = pj.setup.getShort(LSetup.VAR_CODER1_FTE);
		double annualW2Q = pj.setup.getShort(LSetup.VAR_CODER2_FTE);
		double annualRCP = pj.setup.getShort(LSetup.VAR_CODER3_FTE);
		double annualCPT = pj.setup.getShort(LSetup.VAR_CODER4_FTE);
		double annualCO5 = pj.setup.getInt(LSetup.VAR_V5_FTE);
		double totalCAP = 0;
		double totalW2Q = 0;
		double totalRCP = 0;
		double totalCPT = 0;
		double totalCO5 = 0;
		double avgCAP = 0;
		double avgW2Q = 0;
		double avgRCP = 0;
		double avgCPT = 0;
		double avgCO5 = 0;
		double temp = 0;
		OSpecGroup5 specimen = new OSpecGroup5();
		OSpecGroup5 specFsec = new OSpecGroup5();
		HashMap<Short, OSpecGroup5> specimens = new HashMap<Short, OSpecGroup5>();
		ResultSet rst = null;
		try {
			if (noYears < 1) {
				noYears = 1;
			} else if (noYears > 3) {
				noYears = 3;
			}
			// Specimen groups
			calTo.set(Calendar.DAY_OF_MONTH, 1);
			calFrom.setTimeInMillis(calTo.getTimeInMillis());
			calFrom.add(Calendar.YEAR, -noYears);
			dbPowerJ.setTime(pjStms.get(DPowerJ.STM_SPG_SL_SU5), 1, calFrom.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(DPowerJ.STM_SPG_SL_SU5), 2, calTo.getTimeInMillis());
			rst = dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SPG_SL_SU5));
			while (rst.next()) {
				if (rst.getInt("qty") > 0) {
					specimen = new OSpecGroup5();
					specimen.grpID = rst.getShort("sgid");
					specimen.qty = rst.getInt("QTY");
					specimen.totalCAP = rst.getDouble("spv1");
					specimen.totalW2Q = rst.getDouble("spv2");
					specimen.totalRCP = rst.getDouble("spv3");
					specimen.totalCPT = rst.getDouble("spv4");
					specimen.name = rst.getString("sgdc");
					specimens.put(rst.getShort("sgid"), specimen);
					totalCAP += rst.getDouble("spv1");
					totalW2Q += rst.getDouble("spv2");
					totalRCP += rst.getDouble("spv3");
					totalCPT += rst.getDouble("spv4");
				}
			}
			dbPowerJ.close(rst);
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignore) {
			}
			// Additionals
			dbPowerJ.setTime(pjStms.get(DPowerJ.STM_ADD_SL_SPG), 1, calFrom.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(DPowerJ.STM_ADD_SL_SPG), 2, calTo.getTimeInMillis());
			rst = dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_SPG));
			while (rst.next()) {
				specimen = specimens.get(rst.getShort("sgid"));
				if (specimen != null) {
					specimen.totalCAP += rst.getDouble("adv1");
					specimen.totalW2Q += rst.getDouble("adv2");
					specimen.totalRCP += rst.getDouble("adv3");
					specimen.totalCPT += rst.getDouble("adv4");
					totalCAP += rst.getDouble("adv1");
					totalW2Q += rst.getDouble("adv2");
					totalRCP += rst.getDouble("adv3");
					totalCPT += rst.getDouble("adv4");
				}
			}
			dbPowerJ.close(rst);
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignore) {
			}
			// FSEC
			dbPowerJ.setTime(pjStms.get(DPowerJ.STM_FRZ_SL_SU5), 1, calFrom.getTimeInMillis());
			dbPowerJ.setTime(pjStms.get(DPowerJ.STM_FRZ_SL_SU5), 2, calTo.getTimeInMillis());
			rst = dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FRZ_SL_SU5));
			while (rst.next()) {
				if (rst.getInt("qty") > 0) {
					specFsec.grpID = 9999;
					specFsec.qty = rst.getInt("qty");
					specFsec.totalCAP = rst.getDouble("frv1");
					specFsec.totalW2Q = rst.getDouble("frv2");
					specFsec.totalRCP = rst.getDouble("frv3");
					specFsec.totalCPT = rst.getDouble("frv4");
					specFsec.name = "FSEC";
					totalCAP += rst.getDouble("frv1");
					totalW2Q += rst.getDouble("frv2");
					totalRCP += rst.getDouble("frv3");
					totalCPT += rst.getDouble("frv4");
				}
			}
			dbPowerJ.close(rst);
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignore) {
			}
			totalCO5 = (((totalCAP / annualCAP) +
					(totalW2Q / annualW2Q) +
					(totalRCP / annualRCP) +
					(totalCPT / annualCPT)) * annualCO5) / 4;
			if (specFsec.qty > 0) {
				avgCAP = specFsec.totalCAP / totalCAP;
				avgW2Q = specFsec.totalW2Q / totalW2Q;
				avgRCP = specFsec.totalRCP / totalRCP;
				avgCPT = specFsec.totalCPT / totalCPT;
				temp = avgCAP + avgW2Q + avgRCP + avgCPT;
				avgCO5 = temp / 4;
				temp = (avgCO5 * totalCO5) / specFsec.qty;
				specFsec.code5 = pj.numbers.doubleToInt(1, temp);
				if (specFsec.code5 > 0) {
					pj.setup.setInt(LSetup.VAR_V5_FROZEN, specFsec.code5);
				}
			}
			for (Entry<Short, OSpecGroup5> entry : specimens.entrySet()) {
				specimen = entry.getValue();
				avgCAP = specimen.totalCAP / totalCAP;
				avgW2Q = specimen.totalW2Q / totalW2Q;
				avgRCP = specimen.totalRCP / totalRCP;
				avgCPT = specimen.totalCPT / totalCPT;
				temp = avgCAP + avgW2Q + avgRCP + avgCPT;
				avgCO5 = temp / 4;
				temp = (avgCO5 * totalCO5) / specimen.qty;
				specimen.code5 = pj.numbers.doubleToInt(1, temp);
				if (specimen.code5 > 0) {
					dbPowerJ.setInt(pjStms.get(DPowerJ.STM_SPG_UPD_V5), 1, specimen.code5);
					dbPowerJ.setInt(pjStms.get(DPowerJ.STM_SPG_UPD_V5), 2, specimen.grpID);
					dbPowerJ.execute(pjStms.get(DPowerJ.STM_SPG_UPD_V5));
				}
			}
			pj.setup.setLong(LSetup.VAR_V5_LAST, calTo.getTimeInMillis());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbPowerJ.close(rst);
		}
	}
}