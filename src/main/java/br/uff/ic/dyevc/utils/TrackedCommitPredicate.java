package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.CommitInfo;

import org.apache.commons.collections15.Predicate;

/**
 * Predicate to filter commits that are tracked.
 * @author Cristiano Cesario
 */
public class TrackedCommitPredicate implements Predicate<CommitInfo> {
    @Override
    public boolean evaluate(CommitInfo ci) {
        return ci.isTracked();
    }
}
