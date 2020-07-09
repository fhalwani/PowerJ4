package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

class NSpecimen extends NBase {
	private final byte DATA_NAME = 0;
	private final byte DATA_COUNT = 1;
	private final byte DATA_BLOCKS = 2;
	private final byte DATA_SLIDES = 3;
	private final byte DATA_HE = 4;
	private final byte DATA_SS = 5;
	private final byte DATA_IHC = 6;
	private final byte DATA_VALUE1 = 7;
	private final byte DATA_VALUE2 = 8;
	private final byte DATA_VALUE3 = 9;
	private final byte DATA_VALUE4 = 10;
	private final byte DATA_VALUE5 = 11;
	private byte[] rowsView = new byte[4];
	private long timeFrom = 0;
	private long timeTo = 0;
	private String[] columns = { "Name", "Count", "Blocks", "Slides", "H&E", "SS", "IHC", "", "", "", "", "" };
	private TreePath treePath;
	private ITreeTable tree;
	private ModelSpecimen model;
	private IChartPie chartCoder1, chartCoder2, chartCoder3, chartCoder4, chartCoder5;

	NSpecimen(AClient parent) {
		super(parent);
		setName("Specimens");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_SPECIMEN);
		columns[7] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		columns[8] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		columns[9] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		columns[10] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		columns[11] = pj.setup.getString(LSetup.VAR_V5_NAME);
		rowsView[0] = IPanelRows.ROW_FACILITY;
		rowsView[1] = IPanelRows.ROW_SPECIALTY;
		rowsView[2] = IPanelRows.ROW_SUBSPECIAL;
		rowsView[3] = IPanelRows.ROW_SPECIMEN;
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	boolean close() {
		altered = false;
		super.close();
		if (chartCoder1 != null) {
			chartCoder1.close();
		}
		if (chartCoder2 != null) {
			chartCoder2.close();
		}
		if (chartCoder3 != null) {
			chartCoder3.close();
		}
		if (chartCoder4 != null) {
			chartCoder4.close();
		}
		if (chartCoder5 != null) {
			chartCoder5.close();
		}
		return true;
	}

