package br.uff.ic.dyevc.gui.graph;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.graph.GraphBuilder;
import br.uff.ic.dyevc.graph.transform.common.ClusterVertexShapeTransformer;
import br.uff.ic.dyevc.graph.transform.common.VertexStrokeHighlightTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyEdgePaintTransformer;
import br.uff.ic.dyevc.graph.transform.common.EdgeStrokeTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyVertexPaintTransformer;
import br.uff.ic.dyevc.graph.transform.topology.TopologyVertexTooltipTransformer;
import br.uff.ic.dyevc.gui.core.SplashScreen;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.PullRelationship;
import br.uff.ic.dyevc.model.topology.PushRelationship;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

/**
 * Displays the topology for the specified system
 *
 * @author cristiano
 */
public class TopologyWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 1689885032823010309L;
    private String systemName;
    private String callerId;
    private DirectedSparseMultigraph graph;
    private VisualizationViewer vv;
    private Layout layout;
    private JComboBox mouseModesCombo;
    private JButton plus;
    private JButton minus;
    private JButton btnHelp;
    private JCheckBox chkShowPush;
    private JCheckBox chkShowPull;
    private final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse<CommitInfo, CommitRelationship>();
    private final ScalingControl scaler = new CrossoverScalingControl();
    private DirectionDisplayPredicate<RepositoryInfo, CloneRelationship> showRelationPredicate;

    public TopologyWindow(String systemName) {
        this(systemName, null);
    }

    public TopologyWindow(String systemName, String callerId) {
        SplashScreen splash = SplashScreen.getInstance();
        try {
            splash.setStatus("Initializing Graph component");
            this.systemName = systemName;
            this.callerId = callerId;
            SplashScreen.getInstance().setVisible(true);
            initGraphComponent();
            SplashScreen.getInstance().setStatus("Initializing Window components");
            initComponents();
            SplashScreen.getInstance().setVisible(false);
        } catch (DyeVCException ex) {
            splash.dispose();
            JOptionPane.showMessageDialog(null, "Application received the following exception trying to show topology:\n"
                    + ex + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
            WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            setVisible(false);
            dispose();
        } catch (RuntimeException ex) {
            ex.printStackTrace(System.err);
            splash.dispose();
            JOptionPane.showMessageDialog(null, "Application received the following exception trying to show topology:\n"
                    + ex + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
            WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            setVisible(false);
            dispose();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
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
        chkShowPush.setForeground(Color.RED);
        chkShowPush.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showRelationPredicate.showPushRelations(chkShowPush.isSelected());
                vv.repaint();
            }
        });

        chkShowPull = new JCheckBox("Pull");
        chkShowPull.setSelected(true);
        chkShowPull.setForeground(Color.GREEN);
        chkShowPull.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showRelationPredicate.showPullRelations(chkShowPull.isSelected());
                vv.repaint();
            }
        });

        Container content = getContentPane();
        GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
        content.add(gzsp);

        JPanel controls = new JPanel();
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
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="initGraphComponent">
    private void initGraphComponent() throws DyeVCException {
        // create the commit history graph with all commits from repository
        graph = GraphBuilder.createTopologyGraph(systemName);

        // Choosing layout
        layout = new FRLayout<RepositoryInfo, CloneRelationship>(graph);
        Dimension preferredSize = new Dimension(800, 600);

        vv = new VisualizationViewer(layout, preferredSize);

        //Scales the graph to show more nodes
        scaler.scale(vv, 0.9F, vv.getCenter());
        vv.scaleToLayout(scaler);

        vv.setBackground(IConstants.BACKGROUND_COLOR);

        // Adds interaction via mouse and defaults mode to Transforming
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

        // <editor-fold defaultstate="collapsed" desc="vertex transformers">
        //VertexShape
        vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeTransformer());

        //VertexToolTip
        vv.setVertexToolTipTransformer(new TopologyVertexTooltipTransformer());
        ToolTipManager.sharedInstance().setDismissDelay(15000);

        //VertexFillPaint
        vv.getRenderContext().setVertexFillPaintTransformer(new TopologyVertexPaintTransformer(vv.getPickedVertexState(), callerId));

        //VertexLabel
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<RepositoryInfo, String>() {
            @Override
            public String transform(RepositoryInfo c) {
                return c.getHostName() + " - " + c.getCloneName();
            }
        });
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

        //VertexStroke
        vv.getRenderContext().setVertexStrokeTransformer(new VertexStrokeHighlightTransformer<RepositoryInfo, CloneRelationship>(graph, vv.getPickedVertexState()));
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="edge transformers">
        //EdgeLabel - not defined
        //EdgeDrawPaint
        vv.getRenderContext().setEdgeDrawPaintTransformer(new TopologyEdgePaintTransformer(vv.getPickedEdgeState()));

        vv.getRenderContext().setEdgeStrokeTransformer(new EdgeStrokeTransformer<CloneRelationship>());

        //EdgeShape - sets it as quadcurve and defines a greater offset between parallel edges
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve());
        ((AbstractEdgeShapeTransformer) vv.getRenderContext().getEdgeShapeTransformer()).setControlOffsetIncrement(30);
        //</editor-fold>

        showRelationPredicate = new DirectionDisplayPredicate<RepositoryInfo, CloneRelationship>(true, true);
        vv.getRenderContext().setEdgeIncludePredicate(showRelationPredicate);
    }
    //</editor-fold>

    private void resetGraph() {
        layout.setGraph(graph);
        layout.initialize();
        vv.repaint();
    }

    private void resetComponents() {
        graph = null;
        vv = null;
        layout = null;
    }

    /**
     * runs the graph with a default system name
     */
    public static void main(String[] args) {
        try {
            String sysName = "labgc-2012.2";

            TopologyDAO dao = new TopologyDAO();
            dao.readTopologyForSystem(sysName);

            new TopologyWindow(sysName).setVisible(true);
        } catch (DyeVCException ex) {
            Logger.getLogger(TopologyWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final static class DirectionDisplayPredicate<V, E>
            implements Predicate<Context<Graph<V, E>, E>> {

        protected boolean showPush;
        protected boolean showPull;

        public DirectionDisplayPredicate(boolean showPush, boolean showPull) {
            this.showPush = showPush;
            this.showPull = showPull;
        }

        public void showPushRelations(boolean b) {
            showPush = b;
        }

        public void showPullRelations(boolean b) {
            showPull = b;
        }

        @Override
        public boolean evaluate(Context<Graph<V, E>, E> context) {
            Graph<V, E> graph = context.graph;
            E e = context.element;
            if (e instanceof PushRelationship && showPush) {
                return true;
            }
            if (e instanceof PullRelationship && showPull) {
                return true;
            }
            return false;
        }
    }
    String instructions =
            "<html><p>Each vertex in the graph represents a known clone of this system "
            + "in the topology.</p>"
            + "<p>Each vertex label shows the hostname and the "
            + "clone name of the vertex, separated by a dash.</p>"
            + "<p>Each vertex color has a different meaning: </p>"
            + "<ul>"
            + "<li>Green vertex: the vertex that represents your clone</li>"
            + "<li>Red vertices: ordinary clones</li>"
            + "<li>Blue vertices: central repositories (do not pull or push "
            + "to any other clone</li>"
            + "<li>Yellow vertices: picked vertices</li>"
            + "</ul>"
            + "<p>Each edge in the graph represents a relationship between two "
            + "repositories:</p>"
            + "<ul>"
            + "<li>Red: the source vertex pushes to the destination vertex. </li>"
            + "<li>Green, the destination vertex pulls from the source vertex. </li>"
            + "<li>Yellow, if edge is picked. </li>"
            + "</ul>"
            + "<p>Place the mouse over a vertex to view detailed information "
            + "regarding it.</p>"
            + "</html>";
}
