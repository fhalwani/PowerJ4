package ca.powerj;

import java.awt.Color;
import java.awt.Font;

class LConstants {
	static final boolean IS_CLIENT = true;
	static final boolean IS_DESKTOP = false;
	static final boolean IS_SERVER = false;
	static final boolean IS_SETUP = false;
	// User access codes (0-31)
	static final byte ACCESS_DAILY = 0;
	static final byte ACCESS_DIAGN = 1;
	static final byte ACCESS_DISTR = 2;
	static final byte ACCESS_EMBED = 3;
	static final byte ACCESS_FOREC = 4;
	static final byte ACCESS_GROSS = 5;
	static final byte ACCESS_HISTO = 6;
	static final byte ACCESS_NAMES = 7;
	static final byte ACCESS_ROUTE = 8;
	static final byte ACCESS_SCHED = 9;
	static final byte ACCESS_SPECI = 10;
	static final byte ACCESS_TURNA = 11;
	static final byte ACCESS_WORKD = 12;
	static final byte ACCESS_WORKL = 13;
	static final byte ACCESS_EX_PDF = 20;
	static final byte ACCESS_EX_XLS = 21;
	static final byte ACCESS_AU_EDT = 22;
	static final byte ACCESS_AU_ERR = 23;
	static final byte ACCESS_AU_FNL = 24;
	static final byte ACCESS_AU_PNP = 25;
	static final byte ACCESS_STP_IE = 26;
	static final byte ACCESS_STP_PJ = 27;
	static final byte ACCESS_STP_PP = 28;
	static final byte ACCESS_STP_PR = 29;
	static final byte ACCESS_STP_SC = 30;
	static final byte ACCESS_STP_TR = 31;
	// Panels actions
	static final byte ACTION_ABOUT = 1;
	static final byte ACTION_ACCESSION = 2;
	static final byte ACTION_BACKLOG = 3;
	static final byte ACTION_BACKUP = 4;
	static final byte ACTION_CLOSE = 5;
	static final byte ACTION_CODER1 = 6;
	static final byte ACTION_CODER2 = 7;
	static final byte ACTION_CODER3 = 8;
	static final byte ACTION_CODER4 = 9;
	static final byte ACTION_DAILY = 10;
	static final byte ACTION_DISTRIBUTE = 11;
	static final byte ACTION_EDITOR = 12;
	static final byte ACTION_ERROR = 13;
	static final byte ACTION_EXCEL = 14;
	static final byte ACTION_FACILITY = 15;
	static final byte ACTION_FINALS = 16;
	static final byte ACTION_FORECAST = 17;
	static final byte ACTION_HELP = 18;
	static final byte ACTION_HISTOLOGY = 19;
	static final byte ACTION_ORDERGROUP = 20;
	static final byte ACTION_ORDERMASTER = 21;
	static final byte ACTION_PDF = 22;
	static final byte ACTION_PENDING = 23;
	static final byte ACTION_PERSONNEL = 24;
	static final byte ACTION_PROCEDURES = 25;
	static final byte ACTION_QUIT = 26;
	static final byte ACTION_RESTORE = 27;
	static final byte ACTION_ROUTING = 28;
	static final byte ACTION_RULES = 29;
	static final byte ACTION_SCHEDULE = 30;
	static final byte ACTION_SERVICES = 31;
	static final byte ACTION_SETUP = 32;
	static final byte ACTION_SPECGROUP = 33;
	static final byte ACTION_SPECIALTY = 34;
	static final byte ACTION_SPECIMEN = 35;
	static final byte ACTION_SPECMASTER = 36;
	static final byte ACTION_SUBSPECIAL = 37;
	static final byte ACTION_TURNMASTER = 38;
	static final byte ACTION_TURNAROUND = 39;
	static final byte ACTION_UTILIZATION = 40;
	static final byte ACTION_WORKDAYS = 41;
	static final byte ACTION_WORKLOAD = 42;
	static final byte ACTION_LBASE = 43;
	static final byte ACTION_LDAYS = 44;
	static final byte ACTION_LFLOW = 45;
	static final byte ACTION_LLOAD = 46;
	static final byte ACTION_LLOGIN = 47;
	static final byte ACTION_LSYNC = 48;
	static final byte ACTION_LVAL5 = 49;
	// Dialog Codes
	static final byte OPTION_YES = 0;
	static final byte OPTION_NO = 1;
	static final byte OPTION_CANCEL = 2;
	// Error Codes
	static final byte ERROR_NONE = 0;
	static final byte ERROR_ACCESS = 1;
	static final byte ERROR_APP_INSTANCE = 2;
	static final byte ERROR_APP_PATH = 3;
	static final byte ERROR_BINARY_FILE = 4;
	static final byte ERROR_CODING_RULE_UNKNOWN = 5;
	static final byte ERROR_CONNECTION = 6;
	static final byte ERROR_FILE_NOT_FOUND = 7;
	static final byte ERROR_IMPORT = 8;
	static final byte ERROR_IO = 9;
	static final byte ERROR_NULL = 10;
	static final byte ERROR_NUMBER_FORMAT = 11;
	static final byte ERROR_ORDER_UNKNOWN = 12;
	static final byte ERROR_SPECIMEN_UNKNOWN = 13;
	static final byte ERROR_SPECIMENS_COUNT_ZERO = 14;
	static final byte ERROR_SQL = 15;
	static final byte ERROR_UNEXPECTED = 16;
	static final byte ERROR_VARIABLE = 17;
	// Coders constants
	static final byte CODER_1 = 1;
	static final byte CODER_2 = 2;
	static final byte CODER_3 = 3;
	static final byte CODER_4 = 4;
	static final byte CODER_B = 1;
	static final byte CODER_M = 2;
	static final byte CODER_R = 3;
	// Varia
	static final boolean DEBUG_STATE = true;
	static final short SLEEP_TIME = 500;
	static final String APP_NAME = "PowerJ";
	static final String APP_VERSION = "4.0";
	static final String[] ERROR_STRINGS = { "ERROR_NONE", "ERROR_ACCESS", "ERROR_APP_INSTANCE", "ERROR_APP_PATH",
			"ERROR_BINARY_FILE", "ERROR_CODING_RULE_UNKNOWN", "ERROR_CONNECTION", "ERROR_FILE_NOT_FOUND",
			"ERROR_IMPORT", "ERROR_IO", "ERROR_NULL", "ERROR_NUMBER_FORMAT", "ERROR_ORDER_UNKNOWN",
			"ERROR_SPECIMEN_UNKNOWN", "ERROR_SPECIMENS_COUNT_ZERO", "ERROR_SQL", "ERROR_UNEXPECTED", "ERROR_VARIABLE" };
	static final Color COLOR_LIGHT_BLUE = new Color(0, 190, 255);
	static final Color COLOR_AZURE_BLUE = new Color(0, 127, 255);
	static final Color COLOR_DARK_BLUE = new Color(0, 0, 176);
	static final Color COLOR_AMBER = new Color(255, 191, 0);
	static final Color[] COLOR_EVEN_ODD = { new Color(190, 190, 190), Color.WHITE };
	static final Font APP_FONT = new Font("Serif", Font.BOLD, 14);
}