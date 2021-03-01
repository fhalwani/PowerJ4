package ca.powerj.lib;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Properties;
import ca.powerj.database.DBDerby;
import ca.powerj.database.DBMaria;
import ca.powerj.database.DBMssql;
import ca.powerj.database.DBPath;
import ca.powerj.database.DBPostgres;
import ca.powerj.database.DBPowerj;
import ca.powerj.database.DBPowerpath;

public class LibBase {
	private boolean autoLogin = false;
	private boolean offLine = false;
	private boolean isBusy = false;
	private boolean isStopping = false;
	public byte errorID = LibConstants.ERROR_NONE;
	private byte dbPJSystem = 0;
	private byte dbAPSystem = 0;
	private short userID = 0;
	private int timerInterval = 0;
	private int updateInterval = 0;
	private int updateDelay = 0;
	private long lastUpdate = 0;
	private long nextUpdate = 0;
	public LibDates dates = null;
	public LibDefaults defaults = null;
	public LibNumbers numbers = null;
	public LibSetup setup = null;
	private ServerSocket socket = null;
	private Properties properties = new Properties();
	private LibLogs logger = null;

	public LibBase() {
		Thread.currentThread().setName("PowerJ");
		setSocket();
	}

	public void close() {
		if (logger != null) {
			logger.close();
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ignore) {
			}
		}
		System.exit(0);
	}

	public DBPath getDBPath() {
		DBPath dbPath = null;
		switch (dbAPSystem) {
		case LibConstants.AP_POWERPATH:
			dbPath = new DBPowerpath(this);
			break;
		default:
		}
		return dbPath;
	}

	public DBPowerj getDBPJ() {
		DBPowerj dbPowerJ = null;
		switch (dbPJSystem) {
		case LibConstants.DB_DERBY:
			dbPowerJ = new DBDerby(this);
			break;
		case LibConstants.DB_MARIA:
			dbPowerJ = new DBMaria(this);
			break;
		case LibConstants.DB_MSSQL:
			dbPowerJ = new DBMssql(this);
			break;
		case LibConstants.DB_POSTG:
			dbPowerJ = new DBPostgres(this);
			break;
		default:
			errorID = LibConstants.ERROR_CONNECTION;
			log(errorID, LibConstants.APP_NAME, "Invalid AP architecture in bin file");
		}
		if (errorID == LibConstants.ERROR_NONE) {
			dbPowerJ.setDB(dbPJSystem);
		}
		if (errorID == LibConstants.ERROR_NONE) {
			setup = new LibSetup(this, dbPowerJ);
			log(LibConstants.ERROR_NONE, "Updating Variables...");
			timerInterval = setup.getInt(LibSetup.VAR_TIMER);
			updateInterval = setup.getInt(LibSetup.VAR_UPDATER);
			if (!offLine) {
				properties.setProperty("apArch", setup.getString(LibSetup.VAR_AP_NAME));
				properties.setProperty("apHost", setup.getString(LibSetup.VAR_AP_SERVER));
				properties.setProperty("apPort", setup.getString(LibSetup.VAR_AP_PORT));
				properties.setProperty("apSche", setup.getString(LibSetup.VAR_AP_DATABASE));
				properties.setProperty("apUser", setup.getString(LibSetup.VAR_AP_LOGIN));
				properties.setProperty("apPass", setup.getString(LibSetup.VAR_AP_PASSWORD));
				properties.setProperty("coder1", setup.getString(LibSetup.VAR_CODER1_NAME).toUpperCase());
				properties.setProperty("coder2", setup.getString(LibSetup.VAR_CODER2_NAME).toUpperCase());
				properties.setProperty("coder3", setup.getString(LibSetup.VAR_CODER3_NAME).toUpperCase());
				properties.setProperty("coder4", setup.getString(LibSetup.VAR_CODER4_NAME).toUpperCase());
				properties.setProperty("coder5", setup.getString(LibSetup.VAR_CODER5_NAME).toUpperCase());
				if (getProperty("apArch").toUpperCase().equals("POWERPATH")) {
					dbAPSystem = LibConstants.AP_POWERPATH;
				} else {
					// TODO Cerner & Copath here perhaps one day
					errorID = LibConstants.ERROR_CONNECTION;
					log(errorID, LibConstants.APP_NAME, "Invalid AP architecture in setup table");
				}
			}
		}
		if (errorID == LibConstants.ERROR_NONE) {
			dates = new LibDates(this, dbPowerJ);
		}
		if (errorID == LibConstants.ERROR_NONE && lastUpdate == 0) {
			long now = System.currentTimeMillis();
			lastUpdate = dbPowerJ.getLastPending();
			nextUpdate = lastUpdate + updateInterval;
			if (nextUpdate < now) {
				nextUpdate = now + timerInterval;
			}
			log(LibConstants.ERROR_NONE, "Last update: " + dates.formatter(lastUpdate, LibDates.FORMAT_DATETIME));
		}
		return dbPowerJ;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public long getNextUpdate() {
		return nextUpdate;
	}

	public String getProperty(String key) {
		return properties.getProperty(key, "");
	}

	public int getTimerInterval() {
		return timerInterval;
	}

	public int getUpdateDelay() {
		return updateDelay;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public short getUserID() {
		return userID;
	}

	protected void init(String[] args) {
		if (args.length > 0) {
			for (String s : args) {
				if (s != null) {
					s = s.trim();
					if (s.length() > 6 && s.substring(0, 6).toLowerCase().equals("--path")) {
						properties.setProperty("pjDir", s.substring(6).trim());
					} else if (s.equals("--logingross") && !autoLogin) {
						userID = -222;
						autoLogin = true;
						offLine = false;
					} else if (s.equals("--loginhisto") && !autoLogin) {
						userID = -111;
						autoLogin = true;
						offLine = false;
					} else if (s.equals("--nologin")) {
						userID = 1;
						autoLogin = true;
						offLine = true;
					}
				}
			}
		}
		if (errorID == LibConstants.ERROR_NONE) {
			setPath();
		}
		if (errorID == LibConstants.ERROR_NONE) {
			logger = new LibLogs(getProperty("pjDir"));
		}
		if (errorID == LibConstants.ERROR_NONE) {
			numbers = new LibNumbers();
		}
		/** Retrieve PowerJ database variables that are encrypted in powerj.bin **/
		if (getProperty("pjArch").length() == 0) {
			LibCrypto crypto = new LibCrypto(getProperty("pjDir"));
			String[] data = crypto.getData();
			if (data != null) {
				if (data.length == 6) {
					data[0] = data[0].toUpperCase();
					properties.setProperty("pjArch", data[0]);
					properties.setProperty("pjHost", data[1]);
					properties.setProperty("pjPort", data[2]);
					properties.setProperty("pjSche", data[3]);
					properties.setProperty("pjUser", data[4]);
					properties.setProperty("pjPass", data[5]);
					if (data[0].equals("MSSQL")) {
						dbPJSystem = LibConstants.DB_MSSQL;
					} else if (data[0].equals("MARIADB")) {
						dbPJSystem = LibConstants.DB_MARIA;
					} else if (data[0].equals("POSTGRES")) {
						dbPJSystem = LibConstants.DB_POSTG;
					} else if (data[0].equals("DERBY")) {
						dbPJSystem = LibConstants.DB_DERBY;
					} else {
						errorID = LibConstants.ERROR_BINARY_FILE;
						log(errorID, LibConstants.APP_NAME, "Invalid application binary file");
					}
				} else {
					errorID = LibConstants.ERROR_BINARY_FILE;
					log(errorID, LibConstants.APP_NAME, "Invalid application binary file");
				}
			} else {
				errorID = LibConstants.ERROR_BINARY_FILE;
				log(errorID, LibConstants.APP_NAME, "Invalid application binary file");
			}
		}

	}

	public boolean isAutologin() {
		return autoLogin;
	}

	public boolean isBusy() {
		return isBusy;
	}

	public boolean isOffline() {
		return offLine;
	}

	public boolean isStopping() {
		return isStopping;
	}

	public void log(byte severity, String message) {
		if (severity > LibConstants.ERROR_NONE) {
			errorID = severity;
			logger.logError(message);
		} else {
			logger.logInfo(message);
		}
	}

	public void log(byte severity, String name, String message) {
		if (severity > LibConstants.ERROR_NONE) {
			errorID = severity;
			logger.logError(name + ": " + message);
		} else {
			logger.logInfo(name + ": " + message);
		}
	}

	public void log(byte severity, String name, Throwable e) {
		if (severity > LibConstants.ERROR_NONE) {
			errorID = severity;
			logger.logError(name, e);
		} else {
			logger.logInfo(name, e);
		}
	}

	public void setBusy(boolean value) {
		isBusy = value;
	}

	/** Calculate and display the next schedule to scan the database **/
	protected void setNextUpdate() {
		Calendar calendar = Calendar.getInstance();
		// First, set last update
		if (calendar.getTimeInMillis() - nextUpdate <= 0) {
			// Avoid updates time shifting if timer fires early
			lastUpdate = nextUpdate;
		} else {
			// Next update was in the past (overdue) because software was powered off
			lastUpdate = calendar.getTimeInMillis();
		}
		calendar.setTimeInMillis(lastUpdate + updateInterval);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if (calendar.get(Calendar.HOUR_OF_DAY) > setup.getByte(LibSetup.VAR_CLOSING)) {
			// Sleep from 11 pm till 6 am next business day
			calendar.setTimeInMillis(dates.getNextBusinessDay(calendar));
			// Wake up at 6:30 am next day
			calendar.set(Calendar.HOUR_OF_DAY, setup.getByte(LibSetup.VAR_OPENING));
			calendar.add(Calendar.MILLISECOND, getUpdateInterval());
		}
		nextUpdate = calendar.getTimeInMillis();
		log(LibConstants.ERROR_NONE, "Next update: " + dates.formatter(nextUpdate, LibDates.FORMAT_DATETIME));
	}

	/** Find the folder path of the running jar file. */
	private void setPath() {
		final String fs = System.getProperty("file.separator");
		String appDir = getProperty("pjDir");
		boolean altered = false;
		if (appDir.length() == 0) {
			String jarPath = "";
			try {
				File test = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
				jarPath = test.getAbsolutePath();
				if (jarPath.toLowerCase().contains("powerj.jar")) {
					jarPath = jarPath.substring(0, jarPath.length() - 14);
					if (jarPath.toLowerCase().endsWith(fs + "bin" + fs)) {
						jarPath = jarPath.substring(0, jarPath.length() - 4);
					}
					appDir = jarPath;
					altered = true;
				} else {
					errorID = LibConstants.ERROR_APP_PATH;
				}
			} catch (URISyntaxException e) {
				errorID = LibConstants.ERROR_UNEXPECTED;
				e.printStackTrace();
			}
		}
		if ((appDir.length() > 1) && (!appDir.endsWith(fs))) {
			appDir += fs;
			altered = true;
		}
		if (altered) {
			properties.setProperty("pjDir", appDir);
		}
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	private void setSocket() {
		try {
			socket = new ServerSocket(14040);
		} catch (IOException e) {
			// Another instance of the application is running!
			errorID = LibConstants.ERROR_APP_INSTANCE;
		}
	}

	public void setStopping() {
		isStopping = true;
	}

	public void setUserID(short value) {
		userID = value;
	}
}