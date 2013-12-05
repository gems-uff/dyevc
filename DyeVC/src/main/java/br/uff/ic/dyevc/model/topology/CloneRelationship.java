package br.uff.ic.dyevc.model.topology;

/**
 * Relates two <a href="CloneInfo">CloneInfo</a> objects, origin and destination, with
 * each other.
 *
 * @author Cristiano
 */
public class CloneRelationship {
    private RepositoryInfo origin;
    private RepositoryInfo destination;
    private int            nonSyncCommitsCount;

    public int getNonSyncCommitsCount() {
        return nonSyncCommitsCount;
    }

    public void setNonSyncCommitsCount(int nonSyncCommitsCount) {
        this.nonSyncCommitsCount = nonSyncCommitsCount;
    }

    /**
     * Constructs an object of this class.
     *
     * @param origin
     * @param destination
     */
    public CloneRelationship(RepositoryInfo origin, RepositoryInfo destination) {
        this.origin      = origin;
        this.destination = destination;
    }

    public RepositoryInfo getOrigin() {
        return origin;
    }

    public RepositoryInfo getDestination() {
        return destination;
    }
}
