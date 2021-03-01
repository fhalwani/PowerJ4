package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import ca.powerj.database.DBPath;
import ca.powerj.database.DBPowerj;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDefaults;
import ca.powerj.swing.IDialog;
import ca.powerj.swing.IStatusbar;
import ca.powerj.swing.IUtilities;

public class AppFrame extends LibBase implements WindowListener {
	public byte panelID = 0;
	public BasePanel panel = null;
	protected boolean[] userAccess = new boolean[32];
	protected JFrame frame = null;
	private IStatusbar statusBar = null;
	private Hashtable<Byte, Action> actions = null;
	public DBPowerj dbPowerJ = null;
	public DBPath dbPath = null;

	public AppFrame() {
		super();
		for (byte i = 0; i < 32; i++) {
			userAccess[i] = false;
		}
	}

	private void about() {
		new IDialog(frame, JOptionPane.PLAIN_MESSAGE, "About " + LibConstants.APP_NAME, null);
	}

	@Override
	public void close() {
		if (defaults != null) {
			if (frame != null) {
				if (frame.isVisible()) {
					Point location = frame.getLocationOnScreen();
					if (location.x >= 0 && location.y >= 0) {
						defaults.setInt("mainx", location.x);
						defaults.setInt("mainy", location.y);
					}
					Dimension size = frame.getSize();
					if (size.width >= 600 && size.height >= 100) {
						defaults.setInt("mainw", size.width);
						defaults.setInt("mainh", size.height);
					}
				}
				frame.dispose();
			}
			defaults.close();
		}
		super.close();
	}

	protected boolean closePanel() {
		if (panel != null) {
			if (panel.close()) {
				Dimension dim = frame.getSize();
				defaults.setInt("pw" + panelID, dim.width);
				defaults.setInt("ph" + panelID, dim.height);
				statusBar.clear();
				frame.remove(panel);
				frame.setTitle(LibConstants.APP_NAME);
				panelID = 0;
				panel = null;
				frame.invalidate();
				frame.pack();
				dim = new Dimension(defaults.getInt("mainw", 600), defaults.getInt("mainh", 100));
				if (dim.width > 500 && dim.height > 90) {
					frame.setSize(dim);
				}
			} else {
				// Error saving
				return false;
			}
		}
		return true;
	}

