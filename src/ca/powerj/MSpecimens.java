package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

class MSpecimens {
	// private final byte SPECIALTY_ALL = 0;
	private final byte SPECIALTY_GENERAL = 1;
	private final byte SPECIALTY_MSK = 2;
	private final byte SPECIALTY_DERM = 3;
	private final byte SPECIALTY_LYMPHOMA = 4;
	private final byte SPECIALTY_CARDIAC = 5;
	private final byte SPECIALTY_ENT = 6;
	private final byte SPECIALTY_BREAST = 7;
	private final byte SPECIALTY_THORACIC = 8;
	private final byte SPECIALTY_GU = 9;
	private final byte SPECIALTY_GYN = 10;
	private final byte SPECIALTY_GI = 11;
	private final byte SPECIALTY_OPHTHALMIC = 12;
	// private final byte SPECIALTY_RENAL = 13;
	private final byte SPECIALTY_NEURO = 14;
	// private final byte SPECIALTY_BM = 15;
	private short masterID = 0, groupID = 0;
	private OSpecGroup specGroup = new OSpecGroup();
	private HashMap<Short, Short> specMaster = new HashMap<Short, Short>();
	private HashMap<Short, OSpecGroup> specGroups = new HashMap<Short, OSpecGroup>();

	MSpecimens(LBase parent, PreparedStatement pstm) {
		readDB(parent, pstm);
	}

	void close() {
		specMaster.clear();
		specGroups.clear();
	}

	int getValue5() {
		return specGroup.value5;
	}

	short getCoderID(byte row, byte col) {
		return specGroup.codes[col - 1][row - 1].id;
	}

	byte getProcedureID() {
		return specGroup.proID;
	}

	byte getSubspecialtyID() {
		return specGroup.subID;
	}

	byte getSubspecialtyID(String strLabel, String strDescr) {
		byte subID = specGroup.subID;
		if (subID <= SPECIALTY_GENERAL) {
			if (strLabel == null) {
				// Null in autopsies
				strLabel = "";
			}
			if (strDescr == null) {
				strDescr = "";
			}
			if (strLabel.length() > 0 || strDescr.length() > 0) {
				subID = matchSpecialty(strLabel, strDescr);
			}
		}
		return subID;
	}

	boolean hasLN() {
		return specGroup.hasLN;
	}

	/** Match a specimen from PowerPath to one from PowerJ */
	boolean matchSpecimens(short id) {
		if (masterID != id) {
			masterID = id;
			if (specMaster.get(id) == null) {
				groupID = 0;
				specGroup = null;
			} else {
				groupID = specMaster.get(id);
				specGroup = specGroups.get(groupID);
			}
		}
		if (specGroup != null) {
			return true;
		}
		return false;
	}

