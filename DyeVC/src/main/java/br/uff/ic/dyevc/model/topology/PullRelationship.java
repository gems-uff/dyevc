package br.uff.ic.dyevc.model.topology;

/**
 *
 * @author Cristiano
 */
public class PullRelationship extends CloneRelationship {
    public PullRelationship(CloneInfo origin, CloneInfo destination) {
        super(origin, destination);
    }

    @Override
    public String toString() {
        return getDestination().getCloneName() + " <-- pull -- " + getOrigin().getCloneName();
    }
}
