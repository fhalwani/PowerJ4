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
import javax.swing.SwingWorker;

class NDaily extends NBase {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_SPY = 2;
	private final byte CASE_SUB = 3;
	private final byte CASE_PROC = 4;
	private final byte CASE_SPEC = 5;
	private final byte CASE_NOSP = 6;
	private final byte CASE_NOBL = 7;
	private final byte CASE_NOSL = 8;
	private final byte CASE_VAL5 = 9;
	private final byte CASE_ACED = 10;
	private final byte CASE_ROED = 11;
	private final byte CASE_ROBY = 12;
	private final byte CASE_FIBY = 13;
	private final byte CASE_CUTOFF = 14;
	private final byte CASE_PASSED = 15;
	private final byte CASE_DELAY = 16;
	private int routeTime = 0;
	private HashMap<Byte, OTurnaround> turnarounds = new HashMap<Byte, OTurnaround>();
	private ArrayList<OCasePending> list = new ArrayList<OCasePending>();
	private ModelCases modelCases;
	private ITable tblCases;
	private IChartBar2Y chartBar;

	NDaily(AClient parent) {
		super(parent);
		setName("Daily");
		routeTime = parent.setup.getInt(LSetup.VAR_ROUTE_TIME);
		parent.dbPowerJ.prepareDaily();
		getTats();
		createPanel();
		refresh();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		list.clear();
		turnarounds.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		return true;
	}

