package ca.powerj;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class NBase extends JPanel implements DocumentListener, FocusListener {
	volatile boolean altered = false;
	volatile boolean programmaticChange = true;
	AClient pj;

	NBase(AClient parent) {
		super();
		this.pj = parent;
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	}

	boolean close() {
		if (altered) {
			int option = pj.askSave(getName());
			switch (option) {
			case LConstants.OPTION_YES:
				save();
				break;
			case LConstants.OPTION_NO:
				altered = false;
				break;
			default:
				// Cancel close
			}
		}
		if (!altered) {
			pj.dbPowerJ.closeStms(false);
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

	void setFilter(byte[] rows) {}

	void setFilter(short id, short value) {}

	void setFilter(short id, Calendar value) {}

	void trackDocument(DocumentEvent e) {
		if (!programmaticChange) {
			altered = true;
		}
	}

	void xls() {}
}