package ca.powerj;
import java.util.Date;

class OPerson {
	boolean   active    = false;
	boolean[] bits      = new boolean[32];
	short     prsID     = 0;
	int       access    = 0;
	Date      started   = new Date();
	String    code      = "";
	String    initials  = "";
	String    lastname  = "";
	String    firstname = "";
}