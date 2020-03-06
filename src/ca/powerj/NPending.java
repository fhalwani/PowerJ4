package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class NPending extends NBase {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_FAC = 2;
	private final byte CASE_SPY = 3;
	private final byte CASE_SUB = 4;
	private final byte CASE_PROC = 5;
	private final byte CASE_SPEC = 6;
	private final byte CASE_STAT = 7;
	private final byte CASE_VAL5 = 8;
	private final byte CASE_NOSP = 9;
	private final byte CASE_NOBL = 10;
	private final byte CASE_NOSL = 11;
	private final byte CASE_CUTOFF = 12;
	private final byte CASE_PASSED = 13;
	private final byte CASE_DELAY = 14;
	private final byte CASE_ACED = 15;
	private final byte CASE_GRED = 16;
	private final byte CASE_EMED = 17;
	private final byte CASE_MIED = 18;
	private final byte CASE_ROED = 19;
	private final byte CASE_FIED = 20;
	private final byte CASE_GRBY = 21;
	private final byte CASE_EMBY = 22;
	private final byte CASE_MIBY = 23;
	private final byte CASE_ROBY = 24;
	private final byte CASE_FIBY = 25;
	private final byte CASE_GRTA = 26;
	private final byte CASE_EMTA = 27;
	private final byte CASE_MITA = 28;
	private final byte CASE_ROTA = 29;
	private final byte CASE_FITA = 30;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private final byte FILTER_PRO = 3;
	private final byte FILTER_STA = 4;
	private short[] filters = { 0, 0, 0, 0, 8 };
	private ModelPending model;
	private OTurnaround turnaround = new OTurnaround();
	private HashMap<Byte, OTurnaround> turnarounds = new HashMap<Byte, OTurnaround>();
	private OCasePending pending = new OCasePending();
	private ArrayList<OCasePending> pendings = new ArrayList<OCasePending>();
	private ITable tbl;

	NPending(AClient parent) {
		super(parent);
		setName("Pending");
		parent.dbPowerJ.preparePending();
		getTats();
		createPanel();
		refresh();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		pendings.clear();
		turnarounds.clear();
		return true;
	}

	private void createPanel() {
		model = new ModelPending();
		tbl = new ITable(pj, model) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					try {
						Point p = e.getPoint();
						JTable tbl = (JTable) e.getSource();
						int col = columnAtPoint(p);
						int viewRow = rowAtPoint(p);
						int modelRow = tbl.convertRowIndexToModel(viewRow);
						tip = getTooltip(modelRow, col);
					} catch (ArrayIndexOutOfBoundsException ignore) {
					} catch (RuntimeException ignore) {
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		// Set Row Counter Size
		tbl.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tbl.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		JScrollPane scrollTable = IGUI.createJScrollPane(tbl);
		setLayout(new BorderLayout());
		add(new IToolBar(this), BorderLayout.NORTH);
		add(scrollTable, BorderLayout.CENTER);
	}

	private void getTats() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_TUR_SELECT);
		try {
			while (rst.next()) {
				turnaround = new OTurnaround();
				turnaround.turID = rst.getByte("TAID");
				turnaround.gross = rst.getShort("GRSS");
				turnaround.embed = rst.getShort("EMBD");
				turnaround.micro = rst.getShort("MICR");
				turnaround.route = rst.getShort("ROUT");
				turnaround.diagn = rst.getShort("FINL");
				turnarounds.put(turnaround.turID, turnaround);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private String getTooltip(int row, int col) {
		switch (col) {
		case CASE_GRBY:
			return pendings.get(row).grossFull;
		case CASE_EMBY:
			return pendings.get(row).embedFull;
		case CASE_MIBY:
			return pendings.get(row).microFull;
		case CASE_ROBY:
			return pendings.get(row).routeFull;
		case CASE_FIBY:
			return pendings.get(row).finalFull;
		default:
			return null;
		}
	}

	@Override
	void refresh() {
		pj.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	void setFilter(short id, short value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		switch (id) {
		case IToolBar.TB_FAC:
			filters[FILTER_FAC] = value;
			break;
		case IToolBar.TB_PRO:
			filters[FILTER_PRO] = value;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			break;
		case IToolBar.TB_STA:
			filters[FILTER_STA] = value;
			break;
		default:
			filters[FILTER_SUB] = value;
		}
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_FAC] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (pendings.get(entry.getIdentifier()).facID == filters[FILTER_FAC]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_PRO] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (pendings.get(entry.getIdentifier()).procID == filters[FILTER_PRO]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SPY] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (pendings.get(entry.getIdentifier()).spyID == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (pendings.get(entry.getIdentifier()).subID == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_STA] != OCaseStatus.ID_ALL) {
			if (filters[FILTER_STA] == OCaseStatus.ID_HISTO) {
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					@Override
					public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						return (pendings.get(entry.getIdentifier()).statusID > OCaseStatus.ID_ACCES
								&& pendings.get(entry.getIdentifier()).statusID < OCaseStatus.ID_ROUTE);
					}
				};
			} else {
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					@Override
					public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						return (pendings.get(entry.getIdentifier()).statusID == filters[FILTER_STA]);
					}
				};
			}
			rowFilters.add(rowFilter);
		}
		// Add to the compound filter
		rowFilter = RowFilter.andFilter(rowFilters);
		TableRowSorter<ModelPending> sorter = (TableRowSorter<ModelPending>) tbl.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(tbl.getRowCount()));
	}

	private class ModelPending extends ITableModel {
		private final String[] columns = { "NO", "CASE", "FAC", "SPY", "SUB", "PROC", "SPEC", "STATUS",
				pj.setup.getString(LSetup.VAR_V5_NAME), "SPECS", "BLKS", "SLDS", "CUTOFF", "SPENT", "%", "ACCESS",
				"GROSS", "EMBED", "MICRO", "ROUTE", "FINAL", "GRNM", "EMNM", "MINM", "RONM", "FINM", "GRTA", "EMTA",
				"MITA", "ROTA", "FITA" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_ACED:
			case CASE_GRED:
			case CASE_EMED:
			case CASE_MIED:
			case CASE_ROED:
			case CASE_FIED:
				return Calendar.class;
			case CASE_ROW:
			case CASE_NOBL:
			case CASE_NOSL:
			case CASE_CUTOFF:
			case CASE_PASSED:
			case CASE_DELAY:
			case CASE_GRTA:
			case CASE_EMTA:
			case CASE_MITA:
			case CASE_ROTA:
			case CASE_FITA:
				return Short.class;
			case CASE_NOSP:
				return Byte.class;
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
			return pendings.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			switch (col) {
			case CASE_ROW:
				value = tbl.convertRowIndexToView(row) + 1;
				break;
			case CASE_NO:
				value = pendings.get(row).caseNo;
				break;
			case CASE_FAC:
				value = pendings.get(row).facility;
				break;
			case CASE_SPY:
				value = pendings.get(row).specialty;
				break;
			case CASE_SUB:
				value = pendings.get(row).subspecial;
				break;
			case CASE_PROC:
				value = pendings.get(row).procedure;
				break;
			case CASE_STAT:
				value = pendings.get(row).status;
				break;
			case CASE_SPEC:
				value = pendings.get(row).specimen;
				break;
			case CASE_NOSP:
				value = pendings.get(row).noSpec;
				break;
			case CASE_NOBL:
				value = pendings.get(row).noBlocks;
				break;
			case CASE_NOSL:
				value = pendings.get(row).noSlides;
				break;
			case CASE_VAL5:
				value = pendings.get(row).value5 / 60;
				break;
			case CASE_CUTOFF:
				value = pendings.get(row).cutoff;
				break;
			case CASE_PASSED:
				value = pendings.get(row).passed;
				break;
			case CASE_DELAY:
				value = pendings.get(row).delay;
				break;
			case CASE_ACED:
				value = pendings.get(row).accessed;
				break;
			case CASE_GRED:
				value = pendings.get(row).grossed;
				break;
			case CASE_GRBY:
				value = pendings.get(row).grossName;
				break;
			case CASE_GRTA:
				value = pendings.get(row).grossTAT;
				break;
			case CASE_EMED:
				value = pendings.get(row).embeded;
				break;
			case CASE_EMBY:
				value = pendings.get(row).embedName;
				break;
			case CASE_EMTA:
				value = pendings.get(row).embedTAT;
				break;
			case CASE_MIED:
				value = pendings.get(row).microed;
				break;
			case CASE_MIBY:
				value = pendings.get(row).microName;
				break;
			case CASE_MITA:
				value = pendings.get(row).microTAT;
				break;
			case CASE_ROED:
				value = pendings.get(row).routed;
				break;
			case CASE_ROBY:
				value = pendings.get(row).routeName;
				break;
			case CASE_ROTA:
				value = pendings.get(row).routeTAT;
				break;
			case CASE_FIED:
				value = pendings.get(row).finaled;
				break;
			case CASE_FIBY:
				value = pendings.get(row).finalName;
				break;
			case CASE_FITA:
				value = pendings.get(row).finalTAT;
				break;
			default:
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			final String[] statuses = OCaseStatus.NAMES_ALL;
			int buffer = 0;
			int noRows = 0;
			ResultSet rst = null;
			try {
				pj.setBusy(true);
				programmaticChange = true;
				Calendar calToday = Calendar.getInstance();
				rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_PND_SELECT);
				pendings.clear();
				while (rst.next()) {
					pending = new OCasePending();
					pending.spyID = rst.getByte("SYID");
					pending.subID = rst.getByte("SBID");
					pending.procID = rst.getByte("POID");
					pending.turID = rst.getByte("TAID");
					pending.statusID = rst.getByte("PNST");
					pending.noSpec = rst.getByte("PNSP");
					pending.facID = rst.getShort("FAID");
					pending.noBlocks = rst.getShort("PNBL");
					pending.noSlides = rst.getShort("PNSL");
					pending.value5 = rst.getInt("PNV5");
					pending.caseNo = rst.getString("PNNO");
					pending.facility = rst.getString("FANM");
					pending.specialty = rst.getString("SYNM");
					pending.subspecial = rst.getString("SBNM");
					pending.procedure = rst.getString("PONM");
					pending.specimen = rst.getString("SMDC");
					pending.status = statuses[rst.getByte("PNST")];
					pending.accessed.setTimeInMillis(rst.getTimestamp("ACED").getTime());
					pending.grossed.setTimeInMillis(0);
					pending.embeded.setTimeInMillis(0);
					pending.microed.setTimeInMillis(0);
					pending.routed.setTimeInMillis(0);
					pending.finaled.setTimeInMillis(0);
					pending.cutoff = turnaround.gross;
					pending.passed = pj.dates.getBusinessHours(pending.accessed, calToday);
					turnaround = turnarounds.get(pending.turID);
					if (pending.statusID > OCaseStatus.ID_ACCES) {
						pending.grossed.setTimeInMillis(rst.getTimestamp("GRED").getTime());
						pending.grossTAT = rst.getShort("GRTA");
						pending.grossName = rst.getString("GRNM");
						pending.grossFull = rst.getString("GRFR").trim() + " " + rst.getString("GRLS").trim();
						pending.cutoff += turnaround.embed;
					}
					if (pending.statusID > OCaseStatus.ID_GROSS) {
						pending.embeded.setTimeInMillis(rst.getTimestamp("EMED").getTime());
						pending.embedTAT = rst.getShort("EMTA");
						pending.embedName = rst.getString("EMNM");
						pending.embedFull = rst.getString("EMFR").trim() + " " + rst.getString("EMLS").trim();
						pending.cutoff += turnaround.micro;
					}
					if (pending.statusID > OCaseStatus.ID_EMBED) {
						pending.microed.setTimeInMillis(rst.getTimestamp("MIED").getTime());
						pending.microTAT = rst.getShort("MITA");
						pending.microName = rst.getString("MINM");
						pending.microFull = rst.getString("MIFR").trim() + " " + rst.getString("MILS").trim();
						pending.cutoff += turnaround.route;
					}
					if (pending.statusID > OCaseStatus.ID_MICRO) {
						pending.routed.setTimeInMillis(rst.getTimestamp("ROED").getTime());
						pending.routeTAT = rst.getShort("ROTA");
						pending.routeName = rst.getString("RONM");
						pending.routeFull = rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim();
						pending.cutoff += turnaround.diagn;
					}
					if (pending.statusID > OCaseStatus.ID_ROUTE) {
						pending.finaled.setTimeInMillis(rst.getTimestamp("FNED").getTime());
						pending.finalName = rst.getString("FNNM");
						pending.finalFull = rst.getString("FNFR").trim() + " " + rst.getString("FNLS").trim();
					}
					if (pending.statusID == OCaseStatus.ID_FINAL) {
						pending.finalTAT = rst.getShort("FNTA");
						pending.passed = pending.finalTAT;
					}
					if (pending.cutoff > 0) {
						buffer = (100 * pending.passed) / pending.cutoff;
						if (buffer > Short.MAX_VALUE) {
							buffer = Short.MAX_VALUE;
						}
						pending.delay = (short) buffer;
					}
					pendings.add(pending);
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(LConstants.SLEEP_TIME);
						} catch (InterruptedException ignore) {
						}
					}
				}
//				Collections.sort(pendings, new Comparator<OCasePending>() {
//					@Override
//					public int compare(OCasePending o1, OCasePending o2) {
//						return (o1.delay > o2.delay ? -1 : (o1.delay == o2.delay ? 0 : 1));
//					}
//				});
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
			if (model != null) {
				// Display results
				model.fireTableDataChanged();
			}
		}
	}
}