	/** Manually match unknown sub-specialties (peritoneum, LN, FSEC, etc). */
	private byte matchSpecialty(String strLabel, String strDescr) {
		strLabel = strLabel.trim().toLowerCase();
		strDescr = strDescr.trim().toLowerCase();
		if (strLabel.equals("gi"))
			return SPECIALTY_GI;
		if (strLabel.contains("respiratory"))
			return SPECIALTY_THORACIC;
		if (strLabel.contains("mediastinum"))
			return SPECIALTY_THORACIC;
		if (strLabel.contains("extremities"))
			return SPECIALTY_MSK;
		if (strLabel.contains("placenta"))
			return SPECIALTY_GYN;
		if (strLabel.contains("salivary"))
			return SPECIALTY_ENT;
		if (strLabel.contains("thyroid"))
			return SPECIALTY_ENT;
		if (strLabel.contains("urinary"))
			return SPECIALTY_GU;
		if (strLabel.contains("breast"))
			return SPECIALTY_BREAST;
		if (strLabel.contains("kidney"))
			return SPECIALTY_GU;
		if (strLabel.contains("uterus"))
			return SPECIALTY_GYN;
		if (strLabel.contains("endocx"))
			return SPECIALTY_GYN;
		if (strLabel.contains("spleen"))
			return SPECIALTY_LYMPHOMA;
		if (strLabel.contains("marrow"))
			return SPECIALTY_LYMPHOMA;
		if (strLabel.contains("joint"))
			return SPECIALTY_MSK;
		if (strLabel.contains("heart"))
			return SPECIALTY_CARDIAC;
		if (strLabel.contains("ovary"))
			return SPECIALTY_GYN;
		if (strLabel.contains("neuro"))
			return SPECIALTY_NEURO;
		if (strLabel.contains("skin"))
			return SPECIALTY_DERM;
		if (strLabel.contains("oral"))
			return SPECIALTY_ENT;
		if (strLabel.contains("male"))
			return SPECIALTY_GU;
		if (strLabel.contains("soft"))
			return SPECIALTY_MSK;
		if (strLabel.contains("ent"))
			return SPECIALTY_ENT;
		if (strLabel.contains("liv"))
			return SPECIALTY_GI;
		if (strLabel.contains("eye"))
			return SPECIALTY_OPHTHALMIC;
		// Breast
		if (strDescr.contains("mastectomy"))
			return SPECIALTY_BREAST;
		if (strDescr.contains("breast"))
			return SPECIALTY_BREAST;
		if (strDescr.contains("nipple"))
			return SPECIALTY_BREAST;
		// Cardiac
		if (strDescr.contains("aneurysm"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("plaque"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("aortic"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("aorta"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("mitral"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("tricuspid"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("carotid"))
			return SPECIALTY_CARDIAC;
		if (strDescr.contains("varicose"))
			return SPECIALTY_CARDIAC;
		// Derm
		if (strDescr.contains("skin"))
			return SPECIALTY_DERM;
		if (strDescr.contains("melanoma"))
			return SPECIALTY_DERM;
		if (strDescr.contains("auricular"))
			return SPECIALTY_DERM;
		if (strDescr.contains("auricle"))
			return SPECIALTY_DERM;
		if (strDescr.contains("buttock"))
			return SPECIALTY_DERM;
		// ENT
		if (strDescr.contains("thyroid"))
			return SPECIALTY_ENT;
		if (strDescr.contains("laryngeal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("larynx"))
			return SPECIALTY_ENT;
		if (strDescr.contains("pharynx"))
			return SPECIALTY_ENT;
		if (strDescr.contains("pharyngeal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("glotti"))
			return SPECIALTY_ENT;
		if (strDescr.contains("tongue"))
			return SPECIALTY_ENT;
		if (strDescr.contains("tonsil"))
			return SPECIALTY_ENT;
		if (strDescr.contains("palate"))
			return SPECIALTY_ENT;
		if (strDescr.contains("submandibula"))
			return SPECIALTY_ENT;
		if (strDescr.contains("adrenal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("nasal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("nose"))
			return SPECIALTY_ENT;
		if (strDescr.contains("parotid"))
			return SPECIALTY_ENT;
		if (strDescr.contains("vocal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("buccal"))
			return SPECIALTY_ENT;
		if (strDescr.contains("Submental"))
			return SPECIALTY_ENT;
		// GI
		if (strDescr.contains("liver"))
			return SPECIALTY_GI;
		if (strDescr.contains("hepatic"))
			return SPECIALTY_GI;
		if (strDescr.contains("pancrea"))
			return SPECIALTY_GI;
		if (strDescr.contains("rectum"))
			return SPECIALTY_GI;
		if (strDescr.contains("rectal"))
			return SPECIALTY_GI;
		if (strDescr.contains("sigmoid"))
			return SPECIALTY_GI;
		if (strDescr.contains("cecum"))
			return SPECIALTY_GI;
		if (strDescr.contains("cecal"))
			return SPECIALTY_GI;
		if (strDescr.contains("colon"))
			return SPECIALTY_GI;
		if (strDescr.contains("perianal"))
			return SPECIALTY_GI;
		if (strDescr.contains("anal"))
			return SPECIALTY_GI;
		// GU
		if (strDescr.contains("foreskin"))
			return SPECIALTY_GU;
		if (strDescr.contains("kidney"))
			return SPECIALTY_GU;
		if (strDescr.contains("renal"))
			return SPECIALTY_GU;
		if (strDescr.contains("nephric"))
			return SPECIALTY_GU;
		if (strDescr.contains("prostat"))
			return SPECIALTY_GU;
		if (strDescr.contains("ureter"))
			return SPECIALTY_GU;
		if (strDescr.contains("urethra"))
			return SPECIALTY_GU;
		if (strDescr.contains("epididym"))
			return SPECIALTY_GU;
		if (strDescr.contains("scrotum"))
			return SPECIALTY_GU;
		if (strDescr.contains("scrotal"))
			return SPECIALTY_GU;
		// Gyne
		if (strDescr.contains("vagina"))
			return SPECIALTY_GYN;
		if (strDescr.contains("vulva"))
			return SPECIALTY_GYN;
		if (strDescr.contains("uterus"))
			return SPECIALTY_GYN;
		if (strDescr.contains("uterine"))
			return SPECIALTY_GYN;
		if (strDescr.contains("ovary"))
			return SPECIALTY_GYN;
		if (strDescr.contains("ovarian"))
			return SPECIALTY_GYN;
		if (strDescr.contains("endocervi"))
			return SPECIALTY_GYN;
		if (strDescr.contains("endometri"))
			return SPECIALTY_GYN;
		if (strDescr.contains("fetus"))
			return SPECIALTY_GYN;
		if (strDescr.contains("fetal"))
			return SPECIALTY_GYN;
		// Heme - Lymphoma
		if (strDescr.contains("marrow"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("iliac crest"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("spleen"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("splenic"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("lymphoma"))
			return SPECIALTY_LYMPHOMA;
		if (strDescr.contains("leukemia"))
			return SPECIALTY_LYMPHOMA;
		// MSK
		if (strDescr.contains("sarcoma"))
			return SPECIALTY_MSK;
		if (strDescr.contains("lipoma"))
			return SPECIALTY_MSK;
		if (strDescr.contains("amputation"))
			return SPECIALTY_MSK;
		if (strDescr.contains("finger"))
			return SPECIALTY_MSK;
		if (strDescr.contains("toe"))
			return SPECIALTY_MSK;
		if (strDescr.contains("digit"))
			return SPECIALTY_MSK;
		if (strDescr.contains("subcutaneous"))
			return SPECIALTY_MSK;
		if (strDescr.contains("pannus"))
			return SPECIALTY_MSK;
		if (strDescr.contains("knee"))
			return SPECIALTY_MSK;
		if (strDescr.contains("soft tissue"))
			return SPECIALTY_MSK;
		if (strDescr.contains("hip"))
			return SPECIALTY_MSK;
		if (strDescr.contains("vertebra"))
			return SPECIALTY_MSK;
		if (strDescr.contains("sacral"))
			return SPECIALTY_MSK;
		if (strDescr.contains("tendon"))
			return SPECIALTY_MSK;
		if (strDescr.contains("bone"))
			return SPECIALTY_MSK;
		if (strDescr.contains("fracture"))
			return SPECIALTY_MSK;
		if (strDescr.contains("femur"))
			return SPECIALTY_MSK;
		if (strDescr.contains("femoral"))
			return SPECIALTY_MSK;
		if (strDescr.contains("humerus"))
			return SPECIALTY_MSK;
		if (strDescr.contains("radius"))
			return SPECIALTY_MSK;
		if (strDescr.contains("wrist"))
			return SPECIALTY_MSK;
		if (strDescr.contains("hernia"))
			return SPECIALTY_MSK;
		if (strDescr.contains("disc"))
			return SPECIALTY_MSK;
		if (strDescr.contains("lumbar"))
			return SPECIALTY_MSK;
		if (strDescr.contains("synovium"))
			return SPECIALTY_MSK;
		if (strDescr.contains("synovial"))
			return SPECIALTY_MSK;
		if (strDescr.contains("hardware"))
			return SPECIALTY_MSK;
		// Neuro
		if (strDescr.contains("brain"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("frontal"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("temporal"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("parietal"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("occipital"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("cerebell"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("mening"))
			return SPECIALTY_NEURO;
		if (strDescr.contains("pituitary"))
			return SPECIALTY_NEURO;
		// Ophthalmic
		if (strDescr.contains("orbit"))
			return SPECIALTY_OPHTHALMIC;
		if (strDescr.contains("cornea"))
			return SPECIALTY_OPHTHALMIC;
		if (strDescr.contains("lens"))
			return SPECIALTY_OPHTHALMIC;
		// Thoracic
		if (strDescr.contains("lung"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("pleura"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("mediastin"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("trachea"))
			return SPECIALTY_THORACIC;
		if (strDescr.contains("bronch"))
			return SPECIALTY_THORACIC;
		// Give up
		return SPECIALTY_GENERAL;
	}

	private void readDB(LBase pj, PreparedStatement pstm) {
		ResultSet rst = pj.dbPowerJ.getResultSet(pstm);
		specGroups.clear();
		specMaster.clear();
		groupID = 0;
		try {
			while (rst.next()) {
				if (groupID != rst.getShort("SGID")) {
					groupID = rst.getShort("SGID");
					specGroup = specGroups.get(groupID);
					if (specGroup == null) {
						specGroup = new OSpecGroup();
						specGroup.subID = rst.getByte("SBID");
						specGroup.proID = rst.getByte("POID");
						specGroup.value5 = rst.getInt("SGV5");
						specGroup.hasLN = (rst.getString("SGLN").toUpperCase().equals("Y"));
						specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_1 - 1] = new OItem(
								rst.getShort("SG1B"), rst.getString("C1NB"));
						specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_2 - 1] = new OItem(
								rst.getShort("SG2B"), rst.getString("C2NB"));
						specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_3 - 1] = new OItem(
								rst.getShort("SG3B"), rst.getString("C2NB"));
						specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_4 - 1] = new OItem(
								rst.getShort("SG4B"), rst.getString("C4NB"));
						specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_1 - 1] = new OItem(
								rst.getShort("SG1M"), rst.getString("C1NM"));
						specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_2 - 1] = new OItem(
								rst.getShort("SG2M"), rst.getString("C2NM"));
						specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_3 - 1] = new OItem(
								rst.getShort("SG3M"), rst.getString("C3NM"));
						specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_4 - 1] = new OItem(
								rst.getShort("SG4M"), rst.getString("C4NM"));
						specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_1 - 1] = new OItem(
								rst.getShort("SG1R"), rst.getString("C1NR"));
						specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_2 - 1] = new OItem(
								rst.getShort("SG2R"), rst.getString("C2NR"));
						specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_3 - 1] = new OItem(
								rst.getShort("SG3R"), rst.getString("C3NR"));
						specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_4 - 1] = new OItem(
								rst.getShort("SG4R"), rst.getString("C4NR"));
						specGroups.put(groupID, specGroup);
					}
				}
				specMaster.put(rst.getShort("SMID"), groupID);
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Templates Map", e);
		} finally {
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.close(pstm);
		}
	}
}