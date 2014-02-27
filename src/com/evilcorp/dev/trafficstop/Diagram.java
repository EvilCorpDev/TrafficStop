package com.evilcorp.dev.trafficstop;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

public class Diagram {
	
	public Intent getIntent(Context context, String title, double[] ds, double[] ds2, 
			double[] limits, String xTitle, String yTitle, int color) {

		
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, color, PointStyle.CIRCLE);
		
		((XYSeriesRenderer)renderer.getSeriesRendererAt(0)).setFillPoints(true);
		
		setChartSettings(renderer, title, xTitle, yTitle, -5, 20, -5, 20, Color.LTGRAY, Color.LTGRAY);
		
		renderer.setShowGrid(true);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setZoomButtonsVisible(true);
	    renderer.setPanLimits(limits);
	    renderer.setZoomLimits(limits);
	    
	    XYMultipleSeriesDataset dataset = buildDataset(title, ds, ds2);
	    Intent intent = ChartFactory.getLineChartIntent(context, dataset, renderer,
	        title);
	    return intent;
		
	}
	
	private void setRenderer(XYMultipleSeriesRenderer renderer, int colors, PointStyle styles) {
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5f);
	    renderer.setMargins(new int[] { 20, 30, 15, 20 });
	    XYSeriesRenderer r = new XYSeriesRenderer();
	    r.setColor(colors);
	    r.setPointStyle(styles);
	    renderer.addSeriesRenderer(r);
	}
	
	private void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
		      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
		      int labelsColor) {
		    renderer.setChartTitle(title);
		    renderer.setXTitle(xTitle);
		    renderer.setYTitle(yTitle);
		    renderer.setXAxisMin(xMin);
		    renderer.setXAxisMax(xMax);
		    renderer.setYAxisMin(yMin);
		    renderer.setYAxisMax(yMax);
		    renderer.setAxesColor(axesColor);
		    renderer.setLabelsColor(labelsColor);
	}
	
	 protected XYMultipleSeriesDataset buildDataset(String title, double[] ds,
		      double[] ds2) {
		    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		    XYSeries series = new XYSeries(title, 0);
		    for(int i = 0; i < ds.length; i++) {
		    	series.add(ds[i], ds2[i]);
		    }
		    dataset.addSeries(series);
		    return dataset;
		  }

}
