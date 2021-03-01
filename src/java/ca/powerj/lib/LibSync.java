package ca.powerj.lib;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ca.powerj.app.PJServer;
import ca.powerj.data.AccessionData;
import ca.powerj.data.FacilityData;
import ca.powerj.data.OrderMasterData;
import ca.powerj.data.PersonData;
import ca.powerj.data.SpecimenMasterData;

public class LibSync {
	private final String className = "Sync";
	private PJServer base;

	public LibSync(PJServer base) {
		this.base = base;
		base.setBusy(true);
		base.log(LibConstants.ERROR_NONE, className,
				base.dates.formatter(LibDates.FORMAT_DATETIME) + " - Sync Manager Started...");
		base.dbPath.setStatements(LibConstants.ACTION_LSYNC);
		if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
			base.dbPowerJ.setStatements(LibConstants.ACTION_LSYNC);
		}
		if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
			syncAccessions();
		}
		if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
			syncFacilities();
		}
		if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
			syncOrderMaster();
		}
		if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
			syncPersons();
		}
		if (base.errorID == LibConstants.ERROR_NONE && !base.isStopping()) {
			syncSpecimenMaster();
		}
		close();
	}

	private void close() {
		if (base.dbPath != null) {
			base.dbPath.closeStms();
		}
		if (base.dbPowerJ != null) {
			base.dbPowerJ.closeStms();
		}
		base.setBusy(false);
	}

	private void syncAccessions() {
		short noInserts = 0, noUpdates = 0;
		AccessionData apAccession = new AccessionData();
		AccessionData pjAccession = new AccessionData();
		HashMap<Short, AccessionData> apAccessions = base.dbPath.getAccessions();
		HashMap<Short, AccessionData> pjAccessions = base.dbPowerJ.getAccessions();
		for (Map.Entry entry : apAccessions.entrySet()) {
			apAccession = (AccessionData) entry.getValue();
			pjAccession = pjAccessions.get(entry.getKey());
			if (pjAccession == null) {
				noInserts += base.dbPowerJ.setAccession(true, apAccession);
			} else if (!pjAccession.getName().equals(apAccession.getName())) {
				pjAccession.setName(apAccession.getName());
				noUpdates += base.dbPowerJ.setAccession(false, pjAccession);
			}
		}
		if (noUpdates > 0 || noInserts > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					String.format("Found %d new and %d updated accessions.", noInserts, noUpdates));
		}
	}

	private void syncFacilities() {
		short noInserts = 0, noUpdates = 0;
		FacilityData apFacility = new FacilityData();
		FacilityData pjFacility = new FacilityData();
		HashMap<Short, FacilityData> apFacilities = base.dbPath.getFacilities();
		ArrayList<FacilityData> temp = base.dbPowerJ.getFacilities(true);
		HashMap<Short, FacilityData> pjFacilities = new HashMap<Short, FacilityData>();
		for (int i = 0; i < temp.size(); i++) {
			pjFacility = temp.get(i);
			pjFacilities.put(pjFacility.getFacID(), pjFacility);
		}
		for (Map.Entry entry : apFacilities.entrySet()) {
			apFacility = (FacilityData) entry.getValue();
			pjFacility = pjFacilities.get((Short) entry.getKey());
			if (pjFacility == null) {
				base.log(LibConstants.ERROR_NONE, className, "Inserting facility "
						+ apFacility.getDescr() + ", key " + entry.getKey());
				noInserts += base.dbPowerJ.setFacility(true, apFacility);
			}
		}
		if (noUpdates > 0 || noInserts > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					String.format("Found %d new and %d updated facilities.", noInserts, noUpdates));
		}
	}

	private void syncOrderMaster() {
		short noInserts = 0, noUpdates = 0;
		OrderMasterData apOrder = new OrderMasterData();
		OrderMasterData pjOrder = new OrderMasterData();
		HashMap<Short, OrderMasterData> apOrders = base.dbPath.getOrders();
		ArrayList<OrderMasterData> temp = base.dbPowerJ.getOrderMasters();
		HashMap<Short, OrderMasterData> pjOrders = new HashMap<Short, OrderMasterData>();
		for (int i = 0; i < temp.size(); i++) {
			pjOrder = temp.get(i);
			pjOrders.put(pjOrder.getOrdID(), pjOrder);
		}
		temp.clear();
		for (Map.Entry entry : apOrders.entrySet()) {
			apOrder = (OrderMasterData) entry.getValue();
			pjOrder = pjOrders.get(entry.getKey());
			if (pjOrder == null) {
				noInserts += base.dbPowerJ.setOrderMaster(true, apOrder);
			} else if (!pjOrder.getName().equals(apOrder.getName())
					|| !pjOrder.getDescr().equals(apOrder.getDescr())) {
				pjOrder.setName(apOrder.getName());
				pjOrder.setDescr(apOrder.getDescr());
				noUpdates += base.dbPowerJ.setOrderMaster(false, pjOrder);
			}
		}
		if (noUpdates > 0 || noInserts > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					String.format("Found %d new and %d updated orders.", noInserts, noUpdates));
		}
	}

	private void syncPersons() {
		short noInserts = 0, noUpdates = 0;
		PersonData apPerson = new PersonData();
		PersonData pjPerson = new PersonData();
		HashMap<Short, PersonData> apPersons = base.dbPath.getPersons();
		ArrayList<PersonData> temp = base.dbPowerJ.getPersons();
		HashMap<Short, PersonData> pjPersons = new HashMap<Short, PersonData>();
		for (int i = 0; i < temp.size(); i++) {
			pjPerson = temp.get(i);
			pjPersons.put(pjPerson.getPrsID(), pjPerson);
		}
		temp.clear();
		for (Map.Entry entry : apPersons.entrySet()) {
			apPerson = (PersonData) entry.getValue();
			pjPerson = pjPersons.get(entry.getKey());
			if (pjPerson == null) {
				noInserts += base.dbPowerJ.setPerson(true, apPerson);
			} else if (!pjPerson.getLastname().equals(apPerson.getLastname())
					|| !pjPerson.getFirstname().equals(apPerson.getFirstname())
					|| !apPerson.getCode().equals(apPerson.getCode())) {
				pjPerson.setCode(apPerson.getCode());
				pjPerson.setLastname(apPerson.getLastname());
				pjPerson.setFirstname(apPerson.getFirstname());
				noUpdates += base.dbPowerJ.setPerson(false, pjPerson);
			}
		}
		if (noUpdates > 0 || noInserts > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					String.format("Found %d new and %d updated persons.", noInserts, noUpdates));
		}
	}

	private void syncSpecimenMaster() {
		short noInserts = 0, noUpdates = 0;
		SpecimenMasterData apSpecimen = new SpecimenMasterData();
		SpecimenMasterData pjSpecimen = new SpecimenMasterData();
		HashMap<Short, SpecimenMasterData> apSpecimens = base.dbPath.getSpecimens();
		ArrayList<SpecimenMasterData> temp = base.dbPowerJ.getSpecimenMasters();
		HashMap<Short, SpecimenMasterData> pjSpecimens = new HashMap<Short, SpecimenMasterData>();
		for (int i = 0; i < temp.size(); i++) {
			pjSpecimen = temp.get(i);
			pjSpecimens.put(pjSpecimen.getSpmID(), pjSpecimen);
		}
		for (Map.Entry entry : apSpecimens.entrySet()) {
			apSpecimen = (SpecimenMasterData) entry.getValue();
			pjSpecimen = pjSpecimens.get(entry.getKey());
			if (pjSpecimen == null) {
				noInserts += base.dbPowerJ.setSpecimenMaster(true, apSpecimen);
			} else if (!pjSpecimen.getName().equals(apSpecimen.getName())
					|| !pjSpecimen.getDescr().equals(apSpecimen.getDescr())) {
				pjSpecimen.setName(apSpecimen.getName());
				pjSpecimen.setDescr(apSpecimen.getDescr());
				noUpdates += base.dbPowerJ.setSpecimenMaster(false, pjSpecimen);
			}
		}
		if (noUpdates > 0 || noInserts > 0) {
			base.log(LibConstants.ERROR_NONE, className,
					String.format("Found %d new and %d updated specimens.", noInserts, noUpdates));
		}
	}
}