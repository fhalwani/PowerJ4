package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTable;

class NDistribute extends NBase {
	private short facID = 0;
	private double annualFte = 0;
	private long timeFrom = 0;
	private long timeTo = 0;
	private HashMap<Short, DataPerson> persons = new HashMap<Short, DataPerson>();
	private HashMap<Byte, DataSubspec> subs = new HashMap<Byte, DataSubspec>();
	private HashMap<Short, DataFacility> facilities = new HashMap<Short, DataFacility>();
	private ArrayList<DataHeader> headers = new ArrayList<DataHeader>();
	private ArrayList<DataPerson> rows = new ArrayList<DataPerson>();
	private ModelFTE model;
	private ITable tblList;

	NDistribute(AClient parent) {
		super(parent);
		setName("Distribution");
		parent.dbPowerJ.prepareCasesSummary();
		annualFte = Double.parseDouble(parent.setup.getString(LSetup.VAR_V5_FTE));
		if (annualFte < 1.00) {
			annualFte = 1.00;
		}
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		for (Entry<Short, DataFacility> entry1 : facilities.entrySet()) {
			DataFacility facility = entry1.getValue();
			for (Entry<Byte, DataSubspec> entry2 : facility.subspecs.entrySet()) {
				DataSubspec subspec = entry2.getValue();
				subspec.persons.clear();
			}
			facility.subspecs.clear();
		}
		facilities.clear();
		persons.clear();
		subs.clear();
		headers.clear();
		rows.clear();
		return true;
	}

	private void createPanel() {
		model = new ModelFTE();
		tblList = new ITable(pj, model) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int c = columnAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					String s = "";
					if (c <= 0) {
						s = rows.get(m).prsFull;
					} else if (c < headers.size()) {
						s = rows.get(m).prsFull + ": " + headers.get(c - 1).subDescr + ": "
								+ rows.get(m).subspecs.get(headers.get(c - 1).subID);
					} else {
						s = rows.get(m).prsFull + ": " + rows.get(m).fte;
					}
					return s;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		JScrollPane scrollList = IGUI.createJScrollPane(tblList);
		setLayout(new BorderLayout());
		add(new IToolBar(this), BorderLayout.NORTH);
		add(scrollList, BorderLayout.CENTER);
	}

