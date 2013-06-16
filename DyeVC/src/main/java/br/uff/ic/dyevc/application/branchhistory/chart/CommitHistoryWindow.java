package br.uff.ic.dyevc.application.branchhistory.chart;

import br.uff.ic.dyevc.gui.*;
import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.application.branchhistory.view.CreateProjectValuesService;
import br.uff.ic.dyevc.application.branchhistory.view.ProjectService;
import br.uff.ic.dyevc.application.branchhistory.view.ProjectValues;
import br.uff.ic.dyevc.application.branchhistory.chart.BasicRepositoryHistoryGraph;
import br.uff.ic.dyevc.application.branchhistory.metric.LCSAbsoluteMetric;
import br.uff.ic.dyevc.application.branchhistory.metric.LCSMetric;
import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.metric.NumberOfBytes;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import br.uff.ic.dyevc.application.branchhistory.chart.RepositoryHistoryLayout;
import br.uff.ic.dyevc.application.branchhistory.metric.MetricBucket;
import br.uff.ic.dyevc.graph.transform.CHVertexLabelTransformer;
import br.uff.ic.dyevc.graph.transform.ClusterVertexShapeTransformer;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import java.awt.Paint;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ToolTipManager;

/**
 * Displays the commit history for the specified repository
 *
 * @author cristiano
 */
public class CommitHistoryWindow extends javax.swing.JFrame {
    private static final long serialVersionUID = 1689885032823010309L;
    private ProjectRevisions rep;
    private Graph graph;
    private Graph collapsedGraph;
    private VisualizationViewer vv;
    private Layout layout;
    private GraphCollapser collapser;
    Filter<CommitInfo, CommitRelationship> edgeFilter;
    Filter<CommitInfo, CommitRelationship> nodeFilter;
    private JComboBox edgeLineShapeCombo;
    private JComboBox mouseModesCombo;
    private JButton plus;
    private JButton minus;
    private JButton collapse;
    private JButton expand;
    private JButton reset;
    private final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    private final ScalingControl scaler = new CrossoverScalingControl();
    
    private JButton atualizarButton;
    JComboBox metricComboBox;
    
    
    
    GraphZoomScrollPane gzsp;
    
    Set exclusions;

