package ca.powerj;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class IRenderDouble extends JLabel implements TableCellRenderer {
	int noFractions = 0;
	private LNumbers numbers;

    IRenderDouble(LBase parent, int noFractions) {
		super();
		this.numbers = parent.numbers;
		this.noFractions = noFractions;
		setFont(LConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
    }

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		try {
			if (value instanceof Double) {
				if ((Double) value != 0.0) {
					setText(numbers.formatDouble(noFractions, (Double) value));
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