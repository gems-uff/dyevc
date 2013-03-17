package br.uff.ic.dyevc.graph.visualization;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.graph.transform.CommitHistoryTooltipTransformer;
import br.uff.ic.dyevc.graph.transform.CommitHistoryVertexPaintTransformer;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.CommitRelationship;
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
public class RepositoryHistoryViewer {

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
        Layout<CommitInfo, CommitRelationship> layout = new ISOMLayout<CommitInfo, CommitRelationship>(graph);
//        Layout<CommitInfo, CommitRelationship> layout = new MyLayout<CommitInfo, CommitRelationship>(graph);
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
         */
        Transformer<CommitInfo, Paint> vertexPaint = new CommitHistoryVertexPaintTransformer();
        view.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        view.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<CommitInfo>());
        view.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="vertex tooltip transformer">
        Transformer<CommitInfo, String> vertexTooltip = new CommitHistoryTooltipTransformer();
        view.setVertexToolTipTransformer(vertexTooltip);
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        //</editor-fold>

        //Adding edge labels and colors
        //view.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<CommitRelationship>());
        
        view.setBackground(IConstants.BACKGROUND_COLOR);
        
        return view;
    }    
}