	private void createPanel() {
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
						return list.get(m).routeFull;
					case CASE_FIBY:
						return list.get(m).finalFull;
					default:
						return null;
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblCases.addAncestorListener(new IFocusListener());
		// Define color column renderer
		tblCases.getColumnModel().getColumn(CASE_DELAY).setCellRenderer(new IRenderColor(pj));
		// Set Row Counter Size
		tblCases.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tblCases.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		Dimension dim = new Dimension(1200, 400);
		chartBar = new IChartBar2Y(dim);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblCases);
		scrollTable.setMinimumSize(dim);
		JScrollPane scrollChart = IGUI.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollTable);
		splitAll.setBottomComponent(scrollChart);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(splitAll, BorderLayout.CENTER);
	}

	private void getTats() {
		OTurnaround turnaround = new OTurnaround();
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
		// Must initialize a new instance each time
		WorkerList worker = new WorkerList();
		worker.execute();
	}

	private class ModelCases extends ITableModel {
		private final String[] columns = { "NO", "CASE", "SPY", "SUB", "PROC", "SPEC", "SPECS", "BLKS", "SLDS",
				pj.setup.getString(LSetup.VAR_V5_NAME), "ACCESS", "ROUTE", "BY", "TO", "CUTOFF", "passed", "%" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_ACED:
			case CASE_ROED:
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
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			switch (col) {
			case CASE_ROW:
				value = tblCases.convertRowIndexToView(row) + 1;
				break;
			case CASE_NO:
				value = list.get(row).caseNo;
				break;
			case CASE_SPY:
				value = list.get(row).specialty;
				break;
			case CASE_SUB:
				value = list.get(row).subspecial;
				break;
			case CASE_PROC:
				value = list.get(row).procedure;
				break;
			case CASE_SPEC:
				value = list.get(row).specimen;
				break;
			case CASE_NOSP:
				value = list.get(row).noSpec;
				break;
			case CASE_NOBL:
				value = list.get(row).noBlocks;
				break;
			case CASE_NOSL:
				value = list.get(row).noSlides;
				break;
			case CASE_VAL5:
				value = list.get(row).value5 / 60;
				break;
			case CASE_CUTOFF:
				value = list.get(row).cutoff;
				break;
			case CASE_PASSED:
				value = list.get(row).passed;
				break;
			case CASE_DELAY:
				value = list.get(row).delay;
				break;
			case CASE_ACED:
				value = list.get(row).accessed;
				break;
			case CASE_ROED:
				value = list.get(row).routed;
				break;
			case CASE_ROBY:
				value = list.get(row).routeName;
				break;
			case CASE_FIBY:
				value = list.get(row).finalName;
				break;
			default:
			}
			return value;
		}
	}

	private class WorkerList extends SwingWorker<Void, Void> {
		private ArrayList<OWorkflow> lstPersons = new ArrayList<OWorkflow>();

		@Override
		protected Void doInBackground() throws Exception {
			byte statusID = 0;
			short finalID = 0;
			int hours = routeTime / 3600000;
			int minutes = (routeTime % 3600000) / 60000;
			OWorkflow person = new OWorkflow();
			OCasePending pending = new OCasePending();
			OTurnaround turnaround = new OTurnaround();
			Calendar startFinal = Calendar.getInstance();
			Calendar startRoute = Calendar.getInstance();
			Calendar endFinal = Calendar.getInstance();
			Calendar endRoute = Calendar.getInstance();
			HashMap<Short, OWorkflow> mapPersons = new HashMap<Short, OWorkflow>();
			ResultSet rst = null;
			try {
				list.clear();
				mapPersons.clear();
				// Cases routed from yesterday till today at cutoff time
				endRoute.set(Calendar.HOUR, hours);
				endRoute.set(Calendar.MINUTE, minutes);
				endRoute.set(Calendar.SECOND, 0);
				startRoute.setTimeInMillis(pj.dates.getPreviousBusinessDay(endRoute));
				startRoute.set(Calendar.HOUR, hours);
				startRoute.set(Calendar.MINUTE, minutes);
				startRoute.set(Calendar.SECOND, 0);
				// Cases finalized today all day
				startFinal.set(Calendar.HOUR, 0);
				startFinal.set(Calendar.MINUTE, 0);
				startFinal.set(Calendar.SECOND, 0);
				rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_PND_SELECT);
				while (rst.next()) {
					statusID = rst.getByte("PNST");
					finalID = rst.getShort("FNID");
					if (finalID < 1)
						continue;
					if (statusID > OCaseStatus.ID_MICRO && statusID < OCaseStatus.ID_FINAL) {
						if (finalID == pj.userID) {
							pending = new OCasePending();
							pending.turID = rst.getByte("TAID");
							pending.noSpec = rst.getByte("PNSP");
							pending.noBlocks = rst.getShort("PNBL");
							pending.noSlides = rst.getShort("PNSL");
							pending.value5 = rst.getInt("PNV5");
							pending.caseNo = rst.getString("PNNO");
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
							pending.passed = pj.dates.getBusinessHours(pending.accessed, endFinal);
							turnaround = turnarounds.get(pending.turID);
							pending.cutoff = (short) (turnaround.gross + turnaround.embed + turnaround.micro
									+ turnaround.route + turnaround.diagn);
							if (pending.cutoff > 0) {
								pending.delay = (short) ((100 * pending.passed) / pending.cutoff);
							}
							list.add(pending);
						}
						if (person.prsID != finalID) {
							person = mapPersons.get(finalID);
							if (person == null) {
								person = new OWorkflow();
								person.prsID = finalID;
								if (pj.userAccess[LConstants.ACCESS_NAMES] || finalID == pj.userID) {
									// Else, leave blank to hide names later
									person.name = rst.getString("FNNM");
								}
								mapPersons.put(finalID, person);
							}
						}
						person.noPending += rst.getInt("PNV5");
					} else if (statusID == OCaseStatus.ID_FINAL) {
						if (startFinal.getTimeInMillis() < rst.getTimestamp("FNED").getTime()) {
							if (person.prsID != finalID) {
								person = mapPersons.get(finalID);
								if (person == null) {
									person = new OWorkflow();
									person.prsID = finalID;
									if (pj.userAccess[LConstants.ACCESS_NAMES] || finalID == pj.userID) {
										person.name = rst.getString("FNNM");
									}
									mapPersons.put(finalID, person);
								}
							}
							person.noOut += rst.getInt("PNV5");
						}
					}
					if (startRoute.getTimeInMillis() < rst.getTimestamp("ROED").getTime()) {
						if (endRoute.getTimeInMillis() > rst.getTimestamp("ROED").getTime()) {
							if (person.prsID != finalID) {
								person = mapPersons.get(finalID);
								if (person == null) {
									person = new OWorkflow();
									person.prsID = finalID;
									if (pj.userAccess[LConstants.ACCESS_NAMES] || finalID == pj.userID) {
										person.name = rst.getString("FNNM");
									}
									mapPersons.put(finalID, person);
								}
							}
							person.noIn += rst.getInt("PNV5");
						}
					}
				}
				int i = 1;
				for (Entry<Short, OWorkflow> entry : mapPersons.entrySet()) {
					person = entry.getValue();
					if (person.name.length() == 0) {
						person.name = "P" + i;
						i++;
					}
					lstPersons.add(person);
				}
				Collections.sort(lstPersons, new Comparator<OWorkflow>() {
					@Override
					public int compare(OWorkflow o1, OWorkflow o2) {
						return (o1.noPending > o2.noPending ? -1 : (o1.noPending == o2.noPending ? 0 : 1));
					}
				});
				Collections.sort(list, new Comparator<OCasePending>() {
					@Override
					public int compare(OCasePending o1, OCasePending o2) {
						return (o1.delay > o2.delay ? -1 : (o1.delay == o2.delay ? 0 : 1));
					}
				});
				while (lstPersons.size() > 25) {
					lstPersons.remove(lstPersons.size() - 1);
				}
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.closeRst(rst);
				mapPersons.clear();
			}
			return null;
		}

		@Override
		public void done() {
			if (modelCases != null) {
				modelCases.fireTableDataChanged();
			}
			if (lstPersons.size() > 0) {
				String[] legend = { "In", "Out", "Pending" };
				String[] xData = new String[lstPersons.size()];
				double[][] yData = new double[3][lstPersons.size()];
				for (int i = 0; i < lstPersons.size(); i++) {
					xData[i] = lstPersons.get(i).name;
					yData[0][i] = lstPersons.get(i).noIn / 60;
					yData[1][i] = lstPersons.get(i).noOut / 60;
					yData[2][i] = lstPersons.get(i).noPending / 60;
				}
				chartBar.setChart(xData, legend, yData, "Today's Workflow");
				lstPersons.clear();
			}
		}
	}
}