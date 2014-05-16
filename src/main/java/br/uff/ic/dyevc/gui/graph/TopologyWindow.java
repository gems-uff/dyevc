package br.uff.ic.dyevc.gui.graph;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.graph.GraphBuilder;
import br.uff.ic.dyevc.graph.transform.common.VertexStrokeHighlightTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyEdgeLabelTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyEdgePaintTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyEdgeStrokeTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyPickWithIconListener;
import br.uff.ic.dyevc.graph.transform.topology.TopologyVertexIconShapeTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyVertexIconTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyVertexPaintTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyVertexTooltipTransformer;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.PullRelationship;
import br.uff.ic.dyevc.model.topology.PushRelationship;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.utils.PreferencesManager;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
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
import java.awt.GridLayout;
import java.awt.Toolkit;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/**
 * Displays the topology for the specified system
 *
 * @author cristiano
 */
public class TopologyWindow extends javax.swing.JFrame {
    private static final long            serialVersionUID = 1689885032823010309L;
    private ApplicationSettingsBean      settings;
    private final DefaultModalGraphMouse graphMouse   = new DefaultModalGraphMouse<CommitInfo, CommitRelationship>();
    private final ScalingControl         scaler       = new CrossoverScalingControl();
    private String                       instructions =
        "<html><p>Each vertex in the graph represents a known clone of this system in the topology.</p>"
        + "<p>Each vertex label shows the hostname and the clone name of the vertex, separated by a dash.</p>"
        + "<p>Each vertex type/color has a different meaning: </p>" + "<ul>"
        + "<li>Blue computer: the vertex that represents your clone;</li>"
        + "<li>Black computers: ordinary clones;</li>" + "<li>Server: central repositories (do not pull or push "
        + "to any other clone) or clones where DyeVC is not running;</li>"
        + "<li>Vertices with a green checkmark: picked vertices.</li>" + "</ul>"
        + "<p>Each edge in the graph represents a relationship between two repositories. Different"
        + "strokes  represent different relationships:</p>" + "<ul>"
        + "<li>Continuous edges: the source clone pushes to the destination vertex; </li>"
        + "<li>Dotted edges: the destination clone pulls from the source vertex. </li>" + "</ul>"
        + "<p>Edge colors depict the synchronism between two clones:</p>" + "<ul>"
        + "<li>Yellow edges represent a picked edge;</li>"
        + "<li>Green edges represent that source clone is synchronized with destination clone;</li>"
        + "<li>Red edges represent that source clone is not synchronized with destination clone.</li>" + "</ul>"
        + "<p>Each edge label tells how many commits from the source clone are missing in "
        + "the destination clone.</p>" + "<br><p>Place the mouse over a vertex to view detailed information "
        + "regarding it.</p>" + "</html>";
    private String                                                       systemName;
    private String                                                       callerId;
    private DirectedSparseMultigraph                                     graph;
    private VisualizationViewer                                          vv;
    private Layout                                                       layout;
    private JComboBox                                                    mouseModesCombo;
    private JButton                                                      plus;
    private JButton                                                      minus;
    private JButton                                                      btnHelp;
    private JCheckBox                                                    chkShowPush;
    private JCheckBox                                                    chkShowPull;
    private DirectionDisplayPredicate<RepositoryInfo, CloneRelationship> showRelationPredicate;

    /**
     * Creates a topology window without specifying the caller Id
     *
     * @param systemName The system that will have the topology plotted
     */
    public TopologyWindow(String systemName) {
        this(systemName, null);
    }

