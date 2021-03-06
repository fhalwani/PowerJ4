package ca.powerj.lib;

import ca.powerj.database.DBPowerj;

public class LibSetup {
	// Setup variables
	public static final byte VAR_AP_SERVER = 1;
	public static final byte VAR_AP_PORT = 2;
	public static final byte VAR_AP_DATABASE = 3;
	public static final byte VAR_AP_LOGIN = 4;
	public static final byte VAR_AP_PASSWORD = 5;
	public static final byte VAR_SAT_OFF = 6;
	public static final byte VAR_SUN_OFF = 7;
	public static final byte VAR_OPENING = 8;
	public static final byte VAR_CLOSING = 9;
	public static final byte VAR_TIMER = 10;
	public static final byte VAR_UPDATER = 11;
	public static final byte VAR_CODER1_NAME = 12;
	public static final byte VAR_CODER1_ACTIVE = 13;
	public static final byte VAR_CODER2_NAME = 14;
	public static final byte VAR_CODER2_ACTIVE = 15;
	public static final byte VAR_CODER3_NAME = 16;
	public static final byte VAR_CODER3_ACTIVE = 17;
	public static final byte VAR_CODER4_NAME = 18;
	public static final byte VAR_CODER4_ACTIVE = 19;
	public static final byte VAR_MIN_WL_DATE = 20;
	public static final byte VAR_CODER1_FTE = 21;
	public static final byte VAR_CODER2_FTE = 22;
	public static final byte VAR_CODER3_FTE = 23;
	public static final byte VAR_CODER4_FTE = 24;
	public static final byte VAR_BUSINESS_DAYS = 25;
	public static final byte VAR_LAB_NAME = 26;
	public static final byte VAR_AP_NAME = 27;
	public static final byte VAR_ROUTE_TIME = 28;
	public static final byte VAR_V5_INTERVAL = 29;
	public static final byte VAR_V5_UPDATE = 30;
	public static final byte VAR_V5_FTE = 31;
	public static final byte VAR_V5_LAST = 32;
	public static final byte VAR_V5_FROZEN = 33;
	public static final byte VAR_MONTH_RUN = 34;
	public static final byte VAR_CODER5_NAME = 35;
	private LibBase base;
	private DBPowerj dbPowerJ;

	LibSetup(LibBase base, DBPowerj dbPowerJ) {
		this.base = base;
		this.dbPowerJ = dbPowerJ;
	}

	public boolean getBoolean(byte key) {
		return (getString(key).equalsIgnoreCase("Y"));
	}

	public byte getByte(byte key) {
		byte value = base.numbers.parseByte(getString(key));
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

	public int getInt(byte key) {
		int value = base.numbers.parseInt(getString(key));
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
			// routing time (range VAR_OPENING to VAR_CLOSING)
			if (value < 3600000 * getByte(VAR_OPENING)) {
				value = 3600000 * getByte(VAR_OPENING);
			} else if (value > 3600000 * getByte(VAR_CLOSING)) {
				value = 3600000 * getByte(VAR_CLOSING);
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

	public long getLong(byte key) {
		return base.numbers.parseLong(getString(key));
	}

	public short getShort(byte key) {
		return base.numbers.parseShort(getString(key));
	}

	public String getString(byte key) {
		return dbPowerJ.getSetup(key);
	}

	public boolean setBoolean(byte key, boolean value) {
		return setString(key, (value ? "Y" : "N"));
	}

	public boolean setByte(byte key, byte value) {
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

	public boolean setInt(byte key, int value) {
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
			// routing time (range VAR_OPENING to VAR_CLOSING)
			if (value < 3600000 * getByte(VAR_OPENING)) {
				value = 3600000 * getByte(VAR_OPENING);
			} else if (value > 3600000 * getByte(VAR_CLOSING)) {
				value = 3600000 * getByte(VAR_CLOSING);
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

	public boolean setLong(byte key, long value) {
		return setString(key, String.valueOf(value));
	}

	public boolean setShort(byte key, short value) {
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

	public boolean setString(byte key, String value) {
		boolean altered = true;
		value = value.trim();
		if (value.length() > 64) {
			value = value.substring(0, 64);
		}
		if (dbPowerJ.setSetup(key, value) > 0) {
			altered = false;
		}
		return altered;
	}
}