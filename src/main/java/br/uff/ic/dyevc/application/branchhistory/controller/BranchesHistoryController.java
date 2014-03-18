/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.controller;

import br.uff.ic.dyevc.application.branchhistory.chart.CommitHistoryWindow;
import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.metric.MetricBucket;
import br.uff.ic.dyevc.application.branchhistory.metric.MetricCasing;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.application.branchhistory.view.BranchesHistoryWindow;
import br.uff.ic.dyevc.application.branchhistory.view.CreateProjectValuesService;
import br.uff.ic.dyevc.application.branchhistory.view.ProjectService;
import br.uff.ic.dyevc.application.branchhistory.view.ProjectValues;
import br.uff.ic.dyevc.model.MonitoredRepository;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import javax.swing.JCheckBox;

/**
 *
 * @author wallace
 */
public class BranchesHistoryController implements ActionListener, WindowListener{
    
    private MonitoredRepository monitoredRepository;
    private BranchesHistoryWindow branchesHistoryWindow;
    private List<MetricCasing> metricCasingList;
    private ProjectValues projectValues;
    ProjectRevisions projectRevisions;
    VersionedProject versionedProject;
    CreateProjectValuesService createProjectValuesService;
    
    public BranchesHistoryController(MonitoredRepository monitoredRepository){
        this.monitoredRepository = monitoredRepository;
        metricCasingList = MetricBucket.getInstance().getMetricCasingList();
        
        ProjectService projectService = new ProjectService();
        System.out.println("CLONE ADRESS: "+monitoredRepository.getCloneAddress());
        System.out.println("WORKING CLONE ADRESS: "+monitoredRepository.getWorkingCloneAddress());
        try{
            projectRevisions = projectService.getProjectRevisions(monitoredRepository);
            versionedProject = projectService.getVersionedProject(projectRevisions);
        }catch(Exception e){
            
        }
        
        createProjectValuesService = new CreateProjectValuesService();
        
        Metric metric = metricCasingList.get(0).getMetric();
        projectValues = createProjectValuesService.getProjectValues(projectRevisions, versionedProject,metric);
        
        String metricItems[];
        String branchesItems[];
        metricItems = new String[metricCasingList.size()];
        for (int i = 0; i < metricItems.length; i++) {
            metricItems[i] = metricCasingList.get(i).getName();
        }
        
        branchesItems = new String[projectRevisions.getBranchesRevisions().size()];
        for (int i = 0; i < branchesItems.length; i++) {
            branchesItems[i] = projectRevisions.getBranchesRevisions().get(i).getName();
        }
        
        branchesHistoryWindow = new BranchesHistoryWindow(this, this, metricItems, branchesItems, projectValues);
        branchesHistoryWindow.setSize(new Dimension(1000, 500));
        branchesHistoryWindow.pack();
    }
    
    public void execute(){
        branchesHistoryWindow.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(BranchesHistoryWindow.ACTION_BRANCH_CHECKBOX)){
            JCheckBox checkBox = (JCheckBox) e.getSource();
            branchesHistoryWindow.updateChartByCheckBox(checkBox, projectValues);
        } else if(e.getActionCommand().equals(BranchesHistoryWindow.ACTION_BRANCH_COMBOBOX)){
            branchesHistoryWindow.updateChartByComboBox(projectValues);
        } else if(e.getActionCommand().equals(BranchesHistoryWindow.ACTION_GRAPH_BUTTON)){
            new CommitHistoryWindow(projectRevisions).setVisible(true);
        } else if(e.getActionCommand().equals(BranchesHistoryWindow.ACTION_REFRESH_BUTTON)){
            
            Metric metric = metricCasingList.get(branchesHistoryWindow.getSelectedIndexMetric()).getMetric();
            projectValues = createProjectValuesService.getProjectValues(projectRevisions, versionedProject,metric);
           
            
            branchesHistoryWindow.updateValues(projectValues);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        
    }

    @Override
    public void windowClosing(WindowEvent e) {
        
    }

    @Override
    public void windowClosed(WindowEvent e) {
        
    }

    @Override
    public void windowIconified(WindowEvent e) {
        
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        
    }

    @Override
    public void windowActivated(WindowEvent e) {
        
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        
    }
    
}
