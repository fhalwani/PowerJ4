package ca.powerj.swing;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import ca.powerj.gui.AppFrame;
import ca.powerj.data.FacilityData;
import ca.powerj.data.ItemData;
import ca.powerj.data.OrderGroupData;
import ca.powerj.data.ProcedureList;
import ca.powerj.data.SpecialtyList;
import ca.powerj.data.SubspecialtyList;
import ca.powerj.lib.LibConstants;

public class IToolBar extends JToolBar {
	public static final byte TB_FAC = 1;
	public static final byte TB_ORG = 2;
	public static final byte TB_ORT = 3;
	public static final byte TB_PRO = 4;
	public static final byte TB_PRS = 5;
	public static final byte TB_SPG = 6;
	public static final byte TB_SPY = 7;
	public static final byte TB_STA = 8;
	public static final byte TB_SUB = 9;
	public static final byte TB_FROM = 10;
	public static final byte TB_TO = 11;
	public static final byte TB_GO = 12;
	public static final byte TB_SPIN = 13;
	public static final byte TB_FILTER = 14;
	public static final byte TB_INTERVAL = 15;
	public static final byte TB_DATA = 16;
	public static final byte TB_DAILY = 1;
	public static final byte TB_WEEKLY = 2;
	public static final byte TB_CASES = 1;
	public static final byte TB_SPECS = 2;
	public static final byte TB_BLOCKS = 3;
	public static final byte TB_SLIDES = 4;
	public static final byte TB_VALUE1 = 5;
	public static final byte TB_VALUE2 = 6;
	public static final byte TB_VALUE3 = 7;
	public static final byte TB_VALUE4 = 8;
	public static final byte TB_VALUE5 = 9;
	private AppFrame application;

	public IToolBar(AppFrame application) {
		super();
		this.application = application;
		createPanel();
	}

