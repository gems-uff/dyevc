package br.uff.ic.dyevc.graph.transform.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.graph.transform.commithistory.*;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.PushRelationship;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.utils.DateUtil;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.Set;

/**
 * Transforms a relationship between two clones, printing the number of commits that are ahead or behind between them.
 *
 * @author Cristiano
 */
public class TopologyEdgeLabelTransformer implements Transformer<Object, String> {
    private final Set<CommitInfo>       cis;
    private final Predicate<CommitInfo> diffBetweenReps;
    private CloneRelationship           relation;

    /**
     * Constructs ...
     *
     * @param cis
     */
    public TopologyEdgeLabelTransformer(final Set<CommitInfo> cis) {
        super();
        this.cis        = cis;
        diffBetweenReps = new Predicate<CommitInfo>() {
            @Override
            public boolean evaluate(CommitInfo ci) {
                return ci.getFoundIn().contains(relation.getOrigin().getId())
                       &&!ci.getFoundIn().contains(relation.getDestination().getId());
            }
        };
    }

    /**
     * Returns a string telling how many commits are ahead / behind the two vertex of the relationship
     *
     * @param o Edge to be labeled
     * @return the edge formatted as a label
     */
    @Override
    public String transform(Object o) {
        if (o instanceof CloneRelationship) {
            relation = (CloneRelationship)o;

            int result = CollectionUtils.select(cis, diffBetweenReps).size();

            return Integer.toString(result);
        }

        return "";
    }
}
