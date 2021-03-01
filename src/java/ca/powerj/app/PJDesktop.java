package ca.powerj.app;
import ca.powerj.gui.AppFrame;

public class PJDesktop extends AppFrame {
	private static PJServer server = null;

	public PJDesktop(String[] args) {
		super();
		new PJClient(args);
		server = new PJServer();
		server.initialize();
	}
}