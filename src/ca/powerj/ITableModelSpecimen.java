package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

class ITableModelSpecimen extends ITableModel {
	static final byte SPEC_LABEL = 0;
	static final byte SPEC_COLLECTE = 1;
	static final byte SPEC_RECEIVED = 2;
	static final byte SPEC_CODE = 3;
	static final byte SPEC_DESCRIPT = 4;
	private final String[] columns = { "Label", "Collected", "Received", "Code", "Description" };
	private ArrayList<DataSpecimen> specimens = new ArrayList<DataSpecimen>();
	protected AClient pj;

	ITableModelSpecimen(AClient parent) {
		super();
		pj = parent;
	}

	void close() {
		specimens.clear();
	}

	void getData(long caseID, PreparedStatement pstm) {
		pj.dbAP.setLong(pstm, 1, caseID);
		ResultSet rst = pj.dbAP.getResultSet(pstm);
		try {
			specimens.clear();
			while (rst.next()) {
				OItem item = new OItem(rst.getShort("tmplt_profile_id"), " ");
				if (rst.getString("code") != null) {
					item.name = rst.getString("code");
				}
				DataSpecimen thisRow = new DataSpecimen();
				thisRow.specID = rst.getLong("id");
				thisRow.label = rst.getByte("specimen_label");
				thisRow.descr = rst.getString("description");
				thisRow.master = item;
				thisRow.received.setTimeInMillis(rst.getTimestamp("recv_date").getTime());
				if (rst.getTimestamp("collection_date") != null) {
					thisRow.collected.setTimeInMillis(rst.getTimestamp("collection_date").getTime());
				} else {
					thisRow.collected.setTimeInMillis(rst.getTimestamp("recv_date").getTime());
				}
				specimens.add(thisRow);
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
		case SPEC_LABEL:
			return Byte.class;
		case SPEC_CODE:
			return OItem.class;
		case SPEC_DESCRIPT:
			return String.class;
		default:
			return Calendar.class;
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

	short getMasterID(int row) {
		return specimens.get(row).master.id;
	}

	@Override
	public int getRowCount() {
		return specimens.size();
	}

	long getSpecID(int row) {
		return specimens.get(row).specID;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object value = Object.class;
		switch (col) {
		case SPEC_LABEL:
			return specimens.get(row).label;
		case SPEC_CODE:
			return specimens.get(row).master;
		case SPEC_DESCRIPT:
			return specimens.get(row).descr;
		case SPEC_COLLECTE:
			return specimens.get(row).collected;
		case SPEC_RECEIVED:
			return specimens.get(row).received;
		default:
			return value;
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// Only Staff name is editable if connected to update in PowerPath
		return (col == SPEC_CODE);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (value instanceof OItem) {
			OItem item = (OItem) value;
			specimens.get(row).master.id = item.id;
			specimens.get(row).master.name = item.name;
			fireTableCellUpdated(row, col);
		}
	}

	private class DataSpecimen {
		byte label = 0;
		long specID = 0;
		String descr = "";
		OItem master = new OItem();
		Calendar collected = Calendar.getInstance();
		Calendar received = Calendar.getInstance();
	}
}