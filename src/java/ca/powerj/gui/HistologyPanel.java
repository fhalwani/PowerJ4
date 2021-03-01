package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import ca.powerj.data.CaseData;
import ca.powerj.data.HistologyData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.swing.IChartBar2Yaxis;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class HistologyPanel extends BasePanel {
	private final byte FILTER_FAC = 0;
	private final byte FILTER_PRO = 1;
	private final byte FILTER_SUB = 2;
	private final byte ID_IN = 0;
	private final byte ID_OUT = 1;
	private final byte ID_PENDING = 2;
	private int[] filters = { 0, 0, 0 };
	private ArrayList<HistologyData> list = new ArrayList<HistologyData>();
	private ArrayList<Date> dates = new ArrayList<Date>();
	private IChartBar2Yaxis chartEmbed, chartMicro, chartRoute;

	HistologyPanel(AppFrame application) {
		super(application);
		setName("Histology");
		application.dbPowerJ.setStatements(LibConstants.ACTION_HISTOLOGY);
		getDates();
		createPanel();
		refresh();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		super.close();
		list.clear();
		dates.clear();
		if (chartEmbed != null) {
			chartEmbed.close();
		}
		if (chartMicro != null) {
			chartMicro.close();
		}
		if (chartRoute != null) {
			chartRoute.close();
		}
		return true;
	}

	private void createPanel() {
		Dimension dim = new Dimension(600, 200);
		chartEmbed = new IChartBar2Yaxis(dim);
		JPanel pnlEmbed = new JPanel();
		pnlEmbed.add(chartEmbed);
		JScrollPane scrollEmbed = IUtilities.createJScrollPane(pnlEmbed);
		scrollEmbed.setMinimumSize(dim);
		chartMicro = new IChartBar2Yaxis(dim);
		JPanel pnlMicro = new JPanel();
		pnlMicro.add(chartMicro);
		JScrollPane scrollMicro = IUtilities.createJScrollPane(pnlMicro);
		scrollMicro.setMinimumSize(dim);
		chartRoute = new IChartBar2Yaxis(dim);
		JPanel pnlRoute = new JPanel();
		pnlRoute.add(chartRoute);
		JScrollPane scrollRoute = IUtilities.createJScrollPane(pnlRoute);
		scrollRoute.setMinimumSize(dim);
		JSplitPane splitTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTop.setTopComponent(chartEmbed);
		splitTop.setBottomComponent(chartMicro);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(350);
		splitTop.setPreferredSize(new Dimension(600, 500));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(splitTop);
		splitAll.setBottomComponent(chartRoute);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(750);
		splitAll.setPreferredSize(new Dimension(600, 1200));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(application), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
		setSize(700, 900);
	}

	private void getDates() {
		Date endDate = new Date(System.currentTimeMillis());
		do {
			Date thisDate = new Date(endDate.getTime());
			dates.add(thisDate);
			endDate.setTime(application.dates.getPreviousBusinessDay(thisDate.getTime()));
		} while (dates.size() < 7);
		Collections.sort(dates, new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return (o1.getTime() > o2.getTime() ? 1 : (o1.getTime() < o2.getTime() ? -1 : 0));
			}
		});
	}

	@Override
	void refresh() {
		application.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	public void setFilter(short id, int value) {
		switch (id) {
		case IToolBar.TB_FAC:
			filters[FILTER_FAC] = value;
			break;
		case IToolBar.TB_PRO:
			filters[FILTER_PRO] = value;
			break;
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		default:
			application.log(LibConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		// Must initialize a new instance each time
		WorkerFilter worker = new WorkerFilter();
		worker.execute();
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			boolean included = false;
			long startDate = dates.get(0).getTime();
			HistologyData row = new HistologyData();
			CaseData item = new CaseData();
			setName("WorkerData");
			list.clear();
			ArrayList<CaseData> rows = application.dbPowerJ.getPendings(LibConstants.STATUS_FINAL);
			for (int i = 0; i < rows.size(); i++) {
				item = rows.get(i);
				row = new HistologyData();
				row.setStatusID(item.getStatusID());
				included = false;
				if (row.getStatusID() > LibConstants.STATUS_ACCES) {
					if (item.getGrossTime() > startDate) {
						row.setGrossed((byte) application.dates.getBusinessDays(startDate, item.getGrossTime()));
						included = true;
					}
				}
				if (row.getStatusID() > LibConstants.STATUS_GROSS) {
					if (item.getEmbedTime() > startDate) {
						row.setEmbeded((byte) application.dates.getBusinessDays(startDate, item.getEmbedTime()));
						included = true;
					}
				}
				if (row.getStatusID() > LibConstants.STATUS_EMBED) {
					if (item.getMicroTime() > startDate) {
						row.setMicroed((byte) application.dates.getBusinessDays(startDate, item.getMicroTime()));
						included = true;
					}
				}
				if (row.getStatusID() > LibConstants.STATUS_MICRO) {
					if (item.getRouteTime() > startDate) {
						row.setRouted((byte) application.dates.getBusinessDays(startDate, item.getRouteTime()));
						included = true;
					}
				}
				if (included) {
					row.setSubID(item.getSubID());
					row.setProcID(item.getProcID());
					row.setFacID(item.getFacID());
					row.setNoBlocks(item.getNoBlocks());
					row.setNoSlides(item.getNoSlides());
					list.add(row);
				}
			}
			return null;
		}

		@Override
		public void done() {
			WorkerFilter worker = new WorkerFilter();
			worker.execute();
		}
	}

	private class WorkerFilter extends SwingWorker<Void, Void> {
		volatile double[][] embeding = new double[3][dates.size()];
		volatile double[][] microtomy = new double[3][dates.size()];
		volatile double[][] routing = new double[3][dates.size()];

		@Override
		protected Void doInBackground() throws Exception {
			double[] pending = { 0, 0, 0 };
			if (dates.size() == 0) {
				return null;
			}
			for (HistologyData row : list) {
				if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == row.getFacID()) {
					if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == row.getSubID()) {
						if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == row.getProcID()) {
							switch (row.getStatusID()) {
							case LibConstants.STATUS_GROSS:
								pending[0] += row.getNoBlocks();
								break;
							case LibConstants.STATUS_EMBED:
								pending[1] += row.getNoBlocks();
								break;
							case LibConstants.STATUS_MICRO:
								pending[2] += row.getNoSlides();
								break;
							default:
							}
							embeding[ID_IN][row.getGrossed()] += row.getNoBlocks();
							if (row.getStatusID() > LibConstants.STATUS_GROSS) {
								embeding[ID_OUT][row.getEmbeded()] += row.getNoBlocks();
								microtomy[ID_IN][row.getEmbeded()] += row.getNoBlocks();
							}
							if (row.getStatusID() > LibConstants.STATUS_EMBED) {
								microtomy[ID_OUT][row.getMicroed()] += row.getNoBlocks();
								routing[ID_IN][row.getMicroed()] += row.getNoSlides();
							}
							if (row.getStatusID() > LibConstants.STATUS_MICRO) {
								routing[ID_OUT][row.getRouted()] += row.getNoSlides();
							}
						}
					}
				}
			}
			embeding[ID_PENDING][dates.size() - 1] = pending[0];
			microtomy[ID_PENDING][dates.size() - 1] = pending[1];
			routing[ID_PENDING][dates.size() - 1] = pending[2];
			for (int i = dates.size() - 2; i >= 0; i--) {
				embeding[ID_PENDING][i] = embeding[ID_PENDING][i + 1] + embeding[ID_OUT][i + 1]
						- embeding[ID_IN][i + 1];
				microtomy[ID_PENDING][i] = microtomy[ID_PENDING][i + 1] + microtomy[ID_OUT][i + 1]
						- microtomy[ID_IN][i + 1];
				routing[ID_PENDING][i] = routing[ID_PENDING][i + 1] + routing[ID_OUT][i + 1] - routing[ID_IN][i + 1];
				if (embeding[ID_PENDING][i] < 0) {
					embeding[ID_PENDING][i] = 0;
				}
				if (microtomy[ID_PENDING][i] < 0) {
					microtomy[ID_PENDING][i] = 0;
				}
				if (routing[ID_PENDING][i] < 0) {
					routing[ID_PENDING][i] = 0;
				}
			}
			return null;
		}

		@Override
		public void done() {
			if (dates.size() > 0) {
				// Since the earliest date is used as a buffer, we drop it
				String[] xDates = new String[dates.size() - 1];
				double[][] yEmbed = new double[3][dates.size() - 1];
				double[][] yMicro = new double[3][dates.size() - 1];
				double[][] yRoute = new double[3][dates.size() - 1];
				for (byte i = 0; i < xDates.length; i++) {
					xDates[i] = application.dates.formatter(dates.get(i + 1), LibDates.FORMAT_DATESHORT);
					for (byte j = 0; j < 3; j++) {
						yEmbed[j][i] = embeding[j][i + 1];
						yMicro[j][i] = microtomy[j][i + 1];
						yRoute[j][i] = routing[j][i + 1];
					}
				}
				String[] legend = { "In", "Out", "Pending" };
				chartEmbed.setChart(xDates, legend, yEmbed, "Embedding Blocks");
				chartMicro.setChart(xDates, legend, yMicro, "Microtomy Blocks");
				chartRoute.setChart(xDates, legend, yRoute, "Routing Slides");
			}
			application.display("Next update "
					+ application.dates.formatter(application.getNextUpdate(), LibDates.FORMAT_DATETIME));
			application.setBusy(false);
		}
	}
}