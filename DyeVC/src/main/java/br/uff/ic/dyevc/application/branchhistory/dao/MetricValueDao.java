/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.dao;

import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import java.util.List;

/**
 *
 * @author wallace
 */
public interface MetricValueDao {
    public void save(MetricValue metricValue);
    public void delete(MetricValue metricValue);
    public MetricValue find(Metric metric, Revision revision, VersionedItem versionedItem);
    public List<MetricValue> findByMetricAndVersionedItem(Metric metric, VersionedItem versionedItem);
    public void close();
}
