package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CollapsedCommitInfo;
import br.uff.ic.dyevc.model.CommitInfo;

import edu.uci.ics.jung.graph.Graph;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Map;

/**
 * Transforms a object from the commit history graph in a string to be used as the label of a vertex.
 *
 * @author Cristiano
 */
public final class CHVertexLabelTransformer implements Transformer<Object, String> {
    /**
     * <p>Returns a string to be used as a label.</p>
     * <p>If the object is a CommitInfo, returns the toString output as its label.</p>
     * <p>If the object is a Graph, returns the number of contained nodes as its label.</p>
     *
     * @param o object to be labeled
     * @return the object formatted as a label
     */
    @Override
    public String transform(Object o) {
        if (o instanceof CommitInfo) {
            return o.toString();
        }

        if (o instanceof Graph) {
            return Integer.toString(getNodecount((Graph)o));
        }
        
        if (o instanceof CollapsedCommitInfo) {
            return o.toString();
        }

        return "";
    }

    private int getNodecount(Graph graph) {
        int total = 0;
        for (Object o : graph.getVertices()) {
            if (o instanceof Graph) {
                total += getNodecount((Graph)o);
            } else {
                total++;
            }
        }

        return total;
    }
}
