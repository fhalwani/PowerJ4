package ca.powerj;

import java.sql.ResultSet;
import java.sql.SQLException;

class LSetup {
	// Setup variables
	static final byte VAR_AP_SERVER = 1;
	static final byte VAR_AP_PORT = 2;
	static final byte VAR_AP_DATABASE = 3;
	static final byte VAR_AP_LOGIN = 4;
	static final byte VAR_AP_PASSWORD = 5;
	static final byte VAR_SAT_OFF = 6;
	static final byte VAR_SUN_OFF = 7;
	static final byte VAR_OPENING = 8;
	static final byte VAR_CLOSING = 9;
	static final byte VAR_TIMER = 10;
	static final byte VAR_UPDATER = 11;
	static final byte VAR_CODER1_NAME = 12;
	static final byte VAR_CODER1_ACTIVE = 13;
	static final byte VAR_CODER2_NAME = 14;
	static final byte VAR_CODER2_ACTIVE = 15;
	static final byte VAR_CODER3_NAME = 16;
	static final byte VAR_CODER3_ACTIVE = 17;
	static final byte VAR_CODER4_NAME = 18;
	static final byte VAR_CODER4_ACTIVE = 19;
	static final byte VAR_MIN_WL_DATE = 20;
	static final byte VAR_CODER1_FTE = 21;
	static final byte VAR_CODER2_FTE = 22;
	static final byte VAR_CODER3_FTE = 23;
	static final byte VAR_CODER4_FTE = 24;
	static final byte VAR_BUSINESS_DAYS = 25;
	static final byte VAR_LAB_NAME = 26;
	static final byte VAR_AP_NAME = 27;
	static final byte VAR_ROUTE_TIME = 28;
	static final byte VAR_V5_INTERVAL = 29;
	static final byte VAR_V5_UPDATE = 30;
	static final byte VAR_V5_FTE = 31;
	static final byte VAR_V5_LAST = 32;
	static final byte VAR_V5_FROZEN = 33;
	static final byte VAR_MONTH_RUN = 34;
	static final byte VAR_V5_NAME = 35;
	private LBase pj;

	LSetup(LBase pj) {
		this.pj = pj;
	}

	boolean getBoolean(byte key) {
		return (getString(key).equalsIgnoreCase("Y"));
	}

	byte getByte(byte key) {
		byte value = pj.numbers.parseByte(getString(key));
		switch (key) {
		case VAR_OPENING:
			if (value < 0) {
				value = 0;
			} else if (value > 12) {
				value = 12;
			}
			break;
		case VAR_CLOSING:
			if (value < 13) {
				value = 13;
			} else if (value > 23) {
				value = 23;
			}
			break;
		case VAR_V5_INTERVAL:
			// Update every 1-12 months
			if (value < 1) {
				value = 1;
			} else if (value > 12) {
				value = 12;
			}
			break;
		case VAR_V5_UPDATE:
			// Update using aggregate data from the last 1-3 years
			if (value < 1) {
				value = 1;
			} else if (value > 3) {
				value = 3;
			}
			break;
		default:
		}
		return value;
	}

	int getInt(byte key) {
		int value = pj.numbers.parseInt(getString(key));
		switch (key) {
		case VAR_MONTH_RUN:
			// Month number of the last update
			if (value < 1) {
				value = 1;
			} else if (value > 12) {
				value = 12;
			}
			break;
		case VAR_ROUTE_TIME:
			// routing time (range 10 am - 6 pm)
			if (value < 36000000) {
				value = 36000000;
			} else if (value > 64800000) {
				value = 64800000;
			}
			break;
		case VAR_TIMER:
			if (value < 1) {
				value = 1;
			} else if (value > 10) {
				value = 10;
			}
			// From minutes to milliseconds
			value *= 60000;
			break;
		case VAR_UPDATER:
			if (value < 5) {
				// Minimum every 5 minutes to prevent deadlocks
				value = 5;
			} else if (value > 180) {
				// Max every 3 hours
				value = 180;
			}
			// From minutes to milliseconds
			value *= 60000;
			break;
		case VAR_V5_FROZEN:
			// Workload in seconds (range 1-30 minutes)
			if (value < 60) {
				value = 60;
			} else if (value > 1800) {
				value = 1800;
			}
			break;
		case VAR_V5_FTE:
			// Annual Workload in seconds (range 3000000-9999999 minutes)
			if (value < 3000000) {
				value = 3000000;
			} else if (value > 9999999) {
				value = 9999999;
			}
			break;
		default:
		}
		return value;
	}

