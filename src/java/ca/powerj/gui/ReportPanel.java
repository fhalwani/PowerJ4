package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import com.hexidec.ekit.EkitCoreSpell;
import ca.powerj.data.CaseData;
import ca.powerj.data.ReportData;
import ca.powerj.data.ReportSpecimenData;
import ca.powerj.data.SpecimenData;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.IToolBarText;
import ca.powerj.swing.IUtilities;

public class ReportPanel extends BasePanel {
	private short userID = 0;
	private JTree tree;
	private JTextPane textPane;
	private EkitCoreSpell ekitEditor;
	private ReportData reportData;
	private IToolBarText toolBar;

	public ReportPanel(AppFrame application) {
		super(application);
		setName("Reports");
		this.userID = application.getUserID();
		application.dbPowerJ.setStatements(LibConstants.ACTION_REPORT);
		if (!application.isOffline()) {
			application.dbPath.setStatements(LibConstants.ACTION_REPORT);
		}
		reportData = new ReportData();
		createPanel();
		programmaticChange = false;
	}

	private void createPanel() {
		ekitEditor = new EkitCoreSpell();
		DefaultTreeModel treeModel = new DefaultTreeModel(reportData.getRoot());
		tree = new JTree(treeModel);
		tree.setFont(LibConstants.APP_FONT);
		tree.setEditable(false);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setExpandsSelectedPaths(true);
		tree.addAncestorListener(new IFocusListener());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent tse) {
				if (!programmaticChange) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setSelection();
						}
					});
				}
			}
		});
		JScrollPane scrollTree = IUtilities.createJScrollPane(tree);
		scrollTree.setMinimumSize(new Dimension(200, 600));
		textPane = new JTextPane() {
			// Disable cut/paste
			@Override
			public void cut() {}
			@Override
			public void paste() {}
		};
		textPane.setContentType("text/html");
		textPane.setMargin(new Insets(4, 4, 4, 4));
		textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textPane.setEditable(false);
		JScrollPane scrollText = IUtilities.createJScrollPane(textPane);
		scrollText.setMinimumSize(new Dimension(500, 300));
		JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitVertical.setTopComponent(ekitEditor);
		splitVertical.setBottomComponent(textPane);
		splitVertical.setOneTouchExpandable(true);
		splitVertical.setDividerLocation(350);
		JSplitPane splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitHorizontal.setTopComponent(scrollTree);
		splitHorizontal.setBottomComponent(splitVertical);
		splitHorizontal.setOneTouchExpandable(true);
		splitHorizontal.setDividerLocation(250);
		toolBar = new IToolBarText(application, this);
		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(splitHorizontal, BorderLayout.CENTER);
	}

	public void newReport() {
		if (!application.isOffline()) {
			CaseData thisCase = application.dbPath.getCaseLocked();
			if (thisCase.getCaseID() > 0 && thisCase.getStatusID() != LibConstants.STATUS_FINAL) {
				ArrayList<SpecimenData> apSpecimens = application.dbPath.getCaseSpecimens(thisCase.getCaseID());
				if (apSpecimens.size() > 0) {
//					ArrayList<SpecimenData> pjSpecimens = application.dbPowerJ.getCaseSpecimens(thisCase.getCaseID());
					application.dbPowerJ.getCaseUMLS(thisCase.getCaseID(), userID, reportData);
					if (apSpecimens.size() != reportData.getRoot().getChildCount()) {
						reportData.newReport(thisCase.getCaseID(), thisCase.getCaseNo());
						application.dbPowerJ.getSpecimenTissues(userID, reportData, apSpecimens);
						save();
					}
					return;
				}
			}
		}
		reportData.newReport(0, "Unknown");
	}

	@Override
	void save() {
		for (byte i = 0; i < reportData.getRoot().getChildCount(); i++) {
			ReportSpecimenData specimen = reportData.getSpecimenNode(i);
			
		}
	}

	private void setSelection() {
	}

	public void setStyle(int id) {
		if (userID != id) {
			userID = (short) id;
			application.dbPowerJ.getStyle(userID, reportData.getStyle());
			ekitEditor.setDocumentText(reportData.getHtml());
		}
	}
}