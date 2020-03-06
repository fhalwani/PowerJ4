package ca.powerj;

public class PowerJ {
	static final boolean IS_DESKTOP = false;
	static final boolean IS_CLIENT = true;
	static final boolean IS_SERVER = false;
	static final boolean IS_SETUP = false;

	public PowerJ(String[] args) {
		if (IS_CLIENT) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new AClient(args);
				}
			});
		}
		if (IS_DESKTOP) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new ADesktop(args);
				}
			});
		}
		if (IS_SERVER) {
			new AServer(args);
		}
		if (IS_SETUP) {
			new ASetup(args);
		}
	}

	public static void main(String[] args) {
		final String[] b = args;
		new PowerJ(b);
	}
}