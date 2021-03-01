package ca.powerj.swing;
import java.util.ArrayList;
import java.util.Calendar;
import ca.powerj.data.ItemData;
import ca.powerj.data.SpecimenData;

public class ITableModelSpecimen extends ITableModel {
	public static final byte SPEC_LABEL = 0;
	public static final byte SPEC_COLLECTE = 1;
	public static final byte SPEC_RECEIVED = 2;
	public static final byte SPEC_CODE = 3;
	public static final byte SPEC_DESCRIPT = 4;
	private final String[] columns = { "Label", "Collected", "Received", "Code", "Description" };
	private ArrayList<SpecimenData> specimens = new ArrayList<SpecimenData>();

	public ITableModelSpecimen() {
		super();
	}

	public void close() {
		specimens.clear();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case SPEC_LABEL:
			return Byte.class;
		case SPEC_CODE:
			return ItemData.class;
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

	public short getMasterID(int row) {
		return specimens.get(row).getMasterID();
	}

	@Override
	public int getRowCount() {
		return specimens.size();
	}

	public long getSpecID(int row) {
		return specimens.get(row).getSpecID();
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case SPEC_LABEL:
			return specimens.get(row).getLabel();
		case SPEC_CODE:
			return specimens.get(row).getMaster();
		case SPEC_DESCRIPT:
			return specimens.get(row).getDescr();
		case SPEC_COLLECTE:
			return specimens.get(row).getCollected();
		case SPEC_RECEIVED:
			return specimens.get(row).getReceived();
		default:
			return Object.class;
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// Only Staff name is editable if connected to update in PowerPath
		return (col == SPEC_CODE);
	}

	public void setData(ArrayList<SpecimenData> specimens) {
		this.specimens = specimens;
		fireTableDataChanged();
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (value instanceof ItemData) {
			ItemData item = (ItemData) value;
			specimens.get(row).getMaster().setID(item.getID());
			specimens.get(row).getMaster().setName(item.getName());
			fireTableCellUpdated(row, col);
		}
	}
}