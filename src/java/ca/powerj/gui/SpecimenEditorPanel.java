package ca.powerj.gui;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import ca.powerj.data.ItemData;
import ca.powerj.data.SpecimenMasterData;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IComboEditor;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModelEvent;
import ca.powerj.swing.ITableModelSpecimen;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class SpecimenEditorPanel extends BasePanel {
	private ITable tableEvents;
	private ITable tableSpecs;
	private ITableModelEvent modelEvent;
	private ITableModelSpecimen modelSpec;

	SpecimenEditorPanel(AppFrame application) {
		super(application);
		setName("Specimen Editor");
		application.dbPowerJ.setStatements(LibConstants.ACTION_EDITOR);
		if (!application.isOffline()) {
			application.dbPath.setStatements(LibConstants.ACTION_EDITOR);
		}
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			modelEvent.close();
			modelSpec.close();
			if (application.dbPath != null) {
				application.dbPath.closeStms();
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
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_C, "Case No:");
		label.setLabelFor(textField);
		JPanel pnlData = new JPanel();
		pnlData.setLayout(new BoxLayout(pnlData, BoxLayout.X_AXIS));
		pnlData.setOpaque(true);
		pnlData.add(label);
		pnlData.add(textField);
		pnlData.add(Box.createHorizontalGlue());
		modelSpec = new ITableModelSpecimen();
		modelSpec.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE && (!programmaticChange)) {
					save(modelSpec.getMasterID(e.getFirstRow()), modelSpec.getSpecID(e.getFirstRow()));
				}
			}
		});
		tableSpecs = new ITable(modelSpec, application.dates, application.numbers);
		tableSpecs.addFocusListener(this);
		IComboBox cboMaster = new IComboBox();
		cboMaster.setName("SpecMaster");
		cboMaster.setItems(getSpecimenMaster());
		TableColumn column = tableSpecs.getColumnModel().getColumn(ITableModelSpecimen.SPEC_CODE);
		column.setCellEditor(new IComboEditor(cboMaster));
		JScrollPane scrollSpec = IUtilities.createJScrollPane(tableSpecs);
		scrollSpec.setMinimumSize(new Dimension(600, 200));
		modelEvent = new ITableModelEvent();
		tableEvents = new ITable(modelEvent, application.dates, application.numbers);
		tableEvents.addFocusListener(this);
		JScrollPane scrollEvents = IUtilities.createJScrollPane(tableEvents);
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
		if (!application.isOffline()) {
			if (caseNo != null) {
				caseNo = caseNo.trim().toUpperCase();
				if (caseNo.length() > 10) {
					programmaticChange = true;
					long caseID = application.dbPath.getCaseID(caseNo);
					modelEvent.setData(application.dbPath.getCaseEvents(caseID));
					modelSpec.setData(application.dbPath.getCaseSpecimens(caseID));
					programmaticChange = false;
				}
			}
		}
	}

	private Object[] getSpecimenMaster() {
		ArrayList<SpecimenMasterData> temp = application.dbPowerJ.getSpecimenMasters();
		ItemData[] specimens = new ItemData[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			specimens[i] = new ItemData(temp.get(i).getSpmID(), temp.get(i).getName());
		}
		temp.clear();
		return specimens;
	}

	private void save(short masterID, long specID) {
		if (application.dbPath.setSpecimenID(masterID, specID) > 0) {
			altered = false;
		}
	}

	@Override
	void trackDocument(DocumentEvent e) {
		if (!programmaticChange)
			altered = true;
	}
}