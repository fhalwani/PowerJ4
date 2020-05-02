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
			launcher.start();
			Scanner sc = new Scanner(System.in);
			System.out.printf("Enter 'stop' to halt: ");
			while (!sc.nextLine().toLowerCase().equals("stop")) {
				// don't return until user or thread stops
				if (stopped.get()) {
					break;
				}
				try {
					Thread.sleep(60000);
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
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		try {
			start(context.getArguments());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void start() throws Exception {
		String[] args = {};
		System.out.println("Daemon started");
		launcher.init(args);
		if (launcher.errorID == LConstants.ERROR_NONE) {
			// start the thread
			launcher.startWorker();
			// don't return until thread stops
			while (!stopped.get()) {
				try {
					Thread.sleep(60000);
				} catch (InterruptedException ignore) {
				}
			}
		}
		stop(args);
	}

	static void start(String args[]) {
		try {
			if (args.length == 0) {
				launcher.start();
			} else if (args[0].equalsIgnoreCase("start")) {
				launcher.start();
			} else if (args[0].equalsIgnoreCase("stop")) {
				launcher.stop();
			} else {
				System.out.println("Unknown arguement " + args[0]);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void stop() throws Exception {
		while (!launcher.stopWorker()) {
			System.out.println("Waiting for Daemon busy signal");
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ignore) {
			}
		}
		System.exit(0);
	}

	static void stop(String args[]) {
		try {
			launcher.stop();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void destroy() {
		System.out.println("Daemon stopped");
	}
}