    public CommitHistoryWindow(ProjectRevisions rep) {
        this.rep = rep;
        
        
        
        
        
        
        
        ProjectService projectService = new ProjectService();
        CreateProjectValuesService createProjectValuesService = new CreateProjectValuesService();
        VersionedProject versionedProject = projectService.getVersionedProject(rep);
        Metric metric = null;
            metric =  new NumberOfBytes();
             
        ProjectValues projectValues = createProjectValuesService.getProjectValues(rep, versionedProject,metric);
        
        
        
        
        initGraphComponent(projectValues);
        
        System.out.println("VV: "+vv);
        
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Commit History for repository " + rep.getName());
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 700) / 2, (screenSize.height - 750) / 2, 700, 750);

        mouseModesCombo = graphMouse.getModeComboBox();
        mouseModesCombo.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });

        minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
            }
        });

        collapse = new JButton("Collapse");
        collapse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                collapseActionPerformed(e);
            }
        });

        expand = new JButton("Expand");
        expand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expandActionPerformed(e);
            }
        });

        reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResetActionPerformed(e);
            }
        });

        edgeLineShapeCombo = new JComboBox();
        this.edgeLineShapeCombo.setModel(new DefaultComboBoxModel(new String[]{"QuadCurve", "Line", "CubicCurve"}));
        this.edgeLineShapeCombo.setSelectedItem("Line");
        this.edgeLineShapeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                edgeLineShapeSelectionActionPerformed(evt);
            }
        });
        
        
        
        
        String metricItems[];
        metricItems = new String[MetricBucket.getInstance().getMetricCasingList().size()];
        for (int i = 0; i < metricItems.length; i++) {
            metricItems[i] = MetricBucket.getInstance().getMetricCasingList().get(i).getName();
        }
        metricComboBox = new JComboBox(metricItems);
        
        atualizarButton = new JButton("atualizar");
        ActionListener act = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    atualizar();
                }
        };
        atualizarButton.addActionListener(act);
        

        Container content = getContentPane();
        gzsp = new GraphZoomScrollPane(vv);
        content.add(gzsp);
        //content.remove(gzsp);
        //gzsp = new GraphZoomScrollPane(vv);
        //content.add(gzsp);

        JPanel controls = new JPanel();
        JPanel zoomControls = new JPanel(new GridLayout(2, 1));
        zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomControls.add(plus);
        zoomControls.add(minus);
        controls.add(zoomControls);
        JPanel collapseControls = new JPanel(new GridLayout(3, 1));
        collapseControls.setBorder(BorderFactory.createTitledBorder("Picked"));
        collapseControls.add(collapse);
        collapseControls.add(expand);
        collapseControls.add(reset);
        controls.add(collapseControls);
        controls.add(mouseModesCombo);
        controls.add(edgeLineShapeCombo);
        controls.add(metricComboBox);
        controls.add(atualizarButton);
        content.add(controls, BorderLayout.SOUTH);
        this.setSize(new Dimension(730, 616));
        
        
    }// </editor-fold>
    
    
    private void atualizar(){
        ProjectService projectService = new ProjectService();
        CreateProjectValuesService createProjectValuesService = new CreateProjectValuesService();
        VersionedProject versionedProject = projectService.getVersionedProject(rep);
        Metric metric = null;
        int selected = metricComboBox.getSelectedIndex();
        
        metric = MetricBucket.getInstance().getMetricCasingList().get(selected).getMetric();

             
        ProjectValues projectValues = createProjectValuesService.getProjectValues(rep, versionedProject,metric);
        Container content = getContentPane();
        content.removeAll();
        initGraphComponent(projectValues);
        initComponents();
        metricComboBox.setSelectedIndex(selected);
       /* Container content = getContentPane();
        System.out.println("gzsp: "+gzsp.getSize());
        content.remove(gzsp);
        
        
        gzsp = new GraphZoomScrollPane(vv);
        gzsp.setSize(new Dimension(727,616));
        System.out.println("gzsp: "+gzsp.getSize());
        
        content.add(gzsp, BorderLayout.NORTH);
        resetGraph();
        this.repaint();*/
        
    }

    // <editor-fold defaultstate="collapsed" desc="initGraphComponent">
    private void initGraphComponent(ProjectValues projectValues) {
        // create the commit history graph with all commits from repository
        graph = BasicRepositoryHistoryGraph.createBasicRepositoryHistoryGraph(rep);
        collapsedGraph = graph;

        // Choosing layout
        layout = new RepositoryHistoryLayout(graph);
        Dimension preferredSize = new Dimension(620, 580);

        VisualizationModel visualizationModel =
                new DefaultVisualizationModel(layout, preferredSize);
        vv = new VisualizationViewer(visualizationModel, preferredSize);

        //Scales the graph to show more nodes
        scaler.scale(vv, 0.4761905F, vv.getCenter());
        vv.scaleToLayout(scaler);

        collapser = new GraphCollapser(graph);

        PredicatedParallelEdgeIndexFunction eif = PredicatedParallelEdgeIndexFunction.getInstance();
        exclusions = new HashSet();
        eif.setPredicate(new Predicate<CommitInfo>() {
            @Override
            public boolean evaluate(CommitInfo e) {
                return exclusions.contains(e);
            }
        });
        vv.getRenderContext().setParallelEdgeIndexFunction(eif);

        vv.setBackground(IConstants.BACKGROUND_COLOR);

        // Adds interaction via mouse
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());

        nodeFilter = new VertexPredicateFilter(new Predicate<CommitInfo>() {
            @Override
            public boolean evaluate(CommitInfo ci) {
                return true;
            }
        });

        edgeFilter = new EdgePredicateFilter(new Predicate<CommitRelationship>() {
            @Override
            public boolean evaluate(CommitRelationship cr) {
                return true;
            }
        });

        // <editor-fold defaultstate="collapsed" desc="vertex tooltip transformer">
        Transformer<Object, String> vertexTooltip = new CHVertexTooltipTransformer(projectValues);
        vv.setVertexToolTipTransformer(vertexTooltip);
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex fillPaint transformer">
        Transformer<Object, Paint> vertexPaint = new CHVertexPaintTransformer(projectValues);
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex label transformer">
        Transformer<Object, String> vertexLabel = new CHVertexLabelTransformer();
        vv.getRenderContext().setVertexLabelTransformer(vertexLabel);
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //</editor-fold>

        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeTransformer());
        //vv.repaint();
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="action handlers">
    private void collapseActionPerformed(ActionEvent evt) {
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        collapse(picked);
    }

    private void expandActionPerformed(ActionEvent e) {
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        expand(picked);
    }

    private void ResetActionPerformed(ActionEvent evt) {
        resetGraph();
    }

    private void edgeLineShapeSelectionActionPerformed(ActionEvent evt) {
        String mode = (String) this.edgeLineShapeCombo.getSelectedItem();
        if (mode.equalsIgnoreCase("QuadCurve")) {
            vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve());
        } else if (mode.equalsIgnoreCase("Line")) {
            vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        } else if (mode.equalsIgnoreCase("CubicCurve")) {
            vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.CubicCurve());
        }
        this.vv.repaint();
    }
    // </editor-fold>

    private void collapse(Collection picked) {
//        AddFilters();
        if (picked.size() > 1) {
            Graph inGraph = layout.getGraph();
            Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);

            collapsedGraph = ((DirectedOrderedSparseMultigraph) collapser.collapse(layout.getGraph(), clusterGraph));
            double sumx = 0;
            double sumy = 0;
            for (Object v : picked) {
                Point2D p = (Point2D) layout.transform(v);
                sumx += p.getX();
                sumy += p.getY();
            }
            Point2D cp = new Point2D.Double(sumx / picked.size(), sumy / picked.size());
            vv.getRenderContext().getParallelEdgeIndexFunction().reset();
            layout.setGraph(collapsedGraph);
            layout.setLocation(clusterGraph, cp);
            vv.getPickedVertexState().clear();
            vv.repaint();
        }
//    RemoveFilters();
    }

    private void expand(Collection picked) {
        for (Object v : picked) {
            if (v instanceof Graph) {
//                AddFilters();
                collapsedGraph = ((DirectedOrderedSparseMultigraph) collapser.expand(layout.getGraph(), (Graph) v));
                vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                layout.setGraph(collapsedGraph);

            }
//            RemoveFilters();
//            Filter();
            
            vv.getPickedVertexState().clear();
            vv.repaint();
        }
    }

    private void resetGraph() {
        layout.setGraph(graph);
        collapsedGraph = graph;
//        RemoveFilters();
        vv.repaint();
    }

    /**
     * runs the graph with a demo repository
     */
    public static void main(String[] args) {
        //ProjectRevisions rep = new ProjectRevisions();
       // rep.setId("rep1363653250218");
//        rep.setId("rep1364318989748");
        //new CommitHistoryWindow(rep).setVisible(true);
    }
}
