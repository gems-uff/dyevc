package br.uff.ic.dyevc.gui.graph;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.graph.GraphBuilder;
import br.uff.ic.dyevc.graph.GraphDomainMapper;
import br.uff.ic.dyevc.graph.layout.RepositoryHistoryLayout;
import br.uff.ic.dyevc.graph.Position;
import br.uff.ic.dyevc.graph.transform.commithistory.CHTopologyVertexDrawPaintTransformer;
import br.uff.ic.dyevc.graph.transform.commithistory.CHTopologyVertexPaintTransformer;
import br.uff.ic.dyevc.graph.transform.commithistory.CHVertexLabelTransformer;
import br.uff.ic.dyevc.graph.transform.commithistory.CHVertexStrokeTransformer;
import br.uff.ic.dyevc.graph.transform.commithistory.CHVertexTooltipTransformer;
import br.uff.ic.dyevc.graph.transform.common.ClusterVertexShapeTransformer;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitTools;
import br.uff.ic.dyevc.utils.PreferencesManager;
import br.uff.ic.dyevc.utils.RepositoryConverter;
import br.uff.ic.dyevc.utils.StopWatchLogger;

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

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.time.StopWatch;

import org.slf4j.LoggerFactory;

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
import java.awt.Stroke;
import java.awt.Toolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    // <editor-fold defaultstate="collapsed" desc="variables declaration">
    private static final long               serialVersionUID = 1689885032823010309L;
    private MonitoredRepository             rep;
    private DirectedOrderedSparseMultigraph graph;
    private DirectedOrderedSparseMultigraph collapsedGraph;
    private VisualizationViewer             vv;
    private RepositoryHistoryLayout         layout;
    private GraphCollapser                  collapser;
    Filter<CommitInfo, CommitRelationship>  edgeFilter;
    Filter<CommitInfo, CommitRelationship>  nodeFilter;
    private JComboBox                       edgeLineShapeCombo;
    private JComboBox                       mouseModesCombo;
    private JButton                         plusButton;
    private JButton                         minusButton;
    private JButton                         collapseButton;
    private JButton                         expandButton;
    private JButton                         resetButton;
    private JButton                         collapseByTypeButton;
    private JButton                         beginButton;
    private JButton                         endButton;
    private JButton                         helpButton;
    private ApplicationSettingsBean         settings;
    private final DefaultModalGraphMouse    graphMouse   = new DefaultModalGraphMouse<CommitInfo, CommitRelationship>();
    private final ScalingControl            scaler       = new CrossoverScalingControl();
    private String                          instructions =
        "<html><p>Each vertex in the graph represents a known commit of this system in the topology.</p>"
        + "<p>Each vertex label shows the commit's five initial characters.</p>"
        + "<p>If commit is head of any local/remote branch or tag, it is painted with a heavier stroke. "
        + "The stroke <BR>&nbsp;&nbsp;&nbsp;&nbsp;will be black if it is pointed by any branch and orange if it is pointed by "
        + "any tags <BR>&nbsp;&nbsp;&nbsp;&nbsp;(even if also pointed by any branches).</p>"
        + "<p>Each vertex is painted according to its existence in this repository and those related to it <br>"
        + "(those which this one pushes to or pulls from): </p>" + "<ul>"
        + "<li>If vertex exists locally and in all related repositories, it is painted in WHITE;</li>"
        + "<li>If vertex exists locally but do not exists in any push list, it is painted in GREEN;</li>"
        + "<li>If vertex doesn't exist locally, but exists in any pull list, it is painted in YELLOW;</li>"
        + "<li>If vertex exists in a node not related to the local one (can't be pulled from it), it is painted in RED.</li>"
        + "<li>Finally, if vertex does not belong to a tracked branch, it is painted in GRAY;</li>" + "</ul>"
        + "<p>Place the mouse over a vertex to view detailed information regarding it.</p>" + "</html>";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="constructor">

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
            settings = PreferencesManager.getInstance().loadPreferences();
//          SplashScreen.getInstance().setVisible(true);
            StopWatchLogger watch = new StopWatchLogger(CommitHistoryWindow.class);
            watch.start();
            initGraphComponent();
            watch.stopAndLog("Process commit history graph for repository <" + rep.getName() + "> with id <"
                             + rep.getId() + ">.");

