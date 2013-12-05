package br.uff.ic.dyevc.graph.transform.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.topology.CloneRelationship;

import org.apache.commons.collections15.Transformer;

/**
 * Transforms a relationship between two clones, printing the number of commits that are ahead or behind between them.
 *
 * @author Cristiano
 */
public class TopologyEdgeLabelTransformer implements Transformer<Object, String> {
    /**
     * Returns a string telling how many commits are ahead / behind the two vertex of the relationship
     *
     * @param o Edge to be labeled
     * @return the edge formatted as a label
     */
    @Override
    public String transform(Object o) {
        if (o instanceof CloneRelationship) {
            CloneRelationship relation = (CloneRelationship)o;

            return Integer.toString(relation.getNonSyncCommitsCount());
        }

        return "";
    }
}
