package ca.powerj.data;
import javax.swing.tree.DefaultMutableTreeNode;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;

public class ReportData {
	private boolean isValid = false;
	private StyleData style = new StyleData();
	private DefaultMutableTreeNode rootNode = null;

	public ReportData() {
		ReportCaseData reportNode = new ReportCaseData();
		rootNode = new DefaultMutableTreeNode(reportNode);
	}

	public int addDiagnosis(byte specimenNo, byte tissueNo, byte diagnosisNo,
			byte diseaseID, int diagnosisID, String diagnosis, String microscopic, String name) {
		int returnValue = -1;
		try {
			DefaultMutableTreeNode parentNode = getTissue(specimenNo, tissueNo);
			if (parentNode != null) {
				if (parentNode.getChildCount() < Byte.MAX_VALUE) {
					ReportDiagnosisData reportNode = new ReportDiagnosisData(tissueNo);
					reportNode.setDiagnosis(diagnosisNo, diseaseID, diagnosisID, diagnosis, microscopic, name);
					DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(reportNode);
					parentNode.add(childNode);
					returnValue = parentNode.getChildCount() -1;
				}
			}
		} catch (Exception e) {
		}
		validate();
		return returnValue;
	}

	public int addSpecimen(boolean composite, byte specialtyID, byte subspecialtyID,
			byte organID, byte procedureID, byte orderID, short tissueID,
			long specimenID, String specimen, String location, String procedure,
			String name) {
		int returnValue = -1;
		try {
			if (rootNode.getChildCount() < Byte.MAX_VALUE) {
				ReportSpecimenData reportNode = new ReportSpecimenData();
				reportNode.setSpecimen(specialtyID, subspecialtyID,
						organID, procedureID, orderID, 
						specimenID, specimen, location, procedure, name);
				reportNode.setComposite(composite);
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(reportNode);
				rootNode.add(childNode);
				returnValue = rootNode.getChildCount() -1;
			}
		} catch (Exception e) {
		}
		validate();
		return returnValue;
	}

	public int addTissue(byte specimenNo, byte tissueNo, short umlsID, String tissue, String name) {
		int returnValue = -1;
		try {
			DefaultMutableTreeNode parentNode = getSpecimen(specimenNo);
			if (parentNode != null) {
				if (parentNode.getChildCount() < Byte.MAX_VALUE) {
					ReportTissueData reportNode = new ReportTissueData(specimenNo);
					reportNode.setTissue(tissueNo, umlsID, tissue, name);
					DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(reportNode);
					parentNode.add(childNode);
					returnValue = parentNode.getChildCount() -1;
				}
			}
		} catch (Exception e) {
		}
		validate();
		return returnValue;
	}

	public void deleteDiagnosis(byte specimenNo, byte tissueNo, byte diagnosisNo) {
		try {
			DefaultMutableTreeNode parentNode = getTissue(specimenNo, tissueNo);
			if (parentNode != null) {
				parentNode.remove(diagnosisNo);
				validate();
			}
		} catch (Exception e) {
		}
	}

	public void deleteSpecimen(byte specimenNo) {
		try {
			rootNode.remove(specimenNo);
			validate();
		} catch (Exception e) {
		}
	}

	public void deleteTissue(byte specimenNo, byte tissueNo) {
		try {
			DefaultMutableTreeNode parentNode = getSpecimen(specimenNo);
			if (parentNode != null) {
				parentNode.remove(tissueNo);
				validate();
			}
		} catch (Exception e) {
		}
	}

	private String formatDiagnosis(byte specimenNo, byte tissueNo, byte diagnosisNo) {
		ReportDiagnosisData reportNode = getDiagnosisNode(specimenNo, tissueNo, diagnosisNo);
		if (reportNode != null) {
			return formatHtml(style.getCaseDiagnosis(), reportNode.getDiagnosis());
		}
		return "";
	}

