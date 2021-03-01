package ca.powerj;
import java.util.Arrays;
import java.util.Scanner;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import ca.powerj.app.PJServer;
import ca.powerj.lib.LibConstants;

public class PJDaemon implements Daemon {
	private static PJServer server = null;
	private static PJDaemon daemon = new PJDaemon();

	public PJDaemon() {
	}

	@Override
	public void destroy() {
		System.out.println("Daemon destroy called");
	}

	// Implementing the Daemon interface is not required for Windows but it is for Linux
	@Override
	public void init(DaemonContext dc) throws Exception {
		System.out.println("Daemon init called with args: " + Arrays.toString(dc.getArguments()));
	}

	/**
	* Do the work of starting the engine
	*/
	private void initialize() {
		if (server == null) {
			System.out.println("Starting the Engine");
			server = new PJServer();
			server.initialize();
		}
	}

	/**
	* The Java entry point.
	* @param args Command line arguments, all ignored.
	*/
	public static void main(String[] args) {
		// the main routine is only here for debugging
		daemon.initialize();
		Scanner sc = new Scanner(System.in);
		// wait until you receive a stop command from the keyboard
		System.out.printf("Enter 'stop' to halt: ");
		while(!sc.nextLine().toLowerCase().equals("stop"));
		sc.close();
		daemon.terminate();
	}

	@Override
	public void start() {
		System.out.println("Daemon start called");
		initialize();
	}

	@Override
	public void stop() {
		server.log(LibConstants.ERROR_NONE, "Daemon stop called");
		terminate();
	}

	/**
	 * Cleanly stop the engine.
	 */
	public void terminate() {
		if (server != null) {
			server.log(LibConstants.ERROR_NONE, "Stopping the daemon");
			server.setStopping();
		}
	}

	/**
	* Static methods called by prunsrv to start/stop the Windows service.
	* Pass the argument "start" to start the service, and pass "stop" to stop the service.
	* @param args Arguments from prunsrv command line
	**/
	public static void windowsService(String args[]) {
		String cmd = "start";
		if (args.length > 0) {
			cmd = args[0];
		}
		if ("start".equals(cmd)) {
			daemon.windowsStart();
		} else {
			daemon.windowsStop();
		}
	}

	public void windowsStart() {
		System.out.println("windowsStart called");
		initialize();
		while (!server.isStopping()) {
			// don't return until stopped
			synchronized(this) {
				try {
					this.wait(1000);
				} catch(InterruptedException ignore){
					break;
				}
			}
		}
	}

	public void windowsStop() {
		System.out.println("windowsStop called");
		terminate();
		synchronized(this) {
			// stop the start loop
			this.notify();
		}
	}
}