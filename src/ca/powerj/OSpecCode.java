package ca.powerj;
import java.util.HashMap;

class OSpecCode {
	boolean codeBlocks   = true;
	boolean inclusive    = false;
	boolean needFrag     = false;
	byte    procID       = 0;
	short   coderID      = 0;
	short   coderMalig   = 0;
	short   coderRadical = 0;
	double  value        = 0;
	double  valueFS      = 0;
	HashMap<Short, OOrderCode> lstOrders = new HashMap<Short, OOrderCode>();
}