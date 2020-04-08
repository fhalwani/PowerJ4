package ca.powerj;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

class IToolBar extends JToolBar {
	static final byte TB_FAC = 1;
	static final byte TB_ORG = 2;
	static final byte TB_ORT = 3;
	static final byte TB_PRO = 4;
	static final byte TB_PRS = 5;
	static final byte TB_SPG = 6;
	static final byte TB_SPY = 7;
	static final byte TB_STA = 8;
	static final byte TB_SUB = 9;
	static final byte TB_FROM = 10;
	static final byte TB_TO = 11;
	static final byte TB_GO = 12;
	static final byte TB_SPIN = 13;
	static final byte TB_FILTER = 14;
	AClient pj;
	NBase pnlCore;

	IToolBar(NBase panel) {
		super();
		this.pnlCore = panel;
		this.pj = panel.pj;
		createPanel();
	}

	IToolBar(NBase panel, Calendar start, Calendar end, Calendar min, Calendar max, boolean rows) {
		super();
		this.pnlCore = panel;
		this.pj = panel.pj;
		createPanel(start, end, min, max, rows);
	}

	private void createPanel() {
		setAlignmentX(Component.LEFT_ALIGNMENT);
		switch (pj.pnlID) {
		case LConstants.ACTION_BACKLOG:
			addFacility();
			addSpecialty();
			addSubspecialty();
			addProcedure();
			if (!((pj.userID == -222 || pj.userID == -111) && pj.autoLogin)) {
				addStatus();
			}
			break;
		case LConstants.ACTION_DISTRIBUTE:
			addFacility();
			break;
		case LConstants.ACTION_FINALS:
			addFacility();
			addSpecialty();
			addSubspecialty();
			addProcedure();
			break;
		case LConstants.ACTION_HISTOLOGY:
			addFacility();
			addSubspecialty();
			addProcedure();
			break;
		case LConstants.ACTION_ORDERGROUP:
			addOrderTypes();
			break;
		case LConstants.ACTION_ORDERMASTER:
			addOrderGroups();
			break;
		case LConstants.ACTION_PENDING:
			addFacility();
			addSpecialty();
			addSubspecialty();
			addProcedure();
			addStatus();
			break;
		case LConstants.ACTION_PERSONNEL:
			addPerson();
			break;
		case LConstants.ACTION_ROUTING:
			addFacility();
			addSpecialty();
			addSubspecialty();
			break;
		case LConstants.ACTION_SCHEDULE:
			addFacility();
			addSpin();
			break;
		case LConstants.ACTION_SPECGROUP:
			addSpecialty();
			addSubspecialty();
			addProcedure();
			break;
		case LConstants.ACTION_SPECMASTER:
			addSpecialty();
			addSubspecialty();
			addProcedure();
			addFilter();
			break;
		default:
			// Turnaround
			addFacility();
			addSpecialty();
			addSubspecialty();
			addProcedure();
			addStatus();
			break;
		}
	}

	private void createPanel(Calendar start, Calendar end, Calendar min, Calendar max, boolean rows) {
		setAlignmentX(Component.LEFT_ALIGNMENT);
		switch (pj.pnlID) {
		case LConstants.ACTION_FORECAST:
			addFacility();
			addDates(start, end, min, max);
			if (rows)
				addRows();
			addGo();
			break;
		case LConstants.ACTION_SPECIMEN:
			addFacility();
			addDates(start, end, min, max);
			if (rows)
				addRows();
			addGo();
			break;
		case LConstants.ACTION_WORKDAYS:
			addFacility();
			addDates(start, end, min, max);
			addSpin();
			addGo();
			break;
		default:
			// Workload
			if (rows)
				addFacility();
			addDates(start, end, min, max);
			if (rows)
				addRows();
			addGo();
			break;
		}
	}

