package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;

import edu.uci.ics.jung.graph.Graph;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Paint;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Transformer to paint vertices in a commit history graph, according to its presence in local and related repositories.
 *
 * @author Cristiano
 */
public class CHTopologyVertexPaintTransformer implements Transformer<Object, Paint> {
    private final RepositoryInfo info;

    /**
     * Map of commits not found in any of the repositories that {@link #rep} pushes from.
     */
    Map<String, CommitInfo> notInPushListMap;

    /**
     * Map of commits not found in any of the repositories that {@link #rep} pulls to.
     */
    Map<String, CommitInfo> notInPullListMap;

    /**
     * Map of commits not found locally in {@link #rep}.
     */
    Map<String, CommitInfo> notInLocalRepositoryListMap;

    /**
     * Constructs a transformer.
     *
     * @param info RepositoryInfo to use to check related repositories.
     * @param notInPushList the set of commits that do not exist in any repositories in the push list.
     * @param notInPullList the set of commits that do not exist in any repositories in the pull list.
     * @param notInRepList the set of commits that do not exist locally.
     */
    public CHTopologyVertexPaintTransformer(RepositoryInfo info, final Set<CommitInfo> notInPushList,
            final Set<CommitInfo> notInPullList, final Set<CommitInfo> notInRepList) {
        this.info                   = info;
        notInPushListMap            = new TreeMap<String, CommitInfo>();
        notInPullListMap            = new TreeMap<String, CommitInfo>();
        notInLocalRepositoryListMap = new TreeMap<String, CommitInfo>();

        for (CommitInfo ci : notInPushList) {
            notInPushListMap.put(ci.getHash(), ci);
        }

        for (CommitInfo ci : notInPullList) {
            notInPullListMap.put(ci.getHash(), ci);
        }

        for (CommitInfo ci : notInRepList) {
            notInLocalRepositoryListMap.put(ci.getHash(), ci);
        }
    }

    /**
     * Paints according to its presence in local and related repositories.
     * <p>If vertex exists in all related repositories, it is painted in white.
     * <p>If vertex exists locally but do not exists in any push list, it is painted in red.
     * <p>If vertex exists in any pull list but not locally, it is painted in green.
     * <p>Finally, if vertex exists in a node not related to the local one, it is painted gray.
     * @param o the Object to be transformed. It can be either a Graph with collapsed nodes or a CommitInfo
     * @return the color to be used to paint the vertex
     */
    @Override
    public Paint transform(Object o) {
        Paint paint = IConstants.COLOR_COLLAPSED;
        if (o instanceof Graph) {
            return paint;
        }

        if (o instanceof CommitInfo) {
            CommitInfo ci      = (CommitInfo)o;
            boolean    allHave = !(notInLocalRepositoryListMap.containsKey(ci.getHash())
                                   || notInPushListMap.containsKey(ci.getHash()));

            boolean iHavePushDoesnt = !notInLocalRepositoryListMap.containsKey(ci.getHash())
                                      && notInPushListMap.containsKey(ci.getHash());

            boolean iDontHaveSomePullHas = notInLocalRepositoryListMap.containsKey(ci.getHash())
                                           &&!notInPullListMap.containsKey(ci.getHash());

            boolean noOneKnownHas = notInLocalRepositoryListMap.containsKey(ci.getHash())
                                    && notInPushListMap.containsKey(ci.getHash())
                                    && notInPullListMap.containsKey(ci.getHash());

            if (!ci.isTracked()) {
                return IConstants.TOPOLOGY_COLOR_NOT_TRACKED;
            }

            if (allHave) {
                return IConstants.TOPOLOGY_COLOR_ALL_HAVE;
            }

            if (iHavePushDoesnt) {
                return IConstants.TOPOLOGY_COLOR_I_HAVE_PUSH_DONT;
            }

            if (iDontHaveSomePullHas) {
                return IConstants.TOPOLOGY_COLOR_I_DONT_PULL_HAS;
            }

            if (noOneKnownHas) {
                return IConstants.TOPOLOGY_COLOR_NON_RELATED_HAS;
            }
        }

        return paint;
    }
}
