package br.uff.ic.dyevc.model.topology;

/**
 *
 * @author Cristiano
 */
public class PushRelationship extends CloneRelationship {
    public PushRelationship(CloneInfo origin, CloneInfo destination) {
        super(origin, destination);
    }

    @Override
    public String toString() {
        return getOrigin().getCloneName() + " -- push --> " + getDestination().getCloneName();
    }
}
