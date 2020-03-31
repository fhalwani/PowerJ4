package ca.powerj;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class LDates {
	static final byte FORMAT_DATE = 1;
	static final byte FORMAT_DATELONG = 2;
	static final byte FORMAT_DATESHORT = 3;
	static final byte FORMAT_DATETIME = 4;
	static final byte FORMAT_SCHED = 5;
	static final byte FORMAT_TIME = 6;
	static final int ONE_DAY = 86400000; // 24 * 60 * 60 * 1000
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yy", Locale.getDefault());
	private final SimpleDateFormat longFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
	private final SimpleDateFormat tinyFormat = new SimpleDateFormat("d/M", Locale.getDefault());
	private final SimpleDateFormat dateTime = new SimpleDateFormat("d/M/yy H:mm", Locale.getDefault());
	private final SimpleDateFormat schedFormat = new SimpleDateFormat("EEE d", Locale.getDefault());
	private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	static int workHours = 0;
	private LBase pj;

	LDates(LBase pj) {
		this.pj = pj;
		workHours = pj.setup.getByte(LSetup.VAR_CLOSING) - pj.setup.getByte(LSetup.VAR_OPENING);
		if (workHours < 8) {
			workHours = 8;
		} else if (workHours > 24) {
			workHours = 24;
		}
	}

	String formatter(byte format) {
		Date date = new Date();
		return formatter(date, format);
	}

	String formatter(Calendar cal, byte format) {
		if (cal == null) {
			cal = Calendar.getInstance();
		}
		return formatter(cal.getTimeInMillis(), format);
	}

	String formatter(long ms, byte format) {
		String strDate = "";
		switch (format) {
		case FORMAT_DATE:
			strDate = dateFormat.format(ms);
			break;
		case FORMAT_TIME:
			strDate = timeFormat.format(ms);
			break;
		case FORMAT_DATETIME:
			strDate = dateTime.format(ms);
			break;
		case FORMAT_DATELONG:
			strDate = longFormat.format(ms);
			break;
		case FORMAT_SCHED:
			strDate = schedFormat.format(ms);
			break;
		default:
			strDate = tinyFormat.format(ms);
		}
		return strDate;
	}

	public String formatter(Date date, byte format) {
		if (date == null) {
			date = new Date();
		}
		return formatter(date.getTime(), format);
	}

	int getBusinessDays(Calendar calStart, Calendar calEnd) {
		return getBusinessDays(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
	}

	int getBusinessDays(long start, long end) {
		int n = 0;
		if (getNoDays(start, end) != 0) {
			pj.dbPowerJ.setDate(pj.pjStms.get(DPowerJ.STM_WDY_SL_DTE), 1, end);
			n = pj.dbPowerJ.getInt(pj.pjStms.get(DPowerJ.STM_WDY_SL_DTE));
			pj.dbPowerJ.setDate(pj.pjStms.get(DPowerJ.STM_WDY_SL_DTE), 1, start);
			n -= pj.dbPowerJ.getInt(pj.pjStms.get(DPowerJ.STM_WDY_SL_DTE));
		}
		return n;
	}

	short getBusinessHours(long start, long end) {
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(start);
		calEnd.setTimeInMillis(end);
		return getBusinessHours(calStart, calEnd);
	}

	short getBusinessHours(Calendar calStart, Calendar calEnd) {
		// Calculate Difference in working hours between 2 dates
		// Saturday, Sunday and statutory holidays are skipped
		int hours = workHours * getBusinessDays(calStart, calEnd);
		hours += (calEnd.get(Calendar.HOUR_OF_DAY) - calStart.get(Calendar.HOUR_OF_DAY));
		if (hours < 0) {
			hours = 0;
		}
		if (hours > Short.MAX_VALUE) {
			hours = Short.MAX_VALUE;
		}
		return (short) hours;
	}

	int getNoDays(Calendar calStart, Calendar calEnd) {
		return getNoDays(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
	}

	int getNoDays(long start, long end) {
		int i = 0;
		long noDays = (end - start) / ONE_DAY;
		if (noDays > Integer.MAX_VALUE) {
			i = Integer.MAX_VALUE;
		} else {
			i = (int) noDays;
		}
		return i;
	}

	int getNoMonths(Calendar calStart, Calendar calEnd) {
		if (calEnd.get(Calendar.YEAR) != calStart.get(Calendar.YEAR)) {
			return ((calEnd.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)) * 12) + calEnd.get(Calendar.MONTH)
					- calStart.get(Calendar.MONTH);
		}
		return (calEnd.get(Calendar.MONTH) - calStart.get(Calendar.MONTH));
	}

	int getNoMonths(long start, long end) {
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(start);
		calEnd.setTimeInMillis(end);
		return getNoMonths(calStart, calEnd);
	}

	long getNextBusinessDay(Calendar cal) {
		pj.dbPowerJ.setTime(pj.pjStms.get(DPowerJ.STM_WDY_SL_NXT), 1, cal.getTimeInMillis());
		return pj.dbPowerJ.getTime(pj.pjStms.get(DPowerJ.STM_WDY_SL_NXT));
	}

	long getNextBusinessDay(long thisDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(thisDate);
		return getNextBusinessDay(cal);
	}

	long getPreviousBusinessDay(Calendar cal) {
		pj.dbPowerJ.setTime(pj.pjStms.get(DPowerJ.STM_WDY_SL_PRV), 1, cal.getTimeInMillis());
		return pj.dbPowerJ.getTime(pj.pjStms.get(DPowerJ.STM_WDY_SL_PRV));
	}

	long getPreviousBusinessDay(long thisDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(thisDate);
		return getPreviousBusinessDay(cal);
	}

	Calendar setMidnight(Calendar calendar) {
		Calendar calNow = Calendar.getInstance();
		if (calendar != null) {
			calNow.setTimeInMillis(calendar.getTimeInMillis());
		}
		calNow.set(Calendar.HOUR_OF_DAY, 0);
		calNow.set(Calendar.MINUTE, 0);
		calNow.set(Calendar.SECOND, 0);
		calNow.set(Calendar.MILLISECOND, 0);
		return calNow;
	}

	Calendar setMidnight(long time) {
		Calendar calNow = Calendar.getInstance();
		if (time > 0) {
			calNow.setTimeInMillis(time);
		}
		return setMidnight(calNow);
	}

	/** Calculate and display the next schedule to scan the database **/
	void setNextUpdate() {
		Calendar calendar = Calendar.getInstance();
		// First, set last update
		if (calendar.getTimeInMillis() - pj.nextUpdate <= 0) {
			// Avoid updates time shifting if timer fires early
			pj.lastUpdate = pj.nextUpdate;
		} else {
			// Next update was in the past (overdue) because software was powered off
			pj.lastUpdate = calendar.getTimeInMillis();
		}
		calendar.setTimeInMillis(pj.lastUpdate + pj.updateInterval);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if (calendar.get(Calendar.HOUR_OF_DAY) > pj.setup.getByte(LSetup.VAR_CLOSING)) {
			// Sleep from 11 pm till 6 am next business day
			calendar.setTimeInMillis(getNextBusinessDay(calendar));
			// Wake up at 6:30 am next day
			calendar.set(Calendar.HOUR_OF_DAY, pj.setup.getByte(LSetup.VAR_OPENING));
			calendar.add(Calendar.MILLISECOND, pj.updateInterval);
		}
		pj.nextUpdate = calendar.getTimeInMillis();
	}
}