	private void getData() {
		ResultSet rst = null;
		try {
			for (Entry<Short, DataFacility> entry1 : facilities.entrySet()) {
				DataFacility facility = entry1.getValue();
				for (Entry<Byte, DataSubspec> entry2 : facility.subspecs.entrySet()) {
					DataSubspec subspec = entry2.getValue();
					subspec.persons.clear();
				}
				facility.subspecs.clear();
			}
			facilities.clear();
			persons.clear();
			subs.clear();
			pj.dbPowerJ.setDate(DPowerJ.STM_CSE_SL_SUM, 1, timeFrom);
			pj.dbPowerJ.setDate(DPowerJ.STM_CSE_SL_SUM, 2, timeTo);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_CSE_SL_SUM);
			while (rst.next()) {
				DataFacility facility = facilities.get(rst.getShort("FAID"));
				if (facility == null) {
					facility = new DataFacility();
					facilities.put(rst.getShort("FAID"), facility);
				}
				DataSubspec subspec = facility.subspecs.get(rst.getByte("SBID"));
				if (subspec == null) {
					subspec = new DataSubspec();
					subspec.subID = rst.getByte("SBID");
					facility.subspecs.put(rst.getByte("SBID"), subspec);
				}
				DataPerson person = subspec.persons.get(rst.getShort("FNID"));
				if (person == null) {
					person = new DataPerson();
					person.prsID = rst.getShort("FNID");
					subspec.persons.put(rst.getShort("FNID"), person);
				}
				person.fte += rst.getDouble("CAV5");
				subspec.fte += rst.getDouble("CAV5");
				DataSubspec sub = subs.get(rst.getByte("SBID"));
				if (sub == null) {
					sub = new DataSubspec();
					sub.subName = rst.getString("SBNM");
					sub.subDescr = rst.getString("SBDC");
				}
				DataPerson staff = persons.get(rst.getShort("FNID"));
				if (staff == null) {
					staff = new DataPerson();
					staff.prsName = rst.getString("FNNM");
					staff.prsFull = rst.getString("FNLS");
				}
			}
			rst.close();
			// Frozen Sections
			pj.dbPowerJ.setDate(DPowerJ.STM_FRZ_SL_SUM, 1, timeFrom);
			pj.dbPowerJ.setDate(DPowerJ.STM_FRZ_SL_SUM, 2, timeTo);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_FRZ_SL_SUM);
			while (rst.next()) {
				DataFacility facility = facilities.get(rst.getShort("FAID"));
				if (facility == null) {
					facility = new DataFacility();
					facilities.put(rst.getShort("FAID"), facility);
				}
				DataSubspec subspec = facility.subspecs.get(rst.getByte("SBID"));
				if (subspec == null) {
					subspec = new DataSubspec();
					subspec.subID = rst.getByte("SBID");
					facility.subspecs.put(rst.getByte("SBID"), subspec);
				}
				DataPerson person = subspec.persons.get(rst.getShort("PRID"));
				if (person == null) {
					person = new DataPerson();
					person.prsID = rst.getShort("PRID");
					subspec.persons.put(rst.getShort("PRID"), person);
				}
				person.fte += rst.getDouble("CAV5");
				DataSubspec sub = subs.get(rst.getByte("SBID"));
				if (sub == null) {
					sub = new DataSubspec();
					sub.subName = rst.getString("SBNM");
					sub.subDescr = rst.getString("SBDC");
				}
				DataPerson staff = persons.get(rst.getShort("PRID"));
				if (staff == null) {
					staff = new DataPerson();
					staff.prsName = rst.getString("PRNM");
					staff.prsFull = rst.getString("PRLS");
				}
			}
			// Additional
			pj.dbPowerJ.setDate(DPowerJ.STM_ADD_SL_SUM, 1, timeFrom);
			pj.dbPowerJ.setDate(DPowerJ.STM_ADD_SL_SUM, 2, timeTo);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ADD_SL_SUM);
			while (rst.next()) {
				DataFacility facility = facilities.get(rst.getShort("FAID"));
				if (facility == null) {
					facility = new DataFacility();
					facilities.put(rst.getShort("FAID"), facility);
				}
				DataSubspec subspec = facility.subspecs.get(rst.getByte("SBID"));
				if (subspec == null) {
					subspec = new DataSubspec();
					subspec.subID = rst.getByte("SBID");
					facility.subspecs.put(rst.getByte("SBID"), subspec);
				}
				DataPerson person = subspec.persons.get(rst.getShort("PRID"));
				if (person == null) {
					person = new DataPerson();
					person.prsID = rst.getShort("PRID");
					subspec.persons.put(rst.getShort("PRID"), person);
				}
				person.fte += rst.getDouble("CAV5");
				DataSubspec sub = subs.get(rst.getByte("SBID"));
				if (sub == null) {
					sub = new DataSubspec();
					sub.subName = rst.getString("SBNM");
					sub.subDescr = rst.getString("SBDC");
				}
				DataPerson staff = persons.get(rst.getShort("PRID"));
				if (staff == null) {
					staff = new DataPerson();
					staff.prsName = rst.getString("PRNM");
					staff.prsFull = rst.getString("PRLS");
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	void setFilter() {
		DataPerson rowTotal = new DataPerson();
		headers.clear();
		rows.clear();
		for (Entry<Short, DataFacility> entry1 : facilities.entrySet()) {
			if (facID == 0 || facID == entry1.getKey()) {
				DataFacility facility = entry1.getValue();
				for (Entry<Byte, DataSubspec> entry2 : facility.subspecs.entrySet()) {
					DataSubspec subspec = entry2.getValue();
					if (subspec.fte > 0) {
						boolean found = false;
						DataHeader header = new DataHeader();
						for (int i = 0; i < headers.size(); i++) {
							header = headers.get(i);
							if (header.subID == subspec.subID) {
								found = true;
								break;
							}
						}
						if (!found) {
							header = new DataHeader();
							header.subID = subspec.subID;
							header.subName = subs.get(subspec.subID).subName;
							header.subDescr = subs.get(subspec.subID).subDescr;
							headers.add(header);
						}
						for (Entry<Short, DataPerson> entry3 : subspec.persons.entrySet()) {
							DataPerson person = entry3.getValue();
							if (person.fte > 0) {
								found = false;
								DataPerson row = new DataPerson();
								for (int i = 0; i < rows.size(); i++) {
									row = rows.get(i);
									if (row.prsID == person.prsID) {
										found = true;
										break;
									}
								}
								if (!found) {
									row = new DataPerson();
									row.prsID = person.prsID;
									row.prsName = persons.get(person.prsID).prsName;
									row.prsFull = persons.get(person.prsID).prsFull;
									rows.add(row);
								}
								row.fte += person.fte;
								Double fte5 = row.subspecs.get(header.subID);
								if (fte5 == null) {
									row.subspecs.put(header.subID, person.fte);
								} else {
									fte5 += person.fte;
								}
								rowTotal.fte += person.fte;
								Double fteTotal = row.subspecs.get(header.subID);
								if (fteTotal == null) {
									rowTotal.subspecs.put(header.subID, person.fte);
								} else {
									fteTotal += person.fte;
								}
							}
						}
					}
				}
			}
		}
		rowTotal.prsName = "SUM";
		rowTotal.prsFull = "Total";
		rows.add(rowTotal);
		DataHeader colTotal = new DataHeader();
		colTotal.subName = "SUM";
		colTotal.subDescr = "Total";
		headers.add(colTotal);
		for (DataPerson row : rows) {
			for (Entry<Byte, Double> entry2 : row.subspecs.entrySet()) {
				double d = entry2.getValue().doubleValue();
				entry2.setValue(1.00 * d / annualFte);
			}
		}
	}

	private class DataFacility {
		HashMap<Byte, DataSubspec> subspecs = new HashMap<Byte, DataSubspec>();
	}

	private class DataHeader {
		byte subID = 0;
		String subName = "";
		String subDescr = "";
	}

	private class DataPerson {
		short prsID = 0;
		double fte = 0;
		String prsName = "";
		String prsFull = "";
		private HashMap<Byte, Double> subspecs = new HashMap<Byte, Double>();
	}

	private class DataSubspec {
		byte subID = 0;
		double fte = 0;
		String subName = "";
		String subDescr = "";
		private HashMap<Short, DataPerson> persons = new HashMap<Short, DataPerson>();
	}

	private class ModelFTE extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			}
			return Double.class;
		}

		@Override
		public int getColumnCount() {
			return headers.size() + 1;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) {
				return "Staff";
			} else {
				return headers.get(col - 1).subName;
			}
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col <= 0) {
				return rows.get(row).prsName;
			} else if (col < headers.size()) {
				return rows.get(row).subspecs.get(headers.get(col - 1).subID);
			} else {
				return rows.get(row).fte;
			}
		}
	}
}