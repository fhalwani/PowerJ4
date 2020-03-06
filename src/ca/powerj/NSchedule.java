package ca.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

class NSchedule extends NBase {
	private boolean byService = true;
	private short facID = 0;
	private int rowIndex = -1;
	private OService service = new OService();
	private OWorkday workday = new OWorkday();
	private ArrayList<ArrayList<OScheduleService>> scheduleServices = new ArrayList<ArrayList<OScheduleService>>();
	private ArrayList<OScheduleStaff> scheduleStaff = new ArrayList<OScheduleStaff>();
	private ArrayList<OWorkday> workdays = new ArrayList<OWorkday>();
	private ArrayList<OService> services = new ArrayList<OService>();
	private HashMap<Short, String> persons = new HashMap<Short, String>();
	private ArrayList<Date> dates = new ArrayList<Date>();
	private ModelSchedule modelSchedule;
	private ModelDates modelDates;
	private ITable tblList, tblSchedule;
	private IComboBox cboPersons;

	NSchedule(AClient parent) {
		super(parent);
		setName("Schedule");
		parent.dbPowerJ.prepareSchedules(pj.userAccess[LConstants.ACCESS_STP_SC]);
		getWeeks();
		getServices();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			persons.clear();
			services.clear();
			workdays.clear();
			scheduleServices.clear();
			dates.clear();
		}
		return !altered;
	}

	private void createPanel() {
		modelDates = new ModelDates();
		tblList = new ITable(pj, modelDates);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tblList.addAncestorListener(new IFocusListener());
		tblList.addFocusListener(this);
		tblList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				setRow(tblList.convertRowIndexToModel(index));
			}
		});
		tblList.getColumnModel().getColumn(0).setMinWidth(120);
		JScrollPane scrollList = IGUI.createJScrollPane(tblList);
		scrollList.setMinimumSize(new Dimension(200, 900));
		modelSchedule = new ModelSchedule();
		cboPersons = new IComboBox();
		cboPersons.setModel(getPersons());
		tblSchedule = new ITable(pj, modelSchedule) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int c = columnAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					if (c == 0) {
						return (byService ? services.get(m).descr:
							persons.get(scheduleStaff.get(m).prsID));
					} else {
						return (byService ? persons.get(scheduleServices.get(m).get(c -1).person.id) :
							scheduleStaff.get(m).services[c -1]);
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblSchedule.addFocusListener(this);
		TableColumnModel columns = tblSchedule.getColumnModel();
		for (int i = 1; i < columns.getColumnCount(); i++) {
			TableColumn column = columns.getColumn(i);
			column.setCellEditor(new DefaultCellEditor(cboPersons));
		}
		JScrollPane scrollSchedule = IGUI.createJScrollPane(tblSchedule);
		scrollSchedule.setMinimumSize(new Dimension(800, 900));
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollList);
		pnlSplit.setBottomComponent(scrollSchedule);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		add(new IToolBar(this), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getDays() {
		Calendar calMonday = Calendar.getInstance();
		Calendar calStart = pj.dates.setMidnight(null);
		ResultSet rst = null;
		try {
			workdays.clear();
			calMonday.setTimeInMillis(dates.get(rowIndex).getTime());
			pj.dbPowerJ.setDate(DPowerJ.STM_WDY_SELECT, 1, calMonday.getTimeInMillis());
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_WDY_SELECT);
			while (rst.next() && workdays.size() < 7) {
				workday = new OWorkday();
				calStart.setTimeInMillis(rst.getDate("WDDT").getTime());
				workday.date.setTime(calStart.getTimeInMillis());
				workday.name = pj.dates.formatter(workday.date, LDates.FORMAT_SCHED);
				workday.dow  = calStart.get(Calendar.DAY_OF_WEEK);
				workday.wdID = rst.getInt("WDID");
				workday.isOn = (rst.getString("WDTP").equalsIgnoreCase("D"));
				workdays.add(workday);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private Object[] getPersons() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_PRS_SELECT);
		try {
			persons.clear();
			while (rst.next()) {
				if (rst.getString("PRCD").trim().equalsIgnoreCase("PT")
						&& rst.getString("PRAC").trim().equalsIgnoreCase("Y")) {
					list.add(new OItem(rst.getShort("PRID"), rst.getString("PRNM").trim()));
					persons.put(rst.getShort("PRID"),
							rst.getString("PRFR").trim() + " " + rst.getString("PRLS").trim());

				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
	}

	private void getScheduleService() {
		short srvID = 0;
		short dateID = 0;
		short rowID = -1;
		short colID = -1;
		Calendar calMonday = Calendar.getInstance();
		ResultSet rst = null;
		try {
			scheduleServices.clear();
			scheduleStaff.clear();
			OScheduleService schedule = new OScheduleService();
			ArrayList<OScheduleService> cols = new ArrayList<OScheduleService>();
			for (int row = 0; row < services.size(); row++) {
				service = services.get(row);
				cols = new ArrayList<OScheduleService>();
				for (int col = 0; col < workdays.size(); col++) {
					workday         = workdays.get(col);
					schedule        = new OScheduleService();
					schedule.isNew  = true;
					schedule.wdID   = workday.wdID;
					schedule.date   = workday.name;
					schedule.srvID  = service.srvID;
					schedule.servce = service.name;
					// On call or (regular work day and on that specific day)
					schedule.isOn = (service.codes[0] || (workday.isOn && service.codes[workday.dow]));
					if (!schedule.isOn) {
						schedule.person = new OItem((short) -1, "   ");
					}
					cols.add(schedule);
				}
				scheduleServices.add(cols);
			}
			calMonday.setTimeInMillis(dates.get(rowIndex).getTime());
			pj.dbPowerJ.setDate(DPowerJ.STM_SCH_SL_SRV, 1, calMonday.getTimeInMillis());
			calMonday.add(Calendar.DAY_OF_YEAR, 7);
			pj.dbPowerJ.setDate(DPowerJ.STM_SCH_SL_SRV, 2, calMonday.getTimeInMillis());
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SCH_SL_SRV);
			while (rst.next()) {
				if (srvID != rst.getShort("SRID")) {
					srvID = rst.getShort("SRID");
					rowID = -1;
					for (short row = 0; row < services.size(); row++) {
						if (srvID == services.get(row).srvID) {
							rowID = row;
							break;
						}
					}
				}
				if (dateID != rst.getShort("WDID")) {
					dateID = rst.getShort("WDID");
					colID = -1;
					for (short col = 0; col < workdays.size(); col++) {
						if (dateID == workdays.get(col).wdID) {
							colID = col;
							break;
						}
					}
				}
				if (rowID > -1 && colID > -1) {
					scheduleServices.get(rowID).get(colID).isNew = false;
					scheduleServices.get(rowID).get(colID).person = new OItem(rst.getShort("PRID"), rst.getString("PRNM"));
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private void getScheduleStaff() {
		short    prsID       = -1;
		short    dateID      = -1;
		short    rowID       = -1;
		short    colID       = -1;
		Calendar calMonday   = Calendar.getInstance();
		OScheduleStaff staff = new OScheduleStaff();
		ResultSet      rst   = null;
		try {
			scheduleServices.clear();
			scheduleStaff.clear();
			calMonday.setTimeInMillis(dates.get(rowIndex).getTime());
			pj.dbPowerJ.setDate(DPowerJ.STM_SCH_SL_STA, 1, calMonday.getTimeInMillis());
			calMonday.add(Calendar.DAY_OF_YEAR, 6);
			pj.dbPowerJ.setDate(DPowerJ.STM_SCH_SL_STA, 2, calMonday.getTimeInMillis());
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SCH_SL_STA);
			while (rst.next()) {
				if (prsID != rst.getShort("PRID")) {
					prsID  = rst.getShort("PRID");
					rowID  = -1;
					dateID = -1;
					colID  = -1;
					for (short row = 0; row < scheduleStaff.size(); row++) {
						if (prsID == scheduleStaff.get(row).prsID) {
							staff = scheduleStaff.get(row);
							rowID = row;
							break;
						}
					}
					if (rowID == -1) {
						staff = new OScheduleStaff();
						staff.prsID = prsID;
						staff.name  = rst.getString("PRNM");
						for (int i = 0; i < 7; i++) {
							staff.services[i] = "";
						}
						scheduleStaff.add(staff);
						rowID = (short) (scheduleStaff.size() -1);
					}
				}
				if (dateID != rst.getShort("WDID")) {
					dateID = rst.getShort("WDID");
					colID  = -1;
					for (short col = 0; col < workdays.size(); col++) {
						if (dateID == workdays.get(col).wdID) {
							colID = col;
							break;
						}
					}
				}
				if (colID > -1) {
					if (staff.services[colID].length() > 0) {
						staff.services[colID] += "/";
					}
					staff.services[colID] += rst.getString("SRNM");
				} else {
					pj.log(LConstants.ERROR_UNEXPECTED, getName(), "getScheduleStaff - INVALID dateID - " + dateID);
				}
			}
			Collections.sort(scheduleStaff, new Comparator<OScheduleStaff>() {
				@Override
				public int compare(OScheduleStaff o1, OScheduleStaff o2) {
					return o1.name.compareTo(o2.name);
				}
			});
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private void getServices() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SRV_SELECT);
		try {
			services.clear();
			while (rst.next()) {
				if (facID == rst.getShort("FAID") || facID == 0) {
					service = new OService();
					service.srvID = rst.getByte("SRID");
					service.name  = rst.getString("SRNM");
					service.descr = rst.getString("SRDC");
					service.codes = pj.numbers.shortToBoolean(rst.getShort("SRCD"));
					services.add(service);
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private void getWeeks() {
		Calendar isMonday = Calendar.getInstance();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SCH_SL_MON);
		try {
			while (rst.next()) {
				isMonday.setTimeInMillis(rst.getDate("WDDT").getTime());
				if (isMonday.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
					Date date = new Date();
					date.setTime(rst.getDate("WDDT").getTime());
					dates.add(date);
				}
			}
			Collections.sort(dates, new Comparator<Date>() {
				public int compare(Date o1, Date o2) {
					return (o1.getTime() > o2.getTime() ? -1 : (o1.getTime() == o2.getTime() ? 0 : 1));
				}
			});
			for (int i = dates.size() -1 ; dates.size() > 52; i--) {
				dates.remove(i);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	void save(OScheduleService schedule) {
		if (schedule.wdID > 0 && schedule.srvID > 0 && schedule.person.id > 0) {
			byte index = (schedule.isNew ? DPowerJ.STM_SCH_INSERT : DPowerJ.STM_SCH_UPDATE);
			pj.dbPowerJ.setShort(index, 1, schedule.person.id);
			pj.dbPowerJ.setShort(index, 2, schedule.srvID);
			pj.dbPowerJ.setInt(index, 3, schedule.wdID);
			if (pj.dbPowerJ.execute(index) > 0) {
				altered = false;
				if (schedule.isNew) {
					schedule.isNew = false;
				}
			}
		}
	}

	@Override
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_FAC:
			facID = value;
			getServices();
			break;
		default:
			byService = !byService;
			if (byService) {
				getScheduleService();
			} else {
				getScheduleStaff();
			}
			modelSchedule.fireTableStructureChanged();
			TableColumnModel columns = tblSchedule.getColumnModel();
			for (int i = 1; i < columns.getColumnCount(); i++) {
				TableColumn column = columns.getColumn(i);
				if (byService) {
					column.setCellEditor(new DefaultCellEditor(cboPersons));
				} else {
					column.setCellEditor(null);
				}
			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
			if (altered)
				return;
		}
		if (rowIndex != index) {
			if (index < 0 || index >= dates.size()) {
				// Selection got filtered away.
				return;
			}
			rowIndex = index;
			getDays();
			if (byService) {
				getScheduleService();
			} else {
				getScheduleStaff();
			}
			modelSchedule.fireTableStructureChanged();
			pj.statusBar.setMessage(pj.dates.formatter(dates.get(rowIndex).getTime(), LDates.FORMAT_DATELONG));
		}
	}

	private class ModelDates extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			return Date.class;
		}

		@Override
		public String getColumnName(int col) {
			return "Week";
		}

		@Override
		public int getRowCount() {
			return dates.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return dates.get(row);
		}
	}

	private class ModelSchedule extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0 || byService) {
				return String.class;
			}
			return OItem.class;
		}

		@Override
		public int getColumnCount() {
			return workdays.size() + 1;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) {
				return (byService ? "Service": "Staff");
			} else {
				return workdays.get(col - 1).name;
			}
		}

		@Override
		public int getRowCount() {
			return (byService ? services.size(): scheduleStaff.size());
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (byService) {
				if (scheduleServices.size() > row) {
					if (col > 0) {
						if (scheduleServices.get(row).size() > col) {
							return scheduleServices.get(row).get(col - 1).person;
						}
					} else {
						return services.get(row).name;
					}
				}
			} else {
				if (scheduleStaff.size() > row) {
					if (col > 0) {
						return scheduleStaff.get(row).services[col - 1];
					} else {
						return scheduleStaff.get(row).name;
					}
				}
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			// Only if view is byService is editable
			return (byService && col > 0
					&& pj.userAccess[LConstants.ACCESS_STP_SC]
					&& scheduleServices.get(row).get(col - 1).isOn);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col > 0 && pj.userAccess[LConstants.ACCESS_STP_SC] && scheduleServices.get(row).get(col - 1).isOn) {
				scheduleServices.get(row).get(col - 1).person = (OItem) value;
				save(scheduleServices.get(row).get(col - 1));
			}
		}
	}
}