package ca.powerj;
import ca.powerj.app.PJClient;
import ca.powerj.app.PJDesktop;
import ca.powerj.app.PJInstall;

public class PowerJ {
	private static byte IS_NONE = 0;
	private static byte IS_CLIENT = 1;
	private static byte IS_DESKTOP = 2;
	private static byte IS_SETUP = 3;

	public PowerJ(final String[] args, final byte envID) {
		if (envID == IS_CLIENT) {
			new PJClient(args);
		} else if (envID == IS_DESKTOP) {
			new PJDesktop(args);
		} else {
			new PJInstall(args);
		}
	}

	public static void main(String[] args) {
		byte envID = IS_NONE;
		if (args.length > 0) {
			for (String s : args) {
				if (s != null) {
					s = s.trim();
					if (s.equals("--client")) {
						envID = IS_CLIENT;
						break;
					} else if (s.equals("--desktop")) {
						envID = IS_DESKTOP;
						break;
					} else if (s.equals("--setup")) {
						envID = IS_SETUP;
						break;
					}
				}
			}
		}
		if (envID == IS_NONE) {
			System.exit(1);
		} else {
			new PowerJ(args, envID);
		}
	}
}