	private String formatMicroscopic(byte specimenNo, byte tissueNo, byte diagnosisNo) {
		ReportDiagnosisData reportNode = getDiagnosisNode(specimenNo, tissueNo, diagnosisNo);
		if (reportNode != null) {
			return formatHtml(style.getCaseMicroscopic(), reportNode.getMicroscopic());
		}
		return "";
	}

	private String formatSpecimen(byte specimenNo) {
		ReportSpecimenData reportNode = getSpecimenNode(specimenNo);
		if (reportNode != null) {
			String formatted1 = style.getTextSpecimen().replace("<specimen>",
					formatHtml(style.getCaseSpecimen(), reportNode.getSpecimen()));
			String formatted2 = formatted1.replace("<location>",
					formatHtml(style.getCaseLocation(), reportNode.getLocation()));
			return formatted2.replace("<procedure>",
					formatHtml(style.getCaseProcedure(), reportNode.getProcedure()));
		}
		return "";
	}

	private String formatTissue(byte specimenNo, byte tissueNo) {
		ReportTissueData reportNode = getTissueNode(specimenNo, tissueNo);
		if (reportNode != null) {
			return formatHtml(style.getCaseTissue(), reportNode.getTissue());
		}
		return "";
	}

	String formatHtml(byte formatID, String string) {
		String formatted = "";
		if (string != null) {
			if (string.length() > 0) {
				Source source = getSource(string);
				formatted = style.formatHtml(formatID, source);
			}
		}
		return formatted;
	}

	public long getCaseID() {
		ReportCaseData caseNode = (ReportCaseData) rootNode.getUserObject();
		return caseNode.getCaseID();
	}

	public String getCaseNo() {
		ReportCaseData caseNode = (ReportCaseData) rootNode.getUserObject();
		return caseNode.getCaseNo();
	}

