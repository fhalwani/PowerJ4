package ca.powerj;
import java.util.Scanner;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

class AServer extends LBase implements Daemon {
	private static AServer launcher = new AServer();

	/**
	* The Java entry point.
	* This is the main routine to test the application from the command line.
	*/
	AServer(String[] args) {
		super();
		launcher.init(args);
		Scanner sc = new Scanner(System.in);
		// wait until you receive a stop command from the keyboard
		System.out.printf("Enter 'stop' to halt: ");
		while (!sc.nextLine().toLowerCase().equals("stop"));
		try {
			sc.close();
			stop();
		} catch (Exception ignore) {}
	}

	AServer() {
		super();
	}

	@Override
	public void destroy() {
		try {
			stop();
		} catch (Exception ignore) {}
	}

	/**
	* Implementing the Daemon interface is not required for Windows but is for Linux
	*/
	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		launcher.init(context.getArguments());
	}

	/**
	* Do the work of starting the engine
	*/
	void init(String[] args) {
		if (stopped.get()) {
			if (args != null) {
				// automatic login to APIS
				autoLogin = true;
				super.init(args);
				if (errorID != LConstants.ERROR_NONE) {
					try {
						stop();
					} catch (Exception ignore) {}
				}
				startWorker();
			}
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
}