package br.uff.ic.dyevc.gui.graph;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.graph.GraphBuilder;
import br.uff.ic.dyevc.graph.GraphDomainMapper;
import br.uff.ic.dyevc.graph.layout.RepositoryHistoryLayout;
import br.uff.ic.dyevc.graph.transform.commithistory.CHTopologyVertexPaintTransformer;
import br.uff.ic.dyevc.graph.transform.commithistory.CHVertexLabelTransformer;
import br.uff.ic.dyevc.graph.transform.commithistory.CHVertexTooltipTransformer;
import br.uff.ic.dyevc.graph.transform.common.ClusterVertexShapeTransformer;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import br.uff.ic.dyevc.utils.RepositoryConverter;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import org.apache.commons.collections15.MapUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Toolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/**
 * Displays the commit history for the specified repository
 *
 * @author cristiano
 */
public class CommitHistoryWindow extends javax.swing.JFrame {
    private static final long               serialVersionUID = 1689885032823010309L;
    private MonitoredRepository             rep;
    private DirectedOrderedSparseMultigraph graph;
    private DirectedOrderedSparseMultigraph collapsedGraph;
    private VisualizationViewer             vv;
    private RepositoryHistoryLayout         layout;
    private boolean                         isCollapsed;
    private GraphCollapser                  collapser;
    Filter<CommitInfo, CommitRelationship>  edgeFilter;
    Filter<CommitInfo, CommitRelationship>  nodeFilter;
    private JComboBox                       edgeLineShapeCombo;
    private JComboBox                       mouseModesCombo;
    private JButton                         plus;
    private JButton                         minus;
    private JButton                         collapse;
    private JButton                         expand;
    private JButton                         reset;
    private JButton                         collapseByType;
    private JButton                         resetByType;
    private JButton                         btnHelp;
    private final DefaultModalGraphMouse    graphMouse   = new DefaultModalGraphMouse<CommitInfo, CommitRelationship>();
    private final ScalingControl            scaler       = new CrossoverScalingControl();
    private String                          instructions =
        "<html><p>Each vertex in the graph represents a known commit of this system in the topology.</p>"
        + "<p>Each vertex label shows the commit's five initial characters.</p>"
        + "<p>Each vertex is painted according to its existence in this repository and those related to it <br>"
        + "(those which this one pushes to or pulls from): </p>" + "<ul>"
        + "<li>If vertex exists locally and in all related repositories, it is painted in WHITE;</li>"
        + "<li>If vertex exists locally but do not exists in any push list, it is painted in GREEN;</li>"
        + "<li>If vertex doesn't exist locally, but exists in any pull list, it is painted in YELLOW;</li>"
        + "<li>If vertex exists in a node not related to the local one (can't be pulled from it), it is painted in RED.</li>"
        + "<li>Finally, if vertex does not belong to a tracked branch, it is painted in GRAY;</li>" + "</ul>"
        + "<p>Place the mouse over a vertex to view detailed information regarding it.</p>" + "</html>";

