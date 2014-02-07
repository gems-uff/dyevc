package br.uff.ic.dyevc.graph.transform.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.topology.RepositoryInfo;

import org.apache.commons.collections15.Transformer;

//~--- JDK imports ------------------------------------------------------------

import javax.swing.Icon;

/**
 * Maps a repository info characteristic with its icon
 *
 * @author Cristiano Cesario
 *
 */
public class TopologyVertexIconTransformer implements Transformer<RepositoryInfo, Icon> {
    private final String callerId;

    /**
     * Constructs an instance of this transformer
     *
     * @param callerId
     */
    public TopologyVertexIconTransformer(String callerId) {
        this.callerId = callerId;
    }

    /**
     * Returns the icon corresponding to v
     * @return Icon associated with the type of repository that
     * @param v the repository to which return the corresponding icon
     */
    @Override
    public Icon transform(RepositoryInfo v) {
        return TopologyIconFactory.getIcon(v, callerId);
    }
}
