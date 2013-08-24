package br.uff.ic.dyevc.graph.transform.topology;

import br.uff.ic.dyevc.model.topology.RepositoryInfo;

import javax.swing.Icon;

import org.apache.commons.collections15.Transformer;

/**
 * Maps a repository info characteristic with its icon
 *
 * @author Cristiano Cesario
 *
 */
public class TopologyVertexIconTransformer implements Transformer<RepositoryInfo, Icon> {

    private String callerId;

    public TopologyVertexIconTransformer(String callerId) {
        this.callerId = callerId;
    }

    /**
     * Returns the
     * <code>Icon</code> associated with the type of repository that 
     * <code>v</code> represents
     */
    @Override
    public Icon transform(RepositoryInfo v) {
        return TopologyIconFactory.getIcon(v, callerId);
    }
}
