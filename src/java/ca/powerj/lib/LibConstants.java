package ca.powerj.lib;
import java.awt.Color;
import java.awt.Font;

public class LibConstants {
	// User access codes (0-31)
	public static final byte ACCESS_DAILY = 0;
	public static final byte ACCESS_DIAGN = 1;
	public static final byte ACCESS_DISTR = 2;
	public static final byte ACCESS_EMBED = 3;
	public static final byte ACCESS_FOREC = 4;
	public static final byte ACCESS_GROSS = 5;
	public static final byte ACCESS_HISTO = 6;
	public static final byte ACCESS_NAMES = 7;
	public static final byte ACCESS_ROUTE = 8;
	public static final byte ACCESS_SCHED = 9;
	public static final byte ACCESS_SPECI = 10;
	public static final byte ACCESS_TURNA = 11;
	public static final byte ACCESS_WORKD = 12;
	public static final byte ACCESS_WORKL = 13;
	public static final byte ACCESS_EX_PDF = 20;
	public static final byte ACCESS_EX_XLS = 21;
	public static final byte ACCESS_AU_EDT = 22;
	public static final byte ACCESS_AU_ERR = 23;
	public static final byte ACCESS_AU_FNL = 24;
	public static final byte ACCESS_AU_PNP = 25;
	public static final byte ACCESS_STP_IE = 26;
	public static final byte ACCESS_STP_PJ = 27;
	public static final byte ACCESS_STP_PP = 28;
	public static final byte ACCESS_STP_PR = 29;
	public static final byte ACCESS_STP_SC = 30;
	public static final byte ACCESS_STP_TR = 31;
	// Panels actions
	public static final byte ACTION_ABOUT = 1;
	public static final byte ACTION_ACCESSION = 2;
	public static final byte ACTION_BACKLOG = 3;
	public static final byte ACTION_BACKUP = 4;
	public static final byte ACTION_CLOSE = 5;
	public static final byte ACTION_CODER1 = 6;
	public static final byte ACTION_CODER2 = 7;
	public static final byte ACTION_CODER3 = 8;
	public static final byte ACTION_CODER4 = 9;
	public static final byte ACTION_DAILY = 10;
	public static final byte ACTION_DISTRIBUTE = 11;
	public static final byte ACTION_EDITOR = 12;
	public static final byte ACTION_ERROR = 13;
	public static final byte ACTION_EXCEL = 14;
	public static final byte ACTION_FACILITY = 15;
	public static final byte ACTION_FINALS = 16;
	public static final byte ACTION_FORECAST = 17;
	public static final byte ACTION_HELP = 18;
	public static final byte ACTION_HISTOLOGY = 19;
	public static final byte ACTION_ORDERGROUP = 20;
	public static final byte ACTION_ORDERMASTER = 21;
	public static final byte ACTION_PDF = 22;
	public static final byte ACTION_PENDING = 23;
	public static final byte ACTION_PERSONNEL = 24;
	public static final byte ACTION_PROCEDURES = 25;
	public static final byte ACTION_QUIT = 26;
	public static final byte ACTION_REPORT = 27;
	public static final byte ACTION_RESTORE = 28;
	public static final byte ACTION_ROUTING = 29;
	public static final byte ACTION_RULES = 30;
	public static final byte ACTION_SCHEDULE = 31;
	public static final byte ACTION_SERVICES = 32;
	public static final byte ACTION_SETUP = 33;
	public static final byte ACTION_SPECGROUP = 34;
	public static final byte ACTION_SPECIALTY = 35;
	public static final byte ACTION_SPECIMEN = 36;
	public static final byte ACTION_SPECMASTER = 37;
	public static final byte ACTION_SUBSPECIAL = 38;
	public static final byte ACTION_TURNMASTER = 39;
	public static final byte ACTION_TURNAROUND = 40;
	public static final byte ACTION_UTILIZATION = 41;
	public static final byte ACTION_WORKDAYS = 42;
	public static final byte ACTION_WORKLOAD = 43;
	public static final byte ACTION_LBASE = 44;
	public static final byte ACTION_LDAYS = 45;
	public static final byte ACTION_LFLOW = 46;
	public static final byte ACTION_LLOAD = 47;
	public static final byte ACTION_LLOGIN = 48;
	public static final byte ACTION_LSYNC = 49;
	public static final byte ACTION_LVAL5 = 50;
	// PowerJ Database systems
	public static final byte DB_DERBY = 1;
	public static final byte DB_POSTG = 2;
	public static final byte DB_MARIA = 3;
	public static final byte DB_MSSQL = 4;
	// Pathology Database systems
	public static final byte AP_POWERPATH = 1;
	// Error Codes
	public static final byte ERROR_NONE = 0;
	public static final byte ERROR_ACCESS = 1;
	public static final byte ERROR_APP_INSTANCE = 2;
	public static final byte ERROR_APP_PATH = 3;
	public static final byte ERROR_BINARY_FILE = 4;
	public static final byte ERROR_CODING_RULE_UNKNOWN = 5;
	public static final byte ERROR_CONNECTION = 6;
	public static final byte ERROR_FILE_NOT_FOUND = 7;
	public static final byte ERROR_IMPORT = 8;
	public static final byte ERROR_IO = 9;
	public static final byte ERROR_NULL = 10;
	public static final byte ERROR_NUMBER_FORMAT = 11;
	public static final byte ERROR_ORDER_UNKNOWN = 12;
	public static final byte ERROR_SPECIMEN_UNKNOWN = 13;
	public static final byte ERROR_SPECIMENS_COUNT_ZERO = 14;
	public static final byte ERROR_SQL = 15;
	public static final byte ERROR_UNEXPECTED = 16;
	public static final byte ERROR_VARIABLE = 17;
	// Dialog Codes
	public static final byte OPTION_YES = 0;
	public static final byte OPTION_NO = 1;
	public static final byte OPTION_CANCEL = 2;
	// Orders/Slides types and sub-types
	public static final byte ORDER_TYPE_REJECT = 0;
	public static final byte ORDER_TYPE_IGNORE = 1;
	public static final byte ORDER_TYPE_BLOCK = 2;
	public static final byte ORDER_TYPE_BLKFS = 3;
	public static final byte ORDER_TYPE_SLIDE = 4;
	public static final byte ORDER_TYPE_SLDFS = 5;
	public static final byte ORDER_TYPE_SS = 6;
	public static final byte ORDER_TYPE_IHC = 7;
	public static final byte ORDER_TYPE_FISH = 8;
	public static final byte ORDER_TYPE_EM = 9;
	public static final byte ORDER_TYPE_MOL = 10;
	public static final byte ORDER_TYPE_FCM = 11;
	public static final byte ORDER_TYPE_ADD = 12;
	// Personnel codes
	public static final byte PERSON_ALL = 0;
	public static final byte PERSON_CYT = 1;
	public static final byte PERSON_CYG = 2;
	public static final byte PERSON_HIS = 3;
	public static final byte PERSON_SYS = 4;
	public static final byte PERSON_LAB = 5;
	public static final byte PERSON_PAS = 6;
	public static final byte PERSON_PAT = 7;
	public static final byte PERSON_RES = 8;
	public static final byte PERSON_TEC = 9;
	// Coding Rules
	public static final short RULE_UNKNOWN = 0;
	public static final short RULE_IGNORE = 1;
	// Code all specimens in the case as a whole
	public static final short RULE_CASE_INCLUSIVE = 100;	// Autopsy (no orders)
	public static final short RULE_CASE_FIXED = 101;	// plus orders
	public static final short RULE_CASE_GROSS_MICRO = 102;
	public static final short RULE_CASE_BLOCKS_X_MIN_MAX = 103;	// CAP rule 9
	public static final short RULE_CASE_BLOCKS_1_2_PLUSX = 104;
	public static final short RULE_CASE_BLOCKS_1_2_X = 105;
	public static final short RULE_CASE_FRAGS_X_MIN_MAX = 106;
	public static final short RULE_CASE_FRAGS_1_2_PLUSX = 107;
	public static final short RULE_CASE_FRAGS_1_2_X = 108;
	public static final short RULE_CASE_FRAGS_BLOCKS = 109;
	// Code each specimen separately
	public static final short RULE_SPECIMEN_INCLUSIVE = 200;
	public static final short RULE_SPECIMEN_FIXED = 201;	// W2Q, CPT, RCP
	public static final short RULE_SPECIMEN_GROSS_MICRO = 202;
	public static final short RULE_SPECIMEN_EVERY_X_MIN_MAX = 203;	// BM Bx +/- Aspirate +/- Blood smear
	public static final short RULE_SPECIMEN_1_2_PLUSX = 204;	// RCP GI
	public static final short RULE_SPECIMEN_1_2_X = 205;	// RCP GI
	public static final short RULE_SPECIMEN_BLOCKS_X_MIN_MAX = 206;
	public static final short RULE_SPECIMEN_BLOCKS_1_2_PLUSX = 207;
	public static final short RULE_SPECIMEN_BLOCKS_1_2_X = 208;
	public static final short RULE_SPECIMEN_FRAGS_X_MIN_MAX = 209;
	public static final short RULE_SPECIMEN_FRAGS_1_2_PLUSX = 210;
	public static final short RULE_SPECIMEN_FRAGS_1_2_X = 211;
	public static final short RULE_SPECIMEN_FRAGS_BLOCKS = 212;
	// Code specimens that use the same specimen code (prostate x6 or x10)
	public static final short RULE_LINKED_INCLUSIVE = 300;
	public static final short RULE_LINKED_FIXED = 301;
	public static final short RULE_LINKED_EVERY_X_MIN_MAX = 302;
	public static final short RULE_LINKED_1_2_PLUSX = 303;
	public static final short RULE_LINKED_1_2_X = 304;
	public static final short RULE_LINKED_BLOCKS_X_MIN_MAX = 305;
	public static final short RULE_LINKED_BLOCKS_1_2_PLUSX = 306;
	public static final short RULE_LINKED_BLOCKS_1_2_X = 307;
	public static final short RULE_LINKED_FRAGS_X_MIN_MAX = 308;
	public static final short RULE_LINKED_FRAGS_1_2_PLUSX = 309;
	public static final short RULE_LINKED_FRAGS_1_2_X = 310;
	public static final short RULE_LINKED_FRAGS_BLOCKS = 311;
	// Code Orders per group, allow repeats (W2Q)
	public static final short RULE_GROUP_CASE_INCLUSIVE = 600;
	public static final short RULE_GROUP_SPECIMEN_INCLUSIVE = 601;
	public static final short RULE_GROUP_EVERY_X_MIN_MAX = 602;
	public static final short RULE_GROUP_1_2_PLUSX = 603;
	public static final short RULE_GROUP_1_2_X = 604;
	// Code Orders per group, disallow repeats (CPT)
	public static final short RULE_UNIQUE_CASE_INCLUSIVE = 700;
	public static final short RULE_UNIQUE_SPECIMEN_INCLUSIVE = 701;
	public static final short RULE_UNIQUE_EVERY_X_MIN_MAX = 702;
	public static final short RULE_UNIQUE_1_2_PLUSX = 703;
	public static final short RULE_UNIQUE_1_2_X = 704;
	// Code Orders per group if ordered after routing, allow repeats (CAP)
	public static final short RULE_AFTER_CASE_INCLUSIVE = 800;
	public static final short RULE_AFTER_SPECIMEN_INCLUSIVE = 801;
	public static final short RULE_AFTER_EVERY_X_MIN_MAX = 802;
	public static final short RULE_AFTER_1_2_PLUSX = 803;
	public static final short RULE_AFTER_1_2_X = 804;
	// Code Orders per pathologist as additional work (Case Reviews)
	public static final short RULE_ADDL_CASE_INCLUSIVE = 900; 
	public static final short RULE_ADDL_SPECIMEN_INCLUSIVE = 901;
	public static final short RULE_ADDL_EVERY_X_MIN_MAX = 902;
	public static final short RULE_ADDL_1_2_PLUSX = 903;
	public static final short RULE_ADDL_1_2_X = 904;
	// Cases status ordered by hierarchy
	public static final byte STATUS_ACCES = 0;
	public static final byte STATUS_GROSS = 1;
	public static final byte STATUS_EMBED = 2;
	public static final byte STATUS_MICRO = 3;
	public static final byte STATUS_ROUTE = 4;
	public static final byte STATUS_DIAGN = 5;
	public static final byte STATUS_FINAL = 6;
	public static final byte STATUS_HISTO = 7;
	public static final byte STATUS_ALL   = 8;
	// Varia
	public static final boolean DEBUG_STATE = true;
	public static final short SLEEP_TIME = 500;
	public static final long ONE_HOUR = 3600000;
	public static final Color COLOR_LIGHT_BLUE = new Color(0, 190, 255);
	public static final Color COLOR_AZURE_BLUE = new Color(0, 127, 255);
	public static final Color COLOR_DARK_BLUE = new Color(0, 0, 176);
	public static final Color COLOR_AMBER = new Color(255, 191, 0);
	public static final Color[] COLOR_EVEN_ODD = { new Color(190, 190, 190), Color.WHITE };
	public static final Font APP_FONT = new Font("Serif", Font.BOLD, 14);
	public static final String APP_NAME = "PowerJ";
	public static final String APP_VERSION = "5.0";
	public static final String[] ERROR_STRINGS = { "ERROR_NONE", "ERROR_ACCESS", "ERROR_APP_INSTANCE", "ERROR_APP_PATH",
			"ERROR_BINARY_FILE", "ERROR_CODING_RULE_UNKNOWN", "ERROR_CONNECTION", "ERROR_FILE_NOT_FOUND",
			"ERROR_IMPORT", "ERROR_IO", "ERROR_NULL", "ERROR_NUMBER_FORMAT", "ERROR_ORDER_UNKNOWN",
			"ERROR_SPECIMEN_UNKNOWN", "ERROR_SPECIMENS_COUNT_ZERO", "ERROR_SQL", "ERROR_UNEXPECTED", "ERROR_VARIABLE" };
	public static final String[] ORDER_TYPE_STRINGS = {"Reject", "Ignore", "BLK", "FSB", "H&E", "FSS",
			"SS", "IHC", "FISH", "EM", "MOL", "FCM", "ADDL"};
	public static final String[] PERSON_STRINGS = {"* All *", "C", "CG", "H", "IS", "LT", "PA", "PT", "R", "T"};
	public static final String[] STATUS_STRINGS = {"Accession", "Gross", "Embeded", "Microtomy", "Routed", "Diagnosis",
			"Final", "Histology"};
}