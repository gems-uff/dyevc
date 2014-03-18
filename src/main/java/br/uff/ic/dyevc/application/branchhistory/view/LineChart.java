/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author wallace
 */
public class LineChart extends ApplicationFrame{
    
    private XYDataset dataset;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private LineColours lineColours;
    
    public LineChart(ProjectValues projectValues) {

        super(projectValues.getName());

        createDataset(projectValues);
        createChart(projectValues.getName(), projectValues);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(900, 500));
        setContentPane(chartPanel);

    }
    
    public LineChart(ProjectValues projectValues, LineColours lineColours) {

        super(projectValues.getName());

        createDataset(projectValues, lineColours);
        createChart(projectValues.getName(), projectValues, lineColours);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(900, 500));
        setContentPane(chartPanel);

    }
    
    private void createChart(String projectName, ProjectValues projectValues,  LineColours lineColours) {
        
        // create the chart...
        chart = ChartFactory.createXYLineChart(
            projectName,      // chart title
            "Revision",                      // x axis label
            "Value",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        //final StandardLegend legend = (StandardLegend) chart.getLegend();
        //legend.setDisplaySeriesShapes(true);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        //renderer.setSeriesLinesVisible(0, false);
        //renderer.setSeriesShapesVisible(1, false);
        //renderer.setLinesVisible(false);
        
        plot.setRenderer(renderer);
        setColors(renderer, projectValues, lineColours);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
        
        LegendTitle l = chart.getLegend(0);
        
        //LegendTitle lr = LegendTitle;
//        LegendItemCollection i = new LegendItemCollection();
//        System.out.println("LEGENDAS: "+l.getSources()[0].getClass());
        
        chart.removeLegend();
        //chart.addLegend(l);
        
    }
    
    
    private void setColors(XYLineAndShapeRenderer renderer, ProjectValues projectValues, LineColours lineColours){
        int j = 0;
        
        for (BranchValues branchValues : projectValues.getBranchesValues()) {
            LineColor lc = lineColours.getColorByName(branchValues.getName());
            if(lc.isShow()){
                renderer.setSeriesPaint(j, lc.getP());
                renderer.setSeriesShape(j, lc.getS());
            
                Paint p = renderer.getSeriesPaint(j);  
                Shape s = renderer.getSeriesShape(j);
            
                j++;
                for(int i = 1; i < branchValues.getLineValues().size(); i++){
                    renderer.setSeriesPaint(j, p);
                    renderer.setSeriesShape(j, s);
                    j++;
                }
            }
            
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    public ChartPanel getChartPanel(){
        return chartPanel;
    }
    
    /**
     * Creates a sample dataset.
     * 
     * @return a sample dataset.
     */
    private void createDataset(ProjectValues projectValues) {
        
        dataset = new XYSeriesCollection();
        
        for (BranchValues branchValues : projectValues.getBranchesValues()) {
            for(LineValues lineValues : branchValues.getLineValues()){
                XYSeries series = new XYSeries(branchValues.getName());
                int i = 1;
                for(RevisionValue revisionValue : lineValues.getRevisionsValues()){
                    series.add(i, revisionValue.getValue());
                
                    i++;
                }
                ((XYSeriesCollection) dataset).addSeries(series);
            }
            
        }
        
        
    }
    
    private void createDataset(ProjectValues projectValues, LineColours lineColours) {
        
        dataset = new XYSeriesCollection();
        
        for (BranchValues branchValues : projectValues.getBranchesValues()) {
            LineColor lc = lineColours.getColorByName(branchValues.getName());
            if(lc.isShow()){
                for(LineValues lineValues : branchValues.getLineValues()){
                    XYSeries series = new XYSeries(branchValues.getName());
                    int i = 1;
                    for(RevisionValue revisionValue : lineValues.getRevisionsValues()){
                         series.add(i, revisionValue.getValue());
                
                        i++;
                    }
                    ((XYSeriesCollection) dataset).addSeries(series);
                }
            }
            
        }
        
        
    }
    
    /**
     * Creates a chart.
     * 
     * @param dataset  the data for the chart.
     * 
     * @return a chart.
     */
    private void createChart(String projectName, ProjectValues projectValues) {
        
        // create the chart...
        chart = ChartFactory.createXYLineChart(
            projectName,      // chart title
            "Revision",                      // x axis label
            "Value",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        //final StandardLegend legend = (StandardLegend) chart.getLegend();
        //legend.setDisplaySeriesShapes(true);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        //renderer.setSeriesLinesVisible(0, false);
        //renderer.setSeriesShapesVisible(1, false);
        //renderer.setLinesVisible(false);
        
        plot.setRenderer(renderer);
        setColors(renderer, projectValues);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
        
        LegendTitle l = chart.getLegend(0);
        
        //LegendTitle lr = LegendTitle;
//        LegendItemCollection i = new LegendItemCollection();
//        System.out.println("LEGENDAS: "+l.getSources()[0].getClass());
        
        chart.removeLegend();
        //chart.addLegend(l);
        
    }
    
    private void setColors(XYLineAndShapeRenderer renderer, ProjectValues projectValues){
        int j = 0;
        lineColours = new LineColours();
        for (BranchValues branchValues : projectValues.getBranchesValues()) {
            Paint p = renderer.getSeriesPaint(j);  
            Shape s = renderer.getSeriesShape(j);
            LineColor ln = new LineColor(branchValues.getName(), p, s);
            lineColours.addColor(ln);
            j++;
            for(int i = 1; i < branchValues.getLineValues().size(); i++){
                renderer.setSeriesPaint(j, p);
                renderer.setSeriesShape(j, s);
                j++;
            }
            
        }
    }

    /**
     * @return the lineColours
     */
    public LineColours getLineColours() {
        return lineColours;
    }
}
