/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import br.uff.ic.dyevc.application.branchhistory.dao.MetricValue;
import br.uff.ic.dyevc.application.branchhistory.dao.MetricValueDao;
import br.uff.ic.dyevc.application.branchhistory.dao.factory.MetricValueDaoFactory;
import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.model.BranchRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.LineRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import java.util.HashMap;
import java.util.List;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;

/**
 *
 * @author wallace
 */
public class CreateProjectValuesService {

    public ProjectValues getProjectValues(ProjectRevisions project, VersionedItem versionedItem, Metric metric) {
        System.out.println("ASSINATURA DA METRICA: "+metric.getSignature());
        System.out.println("NOME DA METRICA: "+metric.getName());
        ProjectValues projectValues = new ProjectValues(project.getName());
        try {
            
            MetricValueDaoFactory metricValueDaoFactory = MetricValueDaoFactory.getInstance();
            MetricValueDao metricValueDao = metricValueDaoFactory.create();

            HashMap<String, String> hashValues = new HashMap<String, String>();

            //int numberOfRevisions = project.getRevisionsBucket().getRevisionCollection().size();
            //int i = 0;
            List<MetricValue> metricValues = metricValueDao.findByMetricAndVersionedItem(metric, versionedItem);
            //System.out.println("REVISOES GRAVADAS: "+metricValues.size());
            for(MetricValue metricValue : metricValues){
                hashValues.put(metricValue.getRevisionId(), metricValue.getValue());
                //i++;
            }
            
            for (BranchRevisions branch : project.getBranchesRevisions()) {
                BranchValues branchValues = new BranchValues(branch.getName());
                for (LineRevisions line : branch.getLinesRevisions()) {
                    LineValues lineValues = new LineValues();
                    for (Revision revision : line.getRevisions()) {
                        String v =  hashValues.get(revision.getId());
                        String value = null;
                        if(v == null){
                            
                            value = metric.getValue(revision, versionedItem);
                            hashValues.put(revision.getId(), value);
                            MetricValue metricValue = new MetricValue(revision.getId(), versionedItem, metric.getSignature(), value);
                            metricValueDao.save(metricValue);
                            if(Double.valueOf(value) > projectValues.getMaxValue()){
                                projectValues.setMaxValue(Double.valueOf(value));
                            }
                            //i++;
                            //System.out.println("PORCENTAGEM DE REVISOES CALCULADAS: "+((((double) i)/numberOfRevisions)*100)+" %");
                            
                        }else{
                            value = v;
                        }
                        RevisionValue revisionValue = new RevisionValue(Double.valueOf(value), revision.getId());
                        lineValues.addRevisionValue(revisionValue);
                        //System.out.println("Terminou revision: "+revision);
                    }
                    branchValues.addLineValues(lineValues);
                }
                projectValues.addBranchValues(branchValues);
            }
            projectValues.setHashValues(hashValues);
            metricValueDaoFactory.close();
        }catch (Exception e) {
            System.out.println("ERRO: "+e.getMessage());
        }
        
        
        return projectValues;
    }
    
    public void getBlankProject(){
        ProjectValues projectValues = new ProjectValues("");
    }
}
