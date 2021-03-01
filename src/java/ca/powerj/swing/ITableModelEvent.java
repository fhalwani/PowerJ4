package ca.powerj.swing;
import java.util.ArrayList;
import java.util.Calendar;
import ca.powerj.data.EventData;

public class ITableModelEvent extends ITableModel {
	private final byte EVNT_TIME = 0;
	private final byte EVNT_MATERIAL = 1;
	private final byte EVNT_LOCATION = 2;
	private final byte EVNT_DESCRIPT = 3;
	private final String[] columns = { "Time", "Material", "Location", "Description" };
	private ArrayList<EventData> events = new ArrayList<EventData>();

	public ITableModelEvent() {
		super();
	}

	public void close() {
		events.clear();
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
		switch (col) {
		case EVNT_TIME:
			return events.get(row).getTime();
		case EVNT_MATERIAL:
			return events.get(row).getMaterial();
		case EVNT_LOCATION:
			return events.get(row).getLocation();
		case EVNT_DESCRIPT:
			return events.get(row).getDescription();
		default:
			return Object.class;
		}
	}

	public void setData(ArrayList<EventData> events) {
		this.events = events;
		fireTableDataChanged();
	}
}