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
		launcher.init(args);
		if (launcher.errorID == LConstants.ERROR_NONE) {
			// start the thread
			launcher.startWorker();
		}
		Scanner sc = new Scanner(System.in);
		System.out.printf("Enter 'stop' to halt: ");
		while (!sc.nextLine().toLowerCase().equals("stop"))
			;
		sc.close();
		try {
			launcher.stop();
		} catch (Exception e) {
		}
	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		launcher.init(context.getArguments());
		if (errorID == LConstants.ERROR_NONE) {
			// start the thread
			startWorker();
		}
	}

	@Override
	public void start() throws Exception {
		String[] args = {};
		launcher.init(args);
	}

	@Override
	public void stop() throws Exception {
		if (launcher.stopWorker()) {
			System.exit(0);
		}
	}

	@Override
	public void destroy() {
	}
}