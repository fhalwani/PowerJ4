package ca.powerj;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class IRenderItem extends DefaultTableCellRenderer {

	IRenderItem() {
		super();
		setFont(LConstants.APP_FONT);
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
    		setBackground(LConstants.COLOR_LIGHT_BLUE);
    	} else {
			setBackground(LConstants.COLOR_EVEN_ODD[row % 2]);
    	}
    	return this;
	}
}