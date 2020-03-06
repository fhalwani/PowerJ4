package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class NRouting extends NBase {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_FAC = 2;
	private final byte CASE_SPY = 3;
	private final byte CASE_SUB = 4;
	private final byte CASE_PROC = 5;
	private final byte CASE_SPEC = 6;
	private final byte CASE_NOSP = 7;
	private final byte CASE_NOBL = 8;
	private final byte CASE_NOSL = 9;
	private final byte CASE_V5 = 10;
	private final byte CASE_ACED = 11;
	private final byte CASE_ROED = 12;
	private final byte CASE_ROBY = 13;
	private final byte CASE_FIBY = 14;
	private final byte SUM_ROW = 0;
	private final byte SUM_INIT = 1;
	private final byte SUM_NAME = 2;
	private final byte SUM_CASES = 3;
	private final byte SUM_SPECS = 4;
	private final byte SUM_SLIDES = 5;
	private final byte SUM_V5 = 6;
	private final byte SUM_FTE = 7;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private short[] filters = { 0, 0, 0 };
	private int routeTime = 0;
	private int rowIndex = 0;
	private double v5FTE = 0.0;
	private OCasePending pending = new OCasePending();
	private ArrayList<Calendar> dates = new ArrayList<Calendar>();
	private ArrayList<OCasePending> cases = new ArrayList<OCasePending>();
	private ArrayList<OWorkflow> summary = new ArrayList<OWorkflow>();
	private ModelDate modelDate;
	private ModelCases modelCases;
	private ModelSummary modelSummary;
	private ITable tblList, tblCases, tblSummary;
	private IChartBar chartBar;

	NRouting(AClient parent) {
		super(parent);
		setName("Routing");
		parent.dbPowerJ.prepareRoute();
		routeTime = parent.setup.getInt(LSetup.VAR_ROUTE_TIME);
		v5FTE = Double.parseDouble(parent.setup.getString(LSetup.VAR_V5_FTE)) / 215;
		if (v5FTE < 1.00) {
			v5FTE = 1.00;
		}
		getDates();
		refresh();
		createPanel();
		setFilter(IToolBar.TB_FAC, filters[0]);
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		cases.clear();
		summary.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		return true;
	}

	private void createPanel() {
		modelDate = new ModelDate();
		tblList = new ITable(pj, modelDate);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tblList.addAncestorListener(new IFocusListener());
		tblList.addFocusListener(this);
		tblList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				if (index > -1) {
					// else, Selection got filtered away.
					setRow(tblList.convertRowIndexToModel(index));
				}
			}
		});
		tblList.getColumnModel().getColumn(0).setMinWidth(120);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblList);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		modelCases = new ModelCases();
		tblCases = new ITable(pj, modelCases) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					switch (columnAtPoint(p)) {
					case CASE_ROBY:
						return cases.get(m).routeFull;
					case CASE_FIBY:
						return cases.get(m).finalFull;
					default:
						return null;
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblCases.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tblCases.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		modelSummary = new ModelSummary();
		tblSummary = new ITable(pj, modelSummary);
		tblSummary.getColumnModel().getColumn(SUM_ROW).setMinWidth(50);
		tblSummary.getColumnModel().getColumn(SUM_ROW).setMaxWidth(50);
		JScrollPane scrollCases = IGUI.createJScrollPane(tblCases);
		scrollCases.setMinimumSize(new Dimension(800, 200));
		JScrollPane scrollSummary = IGUI.createJScrollPane(tblSummary);
		scrollSummary.setMinimumSize(new Dimension(800, 200));
		Dimension dim = new Dimension(1000, 300);
		chartBar = new IChartBar(dim);
		JScrollPane scrollChart = IGUI.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		JSplitPane splitBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitBottom.setTopComponent(scrollSummary);
		splitBottom.setBottomComponent(scrollCases);
		splitBottom.setOneTouchExpandable(true);
		splitBottom.setDividerLocation(250);
		splitBottom.setPreferredSize(new Dimension(800, 500));
		JSplitPane splitTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTop.setTopComponent(scrollChart);
		splitTop.setBottomComponent(splitBottom);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(350);
		splitTop.setPreferredSize(new Dimension(800, 900));
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(splitTop);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getDates() {
		boolean startTomorrow = false;
		int hours = routeTime / 3600000;
		int minutes = (routeTime % 3600000) / 60000;
		Calendar tomorrow = Calendar.getInstance();
		Calendar yesterday = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();
		// Tomorrow may be a weekend or off-day
		tomorrow.setTimeInMillis(pj.dates.getNextBusinessDay(tomorrow));
		// Today may be a weekend or off-day
		yesterday.setTimeInMillis(pj.dates.getPreviousBusinessDay(tomorrow));
		if (endDate.get(Calendar.HOUR_OF_DAY) > hours) {
			startTomorrow = true;
		} else if (endDate.get(Calendar.HOUR_OF_DAY) == hours && endDate.get(Calendar.MINUTE) >= minutes) {
			startTomorrow = true;
		}
		if (startTomorrow) {
			endDate.setTimeInMillis(tomorrow.getTimeInMillis());
		} else {
			endDate.setTimeInMillis(yesterday.getTimeInMillis());
		}
		endDate.set(Calendar.HOUR, hours);
		endDate.set(Calendar.MINUTE, minutes);
		dates.clear();
		do {
			Calendar thisDate = Calendar.getInstance();
			thisDate.setTimeInMillis(endDate.getTimeInMillis());
			dates.add(thisDate);
			endDate.setTimeInMillis(pj.dates.getPreviousBusinessDay(thisDate));
			endDate.set(Calendar.HOUR, hours);
			endDate.set(Calendar.MINUTE, minutes);
		} while (dates.size() < 6);
	}

	@Override
	void refresh() {
		if (pj.isBusy())
			return;
		pj.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	void setFilter(short id, short value) {
		TableRowSorter<ModelCases> sorter = (TableRowSorter<ModelCases>) tblCases.getRowSorter();
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
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
		default:
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_FAC] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (cases.get(entry.getIdentifier()).facID == filters[FILTER_FAC]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SPY] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (cases.get(entry.getIdentifier()).spyID == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (cases.get(entry.getIdentifier()).subID == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (rowFilters.size() > 0) {
			// Add to the compound filter
			rowFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(rowFilters);
		}
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(tblCases.getRowCount()));
		// Must initialize a new instance each time
		WorkerSummary worker = new WorkerSummary();
		worker.execute();
	}

	private void setRow(int index) {
		if (index >= 0 && index < dates.size() - 1) {
			pj.setBusy(true);
			rowIndex = index;
			// Must initialize a new instance each time
			WorkerData worker = new WorkerData();
			worker.execute();
		}
	}

	private class ModelDate extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			return Calendar.class;
		}

		@Override
		public String getColumnName(int col) {
			return "Date";
		}

		@Override
		public int getRowCount() {
			return dates.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return dates.get(row);
		}
	}

	private class ModelCases extends ITableModel {
		private final String[] columns = { "NO", "CASE", "FAC", "SPY", "SUB", "PROC", "SPEC", "SPECS", "BLKS", "SLDS",
				pj.setup.getString(LSetup.VAR_V5_NAME), "ACCESS", "ROUTE", "BY", "TO" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_ACED:
			case CASE_ROED:
				return Calendar.class;
			case CASE_ROW:
			case CASE_NOBL:
			case CASE_NOSL:
				return Short.class;
			case CASE_NOSP:
				return Byte.class;
			case CASE_V5:
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
			if (row >= 0 && row < cases.size()) {
				switch (col) {
				case CASE_ROW:
					value = tblCases.convertRowIndexToView(row) + 1;
					break;
				case CASE_NO:
					value = cases.get(row).caseNo;
					break;
				case CASE_FAC:
					value = cases.get(row).facility;
					break;
				case CASE_SPY:
					value = cases.get(row).specialty;
					break;
				case CASE_SUB:
					value = cases.get(row).subspecial;
					break;
				case CASE_PROC:
					value = cases.get(row).procedure;
					break;
				case CASE_SPEC:
					value = cases.get(row).specimen;
					break;
				case CASE_NOSP:
					value = cases.get(row).noSpec;
					break;
				case CASE_NOBL:
					value = cases.get(row).noBlocks;
					break;
				case CASE_NOSL:
					value = cases.get(row).noSlides;
					break;
				case CASE_V5:
					value = cases.get(row).value5 / 60;
					break;
				case CASE_ACED:
					value = cases.get(row).accessed;
					break;
				case CASE_ROED:
					value = cases.get(row).routed;
					break;
				case CASE_ROBY:
					value = cases.get(row).routeName;
					break;
				case CASE_FIBY:
					value = cases.get(row).finalName;
					break;
				default:
				}
			}
			return value;
		}
	}

	private class ModelSummary extends ITableModel {
		private final String[] columns = { "NO", "INIT", "NAME", "CASES", "SPECS", "SLIDES",
				pj.setup.getString(LSetup.VAR_V5_NAME), "FTE" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case SUM_ROW:
			case SUM_CASES:
			case SUM_SPECS:
			case SUM_SLIDES:
				return Short.class;
			case SUM_V5:
				return Integer.class;
			case SUM_FTE:
				return Double.class;
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
			return summary.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (row >= 0 && row < summary.size()) {
				switch (col) {
				case SUM_ROW:
					value = tblSummary.convertRowIndexToView(row) + 1;
					break;
				case SUM_INIT:
					value = summary.get(row).name;
					break;
				case SUM_NAME:
					value = summary.get(row).full;
					break;
				case SUM_CASES:
					value = summary.get(row).noCases;
					break;
				case SUM_SPECS:
					value = summary.get(row).noSpecs;
					break;
				case SUM_SLIDES:
					value = summary.get(row).noSldes;
					break;
				case SUM_V5:
					// Display in minutes
					value = summary.get(row).value5 / 60;
					break;
				case SUM_FTE:
					value = summary.get(row).ftIn;
					break;
				default:
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			ResultSet rst = null;
			pj.setBusy(true);
			try {
				cases.clear();
				pj.dbPowerJ.setTime(DPowerJ.STM_PND_SL_ROU, 1, dates.get(rowIndex + 1).getTimeInMillis());
				pj.dbPowerJ.setTime(DPowerJ.STM_PND_SL_ROU, 2, dates.get(rowIndex).getTimeInMillis());
				rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_PND_SL_ROU);
				while (rst.next()) {
					pending = new OCasePending();
					pending.spyID = rst.getByte("SYID");
					pending.subID = rst.getByte("SBID");
					pending.noSpec = rst.getByte("PNSP");
					pending.finalID = rst.getShort("FNID");
					pending.facID = rst.getShort("FAID");
					pending.noBlocks = rst.getShort("PNBL");
					pending.noSlides = rst.getShort("PNSL");
					pending.value5 = rst.getInt("PNV5");
					pending.caseNo = rst.getString("PNNO");
					pending.facility = rst.getString("FANM");
					pending.specialty = rst.getString("SYNM");
					pending.subspecial = rst.getString("SBNM");
					pending.procedure = rst.getString("PONM");
					pending.specimen = rst.getString("SMNM");
					pending.routeName = rst.getString("RONM");
					pending.finalName = rst.getString("FNNM");
					pending.routeFull = rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim();
					pending.finalFull = rst.getString("FNFR").trim() + " " + rst.getString("FNLS").trim();
					pending.accessed.setTimeInMillis(rst.getTimestamp("ACED").getTime());
					pending.routed.setTimeInMillis(rst.getTimestamp("ROED").getTime());
					cases.add(pending);
				}
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.closeRst(rst);
			}
			return null;
		}

		@Override
		public void done() {
			if (modelCases != null) {
				// Display results
				modelCases.fireTableDataChanged();
			}
			pj.setBusy(false);
			// Must initialize a new instance each time
			WorkerSummary worker = new WorkerSummary();
			worker.execute();
		}
	}

	private class WorkerSummary extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			pj.setBusy(true);
			HashMap<Short, OWorkflow> hashMap = new HashMap<Short, OWorkflow>();
			OWorkflow staff = new OWorkflow();
			summary.clear();
			for (OCasePending pending : cases) {
				if (filters[0] == 0 || filters[0] == pending.facID) {
					if (filters[1] == 0 || filters[1] == pending.spyID) {
						if (filters[2] == 0 || filters[2] == pending.subID) {
							staff = hashMap.get(pending.finalID);
							if (staff == null) {
								staff = new OWorkflow();
								staff.prsID = pending.finalID;
								staff.name = pending.finalName;
								staff.full = pending.finalFull;
								hashMap.put(pending.finalID, staff);
							}
							staff.noCases++;
							staff.noSpecs += pending.noSpec;
							staff.noSldes += pending.noSlides;
							staff.value5 += pending.value5;
						}
					}
				}
			}
			for (Entry<Short, OWorkflow> entry : hashMap.entrySet()) {
				staff = entry.getValue();
				staff.ftIn = (1.00 * staff.value5 / v5FTE);
				summary.add(staff);
			}
			hashMap.clear();
			Collections.sort(summary, new Comparator<OWorkflow>() {
				@Override
				public int compare(OWorkflow ds1, OWorkflow ds2) {
					return (ds1.value5 < ds2.value5 ? -1 : (ds1.value5 == ds2.value5 ? 0 : 1));
				}
			});
			return null;
		}

		@Override
		public void done() {
			// Display results
			if (modelSummary != null) {
				modelSummary.fireTableDataChanged();
			}
			if (summary.size() > 0) {
				// Chart Data Set
				String[] x = new String[summary.size()];
				double[] y = new double[summary.size()];
				for (int i = 0; i < summary.size(); i++) {
					x[i] = summary.get(i).name;
					y[i] = summary.get(i).ftIn;
				}
				chartBar.setChart(x, y, pj.dates.formatter(dates.get(rowIndex), LDates.FORMAT_DATE));
			}
			pj.setBusy(false);
		}
	}
}