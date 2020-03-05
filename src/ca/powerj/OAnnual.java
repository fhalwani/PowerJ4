package ca.powerj;
import java.util.HashMap;

class OAnnual {
	byte   spyID      = 0;
	byte   subID      = 0;
	byte   proID      = 0;
	short  facID      = 0;
	String facility   = "";
	String specialty  = "";
	String subspecial = "";
	String procedure  = "";
	HashMap<Short, Integer> cases  = new HashMap<Short, Integer>();
	HashMap<Short, Integer> specs  = new HashMap<Short, Integer>();
	HashMap<Short, Integer> blocks = new HashMap<Short, Integer>();
	HashMap<Short, Integer> slides = new HashMap<Short, Integer>();
	HashMap<Short, Integer> he     = new HashMap<Short, Integer>();
	HashMap<Short, Integer> ss     = new HashMap<Short, Integer>();
	HashMap<Short, Integer> ihc    = new HashMap<Short, Integer>();
	HashMap<Short, Integer> synopt = new HashMap<Short, Integer>();
	HashMap<Short, Integer> frozen = new HashMap<Short, Integer>();
	HashMap<Short, Double>  fte1   = new HashMap<Short, Double>();
	HashMap<Short, Double>  fte2   = new HashMap<Short, Double>();
	HashMap<Short, Double>  fte3   = new HashMap<Short, Double>();
	HashMap<Short, Double>  fte4   = new HashMap<Short, Double>();
	HashMap<Short, Double>  fte5   = new HashMap<Short, Double>();
}