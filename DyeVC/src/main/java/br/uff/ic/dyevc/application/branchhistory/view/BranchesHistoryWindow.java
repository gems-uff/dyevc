/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.view;

import br.uff.ic.dyevc.application.branchhistory.chart.CommitHistoryWindow;
import br.uff.ic.dyevc.application.branchhistory.controller.BranchesHistoryController;
import br.uff.ic.dyevc.application.branchhistory.metric.LCSAbsoluteMetric;
import br.uff.ic.dyevc.application.branchhistory.metric.LCSMetric;
import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.metric.NumberOfBytes;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.model.MonitoredRepository;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartPanel;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author wallace
 */
public class BranchesHistoryWindow extends JFrame{

    
    private ChartPanel chartPanel;
    private JComboBox metricComboBox;
    private JComboBox branchComboBox;
    private JButton graphButton;
    private JButton refreshButton;
    private LineColours lineColours;
    private JPanel branchPanel;
    private JCheckBox brachCheckBoxes[];
    
    private LineChart lineChart;
    
    public final static String ACTION_METRIC_COMBOBOX = "ACTION_METRIC_COMBOBOX";
    public final static String ACTION_BRANCH_COMBOBOX = "ACTION_BRANCH_COMBOBOX";
    public final static String ACTION_GRAPH_BUTTON = "ACTION_GRAPH_BUTTON";
    public final static String ACTION_REFRESH_BUTTON = "ACTION_REFRESH_BUTTON";
    public final static String ACTION_BRANCH_CHECKBOX = "ACTION_BRANCH_CHECKBOX";
    
    
    ProjectRevisions project;
    
    ActionListener actionListener;
    
    
    
    
    
    public BranchesHistoryWindow(ActionListener actionListener, WindowListener windowListener, String metricItems[], String branchesItems[], ProjectValues projetcValues){
        this.actionListener = actionListener;
        createWidgets(metricItems,branchesItems,projetcValues);
        setController(actionListener,windowListener);
    }
    
    private void createWidgets(String metricItems[], String branchesItems[], ProjectValues projectValues){
        metricComboBox = new JComboBox(metricItems);
        branchComboBox = new JComboBox(branchesItems);
        
        lineChart = new LineChart(projectValues);
        lineColours = lineChart.getLineColours();
        lineChart.pack();
        RefineryUtilities.centerFrameOnScreen(lineChart);
        refreshButton = new JButton("atualizar");
        graphButton = new JButton("grafo");
             
        branchPanel = new JPanel();
        branchPanel.setLayout(new GridBagLayout());
        
        
        
        //fazendo isso com os checkboxs
        brachCheckBoxes = new JCheckBox[projectValues.branchesValues.size()];
        GridBagConstraints g = null;
        this.branchPanel.removeAll();
        for (int i = 0; i < projectValues.branchesValues.size(); i++) {
            brachCheckBoxes[i] = new JCheckBox();
            brachCheckBoxes[i].setText(projectValues.branchesValues.get(i).getName());
            brachCheckBoxes[i].setSelected(true);
            LineColor lc = lineColours.getColorByName(projectValues.branchesValues.get(i).getName());
            brachCheckBoxes[i].setForeground((Color) lc.getP());
            brachCheckBoxes[i].addActionListener(actionListener);
            brachCheckBoxes[i].setActionCommand(ACTION_BRANCH_CHECKBOX);
                
            g = new GridBagConstraints(0, i, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 0);
            branchPanel.add(brachCheckBoxes[i], g);
        }
        branchPanel.setVisible(true);
        
        
            
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = null;
            
        gridBagConstraints = new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0);
        p.add(branchPanel, gridBagConstraints);
            
