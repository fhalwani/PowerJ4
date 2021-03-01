package ca.powerj.swing;
import java.awt.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import ca.powerj.data.ItemData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibNumbers;

public class ITable extends JTable {

	public ITable(TableModel tm, LibDates dates, LibNumbers numbers) {
		super(tm);
		setRowHeight(24);
		setShowGrid(true);
		setAutoCreateRowSorter(true);
		setFillsViewportHeight(true);
		setCellSelectionEnabled(true);
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setFont(LibConstants.APP_FONT);
		getTableHeader().setFont(LibConstants.APP_FONT);
		// Render background color in alternating rows
		setDefaultRenderer(String.class, new IRendererString());
		setDefaultRenderer(Long.class, new IRendererInteger(numbers));
		setDefaultRenderer(Integer.class, new IRendererInteger(numbers));
		setDefaultRenderer(Short.class, new IRendererInteger(numbers));
		setDefaultRenderer(Byte.class, new IRendererInteger(numbers));
		setDefaultRenderer(Double.class, new IRendererDouble(numbers, 3));
		setDefaultRenderer(Boolean.class, new IRendererBoolean());
		setDefaultRenderer(Date.class, new IRendererDate(dates));
		setDefaultRenderer(Calendar.class, new IRendererTime(dates));
		setDefaultRenderer(ItemData.class, new IRendererItem());
		setDefaultRenderer(Object.class, new IRendererString());
	}
	
	public boolean editCellAt(int row, int column, EventObject evnt) {
		boolean result = super.editCellAt(row, column, evnt);
		final Component editor = getEditorComponent();
		if (editor != null && editor instanceof JTextComponent) {
			((JTextComponent) editor).selectAll();
			((JTextComponent) editor).setFont(LibConstants.APP_FONT);
		}
		return result;
	}

	public void changeSelection(int row, int column, boolean toggle, boolean extend) {
		super.changeSelection(row, column, toggle, extend);
		if (editCellAt(row, column)) {
			Component editor = getEditorComponent();
			editor.requestFocusInWindow();
		}
	}
}