package ca.powerj.data;
import java.io.IOException;
import com.swabunga.spell.event.XMLWordFinder;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.Tag;

public class StyleData {
	static final byte STYLE_DX_ONLY = 0;
	static final byte STYLE_DX_UP = 1;
	static final byte STYLE_DX_DOWN = 2;
	static final byte STYLE_MIXED = 3;
	private final byte CASE_IGNORE = 0;
	private final byte CASE_UPPER = 1;
	private final byte CASE_LOWER = 2;
	private final byte CASE_TITLE = 3;
	private final byte CASE_SENTENCE = 4;
//	private final byte LIST_UNORDERED_NONE = 0;
	private final byte LIST_UNORDERED_DISC = 1;
	private final byte LIST_UNORDERED_CIRCLE = 2;
	private final byte LIST_UNORDERED_SQUARE = 3;
	private final byte LIST_ORDERED_123 = 4;
	private final byte LIST_ORDERED_ABC = 5;
	private final byte LIST_ORDERED_abc = 6;
	private final byte LIST_ORDERED_III = 7;
	private final byte LIST_ORDERED_iii = 8;
	static final String TAG_LIST_ITEM_START = "<li>";
	static final String TAG_LIST_ITEM_END = "</li>";
	static final String TAG_DOC_END = "</body></html>";
	static final String TAG_DOC_START = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
		+ "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>"
		+ "<title>PowerJ</title><meta name=\"generator\" content=\"PowerJ\"/><style type=\"text/css\"> "
		+ "@page { margin: 2cm } </style></head>";
	private final String TAG_LIST_ORDERED_123 = "<ol>";
	private final String TAG_LIST_ORDERED_ABC = "<ol type=\"A\">";
	private final String TAG_LIST_ORDERED_abc = "<ol type=\"a\">";
	private final String TAG_LIST_ORDERED_III = "<ol type=\"I\">";
	private final String TAG_LIST_ORDERED_iii = "<ol type=\"i\">";
	private final String TAG_LIST_ORDERED_END = "</ol>";
	private final String TAG_LIST_UNORDERED_DISC = "<ul style=\"list-style-type:disc;\">";
	private final String TAG_LIST_UNORDERED_CIRCLE = "<ul style=\"list-style-type:circle;\">";
	private final String TAG_LIST_UNORDERED_SQUARE = "<ul style=\"list-style-type:square;\">";
	private final String TAG_LIST_UNORDERED_NONE = "<ul style=\"list-style-type:none;\">";
	private final String TAG_BODY = "<body style=\"font-family: sans-serif\">";
	private final String TAG_LIST_UNORDERED_END = "</ul>";
	private final String TAG_DIAGNOSIS_START = "<p style=\"margin-top: 0.25cm; margin-bottom: 0.25cm; line-height: 100%\">";
	private final String TAG_DIAGNOSIS_END = "</p>";
	private final String TAG_MICROSCOPIC_START = "<p style=\"margin-top: 0.25cm; margin-bottom: 0.25cm; line-height: 100%\">";
	private final String TAG_MICROSCOPIC_END = "</p>";
	private final String TAG_SPECIMEN_START = "<p style=\"margin-top: 0.25cm; margin-bottom: 0.25cm; line-height: 100%\">";
	private final String TAG_SPECIMEN_TEXT = "<specimen> (<location>), <procedure>:";
	private final String TAG_SPECIMEN_END = "</p>";
	private final String TAG_SPLICER_START = "<p style=\"margin-top: 0.5cm; margin-bottom: 0.5cm; line-height: 100%\">";
	private final String TAG_SPLICER_TEXT = "<b>MICROSCOPIC</b>";
	private final String TAG_SPLICER_END = "</p>";
	private final String TAG_TISSUE_START = "<p style=\"margin-top: 0.25cm; margin-bottom: 0.25cm; line-height: 100%\">";
	private final String TAG_TISSUE_END = ":</p>";
	private byte styleID = STYLE_DX_UP;
	private byte caseDiagnosis = CASE_UPPER;
	private byte caseLocation = CASE_LOWER;
	private byte caseMicroscopic = CASE_SENTENCE;
	private byte caseProcedure = CASE_LOWER;
	private byte caseSpecimen = CASE_TITLE;
	private byte caseTissue = CASE_TITLE;
	private String bodyFont;
	private String listDiagnosisStart;
	private String listDiagnosisEnd;
	private String listSpecimenStart;
	private String listSpecimenEnd;
	private String listTissueStart;
	private String listTissueEnd;
	private String tagDiagnosisStart;
	private String tagDiagnosisEnd;
	private String tagMicroscopicStart;
	private String tagMicroscopicEnd;
	private String tagSpecimenStart;
	private String tagSpecimenEnd;
	private String tagSplicerStart;
	private String tagSplicerEnd;
	private String tagTissueStart;
	private String tagTissueEnd;
	private String textSpecimen;
	private String textSplicer;

