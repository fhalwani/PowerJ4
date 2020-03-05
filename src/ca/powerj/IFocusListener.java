package ca.powerj;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

class IFocusListener implements AncestorListener {
	//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method

	public void ancestorAdded(AncestorEvent e) {
		JComponent component = e.getComponent();
		component.requestFocusInWindow();
		// The listener is only used once and then it is removed from the component
		component.removeAncestorListener(this);
	}

	public void ancestorMoved(AncestorEvent ignore) {}
	public void ancestorRemoved(AncestorEvent ignore) {}
}