	private void addDates(Calendar start, Calendar end, Calendar min, Calendar max) {
		IComboDate cboStart = new IComboDate(start, min, max);
		cboStart.setName("Start");
		cboStart.addAncestorListener(new IFocusListener());
		cboStart.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboDate cbo = (IComboDate) e.getSource();
					pnlCore.setFilter(TB_FROM, cbo.getValue());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_F, "From:");
		label.setLabelFor(cboStart);
		add(label);
		add(cboStart);
		IComboDate cboEnd = new IComboDate(end, min, max);
		cboEnd.setName("End");
		cboEnd.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboDate cbo = (IComboDate) e.getSource();
					pnlCore.setFilter(TB_TO, cbo.getValue());
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "To:");
		label.setLabelFor(cboEnd);
		add(label);
		add(cboEnd);
	}

	private void addFacility() {
		IComboBox cbo = new IComboBox();
		cbo.setName("Facilities");
		cbo.setModel(pj.dbPowerJ.getFacilities(true));
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_FAC, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_F, "Facility:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addFilter() {
		JButton button = IGUI.createJButton(48, KeyEvent.VK_I, "Filter");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pnlCore.setFilter(TB_FILTER, TB_FILTER);
			}
		});
		add(button);
	}

	private void addGo() {
		JButton button = IGUI.createJButton(48, KeyEvent.VK_O, "Ok");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pnlCore.setFilter(TB_GO, TB_GO);
			}
		});
		add(button);
	}

	private void addOrderGroups() {
		IComboBox cbo = new IComboBox();
		cbo.setName("OrderGroups");
		cbo.setModel(pj.dbPowerJ.getOrderGroupArray(true));
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_ORG, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_G, "Groups:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addOrderTypes() {
		IComboBox cbo = new IComboBox();
		cbo.setName("OrderTypes");
		cbo.setModel(pj.dbPowerJ.getOrderTypes(true));
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_ORT, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "Types:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addPerson() {
		IComboBox cbo = new IComboBox();
		cbo.setName("Persons");
		cbo.setModel(pj.dbPowerJ.getPersonCodes(true));
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_PRS, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_P, "Personnel:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addProcedure() {
		IComboBox cbo = new IComboBox();
		cbo.setName("Procedures");
		cbo.setModel(pj.dbPowerJ.getProcedures(true));
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_PRO, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_P, "Procedures:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addRows() {
		byte[] rowsView = new byte[5];
		IComboRows cboRows = new IComboRows(rowsView);
		cboRows.setName("Rows");
		cboRows.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboRows cbo = (IComboRows) e.getSource();
					pnlCore.setFilter(cbo.getValue());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_R, "Rows:");
		label.setLabelFor(cboRows);
		add(label);
		add(cboRows);
	}

	private void addSpecialty() {
		IComboBox cbo = new IComboBox();
		cbo.setName("Specialties");
		cbo.setModel(pj.dbPowerJ.getSpecialties(true));
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_SPY, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_S, "Specialties:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addSpin() {
		JButton button = IGUI.createJButton(48, KeyEvent.VK_I, "Spin");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pnlCore.setFilter(TB_SPIN, TB_SPIN);
			}
		});
		add(button);
	}

	private void addSubspecialty() {
		IComboBox cbo = new IComboBox();
		cbo.setName("Subspecialties");
		cbo.setModel(pj.dbPowerJ.getSubspecialties(true));
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_SUB, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_B, "Subspecialties:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addStatus() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		list.add(new OItem(OCaseStatus.ID_ALL, "* All *"));
		for (byte i = OCaseStatus.ID_ACCES; i < OCaseStatus.ID_ALL; i++) {
			list.add(new OItem(i, OCaseStatus.NAMES_ALL[i]));
		}
		IComboBox cbo = new IComboBox();
		cbo.setName("Status");
		cbo.setModel(list.toArray());
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					pnlCore.setFilter(TB_STA, cb.getIndex());
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "Status:");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}
}