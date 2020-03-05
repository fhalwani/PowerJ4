package ca.powerj;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

class ITreeRender extends DefaultTreeCellRenderer {

	ITreeRender() {
		super();
		setFont(LConstants.APP_FONT);
		setOpaque(true);
	}

	/**
	* TreeCellRenderer method. Overridden to update the visible row.
	*/
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
			boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
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
