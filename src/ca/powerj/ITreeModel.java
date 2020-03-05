package ca.powerj;
import javax.swing.tree.TreeModel;

public interface ITreeModel extends TreeModel {

	public int getColumnCount();

	public String getColumnName(int column);

	public Class<?> getColumnClass(int column);

	public Object getValueAt(Object node, int column);

	public boolean isCellEditable(Object node, int column);

	public void setValueAt(Object aValue, Object node, int column);
}