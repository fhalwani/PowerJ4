package ca.powerj;

class ORule {
	// Coding Rules
	static final short RULE_UNKNOWN = 0;
	static final short RULE_IGNORE = 1;
	// Code all specimens in the case as a whole
	static final short RULE_CASE_INCLUSIVE = 100;	// Autopsy (no orders)
	static final short RULE_CASE_FIXED = 101;	// plus orders
	static final short RULE_CASE_GROSS_MICRO = 102;
	static final short RULE_CASE_BLOCKS_X_MIN_MAX = 103;	// CAP rule 9
	static final short RULE_CASE_BLOCKS_1_2_PLUSX = 104;
	static final short RULE_CASE_BLOCKS_1_2_X = 105;
	static final short RULE_CASE_FRAGS_X_MIN_MAX = 106;
	static final short RULE_CASE_FRAGS_1_2_PLUSX = 107;
	static final short RULE_CASE_FRAGS_1_2_X = 108;
	static final short RULE_CASE_FRAGS_BLOCKS = 109;
	// Code each specimen separately
	static final short RULE_SPECIMEN_INCLUSIVE = 200;
	static final short RULE_SPECIMEN_FIXED = 201;	// W2Q, CPT, RCP
	static final short RULE_SPECIMEN_GROSS_MICRO = 202;
	static final short RULE_SPECIMEN_EVERY_X_MIN_MAX = 203;	// BM Bx +/- Aspirate +/- Blood smear
	static final short RULE_SPECIMEN_1_2_PLUSX = 204;	// RCP GI
	static final short RULE_SPECIMEN_1_2_X = 205;	// RCP GI
	static final short RULE_SPECIMEN_BLOCKS_X_MIN_MAX = 206;
	static final short RULE_SPECIMEN_BLOCKS_1_2_PLUSX = 207;
	static final short RULE_SPECIMEN_BLOCKS_1_2_X = 208;
	static final short RULE_SPECIMEN_FRAGS_X_MIN_MAX = 209;
	static final short RULE_SPECIMEN_FRAGS_1_2_PLUSX = 210;
	static final short RULE_SPECIMEN_FRAGS_1_2_X = 211;
	static final short RULE_SPECIMEN_FRAGS_BLOCKS = 212;
	// Code specimens that use the same specimen code (prostate x6 or x10)
	static final short RULE_LINKED_INCLUSIVE = 300;
	static final short RULE_LINKED_FIXED = 301;
	static final short RULE_LINKED_EVERY_X_MIN_MAX = 302;
	static final short RULE_LINKED_1_2_PLUSX = 303;
	static final short RULE_LINKED_1_2_X = 304;
	static final short RULE_LINKED_BLOCKS_X_MIN_MAX = 305;
	static final short RULE_LINKED_BLOCKS_1_2_PLUSX = 306;
	static final short RULE_LINKED_BLOCKS_1_2_X = 307;
	static final short RULE_LINKED_FRAGS_X_MIN_MAX = 308;
	static final short RULE_LINKED_FRAGS_1_2_PLUSX = 309;
	static final short RULE_LINKED_FRAGS_1_2_X = 310;
	static final short RULE_LINKED_FRAGS_BLOCKS = 311;
	// Code Orders per group, allow repeats (W2Q)
	static final short RULE_GROUP_CASE_INCLUSIVE = 600;
	static final short RULE_GROUP_SPECIMEN_INCLUSIVE = 601;
	static final short RULE_GROUP_EVERY_X_MIN_MAX = 602;
	static final short RULE_GROUP_1_2_PLUSX = 603;
	static final short RULE_GROUP_1_2_X = 604;
	// Code Orders per group, disallow repeats (CPT)
	static final short RULE_UNIQUE_CASE_INCLUSIVE = 700;
	static final short RULE_UNIQUE_SPECIMEN_INCLUSIVE = 701;
	static final short RULE_UNIQUE_EVERY_X_MIN_MAX = 702;
	static final short RULE_UNIQUE_1_2_PLUSX = 703;
	static final short RULE_UNIQUE_1_2_X = 704;
	// Code Orders per group if ordered after routing, allow repeats (CAP)
	static final short RULE_AFTER_CASE_INCLUSIVE = 800;
	static final short RULE_AFTER_SPECIMEN_INCLUSIVE = 801;
	static final short RULE_AFTER_EVERY_X_MIN_MAX = 802;
	static final short RULE_AFTER_1_2_PLUSX = 803;
	static final short RULE_AFTER_1_2_X = 804;
	// Code Orders per pathologist as additional work (Case Reviews)
	static final short RULE_ADDL_CASE_INCLUSIVE = 900; 
	static final short RULE_ADDL_SPECIMEN_INCLUSIVE = 901;
	static final short RULE_ADDL_EVERY_X_MIN_MAX = 902;
	static final short RULE_ADDL_1_2_PLUSX = 903;
	static final short RULE_ADDL_1_2_X = 904;
}