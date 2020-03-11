package ca.powerj;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

class DPowerpath extends DCore {
	static final byte STM_ACCESSIONS   = 1;
	static final byte STM_CASE_BLOCKS  = 2;
	static final byte STM_CASE_DETAILS = 3;
	static final byte STM_CASE_DIAGNOS = 4;
	static final byte STM_CASE_EMBEDED = 5;
	static final byte STM_CASE_EVENTS  = 6;
	static final byte STM_CASE_GROSS   = 7;
	static final byte STM_CASE_MICROTO = 8;
	static final byte STM_CASE_NUMBER  = 9;
	static final byte STM_CASE_ORDERS  = 10;
	static final byte STM_CASE_PROCESS = 11;
	static final byte STM_CASE_ROUTING = 12;
	static final byte STM_CASE_SCANNED = 13;
	static final byte STM_CASE_SPCMNS  = 14;
	static final byte STM_CASE_STAINED = 15;
	static final byte STM_CASE_SYNOPTI = 16;
	static final byte STM_CASES_ACCESS = 17;
	static final byte STM_CASES_ADDEND = 18;
	static final byte STM_CASES_FINAL  = 19;
	static final byte STM_CASES_REVIEW = 20;
	static final byte STM_CORRELATIONS = 21;
	static final byte STM_FACILITIES   = 22;
	static final byte STM_PERS_EVENTS  = 23;
	static final byte STM_PERS_ID      = 24;
	static final byte STM_PERS_LOGIN   = 25;
	static final byte STM_PERSONNEL    = 26;
	static final byte STM_PERS_ORDERS  = 27;
	static final byte STM_PERS_PROCES  = 28;
	static final byte STM_PERS_UNSCAN  = 29;
	static final byte STM_PROCEDURES   = 30;
	static final byte STM_SPECIMENS    = 31;
	static final byte STM_SPEC_ORDERS  = 32;
	static final byte STM_SPEC_UPDATE  = 33;

	DPowerpath(LBase parent) {
		super(parent);
		dbName = "PowerPath";
		if (!parent.offLine) {
			setConnection();
		}
	}

	String getSql(byte index) {
		return setSQL(index);
	}

	void prepareEditor() {
		pstms.put(STM_CASE_EVENTS,  prepareStatement(setSQL(STM_CASE_EVENTS)));
		pstms.put(STM_CASE_NUMBER,  prepareStatement(setSQL(STM_CASE_NUMBER)));
		pstms.put(STM_CASE_SPCMNS,  prepareStatement(setSQL(STM_CASE_SPCMNS)));
		pstms.put(STM_SPEC_UPDATE,  prepareStatement(setSQL(STM_SPEC_UPDATE)));
	}

	void prepareLogin() {
		pstms.put(STM_PERS_LOGIN,  prepareStatement(setSQL(STM_PERS_LOGIN)));
	}

	void prepareSynchronizer() {
		pstms.put(STM_ACCESSIONS, prepareStatement(setSQL(STM_ACCESSIONS)));
		pstms.put(STM_FACILITIES, prepareStatement(setSQL(STM_FACILITIES)));
		pstms.put(STM_PERSONNEL, prepareStatement(setSQL(STM_PERSONNEL)));
		pstms.put(STM_PROCEDURES, prepareStatement(setSQL(STM_PROCEDURES)));
		pstms.put(STM_SPECIMENS,  prepareStatement(setSQL(STM_SPECIMENS)));
	}

	void prepareWorkflow() {
		pstms.put(STM_CASE_BLOCKS,  prepareStatement(setSQL(STM_CASE_BLOCKS)));
		pstms.put(STM_CASE_EMBEDED, prepareStatement(setSQL(STM_CASE_EMBEDED)));
		pstms.put(STM_CASE_MICROTO, prepareStatement(setSQL(STM_CASE_MICROTO)));
		pstms.put(STM_CASE_ORDERS,  prepareStatement(setSQL(STM_CASE_ORDERS)));
		pstms.put(STM_CASE_PROCESS, prepareStatement(setSQL(STM_CASE_PROCESS)));
		pstms.put(STM_CASE_ROUTING, prepareStatement(setSQL(STM_CASE_ROUTING)));
		pstms.put(STM_CASE_SPCMNS,  prepareStatement(setSQL(STM_CASE_SPCMNS)));
		pstms.put(STM_CASES_ACCESS, prepareStatement(setSQL(STM_CASES_ACCESS)));
	}

