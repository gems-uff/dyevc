/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.model.BranchRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.LineRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import java.util.HashMap;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;

/**
 *
 * @author wallace
 */
public class CreateProjectValuesService {

    public ProjectValues getProjectValues(ProjectRevisions project, VersionedItem versionedItem, Metric metric) {
        
        ProjectValues projectValues = new ProjectValues(project.getName());
        try {
            

            HashMap hashValues = new HashMap<String, Double>();

            for (BranchRevisions branch : project.getBranchesRevisions()) {
                BranchValues branchValues = new BranchValues(branch.getName());
                for (LineRevisions line : branch.getLinesRevisions()) {
                    LineValues lineValues = new LineValues();
                    for (Revision revision : line.getRevisions()) {
                        Double v = (Double) hashValues.get(revision.getId());
                        double value = 0;
                        if(v == null){
                            
                            value = metric.getValue(revision, versionedItem, (VersionedProject) versionedItem, project);
                            hashValues.put(revision.getId(), value);
                            System.out.println("calculou: "+value);
                            if(value > projectValues.getMaxValue()){
                                projectValues.setMaxValue(value);
                            }
                            
                        }else{
                            value = v;
                        }
                        RevisionValue revisionValue = new RevisionValue(value, revision.getId());
                        lineValues.addRevisionValue(revisionValue);
                        //System.out.println("Terminou revision: "+revision);
                    }
                    branchValues.addLineValues(lineValues);
                }
                projectValues.addBranchValues(branchValues);
            }
            projectValues.setHashValues(hashValues);
        }catch (Exception e) {
            System.out.println("ERRO: "+e.getMessage());
        }
        
        
        return projectValues;
    }
    
    public void getBlankProject(){
        ProjectValues projectValues = new ProjectValues("");
    }
}
