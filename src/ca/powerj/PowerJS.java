package ca.powerj;

import java.util.Scanner;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class PowerJS extends LBase implements Daemon {
	private static PowerJS launcher = new PowerJS();

	public PowerJS() {
		super();
		autoLogin = true;
	}

	public static void main(String[] args) {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.printf("Enter 'stop' to halt: ");
			start(args);
			while (!sc.nextLine().toLowerCase().equals("stop")) {
				stop(args);
				// don't return until user or thread stops
				if (stopped.get()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignore) {
				}
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			try {
				launcher.stop();
			} catch (Exception e) {
			}
		}
	}

	@Override
	void init(String[] args) {
		super.init(args);
		if (errorID == LConstants.ERROR_NONE) {
			// start the thread
			startWorker();
		}
	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		try {
			start(context.getArguments());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void start() throws Exception {
		final String[] args = {};
		launcher.init(args);
		while (!stopped.get()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignore) {
			}
		}
	}

	static void start(String args[]) {
		try {
			launcher.init(args);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void stop() throws Exception {
		if (launcher.stopWorker()) {
			System.exit(0);
		}
	}

	static void stop(String args[]) {
		try {
			if (launcher.stopWorker()) {
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void destroy() {
	}
}