	private void createFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("ToolTip.font", new FontUIResource(LibConstants.APP_FONT));
		} catch (UnsupportedLookAndFeelException ignore) {
		} catch (IllegalAccessException ignore) {
		} catch (InstantiationException ignore) {
		} catch (ClassNotFoundException ignore) {
		}
		setActions();
		statusBar = new IStatusbar();
		frame = new JFrame(LibConstants.APP_NAME);
		frame.setName(LibConstants.APP_NAME);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLocationRelativeTo(null); // for double monitors
		frame.setIconImage(IUtilities.getImage(48, LibConstants.APP_NAME));
		frame.setLayout(new BorderLayout());
		frame.add(statusBar, BorderLayout.SOUTH);
		frame.addWindowListener(this);
		frame.setJMenuBar(setMenu());
		frame.add(setToolBar(), BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);
		Point location = frame.getLocationOnScreen();
		location.x = defaults.getInt("mainx", 0);
		location.y = defaults.getInt("mainy", 0);
		if (location.x > -1 && location.y > -1) {
			frame.setLocation(location);
		}
		Dimension defaultSize = frame.getPreferredSize();
		defaultSize.width = defaults.getInt("mainw", defaultSize.width);
		defaultSize.height = defaults.getInt("mainh", defaultSize.height);
		if (defaultSize.width > 50 && defaultSize.height > 50) {
			frame.setSize(defaultSize);
		}
		if ((getUserID() == -222 || getUserID() == -111) && isAutologin()) {
			setView(LibConstants.ACTION_BACKLOG);
		}
	}

	public void display(byte severity, String name, String message) {
		if (severity > LibConstants.ERROR_NONE) {
			new IDialog(frame, JOptionPane.ERROR_MESSAGE, name, message);
		} else if (statusBar != null) {
			statusBar.setMessage(message);
		}
	}

	public void display(String message) {
		statusBar.setMessage(message);
	}

	private String getFilePath(String def, FileNameExtensionFilter filter) {
		String fileName = "";
		final JFileChooser fc = new JFileChooser();
		try {
			String dataDir = defaults.getString("datadir", System.getProperty("user.home"));
			if (!dataDir.endsWith(System.getProperty("file.separator"))) {
				dataDir += System.getProperty("file.separator");
			}
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setFileFilter(filter);
			fc.setSelectedFile(new File(dataDir + def));
			int val = fc.showOpenDialog(frame);
			if (val == JFileChooser.APPROVE_OPTION) {
				fileName = fc.getSelectedFile().getAbsolutePath();
				if (!dataDir.equals(fc.getCurrentDirectory().getAbsolutePath())) {
					dataDir = fc.getCurrentDirectory().getAbsolutePath();
					defaults.setString("datadir", dataDir);
				}
			}
		} catch (HeadlessException ignore) {
		}
		return fileName;
	}

	String getFilePdf(String def) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF files", "pdf");
		String fileName = getFilePath(def, filter);
		if (fileName.trim().length() > 2) {
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				String extension = fileName.substring(i + 1).trim().toLowerCase();
				if (!extension.equals("pdf")) {
					fileName = fileName.substring(0, i).trim() + ".pdf";
				}
			} else {
				fileName += ".pdf";
			}
		}
		File file = new File(fileName);
		if (file.exists()) {
			String message = String.format("A file named '%s' already exists. Do you want to replace it?", fileName);
			String[] choices = { "Cancel", "Replace" };
			int[] mnemonics = { KeyEvent.VK_C, KeyEvent.VK_R };
			IDialog dialog = new IDialog(frame, JOptionPane.QUESTION_MESSAGE, "Export", message, choices, mnemonics, 0);
			if (dialog.getChoice() == 0) {
				fileName = "";
			}
		}
		return fileName;
	}

	String getFileXls(String def) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XLS files", "xls");
		String fileName = getFilePath(def, filter);
		if (fileName.trim().length() > 2) {
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				String extension = fileName.substring(i + 1).trim().toLowerCase();
				if (!extension.equals("xls")) {
					fileName = fileName.substring(0, i).trim() + ".xls";
				}
			} else {
				fileName += ".xls";
			}
		}
		File file = new File(fileName);
		if (file.exists()) {
			String message = String.format("A file named '%s' already exists. Do you want to replace it?", fileName);
			String[] choices = { "Cancel", "Replace" };
			int[] mnemonics = { KeyEvent.VK_C, KeyEvent.VK_R };
			IDialog dialog = new IDialog(frame, JOptionPane.QUESTION_MESSAGE, "Export", message, choices, mnemonics, 0);
			if (dialog.getChoice() == 0) {
				fileName = "";
			}
		}
		return fileName;
	}

	private void help() {
		HelpPanel dialog = new HelpPanel(this);
		dialog.setVisible(true);
	}

	@Override
	protected void init(String[] args) {
		super.init(args);
		if (errorID == LibConstants.ERROR_NONE) {
			dbPowerJ = getDBPJ();
		}
		if (errorID == LibConstants.ERROR_NONE) {
			// Open preferences file
			defaults = new LibDefaults(getProperty("pjDir"));
		}
		if (errorID != LibConstants.ERROR_NONE) {
			quit();
		} else if (isOffline() && getUserID() == 1) {
			// Debug mode
			for (byte i = 0; i < 32; i++) {
				userAccess[i] = true;
			}
		} else if (!isAutologin()) {
			LoginPanel login = new LoginPanel(this, frame);
			if (errorID == LibConstants.ERROR_NONE) {
				if (login.cancel) {
					errorID = LibConstants.ERROR_ACCESS;
					new IDialog(frame, JOptionPane.WARNING_MESSAGE, LibConstants.APP_NAME, "Access denied.");
					quit();
				} else {
					short userID = login.getLoginID();
					if (userID == 0) {
						errorID = LibConstants.ERROR_ACCESS;
						new IDialog(frame, JOptionPane.WARNING_MESSAGE, LibConstants.APP_NAME, "Access denied.");
						quit();
					} else if (!setLogin(userID)) {
						errorID = LibConstants.ERROR_ACCESS;
						new IDialog(frame, JOptionPane.WARNING_MESSAGE, LibConstants.APP_NAME, "Access denied.");
						quit();
					} else {
						setUserID(userID);
					}
				}
				// Kill Dialog
				login.dispose();
			}
		}
		if (errorID == LibConstants.ERROR_NONE) {
			if (getUserID() == -222 && isAutologin()) {
				userAccess[LibConstants.ACCESS_GROSS] = true;
				userAccess[LibConstants.ACCESS_SCHED] = true;
			} else if (getUserID() == -111 && isAutologin()) {
				userAccess[LibConstants.ACCESS_HISTO] = true;
				userAccess[LibConstants.ACCESS_EMBED] = true;
				userAccess[LibConstants.ACCESS_ROUTE] = true;
				userAccess[LibConstants.ACCESS_SCHED] = true;
			}
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					createFrame();
				}
			});
		}
	}

	@Override
	public void log(byte severity, String message) {
		super.log(severity, message);
		display(severity, LibConstants.APP_NAME, message);
	}

	@Override
	public void log(byte severity, String name, String message) {
		super.log(severity, name, message);
		display(severity, name, message);
	}

	@Override
	public void log(byte severity, String name, Throwable e) {
		super.log(severity, name, e);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		display(severity, name, sw.toString());
	}

	void quit() {
		if (closePanel()) {
			setStopping();
		}
	}

	protected void refreshPanel() {
		if (panel != null) {
			panel.refresh();
		}
	}

	private void setActions() {
		if (errorID == LibConstants.ERROR_NONE) {
			actions = new Hashtable<Byte, Action>();
			actions.put(LibConstants.ACTION_ABOUT, new ActionAbout());
			actions.put(LibConstants.ACTION_CLOSE, new ActionClose());
			actions.put(LibConstants.ACTION_HELP, new ActionHelp());
			actions.put(LibConstants.ACTION_QUIT, new ActionQuit());
			if (userAccess[LibConstants.ACCESS_EX_PDF]) {
				actions.put(LibConstants.ACTION_PDF, new ActionPdf());
			}
			if (userAccess[LibConstants.ACCESS_EX_XLS]) {
				actions.put(LibConstants.ACTION_EXCEL, new ActionExcel());
			}
			if (userAccess[LibConstants.ACCESS_GROSS] || userAccess[LibConstants.ACCESS_HISTO]
					|| userAccess[LibConstants.ACCESS_DIAGN]) {
				actions.put(LibConstants.ACTION_BACKLOG,
						new ActionView(LibConstants.ACTION_BACKLOG, KeyEvent.VK_B, "Backlog", "Backlog Report"));
			}
			if (userAccess[LibConstants.ACCESS_DAILY]) {
				actions.put(LibConstants.ACTION_DAILY,
						new ActionView(LibConstants.ACTION_DAILY, KeyEvent.VK_D, "Daily", "Daily Workflow Report"));
			}
			if (userAccess[LibConstants.ACCESS_DISTR]) {
				actions.put(LibConstants.ACTION_DISTRIBUTE, new ActionView(LibConstants.ACTION_DISTRIBUTE, KeyEvent.VK_I,
						"Distribution", "Distribution Report"));
			}
			if (userAccess[LibConstants.ACCESS_FOREC]) {
				actions.put(LibConstants.ACTION_FORECAST,
						new ActionView(LibConstants.ACTION_FORECAST, KeyEvent.VK_O, "Forecast", "Annual Forecast Report"));
			}
			if (userAccess[LibConstants.ACCESS_EMBED]) {
				actions.put(LibConstants.ACTION_HISTOLOGY,
						new ActionView(LibConstants.ACTION_HISTOLOGY, KeyEvent.VK_H, "Histology", "Histology Report"));
			}
			if (userAccess[LibConstants.ACCESS_ROUTE]) {
				actions.put(LibConstants.ACTION_ROUTING,
						new ActionView(LibConstants.ACTION_ROUTING, KeyEvent.VK_R, "Routing", "Routing Report"));
			}
			if (userAccess[LibConstants.ACCESS_SCHED]) {
				actions.put(LibConstants.ACTION_SCHEDULE,
						new ActionView(LibConstants.ACTION_SCHEDULE, KeyEvent.VK_S, "Schedule", "Schedule Report"));
			}
			if (userAccess[LibConstants.ACCESS_SPECI]) {
				actions.put(LibConstants.ACTION_SPECIMEN,
						new ActionView(LibConstants.ACTION_SPECIMEN, KeyEvent.VK_P, "Specimens", "Specimens Report"));
			}
			if (userAccess[LibConstants.ACCESS_TURNA]) {
				actions.put(LibConstants.ACTION_TURNAROUND, new ActionView(LibConstants.ACTION_TURNAROUND, KeyEvent.VK_T,
						"Turnaround", "Turnaround Time Report"));
			}
			if (userAccess[LibConstants.ACCESS_WORKD]) {
				actions.put(LibConstants.ACTION_WORKDAYS,
						new ActionView(LibConstants.ACTION_WORKDAYS, KeyEvent.VK_O, "Workdays", "Workdays Report"));
			}
			if (userAccess[LibConstants.ACCESS_WORKL]) {
				actions.put(LibConstants.ACTION_WORKLOAD,
						new ActionView(LibConstants.ACTION_WORKLOAD, KeyEvent.VK_W, "Workload", "Workload Report"));
			}
			if (userAccess[LibConstants.ACCESS_AU_EDT]) {
				actions.put(LibConstants.ACTION_EDITOR,
						new ActionView(LibConstants.ACTION_EDITOR, KeyEvent.VK_E, "Editor", "Specimen Editor"));
			}
			if (userAccess[LibConstants.ACCESS_AU_ERR]) {
				actions.put(LibConstants.ACTION_ERROR,
						new ActionView(LibConstants.ACTION_ERROR, KeyEvent.VK_E, "Errors", "Errors auditor"));
			}
			if (userAccess[LibConstants.ACCESS_AU_FNL]) {
				actions.put(LibConstants.ACTION_FINALS,
						new ActionView(LibConstants.ACTION_FINALS, KeyEvent.VK_F, "Finals", "Final Cases Auditor"));
			}
			if (userAccess[LibConstants.ACCESS_AU_PNP]) {
				actions.put(LibConstants.ACTION_PENDING,
						new ActionView(LibConstants.ACTION_PENDING, KeyEvent.VK_P, "Pending", "Pending Cases Auditor"));
			}
			if (userAccess[LibConstants.ACCESS_STP_PJ]) {
				actions.put(LibConstants.ACTION_CODER1,
						new ActionView(LibConstants.ACTION_CODER1, 0, getProperty("coder1"), getProperty("coder1") + " Setup", "coder1"));
				actions.put(LibConstants.ACTION_CODER2,
						new ActionView(LibConstants.ACTION_CODER2, 0, getProperty("coder2"), getProperty("coder2") + " Setup", "coder2"));
				actions.put(LibConstants.ACTION_CODER3,
						new ActionView(LibConstants.ACTION_CODER3, 0, getProperty("coder3"), getProperty("coder3") + " Setup", "coder3"));
				actions.put(LibConstants.ACTION_CODER4,
						new ActionView(LibConstants.ACTION_CODER4, 0, getProperty("coder4"), getProperty("coder4") + " Setup", "coder4"));
				actions.put(LibConstants.ACTION_ORDERGROUP, new ActionView(LibConstants.ACTION_ORDERGROUP, KeyEvent.VK_G,
						"Orders", "Order Groups Setup", "ordergroups"));
				actions.put(LibConstants.ACTION_PROCEDURES,
						new ActionView(LibConstants.ACTION_PROCEDURES, KeyEvent.VK_R, "Procedures", "Procedures Setup"));
				actions.put(LibConstants.ACTION_RULES,
						new ActionView(LibConstants.ACTION_RULES, KeyEvent.VK_U, "Rules", "Rules Setup"));
				actions.put(LibConstants.ACTION_SETUP,
						new ActionView(LibConstants.ACTION_SETUP, KeyEvent.VK_J, "PowerJ", "PowerJ Setup"));
				actions.put(LibConstants.ACTION_SPECGROUP, new ActionView(LibConstants.ACTION_SPECGROUP, KeyEvent.VK_C,
						"Specimens", "Specimens Groups Setup", "specgroups"));
				actions.put(LibConstants.ACTION_SPECIALTY,
						new ActionView(LibConstants.ACTION_SPECIALTY, KeyEvent.VK_Y, "Specialties", "Specialties Setup"));
				actions.put(LibConstants.ACTION_SUBSPECIAL, new ActionView(LibConstants.ACTION_SUBSPECIAL, KeyEvent.VK_B,
						"Subspecialties", "Subspecialties Setup"));
			}
			if (userAccess[LibConstants.ACCESS_STP_PP]) {
				actions.put(LibConstants.ACTION_ACCESSION,
						new ActionView(LibConstants.ACTION_ACCESSION, 0, "Accessions", "Accessions Setup"));
				actions.put(LibConstants.ACTION_FACILITY,
						new ActionView(LibConstants.ACTION_FACILITY, KeyEvent.VK_F, "Facilities", "Facilities Setup"));
				actions.put(LibConstants.ACTION_ORDERMASTER, new ActionView(LibConstants.ACTION_ORDERMASTER, KeyEvent.VK_O,
						"Orders", "Orders Master Setup", "ordermaster"));
				actions.put(LibConstants.ACTION_SPECMASTER, new ActionView(LibConstants.ACTION_SPECMASTER, KeyEvent.VK_S,
						"Specimens", "Specimens Master Setup", "specmaster"));
				actions.put(LibConstants.ACTION_TURNMASTER,
						new ActionView(LibConstants.ACTION_TURNMASTER, KeyEvent.VK_T, "Turnaround", "Turnaround Setup"));
			}
			if (userAccess[LibConstants.ACCESS_STP_PR]) {
				actions.put(LibConstants.ACTION_PERSONNEL,
						new ActionView(LibConstants.ACTION_PERSONNEL, KeyEvent.VK_P, "Persons", "Persons Setup"));
			}
			if (userAccess[LibConstants.ACCESS_STP_SC]) {
				actions.put(LibConstants.ACTION_SERVICES, new ActionView(LibConstants.ACTION_SERVICES, KeyEvent.VK_V,
						"Services", "Services Setup", "schedule"));
			}
			if (userAccess[LibConstants.ACCESS_STP_TR]) {
				actions.put(LibConstants.ACTION_TURNMASTER, new ActionView(LibConstants.ACTION_TURNMASTER, KeyEvent.VK_T,
						"Turnaround", "Turnaround Time Setup"));
			}
		}
	}

	@Override
	public void setBusy(boolean value) {
		super.setBusy(value);
		if (isBusy()) {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			statusBar.setMessage("Updating data, do not interrupt...");
			statusBar.setProgress(-1);
		} else {
			frame.setCursor(Cursor.getDefaultCursor());
			statusBar.setProgress(100);
		}
	}

	/** Check login access to PowerJ & set userAccess value **/
	private boolean setLogin(short userID) {
		int i = dbPowerJ.getLoginAccess(userID);
		userAccess = numbers.intToBoolean(i);
		return (i != 0);
	}

	private JMenuBar setMenu() {
		byte noItems = 0;
		byte noSubs = 0;
		JMenuBar menuBar = new JMenuBar();
		// File Menu
		JMenu mainItem = new JMenu("File");
		menuBar.add(mainItem);
		mainItem.setMnemonic(KeyEvent.VK_F);
		JMenuItem menuItem = new JMenuItem(actions.get(LibConstants.ACTION_CLOSE));
		mainItem.add(menuItem);
		// Export section
		Action action = actions.get(LibConstants.ACTION_PDF);
		if (action != null) {
			mainItem.addSeparator();
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
		}
		action = actions.get(LibConstants.ACTION_EXCEL);
		if (action != null) {
			mainItem.addSeparator();
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
		}
		mainItem.addSeparator();
		menuItem = new JMenuItem(actions.get(LibConstants.ACTION_QUIT));
		mainItem.add(menuItem);
		// Audit section
		noItems = 0;
		mainItem = new JMenu("Audit");
		mainItem.setMnemonic(KeyEvent.VK_A);
		action = actions.get(LibConstants.ACTION_EDITOR);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_ERROR);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_FINALS);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_PENDING);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		if (noItems > 0) {
			menuBar.add(mainItem);
		}
		// Reports section
		noItems = 0;
		mainItem = new JMenu("Reports");
		mainItem.setMnemonic(KeyEvent.VK_R);
		action = actions.get(LibConstants.ACTION_BACKLOG);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_DAILY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_DISTRIBUTE);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_FORECAST);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_HISTOLOGY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_ROUTING);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_SCHEDULE);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_SPECIMEN);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_TURNAROUND);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_WORKDAYS);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_WORKLOAD);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		if (noItems > 0) {
			menuBar.add(mainItem);
		}
		// Setup section
		mainItem = new JMenu("Setup");
		mainItem.setMnemonic(KeyEvent.VK_S);
		JMenu subMenu = new JMenu(getProperty("apArch") + "...");
		noItems = 0;
		noSubs = 0;
		action = actions.get(LibConstants.ACTION_ACCESSION);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_FACILITY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_ORDERMASTER);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_PERSONNEL);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_SPECMASTER);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		if (noSubs > 0) {
			mainItem.add(subMenu);
			noItems++;
			noSubs = 0;
		}
		subMenu = new JMenu("PowerJ...");
		subMenu.setMnemonic(KeyEvent.VK_J);
		action = actions.get(LibConstants.ACTION_CODER1);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_CODER2);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_CODER3);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_CODER4);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_ORDERGROUP);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_PROCEDURES);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_RULES);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_SPECGROUP);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_SPECIALTY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_SUBSPECIAL);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_TURNMASTER);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_SETUP);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		if (noSubs > 0) {
			mainItem.add(subMenu);
			noItems++;
			noSubs = 0;
		}
		subMenu = new JMenu("Schedule...");
		subMenu.setMnemonic(KeyEvent.VK_C);
		action = actions.get(LibConstants.ACTION_SERVICES);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		if (noSubs > 0) {
			mainItem.add(subMenu);
			noItems++;
			noSubs = 0;
		}
		subMenu = new JMenu("Data...");
		subMenu.setMnemonic(KeyEvent.VK_D);
		action = actions.get(LibConstants.ACTION_BACKUP);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LibConstants.ACTION_RESTORE);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		if (noSubs > 0) {
			mainItem.add(subMenu);
			noItems++;
			noSubs = 0;
		}

		if (noItems > 0) {
			menuBar.add(mainItem);
		}
		// Help section
		mainItem = new JMenu("Help");
		mainItem.setMnemonic(KeyEvent.VK_H);
		menuItem = new JMenuItem(actions.get(LibConstants.ACTION_ABOUT));
		mainItem.add(menuItem);
		menuItem = new JMenuItem(actions.get(LibConstants.ACTION_HELP));
		mainItem.add(menuItem);
		menuBar.add(mainItem);
		return menuBar;
	}

	private JToolBar setToolBar() {
		boolean needsSeparator = false;
		byte noItems = 0;
		JToolBar toolBar = new JToolBar();
		// Main section
		Action action = actions.get(LibConstants.ACTION_BACKLOG);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_DAILY);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_DISTRIBUTE);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_FORECAST);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_HISTOLOGY);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_ROUTING);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_SCHEDULE);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_SPECIMEN);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_TURNAROUND);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_WORKDAYS);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_WORKLOAD);
		if (action != null) {
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		// Auditor section
		needsSeparator = true;
		action = actions.get(LibConstants.ACTION_ERROR);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_FINALS);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_PENDING);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_EDITOR);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		// Export section
		needsSeparator = true;
		action = actions.get(LibConstants.ACTION_PDF);
		if (action != null) {
			toolBar.addSeparator();
			needsSeparator = false;
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		action = actions.get(LibConstants.ACTION_EXCEL);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IUtilities.createJButton(action));
			noItems++;
		}
		// Utilities section
		if (noItems > 1) {
			toolBar.addSeparator();
		}
		toolBar.add(IUtilities.createJButton(actions.get(LibConstants.ACTION_HELP)));
		toolBar.add(IUtilities.createJButton(actions.get(LibConstants.ACTION_ABOUT)));
		toolBar.add(IUtilities.createJButton(actions.get(LibConstants.ACTION_CLOSE)));
		return toolBar;
	}

	private void setView(byte panelid) {
		errorID = LibConstants.ERROR_NONE;
		if (this.panelID == panelid) {
			return;
		} else if (!closePanel()) {
			// Error saving
			return;
		}
		this.panelID = panelid;
		try {
			switch (panelid) {
			case LibConstants.ACTION_ACCESSION:
				panel = new AccessionPanel(this);
				break;
			case LibConstants.ACTION_BACKLOG:
				panel = new BacklogPanel(this);
				break;
			case LibConstants.ACTION_CODER1:
			case LibConstants.ACTION_CODER2:
			case LibConstants.ACTION_CODER3:
			case LibConstants.ACTION_CODER4:
				panel = new CoderPanel(this, panelid);
				break;
			case LibConstants.ACTION_DAILY:
				panel = new DailyPanel(this);
				break;
			case LibConstants.ACTION_DISTRIBUTE:
				panel = new DistributionPanel(this);
				break;
			case LibConstants.ACTION_EDITOR:
				panel = new SpecimenEditorPanel(this);
				break;
			case LibConstants.ACTION_ERROR:
				panel = new ErrorPanel(this);
				break;
			case LibConstants.ACTION_FACILITY:
				panel = new FacililtyPanel(this);
				break;
			case LibConstants.ACTION_FINALS:
				panel = new FinalsPanel(this);
				break;
			case LibConstants.ACTION_FORECAST:
				panel = new ForecastPanel(this);
				break;
			case LibConstants.ACTION_HISTOLOGY:
				panel = new HistologyPanel(this);
				break;
			case LibConstants.ACTION_ORDERGROUP:
				panel = new OrderGroupPanel(this);
				break;
			case LibConstants.ACTION_ORDERMASTER:
				panel = new OrderMasterPanel(this);
				break;
			case LibConstants.ACTION_PENDING:
				panel = new PendingPanel(this);
				break;
			case LibConstants.ACTION_PERSONNEL:
				panel = new PersonnelPanel(this);
				break;
			case LibConstants.ACTION_PROCEDURES:
				panel = new ProcedurePanel(this);
				break;
			case LibConstants.ACTION_ROUTING:
				panel = new RoutingPanel(this);
				break;
			case LibConstants.ACTION_RULES:
				panel = new RulePanel(this);
				break;
			case LibConstants.ACTION_SCHEDULE:
				panel = new SchedulePanel(this);
				break;
			case LibConstants.ACTION_SERVICES:
				panel = new ServicePanel(this);
				break;
			case LibConstants.ACTION_SETUP:
				panel = new SetupPanel(this);
				break;
			case LibConstants.ACTION_SPECGROUP:
				panel = new SpecimenGroupPanel(this);
				break;
			case LibConstants.ACTION_SPECIALTY:
				panel = new SpecialtyPanel(this);
				break;
			case LibConstants.ACTION_SPECIMEN:
				panel = new SpecimenPanel(this);
				break;
			case LibConstants.ACTION_SPECMASTER:
				panel = new SpecimenMasterPanel(this);
				break;
			case LibConstants.ACTION_SUBSPECIAL:
				panel = new SubspecialtyPanel(this);
				break;
			case LibConstants.ACTION_TURNAROUND:
				panel = new TurnaroundPanel(this);
				break;
			case LibConstants.ACTION_TURNMASTER:
				panel = new TurntimePanel(this);
				break;
			case LibConstants.ACTION_WORKDAYS:
				panel = new WorkdaysPanel(this);
				break;
			case LibConstants.ACTION_WORKLOAD:
				panel = new WorkloadPanel(this);
				break;
			default:
				return;
			}
			if (errorID == LibConstants.ERROR_NONE) {
				frame.setTitle(LibConstants.APP_NAME + " - " + panel.getName());
				frame.add(panel, BorderLayout.CENTER);
				frame.invalidate();
				frame.pack();
				Dimension dim = new Dimension(defaults.getInt("pw" + panelID, 900),
						defaults.getInt("ph" + panelID, 900));
				if (dim.width > 500 && dim.height > 90) {
					frame.setSize(dim);
				}
			}
		} catch (Exception e) {
			log(LibConstants.ERROR_UNEXPECTED, "PJClient", e);
		}
	}

	class ActionAbout extends AbstractAction {

		ActionAbout() {
			super("About");
			putValue(SHORT_DESCRIPTION, "About " + LibConstants.APP_NAME);
			putValue(MNEMONIC_KEY, KeyEvent.VK_A);
			putValue(SMALL_ICON, IUtilities.getIcon(16, "about"));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, "about"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			about();
		}
	}

	class ActionClose extends AbstractAction {

		ActionClose() {
			super("Close");
			putValue(SHORT_DESCRIPTION, "Close Form");
			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
			putValue(SMALL_ICON, IUtilities.getIcon(16, "close"));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, "close"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			closePanel();
		}
	}

	class ActionExcel extends AbstractAction {

		ActionExcel() {
			super("Excel");
			putValue(SHORT_DESCRIPTION, "Save data to Excel file");
			putValue(MNEMONIC_KEY, KeyEvent.VK_X);
			putValue(SMALL_ICON, IUtilities.getIcon(16, "excel"));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, "excel"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (panel != null) {
				(panel).xls();
			}
		}
	}

	class ActionHelp extends AbstractAction {

		ActionHelp() {
			super("Help");
			putValue(SHORT_DESCRIPTION, "Help Contents");
			putValue(MNEMONIC_KEY, KeyEvent.VK_H);
			putValue(SMALL_ICON, IUtilities.getIcon(16, "help"));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, "help"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			help();
		}
	}

	class ActionPdf extends AbstractAction {

		ActionPdf() {
			super("Pdf");
			putValue(SHORT_DESCRIPTION, "Save data to PDF file");
			putValue(MNEMONIC_KEY, KeyEvent.VK_P);
			putValue(SMALL_ICON, IUtilities.getIcon(16, "pdf"));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, "pdf"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (panel != null) {
				(panel).pdf();
			}
		}
	}

	class ActionQuit extends AbstractAction {

		ActionQuit() {
			super("Quit");
			putValue(SHORT_DESCRIPTION, "Quit " + LibConstants.APP_NAME);
			putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
			putValue(SMALL_ICON, IUtilities.getIcon(16, "quit"));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, "quit"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			quit();
		}
	}

	class ActionView extends AbstractAction {
		byte panelID = 0;

		ActionView(byte panel, int mnemonic, String name, String description) {
			super(name);
			this.panelID = panel;
			putValue(SHORT_DESCRIPTION, description);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(SMALL_ICON, IUtilities.getIcon(16, name));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, name));
		}

		ActionView(byte panel, int mnemonic, String name, String description, String icon) {
			super(name);
			this.panelID = panel;
			putValue(SHORT_DESCRIPTION, description);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(SMALL_ICON, IUtilities.getIcon(16, icon));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, icon));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setView(panelID);
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		quit();
	}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
}