package ca.powerj;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;

public class ITreeTableSelectionModel extends DefaultTreeSelectionModel {

	public ITreeTableSelectionModel() {
        super();
        getListSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
            }
        });
    }
     
    ListSelectionModel getListSelectionModel() {
        return listSelectionModel;
    }
}
