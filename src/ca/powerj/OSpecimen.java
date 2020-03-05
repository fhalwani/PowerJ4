package ca.powerj;

import java.util.ArrayList;

class OSpecimen {
	int    noSpecs  = 0;
	int    noBlocks = 0;
	int    noSlides = 0;
	int    noHE     = 0;
	int    noSS     = 0;
	int    noIHC    = 0;
	double fte1     = 0;
	double fte2     = 0;
	double fte3     = 0;
	double fte4     = 0;
	double fte5     = 0;
	String name     = "";
	ArrayList<OSpecimen> children = new ArrayList<OSpecimen>();
}