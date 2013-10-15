package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.model.CommitInfo;

import edu.uci.ics.jung.graph.Graph;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Paint;

/**
 * Transformer to paint vertices in a commit history graph according to its type (initial, split, merge, etc.)
 *
 * @author Cristiano
 */
public class CHSplitMergeVertexPaintTransformer implements Transformer<Object, Paint> {
    /**
     * Paints vertex. Default is cyan. If vertex splits in various children,
     * paints it in red. If vertex is a merge, paints it in green. If vertex is
     * both a merge and another split, paints it in yellow.
     */
    @Override
    public Paint transform(Object o) {
        Paint paint = IConstants.COLOR_COLLAPSED;
        if (o instanceof Graph) {
            String text = ((Graph)o).toString();

            return paint;
        }

        if (o instanceof CommitInfo) {
            CommitInfo ci       = (CommitInfo)o;
            int        children = ci.getChildrenCount();
            int        parents  = ci.getParentsCount();
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
        }

        return paint;
    }
}
