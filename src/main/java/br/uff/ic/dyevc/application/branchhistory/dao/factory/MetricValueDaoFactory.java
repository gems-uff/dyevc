/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.dao.factory;

import br.uff.ic.dyevc.application.branchhistory.dao.HsqldbMetricValueDao;
import br.uff.ic.dyevc.application.branchhistory.dao.MetricValueDao;

/**
 *
 * @author wallace
 */
public class MetricValueDaoFactory {
    
    private static MetricValueDaoFactory metricValueDaoFactory;
    private MetricValueDao metricValueDao;
    
    public static MetricValueDaoFactory getInstance(){
        if(metricValueDaoFactory == null){
            metricValueDaoFactory = new MetricValueDaoFactory();
        }
        return metricValueDaoFactory;
    }
    
    private MetricValueDaoFactory(){
        
    }
    
    public MetricValueDao create(){
        metricValueDao = new HsqldbMetricValueDao();
        return metricValueDao;
    }
    
    public void close(){
        metricValueDao.close();
    }
}
