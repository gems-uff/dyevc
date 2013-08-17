package br.uff.ic.dyevc.graph.transform.common;

import java.awt.BasicStroke;
import java.awt.Stroke;
import org.apache.commons.collections15.Transformer;

/**
 * Defines a stroke to the edges of size 2.
 * @author Cristiano
 * @param <E> The edge to transformed
 */
public class EdgeStrokeTransformer<E>
        implements Transformer<E, Stroke> {

    protected Stroke stroke = new BasicStroke(2);

    /**
     * Builds a transformer with a stroke having the specified width
     * @param width The width to be used in the stroke
     */
    public EdgeStrokeTransformer(float width) {
        stroke = new BasicStroke(width);
    }

    /**
     * Builds a transformer with a default stroke of 2
     */
    public EdgeStrokeTransformer() {
        stroke = new BasicStroke(2);
    }

    @Override
    public Stroke transform(E e) {
        return stroke;
    }
}
