package ca.powerj.app;
import ca.powerj.gui.AppFrame;
import ca.powerj.lib.LibConstants;

public class PJClient extends AppFrame implements Runnable {
	private Thread engine = null;

	public PJClient(String[] args) {
		super();
		init(args);
		engine = new Thread(this);
		engine.start();
	}

	@Override
	public void run() {
		final byte JOB_START = 0;
		final byte JOB_REFRESH = 1;
		final byte JOB_SLEEP = 2;
		final byte JOB_SLEEPING = 3;
		final byte JOB_WAKEUP = 4;
		byte jobID = JOB_START;
		Thread.currentThread().setName("PJClient");
		while (!isStopping()) {
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
					setNextUpdate();
					jobID = JOB_REFRESH;
				}
			case JOB_REFRESH:
				if (getNextUpdate() - System.currentTimeMillis() < getTimerInterval()) {
					if (!isBusy()) {
						refreshPanel();
						setNextUpdate();
					}
				} else if (getNextUpdate() - System.currentTimeMillis() > 6800000) {
					// Sleep if nextUpdate is in 2 hours or more
					jobID = JOB_SLEEP;
				}
				break;
			case JOB_SLEEP:
				log(LibConstants.ERROR_NONE, "Going to sleep...");
				jobID = JOB_SLEEPING;
				if (!isOffline()) {
					if (dbPath != null) {
						dbPath.close();
					}
				}
				if (dbPowerJ != null) {
					dbPowerJ.close();
				}
				break;
			case JOB_SLEEPING:
				if (getNextUpdate() - System.currentTimeMillis() < getTimerInterval()) {
					log(LibConstants.ERROR_NONE, "Waking up...");
					jobID = JOB_WAKEUP;
				}
				break;
			default:
			}
			try {
				Thread.sleep(getTimerInterval());
			} catch (InterruptedException ie) {
				break;
			}
		}
		if (!isOffline()) {
			if (dbPath != null) {
				dbPath.close();
			}
		}
		if (dbPowerJ != null) {
			dbPowerJ.close();
		}
		log(LibConstants.ERROR_NONE, "PJClient stopped...");
		super.close();
	}

	@Override
	public void setStopping() {
		super.setStopping();
		engine.interrupt();
	}
}