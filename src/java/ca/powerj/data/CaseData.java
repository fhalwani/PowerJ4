package ca.powerj.data;
import java.util.ArrayList;
import java.util.Calendar;

public class CaseData {
	private boolean codeSpec = false;
	private boolean hasError = false;
	private boolean hasFrag = false;
	private boolean hasLN = false;
	private byte procID = 0;
	private byte spyID = 0;
	private byte statusID = 0;
	private byte subID = 0;
	private byte turnaroundID = 0;
	private short cutoff = 0;
	private short delay = 0;
	private short embedID = 0;
	private short facID = 0;
	private short finalID = 0;
	private short grossID = 0;
	private short mainBlocks = 0;
	private short mainSpec = 0;
	private short microID = 0;
	private short passed = 0;
	private short routeID = 0;
	private short typeID = 0;
	private int noBlocks = 0;
	private int noCases = 0;
	private int noFSSpec = 0;
	private int noFSBlks = 0;
	private int noFSSlds = 0;
	private int noHE = 0;
	private int noIHC = 0;
	private int noMol = 0;
	private int noSlides = 0;
	private int noSpec = 0;
	private int noSS = 0;
	private int noSynop = 0;
	private int embedTAT = 0;
	private int finalTAT = 0;
	private int grossTAT = 0;
	private int microTAT = 0;
	private int routeTAT = 0;
	private int value5 = 0;
	private long caseID = 0;
	private double value1 = 0;
	private double value2 = 0;
	private double value3 = 0;
	private double value4 = 0;
	private Calendar accessed = Calendar.getInstance();
	private Calendar grossed = Calendar.getInstance();
	private Calendar embeded = Calendar.getInstance();
	private Calendar microed = Calendar.getInstance();
	private Calendar routed = Calendar.getInstance();
	private Calendar scanned = Calendar.getInstance();
	private Calendar finaled = Calendar.getInstance();
	private String caseNo = "";
	private String comment = "";
	private String embedName = "";
	private String embedFull = "";
	private String facName = "";
	private String finalName = "";
	private String finalFull = "";
	private String grossName = "";
	private String grossFull = "";
	private String microName = "";
	private String microFull = "";
	private String procName = "";
	private String routeName = "";
	private String routeFull = "";
	private String specName = "";
	private String spyName = "";
	private String statusName = "";
	private String subName = "";
	private ArrayList<SpecimenData> lstSpecimens = new ArrayList<SpecimenData>();

	public void addComment(String value) {
		this.comment += value + "\n";
	}

	public void clearSpecimens() {
		this.lstSpecimens.clear();
	}

	public Calendar getAccessCalendar() {
		return accessed;
	}

	public long getAccessTime() {
		return accessed.getTimeInMillis();
	}

	public long getCaseID() {
		return caseID;
	}

	public String getCaseNo() {
		return caseNo;
	}

	public String getComment() {
		return comment + "\n--------------------------\n";
	}

	public short getCutoff() {
		return cutoff;
	}

	public short getDelay() {
		return delay;
	}

	public Calendar getEmbedCalendar() {
		return embeded;
	}

	public String getEmbedFull() {
		return embedFull;
	}

	public short getEmbedID() {
		return embedID;
	}

	public String getEmbedName() {
		return embedName;
	}

	public int getEmbedTAT() {
		return embedTAT;
	}

	public long getEmbedTime() {
		return embeded.getTimeInMillis();
	}

	public short getFacID() {
		return facID;
	}

	public String getFacName() {
		return facName;
	}

	public Calendar getFinalCalendar() {
		return finaled;
	}

	public String getFinalFull() {
		return finalFull;
	}

	public short getFinalID() {
		return finalID;
	}

	public String getFinalName() {
		return finalName;
	}

	public int getFinalTAT() {
		return finalTAT;
	}

	public long getFinalTime() {
		return finaled.getTimeInMillis();
	}

	public Calendar getGrossCalendar() {
		return grossed;
	}

	public String getGrossFull() {
		return grossFull;
	}

	public short getGrossID() {
		return grossID;
	}

	public String getGrossName() {
		return grossName;
	}

	public int getGrossTAT() {
		return grossTAT;
	}

	public long getGrossTime() {
		return grossed.getTimeInMillis();
	}

	public short getMainBlocks() {
		return mainBlocks;
	}

	public short getMainSpec() {
		return mainSpec;
	}

