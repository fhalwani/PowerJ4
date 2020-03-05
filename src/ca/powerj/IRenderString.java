package ca.powerj;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class IRenderString extends DefaultTableCellRenderer {

    IRenderString() {
		super();
		setFont(LConstants.APP_FONT);
		setOpaque(true);
    }

    public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		if (value instanceof String) {
	    	setText((String)value);
		} else if (value != null) {
	    	setText(value.toString());
		}
		if (isSelected) {
			setBackground(LConstants.COLOR_LIGHT_BLUE);
        } else {
			setBackground(LConstants.COLOR_EVEN_ODD[row % 2]);
        }
		return this;
	}
}