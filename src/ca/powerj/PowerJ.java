package ca.powerj;

public class PowerJ {

	public PowerJ(final String[] args) {
		if (LConstants.IS_CLIENT) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new AClient(args);
				}
			});
		}
		if (LConstants.IS_DESKTOP) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new ADesktop(args);
				}
			});
		}
		if (LConstants.IS_SETUP) {
			new ASetup(args);
		}
	}

	public static void main(String[] args) {
		new PowerJ(args);
	}
}