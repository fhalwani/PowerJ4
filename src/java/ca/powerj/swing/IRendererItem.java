package ca.powerj.swing;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import ca.powerj.lib.LibConstants;

public class IRendererItem extends DefaultTableCellRenderer {

	public IRendererItem() {
		super();
		setFont(LibConstants.APP_FONT);
		setOpaque(true);
		setToolTipText("Click to select from a list");
	}

	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		if (value != null) {
			setText(value.toString());
		}
		if (isSelected) {
			setBackground(LibConstants.COLOR_LIGHT_BLUE);
		} else {
			setBackground(LibConstants.COLOR_EVEN_ODD[row % 2]);
		}
		return this;
	}
}