package ca.powerj;
import java.awt.Component;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class IRenderDate extends JLabel implements TableCellRenderer {
	private LDates dates;

	IRenderDate(LBase parent) {
		super();
		this.dates = parent.dates;
		setFont(LConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if( value instanceof Date) {
			setText(dates.formatter((Date) value, LDates.FORMAT_DATE));
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