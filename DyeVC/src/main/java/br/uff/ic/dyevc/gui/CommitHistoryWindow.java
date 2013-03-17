package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.graph.BasicRepositoryHistoryGraph;
import br.uff.ic.dyevc.gui.icons.ColorIcon;
import br.uff.ic.dyevc.graph.visualization.RepositoryHistoryViewer;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.model.MonitoredRepository;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Displays the commit history for the specified repository
 *
 * @author cristiano
 */
public class CommitHistoryWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 1689885032823010309L;
    private static final Font LEGEND_FONT = new java.awt.Font("Arial", 1, 12);
    MonitoredRepository rep;

    /**
     * Creates new form CommitHistoryWindow
     */
    public CommitHistoryWindow(MonitoredRepository rep) {
        this.rep = rep;
        initComponents();
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
        pnlTitle.setBackground(IConstants.BACKGROUND_COLOR);
        
        JLabel lblTitle = new JLabel();
        lblTitle.setText("Log for repository " + rep.getName());
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setVerticalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        
        pnlTitle.add(lblTitle, BorderLayout.CENTER);
        this.getContentPane().add(pnlTitle, BorderLayout.PAGE_START);
        
        createLegendPanel();

        DirectedSparseMultigraph<CommitInfo, CommitRelationship> graph = BasicRepositoryHistoryGraph.createBasicRepositoryHistoryGraph(rep);
        this.getContentPane().add(RepositoryHistoryViewer.createBasicRepositoryHistoryView(graph), BorderLayout.CENTER);


    }// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="createLegendPanel">
    /**
     * Creates the legend panel.
     */
    private void createLegendPanel() {
        JPanel pnlLegend = new javax.swing.JPanel();
        
        JPanel pnlLegendContents = new javax.swing.JPanel();
        pnlLegendContents.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        pnlLegendContents.setBackground(IConstants.BACKGROUND_COLOR);

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
        lblRegular.setIcon(new ColorIcon(IConstants.COLOR_REGULAR));
        pnlLegendContents.add(lblRegular);

        lblHead.setFont(LEGEND_FONT);
        lblHead.setText("Branch's head");
        lblHead.setIcon(new ColorIcon(IConstants.COLOR_HEAD));
        pnlLegendContents.add(lblHead);

        lblInitial.setFont(LEGEND_FONT);
        lblInitial.setText("Initial commit");
        lblInitial.setIcon(new ColorIcon(IConstants.COLOR_FIRST));
        pnlLegendContents.add(lblInitial);

        lblMerge.setFont(LEGEND_FONT);
        lblMerge.setText("Merge commit");
        lblMerge.setIcon(new ColorIcon(IConstants.COLOR_MERGE));
        pnlLegendContents.add(lblMerge);

        lblSplit.setFont(LEGEND_FONT);
        lblSplit.setText("Split commit");
        lblSplit.setIcon(new ColorIcon(IConstants.COLOR_SPLIT));
        pnlLegendContents.add(lblSplit);

        lblMergeSplit.setFont(LEGEND_FONT);
        lblMergeSplit.setText("Merge and split commit");
        lblMergeSplit.setIcon(new ColorIcon(IConstants.COLOR_MERGE_SPLIT));
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
    

}
