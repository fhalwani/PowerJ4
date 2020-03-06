package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class NBacklog extends NBase {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_ACED = 2;
	private final byte CASE_FAC = 3;
	private final byte CASE_SPY = 4;
	private final byte CASE_SUB = 5;
	private final byte CASE_PROC = 6;
	private final byte CASE_STAT = 7;
	private final byte CASE_SPEC = 8;
	private final byte CASE_NOSP = 9;
	private final byte CASE_NOBL = 10;
	private final byte CASE_NOSL = 11;
	private final byte CASE_VAL5 = 12;
	private final byte CASE_CUTOFF = 13;
	private final byte CASE_PASSED = 14;
	private final byte CASE_DELAY = 15;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private final byte FILTER_PRO = 3;
	private final byte FILTER_STA = 4;
	private short[] filters = { 0, 0, 0, 0, 8 };
	private ModelBacklog model = null;
	private OTurnaround turnaround = new OTurnaround();
	private HashMap<Byte, OTurnaround> turnarounds = new HashMap<Byte, OTurnaround>();
	private OCasePending pending = new OCasePending();
	private ArrayList<OCasePending> pendings = new ArrayList<OCasePending>();
	private ITable tbl;
	private IChartPie chartPie;
	private IChartBar2Y chartBar;

	NBacklog(AClient parent) {
		super(parent);
		setName("Backlog");
		pj.dbPowerJ.prepareBacklog();
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
		if (chartBar != null) {
			chartBar.close();
		}
		if (chartPie != null) {
			chartPie.close();
		}
		return true;
	}

	private void createPanel() {
		model = new ModelBacklog();
		tbl = new ITable(pj, model);
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		// Define color column renderer
		tbl.getColumnModel().getColumn(CASE_DELAY).setCellRenderer(new IRenderColor(pj));
		// Set Row Counter Size
		tbl.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tbl.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		JScrollPane scrollTable = IGUI.createJScrollPane(tbl);
		scrollTable.setMinimumSize(new Dimension(1300, 400));
		Dimension dim = new Dimension(600, 400);
		chartPie = new IChartPie(dim);
		JScrollPane scrollPie = IGUI.createJScrollPane(chartPie);
		scrollPie.setMinimumSize(dim);
		chartBar = new IChartBar2Y(dim);
		JScrollPane scrollBar = IGUI.createJScrollPane(chartBar);
		scrollBar.setMinimumSize(dim);
		JSplitPane splitChart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitChart.setTopComponent(scrollPie);
		splitChart.setBottomComponent(scrollBar);
		splitChart.setOneTouchExpandable(true);
		splitChart.setDividerLocation(650);
		splitChart.setPreferredSize(new Dimension(1300, 400));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(splitChart);
		splitAll.setBottomComponent(scrollTable);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1300, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
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

	@Override
	void refresh() {
		pj.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	void setFilter(short id, short value) {
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
		// Must initialize a new instance each time
		WorkerFilter worker = new WorkerFilter();
		worker.execute();
	}

	private class ModelBacklog extends ITableModel {
		private final String[] columns = { "NO", "CASE", "ACCESS", "FAC", "SPY", "SUB", "PROC", "STATUS", "SPEC",
				"SPECS", "BLKS", "SLDS", pj.setup.getString(LSetup.VAR_V5_NAME), "CUTOFF", "SPENT", "%" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_ACED:
				return Calendar.class;
			case CASE_ROW:
			case CASE_NOBL:
			case CASE_NOSL:
			case CASE_CUTOFF:
			case CASE_PASSED:
			case CASE_DELAY:
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
			case CASE_ACED:
				value = pendings.get(row).accessed;
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
			ResultSet rst = null;
			try {
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
					pending.specimen = rst.getString("SMNM");
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
						pending.cutoff += turnaround.embed;
					}
					if (pending.statusID > OCaseStatus.ID_GROSS) {
						pending.embeded.setTimeInMillis(rst.getTimestamp("EMED").getTime());
						pending.cutoff += turnaround.micro;
					}
					if (pending.statusID > OCaseStatus.ID_EMBED) {
						pending.microed.setTimeInMillis(rst.getTimestamp("MIED").getTime());
						pending.cutoff += turnaround.route;
					}
					if (pending.statusID > OCaseStatus.ID_MICRO) {
						pending.routed.setTimeInMillis(rst.getTimestamp("ROED").getTime());
						pending.cutoff += turnaround.diagn;
					}
					if (pending.statusID > OCaseStatus.ID_ROUTE) {
						pending.finaled.setTimeInMillis(rst.getTimestamp("FNED").getTime());
						pending.finalName = rst.getString("FNNM");
						pending.finalFull = rst.getString("FNFR").trim() + " " + rst.getString("FNLS").trim();
					}
					if (pending.statusID == OCaseStatus.ID_FINAL) {
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
				}
				Collections.sort(pendings, new Comparator<OCasePending>() {
					@Override
					public int compare(OCasePending o1, OCasePending o2) {
						return (o1.delay > o2.delay ? -1 : (o1.delay == o2.delay ? 0 : 1));
					}
				});
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.closeRst(rst);
			}
			return null;
		}

		@Override
		public void done() {
			if (model != null) {
				// Display results
				model.fireTableDataChanged();
			}
			// Must initialize a new instance each time
			WorkerFilter worker = new WorkerFilter();
			worker.execute();
		}
	}

	private class WorkerFilter extends SwingWorker<Void, Void> {
		// Pie Chart Data Set
		String[] xPie = { "Red", "Amber", "Green" };
		double[] yPie;
		// Flow Chart Data Set
		String[] xDates;
		double[][] yDates;

		@Override
		protected Void doInBackground() throws Exception {
			OWorkday workday = new OWorkday();
			ArrayList<OWorkday> workdays = new ArrayList<OWorkday>();
			Calendar calStart = pj.dates.setMidnight(null);
			workday = new OWorkday();
			workday.date.setTime(calStart.getTimeInMillis());
			workday.name = pj.dates.formatter(workday.date, LDates.FORMAT_DATESHORT);
			workdays.add(workday);
			while (workdays.size() < 7) {
				calStart.setTimeInMillis(pj.dates.getPreviousBusinessDay(calStart));
				workday = new OWorkday();
				workday.date.setTime(calStart.getTimeInMillis());
				workday.name = pj.dates.formatter(workday.date, LDates.FORMAT_DATESHORT);
				workdays.add(workday);
			}
			Collections.sort(workdays, new Comparator<OWorkday>() {
				@Override
				public int compare(OWorkday o1, OWorkday o2) {
					return (o1.date.getTime() < o2.date.getTime() ? -1
							: (o1.date.getTime() > o2.date.getTime() ? 1 : 0));
				}
			});
			ArrayList<ArrayList<Double>> workflow = new ArrayList<ArrayList<Double>>();
			for (int x = 0; x < 3; x++) {
				workflow.add(new ArrayList<Double>());
				for (int y = 0; y < workdays.size(); y++) {
					workflow.get(x).add(0d);
				}
			}
			long start = 0;
			long end = 0;
			byte status = OCaseStatus.ID_ACCES;
			double counter = 0;
			yPie = new double[3];
			for (OCasePending pending : pendings) {
				if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == pending.facID) {
					if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == pending.spyID) {
						if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == pending.subID) {
							if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == pending.procID) {
								if (filters[FILTER_STA] == OCaseStatus.ID_ALL || filters[FILTER_STA] == pending.statusID
										|| (filters[FILTER_STA] == OCaseStatus.ID_HISTO
												&& pending.statusID > OCaseStatus.ID_ACCES
												&& pending.statusID < OCaseStatus.ID_ROUTE)) {
									if (pending.statusID < OCaseStatus.ID_FINAL) {
										if (pending.delay > 100) {
											yPie[0]++;
										} else if (pending.delay > 70) {
											yPie[1]++;
										} else {
											yPie[2]++;
										}
									}
								}
								switch (filters[FILTER_STA]) {
								case OCaseStatus.ID_HISTO:
									start = pending.grossed.getTimeInMillis();
									end = pending.routed.getTimeInMillis();
									status = OCaseStatus.ID_ROUTE;
									break;
								case OCaseStatus.ID_GROSS:
									start = pending.accessed.getTimeInMillis();
									end = pending.grossed.getTimeInMillis();
									status = OCaseStatus.ID_GROSS;
									break;
								case OCaseStatus.ID_EMBED:
									start = pending.grossed.getTimeInMillis();
									end = pending.embeded.getTimeInMillis();
									status = OCaseStatus.ID_EMBED;
									break;
								case OCaseStatus.ID_MICRO:
									start = pending.embeded.getTimeInMillis();
									end = pending.microed.getTimeInMillis();
									status = OCaseStatus.ID_MICRO;
									break;
								case OCaseStatus.ID_ROUTE:
									start = pending.microed.getTimeInMillis();
									end = pending.routed.getTimeInMillis();
									status = OCaseStatus.ID_ROUTE;
									break;
								case OCaseStatus.ID_DIAGN:
									start = pending.routed.getTimeInMillis();
									end = pending.finaled.getTimeInMillis();
									status = OCaseStatus.ID_DIAGN;
									break;
								case OCaseStatus.ID_FINAL:
									start = pending.routed.getTimeInMillis();
									end = pending.finaled.getTimeInMillis();
									status = OCaseStatus.ID_FINAL;
									break;
								default:
									start = pending.accessed.getTimeInMillis();
									end = pending.finaled.getTimeInMillis();
									status = OCaseStatus.ID_FINAL;
								}
								for (int x = workdays.size() - 1; x >= 0; x--) {
									if (start > workdays.get(x).date.getTime()) {
										counter = 1.0 + workflow.get(0).get(x);
										workflow.get(0).set(x, counter);
										break;
									}
								}
								if (pending.statusID < status) {
									counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
									workflow.get(2).set(workdays.size() - 1, counter);
								} else {
									for (int x = workdays.size() - 1; x >= 0; x--) {
										if (end > workdays.get(x).date.getTime()) {
											counter = 1.0 + workflow.get(1).get(x);
											workflow.get(1).set(x, counter);
											break;
										}
									}
								}
							}
						}
					}
				}
			}
			for (int x = workdays.size() - 2; x >= 0; x--) {
				counter = workflow.get(2).get(x + 1) + workflow.get(1).get(x + 1) - workflow.get(0).get(x + 1);
				workflow.get(2).set(x, counter);
			}
			// Flow Chart Data Set
			xDates = new String[workdays.size() - 1];
			yDates = new double[3][workdays.size() - 1];
			for (int x = 0; x < xDates.length; x++) {
				xDates[x] = workdays.get(x + 1).name;
				for (int y = 0; y < 3; y++) {
					yDates[y][x] = workflow.get(y).get(x + 1);
				}
			}
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		public void done() {
			if (chartPie != null) {
				// Display results
				chartPie.setChart(xPie, yPie, "Cases Status", IChartPie.COLOR_RAG);
				String[] legend = { "In", "Out", "Pending" };
				chartBar.setChart(xDates, legend, yDates, "Cases Workflow");
			}
			if (tbl != null) {
				RowFilter<AbstractTableModel, Integer> rowFilter = null;
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
				// Status filter is always on
				switch (filters[FILTER_STA]) {
				case OCaseStatus.ID_ALL:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).statusID < OCaseStatus.ID_FINAL);
						}
					};
					break;
				case OCaseStatus.ID_HISTO:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).statusID > OCaseStatus.ID_ACCES
									&& pendings.get(entry.getIdentifier()).statusID < OCaseStatus.ID_ROUTE);
						}
					};
					break;
				default:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).statusID == filters[FILTER_STA]);
						}
					};
				}
				rowFilters.add(rowFilter);
				if (rowFilters.size() > 0) {
					if (rowFilters.size() > 1) {
						// Add to the compound filter
						rowFilter = RowFilter.andFilter(rowFilters);
					}
					TableRowSorter<ModelBacklog> sorter = (TableRowSorter<ModelBacklog>) tbl.getRowSorter();
					sorter.setRowFilter(rowFilter);
					sorter.sort();
				}
			}
			pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(tbl.getRowCount()));
			pj.setBusy(false);
		}
	}
}