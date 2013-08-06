package br.uff.ic.dyevc.model.topology;

/**
 * Relates two <a href="CloneInfo">CloneInfo</a> objects, origin and destination, with 
 * each other.
 * 
 * @author Cristiano
 */
public class CloneRelationship {

    private CloneInfo origin;
    private CloneInfo destination;

    public CloneRelationship(CloneInfo origin, CloneInfo destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public CloneInfo getOrigin() {
        return origin;
    }

    public CloneInfo getDestination() {
        return destination;
    }
}
