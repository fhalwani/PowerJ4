package ca.powerj;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class IRenderInteger extends JLabel implements TableCellRenderer {
	private LNumbers numbers;

	IRenderInteger(LBase parent) {
		super();
		this.numbers = parent.numbers;
		setFont(LConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		try {
			if (value instanceof Integer) {
				if ((Integer) value != 0) {
					setText(numbers.formatNumber((Integer) value));
				} else {
					setText("");
				}
			} else if (value instanceof Short) {
				if ((Short) value != 0) {
					setText(numbers.formatNumber((Short) value));
				} else {
					setText("");
				}
			} else if (value instanceof Byte) {
				if ((Byte) value != 0) {
					setText(numbers.formatNumber((Byte) value));
				} else {
					setText("");
				}
			} else if (value instanceof Long) {
				if ((Long) value != 0) {
					setText(numbers.formatNumber((Long) value));
				} else {
					setText("");
				}
			} else {
				setText("");
			}
		} catch (IllegalArgumentException ignore) {}
		if (isSelected) {
			setBackground(LConstants.COLOR_LIGHT_BLUE);
        } else {
			setBackground(LConstants.COLOR_EVEN_ODD[row % 2]);
        }
		return this;
	}
}