    /**
     * Constructs a CommitHistoryWindow
     *
     * @param rep
     */
    public CommitHistoryWindow(MonitoredRepository rep) {
//      SplashScreen splash = SplashScreen.getInstance();
        try {
//          splash.setStatus("Initializing Graph component");
            this.rep = rep;
//          SplashScreen.getInstance().setVisible(true);
            initGraphComponent();
//          SplashScreen.getInstance().setStatus("Initializing Window components");
            initComponents();
            translateGraph();
//          SplashScreen.getInstance().setVisible(false);
        } catch (DyeVCException ex) {
//          splash.dispose();
            JOptionPane.showMessageDialog(
                null,
                "Application received the following exception trying to show repository log:\n" + ex
                + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
            WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            setVisible(false);
            dispose();
        } catch (RuntimeException ex) {
            ex.printStackTrace(System.err);
//          splash.dispose();
            JOptionPane.showMessageDialog(
                null,
                "Application received the following exception trying to show repository log:\n" + ex
                + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
            WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            setVisible(false);
            dispose();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                resetComponents();
            }
        });
        setAutoRequestFocus(true);
        setTitle("Commit History for repository " + rep.getName());
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 700) / 2, (screenSize.height - 750) / 2, 700, 750);

        mouseModesCombo = graphMouse.getModeComboBox();
        mouseModesCombo.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

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

        collapseByType = new JButton("Collapse By Type");
        collapseByType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                collapseByTypeActionPerformed(e);
            }
        });

        resetByType = new JButton("Reset All Types");
        resetByType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAllTypesActionPerformed(e);
            }
        });

        btnHelp = new JButton("Help");
        btnHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, instructions, "Help", JOptionPane.PLAIN_MESSAGE);
            }
        });

        edgeLineShapeCombo = new JComboBox();
        this.edgeLineShapeCombo.setModel(new DefaultComboBoxModel(new String[] { "QuadCurve", "Line", "CubicCurve" }));
        this.edgeLineShapeCombo.setSelectedItem("CubicCurve");
        this.edgeLineShapeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                edgeLineShapeSelectionActionPerformed(evt);
            }
        });

        Container           content = getContentPane();
        Container           panel   = new JPanel(new BorderLayout());

        GraphZoomScrollPane gzsp    = new GraphZoomScrollPane(vv);
        panel.add(gzsp);

        JPanel controls     = new JPanel();
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
        JPanel collapseControlsByType = new JPanel(new GridLayout(2, 1));
        collapseControlsByType.setBorder(BorderFactory.createTitledBorder("Collapse By Type"));
        collapseControlsByType.add(collapseByType);
        collapseControlsByType.add(resetByType);
        controls.add(collapseControlsByType);
        controls.add(mouseModesCombo);
        controls.add(edgeLineShapeCombo);
        controls.add(btnHelp);
        content.add(panel);
        content.add(controls, BorderLayout.SOUTH);
    }    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="initGraphComponent">
    private void initGraphComponent() throws DyeVCException {

        // create the commit history graph with all commits from repository and maps the graph to the source commit map
        GitCommitTools tools = GitCommitTools.getInstance(rep, true);
        RepositoryInfo info  = new RepositoryConverter(rep).toRepositoryInfo();
        tools.loadExternalCommits(info);
        graph = GraphBuilder.createBasicRepositoryHistoryGraph(tools);
        GraphDomainMapper<Map<String, CommitInfo>> mapper = new GraphDomainMapper(graph, tools.getCommitInfoMap());
        collapsedGraph = graph;
        isCollapsed    = false;
        Dimension preferredSize = new Dimension(580, 580);

        // Choosing layout
        layout = new RepositoryHistoryLayout(mapper, rep, preferredSize);

        final VisualizationModel visualizationModel = new DefaultVisualizationModel(layout, preferredSize);
        vv = new VisualizationViewer(visualizationModel, preferredSize);

        // Scales the graph to show more nodes
        scaler.scale(vv, 0.4761905F, vv.getCenter());
        vv.scaleToLayout(scaler);

        collapser = new GraphCollapser(graph);

        final PredicatedParallelEdgeIndexFunction eif        = PredicatedParallelEdgeIndexFunction.getInstance();
        final Set                                 exclusions = new HashSet();
        eif.setPredicate(new Predicate<CommitRelationship>() {
            @Override
            public boolean evaluate(CommitRelationship e) {
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
        Transformer<Object, String> vertexTooltip = new CHVertexTooltipTransformer(info);
        vv.setVertexToolTipTransformer(vertexTooltip);
        ToolTipManager.sharedInstance().setDismissDelay(15000);

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex fillPaint transformer">
        Transformer<Object, Paint> vertexPaint = new CHTopologyVertexPaintTransformer();
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex label transformer">
        Transformer<Object, String> vertexLabel = new CHVertexLabelTransformer();
        vv.getRenderContext().setVertexLabelTransformer(vertexLabel);
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        // </editor-fold>
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.CubicCurve());
        vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeTransformer());
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="action handlers">
    private void collapseActionPerformed(ActionEvent evt) {
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        collapse(picked);
    }

    private void collapseByTypeActionPerformed(ActionEvent evt) {
        collapseByType();
    }

    private void resetAllTypesActionPerformed(ActionEvent evt) {
        resetAllTypes();
    }

    private void expandActionPerformed(ActionEvent e) {
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        expand(picked);
    }

    private void ResetActionPerformed(ActionEvent evt) {
        resetGraph();
    }

    private void edgeLineShapeSelectionActionPerformed(ActionEvent evt) {
        String mode = (String)this.edgeLineShapeCombo.getSelectedItem();
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

//      AddFilters();
        if (picked.size() > 1) {
            Graph inGraph      = layout.getGraph();
            Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);

            collapsedGraph = ((DirectedOrderedSparseMultigraph)collapser.collapse(layout.getGraph(), clusterGraph));
            double sumx = 0;
            double sumy = 0;
            for (Object v : picked) {
                Point2D p = (Point2D)layout.transform(v);
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

//      RemoveFilters();
    }

    private void collapseByType() {
        layout.setGraph(graph);
        collapsedGraph = graph;
        HashMap<Byte, LinkedList<CommitInfo>> tempGroups = new HashMap<Byte, LinkedList<CommitInfo>>();
        tempGroups.put(IConstants.COMMIT_MASK_ALL_HAVE, new LinkedList<CommitInfo>());
        tempGroups.put(IConstants.COMMIT_MASK_I_HAVE_PUSH_DONT, new LinkedList<CommitInfo>());
        tempGroups.put(IConstants.COMMIT_MASK_I_DONT_PULL_HAS, new LinkedList<CommitInfo>());
        tempGroups.put(IConstants.COMMIT_MASK_NON_RELATED_HAS, new LinkedList<CommitInfo>());
        tempGroups.put(IConstants.COMMIT_MASK_NOT_TRACKED, new LinkedList<CommitInfo>());
        SortedMap<Double, LinkedList<CommitInfo>> collapsedGroups = new TreeMap<Double, LinkedList<CommitInfo>>();

        Point2D                                   previousCoords  = new Point2D.Double(0d, 0d);
        byte                                      currentType     = IConstants.COMMIT_MASK_ALL_HAVE;


        for (Object o : graph.getVertices()) {
            CommitInfo ci = (CommitInfo)o;

            if (graph.getSuccessorCount(ci) == 0) {
                tempGroups.get(IConstants.COMMIT_MASK_ALL_HAVE).add(ci);

                continue;
            }

            boolean sametype = true;
            for (Object child : graph.getPredecessors(ci)) {
                if (((CommitInfo)child).getType() != currentType) {
                    sametype = false;

                    break;
                }
            }

            if (!sametype) {
                LinkedList<CommitInfo> listToClose = tempGroups.remove(currentType);
                if ((listToClose != null) && (listToClose.size() != 0)) {
                    Double position = Double.valueOf(layout.transform(listToClose.peek()).getX());
                    collapsedGroups.put(position, listToClose);
                }

                tempGroups.put(currentType, new LinkedList<CommitInfo>());
                currentType = ci.getType();
            } else {
                tempGroups.get(currentType).addLast(ci);
            }
        }

        Point2D newCoords = previousCoords;
        for (LinkedList<CommitInfo> resto : tempGroups.values()) {
            if (resto.size() != 0) {
                Double position = Double.valueOf(layout.transform(resto.peek()).getX());
                collapsedGroups.put(position, resto);
            }
        }

        for (Double key : collapsedGroups.keySet()) {
            doCollapseByType(collapsedGroups.get(key), newCoords);
            newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
                                           previousCoords.getY());
            previousCoords = newCoords;
        }
//      if (allHave.size() > 0) {
//          newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
//                                         previousCoords.getY());
//          doCollapseByType(allHave, newCoords);
//          previousCoords = newCoords;
//      }
//
//      if (iHavePushDont.size() > 0) {
//          newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
//                                         previousCoords.getY());
//          doCollapseByType(iHavePushDont, newCoords);
//          previousCoords = newCoords;
//      }
//
//      if (notTracked.size() > 0) {
//          newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
//                                         previousCoords.getY() + 2 * RepositoryHistoryLayout.XDISTANCE);
//          doCollapseByType(notTracked, newCoords);
//      }
//
//      if (iDontPullHas.size() > 0) {
//          newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
//                                         previousCoords.getY());
//          doCollapseByType(iDontPullHas, newCoords);
//          previousCoords = newCoords;
//      }
//
//      if (nonRelatedHas.size() > 0) {
//          newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
//                                         previousCoords.getY());
//          doCollapseByType(nonRelatedHas, newCoords);
//          previousCoords = newCoords;
//      }

        vv.getRenderContext().getParallelEdgeIndexFunction().reset();
        vv.getPickedVertexState().clear();
        isCollapsed = true;
        translateGraph();
        vv.repaint();
    }

    private void doCollapseByType(Collection picked, Point2D coords) {
        Graph inGraph      = layout.getGraph();
        Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);

        collapsedGraph = ((DirectedOrderedSparseMultigraph)collapser.collapse(inGraph, clusterGraph));
        layout.setCollapsed(true);
        layout.setGraph(collapsedGraph);
        layout.setLocation(clusterGraph, coords);
    }

    private void expand(Collection picked) {
        for (Object v : picked) {
            if (v instanceof Graph) {

//              AddFilters();
                collapsedGraph = ((DirectedOrderedSparseMultigraph)collapser.expand(layout.getGraph(), (Graph)v));
                vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                layout.setGraph(collapsedGraph);

            }

//          RemoveFilters();
//          Filter();
            vv.getPickedVertexState().clear();
            vv.repaint();
        }
    }

    private void resetAllTypes() {
        resetGraph();
        translateGraph();
    }

    private void resetGraph() {
        layout.setCollapsed(false);
        layout.setGraph(graph);
        collapsedGraph = graph;
        isCollapsed    = false;
        layout.initialize();
        vv.repaint();
    }

    private void resetComponents() {
        rep            = null;
        graph          = null;
        collapsedGraph = null;
        vv             = null;
        layout         = null;
        collapser      = null;
        edgeFilter     = null;
        nodeFilter     = null;
    }

    /**
     * runs the graph with a demo repository
     */
    public static void main(String[] args) {
//      MonitoredRepository rep = new MonitoredRepository("rep1386777018509");
//      rep.setCloneAddress("F:/mybackups/Educacao/Mestrado-UFF/Git/git");
        MonitoredRepository rep = new MonitoredRepository("rep1391645758732");
        rep.setCloneAddress("F:\\mybackups\\Educacao\\Mestrado-UFF\\Git\\saposTeste");
        rep.setName("saposTeste");
        rep.setSystemName("sapos");

        new CommitHistoryWindow(rep).setVisible(true);
    }

    /**
     * Translates graph, positioning it at the farthest X position.
     */
    private void translateGraph() {
        MutableTransformer modelTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
        float dx;
        float dy;
        int   lastXPosition = graph.getVertexCount() * (int)RepositoryHistoryLayout.XDISTANCE;
        int   showPosition  = lastXPosition - vv.getPreferredSize().width;
        Point graphEnd      = new Point(layout.getWidth() - vv.getPreferredSize().width, 0);
        Point graphStart    = new Point(0, 0);
        if (isCollapsed) {
            dx = (float)(showPosition);
            dy = (float)(graphEnd.getY() - graphStart.getY());
        } else {
            dx = (float)(graphStart.getX() - graphEnd.getX());
            dy = (float)(graphStart.getY() - graphEnd.getY());
        }

        modelTransformer.translate(dx, dy);
    }
}