        gridBagConstraints = new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0);
        p.add(branchComboBox, gridBagConstraints);
            
            
        gridBagConstraints = new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 0, 0, 0), 0, 0);
        p.add(metricComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints(0, 5, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 0, 0, 0), 0, 0);
        p.add(refreshButton, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints(0, 6, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 0, 0), 0, 0);
        p.add(graphButton, gridBagConstraints);
        

        chartPanel = lineChart.getChartPanel();

        this.add(chartPanel, BorderLayout.CENTER);
        this.add(p, BorderLayout.EAST);

    }
    
    private void setController(ActionListener actionListener, WindowListener windowListener){
        metricComboBox.addActionListener(actionListener);
        branchComboBox.addActionListener(actionListener);
        graphButton.addActionListener(actionListener);
        refreshButton.addActionListener(actionListener);
        
        metricComboBox.setActionCommand(BranchesHistoryWindow.ACTION_METRIC_COMBOBOX);
        branchComboBox.setActionCommand(BranchesHistoryWindow.ACTION_BRANCH_COMBOBOX);
        graphButton.setActionCommand(BranchesHistoryWindow.ACTION_GRAPH_BUTTON);
        refreshButton.setActionCommand(BranchesHistoryWindow.ACTION_REFRESH_BUTTON);
    }
    
    public int getSelectedIndexMetric(){
        return metricComboBox.getSelectedIndex();
    }
    
    public void updateChartByComboBox(ProjectValues projectValues){
        String selected = (String) branchComboBox.getSelectedItem();
        System.out.println("Selected: "+selected);
        projectValues.setOnTop(selected);
        
        
        LineChart chart = new LineChart(projectValues, lineColours);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        this.remove(chartPanel);
        chartPanel = chart.getChartPanel();
            
        this.add(chartPanel, BorderLayout.CENTER);
        this.repaint();
    }
    
    public void updateChartByCheckBox(JCheckBox checkBox, ProjectValues projectValues){
        
        LineColor lc = lineColours.getColorByName(checkBox.getText());
        lc.setShow(checkBox.isSelected());
         
        LineChart chart = new LineChart(projectValues, lineColours);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        this.remove(chartPanel);
        chartPanel = chart.getChartPanel();
            
        this.add(chartPanel, BorderLayout.CENTER);
        this.repaint();
    }
    
    public void updateValues(ProjectValues projectValues){
        LineChart chart = new LineChart(projectValues);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        this.remove(chartPanel);
        chartPanel = chart.getChartPanel();
        lineColours = chart.getLineColours();

        String items[] = new String[projectValues.branchesValues.size()];
        for (int i = 0; i < projectValues.branchesValues.size(); i++) {
            items[i] = projectValues.branchesValues.get(i).getName();
        }
        
            
            
        //fazendo isso com os checkboxs
        //brachCheckBoxes = new JCheckBox[projectValues.branchesValues.size()];
        GridBagConstraints g = null;
        //this.branchPanel.removeAll();
//        for (int i = 0; i < projectValues.branchesValues.size(); i++) {
//            brachCheckBoxes[i] = new JCheckBox();
//            brachCheckBoxes[i].setText(projectValues.branchesValues.get(i).getName());
//            brachCheckBoxes[i].setSelected(true);
//            LineColor lc = lineColours.getColorByName(projectValues.branchesValues.get(i).getName());
//            brachCheckBoxes[i].setForeground((Color) lc.getP());
//            brachCheckBoxes[i].addActionListener(actionListener);
//            brachCheckBoxes[i].setActionCommand(ACTION_BRANCH_CHECKBOX);
//                
//            g = new GridBagConstraints(0, i, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 0);
//            branchPanel.add(brachCheckBoxes[i], g);
//        }
        
        for (int i = 0; i < brachCheckBoxes.length; i++) {
            brachCheckBoxes[i].setSelected(true);
        }
        
        branchComboBox.setSelectedIndex(0);

        this.add(chartPanel, BorderLayout.CENTER);
        this.repaint();
    }
    
    public void initGraph(){
        new CommitHistoryWindow(project).setVisible(true);
    }
    

    
    

}
