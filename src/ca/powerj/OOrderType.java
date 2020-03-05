package ca.powerj;

class OOrderType {
	// Orders/Slides types and sub-types
	static final byte REJECT = 0;
	static final byte IGNORE = 1;
	static final byte BLK = 2;
	static final byte BLK_FS = 3;
	static final byte SLIDE = 4;
	static final byte SLD_FS = 5;
	static final byte SS = 6;
	static final byte IHC = 7;
	static final byte FISH = 8;
	static final byte EM = 9;
	static final byte MOLEC = 10;
	static final byte FCM = 11;
	static final byte ADDITINAL = 12;
	static final String[] TYPES = {"Reject", "Ignore", "BLK", "FSB", "H&E", "FSS",
		"SS", "IHC", "FISH", "EM", "MOL", "FCM", "ADDL"};
}