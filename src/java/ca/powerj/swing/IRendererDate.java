package ca.powerj.swing;

import java.awt.Component;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;

public class IRendererDate extends JLabel implements TableCellRenderer {
	private LibDates dates = null;

	public IRendererDate(LibDates dates) {
		super();
		this.dates = dates;
		setFont(LibConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if( value instanceof Date) {
			setText(dates.formatter((Date) value, LibDates.FORMAT_DATE));
		} else {
			setText("-");
		}
		if (isSelected) {
			setBackground(LibConstants.COLOR_LIGHT_BLUE);
		} else {
			setBackground(LibConstants.COLOR_EVEN_ODD[row % 2]);
		}
		return this;
	}
}