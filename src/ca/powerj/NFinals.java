package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class NFinals extends NBase {
	private final byte ADDL_CODE = 0;
	private final byte ADDL_VAL1 = 1;
	private final byte ADDL_VAL2 = 2;
	private final byte ADDL_VAL3 = 3;
	private final byte ADDL_VAL4 = 4;
	private final byte ADDL_VAL5 = 5;
	private final byte ADDL_STAF = 6;
	private final byte ADDL_DATE = 7;
	private final byte CASE_NO = 0;
	private final byte CASE_FAC = 1;
	private final byte CASE_SPY = 2;
	private final byte CASE_SUB = 3;
	private final byte CASE_PROC = 4;
	private final byte CASE_SPEC = 5;
	private final byte CASE_VAL1 = 6;
	private final byte CASE_VAL2 = 7;
	private final byte CASE_VAL3 = 8;
	private final byte CASE_VAL4 = 9;
	private final byte CASE_VAL5 = 10;
	private final byte CASE_NOSP = 11;
	private final byte CASE_NOBL = 12;
	private final byte CASE_NOSL = 13;
	private final byte CASE_NOHE = 14;
	private final byte CASE_NOSS = 15;
	private final byte CASE_NOIH = 16;
	private final byte CASE_NOMO = 17;
	private final byte CASE_NOSY = 18;
	private final byte CASE_NOFS = 19;
	private final byte CASE_ACED = 20;
	private final byte CASE_GRED = 21;
	private final byte CASE_EMED = 22;
	private final byte CASE_MIED = 23;
	private final byte CASE_ROED = 24;
	private final byte CASE_FIED = 25;
	private final byte CASE_GRBY = 26;
	private final byte CASE_EMBY = 27;
	private final byte CASE_MIBY = 28;
	private final byte CASE_ROBY = 29;
	private final byte CASE_FIBY = 30;
	private final byte CASE_GRTA = 31;
	private final byte CASE_EMTA = 32;
	private final byte CASE_MITA = 33;
	private final byte CASE_ROTA = 34;
	private final byte CASE_FITA = 35;
	private final byte FRZN_STAF = 0;
	private final byte FRZN_VAL1 = 1;
	private final byte FRZN_VAL2 = 2;
	private final byte FRZN_VAL3 = 3;
	private final byte FRZN_VAL4 = 4;
	private final byte FRZN_VAL5 = 5;
	private final byte FRZN_NOBL = 6;
	private final byte FRZN_NOSL = 7;
	private final byte ORDER_NAME = 0;
	private final byte ORDER_QTY = 1;
	private final byte ORDER_VAL1 = 2;
	private final byte ORDER_VAL2 = 3;
	private final byte ORDER_VAL3 = 4;
	private final byte ORDER_VAL4 = 5;
	private final byte SPEC_NAME = 0;
	private final byte SPEC_VAL1 = 1;
	private final byte SPEC_VAL2 = 2;
	private final byte SPEC_VAL3 = 3;
	private final byte SPEC_VAL4 = 4;
	private final byte SPEC_VAL5 = 5;
	private final byte SPEC_NOBL = 6;
	private final byte SPEC_NOSL = 7;
	private final byte SPEC_NOFR = 8;
	private final byte SPEC_NOHE = 9;
	private final byte SPEC_NOSS = 10;
	private final byte SPEC_NOIH = 11;
	private final byte SPEC_NOMO = 12;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private final byte FILTER_PRO = 3;
	private short[] filters = { 0, 0, 0, 0 };
	private String[] coders = new String[5];
	private long caseID = 0;
	private long specID = 0;
	private ArrayList<OCaseFinal> cases = new ArrayList<OCaseFinal>();
	private ArrayList<OSpecFinal> specimens = new ArrayList<OSpecFinal>();
	private ArrayList<OOrderFinal> orders = new ArrayList<OOrderFinal>();
	private ArrayList<OFrozen> frozens = new ArrayList<OFrozen>();
	private ArrayList<OAdditional> additionals = new ArrayList<OAdditional>();
	private ModelAdditional modelAddl;
	private ModelCase modelCase;
	private ModelFrozen modelFrozen;
	private ModelOrder modelOrder;
	private ModelSpecimen modelSpec;
	private ITable tblCase, tblSpec;
	private JTextArea txtComment;

	NFinals(AClient parent) {
		super(parent);
		setName("Finals");
		parent.dbPowerJ.prepareFinals();
		coders[0] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		coders[1] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		coders[2] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		coders[3] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		coders[4] = pj.setup.getString(LSetup.VAR_V5_NAME);
		createPanel();
		WorkerData worker = new WorkerData();
		worker.execute();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		cases.clear();
		return true;
	}

	private void createPanel() {
		modelCase = new ModelCase();
		tblCase = new ITable(pj, modelCase) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					Point p = e.getPoint();
					int col = columnAtPoint(p);
					JTable tbl = (JTable) e.getSource();
					int viewRow = rowAtPoint(p);
					int modelRow = tbl.convertRowIndexToModel(viewRow);
					String tip = null;
					switch (col) {
					case CASE_GRBY:
						tip = cases.get(modelRow).grossName;
						break;
					case CASE_EMBY:
						tip = cases.get(modelRow).embedName;
						break;
					case CASE_MIBY:
						tip = cases.get(modelRow).microName;
						break;
					case CASE_ROBY:
						tip = cases.get(modelRow).routeName;
						break;
					case CASE_FIBY:
						tip = cases.get(modelRow).finalName;
						break;
					default:
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblCase.setName("Cases");
		tblCase.addFocusListener(this);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tblCase.addAncestorListener(new IFocusListener());
		tblCase.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (programmaticChange)
					return;
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int viewRow = lsm.getMinSelectionIndex();
				if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblCase.convertRowIndexToModel(viewRow);
					if (caseID != cases.get(modelRow).caseID) {
						caseID = cases.get(modelRow).caseID;
						setCase();
					}
				}
			}
		});
		JScrollPane scrollCase = IGUI.createJScrollPane(tblCase);
		scrollCase.setMinimumSize(new Dimension(900, 100));
		modelSpec = new ModelSpecimen();
		tblSpec = new ITable(pj, modelSpec) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					if (!programmaticChange) {
						Point p = e.getPoint();
						int col = columnAtPoint(p);
						if (col == SPEC_NAME) {
							int viewRow = rowAtPoint(p);
							JTable tbl = (JTable) e.getSource();
							int modelRow = tbl.convertRowIndexToModel(viewRow);
							tip = specimens.get(modelRow).descr;
						}
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblSpec.addFocusListener(this);
		tblSpec.setName("Specimens");
		tblSpec.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (programmaticChange)
					return;
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int viewRow = lsm.getMinSelectionIndex();
				if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblSpec.convertRowIndexToModel(viewRow);
					if (specID != specimens.get(modelRow).specID) {
						specID = specimens.get(modelRow).specID;
						setSpecimen();
					}
				}
			}
		});
		JScrollPane scrollSpec = IGUI.createJScrollPane(tblSpec);
		scrollSpec.setMinimumSize(new Dimension(900, 100));
		modelOrder = new ModelOrder();
		ITable tblOrder = new ITable(pj, modelOrder);
		tblOrder.addFocusListener(this);
		JScrollPane scrollOrder = IGUI.createJScrollPane(tblOrder);
		scrollOrder.setMinimumSize(new Dimension(500, 100));
		modelFrozen = new ModelFrozen();
		ITable tblFrozen = new ITable(pj, modelFrozen) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					if (!programmaticChange) {
						Point p = e.getPoint();
						int col = columnAtPoint(p);
						if (col == FRZN_STAF) {
							int viewRow = rowAtPoint(p);
							JTable tbl = (JTable) e.getSource();
							int modelRow = tbl.convertRowIndexToModel(viewRow);
							tip = frozens.get(modelRow).name;
						}
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblFrozen.addFocusListener(this);
		JScrollPane scrollFrozen = IGUI.createJScrollPane(tblFrozen);
		scrollFrozen.setMinimumSize(new Dimension(500, 100));
		modelAddl = new ModelAdditional();
		ITable tblAddl = new ITable(pj, modelAddl) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					if (!programmaticChange) {
						Point p = e.getPoint();
						if (columnAtPoint(p) == ADDL_STAF) {
							JTable tbl = (JTable) e.getSource();
							int viewRow = rowAtPoint(p);
							int modelRow = tbl.convertRowIndexToModel(viewRow);
							try {
								tip = additionals.get(modelRow).finalFull;
							} catch (RuntimeException ignore) {
							}
						}
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblAddl.addFocusListener(this);
		JScrollPane scrollAddl = IGUI.createJScrollPane(tblAddl);
		scrollAddl.setMinimumSize(new Dimension(500, 100));
		txtComment = new JTextArea();
		txtComment.setEditable(false);
		txtComment.setMargin(new Insets(5, 5, 5, 5));
		txtComment.setFont(LConstants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = IGUI.createJScrollPane(txtComment);
		scrollComment.setMinimumSize(new Dimension(300, 400));
		JSplitPane splitTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTop.setTopComponent(scrollCase);
		splitTop.setBottomComponent(scrollSpec);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(150);
		splitTop.setPreferredSize(new Dimension(900, 300));
		JSplitPane splitMiddle = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitMiddle.setTopComponent(scrollOrder);
		splitMiddle.setBottomComponent(scrollFrozen);
		splitMiddle.setOneTouchExpandable(true);
		splitMiddle.setDividerLocation(150);
		splitMiddle.setPreferredSize(new Dimension(500, 300));
		JSplitPane splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitLeft.setTopComponent(splitMiddle);
		splitLeft.setBottomComponent(scrollAddl);
		splitLeft.setOneTouchExpandable(true);
		splitLeft.setDividerLocation(350);
		splitLeft.setPreferredSize(new Dimension(500, 500));
		JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitBottom.setTopComponent(splitLeft);
		splitBottom.setBottomComponent(scrollComment);
		splitBottom.setOneTouchExpandable(true);
		splitBottom.setDividerLocation(550);
		splitBottom.setPreferredSize(new Dimension(900, 500));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(splitTop);
		splitAll.setBottomComponent(splitBottom);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(350);
		splitAll.setPreferredSize(new Dimension(900, 900));
		JScrollPane scrollAll = IGUI.createJScrollPane(splitAll);
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this), BorderLayout.NORTH);
		add(scrollAll, BorderLayout.CENTER);
	}

	private void setCase() {
		if (LBase.busy.get())
			return;
		String comment = "";
		ResultSet rst = null;
		try {
			pj.setBusy(true);
			programmaticChange = true;
			specID = 0;
			specimens.clear();
			additionals.clear();
			orders.clear();
			frozens.clear();
			pj.dbPowerJ.setLong(DPowerJ.STM_CMT_SELECT, 1, caseID);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_CMT_SELECT);
			while (rst.next()) {
				if (rst.getString("CODC") != null) {
					// deleted after a year
					comment = rst.getString("CODC");
				}
			}
			pj.dbPowerJ.closeRst(rst);
			pj.dbPowerJ.setLong(DPowerJ.STM_SPE_SELECT, 1, caseID);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPE_SELECT);
			OSpecFinal specimen = new OSpecFinal();
			while (rst.next()) {
				specimen = new OSpecFinal();
				specimen.noBlocks = rst.getShort("SPBL");
				specimen.noSlides = rst.getShort("SPSL");
				specimen.noHE = rst.getShort("SPHE");
				specimen.noSS = rst.getShort("SPSS");
				specimen.noIHC = rst.getShort("SPIH");
				specimen.noMOL = rst.getShort("SPMO");
				specimen.noFrags = rst.getShort("SPFR");
				specimen.value5 = rst.getInt("SPV5");
				specimen.specID = rst.getLong("SPID");
				specimen.value1 = rst.getDouble("SPV1");
				specimen.value2 = rst.getDouble("SPV2");
				specimen.value3 = rst.getDouble("SPV3");
				specimen.value4 = rst.getDouble("SPV4");
				specimen.name = rst.getString("SMNM");
				specimen.descr = rst.getString("SPDC");
				specimens.add(specimen);
			}
			pj.dbPowerJ.closeRst(rst);
			pj.dbPowerJ.setLong(DPowerJ.STM_ADD_SL_CID, 1, caseID);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ADD_SL_CID);
			byte codeID = 0;
			final String[] codes = { "NIL", "AMND", "ADDN", "CORR", "REVW" };
			OAdditional additional = new OAdditional();
			while (rst.next()) {
				if (rst.getShort("ADCD") > 4) {
					// Reviews
					codeID = 4;
				} else {
					codeID = rst.getByte("ADCD");
				}
				additional = new OAdditional();
				additional.code = codes[codeID];
				additional.value5 = rst.getInt("ADV5");
				additional.value1 = rst.getDouble("ADV1");
				additional.value2 = rst.getDouble("ADV2");
				additional.value3 = rst.getDouble("ADV3");
				additional.value4 = rst.getDouble("ADV4");
				additional.finalName = rst.getString("PRNM");
				additional.finalFull = rst.getString("PRLS") + ", " + rst.getString("PRFR").substring(0, 1);
				additional.finaled.setTimeInMillis(rst.getTimestamp("ADDT").getTime());
				additionals.add(additional);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.setBusy(false);
			programmaticChange = false;
			pj.dbPowerJ.closeRst(rst);
			modelAddl.fireTableDataChanged();
			modelFrozen.fireTableDataChanged();
			modelOrder.fireTableDataChanged();
			modelSpec.fireTableDataChanged();
			txtComment.setText(comment);
		}
	}

	@Override
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_FAC:
			filters[FILTER_FAC] = value;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			break;
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		case IToolBar.TB_PRO:
			filters[FILTER_PRO] = value;
			break;
		default:
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		caseID = 0;
		specID = 0;
		cases.clear();
		specimens.clear();
		additionals.clear();
		orders.clear();
		frozens.clear();
		txtComment.setText("");
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	private void setSpecimen() {
		if (LBase.busy.get())
			return;
		ResultSet rst = null;
		try {
			pj.setBusy(true);
			programmaticChange = true;
			orders.clear();
			frozens.clear();
			pj.dbPowerJ.setLong(DPowerJ.STM_ORD_SELECT, 1, specID);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ORD_SELECT);
			OOrderFinal order = new OOrderFinal();
			while (rst.next()) {
				order = new OOrderFinal();
				order.qty = rst.getShort("ORQY");
				order.value1 = rst.getDouble("ORV1");
				order.value2 = rst.getDouble("ORV2");
				order.value3 = rst.getDouble("ORV3");
				order.value4 = rst.getDouble("ORV4");
				order.name = rst.getString("OGNM");
				orders.add(order);
			}
			pj.dbPowerJ.closeRst(rst);
			pj.dbPowerJ.setLong(DPowerJ.STM_FRZ_SL_SID, 1, specID);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_FRZ_SL_SID);
			OFrozen frozen = new OFrozen();
			while (rst.next()) {
				frozen = new OFrozen();
				frozen.noBlocks = rst.getShort("SPBL");
				frozen.noSlides = rst.getShort("SPSL");
				frozen.value5 = rst.getInt("SPV5");
				frozen.value1 = rst.getDouble("SPV1");
				frozen.value2 = rst.getDouble("SPV2");
				frozen.value3 = rst.getDouble("SPV3");
				frozen.value4 = rst.getDouble("SPV4");
				frozen.finalBy = rst.getString("PRNM");
				frozen.name = rst.getString("PRLS") + ", " + rst.getString("PRFR").substring(0, 1);
				frozens.add(frozen);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.setBusy(false);
			programmaticChange = false;
			pj.dbPowerJ.closeRst(rst);
			modelFrozen.fireTableDataChanged();
			modelOrder.fireTableDataChanged();
		}
	}

	private class ModelAdditional extends ITableModel {
		private final String[] columns = { "ADDL", coders[0], coders[1], coders[2], coders[3], coders[4], "STAFF",
				"DATE" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case ADDL_VAL1:
			case ADDL_VAL2:
			case ADDL_VAL3:
			case ADDL_VAL4:
				return Double.class;
			case ADDL_VAL5:
				return Integer.class;
			case ADDL_DATE:
				return Calendar.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public int getRowCount() {
			return additionals.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			OAdditional thisRow = additionals.get(row);
			switch (col) {
			case ADDL_CODE:
				value = thisRow.code;
				break;
			case ADDL_STAF:
				value = thisRow.finalName;
				break;
			case ADDL_DATE:
				value = thisRow.finaled;
				break;
			case ADDL_VAL1:
				value = thisRow.value1;
				break;
			case ADDL_VAL2:
				value = thisRow.value2;
				break;
			case ADDL_VAL3:
				value = thisRow.value3;
				break;
			case ADDL_VAL4:
				value = thisRow.value4;
				break;
			default:
				value = thisRow.value5 / 60;
			}
			return value;
		}
	}

	private class ModelCase extends ITableModel {
		private final String[] columns = { "NO", "FAC", "SPY", "SUB", "PROC", "SPEC", coders[0], coders[1], coders[2],
				coders[3], coders[4], "SPECS", "BLKS", "SLDS", "H&E", "SS", "IHC", "MOL", "SYNP", "FS", "ACCESS",
				"GROSS", "EMBED", "MICRO", "ROUTE", "FINAL", "GRNM", "EMNM", "MINM", "RONM", "FINM", "GRTA", "EMTA",
				"MITA", "ROTA", "FITA" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_VAL1:
			case CASE_VAL2:
			case CASE_VAL3:
			case CASE_VAL4:
				return Double.class;
			case CASE_ACED:
			case CASE_GRED:
			case CASE_EMED:
			case CASE_MIED:
			case CASE_ROED:
			case CASE_FIED:
				return Calendar.class;
			case CASE_NOBL:
			case CASE_NOSL:
			case CASE_NOSP:
			case CASE_NOSY:
			case CASE_NOFS:
			case CASE_NOHE:
			case CASE_NOSS:
			case CASE_NOIH:
			case CASE_NOMO:
				return Short.class;
			case CASE_VAL5:
				return Integer.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public int getRowCount() {
			return cases.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			OCaseFinal thisRow = cases.get(row);
			switch (col) {
			case CASE_NO:
				value = thisRow.caseNo;
				break;
			case CASE_FAC:
				value = thisRow.facName;
				break;
			case CASE_SPY:
				value = thisRow.spyName;
				break;
			case CASE_SUB:
				value = thisRow.subName;
				break;
			case CASE_PROC:
				value = thisRow.procName;
				break;
			case CASE_SPEC:
				value = thisRow.specName;
				break;
			case CASE_ACED:
				value = thisRow.accessed;
				break;
			case CASE_GRED:
				value = thisRow.grossed;
				break;
			case CASE_GRBY:
				value = thisRow.grossName;
				break;
			case CASE_GRTA:
				value = thisRow.grossTAT;
				break;
			case CASE_EMED:
				value = thisRow.embeded;
				break;
			case CASE_EMBY:
				value = thisRow.embedName;
				break;
			case CASE_EMTA:
				value = thisRow.embedTAT;
				break;
			case CASE_MIED:
				value = thisRow.microed;
				break;
			case CASE_MIBY:
				value = thisRow.microName;
				break;
			case CASE_MITA:
				value = thisRow.microTAT;
				break;
			case CASE_ROED:
				value = thisRow.routed;
				break;
			case CASE_ROBY:
				value = thisRow.routeName;
				break;
			case CASE_ROTA:
				value = thisRow.routeTAT;
				break;
			case CASE_FIED:
				value = thisRow.finaled;
				break;
			case CASE_FIBY:
				value = thisRow.finalName;
				break;
			case CASE_FITA:
				value = thisRow.finalTAT;
				break;
			case CASE_NOSP:
				value = thisRow.noSpec;
				break;
			case CASE_NOBL:
				value = thisRow.noBlocks;
				break;
			case CASE_NOSL:
				value = thisRow.noSlides;
				break;
			case CASE_NOSY:
				value = thisRow.noSynop;
				break;
			case CASE_NOFS:
				value = thisRow.noFSSpec;
				break;
			case CASE_NOHE:
				value = thisRow.noHE;
				break;
			case CASE_NOSS:
				value = thisRow.noSS;
				break;
			case CASE_NOIH:
				value = thisRow.noIHC;
				break;
			case CASE_NOMO:
				value = thisRow.noMol;
				break;
			case CASE_VAL1:
				value = thisRow.value1;
				break;
			case CASE_VAL2:
				value = thisRow.value2;
				break;
			case CASE_VAL3:
				value = thisRow.value3;
				break;
			case CASE_VAL4:
				value = thisRow.value4;
				break;
			default:
				value = thisRow.value5 / 60;
			}
			return value;
		}
	}

	private class ModelFrozen extends ITableModel {
		private final String[] columns = { "FSEC", coders[0], coders[1], coders[2], coders[3], coders[4], "BLKS",
				"SLDS" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case FRZN_VAL1:
			case FRZN_VAL2:
			case FRZN_VAL3:
			case FRZN_VAL4:
				return Double.class;
			case FRZN_NOBL:
			case FRZN_NOSL:
				return Short.class;
			case FRZN_VAL5:
				return Integer.class;
			default:
				return OItem.class;
			}
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public int getRowCount() {
			return frozens.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			OFrozen thisRow = frozens.get(row);
			switch (col) {
			case FRZN_STAF:
				value = thisRow.finalBy;
				break;
			case FRZN_NOBL:
				value = thisRow.noBlocks;
				break;
			case FRZN_NOSL:
				value = thisRow.noSlides;
				break;
			case ADDL_VAL1:
				value = thisRow.value1;
				break;
			case ADDL_VAL2:
				value = thisRow.value2;
				break;
			case ADDL_VAL3:
				value = thisRow.value3;
				break;
			case ADDL_VAL4:
				value = thisRow.value4;
				break;
			default:
				value = thisRow.value5 / 60;
			}
			return value;
		}
	}

	private class ModelOrder extends ITableModel {
		private final String[] columns = { "ORDER", "QTY", coders[0], coders[1], coders[2], coders[3] };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case ORDER_VAL1:
			case ORDER_VAL2:
			case ORDER_VAL3:
			case ORDER_VAL4:
				return Double.class;
			case ORDER_QTY:
				return Short.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public int getRowCount() {
			return orders.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			switch (col) {
			case ORDER_NAME:
				value = orders.get(row).name;
				break;
			case ORDER_QTY:
				value = orders.get(row).qty;
				break;
			case ORDER_VAL1:
				value = orders.get(row).value1;
				break;
			case ORDER_VAL2:
				value = orders.get(row).value2;
				break;
			case ORDER_VAL3:
				value = orders.get(row).value3;
				break;
			case ORDER_VAL4:
				value = orders.get(row).value4;
				break;
			default:
			}
			return value;
		}
	}

	private class ModelSpecimen extends ITableModel {
		private final String[] columns = { "SPEC", coders[0], coders[1], coders[2], coders[3], coders[4], "BLKS",
				"SLDS", "FRAG", "H&E", "SS", "IHC", "MOL" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case SPEC_VAL1:
			case SPEC_VAL2:
			case SPEC_VAL3:
			case SPEC_VAL4:
				return Double.class;
			case SPEC_NOBL:
			case SPEC_NOSL:
			case SPEC_NOHE:
			case SPEC_NOSS:
			case SPEC_NOIH:
			case SPEC_NOMO:
			case SPEC_NOFR:
				return Short.class;
			case SPEC_VAL5:
				return Integer.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public int getRowCount() {
			return specimens.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			OSpecFinal thisRow = specimens.get(row);
			switch (col) {
			case SPEC_NAME:
				value = thisRow.name;
				break;
			case SPEC_NOBL:
				value = thisRow.noBlocks;
				break;
			case SPEC_NOSL:
				value = thisRow.noSlides;
				break;
			case SPEC_NOHE:
				value = thisRow.noHE;
				break;
			case SPEC_NOSS:
				value = thisRow.noSS;
				break;
			case SPEC_NOIH:
				value = thisRow.noIHC;
				break;
			case SPEC_NOMO:
				value = thisRow.noMOL;
				break;
			case SPEC_NOFR:
				value = thisRow.noFrags;
				break;
			case SPEC_VAL1:
				value = thisRow.value1;
				break;
			case SPEC_VAL2:
				value = thisRow.value2;
				break;
			case SPEC_VAL3:
				value = thisRow.value3;
				break;
			case SPEC_VAL4:
				value = thisRow.value4;
				break;
			default:
				value = thisRow.value5 / 60;
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			final String[] fields = { " AND FAID = ", " AND SYID = ", " AND SBID = ", " AND POID = " };
			int noRows = 0;
			String filter = "";
			ResultSet rst = null;
			try {
				pj.setBusy(true);
				programmaticChange = true;
				for (int i = 0; i < filters.length; i++) {
					if (filters[i] > 0) {
						filter += fields[i] + filters[i];
					}
				}
				if (filter.length() > 0) {
					filter = " WHERE " + filter.substring(5);
				}
				rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_CSE_SELECT, filter);
				while (rst.next()) {
					OCaseFinal thisRow = new OCaseFinal();
					thisRow.noSynop = rst.getByte("CASY");
					thisRow.noSpec = rst.getByte("CASP");
					thisRow.noFSSpec = rst.getByte("CAFS");
					thisRow.noBlocks = rst.getShort("CABL");
					thisRow.noSlides = rst.getShort("CASL");
					thisRow.noHE = rst.getShort("CAHE");
					thisRow.noSS = rst.getShort("CASS");
					thisRow.noIHC = rst.getShort("CAIH");
					thisRow.noMol = rst.getShort("CAMO");
					thisRow.grossTAT = rst.getShort("GRTA");
					thisRow.embedTAT = rst.getShort("EMTA");
					thisRow.microTAT = rst.getShort("MITA");
					thisRow.routeTAT = rst.getShort("ROTA");
					thisRow.finalTAT = rst.getShort("FNTA");
					thisRow.value5 = rst.getInt("CAV5");
					thisRow.caseID = rst.getLong("CAID");
					thisRow.value1 = rst.getDouble("CAV1");
					thisRow.value2 = rst.getDouble("CAV2");
					thisRow.value3 = rst.getDouble("CAV3");
					thisRow.value4 = rst.getDouble("CAV4");
					thisRow.caseNo = rst.getString("CANO");
					thisRow.facName = rst.getString("FANM");
					thisRow.spyName = rst.getString("SYNM");
					thisRow.subName = rst.getString("SBNM");
					thisRow.procName = rst.getString("PONM");
					thisRow.specName = rst.getString("SMNM");
					thisRow.grossName = rst.getString("GRNM");
					thisRow.embedName = rst.getString("EMNM");
					thisRow.microName = rst.getString("MINM");
					thisRow.routeName = rst.getString("RONM");
					thisRow.finalName = rst.getString("FNNM");
					thisRow.grossFull = rst.getString("GRFR").trim() + " " + rst.getString("GRLS").trim();
					thisRow.embedFull = rst.getString("EMFR").trim() + " " + rst.getString("EMLS").trim();
					thisRow.microFull = rst.getString("MIFR").trim() + " " + rst.getString("MILS").trim();
					thisRow.routeFull = rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim();
					thisRow.finalFull = rst.getString("FNFR").trim() + " " + rst.getString("FNLS").trim();
					thisRow.accessed.setTimeInMillis(rst.getTimestamp("ACED").getTime());
					thisRow.grossed.setTimeInMillis(rst.getTimestamp("GRED").getTime());
					thisRow.embeded.setTimeInMillis(rst.getTimestamp("EMED").getTime());
					thisRow.microed.setTimeInMillis(rst.getTimestamp("MIED").getTime());
					thisRow.routed.setTimeInMillis(rst.getTimestamp("ROED").getTime());
					thisRow.finaled.setTimeInMillis(rst.getTimestamp("FNED").getTime());
					cases.add(thisRow);
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(LConstants.SLEEP_TIME);
						} catch (InterruptedException ignore) {
						}
					}
				}
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.closeRst(rst);
				pj.setBusy(false);
				programmaticChange = false;
			}
			return null;
		}

		@Override
		public void done() {
			if (modelCase != null) {
				modelCase.fireTableDataChanged();
				modelAddl.fireTableDataChanged();
				modelFrozen.fireTableDataChanged();
				modelOrder.fireTableDataChanged();
				modelSpec.fireTableDataChanged();
			}
			pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(tblCase.getRowCount()));
		}
	}
}