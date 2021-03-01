package ca.powerj.swing;
import java.awt.Dimension;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.DataPointComponent;
import org.eclipse.birt.chart.model.attribute.DataPointComponentType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.DataPointComponentImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaNumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;

public class IChartBar extends IChartBase {

	public IChartBar(Dimension dim) {
		super(dim);
	}

	private Chart createChart(String[] xData, double[] yData, String caption) {
		ChartWithAxes chart = ChartWithAxesImpl.create();
		chart.setType("Bar Chart");
		chart.setSubType("Side-by-side");
		chart.setOrientation(Orientation.VERTICAL_LITERAL);
		chart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		chart.getBlock().getOutline().setVisible(true);
		// Plot
		Plot p = chart.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.WHITE());
		p.getOutline().setVisible(false);
		// Title
		chart.getTitle().getLabel().getCaption().setValue(caption);
		chart.getTitle().setAnchor(Anchor.NORTH_WEST_LITERAL);
		// Legend
		chart.getLegend().setVisible(false);
		// X-Axis
		Axis xAxisPrimary = chart.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		xAxisPrimary.getLabel().getCaption().getFont().setSize(10.0f);
		// Y-Axis
		Axis yAxisPrimary = chart.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		yAxisPrimary.getLabel().getCaption().getFont().setSize(10.0f);
		// Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(xData);
		NumberDataSet orthoValues = NumberDataSetImpl.create(yData);
		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");
		sd.getBaseSampleData().add(sdBase);
		OrthogonalSampleData sdOrthogonal = DataFactory.eINSTANCE.createOrthogonalSampleData();
		sdOrthogonal.setDataSetRepresentation("");
		sdOrthogonal.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal);
		chart.setSampleData(sd);
		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		sdX.getSeriesPalette().shift(0);
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);
		// Y-Series
		BarSeries bs = (BarSeries) BarSeriesImpl.create();
		bs.setDataSet(orthoValues);
		bs.setSeriesIdentifier("Series");
		bs.getLabel().setVisible(true);
		bs.getLabel().getCaption().getFont().setSize(10.0f);
		DataPointComponent dpc = DataPointComponentImpl.create(DataPointComponentType.ORTHOGONAL_VALUE_LITERAL,
				JavaNumberFormatSpecifierImpl.create("##.##"));
		bs.getDataPoint().getComponents().clear();
		bs.getDataPoint().getComponents().add(dpc);
		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().shift(-1);
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(bs);
		return chart;
	}

	public void setChart(String[] xData, double[] yData, String caption) {
		if (xData == null || yData == null) {
			return;
		}
		cm = createChart(xData, yData, caption);
		bNeedsGeneration = true;
		updateBuffer();
		repaint();
	}
}