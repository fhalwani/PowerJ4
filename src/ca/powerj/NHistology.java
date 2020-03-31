package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

class NHistology extends NBase {
	private final byte FILTER_FAC = 0;
	private final byte FILTER_PRO = 1;
	private final byte FILTER_SUB = 2;
	private final byte ID_IN = 0;
	private final byte ID_OUT = 1;
	private final byte ID_PENDING = 2;
	private short[] filters = { 0, 0, 0 };
	private ArrayList<OCase> rows = new ArrayList<OCase>();
	private ArrayList<Date> dates = new ArrayList<Date>();
	private IChartBar2Y chartEmbed, chartMicro, chartRoute;

	NHistology(AClient parent) {
		super(parent);
		setName("Histology");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_HISTOLOGY);
		getDates();
		createPanel();
		refresh();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		rows.clear();
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
		chartEmbed = new IChartBar2Y(dim);
		JPanel pnlEmbed = new JPanel();
		pnlEmbed.add(chartEmbed);
		JScrollPane scrollEmbed = IGUI.createJScrollPane(pnlEmbed);
		scrollEmbed.setMinimumSize(dim);
		chartMicro = new IChartBar2Y(dim);
		JPanel pnlMicro = new JPanel();
		pnlMicro.add(chartMicro);
		JScrollPane scrollMicro = IGUI.createJScrollPane(pnlMicro);
		scrollMicro.setMinimumSize(dim);
		chartRoute = new IChartBar2Y(dim);
		JPanel pnlRoute = new JPanel();
		pnlRoute.add(chartRoute);
		JScrollPane scrollRoute = IGUI.createJScrollPane(pnlRoute);
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
		add(new IToolBar(this), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
		setSize(700, 900);
	}

	private void getDates() {
		Date endDate = new Date(System.currentTimeMillis());
		do {
			Date thisDate = new Date(endDate.getTime());
			dates.add(thisDate);
			endDate.setTime(pj.dates.getPreviousBusinessDay(thisDate.getTime()));
		} while (dates.size() < 7);
		Collections.sort(dates, new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return (o1.getTime() > o2.getTime() ? 1 : -1);
			}
		});
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
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		default:
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
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
			OCase row = new OCase();
			ResultSet rst = null;
			try {
				rows.clear();
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PND_SELECT));
				while (rst.next()) {
					if (rst.getByte("PNST") == OCaseStatus.ID_ACCES) {
						continue;
					}
					included = false;
					row = new OCase();
					row.statusID = rst.getByte("PNST");
					if (row.statusID > OCaseStatus.ID_ACCES) {
						if (rst.getTimestamp("GRED").getTime() > startDate) {
							row.grossed = (byte) pj.dates.getBusinessDays(startDate,
									rst.getTimestamp("GRED").getTime());
							included = true;
						}
					}
					if (row.statusID > OCaseStatus.ID_GROSS) {
						if (rst.getTimestamp("EMED").getTime() > startDate) {
							row.embeded = (byte) pj.dates.getBusinessDays(startDate,
									rst.getTimestamp("EMED").getTime());
							included = true;
						}
					}
					if (row.statusID > OCaseStatus.ID_EMBED) {
						if (rst.getTimestamp("MIED").getTime() > startDate) {
							row.microed = (byte) pj.dates.getBusinessDays(startDate,
									rst.getTimestamp("MIED").getTime());
							included = true;
						}
					}
					if (row.statusID > OCaseStatus.ID_MICRO) {
						if (rst.getTimestamp("ROED").getTime() > startDate) {
							row.routed = (byte) pj.dates.getBusinessDays(startDate, rst.getTimestamp("ROED").getTime());
							included = true;
						}
					}
					if (included) {
						row.subID = rst.getByte("SBID");
						row.procID = rst.getByte("POID");
						row.facID = rst.getShort("FAID");
						row.noBlocks = rst.getShort("PNBL");
						row.noSlides = rst.getShort("PNSL");
						rows.add(row);
					}
				}
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
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
			for (OCase row : rows) {
				if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == row.facID) {
					if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == row.subID) {
						if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == row.procID) {
							switch (row.statusID) {
							case OCaseStatus.ID_GROSS:
								pending[0] += row.noBlocks;
								break;
							case OCaseStatus.ID_EMBED:
								pending[1] += row.noBlocks;
								break;
							case OCaseStatus.ID_MICRO:
								pending[2] += row.noSlides;
								break;
							default:
							}
							embeding[ID_IN][row.grossed] += row.noBlocks;
							if (row.statusID > OCaseStatus.ID_GROSS) {
								embeding[ID_OUT][row.embeded] += row.noBlocks;
								microtomy[ID_IN][row.embeded] += row.noBlocks;
							}
							if (row.statusID > OCaseStatus.ID_EMBED) {
								microtomy[ID_OUT][row.microed] += row.noBlocks;
								routing[ID_IN][row.microed] += row.noSlides;
							}
							if (row.statusID > OCaseStatus.ID_MICRO) {
								routing[ID_OUT][row.routed] += row.noSlides;
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
					xDates[i] = pj.dates.formatter(dates.get(i + 1), LDates.FORMAT_DATESHORT);
					for (byte j = 0; j < 3; j++) {
						yEmbed[j][i] = embeding[j][i + 1];
						yMicro[j][i] = microtomy[j][i + 1];
						yRoute[j][i] = routing[j][i + 1];
					}
					String[] legend = { "In", "Out", "Pending" };
					chartEmbed.setChart(xDates, legend, yEmbed, "Embedding Blocks");
					chartMicro.setChart(xDates, legend, yMicro, "Microtomy Blocks");
					chartRoute.setChart(xDates, legend, yRoute, "Routing Slides");
				}
			}
			pj.setBusy(false);
		}
	}

	private class OCase {
		byte statusID = 0;
		byte subID = 0;
		byte procID = 0;
		byte grossed = 0; // How many days ago the case was grossed
		byte embeded = 0; // How many days ago the case was embedded
		byte microed = 0;
		byte routed = 0;
		short facID = 0;
		short noBlocks = 0;
		short noSlides = 0;
	}
}