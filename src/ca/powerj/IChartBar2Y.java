package ca.powerj;

import java.awt.Dimension;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LineAttributes;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.LineAttributesImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.AxisImpl;
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

public class IChartBar2Y extends IChartCore {

	IChartBar2Y(Dimension dim) {
		super(dim);
	}

	private Chart createChart(String[] xData, String[] legend, double[][] yData, String caption) {
		ChartWithAxes chart = ChartWithAxesImpl.create();
		chart.setType("Bar Chart"); //$NON-NLS-1$
		chart.setSubType("Side-by-side"); //$NON-NLS-1$
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
		yAxisPrimary.getLabel().getCaption().getFont().setSize(10.0f);
		// Y-Axis (2)
		Axis yAxis = AxisImpl.create(Axis.ORTHOGONAL);
		yAxis.setType(AxisType.LINEAR_LITERAL);
		yAxis.getMajorGrid().setTickStyle(TickStyle.RIGHT_LITERAL);
		yAxis.setLabelPosition(Position.RIGHT_LITERAL);
		yAxis.getLabel().getCaption().getFont().setSize(10.0f);
		xAxisPrimary.getAssociatedAxes().add(yAxis);
		// Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(xData);
		NumberDataSet orthoValues1 = NumberDataSetImpl.create(yData[0]);
		NumberDataSet orthoValues2 = NumberDataSetImpl.create(yData[1]);
		NumberDataSet orthoValues3 = NumberDataSetImpl.create(yData[2]);
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
		// Cases In (Y-Series 1)
		LineSeries ls1 = (LineSeries) LineSeriesImpl.create();
		ls1.setSeriesIdentifier(legend[0]);
		ls1.setDataSet(orthoValues1);
		LineAttributes la1 = LineAttributesImpl.create(ColorDefinitionImpl.ORANGE(), LineStyle.SOLID_LITERAL, 2);
		ls1.setLineAttributes(la1);
		ls1.getLabel().setVisible(true);
		ls1.getLabel().getCaption().getFont().setSize(10.0f);
		for (int i = 0; i < ls1.getMarkers().size(); i++) {
			ls1.getMarkers().get(i).setType(MarkerType.TRIANGLE_LITERAL);
			ls1.getMarkers().get(i).setSize(4);
		}
		SeriesDefinition sdY1 = SeriesDefinitionImpl.create();
		sdY1.getSeriesPalette().shift(-2);
		yAxisPrimary.getSeriesDefinitions().add(sdY1);
		sdY1.getSeries().add(ls1);
		// Cases Out (Y-Series 2)
		LineSeries ls2 = (LineSeries) LineSeriesImpl.create();
		ls2.setSeriesIdentifier(legend[1]);
		ls2.setDataSet(orthoValues2);
		LineAttributes la2 = LineAttributesImpl.create(ColorDefinitionImpl.GREEN(), LineStyle.SOLID_LITERAL, 2);
		ls2.setLineAttributes(la2);
		ls2.getLabel().setVisible(true);
		ls2.getLabel().getCaption().getFont().setSize(10.0f);
		for (int i = 0; i < ls2.getMarkers().size(); i++) {
			ls2.getMarkers().get(i).setType(MarkerType.BOX_LITERAL);
			ls2.getMarkers().get(i).setSize(4);
		}
		sdY1.getSeries().add(ls2);
		// Backlog (Y-Serires 3)
		LineSeries ls3 = (LineSeries) LineSeriesImpl.create();
		ls3.setSeriesIdentifier(legend[2]);
		ls3.setDataSet(orthoValues3);
		LineAttributes la3 = LineAttributesImpl.create(ColorDefinitionImpl.RED(), LineStyle.SOLID_LITERAL, 2);
		ls3.setLineAttributes(la3);
		ls3.getLabel().setVisible(true);
		ls3.getLabel().getCaption().getFont().setSize(10.0f);
		for (int i = 0; i < ls3.getMarkers().size(); i++) {
			ls3.getMarkers().get(i).setType(MarkerType.CIRCLE_LITERAL);
			ls3.getMarkers().get(i).setSize(4);
		}
		SeriesDefinition sdY2 = SeriesDefinitionImpl.create();
		sdY2.getSeriesPalette().shift(-3);
		yAxis.getSeriesDefinitions().add(sdY2);
		sdY2.getSeries().add(ls3);
		return chart;
	}

	void setChart(String[] xData, String[] legend, double[][] yData, String caption) {
		if (xData == null || yData == null) {
			return;
		}
		cm = createChart(xData, legend, yData, caption);
		bNeedsGeneration = true;
		updateBuffer();
		repaint();
	}
}