	public StyleData() {
		setDefaults();
	}

	String formatHtml(byte formatID, Source source) {
		StringBuilder sbString = new StringBuilder();
		for (Segment segment : source) {
			if (segment instanceof Tag) {
				Tag tag = (Tag)segment;
				sbString.append(tag.tidy());
			} else if (segment instanceof CharacterReference) {
				CharacterReference charRef = (CharacterReference) segment;
				try {
					charRef.appendCharTo(sbString);
				} catch (IOException ignore) {
				}
			} else {
				sbString.append(formatText(formatID, segment.toString()));
			}
		}
		return sbString.toString();
	}

	private String formatText(byte formatID, String str) {
		XMLWordFinder finder = new XMLWordFinder(str);
		String currentWord = "";
		String newWord = "";
		while (finder.hasNext()) {
			currentWord = finder.next().toString();
			if (currentWord.length() > 0) {
				switch (formatID) {
				case CASE_UPPER:
					newWord = currentWord.toUpperCase();
					break;
				case CASE_LOWER:
					newWord = currentWord.toLowerCase();
					break;
				case CASE_SENTENCE:
					if (finder.startsSentence()) {
						if (currentWord.length() > 1) {
							newWord = currentWord.substring(0, 1).toUpperCase()
									+ currentWord.substring(1).toLowerCase();
						} else {
							newWord = currentWord.toLowerCase();
						}
					} else {
						newWord = currentWord.toLowerCase();
					}
					break;
				case CASE_TITLE:
					if (currentWord.length() > 1) {
						newWord = currentWord.substring(0, 1).toUpperCase()
								+ currentWord.substring(1).toLowerCase();
					} else {
						newWord = currentWord.toLowerCase();
					}
					break;
				default:
					// None
					newWord = currentWord;
				}
				finder.replace(newWord);
			}
		}
		return finder.getText();
	}

	byte getStyleID() {
		return styleID;
	}

	byte getCaseDiagnosis() {
		return caseDiagnosis;
	}

	byte getCaseLocation() {
		return caseLocation;
	}

	byte getCaseMicroscopic() {
		return caseMicroscopic;
	}

	byte getCaseProcedure() {
		return caseProcedure;
	}

	byte getCaseSpecimen() {
		return caseSpecimen;
	}

	byte getCaseTissue() {
		return caseTissue;
	}

	String getBodyFont() {
		return bodyFont;
	}

	String getListDiagnosisStart() {
		return listDiagnosisStart;
	}

	String getListDiagnosisEnd() {
		return listDiagnosisEnd;
	}

	String getListSpecimenStart() {
		return listSpecimenStart;
	}

	String getListSpecimenEnd() {
		return listSpecimenEnd;
	}

	String getListTissueStart() {
		return listTissueStart;
	}

	String getListTissueEnd() {
		return listTissueEnd;
	}

	String getTagDiagnosisStart() {
		return tagDiagnosisStart;
	}

	String getTagDiagnosisEnd() {
		return tagDiagnosisEnd;
	}

	String getTagMicroscopicStart() {
		return tagMicroscopicStart;
	}

	String getTagMicroscopicEnd() {
		return tagMicroscopicEnd;
	}

	String getTagSpecimenStart() {
		return tagSpecimenStart;
	}

	String getTagSpecimenEnd() {
		return tagSpecimenEnd;
	}

	String getTagSplicerStart() {
		return tagSplicerStart;
	}

	String getTagSplicerEnd() {
		return tagSplicerEnd;
	}

	String getTagTissueStart() {
		return tagTissueStart;
	}

	String getTagTissueEnd() {
		return tagTissueEnd;
	}

	String getTextSpecimen() {
		return textSpecimen;
	}

