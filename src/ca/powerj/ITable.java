package ca.powerj;
import java.awt.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

class ITable extends JTable {

	ITable(AClient pj, TableModel tm) {
		super(tm);
		setRowHeight(24);
		setShowGrid(true);
		setAutoCreateRowSorter(true);
		setFillsViewportHeight(true);
		setCellSelectionEnabled(true);
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setFont(LConstants.APP_FONT);
		getTableHeader().setFont(LConstants.APP_FONT);
		// Render background color in alternating rows
		setDefaultRenderer(String.class, new IRenderString());
		setDefaultRenderer(Long.class, new IRenderInteger(pj));
		setDefaultRenderer(Integer.class, new IRenderInteger(pj));
		setDefaultRenderer(Short.class, new IRenderInteger(pj));
		setDefaultRenderer(Byte.class, new IRenderInteger(pj));
		setDefaultRenderer(Double.class, new IRenderDouble(pj, 3));
		setDefaultRenderer(Boolean.class, new IRenderBoolean());
		setDefaultRenderer(Date.class, new IRenderDate(pj));
		setDefaultRenderer(Calendar.class, new IRenderDateTime(pj));
		setDefaultRenderer(OItem.class, new IRenderItem());
		setDefaultRenderer(Object.class, new IRenderString());
	}
	
	public boolean editCellAt(int row, int column, EventObject evnt) {
		boolean result = super.editCellAt(row, column, evnt);
		final Component editor = getEditorComponent();
		if (editor != null && editor instanceof JTextComponent) {
			((JTextComponent) editor).selectAll();
			((JTextComponent) editor).setFont(LConstants.APP_FONT);
		}
		return result;
	}
}