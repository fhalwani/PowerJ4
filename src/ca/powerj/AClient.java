package ca.powerj;

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
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;

class AClient extends LBase implements Runnable, WindowListener {
	boolean[] userAccess = new boolean[32];
	JFrame frame = null;
	private Hashtable<Byte, Action> actions;
	IStatusbar statusBar = null;

	AClient(String[] args) {
		super();
		for (byte i = 0; i < 32; i++) {
			userAccess[i] = false;
		}
		init(args);
	}

	private void about() {
		new NDialog(frame, JOptionPane.PLAIN_MESSAGE, "About " + LConstants.APP_NAME, null);
	}

	int askSave(String name) {
		return new NDialog(frame, name).getChoice();
	}

	private boolean closePanel() {
		if (pnlCore != null) {
			if (pnlCore.close()) {
				Dimension dim = frame.getSize();
				defaults.setInt("pw" + pnlID, dim.width);
				defaults.setInt("ph" + pnlID, dim.height);
				statusBar.clear();
				frame.remove(pnlCore);
				frame.setTitle(LConstants.APP_NAME);
				pnlID = 0;
				pnlCore = null;
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
		statusBar = new IStatusbar();
		frame = new JFrame(LConstants.APP_NAME);
		frame.setName(LConstants.APP_NAME);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLocationRelativeTo(null); // for double monitors
		frame.setIconImage(IGUI.getImage(48, LConstants.APP_NAME));
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
	}

	private void display(byte severity, String name, String message) {
		if (severity > LConstants.ERROR_NONE) {
			new NDialog(frame, JOptionPane.ERROR_MESSAGE, name, message);
		} else if (statusBar != null) {
			statusBar.setMessage(message);
		}
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
			NDialog dialog = new NDialog(frame, JOptionPane.QUESTION_MESSAGE, "Export", message, choices, mnemonics, 0);
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
			NDialog dialog = new NDialog(frame, JOptionPane.QUESTION_MESSAGE, "Export", message, choices, mnemonics, 0);
			if (dialog.getChoice() == 0) {
				fileName = "";
			}
		}
		return fileName;
	}

	private void help() {
		NHelp dialog = new NHelp(this);
		dialog.setVisible(true);
	}

	@Override
	void init(String[] args) {
		super.init(args);
		try {
			/* Use the System Look and Feel */
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("ToolTip.font", new FontUIResource(LConstants.APP_FONT));
		} catch (UnsupportedLookAndFeelException ignore) {
		} catch (IllegalAccessException ignore) {
		} catch (InstantiationException ignore) {
		} catch (ClassNotFoundException ignore) {
		}
		if (errorID == LConstants.ERROR_NONE) {
			// Open preferences file
			defaults = new LDefaults(this);
		}
		if (errorID != LConstants.ERROR_NONE) {
			quit();
		} else if (offLine && userID == 1) {
			// Debug mode
			for (byte i = 0; i < 32; i++) {
				userAccess[i] = true;
			}
		} else if (!autoLogin) {
			NLogin login = new NLogin(this, frame);
			if (errorID == LConstants.ERROR_NONE) {
				if (login.cancel) {
					errorID = LConstants.ERROR_ACCESS;
					new NDialog(frame, JOptionPane.WARNING_MESSAGE, LConstants.APP_NAME, "Access denied.");
					quit();
				} else if (!login.validateLogin()) {
					errorID = LConstants.ERROR_ACCESS;
					new NDialog(frame, JOptionPane.WARNING_MESSAGE, LConstants.APP_NAME, "Access denied.");
					quit();
				} else if (!setLogin()) {
					errorID = LConstants.ERROR_ACCESS;
					new NDialog(frame, JOptionPane.WARNING_MESSAGE, LConstants.APP_NAME, "Access denied.");
					quit();
				}
			}
			// Kill Dialog
			login.dispose();
		} else {
			initDBAP();
		}
		if (errorID == LConstants.ERROR_NONE) {
			if (userID == -222 && autoLogin) {
				userAccess[LConstants.ACCESS_GROSS] = true;
				userAccess[LConstants.ACCESS_SCHED] = true;
			} else if (userID == -111 && autoLogin) {
				userAccess[LConstants.ACCESS_HISTO] = true;
				userAccess[LConstants.ACCESS_EMBED] = true;
				userAccess[LConstants.ACCESS_ROUTE] = true;
				userAccess[LConstants.ACCESS_SCHED] = true;
			}
			setActions();
			createFrame();
			startWorker();
			if ((userID == -222 || userID == -111) && autoLogin) {
				setView(LConstants.ACTION_BACKLOG);
			}
		}
	}

	boolean isBusy() {
		return LBase.busy.get();
	}

	@Override
	void log(byte severity, String message) {
		super.log(severity, message);
		display(severity, LConstants.APP_NAME, message);
	}

	@Override
	void log(byte severity, String name, String message) {
		super.log(severity, name, message);
		display(severity, name, message);
	}

	@Override
	void log(byte severity, String name, Throwable e) {
		super.log(severity, name, e);
		display(severity, name, e.getLocalizedMessage());
	}

	void quit() {
		if (closePanel()) {
			if (super.stopWorker()) {
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
				System.exit(0);
			} else {
				display(LConstants.ERROR_NONE, LConstants.APP_NAME, "Updates are in progross, please wait...");
			}
		}
	}

	private void setActions() {
		String name = "";
		actions = new Hashtable<Byte, Action>();
		actions.put(LConstants.ACTION_ABOUT, new ActionAbout());
		actions.put(LConstants.ACTION_CLOSE, new ActionClose());
		actions.put(LConstants.ACTION_HELP, new ActionHelp());
		actions.put(LConstants.ACTION_QUIT, new ActionQuit());
		if (userAccess[LConstants.ACCESS_EX_PDF]) {
			actions.put(LConstants.ACTION_PDF, new ActionPdf());
		}
		if (userAccess[LConstants.ACCESS_EX_XLS]) {
			actions.put(LConstants.ACTION_EXCEL, new ActionExcel());
		}
		if (userAccess[LConstants.ACCESS_GROSS] || userAccess[LConstants.ACCESS_HISTO]
				|| userAccess[LConstants.ACCESS_DIAGN]) {
			actions.put(LConstants.ACTION_BACKLOG,
					new ActionView(LConstants.ACTION_BACKLOG, KeyEvent.VK_B, "Backlog", "Backlog Report"));
		}
		if (userAccess[LConstants.ACCESS_DAILY]) {
			actions.put(LConstants.ACTION_DAILY,
					new ActionView(LConstants.ACTION_DAILY, KeyEvent.VK_D, "Daily", "Daily Workflow Report"));
		}
		if (userAccess[LConstants.ACCESS_DISTR]) {
			actions.put(LConstants.ACTION_DISTRIBUTE,
					new ActionView(LConstants.ACTION_DISTRIBUTE, KeyEvent.VK_I, "Distribution", "Distribution Report"));
		}
		if (userAccess[LConstants.ACCESS_FOREC]) {
			actions.put(LConstants.ACTION_FORECAST,
					new ActionView(LConstants.ACTION_FORECAST, KeyEvent.VK_O, "Forecast", "Annual Forecast Report"));
		}
		if (userAccess[LConstants.ACCESS_EMBED]) {
			actions.put(LConstants.ACTION_HISTOLOGY,
					new ActionView(LConstants.ACTION_HISTOLOGY, KeyEvent.VK_H, "Histology", "Histology Report"));
		}
		if (userAccess[LConstants.ACCESS_ROUTE]) {
			actions.put(LConstants.ACTION_ROUTING,
					new ActionView(LConstants.ACTION_ROUTING, KeyEvent.VK_R, "Routing", "Routing Report"));
		}
		if (userAccess[LConstants.ACCESS_SCHED]) {
			actions.put(LConstants.ACTION_SCHEDULE,
					new ActionView(LConstants.ACTION_SCHEDULE, KeyEvent.VK_S, "Schedule", "Schedule Report"));
		}
		if (userAccess[LConstants.ACCESS_SPECI]) {
			actions.put(LConstants.ACTION_SPECIMEN,
					new ActionView(LConstants.ACTION_SPECIMEN, KeyEvent.VK_P, "Specimens", "Specimens Report"));
		}
		if (userAccess[LConstants.ACCESS_TURNA]) {
			actions.put(LConstants.ACTION_TURNAROUND, new ActionView(LConstants.ACTION_TURNAROUND, KeyEvent.VK_T,
					"Turnaround", "Turnaround Time Report"));
		}
		if (userAccess[LConstants.ACCESS_WORKD]) {
			actions.put(LConstants.ACTION_WORKDAYS,
					new ActionView(LConstants.ACTION_WORKDAYS, KeyEvent.VK_O, "Workdays", "Workdays Report"));
		}
		if (userAccess[LConstants.ACCESS_WORKL]) {
			actions.put(LConstants.ACTION_WORKLOAD,
					new ActionView(LConstants.ACTION_WORKLOAD, KeyEvent.VK_W, "Workload", "Workload Report"));
		}
		if (userAccess[LConstants.ACCESS_AU_EDT]) {
			actions.put(LConstants.ACTION_EDITOR,
					new ActionView(LConstants.ACTION_EDITOR, KeyEvent.VK_E, "Editor", "Specimen Editor"));
		}
		if (userAccess[LConstants.ACCESS_AU_ERR]) {
			actions.put(LConstants.ACTION_ERROR,
					new ActionView(LConstants.ACTION_ERROR, KeyEvent.VK_E, "Errors", "Errors auditor"));
		}
		if (userAccess[LConstants.ACCESS_AU_FNL]) {
			actions.put(LConstants.ACTION_FINALS,
					new ActionView(LConstants.ACTION_FINALS, KeyEvent.VK_F, "Finals", "Final Cases Auditor"));
		}
		if (userAccess[LConstants.ACCESS_AU_PNP]) {
			actions.put(LConstants.ACTION_PENDING,
					new ActionView(LConstants.ACTION_PENDING, KeyEvent.VK_P, "Pending", "Pending Cases Auditor"));
		}
		if (userAccess[LConstants.ACCESS_STP_IE]) {
			actions.put(LConstants.ACTION_BACKUP, new ActionBackup());
			actions.put(LConstants.ACTION_RESTORE, new ActionRestore());
		}
		if (userAccess[LConstants.ACCESS_STP_PJ]) {
			name = setup.getString(LSetup.VAR_CODER1_NAME);
			actions.put(LConstants.ACTION_CODER1,
					new ActionView(LConstants.ACTION_CODER1, 0, name, name + " Setup", "coder1"));
			name = setup.getString(LSetup.VAR_CODER2_NAME);
			actions.put(LConstants.ACTION_CODER2,
					new ActionView(LConstants.ACTION_CODER2, 0, name, name + " Setup", "coder2"));
			name = setup.getString(LSetup.VAR_CODER3_NAME);
			actions.put(LConstants.ACTION_CODER3,
					new ActionView(LConstants.ACTION_CODER3, 0, name, name + " Setup", "coder3"));
			name = setup.getString(LSetup.VAR_CODER4_NAME);
			actions.put(LConstants.ACTION_CODER4,
					new ActionView(LConstants.ACTION_CODER4, 0, name, name + " Setup", "coder4"));
			actions.put(LConstants.ACTION_ORDERGROUP, new ActionView(LConstants.ACTION_ORDERGROUP, KeyEvent.VK_G,
					"Order Groups", "Order Groups Setup", "ordergroups"));
			actions.put(LConstants.ACTION_PROCEDURES,
					new ActionView(LConstants.ACTION_PROCEDURES, KeyEvent.VK_R, "Procedures", "Procedures Setup"));
			actions.put(LConstants.ACTION_RULES,
					new ActionView(LConstants.ACTION_RULES, KeyEvent.VK_U, "Rules", "Rules Setup"));
			actions.put(LConstants.ACTION_SETUP,
					new ActionView(LConstants.ACTION_SETUP, KeyEvent.VK_J, "PowerJ", "PowerJ Setup"));
			actions.put(LConstants.ACTION_SPECGROUP, new ActionView(LConstants.ACTION_SPECGROUP, KeyEvent.VK_C,
					"Specimen Groups", "Specimens Groups Setup", "specgroups"));
			actions.put(LConstants.ACTION_SPECIALTY,
					new ActionView(LConstants.ACTION_SPECIALTY, KeyEvent.VK_Y, "Specialties", "Specialties Setup"));
			actions.put(LConstants.ACTION_SUBSPECIAL, new ActionView(LConstants.ACTION_SUBSPECIAL, KeyEvent.VK_B,
					"Subspecialties", "Subspecialties Setup"));
		}
		if (userAccess[LConstants.ACCESS_STP_PP]) {
			actions.put(LConstants.ACTION_ACCESSION,
					new ActionView(LConstants.ACTION_ACCESSION, 0, "Accessions", "Accessions Setup"));
			actions.put(LConstants.ACTION_FACILITY,
					new ActionView(LConstants.ACTION_FACILITY, KeyEvent.VK_F, "Facilities", "Facilities Setup"));
			actions.put(LConstants.ACTION_ORDERMASTER, new ActionView(LConstants.ACTION_ORDERMASTER, KeyEvent.VK_O,
					"Orders", "Orders Master Setup", "ordermaster"));
			actions.put(LConstants.ACTION_SPECMASTER, new ActionView(LConstants.ACTION_SPECMASTER, KeyEvent.VK_S,
					"Specimens", "Specimens Master Setup", "specmaster"));
			actions.put(LConstants.ACTION_TURNMASTER,
					new ActionView(LConstants.ACTION_TURNMASTER, KeyEvent.VK_T, "Turnaround", "Turnaround Setup"));
		}
		if (userAccess[LConstants.ACCESS_STP_PR]) {
			actions.put(LConstants.ACTION_PERSONNEL,
					new ActionView(LConstants.ACTION_PERSONNEL, KeyEvent.VK_P, "Persons", "Persons Setup"));
		}
		if (userAccess[LConstants.ACCESS_STP_SC]) {
			actions.put(LConstants.ACTION_SERVICES, new ActionView(LConstants.ACTION_SERVICES, KeyEvent.VK_V,
					"Services", "Services Setup", "schedule"));
		}
		if (userAccess[LConstants.ACCESS_STP_TR]) {
			actions.put(LConstants.ACTION_TURNMASTER,
					new ActionView(LConstants.ACTION_TURNMASTER, KeyEvent.VK_T, "Turnaround", "Turnaround Time Setup"));
		}
	}

	void setBusy(boolean busy) {
		LBase.busy.set(busy);
		if (busy) {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			statusBar.setMessage("Updating data, do not interrupt...");
			statusBar.setProgress(-1);
		} else {
			frame.setCursor(Cursor.getDefaultCursor());
			statusBar.setProgress(100);
		}
	}

	/** Check login access to PowerJ & set userAccess value **/
	private boolean setLogin() {
		int i = 0;
		try {
			Hashtable<Byte, PreparedStatement> pstms = dbPowerJ.prepareStatements(LConstants.ACTION_LLOGIN);
			pstms.get(DPowerJ.STM_PRS_SL_PID).setShort(1, userID);
			i = dbPowerJ.getInt(pstms.get(DPowerJ.STM_PRS_SL_PID));
			dbPowerJ.close(pstms);
			userAccess = numbers.intToBoolean(i);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, "PJClient", e);
		}
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
		JMenuItem menuItem = new JMenuItem(actions.get(LConstants.ACTION_CLOSE));
		mainItem.add(menuItem);
		// Export section
		Action action = actions.get(LConstants.ACTION_PDF);
		if (action != null) {
			mainItem.addSeparator();
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
		}
		action = actions.get(LConstants.ACTION_EXCEL);
		if (action != null) {
			mainItem.addSeparator();
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
		}
		mainItem.addSeparator();
		menuItem = new JMenuItem(actions.get(LConstants.ACTION_QUIT));
		mainItem.add(menuItem);
		// Audit section
		noItems = 0;
		mainItem = new JMenu("Audit");
		mainItem.setMnemonic(KeyEvent.VK_A);
		action = actions.get(LConstants.ACTION_EDITOR);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_ERROR);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_FINALS);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_PENDING);
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
		action = actions.get(LConstants.ACTION_BACKLOG);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_DAILY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_DISTRIBUTE);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_FORECAST);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_HISTOLOGY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_ROUTING);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_SCHEDULE);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_SPECIMEN);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_TURNAROUND);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_WORKDAYS);
		if (action != null) {
			menuItem = new JMenuItem(action);
			mainItem.add(menuItem);
			noItems++;
		}
		action = actions.get(LConstants.ACTION_WORKLOAD);
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
		JMenu subMenu = new JMenu(setup.getString(LSetup.VAR_AP_NAME) + "...");
		noItems = 0;
		noSubs = 0;
		action = actions.get(LConstants.ACTION_ACCESSION);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_FACILITY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_ORDERMASTER);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_PERSONNEL);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_SPECMASTER);
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
		action = actions.get(LConstants.ACTION_CODER1);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_CODER2);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_CODER3);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_CODER4);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_ORDERGROUP);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_PROCEDURES);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_RULES);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_SPECGROUP);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_SPECIALTY);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_SUBSPECIAL);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_TURNMASTER);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_SETUP);
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
		action = actions.get(LConstants.ACTION_SERVICES);
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
		action = actions.get(LConstants.ACTION_BACKUP);
		if (action != null) {
			menuItem = new JMenuItem(action);
			subMenu.add(menuItem);
			noSubs++;
		}
		action = actions.get(LConstants.ACTION_RESTORE);
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
		menuItem = new JMenuItem(actions.get(LConstants.ACTION_ABOUT));
		mainItem.add(menuItem);
		menuItem = new JMenuItem(actions.get(LConstants.ACTION_HELP));
		mainItem.add(menuItem);
		menuBar.add(mainItem);
		return menuBar;
	}

	private JToolBar setToolBar() {
		boolean needsSeparator = false;
		byte noItems = 0;
		JToolBar toolBar = new JToolBar();
		// Main section
		Action action = actions.get(LConstants.ACTION_BACKLOG);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_DAILY);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_DISTRIBUTE);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_FORECAST);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_HISTOLOGY);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_ROUTING);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_SCHEDULE);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_SPECIMEN);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_TURNAROUND);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_WORKDAYS);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_WORKLOAD);
		if (action != null) {
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		// Auditor section
		needsSeparator = true;
		action = actions.get(LConstants.ACTION_ERROR);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_FINALS);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_PENDING);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_EDITOR);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		// Export section
		needsSeparator = true;
		action = actions.get(LConstants.ACTION_PDF);
		if (action != null) {
			toolBar.addSeparator();
			needsSeparator = false;
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		action = actions.get(LConstants.ACTION_EXCEL);
		if (action != null) {
			if (needsSeparator) {
				toolBar.addSeparator();
				needsSeparator = false;
			}
			toolBar.add(IGUI.createJButton(action));
			noItems++;
		}
		// Utilities section
		if (noItems > 1) {
			toolBar.addSeparator();
		}
		toolBar.add(IGUI.createJButton(actions.get(LConstants.ACTION_HELP)));
		toolBar.add(IGUI.createJButton(actions.get(LConstants.ACTION_ABOUT)));
		toolBar.add(IGUI.createJButton(actions.get(LConstants.ACTION_CLOSE)));
		return toolBar;
	}

	private void setView(byte id) {
		errorID = LConstants.ERROR_NONE;
		if (this.pnlID == id) {
			return;
		} else if (!closePanel()) {
			// Error saving
			return;
		}
		this.pnlID = id;
		try {
			switch (id) {
			case LConstants.ACTION_ACCESSION:
				pnlCore = new NAccession(this);
				break;
			case LConstants.ACTION_BACKLOG:
				pnlCore = new NBacklog(this);
				break;
			case LConstants.ACTION_CODER1:
				pnlCore = new NCoder(this, NCoder.CODER1);
				break;
			case LConstants.ACTION_CODER2:
				pnlCore = new NCoder(this, NCoder.CODER2);
				break;
			case LConstants.ACTION_CODER3:
				pnlCore = new NCoder(this, NCoder.CODER3);
				break;
			case LConstants.ACTION_CODER4:
				pnlCore = new NCoder(this, NCoder.CODER4);
				break;
			case LConstants.ACTION_DAILY:
				pnlCore = new NDaily(this);
				break;
			case LConstants.ACTION_DISTRIBUTE:
				pnlCore = new NDistribute(this);
				break;
			case LConstants.ACTION_EDITOR:
				pnlCore = new NEdit(this);
				break;
			case LConstants.ACTION_ERROR:
				pnlCore = new NError(this);
				break;
			case LConstants.ACTION_FACILITY:
				pnlCore = new NFacililty(this);
				break;
			case LConstants.ACTION_FINALS:
				pnlCore = new NFinals(this);
				break;
			case LConstants.ACTION_FORECAST:
				pnlCore = new NForecast(this);
				break;
			case LConstants.ACTION_HISTOLOGY:
				pnlCore = new NHistology(this);
				break;
			case LConstants.ACTION_ORDERGROUP:
				pnlCore = new NOrderGroup(this);
				break;
			case LConstants.ACTION_ORDERMASTER:
				pnlCore = new NOrderMaster(this);
				break;
			case LConstants.ACTION_PENDING:
				pnlCore = new NPending(this);
				break;
			case LConstants.ACTION_PERSONNEL:
				pnlCore = new NPersonnel(this);
				break;
			case LConstants.ACTION_PROCEDURES:
				pnlCore = new NProcedure(this);
				break;
			case LConstants.ACTION_ROUTING:
				pnlCore = new NRouting(this);
				break;
			case LConstants.ACTION_RULES:
				pnlCore = new NRule(this);
				break;
			case LConstants.ACTION_SCHEDULE:
				pnlCore = new NSchedule(this);
				break;
			case LConstants.ACTION_SERVICES:
				pnlCore = new NService(this);
				break;
			case LConstants.ACTION_SETUP:
				pnlCore = new NSetup(this);
				break;
			case LConstants.ACTION_SPECGROUP:
				pnlCore = new NSpecimenGroup(this);
				break;
			case LConstants.ACTION_SPECIALTY:
				pnlCore = new NSpecialty(this);
				break;
			case LConstants.ACTION_SPECIMEN:
				pnlCore = new NSpecimen(this);
				break;
			case LConstants.ACTION_SPECMASTER:
				pnlCore = new NSpecimenMaster(this);
				break;
			case LConstants.ACTION_SUBSPECIAL:
				pnlCore = new NSubspecialty(this);
				break;
			case LConstants.ACTION_TURNAROUND:
				pnlCore = new NTurnaround(this);
				break;
			case LConstants.ACTION_TURNMASTER:
				pnlCore = new NTurnMaster(this);
				break;
			case LConstants.ACTION_WORKDAYS:
				pnlCore = new NWorkdays(this);
				break;
			case LConstants.ACTION_WORKLOAD:
				pnlCore = new NWorkload(this);
				break;
			default:
				return;
			}
			if (errorID == LConstants.ERROR_NONE) {
				frame.setTitle(LConstants.APP_NAME + " - " + pnlCore.getName());
				frame.add(pnlCore, BorderLayout.CENTER);
				frame.invalidate();
				frame.pack();
				Dimension dim = new Dimension(defaults.getInt("pw" + pnlID, 900), defaults.getInt("ph" + pnlID, 900));
				if (dim.width > 500 && dim.height > 90) {
					frame.setSize(dim);
				}
			}
		} catch (Exception e) {
			log(LConstants.ERROR_UNEXPECTED, "PJClient", e);
		}
	}

	class ActionAbout extends AbstractAction {

		ActionAbout() {
			super("About");
			putValue(SHORT_DESCRIPTION, "About " + LConstants.APP_NAME);
			putValue(MNEMONIC_KEY, KeyEvent.VK_A);
			putValue(SMALL_ICON, IGUI.getIcon(16, "about"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "about"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			about();
		}
	}

	class ActionBackup extends AbstractAction {

		ActionBackup() {
			super("Backup");
			putValue(SHORT_DESCRIPTION, "Backup Database");
			putValue(MNEMONIC_KEY, KeyEvent.VK_B);
			putValue(SMALL_ICON, IGUI.getIcon(16, "backup"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "backup"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DataBackup worker = new DataBackup();
			worker.execute();
		}
	}

	class ActionClose extends AbstractAction {

		ActionClose() {
			super("Close");
			putValue(SHORT_DESCRIPTION, "Close Form");
			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
			putValue(SMALL_ICON, IGUI.getIcon(16, "close"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "close"));
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
			putValue(SMALL_ICON, IGUI.getIcon(16, "excel"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "excel"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (pnlCore != null) {
				(pnlCore).xls();
			}
		}
	}

	class ActionHelp extends AbstractAction {

		ActionHelp() {
			super("Help");
			putValue(SHORT_DESCRIPTION, "Help Contents");
			putValue(MNEMONIC_KEY, KeyEvent.VK_H);
			putValue(SMALL_ICON, IGUI.getIcon(16, "help"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "help"));
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
			putValue(SMALL_ICON, IGUI.getIcon(16, "pdf"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "pdf"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (pnlCore != null) {
				(pnlCore).pdf();
			}
		}
	}

	class ActionQuit extends AbstractAction {

		ActionQuit() {
			super("Quit");
			putValue(SHORT_DESCRIPTION, "Quit " + LConstants.APP_NAME);
			putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
			putValue(SMALL_ICON, IGUI.getIcon(16, "quit"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "quit"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			quit();
		}
	}

	class ActionRestore extends AbstractAction {

		ActionRestore() {
			super("Restore");
			putValue(SHORT_DESCRIPTION, "Restore Database");
			putValue(MNEMONIC_KEY, KeyEvent.VK_R);
			putValue(SMALL_ICON, IGUI.getIcon(16, "restore"));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, "restore"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DataImporter worker = new DataImporter();
			worker.execute();
		}
	}

	class ActionView extends AbstractAction {
		byte panelID = 0;

		ActionView(byte panel, int mnemonic, String name, String description) {
			super(name);
			this.panelID = panel;
			putValue(SHORT_DESCRIPTION, description);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(SMALL_ICON, IGUI.getIcon(16, name));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, name));
		}

		ActionView(byte panel, int mnemonic, String name, String description, String icon) {
			super(name);
			this.panelID = panel;
			putValue(SHORT_DESCRIPTION, description);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(SMALL_ICON, IGUI.getIcon(16, icon));
			putValue(LARGE_ICON_KEY, IGUI.getIcon(32, icon));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setView(panelID);
		}
	}

	private class DataBackup extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			LBackup worker = new LBackup(AClient.this);
			worker.backup();
			return null;
		}

		@Override
		public void done() {
			setBusy(false);
		}
	}

	private class DataImporter extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			LBackup worker = new LBackup(AClient.this);
			worker.restore();
			return null;
		}

		@Override
		public void done() {
			setBusy(false);
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		quit();
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
}