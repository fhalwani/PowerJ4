package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

class NTurnaround extends NBase {
	private final String[] aMonths = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
			"Dec" };
	private short[] filters = { 0, 0, 0, 0, 0 };
	private int yearFrom = 9999;
	private int yearTo = 0;
	private ArrayList<OTurnSum> rows = new ArrayList<OTurnSum>();
	private IChartDial chartDial;
	private IChartBar chartBar;
	private IChartLine chartLine;

	NTurnaround(AClient parent) {
		super(parent);
		setName("Turnaround");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_TURNAROUND);
		createPanel();
		getData();
		setFilter(IToolBar.TB_FAC, (short) 0);
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		rows.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		if (chartDial != null) {
			chartDial.close();
		}
		if (chartLine != null) {
			chartLine.close();
		}
		return true;
	}

	private void createPanel() {
		Dimension dim = new Dimension(400, 400);
		chartDial = new IChartDial(dim);
		JScrollPane scrollCurrent = IGUI.createJScrollPane(chartDial);
		scrollCurrent.setMinimumSize(dim);
		dim = new Dimension(600, 400);
		chartBar = new IChartBar(dim);
		JScrollPane scrollYears = IGUI.createJScrollPane(chartBar);
		scrollYears.setMinimumSize(dim);
		dim = new Dimension(1050, 400);
		chartLine = new IChartLine(dim);
		JScrollPane scrollMonths = IGUI.createJScrollPane(chartLine);
		scrollMonths.setMinimumSize(dim);
		JSplitPane splitTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitTop.setTopComponent(scrollCurrent);
		splitTop.setBottomComponent(scrollYears);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(450);
		splitTop.setPreferredSize(new Dimension(1100, 400));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(splitTop);
		splitAll.setBottomComponent(scrollMonths);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void getData() {
		OTurnSum row = new OTurnSum();
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_TAT));
		yearFrom = 9999;
		try {
			while (rst.next()) {
				row = new OTurnSum();
				row.spyID = rst.getByte("syid");
				row.subID = rst.getByte("sbid");
				row.proID = rst.getByte("poid");
				row.month = rst.getByte("fnmonth");
				row.year = rst.getShort("fnyear");
				row.facID = rst.getShort("faid");
				row.qty = rst.getInt("CASES");
				row.gross = rst.getInt("grta");
				row.embed = rst.getInt("emta");
				row.micro = rst.getInt("mita");
				row.route = rst.getInt("rota");
				row.diagn = rst.getInt("fnta");
				if (yearFrom > row.year)
					yearFrom = row.year;
				if (yearTo < row.year)
					yearTo = row.year;
				rows.add(row);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void setFilter(short id, short value) {
		final byte FILTER_FAC = 0;
		final byte FILTER_PRO = 1;
		final byte FILTER_SPY = 2;
		final byte FILTER_STA = 3;
		final byte FILTER_SUB = 4;
		// Maximum years for monthly is the last 5 years
		int noYears = (yearTo - yearFrom > 4 ? 5 : yearTo - yearFrom);
		double maxCurrent = 0;
		double[] yCurrent = { 0 };
		double[] yYears = new double[yearTo - yearFrom + 1];
		double[][] yMonths = new double[noYears][aMonths.length];
		double[][] totalYears = new double[yearTo - yearFrom + 1][2];
		double[][][] totalMonths = new double[yearTo - yearFrom + 1][aMonths.length][2];
		String[] xCurrent = new String[1];
		String[] aYears = new String[yearTo - yearFrom + 1];
		String[] aMonthlyYears = new String[noYears];
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
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		default:
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		for (OTurnSum row : rows) {
			if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == row.facID) {
				if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == row.spyID) {
					if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == row.subID) {
						if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == row.proID) {
							totalYears[row.year - yearFrom][0] += row.qty;
							totalMonths[row.year - yearFrom][row.month - 1][0] += row.qty;
							switch (filters[FILTER_STA]) {
							case OCaseStatus.ID_GROSS:
								totalYears[row.year - yearFrom][1] += row.gross;
								totalMonths[row.year - yearFrom][row.month - 1][1] += row.gross;
								break;
							case OCaseStatus.ID_EMBED:
								totalYears[row.year - yearFrom][1] += row.embed;
								totalMonths[row.year - yearFrom][row.month - 1][1] += row.embed;
								break;
							case OCaseStatus.ID_MICRO:
								totalYears[row.year - yearFrom][1] += row.micro;
								totalMonths[row.year - yearFrom][row.month - 1][1] += row.micro;
								break;
							case OCaseStatus.ID_ROUTE:
							case OCaseStatus.ID_HISTO:
								totalYears[row.year - yearFrom][1] += row.route;
								totalMonths[row.year - yearFrom][row.month - 1][1] += row.route;
								break;
							default:
								totalYears[row.year - yearFrom][1] += row.diagn;
								totalMonths[row.year - yearFrom][row.month - 1][1] += row.diagn;
							}
						}
					}
				}
			}
		}
		for (byte y = 0; y < aYears.length; y++) {
			if (totalYears[y][0] > 0) {
				yYears[y] = (int) (totalYears[y][1] / totalYears[y][0]);
				aYears[y] = Integer.toString(yearFrom + y);
				if (y - noYears >= 0) {
					aMonthlyYears[y - noYears] = aYears[y];
					for (byte m = 0; m < aMonths.length; m++) {
						if (totalMonths[y][m][0] > 0) {
							yMonths[y - noYears][m] = totalMonths[y][m][1] / totalMonths[y][m][0];
							yCurrent[0] = yMonths[y - noYears][m];
						}
					}
				}
			}
		}
		xCurrent[0] = pj.numbers.formatDouble(0, yCurrent[0]);
		switch (filters[FILTER_STA]) {
		case OCaseStatus.ID_GROSS:
			maxCurrent = 24;
			break;
		case OCaseStatus.ID_EMBED:
			maxCurrent = 36;
			break;
		case OCaseStatus.ID_MICRO:
			maxCurrent = 48;
			break;
		case OCaseStatus.ID_ROUTE:
		case OCaseStatus.ID_HISTO:
			maxCurrent = 52;
			break;
		default:
			maxCurrent = 72;
		}
		while (maxCurrent < yCurrent[0]) {
			maxCurrent += 12;
		}
		if (yYears.length > 0) {
			chartBar.setChart(aYears, yYears, "Annual");
			chartLine.setChart(aMonths, aMonthlyYears, yMonths, "Monthly");
			chartDial.setChart(xCurrent, yCurrent, maxCurrent, "Current");
		}
	}
}