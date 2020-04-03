package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

class NForecast extends NBase {
	private final byte DATA_NAMES = 0;
	private final byte DATA_CASES = 1;
	private final byte DATA_SPECS = 2;
	private final byte DATA_BLCKS = 3;
	private final byte DATA_SLIDE = 4;
	private final byte DATA_SL_HE = 5;
	private final byte DATA_SL_SS = 6;
	private final byte DATA_SL_IH = 7;
	private final byte DATA_SYNOP = 8;
	private final byte DATA_FROZN = 9;
	private final byte DATA_VALU1 = 10;
	private final byte DATA_VALU2 = 11;
	private final byte DATA_VALU3 = 12;
	private final byte DATA_VALU4 = 13;
	private final byte DATA_VALU5 = 14;
	private byte[] rowsView = new byte[4];
	private short facID = 0;
	private long timeFrom = 0;
	private long timeTo = 0;
	private String[] coders = new String[5];
	private OAnnualNode node = new OAnnualNode();
	private ModelList model;
	private TreePath treePath;
	private ITree tree;
	private ITable table;
	private IChart2Lines chartCases, chartSpecs, chartBlocks, chartSlides, chartFrozen, chartFTE;

	public NForecast(AClient parent) {
		super(parent);
		setName("Forecast");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_FORECAST);
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		if (chartCases != null) {
			chartCases.close();
		}
		if (chartSpecs != null) {
			chartSpecs.close();
		}
		if (chartBlocks != null) {
			chartBlocks.close();
		}
		if (chartSlides != null) {
			chartSlides.close();
		}
		if (chartFrozen != null) {
			chartFrozen.close();
		}
		if (chartFTE != null) {
			chartFTE.close();
		}
		return true;
	}

	private void createPanel() {
		coders[0] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		coders[1] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		coders[2] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		coders[3] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		coders[4] = pj.setup.getString(LSetup.VAR_V5_NAME);
		OAnnualNode node = new OAnnualNode();
		TreeNode rootNode = new DefaultMutableTreeNode(node);
		TreeModel treeModel = new DefaultTreeModel(rootNode);
		tree = new ITree(treeModel);
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setView(e.getNewLeadSelectionPath());
					}
				});
			}
		});
		JScrollPane scrollTree = IGUI.createJScrollPane(tree);
		scrollTree.setMinimumSize(new Dimension(250, 800));
		int[] nYears = { 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022 };
		model = new ModelList(getHeaders(nYears));
		table = new ITable(pj, model);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		for (int i = 1; i < 5; i++) {
			table.getColumnModel().getColumn(i).setMinWidth(100);
		}
		JScrollPane scrollTable = IGUI.createJScrollPane(table);
		scrollTable.setMinimumSize(new Dimension(600, 400));
		Dimension dim = new Dimension(200, 200);
		chartCases = new IChart2Lines(dim);
		chartSpecs = new IChart2Lines(dim);
		chartBlocks = new IChart2Lines(dim);
		chartSlides = new IChart2Lines(dim);
		chartFrozen = new IChart2Lines(dim);
		chartFTE = new IChart2Lines(dim);
		JPanel pnlChartTop = new JPanel();
		pnlChartTop.setMinimumSize(new Dimension(600, 200));
		pnlChartTop.setLayout(new BoxLayout(pnlChartTop, BoxLayout.X_AXIS));
		pnlChartTop.setOpaque(true);
		pnlChartTop.add(chartCases);
		pnlChartTop.add(chartSpecs);
		pnlChartTop.add(chartBlocks);
		JScrollPane scrollChartTop = IGUI.createJScrollPane(pnlChartTop);
		scrollChartTop.setMinimumSize(new Dimension(600, 200));
		JPanel pnlChartDown = new JPanel();
		pnlChartDown.setMinimumSize(new Dimension(600, 200));
		pnlChartDown.setLayout(new BoxLayout(pnlChartDown, BoxLayout.X_AXIS));
		pnlChartDown.setOpaque(true);
		pnlChartDown.add(chartFTE);
		pnlChartDown.add(chartSlides);
		pnlChartDown.add(chartFrozen);
		JScrollPane scrollChartDown = IGUI.createJScrollPane(pnlChartDown);
		scrollChartDown.setMinimumSize(new Dimension(600, 200));
		JSplitPane splitTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTop.setTopComponent(scrollChartTop);
		splitTop.setBottomComponent(scrollChartDown);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(250);
		splitTop.setMinimumSize(new Dimension(700, 500));
		JSplitPane splitDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitDown.setTopComponent(splitTop);
		splitDown.setBottomComponent(scrollTable);
		splitDown.setOneTouchExpandable(true);
		splitDown.setDividerLocation(550);
		splitDown.setMinimumSize(new Dimension(700, 800));
		JSplitPane splitAll = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitAll.setTopComponent(scrollTree);
		splitAll.setBottomComponent(splitDown);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(250);
		splitAll.setMinimumSize(new Dimension(1000, 900));
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = pj.dates.setMidnight(null);
		Calendar calBeg = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calMin.setTimeInMillis(pj.setup.getLong(LSetup.VAR_MIN_WL_DATE));
		calMax.set(Calendar.DAY_OF_YEAR, 1);
		calBeg.setTimeInMillis(calMin.getTimeInMillis());
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		timeFrom = calMin.getTimeInMillis();
		timeTo = calMax.getTimeInMillis();
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, calBeg, calEnd, calMin, calMax, true), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private String[] getHeaders(int[] nYears) {
		String[] headers = new String[nYears.length];
		for (int i = 0; i < nYears.length; i++) {
			headers[i] = Integer.toString(nYears[i]);
		}
		return headers;
	}

	@Override
	void setFilter(byte[] rows) {
		for (int i = 0; i < rowsView.length; i++) {
			rowsView[i] = rows[i];
		}
		altered = true;
	}

	@Override
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_FAC:
			facID = value;
			altered = true;
			break;
		default:
			if (altered && timeTo > timeFrom) {
				altered = false;
				WorkerData worker = new WorkerData();
				worker.execute();
			}
		}
	}

	@Override
	void setFilter(short id, Calendar value) {
		switch (id) {
		case IToolBar.TB_FROM:
			timeFrom = value.getTimeInMillis();
			break;
		default:
			timeTo = value.getTimeInMillis();
		}
		altered = true;
	}

	private void setView(TreePath newPath) {
		if (newPath == null)
			return;
		if (treePath == null || !treePath.equals(newPath)) {
			treePath = newPath;
			node = (OAnnualNode) treePath.getPathComponent(treePath.getPathCount() - 1);
			model.fireTableDataChanged();
		}
	}

	private class ModelList extends AbstractTableModel {
		private final String[] rows = { "Cases", "Specimens", "Blocks", "Slides", "H&E", "SS", "IHC", "Synoptics",
				"Frozens", coders[0], coders[1], coders[2], coders[3], coders[4] };
		private String[] columns;

		ModelList(String[] headers) {
			this.columns = new String[headers.length + 1];
			for (int i = 1; i < this.columns.length; i++) {
				this.columns[i] = headers[i - 1];
			}
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			} else if (col < 10) {
				return Integer.class;
			} else {
				return Double.class;
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
			return rows.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (col == DATA_NAMES && rows.length > 0 && rows.length > row) {
				value = rows[row];
			} else if (node.cases == null) {
				value = 0;
			} else if (col < columns.length - 3) {
				switch (col) {
				case DATA_CASES:
					value = node.cases[col - 1];
				case DATA_SPECS:
					value = node.specs[col - 1];
				case DATA_BLCKS:
					value = node.blocks[col - 1];
				case DATA_SLIDE:
					value = node.slides[col - 1];
				case DATA_SL_HE:
					value = node.he[col - 1];
				case DATA_SL_SS:
					value = node.ss[col - 1];
				case DATA_SL_IH:
					value = node.ihc[col - 1];
				case DATA_SYNOP:
					value = node.synopt[col - 1];
				case DATA_FROZN:
					value = node.frozen[col - 1];
				case DATA_VALU1:
					value = node.fte1[col - 1];
				case DATA_VALU2:
					value = node.fte2[col - 1];
				case DATA_VALU3:
					value = node.fte3[col - 1];
				case DATA_VALU4:
					value = node.fte4[col - 1];
				case DATA_VALU5:
					value = node.fte5[col - 1];
				default:
					value = "N/A";
				}
			} else {
				switch (col) {
				case DATA_CASES:
					value = node.casesf[col - 1];
				case DATA_SPECS:
					value = node.specsf[col - 1];
				case DATA_BLCKS:
					value = node.blocksf[col - 1];
				case DATA_SLIDE:
					value = node.slidesf[col - 1];
				case DATA_SL_HE:
					value = node.hef[col - 1];
				case DATA_SL_SS:
					value = node.ssf[col - 1];
				case DATA_SL_IH:
					value = node.ihcf[col - 1];
				case DATA_SYNOP:
					value = node.synoptf[col - 1];
				case DATA_FROZN:
					value = node.frozenf[col - 1];
				case DATA_VALU1:
					value = node.fte1f[col - 1];
				case DATA_VALU2:
					value = node.fte2f[col - 1];
				case DATA_VALU3:
					value = node.fte3f[col - 1];
				case DATA_VALU4:
					value = node.fte4f[col - 1];
				case DATA_VALU5:
					value = node.fte5f[col - 1];
				default:
					value = "N/A";
				}
			}
			return value;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private short yearMax = 0;
		private short yearMin = 9999;
		private int[] nYears;
		private ArrayList<OAnnual> annuals = new ArrayList<OAnnual>();

		@Override
		protected Void doInBackground() throws Exception {
			getData();
			structureData();
			return null;
		}

		@Override
		public void done() {
			// Display tree results
			OAnnualNode root = (OAnnualNode) tree.getModel().getRoot();
			TreeNode rootNode = new DefaultMutableTreeNode(root);
			TreeModel treeModel = new DefaultTreeModel(rootNode);
			tree.setModel(treeModel);
			// Display table results
			ModelList model = new ModelList(getHeaders(nYears));
			table.setModel(model);
			setView(tree.getPathForRow(0));
			pj.setBusy(false);
		}

		private int[] extrapolate(int[] oYears, int[] nYears, int[] oCount) {
			PolynomialFunction func = setFunction(oYears, oCount);
			int[] nCount = new int[nYears.length];
			for (int i = 0; i < nYears.length; i++) {
				double xpol = func.value(nYears[i]);
				nCount[i] = (int) Math.round(xpol);
			}
			;
			return nCount;
		}

		private double[] extrapolate(int[] oYears, int[] nYears, double[] oCount) {
			PolynomialFunction func = setFunction(oYears, oCount);
			double[] nCount = new double[nYears.length];
			for (int i = 0; i < nYears.length; i++) {
				nCount[i] = func.value(nYears[i]);
			}
			;
			return nCount;
		}

		private void extrapolate(int[] oYears, int[] nYears, OAnnualNode node) {
			node.casesf = extrapolate(oYears, nYears, node.cases);
			node.specsf = extrapolate(oYears, nYears, node.specs);
			node.blocksf = extrapolate(oYears, nYears, node.blocks);
			node.slidesf = extrapolate(oYears, nYears, node.slides);
			node.hef = extrapolate(oYears, nYears, node.he);
			node.ssf = extrapolate(oYears, nYears, node.ss);
			node.ihcf = extrapolate(oYears, nYears, node.ihc);
			node.synoptf = extrapolate(oYears, nYears, node.synopt);
			node.frozenf = extrapolate(oYears, nYears, node.frozen);
			node.fte1f = extrapolate(oYears, nYears, node.fte1);
			node.fte2f = extrapolate(oYears, nYears, node.fte2);
			node.fte3f = extrapolate(oYears, nYears, node.fte3);
			node.fte4f = extrapolate(oYears, nYears, node.fte4);
			node.fte5f = extrapolate(oYears, nYears, node.fte5);
			for (int i = 0; i < node.children.length; i++) {
				OAnnualNode child = (OAnnualNode) node.children[i];
				extrapolate(oYears, nYears, child);
			}
		}

		private void forecast(OAnnualNode root) {
			short noYears = (short) (yearMax - yearMin + 1);
			short yearID = yearMin;
			int[] oYears = new int[noYears];
			nYears = new int[noYears + 3];
			for (int i = 0; i < noYears; i++) {
				oYears[i] = yearID;
				nYears[i] = yearID;
				yearID++;
			}
			for (int i = 0; i < 3; i++) {
				nYears[noYears + i] = yearID;
				yearID++;
			}
			extrapolate(oYears, nYears, root);
		}

		private void getData() {
			short yearID = 0;
			OAnnual annual = new OAnnual();
			ResultSet rst = null;
			setName("AnnualWorker");
			try {
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_YER), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_YER), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_YER));
				while (rst.next()) {
					if (facID > 0 && facID != rst.getShort("FAID")) {
						continue;
					}
					annual = new OAnnual();
					annual.facID = rst.getShort("FAID");
					annual.spyID = rst.getByte("SYID");
					annual.subID = rst.getByte("SBID");
					annual.proID = rst.getByte("POID");
					annual.facility = rst.getString("FANM").trim();
					annual.specialty = rst.getString("SYNM").trim();
					annual.subspecial = rst.getString("SBNM").trim();
					annual.procedure = rst.getString("PONM").trim();
					yearID = rst.getShort("FNYEAR");
					annual.cases.put(yearID, rst.getInt("CACA"));
					annual.specs.put(yearID, rst.getInt("CASP"));
					annual.blocks.put(yearID, rst.getInt("CABL"));
					annual.slides.put(yearID, rst.getInt("CASL"));
					annual.synopt.put(yearID, rst.getInt("CASY"));
					annual.frozen.put(yearID, rst.getInt("CAFS"));
					annual.he.put(yearID, rst.getInt("CAHE"));
					annual.ss.put(yearID, rst.getInt("CASS"));
					annual.ihc.put(yearID, rst.getInt("CAIH"));
					annual.fte5.put(yearID, rst.getDouble("CAV1"));
					annual.fte2.put(yearID, rst.getDouble("CAV2"));
					annual.fte3.put(yearID, rst.getDouble("CAV3"));
					annual.fte4.put(yearID, rst.getDouble("CAV4"));
					annual.fte5.put(yearID, (double) rst.getInt("CAV5"));
					annuals.add(annual);
					if (yearMin > yearID) {
						yearMin = yearID;
					}
					if (yearMax < yearID) {
						yearMax = yearID;
					}
				}
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
		}

		private PolynomialFunction setFunction(int[] years, int[] count) {
			double[] dCount = new double[count.length];
			for (int i = 0; i < count.length; i++) {
				dCount[i] = count[i];
			}
			return setFunction(years, dCount);
		}

		private PolynomialFunction setFunction(int[] years, double[] count) {
			WeightedObservedPoints obs = new WeightedObservedPoints();
			for (int i = 0; i < years.length; i++) {
				obs.add(years[i], count[i]);
			}
			PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
			double[] coeff = fitter.fit(obs.toList());
			// make polynomial
			return new PolynomialFunction(coeff);
		}

		private void setModel(OAnnualList item, OAnnualNode node) {
			node.name = item.name;
			node.cases = item.cases;
			node.specs = item.specs;
			node.blocks = item.blocks;
			node.slides = item.slides;
			node.he = item.he;
			node.ss = item.ss;
			node.ihc = item.ihc;
			node.synopt = item.synopt;
			node.frozen = item.frozen;
			node.fte1 = item.fte1;
			node.fte2 = item.fte2;
			node.fte3 = item.fte3;
			node.fte4 = item.fte4;
			node.fte5 = item.fte5;
			node.children = new OAnnualNode[item.children.size()];
			for (int i = 0; i < item.children.size(); i++) {
				OAnnualList childItem = item.children.get(i);
				OAnnualNode childNode = new OAnnualNode();
				setModel(childItem, childNode);
			}
		}

		private void setTotals(OAnnualList master, double fte1, double fte2, double fte3, double fte4, double fte5) {
			OAnnualList child;
			for (int i = master.children.size() - 1; i >= 0; i--) {
				child = master.children.get(i);
				if (child.id < 0) {
					// filtered out
					master.children.remove(i);
					continue;
				}
				for (int j = 0; j < child.fte1.length; j++) {
					child.fte1[j] = child.fte1[j] / fte1;
					child.fte2[j] = child.fte2[j] / fte2;
					child.fte3[j] = child.fte3[j] / fte3;
					child.fte4[j] = child.fte4[j] / fte4;
					child.fte5[j] = child.fte5[j] / fte5;
				}
				if (child.children.size() > 0) {
					setTotals(child, fte1, fte2, fte3, fte4, fte5);
				}
			}
		}

		private void structureData() {
			byte noYears = (byte) (yearMax - yearMin + 1);
			short id = -1;
			short ids[] = new short[rowsView.length];
			int rowNos[] = new int[rowsView.length];
			int size = annuals.size();
			int noDays = pj.dates.getNoDays(timeFrom, timeTo);
			double fte1 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER1_FTE) / 365;
			double fte2 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER2_FTE) / 365;
			double fte3 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER3_FTE) / 365;
			double fte4 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER4_FTE) / 365;
			double fte5 = 1.0 * noDays * pj.setup.getInt(LSetup.VAR_V5_FTE) / 365;
			String name = "";
			OAnnual row = new OAnnual();
			OAnnualList child0 = new OAnnualList(name, noYears, id);
			OAnnualList child1 = new OAnnualList(name, noYears, id);
			OAnnualList child2 = new OAnnualList(name, noYears, id);
			OAnnualList child3 = new OAnnualList(name, noYears, id);
			OAnnualList child4 = new OAnnualList(name, noYears, id);
			for (int i = 0; i < rowsView.length; i++) {
				ids[i] = id;
			}
			if (fte5 == 0) {
				fte5 = 1.0;
			}
			if (fte2 == 0) {
				fte2 = 1.0;
			}
			if (fte3 == 0) {
				fte3 = 1.0;
			}
			if (fte4 == 0) {
				fte4 = 1.0;
			}
			if (fte5 == 0) {
				fte5 = 1.0;
			}
			child0.name = "Total";
			for (int x = 0; x < size; x++) {
				row = annuals.get(x);
				// Match 1st node
				switch (rowsView[0]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				default:
					id = -2;
					name = "N/A";
				}
				if (ids[0] != id) {
					ids[0] = id;
					rowNos[0] = -1;
					for (int i = 1; i < rowsView.length; i++) {
						ids[i] = -1;
						rowNos[i] = -1;
					}
					for (int j = 0; j < child0.children.size(); j++) {
						child1 = child0.children.get(j);
						if (child1.id == ids[0]) {
							rowNos[0] = j;
							break;
						}
					}
					if (rowNos[0] < 0) {
						rowNos[0] = child0.children.size();
						child1 = new OAnnualList(name, noYears, ids[0]);
						child0.children.add(child1);
					}
				}
				// Match 2nd node
				switch (rowsView[1]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				default:
					id = -2;
				}
				if (ids[1] != id) {
					ids[1] = id;
					rowNos[1] = -1;
					for (int i = 2; i < rowsView.length; i++) {
						ids[i] = -1;
						rowNos[i] = -1;
					}
					for (int i = 0; i < child1.children.size(); i++) {
						child2 = child1.children.get(i);
						if (child2.id == ids[1]) {
							rowNos[1] = i;
							break;
						}
					}
					if (rowNos[1] < 0) {
						rowNos[1] = child1.children.size();
						child2 = new OAnnualList(name, noYears, ids[1]);
						child1.children.add(child2);
					}
				}
				// Match 3rd node
				switch (rowsView[2]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				default:
					id = -2;
				}
				if (ids[2] != id) {
					ids[2] = id;
					ids[3] = -1;
					ids[4] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
					for (int i = 0; i < child2.children.size(); i++) {
						child3 = child2.children.get(i);
						if (child3.id == ids[2]) {
							rowNos[2] = i;
							break;
						}
					}
					if (rowNos[2] < 0) {
						rowNos[2] = child2.children.size();
						child3 = new OAnnualList(name, noYears, ids[2]);
						child2.children.add(child3);
					}
				}
				// Match 4th node
				switch (rowsView[3]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				default:
					id = -2;
				}
				if (ids[3] != id) {
					ids[3] = id;
					ids[4] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
					for (int i = 0; i < child3.children.size(); i++) {
						child4 = child3.children.get(i);
						if (child4.id == ids[3]) {
							rowNos[3] = i;
							break;
						}
					}
					if (rowNos[3] < 0) {
						rowNos[3] = child3.children.size();
						child4 = new OAnnualList(name, noYears, ids[3]);
						child3.children.add(child4);
					}
				}
				short yearID = yearMin;
				int qty = 0;
				double val = 0;
				for (short y = 0; y < noYears; y++) {
					qty = row.cases.get(yearID);
					if (qty > 0) {
						child0.cases[y] += qty;
						child1.cases[y] += qty;
						child2.cases[y] += qty;
						child3.cases[y] += qty;
						child4.cases[y] = qty;
					}
					qty = row.specs.get(yearID);
					if (qty > 0) {
						child0.specs[y] += qty;
						child1.specs[y] += qty;
						child2.specs[y] += qty;
						child3.specs[y] += qty;
						child4.specs[y] = qty;
					}
					qty = row.slides.get(yearID);
					if (qty > 0) {
						child0.slides[y] += qty;
						child1.slides[y] += qty;
						child2.slides[y] += qty;
						child3.slides[y] += qty;
						child4.slides[y] = qty;
					}
					qty = row.he.get(yearID);
					if (qty > 0) {
						child0.he[y] += qty;
						child1.he[y] += qty;
						child2.he[y] += qty;
						child3.he[y] += qty;
						child4.he[y] = qty;
					}
					qty = row.ss.get(yearID);
					if (qty > 0) {
						child0.ss[y] += qty;
						child1.ss[y] += qty;
						child2.ss[y] += qty;
						child3.ss[y] += qty;
						child4.ss[y] = qty;
					}
					qty = row.ihc.get(yearID);
					if (qty > 0) {
						child0.ihc[y] += qty;
						child1.ihc[y] += qty;
						child2.ihc[y] += qty;
						child3.ihc[y] += qty;
						child4.ihc[y] = qty;
					}
					qty = row.synopt.get(yearID);
					if (qty > 0) {
						child0.synopt[y] += qty;
						child1.synopt[y] += qty;
						child2.synopt[y] += qty;
						child3.synopt[y] += qty;
						child4.synopt[y] = qty;
					}
					qty = row.frozen.get(yearID);
					if (qty > 0) {
						child0.frozen[y] += qty;
						child1.frozen[y] += qty;
						child2.frozen[y] += qty;
						child3.frozen[y] += qty;
						child4.frozen[y] = qty;
					}
					val = row.fte1.get(yearID);
					if (qty > 0) {
						child0.fte1[y] += val;
						child1.fte1[y] += val;
						child2.fte1[y] += val;
						child3.fte1[y] += val;
						child4.fte1[y] = val;
					}
					val = row.fte2.get(yearID);
					if (qty > 0) {
						child0.fte2[y] += val;
						child1.fte2[y] += val;
						child2.fte2[y] += val;
						child3.fte2[y] += val;
						child4.fte2[y] = val;
					}
					val = row.fte3.get(yearID);
					if (qty > 0) {
						child0.fte3[y] += val;
						child1.fte3[y] += val;
						child2.fte3[y] += val;
						child3.fte3[y] += val;
						child4.fte3[y] = val;
					}
					val = row.fte4.get(yearID);
					if (qty > 0) {
						child0.fte4[y] += val;
						child1.fte4[y] += val;
						child2.fte4[y] += val;
						child3.fte4[y] += val;
						child4.fte4[y] = val;
					}
					val = row.fte5.get(yearID);
					if (qty > 0) {
						child0.fte5[y] += val;
						child1.fte5[y] += val;
						child2.fte5[y] += val;
						child3.fte5[y] += val;
						child4.fte5[y] = val;
					}
					yearID++;
				}
			}
			for (int i = 0; i < child0.fte1.length; i++) {
				child0.fte1[i] = child0.fte1[i] / fte1;
				child0.fte2[i] = child0.fte2[i] / fte2;
				child0.fte3[i] = child0.fte3[i] / fte3;
				child0.fte4[i] = child0.fte4[i] / fte4;
				child0.fte5[i] = child0.fte5[i] / fte5;
			}
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			setTotals(child0, fte1, fte2, fte3, fte4, fte5);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			OAnnualNode node0 = (OAnnualNode) tree.getModel().getRoot();
			setModel(child0, node0);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			forecast(node0);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}