	void prepareWorkload() {
		pstms.put(STM_CASE_BLOCKS,  prepareStatement(setSQL(STM_CASE_BLOCKS)));
		pstms.put(STM_CASE_DETAILS, prepareStatement(setSQL(STM_CASE_DETAILS)));
		pstms.put(STM_CASE_DIAGNOS, prepareStatement(setSQL(STM_CASE_DIAGNOS)));
		pstms.put(STM_CASE_EMBEDED, prepareStatement(setSQL(STM_CASE_EMBEDED)));
		pstms.put(STM_CASE_GROSS,   prepareStatement(setSQL(STM_CASE_GROSS)));
		pstms.put(STM_CASE_MICROTO, prepareStatement(setSQL(STM_CASE_MICROTO)));
		pstms.put(STM_CASE_NUMBER,  prepareStatement(setSQL(STM_CASE_NUMBER)));
		pstms.put(STM_CASE_ORDERS,  prepareStatement(setSQL(STM_CASE_ORDERS)));
		pstms.put(STM_CASE_PROCESS, prepareStatement(setSQL(STM_CASE_PROCESS)));
		pstms.put(STM_CASE_ROUTING, prepareStatement(setSQL(STM_CASE_ROUTING)));
		pstms.put(STM_CASE_SCANNED,  prepareStatement(setSQL(STM_CASE_SCANNED)));
		pstms.put(STM_CASE_SPCMNS,  prepareStatement(setSQL(STM_CASE_SPCMNS)));
		pstms.put(STM_CASE_SYNOPTI, prepareStatement(setSQL(STM_CASE_SYNOPTI)));
		pstms.put(STM_CASES_ADDEND, prepareStatement(setSQL(STM_CASES_ADDEND)));
		pstms.put(STM_CASES_FINAL,  prepareStatement(setSQL(STM_CASES_FINAL)));
		pstms.put(STM_CASES_REVIEW, prepareStatement(setSQL(STM_CASES_REVIEW)));
		pstms.put(STM_CORRELATIONS, prepareStatement(setSQL(STM_CORRELATIONS)));
		pstms.put(STM_SPEC_ORDERS,  prepareStatement(setSQL(STM_SPEC_ORDERS)));
	}

