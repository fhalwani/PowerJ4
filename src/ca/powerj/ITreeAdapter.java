package ca.powerj;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

class ITreeAdapter extends AbstractTableModel {
	JTree tree;
	ITreeModel treeModel;

	ITreeAdapter(ITreeModel treeModel, JTree tree) {
		this.tree = tree;
		this.treeModel = treeModel;
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			// Don't use fireTableRowsInserted() here; 
			// the selection model would get updated twice.
			public void treeExpanded(TreeExpansionEvent event) {  
				fireTableDataChanged(); 
			}

			public void treeCollapsed(TreeExpansionEvent event) {  
				fireTableDataChanged(); 
			}
		});
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return treeModel.getColumnClass(column);
	}

	@Override
	public int getColumnCount() {
		return treeModel.getColumnCount();
	}

	@Override
	public String getColumnName(int column) {
		return treeModel.getColumnName(column);
	}

	@Override
	public int getRowCount() {
		return tree.getRowCount();
	}

	@Override
	public Object getValueAt(int row, int column) {
		return treeModel.getValueAt(nodeForRow(row), column);
	}

	protected Object nodeForRow(int row) {
		TreePath treePath = tree.getPathForRow(row);
		return treePath.getLastPathComponent();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return treeModel.isCellEditable(nodeForRow(row), column);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		treeModel.setValueAt(value, nodeForRow(row), column);
	}
}