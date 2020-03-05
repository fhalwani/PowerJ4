package ca.powerj;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

class NEdit extends NBase {
	private ITable tblEvents;
	private ITable tblSpec;
	private ITableModelEvent modelEvent;
	private ITableModelSpecimen modelSpec;

	NEdit(AClient parent) {
		super(parent);
		setName("Specimen Editor");
		pj.dbPowerJ.prepareEditor();
		if (!parent.offLine) {
			pj.dbAP.prepareEditor();
		}
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			modelEvent.close();
			modelSpec.close();
			if (pj.dbAP != null) {
				pj.dbAP.close();
			}
		}
		return !altered;
	}

	private void createPanel() {
		ITextString textField = new ITextString(3, 15);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		textField.addAncestorListener(new IFocusListener());
		textField.setName("CaseNo");
		textField.setMaximumSize(textField.getPreferredSize());
		textField.addFocusListener(this);
		textField.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_C, "Case No:");
		label.setLabelFor(textField);
		JPanel pnlData = new JPanel();
		pnlData.setLayout(new BoxLayout(pnlData, BoxLayout.X_AXIS));
		pnlData.setOpaque(true);
		pnlData.add(label);
		pnlData.add(textField);
		pnlData.add(Box.createHorizontalGlue());
		modelSpec = new ITableModelSpecimen(pj);
		modelSpec.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE) {
					save(modelSpec.getMasterID(e.getFirstRow()), modelSpec.getSpecID(e.getColumn()));
				}
			}
		});
		tblSpec = new ITable(pj, modelSpec);
		tblSpec.addFocusListener(this);
		IComboBox cboMaster = new IComboBox();
		cboMaster.setName("SpecMaster");
		cboMaster.setModel(pj.dbPowerJ.getSpecimenMaster(false));
		TableColumn column = tblSpec.getColumnModel().getColumn(ITableModelSpecimen.SPEC_CODE);
		column.setCellEditor(new DefaultCellEditor(cboMaster));
		JScrollPane scrollSpec = IGUI.createJScrollPane(tblSpec);
		scrollSpec.setMinimumSize(new Dimension(600, 200));
		modelEvent = new ITableModelEvent(pj);
		tblEvents = new ITable(pj, modelEvent);
		tblEvents.addFocusListener(this);
		JScrollPane scrollEvents = IGUI.createJScrollPane(tblEvents);
		scrollEvents.setMinimumSize(new Dimension(600, 200));
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		pnlSplit.setTopComponent(scrollSpec);
		pnlSplit.setBottomComponent(scrollEvents);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setMinimumSize(new Dimension(600, 500));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		add(pnlData);
		add(pnlSplit);
	}

	private void getData(String caseNo) {
		pj.dbAP.setString(DPowerpath.STM_CASE_NUMBER, 1, caseNo);
		long caseID = pj.dbAP.getLong(DPowerpath.STM_CASE_NUMBER);
		modelEvent.getData(caseID);
		modelSpec.getData(caseID);
	}

	private void save(short masterID, long specID) {
		pj.dbAP.setShort(DPowerpath.STM_SPEC_UPDATE, 1, masterID);
		pj.dbAP.setLong(DPowerpath.STM_SPEC_UPDATE, 2, specID);
		if (pj.dbAP.execute(DPowerpath.STM_SPEC_UPDATE) > 0) {
			altered = false;
		}
	}

	@Override
	void trackDocument(DocumentEvent e) {
		if (!programmaticChange) {
			if (!pj.offLine) {
				try {
					Document doc = e.getDocument();
					String caseNo = doc.getText(0, doc.getLength());
					if (caseNo != null) {
						caseNo = caseNo.trim().toUpperCase();
						if (caseNo.length() == 12) {
							altered = true;
							getData(caseNo);
						}
					}
				} catch (BadLocationException ignore) {
				}
			}
		}
	}
}