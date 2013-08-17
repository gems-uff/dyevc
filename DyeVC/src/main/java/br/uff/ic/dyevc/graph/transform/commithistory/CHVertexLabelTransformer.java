package br.uff.ic.dyevc.graph.transform.commithistory;

import br.uff.ic.dyevc.model.CommitInfo;
import org.apache.commons.collections15.Transformer;

/**
 * Transforms a CommitInfo in a string to be used as the label of a vertex.
 *
 * @author Cristiano
 */
public class CHVertexLabelTransformer implements Transformer<Object, String> {

    /**
     * Returns a string to be used as the label
     *
     * @param ci Info to be labeled
     * @return the info formatted as a label
     */
    @Override
    public String transform(Object o) {
        if (o instanceof CommitInfo) {
            return o.toString();
        }
        return "";
    }
}
