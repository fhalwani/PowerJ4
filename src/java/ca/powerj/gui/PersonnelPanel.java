package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import ca.powerj.data.PersonData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class PersonnelPanel extends BasePanel {
	private int rowIndex = 0;
	private PersonData person = new PersonData();
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	private ArrayList<PersonData> list = new ArrayList<PersonData>();
	private ITextString txtInitials;
	private ModelPerson model;
	private ITable tbl;
	private JLabel lblName, lblCode, lblStart;
	private JCheckBox ckbActive;

	PersonnelPanel(AppFrame application) {
		super(application);
		setName("Persons");
		application.dbPowerJ.setStatements(LibConstants.ACTION_PERSONNEL);
		list = application.dbPowerJ.getPersons();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		final String[] ckbNames = { "Daily", "Diagnosis", "Distribution", "Histo Stations", "Forecast", "Grossing",
				"Histo Backlog", "View Names", "Routing", "Schedule", "Specimens", "Turnaround", "Workdays", "Workload",
				"", "", "", "", "", "", "PDF", "XLS", "Case Editor", "Errors", "Finals", "Pending", "Import/Export DB",
				"PowerJ", "Powerpath", "Personnel", "Schedule", "Turnaround" };
		for (int i = 0; i < 32; i++) {
			JCheckBox ckb = new JCheckBox();
			ckb.setName(ckbNames[i]);
			ckb.setText(ckbNames[i]);
			ckb.setFont(LibConstants.APP_FONT);
			ckb.addFocusListener(this);
			ckb.addItemListener(new ItemListener () {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!programmaticChange) {
						altered = true;
					}
				}
				
			});
			checkboxes.add(ckb);
		}
		model = new ModelPerson();
		tbl = new ITable(model, application.dates, application.numbers);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				if (index > -1) {
					// else, Selection got filtered away.
					setRow(tbl.convertRowIndexToModel(index));
				}
			}
		});
		tbl.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IUtilities.createJScrollPane(tbl);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlData.setLayout(new BoxLayout(pnlData, BoxLayout.Y_AXIS));
		pnlData.add(createPanelNames());
		pnlData.add(createPanelWorkflow());
		pnlData.add(createPanelWorkload());
		pnlData.add(createPanelSchedule());
		pnlData.add(createPanelExport());
		pnlData.add(createPanelAudit());
		pnlData.add(createPanelSetup());
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		setLayout(new BorderLayout());
		add(new IToolBar(application), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private JPanel createPanelNames() {
		JPanel pnlNames = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Personnel");
		title.setTitleJustification(TitledBorder.CENTER);
		pnlNames.setBorder(title);
		pnlNames.setName("Personnel");
		pnlNames.setLayout(new GridBagLayout());
		pnlNames.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		lblName = IUtilities.createJLabel(SwingConstants.LEFT, 0, "xxxx");
		IUtilities.addComponent(lblName, 0, 0, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		lblCode = IUtilities.createJLabel(SwingConstants.LEFT, 0, "xx");
		IUtilities.addComponent(lblCode, 1, 0, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		txtInitials = new ITextString(2, 3);
		txtInitials.setName("Initials");
		txtInitials.addFocusListener(this);
		txtInitials.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_I, "Initials:");
		label.setLabelFor(txtInitials);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlNames);
		IUtilities.addComponent(txtInitials, 1, 1, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Start Date:");
		IUtilities.addComponent(label, 0, 2, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlNames);
		lblStart = IUtilities.createJLabel(SwingConstants.LEFT, 0, "01/01/2010");
		IUtilities.addComponent(lblStart, 1, 2, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		ckbActive = new JCheckBox("Active");
		ckbActive.setMnemonic(KeyEvent.VK_A);
		ckbActive.setFont(LibConstants.APP_FONT);
		ckbActive.addFocusListener(this);
		ckbActive.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					person.setActive(ckbActive.isSelected());
					altered = true;
				}
			}
		});
		IUtilities.addComponent(ckbActive, 0, 3, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		return pnlNames;
	}

	private JPanel createPanelAudit() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Audit");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Audit");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		byte[] items = { LibConstants.ACCESS_AU_EDT, LibConstants.ACCESS_AU_ERR, LibConstants.ACCESS_AU_FNL,
				LibConstants.ACCESS_AU_PNP };
		for (int i = 0; i < items.length; i++) {
			IUtilities.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private JPanel createPanelExport() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Export");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Export");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		byte[] items = { LibConstants.ACCESS_EX_PDF, LibConstants.ACCESS_EX_XLS };
		for (int i = 0; i < items.length; i++) {
			IUtilities.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private JPanel createPanelSchedule() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Schedule");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Schedule");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		byte[] items = { LibConstants.ACCESS_SCHED, LibConstants.ACCESS_WORKD };
		for (int i = 0; i < items.length; i++) {
			IUtilities.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private JPanel createPanelSetup() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Setup");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Setup");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		byte[] items = { LibConstants.ACCESS_STP_IE, LibConstants.ACCESS_STP_PJ, LibConstants.ACCESS_STP_PP,
				LibConstants.ACCESS_STP_PR, LibConstants.ACCESS_STP_SC, LibConstants.ACCESS_STP_TR };
		for (int i = 0; i < items.length; i++) {
			IUtilities.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private JPanel createPanelWorkflow() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Workflow");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Workflow");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		byte[] items = { LibConstants.ACCESS_DAILY, LibConstants.ACCESS_DIAGN, LibConstants.ACCESS_GROSS,
				LibConstants.ACCESS_ROUTE, LibConstants.ACCESS_HISTO, LibConstants.ACCESS_EMBED, LibConstants.ACCESS_NAMES };
		for (int i = 0; i < items.length; i++) {
			IUtilities.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private JPanel createPanelWorkload() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Workload");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Workload");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		byte[] items = { LibConstants.ACCESS_DISTR, LibConstants.ACCESS_FOREC, LibConstants.ACCESS_SPECI,
				LibConstants.ACCESS_TURNA, LibConstants.ACCESS_WORKL };
		for (int i = 0; i < items.length; i++) {
			IUtilities.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	@Override
	void save() {
		for (int i = 0; i < 32; i++) {
			person.setBit(i, checkboxes.get(i).isSelected());
		}
		person.setAccess(application.numbers.booleanToInt(person.getBits()));
		if (application.dbPowerJ.setPerson(false, person) > 0) {
			altered = false;
		}
	}

	@Override
	public void setFilter(short id, final int value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (id == IToolBar.TB_PRS && value > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).getCode().equals(LibConstants.PERSON_STRINGS[value]));
				}
			};
		}
		TableRowSorter<ModelPerson> sorter = (TableRowSorter<ModelPerson>) tbl.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		person = list.get(rowIndex);
		lblCode.setText(person.getCode());
		lblName.setText(person.getFirstname() + " " + person.getLastname());
		txtInitials.setText(person.getInitials());
		ckbActive.setSelected(person.isActive());
		lblStart.setText(application.dates.formatter(person.getStarted(), LibDates.FORMAT_DATE));
		for (int i = 0; i < 32; i++) {
			checkboxes.get(i).setSelected(person.getBit(i));
		}
		programmaticChange = false;
	}

	@Override
	void trackDocument(DocumentEvent e) {
		if (!programmaticChange) {
			try {
				person.setInitials(e.getDocument().getText(0, e.getDocument().getLength()));
				altered = true;
			} catch (BadLocationException ignore) {
			}
		}
	}

	private class ModelPerson extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (list.size() > 0 && row < list.size()) {
				value = list.get(row).getFirstname() + " " + list.get(row).getLastname();
			}
			return value;
		}
	}
}