package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

class NTurnaround extends NBase {
	private final String[] aMonths = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
			"Dec" };
	private short[] filters = { 0, 0, 0, 0, 0 };
	private int firstYear = 9999;
	private ArrayList<OTurnSum> rows = new ArrayList<OTurnSum>();
	private HashMap<Integer, String> years = new HashMap<Integer, String>();
	private HashMap<Integer, String> months = new HashMap<Integer, String>();
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
		months.clear();
		years.clear();
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
		int year = 0;
		int month = 0;
		String str = "";
		OTurnSum row = new OTurnSum();
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_TAT));
		firstYear = 9999;
		try {
			while (rst.next()) {
				row = new OTurnSum();
				row.spyID = rst.getByte("SYID");
				row.subID = rst.getByte("SBID");
				row.proID = rst.getByte("POID");
				row.month = rst.getByte("FNMONTH");
				row.year = rst.getShort("FNYEAR");
				row.facID = rst.getShort("FAID");
				row.qty = rst.getInt("CASES");
				row.gross = rst.getInt("GRTA");
				row.embed = rst.getInt("EMTA");
				row.micro = rst.getInt("MITA");
				row.route = rst.getInt("ROTA");
				row.diagn = rst.getInt("FNTA");
				// Some are not initialized (December 31, 1969)
				if (row.diagn < 0)
					row.diagn = 0;
				if (row.route < 0)
					row.route = 0;
				if (row.micro < 0)
					row.micro = 0;
				if (row.embed < 0)
					row.embed = 0;
				if (row.gross < 0)
					row.gross = 0;
				rows.add(row);
				if (year != row.year) {
					year = row.year;
					str = years.get(year);
					if (str == null) {
						years.put(year, Integer.toString(year));
					}
					if (firstYear > year) {
						firstYear = year;
					}
				}
				if (month != row.month) {
					month = row.month;
					str = months.get(month);
					if (str == null) {
						months.put(month, Integer.toString(month));
					}
				}
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
		String[] aYears = new String[years.size()];
		for (byte b = 0; b < aYears.length; b++) {
			aYears[b] = years.get(firstYear + b);
		}
		double[][] totalYears = new double[aYears.length][2];
		double[][][] totalMonths = new double[aYears.length][aMonths.length][2];
		for (OTurnSum row : rows) {
			if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == row.facID) {
				if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == row.spyID) {
					if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == row.subID) {
						if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == row.proID) {
							totalYears[row.year - firstYear][0] += row.qty;
							totalMonths[row.year - firstYear][row.month - 1][0] += row.qty;
							switch (filters[FILTER_STA]) {
							case OCaseStatus.ID_GROSS:
								totalYears[row.year - firstYear][1] += row.gross;
								totalMonths[row.year - firstYear][row.month - 1][1] += row.gross;
								break;
							case OCaseStatus.ID_EMBED:
								totalYears[row.year - firstYear][1] += row.embed;
								totalMonths[row.year - firstYear][row.month - 1][1] += row.embed;
								break;
							case OCaseStatus.ID_MICRO:
								totalYears[row.year - firstYear][1] += row.micro;
								totalMonths[row.year - firstYear][row.month - 1][1] += row.micro;
								break;
							case OCaseStatus.ID_ROUTE:
							case OCaseStatus.ID_HISTO:
								totalYears[row.year - firstYear][1] += row.route;
								totalMonths[row.year - firstYear][row.month - 1][1] += row.route;
								break;
							default:
								totalYears[row.year - firstYear][1] += row.diagn;
								totalMonths[row.year - firstYear][row.month - 1][1] += row.diagn;
							}
						}
					}
				}
			}
		}
		double[] yCurrent = { 0 };
		double[] yYears = new double[aYears.length];
		double[][] yMonths = new double[aYears.length][aMonths.length];
		for (byte y = 0; y < aYears.length; y++) {
			if (totalYears[y][0] > 0) {
				yYears[y] = (int) (totalYears[y][1] / totalYears[y][0]);
				for (byte m = 0; m < aMonths.length; m++) {
					if (totalMonths[y][m][0] > 0) {
						yMonths[y][m] = totalMonths[y][m][1] / totalMonths[y][m][0];
						yCurrent[0] = yMonths[y][m];
					}
				}
			}
		}
		String[] xData = { pj.numbers.formatDouble(0, yCurrent[0]) };
		double maxData = 0;
		switch (filters[FILTER_STA]) {
		case OCaseStatus.ID_GROSS:
			maxData = 24;
			break;
		case OCaseStatus.ID_EMBED:
			maxData = 48;
			break;
		case OCaseStatus.ID_MICRO:
			maxData = 72;
			break;
		case OCaseStatus.ID_ROUTE:
			maxData = 96;
			break;
		case OCaseStatus.ID_HISTO:
			maxData = 96;
			break;
		default:
			maxData = 120;
		}
		while (maxData < yCurrent[0]) {
			maxData += 24;
		}
		if (yYears.length > 0) {
			chartBar.setChart(aYears, yYears, "Annual");
			chartLine.setChart(aMonths, aYears, yMonths, "Monthly");
			chartDial.setChart(xData, yCurrent, maxData, "Current");
		}
	}
}