package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class NPersonnel extends NBase implements ItemListener {
	private int rowIndex = 0;
	private OPerson person = new OPerson();
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	private ArrayList<OPerson> list = new ArrayList<OPerson>();
	private ITextString txtInitials;
	private ModelPerson model;
	private ITable tbl;
	private JLabel lblName, lblCode, lblStart;
	private JCheckBox ckbActive;

	NPersonnel(AClient parent) {
		super(parent);
		setName("Persons");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_PERSONNEL);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
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
			ckb.setFont(LConstants.APP_FONT);
			ckb.addFocusListener(this);
			ckb.addItemListener(this);
			checkboxes.add(ckb);
		}
		model = new ModelPerson();
		tbl = new ITable(pj, model);
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
		JScrollPane scrollTable = IGUI.createJScrollPane(tbl);
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
		add(new IToolBar(this), BorderLayout.NORTH);
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
		lblName = IGUI.createJLabel(SwingConstants.LEFT, 0, "xxxx");
		IGUI.addComponent(lblName, 0, 0, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		lblCode = IGUI.createJLabel(SwingConstants.LEFT, 0, "xx");
		IGUI.addComponent(lblCode, 1, 0, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		txtInitials = new ITextString(2, 3);
		txtInitials.setName("Initials");
		txtInitials.addFocusListener(this);
		txtInitials.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_I, "Initials:");
		label.setLabelFor(txtInitials);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlNames);
		IGUI.addComponent(txtInitials, 1, 1, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Start Date:");
		IGUI.addComponent(label, 0, 2, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlNames);
		lblStart = IGUI.createJLabel(SwingConstants.LEFT, 0, "01/01/2010");
		IGUI.addComponent(lblStart, 1, 2, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlNames);
		ckbActive = new JCheckBox("Active");
		ckbActive.setMnemonic(KeyEvent.VK_A);
		ckbActive.setFont(LConstants.APP_FONT);
		ckbActive.addFocusListener(this);
		ckbActive.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					person.active = ckbActive.isSelected();
					altered = true;
				}
			}
		});
		IGUI.addComponent(ckbActive, 0, 3, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		byte[] items = { LConstants.ACCESS_AU_EDT, LConstants.ACCESS_AU_ERR, LConstants.ACCESS_AU_FNL,
				LConstants.ACCESS_AU_PNP };
		for (int i = 0; i < items.length; i++) {
			IGUI.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
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
		byte[] items = { LConstants.ACCESS_EX_PDF, LConstants.ACCESS_EX_XLS };
		for (int i = 0; i < items.length; i++) {
			IGUI.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
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
		byte[] items = { LConstants.ACCESS_SCHED, LConstants.ACCESS_WORKD };
		for (int i = 0; i < items.length; i++) {
			IGUI.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
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
		byte[] items = { LConstants.ACCESS_STP_IE, LConstants.ACCESS_STP_PJ, LConstants.ACCESS_STP_PP,
				LConstants.ACCESS_STP_PR, LConstants.ACCESS_STP_SC, LConstants.ACCESS_STP_TR };
		for (int i = 0; i < items.length; i++) {
			IGUI.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
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
		byte[] items = { LConstants.ACCESS_DAILY, LConstants.ACCESS_DIAGN, LConstants.ACCESS_GROSS,
				LConstants.ACCESS_ROUTE, LConstants.ACCESS_HISTO, LConstants.ACCESS_EMBED, LConstants.ACCESS_NAMES };
		for (int i = 0; i < items.length; i++) {
			IGUI.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
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
		byte[] items = { LConstants.ACCESS_DISTR, LConstants.ACCESS_FOREC, LConstants.ACCESS_SPECI,
				LConstants.ACCESS_TURNA, LConstants.ACCESS_WORKL };
		for (int i = 0; i < items.length; i++) {
			IGUI.addComponent(checkboxes.get(items[i]), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PRS_SELECT));
		try {
			while (rst.next()) {
				person = new OPerson();
				person.prsID = rst.getShort("prid");
				person.access = rst.getInt("prvl");
				person.code = rst.getString("prcd").trim();
				person.initials = rst.getString("prnm").trim();
				person.firstname = rst.getString("prfr").trim();
				person.lastname = rst.getString("prls").trim();
				person.active = (rst.getString("prac").equalsIgnoreCase("Y"));
				person.bits = pj.numbers.intToBoolean(person.access);
				person.started.setTime(rst.getDate("prdt").getTime());
				list.add(person);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!programmaticChange) {
			altered = true;
		}
	}

	@Override
	void save() {
		for (int i = 0; i < 32; i++) {
			person.bits[i] = checkboxes.get(i).isSelected();
		}
		person.access = pj.numbers.booleanToInt(person.bits);
		person.initials = person.initials.trim().toUpperCase();
		if (person.initials.length() > 3) {
			person.initials = person.initials.substring(0, 3);
		}
		pj.dbPowerJ.setInt(pjStms.get(DPowerJ.STM_PRS_UPDATE), 1, person.access);
		pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_PRS_UPDATE), 2, person.started.getTime());
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_PRS_UPDATE), 3, person.code.trim());
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_PRS_UPDATE), 4, (person.active ? "Y" : "N"));
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_PRS_UPDATE), 5, person.initials);
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_PRS_UPDATE), 6, person.lastname.trim());
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_PRS_UPDATE), 7, person.firstname.trim());
		pj.dbPowerJ.setShort(pjStms.get(DPowerJ.STM_PRS_UPDATE), 8, person.prsID);
		if (pj.dbPowerJ.execute(pjStms.get(DPowerJ.STM_PRS_UPDATE)) > 0) {
			altered = false;
		}
	}

	@Override
	void setFilter(short id, final short value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (id == IToolBar.TB_PRS && value > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).code.equals(OPersonJob.CODES[value]));
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
		lblCode.setText(person.code);
		lblName.setText(person.firstname + " " + person.lastname);
		txtInitials.setText(person.initials);
		ckbActive.setSelected(person.active);
		lblStart.setText(pj.dates.formatter(person.started, LDates.FORMAT_DATE));
		for (int i = 0; i < 32; i++) {
			checkboxes.get(i).setSelected(person.bits[i]);
		}
		programmaticChange = false;
	}

	@Override
	void trackDocument(DocumentEvent e) {
		if (!programmaticChange) {
			try {
				person.initials = e.getDocument().getText(0, e.getDocument().getLength());
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
				value = list.get(row).firstname + " " + list.get(row).lastname;
			}
			return value;
		}
	}
}