	public IToolBar(AppFrame application, Calendar start, Calendar end, Calendar min, Calendar max, byte[] values) {
		super();
		this.application = application;
		createPanel(start, end, min, max, values);
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
					application.panel.setFilter(TB_FROM, cbo.getValue());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_F, "From: ");
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
					application.panel.setFilter(TB_TO, cbo.getValue());
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "To: ");
		label.setLabelFor(cboEnd);
		add(label);
		add(cboEnd);
	}

	private void addDayWeek() {
		ActionListener btnListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Weekly")) {
					application.panel.setFilter(TB_INTERVAL, TB_WEEKLY);
				} else {
					application.panel.setFilter(TB_INTERVAL, TB_DAILY);
				}
			}
		};
		JRadioButton btnDaily = new JRadioButton("Daily");
		btnDaily.setMnemonic(KeyEvent.VK_D);
		btnDaily.setSelected(true);
		btnDaily.setActionCommand("Daily");
		btnDaily.addActionListener(btnListener);
		JRadioButton btnWeekly = new JRadioButton("Weekly");
		btnWeekly.setMnemonic(KeyEvent.VK_W);
		btnWeekly.setActionCommand("Weekly");
		btnWeekly.addActionListener(btnListener);
		ButtonGroup buttons = new ButtonGroup();
		buttons.add(btnDaily);
		buttons.add(btnWeekly);
		add(btnDaily);
		add(btnWeekly);
	}

	private void addFacility() {
		ArrayList<FacilityData> temp = application.dbPowerJ.getFacilities(false);
		ArrayList<ItemData> facilities = new ArrayList<ItemData>();
		facilities.add(new ItemData((short) 0, "* All *"));
		for (int i = 0; i < temp.size(); i++) {
			facilities.add(new ItemData(temp.get(i).getFacID(), temp.get(i).getName()));
		}
		IComboBox cbo = new IComboBox();
		cbo.setName("Facilities");
		cbo.setItems(facilities.toArray());
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_FAC, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_F, "Facility: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addField() {
		ItemData[] list = new ItemData[9];
		list[0] = new ItemData(TB_CASES, "Cases");
		list[1] = new ItemData(TB_SPECS, "Specimens");
		list[2] = new ItemData(TB_BLOCKS, "Blocks");
		list[3] = new ItemData(TB_SLIDES, "Slides");
		list[4] = new ItemData(TB_VALUE1, application.getProperty("coder1"));
		list[5] = new ItemData(TB_VALUE2, application.getProperty("coder2"));
		list[6] = new ItemData(TB_VALUE3, application.getProperty("coder3"));
		list[7] = new ItemData(TB_VALUE4, application.getProperty("coder4"));
		list[8] = new ItemData(TB_VALUE5, application.getProperty("coder5"));
		IComboBox cbo = new IComboBox();
		cbo.setName("Status");
		cbo.setItems(list);
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_DATA, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_D, "Data: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addFilter() {
		JButton button = IUtilities.createJButton(48, KeyEvent.VK_I, "Filter");
		button.putClientProperty("status", "off");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton btn = (JButton) e.getSource();
				String status = (String) btn.getClientProperty("status");
				if (status.equals("off")) {
					btn.putClientProperty("status", "on");
					btn.setIcon(IUtilities.getIcon(48, "nofilter"));
				} else {
					btn.putClientProperty("status", "off");
					btn.setIcon(IUtilities.getIcon(48, "filter"));
				}
				application.panel.setFilter(TB_FILTER, TB_FILTER);
			}
		});
		add(button);
	}

	private void addGo() {
		JButton button = IUtilities.createJButton(48, KeyEvent.VK_O, "Ok");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				application.panel.setFilter(TB_GO, TB_GO);
			}
		});
		add(button);
	}

	private void addOrderGroups() {
		ArrayList<OrderGroupData> temp = application.dbPowerJ.getOrderGroups();
		ItemData[] list = new ItemData[temp.size() + 1];
		list[0] = new ItemData((short) 0, "** All **");
		for (int i = 0; i < temp.size(); i++) {
			list[i + 1] = new ItemData(temp.get(i).getGrpID(), temp.get(i).getName());
		}
		temp.clear();
		IComboBox cbo = new IComboBox();
		cbo.setName("OrderGroups");
		cbo.setItems(list);
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_ORG, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_G, "Groups: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addOrderTypes() {
		ArrayList<ItemData> temp = application.dbPowerJ.getOrderTypes();
		ItemData[] items = new ItemData[temp.size() + 1];
		items[0] = new ItemData((short) 0, "** All **");
		for (int i = 0; i < temp.size(); i++) {
			items[i + 1] = new ItemData(temp.get(i).getID(), temp.get(i).getName());
		}
		temp.clear();
		IComboBox cbo = new IComboBox();
		cbo.setName("OrderTypes");
		cbo.setItems(items);
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_ORT, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "Types: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addPerson() {
		ItemData[] items = new ItemData[LibConstants.PERSON_STRINGS.length + 1];
		items[0] = new ItemData((short) 0, "** All **");
		for (byte i = 1; i < LibConstants.PERSON_STRINGS.length; i++) {
			items[i + 1] = new ItemData(i, LibConstants.PERSON_STRINGS[i]);
		}
		IComboBox cbo = new IComboBox();
		cbo.setName("Persons");
		cbo.setItems(items);
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_PRS, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_P, "Personnel: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addProcedure() {
		ProcedureList lstProcedures = application.dbPowerJ.getProcedures(true);
		IComboBox cbo = new IComboBox();
		cbo.setName("Procedures");
		cbo.setItems(lstProcedures.getAll());
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_PRO, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_P, "Procedures: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addRows(byte[] rows, byte[] values) {
		IRowCombo cboRows = new IRowCombo(rows, values);
		cboRows.setName("Rows");
		cboRows.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IRowCombo cbo = (IRowCombo) e.getSource();
					application.panel.setFilter(cbo.getValue());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_R, "Rows: ");
		label.setLabelFor(cboRows);
		add(label);
		add(cboRows);
	}

	private void addSpecialty() {
		SpecialtyList lstSpecialties = application.dbPowerJ.getSpecialties(true);
		IComboBox cbo = new IComboBox();
		cbo.setName("Specialties");
		cbo.setItems(lstSpecialties.getAll());
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_SPY, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_S, "Specialties: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addSpin() {
		JButton button = IUtilities.createJButton(48, KeyEvent.VK_I, "Spin");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				application.panel.setFilter(TB_SPIN, TB_SPIN);
			}
		});
		add(button);
	}

	private void addStatus() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		list.add(new ItemData(LibConstants.STATUS_ALL, "* All *"));
		for (byte i = LibConstants.STATUS_ACCES; i < LibConstants.STATUS_ALL; i++) {
			list.add(new ItemData(i, LibConstants.STATUS_STRINGS[i]));
		}
		IComboBox cbo = new IComboBox();
		cbo.setName("Status");
		cbo.setItems(list.toArray());
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_STA, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "Status: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void addSubspecialty() {
		SubspecialtyList lstSubspecialties = application.dbPowerJ.getSubspecialties(true);
		IComboBox cbo = new IComboBox();
		cbo.setName("Subspecialties");
		cbo.setItems(lstSubspecialties.getAll());
		cbo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboBox cb = (IComboBox) e.getSource();
					application.panel.setFilter(TB_SUB, cb.getIndex());
				}
			}
		});
		JLabel label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_B, "Subspecialties: ");
		label.setLabelFor(cbo);
		add(label);
		add(cbo);
	}

	private void createPanel() {
		setAlignmentX(Component.LEFT_ALIGNMENT);
		switch (application.panelID) {
		case LibConstants.ACTION_BACKLOG:
			addFacility();
			addSpecialty();
			addSubspecialty();
			addProcedure();
			if (!((application.getUserID() == -222 || application.getUserID() == -111) && application.isAutologin())) {
				addStatus();
			}
			break;
		case LibConstants.ACTION_FINALS:
			addFacility();
			addSpecialty();
			addSubspecialty();
			addProcedure();
			break;
		case LibConstants.ACTION_HISTOLOGY:
			addFacility();
			addSubspecialty();
			addProcedure();
			break;
		case LibConstants.ACTION_ORDERGROUP:
			addOrderTypes();
			break;
		case LibConstants.ACTION_ORDERMASTER:
			addOrderGroups();
			break;
		case LibConstants.ACTION_PENDING:
			addFacility();
			addSpecialty();
			addSubspecialty();
			addProcedure();
			addStatus();
			break;
		case LibConstants.ACTION_PERSONNEL:
			addPerson();
			break;
		case LibConstants.ACTION_ROUTING:
			addFacility();
			addSpecialty();
			addSubspecialty();
			addDayWeek();
			break;
		case LibConstants.ACTION_SCHEDULE:
			addFacility();
			addSpin();
			break;
		case LibConstants.ACTION_SPECGROUP:
			addSpecialty();
			addSubspecialty();
			addProcedure();
			break;
		case LibConstants.ACTION_SPECMASTER:
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

	private void createPanel(Calendar start, Calendar end, Calendar min, Calendar max, byte[] values) {
		setAlignmentX(Component.LEFT_ALIGNMENT);
		switch (application.panelID) {
		case LibConstants.ACTION_DISTRIBUTE:
			addFacility();
			addSpecialty();
			addDates(start, end, min, max);
			addField();
			addGo();
			break;
		case LibConstants.ACTION_FORECAST:
			if (values != null) {
				byte[] rows = { IRowPanel.SPN_FACILITY, IRowPanel.SPN_SPECIALTY, IRowPanel.SPN_SUBSPECIAL,
						IRowPanel.SPN_PROCEDURE, IRowPanel.SPN_SPECIMEN };
				addRows(rows, values);
			}
			addGo();
			break;
		case LibConstants.ACTION_SPECIMEN:
			addDates(start, end, min, max);
			if (values != null) {
				byte[] rows = { IRowPanel.SPN_FACILITY, IRowPanel.SPN_SPECIALTY, IRowPanel.SPN_SUBSPECIAL,
						IRowPanel.SPN_PROCEDURE, IRowPanel.SPN_SPECIMEN };
				addRows(rows, values);
			}
			addGo();
			break;
		case LibConstants.ACTION_WORKDAYS:
			addFacility();
			addDates(start, end, min, max);
			addSpin();
			addGo();
			break;
		default:
			// Workload
			addDates(start, end, min, max);
			if (values != null) {
				byte[] rows = { IRowPanel.SPN_FACILITY, IRowPanel.SPN_SPECIALTY, IRowPanel.SPN_SUBSPECIAL,
						IRowPanel.SPN_PROCEDURE, IRowPanel.SPN_STAFF };
				addRows(rows, values);
			}
			addGo();
			break;
		}
	}
}