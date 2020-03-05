package ca.powerj;

import java.util.HashMap;

class OSpecFinal {
	byte errorID = 0;
	byte procID = 0;
	byte subID = 0;
	short spmID = 0;
	short noFrags = 1;
	short noBlocks = 0;
	short noFSBlks = 0;
	short noSlides = 0;
	short noFSSlds = 0;
	short noHE = 0;
	short noSS = 0;
	short noIHC = 0;
	short noMOL = 0;
	int value5 = 0;
	double value1 = 0;
	double value2 = 0;
	double value3 = 0;
	double value4 = 0;
	long specID = 0;
	String name = "";
	String descr = "";
	HashMap<Short, OOrderFinal> lstOrders = new HashMap<Short, OOrderFinal>();
}