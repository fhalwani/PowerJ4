package ca.powerj;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

class ITreeTableModel implements ITreeModel {
	protected EventListenerList listenerList = new EventListenerList();
	protected Object root;     

	public ITreeTableModel(Object root) {
		this.root = root; 
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	protected void fireTreeNodesChanged(Object source, Object[] path, 
			int[] childIndices, Object[] children) {
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TreeModelListener.class) {
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
			}          
		}
	}

	protected void fireTreeNodesRemoved(Object source, Object[] path,
			int[] childIndices, Object[] children) {
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TreeModelListener.class) {
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
			}          
		}
	}

	protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		for (int i = listeners.length-2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
			}          
		}
	}

	@Override
	public Object getChild(Object arg0, int arg1) {
		return null;
	}

	@Override
	public int getChildCount(Object arg0) {
		return 0;
	}

	protected Object[] getChildren(Object node) {
		return null;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return Object.class;
	}

	@Override
	public int getColumnCount() {
		return 0;
	}

	@Override
	public String getColumnName(int column) {
		return null;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		for (int i = 0; i < getChildCount(parent); i++) {
			if (getChild(parent, i).equals(child)) { 
				return i; 
			}
		}
		return -1; 
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getValueAt(Object node, int column) {
		return null;
	}

	@Override
	public boolean isCellEditable(Object node, int column) { 
		return getColumnClass(column) == ITreeModel.class; 
	}

	@Override
	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0; 
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	@Override
	public void setValueAt(Object aValue, Object node, int column) {}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {}
}