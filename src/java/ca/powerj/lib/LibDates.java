package ca.powerj.lib;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ca.powerj.database.DBPowerj;

public class LibDates {
	public static final byte FORMAT_DATE = 1;
	public static final byte FORMAT_DATELONG = 2;
	public static final byte FORMAT_DATESHORT = 3;
	public static final byte FORMAT_DATETIME = 4;
	public static final byte FORMAT_SCHED = 5;
	public static final byte FORMAT_TIME = 6;
	public static final int ONE_DAY = 86400000; // in milliseconds
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yy", Locale.getDefault());
	private final SimpleDateFormat longFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
	private final SimpleDateFormat tinyFormat = new SimpleDateFormat("d/M", Locale.getDefault());
	private final SimpleDateFormat dateTime = new SimpleDateFormat("d/M/yy H:mm", Locale.getDefault());
	private final SimpleDateFormat schedFormat = new SimpleDateFormat("EEE d", Locale.getDefault());
	private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);
	private int workHours = 0;
	private DBPowerj dbPowerJ;
	
	LibDates(LibBase base, DBPowerj dbPowerJ) {
		this.dbPowerJ = dbPowerJ;
		workHours = base.setup.getByte(LibSetup.VAR_CLOSING) - base.setup.getByte(LibSetup.VAR_OPENING);
		if (workHours < 8) {
			workHours = 8;
		} else if (workHours > 24) {
			workHours = 24;
		}
	}

	public String formatter(byte format) {
		Date date = new Date();
		return formatter(date, format);
	}

	public String formatter(Calendar cal, byte format) {
		if (cal == null) {
			cal = Calendar.getInstance();
		}
		return formatter(cal.getTimeInMillis(), format);
	}

	public String formatter(long ms, byte format) {
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

	public int getBusinessDays(Calendar calStart, Calendar calEnd) {
		return getBusinessDays(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
	}

	public int getBusinessDays(long start, long end) {
		return (dbPowerJ.getWorkdayNo(end) - dbPowerJ.getWorkdayNo(start));
	}

	public short getBusinessHours(long start, long end) {
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(start);
		calEnd.setTimeInMillis(end);
		return getBusinessHours(calStart, calEnd);
	}

	public short getBusinessHours(Calendar calStart, Calendar calEnd) {
		// Calculate Difference in working hours between 2 dates
		// Saturday, Sunday and statutory holidays are skipped
		int hours = workHours * getBusinessDays(calStart, calEnd);
		hours += (calEnd.get(Calendar.HOUR_OF_DAY) - calStart.get(Calendar.HOUR_OF_DAY));
		if (hours < 0) {
			hours += workHours;
		} else if (hours > Short.MAX_VALUE) {
			hours = Short.MAX_VALUE;
		}
		return (short) hours;
	}

	public int getNoDays(Calendar calStart, Calendar calEnd) {
		if (calEnd.get(Calendar.YEAR) - calStart.get(Calendar.YEAR) > 0
				&& calEnd.get(Calendar.DAY_OF_YEAR) > 1) {
			return getNoDays(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
		}
		return (calEnd.get(Calendar.DAY_OF_YEAR) - calStart.get(Calendar.DAY_OF_YEAR));
	}

	public int getNoDays(long start, long end) {
		int i = 0;
		if (end - start > ONE_DAY) {
			long noDays = (end - start) / ONE_DAY;
			if (noDays > Integer.MAX_VALUE) {
				i = Integer.MAX_VALUE;
			} else {
				i = (int) noDays;
			}
		} else {
			Calendar calStart = Calendar.getInstance();
			Calendar calEnd = Calendar.getInstance();
			calStart.setTimeInMillis(start);
			calEnd.setTimeInMillis(end);
			i = getNoDays(calStart, calEnd);
		}
		return i;
	}

	public int getNoMonths(Calendar calStart, Calendar calEnd) {
		if (calEnd.get(Calendar.YEAR) != calStart.get(Calendar.YEAR)) {
			return ((calEnd.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)) * 12) + calEnd.get(Calendar.MONTH)
					- calStart.get(Calendar.MONTH);
		}
		return (calEnd.get(Calendar.MONTH) - calStart.get(Calendar.MONTH));
	}

	public int getNoMonths(long start, long end) {
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(start);
		calEnd.setTimeInMillis(end);
		return getNoMonths(calStart, calEnd);
	}

	public long getNextBusinessDay(Calendar cal) {
		return getNextBusinessDay(cal.getTimeInMillis());
	}

	public long getNextBusinessDay(long date) {
		return dbPowerJ.getWorkdayNext(date);
	}

	public long getPreviousBusinessDay(Calendar cal) {
		return getPreviousBusinessDay(cal.getTimeInMillis());
	}

	public long getPreviousBusinessDay(long date) {
		return dbPowerJ.getWorkdayPrevious(date);
	}

	public Calendar setMidnight(Calendar calendar) {
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

	public Calendar setMidnight(long time) {
		Calendar calNow = Calendar.getInstance();
		if (time > 0) {
			calNow.setTimeInMillis(time);
		}
		return setMidnight(calNow);
	}
}