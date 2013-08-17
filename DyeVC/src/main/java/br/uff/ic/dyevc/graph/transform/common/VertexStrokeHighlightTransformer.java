package br.uff.ic.dyevc.graph.transform.common;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import java.awt.BasicStroke;
import java.awt.Stroke;
import org.apache.commons.collections15.Transformer;

/**
 * Defines a stroke for the vertex.
 * <ul>
 *      <li>If the vertex is picked, it gets a heavy stroke.</li>
 *      <li>If the vertex is connected to a picked vertex, it gets a medium stroke.</li>
 *      <li>If the vertex is not picked nor connected to a picked vertex, it gets a light stroke.</li>
 * </ul>
 *
 * @author Cristiano
 */
public class VertexStrokeHighlightTransformer<V, E> implements
        Transformer<V, Stroke> {

    private float[] dotting_heavy = {5.0f, 10.0f};
    private float[] dotting_medium = {3.0f, 6.0f};

    protected boolean highlight = false;

    protected Stroke heavy = new BasicStroke(5.0f,
            BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,5.0f, dotting_heavy, 0f);
    protected Stroke medium = new BasicStroke(3.0f,
            BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 3.0f, dotting_medium, 0f);
    protected Stroke light = new BasicStroke(1);
    protected PickedInfo<V> pi;
    protected Graph<V, E> graph;

    public VertexStrokeHighlightTransformer(Graph<V, E> graph, PickedInfo<V> pi) {
        this.graph = graph;
        this.pi = pi;
        this.highlight = true;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    @Override
    public Stroke transform(V v) {
        if (highlight) {
            if (pi.isPicked(v)) {
                return heavy;
            } else {
                for (V w : graph.getNeighbors(v)) {
                    if (pi.isPicked(w)) {
                        return medium;
                    }
                }
                return light;
            }
        } else {
            return light;
        }
    }
}