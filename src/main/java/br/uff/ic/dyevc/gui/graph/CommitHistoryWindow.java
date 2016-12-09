package br.uff.ic.dyevc.gui.graph;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.VCSException;
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
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.model.CollapsedCommitInfo;
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
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import org.jfree.util.Log;

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
        
        graph = autoCollapse1(graph);
        graph = autoCollapse2(graph);
        graph = autoCollapse1(graph);
        graph = autoCollapse2(graph);
        
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
    
    /**
     * Automatically collapses a graph based on the existence of commit in other partners.
     * Complete method documentation...
     * @param graph Graph to be collapsed
     * @return The original graph, collapsed by....
     */
    private DirectedOrderedSparseMultigraph autoCollapse1(DirectedOrderedSparseMultigraph graph) {
        //TODO implement method to automatically collapse graph...
        DirectedOrderedSparseMultigraph new_graph = new DirectedOrderedSparseMultigraph();
        
        // Use sets for representing the all commit set
        Set<CommitInfo> visited_set = new HashSet<CommitInfo>();
        
        Set<CommitInfo> not_collapsed_set = new HashSet<CommitInfo>();
        Set<CollapsedCommitInfo> collapses = new HashSet<CollapsedCommitInfo>();
        Set<CommitRelationship> edges = new HashSet<CommitRelationship>();
        
        for (Object v : graph.getVertices())
        {
            CommitInfo currentNode = (CommitInfo)v;
            
            if(!visited_set.contains(currentNode) && DegreeTwo(currentNode))
            {
                CollapsedCommitInfo collapsed_nodes = new CollapsedCommitInfo(currentNode);
                CommitInfo last_parent = AddAllParentsToCollapse(currentNode, collapsed_nodes, visited_set);
                CommitInfo last_child = AddAllChildrenToCollapse(currentNode, collapsed_nodes, visited_set);
                // If collapse is made
                if (last_child != last_parent)
                {
                    collapsed_nodes.SetAncestor(last_parent);
                    collapsed_nodes.incrementParents();
                    collapsed_nodes.SetDescendant(last_child);
                    collapsed_nodes.incrementChildren();
                    collapses.add(collapsed_nodes);
                    
                    CommitInfo parent_of_collapse = GetFirstParent(last_parent);
                    CommitInfo child_of_collapse = GetFirstChild(last_child);
                    
                        edges.add(new CommitRelationship(parent_of_collapse, collapsed_nodes));
                        edges.add(new CommitRelationship(collapsed_nodes, child_of_collapse));
                }
                else {not_collapsed_set.add(currentNode);}
            }
            else
            {
                if(!visited_set.contains(currentNode)) not_collapsed_set.add(currentNode);
            }
            visited_set.add(currentNode);
        }
        
        for (CommitInfo commitInfo : not_collapsed_set)
        {
            new_graph.addVertex(commitInfo);
        }
        for (CollapsedCommitInfo collapse : collapses)
        {
            new_graph.addVertex(collapse);
        }
        for (CommitRelationship commitRelationship : edges)
        {
            new_graph.addEdge(commitRelationship, commitRelationship.getChild(), commitRelationship.getParent());
        }
        for (Object commitRel : graph.getEdges())
        {
            CommitRelationship cr = (CommitRelationship) commitRel;
            if(not_collapsed_set.contains(cr.getChild()) && not_collapsed_set.contains(cr.getParent()))
                new_graph.addEdge(cr, cr.getChild(), cr.getParent());
        }
        
        return new_graph;
    }

    /**
     * Collapses parents when they are chains of commits with different color
     * @param collapsed_nodes
     * @param parent_of_collapse
     * @param currentNode
     * @param visited_set
     * @param collapses
     * @param edges 
     */
    private void CollapseAdjacentParents(CollapsedCommitInfo collapsed_nodes, CommitInfo parent_of_collapse,
            Set<CommitInfo> visited_set, Set<CollapsedCommitInfo> collapses, Set<CommitRelationship> edges) {
        // Firstly, look the parent of collapse:
        CollapsedCommitInfo previous_collapse_par = collapsed_nodes;
        while(DegreeTwo(parent_of_collapse))
        {
            CollapsedCommitInfo collapsed_nodes_parents = new CollapsedCommitInfo(parent_of_collapse);
            CommitInfo last_parent_p = AddAllParentsToCollapse(parent_of_collapse, collapsed_nodes_parents, visited_set);
            if(last_parent_p != parent_of_collapse)
            {
                // There is a new collapsed node linked to previous
                collapsed_nodes_parents.SetAncestor(last_parent_p);
                collapsed_nodes_parents.SetDescendant(parent_of_collapse);
                collapses.add(collapsed_nodes_parents);
                edges.add(new CommitRelationship(collapsed_nodes_parents, previous_collapse_par));
                previous_collapse_par = collapsed_nodes_parents;
                parent_of_collapse = GetFirstParent(last_parent_p);
            }
            else
            {
                edges.add(new CommitRelationship(parent_of_collapse, previous_collapse_par));
                break;
            }
        }
    }
    
        /**
     * Collapses children when they are chains of commits with different color
     * @param collapsed_nodes
     * @param parent_of_collapse
     * @param currentNode
     * @param visited_set
     * @param collapses
     * @param edges 
     */
    private void CollapseAdjacentChildren(CollapsedCommitInfo collapsed_nodes, CommitInfo child_of_collapse, 
            Set<CommitInfo> visited_set, Set<CollapsedCommitInfo> collapses, Set<CommitRelationship> edges) {
        CollapsedCommitInfo previous_collapse_ch = collapsed_nodes;
        while(DegreeTwo(child_of_collapse))
        {
            CollapsedCommitInfo collapsed_nodes_children = new CollapsedCommitInfo(child_of_collapse);
            CommitInfo last_children_c = AddAllChildrenToCollapse(child_of_collapse, collapsed_nodes_children, visited_set);
            if(last_children_c != child_of_collapse)
            {
                // There is a new collapsed node linked to previous
                collapsed_nodes_children.SetAncestor(child_of_collapse);
                collapsed_nodes_children.SetDescendant(last_children_c);
                collapses.add(collapsed_nodes_children);
                edges.add(new CommitRelationship(previous_collapse_ch, collapsed_nodes_children));
                previous_collapse_ch = collapsed_nodes_children;
                child_of_collapse = GetFirstChild(last_children_c);
            }
            else
            {
                edges.add(new CommitRelationship(previous_collapse_ch, child_of_collapse));
                break;
            }
        }
    }
    
    /**
     * Visit and add to visited_set all parents of currentNode with same condition
     * @param currentNode
     * @param collapsed_nodes
     * @param visited_set
     * @return last element added
     */
    private CommitInfo AddAllParentsToCollapse(CommitInfo currentNode, CollapsedCommitInfo collapsed_nodes, Set<CommitInfo> visited_set)
    {
        CommitInfo parent = currentNode;
        CommitInfo last_collapsed_parent = null;
        while(!visited_set.contains(parent) && DegreeTwo(parent) && parent.getType() == currentNode.getType())
        {
            //Add to new collapsed node
            visited_set.add(parent);
            collapsed_nodes.AddCommitToCollapse(parent);
            last_collapsed_parent = parent;
            parent = GetFirstParent(parent);
        }
        return last_collapsed_parent == null? currentNode : last_collapsed_parent;
    }
    
    /**
     * Visit and add to visited_set all children of currentNode with same condition
     * @param currentNode
     * @param collapsed_nodes
     * @param visited_set
     * @return last element added
     */
    private CommitInfo AddAllChildrenToCollapse(CommitInfo currentNode, CollapsedCommitInfo collapsed_nodes, Set<CommitInfo> visited_set)
    {
        CommitInfo child = currentNode;
        CommitInfo last_collapsed_child = null;
        while((!visited_set.contains(child) && DegreeTwo(child) && child.getType() == currentNode.getType())
                || child == currentNode)
        {
            //Add to new collapsed node
            visited_set.add(child);
            collapsed_nodes.AddCommitToCollapse(child);
            last_collapsed_child = child;
            child = GetFirstChild(child);
        }
        return last_collapsed_child == null? currentNode : last_collapsed_child;
    }
    
    private CommitInfo GetFirstParent(CommitInfo ci)
    {
        return (CommitInfo) graph.getPredecessors(ci).iterator().next();
    }
    
    private CommitInfo GetFirstChild(CommitInfo ci)
    {
        return (CommitInfo) graph.getSuccessors(ci).iterator().next();
    }
    
    private boolean DegreeTwo(CommitInfo node)
    {
        return graph.getPredecessorCount(node) == 1 && graph.getSuccessorCount(node) == 1;
    }
    
    private DirectedOrderedSparseMultigraph autoCollapse2(DirectedOrderedSparseMultigraph graph) {
        //TODO implement method to automatically collapse graph...
        DirectedOrderedSparseMultigraph new_graph = new DirectedOrderedSparseMultigraph();
        
        // Use sets for representing the all commit set
        Set<CommitInfo> visited_set = new HashSet<CommitInfo>();
        
        Set<CommitInfo> not_collapsed_set = new HashSet<CommitInfo>();
        Set<CollapsedCommitInfo> collapses = new HashSet<CollapsedCommitInfo>();
        Set<CommitRelationship> edges = new HashSet<CommitRelationship>();
        Map<CommitInfo, CollapsedCommitInfo> node_collapse_dict = new HashMap<CommitInfo, CollapsedCommitInfo>();
        
        for (Object v : graph.getVertices())
        {
            CommitInfo currentNode = (CommitInfo)v;

            if(!visited_set.contains(currentNode))
            {
                if(graph.getPredecessorCount(currentNode) == 1 && graph.getSuccessorCount(currentNode) == 2) // CONDITION 1
                {
                    Object[] children = graph.getSuccessors(currentNode).toArray();
                    CommitInfo child1 = (CommitInfo) children[0];
                    CommitInfo child2 = (CommitInfo) children[1];
                    
                    if(currentNode.getType() == child1.getType() && currentNode.getType() == child2.getType() &&
                            graph.getSuccessorCount(child1) == 1 && graph.getSuccessorCount(child2) == 1)
                    {
                        CollapsedCommitInfo collapsed_nodes = new CollapsedCommitInfo(currentNode);
                        // Collapse vertexes (2 cases)
                        if((GetFirstChild(child1) == child2 && graph.getPredecessorCount(child1) == 1 && graph.getPredecessorCount(child2) == 2) ||
                                (GetFirstChild(child2) == child1 && graph.getPredecessorCount(child1) == 2 && graph.getPredecessorCount(child2) == 1)) // First case
                        {
                            collapsed_nodes.AddCommitToCollapse(child1);
                            // If collapse is made, remove collapsed vertexes from not_collapsed_set
                            // (they can be there due to unknown order of enumeration)
                            not_collapsed_set.remove(child1);
                            visited_set.add(child1);
                            collapsed_nodes.AddCommitToCollapse(child2);
                            not_collapsed_set.remove(child2);
                            visited_set.add(child2);
                            collapsed_nodes.SetAncestor(GetFirstParent(currentNode));
                            collapsed_nodes.incrementParents();
                            collapses.add(collapsed_nodes);
                            // Add edges pointing outside the collapse
                            edges.add(new CommitRelationship(GetFirstParent(currentNode), currentNode));
                            CommitInfo collapse_child = GetFirstChild(child1) == child2 ? GetFirstChild(child2) : GetFirstChild(child1);
                            edges.add(new CommitRelationship(currentNode, collapse_child));
                            collapsed_nodes.SetDescendant(GetFirstChild(collapse_child));
                            collapsed_nodes.incrementChildren();
                            
                            node_collapse_dict.put(currentNode, collapsed_nodes);
                            node_collapse_dict.put(child1, collapsed_nodes);
                            node_collapse_dict.put(child2, collapsed_nodes);
                        }
                        else if(graph.getPredecessorCount(child1) == 1 && graph.getPredecessorCount(child2) == 1 &&
                                GetFirstChild(child1) == GetFirstChild(child2) && graph.getSuccessorCount(GetFirstChild(child1)) == 1 &&
                                GetFirstChild(child1).getType() == currentNode.getType()) // Second case
                        {
                            CommitInfo child_of_childs = GetFirstChild(child1);
                            collapsed_nodes.AddCommitToCollapse(child1);
                            not_collapsed_set.remove(child1);
                            visited_set.add(child1);
                            collapsed_nodes.AddCommitToCollapse(child2);
                            not_collapsed_set.remove(child2);
                            visited_set.add(child2);
                            collapsed_nodes.AddCommitToCollapse(child_of_childs);
                            not_collapsed_set.remove(child_of_childs);
                            visited_set.add(child_of_childs);
                            collapsed_nodes.SetAncestor(GetFirstParent(currentNode));
                            collapsed_nodes.incrementParents();
                            collapses.add(collapsed_nodes);
                            
                            edges.add(new CommitRelationship(GetFirstParent(currentNode), currentNode));
                            CommitInfo child_of_child_of_childs = GetFirstChild(child_of_childs);
                            edges.add(new CommitRelationship(currentNode, child_of_child_of_childs));
                            collapsed_nodes.SetDescendant(GetFirstChild(child_of_child_of_childs));
                            collapsed_nodes.incrementChildren();
                            
                            node_collapse_dict.put(currentNode, collapsed_nodes);
                            node_collapse_dict.put(child1, collapsed_nodes);
                            node_collapse_dict.put(child2, collapsed_nodes);
                            node_collapse_dict.put(child_of_childs, collapsed_nodes);
                        }
                        else {not_collapsed_set.add(currentNode);}
                    }
                    else {not_collapsed_set.add(currentNode);}
                }
                else {not_collapsed_set.add(currentNode);}
                
                visited_set.add(currentNode);
            }
            
        }
        
        for (CommitInfo commitInfo : not_collapsed_set)
        {
            new_graph.addVertex(commitInfo);
        }
        for (CollapsedCommitInfo collapse : collapses)
        {
            new_graph.addVertex(collapse);
        }
        for (CommitRelationship commitRelationship : edges)
        {
            CommitInfo child = commitRelationship.getChild();
            CommitInfo parent = commitRelationship.getParent();
            if(!not_collapsed_set.contains(child) && not_collapsed_set.contains(parent))
            {
                CollapsedCommitInfo c = node_collapse_dict.get(child);
                new_graph.addEdge(new CommitRelationship(parent, c), c, parent);
            }
            else if(not_collapsed_set.contains(child) && !not_collapsed_set.contains(parent))
            {
                CollapsedCommitInfo p = node_collapse_dict.get(parent);
                new_graph.addEdge(new CommitRelationship(p, child), child, p);
            }
            else if(!not_collapsed_set.contains(child) && !not_collapsed_set.contains(parent))
            {
                CollapsedCommitInfo c = node_collapse_dict.get(child);
                CollapsedCommitInfo p = node_collapse_dict.get(parent);
                if(new_graph.findEdge(c, p) == null)
                    new_graph.addEdge(new CommitRelationship(p, c), c, p);
            }
        }
        for (Object commitRel : graph.getEdges())
        {
            CommitRelationship cr = (CommitRelationship) commitRel;
            if(not_collapsed_set.contains(cr.getChild()) && not_collapsed_set.contains(cr.getParent()))
                new_graph.addEdge(cr, cr.getChild(), cr.getParent());
        }
        
        return new_graph;
    }
}