    /**
     * Creates a topology window specifying the caller Id
     *
     *
     * @param systemName The system that will have the topology plotted
     * @param callerId The caller Id (node from which the plotting was asked
     */
    public TopologyWindow(String systemName, String callerId) {
//      SplashScreen splash = SplashScreen.getInstance();
        try {
//          splash.setStatus("Initializing Graph component");
            this.systemName = systemName;
            this.callerId   = callerId;
            settings        = PreferencesManager.getInstance().loadPreferences();
//          SplashScreen.getInstance().setVisible(true);
            StopWatch watch = new StopWatch();
            if (settings.isPerformanceMode()) {
                watch.start();
            }

            initGraphComponent();

            if (settings.isPerformanceMode()) {
                watch.stop();
                LoggerFactory.getLogger(TopologyWindow.class).info("Time taken to process topology graph for system <"
                                        + systemName + ">: " + watch.toString());

            }

//          SplashScreen.getInstance().setStatus("Initializing Window components");
            if (settings.isPerformanceMode()) {
                watch.reset();
                watch.start();
            }

            initComponents();
            LoggerFactory.getLogger(TopologyWindow.class).info("Time taken to plot topology graph for system <"
                                    + systemName + ">: " + watch.toString());
//          SplashScreen.getInstance().setVisible(false);
        } catch (DyeVCException ex) {
//          splash.dispose();
            JOptionPane.showMessageDialog(null,
                                          "Application received the following exception trying to show topology:\n"
                                          + ex + "\n\nOpen console window to see error details.", "Error found!",
                                              JOptionPane.ERROR_MESSAGE);
            WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            setVisible(false);
            dispose();
        } catch (RuntimeException ex) {
            ex.printStackTrace(System.err);
//          splash.dispose();
            JOptionPane.showMessageDialog(null,
                                          "Application received the following exception trying to show topology:\n"
                                          + ex + "\n\nOpen console window to see error details.", "Error found!",
                                              JOptionPane.ERROR_MESSAGE);
            WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            setVisible(false);
            dispose();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">

    /**
     * Method description
     *
     */
    private void initComponents() {
        if (callerId == null) {
            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        } else {
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                resetComponents();
            }
        });
        setAutoRequestFocus(true);
        setTitle("Topology for system " + systemName);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 1024) / 2, (screenSize.height - 768) / 2, 1024, 768);
        mouseModesCombo = graphMouse.getModeComboBox();
        mouseModesCombo.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
                vv.repaint();
            }
        });
        minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
                vv.repaint();
            }
        });
        btnHelp = new JButton("Help");
        btnHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, instructions, "Help", JOptionPane.PLAIN_MESSAGE);
            }
        });
        chkShowPush = new JCheckBox("Push");
        chkShowPush.setSelected(true);
        chkShowPush.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showRelationPredicate.showPushRelations(chkShowPush.isSelected());
                vv.repaint();
            }
        });
        chkShowPull = new JCheckBox("Pull");
        chkShowPull.setSelected(true);
        chkShowPull.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showRelationPredicate.showPullRelations(chkShowPull.isSelected());
                vv.repaint();
            }
        });
        Container           content = getContentPane();
        GraphZoomScrollPane gzsp    = new GraphZoomScrollPane(vv);
        content.add(gzsp);
        JPanel controls     = new JPanel();
        JPanel zoomControls = new JPanel(new GridLayout(2, 1));
        zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomControls.add(plus);
        zoomControls.add(minus);
        controls.add(zoomControls);
        JPanel pnlMouseMoude = new JPanel(new GridLayout(1, 1));
        pnlMouseMoude.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        pnlMouseMoude.add(mouseModesCombo);
        controls.add(pnlMouseMoude);
        JPanel pnlShowEdge = new JPanel(new GridLayout(1, 3));
        pnlShowEdge.setBorder(BorderFactory.createTitledBorder("Filter relations"));
        pnlShowEdge.add(chkShowPush);
        pnlShowEdge.add(chkShowPull);
        controls.add(pnlShowEdge);
        controls.add(btnHelp);
        content.add(controls, BorderLayout.SOUTH);
    }    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="initGraphComponent">

    /**
     * Method description
     *
     *
     * @throws DyeVCException
     */
    private void initGraphComponent() throws DyeVCException {

        // create the commit history graph with all commits from repository
        graph = GraphBuilder.createTopologyGraph(systemName);

        // Choosing layout
        layout = new FRLayout<RepositoryInfo, CloneRelationship>(graph);
        Dimension preferredSize = new Dimension(800, 600);
        vv = new VisualizationViewer(layout, preferredSize);

        // Scales the graph to show more nodes
        scaler.scale(vv, 0.9F, vv.getCenter());
        vv.scaleToLayout(scaler);
        vv.setBackground(IConstants.BACKGROUND_COLOR);

        // Adds interaction via mouse and defaults mode to Transforming
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        PickedState<RepositoryInfo> ps = vv.getPickedVertexState();

        // <editor-fold defaultstate="collapsed" desc="vertex transformers">
        // VertexToolTip
        vv.setVertexToolTipTransformer(new TopologyVertexTooltipTransformer());
        ToolTipManager.sharedInstance().setDismissDelay(15000);

        // VertexFillPaint
        vv.getRenderContext().setVertexFillPaintTransformer(new TopologyVertexPaintTransformer(ps, callerId));

        // VertexLabel
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<RepositoryInfo, String>() {
            @Override
            public String transform(RepositoryInfo c) {
                return c.getCloneName() + " [" + c.getId() + "]";
            }
        });
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

        // VertexStroke
        vv.getRenderContext().setVertexStrokeTransformer(new VertexStrokeHighlightTransformer<RepositoryInfo,
                CloneRelationship>(graph, vv.getPickedVertexState()));

        // VertexShape