	public Calendar getMicroCalendar() {
		return microed;
	}

	public String getMicroFull() {
		return microFull;
	}

	public short getMicroID() {
		return microID;
	}

	public String getMicroName() {
		return microName;
	}

	public int getMicroTAT() {
		return microTAT;
	}

	public long getMicroTime() {
		return microed.getTimeInMillis();
	}

	public int getNoBlocks() {
		return noBlocks;
	}

	public int getNoCases() {
		return noCases;
	}

	public int getNoFSBlks() {
		return noFSBlks;
	}

	public int getNoFSSlds() {
		return noFSSlds;
	}

	public int getNoFSSpec() {
		return noFSSpec;
	}

	public int getNoHE() {
		return noHE;
	}

	public int getNoIHC() {
		return noIHC;
	}

	public int getNoMol() {
		return noMol;
	}

	public int getNoSlides() {
		return noSlides;
	}

	public int getNoSpecs() {
		return noSpec;
	}

	public int getNoSS() {
		return noSS;
	}

	public int getNoSynop() {
		return noSynop;
	}

	public short getPassed() {
		return passed;
	}

	public byte getProcID() {
		return procID;
	}

	public String getProcName() {
		return procName;
	}

	public Calendar getRouteCalendar() {
		return routed;
	}

	public String getRouteFull() {
		return routeFull;
	}

	public short getRouteID() {
		return routeID;
	}

	public String getRouteName() {
		return routeName;
	}

	public int getRouteTAT() {
		return routeTAT;
	}

	public long getRouteTime() {
		return routed.getTimeInMillis();
	}

	public Calendar getScanCalendar() {
		return scanned;
	}

	public long getScanTime() {
		return scanned.getTimeInMillis();
	}

	public SpecimenData getSpecimen(int value) {
		return lstSpecimens.get(value);
	}

	public String getSpecName() {
		return specName;
	}

	public byte getSpyID() {
		return spyID;
	}

	public String getSpyName() {
		return spyName;
	}

	public byte getStatusID() {
		return statusID;
	}

	public String getStatusName() {
		return statusName;
	}

	public byte getSubID() {
		return subID;
	}

	public String getSubName() {
		return subName;
	}

	public byte getTurnaroundID() {
		return turnaroundID;
	}

	public short getTypeID() {
		return typeID;
	}

	public double getValue1() {
		return value1;
	}

	public double getValue2() {
		return value2;
	}

	public double getValue3() {
		return value3;
	}

	public double getValue4() {
		return value4;
	}

	public int getValue5() {
		return value5;
	}

	public boolean isCodeSpec() {
		return codeSpec;
	}

	public boolean isHasError() {
		return hasError;
	}

	public boolean isHasFrag() {
		return hasFrag;
	}

	public boolean isHasLN() {
		return hasLN;
	}

	public void setAccessed(long value) {
		this.accessed.setTimeInMillis(value);
	}

	public void setCaseID(long value) {
		this.caseID = value;
	}

	public void setCaseNo(String value) {
		this.caseNo = value;
	}

	public void setCodeSpec(boolean value) {
		this.codeSpec = value;
	}

	public void setComment(String value) {
		this.comment = value;
	}

	public void setCutoff(short value) {
		this.cutoff = value;
	}

	public void setDelay(short value) {
		this.delay = value;
	}

	public void setEmbeded(long value) {
		this.embeded.setTimeInMillis(value);
	}

	public void setEmbedFull(String value) {
		this.embedFull = value;
	}

	public void setEmbedID(short value) {
		this.embedID = value;
	}

	public void setEmbedName(String value) {
		this.embedName = value;
	}

	public void setEmbedTAT(int value) {
		if (value > Short.MAX_VALUE) {
			this.embedTAT = Short.MAX_VALUE;
		} else {
			this.embedTAT = value;
		}
	}

	public void setFacID(short value) {
		this.facID = value;
	}

	public void setFacName(String value) {
		this.facName = value;
	}

	public void setFinaled(long value) {
		this.finaled.setTimeInMillis(value);
	}

	public void setFinalFull(String value) {
		this.finalFull = value;
	}

	public void setFinalID(short value) {
		this.finalID = value;
	}

	public void setFinalName(String value) {
		this.finalName = value;
	}

	public void setFinalTAT(int value) {
		if (value > Short.MAX_VALUE) {
			this.finalTAT = Short.MAX_VALUE;
		} else {
			this.finalTAT = value;
		}
	}

