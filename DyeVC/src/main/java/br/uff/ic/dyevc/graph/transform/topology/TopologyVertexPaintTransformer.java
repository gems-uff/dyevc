package br.uff.ic.dyevc.graph.transform.topology;

import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import java.awt.Color;
import java.awt.Paint;

/**
 * Transformer to paint vertices in a topology graph
 *
 * @author Cristiano
 */
public class TopologyVertexPaintTransformer extends PickableVertexPaintTransformer<RepositoryInfo> {

    private String callerId;

    /**
     * Builds a transformer to paint topology vertices
     *
     * @param pi The info about whether the vertex is picked or not
     * @param callerId The id of the repository to be shown as the focus
     */
    public TopologyVertexPaintTransformer(PickedInfo<RepositoryInfo> pi, String callerId) {
        super(pi, Color.RED, Color.YELLOW);
        this.callerId = callerId != null ? callerId : "";
    }

    /**
     * Paints vertex. Default is red. Otherwise:
     * <ul>
     *      <li>Green, if vertex corresponds to the clone where user chose to view topology. </li>
     *      <li>Blue, if vertex is a central repository (does not push to or pull from anyone. </li>
     *      <li>Yellow, if vertex is picked. </li>
     * </ul>
     */
    @Override
    public Paint transform(RepositoryInfo info) {
        Paint paint = super.transform(info);
        if (!pi.isPicked(info)) {
            if (info.getId().equals(callerId)) {
                paint = Color.GREEN;
            }
            if (info.getPullsFrom().isEmpty()
                    && info.getPushesTo().isEmpty()) {
                paint = Color.BLUE;
            }
        }
        return paint;
    }
}
