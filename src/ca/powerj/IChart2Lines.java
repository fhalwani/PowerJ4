package ca.powerj;
import java.awt.Dimension;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.Marker;
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
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;

class IChart2Lines extends IChartCore {

	public IChart2Lines(Dimension dim) {
		super(dim);
	}

	private Chart createChart(String[] xData, double[][] yData, String caption) {
		ChartWithAxes chart = ChartWithAxesImpl.create();
		chart.setType("Line Chart");
		chart.setSubType("Overlay");
		// Plot
		chart.setSeriesThickness(25);
		chart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		// Legend
		chart.getLegend().getText().getFont().setBold(true);
		// Title
		chart.getTitle().getLabel().getCaption().setValue(caption);
		chart.getTitle().getOutline().setVisible(true);
		chart.getTitle().setAnchor(Anchor.NORTH_WEST_LITERAL);
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
		NumberDataSet orthoValues1 = NumberDataSetImpl.create(yData[0]);
		NumberDataSet orthoValues2 = NumberDataSetImpl.create(yData[1]);
		SampleData sd = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");
		sd.getBaseSampleData().add(sdBase);
		OrthogonalSampleData sdOrthogonal1 = DataFactory.eINSTANCE.createOrthogonalSampleData();
		sdOrthogonal1.setDataSetRepresentation("");
		sdOrthogonal1.setSeriesDefinitionIndex(0);
		sd.getOrthogonalSampleData().add(sdOrthogonal1);
		OrthogonalSampleData sdOrthogonal2 = DataFactory.eINSTANCE.createOrthogonalSampleData();
		sdOrthogonal2.setDataSetRepresentation("");
		sdOrthogonal2.setSeriesDefinitionIndex(1);
		sd.getOrthogonalSampleData().add(sdOrthogonal2);
		chart.setSampleData(sd);
		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);
		// Y-Series 1
		LineSeries ls1 = (LineSeries) LineSeriesImpl.create();
//		ls1.setSeriesIdentifier("Actual");
		ls1.setDataSet(orthoValues1);
		ls1.getLineAttributes().setColor(ColorDefinitionImpl.RED());
		for (int i = 0; i < ls1.getMarkers().size(); i++) {
			((Marker) ls1.getMarkers().get(i)).setType(MarkerType.TRIANGLE_LITERAL);
			((Marker) ls1.getMarkers().get(i)).setSize(4);
		}
		ls1.getLabel().setVisible(false);
		// Y-Series 2
		LineSeries ls2 = (LineSeries) LineSeriesImpl.create();
//		ls2.setSeriesIdentifier("Forecast");
		ls2.setDataSet(orthoValues2);
		ls2.getLineAttributes().setColor(ColorDefinitionImpl.GREEN());
		for (int i = 0; i < ls2.getMarkers().size(); i++) {
			((Marker) ls2.getMarkers().get(i)).setType(MarkerType.BOX_LITERAL);
			((Marker) ls2.getMarkers().get(i)).setSize(4);
		}
		ls2.getLabel().setVisible(false);
		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().shift(-2);
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(ls1);
		sdY.getSeries().add(ls2);
		return chart;
	}

	void setChart(String[] x, double[][] y, String caption) {
		cm = createChart(x, y, caption);
		bNeedsGeneration = true;
		updateBuffer();
		repaint();
	}
}
