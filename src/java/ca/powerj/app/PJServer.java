package ca.powerj.app;
import java.util.Calendar;
import ca.powerj.database.DBPath;
import ca.powerj.database.DBPowerj;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibCode5;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibFinals;
import ca.powerj.lib.LibPending;
import ca.powerj.lib.LibSetup;
import ca.powerj.lib.LibSync;
import ca.powerj.lib.LibWorkdays;

public class PJServer extends LibBase implements Runnable {
	private boolean upToDate = false;
	public DBPowerj dbPowerJ = null;
	public DBPath dbPath = null;
	private Thread engine = null;

	public PJServer() {
		super();
	}

	public void initialize() {
		final String[] args = {};
		init(args);
		engine = new Thread(this);
		engine.start();
	}

	boolean isNewMonth() {
		Calendar calNow = Calendar.getInstance();
		return (setup.getInt(LibSetup.VAR_MONTH_RUN) != calNow.get(Calendar.MONTH));
	}

	@Override
	public void run() {
		final byte JOB_START = 0;
		final byte JOB_REFRESH = 1;
		final byte JOB_SLEEP = 2;
		final byte JOB_SLEEPING = 3;
		final byte JOB_WAKEUP = 4;
		byte jobID = JOB_START;
		boolean firstUpdate = true;
		Thread.currentThread().setName("PJServer");
		while (!isStopping()) {
			if (!isBusy()) {
				if (!isOffline()) {
					errorID = LibConstants.ERROR_NONE;
					switch (jobID) {
					case JOB_START:
					case JOB_WAKEUP:
						if (dbPowerJ == null || !dbPowerJ.connected()) {
							dbPowerJ = getDBPJ();
						}
						if (errorID == LibConstants.ERROR_NONE) {
							if (!isOffline()) {
								if (dbPath == null || !dbPath.connected()) {
									dbPath = getDBPath();
								}
							}
						}
						if (errorID == LibConstants.ERROR_NONE) {
							log(LibConstants.ERROR_NONE, "Starting Sync...");
							new LibSync(this);
							if (isNewMonth()) {
								log(LibConstants.ERROR_NONE, "Starting Workdays...");
								new LibWorkdays(this);
								if (errorID == LibConstants.ERROR_NONE) {
									log(LibConstants.ERROR_NONE, "Starting Code5...");
									new LibCode5(this);
								}
							}
						}
						if (errorID == LibConstants.ERROR_NONE) {
							setNextUpdate();
							jobID = JOB_REFRESH;
						}
					case JOB_REFRESH:
						if (getNextUpdate() - System.currentTimeMillis() < getTimerInterval()) {
							log(LibConstants.ERROR_NONE, "Starting Workflow...");
							new LibPending(firstUpdate, this);
							firstUpdate = false;
							setNextUpdate();
						} else if (!firstUpdate && !upToDate) {
							log(LibConstants.ERROR_NONE, "Starting Workload...");
							new LibFinals(false, "", this);
						} else if (getNextUpdate() - System.currentTimeMillis() > 6800000) {
							// Sleep if nextUpdate is in 2 hours or more
							jobID = JOB_SLEEP;
						}
						break;
					case JOB_SLEEP:
						log(LibConstants.ERROR_NONE, "Going to sleep...");
						jobID = JOB_SLEEPING;
						if (dbPath != null) {
							dbPath.close();
						}
						if (dbPowerJ != null) {
							dbPowerJ.close();
						}
						break;
					case JOB_SLEEPING:
						if (getNextUpdate() - System.currentTimeMillis() < getTimerInterval()) {
							log(LibConstants.ERROR_NONE, "Waking up...");
							firstUpdate = true;
							jobID = JOB_WAKEUP;
						}
						break;
					default:
					}
				}
			}
			try {
				Thread.sleep(getTimerInterval());
			} catch (InterruptedException ie) {
				break;
			}
		}
		if (dbPath != null) {
			dbPath.close();
		}
		if (dbPowerJ != null) {
			dbPowerJ.close();
		}
		log(LibConstants.ERROR_NONE, "PJServer stopped...");
		super.close();
	}

	@Override
	public void setStopping() {
		super.setStopping();
		engine.interrupt();
	}

	public void setUptodate() {
		upToDate = true;
	}
}