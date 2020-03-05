package ca.powerj;
import javax.swing.table.AbstractTableModel;

class ITableModel extends AbstractTableModel {

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int col) {
		return null;
	}

	@Override
	public int getRowCount() {
		return 0;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		// Ignore, table not editable
	}
}