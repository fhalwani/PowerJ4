package ca.powerj;

class OSpecGroup {
	boolean newRow = false;
	boolean hasLN = false;
	byte subID = 0;
	byte spyID = 0;
	byte proID = 0;
	short grpID = 0;
	int value5 = 0;
	String descr = "";
	OItem[][] codes = { { new OItem(), new OItem(), new OItem(), new OItem() },
			{ new OItem(), new OItem(), new OItem(), new OItem() },
			{ new OItem(), new OItem(), new OItem(), new OItem() } };
}