	private void setConnection() {
		SQLServerDataSource ds = new SQLServerDataSource();
		try {
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			ds.setPortNumber(pj.numbers.parseInt(pj.apPort));
			ds.setServerName(pj.apHost);
			ds.setDatabaseName(pj.apSchema);
			ds.setUser(pj.apUser);
			ds.setPassword(pj.apPass);
			connection = ds.getConnection();
		} catch (SQLServerException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private String setSQL(short id) {
		switch (id) {
		case STM_ACCESSIONS:
			return "SELECT id, name FROM acc_type ORDER BY id";
		case STM_CASE_BLOCKS:
			return "SELECT count(*) AS Blocks FROM acc_block WITH (NOLOCK) WHERE acc_specimen_id = ?";
		case STM_CASE_DETAILS:
			return "SELECT a.accession_no, a.created_date, a.facility_id, a.acc_type_id, s.completed_date, s.assigned_to_id\n" +
			"FROM accession_2 AS a WITH (NOLOCK) INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id\n" +
			"INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id WHERE a.id = ? AND p.description = 'Final'";
		case STM_CASE_DIAGNOS:
			return "SELECT finding, finding_text FROM acc_results WITH (NOLOCK) WHERE acc_id = ?";
		case STM_CASE_EMBEDED:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK)\n" +
			"WHERE acc_id = ? AND source_rec_type = 'B' AND amp_mode = 'embedding' AND event_type = 'material_scan'\n" +
			"ORDER BY event_date";
		case STM_CASE_EVENTS:
			return "SELECT event_date, source_rec_type, material_label, event_type, event_location, event_description\n" + 
			"FROM acc_amp_event WITH (NOLOCK) WHERE acc_id = ? ORDER BY event_date";
		case STM_CASE_GROSS:
			return "SELECT r.finding, r.finding_text FROM acc_results AS r WITH (NOLOCK)\n" +
			"INNER JOIN path_rpt_heading AS h WITH (NOLOCK) ON h.id = r.heading_id\n" +
			"WHERE r.acc_id = ? AND h.name LIKE 'gross%'";
		case STM_CASE_MICROTO:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK)\n" +
			"WHERE acc_id = ? AND source_rec_type = 'B' AND amp_mode = 'histology' AND event_type = 'material_scan'\n" +
			"ORDER BY event_date";
		case STM_CASE_NUMBER:
			return "SELECT id FROM accession_2 WITH (NOLOCK) WHERE accession_no = ?";
		case STM_CASE_ORDERS:
			return "SELECT o.procedure_id, o.quantity, o.created_date, p.code FROM acc_order AS o WITH (NOLOCK) \n" +
			"INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id \n" +
			"WHERE o.acc_id = ? AND (o.created_date BETWEEN ? AND ?) AND p.preparation_catg = 'L' \n" +
			"ORDER BY o.procedure_id";
		case STM_CASE_PROCESS:
			return "SELECT a.assigned_to_id, a.completed_date, s.description FROM acc_process_step AS a WITH (NOLOCK)\n" +
			"INNER JOIN process_step AS s WITH (NOLOCK) ON s.id = a.step_id WHERE a.acc_id = ? ORDER BY s.sort_ord";
		case STM_CASE_ROUTING:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK)\n" +
			"WHERE acc_id = ? AND source_rec_type = 'L' AND amp_mode = 'slide distribution'\n" +
			"AND event_type = 'material_routed' ORDER BY event_date";
		case STM_CASE_SCANNED:
			return "SELECT event_date FROM acc_amp_event WITH (NOLOCK)\n" +
			"WHERE acc_id = ? AND event_type = 'material_scan'\n" +
			"AND source_rec_type = 'L' AND amp_mode = 'Diagnostician'\n" +
			"ORDER BY event_date";
		case STM_CASE_SPCMNS:
			// Get specimens of a case (use left outer join, or no specimens in autopsies)
			return "SELECT s.id, s.specimen_label, s.tmplt_profile_id, s.description, s.collection_date, s.recv_date, t.code, c.label_name\n" +
			"FROM acc_specimen AS s WITH (NOLOCK) LEFT OUTER JOIN tmplt_profile AS t WITH (NOLOCK) ON t.id = s.tmplt_profile_id\n" +
			"LEFT OUTER JOIN specimen_category AS c WITH (NOLOCK) ON c.id = s.specimen_category_id\n" +
			"WHERE s.acc_id = ? ORDER BY s.specimen_label";
		case STM_CASE_STAINED:
			return "SELECT event_date, personnel_id FROM acc_amp_event WITH (NOLOCK) WHERE acc_id = ?\n" +
			"AND source_rec_type = 'L' AND amp_mode = 'slide distribution' AND event_type = 'slide_completed'\n" +
			"ORDER BY event_date";
		case STM_CASE_SYNOPTI:
			return "SELECT count(*) AS Synoptics FROM acc_worksheet WITH (NOLOCK) WHERE acc_id = ?";
		case STM_CASES_ACCESS:
			return "SELECT id, acc_type_id, facility_id, created_date, accession_no FROM accession_2 WITH (NOLOCK)\n" +
			"WHERE created_date > ? AND imported_case = 'N' ORDER BY created_date";
		case STM_CASES_ADDEND:
			return "SELECT a.acc_id, a.assigned_to_id, a.completed_date, s.description FROM acc_process_step AS a WITH (NOLOCK)\n" +
			"INNER JOIN process_step AS s WITH (NOLOCK) ON s.id = a.step_id WHERE (a.completed_date BETWEEN ? AND ?) AND s.type = 'F'\n" +
			"ORDER BY a.acc_id, a.completed_date";
		case STM_CASES_FINAL:
			return "SELECT a.id AS CaseID, a.acc_type_id, a.facility_id FROM accession_2 AS a WITH (NOLOCK)\n" +
			"INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id\n" +
			"INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id\n" +
			"WHERE (s.completed_date BETWEEN ? AND ?) AND p.description = 'Final' ORDER BY s.completed_date";
		case STM_CASES_REVIEW:
			return "SELECT o.acc_id, o.created_date, o.procedure_id, o.quantity, o.ordered_by_id, p.code\n" +
			"FROM acc_order AS o WITH (NOLOCK) INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id\n" +
			"WHERE o.created_date BETWEEN ? AND ? ORDER BY o.acc_id, o.created_date";
		case STM_CORRELATIONS:
			return "SELECT c.correlation_date, c.correlated_by_id, s.acc_id FROM acc_correlation AS c WITH (NOLOCK)\n" +
			"INNER JOIN acc_specimen AS s WITH (NOLOCK) ON s.id = c.correlated_specimen_id\n" +
			"WHERE (c.correlation_date BETWEEN ? AND ?) AND c.correlation_status = 'C' ORDER BY s.acc_id";
		case STM_FACILITIES:
			return "SELECT id, code, name FROM facility ORDER BY id";
		case STM_PERS_EVENTS:
			return "SELECT e.event_date, e.source_rec_type, e.material_label, e.event_location,\n" +
			"e.event_description, e.amp_mode, e.event_type, a.accession_no\n" +
			"FROM acc_amp_event AS e WITH (NOLOCK) INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = e.acc_id\n" +
			"WHERE (e.event_date BETWEEN ? AND ?) AND e.personnel_id = ? ORDER BY e.event_date";
		case STM_PERS_ID:
			return "SELECT last_name, first_name FROM personnel_2 WITH (NOLOCK) WHERE id = ?";
		case STM_PERS_LOGIN:
			return "SELECT id FROM personnel_2 WHERE login_name = ?";
		case STM_PERSONNEL:
			return "SELECT id, persnl_class_id, last_name, first_name FROM personnel_2 ORDER BY id";
		case STM_PERS_ORDERS:
			return "SELECT o.created_date, p.code, a.accession_no FROM acc_order AS o WITH (NOLOCK)\n" +
			"INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id\n" +
			"INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = o.acc_id\n" +
			"WHERE (o.created_date BETWEEN ? AND ?) AND o.ordered_by_id = ? ORDER BY o.created_date";
		case STM_PERS_PROCES:
			return "SELECT aps.completed_date, ps.description, a.accession_no FROM acc_process_step AS aps WITH (NOLOCK)\n" +
			"INNER JOIN process_step AS ps WITH (NOLOCK) ON ps.id = aps.step_id\n" +
			"INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = aps.acc_id\n" +
			"WHERE (aps.completed_date BETWEEN ? AND ?) AND aps.assigned_to_id = ? ORDER BY aps.completed_date";
		case STM_PERS_UNSCAN:
			return "SELECT DISTINCT a.id, a.accession_no, s.completed_date, pr.last_name, pr.first_name\n" +
			"FROM accession_2 AS a WITH (NOLOCK) INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id\n" +
			"INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id INNER JOIN personnel_2 AS pr WITH (NOLOCK) ON pr.id = s.assigned_to_id\n" +
			"WHERE s.completed_date BETWEEN ? AND ? AND p.description = 'Final' AND a.accession_no NOT LIKE 'CN%' AND a.accession_no NOT LIKE 'NSW%'\n" +
			"AND a.accession_no NOT LIKE 'OIM%' AND a.id NOT IN (SELECT DISTINCT a.id FROM accession_2 AS a WITH (NOLOCK)\n" +
			"INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id INNER JOIN acc_amp_event AS e WITH (NOLOCK) ON e.acc_id = a.id\n" +
			"WHERE e.event_date BETWEEN ? AND ? AND e.personnel_id = s.assigned_to_id AND e.event_type = 'material_scan') ORDER BY pr.last_name, s.completed_date";
		case STM_PROCEDURES:
			return "SELECT id, code, description FROM lab_procedure ORDER BY id";
		case STM_SPECIMENS:
			return "SELECT id, code, description FROM tmplt_profile WHERE type = 'S' ORDER BY id";
		case STM_SPEC_ORDERS:
			return "SELECT o.procedure_id, o.quantity, o.created_date, p.code FROM acc_order AS o WITH (NOLOCK) \n" +
			"INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id \n" +
			"WHERE o.acc_specimen_id = ? AND o.created_date < ? \n" +
			"ORDER BY o.procedure_id";
		default:
			return "UPDATE acc_specimen SET tmplt_profile_id = ? WHERE id = ?";
		}
	}
}