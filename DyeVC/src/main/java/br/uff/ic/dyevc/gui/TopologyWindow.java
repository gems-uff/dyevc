package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.graph.GraphBuilder;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
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

import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import org.apache.commons.collections15.Transformer;

/**
 * Displays the topology for the specified system
 *
 * @author cristiano
 */
public class TopologyWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 1689885032823010309L;
    private String systemName;
    private DirectedSparseMultigraph graph;
    private VisualizationViewer vv;
    private Layout layout;
    private JComboBox edgeLineShapeCombo;
    private JComboBox mouseModesCombo;
    private JButton plus;
    private JButton minus;
    private final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse<CommitInfo, CommitRelationship>();
    private final ScalingControl scaler = new CrossoverScalingControl();

    public TopologyWindow(String systemName) {
        SplashScreen splash = SplashScreen.getInstance();
        try {
            splash.setStatus("Initializing Graph component");
            this.systemName = systemName;
            SplashScreen.getInstance().setVisible(true);
            initGraphComponent();
            SplashScreen.getInstance().setStatus("Initializing Window components");
            initComponents();
            SplashScreen.getInstance().setVisible(false);
        } catch (DyeVCException ex) {
            splash.dispose();
            JOptionPane.showMessageDialog(null, "Application received the following exception trying to show topology:\n" +
                    ex + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
            this.dispose();
        } catch(RuntimeException ex) {
            ex.printStackTrace(System.err);
            splash.dispose();
            JOptionPane.showMessageDialog(null, "Application received the following exception trying to show topology:\n" +
                    ex + "\n\nOpen console window to see error details.", "Error found!", JOptionPane.ERROR_MESSAGE);
            this.dispose();
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
        setTitle("Topology for system " + systemName);
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

        edgeLineShapeCombo = new JComboBox();
        this.edgeLineShapeCombo.setModel(new DefaultComboBoxModel(new String[]{"QuadCurve", "Line", "CubicCurve"}));
        this.edgeLineShapeCombo.setSelectedItem("Line");
        this.edgeLineShapeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                edgeLineShapeSelectionActionPerformed(evt);
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
        controls.add(mouseModesCombo);
        controls.add(edgeLineShapeCombo);
        content.add(controls, BorderLayout.SOUTH);
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="initGraphComponent">
    private void initGraphComponent() throws DyeVCException {
        // create the commit history graph with all commits from repository
        graph = GraphBuilder.createTopologyGraph(systemName);

        // Choosing layout
        layout = new FRLayout<RepositoryInfo, CloneRelationship>(graph);
        Dimension preferredSize = new Dimension(580, 580);

        final VisualizationModel visualizationModel =
                new DefaultVisualizationModel(layout, preferredSize);
        vv = new VisualizationViewer(visualizationModel, preferredSize);

        //Scales the graph to show more nodes
        scaler.scale(vv, 0.4761905F, vv.getCenter());
        vv.scaleToLayout(scaler);

        vv.setBackground(IConstants.BACKGROUND_COLOR);

        // Adds interaction via mouse
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());

        // <editor-fold defaultstate="collapsed" desc="vertex tooltip transformer">
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex fillPaint transformer">
//        Transformer<Object, Paint> vertexPaint = new CHVertexPaintTransformer();
//        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex label transformer">
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<RepositoryInfo,String>(){
            public String transform(RepositoryInfo c) {
                return c.getCloneName();
            }
        });
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="edge label transformer">
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        //</editor-fold>

        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
//        vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeTransformer());
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="action handlers">
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
            String sysName = "dyevc";

            TopologyDAO dao = new TopologyDAO();
            dao.readTopology();

            new TopologyWindow(sysName).setVisible(true);
        } catch (DyeVCException ex) {
            Logger.getLogger(TopologyWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
