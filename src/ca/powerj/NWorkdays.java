package ca.powerj;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

class NWorkdays extends NBase {
	private boolean byService = true;
	private short   facID     = 0;
	private long    timeFrom  = 0;
	private long    timeTo    = 0;
	volatile ArrayList<DataHeader> headers = new ArrayList<DataHeader>();
	volatile ArrayList<DataRow> rows = new ArrayList<DataRow>();
	private ModelWorkdays model;
	private ITable tblList;

	public NWorkdays(AClient parent) {
		super(parent);
		setName("Workdays");
		parent.dbPowerJ.prepareScheduleSummary();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		headers.clear();
		rows.clear();
		return true;
	}

	private void createPanel() {
		model = new ModelWorkdays();
		tblList = new ITable(pj, model) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					String s = "";
					if (m >= 0 && m < rows.size()) {
						s = rows.get(m).full;
					}
					return s;
				} catch (IndexOutOfBoundsException ignore) {
				    return null;
				}
			}
		};
		JScrollPane scrollList = IGUI.createJScrollPane(tblList);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = pj.dates.setMidnight(null);
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calMax.set(Calendar.DAY_OF_MONTH, 1);
		timeFrom = pj.setup.getLong(LSetup.VAR_MIN_WL_DATE);
		timeTo = calMax.getTimeInMillis();
		calMin.setTimeInMillis(timeFrom);
		calStart.setTimeInMillis(calMin.getTimeInMillis());
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, calStart, calEnd, calMin, calMax, false), BorderLayout.NORTH);
		add(scrollList, BorderLayout.CENTER);
	}

	@Override
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_FAC:
			facID = value;
			break;
		case IToolBar.TB_SPIN:
			byService = !byService;
			if (timeTo > timeFrom) {
				WorkerData worker = new WorkerData();
				worker.execute();
			}
			break;
		default:
			if (timeTo > timeFrom) {
				WorkerData worker = new WorkerData();
				worker.execute();
			}
		}
	}

	@Override
	void setFilter(short id, Calendar value) {
		switch (id) {
		case IToolBar.TB_FROM:
			timeFrom = value.getTimeInMillis();
			break;
		default:
			timeTo = value.getTimeInMillis();
		}
	}

	private class DataHeader {
		short  srvID    = 0;
		String srvName  = "";
	}

	private class DataRow {
		short  noDays = 0;
		String name   = "";
		String full   = "";
		short[] services;
	}

	private class ModelWorkdays extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			}
			return Short.class;
		}

		@Override
		public int getColumnCount() {
			return headers.size();
		}

		@Override
		public String getColumnName(int col) {
			return headers.get(col).srvName;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return rows.get(row).name;
			} else if (col > 1) {
				return rows.get(row).services[col-2];
			} else {
				return rows.get(row).noDays;
			}
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			short      tmpSrvID = 0;
			short      tmpPrsID = 0;
			int        tmpWdID  = 0;
			String     service  = "";
			DataSrvce  prsSrvce = new DataSrvce();
			DataPerson person   = new DataPerson();
			DataHeader header   = new DataHeader();
			DataRow    row      = new DataRow();
			ResultSet  rst      = null;
			HashMap<Short, DataPerson> persons  = new HashMap<Short, DataPerson>();
			HashMap<Short, String>     services = new HashMap<Short, String>();
			try {
				headers.clear();
				rows.clear();
				pj.dbPowerJ.setDate(DPowerJ.STM_SCH_SL_SUM, 1, timeFrom);
				pj.dbPowerJ.setDate(DPowerJ.STM_SCH_SL_SUM, 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SCH_SL_SUM);
				while (rst.next()) {
					if (facID > 0 && facID != rst.getShort("FAID")) {
						continue;
					}
					if (tmpPrsID != rst.getShort("PRID")) {
						tmpWdID  = 0;
						tmpSrvID = 0;
						tmpPrsID = rst.getShort("PRID");
						person   = persons.get(tmpPrsID);
						if (person == null) {
							person = new DataPerson();
							person.name = rst.getString("PRNM");
							person.full = rst.getString("PRFR").trim() + " " + rst.getString("PRLS").trim();
							persons.put(tmpPrsID, person);
						}
					}
					if (tmpWdID != rst.getInt("WDID")) {
						tmpWdID = rst.getInt("WDID");
						person.noDays++;
					}
					if (byService) {
						if (tmpSrvID != rst.getShort("SRID")) {
							tmpSrvID = rst.getShort("SRID");
							prsSrvce = person.services.get(tmpSrvID);
							if (prsSrvce == null) {
								prsSrvce = new DataSrvce();
								prsSrvce.name = rst.getString("SRNM");
								person.services.put(tmpSrvID, prsSrvce);
							}
							service = services.get(tmpSrvID);
							if (service == null) {
								services.put(tmpSrvID, prsSrvce.name);
							}
						}
					} else {
						if (tmpSrvID != rst.getShort("SBID")) {
							tmpSrvID = rst.getShort("SBID");
							prsSrvce = person.services.get(tmpSrvID);
							if (prsSrvce == null) {
								prsSrvce = new DataSrvce();
								prsSrvce.name = rst.getString("SBNM");
								person.services.put(tmpSrvID, prsSrvce);
							}
							service = services.get(tmpSrvID);
							if (service == null) {
								services.put(tmpSrvID, prsSrvce.name);
							}
						}
					}
					
					prsSrvce.noDays += 1;
				}
				ArrayList<DataHeader> temp = new ArrayList<DataHeader>();
				for (Entry<Short, String> entry : services.entrySet()) {
					header = new DataHeader();
					header.srvID   = entry.getKey();
					header.srvName = entry.getValue();
					temp.add(header);
				}
				int noServices = temp.size();
				Collections.sort(temp, new Comparator<DataHeader>() {
					@Override
					public int compare(DataHeader o1, DataHeader o2) {
						return o1.srvName.compareToIgnoreCase(o2.srvName);
					}
				});
				headers.clear();
				header = new DataHeader();
				header.srvName  = "NAME";
				headers.add(header);
				header = new DataHeader();
				header.srvName  = "DAYS";
				headers.add(header);
				for (int i = 0; i < noServices; i++) {
					header = new DataHeader();
					header.srvID   = temp.get(i).srvID;
					header.srvName = temp.get(i).srvName;
					headers.add(header);
				}
				rows.clear();
				for (Entry<Short, DataPerson> entry : persons.entrySet()) {
					person = entry.getValue();
					row = new DataRow();
					row.name   = person.name;
					row.full   = person.full;
					row.noDays = person.noDays;
					row.services = new short[noServices];
					for (int i = 0; i < noServices; i++) {
						header = temp.get(i);
						DataSrvce ds = person.services.get(header.srvID);
						if (ds == null) {
							row.services[i] = 0;
						} else if (header.srvName.equals(ds.name)) {
							row.services[i] = ds.noDays;
						} else {
							row.services[i] = 0;
						}
					}
					rows.add(row);
				}
				Collections.sort(rows, new Comparator<DataRow>() {
					@Override
					public int compare(DataRow o1, DataRow o2) {
						return (o1.noDays > o2.noDays ? -1
								: (o1.noDays < o2.noDays ? 1 : o1.name.compareTo(o2.name)));
					}
				});
				DataRow total = new DataRow();
				total.name  = "Ztotal";
				total.full  = "Ztotal";
				total.services = new short[noServices];
				for (int i = 0; i < rows.size() -1; i++) {
					total.noDays += rows.get(i).noDays;
					for (int j = 0; j < noServices; j++) {
						total.services[j] += rows.get(i).services[j];
					}
				}
				rows.add(total);
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.closeRst(rst);
			}
			return null;
		}

		@Override
		public void done() {
			model.fireTableStructureChanged();
			pj.setBusy(false);
		}

		private class DataPerson {
			short  noDays  = 0;
			String name    = "";
			String full    = "";
			HashMap<Short, DataSrvce> services = new HashMap<Short, DataSrvce>();
		}

		private class DataSrvce {
			short  noDays = 0;
			String name   = "";
		}
	}
}
