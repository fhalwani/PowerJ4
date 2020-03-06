package ca.powerj;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

class LBase implements Runnable {
	static AtomicBoolean stopped = new AtomicBoolean(true);
	static AtomicBoolean busy = new AtomicBoolean(true);
	boolean autoLogin = false;
	boolean offLine = false;
	byte pnlID = 0;
	byte errorID = LConstants.ERROR_NONE;
	short userID = 0;
	int timerInterval = 0;
	int updateInterval = 0;
	int updateDelay = 0;
	long lastUpdate = 0;
	long nextUpdate = 0;
	String appDir = "";
	String pjArch = "";
	String pjHost = "";
	String pjPort = "";
	String pjSchema = "";
	String pjUser = "";
	String pjPass = "";
	String apArch = "";
	String apHost = "";
	String apPort = "";
	String apSchema = "";
	String apUser = "";
	String apPass = "";
	NBase pnlCore = null;
	LDates dates = null;
	LDefaults defaults = null;
	LNumbers numbers = null;
	LSetup setup = null;
	DPowerJ dbPowerJ = null;;
	DPowerpath dbAP = null;
	private ServerSocket socket = null;
	private LLogs logger = null;
	private Thread engine = null;

	LBase() {
		Thread.currentThread().setName("PowerJ");
		setSocket();
	}

	boolean abort() {
		return stopped.get();
	}

	void getLastUpdate() {
		long now = System.currentTimeMillis();
		lastUpdate = dbPowerJ.getTime(DPowerJ.STM_PND_SL_LST);
		nextUpdate = lastUpdate + updateInterval;
		if (nextUpdate < now) {
			nextUpdate = now + timerInterval;
		}
	}

	void initDBAP() {
		boolean isConnected = false;
		if (dbAP != null) {
			if (dbAP.connected()) {
				isConnected = true;
			}
		}
		if (!isConnected) {
			if (apArch.equals("POWERPATH")) {
				dbAP = new DPowerpath(this);
			} else {
				// Cerner & Copath here perhaps one day
				dbAP = new DPowerpath(this);
			}
		}
	}

	/** Retrieve PowerJ database variables that are encrypted in powerj.bin **/
	void initDBPJ() {
		if (pjArch.trim().length() == 0) {
			LCrypto crypto = new LCrypto(appDir);
			String[] data = crypto.getData();
			if (data != null) {
				if (data.length == 6) {
					pjArch = data[0].toUpperCase();
					pjHost = data[1];
					pjPort = data[2];
					pjSchema = data[3];
					pjUser = data[4];
					pjPass = data[5];
				} else {
					log(LConstants.ERROR_BINARY_FILE, "Variables", "Invalid application binary file");
				}
			} else {
				log(LConstants.ERROR_BINARY_FILE, "Variables", "Invalid application binary file");
			}
		}
		boolean isConnected = false;
		if (dbPowerJ != null) {
			if (dbPowerJ.connected()) {
				isConnected = true;
			}
		}
		if (!isConnected) {
			if (pjArch.equals("DERBY")) {
				// Open local database
				dbPowerJ = new DDerby(this);
			} else if (pjArch.equals("MSSQL")) {
				// Open Microsoft SQL Server database
				dbPowerJ = new DMicrosoft(this);
			} else if (pjArch.equals("MARIADB")) {
				// Open MySQL Server database
				dbPowerJ = new DMaria(this);
			} else if (pjArch.equals("POSTGRES")) {
				// Open PostgreSQL Server database
				dbPowerJ = new DPostgres(this);
			} else {
				log(LConstants.ERROR_BINARY_FILE, "Variables", "Invalid application binary file");
			}
		}
	}

