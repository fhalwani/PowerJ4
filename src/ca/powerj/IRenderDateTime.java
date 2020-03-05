package ca.powerj;
import java.awt.Component;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class IRenderDateTime extends JLabel implements TableCellRenderer {
	private LDates dates;

	IRenderDateTime(LBase parent) {
		super();
		this.dates = parent.dates;
		setFont(LConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if( value instanceof Calendar) {
			setText(dates.formatter((Calendar) value, LDates.FORMAT_DATETIME));
        } else {
			setText("-");
        }
		if (isSelected) {
			setBackground(LConstants.COLOR_LIGHT_BLUE);
        } else {
			setBackground(LConstants.COLOR_EVEN_ODD[row % 2]);
        }
		return this;
	}
}