package ca.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

class NError extends NBase {
	private long caseID = 0;
	private ArrayList<OError> lstErrors = new ArrayList<OError>();
	private ITableModelEvent modelEvent;
	private ITableModelSpecimen modelSpec;
	private ModelError modelError;
	private ITable tblEvents;
	private ITable tblSpec;
	private ITable tblError;
	private JTextArea txtComment;

	NError(AClient parent) {
		super(parent);
		setName("Error");
		parent.dbPowerJ.prepareError();
		if (!parent.offLine) {
			pj.dbAP.prepareEditor();
		}
		getData();
		createPanel();
		programmaticChange = false;
	}

	boolean close() {
		if (super.close()) {
			modelEvent.close();
			modelSpec.close();
			if (pj.dbAP != null) {
				pj.dbAP.close();
			}
		}
		return !altered;
	}

	private void createPanel() {
		modelError = new ModelError();
		tblError = new ITable(pj, modelError);
		tblError.addAncestorListener(new IFocusListener());
		tblError.addFocusListener(this);
        tblError.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int index = lsm.getMinSelectionIndex();
		        if (index > -1) {
					// else, Selection got filtered away.
					setRow(tblError.convertRowIndexToModel(index));
		        }
			}
        });
        tblError.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblError);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		modelSpec = new ITableModelSpecimen(pj);
		modelSpec.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (!programmaticChange) {
					if (e.getType() == TableModelEvent.UPDATE) {
						save (modelSpec.getMasterID(e.getFirstRow()),
								modelSpec.getSpecID(e.getFirstRow()));
					}
				}
			}
		});
		tblSpec = new ITable(pj, modelSpec);
		tblSpec.addFocusListener(this);
		IComboBox cboMaster = new IComboBox();
		cboMaster.setName("SpecMaster");
		cboMaster.setModel(pj.dbPowerJ.getSpecimenMaster(false));
		TableColumn column = tblSpec.getColumnModel().getColumn(ITableModelSpecimen.SPEC_CODE);
		column.setCellEditor(new DefaultCellEditor(cboMaster));
		JScrollPane scrollSpec = IGUI.createJScrollPane(tblSpec);
		scrollSpec.setMinimumSize(new Dimension(600, 200));
		modelEvent = new ITableModelEvent(pj);
		tblEvents = new ITable(pj, modelEvent);
		tblEvents.addFocusListener(this);
		JScrollPane scrollEvents = IGUI.createJScrollPane(tblEvents);
		scrollEvents.setMinimumSize(new Dimension(600, 400));
		JSplitPane spltVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		spltVertical.setTopComponent(scrollSpec);
		spltVertical.setBottomComponent(scrollEvents);
		spltVertical.setOneTouchExpandable(true);
		spltVertical.setDividerLocation(350);
		spltVertical.setPreferredSize(new Dimension(700, 700));
		txtComment = new JTextArea();
		txtComment.setEditable(false);
		txtComment.setMargin(new Insets(5, 5, 5, 5));
		txtComment.setFont(LConstants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = IGUI.createJScrollPane(txtComment);
		scrollComment.setMinimumSize(new Dimension(400, 700));
		JSplitPane spltHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		spltHorizontal.setTopComponent(scrollComment);
		spltHorizontal.setBottomComponent(spltVertical);
		spltHorizontal.setOneTouchExpandable(true);
		spltHorizontal.setDividerLocation(450);
		spltHorizontal.setPreferredSize(new Dimension(1200, 700));
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(spltHorizontal);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(1500, 700));
		setLayout(new BorderLayout());
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ERR_SELECT);
		try {
			while (rst.next()) {
				OError error = new OError();
				error.caseID = rst.getLong("CAID");
				error.errID = rst.getByte("ERID");
				error.caseNo = rst.getString("CANO");
				lstErrors.add(error);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
			pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(lstErrors.size()));
		}
	}

	private void save(short masterID, long specID) {
		pj.dbAP.setShort(DPowerpath.STM_SPEC_UPDATE, 1, masterID);
		pj.dbAP.setLong(DPowerpath.STM_SPEC_UPDATE, 2, specID);
		if (pj.dbAP.execute(DPowerpath.STM_SPEC_UPDATE) > 0) {
			pj.dbPowerJ.setLong(DPowerJ.STM_ERR_UPDATE, 1, caseID);
			if (pj.dbPowerJ.execute(DPowerJ.STM_ERR_UPDATE) > 0) {
				altered = false;
			}
		}
	}

	private void setRow(int row) {
		programmaticChange = true;
		pj.setBusy(true);
		caseID = lstErrors.get(row).caseID;
		if (!pj.offLine) {
			modelSpec.getData(caseID);
			modelSpec.getData(caseID);
		}
		pj.dbPowerJ.setLong(DPowerJ.STM_ERR_SL_CMT, 1, caseID);
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ERR_SL_CMT);
		try {
			while (rst.next()) {
				if (rst.getString("ERDC") != null) {
					txtComment.setText(rst.getString("ERDC"));
					txtComment.setCaretPosition(0);
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		programmaticChange = false;
		pj.setBusy(false);
	}

	private class ModelError extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return lstErrors.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return lstErrors.get(row).caseNo;
		}
	}
}