	void init(String[] args) {
		for (String s : args) {
			s = s.trim();
			if (s.length() > 6 && s.substring(0, 6).toLowerCase().equals("--path")) {
				appDir = s.substring(6).trim();
			} else if (s.length() > 11 && s.substring(0, 12).toLowerCase().equals("--logingross")) {
				// gross or grossing
				userID = -222;
				autoLogin = true;
			} else if (s.length() > 11 && s.substring(0, 12).toLowerCase().equals("--loginhisto")) {
				// histo or histology
				userID = -111;
				autoLogin = true;
			} else if (s.length() > 11 && s.substring(0, 12).toLowerCase().equals("--login00375")) {
				userID = 375;
				autoLogin = true;
			} else if (s.toLowerCase().equals("--offline")) {
				offLine = true;
			}
		}
		if (errorID == LConstants.ERROR_NONE) {
			setPath();
		}
		if (errorID == LConstants.ERROR_NONE) {
			logger = new LLogs(appDir);
		}
		if (errorID == LConstants.ERROR_NONE) {
			initDBPJ();
		}
		if (errorID == LConstants.ERROR_NONE) {
			numbers = new LNumbers();
			setup = new LSetup(this);
			apArch = setup.getString(LSetup.VAR_AP_NAME);
			apHost = setup.getString(LSetup.VAR_AP_SERVER);
			apPort = setup.getString(LSetup.VAR_AP_PORT);
			apSchema = setup.getString(LSetup.VAR_AP_DATABASE);
			apUser = setup.getString(LSetup.VAR_AP_LOGIN);
			apPass = setup.getString(LSetup.VAR_AP_PASSWORD);
			// From minutes to milliseconds
			timerInterval = 60000 * setup.getInt(LSetup.VAR_TIMER);
			updateInterval = 60000 * setup.getInt(LSetup.VAR_UPDATER);
			if (PowerJ.IS_CLIENT) {
				// Synchronize clients 2-10 minutes after server update
				Random rand = new Random();
				updateDelay = (rand.nextInt(9) + 2) * 30000;
			}
			if (errorID == LConstants.ERROR_NONE) {
				dates = new LDates(this);
			}
		}
	}

	boolean isBusy() {
		return busy.get();
	}

	private boolean isNewMonth() {
		int lastRun = setup.getInt(LSetup.VAR_V5_INTERVAL);
		Calendar calNow = Calendar.getInstance();
		return (lastRun != calNow.get(Calendar.MONTH));
	}

	void log(byte severity, String message) {
		if (severity > LConstants.ERROR_NONE) {
			errorID = severity;
			logger.logError(message);
		} else {
			logger.logInfo(message);
		}
		System.out.println(message);
	}

	void log(byte severity, String name, String message) {
		if (severity > LConstants.ERROR_NONE) {
			errorID = severity;
			logger.logError(name + ": " + message);
		} else {
			logger.logInfo(name + ": " + message);
		}
		System.out.println(message);
	}

	void log(byte severity, String name, Throwable e) {
		if (severity > LConstants.ERROR_NONE) {
			errorID = severity;
			logger.logError(name, e);
		} else {
			logger.logInfo(name, e);
		}
		e.printStackTrace(System.err);
	}

