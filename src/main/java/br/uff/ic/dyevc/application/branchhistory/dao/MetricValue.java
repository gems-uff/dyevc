/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.dao;

import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;

/**
 *
 * @author wallace
 */
public class MetricValue {
    private String value;
    private VersionedItem versionedItem;
    private String revisionId;
    private String metricSignature;
    
    public MetricValue(String revisionId, VersionedItem versionedItem, String metricSignature,  String value){
        this.value = value;
        this.revisionId = revisionId;
        this.versionedItem = versionedItem;
        this.metricSignature = metricSignature;
        
    }
    
    public String getValue(){
        return value;
    }


    /**
     * @return the versionedItem
     */
    public VersionedItem getVersionedItem() {
        return versionedItem;
    }

    /**
     * @return the revision
     */
    public String getRevisionId() {
        return revisionId;
    }

    /**
     * @return the metric
     */
    public String getMetricSignature() {
        return metricSignature;
    }
}
