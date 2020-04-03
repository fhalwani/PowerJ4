package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

class ZGUI implements WindowListener {
	private long timeFrom = 0;
	private long timeTo = 0;
	private final SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
	private JFrame frame = null;
	private JLabel label1, label2;

	ZGUI() {}

	static void createAndShowGUI(String[] args) {
		try {
			/* Use the System Look and Feel */
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("ToolTip.font", new FontUIResource(LConstants.APP_FONT));
		} catch (UnsupportedLookAndFeelException ignore) {
		} catch (IllegalAccessException ignore) {
		} catch (InstantiationException ignore) {
		} catch (ClassNotFoundException ignore) {
		}
		new ZGUI().init(args);
	}

	private JToolBar createToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calMin.set(Calendar.YEAR, 2011);
		calMin.set(Calendar.MONTH, Calendar.JANUARY);
		calMin.set(Calendar.DAY_OF_MONTH, 1);
		calMin.set(Calendar.HOUR_OF_DAY, 0);
		calMin.set(Calendar.MINUTE, 0);
		calMin.set(Calendar.SECOND, 0);
		calMin.set(Calendar.MILLISECOND, 0);
		calMax.set(Calendar.MONTH, Calendar.JANUARY);
		calMax.set(Calendar.DAY_OF_MONTH, 1);
		calMax.set(Calendar.HOUR_OF_DAY, 0);
		calMax.set(Calendar.MINUTE, 0);
		calMax.set(Calendar.SECOND, 0);
		calMax.set(Calendar.MILLISECOND, 0);
		timeFrom = calMin.getTimeInMillis();
		timeTo   = calMax.getTimeInMillis();
		calStart.setTimeInMillis(calMin.getTimeInMillis());
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		IComboDate cboStart = new IComboDate(calStart, calMin, calMax);
		cboStart.setName("cboStart");
		cboStart.addAncestorListener(new IFocusListener());
		cboStart.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboDate cbo = (IComboDate) e.getSource();
					getData(cbo.getValue(), null);
				}
			}
		});
		JLabel label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_F, "From:");
		label.setLabelFor(cboStart);
		toolbar.add(label);
		toolbar.add(cboStart);
		IComboDate cboEnd = new IComboDate(calEnd, calMin, calMax);
		cboEnd.setName("cboEnd");
		cboEnd.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboDate cbo = (IComboDate) e.getSource();
					getData(null, cbo.getValue());
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "To:");
		label.setLabelFor(cboEnd);
		toolbar.add(label);
		toolbar.add(cboEnd);
		return toolbar;
	}

	private void getData(Calendar calFrom, Calendar calTo) {
		if (calFrom != null) {
			timeFrom = calFrom.getTimeInMillis();
			label1.setText("From: " + formatter.format(timeFrom));
			System.out.println("From: " + timeFrom);
		} else if (calTo != null) {
			timeTo = calTo.getTimeInMillis();
			label2.setText("To: " + formatter.format(timeTo));
			System.out.println("To: " + timeTo);
		}
	}

	private void init(String[] args) {
		label1 = new JLabel("Date From: ");
		label2 = new JLabel("Date To: ");
		JPanel pnlCore = new JPanel();
		pnlCore.add(label1);
		pnlCore.add(label2);
		frame = new JFrame("TestGUI");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
		frame.add(createToolbar(), BorderLayout.NORTH);
		frame.add(pnlCore, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		final String[] sargs = args;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI(sargs);
			}
		});
	}

	private void quit() {
		frame.dispose();
		System.exit(0);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		quit();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}
