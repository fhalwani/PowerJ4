package ca.powerj.lib;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ca.powerj.app.PJServer;
import ca.powerj.data.SpecimenCode5Data;

public class LibCode5 {
	final String className = "Code5";
	private PJServer base;

	public LibCode5(PJServer base) {
		this.base = base;
		base.setBusy(true);
		base.log(LibConstants.ERROR_NONE, className,
				base.dates.formatter(LibDates.FORMAT_DATETIME) + " - Code5 Manager Started...");
		base.dbPowerJ.setStatements(LibConstants.ACTION_LVAL5);
		update();
		close();
	}

	private void close() {
		if (base.dbPowerJ != null) {
			base.dbPowerJ.closeStms();
		}
		base.setBusy(false);
	}

	private void update() {
		byte interval = base.setup.getByte(LibSetup.VAR_V5_INTERVAL);
		if (interval < 1) {
			interval = 1;
		} else if (interval > 24) {
			interval = 24;
		}
		Calendar calFrom = base.dates.setMidnight(base.setup.getLong(LibSetup.VAR_V5_LAST));
		Calendar calTo = base.dates.setMidnight(0);
		int noMonths = base.dates.getNoMonths(calFrom, calTo);
		if (noMonths < interval) {
			return;
		}
		byte noYears = base.setup.getByte(LibSetup.VAR_V5_UPDATE);
		double annualCAP = base.setup.getShort(LibSetup.VAR_CODER1_FTE);
		double annualW2Q = base.setup.getShort(LibSetup.VAR_CODER2_FTE);
		double annualRCP = base.setup.getShort(LibSetup.VAR_CODER3_FTE);
		double annualCPT = base.setup.getShort(LibSetup.VAR_CODER4_FTE);
		double annualCO5 = base.setup.getInt(LibSetup.VAR_V5_FTE);
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
		SpecimenCode5Data specimen = new SpecimenCode5Data();
		if (noYears < 1) {
			noYears = 1;
		} else if (noYears > 3) {
			noYears = 3;
		}
		calTo.set(Calendar.DAY_OF_MONTH, 1);
		calFrom.setTimeInMillis(calTo.getTimeInMillis());
		calFrom.add(Calendar.YEAR, -noYears);
		HashMap<Short, SpecimenCode5Data> specimens = base.dbPowerJ.getSpecimenGroupCode5(calFrom.getTimeInMillis(), calTo.getTimeInMillis());
		if (base.isStopping()) {
			return;
		} else {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
		HashMap<Short, SpecimenCode5Data> additionals = base.dbPowerJ.getAdditionalCode5(calFrom.getTimeInMillis(), calTo.getTimeInMillis());
		if (base.isStopping()) {
			return;
		} else {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
		SpecimenCode5Data additional = new SpecimenCode5Data();
		for (Map.Entry entry : additionals.entrySet()) {
			additional = (SpecimenCode5Data) entry.getValue();
			specimen = specimens.get(additional.getGrpID());
			if (specimen != null) {
				specimen.setTotalCAP(specimen.getTotalCAP() + additional.getTotalCAP());
				specimen.setTotalW2Q(specimen.getTotalW2Q() + additional.getTotalW2Q());
				specimen.setTotalRCP(specimen.getTotalRCP() + additional.getTotalRCP());
				specimen.setTotalCPT(specimen.getTotalCPT() + additional.getTotalCPT());
			}
		}
		additionals.clear();
		if (base.isStopping()) {
			return;
		} else {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
		SpecimenCode5Data frozens = base.dbPowerJ.getFrozenCode5(calFrom.getTimeInMillis(), calTo.getTimeInMillis());
		if (base.isStopping()) {
			return;
		} else {
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
		for (Map.Entry entry : specimens.entrySet()) {
			specimen = (SpecimenCode5Data) entry.getValue();
			if (specimen.getQty() < 1) {
				specimen.setQty(1);
			}
			totalCAP += specimen.getTotalCAP();
			totalW2Q += specimen.getTotalW2Q();
			totalRCP += specimen.getTotalRCP();
			totalCPT += specimen.getTotalCPT();
		}
		totalCAP += frozens.getTotalCAP();
		totalW2Q += frozens.getTotalW2Q();
		totalRCP += frozens.getTotalRCP();
		totalCPT += frozens.getTotalCPT();
		totalCO5 = (((totalCAP / annualCAP) + (totalW2Q / annualW2Q)
			+ (totalRCP / annualRCP) + (totalCPT / annualCPT))
			* annualCO5) / 4;
		if (frozens.getQty() > 0) {
			avgCAP = frozens.getTotalCAP() / totalCAP;
			avgW2Q = frozens.getTotalW2Q() / totalW2Q;
			avgRCP = frozens.getTotalRCP() / totalRCP;
			avgCPT = frozens.getTotalCPT() / totalCPT;
			temp = avgCAP + avgW2Q + avgRCP + avgCPT;
			avgCO5 = temp / 4;
			temp = (avgCO5 * totalCO5) / frozens.getQty();
			frozens.setCode5(base.numbers.doubleToInt(1, temp));
			if (frozens.getCode5() > 0) {
				base.setup.setInt(LibSetup.VAR_V5_FROZEN, frozens.getCode5());
			}
		}
		for (Entry<Short, SpecimenCode5Data> entry : specimens.entrySet()) {
			specimen = entry.getValue();
			avgCAP = specimen.getTotalCAP() / totalCAP;
			avgW2Q = specimen.getTotalW2Q() / totalW2Q;
			avgRCP = specimen.getTotalRCP() / totalRCP;
			avgCPT = specimen.getTotalCPT() / totalCPT;
			temp = avgCAP + avgW2Q + avgRCP + avgCPT;
			avgCO5 = temp / 4;
			temp = (avgCO5 * totalCO5) / specimen.getQty();
			specimen.setCode5(base.numbers.doubleToInt(1, temp));
			if (specimen.getCode5() > 0) {
				base.dbPowerJ.setSpecimenGroupValue5(specimen.getGrpID(), specimen.getCode5());
			}
		}
		base.setup.setLong(LibSetup.VAR_V5_LAST, calTo.getTimeInMillis());
	}
}