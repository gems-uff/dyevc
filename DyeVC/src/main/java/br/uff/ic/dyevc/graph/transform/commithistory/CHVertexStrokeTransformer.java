package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BasicStroke;
import java.awt.Stroke;

import java.util.List;
import java.util.Map;

/**
 * Transforms a CommitInfo returning the stroke to be used in vertices. Vertices that are heads of any branches are
 * painted with a different stroke.
 * @author         Cristiano Cesario
 */
public final class CHVertexStrokeTransformer implements Transformer<Object, Stroke> {
    private final Map<String, List<String>> commitToBranchNamesMap;
    private final Stroke                    medium = new BasicStroke(3);
    private final Stroke                    light  = new BasicStroke(1);

    /**
     * Constructs an instance of this transformer
     *
     * @param commitMap Map of commits to branch names that point to them.
     */
    public CHVertexStrokeTransformer(Map<String, List<String>> commitMap) {
        super();
        this.commitToBranchNamesMap = commitMap;
    }

    @Override
    public Stroke transform(Object o) {
        if (o instanceof CommitInfo) {
            if (commitToBranchNamesMap.containsKey(((CommitInfo)o).getHash())) {
                return medium;
            }
        }

        return light;
    }
}