	private void createPanel() {
		OSpecnode root = new OSpecnode("Total");
		model = new ModelSpecimen(root);
		tree = new ITreeTable(pj, model);
		tree.setFocusable(true);
		tree.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setCharts(e.getNewLeadSelectionPath());
					}
				});
			}
		});
		JScrollPane scrollTree = IGUI.createJScrollPane(tree);
		scrollTree.setMinimumSize(new Dimension(1000, 600));
		chartCoder1 = new IChartPie(new Dimension(200, 200));
		chartCoder2 = new IChartPie(new Dimension(200, 200));
		chartCoder3 = new IChartPie(new Dimension(200, 200));
		chartCoder4 = new IChartPie(new Dimension(200, 200));
		chartCoder5 = new IChartPie(new Dimension(200, 200));
		JPanel pnlCharts = new JPanel();
		pnlCharts.setMinimumSize(new Dimension(1000, 200));
		pnlCharts.setLayout(new BoxLayout(pnlCharts, BoxLayout.X_AXIS));
		pnlCharts.setOpaque(true);
		pnlCharts.add(chartCoder1);
		pnlCharts.add(chartCoder2);
		pnlCharts.add(chartCoder3);
		pnlCharts.add(chartCoder4);
		pnlCharts.add(chartCoder5);
		JScrollPane scrollCharts = IGUI.createJScrollPane(pnlCharts);
		scrollCharts.setMinimumSize(new Dimension(1000, 200));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollCharts);
		splitAll.setBottomComponent(scrollTree);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(250);
		splitAll.setMinimumSize(new Dimension(1000, 900));
		Calendar calStart = pj.dates.setMidnight(null);
		Calendar calEnd = pj.dates.setMidnight(null);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		calStart.add(Calendar.YEAR, -1);
		timeFrom = calStart.getTimeInMillis();
		timeTo = calEnd.getTimeInMillis();
		calMax.setTimeInMillis(timeTo);
		calMin.setTimeInMillis(pj.setup.getLong(LSetup.VAR_MIN_WL_DATE));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, calStart, calEnd, calMin, calMax, rowsView), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void setCharts(TreePath newPath) {
		if (newPath == null)
			return;
		if (treePath == null || !treePath.equals(newPath)) {
			treePath = newPath;
			OSpecnode node = (OSpecnode) treePath.getPathComponent(treePath.getPathCount() - 1);
			if (node != null && node.children != null && node.children.length > 0) {
				int count = node.children.length;
				String[] xTitles = new String[count];
				double[] yCoder1 = new double[count];
				double[] yCoder2 = new double[count];
				double[] yCoder3 = new double[count];
				double[] yCoder4 = new double[count];
				double[] yCoder5 = new double[count];
				for (int i = 0; i < count; i++) {
					OSpecnode leaf = (OSpecnode) node.children[i];
					xTitles[i] = leaf.name;
					yCoder1[i] = leaf.fte1;
					yCoder2[i] = leaf.fte2;
					yCoder3[i] = leaf.fte3;
					yCoder4[i] = leaf.fte4;
					yCoder5[i] = leaf.fte5;
				}
				chartCoder1.setChart(xTitles, yCoder1, columns[7], IChartPie.COLOR_DEF);
				chartCoder2.setChart(xTitles, yCoder2, columns[8], IChartPie.COLOR_DEF);
				chartCoder3.setChart(xTitles, yCoder3, columns[9], IChartPie.COLOR_DEF);
				chartCoder4.setChart(xTitles, yCoder4, columns[10], IChartPie.COLOR_DEF);
				chartCoder5.setChart(xTitles, yCoder5, columns[11], IChartPie.COLOR_DEF);
			}
		}
		altered = false;
	}

	@Override
	void setFilter(int[] rows) {
		for (int i = 0; i < rowsView.length; i++) {
			rowsView[i] = (byte) rows[i];
		}
		altered = true;
	}

	@Override
	void setFilter(short id, short value) {
		if (altered && timeTo > timeFrom) {
			pj.setBusy(true);
			WorkerData worker = new WorkerData();
			worker.execute();
			altered = false;
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

	private class ModelSpecimen extends ITreeTableModel implements ITreeModel {

		private final Class<?>[] types = { ITreeModel.class, Integer.class, Integer.class, Integer.class, Integer.class,
				Integer.class, Integer.class, Double.class, Double.class, Double.class, Double.class, Double.class };

		public ModelSpecimen(Object nodeRoot) {
			super(nodeRoot);
		}

		@Override
		public Object getChild(Object node, int element) {
			return ((OSpecnode) node).children[element];
		}

		@Override
		public int getChildCount(Object node) {
			Object[] children = getChildren(node);
			return (children == null) ? 0 : children.length;
		}

		protected Object[] getChildren(Object node) {
			return ((OSpecnode) node).children;
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return types[column];
		}

		@Override
		public Object getValueAt(Object node, int column) {
			OSpecnode data = (OSpecnode) node;
			switch (column) {
			case DATA_NAME:
				return data.name;
			case DATA_COUNT:
				return data.noSpecs;
			case DATA_BLOCKS:
				return data.noBlocks;
			case DATA_SLIDES:
				return data.noSlides;
			case DATA_HE:
				return data.noHE;
			case DATA_SS:
				return data.noSS;
			case DATA_IHC:
				return data.noIHC;
			case DATA_VALUE1:
				return data.fte1;
			case DATA_VALUE2:
				return data.fte2;
			case DATA_VALUE3:
				return data.fte3;
			case DATA_VALUE4:
				return data.fte4;
			case DATA_VALUE5:
				return data.fte5;
			default:
				return null;
			}
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private ArrayList<OSpecimen> rows = new ArrayList<OSpecimen>();

		@Override
		protected Void doInBackground() throws Exception {
			setName("WorkerData");
			getData();
			structureData();
			return null;
		}

		@Override
		public void done() {
			// Display results
			OSpecnode root = (OSpecnode) model.getRoot();
			model = new ModelSpecimen(root);
			tree.setTreeTableModel(model);
			setCharts(tree.getTree().getPathForRow(0));
			pj.statusBar.setMessage("Specimens " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATELONG) + " - "
					+ pj.dates.formatter(timeTo, LDates.FORMAT_DATELONG));
			pj.setBusy(false);
		}

		private void getData() {
			boolean exists = false;
			OSpecimen row = new OSpecimen();
			ResultSet rst = null;
			try {
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SPG_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SPG_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SPG_SL_SUM));
				while (rst.next()) {
					row = new OSpecimen();
					row.facID = rst.getShort("faid");
					row.spyID = rst.getByte("syid");
					row.subID = rst.getByte("sbid");
					row.spgID = rst.getShort("sgid");
					row.noSpecs = rst.getInt("qty");
					row.noBlocks = rst.getInt("spbl");
					row.noSlides = rst.getInt("spsl");
					row.noHE = rst.getInt("sphe");
					row.noSS = rst.getInt("spss");
					row.noIHC = rst.getInt("spih");
					row.fte1 = rst.getDouble("spv1");
					row.fte2 = rst.getDouble("spv2");
					row.fte3 = rst.getDouble("spv3");
					row.fte4 = rst.getDouble("spv4");
					row.fte5 = rst.getDouble("spv5");
					row.facility = rst.getString("fanm");
					row.specialty = rst.getString("synm");
					row.subspecial = rst.getString("sbnm");
					row.specimen = rst.getString("sgdc");
					rows.add(row);
				}
				rst.close();
				// Frozen Section
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FRZ_SL_SUM));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("faid") && rows.get(i).spyID == rst.getByte("syid")
								&& rows.get(i).spgID == 270) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OSpecimen();
						row.facID = rst.getShort("faid");
						row.spyID = rst.getByte("syid");
						row.subID = 1;
						row.spgID = 270;
						row.facility = rst.getString("fanm");
						row.specialty = rst.getString("synm");
						row.subspecial = "GNR";
						row.specimen = rst.getString("sgdc");
						rows.add(row);
					}
					row.fte1 += rst.getDouble("frv1");
					row.fte2 += rst.getDouble("frv2");
					row.fte3 += rst.getDouble("frv3");
					row.fte4 += rst.getDouble("frv4");
					row.fte5 += rst.getDouble("frv5");
				}
				rst.close();
				// Additional
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SPG), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SPG), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_SPG));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("faid") && rows.get(i).spyID == rst.getByte("syid")
								&& rows.get(i).subID == rst.getByte("sbid") && rows.get(i).spgID == rst.getShort("sgid")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OSpecimen();
						row.facID = rst.getShort("faid");
						row.spyID = rst.getByte("syid");
						row.subID = rst.getByte("sbid");
						row.spgID = rst.getShort("sgid");
						row.facility = rst.getString("fanm");
						row.specialty = rst.getString("synm");
						row.subspecial = rst.getString("sbnm");
						row.specimen = rst.getString("sgdc");
						rows.add(row);
					}
					row.fte1 += rst.getDouble("adv1");
					row.fte2 += rst.getDouble("adv2");
					row.fte3 += rst.getDouble("adv3");
					row.fte4 += rst.getDouble("adv4");
					row.fte5 += rst.getDouble("adv5");
				}
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
		}

		private void setModel(OSpeclist root) {
			OSpecnode node0 = (OSpecnode) tree.tree.getModel().getRoot();
			OSpecnode node1 = new OSpecnode("node1");
			OSpecnode node2 = new OSpecnode("node2");
			OSpecnode node3 = new OSpecnode("node3");
			OSpecnode node4 = new OSpecnode("node4");
			OSpeclist data1 = new OSpeclist();
			OSpeclist data2 = new OSpeclist();
			OSpeclist data3 = new OSpeclist();
			OSpeclist data4 = new OSpeclist();
			node0.noSpecs = root.noSpecs;
			node0.noBlocks = root.noBlocks;
			node0.noSlides = root.noSlides;
			node0.noHE = root.noHE;
			node0.noSS = root.noSS;
			node0.noIHC = root.noIHC;
			node0.fte1 = root.fte1;
			node0.fte2 = root.fte2;
			node0.fte3 = root.fte3;
			node0.fte4 = root.fte4;
			node0.fte5 = root.fte5;
			node0.children = new OSpecnode[root.children.size()];
			for (int i = 0; i < root.children.size(); i++) {
				data1 = root.children.get(i);
				node1 = new OSpecnode(data1.name);
				node1.noSpecs = data1.noSpecs;
				node1.noBlocks = data1.noBlocks;
				node1.noSlides = data1.noSlides;
				node1.noHE = data1.noHE;
				node1.noSS = data1.noSS;
				node1.noIHC = data1.noIHC;
				node1.fte1 = data1.fte1;
				node1.fte2 = data1.fte2;
				node1.fte3 = data1.fte3;
				node1.fte4 = data1.fte4;
				node1.fte5 = data1.fte5;
				node1.children = new OSpecnode[data1.children.size()];
				for (int j = 0; j < data1.children.size(); j++) {
					data2 = data1.children.get(j);
					node2 = new OSpecnode(data2.name);
					node2.noSpecs = data2.noSpecs;
					node2.noBlocks = data2.noBlocks;
					node2.noSlides = data2.noSlides;
					node2.noHE = data2.noHE;
					node2.noSS = data2.noSS;
					node2.noIHC = data2.noIHC;
					node2.fte1 = data2.fte1;
					node2.fte2 = data2.fte2;
					node2.fte3 = data2.fte3;
					node2.fte4 = data2.fte4;
					node2.fte5 = data2.fte5;
					node2.children = new OSpecnode[data2.children.size()];
					for (int k = 0; k < data2.children.size(); k++) {
						data3 = data2.children.get(k);
						node3 = new OSpecnode(data3.name);
						node3.noSpecs = data3.noSpecs;
						node3.noBlocks = data3.noBlocks;
						node3.noSlides = data3.noSlides;
						node3.noHE = data3.noHE;
						node3.noSS = data3.noSS;
						node3.noIHC = data3.noIHC;
						node3.fte1 = data3.fte1;
						node3.fte2 = data3.fte2;
						node3.fte3 = data3.fte3;
						node3.fte4 = data3.fte4;
						node3.fte5 = data3.fte5;
						node3.children = new OSpecnode[data3.children.size()];
						for (int l = 0; l < data3.children.size(); l++) {
							data4 = data3.children.get(l);
							node4 = new OSpecnode(data4.name);
							node4.noSpecs = data4.noSpecs;
							node4.noBlocks = data4.noBlocks;
							node4.noSlides = data4.noSlides;
							node4.noHE = data4.noHE;
							node4.noSS = data4.noSS;
							node4.noIHC = data4.noIHC;
							node4.fte1 = data4.fte1;
							node4.fte2 = data4.fte2;
							node4.fte3 = data4.fte3;
							node4.fte4 = data4.fte4;
							node4.fte5 = data4.fte5;
							node3.children[l] = node4;
						}
						node2.children[k] = node3;
					}
					node1.children[j] = node2;
				}
				node0.children[i] = node1;
			}
		}

		private void setTotals(OSpeclist master, double fte1, double fte2, double fte3, double fte4, double fte5) {
			OSpeclist child = new OSpeclist();
			for (int i = master.children.size() - 1; i >= 0; i--) {
				child = master.children.get(i);
				if (child.id < 0) {
					// filtered out
					master.children.remove(i);
					continue;
				}
				child.fte1 = child.fte1 / fte1;
				child.fte2 = child.fte2 / fte2;
				child.fte3 = child.fte3 / fte3;
				child.fte4 = child.fte4 / fte4;
				child.fte5 = child.fte5 / fte5;
				if (child.children.size() > 0) {
					setTotals(child, fte1, fte2, fte3, fte4, fte5);
				}
			}
		}

		private void sortChildren(OSpeclist master) {
			Collections.sort(master.children, new Comparator<OSpeclist>() {
				@Override
				public int compare(OSpeclist o1, OSpeclist o2) {
					return (o1.name.compareToIgnoreCase(o2.name));
				}
			});
			OSpeclist child = new OSpeclist();
			for (int i = 0; i < master.children.size(); i++) {
				child = master.children.get(i);
				if (child.children.size() > 0) {
					sortChildren(child);
				}
			}
		}

		private void structureData() {
			short id = 0;
			short ids[] = new short[rowsView.length];
			int rowNos[] = new int[rowsView.length];
			int size = rows.size();
			int noDays = pj.dates.getNoDays(timeFrom, timeTo);
			double fte1 = pj.setup.getShort(LSetup.VAR_CODER1_FTE) * noDays / 365.0;
			double fte2 = pj.setup.getShort(LSetup.VAR_CODER2_FTE) * noDays / 365.0;
			double fte3 = pj.setup.getShort(LSetup.VAR_CODER3_FTE) * noDays / 365.0;
			double fte4 = pj.setup.getShort(LSetup.VAR_CODER4_FTE) * noDays / 365.0;
			double fte5 = pj.setup.getInt(LSetup.VAR_V5_FTE) * noDays / 365.0;
			String name = "";
			OSpeclist data0 = new OSpeclist();
			OSpeclist data1 = new OSpeclist();
			OSpeclist data2 = new OSpeclist();
			OSpeclist data3 = new OSpeclist();
			OSpeclist data4 = new OSpeclist();
			OSpecimen row = new OSpecimen();
			for (int i = 0; i < rowsView.length; i++) {
				ids[i] = -1;
			}
			if (fte1 == 0) {
				fte1 = 1.0;
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
			for (int x = 0; x < size; x++) {
				row = rows.get(x);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
					break;
				default:
					id = -2;
				}
				if (ids[0] != id) {
					ids[0] = id;
					rowNos[0] = -1;
					for (int i = 1; i < rowsView.length; i++) {
						ids[i] = -1;
						rowNos[i] = -1;
					}
					for (int j = 0; j < data0.children.size(); j++) {
						data1 = data0.children.get(j);
						if (data1.id == ids[0]) {
							rowNos[0] = j;
							break;
						}
					}
					if (rowNos[0] < 0) {
						rowNos[0] = data0.children.size();
						data1 = new OSpeclist();
						data1.id = ids[0];
						data1.name = name;
						data0.children.add(data1);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
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
					for (int i = 0; i < data1.children.size(); i++) {
						data2 = data1.children.get(i);
						if (data2.id == ids[1]) {
							rowNos[1] = i;
							break;
						}
					}
					if (rowNos[1] < 0) {
						rowNos[1] = data1.children.size();
						data2 = new OSpeclist();
						data2.id = ids[1];
						data2.name = name;
						data1.children.add(data2);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
					break;
				default:
					id = -2;
				}
				if (ids[2] != id) {
					ids[2] = id;
					ids[3] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					for (int i = 0; i < data2.children.size(); i++) {
						data3 = data2.children.get(i);
						if (data3.id == ids[2]) {
							rowNos[2] = i;
							break;
						}
					}
					if (rowNos[2] < 0) {
						rowNos[2] = data2.children.size();
						data3 = new OSpeclist();
						data3.id = ids[2];
						data3.name = name;
						data2.children.add(data3);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
					break;
				default:
					id = -2;
				}
				if (ids[3] != id) {
					ids[3] = id;
					ids[4] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
					for (int i = 0; i < data3.children.size(); i++) {
						data4 = data3.children.get(i);
						if (data4.id == ids[3]) {
							rowNos[3] = i;
							break;
						}
					}
					if (rowNos[3] < 0) {
						rowNos[3] = data3.children.size();
						data4 = new OSpeclist();
						data4.id = ids[3];
						data4.name = name;
						data3.children.add(data4);
					}
				}
				data4.noSpecs += row.noSpecs;
				data3.noSpecs += row.noSpecs;
				data2.noSpecs += row.noSpecs;
				data1.noSpecs += row.noSpecs;
				data0.noSpecs += row.noSpecs;
				data4.noBlocks += row.noBlocks;
				data3.noBlocks += row.noBlocks;
				data2.noBlocks += row.noBlocks;
				data1.noBlocks += row.noBlocks;
				data0.noBlocks += row.noBlocks;
				data4.noSlides += row.noSlides;
				data3.noSlides += row.noSlides;
				data2.noSlides += row.noSlides;
				data1.noSlides += row.noSlides;
				data0.noSlides += row.noSlides;
				data4.noHE += row.noHE;
				data3.noHE += row.noHE;
				data2.noHE += row.noHE;
				data1.noHE += row.noHE;
				data0.noHE += row.noHE;
				data4.noSS += row.noSS;
				data3.noSS += row.noSS;
				data2.noSS += row.noSS;
				data1.noSS += row.noSS;
				data0.noSS += row.noSS;
				data4.noIHC += row.noIHC;
				data3.noIHC += row.noIHC;
				data2.noIHC += row.noIHC;
				data1.noIHC += row.noIHC;
				data0.noIHC += row.noIHC;
				data4.fte1 += row.fte1;
				data3.fte1 += row.fte1;
				data2.fte1 += row.fte1;
				data1.fte1 += row.fte1;
				data0.fte1 += row.fte1;
				data4.fte2 += row.fte2;
				data3.fte2 += row.fte2;
				data2.fte2 += row.fte2;
				data1.fte2 += row.fte2;
				data0.fte2 += row.fte2;
				data4.fte3 += row.fte3;
				data3.fte3 += row.fte3;
				data2.fte3 += row.fte3;
				data1.fte3 += row.fte3;
				data0.fte3 += row.fte3;
				data4.fte4 += row.fte4;
				data3.fte4 += row.fte4;
				data2.fte4 += row.fte4;
				data1.fte4 += row.fte4;
				data0.fte4 += row.fte4;
				data4.fte5 += row.fte5;
				data3.fte5 += row.fte5;
				data2.fte5 += row.fte5;
				data1.fte5 += row.fte5;
				data0.fte5 += row.fte5;
			}
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			data0.fte1 = data0.fte1 / fte1;
			data0.fte2 = data0.fte2 / fte2;
			data0.fte3 = data0.fte3 / fte3;
			data0.fte4 = data0.fte4 / fte4;
			data0.fte5 = data0.fte5 / fte5;
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			setTotals(data0, fte1, fte2, fte3, fte4, fte5);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			sortChildren(data0);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			setModel(data0);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}