	String getTextSplicer() {
		return textSplicer;
	}

	public void setDefaults() {
		styleID = STYLE_DX_DOWN;
		caseDiagnosis = CASE_SENTENCE;
		caseLocation = CASE_LOWER;
		caseMicroscopic = CASE_SENTENCE;
		caseProcedure = CASE_LOWER;
		caseSpecimen = CASE_TITLE;
		caseTissue = CASE_TITLE;
		listDiagnosisStart = TAG_LIST_UNORDERED_DISC;
		listDiagnosisEnd = TAG_LIST_UNORDERED_END;
		listSpecimenStart = TAG_LIST_ORDERED_123;
		listSpecimenEnd = TAG_LIST_ORDERED_END;
		listTissueStart = TAG_LIST_ORDERED_abc;
		listTissueEnd = TAG_LIST_ORDERED_END;
		bodyFont = TAG_BODY;
		tagDiagnosisStart = TAG_DIAGNOSIS_START;
		tagDiagnosisEnd = TAG_DIAGNOSIS_END;
		tagMicroscopicStart = TAG_MICROSCOPIC_START;
		tagMicroscopicEnd = TAG_MICROSCOPIC_END;
		tagSpecimenStart = TAG_SPECIMEN_START;
		tagSpecimenEnd = TAG_SPECIMEN_END;
		tagSplicerStart = TAG_SPLICER_START;
		tagSplicerEnd = TAG_SPLICER_END;
		tagTissueStart = TAG_TISSUE_START;
		tagTissueEnd = TAG_TISSUE_END;
		textSpecimen = TAG_SPECIMEN_TEXT;
		textSplicer = TAG_SPLICER_TEXT;
	}

