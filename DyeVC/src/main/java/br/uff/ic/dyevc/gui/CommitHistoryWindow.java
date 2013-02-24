package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.exception.VCSException;
import br.uff.ic.dyevc.gui.icons.ColorIcon;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.tools.vcs.git.GitCommitHistory;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.utils.DateUtil;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import org.apache.commons.collections15.Transformer;

/**
 * Displays the commit history for the specified repository
 *
 * @author cristiano
 */
public class CommitHistoryWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 1689885032823010309L;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color COLOR_REGULAR = Color.CYAN;
    private static final Color COLOR_MERGE_SPLIT = Color.YELLOW;
    private static final Color COLOR_HEAD = Color.GRAY;
    private static final Color COLOR_FIRST = Color.BLACK;
    private static final Color COLOR_MERGE = Color.GREEN;
    private static final Color COLOR_SPLIT = Color.RED;
    private static final Font LEGEND_FONT = new java.awt.Font("Arial", 1, 12);
    MonitoredRepository rep;

    /**
     * Creates new form CommitHistoryWindow
     */
    public CommitHistoryWindow(MonitoredRepository rep) {
        this.rep = rep;
        initComponents();
        DirectedSparseMultigraph<CommitInfo, CommitRelationship> graph = createGraph(rep);
        initGraphComponent(graph);
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Commit History");
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 700) / 2, (screenSize.height - 750) / 2, 700, 750);

        JPanel pnlTitle = new javax.swing.JPanel();
        pnlTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        pnlTitle.setLayout(new BorderLayout(getWidth(), 30));
        pnlTitle.setBackground(BACKGROUND_COLOR);
        
        JLabel lblTitle = new JLabel();
        lblTitle.setText("Log for repository " + rep.getName());
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setVerticalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        
        pnlTitle.add(lblTitle, BorderLayout.CENTER);
        this.getContentPane().add(pnlTitle, BorderLayout.PAGE_START);
        
        createLegendPanel();

    }// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="createLegendPanel">
    /**
     * Creates the legend panel.
     */
    private void createLegendPanel() {
        JPanel pnlLegend = new javax.swing.JPanel();
        
        JPanel pnlLegendContents = new javax.swing.JPanel();
        pnlLegendContents.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        pnlLegendContents.setBackground(BACKGROUND_COLOR);

        JLabel lblLegend = new javax.swing.JLabel();
        JLabel lblRegular = new javax.swing.JLabel();
        JLabel lblHead = new javax.swing.JLabel();
        JLabel lblInitial = new javax.swing.JLabel();
        JLabel lblBlank = new javax.swing.JLabel();
        JLabel lblMerge = new javax.swing.JLabel();
        JLabel lblSplit = new javax.swing.JLabel();
        JLabel lblMergeSplit = new javax.swing.JLabel();

        GridLayout grid = new java.awt.GridLayout(4, 2);
        grid.setHgap(3);
        pnlLegendContents.setLayout(grid);

        lblLegend.setFont(LEGEND_FONT);
        lblLegend.setText("Legend:");
        pnlLegendContents.add(lblLegend);

        lblBlank.setFont(LEGEND_FONT);
        lblBlank.setText("");
        pnlLegendContents.add(lblBlank);

        lblRegular.setFont(LEGEND_FONT);
        lblRegular.setText("Regular commit");
        lblRegular.setIcon(new ColorIcon(COLOR_REGULAR));
        pnlLegendContents.add(lblRegular);

        lblHead.setFont(LEGEND_FONT);
        lblHead.setText("Branch's head");
        lblHead.setIcon(new ColorIcon(COLOR_HEAD));
        pnlLegendContents.add(lblHead);

        lblInitial.setFont(LEGEND_FONT);
        lblInitial.setText("Initial commit");
        lblInitial.setIcon(new ColorIcon(COLOR_FIRST));
        pnlLegendContents.add(lblInitial);

        lblMerge.setFont(LEGEND_FONT);
        lblMerge.setText("Merge commit");
        lblMerge.setIcon(new ColorIcon(COLOR_MERGE));
        pnlLegendContents.add(lblMerge);

        lblSplit.setFont(LEGEND_FONT);
        lblSplit.setText("Split commit");
        lblSplit.setIcon(new ColorIcon(COLOR_SPLIT));
        pnlLegendContents.add(lblSplit);

        lblMergeSplit.setFont(LEGEND_FONT);
        lblMergeSplit.setText("Merge and split commit");
        lblMergeSplit.setIcon(new ColorIcon(COLOR_MERGE_SPLIT));
        pnlLegendContents.add(lblMergeSplit);
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(pnlLegend);
        pnlLegend.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlLegendContents, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlLegendContents, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
        );
        
        this.getContentPane().add(pnlLegend, BorderLayout.PAGE_END);
    }//</editor-fold>
    

    private void initGraphComponent(DirectedSparseMultigraph<CommitInfo, CommitRelationship> graph) {
        // Choosing layout
//        Layout<CommitInfo, CommitRelationship> layout = new CircleLayout<CommitInfo, CommitRelationship> (graph);
//        Layout<CommitInfo, CommitRelationship> layout = new FRLayout2<CommitInfo, CommitRelationship> (graph);
        Layout<CommitInfo, CommitRelationship> layout = new ISOMLayout<CommitInfo, CommitRelationship>(graph);
//        Layout<CommitInfo, CommitRelationship> layout = new SpringLayout<CommitInfo, CommitRelationship> (graph);
//        Layout<CommitInfo, CommitRelationship> layout = new KKLayout<CommitInfo, CommitRelationship> (graph);
//        Layout<CommitInfo, CommitRelationship> layout = new DAGLayout<CommitInfo, CommitRelationship> (graph);
        layout.setSize(new Dimension(580, 580));

        VisualizationViewer<CommitInfo, CommitRelationship> view = new VisualizationViewer<CommitInfo, CommitRelationship>(layout);

        // Adding interation via mouse
        DefaultModalGraphMouse mouse = new DefaultModalGraphMouse();
        mouse.setMode(ModalGraphMouse.Mode.PICKING);
        view.setGraphMouse(mouse);
        view.addKeyListener(mouse.getModeKeyListener());

        // <editor-fold defaultstate="collapsed" desc="labels and colors transformers">
        //Adding vertex labels and colors
        /* Paints vertex. 
         *      Default is cyan.
         *      If vertex splits in various children, paints it in red.
         *      If vertex is a merge, paints it in green.
         *      If vertex is both a merge and another split, paints it in yellow.
         */
        Transformer<CommitInfo, Paint> vertexPaint = new Transformer<CommitInfo, Paint>() {
            @Override
            public Paint transform(CommitInfo ci) {
                Paint paint = COLOR_REGULAR;
                int children = ci.getChildrenCount();
                int parents = ci.getParentsCount();
                if (children > 1) {
                    if (parents > 1) {
                        paint = COLOR_MERGE_SPLIT;
                    } else {
                        paint = COLOR_SPLIT;
                    }
                } else {
                    if (parents > 1) {
                        paint = COLOR_MERGE;
                    } else {
                        paint = COLOR_REGULAR;
                    }
                }
                if (parents == 0) {
                    paint = COLOR_FIRST;
                }
                if (children == 0) {
                    paint = COLOR_HEAD;
                }
                return paint;
            }
        };
        view.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        view.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<CommitInfo>());
        view.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex tooltip transformer">
        Transformer<CommitInfo, String> vertexTooltip = new Transformer<CommitInfo, String>() {
            @Override
            public String transform(CommitInfo ci) {
                StringBuilder details = new StringBuilder("<html>");
                if (ci.getParentsCount() == 0) {
                    details.append("<b>This is the first commit!</b>").append("<br><br>");
                }
                if (ci.getChildrenCount() == 0) {
                    details.append("<b>This is a branch's head!</b>").append("<br><br>");
                }
                details.append("<b>Commit id: </b>").append(ci.getId()).append("<br>");
                details.append("<b>time: </b>")
                        .append(DateUtil.format(ci.getCommitDate(), "yyyy-MM-dd HH:mm:ss.SSS"))
                        .append("<br>");
                details.append("<b>commiter: </b>")
                        .append(ci.getCommitter())
                        .append("<br>");
                details.append("<b>commiter: </b>")
                        .append(ci.getShortMessage())
                        .append("</html>");
                return details.toString();
            }
        };
        view.setVertexToolTipTransformer(vertexTooltip);
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        //</editor-fold>

        //Adding edge labels and colors
        //view.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<CommitRelationship>());
        
        view.setBackground(BACKGROUND_COLOR);

        this.getContentPane().add(view, BorderLayout.CENTER);
    }

    /**
     * Creates a dag representing the commit history for the specified repository
     * @param rep the repository for which the graph will be created
     * @return a graph representing the repository
     */
    private DirectedSparseMultigraph<CommitInfo, CommitRelationship> createGraph(MonitoredRepository rep) {
        final DirectedSparseMultigraph<CommitInfo, CommitRelationship> graph = new DirectedSparseMultigraph<CommitInfo, CommitRelationship>();
        GitConnector git;
        try {
            git = new GitConnector(rep.getCloneAddress(), rep.getName());
            GitCommitHistory ch = GitCommitHistory.getInstance(git);
            for (CommitInfo commitInfo : ch.getCommitInfos()) {
                graph.addVertex(commitInfo);
            }
            for (CommitRelationship commitRelationship : ch.getCommitRelationships()) {
                graph.addEdge(commitRelationship, commitRelationship.getChild(), commitRelationship.getParent());
            }
            return graph;
        } catch (VCSException ex) {
            Logger.getLogger(CommitHistoryWindow.class.getName()).log(Level.SEVERE, null, ex);
            MessageManager.getInstance().addMessage("Error during graph creation: " + ex);
        }
        
        return graph;
    }
}
