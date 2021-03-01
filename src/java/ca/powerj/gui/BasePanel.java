package ca.powerj.gui;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IDialog;

public class BasePanel extends JPanel implements DocumentListener, FocusListener {
	volatile boolean altered = false;
	volatile boolean programmaticChange = true;
	AppFrame application;

	public BasePanel(AppFrame application) {
		super();
		this.application = application;
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	}

	int askSave(String name) {
		return new IDialog(application.frame, name).getChoice();
	}

	public boolean close() {
		if (altered) {
			int option = askSave(getName());
			switch (option) {
			case LibConstants.OPTION_YES:
				save();
				break;
			case LibConstants.OPTION_NO:
				altered = false;
				break;
			default:
				// Cancel close
			}
		}
		if (!altered) {
			if (application.dbPowerJ != null) {
				application.dbPowerJ.closeStms();
			}
		}
		return !altered;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		trackDocument(e);
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (altered) {
			save();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		trackDocument(e);
	}

	void pdf() {}
	void refresh() {}

	@Override
	public void removeUpdate(DocumentEvent e) {
		trackDocument(e);
	}

	void save() {}

	public void setFilter(int[] values) {}

	public void setFilter(short id, int value) {}

	public void setFilter(short id, Calendar value) {}

	void trackDocument(DocumentEvent e) {
		if (!programmaticChange) {
			altered = true;
		}
	}

	void xls() {}
}