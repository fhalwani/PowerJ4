package ca.powerj.swing;
import java.awt.Component;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;

public class IRendererTime extends JLabel implements TableCellRenderer {
	private LibDates dates = null;

	public IRendererTime(LibDates dates) {
		super();
		this.dates = dates;
		setFont(LibConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if( value instanceof Calendar) {
			setText(dates.formatter((Calendar) value, LibDates.FORMAT_DATETIME));
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