package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.graph.GraphBuilder;
import br.uff.ic.dyevc.model.CommitInfo;

import org.apache.commons.collections15.Predicate;

/**
 * Predicate to filter commits found in the repository with id = originId but not found in the repository with
 * id = destinationId.
 * @author Cristiano Cesario
 */
public class DiffBetweenReps implements Predicate<CommitInfo> {
    String originId      = null;
    String destinationId = null;

    @Override
    public boolean evaluate(CommitInfo ci) {
        return ci.getFoundIn().contains(originId) &&!ci.getFoundIn().contains(destinationId);
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getOriginId() {
        return originId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationId() {
        return destinationId;
    }
}