//          SplashScreen.getInstance().setStatus("Initializing Window components");
            watch.start();

            initComponents();
            translateGraph(Position.END);

            if (settings.isPerformanceMode()) {
                watch.stopAndLog("Plot commit history graph for repository <" + rep.getName() + "> with id <"
                                 + rep.getId() + ">.");
            }
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
    // </editor-fold>

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

        plusButton = new JButton("+");
        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });

        minusButton = new JButton("-");
        minusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
            }
        });

        collapseButton = new JButton("Collapse Picked");
        collapseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                collapseActionPerformed(e);
            }
        });

        expandButton = new JButton("Expand Picked");
        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expandActionPerformed(e);
            }
        });

        resetButton = new JButton("Reset Graph");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResetActionPerformed(e);
            }
        });

        collapseByTypeButton = new JButton("Collapse By Type");
        collapseByTypeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                collapseByTypeActionPerformed(e);
            }
        });

        helpButton = new JButton("Help");
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, instructions, "Help", JOptionPane.PLAIN_MESSAGE);
            }
        });

        beginButton = new JButton("Beginning");
        beginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                translateGraph(Position.START);
            }
        });

        endButton = new JButton("End");
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                translateGraph(Position.END);
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
        zoomControls.add(plusButton);
        zoomControls.add(minusButton);
        controls.add(zoomControls);
        JPanel collapseControls = new JPanel(new GridLayout(4, 1));
        collapseControls.setBorder(BorderFactory.createTitledBorder("Collapsing"));
        collapseControls.add(collapseButton);
        collapseControls.add(expandButton);
        collapseControls.add(collapseByTypeButton);
        collapseControls.add(resetButton);
        controls.add(collapseControls);
        JPanel moveScreen = new JPanel(new GridLayout(2, 1));
        moveScreen.setBorder(BorderFactory.createTitledBorder("Move to:"));
        moveScreen.add(beginButton);
        moveScreen.add(endButton);
        controls.add(moveScreen);
        JPanel mouseMode = new JPanel(new GridLayout(1, 1));
        mouseMode.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        mouseMode.add(mouseModesCombo);
        controls.add(mouseMode);
        JPanel edgeLineType = new JPanel(new GridLayout(1, 1));
        edgeLineType.setBorder(BorderFactory.createTitledBorder("Edge Line Type"));
        edgeLineType.add(edgeLineShapeCombo);
        controls.add(edgeLineType);
        controls.add(helpButton);
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
        Transformer<Object, String> vertexTooltip = new CHVertexTooltipTransformer(info, tools.getHeadsCommitsMap(),
                                                        tools.getTagsCommitsMap());
        vv.setVertexToolTipTransformer(vertexTooltip);
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex fillPaint transformer">
        Transformer<Object, Paint> vertexPaint = new CHTopologyVertexPaintTransformer();
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex drawPaint transformer">
        Transformer<Object, Paint> drawPaint = new CHTopologyVertexDrawPaintTransformer(tools.getHeadsCommitsMap(),
                                                   tools.getTagsCommitsMap());
        vv.getRenderContext().setVertexDrawPaintTransformer(drawPaint);

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex stroke transformer">
        Transformer<Object, Stroke> vertexStroke = new CHVertexStrokeTransformer(tools.getHeadsCommitsMap(),
                                                       tools.getTagsCommitsMap());
        vv.getRenderContext().setVertexStrokeTransformer(vertexStroke);

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

    // <editor-fold defaultstate="collapsed" desc="collapsing and expanding">
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="collapseByType">
    private void collapseByType2() {
        layout.setGraph(graph);
        collapsedGraph = graph;
        List<List>      groups          = new ArrayList<List>();
        Map<Byte, List> groupsByTypeMap = new TreeMap<Byte, List>();
        groupsByTypeMap.put(IConstants.COMMIT_MASK_ALL_HAVE, new ArrayList<List>());
        groupsByTypeMap.put(IConstants.COMMIT_MASK_I_HAVE_PUSH_DONT, new ArrayList<List>());
        groupsByTypeMap.put(IConstants.COMMIT_MASK_I_DONT_PULL_HAS, new ArrayList<List>());
        groupsByTypeMap.put(IConstants.COMMIT_MASK_NON_RELATED_HAS, new ArrayList<List>());
        groupsByTypeMap.put(IConstants.COMMIT_MASK_NOT_TRACKED, new ArrayList<List>());
        byte currentType  = IConstants.COMMIT_MASK_ALL_HAVE;
        List currentGroup = new ArrayList();

        for (Object o : graph.getVertices()) {
            CommitInfo currentNode      = (CommitInfo)o;
            boolean    childrenSameType = checkChildrenForSameType(currentNode);
            if (currentNode.getType() == currentType) {
                if (childrenSameType) {
                    currentGroup.add(currentNode);
                } else {                              // not all children are of same type as currentType
                    if (!currentGroup.isEmpty()) {    // include currentNode into currentGroup and close the group
                        groupsByTypeMap.get(currentType).add(currentGroup);
                        groups.add(currentGroup);
                        currentGroup = new ArrayList();
                    }

                    currentGroup.add(currentNode);
                    groupsByTypeMap.get(currentType).add(currentGroup);
                    groups.add(currentGroup);
                    currentGroup = new ArrayList();
                }
            } else {                                  // currentNode has a different type from currentType
                if (!currentGroup.isEmpty()) {        // close currentGroup to create a new one
                    groupsByTypeMap.get(currentType).add(currentGroup);
                    groups.add(currentGroup);
                    currentGroup = new ArrayList();
                }

                currentType = currentNode.getType();
                currentGroup.add(currentNode);

                if (!childrenSameType) {
                    groupsByTypeMap.get(currentType).add(currentGroup);
                    groups.add(currentGroup);
                    currentGroup = new ArrayList();
                }
            }
        }

        Point2D currentCoords = new Point2D.Double(0, 0);
        for (List list : groups) {
            doCollapseByType(list, currentCoords);
            currentCoords = new Point2D.Double(currentCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
                                               currentCoords.getY());
        }

        vv.getRenderContext().getParallelEdgeIndexFunction().reset();
        vv.getPickedVertexState().clear();
        translateGraph(Position.START);
        vv.repaint();
    }

    private boolean checkChildrenForSameType(CommitInfo currentNode) {
        boolean result = true;
        for (Object o : graph.getPredecessors(currentNode)) {
            CommitInfo ci = (CommitInfo)o;
            if (ci.getType() != currentNode.getType()) {
                result = false;

                break;
            }
        }

        return result;
    }

    private void collapseByType() {
        layout.setGraph(graph);
        collapsedGraph = graph;
        Collection allHave        = new ArrayList();
        Collection iHavePushDont  = new ArrayList();
        Collection iDontPullHas   = new ArrayList();
        Collection nonRelatedHas  = new ArrayList();
        Collection notTracked     = new ArrayList();
        Point2D    previousCoords = null;
        for (Object o : layout.getGraph().getVertices()) {
            CommitInfo ci = (CommitInfo)o;
            if (layout.getGraph().getSuccessorCount(o) == 0) {
                previousCoords = layout.transform(o);
            }

            switch (ci.getType()) {
            case IConstants.COMMIT_MASK_ALL_HAVE :
                allHave.add(o);

                break;

            case IConstants.COMMIT_MASK_I_HAVE_PUSH_DONT :
                iHavePushDont.add(o);

                break;

            case IConstants.COMMIT_MASK_I_DONT_PULL_HAS :
                iDontPullHas.add(o);

                break;

            case IConstants.COMMIT_MASK_NON_RELATED_HAS :
                nonRelatedHas.add(o);

                break;

            case IConstants.COMMIT_MASK_NOT_TRACKED :
                notTracked.add(o);

                break;
            }
        }

        Point2D newCoords = previousCoords;
        if (allHave.size() > 0) {
            newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
                                           previousCoords.getY());
            doCollapseByType(allHave, newCoords);
            previousCoords = newCoords;
        }

        if (iHavePushDont.size() > 0) {
            newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
                                           previousCoords.getY());
            doCollapseByType(iHavePushDont, newCoords);
            previousCoords = newCoords;
        }

        if (notTracked.size() > 0) {
            newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
                                           previousCoords.getY() + 2 * RepositoryHistoryLayout.XDISTANCE);
            doCollapseByType(notTracked, newCoords);
        }

        if (iDontPullHas.size() > 0) {
            newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
                                           previousCoords.getY());
            doCollapseByType(iDontPullHas, newCoords);
            previousCoords = newCoords;
        }

        if (nonRelatedHas.size() > 0) {
            newCoords = new Point2D.Double(previousCoords.getX() + 2 * RepositoryHistoryLayout.XDISTANCE,
                                           previousCoords.getY());
            doCollapseByType(nonRelatedHas, newCoords);
            previousCoords = newCoords;
        }

        vv.getRenderContext().getParallelEdgeIndexFunction().reset();
        vv.getPickedVertexState().clear();
        translateGraph(Position.START);
        vv.repaint();
    }

    private void doCollapseByType(Collection picked, Point2D coords) {
        Graph inGraph      = layout.getGraph();
        Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);

        collapsedGraph = ((DirectedOrderedSparseMultigraph)collapser.collapse(inGraph, clusterGraph));
        layout.setGraph(collapsedGraph);
        layout.setLocation(clusterGraph, coords);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="reset">
    private void resetGraph() {
        layout.setGraph(graph);
        collapsedGraph = graph;
        layout.initialize(true);
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
    // </editor-fold>

    /**
     * runs the graph with a demo repository
     */
    public static void main(String[] args) {
        MonitoredRepositories reps = PreferencesManager.getInstance().loadMonitoredRepositories();
        MonitoredRepository   rep  = MonitoredRepositories.getMonitoredProjectById("rep1391645758732");    // saposTeste
        new CommitHistoryWindow(rep).setVisible(true);
    }

    /**
     * Translates graph, positioning it at the farthest X position.
     */
    private void translateGraph(Position position) {
        MutableTransformer modelTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
        double currentX = modelTransformer.getTranslateX();
        double currentY = modelTransformer.getTranslateY();
        double dx;
        double dy;
        int    showPosition = layout.getWidth() - vv.getPreferredSize().width;
        Point  graphEnd     = new Point(showPosition, 0);
        Point  graphStart   = new Point(0, 0);
        if (position == Position.START) {
            dx = (graphStart.getX() - currentX);
            dy = (graphStart.getY() - currentY);
        } else {
            dx = (-currentX - graphEnd.getX());
            dy = (-currentY - graphEnd.getY());
        }

        modelTransformer.translate(dx, dy);
    }
}
