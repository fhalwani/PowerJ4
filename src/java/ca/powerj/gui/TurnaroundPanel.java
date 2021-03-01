package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import ca.powerj.data.TurnaroundSum;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IChartBar;
import ca.powerj.swing.IChartDial;
import ca.powerj.swing.IChartLine;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class TurnaroundPanel extends BasePanel {
	private final String[] aMonths = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private int[] filters = { 0, 0, 0, 0, 0 };
	private int yearFrom = 9999;
	private int yearTo = 0;
	private ArrayList<TurnaroundSum> turnarounds = new ArrayList<TurnaroundSum>();
	private IChartDial chartDial;
	private IChartBar chartBar;
	private IChartLine chartLine;

	TurnaroundPanel(AppFrame application) {
		super(application);
		setName("Turnaround");
		application.dbPowerJ.setStatements(LibConstants.ACTION_TURNAROUND);
		createPanel();
		getData();
		setFilter(IToolBar.TB_FAC, (short) 0);
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		super.close();
		turnarounds.clear();
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
		JScrollPane scrollCurrent = IUtilities.createJScrollPane(chartDial);
		scrollCurrent.setMinimumSize(dim);
		dim = new Dimension(600, 400);
		chartBar = new IChartBar(dim);
		JScrollPane scrollYears = IUtilities.createJScrollPane(chartBar);
		scrollYears.setMinimumSize(dim);
		dim = new Dimension(1050, 400);
		chartLine = new IChartLine(dim);
		JScrollPane scrollMonths = IUtilities.createJScrollPane(chartLine);
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
		add(new IToolBar(application), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void getData() {
		yearFrom = 9999;
		yearTo = 0;
		turnarounds = application.dbPowerJ.getTurnaroundSum();
		for (int i = 0; i < turnarounds.size(); i++) {
			if (yearFrom > turnarounds.get(i).getYear())
				yearFrom = turnarounds.get(i).getYear();
			if (yearTo < turnarounds.get(i).getYear())
				yearTo = turnarounds.get(i).getYear();
		}
	}

	@Override
	public void setFilter(short id, int value) {
		final byte FILTER_FAC = 0;
		final byte FILTER_PRO = 1;
		final byte FILTER_SPY = 2;
		final byte FILTER_STA = 3;
		final byte FILTER_SUB = 4;
		// Maximum years for monthly is the last 5 years
		int noYears = (yearTo - yearFrom > 5 ? 5 : yearTo - yearFrom);
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
			application.log(LibConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		for (TurnaroundSum row : turnarounds) {
			if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == row.getFacID()) {
				if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == row.getSpyID()) {
					if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == row.getSubID()) {
						if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == row.getProID()) {
							totalYears[row.getYear() - yearFrom][0] += row.getQty();
							totalMonths[row.getYear() - yearFrom][row.getMonth() - 1][0] += row.getQty();
							switch (filters[FILTER_STA]) {
							case LibConstants.STATUS_GROSS:
								totalYears[row.getYear() - yearFrom][1] += row.getGross();
								totalMonths[row.getYear() - yearFrom][row.getMonth() - 1][1] += row.getGross();
								break;
							case LibConstants.STATUS_EMBED:
								totalYears[row.getYear() - yearFrom][1] += row.getEmbed();
								totalMonths[row.getYear() - yearFrom][row.getMonth() - 1][1] += row.getEmbed();
								break;
							case LibConstants.STATUS_MICRO:
								totalYears[row.getYear() - yearFrom][1] += row.getMicro();
								totalMonths[row.getYear() - yearFrom][row.getMonth() - 1][1] += row.getMicro();
								break;
							case LibConstants.STATUS_ROUTE:
							case LibConstants.STATUS_HISTO:
								totalYears[row.getYear() - yearFrom][1] += row.getRoute();
								totalMonths[row.getYear() - yearFrom][row.getMonth() - 1][1] += row.getRoute();
								break;
							default:
								totalYears[row.getYear() - yearFrom][1] += row.getDiagn();
								totalMonths[row.getYear() - yearFrom][row.getMonth() - 1][1] += row.getDiagn();
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
				if (y -noYears -1 >= 0) {
					aMonthlyYears[y -noYears -1] = aYears[y];
					for (byte m = 0; m < 12; m++) {
						if (totalMonths[y][m][0] > 0 && totalMonths[y][m][1] > 0) {
							yMonths[y -noYears -1][m] = totalMonths[y][m][1] / totalMonths[y][m][0];
							yCurrent[0] = yMonths[y -noYears -1][m];
							xCurrent[0] = aMonths[m] + " " + aMonthlyYears[y -noYears -1];
						}
					}
				}
			}
		}
		switch (filters[FILTER_STA]) {
		case LibConstants.STATUS_GROSS:
			maxCurrent = 24;
			break;
		case LibConstants.STATUS_EMBED:
			maxCurrent = 36;
			break;
		case LibConstants.STATUS_MICRO:
			maxCurrent = 48;
			break;
		case LibConstants.STATUS_ROUTE:
		case LibConstants.STATUS_HISTO:
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