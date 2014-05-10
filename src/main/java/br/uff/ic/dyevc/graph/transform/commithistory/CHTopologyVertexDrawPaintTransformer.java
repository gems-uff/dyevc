package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.model.CommitInfo;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Paint;

import java.util.List;
import java.util.Map;

/**
 * Transformer to paint the lines around vertices in a commit history graph, depending upon existing tags or branches
 * pointing to it.
 *
 * @author Cristiano
 */
public class CHTopologyVertexDrawPaintTransformer implements Transformer<Object, Paint> {
    private final Map<String, List<String>> commitToBranchNamesMap;
    private final Map<String, List<String>> commitToTagNamesMap;

    /**
     * Constructs an instance of this transformer
     *
     * @param commitMap Map of commits to branch names that point to them.
     * @param tagMap Map of commits to tag names that point to them.
     */
    public CHTopologyVertexDrawPaintTransformer(Map<String, List<String>> commitMap, Map<String, List<String>> tagMap) {
        super();
        this.commitToBranchNamesMap = commitMap;
        this.commitToTagNamesMap    = tagMap;
    }

    /**
     * Paints according to the existence of branches or tags pointing to it.
     *
     * @param o the Object to be transformed. It can be either a Graph with collapsed nodes or a CommitInfo
     * @return the color to be used to paint the vertex
     */
    @Override
    public Paint transform(Object o) {
        Paint paint = Color.BLACK;
        if (o instanceof CommitInfo) {
            if (commitToTagNamesMap.containsKey(((CommitInfo)o).getHash())) {
                return IConstants.TOPOLOGY_STROKE_COLOR_REFERENCED_BY_TAG;
            } else if (commitToBranchNamesMap.containsKey(((CommitInfo)o).getHash())) {
                return IConstants.TOPOLOGY_STROKE_COLOR_REFERENCED_BY_BRANCH;
            }
        }

        return paint;
    }
}
