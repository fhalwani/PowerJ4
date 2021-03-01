package ca.powerj.lib;
import java.util.Calendar;

import ca.powerj.app.PJServer;
import ca.powerj.data.WorkdayData;

public class LibWorkdays {
	private final String className = "Workdays";
	private PJServer base;

	public LibWorkdays(PJServer base) {
		this.base = base;
		base.setBusy(true);
		base.log(LibConstants.ERROR_NONE, className,
				base.dates.formatter(LibDates.FORMAT_DATETIME) + " - Workdays Manager Started...");
		base.dbPowerJ.setStatements(LibConstants.ACTION_LDAYS);
		setWorkdays();
	}

	private void addWorkdays(int dayID, int dayNo, Calendar calDate, long maxDate) {
		final byte DATE_NEWYEAR = 0;
		final byte DATE_FAMILY = 1;
		final byte DATE_GOOD = 2;
		final byte DATE_EASTER = 3;
		final byte DATE_VICTORIA = 4;
		final byte DATE_CANADA = 5;
		final byte DATE_CIVIC = 6;
		final byte DATE_LABOUR = 7;
		final byte DATE_THANKS = 8;
		final byte DATE_REMEMBER = 9;
		final byte DATE_XMAS = 10;
		final byte DATE_BOXING = 11;
		final byte DATE_WEEKEND = 0;
		final byte DATE_HOLIDAY = 1;
		final byte DATE_WEEKDAY = 2;
		final String[] dayTypes = { "E", "H", "D" };
		boolean saturdayOff = base.setup.getBoolean(LibSetup.VAR_SAT_OFF);
		boolean sundayOff = base.setup.getBoolean(LibSetup.VAR_SUN_OFF);
		boolean[] blnMatched = { false, false, false, false, false, false, false,
				false, false, false, false, false };
		byte dayType = DATE_WEEKDAY;
		int month = 0;
		int year = 0;
		int day = 0;
		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;
		int e = 0;
		int f = 0;
		int g = 0;
		int h = 0;
		int i = 0;
		int k = 0;
		int l = 0;
		int m = 0;
		Calendar easterMonday = Calendar.getInstance();
		while (calDate.getTimeInMillis() < maxDate) {
			if (year < calDate.get(Calendar.YEAR)) {
				year = calDate.get(Calendar.YEAR);
				for (int n = 0; n < 12; n++) {
					blnMatched[n] = false;
				}
				a = year % 19;
				b = year / 100;
				c = year % 100;
				d = b / 4;
				e = b % 4;
				f = (b + 8) / 25;
				g = (b - f + 1) / 3;
				h = (19 * a + b - d - g + 15) % 30;
				i = c / 4;
				k = c % 4;
				l = (32 + 2 * e + 2 * i - h - k) % 7;
				m = (a + 11 * h + 22 * l) / 451;
				month = ((h + l - 7 * m + 114) / 31) - 1;
				day = ((h + l - 7 * m + 114) % 31) + 1;
				easterMonday.set(Calendar.YEAR, year);
				easterMonday.set(Calendar.MONTH, month);
				easterMonday.set(Calendar.DAY_OF_MONTH, day);
				easterMonday.add(Calendar.DAY_OF_YEAR, -2);
			}
			dayType = DATE_WEEKDAY;
			switch (calDate.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SATURDAY:
				if (saturdayOff) {
					dayType = DATE_WEEKEND;
				}
				break;
			case Calendar.SUNDAY:
				if (sundayOff) {
					dayType = DATE_WEEKEND;
				}
				break;
			default:
				switch (calDate.get(Calendar.MONTH)) {
				case Calendar.JANUARY:
					if (!blnMatched[DATE_NEWYEAR]) {
						if (calDate.get(Calendar.DAY_OF_MONTH) == 1) {
							// New Year is on a weekday
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_NEWYEAR] = true;
						} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_MONTH) < 4) {
								// New Year was last Saturday or Sunday
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_NEWYEAR] = true;
							}
						}
					}
					break;
				case Calendar.FEBRUARY:
					if (!blnMatched[DATE_FAMILY]) {
						if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 3) {
								// Family Day is 3rd Monday of week
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_FAMILY] = true;
							}
						}
					}
					break;
				case Calendar.MARCH:
				case Calendar.APRIL:
					if (!blnMatched[DATE_GOOD]) {
						if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
							if (calDate.get(Calendar.DAY_OF_YEAR) == easterMonday.get(Calendar.DAY_OF_YEAR)) {
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_GOOD] = true;
							}
						}
					} else if (!blnMatched[DATE_EASTER]) {
						if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_YEAR) == (easterMonday.get(Calendar.DAY_OF_YEAR) + 3)) {
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_EASTER] = true;
							}
						}
					}
					break;
				case Calendar.MAY:
					if (!blnMatched[DATE_VICTORIA]) {
						if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_MONTH) > 17 && calDate.get(Calendar.DAY_OF_MONTH) < 25) {
								// Victoria Day is Monday before May 25th
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_VICTORIA] = true;
							}
						}
					}
					break;
				case Calendar.JULY:
					if (!blnMatched[DATE_CANADA]) {
						if (calDate.get(Calendar.DAY_OF_MONTH) == 1) {
							// Canada Day is on a weekday
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_CANADA] = true;
						} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_MONTH) < 4) {
								// Canada Day was last Saturday or Sunday
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_CANADA] = true;
							}
						}
					}
					break;
				case Calendar.AUGUST:
					if (!blnMatched[DATE_CIVIC]) {
						if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 1) {
								// Civic holiday is 1st Monday of August
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_CIVIC] = true;
							}
						}
					}
					break;
				case Calendar.SEPTEMBER:
					if (!blnMatched[DATE_LABOUR]) {
						if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 1) {
								// Labour Day is 1st Monday of September
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_LABOUR] = true;
							}
						}
					}
					break;
				case Calendar.OCTOBER:
					if (!blnMatched[DATE_THANKS]) {
						if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
							if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 2) {
								// Thanksgiving is 2nd Monday of October
								dayType = DATE_HOLIDAY;
								blnMatched[DATE_THANKS] = true;
							}
						}
					}
					break;
				case Calendar.NOVEMBER:
					if (!blnMatched[DATE_REMEMBER]) {
						if (calDate.get(Calendar.DAY_OF_MONTH) == 11) {
							// Remembrance Day is always November 11th, no Monday substitute
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_REMEMBER] = true;
						}
					}
					break;
				case Calendar.DECEMBER:
					if (!blnMatched[DATE_XMAS]) {
						if (calDate.get(Calendar.DAY_OF_MONTH) == 25) {
							// Christmas is on a weekday
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_XMAS] = true;
						} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
								&& (calDate.get(Calendar.DAY_OF_MONTH) == 26
										|| calDate.get(Calendar.DAY_OF_MONTH) == 27)) {
							// Christmas was last Saturday or Sunday
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_XMAS] = true;
						}
					} else if (!blnMatched[DATE_BOXING]) {
						if (calDate.get(Calendar.DAY_OF_MONTH) == 26) {
							// Boxing Day is on a weekday
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_BOXING] = true;
						} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
								&& calDate.get(Calendar.DAY_OF_MONTH) == 28) {
							// Boxing Day was last Saturday
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_BOXING] = true;
						} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY
								&& (calDate.get(Calendar.DAY_OF_MONTH) == 27
										|| calDate.get(Calendar.DAY_OF_MONTH) == 28)) {
							// Boxing Day was last Sunday or Monday
							dayType = DATE_HOLIDAY;
							blnMatched[DATE_BOXING] = true;
						}
					}
					break;
				default:
					dayType = DATE_WEEKDAY;
				}
			}
			if (base.dbPowerJ.setWorkday(dayID, dayNo, calDate.getTimeInMillis(), dayTypes[dayType]) > 0) {
				dayID++;
				if (dayType == DATE_WEEKDAY) {
					dayNo++;
				}
				if (dayID > 0 && dayID % 100 == 0) {
					if (base.isStopping()) {
						break;
					} else {
						try {
							Thread.sleep(LibConstants.SLEEP_TIME);
						} catch (InterruptedException ignore) {
						}
					}
				}
			} else {
				break;
			}
			calDate.add(Calendar.DAY_OF_YEAR, 1);
		}
	}

	private void close() {
		base.dbPowerJ.closeStms();
		base.setBusy(false);
	}

	private void setWorkdays() {
		int dayID = 0;
		int dayNo = 0;
		WorkdayData workday = base.dbPowerJ.getLastWorkday();
		Calendar calDate = base.dates.setMidnight(null);
		Calendar maxDate = base.dates.setMidnight(null);
		calDate.set(Calendar.YEAR, 2009);
		calDate.set(Calendar.MONTH, Calendar.DECEMBER);
		calDate.set(Calendar.DAY_OF_MONTH, 31);
		maxDate.add(Calendar.MONTH, 3);
		maxDate.set(Calendar.DAY_OF_MONTH, 1);
		dayID = workday.getDayID() +1;
		dayNo = workday.getDayNo() +1;
		calDate.setTimeInMillis(workday.getTime());
		calDate.add(Calendar.DAY_OF_YEAR, 1);
		if (!base.isStopping()) {
			if (calDate.getTimeInMillis() < maxDate.getTimeInMillis()) {
				addWorkdays(dayID, dayNo, calDate, maxDate.getTimeInMillis());
				base.setup.setInt(LibSetup.VAR_MONTH_RUN, base.dates.setMidnight(null).get(Calendar.MONTH));
			}
		}
		close();
	}
}