	long getLong(byte key) {
		return pj.numbers.parseLong(getString(key));
	}

	short getShort(byte key) {
		return pj.numbers.parseShort(getString(key));
	}

	String getString(byte key) {
		String value = "";
		pj.dbPowerJ.setByte(pj.pjStms.get(DPowerJ.STM_STP_SL_SID), 1, key);
		ResultSet rst = pj.dbPowerJ.getResultSet(pj.pjStms.get(DPowerJ.STM_STP_SL_SID));
		try {
			while (rst.next()) {
				if (rst.getString("STVA") != null) {
					value = rst.getString("STVA").trim();
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Setup", e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
		return value;
	}

	boolean setBoolean(byte key, boolean value) {
		return setString(key, (value ? "Y" : "N"));
	}

	boolean setByte(byte key, byte value) {
		switch (key) {
		case VAR_OPENING:
			if (value < 0) {
				value = 0;
			} else if (value > 12) {
				value = 12;
			}
			break;
		case VAR_CLOSING:
			if (value < 13) {
				value = 13;
			} else if (value > 23) {
				value = 23;
			}
			break;
		case VAR_V5_INTERVAL:
			// Update every 1-12 months
			if (value < 1) {
				value = 1;
			} else if (value > 12) {
				value = 12;
			}
			break;
		case VAR_V5_UPDATE:
			// Update using aggregate data from the last 1-3 years
			if (value < 1) {
				value = 1;
			} else if (value > 3) {
				value = 3;
			}
			break;
		default:
		}
		return setString(key, String.valueOf(value));
	}

	boolean setInt(byte key, int value) {
		switch (key) {
		case VAR_MONTH_RUN:
			// Month number of the last update
			if (value < 1) {
				value = 1;
			} else if (value > 12) {
				value = 12;
			}
			break;
		case VAR_ROUTE_TIME:
			// routing time (range 10 am - 6 pm)
			if (value < 36000000) {
				value = 36000000;
			} else if (value > 64800000) {
				value = 64800000;
			}
			break;
		case VAR_TIMER:
			if (value < 1) {
				value = 1;
			} else if (value > 10) {
				value = 10;
			}
			break;
		case VAR_UPDATER:
			if (value < 5) {
				// Minimum every 5 minutes to prevent deadlocks
				value = 5;
			} else if (value > 1440) {
				// Max every 24 hours
				value = 1440;
			}
			break;
		case VAR_V5_FROZEN:
			// Workload in seconds (range 1-30 minutes)
			if (value < 60) {
				value = 60;
			} else if (value > 1800) {
				value = 1800;
			}
			break;
		case VAR_V5_FTE:
			// Annual Workload in seconds (range 3000000-9999999 minutes)
			if (value < 3000000) {
				value = 3000000;
			} else if (value > 9999999) {
				value = 9999999;
			}
			break;
		default:
		}
		return setString(key, String.valueOf(value));
	}

	boolean setLong(byte key, long value) {
		return setString(key, String.valueOf(value));
	}

	boolean setShort(byte key, short value) {
		switch (key) {
		case VAR_BUSINESS_DAYS:
			if (value < 1) {
				value = 1;
			} else if (value > 365) {
				value = 365;
			}
			break;
		default:
		}
		return setString(key, String.valueOf(value));
	}

	boolean setString(byte key, String value) {
		boolean altered = true;
		value = value.trim();
		if (value.length() > 64) {
			value = value.substring(0, 64);
		}
		pj.dbPowerJ.setString(pj.pjStms.get(DPowerJ.STM_STP_UPDATE), 1, value);
		pj.dbPowerJ.setByte(pj.pjStms.get(DPowerJ.STM_STP_UPDATE), 2, key);
		if (pj.dbPowerJ.execute(pj.pjStms.get(DPowerJ.STM_STP_UPDATE)) > 0) {
			altered = false;
		}
		return altered;
	}
}