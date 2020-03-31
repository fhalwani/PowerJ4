package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

class ITableModelEvent extends ITableModel {
	private final byte EVNT_TIME = 0;
	private final byte EVNT_MATERIAL = 1;
	private final byte EVNT_LOCATION = 2;
	private final byte EVNT_DESCRIPT = 3;
	private final String[] columns = { "Time", "Material", "Location", "Description" };
	private ArrayList<DataEvent> events = new ArrayList<DataEvent>();
	protected AClient pj;

	ITableModelEvent(AClient parent) {
		super();
		pj = parent;
	}

	void close() {
		events.clear();
	}

	void getData(long caseID, PreparedStatement pstm) {
		String strMaterial = "";
		pj.dbAP.setLong(pstm, 1, caseID);
		ResultSet rst = pj.dbAP.getResultSet(pstm);
		try {
			events.clear();
			while (rst.next()) {
				strMaterial = rst.getString("source_rec_type").trim();
				if (strMaterial.equals("S")) {
					strMaterial = "Specimen ";
				} else if (strMaterial.equals("B")) {
					strMaterial = "Block ";
				} else if (strMaterial.equals("L")) {
					if (rst.getString("event_type").trim().toLowerCase().equals("folder_scanned")) {
						// Skip these, meaningless
						continue;
					}
					strMaterial = "Slide ";
				} else {
					strMaterial = "";
				}
				strMaterial += rst.getString("material_label").trim();
				DataEvent thisRow = new DataEvent();
				thisRow.material = strMaterial;
				thisRow.location = rst.getString("event_location");
				thisRow.description = rst.getString("event_description");
				thisRow.date.setTimeInMillis(rst.getTimestamp("event_date").getTime());
				events.add(thisRow);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, this.getClass().getName(), e);
		} finally {
			pj.dbAP.close(rst);
			fireTableDataChanged();
		}
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case EVNT_TIME:
			return Calendar.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int col) {
		return columns[col];
	}

	@Override
	public int getRowCount() {
		return events.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object value = Object.class;
		switch (col) {
		case EVNT_TIME:
			return events.get(row).date;
		case EVNT_MATERIAL:
			return events.get(row).material;
		case EVNT_LOCATION:
			return events.get(row).location;
		case EVNT_DESCRIPT:
			return events.get(row).description;
		default:
			return value;
		}
	}

	private class DataEvent {
		String material = "";
		String location = "";
		String description = "";
		Calendar date = Calendar.getInstance();
	}
}