package ca.powerj.swing;
import java.awt.Dimension;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
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
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;

public class IChartLine extends IChartBase {

	public IChartLine(Dimension dim) {
		super(dim);
	}

	private Chart createChart(String[] xData, String[] legend, double[][] yData, String caption) {
		ChartWithAxes chart = ChartWithAxesImpl.create();
		chart.setType("LineChart");
		chart.setSubType("Overlay");
		chart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		chart.getBlock().getOutline().setVisible(true);
		// Plot
		chart.getPlot().getClientArea().setBackground(ColorDefinitionImpl.WHITE());
		// Title
		chart.getTitle().getLabel().getCaption().setValue(caption);
		chart.getTitle().setAnchor(Anchor.NORTH_WEST_LITERAL);
		// Legend
		Legend lg = chart.getLegend();
		lg.getText().getFont().setBold(true);
		lg.setItemType(LegendItemType.SERIES_LITERAL);
		// X-Axis
		Axis xAxisPrimary = chart.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);
		xAxisPrimary.getLabel().getCaption().getFont().setSize(10.0f);
		// Y-Axis
		Axis yAxisPrimary = chart.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		// Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(xData);
		NumberDataSet[] orthoValues = new NumberDataSet[yData.length];
		for (int i = 0; i < yData.length; i++) {
			orthoValues[i] = NumberDataSetImpl.create(yData[i]);
		}
		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");
		sd.getBaseSampleData().add(sdBase);
		OrthogonalSampleData[] sdOrthogonal = new OrthogonalSampleData[yData.length];
		for (int i = 0; i < yData.length; i++) {
			sdOrthogonal[i] = DataFactory.eINSTANCE.createOrthogonalSampleData();
			sdOrthogonal[i].setDataSetRepresentation("");
			sdOrthogonal[i].setSeriesDefinitionIndex(i);
			sd.getOrthogonalSampleData().add(sdOrthogonal[i]);
		}
		chart.setSampleData(sd);
		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);
		// Y-Series
		LineSeries[] ls = new LineSeries[yData.length];
		for (int i = 0; i < yData.length; i++) {
			ls[i] = (LineSeries) LineSeriesImpl.create();
			ls[i].setSeriesIdentifier(legend[i]);
			ls[i].setDataSet(orthoValues[i]);
			ls[i].getLabel().setVisible(false);
			ls[i].setCurve(true);
			ls[i].setTranslucent(true);
			ls[i].getLineAttributes().setThickness(2);
			for (int j = 0; j < ls[i].getMarkers().size(); j++) {
				ls[i].getMarkers().get(j).setType(MarkerType.TRIANGLE_LITERAL);
			}
		}
		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().shift(-2);
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		for (int i = 0; i < yData.length; i++) {
			sdY.getSeries().add(ls[i]);
		}
		return chart;
	}

	public void setChart(String[] xData, String[] legend, double[][] yData, String caption) {
		if (xData == null || yData == null) {
			return;
		}
		cm = createChart(xData, legend, yData, caption);
		bNeedsGeneration = true;
		updateBuffer();
		repaint();
	}
}