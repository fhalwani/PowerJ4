package ca.powerj;
import java.util.ArrayList;

class OWorklist {
	short  id       = 0;
	int    noCases  = 0;
	int    noSpecs  = 0;
	int    noBlocks = 0;
	int    noSlides = 0;
	double fte1     = 0;
	double fte2     = 0;
	double fte3     = 0;
	double fte4     = 0;
	double fte5     = 0;
	String name     = "";
	ArrayList<OWorklist> children = new ArrayList<OWorklist>();
}