	public void setStyle(byte styleID, byte caseDiagnosis, byte caseLocation, byte caseMicroscopic,
			byte caseProcedure, byte caseSpecimen, byte caseTissue, byte listDiagnosis, byte listSpecimen,
			byte listTissue, String bodyFont, String tagDiagnosisStart, String tagMicroscopicStart,
			String tagSpecimenStart, String tagTissueStart, String tagSplicerStart, String textSpecimen,
			String textSplicer) {
		if (styleID >= STYLE_DX_ONLY && styleID <= STYLE_MIXED) {
			this.styleID = styleID;
		}
		if (caseDiagnosis >= CASE_IGNORE && caseDiagnosis <= CASE_SENTENCE) {
			this.caseDiagnosis = caseDiagnosis;
		}
		if (caseLocation >= CASE_IGNORE && caseLocation <= CASE_SENTENCE) {
			this.caseLocation = caseLocation;
		}
		if (caseMicroscopic >= CASE_IGNORE && caseMicroscopic <= CASE_SENTENCE) {
			this.caseMicroscopic = caseMicroscopic;
		}
		if (caseProcedure >= CASE_IGNORE && caseProcedure <= CASE_SENTENCE) {
			this.caseProcedure = caseProcedure;
		}
		if (caseSpecimen >= CASE_IGNORE && caseSpecimen <= CASE_SENTENCE) {
			this.caseSpecimen = caseSpecimen;
		}
		if (caseTissue >= CASE_IGNORE && caseTissue <= CASE_SENTENCE) {
			this.caseTissue = caseTissue;
		}
		if (bodyFont != null) {
			this.bodyFont = "<body " + bodyFont + ">";
		}
		if (tagDiagnosisStart != null) {
			this.tagDiagnosisStart = "<p " + tagDiagnosisStart + ">";
		}
		if (tagMicroscopicStart != null) {
			this.tagMicroscopicStart = "<p " + tagMicroscopicStart + ">";
		}
		if (tagSpecimenStart != null) {
			this.tagSpecimenStart = "<p " + tagSpecimenStart + ">";
		}
		if (tagTissueStart != null) {
			this.tagTissueStart = "<p " + tagTissueStart + ">";
		}
		if (tagTissueStart != null) {
			this.tagTissueStart = "<p " + tagTissueStart + ">";
		}
		if (tagSplicerStart != null) {
			this.tagSplicerStart = "<p " + tagSplicerStart + ">";
		}
		if (textSplicer != null) {
			this.textSplicer = "<p " + textSplicer + ">";
		}
		switch (listDiagnosis) {
		case LIST_ORDERED_123:
			this.listDiagnosisStart = TAG_LIST_ORDERED_123;
			this.listDiagnosisEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_abc:
			this.listDiagnosisStart = TAG_LIST_ORDERED_abc;
			this.listDiagnosisEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_ABC:
			this.listDiagnosisStart = TAG_LIST_ORDERED_ABC;
			this.listDiagnosisEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_iii:
			this.listDiagnosisStart = TAG_LIST_ORDERED_iii;
			this.listDiagnosisEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_III:
			this.listDiagnosisStart = TAG_LIST_ORDERED_III;
			this.listDiagnosisEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_UNORDERED_CIRCLE:
			this.listDiagnosisStart = TAG_LIST_UNORDERED_CIRCLE;
			this.listDiagnosisEnd = TAG_LIST_UNORDERED_END;
			break;
		case LIST_UNORDERED_DISC:
			this.listDiagnosisStart = TAG_LIST_UNORDERED_DISC;
			this.listDiagnosisEnd = TAG_LIST_UNORDERED_END;
			break;
		case LIST_UNORDERED_SQUARE:
			this.listDiagnosisStart = TAG_LIST_UNORDERED_SQUARE;
			this.listDiagnosisEnd = TAG_LIST_UNORDERED_END;
			break;
		default:
			this.listDiagnosisStart = TAG_LIST_UNORDERED_NONE;
			this.listDiagnosisEnd = TAG_LIST_UNORDERED_END;
		}
		switch (listSpecimen) {
		case LIST_ORDERED_123:
			this.listSpecimenStart = TAG_LIST_ORDERED_123;
			this.listSpecimenEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_abc:
			this.listSpecimenStart = TAG_LIST_ORDERED_abc;
			this.listSpecimenEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_ABC:
			this.listSpecimenStart = TAG_LIST_ORDERED_ABC;
			this.listSpecimenEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_iii:
			this.listSpecimenStart = TAG_LIST_ORDERED_iii;
			this.listSpecimenEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_III:
			this.listSpecimenStart = TAG_LIST_ORDERED_III;
			this.listSpecimenEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_UNORDERED_CIRCLE:
			this.listSpecimenStart = TAG_LIST_UNORDERED_CIRCLE;
			this.listSpecimenEnd = TAG_LIST_UNORDERED_END;
			break;
		case LIST_UNORDERED_DISC:
			this.listSpecimenStart = TAG_LIST_UNORDERED_DISC;
			this.listSpecimenEnd = TAG_LIST_UNORDERED_END;
			break;
		case LIST_UNORDERED_SQUARE:
			this.listSpecimenStart = TAG_LIST_UNORDERED_SQUARE;
			this.listSpecimenEnd = TAG_LIST_UNORDERED_END;
			break;
		default:
			this.listSpecimenStart = TAG_LIST_UNORDERED_NONE;
			this.listSpecimenEnd = TAG_LIST_UNORDERED_END;
		}
		switch (listTissue) {
		case LIST_ORDERED_123:
			this.listTissueStart = TAG_LIST_ORDERED_123;
			this.listTissueEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_abc:
			this.listTissueStart = TAG_LIST_ORDERED_abc;
			this.listTissueEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_ABC:
			this.listTissueStart = TAG_LIST_ORDERED_ABC;
			this.listTissueEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_iii:
			this.listTissueStart = TAG_LIST_ORDERED_iii;
			this.listTissueEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_ORDERED_III:
			this.listTissueStart = TAG_LIST_ORDERED_III;
			this.listTissueEnd = TAG_LIST_ORDERED_END;
			break;
		case LIST_UNORDERED_CIRCLE:
			this.listTissueStart = TAG_LIST_UNORDERED_CIRCLE;
			this.listTissueEnd = TAG_LIST_UNORDERED_END;
			break;
		case LIST_UNORDERED_DISC:
			this.listTissueStart = TAG_LIST_UNORDERED_DISC;
			this.listTissueEnd = TAG_LIST_UNORDERED_END;
			break;
		case LIST_UNORDERED_SQUARE:
			this.listTissueStart = TAG_LIST_UNORDERED_SQUARE;
			this.listTissueEnd = TAG_LIST_UNORDERED_END;
			break;
		default:
			this.listTissueStart = TAG_LIST_UNORDERED_NONE;
			this.listTissueEnd = TAG_LIST_UNORDERED_END;
		}
	}
}