//      vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeTransformer());
        vv.getRenderContext().setVertexShapeTransformer(new TopologyVertexIconShapeTransformer(callerId));

        // VertexIcon
        TopologyVertexIconTransformer vertexIconTransformer = new TopologyVertexIconTransformer(callerId);
        vv.getRenderContext().setVertexIconTransformer(vertexIconTransformer);

        // Adds a listener to decorate the vertex with a checkmark icon when its picked
        ps.addItemListener(new TopologyPickWithIconListener(vertexIconTransformer));

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="edge transformers">
        // EdgeLabel
        Transformer<Object, String> edgeLabel = new TopologyEdgeLabelTransformer();
        vv.getRenderContext().setEdgeLabelTransformer(edgeLabel);
        EdgeLabelRenderer edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer();
        edgeLabelRenderer.setRotateEdgeLabels(true);

        // EdgeDrawPaint
        TopologyEdgePaintTransformer ept = new TopologyEdgePaintTransformer(vv.getPickedEdgeState());
        vv.getRenderContext().setEdgeDrawPaintTransformer(ept);

        // Arrows
        vv.getRenderContext().setArrowFillPaintTransformer(ept);
        vv.getRenderContext().setArrowDrawPaintTransformer(ept);

        // EdgeStroke
        vv.getRenderContext().setEdgeStrokeTransformer(new TopologyEdgeStrokeTransformer<CloneRelationship>());

        // EdgeShape - sets it as quadcurve and defines a greater offset between parallel edges
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve());
        ((AbstractEdgeShapeTransformer)vv.getRenderContext().getEdgeShapeTransformer()).setControlOffsetIncrement(30);

        // </editor-fold>
        showRelationPredicate = new DirectionDisplayPredicate<RepositoryInfo, CloneRelationship>(true, true);
        vv.getRenderContext().setEdgeIncludePredicate(showRelationPredicate);
    }

    // </editor-fold>

    /**
     * Method description
     *
     */
    private void resetGraph() {
        layout.setGraph(graph);
        layout.initialize();
        vv.repaint();
    }

    /**
     * Method description
     *
     */
    private void resetComponents() {
        graph  = null;
        vv     = null;
        layout = null;
    }

    /**
     * runs the graph with a default system name
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            String      sysName = "dyevc";
            TopologyDAO dao     = new TopologyDAO();
            dao.readTopologyForSystem(sysName);
            new TopologyWindow(sysName).setVisible(true);
        } catch (DyeVCException ex) {
            Logger.getLogger(TopologyWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Predicate to filter push and / or pull edges
     *
     * @param <V> The type of vertices
     * @param <E> The type of edges
     *
     * @author Cristiano Cesario
     */
    private final static class DirectionDisplayPredicate<V, E> implements Predicate<Context<Graph<V, E>, E>> {
        /**
         * If true, than show push edges
         */
        protected boolean showPush;

        /**
         * If true, than show pull edges
         */
        protected boolean showPull;

        /**
         * Builds the predicate with initial values specified for <code>showPush</code> and <code>showPull</code>
         *
         * @param showPush The initial value for showPush
         * @param showPull Theh initial value for showPull
         */
        public DirectionDisplayPredicate(boolean showPush, boolean showPull) {
            this.showPush = showPush;
            this.showPull = showPull;
        }

        /**
         * Specifies whether push relations should be shown or not
         *
         * @param b If true, than show push relations
         */
        public void showPushRelations(boolean b) {
            showPush = b;
        }

        /**
         * Specifies whether pull relations should be shown or not
         *
         * @param b If true, than show pull relations
         */
        public void showPullRelations(boolean b) {
            showPull = b;
        }

        /**
         * Evaluate the predicate for each edge, showing the edge or not, according to its type
         *
         * @param context The context where the predicate will be evaluated
         *
         * @return True, if the edge is to be shown and false otherwise
         */
        @Override
        public boolean evaluate(Context<Graph<V, E>, E> context) {
            Graph<V, E> graph = context.graph;
            E           e     = context.element;
            if ((e instanceof PushRelationship) && showPush) {
                return true;
            }

            if ((e instanceof PullRelationship) && showPull) {
                return true;
            }

            return false;
        }
    }
}
