package br.uff.ic.dyevc.graph.transform.commithistory;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;

import edu.uci.ics.jung.graph.Graph;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Paint;

import java.util.Iterator;
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
     * <p>
     * If vertex exists in all related repositories, it is painted in white.</p>
     * <p>
     * If vertex exists locally but do not exists in any push list, it is painted in red.</p>
     * <p>
     * If vertex exists in any pull list but not locally, it is painted in green.</p>
     * <p>
     * Finally, if vertex exists in a node not related to the local one, it is painted gray.</p>
     * <p>
     * For collapsed nodes, color is chosen according to the contained nodes, following same rule
     * (mixed collapsed nodes are painted in magenta.</p>
     *
     * @param o the Object to be transformed. It can be either a Graph with collapsed nodes or a CommitInfo
     * @return the color to be used to paint the vertex
     */
    @Override
    public Paint transform(Object o) {
        Paint paint = IConstants.COLOR_COLLAPSED;
        if (o instanceof Graph) {
            return getColor(getType((Graph)o));
        }

        if (o instanceof CommitInfo) {
            CommitInfo ci = (CommitInfo)o;
            if (ci.getType() != IConstants.COMMIT_MASK_NOT_SET) {
                return getColor(ci.getType());
            }

            boolean allHave = !(notInLocalRepositoryListMap.containsKey(ci.getHash())
                                || notInPushListMap.containsKey(ci.getHash()));

            boolean iHavePushDoesnt = !notInLocalRepositoryListMap.containsKey(ci.getHash())
                                      && notInPushListMap.containsKey(ci.getHash());

            boolean iDontHaveSomePullHas = notInLocalRepositoryListMap.containsKey(ci.getHash())
                                           &&!notInPullListMap.containsKey(ci.getHash());

            boolean noOneKnownHas = notInLocalRepositoryListMap.containsKey(ci.getHash())
                                    && notInPushListMap.containsKey(ci.getHash())
                                    && notInPullListMap.containsKey(ci.getHash());

            if (!ci.isTracked()) {
                ci.setType(IConstants.COMMIT_MASK_NOT_TRACKED);

                return getColor(ci.getType());
            }

            if (allHave) {
                ci.setType(IConstants.COMMIT_MASK_ALL_HAVE);

                return getColor(ci.getType());
            }

            if (iHavePushDoesnt) {
                ci.setType(IConstants.COMMIT_MASK_I_HAVE_PUSH_DONT);

                return getColor(ci.getType());
            }

            if (iDontHaveSomePullHas) {
                ci.setType(IConstants.COMMIT_MASK_I_DONT_PULL_HAS);

                return getColor(ci.getType());
            }

            if (noOneKnownHas) {
                ci.setType(IConstants.COMMIT_MASK_NON_RELATED_HAS);

                return getColor(ci.getType());
            }
        }

        return paint;
    }

    /**
     * Return the type of a graph, according to the type of contained commits. If all contained commits are of the
     * same type, returns that type. If at least one of the contained commits is of a different type, returns a generic
     * collapsed type. This function is recursive, because a collapsed node may contain other collapsed nodes.
     * @param g The graph for which the type is desired.
     * @return The type of the graph.
     */
    private byte getType(Graph g) {
        byte     result = IConstants.COMMIT_MASK_COLLAPSED;
        Iterator it     = g.getVertices().iterator();
        Object   first  = it.next();
        byte     type;
        if (first instanceof Graph) {
            type = getType((Graph)first);
        }

        if (first instanceof CommitInfo) {
            type = ((CommitInfo)first).getType();

            while (it.hasNext()) {
                Object next = it.next();
                byte   typeNext;
                if (next instanceof CommitInfo) {
                    typeNext = ((CommitInfo)next).getType();
                } else {
                    typeNext = getType((Graph)next);
                }

                if (typeNext != type) {
                    return result;
                }
            }

            return type;
        }

        return result;
    }

    /**
     * Gets the color according to the mask informed.
     * @param colorMask the Mask for which a color is desired.
     * @return The color that matches the specified mask.
     */
    private Paint getColor(byte colorMask) {
        Paint result = IConstants.COLOR_COLLAPSED;
        switch (colorMask) {
        case IConstants.COMMIT_MASK_NOT_TRACKED :
            result = IConstants.TOPOLOGY_COLOR_NOT_TRACKED;

            break;

        case IConstants.COMMIT_MASK_ALL_HAVE :
            result = IConstants.TOPOLOGY_COLOR_ALL_HAVE;

            break;

        case IConstants.COMMIT_MASK_I_HAVE_PUSH_DONT :
            result = IConstants.TOPOLOGY_COLOR_I_HAVE_PUSH_DONT;

            break;

        case IConstants.COMMIT_MASK_I_DONT_PULL_HAS :
            result = IConstants.TOPOLOGY_COLOR_I_DONT_PULL_HAS;

            break;

        case IConstants.COMMIT_MASK_NON_RELATED_HAS :
            result = IConstants.TOPOLOGY_COLOR_NON_RELATED_HAS;

            break;
        }

        return result;
    }
}