	public void setGrossed(long value) {
		this.grossed.setTimeInMillis(value);
	}

	public void setGrossFull(String value) {
		this.grossFull = value;
	}

	public void setGrossID(short value) {
		this.grossID = value;
	}

	public void setGrossName(String value) {
		this.grossName = value;
	}

	public void setGrossTAT(int value) {
		if (value > Short.MAX_VALUE) {
			this.grossTAT = Short.MAX_VALUE;
		} else {
			this.grossTAT = value;
		}
	}

	public void setHasError(boolean value) {
		this.hasError = value;
	}

	public void setHasFrag(boolean value) {
		this.hasFrag = value;
	}

	public void setHasLN(boolean value) {
		this.hasLN = value;
	}

	public void setMainBlocks(short value) {
		this.mainBlocks = value;
	}

	public void setMainSpec(short value) {
		this.mainSpec = value;
	}

	public void setMicroed(long value) {
		this.microed.setTimeInMillis(value);
	}

	public void setMicroFull(String value) {
		this.microFull = value;
	}

	public void setMicroID(short value) {
		this.microID = value;
	}

	public void setMicroName(String value) {
		this.microName = value;
	}

	public void setMicroTAT(int value) {
		if (value > Short.MAX_VALUE) {
			this.microTAT = Short.MAX_VALUE;
		} else {
			this.microTAT = value;
		}
	}

	public void setNoBlocks(int value) {
		this.noBlocks = value;
	}

	public void setNoCases(int value) {
		this.noCases = value;
	}

	public void setNoFSBlks(int value) {
		this.noFSBlks = value;
	}

	public void setNoFSSlds(int value) {
		this.noFSSlds = value;
	}

	public void setNoFSSpec(int value) {
		this.noFSSpec = value;
	}

	public void setNoHE(int value) {
		this.noHE = value;
	}

	public void setNoIHC(int value) {
		this.noIHC = value;
	}

	public void setNoMol(int value) {
		this.noMol = value;
	}

	public void setNoSlides(int value) {
		this.noSlides = value;
	}

	public void setNoSpecs(int value) {
		this.noSpec = value;
	}

	public void setNoSS(int value) {
		this.noSS = value;
	}

	public void setNoSynop(int value) {
		this.noSynop = value;
	}

	public void setPassed(short value) {
		if (value > Short.MAX_VALUE) {
			this.passed = Short.MAX_VALUE;
		} else {
			this.passed = (short) value;
		}
	}

	public void setProcID(byte value) {
		this.procID = value;
	}

	public void setProcName(String value) {
		this.procName = value;
	}

	public void setRouted(long value) {
		this.routed.setTimeInMillis(value);
	}

	public void setRouteFull(String value) {
		this.routeFull = value;
	}

	public void setRouteID(short value) {
		this.routeID = value;
	}

	public void setRouteName(String value) {
		this.routeName = value;
	}

	public void setRouteTAT(int value) {
		if (value > Short.MAX_VALUE) {
			this.routeTAT = Short.MAX_VALUE;
		} else {
			this.routeTAT = value;
		}
	}

	public void setScanned(long value) {
		this.scanned.setTimeInMillis(value);
	}

	public void setSpecimen(SpecimenData value) {
		this.lstSpecimens.add(value);
		this.noSpec++;
	}

	public void setSpecName(String value) {
		this.specName = value;
	}

	public void setSpyID(byte value) {
		this.spyID = value;
	}

	public void setSpyName(String value) {
		this.spyName = value;
	}

	public void setStatusID(byte value) {
		this.statusID = value;
	}

	public void setStatusName(String value) {
		this.statusName = value;
	}

	public void setSubID(byte value) {
		this.subID = value;
	}

	public void setSubName(String value) {
		this.subName = value;
	}

	public void setTurnaroundID(byte value) {
		this.turnaroundID = value;
	}

	public void setTypeID(short value) {
		this.typeID = value;
	}

	public void setValue1(double value) {
		this.value1 = value;
	}

	public void setValue2(double value) {
		this.value2 = value;
	}

	public void setValue3(double value) {
		this.value3 = value;
	}

	public void setValue4(double value) {
		this.value4 = value;
	}

	public void setValue5(int value) {
		this.value5 = value;
	}

	@Override
	public String toString() {
		return caseNo;
	}
}