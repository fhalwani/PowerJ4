package ca.powerj;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

class LValue5 {
	final String className = "Value5";
	LBase pj;
	DPowerJ dbPowerJ;

	LValue5(LBase pj) {
		this.pj = pj;
		dbPowerJ = pj.dbPowerJ;
		pj.log(LConstants.ERROR_NONE, className,
				pj.dates.formatter(LDates.FORMAT_DATETIME) + " - Value5 Manager Started...");
		dbPowerJ.prepareValue5();
		update();
		close();
	}

	private void close() {
		dbPowerJ.closeStms(false);
		LBase.busy.set(false);
	}

	private void update() {
		final byte V5_ROWID = 0;
		final byte V5_QTY = 1;
		final byte V5_VAL1 = 2;
		final byte V5_VAL2 = 3;
		final byte V5_VAL3 = 4;
		final byte V5_VAL4 = 5;
		byte interval = pj.setup.getByte(LSetup.VAR_V5_INTERVAL);
		byte noYears = pj.setup.getByte(LSetup.VAR_V5_UPDATE);
		boolean[] isActive = { false, false, pj.setup.getBoolean(LSetup.VAR_CODER1_ACTIVE),
				pj.setup.getBoolean(LSetup.VAR_CODER2_ACTIVE), pj.setup.getBoolean(LSetup.VAR_CODER3_ACTIVE),
				pj.setup.getBoolean(LSetup.VAR_CODER4_ACTIVE) };
		int avg = 0;
		int count = 0;
		int value5 = 0;
		int fte5 = pj.setup.getInt(LSetup.VAR_V5_FTE); // 4644000
		Integer[] frozn = { 0, 0, 0, 0, 0, 0 };
		Integer[] total = { 0, 0, 0, 0, 0, 0 };
		Calendar calLast = pj.dates.setMidnight(pj.setup.getLong(LSetup.VAR_V5_LAST));
		Calendar calNext = pj.dates.setMidnight(0);
		int noMonths = pj.dates.getNoMonths(calLast, calNext);
		if (noMonths < interval) {
			return;
		}
		ArrayList<Integer[]> specimens = new ArrayList<Integer[]>();
		ResultSet rst = null;
		try {
			if (noYears < 1) {
				noYears = 1;
			} else if (noYears > 3) {
				noYears = 3;
			}
			calNext.set(Calendar.DAY_OF_MONTH, 1);
			calLast.setTimeInMillis(calNext.getTimeInMillis());
			calLast.add(Calendar.YEAR, -noYears);
			dbPowerJ.setTime(DPowerJ.STM_SPG_SL_SU5, 1, calLast.getTimeInMillis());
			dbPowerJ.setTime(DPowerJ.STM_SPG_SL_SU5, 2, calNext.getTimeInMillis());
			rst = dbPowerJ.getResultSet(DPowerJ.STM_SPG_SL_SU5);
			while (rst.next()) {
				if (rst.getInt("QTY") > 0) {
					Integer[] specimen = { rst.getInt("SGID"), rst.getInt("QTY"),
							pj.numbers.doubleToInt(2, rst.getDouble("SPV1") * 100),
							pj.numbers.doubleToInt(2, rst.getDouble("SPV2") * 100),
							pj.numbers.doubleToInt(2, rst.getDouble("SPV3") * 100),
							pj.numbers.doubleToInt(2, rst.getDouble("SPV4") * 100) };
					specimens.add(specimen);
					total[V5_VAL1] += specimen[V5_VAL1];
					total[V5_VAL2] += specimen[V5_VAL2];
					total[V5_VAL3] += specimen[V5_VAL3];
					total[V5_VAL4] += specimen[V5_VAL4];
				}
			}
			dbPowerJ.closeRst(rst);
			dbPowerJ.setTime(DPowerJ.STM_FRZ_SL_SU5, 1, calLast.getTimeInMillis());
			dbPowerJ.setTime(DPowerJ.STM_FRZ_SL_SU5, 2, calNext.getTimeInMillis());
			rst = dbPowerJ.getResultSet(DPowerJ.STM_FRZ_SL_SU5);
			while (rst.next()) {
				if (rst.getInt("QTY") > 0) {
					frozn[V5_QTY] = rst.getInt("QTY");
					frozn[V5_VAL1] = pj.numbers.doubleToInt(2, rst.getDouble("FRV1") * 100);
					frozn[V5_VAL2] = pj.numbers.doubleToInt(2, rst.getDouble("FRV2") * 100);
					frozn[V5_VAL3] = pj.numbers.doubleToInt(2, rst.getDouble("FRV3") * 100);
					frozn[V5_VAL4] = pj.numbers.doubleToInt(2, rst.getDouble("FRV4") * 100);
					total[V5_VAL1] += frozn[V5_VAL1];
					total[V5_VAL2] += frozn[V5_VAL2];
					total[V5_VAL3] += frozn[V5_VAL3];
					total[V5_VAL4] += frozn[V5_VAL4];
				}
			}
			dbPowerJ.closeRst(rst);
			for (int i = 0; i < specimens.size(); i++) {
				Integer[] specimen = specimens.get(i);
				avg = 0;
				count = 0;
				for (byte j = V5_VAL1; j <= V5_VAL4; j++) {
					if (total[j] > 0 && isActive[j]) {
						avg += (specimen[j] / total[j]);
						count++;
					}
				}
				if (count > 0 && specimen[V5_QTY] > 0) {
					avg = avg / count;
					value5 = avg * fte5 / specimen[V5_QTY];
					if (value5 > 0) {
						dbPowerJ.setInt(DPowerJ.STM_SPG_UPD_V5, 1, value5);
						dbPowerJ.setInt(DPowerJ.STM_SPG_UPD_V5, 2, specimen[V5_ROWID]);
						dbPowerJ.execute(DPowerJ.STM_SPG_UPD_V5);
					}
				}
			}
			if (frozn[V5_QTY] > 0) {
				avg = 0;
				count = 0;
				for (byte j = V5_VAL1; j <= V5_VAL4; j++) {
					if (total[j] > 0 && isActive[j]) {
						avg += (frozn[j] / total[j]);
						count++;
					}
				}
				if (count > 0 && frozn[V5_QTY] > 0) {
					avg = avg / count;
					value5 = avg * fte5 / frozn[V5_QTY];
					if (value5 > 0) {
						pj.setup.setInt(LSetup.VAR_V5_FROZEN, value5);
					}
				}
			}
			pj.setup.setLong(LSetup.VAR_V5_LAST, calNext.getTimeInMillis());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, className, e);
		} finally {
			dbPowerJ.closeRst(rst);
		}
	}
}