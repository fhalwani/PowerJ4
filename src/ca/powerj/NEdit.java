package ca.powerj;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

class NEdit extends NBase {
	private Hashtable<Byte, PreparedStatement> apStms = null;
	private ITable tblEvents;
	private ITable tblSpec;
	private ITableModelEvent modelEvent;
	private ITableModelSpecimen modelSpec;

	NEdit(AClient parent) {
		super(parent);
		setName("Specimen Editor");
		pjStms = pj.dbPowerJ.prepareStatements(LConstants.ACTION_EDITOR);
		if (!parent.offLine) {
			apStms = pj.dbAP.prepareStatements(LConstants.ACTION_EDITOR);
		}
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			modelEvent.close();
			modelSpec.close();
			if (pj.dbAP != null && apStms != null) {
				pj.dbAP.close(apStms);
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
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (altered) {
					altered = false;
					try {
						JTextField tf = (JTextField) e.getSource();
						Document doc = tf.getDocument();
						String caseNo = doc.getText(0, doc.getLength());
						getData(caseNo);
					} catch (BadLocationException ignore) {
					}
				}
			}
		});
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
				if (e.getType() == TableModelEvent.UPDATE && (!programmaticChange)) {
					save(modelSpec.getMasterID(e.getFirstRow()), modelSpec.getSpecID(e.getColumn()));
				}
			}
		});
		tblSpec = new ITable(pj, modelSpec);
		tblSpec.addFocusListener(this);
		IComboBox cboMaster = new IComboBox();
		cboMaster.setName("SpecMaster");
		cboMaster.setModel(pj.dbPowerJ.getSpecimenMaster(false, pjStms.get(DPowerJ.STM_SPM_SELECT)));
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
		if (caseNo != null) {
			caseNo = caseNo.trim().toUpperCase();
			if (caseNo.length() > 10) {
				programmaticChange = true;
				pj.dbAP.setString(apStms.get(DPowerpath.STM_CASE_NUMBER), 1, caseNo);
				long caseID = pj.dbAP.getLong(apStms.get(DPowerpath.STM_CASE_NUMBER));
				modelEvent.getData(caseID, apStms.get(DPowerpath.STM_CASE_EVENTS));
				modelSpec.getData(caseID, apStms.get(DPowerpath.STM_CASE_SPCMNS));
				programmaticChange = false;
			}
		}
	}

	private void save(short masterID, long specID) {
		pj.dbAP.setShort(apStms.get(DPowerpath.STM_SPEC_UPDATE), 1, masterID);
		pj.dbAP.setLong(apStms.get(DPowerpath.STM_SPEC_UPDATE), 2, specID);
		if (pj.dbAP.execute(apStms.get(DPowerpath.STM_SPEC_UPDATE)) > 0) {
			altered = false;
		}
	}

	@Override
	void trackDocument(DocumentEvent e) {
		if (!programmaticChange)
			altered = true;
	}
}