	@Override
	public void run() {
		if (PowerJ.IS_SETUP) {
			return;
		}
		final byte JOB_STARTUP = 1;
		final byte JOB_REFRESH = 2;
		final byte JOB_SLEEP = 3;
		final byte JOB_MONTHLY = 4;
		final byte JOB_DAILY = 5;
		final byte JOB_SLEEPING = 6;
		byte jobID = JOB_STARTUP;
		boolean isStarting = true;
		boolean isUpToDate = false;
		while (!stopped.get()) {
			if (!busy.get()) {
				if (!offLine) {
					// Reset error flag
					errorID = LConstants.ERROR_NONE;
					switch (jobID) {
					case JOB_STARTUP:
						initDBPJ();
						if (errorID == LConstants.ERROR_NONE) {
							initDBAP();
						}
						if (errorID == LConstants.ERROR_NONE) {
							getLastUpdate();
							jobID = JOB_MONTHLY;
							isStarting = true;
						}
						break;
					case JOB_MONTHLY:
						busy.set(true);
						if (PowerJ.IS_SERVER) {
							if (isNewMonth()) {
								new LWorkdays(this);
								if (errorID == LConstants.ERROR_NONE) {
									new LValue5(this);
								}
							}
						}
						if (PowerJ.IS_DESKTOP) {
							if (isNewMonth()) {
								new LWorkdays(this);
//								if (errorID == LConstants.ERROR_NONE) {
//									new LValue5(this);
//								}
							}
						}
						jobID = JOB_DAILY;
						busy.set(false);
						break;
					case JOB_DAILY:
						busy.set(true);
						if (PowerJ.IS_SERVER) {
							new LSync(this);
						}
						if (PowerJ.IS_DESKTOP) {
							new LSync(this);
						}
						jobID = JOB_REFRESH;
						busy.set(false);
						break;
					case JOB_REFRESH:
						busy.set(true);
						if (PowerJ.IS_SERVER) {
							if (nextUpdate - System.currentTimeMillis() < timerInterval) {
								new LPending(isStarting, this);
								isStarting = false;
								setNextUpdate();
							} else if (!isUpToDate) {
								LFinals worker = new LFinals(this);
								isUpToDate = worker.isUpToDate();
								worker.close();
							} else if (nextUpdate - System.currentTimeMillis() > (updateInterval * 5)) {
								jobID = JOB_SLEEP;
							}
						}
						if (PowerJ.IS_DESKTOP) {
							if (nextUpdate - System.currentTimeMillis() < timerInterval) {
								new LPending(isStarting, this);
								isStarting = false;
								if (pnlID > 0 && pnlCore != null) {
									pnlCore.refresh();
								}
								setNextUpdate();
							} else if (!isUpToDate) {
								LFinals worker = new LFinals(this);
								isUpToDate = worker.isUpToDate();
								worker.close();
							} else if (nextUpdate - System.currentTimeMillis() > (updateInterval * 5)) {
								jobID = JOB_SLEEP;
							}
						}
						if (PowerJ.IS_CLIENT) {
							if (nextUpdate - System.currentTimeMillis() < timerInterval) {
								if (pnlID > 0 & pnlCore != null) {
									pnlCore.refresh();
								}
								setNextUpdate();
							} else if (nextUpdate - System.currentTimeMillis() > (updateInterval * 5)) {
								jobID = JOB_SLEEP;
							}
						}
						busy.set(false);
						break;
					case JOB_SLEEP:
						if (!busy.get()) {
							jobID = JOB_SLEEPING;
							log(LConstants.ERROR_NONE, LConstants.APP_NAME,
									dates.formatter(LDates.FORMAT_DATETIME) + " - Going to sleep...");
							if (dbAP != null) {
								dbAP.close();
							}
							if (dbPowerJ != null) {
								dbPowerJ.close();
							}
						}
						break;
					case JOB_SLEEPING:
						if (nextUpdate - System.currentTimeMillis() < (timerInterval * 2)) {
							log(LConstants.ERROR_NONE, LConstants.APP_NAME,
									dates.formatter(LDates.FORMAT_DATETIME) + " - Waking up...");
							jobID = JOB_STARTUP;
							isUpToDate = false;
						}
						break;
					default:
					}
				}
			}
			synchronized (this) {
				try {
					wait((isUpToDate ? timerInterval : 1000));
				} catch (InterruptedException ignore) {
				}
			}
		}
	}

	void setNextUpdate() {
		dates.setNextUpdate();
		log(LConstants.ERROR_NONE, "Next update: " + dates.formatter(nextUpdate, LDates.FORMAT_DATETIME));
	}

	/** Find the folder path of the running jar file. */
	private void setPath() {
		final String fs = System.getProperty("file.separator");
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
				} else {
					errorID = LConstants.ERROR_APP_PATH;
				}
			} catch (URISyntaxException e) {
				errorID = LConstants.ERROR_UNEXPECTED;
				e.printStackTrace();
			}
		}
		if ((appDir.length() > 1) && (!appDir.endsWith(fs))) {
			appDir += fs;
		}
	}

	private void setSocket() {
		try {
			socket = new ServerSocket(4040);
		} catch (IOException e) {
			// Another instance of the application is running!
			errorID = LConstants.ERROR_APP_INSTANCE;
		}
	}

	/**
	 * start the updates thread engine.
	 */
	void startWorker() {
		stopped.set(false);
		busy.set(false);
		engine = new Thread(this);
		engine.setName("PJWorker");
		engine.start();
	}

	/**
	 * Cleanly stop the engine.
	 */
	boolean stopWorker() {
		if (busy.get()) {
			return false;
		}
		stopped.set(true);
		synchronized (this) {
			notifyAll();
		}
		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			try {
				if (thread.getName().equals("PJWorker")) {
					// Wait for the thread to close
					thread.join();
				}
			} catch (InterruptedException ignore) {
			}
		}
		if (dbPowerJ != null) {
			dbPowerJ.close();
		}
		if (dbAP != null) {
			dbAP.close();
		}
		if (logger != null) {
			logger.close();
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ignore) {
			}
		}
		return true;
	}
}