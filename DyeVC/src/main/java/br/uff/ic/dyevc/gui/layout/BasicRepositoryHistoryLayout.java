package br.uff.ic.dyevc.gui.layout;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
import br.uff.ic.dyevc.utils.DateUtil;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.Dimension;
import java.awt.Paint;
import javax.swing.ToolTipManager;
import org.apache.commons.collections15.Transformer;

/**
 * Basic layout for representing a DAG, with different coloring to represent
 * special commits
 *
 * @author Cristiano
 */
public class BasicRepositoryHistoryLayout {

    /**
     * Returns a view that shows the initial and final commits with different
     * coloring. Commits where branchs are created or merged also are colored to 
     * represent this actions. A tooltip is configured for each node, that shows 
     * info about the commit, such as its id, commiter, date/time and message.
     *
     * @param graph
     * @return
     */
    public static VisualizationViewer<CommitInfo, CommitRelationship> createBasicRepositoryHistoryView(DirectedSparseMultigraph<CommitInfo, CommitRelationship> graph) {
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
                Paint paint = IConstants.COLOR_REGULAR;
                int children = ci.getChildrenCount();
                int parents = ci.getParentsCount();
                if (children > 1) {
                    if (parents > 1) {
                        paint = IConstants.COLOR_MERGE_SPLIT;
                    } else {
                        paint = IConstants.COLOR_SPLIT;
                    }
                } else {
                    if (parents > 1) {
                        paint = IConstants.COLOR_MERGE;
                    } else {
                        paint = IConstants.COLOR_REGULAR;
                    }
                }
                if (parents == 0) {
                    paint = IConstants.COLOR_FIRST;
                }
                if (children == 0) {
                    paint = IConstants.COLOR_HEAD;
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
                details.append("<b>message: </b>")
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
        
        view.setBackground(IConstants.BACKGROUND_COLOR);
        
        return view;
    }    
}
