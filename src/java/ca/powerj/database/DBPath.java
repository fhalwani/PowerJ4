package ca.powerj.database;
import java.util.ArrayList;
import java.util.HashMap;
import ca.powerj.data.AccessionData;
import ca.powerj.data.CaseData;
import ca.powerj.data.AdditionalOrderData;
import ca.powerj.data.EventData;
import ca.powerj.data.FacilityData;
import ca.powerj.data.OrderData;
import ca.powerj.data.OrderMasterList;
import ca.powerj.data.OrderMasterData;
import ca.powerj.data.PathologistList;
import ca.powerj.data.PersonData;
import ca.powerj.data.SpecimenData;
import ca.powerj.data.SpecimenMasterData;
import ca.powerj.lib.LibBase;

public class DBPath extends DBBase {

	public DBPath(LibBase base) {
		super(base);
		dbName = "Pathology";
	}

	/** Close statements. */
	public void closeStms() {
	}

	/** Retrieve master accessions. */
	public HashMap<Short, AccessionData> getAccessions() {
		return null;
	}

	/** Retrieve amended and addended cases. */
	public ArrayList<CaseData> getAddenda(long fromTime, long toTime) {
		return null;
	}

	/** Retrieve Review orders of cases. */
	public ArrayList<AdditionalOrderData> getAdditionals(long fromTime, long toTime, OrderMasterList masterOrders) {
		return null;
	}

	/** Retrieve cancelled cases added since last run. */
	public boolean getCanceled(long caseID) {
		return false;
	}

	/** Retrieve a case details. */
	public CaseData getCaseDetails(long caseID) {
		return null;
	}

	/** Retrieve all events for a case. */
	public ArrayList<EventData> getCaseEvents(long caseID) {
		return null;
	}

	/** Retrieve caseID given a case number. */
	public long getCaseID(String caseNo) {
		return 0;
	}

	/** Retrieve a case locked by the current user. */
	public CaseData getCaseLocked() {
		return null;
	}

	/** Retrieve orders and stained slides for a case. */
	public ArrayList<OrderData> getCaseOrders(long caseID, long fromTime, long toTime) {
		return null;
	}

	/** Retrieve all specimens of a case. */
	public ArrayList<SpecimenData> getCaseSpecimens(long caseID) {
		return null;
	}

	/** Retrieve cancelled cases added since last run. */
	public ArrayList<CaseData> getCorrelations(long fromTime, long toTime) {
		return null;
	}

	/** Retrieve embedded status of grossed cases. */
	public boolean getEmbedded(CaseData thisCase) {
		return false;
	}

	/** Retrieve all master facilities. */
	public HashMap<Short, FacilityData> getFacilities() {
		return null;
	}

	/** Retrieve all finaled cases. */
	public ArrayList<Long> getFinaled(long fromTime, long toTime) {
		return null;
	}

	/** Retrieve final status of routed cases. */
	public boolean getFinaled(CaseData thisCase) {
		return false;
	}

	/** Retrieve grossing status of accession cases. */
	public boolean getGrossed(CaseData thisCase) {
		return false;
	}

	/** Retrieve the userID value **/
	public short getLoginID() {
		return 0;
	}

	/** Retrieve microtomy status of embedded cases. */
	public boolean getMicrotomed(CaseData thisCase) {
		return false;
	}

	/** Retrieve new cases added since last run. */
	public ArrayList<CaseData> getNewCases(long fromTime) {
		return null;
	}

	/** Retrieve Number of fragments for biopsy specimen/s (CAP). */
	public void getNoFrags(CaseData thisCase) {
	}

	/** Retrieve Number of synoptic reports for a case. */
	public byte getNoSynoptics(long caseID) {
		return 0;
	}

	/** Retrieve all master orders. */
	public HashMap<Short, OrderMasterData> getOrders() {
		return null;
	}

	/** Retrieve all master personnel. */
	public HashMap<Short, PersonData> getPersons() {
		return null;
	}

	/** Retrieve Routing status of cases. */
	public short getRouted(CaseData thisCase) {
		return 0;
	}

	/** Retrieve cases that are not routed (cytology) but slides scanned by a staff. */
	public boolean getScanned(CaseData thisCase, PathologistList pathologists) {
		return false;
	}

	/** Retrieve first scan time by final ID. */
	public boolean getScanned(CaseData thisCase) {
		return false;
	}

	/** Retrieve Number of blocks for a specimen. */
	public short getSpecimenBlocks(long specID) {
		return 0;
	}

	/** Retrieve orders and stained slides for a specimen. */
	public ArrayList<OrderData> getSpecimenOrders(long specID, long finalTime) {
		return null;
	}

	/** Retrieve all master specimens. */
	public HashMap<Short, SpecimenMasterData> getSpecimens() {
		return null;
	}

	public int setSpecimenID(short masterID, long specID) {
		return 0;
	}
}