	private DefaultMutableTreeNode getDiagnosis(byte specimenNo, byte tissueNo, byte diagnosisNo) {
		try {
			DefaultMutableTreeNode parentNode = getTissue(specimenNo, tissueNo);
			if (parentNode != null) {
				return (DefaultMutableTreeNode) parentNode.getChildAt(diagnosisNo);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public ReportDiagnosisData getDiagnosisNode(byte specimenNo, byte tissueNo, byte diagnosisNo) {
		DefaultMutableTreeNode parentNode = getDiagnosis(specimenNo, tissueNo, diagnosisNo);
		if (parentNode != null) {
			return (ReportDiagnosisData) parentNode.getUserObject();
		}
		return null;
	}

	private byte getDiagnosisCount(byte specimenNo, byte tissueNo) {
		DefaultMutableTreeNode parentNode = getTissue(specimenNo, tissueNo);
		if (parentNode != null) {
			return (byte) parentNode.getChildCount();
		}
		return 0;
	}

	public String getHtml() {
		String htmlText = "";
		if (style.getStyleID() == StyleData.STYLE_MIXED) {
			htmlText = parseMixed();
		} else {
			htmlText = parseDiagMicro();
		}
		return new SourceFormatter(getSource(htmlText)).toString();
	}

	public DefaultMutableTreeNode getRoot() {
		return rootNode;
	}

	private Source getSource(String htmlText) {
		Source source = new Source(htmlText);
		source.fullSequentialParse();
		return source;
	}

	private DefaultMutableTreeNode getSpecimen(byte specimenNo) {
		try {
			return (DefaultMutableTreeNode) rootNode.getChildAt(specimenNo);
		} catch (Exception e) {
		}
		return null;
	}

	public ReportSpecimenData getSpecimenNode(byte specimenNo) {
		DefaultMutableTreeNode parentNode = getSpecimen(specimenNo);
		if (parentNode != null) {
			return (ReportSpecimenData) parentNode.getUserObject();
		}
		return null;
	}

	public StyleData getStyle() {
		return style;
	}

	private DefaultMutableTreeNode getTissue(byte specimenNo, byte tissueNo) {
		try {
			DefaultMutableTreeNode parentNode = getSpecimen(specimenNo);
			if (parentNode != null) {
				return (DefaultMutableTreeNode) parentNode.getChildAt(tissueNo);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public ReportTissueData getTissueNode(byte specimenNo, byte tissueNo) {
		DefaultMutableTreeNode parentNode = getTissue(specimenNo, tissueNo);
		if (parentNode != null) {
			return (ReportTissueData) parentNode.getUserObject();
		}
		return null;
	}

	public byte getTissueCount(byte specimenNo) {
		DefaultMutableTreeNode parentNode = getSpecimen(specimenNo);
		if (parentNode != null) {
			return (byte) parentNode.getChildCount();
		}
		return 0;
	}

	public boolean isValid() {
		return isValid;
	}

	public void newReport(long caseID, String caseNo) {
		removeAllChildren(rootNode);
		ReportCaseData reportCaseData = (ReportCaseData) rootNode.getUserObject();
		reportCaseData.setCase(caseID, caseNo);
	}

	private void removeAllChildren(DefaultMutableTreeNode reportNode) {
		for (int i = reportNode.getChildCount() -1; i >= 0; i--) {
			if (reportNode.getChildAt(i).getChildCount() > 0) {
				removeAllChildren((DefaultMutableTreeNode) reportNode.getChildAt(i));
			}
			reportNode.remove(i);
		}
	}

	private String parseDiagMicro() {
		byte counter = 0;
		byte noTissues = 0;
		byte noDiagnosis = 0;
		byte noSpecimens = (byte) rootNode.getChildCount();
		StringBuilder sbDiagnosis = new StringBuilder();
		StringBuilder sbMicroscopic = new StringBuilder();
		StringBuilder sbString = new StringBuilder();
		String results = "";
		if (noSpecimens > 1) {
			sbDiagnosis.append(style.getListSpecimenStart());
		}
		for (byte i = 0; i < noSpecimens; i++) {
			if (noSpecimens > 1) {
				sbDiagnosis.append(StyleData.TAG_LIST_ITEM_START);
			}
			sbDiagnosis.append(style.getTagSpecimenStart());
			sbDiagnosis.append(formatSpecimen(i));
			sbDiagnosis.append(style.getTagSpecimenEnd());
			if (getSpecimenNode(i).isComposite()) {
				sbDiagnosis.append(style.getListTissueStart());
				noTissues = getTissueCount(i);
				for (byte k = 0; k < noTissues; k++) {
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_START);
					sbDiagnosis.append(style.getTagTissueStart());
					sbDiagnosis.append(formatTissue(i, k));
					sbDiagnosis.append(style.getTagTissueEnd());
					sbDiagnosis.append(style.getListDiagnosisStart());
					noDiagnosis = getDiagnosisCount(i, k);
					for (byte j = 0; j < noDiagnosis; j++) {
						sbDiagnosis.append(StyleData.TAG_LIST_ITEM_START);
						sbDiagnosis.append(style.getTagDiagnosisStart());
						sbDiagnosis.append(formatDiagnosis(i, k, j));
						sbDiagnosis.append(style.getTagDiagnosisEnd());
						sbDiagnosis.append(StyleData.TAG_LIST_ITEM_END);
						if (style.getStyleID() != StyleData.STYLE_DX_ONLY) {
							results = formatMicroscopic(i, k, i);
							if (results.length() > 0) {
								sbString.append(style.getTagMicroscopicStart());
								sbString.append(results);
								sbString.append(style.getTagMicroscopicEnd());
								
							}
						}
					}
					sbDiagnosis.append(style.getListDiagnosisEnd());
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_END);
				}
				sbDiagnosis.append(style.getListTissueEnd());
			} else {
				byte k = 0;
				sbDiagnosis.append(style.getListDiagnosisStart());
				noDiagnosis = getDiagnosisCount(i, k);
				for (byte j = 0; j < noDiagnosis; j++) {
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_START);
					sbDiagnosis.append(style.getTagDiagnosisStart());
					sbDiagnosis.append(formatDiagnosis(i, k, j));
					sbDiagnosis.append(style.getTagDiagnosisEnd());
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_END);
					if (style.getStyleID() != StyleData.STYLE_DX_ONLY) {
						results = formatMicroscopic(i, k, j);
						if (results.length() > 0) {
							sbString.append(style.getTagMicroscopicStart());
							sbString.append(results);
							sbString.append(style.getTagMicroscopicEnd());
						}
					}
				}
				sbDiagnosis.append(style.getListDiagnosisEnd());
			}
			if (noSpecimens > 1) {
				sbDiagnosis.append(StyleData.TAG_LIST_ITEM_END);
			}
			if (style.getStyleID() != StyleData.STYLE_DX_ONLY) {
				if (noSpecimens > 1) {
					if (counter == 0) {
						sbMicroscopic.append(style.getListSpecimenStart());
					}
					sbMicroscopic.append(StyleData.TAG_LIST_ITEM_START);
				}
				sbMicroscopic.append(sbString);
				counter++;
				if (noSpecimens > 1) {
					sbMicroscopic.append(StyleData.TAG_LIST_ITEM_END);
				}
				sbString.delete(0, sbString.length());
			}
		}
		if (noSpecimens > 1) {
			sbDiagnosis.append(style.getListSpecimenEnd());
			if (counter > 0) {
				sbMicroscopic.append(style.getListSpecimenEnd());
			}
		}
		sbString.append(StyleData.TAG_DOC_START);
		sbString.append(style.getBodyFont());
		switch (style.getStyleID()) {
		case StyleData.STYLE_DX_UP:
			sbString.append(sbDiagnosis);
			if (counter > 0) {
				sbString.append(style.getTagSplicerStart());
				sbString.append(style.getTextSplicer());
				sbString.append(style.getTagSplicerEnd());
				sbString.append(sbMicroscopic);
			}
			break;
		case StyleData.STYLE_DX_DOWN:
			if (counter > 0) {
				sbString.append(sbMicroscopic);
				sbString.append(style.getTagSplicerStart());
				sbString.append(style.getTextSplicer());
				sbString.append(style.getTagSplicerEnd());
			}
			sbString.append(sbDiagnosis);
			break;
		default:
			sbString.append(sbDiagnosis);
		}
		sbString.append(StyleData.TAG_DOC_END);
		return sbString.toString();
	}

	private String parseMixed() {
		byte counter = 0;
		byte noTissues = 0;
		byte noDiagnosis = 0;
		byte noSpecimens = (byte) rootNode.getChildCount();
		StringBuilder sbDiagnosis = new StringBuilder();
		StringBuilder sbMicroscopic = new StringBuilder();
		StringBuilder sbString = new StringBuilder();
		String results = "";
		sbString.append(StyleData.TAG_DOC_START);
		sbString.append(style.getBodyFont());
		if (noSpecimens > 1) {
			sbString.append(style.getListSpecimenStart());
		}
		for (byte i = 0; i < noSpecimens; i++) {
			if (noSpecimens > 1) {
				sbString.append(StyleData.TAG_LIST_ITEM_START);
			}
			sbDiagnosis.append(style.getTagSpecimenStart());
			sbDiagnosis.append(formatSpecimen(i));
			sbDiagnosis.append(style.getTagSpecimenEnd());
			if (getSpecimenNode(i).isComposite()) {
				sbDiagnosis.append(style.getListTissueStart());
				noTissues = getTissueCount(i);
				for (byte k = 0; k < noTissues; k++) {
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_START);
					sbDiagnosis.append(style.getTagTissueStart());
					sbDiagnosis.append(formatTissue(i, k));
					sbDiagnosis.append(style.getTagTissueEnd());
					sbDiagnosis.append(style.getListDiagnosisStart());
					noDiagnosis = getDiagnosisCount(i, k);
					for (byte j = 0; j < noDiagnosis; j++) {
						sbDiagnosis.append(StyleData.TAG_LIST_ITEM_START);
						sbDiagnosis.append(style.getTagDiagnosisStart());
						sbDiagnosis.append(formatDiagnosis(i, k, j));
						sbDiagnosis.append(style.getTagDiagnosisEnd());
						sbDiagnosis.append(StyleData.TAG_LIST_ITEM_END);
						if (style.getStyleID() != StyleData.STYLE_DX_ONLY) {
							results = formatMicroscopic(i, k, i);
							if (results.length() > 0) {
								sbMicroscopic.append(style.getTagMicroscopicStart());
								sbMicroscopic.append(results);
								sbMicroscopic.append(style.getTagMicroscopicEnd());
								counter++;
							}
						}
					}
					sbDiagnosis.append(style.getListDiagnosisEnd());
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_END);
				}
				sbDiagnosis.append(style.getListTissueEnd());
			} else {
				byte k = 0;
				sbDiagnosis.append(style.getListDiagnosisStart());
				noDiagnosis = getDiagnosisCount(i, k);
				for (byte j = 0; j < noDiagnosis; j++) {
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_START);
					sbDiagnosis.append(style.getTagDiagnosisStart());
					sbDiagnosis.append(formatDiagnosis(i, k, j));
					sbDiagnosis.append(style.getTagDiagnosisEnd());
					sbDiagnosis.append(StyleData.TAG_LIST_ITEM_END);
					if (style.getStyleID() != StyleData.STYLE_DX_ONLY) {
						results = formatMicroscopic(i, k, i);
						if (results.length() > 0) {
							sbMicroscopic.append(style.getTagMicroscopicStart());
							sbMicroscopic.append(results);
							sbMicroscopic.append(style.getTagMicroscopicEnd());
							counter++;
						}
					}
				}
				sbDiagnosis.append(style.getListDiagnosisEnd());
			}
			if (counter > 0) {
				sbString.append(sbMicroscopic);
				sbMicroscopic.delete(0, sbMicroscopic.length());
				counter = 0;
			}
			sbString.append(sbDiagnosis);
			sbDiagnosis.delete(0, sbDiagnosis.length());
			if (noSpecimens > 1) {
				sbString.append(StyleData.TAG_LIST_ITEM_END);
			}
		}
		if (noSpecimens > 1) {
			sbString.append(StyleData.TAG_LIST_ITEM_END);
			sbString.append(style.getListSpecimenEnd());
		}
		sbString.append(StyleData.TAG_DOC_END);
		return sbString.toString();
	}

	public void setDiagnosis(byte specimenNo, byte tissueNo, byte diagnosisNo,
			byte diseaseID, int diagnosisID, String diagnosis, String microscopic, String name) {
		ReportDiagnosisData reportNode = getDiagnosisNode(specimenNo, tissueNo, diagnosisNo);
		if (reportNode != null) {
			reportNode.setDiagnosis(diagnosisNo, diseaseID, diagnosisID, diagnosis, microscopic, name);
			validate();
		}
	}

	public void setSpecimen(byte specimenNo, boolean composite, byte specialtyID,
			byte subspecialtyID, byte organID, byte procedureID, byte orderID,
			long specimenID, String specimen, String location, String procedure,
			String name) {
		ReportSpecimenData reportNode = getSpecimenNode(specimenNo);
		if (reportNode != null) {
			reportNode.setSpecimen(specialtyID, subspecialtyID,
					organID, procedureID, orderID, specimenID,
					specimen, location, procedure, name);
			validate();
		}
	}

	public void setTissue(byte specimenNo, byte tissueNo, short tissueID, String tissue, String name) {
		ReportTissueData reportNode = getTissueNode(specimenNo, tissueNo);
		if (reportNode != null) {
			reportNode.setTissue(tissueNo, tissueID, tissue, name);
			validate();
		}
	}

	private void validate() {
		isValid = false;
		if (rootNode.getChildCount() > 0) {
			// specimen count > 0
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				if (rootNode.getChildAt(i).getChildCount() > 0) {
					// tissue count > 0
					isValid = true;
					for (int j = 0; j < rootNode.getChildAt(i).getChildCount(); i++) {
						if (rootNode.getChildAt(i).getChildAt(j).getChildCount() == 0) {
							// diagnosis count is 0
							isValid = false;
							break;
						}
					}
				} else {
					// tissue count is 0
					isValid = false;
					break;
				}
			}
		}
	}
}