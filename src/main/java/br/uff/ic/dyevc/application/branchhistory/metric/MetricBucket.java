/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.metric;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class MetricBucket {
    
    private static MetricBucket metricBucket;
    
    private List<MetricCasing> metricCasingList;
    
    public static MetricBucket getInstance(){
        if(metricBucket == null){
            metricBucket = new MetricBucket();
        }
        return metricBucket;
    }
            
    private MetricBucket(){
        metricCasingList = new LinkedList<MetricCasing>();
        
        collectMetrics();
    }
    
    private void collectMetrics(){
        MetricCasing metricCasing;
        Metric metric;
        metric = new NumberOfBytes();
        metricCasing = new MetricCasing("Number Of Bytes", metric);
        metricCasing.setId(1);
        getMetricCasingList().add(metricCasing);
        metric = new LCSMetric();
        metricCasing = new MetricCasing("LCS Metric", metric);
        metricCasing.setId(2);
        getMetricCasingList().add(metricCasing);
        metric = new LCSAbsoluteMetric();
        metricCasing = new MetricCasing("LCS Absolute Metric", metric);
        metricCasing.setId(3);
        getMetricCasingList().add(metricCasing);
        
        metric = new AddMetric();
        metricCasing = new MetricCasing("Add Metric", metric);
        metricCasing.setId(4);
        getMetricCasingList().add(metricCasing);
        
        metric = new AbsoluteAddMetric();
        metricCasing = new MetricCasing("Absolute Add Metric", metric);
        metricCasing.setId(5);
        getMetricCasingList().add(metricCasing);
        
        metric = new DeleteMetric();
        metricCasing = new MetricCasing("Delete Metric", metric);
        metricCasing.setId(6);
        getMetricCasingList().add(metricCasing);
        
        metric = new AbsoluteDeleteMetric();
        metricCasing = new MetricCasing("Absolute Delete Metric", metric);
        metricCasing.setId(7);
        getMetricCasingList().add(metricCasing);
        
    }

    /**
     * @return the metricCasingList
     */
    public List<MetricCasing> getMetricCasingList() {
        return metricCasingList;
    }
}
